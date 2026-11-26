package com.benkio.chattelegramadapter.model

import com.benkio.chatcore.BotEngineDeliveryMediaSelectionSpec
import munit.CatsEffectSuite

class TelegramMediaSelectionSpec extends CatsEffectSuite {

  test("Telegram keeps the random choice among the files of a media reply, markdown files included") {
    BotEngineDeliveryMediaSelectionSpec.assertRandomChoice(TelegramCapabilities.value)
  }
}
