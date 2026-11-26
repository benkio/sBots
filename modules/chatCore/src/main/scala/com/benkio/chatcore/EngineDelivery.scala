package com.benkio.chatcore

import cats.effect.Async
import cats.syntax.all.*
import com.benkio.chatcore.model.reply.EffectfulReply
import com.benkio.chatcore.model.reply.PlainReply
import com.benkio.chatcore.model.reply.Reply
import com.benkio.chatcore.model.reply.ReplyValue
import com.benkio.chatcore.model.Message
import com.benkio.chatcore.repository.db.DBLayer

/** Shared by every section of the [[BotEngine]] to answer: the only place that sends something to the platform.
  *
  * [[reply]] computes a [[Reply]] into the value to send (a plain one here, an effectful one by the platform),
  * remembers it for the text mode paging and delivers it. [[deliver]] sends a value that is already computed, adapted
  * to what the platform supports (see the ReplyAdaptation).
  *
  * @param dbLayer
  *   where the adaptation looks up the media the platform cannot send
  * @param textMode
  *   remembers the sent values, so the text commands of the platforms without buttons can page them again
  */
private[chatcore] trait EngineDelivery[F[_]] {

  /** Computes the reply, remembers it for the text mode paging and delivers it. */
  def reply(reply: Reply, msg: Message)(using platform: PlatformAdapter[F]): F[Unit]

  /** Sends a value that is already computed, adapted to what the platform supports. */
  def deliver(replyValue: ReplyValue, msg: Message, replyToMessage: Boolean)(using
      platform: PlatformAdapter[F]
  ): F[Unit]
}

/** The [[EngineDelivery]] on top of the DB (the media to adapt) and the text mode paging. */
private[chatcore] class DefaultEngineDelivery[F[_]: Async](dbLayer: DBLayer[F], textMode: PagedListTextMode[F])
    extends EngineDelivery[F] {

  def reply(reply: Reply, msg: Message)(using platform: PlatformAdapter[F]): F[Unit] =
    for {
      replyValue <- reply match {
        case plainReply: PlainReply         => BotEngine.runPlainReply[F](plainReply, platform.capabilities)
        case effectfulReply: EffectfulReply => platform.runEffectful(effectfulReply, msg)
      }
      _ <- textMode.remember(request = msg, replyValue = replyValue)
      _ <- deliver(replyValue = replyValue, msg = msg, replyToMessage = reply.replyToMessage)
    } yield ()

  // What the platform can send: what is not supported is adapted to it, see the ReplyTransformer
  def deliver(replyValue: ReplyValue, msg: Message, replyToMessage: Boolean)(using
      platform: PlatformAdapter[F]
  ): F[Unit] =
    ReplyAdaptation
      .adaptToPlatform(dbLayer, platform.capabilities)(replyValue)
      .flatMap(platform.sendAll(_, msg, replyToMessage))
}
