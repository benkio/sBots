package com.benkio.chatcore.http

import cats.effect.Async
import cats.effect.Resource
import cats.implicits.*
import io.chrisdavenport.mules.*
import io.chrisdavenport.mules.http4s.*
import log.effect.LogWriter
import org.http4s.*
import org.http4s.client.middleware.FollowRedirect
import org.http4s.client.Client
import org.http4s.syntax.literals.*

import java.nio.file.Path
import scala.concurrent.duration.*

trait MegaClient[F[_]] {
  def fetchFile(filename: String, url: Uri): Resource[F, Path]
}

object MegaClient {
  def apply[F[_]: Async: LogWriter](
      httpClient: Client[F],
      megaApiUri: Uri = uri"https://g.api.mega.co.nz/cs"
  ): F[MegaClient[F]] = for {
    httpCache <- MemoryCache.ofSingleImmutableMap[F, (Method, Uri), CacheItem](defaultExpiration =
      TimeSpec.fromDuration(6.hours)
    )
    cachedMiddleware = CacheMiddleware.client(httpCache, CacheType.Public)
  } yield new MegaClientImpl[F](
    httpClient = cachedMiddleware(FollowRedirect(3)(httpClient)),
    megaApiUri = megaApiUri
  )

  private class MegaClientImpl[F[_]: Async: LogWriter](httpClient: Client[F], megaApiUri: Uri) extends MegaClient[F] {

    override def fetchFile(filename: String, url: Uri): Resource[F, Path] = {
      ???
    }
  }
}
