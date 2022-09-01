package com.benkio.richardphjbensonbot

import cats.effect.Async
import cats.implicits._
import com.benkio.telegrambotinfrastructure.botcapabilities.ResourceAccess
import org.http4s._
import org.http4s.client.Client
import org.typelevel.ci._

import java.io.File

trait UrlFetcher[F[_]] {

  def fetchFromDropbox(url: String, filename: String): F[File]

}

object UrlFetcher {
  def apply[F[_]: Async](httpClient: Client[F]): UrlFetcher[F] = new UrlFetcherImpl[F](httpClient)

  final case class UnexpectedDropboxResponse[F[_]](response: Response[F])     extends Throwable
  final case class DropboxLocationHeaderNotFound[F[_]](response: Response[F]) extends Throwable

  private class UrlFetcherImpl[F[_]: Async](httpClient: Client[F]) extends UrlFetcher[F] {

    def fetchFromDropbox(filename: String, url: String): F[File] = {
      httpClient.get(url)(response =>
        response.status match {
          case Status.Ok =>
            response.as[Array[Byte]].map(filecontent => ResourceAccess.toTempFile(filename, filecontent))
          case Status.Found => {
            val locationHeaderValue: F[String] = response.headers
              .get(CIString("location"))
              .fold(Async[F].raiseError[String](DropboxLocationHeaderNotFound(response)))(nel =>
                Async[F].pure(nel.head.value)
              )

            locationHeaderValue.flatMap(locationHeader =>
              if (locationHeader.startsWith("http"))
                fetchFromDropbox(filename, locationHeader)
              else fetchFromDropbox(filename, "https://www.dropbox.com" + locationHeader)
            )
          }
          case _ => Async[F].raiseError(UnexpectedDropboxResponse(response))
        }
      )
    }
  }
}
