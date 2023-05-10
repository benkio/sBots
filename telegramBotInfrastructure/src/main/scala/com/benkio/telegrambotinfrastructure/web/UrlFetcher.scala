package com.benkio.telegrambotinfrastructure.web

import cats.effect.Async
import cats.effect.Resource
import cats.implicits._
import cats.Applicative
import cats.ApplicativeError
import com.benkio.telegrambotinfrastructure.resources.ResourceAccess
import io.chrisdavenport.mules._
import io.chrisdavenport.mules.http4s._
import log.effect.LogWriter
import org.http4s.Method.GET
import org.http4s._
import org.http4s.client.Client
import org.http4s.client.middleware.FollowRedirect
import org.typelevel.ci._

import java.io.File
import scala.concurrent.duration._

trait UrlFetcher[F[_]] {

  def fetchFromDropbox(filename: String, url: String): Resource[F, File]

}

object UrlFetcher {
  def apply[F[_]: Async](httpClient: Client[F])(implicit log: LogWriter[F]): F[UrlFetcher[F]] = for {
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

    def fetchFromDropbox(filename: String, url: String): Resource[F, File] = {
      val req = Request[F](GET, Uri.unsafeFromString(url))
      httpClient
        .run(req)
        .flatMap(response => {
          val followup: Resource[F, File] = (
            response.status,
            response.headers.get(ci"Location")
          ) match { // non standard redirect because dropbox
            case (Status.Found, Some(loc)) => fetchFromDropbox(filename, loc.head.value)
            case (Status.Found, None) =>
              Resource.raiseError[F, File, Throwable](DropboxLocationHeaderNotFound[F](response))
            case _ =>
              Resource
                .eval(
                  response.body.compile.toList.flatMap(content =>
                    if (content.isEmpty)
                      ApplicativeError[F, Throwable].raiseError[File](UnexpectedDropboxResponse[F](response))
                    else Applicative[F].pure(ResourceAccess.toTempFile(filename, content.toArray))
                  )
                )

          }

          Resource.eval(log.info(s"Received response $response")) *> followup
        })
    }
  }
}
