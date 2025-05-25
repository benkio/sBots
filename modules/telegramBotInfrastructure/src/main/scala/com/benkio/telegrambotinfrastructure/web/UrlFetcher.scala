package com.benkio.telegrambotinfrastructure.web

import annotation.unused
import cats.effect.Async
import cats.effect.Resource
import cats.implicits.*
import com.benkio.telegrambotinfrastructure.resources.ResourceAccess
import com.benkio.telegrambotinfrastructure.web.JsonParser
import io.chrisdavenport.mules.*
import io.chrisdavenport.mules.http4s.*
import io.circe.Json
import log.effect.LogWriter
import org.http4s.*
import org.http4s.circe.*
import org.http4s.client.middleware.FollowRedirect
import org.http4s.client.Client
import org.http4s.Method.GET
import org.typelevel.ci.*

import java.io.File
import scala.concurrent.duration.*

trait UrlFetcher[F[_]] {
  def fetchFileFromDropbox(filename: String, url: Uri): Resource[F, File]
  def fetchInvidiousChannelVideos(url: Uri): F[List[Json]]
  def fetchInvidiousChannelStreams(url: Uri): F[List[Json]]
  def fetchInvidiousPlailistVideos(url: Uri): F[List[Json]]
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

  private class UrlFetcherImpl[F[_]: Async: LogWriter](httpClient: Client[F], log: LogWriter[F]) extends UrlFetcher[F] {

    def fetchFileFromDropbox(filename: String, url: Uri): Resource[F, File] = {
      val req = Request[F](GET, url)
      httpClient
        .run(req)
        .flatMap(response => {
          val followup: Resource[F, File] = (
            response.status,
            response.headers.get(ci"Location").flatMap(hl => Uri.fromString(hl.head.value).toOption)
          ) match { // non standard redirect because dropbox
            case (Status.Found, Some(locationUri)) => fetchFileFromDropbox(filename, locationUri)
            case (Status.Found, None) =>
              Resource.raiseError[F, File, Throwable](DropboxLocationHeaderNotFound[F](response))
            case _ =>
              for {
                content <- Resource.eval(response.body.compile.toList)
                _       <- Resource.eval(log.info(s"[UrlFetcher:56:79] received ${content.length} bytes for $filename"))
                _       <- Resource.eval(Async[F].raiseWhen(content.isEmpty)(UnexpectedDropboxResponse[F](response)))
                result  <- ResourceAccess.toTempFile(filename, content.toArray)
              } yield result
          }

          Resource.eval(log.info(s"Received response $response")) *> followup
        })
    }

    @unused
    private def collectInvidiousVideoIds(url: Uri): F[List[String]] =
      for
        _            <- LogWriter.info(s"[UrlFetcher] collectInvidiousVideoIds for $url")
        responseJson <- httpClient.expect[Json](url)
        (ids, maybeContinuation) = JsonParser.Invidious.parseInvidiousVideoIds(responseJson)
        continuationIds <- maybeContinuation.fold(List.empty.pure[F])(c =>
          collectInvidiousVideoIds(url.withQueryParam("continuation", c))
        )
      yield ids ++ continuationIds

    override def fetchInvidiousChannelVideos(url: Uri): F[List[Json]]  = ???
    override def fetchInvidiousChannelStreams(url: Uri): F[List[Json]] = ???
    override def fetchInvidiousPlailistVideos(url: Uri): F[List[Json]] = ???
  }
}
