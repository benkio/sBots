package com.benkio.integration.integrationmunit.performance

import cats.effect.IO
import com.benkio.main.BotRegistryEntry
import com.benkio.ABarberoBot.ABarberoBot

class ITABarberoBotPerformanceSpec extends BaseBotPerformanceSpec {

  override def botEntry: BotRegistryEntry[IO] =
    BotRegistryEntry[IO](sBotInfo = ABarberoBot.sBotInfo)

  /** Explicit, switchable performance scenario for this bot. */
  override def perfCase: PerfCase = PerfCase(
    inputText = "particelle cadaveriche",
    description = "representative small media trigger (abar_ParticelleCadaveriche.mp3)"
  )

  registerPerformanceTest()
}
