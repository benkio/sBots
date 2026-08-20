package com.benkio.chatcore.repository

import cats.*
import cats.data.NonEmptyList
import cats.effect.*
import cats.implicits.*
import com.benkio.chatcore.model.media.MediaResource
import com.benkio.chatcore.model.media.MediaResource.MediaResourceFile
import com.benkio.chatcore.model.reply.MediaFile
import com.benkio.chatcore.model.SBotInfo.SBotId
import log.effect.LogWriter

import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.util.jar.JarFile
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
    val jarEntries: Resource[F, List[String]] = for {
      jarPath <- Resource.eval(Async[F].blocking(Paths.get(getClass().getProtectionDomain().getCodeSource().getLocation().toURI())))
      entries <- if !Files.isRegularFile(jarPath) then Resource.pure[F, List[String]](List.empty)
      else
        Resource
          .fromAutoCloseable(Async[F].blocking(new JarFile(jarPath.toFile())))
          .evalMap(jar =>
            Async[F].blocking {
              jar.entries().asScala
                .map(_.getName)
                .filter(name => name.startsWith(criteria + "/") && name.length > criteria.length + 1)
                .toList
            }
          )
    }
    yield entries

    val fsEntries: Resource[F, List[Path]] = {
      val root = Repository.buildPath(criteria, stage)
      Resource
        .fromAutoCloseable(Async[F].blocking(Files.walk(root)))
        .evalMap(walk =>
          Async[F].blocking {
            walk.iterator().asScala.toList.tail
              .filter((fl: Path) => fl.getFileName.toString.startsWith(botId.value))
              .map((fl: Path) => root.resolve(fl.getFileName))
          }
        )
        .handleErrorWith(_ => Resource.pure(List.empty[Path]))
    }

    for {
      entriesFromJar <- jarEntries
      entriesFromFs  <- fsEntries
      result         <- NonEmptyList
        .fromList(entriesFromJar.filter(s => s.stripPrefix(s"$criteria/").startsWith(botId.value)))
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
              entriesFromFs.map(path =>
                NonEmptyList
                  .one(
                    MediaResourceFile(
                      Resource
                        .pure[F, Path](path)
                    )
                  )
              )
            )
            .map(x => Resource.pure(Right(x)))
        )
        .getOrElse(Resource.pure(Left(Repository.RepositoryError.NoResourcesFoundKind(criteria, botId))))
    }
    yield result
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
