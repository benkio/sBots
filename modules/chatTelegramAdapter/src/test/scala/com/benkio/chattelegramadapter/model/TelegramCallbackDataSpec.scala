package com.benkio.chattelegramadapter.model

import com.benkio.chatcore.model.reply.PagedListAction
import com.benkio.chatcore.model.SubscriptionFlowAction
import com.benkio.chatcore.model.SubscriptionSchedulePreset
import com.benkio.chatcore.model.SubscriptionType
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
        case PagedListAction.Trigger(value) =>
          assertEquals(callbackData.toId.get, s"trigger-$value")
      }
    }
  }

  property("non-action strings are rejected") {
    forAll(mediaCallbackPayloadGen.suchThat(s => s != "media" && s != "show" && s != "trigger")) { (raw: String) =>
      assertEquals(TelegramCallbackData.decode(raw), None)
    }
  }

  property("malformed pagination-like strings are rejected") {
    forAll(Gen.alphaNumStr.suchThat(_.nonEmpty)) { (suffix: String) =>
      val raw = s"previousPage-notACommand-$suffix"
      assertEquals(TelegramCallbackData.decode(raw), None)
    }
  }

  property("media/show/trigger callback payloads preserve dashes in IDs") {
    forAll(shortIdGen, shortIdGen) { (prefix, suffix) =>
      val payload = s"$prefix-$suffix"
      assertEquals(TelegramCallbackData.decode(s"media-$payload"), Some(PagedListAction.Media(payload)))
      assertEquals(TelegramCallbackData.decode(s"show-$payload"), Some(PagedListAction.Show(payload, None)))
      assertEquals(TelegramCallbackData.decode(s"trigger-$payload"), Some(PagedListAction.Trigger(payload)))
    }
  }

  test("show callback payload should parse youtube timestamp when present") {
    assertEquals(
      TelegramCallbackData.decode("show-abc-123|1m2s"),
      Some(PagedListAction.Show("abc-123", Some("1m2s")))
    )
  }

  test("decodeFlow decodes every guided-flow button id") {
    val actions = List(
      SubscriptionFlowAction.back,
      SubscriptionFlowAction.cancel,
      SubscriptionFlowAction.confirm,
      SubscriptionFlowAction.chooseType(SubscriptionType.Show),
      SubscriptionFlowAction.chooseType(SubscriptionType.Trigger),
      SubscriptionFlowAction.choosePreset(SubscriptionSchedulePreset.Hourly),
      SubscriptionFlowAction.enterTarget("")
    )
    actions.foreach { action =>
      val id = SubscriptionFlowAction.toId(action).get
      assertEquals(TelegramCallbackData.decodeFlow(id), Some(action))
      assertEquals(TelegramCallbackData.decode(id), None)
    }
  }

  test("decodeFlow rejects ids that are not guided-flow button ids") {
    List("media-x", "flow-unknown", "flow-preset-nope", "flow-" + ("x" * 120)).foreach { id =>
      assertEquals(TelegramCallbackData.decodeFlow(id), None)
    }
  }
}
