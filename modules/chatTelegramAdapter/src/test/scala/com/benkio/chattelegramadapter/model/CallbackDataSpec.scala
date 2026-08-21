package com.benkio.chattelegramadapter.model

import com.benkio.chattelegramadapter.Arbitraries.given
import com.benkio.chattelegramadapter.Generators.mediaCallbackPayloadGen
import munit.ScalaCheckSuite
import org.scalacheck.Gen
import org.scalacheck.Prop.*

class CallbackDataSpec extends ScalaCheckSuite {

  property("toCallbackKey / apply should round-trip for generated CallbackData") {
    forAll { (callbackData: CallbackData) =>
      assertEquals(CallbackData(callbackData.toCallbackKey), callbackData)
    }
  }

  property("PreviousPage and NextPage encode with commandKey and page") {
    forAll { (callbackData: CallbackData) =>
      callbackData match {
        case CallbackData.PreviousPage(page, commandKey) =>
          assertEquals(callbackData.toCallbackKey, s"previousPage-${commandKey.asString}-$page")
        case CallbackData.NextPage(page, commandKey) =>
          assertEquals(callbackData.toCallbackKey, s"nextPage-${commandKey.asString}-$page")
        case CallbackData.Media(value) =>
          assertEquals(callbackData.toCallbackKey, s"media-$value")
        case CallbackData.Show(value) =>
          assertEquals(callbackData.toCallbackKey, s"show-$value")
      }
    }
  }

  property("non-pagination strings decode as Media") {
    forAll(mediaCallbackPayloadGen) { (raw: String) =>
      assertEquals(CallbackData(raw), CallbackData.Media(raw))
    }
  }

  property("malformed pagination-like strings fall back to Media") {
    forAll(Gen.alphaNumStr.suchThat(_.nonEmpty)) { (suffix: String) =>
      val raw = s"previousPage-notACommand-$suffix"
      assertEquals(CallbackData(raw), CallbackData.Media(raw))
    }
  }

  property("media/show callback payloads preserve dashes in IDs") {
    forAll(Gen.alphaNumStr.suchThat(_.nonEmpty), Gen.alphaNumStr.suchThat(_.nonEmpty)) { (prefix, suffix) =>
      val payload = s"$prefix-$suffix"
      assertEquals(CallbackData(s"media-$payload"), CallbackData.Media(payload))
      assertEquals(CallbackData(s"show-$payload"), CallbackData.Show(payload))
    }
  }
}
