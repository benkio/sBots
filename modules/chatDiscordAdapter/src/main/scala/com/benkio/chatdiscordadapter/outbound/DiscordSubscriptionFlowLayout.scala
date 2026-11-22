package com.benkio.chatdiscordadapter.outbound

import com.benkio.chatcore.model.SubscriptionFlowAction
import com.benkio.chatcore.model.SubscriptionFlowActionKind
import com.benkio.chatcore.model.SubscriptionFlowButton
import com.benkio.chatcore.model.SubscriptionFlowPrompt
import com.benkio.chatdiscordadapter.model.DiscordLimits

/** A guided `/subscribe` prompt as a Discord message. The action id is the one of the engine: the adapter never puts
  * draft state in a button. Discord allows five rows of five buttons.
  */
object DiscordSubscriptionFlowLayout {

  val maxLabelLength: Int   = 80
  val maxButtonsPerRow: Int = 5
  val maxRows: Int          = 5

  /** The engine's rows are a grouping hint, but Discord allows at most five rows of five buttons. Pack the choices into
    * rows and always reserve the last one for navigation: dropping it would leave the user unable to go back or cancel
    * at a step with many choices, such as the five presets of the schedule step.
    */
  def render(prompt: SubscriptionFlowPrompt): DiscordButtonMessage = {
    val buttons    = prompt.buttons.flatten
    val navigation = buttons.filter(isNavigation).flatMap(button)
    val choices    = buttons.filterNot(isNavigation).flatMap(button)
    DiscordButtonMessage(
      text = prompt.text.take(DiscordLimits.maxTextMessageLength),
      rows = choices
        .grouped(maxButtonsPerRow)
        .take(maxRows - 1)
        .toList ++ Option.when(navigation.nonEmpty)(navigation.take(maxButtonsPerRow))
    )
  }

  private def isNavigation(value: SubscriptionFlowButton): Boolean =
    value.action.kind == SubscriptionFlowActionKind.Back || value.action.kind == SubscriptionFlowActionKind.Cancel

  private def button(value: SubscriptionFlowButton): Option[DiscordButton] =
    SubscriptionFlowAction
      .toId(value.action)
      .map(id => DiscordButton(id, value.label.take(maxLabelLength)))
}
