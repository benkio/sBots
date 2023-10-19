package com.benkio.telegrambotinfrastructure.resources

import cats._
import cats.effect._
import cats.implicits._
import com.benkio.telegrambotinfrastructure.model.MediaFile
import com.benkio.telegrambotinfrastructure.resources.db.DBMedia
import com.benkio.telegrambotinfrastructure.web.UrlFetcher
import log.effect.LogWriter

import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.util.jar.JarFile
import scala.collection.mutable.ArrayBuffer
import scala.jdk.CollectionConverters._

trait ResourceAccess[F[_]] {
  def getResourceByteArray(resourceName: String): Resource[F, Array[Byte]]
  def getResourcesByKind(criteria: String): Resource[F, List[File]]
  def getResourceFile(mediaFile: MediaFile)(implicit syncF: Sync[F], log: LogWriter[F]): Resource[F, File] = {
    for {
      _           <- Resource.eval(log.info(s"getResourceFile of $mediaFile"))
      fileContent <- getResourceByteArray(mediaFile.filepath)
      tempFile = ResourceAccess.toTempFile(mediaFile.filename, Array.empty)
      fos <- Resource.make(syncF.delay(new FileOutputStream(tempFile)))(fos => Sync[F].delay(fos.close()))
    } yield {
      fos.write(fileContent)
      tempFile
    }
  }
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

  def fromResources[F[_]: Sync]: fromResources[F] = new fromResources[F]
  class fromResources[F[_]: Sync] extends ResourceAccess[F] {

    def getResourceByteArray(resourceName: String): Resource[F, Array[Byte]] =
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

    def getResourcesByKind(criteria: String): Resource[F, List[File]] = {
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
          .pure[F, List[File]](
            Files
              .walk(buildPath(criteria))
              .iterator
              .asScala
              .toList
              .tail
              .map((fl: Path) => new File(buildPath(criteria).toString + "/" + fl.getFileName.toString))
          )
      } else {
        result.toList.traverse(s =>
          getResourceByteArray(s).map(content => toTempFile(s.stripPrefix(s"$criteria/"), content))
        )
      }
    }
  }

  def dbResources[F[_]: Async](dbMedia: DBMedia[F], urlFetcher: UrlFetcher[F]): ResourceAccess[F] =
    new ResourceAccess[F] {

      def getResourceByteArray(resourceName: String): Resource[F, Array[Byte]] =
        for {
          media <- Resource.eval(dbMedia.getMedia(resourceName))
          _     <- Resource.eval(dbMedia.incrementMediaCount(media.media_name))
          file  <- urlFetcher.fetchFromDropbox(resourceName, media.media_url)
        } yield Files.readAllBytes(file.toPath)

      def getResourcesByKind(criteria: String): Resource[F, List[File]] =
        for {
          medias <- Resource.eval(dbMedia.getMediaByKind(criteria))
          files  <- medias.traverse(media => urlFetcher.fetchFromDropbox(media.media_name, media.media_url))
        } yield files
    }
}
