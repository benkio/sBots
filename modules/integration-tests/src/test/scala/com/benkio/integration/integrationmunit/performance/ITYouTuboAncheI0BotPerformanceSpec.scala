package com.benkio.integration.integrationmunit.performance

import cats.effect.IO
import com.benkio.main.BotRegistryEntry
import com.benkio.YouTuboAncheI0Bot.YouTuboAncheI0Bot

class ITYouTuboAncheI0BotPerformanceSpec extends BaseBotPerformanceSpec {

  override def botEntry: BotRegistryEntry[IO] =
    BotRegistryEntry[IO](sBotInfo = YouTuboAncheI0Bot.sBotInfo)

  /** Explicit, switchable performance scenario for this bot. */
  override def perfCase: PerfCase = PerfCase(
    inputText = "coda così lunga",
    description = "representative small media trigger (ytai_CodaLungaGif.mp4)"
  )

  registerPerformanceTest()
}
