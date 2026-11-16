package com.benkio.chatdiscordadapter.model

import com.benkio.chatcore.BotEngineMediaSelectionSpec
import munit.CatsEffectSuite

class DiscordMediaSelectionSpec extends CatsEffectSuite {

  test("Discord keeps the random choice among the files of a media reply, markdown files included") {
    BotEngineMediaSelectionSpec.assertRandomChoice(DiscordCapabilities.value)
  }
}
