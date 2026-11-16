package com.benkio.integration.integrationmunit.performance

import cats.effect.IO
import com.benkio.main.BotRegistryEntry
import com.benkio.VittorioSgarbiBot.VittorioSgarbiBot

class ITVittorioSgarbiBotPerformanceSpec extends BaseBotPerformanceSpec {

  override def botEntry: BotRegistryEntry[IO] =
    BotRegistryEntry[IO](sBotInfo = VittorioSgarbiBot.sBotInfo)

  /** Explicit, switchable performance scenario for this bot. */
  override def perfCase: PerfCase = PerfCase(
    inputText = "sei sporco",
    description = "representative small media trigger (sgar_VaffanculoDeficiente.mp3)"
  )

  registerPerformanceTest()
}
