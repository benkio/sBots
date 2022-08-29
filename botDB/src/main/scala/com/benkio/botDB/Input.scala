package com.benkio.botDB

import cats.data.NonEmptyList
import io.chrisdavenport.cormorant._
import io.chrisdavenport.cormorant.generic.semiauto._
import io.chrisdavenport.cormorant.implicits._

import java.net.URL
import scala.util.Try

final case class Input(
    filename: String,
    kind: Option[String],
    url: URL
)

object Input {

  private val headers: CSV.Headers =
    CSV.Headers(
      NonEmptyList.of(
        CSV.Header("filename"),
        CSV.Header("kind"),
        CSV.Header("url")
      )
    )

  implicit val readUrl: Read[URL] =
    Read.fromHeaders((hs, r) =>
      for {
        url <- r.l.toNev
          .get(2)
          .flatMap(field => Try(new URL(field.x)).toOption)
          .toRight(Error.DecodeFailure.single("Couldn't parse the URL"))
        _ <- hs.l.toNev
          .get(2)
          .map(h => h.value == "url")
          .toRight(Error.DecodeFailure.single("Third header should be `url`"))
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
