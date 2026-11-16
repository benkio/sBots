package com.benkio.chattelegramadapter.model

import com.benkio.chatcore.BotEngineMediaSelectionSpec
import munit.CatsEffectSuite

class TelegramMediaSelectionSpec extends CatsEffectSuite {

  test("Telegram keeps the random choice among the files of a media reply, markdown files included") {
    BotEngineMediaSelectionSpec.assertRandomChoice(TelegramCapabilities.value)
  }
}
