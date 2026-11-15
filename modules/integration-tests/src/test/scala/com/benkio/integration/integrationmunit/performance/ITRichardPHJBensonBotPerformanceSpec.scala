package com.benkio.integration.integrationmunit.performance

import cats.effect.IO
import com.benkio.RichardPHJBensonBot.RichardPHJBensonBot
import com.benkio.main.BotRegistryEntry

class ITRichardPHJBensonBotPerformanceSpec extends BaseBotPerformanceSpec {

  override def botEntry: BotRegistryEntry[IO] =
    BotRegistryEntry[IO](sBotInfo = RichardPHJBensonBot.sBotInfo)

  /** Explicit, switchable performance scenario for this bot. */
  override def perfCase: PerfCase = PerfCase(
      inputText = "rifiutato",
      description = "designated large/heavy media case including rphjb_RifiutatoLavorareStoriaMusicista.mp4 (~9MB-class target)"
    )

  registerPerformanceTest()
}
