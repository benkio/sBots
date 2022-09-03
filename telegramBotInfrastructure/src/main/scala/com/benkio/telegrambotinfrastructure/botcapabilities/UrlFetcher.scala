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
import scalacache._
import scalacache.caffeine.CaffeineCache

import java.io.File
import scala.concurrent.duration._

trait UrlFetcher[F[_]] {

  def fetchFromDropbox(filename: String, url: String): Resource[F, File]

}

object UrlFetcher {

  def apply[F[_]: Async](httpClient: Client[F])(implicit log: LogWriter[F]): Resource[F, UrlFetcher[F]] = for {
    httpCache <- CaffeineCache[({ type L[A] = Resource[F, A] })#L, (String, String), File]
  } yield new UrlFetcherImpl[F](
    httpClient = FollowRedirect(3)(httpClient),
    log = log,
    httpCache = httpCache
  )

  final case class UnexpectedDropboxResponse[F[_]](response: Response[F])     extends Throwable
  final case class DropboxLocationHeaderNotFound[F[_]](response: Response[F]) extends Throwable

  private class UrlFetcherImpl[F[_]: Async](
      httpClient: Client[F],
      log: LogWriter[F],
      httpCache: Cache[({ type L[A] = Resource[F, A] })#L, (String, String), File]
  ) extends UrlFetcher[F] {

    def fetchFromDropbox(filename: String, url: String): Resource[F, File] = {
      httpCache.cachingF((filename, url))(ttl = Some(12.hours)) {
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
                  .eval(
                    response.body.compile.toList.map(content => ResourceAccess.toTempFile(filename, content.toArray))
                  )

            }

            Resource.eval(log.info(s"Received response $response")) *> followup
          })
      }
    }
  }
}
