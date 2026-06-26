package com.benkio.chatcore.conversions

import cats.syntax.all.*
import io.circe.parser.decode
import io.circe.Decoder
import io.circe.Encoder
import io.circe.Json
import io.circe.KeyDecoder
import io.circe.KeyEncoder

import scala.concurrent.duration.*
import scala.util.matching.Regex

object JsonConversions {
  def decodeStringToJson[A: Decoder](kinds: String): List[A] =
    decode[List[A]](kinds)
      .handleErrorWith(_ => decode[String](kinds).flatMap(decode[List[A]]))
      .getOrElse(List.empty)

  object SrtDecoder {

    // Regex pattern to capture hh:mm:ss,SSS
    private val SrtTimePattern: Regex =
      """(\d{2}):(\d{2}):(\d{2}),(\d{3})""".r

    private def finiteDurationToSrtTimestamp(finiteDuration: FiniteDuration): String = {
      val hours   = finiteDuration.toHours
      val minutes = finiteDuration.toMinutes % 60
      val seconds = finiteDuration.toSeconds % 60
      val millis  = finiteDuration.toMillis  % 1000

      f"$hours%02d:$minutes%02d:$seconds%02d,$millis%03d"
    }

    // Custom Circe Decoder for FiniteDuration
    implicit val finiteDurationDecoder: Decoder[FiniteDuration] =
      Decoder.decodeString.emap { str =>
        str match {
          case SrtTimePattern(h, m, s, ms) =>
            val totalMs =
              (h.toLong.hours.toMillis) +
                (m.toLong.minutes.toMillis) +
                (s.toLong.seconds.toMillis) +
                ms.toLong

            Right(totalMs.milliseconds)

          case _ =>
            Left(s"Invalid SRT timestamp format: '$str'. Expected format: hh:mm:ss,SSS")
        }
      }

    // Custom Circe Decoder for FiniteDuration
    implicit val finiteDurationEncoder: Encoder[FiniteDuration] =
      Encoder.instance[FiniteDuration](finiteDuration => Json.fromString(finiteDurationToSrtTimestamp(finiteDuration)))

    implicit val finiteDurationKeyEncoder: KeyEncoder[FiniteDuration] =
      KeyEncoder[String].contramap(finiteDurationToSrtTimestamp)

    implicit val finiteDurationKeyDecoder: KeyDecoder[FiniteDuration] =
      KeyDecoder.instance(str =>
        finiteDurationDecoder
          .decodeJson(Json.fromString(str.stripPrefix("\"").stripSuffix("\"")))
          .toOption
      )
  }
}
