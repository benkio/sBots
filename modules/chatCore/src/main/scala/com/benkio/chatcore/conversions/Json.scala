package com.benkio.chatcore.conversions

import cats.syntax.all.*
import io.circe.Decoder
import io.circe.parser.decode

object Json {
  def decodeStringToJson[A: Decoder](kinds: String): List[A] =
    decode[List[A]](kinds)
      .handleErrorWith(_ => decode[String](kinds).flatMap(decode[List[A]]))
      .getOrElse(List.empty)
}
