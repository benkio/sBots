package com.benkio.telegrambotinfrastructure.resources

import cats.*
import cats.data.NonEmptyList
import cats.effect.*
import cats.implicits.*
import com.benkio.telegrambotinfrastructure.model.media.Media
import com.benkio.telegrambotinfrastructure.model.media.MediaResource
import com.benkio.telegrambotinfrastructure.model.media.MediaResource.MediaResourceFile
import com.benkio.telegrambotinfrastructure.model.media.MediaResource.MediaResourceIFile
import com.benkio.telegrambotinfrastructure.model.reply.MediaFile
import com.benkio.telegrambotinfrastructure.resources.db.DBMedia
import com.benkio.telegrambotinfrastructure.resources.db.DBMediaData
import com.benkio.telegrambotinfrastructure.web.DropboxClient
import log.effect.LogWriter
import org.http4s.Uri

import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileInputStream
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.util.jar.JarFile
import scala.collection.mutable.ArrayBuffer
import scala.io.Source
import scala.jdk.CollectionConverters.*
import scala.util.Try

trait ResourceAccess[F[_]] {
  def getResourcesByKind(criteria: String): Resource[F, NonEmptyList[NonEmptyList[MediaResource[F]]]]
  def getResourceFile(mediaFile: MediaFile): Resource[F, NonEmptyList[MediaResource[F]]]
}

object ResourceAccess {

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

  def fromResources[F[_]: Async: LogWriter](stage: Option[String] = None): fromResources[F] =
    new fromResources[F](stage)
  class fromResources[F[_]: Async: LogWriter](stage: Option[String] = None) extends ResourceAccess[F] {

    private def getResourceByteArray(resourceName: String): Resource[F, Array[Byte]] =
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
      } yield (fis, bais)).evalMap { case (fis, bais) =>
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

    def getResourcesByKind(criteria: String): Resource[F, NonEmptyList[NonEmptyList[MediaResource[F]]]] = {
      val jarFile = new File(getClass().getProtectionDomain().getCodeSource().getLocation().getPath())
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
        .fromList(result.toList)
        .map(
          _.traverse(s =>
            getResourceByteArray(s).map(content =>
              NonEmptyList
                .one(
                  MediaResourceFile(toTempFile(s.stripPrefix(s"$criteria/"), content))
                )
            )
          )
        )
        .getOrElse(
          Resource
            .eval[F, NonEmptyList[NonEmptyList[MediaResourceFile[F]]]](
              Async[F].fromOption(
                NonEmptyList.fromList(
                  Files
                    .walk(buildPath(criteria, stage))
                    .iterator
                    .asScala
                    .toList
                    .tail
                    .map((fl: Path) =>
                      NonEmptyList
                        .one(
                          MediaResourceFile(
                            Resource.pure(File(buildPath(criteria, stage).toString + "/" + fl.getFileName.toString))
                          )
                        )
                    )
                ),
                NoResourcesFoundKind(criteria)
              )
            )
        )
    }

    override def getResourceFile(
        mediaFile: MediaFile
    ): Resource[F, NonEmptyList[MediaResource[F]]] = {
      for {
        _           <- Resource.eval(LogWriter.info(s"getResourceFile for $mediaFile"))
        fileContent <- getResourceByteArray(mediaFile.filepath)
        tempFile = ResourceAccess.toTempFile(mediaFile.filename, fileContent)
      } yield NonEmptyList.one(MediaResourceFile(tempFile))
    }
  }

  def dbResources[F[_]: Async: LogWriter](dbMedia: DBMedia[F], dropboxClient: DropboxClient[F]): ResourceAccess[F] =
    new ResourceAccess[F] {

      override def getResourcesByKind(criteria: String): Resource[F, NonEmptyList[NonEmptyList[MediaResource[F]]]] =
        for {
          _ <- Resource.eval(
            LogWriter.info(s"[ResourcesAccess:162:53] getMediaByKind fetching mediaResources by $criteria")
          )
          medias <- Resource.eval(dbMedia.getMediaByKind(criteria))
          files  <-
            medias.traverse(
              dbMediaDataToMediaResource
            )
          result <- Resource.eval(Async[F].fromOption(NonEmptyList.fromList(files), NoResourcesFoundKind(criteria)))
        } yield result

      override def getResourceFile(
          mediaFile: MediaFile
      ): Resource[F, NonEmptyList[MediaResource[F]]] = {
        for {
          _             <- Resource.eval(LogWriter.info(s"[ResourcesAccess] getResourceFile of $mediaFile"))
          media         <- Resource.eval(dbMedia.getMedia(mediaFile.filepath))
          _             <- Resource.eval(dbMedia.incrementMediaCount(media.media_name))
          mediaResource <- dbMediaDataToMediaResource(media)
        } yield mediaResource
      }

      private[resources] def dbMediaDataToMediaResource(
          dbMediaData: DBMediaData
      ): Resource[F, NonEmptyList[MediaResource[F]]] = {
        def buildMediaResources(
            mediaName: String,
            sources: NonEmptyList[Either[String, Uri]]
        ): Resource[F, NonEmptyList[MediaResource[F]]] =
          Resource.pure(sources.map {
            case Left(iFile) => MediaResourceIFile(iFile)
            case Right(uri)  =>
              MediaResourceFile(
                dropboxClient
                  .fetchFile(mediaName, uri)
                  .onError(e =>
                    Resource.eval(
                      LogWriter
                        .error(s"[ResourcesAccess] Uri $uri for $dbMediaData failed to fetch the data with error: $e")
                    )
                  )
              )
          })

        for
          media <- Resource.eval(Async[F].fromEither(Media(dbMediaData)))
          _ <- Resource.eval(LogWriter.info(s"[ResourcesAccess] fetching data for $media from ${media.mediaSources}"))
          nonEmptyMediaSources <- Resource.eval(
            Async[F].fromOption(NonEmptyList.fromList(media.mediaSources), NoResourcesFoundDBMediaData(dbMediaData))
          )
          mediaResource <- buildMediaResources(media.mediaName, nonEmptyMediaSources)
        yield mediaResource
      }
    }
}
