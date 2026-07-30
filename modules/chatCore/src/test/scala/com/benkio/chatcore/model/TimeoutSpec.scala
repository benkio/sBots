package com.benkio.chatcore.model

import com.benkio.chatcore.model.SBotInfo.SBotId
import com.benkio.chatcore.Arbitraries.given
import com.benkio.chatcore.Generators.timeoutHhMmSsGen
import munit.ScalaCheckSuite
import org.scalacheck.Arbitrary.arbitrary
import org.scalacheck.Gen
import org.scalacheck.Prop.*

import java.time.Instant
import scala.concurrent.duration.*

class TimeoutSpec extends ScalaCheckSuite {

  property("timeStringToDuration parses HH:MM:SS") {
    forAll(timeoutHhMmSsGen) { case (formatted, expected) =>
      assertEquals(Timeout.timeStringToDuration(formatted), expected)
    }
  }

  property("Timeout.apply(chatId, botId, string) succeeds for valid HH:MM:SS") {
    forAll(timeoutHhMmSsGen, arbitrary[SBotId]) { case ((formatted, expected), botId) =>
      val actual = Timeout(ChatId(1L), botId, formatted)
      assert(actual.isRight)
      assertEquals(actual.toOption.get.timeoutValue, expected)
      assertEquals(actual.toOption.get.chatId.value, 1L)
      assertEquals(actual.toOption.get.botId, botId)
    }
  }

  property("Timeout.apply(chatId, botId, string) fails for invalid input") {
    forAll(Gen.alphaStr.suchThat(s => s.isEmpty || !s.matches("\\d{2}:\\d{2}:\\d{2}"))) { (invalid: String) =>
      assert(Timeout(ChatId(1L), SBotId("botId"), invalid).isLeft)
    }
  }

  property("default Timeout has zero duration") {
    forAll { (botId: SBotId) =>
      val actual = Timeout(ChatId(1L), botId)
      assertEquals(actual.timeoutValue, 0.millis)
      assertEquals(actual.botId, botId)
    }
  }

  property("isExpired is true when lastInteraction is older than timeoutValue") {
    forAll(Gen.choose(1L, 10_000L)) { (timeoutMs: Long) =>
      val timeout = Timeout(
        ChatId(1L),
        SBotId("botId"),
        timeoutMs.millis,
        Instant.now().minusMillis(timeoutMs + 50)
      )
      assert(Timeout.isExpired(timeout))
    }
  }

  property("isExpired is false when lastInteraction is recent relative to timeoutValue") {
    forAll(Gen.choose(1_000L, 60_000L)) { (timeoutMs: Long) =>
      val timeout = Timeout(ChatId(1L), SBotId("botId"), timeoutMs.millis, Instant.now())
      assert(!Timeout.isExpired(timeout))
    }
  }
}
