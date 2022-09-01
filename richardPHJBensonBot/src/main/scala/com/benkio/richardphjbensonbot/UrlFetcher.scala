package com.benkio.richardphjbensonbot

import cats.effect.Async
import cats.effect._
import cats.implicits._
import com.benkio.telegrambotinfrastructure.botcapabilities.ResourceAccess
import org.http4s.client.Client

import java.io.File

trait UrlFetcher[F[_]] {

  def fetch(url: String, filename: String): F[Fiber[F, Throwable, File]]

}

object UrlFetcher {
  def apply[F[_]: Async](httpClient: Client[F]): UrlFetcher[F] = new UrlFetcherImpl[F](httpClient)
  final case class UrlFetcherException(url: String, filename: String)
      extends Throwable(s"download of $url for $filename failed")
  private class UrlFetcherImpl[F[_]: Async](httpClient: Client[F]) extends UrlFetcher[F] {
    def fetch(filename: String, url: String): F[Fiber[F, Throwable, File]] = {
      val contentF: F[Array[Byte]] =
        httpClient.get[Array[Byte]](url)(response => {
          response.body.compile.toList.map(_.toArray)
        })
      Async[F].start(contentF.map(content => ResourceAccess.toTempFile(filename, content)))
    }
  }
}
