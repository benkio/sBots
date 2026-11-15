package com.benkio.integration.integrationmunit.performance

import cats.effect.IO
import com.benkio.Alessandro0rlandoBot.Alessandro0rlandoBot
import com.benkio.main.BotRegistryEntry

class ITAlessandro0rlandoBotPerformanceSpec extends BaseBotPerformanceSpec {

  override def botEntry: BotRegistryEntry[IO] =
    BotRegistryEntry[IO](sBotInfo = Alessandro0rlandoBot.sBotInfo)

  /** Explicit, switchable performance scenario for this bot. */
  override def perfCase: PerfCase = PerfCase(
      inputText = "energia",
      description = "representative small media trigger (orl_EnergiaSchifano.sticker)"
    )

  registerPerformanceTest()
}
