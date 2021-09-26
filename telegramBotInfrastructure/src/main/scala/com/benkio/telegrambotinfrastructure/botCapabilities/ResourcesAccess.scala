package com.benkio.telegrambotinfrastructure.botCapabilities

import cats._
import cats.effect._
import cats.implicits._
import com.benkio.telegrambotinfrastructure.model.MediaFile

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

sealed trait ResourceSource

case object FileSystem              extends ResourceSource
case class Database(dbName: String) extends ResourceSource
case class All(dbName: String)      extends ResourceSource

object ResourceSource {
  def selectResourceAccess[A <: ResourceSource](source: A): ResourceAccess =
    source match {
      case All(dbName)        => ResourceAccess.all(dbName)
      case Database(dbName)   => ResourceAccess.database(dbName)
      case _: FileSystem.type => ResourceAccess.fileSystem
    }
}

trait ResourceAccess {
  def getResourceByteArray[F[_]: Async](resourceName: String): Resource[F, Array[Byte]]
  def getResourcesByKind[F[_]: Async](criteria: String): Resource[F, List[MediaFile]]
  def getResourceFile[F[_]: Async](mediaFile: MediaFile): Resource[F, File] = {
    for {
      fileContent <- getResourceByteArray(mediaFile.filepath)
      tempFile = File.createTempFile(mediaFile.filename, mediaFile.extension, null)
      fos <- Resource.make(Async[F].delay(new FileOutputStream(tempFile)))(fos => Async[F].delay(fos.close()))
    } yield {
      fos.write(fileContent)
      tempFile
    }
  }
}

