package com.benkio.telegrambotinfrastructure.dataentry

import munit.*

class MediaFileNameOpsSpec extends FunSuite {
  test("getPrefix should return the expected prefix") {
    val test = List(
      "rphjb_06.mp4"                                    -> "rphjb_06",
      "rphjb_06Gif.mp4"                                 -> "rphjb_06",
      "rphjb_2Orecchie1Bocca.mp4"                       -> "rphjb_2Orecchie1Bocca",
      "rphjb_2Orecchie1Bocca2.mp4"                      -> "rphjb_2Orecchie1Bocca2",
      "rphjb_3Minuti.mp4"                               -> "rphjb_3Minuti",
      "rphjb_4SoloTempiInTestaOrologiSfuggonoPolsi.mp4" -> "rphjb_4SoloTempiInTestaOrologiSfuggonoPolsi",
      "rphjb_5DitaRivolta.mp3"                          -> "rphjb_5DitaRivolta",
      "rphjb_5DitaRivolta.mp4"                          -> "rphjb_5DitaRivolta",
      "rphjb_5DitaRivoltaGif.mp4"                       -> "rphjb_5DitaRivolta",
      "rphjb_911TorriGemelleBinLaden.mp3"               -> "rphjb_911TorriGemelleBinLaden",
      "rphjb_911TorriGemelleBinLaden.mp4"               -> "rphjb_911TorriGemelleBinLaden"
    )
    test.foreach { case (input, expected) =>
      assertEquals(MediaFileNameOps.getPrefix(input), expected)
    }
  }
}
