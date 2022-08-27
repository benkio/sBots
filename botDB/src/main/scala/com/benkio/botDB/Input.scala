package com.benkio.botDB

import java.net.URL
import io.chrisdavenport.cormorant._
import io.chrisdavenport.cormorant.generic.semiauto._
import io.chrisdavenport.cormorant.implicits._
import scala.util.Try

import cats.data.NonEmptyList

final case class Input(
  filename: String,
  kind: Option[String],
  url: URL
)

object Input {

  private val headers: CSV.Headers =
    CSV.Headers(NonEmptyList.of(
      CSV.Header("filename"),
      CSV.Header("kind"),
      CSV.Header("url")
    ))

  implicit val readUrl: Read[URL] =
    Read.fromHeaders(
      (_, r) => r.l.toNev.get(2).flatMap(field => Try(new URL(field.x)).toOption).toRight(Error.DecodeFailure.single("Couldn't parse the URL")))(headers)

  implicit val writeURL: Write[URL] = new Write[URL] {
    def write(a: URL): CSV.Row =
      CSV.Row(NonEmptyList.one(CSV.Field(a.toString)))
  }
  implicit val lrUrl: LabelledRead[URL] = LabelledRead.fromRead
  implicit val rrUrl: LabelledWrite[URL] = LabelledWrite.byHeaders(headers)
  implicit val lr: LabelledRead[Input] = deriveLabelledRead
  implicit val lw: LabelledWrite[Input] = deriveLabelledWrite

}
