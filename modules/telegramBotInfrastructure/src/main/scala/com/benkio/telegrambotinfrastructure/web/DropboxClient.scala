package com.benkio.telegrambotinfrastructure.web

import cats.effect.Async
import cats.effect.Resource
import cats.implicits.*
import com.benkio.telegrambotinfrastructure.resources.ResourceAccess
import io.chrisdavenport.mules.*
import io.chrisdavenport.mules.http4s.*
import log.effect.LogWriter
import org.http4s.*
import org.http4s.client.middleware.FollowRedirect
import org.http4s.client.Client
import org.http4s.Method.GET
import org.typelevel.ci.*

import java.io.File
import scala.concurrent.duration.*

trait DropboxClient[F[_]] {
  def fetchFile(filename: String, url: Uri): Resource[F, File]
}

object DropboxClient {
  def apply[F[_]: Async: LogWriter](httpClient: Client[F]): F[DropboxClient[F]] = for {
    httpCache <- MemoryCache.ofSingleImmutableMap[F, (Method, Uri), CacheItem](defaultExpiration =
      TimeSpec.fromDuration(6.hours)
    )
    cachedMiddleware = CacheMiddleware.client(httpCache, CacheType.Public)
  } yield new DropboxClientImpl[F](
    httpClient = cachedMiddleware(FollowRedirect(3)(httpClient))
  )

  final case class UnexpectedDropboxResponse[F[_]](response: Response[F])     extends Throwable
  final case class DropboxLocationHeaderNotFound[F[_]](response: Response[F]) extends Throwable

  private class DropboxClientImpl[F[_]: Async: LogWriter](httpClient: Client[F]) extends DropboxClient[F] {

    def fetchFile(filename: String, url: Uri): Resource[F, File] = {
      val req = Request[F](GET, url)
      httpClient
        .run(req)
        .flatMap(response => {
          val followup: Resource[F, File] = (
            response.status,
            response.headers.get(ci"Location").flatMap(hl => Uri.fromString(hl.head.value).toOption)
          ) match { // non standard redirect because dropbox
            case (Status.Found, Some(locationUri)) => fetchFile(filename, locationUri)
            case (Status.Found, None) =>
              Resource.raiseError[F, File, Throwable](DropboxLocationHeaderNotFound[F](response))
            case _ =>
              for {
                content <- Resource.eval(response.body.compile.toList)
                _ <- Resource
                  .eval(LogWriter.info(s"[DropboxClient:56:79] received ${content.length} bytes for $filename"))
                _      <- Resource.eval(Async[F].raiseWhen(content.isEmpty)(UnexpectedDropboxResponse[F](response)))
                result <- ResourceAccess.toTempFile(filename, content.toArray)
              } yield result
          }

          Resource.eval(LogWriter.info(s"Received response $response")) *> followup
        })
    }
  }
}
