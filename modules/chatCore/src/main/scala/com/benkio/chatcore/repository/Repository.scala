package com.benkio.chatcore.repository

import cats.*
import cats.data.NonEmptyList
import cats.effect.*
import cats.implicits.*
import com.benkio.chatcore.model.media.MediaResource
import com.benkio.chatcore.model.reply.MediaFile
import com.benkio.chatcore.model.Platform
import com.benkio.chatcore.model.PlatformCapabilities
import com.benkio.chatcore.model.SBotInfo.SBotId
import com.benkio.chatcore.paths.SBotPaths
import com.benkio.chatcore.repository.db.DBMediaData
import log.effect.LogWriter

import java.io.FileInputStream
import java.nio.file.Files
import java.nio.file.Path
import scala.io.Source

trait Repository[F[_]] {
  def getResourcesByKind(
      criteria: String,
      botId: SBotId
  ): Resource[F, Either[Repository.RepositoryError, NonEmptyList[NonEmptyList[MediaResource[F]]]]]
  def getResourceFile(
      mediaFile: MediaFile
  ): Resource[F, Either[Repository.RepositoryError, NonEmptyList[MediaResource[F]]]]

  /** Registers a file reference returned by a successful send of `mediaFile`, so the next sends of that platform can
    * try it first. The reference is stored with its platform; a platform that has no file references keeps the no-op.
    */
  def registerFileId(mediaFile: MediaFile, platform: Platform, fileId: String)(using Applicative[F]): F[Unit]

  /** Replaces a stored reference that failed on send with the fresh ID returned by the successful retry. Platforms
    * without file references keep the no-op.
    */
  def replaceFileId(mediaFile: MediaFile, platform: Platform, failedId: String, newId: String)(using
      Applicative[F]
  ): F[Unit]

  /** Counts a successful send of `mediaFile`: called by the adapters once the file reached the chat, never when the
    * resources are only fetched. A repository without a media count keeps the no-op.
    */
  def registerSend(@scala.annotation.unused mediaFile: MediaFile)(using Applicative[F]): F[Unit] = Applicative[F].unit

  /** The same repository, that gives only the resources the platform can use: the references to files already uploaded
    * (`MediaResourceIFile`) only if it accepts them, see
    * [[com.benkio.chatcore.model.reply.ReplyTransformer.usableSources]]. What is left without a usable resource is a
    * not found error, the same as a media that does not exist.
    */
  final def usableFor(capabilities: PlatformCapabilities): Repository[F] =
    UsableResourcesRepository(this, capabilities)
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

  /** An empty temporary file, deleted when the resource is released. */
  def tempFile[F[_]: Async](fileName: String): Resource[F, Path] = {
    val (name, ext) = fileName.span(_ != '.')
    Resource.make(Async[F].blocking(Files.createTempFile(name, ext)))(f =>
      Async[F].blocking(Files.deleteIfExists(f)).void
    )
  }

  def toTempFile[F[_]: Async](fileName: String, content: Array[Byte]): Resource[F, Path] =
    tempFile[F](fileName).evalTap(path => Async[F].blocking(Files.write(path, content)).void)

  def fileToString[F[_]: Async: LogWriter](path: Path): Resource[F, String] =
    Resource
      .fromAutoCloseable(Async[F].blocking(Source.fromFile(path.toFile())))
      .evalMap(source => Async[F].blocking(source.getLines().mkString("\n")))
      .handleErrorWith((e: Throwable) =>
        Resource.eval(LogWriter.error(s"[Repository] `fileToString` failed with $e")) >>
          Resource.pure[F, String]("")
      )

  /** Resolves a resource path against the injected project root instead of the process working directory. A leading
    * separator in `subResourceFilePath` is ignored: these values are stage-relative resource paths (some configs still
    * use a leading `/`, e.g. `/testdata/`), not filesystem absolute paths. `Path.resolve` would otherwise discard the
    * stage root and search the filesystem root.
    */
  def buildPath(subResourceFilePath: String, stage: Option[String] = None): Path = {
    val relative = subResourceFilePath.dropWhile(c => c == '/' || c == '\\')
    SBotPaths.resourcesRoot(stage.getOrElse("main")).resolve(relative)
  }

  def getResourceByteArray[F[_]: Async: LogWriter](
      resourceName: String
  ): Resource[F, Either[Repository.RepositoryError, Array[Byte]]] =
    (for {
      _ <- Resource.eval(
        LogWriter.trace(
          s"""[ResourcesAccess] getResourceByteArray Retrieve the file(${resourceName}) locally at ${getClass()
              .getResource(
                "/" + resourceName
              )}"""
        )
      )
      result <- Resource
        .fromAutoCloseable(
          Async[F].blocking(
            Option(getClass().getResourceAsStream("/" + resourceName))
              .getOrElse(new FileInputStream(resourceName))
          )
        )
        .evalMap(inputStream => Async[F].blocking(inputStream.readAllBytes()))
      _ <- Resource.eval(
        LogWriter.trace(s"[ResourcesAccess:86:48] getResourceByteArray total bytes read: ${result.size}")
      )
    } yield Right(result))
      .handleErrorWith(_ => Resource.pure(Left(Repository.RepositoryError.NoResourcesFoundByteArray(resourceName))))
}
