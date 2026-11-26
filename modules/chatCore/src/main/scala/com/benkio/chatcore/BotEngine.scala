package com.benkio.chatcore

import cats.effect.Async
import cats.effect.Clock
import cats.effect.Sync
import cats.syntax.all.*
import com.benkio.chatcore.config.SBotConfig
import com.benkio.chatcore.llm.LlmEngine
import com.benkio.chatcore.llm.LlmResponder
import com.benkio.chatcore.messagefiltering.*
import com.benkio.chatcore.model.reply.MarkdownFile
import com.benkio.chatcore.model.reply.MediaFile
import com.benkio.chatcore.model.reply.MediaReply
import com.benkio.chatcore.model.reply.PlainReply
import com.benkio.chatcore.model.reply.ReplyBundleCommand
import com.benkio.chatcore.model.reply.ReplyBundleMessage
import com.benkio.chatcore.model.reply.ReplyValue
import com.benkio.chatcore.model.reply.TextReply
import com.benkio.chatcore.model.Message
import com.benkio.chatcore.model.MessageType
import com.benkio.chatcore.model.PlatformCapabilities
import com.benkio.chatcore.model.SubscriptionFlowAction
import com.benkio.chatcore.patterns.PostComputationPatterns
import com.benkio.chatcore.repository.db.DBLayer
import com.benkio.chatcore.UsageStats.Outcome
import log.effect.LogWriter

import scala.concurrent.duration.*

/** The logic of a bot, whatever the platform: what to reply to a message, a command or a file request. The platform is
  * reached through the [[PlatformAdapter]] only: to send and compute the replies, and for the way it writes the
  * commands (see [[com.benkio.chatcore.model.CommandSyntax]]).
  *
  * The engine orchestrates the handling of a message: the duplicate check, the request timeout, the stats line and the
  * order of the sections. The sections are their own classes:
  *   - [[EngineReactiveMessages]]: the reply bundle matching a plain message, and the answers of the guided flow
  *   - [[EngineCommands]]: the commands, the start of the guided `/subscribe` flow and the file requests
  *   - [[EngineLlm]]: the LLM answer and the memory of the messages, after and outside the request timeout
  *   - [[EngineDelivery]] and [[EngineSubscriptionFlow]]: what the sections share, sending a reply and the guided flow
  *
  * @param filteringMatchesMessages
  *   whether the reply bundle selected for a message can be used, e.g. not while the chat is in timeout
  * @param postComputation
  *   what to do after a message is handled, e.g. logging the last interaction of the chat
  * @param notifier
  *   tells the operators about the requests that time out and the checks that fail
  * @param llmResponder
  *   answers the messages directed to the bot with the LLM; the `noop` default never answers
  * @param llmTimeout
  *   the time the LLM has to answer: it runs after, and outside, the request processing timeout, and an answer that
  *   comes later is dropped silently
  */