object ResourceAccess {
  val fileSystem = new ResourceAccess {

    def getResourceByteArray[F[_]: Async](resourceName: String): Resource[F, Array[Byte]] =
      (for {
        fis <- Resource.make(Async[F].delay {
          val stream = getClass().getResourceAsStream("/" + resourceName)
          if (stream == null) new FileInputStream(resourceName) else stream
        })(fis => Async[F].delay(fis.close()))
        bais <- Resource.make(Async[F].delay(new ByteArrayOutputStream()))(bais => Async[F].delay(bais.close()))
      } yield (fis, bais)).evalMap { case (fis, bais) =>
        val tempArray = new Array[Byte](16384)
        for {
          firstChunk <- Async[F].delay(fis.read(tempArray, 0, tempArray.length))
          _ <- Monad[F].iterateWhileM(firstChunk)(chunk =>
            Async[F].delay(bais.write(tempArray, 0, chunk)) *> Async[F].delay(fis.read(tempArray, 0, tempArray.length))
          )(_ != -1)
        } yield bais.toByteArray()
      }

    def buildPath(subResourceFilePath: String): Path =
      Paths.get(Paths.get("").toAbsolutePath().toString(), "src", "main", "resources", subResourceFilePath)

    def getResourcesByKind[F[_]: Async](criteria: String): Resource[F, List[MediaFile]] = {
      val jarFile = new File(getClass().getProtectionDomain().getCodeSource().getLocation().getPath())
      val result: ArrayBuffer[String] = new ArrayBuffer();

      // from https://stackoverflow.com/questions/11012819/how-can-i-get-a-resource-folder-from-inside-my-jar-file
      if (jarFile.isFile()) { // Run with JAR file
        val jar     = new JarFile(jarFile)
        val entries = jar.entries() //gives ALL entries in jar
        while (entries.hasMoreElements()) {
          val name = entries.nextElement().getName()
          if (name.startsWith(criteria + "/") && name.length > (criteria.length + 1)) { //filter according to the path
            result += name
          }
        }
        jar.close()
      }

      Resource
        .pure[F, List[MediaFile]](
          if (result.size == 0)
            Files
              .walk(buildPath(criteria))
              .iterator
              .asScala
              .toList
              .tail
              .map((fl: Path) => MediaFile(buildPath(criteria).toString + "/" + fl.getFileName.toString))
          else result.toList.map(s => MediaFile(s))
        )
    }
  }
  def database(dbName: String) = new ResourceAccess {
    import java.sql.Connection
    import java.sql.DriverManager
    import java.sql.PreparedStatement
    import java.sql.ResultSet

    Class.forName("org.sqlite.JDBC")
    val dbPath = "jdbc:sqlite:" + Paths.get("").toAbsolutePath() + "/" + dbName
    private def withConnection[F[_]: Async, A](dbComputation: Connection => F[A]): Resource[F, A] =
      Resource
        .make(Async[F].delay(DriverManager.getConnection(dbPath)))(conn => Async[F].delay(conn.close()))
        .evalMap(
          dbComputation
        )

    def getResourceByteArray[F[_]: Async](resourceName: String): Resource[F, Array[Byte]] = {
      val compute: Connection => F[Array[Byte]] = conn => {
        val query: String = s"SELECT file_data FROM Mediafile WHERE file_name LIKE '$resourceName'"
        Async[F]
          .delay(conn.prepareStatement(query))
          .flatMap((statement: PreparedStatement) =>
            Async[F]
              .delay(statement.executeQuery())
              .flatMap((rs: ResultSet) => Async[F].delay(rs.next) >> Async[F].delay(rs.getBytes("file_data")))
          )
      }
      withConnection(compute)
    }

    def getResourcesByKind[F[_]: Async](criteria: String): Resource[F, List[MediaFile]] = {
      val compute: Connection => F[List[MediaFile]] = conn => {
        val query: String               = "SELECT file_name FROM Mediafile WHERE file_type LIKE '" + criteria + "'"
        val result: ArrayBuffer[String] = ArrayBuffer.empty[String]
        Async[F]
          .delay(conn.prepareStatement(query))
          .flatMap((statement: PreparedStatement) =>
            Async[F]
              .delay(statement.executeQuery())
              .flatMap((rs: ResultSet) =>
                Monad[F]
                  .iterateWhileM(rs.next)(_ =>
                    Async[F].delay(result += rs.getString("file_name")) >> Async[F].delay(rs.next)
                  )(
                    _ == true
                  )
                  .map(_ => result.toList.map(n => MediaFile(n)))
              )
          )
      }
      withConnection(compute)
    }

    // def insertResourcesToDB(resourceSubFolder: String): Unit = {
    //   val compute: Connection => Unit = conn => {
    //     val files = ResourceAccess.fileSystem.getResourcesByKind(resourceSubFolder)

    //     files.foreach { mediafile =>
    //       val bytes: Array[Byte] = Files.readAllBytes(Paths.get(mediafile.filepath))
    //       val ps                 = conn.prepareStatement("INSERT INTO Mediafile (file_name, file_type, file_data) VALUES (?, ?, ?)")
    //       ps.setString(1, mediafile.filename)
    //       ps.setString(2, mediafile.extension)
    //       ps.setBytes(3, bytes)
    //       val s = ps.executeUpdate()
    //       if (s > 0) println("Operation Successful")
    //       ps.close()
    //     }
    //   }
    //   withConnection(compute)
    // }
  }
  def all(dbName: String) = new ResourceAccess {

    def getResourceByteArray[F[_]: Async](resourceName: String): Resource[F, Array[Byte]] =
      Resource
        .catsEffectMonadErrorForResource(MonadThrow[F])
        .handleError[Array[Byte]](
          Resource
            .catsEffectMonadErrorForResource(MonadThrow[F])
            .handleErrorWith[Array[Byte]](fileSystem.getResourceByteArray(resourceName))(_ =>
              database(dbName).getResourceByteArray(resourceName)
            )
        )(_ => Array.empty)

    def getResourcesByKind[F[_]: Async](criteria: String): Resource[F, List[MediaFile]] =
      Resource
        .catsEffectMonadErrorForResource(MonadThrow[F])
        .handleError[List[MediaFile]](
          Resource
            .catsEffectMonadErrorForResource(MonadThrow[F])
            .handleErrorWith[List[MediaFile]](fileSystem.getResourcesByKind(criteria))(_ =>
              database(dbName).getResourcesByKind(criteria)
            )
        )(_ => List.empty)
  }
}
