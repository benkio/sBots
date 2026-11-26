package com.benkio.chatcore

import cats.effect.syntax.all.*
import cats.effect.Async
import cats.effect.Ref
import cats.effect.Sync
import cats.syntax.all.*
import cats.Applicative
import com.benkio.chatcore.config.SBotConfig
import com.benkio.chatcore.llm.LlmResponder
import com.benkio.chatcore.messagefiltering.*
import com.benkio.chatcore.model.reply.Reply
import com.benkio.chatcore.model.ChatId
import com.benkio.chatcore.model.Message
import com.benkio.chatcore.model.MessageType
import com.benkio.chatcore.repository.db.DBLayer
import log.effect.LogWriter

import java.util.concurrent.ConcurrentHashMap
import scala.concurrent.duration.*

/** What the engine still does for the LLM after the reactive part: ask it, remember the message only, or nothing. */
private[chatcore] enum Stage {
  case Ask, Remember, Nothing
}

/** Why the engine did not ask the LLM about a message. Logged at info level: a non-reply is otherwise invisible because
  * no responder interaction exists to produce an `llm_interaction` line.
  */
private[chatcore] enum LlmSkip(val label: String) {
  case ResponderOff    extends LlmSkip("responder_off")
  case NotDirected     extends LlmSkip("not_directed")
  case Forwarded       extends LlmSkip("forwarded")
  case TooOld          extends LlmSkip("too_old")
  case IgnoredPrefix   extends LlmSkip("ignored_prefix")
  case Disabled        extends LlmSkip("disabled")
  case ReactiveBlocked extends LlmSkip("reactive_blocked")
  case InTimeout       extends LlmSkip("in_timeout")
  case InFlight        extends LlmSkip("in_flight")
  case NoReply         extends LlmSkip("no_reply")
}

/** The LLM section of the [[BotEngine]]: runs after, and outside, the request timeout, on the plain messages the
  * reactive sections left to it ([[Stage]]). It decides whether the responder is asked (see [[LlmSkip]] for the gates),
  * sends its answer through the [[EngineDelivery]], and hands every plain message, and the answer sent, to the
  * responder to remember. A failure or a timeout of the LLM never breaks the message: it is logged and dropped.
  *
  * @param sBotConfig
  *   the bot configuration: its name for the logs, the filters of the messages and the request timeout
  * @param dbLayer
  *   the chat LLM settings and the timeout of the chat, shared with the reactive replies
  * @param llmResponder
  *   answers the messages directed to the bot and remembers the others
  * @param delivery
  *   sends the answer of the responder
  * @param postComputation
  *   what to do after a message is answered, e.g. logging the last interaction of the chat
  * @param llmTimeout
  *   the time the LLM has to answer: it runs after, and outside, the request processing timeout, and an answer that
  *   comes later is dropped silently
  */
private[chatcore] trait EngineLlm[F[_]] {

  /** What is left for the LLM after the reactive part. */
  def stageAfter(msg: Message, reactive: Reactive)(using platform: PlatformAdapter[F]): F[Stage]

  /** Runs the LLM part of the message: asks the responder if the stage says so, then remembers. */
  def stage(msg: Message, stage: Stage)(using platform: PlatformAdapter[F]): F[Unit]

  /** Logs why the LLM was not asked about the message. */
  def logSkip(msg: Message, reason: LlmSkip): F[Unit]
}

