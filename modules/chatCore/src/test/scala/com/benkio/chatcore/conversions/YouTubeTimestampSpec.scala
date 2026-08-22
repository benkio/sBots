package com.benkio.chatcore.conversions

import com.benkio.chatcore.Arbitraries.given
import com.benkio.chatcore.InvalidYoutubeTimestamp
import munit.FunSuite
import munit.ScalaCheckEffectSuite
import org.scalacheck.Prop.forAll

import scala.concurrent.duration.*

class YouTubeTimestampSpec extends FunSuite with ScalaCheckEffectSuite {

  test("finiteDurationToYoutubeTimestamp should format seconds only") {
    assertEquals(YouTubeTimestamp.finiteDurationToYoutubeTimestamp(45.seconds), "45s")
    assertEquals(YouTubeTimestamp.finiteDurationToYoutubeTimestamp(0.seconds), "0s")
  }

  test("finiteDurationToYoutubeTimestamp should format minutes and seconds") {
    assertEquals(YouTubeTimestamp.finiteDurationToYoutubeTimestamp(4.minutes + 5.seconds), "4m5s")
    assertEquals(YouTubeTimestamp.finiteDurationToYoutubeTimestamp(4.minutes), "4m")
  }

  test("finiteDurationToYoutubeTimestamp should format hours, minutes and seconds") {
    assertEquals(
      YouTubeTimestamp.finiteDurationToYoutubeTimestamp(1.hour + 2.minutes + 3.seconds),
      "1h2m3s"
    )
    assertEquals(YouTubeTimestamp.finiteDurationToYoutubeTimestamp(2.hours), "2h")
  }

  test("finiteDurationToYoutubeTimestamp should output a valid youtube timestamp") {
    forAll { (duration: FiniteDuration) =>
      val rendered = YouTubeTimestamp.finiteDurationToYoutubeTimestamp(duration)

      assert(rendered.matches("""\d+h\d+m\d+s|\d+h\d+m|\d+h\d+s|\d+h|\d+m\d+s|\d+m|\d+s"""))
      assert(!rendered.contains("-"))
    }
  }

  test("youtubeTimestampToFiniteDuration should parse generated valid youtube timestamps") {
    forAll { (duration: FiniteDuration) =>
      val normalized = duration.toSeconds.seconds
      val rendered   = YouTubeTimestamp.finiteDurationToYoutubeTimestamp(normalized)
      assertEquals(YouTubeTimestamp.youtubeTimestampToFiniteDuration(rendered), Some(normalized))
    }
  }

  test("youtubeTimestampToFiniteDuration should reject generated invalid youtube timestamps") {
    forAll { (timestamp: InvalidYoutubeTimestamp) =>
      assertEquals(YouTubeTimestamp.youtubeTimestampToFiniteDuration(timestamp.value), None)
    }
  }
}
