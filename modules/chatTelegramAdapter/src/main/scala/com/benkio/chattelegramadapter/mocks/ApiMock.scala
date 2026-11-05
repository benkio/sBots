package com.benkio.chattelegramadapter.mocks

import cats.effect.IO
import io.circe.syntax.*
import io.circe.Json
import telegramium.bots.client.Method
import telegramium.bots.high.Api
import telegramium.bots.Chat
import telegramium.bots.CirceImplicits.*
import telegramium.bots.Message

final class ApiMock extends Api[IO] {
  override def execute[Res](method: Method[Res]): IO[Res] =
    IO(ApiMock.expectedResponses(method.payload.name))
      .flatMap(json => IO.fromEither(method.decoder.decodeJson(json)))
}

object ApiMock {
  given Api[IO] = new ApiMock

  private def messageResponse(text: String): Json =
    Message(
      0,
      date = 0,
      chat = Chat(0, `type` = "private"),
      text = Some(text)
    ).asJson

  val expectedResponses: Map[String, Json] = Map(
    "sendChatAction"  -> Json.False,
    "deleteMessage"   -> true.asJson,
    "sendMessage"     -> messageResponse("[apiMock] sendMessage reply"),
    "sendRichMessage" -> messageResponse("[apiMock] sendRichMessage reply"),
    "sendVideo"       -> messageResponse("[apiMock] sendVideo reply"),
    "sendAudio"       -> messageResponse("[apiMock] sendMp3 reply"),
    "sendAnimation"   -> messageResponse("[apiMock] sendGif reply"),
    "sendPhoto"       -> messageResponse("[apiMock] sendPhoto reply"),
    "sendDocument"    -> messageResponse("[apiMock] sendDocument reply")
  )
}
