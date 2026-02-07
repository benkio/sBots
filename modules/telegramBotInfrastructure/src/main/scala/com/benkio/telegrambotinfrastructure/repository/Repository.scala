package com.benkio.telegrambotinfrastructure.repository

import cats.*
import cats.data.NonEmptyList
import cats.effect.*
import cats.implicits.*
import com.benkio.telegrambotinfrastructure.model.media.MediaResource
import com.benkio.telegrambotinfrastructure.model.reply.MediaFile
import com.benkio.telegrambotinfrastructure.model.SBotInfo.SBotId
import com.benkio.telegrambotinfrastructure.repository.db.DBMediaData
import log.effect.LogWriter

import java.io.File
import java.nio.file.Files
import fs2.io.file.Path
import java.nio.file.Paths
import scala.io.Source

trait Repository[F[_]] {
  def getResourcesByKind(
      criteria: String,
      botId: SBotId
  ): Resource[F, Either[Repository.RepositoryError, NonEmptyList[NonEmptyList[MediaResource[F]]]]]
  def getResourceFile(
      mediaFile: MediaFile
  ): Resource[F, Either[Repository.RepositoryError, NonEmptyList[MediaResource[F]]]]
}

object Repository {

  enum RepositoryError(msg: String) extends Throwable(msg) {
    case NoResourcesFoundKind(criteria: String, botId: SBotId)
        extends RepositoryError(
          s"""[Repository] getResourcesByKind returned no results for ${botId.value} - $criteria"""
        )
    case NoResourcesFoundFile(mediaFile: MediaFile)
        extends RepositoryError(s"[Repository] getResourceFile returned no results for $mediaFile")
    case NoResourcesFoundByteArray(resourceName: String)
        extends RepositoryError(s"[Repository] getResourceByteArray returned no results for $resourceName")
    case NoResourcesFoundDBMediaData(dbMediaData: DBMediaData)
        extends RepositoryError(s"[Repository] No Media Resource found in $dbMediaData")
    case DBMediaDataToMediaResourceError(dbMediaData: DBMediaData, error: Throwable)
        extends RepositoryError(
          s"[Repository] An error occurred when converting data from $dbMediaData to MediaResource: ${error.getMessage()}"
        )
  }

  def toTempFile[F[_]: Async](fileName: String, content: Array[Byte]): Resource[F, File] = Resource.make(
    Async[F].delay {
      val (name, ext) = fileName.span(_ != '.')
      val tempFile    = File.createTempFile(name, ext)
      Files.write(tempFile.toPath(), content)
      tempFile.deleteOnExit()
      tempFile
    }
  )(f => Async[F].delay(f.delete()).void)

  def fileToString[F[_]: Async: LogWriter](file: File): Resource[F, String] =
    Resource
      .make(Async[F].delay(Source.fromFile(file)))(bs => Async[F].delay(bs.close))
      .map(_.getLines.mkString("\n"))
      .handleErrorWith((e: Throwable) =>
        Resource.eval(LogWriter.error(s"[Repository] `fileToString` failed with $e")) >>
          Resource.pure[F, String]("")
      )

  def buildPath(subResourceFilePath: String, stage: Option[String] = None): Path =
    Paths.get(
      Paths.get("").toAbsolutePath().toString(),
      "src",
      stage.getOrElse("main"),
      "resources",
      subResourceFilePath
    )
}
