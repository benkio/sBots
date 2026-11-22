package com.benkio.chatcore

import cats.effect.std.Semaphore
import cats.effect.Async
import cats.syntax.all.*
import com.benkio.chatcore.model.*
import com.benkio.chatcore.model.reply.ReplyValue
import com.benkio.chatcore.model.reply.Text
import com.benkio.chatcore.patterns.SubscriptionCommands.SubscribeUnsubscribeCommand
import cron4s.Cron

import java.time.LocalDateTime
import java.util.concurrent.ConcurrentHashMap
import scala.concurrent.duration.FiniteDuration

/** What the engine does after a step of the guided subscription flow: show or edit a prompt, send a new message, or
  * leave an ordinary message to the normal reply bundles.
  */
sealed trait SubscriptionFlowOutcome

object SubscriptionFlowOutcome {
  final case class Prompt(value: SubscriptionFlowPrompt) extends SubscriptionFlowOutcome
  final case class Message(value: ReplyValue)            extends SubscriptionFlowOutcome
}

/** The platform-neutral guided `/subscribe` flow. It owns the drafts and decides the next prompt; the adapters only
  * render the prompt and turn a press or a plain message into a neutral action.
  *
  * @param createSubscription
  *   the same creation the one-shot command uses, returning a typed failure so the flow can explain it
  */
