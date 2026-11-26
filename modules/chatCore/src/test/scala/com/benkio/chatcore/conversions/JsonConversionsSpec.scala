package com.benkio.chatcore.conversions

import cats.syntax.either.*
import com.benkio.chatcore.conversions.JsonConversions.SrtDecoder.given
import io.circe.syntax.*
import io.circe.Json
import io.circe.KeyDecoder
import io.circe.KeyEncoder
import munit.FunSuite

import scala.concurrent.duration.*

class JsonConversionsSpec extends FunSuite {

  test("decodeStringToJson decodes a JSON array") {
    assertEquals(JsonConversions.decodeStringToJson[Int]("[1,2,3]"), List(1, 2, 3))
  }

  test("decodeStringToJson decodes an array that is itself encoded as a JSON string") {
    assertEquals(JsonConversions.decodeStringToJson[String]("\"[\\\"a\\\",\\\"b\\\"]\""), List("a", "b"))
  }

  test("decodeStringToJson returns an empty list for input that is not an array") {
    assertEquals(JsonConversions.decodeStringToJson[Int]("not json"), List.empty[Int])
    assertEquals(JsonConversions.decodeStringToJson[Int]("""{"a":1}"""), List.empty[Int])
  }

  test("the SRT encoder writes the timestamp and the decoder reads it back") {
    val duration = 1.hour + 2.minutes + 3.seconds + 4.millis

    assertEquals(duration.asJson, Json.fromString("01:02:03,004"))
    assertEquals(duration.asJson.as[FiniteDuration], Right(duration))
  }

  test("the SRT decoder rejects a timestamp with the wrong format") {
    val decoded = Json.fromString("not a timestamp").as[FiniteDuration]

    assertEquals(
      decoded.leftMap(_.message),
      Left("Invalid SRT timestamp format: 'not a timestamp'. Expected format: hh:mm:ss,SSS")
    )
  }

  test("the SRT key codec round-trips a FiniteDuration") {
    val duration = 2.minutes + 5.seconds
    val key      = KeyEncoder[FiniteDuration].apply(duration)

    assertEquals(key, "00:02:05,000")
    assertEquals(KeyDecoder[FiniteDuration].apply(key), Some(duration))
  }
}
