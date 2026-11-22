package com.benkio.chatdiscordadapter.outbound

import com.benkio.chatcore.model.ChatId
import com.benkio.chatcore.model.Platform
import com.benkio.chatcore.model.SBotInfo.SBotId
import com.benkio.chatcore.model.SubscriptionDraftStep
import com.benkio.chatcore.model.SubscriptionFlowAction
import com.benkio.chatcore.model.SubscriptionFlowButton
import com.benkio.chatcore.model.SubscriptionFlowKey
import com.benkio.chatcore.model.SubscriptionFlowPrompt
import com.benkio.chatcore.model.SubscriptionSchedulePreset
import munit.FunSuite

class DiscordSubscriptionFlowLayoutSpec extends FunSuite {

  private val key = SubscriptionFlowKey(ChatId(Platform.Discord, "9"), SBotId("bot"), "alice")

  /** The same rows the core builds for the schedule step: one per preset, then back and cancel. */
  private def prompt(step: SubscriptionDraftStep, buttons: List[List[SubscriptionFlowButton]]): SubscriptionFlowPrompt =
    SubscriptionFlowPrompt(key, step, "Choose", buttons)

  private val navigation =
    List(
      List(SubscriptionFlowButton("back", SubscriptionFlowAction.back)),
      List(SubscriptionFlowButton("cancel", SubscriptionFlowAction.cancel))
    )

  test("the five presets, back and cancel all survive Discord's row budget") {
    val buttons = SubscriptionSchedulePreset.values
      .map(preset => List(SubscriptionFlowButton(preset.label, SubscriptionFlowAction.choosePreset(preset))))
      .toList ++ navigation
    val rendered = DiscordSubscriptionFlowLayout.render(prompt(SubscriptionDraftStep.Schedule, buttons))

    assert(rendered.rows.size <= DiscordSubscriptionFlowLayout.maxRows)
    assert(rendered.rows.forall(_.size <= DiscordSubscriptionFlowLayout.maxButtonsPerRow))
    val ids = rendered.rows.flatten.map(_.id)
    assertEquals(ids.count(_ == "flow-back"), 1)
    assertEquals(ids.count(_ == "flow-cancel"), 1)
    assertEquals(ids.count(_.startsWith("flow-preset-")), SubscriptionSchedulePreset.values.size)
  }

  test("every step keeps its navigation buttons") {
    val steps = List(
      SubscriptionDraftStep.Type   -> List(List(SubscriptionFlowButton("cancel", SubscriptionFlowAction.cancel))),
      SubscriptionDraftStep.Target -> (List(
        List(SubscriptionFlowButton("random", SubscriptionFlowAction.enterTarget("")))
      ) ++ navigation),
      SubscriptionDraftStep.Confirm -> (List(
        List(SubscriptionFlowButton("confirm", SubscriptionFlowAction.confirm))
      ) ++ navigation)
    )
    steps.foreach { case (step, buttons) =>
      val ids = DiscordSubscriptionFlowLayout.render(prompt(step, buttons)).rows.flatten.map(_.id)
      assert(ids.contains("flow-cancel"), s"$step has no cancel")
      if step != SubscriptionDraftStep.Type then assert(ids.contains("flow-back"), s"$step has no back")
    }
  }
}
