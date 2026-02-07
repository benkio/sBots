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

import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileInputStream
import java.nio.file.Files
import fs2.io.file.Path
import java.util.jar.JarFile
import scala.collection.mutable.ArrayBuffer
import scala.jdk.CollectionConverters.*
import scala.util.Try

object ResourcesRepository {
  def fromResources[F[_]: Async: LogWriter](stage: Option[String] = None): ResourceRepository[F] =
    new ResourceRepository[F](stage)
}

class ResourceRepository[F[_]: Async: LogWriter](stage: Option[String] = None) extends Repository[F] {

  private def getResourceByteArray(resourceName: String): Resource[F, Either[Repository.RepositoryError, Array[Byte]]] =
    (for {
      _ <- Resource.eval(
        LogWriter.info(
          s"""[ResourcesAccess] getResourceByteArray Retrieve the file locally at ${getClass().getResource(
              "/" + resourceName
            )}"""
        )
      )
      fis <- Resource.make(Async[F].fromTry {
        val stream = getClass().getResourceAsStream("/" + resourceName)
        Try(if stream == null then new FileInputStream(resourceName) else stream)
      })(fis => Async[F].delay(fis.close()))
      bais <- Resource.make(Async[F].delay(new ByteArrayOutputStream()))(bais => Async[F].delay(bais.close()))
    } yield (fis, bais))
      .evalMap { case (fis, bais) =>
        val tempArray = new Array[Byte](16384)
        for {
          firstChunk <- Async[F].delay(fis.read(tempArray, 0, tempArray.length))
          _          <- Monad[F].iterateWhileM(firstChunk)(chunk =>
            Async[F].delay(bais.write(tempArray, 0, chunk)) *> Async[F].delay(fis.read(tempArray, 0, tempArray.length))
          )(_ != -1)
          result = bais.toByteArray()
          _ <- LogWriter.info(s"[ResourcesAccess:86:48] getResourceByteArray total bytes read: ${result.size}")
        } yield result
      }
      .map(Right(_))
      .handleErrorWith(_ => Resource.pure(Left(Repository.RepositoryError.NoResourcesFoundByteArray(resourceName))))

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
          getResourceByteArray(s).map(contentEither =>
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
      fileContentEither <- getResourceByteArray(mediaFile.filepath)
      tempFileEither    <- fileContentEither.fold(
        e => Resource.pure(Left(e)),
        fileContent => Repository.toTempFile(mediaFile.filename, fileContent).map(Right(_))
      )
    } yield tempFileEither.map(tempFile => NonEmptyList.one(MediaResourceFile(Resource.pure(tempFile))))
  }
}
