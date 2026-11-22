package com.benkio.chatdiscordadapter.outbound

import com.benkio.chatcore.model.SubscriptionFlowAction
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

  def render(prompt: SubscriptionFlowPrompt): DiscordButtonMessage =
    DiscordButtonMessage(
      text = prompt.text.take(DiscordLimits.maxTextMessageLength),
      rows = prompt.buttons
        .map(_.flatMap(button).take(maxButtonsPerRow))
        .filter(_.nonEmpty)
        .take(maxRows)
    )

  private def button(value: SubscriptionFlowButton): Option[DiscordButton] =
    SubscriptionFlowAction
      .toId(value.action)
      .map(id => DiscordButton(id, value.label.take(maxLabelLength)))
}
