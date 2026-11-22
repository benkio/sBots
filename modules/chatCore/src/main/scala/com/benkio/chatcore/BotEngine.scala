package com.benkio.chatcore

import cats.effect.Async
import cats.effect.Clock
import cats.effect.Sync
import cats.syntax.all.*
import com.benkio.chatcore.config.SBotConfig
import com.benkio.chatcore.messagefiltering.*
import com.benkio.chatcore.model.reply.EffectfulReply
import com.benkio.chatcore.model.reply.MarkdownFile
import com.benkio.chatcore.model.reply.MediaFile
import com.benkio.chatcore.model.reply.MediaReply
import com.benkio.chatcore.model.reply.PlainReply
import com.benkio.chatcore.model.reply.Reply
import com.benkio.chatcore.model.reply.ReplyBundleCommand
import com.benkio.chatcore.model.reply.ReplyBundleMessage
import com.benkio.chatcore.model.reply.ReplyValue
import com.benkio.chatcore.model.reply.TextReply
import com.benkio.chatcore.model.CommandKey
import com.benkio.chatcore.model.Message
import com.benkio.chatcore.model.MessageType
import com.benkio.chatcore.model.PlatformCapabilities
import com.benkio.chatcore.model.SubscriptionFlowAction
import com.benkio.chatcore.model.SubscriptionFlowKey
import com.benkio.chatcore.patterns.CommandPatterns.InstructionsCommand
import com.benkio.chatcore.patterns.CommandPatternsGroup
import com.benkio.chatcore.patterns.PostComputationPatterns
import com.benkio.chatcore.patterns.SubscriptionCommands.SubscribeUnsubscribeCommand
import com.benkio.chatcore.repository.db.DBLayer
import com.benkio.chatcore.SubscriptionFlowOutcome
import com.benkio.chatcore.SubscriptionFlowService
import com.benkio.chatcore.UsageStats.Outcome
import log.effect.LogWriter

import scala.concurrent.duration.FiniteDuration

/** The logic of a bot, whatever the platform: what to reply to a message, a command or a file request. The platform is
  * reached through the [[PlatformAdapter]] only: to send and compute the replies, and for the way it writes the
  * commands (see [[com.benkio.chatcore.model.CommandSyntax]]).
  *
  * @param filteringMatchesMessages
  *   whether the reply bundle selected for a message can be used, e.g. not while the chat is in timeout
  * @param postComputation
  *   what to do after a message is handled, e.g. logging the last interaction of the chat
  * @param notifier
  *   tells the operators about the requests that time out and the checks that fail
  */
