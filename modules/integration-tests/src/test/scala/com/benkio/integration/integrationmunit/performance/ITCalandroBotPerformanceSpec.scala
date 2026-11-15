package com.benkio.integration.integrationmunit.performance

import cats.effect.IO
import com.benkio.main.BotRegistryEntry
import com.benkio.CalandroBot.CalandroBot

class ITCalandroBotPerformanceSpec extends BaseBotPerformanceSpec {

  override def botEntry: BotRegistryEntry[IO] =
    BotRegistryEntry[IO](sBotInfo = CalandroBot.sBotInfo)

  /** Explicit, switchable performance scenario for this bot. */
  override def perfCase: PerfCase = PerfCase(
    inputText = "sbrighi",
    description = "representative text-only trigger"
  )

  registerPerformanceTest()
}
