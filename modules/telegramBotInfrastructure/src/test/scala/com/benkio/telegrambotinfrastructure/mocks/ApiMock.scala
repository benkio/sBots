package com.benkio.telegrambotinfrastructure.mocks

import cats.effect.IO
import io.circe.*
import io.circe.syntax.*
import telegramium.bots.client.Method
import telegramium.bots.high.Api
import telegramium.bots.Chat
import telegramium.bots.CirceImplicits.*
import telegramium.bots.Message

class ApiMock extends Api[IO] {
  def execute[Res](method: Method[Res]): IO[Res] =
    IO(ApiMock.expectedResponses(method.payload.name))
      .flatMap(json => IO.fromEither(method.decoder.decodeJson(json)))
}

object ApiMock {
  given Api[IO]                            = new ApiMock
  val expectedResponses: Map[String, Json] = Map(
    "sendChatAction" -> Json.False,
    "deleteMessage"  -> true.asJson,
    "sendMessage"    -> Message(
      0,
      date = 0,
      chat = Chat(0, `type` = "private"),
      text = Some("[apiMock] sendMessage reply")
    ).asJson,
    "sendVideo" -> Message(
      0,
      date = 0,
      chat = Chat(0, `type` = "private"),
      text = Some("[apiMock] sendVideo reply")
    ).asJson,
    "sendAudio" -> Message(
      0,
      date = 0,
      chat = Chat(0, `type` = "private"),
      text = Some("[apiMock] sendMp3 reply")
    ).asJson,
    "sendAnimation" -> Message(
      0,
      date = 0,
      chat = Chat(0, `type` = "private"),
      text = Some("[apiMock] sendGif reply")
    ).asJson,
    "sendPhoto" -> Message(
      0,
      date = 0,
      chat = Chat(0, `type` = "private"),
      text = Some("[apiMock] sendPhoto reply")
    ).asJson,
    "sendDocument" -> Message(
      0,
      date = 0,
      chat = Chat(0, `type` = "private"),
      text = Some("[apiMock] sendDocument reply")
    ).asJson
  )
}
