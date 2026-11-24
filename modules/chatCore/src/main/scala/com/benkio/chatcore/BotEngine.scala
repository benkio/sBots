package com.benkio.chatcore

import cats.effect.syntax.all.*
import cats.effect.Async
import cats.effect.Clock
import cats.effect.Ref
import cats.effect.Sync
import cats.syntax.all.*
import cats.Applicative
import com.benkio.chatcore.config.SBotConfig
import com.benkio.chatcore.llm.LlmEngine
import com.benkio.chatcore.llm.LlmResponder
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
import com.benkio.chatcore.model.ChatId
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
import com.benkio.chatcore.BotEngine.Reactive
import com.benkio.chatcore.BotEngine.Stage
import com.benkio.chatcore.SubscriptionFlowOutcome
import com.benkio.chatcore.SubscriptionFlowService
import com.benkio.chatcore.UsageStats.Outcome
import log.effect.LogWriter

import java.util.concurrent.ConcurrentHashMap
import scala.concurrent.duration.*

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

  /** The reactive part of a message: a guided-flow answer, or a matching bundle that the filters let through. Nothing
    * matching leaves the message [[Reactive.Unmatched]], the only case the LLM can still answer.
    */
  private def messageLogicReplied(msg: Message)(using platform: PlatformAdapter[F]): F[Reactive] =
    guidedFlowRequester(msg, platform).fold(messageReplyBundles(msg)) { requester =>
      subscriptionFlow
        .acceptPlainMessage(msg, requester, deliverFlow(_, msg, requester))
        .flatMap(outcome => outcome.fold(messageReplyBundles(msg))(_ => (Reactive.Done(true): Reactive).pure[F]))
    }

  private def messageReplyBundles(msg: Message)(using platform: PlatformAdapter[F]): F[Reactive] =
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
      .map(_.fold[Reactive](Reactive.Unmatched)(Reactive.Done(_)))

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

  /** Whether a reply was sent by the reactive logic (the LLM is not part of it). */
  def botLogic(msg: Message)(using platform: PlatformAdapter[F]): F[Boolean] =
    reactiveLogic(msg).map {
      case Reactive.Done(replied) => replied
      case Reactive.Unmatched     => false
    }

  private def reactiveLogic(msg: Message)(using platform: PlatformAdapter[F]): F[Reactive] =
    msg.messageType(sBotConfig.sBotInfo.botId, platform.capabilities.commandSyntax) match {
      case MessageType.Message =>
        LogWriter.trace("Message Type: Message") >> messageLogicReplied(msg)
      case MessageType.Command =>
        LogWriter.trace("Message Type: Command") >> commandLogicReplied(msg).map(Reactive.Done(_))
      case MessageType.FileRequest =>
        LogWriter.trace("Message Type: File Request") >> fileRequestLogicReplied(msg).map(Reactive.Done(_))
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
      _ <- llmStage(msg, stage).onError(_ => logStats(Outcome.Failed, start))
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

  /** What is left for the LLM side: the message is unanswered and can be asked to it, or only remembered, or nothing.
    */
  private def handleMessage(msg: Message)(using platform: PlatformAdapter[F]): F[Stage] = for {
    _        <- LogWriter.info(s"${sBotConfig.sBotInfo.botName}: A message arrived with content: ${msg.text}")
    reactive <- reactiveLogic(msg)
    replied = reactive == Reactive.Done(true)
    // the last interaction (what the chat timeout is measured from) is logged only after a reply
    _ <- postComputation(msg).whenA(replied)
  } yield
    if msg.messageType(sBotConfig.sBotInfo.botId, platform.capabilities.commandSyntax) != MessageType.Message
    then Stage.Nothing
    else if reactive == Reactive.Unmatched then Stage.Ask
    else Stage.Remember

  // LLM ////////////////////////////////////////////////////////////////

  /** The chats with an LLM call in flight: the cooldown starts only when an answer is sent, so a second directed
    * message arriving meanwhile would otherwise reach the LLM too.
    */
  // plain mutable state private to this engine, atomic by itself; every access is suspended in F
  private val llmInFlight: java.util.Set[ChatId] = ConcurrentHashMap.newKeySet[ChatId]()

  private def claimLlmChat(chatId: ChatId): F[Boolean] = Sync[F].delay(llmInFlight.add(chatId))

  private def releaseLlmChat(chatId: ChatId): F[Unit] = Sync[F].delay(llmInFlight.remove(chatId)).void

  /** Runs after, and outside, the request timeout. Only a plain message nothing reactive answered ([[Stage.Ask]]),
    * directed at the bot, in a chat that enabled the LLM and whose timeout (the cooldown shared with the reactive
    * replies) has expired, reaches a responder that can answer. A failure or a timeout of the LLM never breaks the
    * message: it is dropped. Every plain message is then handed to the responder to remember, after the answer so it is
    * not part of its own prompt, followed by the answer that was sent.
    */
  private def llmStage(msg: Message, stage: Stage)(using platform: PlatformAdapter[F]): F[Unit] =
    Option.when(stage != Stage.Nothing && llmResponder.answers)(stage).traverse_ { _ =>
      val sent: F[Option[(Reply, List[ReplyBundleMessage])]] =
        if stage == Stage.Ask then llmLogic(msg) else none[(Reply, List[ReplyBundleMessage])].pure[F]
      sent.flatMap { answered =>
        // bounded like the LLM call: a stalled DB must not hold the handler, and the stats line is always logged
        val remembering: F[Unit] =
          llmResponder.remember(msg) *>
            answered.traverse_((reply, candidates) => llmResponder.rememberAnswer(msg, reply, candidates))
        Async[F]
          .timeoutTo(
            remembering,
            llmTimeout,
            LogWriter.warn(
              s"${sBotConfig.sBotInfo.botName}: the LLM memory timed out after $llmTimeout (chatId=${msg.chatId.id})"
            )
          )
          .handleErrorWith(e =>
            LogWriter.warn(s"${sBotConfig.sBotInfo.botName}: the LLM memory failed (chatId=${msg.chatId.id}): $e")
          )
      }
    }

  /** The reply sent by the LLM, if any, with the candidates it was offered. */
  private def llmLogic(msg: Message)(using
      platform: PlatformAdapter[F]
  ): F[Option[(Reply, List[ReplyBundleMessage])]] = {
    val botName = sBotConfig.sBotInfo.botName
    val none_   = none[(Reply, List[ReplyBundleMessage])].pure[F]
    // The delivered answer is kept as soon as it is sent, so a stage that is cut afterwards (a stalled interaction
    // update) or fails still hands it to the memory: the user saw that reply.
    def stage(delivered: Ref[F, Option[(Reply, List[ReplyBundleMessage])]]): F[Unit] = {
      // readiness (settings and cooldown) is read while the claim is held, so it is the current cooldown, not one from
      // before a concurrent answer started it
      val ready: F[Unit] =
        llmChatReady(msg).ifM(
          answerWithLlm(msg).flatMap(answered =>
            delivered.set(answered) *> postComputation(msg).whenA(answered.isDefined)
          ),
          Applicative[F].unit
        )
      // the whole stage is bounded: the LLM has its own budget, the readiness lookup, the delivery and the interaction
      // update have the one of a reactive request (unbounded when that is disabled, like the reactive path)
      val bounded: F[Unit] =
        sBotConfig.requestProcessingTimeout.fold(ready) { requestTimeout =>
          Async[F].timeoutTo(
            ready,
            llmTimeout + requestTimeout,
            LogWriter.warn(s"$botName: the LLM stage timed out, dropped (chatId=${msg.chatId.id})")
          )
        }
      // claim and release are one uncancelable unit: a cancel or a timeout can never leave the chat blocked
      Async[F].uncancelable { poll =>
        claimLlmChat(msg.chatId).ifM(
          poll(bounded).guarantee(releaseLlmChat(msg.chatId)),
          Applicative[F].unit
        )
      }
    }
    if !isLlmCandidate(msg) then none_
    else
      for {
        delivered <- Ref.of[F, Option[(Reply, List[ReplyBundleMessage])]](None)
        _         <- stage(delivered).handleErrorWith(e =>
          LogWriter.error(s"$botName: the LLM reply failed and was dropped (chatId=${msg.chatId.id}): $e")
        )
        answered <- delivered.get
      } yield answered
  }

  private def isLlmCandidate(msg: Message): Boolean =
    msg.isDirectedAt(sBotConfig.sBotInfo.botName) &&
      FilteringForward.filter(msg, sBotConfig.disableForward) &&
      FilteringOlder.filter(msg) &&
      !sBotConfig.ignoreMessagePrefix.exists(prefix => msg.text.exists(_.startsWith(prefix)))

  private def llmChatReady(msg: Message): F[Boolean] =
    dbLayer.llmSettings
      .status(msg.chatId)
      .map(_.enabled)
      .ifM(
        FilteringTimeout.chatAllowed(dbLayer, sBotConfig.sBotInfo.botId).apply(msg),
        false.pure[F]
      )

  /** The reply sent, with the candidates it was offered. */
  private def answerWithLlm(msg: Message)(using
      platform: PlatformAdapter[F]
  ): F[Option[(Reply, List[ReplyBundleMessage])]] = {
    val candidates = ReplyBundleMessage.matchingReplyBundles(
      msg = msg,
      messageRepliesData = messageRepliesData,
      ignoreMessagePrefix = sBotConfig.ignoreMessagePrefix
    )
    // the typing action is inside the timeout too: a hung platform call cannot hold the message
    val asked: F[Option[Option[Reply]]] =
      (platform.sendTyping(msg).handleErrorWith(e => LogWriter.warn(s"typing action failed: $e")) *>
        llmResponder.respond(msg, candidates)).map(Some(_))
    for {
      answer  <- Async[F].timeoutTo(asked, llmTimeout, none[Option[Reply]].pure[F]).attempt
      replied <- answer match {
        case Right(Some(Some(llmReply))) => reply(llmReply, msg).as(Some((llmReply, candidates)))
        case Right(Some(None))           => none[(Reply, List[ReplyBundleMessage])].pure[F]
        case Right(None)                 =>
          LogWriter
            .warn(
              s"${sBotConfig.sBotInfo.botName}: the LLM did not answer within $llmTimeout, dropped (chatId=${msg.chatId.id})"
            )
            .as(none[(Reply, List[ReplyBundleMessage])])
        case Left(e) =>
          LogWriter
            .error(s"${sBotConfig.sBotInfo.botName}: the LLM failed, dropped (chatId=${msg.chatId.id}): $e")
            .as(none[(Reply, List[ReplyBundleMessage])])
      }
    } yield replied
  }

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

  val defaultLlmTimeout: FiniteDuration = LlmEngine.defaultTimeout

  /** What the reactive logic made of a message: it decided (a reply was sent or not, e.g. a bundle blocked by the chat
    * timeout), or nothing matched and the LLM may answer.
    */
  /** What the engine still does for the LLM after the reactive part: ask it, remember the message only, or nothing. */
  private enum Stage {
    case Ask, Remember, Nothing
  }

  private enum Reactive {
    case Done(replied: Boolean)
    case Unmatched
  }

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
