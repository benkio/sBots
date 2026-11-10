package com.benkio.chattelegramadapter.mocks

import cats.effect.kernel.Ref
import cats.effect.IO
import io.circe.Json
import telegramium.bots.client.Method
import telegramium.bots.high.Api

/** Records the name and the JSON payload of every executed method, answering with the `ApiMock` responses. */
final class PayloadRecordingApi(ref: Ref[IO, List[(String, Json)]]) extends Api[IO] {
  def execute[Res](method: Method[Res]): IO[Res] =
    for {
      _        <- ref.update(_ :+ (method.payload.name -> method.payload.json))
      response <- IO.fromEither(method.decoder.decodeJson(ApiMock.expectedResponses(method.payload.name)))
    } yield response
}

object PayloadRecordingApi {
  def apply(): IO[(PayloadRecordingApi, IO[List[(String, Json)]])] =
    Ref.of[IO, List[(String, Json)]](List.empty).map(ref => (new PayloadRecordingApi(ref), ref.get))
}
