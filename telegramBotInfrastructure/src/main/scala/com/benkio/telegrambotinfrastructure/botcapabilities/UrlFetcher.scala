package com.benkio.telegrambotinfrastructure.botcapabilities

import cats.effect.Async
import cats.effect.Resource
import cats.implicits._
import com.benkio.telegrambotinfrastructure.botcapabilities.ResourceAccess
import log.effect.LogWriter
import org.http4s.Method.GET
import org.http4s._
import org.http4s.client.Client
import org.http4s.client.middleware.FollowRedirect
import org.typelevel.ci._

import java.io.File

trait UrlFetcher[F[_]] {

  def fetchFromDropbox(filename: String, url: String): Resource[F, File]

}

object UrlFetcher {
  def apply[F[_]: Async](httpClient: Client[F])(implicit log: LogWriter[F]): UrlFetcher[F] =
    new UrlFetcherImpl[F](httpClient = FollowRedirect(3)(httpClient), log = log)

  final case class UnexpectedDropboxResponse[F[_]](response: Response[F])     extends Throwable
  final case class DropboxLocationHeaderNotFound[F[_]](response: Response[F]) extends Throwable

  private class UrlFetcherImpl[F[_]: Async](httpClient: Client[F], log: LogWriter[F]) extends UrlFetcher[F] {

    def fetchFromDropbox(filename: String, url: String): Resource[F, File] = {
      val req = Request[F](GET, Uri.unsafeFromString(url))
      httpClient
        .run(req)
        .flatMap(response => {
          val followup: Resource[F, File] = (
            response.status == Status.Found,
            response.headers.get(ci"Location")
          ) match { // non standard redirect because dropbox
            case (true, Some(loc)) => fetchFromDropbox(filename, loc.head.value)
            case (true, None) => Resource.raiseError[F, File, Throwable](DropboxLocationHeaderNotFound[F](response))
            case (false, _) =>
              Resource
                .eval(response.body.compile.toList.map(content => ResourceAccess.toTempFile(filename, content.toArray)))

          }

          Resource.eval(log.info(s"Received response $response")) *> followup
        })
    }
  }
}