final class SubscriptionFlowService[F[_]: Async](
    store: SubscriptionDraftStore[F],
    sBotInfo: SBotInfo,
    expiry: FiniteDuration,
    ttl: Option[FiniteDuration],
    createSubscription: (Message, SBotInfo, SubscribeArguments) => F[Either[Throwable, ReplyValue]]
) {

  /** Presses of one draft can arrive on parallel fibers (a webhook or a button listener). Serializing the transitions
    * of a triple keeps them atomic from the caller's point of view: in particular, two confirms cannot both see a live
    * draft and create two subscriptions.
    */
  private val keyLocks = new ConcurrentHashMap[SubscriptionFlowKey, Semaphore[F]]()

  private def withKeyLock[A](key: SubscriptionFlowKey)(body: F[A]): F[A] =
    Option(keyLocks.get(key))
      .fold(Semaphore[F](1).map(created => Option(keyLocks.putIfAbsent(key, created)).getOrElse(created)))(_.pure[F])
      .flatMap(_.permit.use(_ => body))

  def start(msg: Message, requester: String): F[SubscriptionFlowOutcome] =
    withKeyLock(key(msg, requester)) {
      Async[F].realTime.flatMap { now =>
        val draft = SubscriptionDraft(key(msg, requester), SubscriptionDraftStep.Type, None, None, None, now)
        store.start(draft).map {
          case SubscriptionDraftStartResult.Started =>
            prompt(draft)
          case SubscriptionDraftStartResult.Full =>
            SubscriptionFlowOutcome.Message(error(SubscriptionFlowFailure.StoreFull, storeFullText))
        }
      }
    }

  def submit(msg: Message, action: SubscriptionFlowAction, requester: String): F[SubscriptionFlowOutcome] =
    withKeyLock(key(msg, requester))(submitLocked(msg = msg, action = action, requester = requester))

  private def submitLocked(
      msg: Message,
      action: SubscriptionFlowAction,
      requester: String
  ): F[SubscriptionFlowOutcome] =
    store.get(key(msg, requester)).flatMap {
      case None =>
        SubscriptionFlowOutcome.Message(error(SubscriptionFlowFailure.NoActiveFlow, noActiveFlowText)).pure[F]
      case Some(draft) if !isAllowed(draft, action) =>
        save(draft).as(
          SubscriptionFlowOutcome.Message(error(SubscriptionFlowFailure.InvalidAction, invalidActionText))
        )
      case Some(draft) => applyAction(msg = msg, draft = draft, action = action)
    }

  /** A plain message is a flow answer only at the schedule and target steps, and only for the draft owner. */
  def acceptPlainMessage(msg: Message, requester: String): F[Option[SubscriptionFlowOutcome]] =
    withKeyLock(key(msg, requester)) {
      store.get(key(msg, requester)).flatMap {
        case Some(draft) if draft.step == SubscriptionDraftStep.Schedule =>
          submitLocked(
            msg = msg,
            action = SubscriptionFlowAction.enterCron(msg.text.getOrElse("")),
            requester = requester
          ).map(Some(_))
        case Some(draft) if draft.step == SubscriptionDraftStep.Target =>
          submitLocked(
            msg = msg,
            action = SubscriptionFlowAction.enterTarget(msg.text.getOrElse("")),
            requester = requester
          ).map(Some(_))
        case _ => none[SubscriptionFlowOutcome].pure[F]
      }
    }

  private def key(msg: Message, requester: String): SubscriptionFlowKey =
    SubscriptionFlowKey(msg.chatId, sBotInfo.botId, requester)

  private def isAllowed(draft: SubscriptionDraft, action: SubscriptionFlowAction): Boolean = {
    import SubscriptionFlowActionKind.*
    action.kind match {
      case Cancel     => true
      case Back       => draft.step != SubscriptionDraftStep.Type
      case Confirm    => draft.step == SubscriptionDraftStep.Confirm
      case ChooseType =>
        draft.step == SubscriptionDraftStep.Type &&
        action.subscriptionType.exists(t => t == SubscriptionType.Show || t == SubscriptionType.Trigger)
      case ChoosePreset => draft.step == SubscriptionDraftStep.Schedule && action.preset.nonEmpty
      case EnterCron    => draft.step == SubscriptionDraftStep.Schedule && action.text.exists(_.nonEmpty)
      case EnterTarget  => draft.step == SubscriptionDraftStep.Target && action.text.nonEmpty
    }
  }

  private def applyAction(
      msg: Message,
      draft: SubscriptionDraft,
      action: SubscriptionFlowAction
  ): F[SubscriptionFlowOutcome] = {
    import SubscriptionFlowActionKind.*
    action.kind match {
      case Cancel     => finish(draft = draft, detail = cancelledText, reason = SubscriptionFlowFailure.Cancelled)
      case Back       => save(goBack(draft)).map(prompt)
      case ChooseType =>
        save(draft.copy(step = SubscriptionDraftStep.Schedule, subscriptionType = action.subscriptionType)).map(prompt)
      case EnterTarget  => enterTarget(draft, action.text.getOrElse(""))
      case Confirm      => confirm(msg, draft)
      case ChoosePreset =>
        action.preset.fold(
          save(draft).as(
            SubscriptionFlowOutcome.Message(error(SubscriptionFlowFailure.InvalidAction, invalidActionText))
          )
        )(preset => setSchedule(draft = draft, cron = preset.cron, dropOnInvalid = false))
      case EnterCron =>
        action.text.fold(
          save(draft).as(
            SubscriptionFlowOutcome.Message(error(SubscriptionFlowFailure.InvalidAction, invalidActionText))
          )
        )(cron => setSchedule(draft = draft, cron = cron, dropOnInvalid = true))
    }
  }

  private def goBack(draft: SubscriptionDraft): SubscriptionDraft =
    draft.step match {
      case SubscriptionDraftStep.Schedule => draft.copy(step = SubscriptionDraftStep.Type, subscriptionType = None)
      case SubscriptionDraftStep.Target   => draft.copy(step = SubscriptionDraftStep.Schedule, schedule = None)
      case SubscriptionDraftStep.Confirm  => draft.copy(step = SubscriptionDraftStep.Target, target = None)
      case SubscriptionDraftStep.Type     => draft
    }

  private def setSchedule(draft: SubscriptionDraft, cron: String, dropOnInvalid: Boolean): F[SubscriptionFlowOutcome] =
    validateCron(cron) match {
      case Right(_) =>
        save(draft.copy(step = SubscriptionDraftStep.Target, schedule = Some(cron))).map(prompt)
      case Left(reason) =>
        val outcome = SubscriptionFlowOutcome.Message(
          error(
            SubscriptionFlowFailure.InvalidCron,
            s"Invalid schedule: ${Option(reason.getMessage).getOrElse(reason.toString)}"
          )
        )
        if dropOnInvalid then store.remove(draft.key).as(outcome) else save(draft).as(outcome)
    }

  private def enterTarget(draft: SubscriptionDraft, text: String): F[SubscriptionFlowOutcome] =
    if text.length > SubscriptionRules.maxTargetLength then save(draft).as(
      SubscriptionFlowOutcome.Message(error(SubscriptionFlowFailure.TargetTooLong, targetTooLongText))
    )
    else {
      val target = Option.when(text.nonEmpty)(text)
      save(draft.copy(step = SubscriptionDraftStep.Confirm, target = target)).map(prompt)
    }

  private def confirm(msg: Message, draft: SubscriptionDraft): F[SubscriptionFlowOutcome] = {
    val arguments = for {
      subscriptionType <- draft.subscriptionType.toRight(
        new IllegalArgumentException("The subscription type is missing")
      )
      cron <- draft.schedule.toRight(new IllegalArgumentException("The subscription schedule is missing"))
    } yield SubscribeArguments(cron, subscriptionType, draft.target)

    arguments.fold(
      reason => finish(draft = draft, detail = reason.getMessage, reason = SubscriptionFlowFailure.CreationRejected),
      args =>
        createSubscription(msg, sBotInfo, args).flatMap {
          case Right(replyValue) => store.remove(draft.key).as(SubscriptionFlowOutcome.Message(replyValue))
          case Left(reason)      =>
            val detail =
              s"${Option(reason.getMessage).getOrElse(reason.toString)}. Run it directly with: ${oneShotCommand(args)}"
            finish(draft = draft, detail = detail, reason = SubscriptionFlowFailure.CreationRejected)
        }
    )
  }

  private def finish(
      draft: SubscriptionDraft,
      detail: String,
      reason: SubscriptionFlowFailure
  ): F[SubscriptionFlowOutcome] =
    store.remove(draft.key).as(SubscriptionFlowOutcome.Message(error(reason, detail)))

  private def save(draft: SubscriptionDraft): F[SubscriptionDraft] =
    Async[F].realTime.flatMap { now =>
      val refreshed = draft.copy(savedAt = now)
      store.put(refreshed).as(refreshed)
    }

  private def validateCron(cron: String): Either[Throwable, Unit] =
    Cron(cron).flatMap(parsed => SubscriptionRules.validateInterval(parsed, LocalDateTime.now))

  private def prompt(draft: SubscriptionDraft): SubscriptionFlowOutcome =
    SubscriptionFlowOutcome.Prompt(
      SubscriptionFlowPrompt(draft.key, draft.step, promptText(draft), promptButtons(draft.step))
    )

  private def promptText(draft: SubscriptionDraft): String =
    draft.step match {
      case SubscriptionDraftStep.Type =>
        s"Choose the subscription type: show or trigger. An idle setup expires after $expiry."
      case SubscriptionDraftStep.Schedule =>
        "Choose a schedule or type a 6-field cron (second minute hour day-of-month month day-of-week)."
      case SubscriptionDraftStep.Target if draft.subscriptionType.contains(SubscriptionType.Trigger) =>
        "Type the trigger text, or choose random. Check the spelling first with /triggersearch."
      case SubscriptionDraftStep.Target =>
        "Type the show id or search, or choose random."
      case SubscriptionDraftStep.Confirm =>
        val schedule   = draft.schedule.map(scheduleLabel).getOrElse("unknown")
        val cron       = draft.schedule.getOrElse("unknown")
        val occurrence = draft.schedule
          .flatMap(value => Cron(value).toOption)
          .fold("`Unknown next occurrence`")(SubscribeUnsubscribeCommand.nextOccurrence)
        val target = draft.target.getOrElse("random")
        s"Confirm subscription: type ${draft.subscriptionType.fold("unknown")(_.asString)}, schedule $schedule " +
          s"($cron), next occurrence $occurrence, target $target."
    }

  private def scheduleLabel(cron: String): String =
    SubscriptionSchedulePreset.values.find(_.cron == cron).fold("custom")(_.label)

  private def promptButtons(step: SubscriptionDraftStep): List[List[SubscriptionFlowButton]] = {
    val navigation = List(SubscriptionFlowButton("cancel", SubscriptionFlowAction.cancel))
    step match {
      case SubscriptionDraftStep.Type =>
        List(
          List(
            SubscriptionFlowButton("show", SubscriptionFlowAction.chooseType(SubscriptionType.Show)),
            SubscriptionFlowButton("trigger", SubscriptionFlowAction.chooseType(SubscriptionType.Trigger))
          ),
          navigation
        )
      case SubscriptionDraftStep.Schedule =>
        SubscriptionSchedulePreset.values
          .map(preset => List(SubscriptionFlowButton(preset.label, SubscriptionFlowAction.choosePreset(preset))))
          .toList ++ List(List(SubscriptionFlowButton("back", SubscriptionFlowAction.back)), navigation)
      case SubscriptionDraftStep.Target =>
        List(List(SubscriptionFlowButton("random", SubscriptionFlowAction.enterTarget("")))) ++
          List(List(SubscriptionFlowButton("back", SubscriptionFlowAction.back)), navigation)
      case SubscriptionDraftStep.Confirm =>
        List(List(SubscriptionFlowButton("confirm", SubscriptionFlowAction.confirm))) ++
          List(List(SubscriptionFlowButton("back", SubscriptionFlowAction.back)), navigation)
    }
  }

  private def error(reason: SubscriptionFlowFailure, detail: String): Text =
    Text(s"Subscription setup failed (${reason.asString}): $detail", ttl)

  private def oneShotCommand(arguments: SubscribeArguments): String = {
    val target = arguments.target.fold("")(value => s" $value")
    s"/subscribe ${arguments.cron} ${arguments.subscriptionType.asString}$target"
  }

  private val noActiveFlowText: String =
    "No active subscription setup. Start again with /subscribe."
  private val invalidActionText: String =
    "That button or text does not fit this step."
  private val targetTooLongText: String =
    s"The target is too long: at most ${SubscriptionRules.maxTargetLength} characters."
  private val cancelledText: String = "Subscription setup cancelled."
  private val storeFullText: String =
    "Too many subscription setups are in progress. Use /subscribe <cron> [type [target]], or wait and retry."
}
