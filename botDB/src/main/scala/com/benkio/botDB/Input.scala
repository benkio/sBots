package com.benkio.botDB

import cats.data.NonEmptyList
import cats.implicits._

import java.net.URL
import scala.util.Try

final case class Input(
    filename: String,
    kind: Option[String],
    mime: Option[String],
    url: URL
)

object Input {

  private val headers: CSV.Headers =
    CSV.Headers(
      NonEmptyList.of(
        CSV.Header("filename"),
        CSV.Header("kind"),
        CSV.Header("mime"),
        CSV.Header("url")
      )
    )

  implicit val readUrl: Read[URL] =
    Read.fromHeaders((hs, r) =>
      for {
        _ <- hs.l.toNev
          .get(3)
          .map(h => h.value == "url")
          .toRight(Error.DecodeFailure.single("Third header should be `url`"))
        url <- r.l.toNev
          .get(3)
          .fold[Either[Error.DecodeFailure, URL]](
            Left(Error.DecodeFailure.single(s"Url Value in input CSV is required"))
          )(field =>
            Try(new URL(field.x)).toEither
              .leftMap(_ => Error.DecodeFailure.single(s"Couldn't parse the URL: ${field.x}"))
          )
      } yield url
    )(headers)

  implicit val writeURL: Write[URL] = new Write[URL] {
    def write(a: URL): CSV.Row =
      CSV.Row(NonEmptyList.one(CSV.Field(a.toString)))
  }
  implicit val lrUrl: LabelledRead[URL]  = LabelledRead.fromRead
  implicit val rrUrl: LabelledWrite[URL] = LabelledWrite.byHeaders(headers)
  implicit val lr: LabelledRead[Input]   = deriveLabelledRead
  implicit val lw: LabelledWrite[Input]  = deriveLabelledWrite

}
