package com.benkio.telegrambotinfrastructure.botcapabilities

import cats.effect.Async
import cats.effect._
import cats.implicits._
import com.benkio.telegrambotinfrastructure.botcapabilities.ResourceAccess

import java.io.File
import java.net.URL
import scala.language.postfixOps

import sys.process._

trait UrlFetcher[F[_]] {

  def fetch(url: String, filename: String): F[Fiber[F, Throwable, File]]

}

object UrlFetcher {
  def apply[F[_]: Async](): UrlFetcher[F] = new DropboxFetcherImpl[F]()
  final case class UrlFetcherException(url: String, filename: String)
      extends Throwable(s"download of $url for $filename failed")

  private class DropboxFetcherImpl[F[_]: Async]() extends UrlFetcher[F] {
    def fetch(filename: String, url: String): F[Fiber[F, Throwable, File]] = {
      val file = ResourceAccess.toTempFile(filename)
      val process = for {
        exitCode <- Async[F].delay(new URL(url) #> file !)
        _        <- if (exitCode == 0) Async[F].unit else Async[F].raiseError(UrlFetcherException(url, filename))
      } yield file
      Async[F].start(process)
    }
  }
}
