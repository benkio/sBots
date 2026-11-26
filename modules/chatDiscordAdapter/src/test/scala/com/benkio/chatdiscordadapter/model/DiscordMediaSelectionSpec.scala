package com.benkio.chatdiscordadapter.model

import com.benkio.chatcore.BotEngineDeliveryMediaSelectionSpec
import munit.CatsEffectSuite

class DiscordMediaSelectionSpec extends CatsEffectSuite {

  test("Discord keeps the random choice among the files of a media reply, markdown files included") {
    BotEngineDeliveryMediaSelectionSpec.assertRandomChoice(DiscordCapabilities.value)
  }
}
