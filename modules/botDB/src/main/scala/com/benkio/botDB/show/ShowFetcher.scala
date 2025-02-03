package com.benkio.botDB.show

import cats.effect.Resource
import cats.effect.kernel.Async
import cats.implicits.*
import com.benkio.telegrambotinfrastructure.resources.db.DBShowData
import io.circe.parser.decode
import java.io.File
import scala.sys.process.*
import scala.sys.process.Process

trait ShowFetcher[F[_]]:
  def generateShowJson(source: ShowSource): F[List[DBShowData]]

object ShowFetcher {
  def apply[F[_]: Async](): ShowFetcher[F] = ShowFetcherImpl[F]()

  private class ShowFetcherImpl[F[_]: Async]() extends ShowFetcher[F] {
    override def generateShowJson(source: ShowSource): F[List[DBShowData]] =
      for
        _            <- checkDependencies
        jsonFiltered <- fetchJson(source).use(filterJson(_, source))
        json         <- parseJson(jsonFiltered)
      yield json

    private val shellDependencies: List[String] = List("yt-dlp", "jq")
    private def checkDependencies: F[Unit] =
      shellDependencies
        .traverse_(program =>
          Async[F]
            .delay(s"which $program".!)
            .flatMap(result =>
              Async[F].raiseUnless(result == 0)(
                Throwable(s"[ShowFetcher] error checking dependencies: $program is missing")
              )
            )
        )

    // Download the json from the source in a temp file
    private def fetchJson(source: ShowSource): Resource[F, File] =
      val file = File(source.outputFilePath)
      if file.exists() then Resource.pure(file)
      else
        Resource
          .pure(file)
          .evalMap(f =>
            Async[F].delay(
              Process(source.toYTDLPCommand).#>(f).!
            ) >> Async[F].pure(f)
          )

    // Parse the json temp File from the source to Json
    private def filterJson(sourceJson: File, source: ShowSource): F[String] =
      val jqCommand = Process(source.toJqCommand).#<(sourceJson)
      for jsonString <- Async[F].delay(jqCommand.!!)
      yield jsonString

      // Parse the json temp File from the source to Json
    private def parseJson(jsonString: String): F[List[DBShowData]] =
      Async[F].fromEither(decode[List[DBShowData]](jsonString))
  }
}
