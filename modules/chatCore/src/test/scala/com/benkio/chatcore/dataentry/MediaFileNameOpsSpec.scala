package com.benkio.chatcore.dataentry

import munit.ScalaCheckSuite
import org.scalacheck.Gen
import org.scalacheck.Prop.forAll

class MediaFileNameOpsSpec extends ScalaCheckSuite {
  test("getPrefix should return the expected prefix") {
    val test = List(
      "rphjb_06.mp4"                                    -> "rphjb_06",
      "rphjb_06Gif.mp4"                                 -> "rphjb_06",
      "rphjb_06.md"                                     -> "rphjb_06",
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

  property("getPrefix should keep filename unchanged when no extension exists") {
    forAll(Gen.alphaNumStr.suchThat(_.nonEmpty)) { (filename: String) =>
      assertEquals(MediaFileNameOps.getPrefix(filename), filename)
    }
  }

  property("getPrefix should drop sticker suffix using the dedicated branch") {
    forAll(Gen.alphaNumStr.suchThat(_.nonEmpty)) { (prefix: String) =>
      val filename = s"${prefix}.sticker"
      assertEquals(MediaFileNameOps.getPrefix(filename), prefix)
    }
  }

  property("getPrefix should remove only the last extension segment for unknown extensions") {
    val validPrefix = Gen.alphaNumStr.suchThat(_.nonEmpty)
    val unknownExtension = Gen.oneOf("tar", "zip", "abc", "tmp")
    forAll(validPrefix, unknownExtension) { (prefix: String, extension: String) =>
      val filename = s"$prefix.backup.$extension"
      assertEquals(MediaFileNameOps.getPrefix(filename), s"$prefix.backup")
    }
  }
}
