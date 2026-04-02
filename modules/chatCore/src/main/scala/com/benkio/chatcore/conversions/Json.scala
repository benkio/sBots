package com.benkio.chatcore.conversions

import cats.syntax.all.*
import io.circe.parser.decode
import io.circe.Decoder

object Json {
  def decodeStringToJson[A: Decoder](kinds: String): List[A] =
    decode[List[A]](kinds)
      .handleErrorWith(_ => decode[String](kinds).flatMap(decode[List[A]]))
      .getOrElse(List.empty)
}