class BotEngine[F[_]: Async: LogWriter](
    val sBotConfig: SBotConfig,
    dbLayer: DBLayer[F],
    filteringMatchesMessages: (ReplyBundleMessage, Message) => F[Boolean],
    postComputation: Message => F[Unit],
    notifier: Notifier[F],
    backgroundJobManager: BackgroundJobManager[F]
) {

  // Replies data ////////////////////////////////////////////////////////

  // Loaded once with the setup of the bot and served from memory by the DBLayer
  def messageRepliesData: List[ReplyBundleMessage] = dbLayer.dbMessageReplies.messageReplies
  def commandRepliesData: List[ReplyBundleCommand] = dbLayer.dbCommandReplies.commandReplies

  lazy val fixedCommands: List[ReplyBundleCommand] = {
    val commandsPattersGroup =
      CommandPatternsGroup.TriggerGroup.group(
        triggerFileUri = sBotConfig.triggerListUri,
        sBotInfo = sBotConfig.sBotInfo,
        messageRepliesData = messageRepliesData,
        ignoreMessagePrefix = sBotConfig.ignoreMessagePrefix
      )
    commandsPattersGroup :+ InstructionsCommand.instructionsReplyBundleCommand(
      sBotInfo = sBotConfig.sBotInfo,
      instructionsUri = sBotConfig.instructionsUri
    )
  }

  lazy val allCommandRepliesData: List[ReplyBundleCommand] =
    commandRepliesData ++ fixedCommands

  /** The text-mode paging of platforms without buttons owns its remembered pages; the orchestrator holds no state. */
  private lazy val textMode = new PagedListTextMode[F](
    botName = sBotConfig.sBotInfo.botName,
    allCommandRepliesData = () => allCommandRepliesData,
    dbLayer = dbLayer
  )

  /** The guided `/subscribe` flow owns its drafts here; the adapters only render its prompts and actions. */
  private lazy val subscriptionFlow = new SubscriptionFlowService[F](
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

  // Bot logic ////////////////////////////////////////////////////////////

  def messageLogic(msg: Message)(using platform: PlatformAdapter[F]): F[Unit] = messageLogicReplied(msg).void

  /** Whether a reply was sent: a guided-flow answer, or a matching bundle that the filters let through. */
  private def messageLogicReplied(msg: Message)(using platform: PlatformAdapter[F]): F[Boolean] =
    guidedFlowRequester(msg, platform).fold(messageReplyBundles(msg)) { requester =>
      subscriptionFlow
        .acceptPlainMessage(msg, requester, deliverFlow(_, msg, requester))
        .flatMap(outcome => outcome.fold(messageReplyBundles(msg))(_ => true.pure[F]))
    }

  private def messageReplyBundles(msg: Message)(using platform: PlatformAdapter[F]): F[Boolean] =
    ReplyBundleMessage
      .selectReplyBundle(
        msg = msg,
        messageRepliesData = messageRepliesData,
        ignoreMessagePrefix = sBotConfig.ignoreMessagePrefix,
        disableForward = sBotConfig.disableForward
      )
      .traverse(replyBundle =>
        for {
          _ <- LogWriter
            .info(s"Computing message ${msg.text} matching message reply bundle triggers: ${replyBundle.trigger} ")
          passesFilters <- filteringMatchesMessages(replyBundle, msg)
          _             <- reply(replyBundle.reply, msg).whenA(passesFilters)
        } yield passesFilters
      )
      .map(_.getOrElse(false))

  def commandLogic(msg: Message)(using platform: PlatformAdapter[F]): F[Unit] = commandLogicReplied(msg).void

  private def commandLogicReplied(msg: Message)(using platform: PlatformAdapter[F]): F[Boolean] =
    guidedFlowRequester(msg, platform)
      .filter(_ => isBareSubscribeCommand(msg, platform))
      .fold(commandReplyBundles(msg)) { requester =>
        subscriptionFlow.start(msg, requester, deliverFlow(_, msg, requester)).as(true)
      }

  private def commandReplyBundles(msg: Message)(using platform: PlatformAdapter[F]): F[Boolean] =
    ReplyBundleCommand
      .selectCommandReplyBundle(
        msg = msg,
        allCommandRepliesData = allCommandRepliesData,
        botName = sBotConfig.sBotInfo.botName,
        syntax = platform.capabilities.commandSyntax
      )
      .traverse(commandReply =>
        LogWriter.info(
          s"${sBotConfig.sBotInfo.botName}: Computing command ${msg.text} matching command reply bundle"
        ) *> reply(commandReply.reply, msg).as(true)
      )
      .flatMap {
        case Some(replied) => replied.pure[F]
        case None          => textMode.handleCommand(msg)
      }

  /** A guided flow needs an owner, buttons and in-place edits: Telegram and Discord only. */
  private def guidedFlowRequester(msg: Message, platform: PlatformAdapter[F]): Option[String] =
    Option
      .when(
        platform.capabilities.suppliesSenderId &&
          platform.capabilities.supportsEdit &&
          platform.capabilities.supportsButtons
      )(msg.authorId)
      .flatten

  private def isBareSubscribeCommand(msg: Message, platform: PlatformAdapter[F]): Boolean =
    msg.text.exists { text =>
      val (word, rest) = text.trim.span(_ != ' ')
      platform.capabilities.commandSyntax
        .isCommandWord(word, CommandKey.Subscribe.asString, sBotConfig.sBotInfo.botName) && rest.trim.isEmpty
    }

  /** The entry point the adapters use for a press on a guided-flow prompt. */
  def onSubscriptionFlowAction(msg: Message, action: SubscriptionFlowAction, requester: String)(using
      platform: PlatformAdapter[F]
  ): F[Unit] =
    subscriptionFlow.submit(msg, action, requester, deliverFlow(_, msg, requester)).void

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
    updatePromptHandle *> deliver(replyValue = replyValue, msg = msg, replyToMessage = false)
  }

  def fileRequestLogic(msg: Message)(using platform: PlatformAdapter[F]): F[Unit] = fileRequestLogicReplied(msg).void

  private def fileRequestLogicReplied(msg: Message)(using platform: PlatformAdapter[F]): F[Boolean] =
    msg.getContent
      .traverse(content =>
        deliver(
          replyValue = MediaFile.fromString(platform.capabilities.commandSyntax.fileRequestContent(content)),
          msg = msg,
          replyToMessage = true
        ).as(true)
      )
      .map(_.getOrElse(false))

  /** Whether a reply was sent. */
  def botLogic(msg: Message)(using platform: PlatformAdapter[F]): F[Boolean] =
    msg.messageType(sBotConfig.sBotInfo.botId, platform.capabilities.commandSyntax) match {
      case MessageType.Message =>
        LogWriter.trace("Message Type: Message") >> messageLogicReplied(msg)
      case MessageType.Command =>
        LogWriter.trace("Message Type: Command") >> commandLogicReplied(msg)
      case MessageType.FileRequest =>
        LogWriter.trace("Message Type: File Request") >> fileRequestLogicReplied(msg)
    }

  /** The entry point: a message arrived. One `[stats]` line tells how it ended and how long it took. */
  def onMessage(msg: Message)(using platform: PlatformAdapter[F]): F[Unit] = {
    val process: F[Outcome] = for {
      _       <- LogWriter.trace(s"${sBotConfig.sBotInfo.botName}: A message arrived: $msg")
      outcome <- isDuplicate(msg).ifM(skipDuplicate(msg).as(Outcome.Duplicate), handleMessage(msg).as(Outcome.Handled))
    } yield outcome

    val bounded: F[Outcome] = sBotConfig.requestProcessingTimeout match {
      case Some(timeout) => withRequestTimeout(msg, timeout)(process)
      case None          => process
    }

    for {
      start   <- Clock[F].monotonic
      outcome <- bounded.onError(_ => logStats(Outcome.Failed, start))
      _       <- logStats(outcome, start)
    } yield ()
  }

  private def logStats(outcome: Outcome, start: FiniteDuration)(using platform: PlatformAdapter[F]): F[Unit] =
    Clock[F].monotonic.flatMap(end =>
      LogWriter.info(
        UsageStats.messageLine(
          bot = sBotConfig.sBotInfo.botName.toString,
          platform = platform.platform.id,
          outcome = outcome,
          latency = end - start
        )
      )
    )

  private def skipDuplicate(msg: Message): F[Unit] =
    LogWriter.info(
      s"${sBotConfig.sBotInfo.botName}: duplicate message skipped (chatId=${msg.chatId}, messageId=${msg.messageId})"
    )

  private def handleMessage(msg: Message)(using platform: PlatformAdapter[F]): F[Unit] = for {
    _       <- LogWriter.info(s"${sBotConfig.sBotInfo.botName}: A message arrived with content: ${msg.text}")
    replied <- botLogic(msg)
    // the last interaction (what the chat timeout is measured from) is logged only after a reply
    _ <- postComputation(msg).whenA(replied)
  } yield ()

  /** Whether the message was already received: the platforms can deliver the same message more than once (e.g. Telegram
    * retries an update that was not acknowledged in time). Without a message id (e.g. IRC) it can't be told, so the
    * message is processed. If the check fails, the message is processed too: better a duplicate than a missing reply.
    */
  private def isDuplicate(msg: Message): F[Boolean] =
    msg.messageId.fold(false.pure[F])(messageId =>
      dbLayer.dbProcessedMessage
        .markIfNew(msg.chatId, sBotConfig.sBotInfo.botId, messageId)
        .map(isNew => !isNew)
        .handleErrorWith(e =>
          LogWriter.error(
            s"${sBotConfig.sBotInfo.botName}: duplicate check failed, processing the message anyway: $e"
          ) *> notifier.error(
            sBotConfig.sBotInfo,
            s"duplicate check failed, the message is processed anyway (chatId=${msg.chatId.id}, messageId=$messageId)",
            e
          ) *> false.pure[F]
        )
    )

  private def withRequestTimeout(msg: Message, timeout: FiniteDuration)(
      process: F[Outcome]
  )(using platform: PlatformAdapter[F]): F[Outcome] = {
    val timedOut: F[Unit] = {
      val details =
        s"platform=${platform.capabilities.platform}, chatId=${msg.chatId.id}, messageId=${msg.messageId}, content=${msg.text}"
      LogWriter.error(
        s"${sBotConfig.sBotInfo.botName}: request processing timed out after $timeout and was dropped ($details)"
      ) *> notifier.error(
        sBotConfig.sBotInfo,
        s"request processing timed out after $timeout and was dropped ($details)",
        BotEngine.RequestTimedOut(timeout)
      )
    }
    Async[F].timeoutTo(process, timeout, timedOut.as(Outcome.TimedOut))
  }

  private def reply(reply: Reply, msg: Message)(using platform: PlatformAdapter[F]): F[Unit] =
    for {
      replyValue <- reply match {
        case plainReply: PlainReply         => BotEngine.runPlainReply[F](plainReply, platform.capabilities)
        case effectfulReply: EffectfulReply => platform.runEffectful(effectfulReply, msg)
      }
      _ <- textMode.remember(request = msg, replyValue = replyValue)
      _ <- deliver(replyValue = replyValue, msg = msg, replyToMessage = reply.replyToMessage)
    } yield ()

  // What the platform can send: what is not supported is adapted to it, see the ReplyTransformer
  private def deliver(replyValue: ReplyValue, msg: Message, replyToMessage: Boolean)(using
      platform: PlatformAdapter[F]
  ): F[Unit] =
    ReplyAdaptation
      .adaptToPlatform(dbLayer, platform.capabilities)(replyValue)
      .flatMap(platform.sendAll(_, msg, replyToMessage))
}

