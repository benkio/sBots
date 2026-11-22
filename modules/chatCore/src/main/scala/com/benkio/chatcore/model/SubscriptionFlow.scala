package com.benkio.chatcore.model

import com.benkio.chatcore.model.reply.ReplyValue

import scala.concurrent.duration.FiniteDuration

/** The step of a guided subscription draft. The draft is owned by the engine: a button only carries an action. */
enum SubscriptionDraftStep(val asString: String) {
  case Type     extends SubscriptionDraftStep("type")
  case Schedule extends SubscriptionDraftStep("schedule")
  case Target   extends SubscriptionDraftStep("target")
  case Confirm  extends SubscriptionDraftStep("confirm")
}

/** The schedules offered as buttons. Their crons are the ones of the Allium spec, all above the minimum interval. */
enum SubscriptionSchedulePreset(val asString: String, val cron: String, val label: String) {
  case EveryMinute extends SubscriptionSchedulePreset("every_minute", "0 * * ? * *", "every minute")
  case Every15Minutes
      extends SubscriptionSchedulePreset(
        "every_15_minutes",
        "0 */15 * ? * *",
        "every 15 minutes"
      )
  case Hourly       extends SubscriptionSchedulePreset("hourly", "0 0 * ? * *", "hourly")
  case Daily0930    extends SubscriptionSchedulePreset("daily_0930", "0 30 9 * * ?", "daily at 09:30")
  case Weekdays1800 extends SubscriptionSchedulePreset("weekdays_1800", "0 0 18 ? * MON-FRI", "weekdays at 18:00")
}

object SubscriptionSchedulePreset {
  def fromString(value: String): Option[SubscriptionSchedulePreset] =
    SubscriptionSchedulePreset.values.find(_.asString == value)
}

/** What a button or a plain message asks the engine to do in a guided subscription. */
enum SubscriptionFlowActionKind(val asString: String) {
  case ChooseType   extends SubscriptionFlowActionKind("choose_type")
  case ChoosePreset extends SubscriptionFlowActionKind("choose_preset")
  case EnterCron    extends SubscriptionFlowActionKind("enter_cron")
  case EnterTarget  extends SubscriptionFlowActionKind("enter_target")
  case Back         extends SubscriptionFlowActionKind("back")
  case Cancel       extends SubscriptionFlowActionKind("cancel")
  case Confirm      extends SubscriptionFlowActionKind("confirm")
}

/** Why a guided subscription could not continue or complete. */
enum SubscriptionFlowFailure(val asString: String) {
  case NoActiveFlow     extends SubscriptionFlowFailure("no_active_flow")
  case InvalidAction    extends SubscriptionFlowFailure("invalid_action")
  case InvalidCron      extends SubscriptionFlowFailure("invalid_cron")
  case TargetTooLong    extends SubscriptionFlowFailure("target_too_long")
  case StoreFull        extends SubscriptionFlowFailure("store_full")
  case CreationRejected extends SubscriptionFlowFailure("creation_rejected")
  case Cancelled        extends SubscriptionFlowFailure("cancelled")
}

/** The identity of a draft and of the prompt message that shows its current step. */
final case class SubscriptionFlowKey(chatId: ChatId, botId: SBotInfo.SBotId, requester: String)

/** A press on a prompt, or a plain message: the adapter checks the owner before turning it into an engine action. */
final case class SubscriptionFlowAction(
    kind: SubscriptionFlowActionKind,
    subscriptionType: Option[SubscriptionType] = None,
    preset: Option[SubscriptionSchedulePreset] = None,
    text: Option[String] = None
)

/** A neutral button of a guided-flow prompt: the adapter only maps the label and the action to its own components. */
final case class SubscriptionFlowButton(label: String, action: SubscriptionFlowAction)

/** The prompt the adapters render and edit in place while the draft moves across its steps. */
final case class SubscriptionFlowPrompt(
    key: SubscriptionFlowKey,
    step: SubscriptionDraftStep,
    text: String,
    buttons: List[List[SubscriptionFlowButton]]
) extends ReplyValue

/** A draft as stored in the engine: its shape depends on the step it is in. */
final case class SubscriptionDraft(
    key: SubscriptionFlowKey,
    step: SubscriptionDraftStep,
    subscriptionType: Option[SubscriptionType],
    schedule: Option[String],
    target: Option[String],
    savedAt: FiniteDuration
)

object SubscriptionFlowAction {

  /** The id carries the action only, never the draft: the draft is found from chat, bot and requester. Typed cron and
    * non-empty target are free text, so they have no button id.
    */
  def toId(action: SubscriptionFlowAction): Option[String] = {
    val raw: Option[String] = action.kind match {
      case SubscriptionFlowActionKind.ChooseType =>
        action.subscriptionType.map(t => s"flow-type-${t.asString}")
      case SubscriptionFlowActionKind.ChoosePreset =>
        action.preset.map(p => s"flow-preset-${p.asString}")
      case SubscriptionFlowActionKind.EnterTarget if action.text.contains("") =>
        Some("flow-target-random")
      case SubscriptionFlowActionKind.Back    => Some("flow-back")
      case SubscriptionFlowActionKind.Cancel  => Some("flow-cancel")
      case SubscriptionFlowActionKind.Confirm => Some("flow-confirm")
      case _                                  => None
    }
    raw.filter(_.length <= SubscriptionFlowAction.maxIdLength)
  }

  def fromId(id: String): Option[SubscriptionFlowAction] =
    Option.when(id.length <= SubscriptionFlowAction.maxIdLength)(id).flatMap {
      case "flow-type-show"                                => Some(chooseType(SubscriptionType.Show))
      case "flow-type-trigger"                             => Some(chooseType(SubscriptionType.Trigger))
      case "flow-target-random"                            => Some(enterTarget(""))
      case "flow-back"                                     => Some(back)
      case "flow-cancel"                                   => Some(cancel)
      case "flow-confirm"                                  => Some(confirm)
      case presetId if presetId.startsWith("flow-preset-") =>
        SubscriptionSchedulePreset
          .fromString(presetId.stripPrefix("flow-preset-"))
          .map(choosePreset)
      case _ => None
    }

  val maxIdLength: Int                = 100
  val back: SubscriptionFlowAction    = SubscriptionFlowAction(SubscriptionFlowActionKind.Back)
  val cancel: SubscriptionFlowAction  = SubscriptionFlowAction(SubscriptionFlowActionKind.Cancel)
  val confirm: SubscriptionFlowAction = SubscriptionFlowAction(SubscriptionFlowActionKind.Confirm)

  def chooseType(subscriptionType: SubscriptionType): SubscriptionFlowAction =
    SubscriptionFlowAction(SubscriptionFlowActionKind.ChooseType, subscriptionType = Some(subscriptionType))

  def choosePreset(preset: SubscriptionSchedulePreset): SubscriptionFlowAction =
    SubscriptionFlowAction(SubscriptionFlowActionKind.ChoosePreset, preset = Some(preset))

  def enterCron(text: String): SubscriptionFlowAction =
    SubscriptionFlowAction(SubscriptionFlowActionKind.EnterCron, text = Some(text))

  def enterTarget(text: String): SubscriptionFlowAction =
    SubscriptionFlowAction(SubscriptionFlowActionKind.EnterTarget, text = Some(text))
}
