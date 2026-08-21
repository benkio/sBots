package com.benkio.chattelegramadapter.mocks

import cats.effect.kernel.Ref
import cats.effect.IO
import telegramium.bots.client.Method
import telegramium.bots.high.Api

final class RecordingApi(ref: Ref[IO, List[String]]) extends Api[IO] {
  def execute[Res](method: Method[Res]): IO[Res] =
    for {
      _        <- ref.update(_ :+ method.payload.name)
      response <- IO.fromEither(method.decoder.decodeJson(ApiMock.expectedResponses(method.payload.name)))
    } yield response
}