object BotEngine {

  final case class RequestTimedOut(timeout: FiniteDuration)
      extends Throwable(s"the request processing timed out after $timeout")

  /** The engine with the standard filtering (the timeout of the chat) and post computation (log the last interaction).
    */
  def apply[F[_]: Async: LogWriter](
      sBotConfig: SBotConfig,
      dbLayer: DBLayer[F],
      notifier: Notifier[F],
      backgroundJobManager: BackgroundJobManager[F]
  ): BotEngine[F] =
    new BotEngine[F](
      sBotConfig = sBotConfig,
      dbLayer = dbLayer,
      filteringMatchesMessages = FilteringTimeout.filter(dbLayer, sBotConfig.sBotInfo.botId),
      postComputation = PostComputationPatterns.timeoutPostComputation(
        dbTimeout = dbLayer.dbTimeout,
        sBotInfo = sBotConfig.sBotInfo,
        notifier = notifier
      ),
      notifier = notifier,
      backgroundJobManager = backgroundJobManager
    )

  /** Picks one of the values of a reply that is just data. */
  def runPlainReply[F[_]: Sync](reply: PlainReply, capabilities: PlatformCapabilities): F[ReplyValue] = reply match {
    case textReply: TextReply   => RandomSelection.select(textReply.text)
    case mediaReply: MediaReply => selectMediaFile(mediaReply.mediaFiles, capabilities)
  }

  /** The file of a media reply: on a text only platform, the markdown file (the first by file name, to be always the
    * same) when the set has one, as it is the text the platform can show; otherwise a random one.
    */
  def selectMediaFile[F[_]: Sync](mediaFiles: Iterable[MediaFile], capabilities: PlatformCapabilities): F[ReplyValue] =
    Option
      .when(capabilities.textOnly)(mediaFiles.collect { case markdown: MarkdownFile => markdown })
      .flatMap(_.toList.minByOption(_.filename))
      .fold(RandomSelection.select[F](mediaFiles))(markdown => (markdown: ReplyValue).pure[F])
}
