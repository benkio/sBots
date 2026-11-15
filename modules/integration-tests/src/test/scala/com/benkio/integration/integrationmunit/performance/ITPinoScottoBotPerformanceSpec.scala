package com.benkio.integration.integrationmunit.performance

import cats.effect.IO
import com.benkio.PinoScottoBot.PinoScottoBot
import com.benkio.main.BotRegistryEntry

class ITPinoScottoBotPerformanceSpec extends BaseBotPerformanceSpec {

  override def botEntry: BotRegistryEntry[IO] =
    BotRegistryEntry[IO](sBotInfo = PinoScottoBot.sBotInfo)

  /** Explicit, switchable performance scenario for this bot. */
  override def perfCase: PerfCase = PerfCase(
      inputText = "al posto del",
      description = "representative small media bundle trigger (3 media files)"
    )

  registerPerformanceTest()
}
