package com.benkio.chatcore

import cats.effect.Async
import cats.syntax.all.*
import com.benkio.chatcore.config.SBotConfig
import com.benkio.chatcore.model.reply.ReplyBundleMessage
import com.benkio.chatcore.model.Message
import com.benkio.chatcore.repository.db.DBLayer
import log.effect.LogWriter

/** What the reactive logic made of a message: it decided (a reply was sent or not, e.g. a bundle blocked by the chat
  * timeout), or nothing matched and the LLM may answer.
  */
private[chatcore] enum Reactive {
  case Done(replied: Boolean)
  case Unmatched
}

extension (r: Reactive) def replied: Boolean = r match {
  case Reactive.Done(replied) => replied
  case Reactive.Unmatched     => false
}

/** The reactive messages section of the [[BotEngine]]: what the bot answers to a plain message by itself. A message is
  * first offered to the guided flow, when the platform supports it and the sender has a draft open, then to the reply
  * bundle matching its text, which is sent only if the filters let it through. A message nothing answers is
  * [[Reactive.Unmatched]], the only case left to the LLM.
  *
  * @param dbLayer
  *   where the message reply bundles are loaded from
  * @param subscriptionFlow
  *   takes the message as an answer of an open guided flow
  * @param delivery
  *   sends the reply
  * @param filteringMatchesMessages
  *   whether the reply bundle selected for a message can be used, e.g. not while the chat is in timeout
  */
private[chatcore] trait EngineReactiveMessages[F[_]] {

  def messageRepliesData: List[ReplyBundleMessage]

  /** Answers the message if it can, see [[Reactive]]. */
  def logicReplied(msg: Message)(using platform: PlatformAdapter[F]): F[Reactive]
}

/** The [[EngineReactiveMessages]] that selects the reply bundle from the DB data. */
private[chatcore] class DefaultEngineReactiveMessages[F[_]: Async: LogWriter](
    sBotConfig: SBotConfig,
    dbLayer: DBLayer[F],
    filteringMatchesMessages: (ReplyBundleMessage, Message) => F[Boolean],
    subscriptionFlow: EngineSubscriptionFlow[F],
    delivery: EngineDelivery[F]
) extends EngineReactiveMessages[F] {

  // Loaded once with the setup of the bot and served from memory by the DBLayer
  def messageRepliesData: List[ReplyBundleMessage] = dbLayer.dbMessageReplies.messageReplies

  /** Nothing matching leaves the message [[Reactive.Unmatched]], the only case the LLM can still answer. */
  def logicReplied(msg: Message)(using platform: PlatformAdapter[F]): F[Reactive] =
    subscriptionFlow.requester(msg, platform).fold(replyBundles(msg)) { requester =>
      subscriptionFlow
        .acceptPlainMessage(msg, requester)
        .ifM(
          (Reactive.Done(true): Reactive).pure[F],
          replyBundles(msg)
        )
    }

  private def replyBundles(msg: Message)(using platform: PlatformAdapter[F]): F[Reactive] =
    ReplyBundleMessage
      .selectReplyBundle(
        msg = msg,
        messageRepliesData = messageRepliesData,
        ignoreMessagePrefix = sBotConfig.ignoreMessagePrefix,
        disableForward = sBotConfig.disableForward
      )
      .traverse(replyBundleLogic(_, msg))
      .map(_.fold[Reactive](Reactive.Unmatched)(Reactive.Done(_)))

  /** @return whether the reply bundle was sent */
  private def replyBundleLogic(replyBundle: ReplyBundleMessage, msg: Message)(using
      platform: PlatformAdapter[F]
  ): F[Boolean] =
    for {
      _ <- LogWriter
        .info(s"Computing message ${msg.text} matching message reply bundle triggers: ${replyBundle.trigger} ")
      passesFilters <- filteringMatchesMessages(replyBundle, msg)
      _             <- delivery.reply(replyBundle.reply, msg).whenA(passesFilters)
    } yield passesFilters
}
