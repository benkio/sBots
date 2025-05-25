package com.benkio.telegrambotinfrastructure.web

import cats.effect.Async
import cats.implicits.*
import com.benkio.telegrambotinfrastructure.web.JsonParser
import io.circe.Json
import log.effect.LogWriter
import org.http4s.*
import org.http4s.circe.*
import org.http4s.client.Client

trait InvidiousClient[F[_]] {
  def fetchChannelVideos(url: Uri): F[List[Json]]
  def fetchChannelStreams(url: Uri): F[List[Json]]
  def fetchPlailistVideos(url: Uri): F[List[Json]]
}

object InvidiousClient {
  def apply[F[_]: Async: LogWriter](httpClient: Client[F], invidiousInstance: Uri): InvidiousClient[F] =
    new InvidiousClientImpl[F](
      httpClient = httpClient,
      invidiousInstance = invidiousInstance
    )

  private class InvidiousClientImpl[F[_]: Async: LogWriter](httpClient: Client[F], invidiousInstance: Uri)
      extends InvidiousClient[F] {

    private def collectInvidiousVideoIds(url: Uri): F[List[String]] =
      for
        _            <- LogWriter.info(s"[InvidiousClient] collectInvidiousVideoIds for $url")
        responseJson <- httpClient.expect[Json](url)
        (ids, maybeContinuation) = JsonParser.Invidious.parseInvidiousVideoIds(responseJson)
        continuationIds <- maybeContinuation.fold(List.empty.pure[F])(c =>
          collectInvidiousVideoIds(url.withQueryParam("continuation", c))
        )
      yield ids ++ continuationIds

    private def buildSingleVideoUrl(id: String): Uri =
      invidiousInstance / "api" / "v1" / "videos" / id

    override def fetchChannelVideos(url: Uri): F[List[Json]] =
      for {
        _          <- LogWriter.info(s"[InvidiousClient:fetchChannelVideos] for $url")
        videoIds   <- collectInvidiousVideoIds(url)
        videoJsons <- videoIds.traverse(id => httpClient.expect[Json](buildSingleVideoUrl(id)))
      } yield videoJsons

    override def fetchChannelStreams(url: Uri): F[List[Json]] = ???
    override def fetchPlailistVideos(url: Uri): F[List[Json]] = ???
  }
}