class BotEngine[F[_]: Async: LogWriter](
    val sBotConfig: SBotConfig,
    dbLayer: DBLayer[F],
    filteringMatchesMessages: (ReplyBundleMessage, Message) => F[Boolean],
    postComputation: Message => F[Unit],
    notifier: Notifier[F],
    backgroundJobManager: BackgroundJobManager[F],
    val llmResponder: LlmResponder[F],
    llmTimeout: FiniteDuration = BotEngine.defaultLlmTimeout
) {

  // Sections ///////////////////////////////////////////////////////////////
  // The reactive messages, the commands and the LLM live in their own classes (EngineReactiveMessages, EngineCommands,
  // EngineLlm); the orchestration of a message through them stays here.

  private lazy val reactiveMessages: EngineReactiveMessages[F] = new DefaultEngineReactiveMessages[F](
    sBotConfig = sBotConfig,
    dbLayer = dbLayer,
    filteringMatchesMessages = filteringMatchesMessages,
    subscriptionFlow = subscriptionFlow,
    delivery = delivery
  )

  private lazy val commandReplies: EngineCommandReplies[F] =
    new DefaultEngineCommandReplies[F](sBotConfig, dbLayer, () => messageRepliesData)

  /** The text-mode paging of platforms without buttons owns its remembered pages; the orchestrator holds no state. */
  private lazy val textMode = new PagedListTextMode[F](
    botName = sBotConfig.sBotInfo.botName,
    allCommandRepliesData = () => allCommandRepliesData,
    dbLayer = dbLayer
  )

  private lazy val delivery: EngineDelivery[F] = new DefaultEngineDelivery[F](dbLayer, textMode)

  private lazy val subscriptionFlow: EngineSubscriptionFlow[F] =
    new DefaultEngineSubscriptionFlow[F](sBotConfig, dbLayer, backgroundJobManager, delivery)

  private lazy val commands: EngineCommands[F] =
    new DefaultEngineCommands[F](sBotConfig, commandReplies, textMode, subscriptionFlow, delivery)

  private lazy val llm: EngineLlm[F] =
    new DefaultEngineLlm[F](sBotConfig, dbLayer, postComputation, llmResponder, llmTimeout, delivery)

  // Replies data ////////////////////////////////////////////////////////

  def messageRepliesData: List[ReplyBundleMessage]    = reactiveMessages.messageRepliesData
  def commandRepliesData: List[ReplyBundleCommand]    = commandReplies.commandRepliesData
  def fixedCommands: List[ReplyBundleCommand]         = commandReplies.fixedCommands
  def allCommandRepliesData: List[ReplyBundleCommand] = commandReplies.allCommandRepliesData

  // Bot logic ////////////////////////////////////////////////////////////

  def messageLogic(msg: Message)(using platform: PlatformAdapter[F]): F[Unit] =
    reactiveMessages.logicReplied(msg).void

  def commandLogic(msg: Message)(using platform: PlatformAdapter[F]): F[Unit] = commands.commandLogicReplied(msg).void

  def fileRequestLogic(msg: Message)(using platform: PlatformAdapter[F]): F[Unit] =
    commands.fileRequestLogicReplied(msg).void

  /** The entry point the adapters use for a press on a guided-flow prompt. */
  def onSubscriptionFlowAction(msg: Message, action: SubscriptionFlowAction, requester: String)(using
      platform: PlatformAdapter[F]
  ): F[Unit] =
    subscriptionFlow.submit(msg, action, requester)

  /** Whether a reply was sent by the reactive logic (the LLM is not part of it). */
  def botLogic(msg: Message)(using platform: PlatformAdapter[F]): F[Boolean] =
    reactiveLogic(msg).map {
      case Reactive.Done(replied) => replied
      case Reactive.Unmatched     => false
    }

  private def reactiveLogic(msg: Message)(using platform: PlatformAdapter[F]): F[Reactive] =
    msg.messageType(sBotConfig.sBotInfo.botId, platform.capabilities.commandSyntax) match {
      case MessageType.Message =>
        LogWriter.trace("Message Type: Message") >> reactiveMessages.logicReplied(msg)
      case MessageType.Command =>
        LogWriter.trace("Message Type: Command") >> commands.commandLogicReplied(msg).map(Reactive.Done(_))
      case MessageType.FileRequest =>
        LogWriter.trace("Message Type: File Request") >> commands.fileRequestLogicReplied(msg).map(Reactive.Done(_))
    }

  /** The entry point: a message arrived. One `[stats]` line tells how it ended and how long it took. */
  def onMessage(msg: Message)(using platform: PlatformAdapter[F]): F[Unit] = {
    // the reactive part is bounded by the request timeout; the second value is what is left to do for the LLM
    val process: F[(Outcome, Stage)] = for {
      _       <- LogWriter.trace(s"${sBotConfig.sBotInfo.botName}: A message arrived: $msg")
      handled <- isDuplicate(msg).ifM(
        skipDuplicate(msg).as((Outcome.Duplicate, Stage.Nothing)),
        handleMessage(msg).map(stage => (Outcome.Handled, stage))
      )
    } yield handled

    val bounded: F[(Outcome, Stage)] = sBotConfig.requestProcessingTimeout match {
      case Some(timeout) => withRequestTimeout(msg, timeout)(process)
      case None          => process
    }

    for {
      start         <- Clock[F].monotonic
      boundedResult <- bounded.onError(_ => logStats(Outcome.Failed, start))
      (outcome, stage) = boundedResult
      _ <- llm.stage(msg, stage).onError(_ => logStats(Outcome.Failed, start))
      _ <- logStats(outcome, start)
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

  /** What is left for the LLM side, see [[EngineLlm.stageAfter]]. */
  private def handleMessage(msg: Message)(using platform: PlatformAdapter[F]): F[Stage] = for {
    _        <- LogWriter.info(s"${sBotConfig.sBotInfo.botName}: A message arrived with content: ${msg.text}")
    reactive <- reactiveLogic(msg)
    replied = reactive == Reactive.Done(true)
    // the last interaction (what the chat timeout is measured from) is logged only after a reply
    _     <- postComputation(msg).whenA(replied)
    stage <- llm.stageAfter(msg, reactive)
  } yield stage

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
      process: F[(Outcome, Stage)]
  )(using platform: PlatformAdapter[F]): F[(Outcome, Stage)] = {
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
    Async[F].timeoutTo(process, timeout, timedOut.as((Outcome.TimedOut, Stage.Nothing)))
  }
}

object BotEngine {

  val defaultLlmTimeout: FiniteDuration = LlmEngine.defaultTimeout

  final case class RequestTimedOut(timeout: FiniteDuration)
      extends Throwable(s"the request processing timed out after $timeout")

  def apply[F[_]: Async: LogWriter](
      sBotConfig: SBotConfig,
      dbLayer: DBLayer[F],
      notifier: Notifier[F],
      backgroundJobManager: BackgroundJobManager[F]
  ): BotEngine[F] = apply[F](
    sBotConfig = sBotConfig,
    dbLayer = dbLayer,
    notifier = notifier,
    backgroundJobManager = backgroundJobManager,
    llmResponder = dbLayer.llm.responder,
    llmTimeout = dbLayer.llm.timeout
  )

  /** The engine with the standard filtering (the timeout of the chat) and post computation (log the last interaction).
    */
  def apply[F[_]: Async: LogWriter](
      sBotConfig: SBotConfig,
      dbLayer: DBLayer[F],
      notifier: Notifier[F],
      backgroundJobManager: BackgroundJobManager[F],
      llmResponder: LlmResponder[F],
      llmTimeout: FiniteDuration = defaultLlmTimeout
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
      backgroundJobManager = backgroundJobManager,
      llmResponder = llmResponder,
      llmTimeout = llmTimeout
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
