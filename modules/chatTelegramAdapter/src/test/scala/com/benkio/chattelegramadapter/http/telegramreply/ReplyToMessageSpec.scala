package com.benkio.chattelegramadapter.http.telegramreply

import cats.data.NonEmptyList
import cats.effect.IO
import com.benkio.chatcore.mocks.NotifierMock.given
import com.benkio.chatcore.mocks.RepositoryMock
import com.benkio.chatcore.model.media.MediaResource
import com.benkio.chatcore.model.reply.Markdown
import com.benkio.chatcore.model.reply.Mp3File
import com.benkio.chatcore.model.reply.PagedList
import com.benkio.chatcore.model.reply.ReplyValue
import com.benkio.chatcore.model.reply.Text
import com.benkio.chatcore.model.Message
import com.benkio.chatcore.model.MessageId
import com.benkio.chatcore.Logger.given
import com.benkio.chattelegramadapter.conversions.MessageConversions.replyParameters
import com.benkio.chattelegramadapter.http.telegramreply.messagereply.TelegramMessageReply
import com.benkio.chattelegramadapter.mocks.PayloadRecordingApi
import com.benkio.chattelegramadapter.model.TelegramIds
import io.circe.Json
import munit.CatsEffectSuite
import telegramium.bots.ReplyParameters

/** Telegram replies to a message when asked to and when there is a message to reply to: what a subscription sends has
  * none.
  */
class ReplyToMessageSpec extends CatsEffectSuite {

  private def message(id: Option[MessageId]): Message =
    Message(id, 0L, TelegramIds.chatId(10L), "private", text = Some("hi"))

  private val withId: Message    = message(Some(TelegramIds.messageId(7)))
  private val withoutId: Message = message(None)

  private val repository = RepositoryMock(getResourceFileHandler =
    _ => IO.pure(Right(NonEmptyList.one(MediaResource.MediaResourceIFile("a file id"))))
  )

  private val values: List[(String, ReplyValue)] = List(
    "a text"       -> Text("text"),
    "a markdown"   -> Markdown("**markdown**"),
    "a paged list" -> PagedList.topTwenty(List(Mp3File("a.mp3")), page = 0),
    "a media file" -> Mp3File("a.mp3")
  )

  /** Whether what was sent to the chat (not the chat actions) is a reply to a message. */
  private def sentAsReply(value: ReplyValue, msg: Message, replyToMessage: Boolean): IO[List[Boolean]] =
    for {
      apiAndRecording <- PayloadRecordingApi()
      (api, recorded) = apiAndRecording
      _ <- TelegramMessageReply
        .sendReplyValue[IO](value, msg, repository, replyToMessage)(using summon, summon, api, summon)
      calls <- recorded
    } yield calls.collect {
      case (name, payload: Json) if name.startsWith("send") && name != "sendChatAction" =>
        payload.hcursor.downField("reply_parameters").succeeded
    }

  test("the reply parameters are those of the message, only when asked and only if there is a message") {
    assertEquals(withId.replyParameters(replyToMessage = true), Some(ReplyParameters(Some(7))))
    assertEquals(withId.replyParameters(replyToMessage = false), None)
    assertEquals(withoutId.replyParameters(replyToMessage = true), None)
    assertEquals(withoutId.replyParameters(replyToMessage = false), None)
  }

  values.foreach { case (name, value) =>
    test(s"$name replies to the message when asked to and the message has an id") {
      sentAsReply(value = value, msg = withId, replyToMessage = true).map(sent => assertEquals(sent, List(true)))
    }

    test(s"$name does not reply to the message when it is not asked to") {
      sentAsReply(value = value, msg = withId, replyToMessage = false).map(sent => assertEquals(sent, List(false)))
    }

    test(s"$name is sent without a reply when there is no message to reply to, even if asked") {
      sentAsReply(value = value, msg = withoutId, replyToMessage = true).map(sent => assertEquals(sent, List(false)))
    }
  }
}
