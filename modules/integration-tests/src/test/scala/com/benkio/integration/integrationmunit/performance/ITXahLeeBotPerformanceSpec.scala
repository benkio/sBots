package com.benkio.integration.integrationmunit.performance

import cats.effect.IO
import com.benkio.main.BotRegistryEntry
import com.benkio.XahLeeBot.XahLeeBot

class ITXahLeeBotPerformanceSpec extends BaseBotPerformanceSpec {

  override def botEntry: BotRegistryEntry[IO] =
    BotRegistryEntry[IO](sBotInfo = XahLeeBot.sBotInfo)

  /** Explicit, switchable performance scenario for this bot. */
  override def perfCase: PerfCase = PerfCase(
    inputText = "/instructions",
    description = "fixed instructions command (representative command path; no message replies)"
  )

  registerPerformanceTest()
}
