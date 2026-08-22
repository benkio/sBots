package com.benkio.chattelegramadapter.http.telegramreply.callbackreply

import cats.effect.kernel.Ref
import cats.effect.IO
import cats.syntax.all.*
import com.benkio.chatcore.mocks.BackgroundJobManagerMock
import com.benkio.chatcore.mocks.DBLayerMock
import com.benkio.chatcore.mocks.RepositoryMock
import com.benkio.chatcore.model.reply.ReplyValue
import com.benkio.chatcore.model.Message as ModelMessage
import com.benkio.chatcore.model.SBotInfo.SBotId
import com.benkio.chatcore.repository.db.DBShowData
import com.benkio.chatcore.Logger.given
import com.benkio.chattelegramadapter.mocks.RecordingApi
import com.benkio.chattelegramadapter.model.CallbackData
import munit.CatsEffectSuite
import munit.ScalaCheckSuite
import org.scalacheck.Gen
import org.scalacheck.Prop.*
import telegramium.bots.high.Api
import telegramium.bots.Chat
import telegramium.bots.MaybeInaccessibleMessage
import telegramium.bots.Message as TelegramMessage

class TelegramCallbackReplyRoutingSpec extends CatsEffectSuite {

  private val callbackMsg: MaybeInaccessibleMessage =
    TelegramMessage(
      messageId = 1,
      date = 0,
      chat = Chat(id = 1, `type` = "private"),
      text = Some("callback")
    )

  private val dbShowData: DBShowData = DBShowData(
    show_id = "show-id-1",
    bot_id = "sbot",
    show_title = "Test show title",
    show_upload_date = "2025-04-24T12:01:24.000Z",
    show_duration = 10,
    show_description = Some("Test show description"),
    show_is_live = false,
    show_origin_automatic_caption = Some("caption"),
    show_origin_automatic_caption_srt = """[["00:00:01,000","caption"]]"""
  )

  test("reply should route show callbacks through Show.reply and send message + delete callback") {
    for {
      methodNames <- Ref.of[IO, List[String]](List.empty)
      calls       <- {
        given Api[IO] = new RecordingApi(methodNames)
        val dbLayer   = DBLayerMock.mock(
          botId = SBotId("sbot"),
          shows = List(dbShowData)
        )
        TelegramCallbackReply.reply[IO](
          msg = callbackMsg,
          callbackData = CallbackData.Show(dbShowData.show_id, None),
          repository = RepositoryMock(),
          allCommandRepliesData = List.empty,
          backgroundJobManager = BackgroundJobManagerMock.mock(),
          effectfulCallbacks = Map.empty[String, ModelMessage => IO[ReplyValue]],
          dbLayer = dbLayer,
          ttl = None
        ) *> methodNames.get
      }
    } yield assertEquals(calls, List("sendChatAction", "sendMessage", "deleteMessage"))
  }
}

class TelegramCallbackReplySpec extends ScalaCheckSuite {

  property("nextPage increments the current page") {
    forAll(Gen.choose(-10, 100)) { (page: Int) =>
      assertEquals(TelegramCallbackReply.nextPage(page), page + 1)
    }
  }

  property("previousPage decrements and floors at zero") {
    forAll(Gen.choose(-10, 100)) { (page: Int) =>
      assertEquals(TelegramCallbackReply.previousPage(page), (page - 1).max(0))
    }
  }
}
