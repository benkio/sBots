package com.benkio.chattelegramadapter.http.telegramreply.callbackreply

import cats.effect.kernel.Ref
import cats.effect.IO
import cats.syntax.all.*
import com.benkio.chatcore.mocks.BackgroundJobManagerMock
import com.benkio.chatcore.mocks.DBLayerMock
import com.benkio.chatcore.mocks.NotifierMock.given
import com.benkio.chatcore.mocks.RepositoryMock
import com.benkio.chatcore.model.reply.PagedListAction
import com.benkio.chatcore.model.CommandKey
import com.benkio.chatcore.model.SBotInfo.SBotId
import com.benkio.chatcore.repository.db.DBShowData
import com.benkio.chatcore.EffectfulCallback
import com.benkio.chatcore.Logger.given
import com.benkio.chattelegramadapter.mocks.RecordingApi
import munit.CatsEffectSuite
import munit.ScalaCheckSuite
import org.scalacheck.Gen
import org.scalacheck.Prop.*
import telegramium.bots.high.Api
import telegramium.bots.Chat
import telegramium.bots.InlineKeyboardButton
import telegramium.bots.InlineKeyboardMarkup
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
          callbackData = PagedListAction.Show(dbShowData.show_id, None),
          repository = RepositoryMock(),
          allCommandRepliesData = List.empty,
          backgroundJobManager = BackgroundJobManagerMock.mock(),
          effectfulCallbacks = Map.empty[String, EffectfulCallback[IO]],
          dbLayer = dbLayer,
          ttl = None
        ) *> methodNames.get
      }
    } yield assertEquals(calls, List("sendChatAction", "sendMessage", "deleteMessage"))
  }

  test("a stale page press does not edit the keyboard") {
    for {
      methodNames <- Ref.of[IO, List[String]](List.empty)
      calls       <- {
        given Api[IO] = new RecordingApi(methodNames)
        val staleMsg  = TelegramMessage(
          messageId = 1,
          date = 0,
          chat = Chat(id = 1, `type` = "private"),
          replyMarkup = Some(
            InlineKeyboardMarkup(
              List(List(InlineKeyboardButton(text = "item", callbackData = PagedListAction.Media("a.mp3").toId)))
            )
          )
        )
        TelegramCallbackReply.reply[IO](
          msg = staleMsg,
          callbackData = PagedListAction.NextPage(0, CommandKey.Random),
          repository = RepositoryMock(),
          allCommandRepliesData = List.empty,
          backgroundJobManager = BackgroundJobManagerMock.mock(),
          effectfulCallbacks = Map.empty[String, EffectfulCallback[IO]],
          dbLayer = DBLayerMock.mock(botId = SBotId("sbot")),
          ttl = None
        ) *> methodNames.get
      }
    } yield assertEquals(calls, List.empty)
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

  property("isStalePagePress is false when the button is still in the keyboard") {
    forAll(Gen.choose(0, 10)) { (page: Int) =>
      val action = PagedListAction.NextPage(page, CommandKey.Random)
      val msg    = TelegramMessage(
        messageId = 1,
        date = 0,
        chat = Chat(id = 1, `type` = "private"),
        replyMarkup = Some(
          InlineKeyboardMarkup(List(List(InlineKeyboardButton(text = "next", callbackData = action.toId))))
        )
      )
      assert(!TelegramCallbackReply.isStalePagePress(msg, action))
    }
  }

  property("isStalePagePress is true when the button was removed from the keyboard") {
    forAll(Gen.choose(0, 10)) { (page: Int) =>
      val action = PagedListAction.NextPage(page, CommandKey.Random)
      val msg    = TelegramMessage(
        messageId = 1,
        date = 0,
        chat = Chat(id = 1, `type` = "private"),
        replyMarkup = Some(
          InlineKeyboardMarkup(
            List(
              List(
                InlineKeyboardButton(
                  text = "prev",
                  callbackData = PagedListAction.PreviousPage(page, CommandKey.Random).toId
                )
              )
            )
          )
        )
      )
      assert(TelegramCallbackReply.isStalePagePress(msg, action))
    }
  }

  property("isStalePagePress is false without keyboard information") {
    val action = PagedListAction.NextPage(0, CommandKey.Random)
    val msg    = TelegramMessage(messageId = 1, date = 0, chat = Chat(id = 1, `type` = "private"))
    assert(!TelegramCallbackReply.isStalePagePress(msg, action))
  }
}
