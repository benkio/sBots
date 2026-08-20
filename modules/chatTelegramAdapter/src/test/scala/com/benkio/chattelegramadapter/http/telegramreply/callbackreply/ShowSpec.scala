package com.benkio.chattelegramadapter.http.telegramreply.callbackreply

import cats.effect.kernel.Ref
import cats.effect.IO
import com.benkio.chatcore.mocks.DBLayerMock
import com.benkio.chatcore.model.SBotInfo.SBotId
import com.benkio.chatcore.repository.db.DBShowData
import com.benkio.chatcore.Logger.given
import com.benkio.chattelegramadapter.mocks.RecordingApi
import munit.CatsEffectSuite
import telegramium.bots.high.Api
import telegramium.bots.Chat
import telegramium.bots.MaybeInaccessibleMessage
import telegramium.bots.Message as TelegramMessage

class ShowSpec extends CatsEffectSuite {

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

  test("reply should send show text and delete callback message when show exists") {
    for {
      methodNames <- Ref.of[IO, List[String]](List.empty)
      calls       <- {
        given Api[IO] = new RecordingApi(methodNames)
        val dbLayer   = DBLayerMock.mock(botId = SBotId("sbot"), shows = List(dbShowData))
        Show.reply[IO](msg = callbackMsg, showId = dbShowData.show_id, showDb = dbLayer.dbShow) *> methodNames.get
      }
    } yield assertEquals(calls, List("sendChatAction", "sendMessage", "deleteMessage"))
  }

  test("reply should send fallback text and delete callback message when show is missing") {
    for {
      methodNames <- Ref.of[IO, List[String]](List.empty)
      calls       <- {
        given Api[IO] = new RecordingApi(methodNames)
        val dbLayer   = DBLayerMock.mock(botId = SBotId("sbot"), shows = List.empty)
        Show.reply[IO](msg = callbackMsg, showId = "missing-show-id", showDb = dbLayer.dbShow) *> methodNames.get
      }
    } yield assertEquals(calls, List("sendChatAction", "sendMessage", "deleteMessage"))
  }
}