/** The [[EngineLlm]] on top of the [[LlmResponder]]. */
private[chatcore] class DefaultEngineLlm[F[_]: Async: LogWriter](
    sBotConfig: SBotConfig,
    dbLayer: DBLayer[F],
    postComputation: Message => F[Unit],
    llmResponder: LlmResponder[F],
    llmTimeout: FiniteDuration,
    delivery: EngineDelivery[F]
) extends EngineLlm[F] {

  /** The chats with an LLM call in flight: the cooldown starts only when an answer is sent, so a second directed
    * message arriving meanwhile would otherwise reach the LLM too.
    */
  // plain mutable state private to this engine, atomic by itself; every access is suspended in F
  private val llmInFlight: java.util.Set[ChatId] = ConcurrentHashMap.newKeySet[ChatId]()

  private def claimLlmChat(chatId: ChatId): F[Boolean] = Sync[F].delay(llmInFlight.add(chatId))

  private def releaseLlmChat(chatId: ChatId): F[Unit] = Sync[F].delay(llmInFlight.remove(chatId)).void

  /** What is left for the LLM after the reactive part: only a plain message is ever left. One nothing answered
    * ([[Reactive.Unmatched]]) can be asked to it, the others are only remembered. A bundle that matched but was blocked
    * by the filters (the chat timeout in production) never reaches the LLM: it is logged, or that no-reply has no
    * explanation at all (the known defect in specs/llm.allium).
    */
  def stageAfter(msg: Message, reactive: Reactive)(using platform: PlatformAdapter[F]): F[Stage] =
    if msg.messageType(sBotConfig.sBotInfo.botId, platform.capabilities.commandSyntax) != MessageType.Message
    then Stage.Nothing.pure[F]
    else
      logSkip(msg, LlmSkip.ReactiveBlocked).whenA(reactive == Reactive.Done(false)).as {
        if reactive == Reactive.Unmatched then Stage.Ask else Stage.Remember
      }

  /** Runs after, and outside, the request timeout. Only a plain message nothing reactive answered ([[Stage.Ask]]),
    * directed at the bot, in a chat that enabled the LLM and whose timeout (the cooldown shared with the reactive
    * replies) has expired, reaches a responder that can answer. A failure or a timeout of the LLM never breaks the
    * message: it is dropped. Every plain message is then handed to the responder to remember, after the answer so it is
    * not part of its own prompt, followed by the answer that was sent.
    */
  def stage(msg: Message, stage: Stage)(using platform: PlatformAdapter[F]): F[Unit] =
    if stage == Stage.Nothing then Applicative[F].unit
    else if !llmResponder.answers then logSkip(msg, LlmSkip.ResponderOff)
    else {
      val sent: F[Option[Reply]] =
        if stage == Stage.Ask then logic(msg) else none[Reply].pure[F]
      sent.flatMap { answered =>
        // bounded like the LLM call: a stalled DB must not hold the handler, and the stats line is always logged
        val remembering: F[Unit] =
          llmResponder.remember(msg) *>
            answered.traverse_(reply => llmResponder.rememberAnswer(msg, reply))
        Async[F]
          .timeoutTo(
            remembering,
            llmTimeout,
            LogWriter.warn(
              s"${sBotConfig.sBotInfo.botName}: the LLM memory timed out after $llmTimeout (chatId=${msg.chatId.id})"
            )
          )
          .handleErrorWith(e =>
            LogWriter.warn(
              s"${sBotConfig.sBotInfo.botName}: the LLM memory failed (chatId=${msg.chatId.id}): ${e.getClass.getSimpleName}"
            )
          )
      }
    }

  /** A skipped LLM interaction has no observer line, so the reason has to be logged here to explain a non-reply. */
  def logSkip(msg: Message, reason: LlmSkip): F[Unit] =
    LogWriter.info(
      s"${sBotConfig.sBotInfo.botName}: LLM not asked (chatId=${msg.chatId.id}): reason=${reason.label}"
    )

  /** The reply sent by the LLM, if any. */
  private def logic(msg: Message)(using
      platform: PlatformAdapter[F]
  ): F[Option[Reply]] = {
    val botName = sBotConfig.sBotInfo.botName
    val none_   = none[Reply].pure[F]
    // The delivered answer is kept as soon as it is sent, so a stage that is cut afterwards (a stalled interaction
    // update) or fails still hands it to the memory: the user saw that reply.
    def stage(delivered: Ref[F, Option[Reply]]): F[Unit] = {
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
          logSkip(msg, LlmSkip.InFlight)
        )
      }
    }
    candidateSkip(msg) match {
      case Some(reason) => logSkip(msg, reason) *> none_
      case None         =>
        for {
          delivered <- Ref.of[F, Option[Reply]](None)
          _         <- stage(delivered).handleErrorWith(e =>
            LogWriter.error(
              s"$botName: the LLM reply failed and was dropped (chatId=${msg.chatId.id}): ${e.getClass.getSimpleName} - ${e.getMessage()}"
            )
          )
          answered <- delivered.get
        } yield answered
    }
  }

  /** The first filter that keeps a message away from the responder, if any. */
  private def candidateSkip(msg: Message): Option[LlmSkip] =
    if !msg.isDirectedAt(sBotConfig.sBotInfo.botName) then Some(LlmSkip.NotDirected)
    else if !FilteringForward.filter(msg, sBotConfig.disableForward) then Some(LlmSkip.Forwarded)
    else if !FilteringOlder.filter(msg) then Some(LlmSkip.TooOld)
    else if sBotConfig.ignoreMessagePrefix.exists(prefix => msg.text.exists(_.startsWith(prefix))) then Some(
      LlmSkip.IgnoredPrefix
    )
    else None

  private def llmChatReady(msg: Message): F[Boolean] =
    dbLayer.llmSettings
      .status(msg.chatId)
      .map(_.enabled)
      .ifM(
        FilteringTimeout
          .chatAllowed(dbLayer, sBotConfig.sBotInfo.botId)
          .apply(msg)
          .ifM(
            true.pure[F],
            logSkip(msg, LlmSkip.InTimeout).as(false)
          ),
        logSkip(msg, LlmSkip.Disabled).as(false)
      )

  /** The reply sent, if any. */
  private def answerWithLlm(msg: Message)(using
      platform: PlatformAdapter[F]
  ): F[Option[Reply]] = {
    // the typing action is inside the timeout too: a hung platform call cannot hold the message
    val asked: F[Option[Option[Reply]]] =
      llmResponder
        .respondAfter(
          msg,
          platform
            .sendTyping(msg)
            .handleErrorWith(e => LogWriter.warn(s"typing action failed: ${e.getClass.getSimpleName}"))
        )
        .map(Some(_))
    for {
      answer  <- Async[F].timeoutTo(asked, llmTimeout, none[Option[Reply]].pure[F]).attempt
      replied <- answer match {
        case Right(Some(Some(llmReply))) => delivery.reply(llmReply, msg).as(Some(llmReply))
        case Right(Some(None))           =>
          logSkip(msg, LlmSkip.NoReply) *> none[Reply].pure[F]
        case Right(None) =>
          LogWriter
            .warn(
              s"${sBotConfig.sBotInfo.botName}: the LLM did not answer within $llmTimeout, dropped (chatId=${msg.chatId.id})"
            )
            .as(none[Reply])
        case Left(e) =>
          LogWriter
            .error(
              s"${sBotConfig.sBotInfo.botName}: the LLM failed, dropped (chatId=${msg.chatId.id}): ${e.getClass.getSimpleName}"
            )
            .as(none[Reply])
      }
    } yield replied
  }
}
