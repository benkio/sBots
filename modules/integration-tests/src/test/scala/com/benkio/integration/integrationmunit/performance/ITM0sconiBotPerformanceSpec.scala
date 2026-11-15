package com.benkio.integration.integrationmunit.performance

import cats.effect.IO
import com.benkio.main.BotRegistryEntry
import com.benkio.M0sconiBot.M0sconiBot

class ITM0sconiBotPerformanceSpec extends BaseBotPerformanceSpec {

  override def botEntry: BotRegistryEntry[IO] =
    BotRegistryEntry[IO](sBotInfo = M0sconiBot.sBotInfo)

  /** Explicit, switchable performance scenario for this bot. */
  override def perfCase: PerfCase = PerfCase(
    inputText = "stronzi",
    description = "representative small media trigger (mos_Stronzi.mp3)"
  )

  registerPerformanceTest()
}
