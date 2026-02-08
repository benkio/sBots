package com.benkio.telegrambotinfrastructure.repository

import cats.*
import cats.data.NonEmptyList
import cats.effect.*
import cats.implicits.*
import com.benkio.telegrambotinfrastructure.model.media.MediaResource
import com.benkio.telegrambotinfrastructure.model.media.MediaResource.MediaResourceFile
import com.benkio.telegrambotinfrastructure.model.reply.MediaFile
import com.benkio.telegrambotinfrastructure.model.SBotInfo.SBotId
import log.effect.LogWriter

import java.io.File
import java.nio.file.Files
import java.nio.file.Path
import java.util.jar.JarFile
import scala.collection.mutable.ArrayBuffer
import scala.jdk.CollectionConverters.*

object ResourcesRepository {
  def fromResources[F[_]: Async: LogWriter](stage: Option[String] = None): ResourceRepository[F] =
    new ResourceRepository[F](stage)
}

class ResourceRepository[F[_]: Async: LogWriter](stage: Option[String] = None) extends Repository[F] {

  def getResourcesByKind(
      criteria: String,
      botId: SBotId
  ): Resource[F, Either[Repository.RepositoryError, NonEmptyList[NonEmptyList[MediaResource[F]]]]] = {
    val jarFile                     = new File(getClass().getProtectionDomain().getCodeSource().getLocation().getPath())
    val result: ArrayBuffer[String] = new ArrayBuffer();

    // from https://stackoverflow.com/questions/11012819/how-can-i-get-a-resource-folder-from-inside-my-jar-file
    if jarFile.isFile() then { // Run with JAR file
      val jar     = new JarFile(jarFile)
      val entries = jar.entries() // gives ALL entries in jar
      while entries.hasMoreElements() do {
        val name = entries.nextElement().getName()
        if name
          .startsWith(criteria + "/") && name.length > (criteria.length + 1)
        then { // filter according to the path
          result += name
        }
      }
      jar.close()
    }

    NonEmptyList
      .fromList(result.filter(s => s.stripPrefix(s"$criteria/").startsWith(botId.value)).toList)
      .map(
        _.traverse(s =>
          Repository
            .getResourceByteArray(s)
            .map(contentEither =>
              contentEither.map(content =>
                NonEmptyList
                  .one(
                    MediaResourceFile(Repository.toTempFile(s.stripPrefix(s"$criteria/"), content))
                  )
              )
            )
        ).map(_.sequence)
      )
      .orElse(
        NonEmptyList
          .fromList(
            Files
              .walk(Repository.buildPath(criteria, stage))
              .iterator
              .asScala
              .toList
              .tail
              .filter((fl: Path) => fl.getFileName.toString.startsWith(botId.value))
              .map((fl: Path) =>
                NonEmptyList
                  .one(
                    MediaResourceFile(
                      Resource
                        .pure[F, File](
                          File(Repository.buildPath(criteria, stage).toString + "/" + fl.getFileName.toString)
                        )
                    )
                  )
              )
          )
          .map(x => Resource.pure(Right(x)))
      )
      .getOrElse(Resource.pure(Left(Repository.RepositoryError.NoResourcesFoundKind(criteria, botId))))
  }

  override def getResourceFile(
      mediaFile: MediaFile
  ): Resource[F, Either[Repository.RepositoryError, NonEmptyList[MediaResource[F]]]] = {
    for {
      _                 <- Resource.eval(LogWriter.info(s"getResourceFile for $mediaFile"))
      fileContentEither <- Repository.getResourceByteArray(mediaFile.filepath)
      tempFileEither    <- fileContentEither.fold(
        e => Resource.pure(Left(e)),
        fileContent => Repository.toTempFile(mediaFile.filename, fileContent).map(Right(_))
      )
    } yield tempFileEither.map(tempFile => NonEmptyList.one(MediaResourceFile(Resource.pure(tempFile))))
  }
}
