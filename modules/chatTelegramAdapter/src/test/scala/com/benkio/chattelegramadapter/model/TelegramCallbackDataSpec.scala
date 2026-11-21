package com.benkio.chattelegramadapter.model

import com.benkio.chatcore.model.reply.PagedListAction
import com.benkio.chattelegramadapter.Arbitraries.given
import com.benkio.chattelegramadapter.Generators.mediaCallbackPayloadGen
import munit.ScalaCheckSuite
import org.scalacheck.Gen
import org.scalacheck.Prop.*

class TelegramCallbackDataSpec extends ScalaCheckSuite {

  // the ids stay under PagedListAction.maxIdLength
  private val shortIdGen: Gen[String] = Gen.alphaNumStr.suchThat(_.nonEmpty).map(_.take(40))

  property("toCallbackKey / apply should round-trip for generated CallbackData") {
    forAll { (callbackData: PagedListAction) =>
      assertEquals(TelegramCallbackData.decode(callbackData.toId.get), Some(callbackData))
    }
  }

  property("PreviousPage and NextPage encode with commandKey and page") {
    forAll { (callbackData: PagedListAction) =>
      callbackData match {
        case PagedListAction.PreviousPage(page, commandKey) =>
          assertEquals(callbackData.toId.get, s"previousPage-${commandKey.asString}-$page")
        case PagedListAction.NextPage(page, commandKey) =>
          assertEquals(callbackData.toId.get, s"nextPage-${commandKey.asString}-$page")
        case PagedListAction.Media(value) =>
          assertEquals(callbackData.toId.get, s"media-$value")
        case PagedListAction.Show(value, timestamp) =>
          assertEquals(callbackData.toId.get, s"""show-$value|${timestamp.getOrElse("")}""")
      }
    }
  }

  property("non-action strings are rejected") {
    forAll(mediaCallbackPayloadGen.suchThat(s => s != "media" && s != "show")) { (raw: String) =>
      assertEquals(TelegramCallbackData.decode(raw), None)
    }
  }

  property("malformed pagination-like strings are rejected") {
    forAll(Gen.alphaNumStr.suchThat(_.nonEmpty)) { (suffix: String) =>
      val raw = s"previousPage-notACommand-$suffix"
      assertEquals(TelegramCallbackData.decode(raw), None)
    }
  }

  property("media/show callback payloads preserve dashes in IDs") {
    forAll(shortIdGen, shortIdGen) { (prefix, suffix) =>
      val payload = s"$prefix-$suffix"
      assertEquals(TelegramCallbackData.decode(s"media-$payload"), Some(PagedListAction.Media(payload)))
      assertEquals(TelegramCallbackData.decode(s"show-$payload"), Some(PagedListAction.Show(payload, None)))
    }
  }

  test("show callback payload should parse youtube timestamp when present") {
    assertEquals(
      TelegramCallbackData.decode("show-abc-123|1m2s"),
      Some(PagedListAction.Show("abc-123", Some("1m2s")))
    )
  }
}
