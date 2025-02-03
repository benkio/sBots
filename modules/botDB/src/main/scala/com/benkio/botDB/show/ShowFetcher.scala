package com.benkio.botDB.show

import cats.MonadThrow
import org.http4s.Uri
import io.circe.Json
import scala.sys.process.*
import cats.implicits.*

trait ShowFetcher[F[_]]:
  def generateShowJson(source: Uri): F[Json]

object ShowFetcher {
  def apply[F[_]: MonadThrow](): ShowFetcher[F] = ShowFetcherImpl[F]()

  private class ShowFetcherImpl[F[_]: MonadThrow]() extends ShowFetcher[F] {
    override def generateShowJson(source: Uri): F[Json] =
      checkDependencies >> ???

    private val shellDependencies = List("yt-dlp", "jq")
    private def checkDependencies: F[Unit] =
        shellDependencies
          .traverse_(program =>
             MonadThrow[F].raiseUnless(s"which $program".! == 0)(
               Throwable(s"[ShowFetcher] error checking dependencies: $program is missing")
          )
      )
  }
}
