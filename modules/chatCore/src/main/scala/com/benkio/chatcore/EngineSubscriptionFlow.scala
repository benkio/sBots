package com.benkio.chatcore

import cats.effect.Async
import cats.syntax.all.*
import com.benkio.chatcore.config.SBotConfig
import com.benkio.chatcore.model.reply.ReplyValue
import com.benkio.chatcore.model.CommandKey
import com.benkio.chatcore.model.Message
import com.benkio.chatcore.model.SubscriptionFlowAction
import com.benkio.chatcore.model.SubscriptionFlowKey
import com.benkio.chatcore.patterns.SubscriptionCommands.SubscribeUnsubscribeCommand
import com.benkio.chatcore.repository.db.DBLayer

/** The guided `/subscribe` flow of the [[BotEngine]], on top of the [[SubscriptionFlowService]]: owns its drafts here,
  * the adapters only render its prompts and actions. Used by the commands (its start), the reactive messages (the plain
  * answers of the flow) and the adapters (the press on a prompt, through [[BotEngine.onSubscriptionFlowAction]]).
  *
  * It decides whether a platform can run the flow at all ([[requester]]) and delivers every outcome through the
  * [[EngineDelivery]], keeping the prompt handle of the adapter up to date.
  */
private[chatcore] trait EngineSubscriptionFlow[F[_]] {

  /** The owner of a guided flow, if the platform can run one for the message. */
  def requester(msg: Message, platform: PlatformAdapter[F]): Option[String]

  /** Whether the message is the `/subscribe` command with no arguments. */
  def isBareSubscribeCommand(msg: Message, platform: PlatformAdapter[F]): Boolean

  /** Starts the flow of the requester. */
  def start(msg: Message, requester: String)(using platform: PlatformAdapter[F]): F[Unit]

  /** Whether the flow took the plain message as an answer. */
  def acceptPlainMessage(msg: Message, requester: String)(using platform: PlatformAdapter[F]): F[Boolean]

  /** The press on a prompt of the flow. */
  def submit(msg: Message, action: SubscriptionFlowAction, requester: String)(using
      platform: PlatformAdapter[F]
  ): F[Unit]
}

/** The [[EngineSubscriptionFlow]] on top of the [[SubscriptionFlowService]]. */
private[chatcore] class DefaultEngineSubscriptionFlow[F[_]: Async](
    sBotConfig: SBotConfig,
    dbLayer: DBLayer[F],
    backgroundJobManager: BackgroundJobManager[F],
    delivery: EngineDelivery[F]
) extends EngineSubscriptionFlow[F] {

  private val service = new SubscriptionFlowService[F](
    store = SubscriptionDraftStore.db[F](dbLayer.dbSubscriptionDraft),
    sBotInfo = sBotConfig.sBotInfo,
    expiry = sBotConfig.subscriptionDraftExpiry,
    ttl = sBotConfig.messageTimeToLive,
    createSubscription = { (message, botInfo, arguments) =>
      SubscribeUnsubscribeCommand.createSubscriptionReply[F](
        backgroundJobManager = backgroundJobManager,
        dbMessageReplies = dbLayer.dbMessageReplies,
        m = message,
        sBotInfo = botInfo,
        arguments = arguments
      )
    }
  )

  /** A guided flow needs an owner, buttons and in-place edits: Telegram and Discord only. */
  def requester(msg: Message, platform: PlatformAdapter[F]): Option[String] =
    Option
      .when(
        platform.capabilities.suppliesSenderId &&
          platform.capabilities.supportsEdit &&
          platform.capabilities.supportsButtons
      )(msg.authorId)
      .flatten

  def isBareSubscribeCommand(msg: Message, platform: PlatformAdapter[F]): Boolean =
    msg.text.exists { text =>
      val (word, rest) = text.trim.span(_ != ' ')
      platform.capabilities.commandSyntax
        .isCommandWord(word, CommandKey.Subscribe.asString, sBotConfig.sBotInfo.botName) && rest.trim.isEmpty
    }

  def start(msg: Message, requester: String)(using platform: PlatformAdapter[F]): F[Unit] =
    service.start(msg, requester, deliverFlow(_, msg, requester)).void

  /** Whether the flow took the plain message as an answer. */
  def acceptPlainMessage(msg: Message, requester: String)(using platform: PlatformAdapter[F]): F[Boolean] =
    service.acceptPlainMessage(msg, requester, deliverFlow(_, msg, requester)).map(_.isDefined)

  /** The entry point the adapters use for a press on a guided-flow prompt. */
  def submit(msg: Message, action: SubscriptionFlowAction, requester: String)(using
      platform: PlatformAdapter[F]
  ): F[Unit] =
    service.submit(msg, action, requester, deliverFlow(_, msg, requester)).void

  /** Called by the flow service while it still holds the lock of the draft, so replies cannot overtake each other. A
    * prompt answer refreshes the adapter's prompt handle; a terminal message clears it so the next flow sends a fresh
    * prompt instead of editing a stale one.
    */
  private def deliverFlow(outcome: SubscriptionFlowOutcome, msg: Message, requester: String)(using
      platform: PlatformAdapter[F]
  ): F[Unit] = {
    val key                              = SubscriptionFlowKey(msg.chatId, sBotConfig.sBotInfo.botId, requester)
    val (replyValue, updatePromptHandle) = outcome match {
      case SubscriptionFlowOutcome.Prompt(value)            => (value: ReplyValue, platform.touchFlowPrompt(key))
      case SubscriptionFlowOutcome.Message(value, terminal) =>
        (value: ReplyValue, if terminal then platform.clearFlowPrompt(key) else platform.touchFlowPrompt(key))
    }
    updatePromptHandle *> delivery.deliver(replyValue = replyValue, msg = msg, replyToMessage = false)
  }
}
