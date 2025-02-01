package com.benkio.telegrambotinfrastructure.web

import cats.effect.Async
import cats.effect.Resource
import cats.implicits.*
import com.benkio.telegrambotinfrastructure.resources.ResourceAccess
import io.chrisdavenport.mules.*
import io.chrisdavenport.mules.http4s.*
import log.effect.LogWriter
import org.http4s.Method.GET
import org.http4s.*
import org.http4s.client.Client
import org.http4s.client.middleware.FollowRedirect
import org.typelevel.ci.*

import java.io.File
import scala.concurrent.duration.*

trait UrlFetcher[F[_]] {

  def fetchFromDropbox(filename: String, url: Uri): Resource[F, File]

}

object UrlFetcher {
  def apply[F[_]: Async](httpClient: Client[F])(using log: LogWriter[F]): F[UrlFetcher[F]] = for {
    httpCache <- MemoryCache.ofSingleImmutableMap[F, (Method, Uri), CacheItem](defaultExpiration =
      TimeSpec.fromDuration(6.hours)
    )
    cachedMiddleware = CacheMiddleware.client(httpCache, CacheType.Public)
  } yield new UrlFetcherImpl[F](
    httpClient = cachedMiddleware(FollowRedirect(3)(httpClient)),
    log = log
  )

  final case class UnexpectedDropboxResponse[F[_]](response: Response[F])     extends Throwable
  final case class DropboxLocationHeaderNotFound[F[_]](response: Response[F]) extends Throwable

  private class UrlFetcherImpl[F[_]: Async](httpClient: Client[F], log: LogWriter[F]) extends UrlFetcher[F] {

    def fetchFromDropbox(filename: String, url: Uri): Resource[F, File] = {
      val req = Request[F](GET, url)
      httpClient
        .run(req)
        .flatMap(response => {
          val followup: Resource[F, File] = (
            response.status,
            response.headers.get(ci"Location").flatMap(hl => Uri.fromString(hl.head.value).toOption)
          ) match { // non standard redirect because dropbox
            case (Status.Found, Some(locationUri)) => fetchFromDropbox(filename, locationUri)
            case (Status.Found, None) =>
              Resource.raiseError[F, File, Throwable](DropboxLocationHeaderNotFound[F](response))
            case _ =>
              Resource
                .make(
                  for {
                    content <- response.body.compile.toList
                    _       <- Async[F].raiseWhen(content.isEmpty)(UnexpectedDropboxResponse[F](response))
                    result <- Async[F].delay(
                      ResourceAccess.toTempFile(filename, content.toArray)
                    )
                  } yield result
                )((f: File) => Async[F].delay(f.delete()).void)

          }

          Resource.eval(log.info(s"Received response $response")) *> followup
        })
    }
  }
}
