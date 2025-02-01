package com.benkio.telegrambotinfrastructure.resources

import cats.*
import cats.effect.*
import cats.implicits.*
import com.benkio.telegrambotinfrastructure.model.media.MediaResource
import com.benkio.telegrambotinfrastructure.model.media.Media
import com.benkio.telegrambotinfrastructure.model.reply.MediaFile
import com.benkio.telegrambotinfrastructure.resources.db.DBMedia
import com.benkio.telegrambotinfrastructure.resources.db.DBMediaData
import com.benkio.telegrambotinfrastructure.web.UrlFetcher
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.util.jar.JarFile
import log.effect.LogWriter
import org.http4s.Uri
import scala.collection.mutable.ArrayBuffer
import scala.jdk.CollectionConverters.*

trait ResourceAccess[F[_]] {
  def getResourcesByKind(criteria: String): Resource[F, List[MediaResource]]
  def getResourceFile(mediaFile: MediaFile): Resource[F, MediaResource]
}

object ResourceAccess {

  def toTempFile(fileName: String, content: Array[Byte]): File = {
    val (name, ext) = fileName.span(_ != '.')
    val tempFile    = File.createTempFile(name, ext)
    Files.write(tempFile.toPath(), content)
    tempFile.deleteOnExit()
    tempFile
  }

  def buildPath(subResourceFilePath: String, stage: Option[String] = None): Path =
    Paths.get(
      Paths.get("").toAbsolutePath().toString(),
      "src",
      stage.getOrElse("main"),
      "resources",
      subResourceFilePath
    )

  def fromResources[F[_]: Sync: LogWriter](stage: Option[String] = None): fromResources[F] = new fromResources[F](stage)
  class fromResources[F[_]: Sync: LogWriter](stage: Option[String] = None) extends ResourceAccess[F] {

    private def getResourceByteArray(resourceName: String): Resource[F, Array[Byte]] =
      (for {
        fis <- Resource.make(Sync[F].delay {
          val stream = getClass().getResourceAsStream("/" + resourceName)
          if (stream == null) new FileInputStream(resourceName) else stream
        })(fis => Sync[F].delay(fis.close()))
        bais <- Resource.make(Sync[F].delay(new ByteArrayOutputStream()))(bais => Sync[F].delay(bais.close()))
      } yield (fis, bais)).evalMap { case (fis, bais) =>
        val tempArray = new Array[Byte](16384)
        for {
          firstChunk <- Sync[F].delay(fis.read(tempArray, 0, tempArray.length))
          _ <- Monad[F].iterateWhileM(firstChunk)(chunk =>
            Sync[F].delay(bais.write(tempArray, 0, chunk)) *> Sync[F].delay(fis.read(tempArray, 0, tempArray.length))
          )(_ != -1)
        } yield bais.toByteArray()
      }

    def getResourcesByKind(criteria: String): Resource[F, List[MediaResource]] = {
      val jarFile = new File(getClass().getProtectionDomain().getCodeSource().getLocation().getPath())
      val result: ArrayBuffer[String] = new ArrayBuffer();

      // from https://stackoverflow.com/questions/11012819/how-can-i-get-a-resource-folder-from-inside-my-jar-file
      if (jarFile.isFile()) { // Run with JAR file
        val jar     = new JarFile(jarFile)
        val entries = jar.entries() // gives ALL entries in jar
        while (entries.hasMoreElements()) {
          val name = entries.nextElement().getName()
          if (name.startsWith(criteria + "/") && name.length > (criteria.length + 1)) { // filter according to the path
            result += name
          }
        }
        jar.close()
      }

      if (result.size == 0) {
        Resource
          .pure[F, List[MediaResource.MediaResourceFile]](
            Files
              .walk(buildPath(criteria, stage))
              .iterator
              .asScala
              .toList
              .tail
              .map((fl: Path) =>
                MediaResource.MediaResourceFile(
                  new File(buildPath(criteria, stage).toString + "/" + fl.getFileName.toString)
                )
              )
          )
      } else {
        result.toList.traverse(s =>
          getResourceByteArray(s).map(content =>
            MediaResource.MediaResourceFile(toTempFile(s.stripPrefix(s"$criteria/"), content))
          )
        )
      }
    }

    override def getResourceFile(
        mediaFile: MediaFile
    ): Resource[F, MediaResource] = {
      for {
        _           <- Resource.eval(LogWriter.info(s"getResourceFile of $mediaFile"))
        fileContent <- getResourceByteArray(mediaFile.filepath)
        tempFile = ResourceAccess.toTempFile(mediaFile.filename, Array.empty)
        fos <- Resource.make(Sync[F].delay(new FileOutputStream(tempFile)))(fos => Sync[F].delay(fos.close()))
      } yield {
        fos.write(fileContent)
        MediaResource.MediaResourceFile(tempFile)
      }
    }
  }

  def dbResources[F[_]: Async: LogWriter](dbMedia: DBMedia[F], urlFetcher: UrlFetcher[F]): ResourceAccess[F] =
    new ResourceAccess[F] {

      private def dbMediaDataToMediaResource(dbMedia: DBMediaData): Resource[F, MediaResource] = {
        def findFirstSuccedingSource(
            mediaName: String,
            sources: List[Either[String, Uri]]
        ): Resource[F, MediaResource] = sources match
          case Nil =>
            Resource.eval(
              LogWriter.error(s"[ResourcesAccess] couldn't find a successful source for $dbMedia") >> Async[F]
                .raiseError(Throwable(s"[ResourcesAccess] couldn't find a successful source for $dbMedia"))
            )
          case Left(iFile) :: _ => Resource.pure(MediaResource.MediaResourceIFile(iFile))
          case Right(uri) :: t =>
            urlFetcher
              .fetchFromDropbox(mediaName, uri)
              .map(MediaResource.MediaResourceFile(_))
              .handleErrorWith[MediaResource, Throwable](e =>
                Resource.eval(
                  LogWriter.error(s"[ResourcesAccess] Uri $uri for $dbMedia failed to fetch the data")
                ) >> findFirstSuccedingSource(mediaName, t)
              )
        for
          media         <- Resource.eval(Async[F].fromEither(Media(dbMedia)))
          mediaResource <- findFirstSuccedingSource(media.mediaName, media.mediaSources)
        yield mediaResource
      }

      override def getResourcesByKind(criteria: String): Resource[F, List[MediaResource]] =
        for {
          medias <- Resource.eval(dbMedia.getMediaByKind(criteria))
          files <-
            medias.traverse(
              dbMediaDataToMediaResource
            )
        } yield files

      override def getResourceFile(
          mediaFile: MediaFile
      ): Resource[F, MediaResource] = {
        for {
          _             <- Resource.eval(LogWriter.info(s"[ResourcesAccess] `getResourceFile` of $mediaFile"))
          media         <- Resource.eval(dbMedia.getMedia(mediaFile.filepath))
          _             <- Resource.eval(dbMedia.incrementMediaCount(media.media_name))
          mediaResource <- dbMediaDataToMediaResource(media)
        } yield mediaResource
      }
    }
}
