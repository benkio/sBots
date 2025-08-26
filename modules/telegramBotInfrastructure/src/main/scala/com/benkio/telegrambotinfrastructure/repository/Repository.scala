package com.benkio.telegrambotinfrastructure.repository

import cats.*
import cats.data.NonEmptyList
import cats.effect.*
import cats.implicits.*
import com.benkio.telegrambotinfrastructure.model.media.MediaResource
import com.benkio.telegrambotinfrastructure.model.reply.MediaFile
import com.benkio.telegrambotinfrastructure.repository.db.DBMediaData
import log.effect.LogWriter

import java.io.File
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import scala.io.Source

trait Repository[F[_]] {
  def getResourcesByKind(criteria: String): Resource[F, NonEmptyList[NonEmptyList[MediaResource[F]]]]
  def getResourceFile(mediaFile: MediaFile): Resource[F, NonEmptyList[MediaResource[F]]]
}

object Repository {

  final case class NoResourcesFoundKind(criteria: String)
      extends Throwable(s"[ResourcesAccess] getResourcesByKind returned no resunlts for $criteria")
  final case class NoResourcesFoundDBMediaData(dbMediaData: DBMediaData)
      extends Throwable(s"[ResourcesAccess] no Media Resource found in $dbMediaData")

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
        Resource.eval(LogWriter.error(s"[ResourcesAccess] `fileToString` failed with $e")) >>
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
