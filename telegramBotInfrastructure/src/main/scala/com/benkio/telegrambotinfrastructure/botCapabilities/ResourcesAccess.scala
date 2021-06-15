package com.benkio.telegrambotinfrastructure.botCapabilities

import com.benkio.telegrambotinfrastructure.model.MediaFile
import scala.jdk.CollectionConverters._
import java.nio.file.Paths
import java.nio.file.Files
import java.nio.file.Path
import java.io.{FileOutputStream, FileInputStream, File, ByteArrayOutputStream}
import cats.effect._
import cats._
import cats.implicits._

sealed trait ResourceSource

case class FileSystem(subModuleDirectory: String)               extends ResourceSource
case class Database(subModuleDirectory: String, dbName: String) extends ResourceSource
case class All(subModuleDirectory: String, dbName: String)      extends ResourceSource

object ResourceSource {
  def selectResourceAccess[A <: ResourceSource](source: A): ResourceAccess =
    source match {
      case All(subModuleDirectory, dbName)      => ResourceAccess.all(subModuleDirectory, dbName)
      case Database(subModuleDirectory, dbName) => ResourceAccess.database(subModuleDirectory, dbName)
      case FileSystem(subModuleDirectory)       => ResourceAccess.fileSystem(subModuleDirectory)
    }
}

trait ResourceAccess {
  def getResourceByteArray(resourceName: String): Resource[IO, Array[Byte]]
  def getResourcesByKind(criteria: String): Resource[IO, List[MediaFile]]
  def getResourceFile(mediaFile: MediaFile): Resource[IO, File] = {
    for {
      fileContent <- getResourceByteArray(mediaFile.filepath)
      tempFile = File.createTempFile(mediaFile.filename, mediaFile.extension, null)
      fos <- Resource.make(IO(new FileOutputStream(tempFile)))(fos => IO(fos.close()))
    } yield {
      fos.write(fileContent)
      tempFile
    }
  }
}

object ResourceAccess  {
  def fileSystem(subModuleDirectory: String) = new ResourceAccess {
    val rootPath = Paths.get(subModuleDirectory).toAbsolutePath()

    def getResourceByteArray(resourceName: String): Resource[IO, Array[Byte]] = (for {
      fis <- Resource.make(IO(new FileInputStream(new File(buildPath(resourceName).toString))))(fis => IO(fis.close()))
      bais <- Resource.make(IO(new ByteArrayOutputStream()))(bais => IO(bais.close()))
    } yield (fis,bais)).evalMap {
      case (fis, bais) =>
        val tempArray = new Array[Byte](16384)
        for {
          firstChunk <- IO(fis.read(tempArray, 0, tempArray.length))
          _ <- Monad[IO].iterateWhileM(firstChunk)(chunk => IO(bais.write(tempArray, 0, chunk)) *> IO(fis.read(tempArray, 0, tempArray.length)))(_ != -1)
        } yield bais.toByteArray()
    }

    def buildPath(subResourceFilePath: String): Path =
      Paths.get(rootPath.toString(), "src", "main", "resources", subResourceFilePath)

    def getResourcesByKind(criteria: String): Resource[IO, List[MediaFile]] =
      Resource.pure[IO, List[MediaFile]](
        Files
          .walk(buildPath(criteria))
          .iterator
          .asScala
          .toList
          .tail
          .map((fl: Path) => MediaFile(buildPath(criteria).toString + "/" + fl.getFileName.toString)))

  }
  def database(subModuleDirectory: String, dbName: String) = new ResourceAccess {
    import java.sql.Connection
    import java.sql.DriverManager
    import java.sql.PreparedStatement
    import java.sql.ResultSet
    import scala.collection.mutable.ArrayBuffer

    Class.forName("org.sqlite.JDBC");
    val dbPath = "jdbc:sqlite:" + Paths.get(subModuleDirectory).toAbsolutePath() + "/" + dbName
    def withConnection[A](dbComputation: Connection => IO[A]): Resource[IO, A] =
      Resource.make(IO(DriverManager.getConnection(dbPath)))(conn => IO(conn.close())).evalMap(
        dbComputation
      )

    def getResourceByteArray(resourceName: String): Resource[IO, Array[Byte]] = {
      val compute: Connection => IO[Array[Byte]] = conn => {
        val query: String                = s"SELECT file_data FROM Mediafile WHERE file_name LIKE '$resourceName'"
        IO(conn.prepareStatement(query)).flatMap((statement: PreparedStatement) =>
          IO(statement.executeQuery()).flatMap((rs: ResultSet) =>
            IO(rs.next) >> IO(rs.getBytes("file_data"))
          )
        )
      }
      withConnection(compute)
    }

    def getResourcesByKind(criteria: String): Resource[IO, List[MediaFile]] = {
      val compute: Connection => IO[List[MediaFile]] = conn => {
        val query: String                = "SELECT file_name FROM Mediafile WHERE file_type LIKE '" + criteria + "'"
        val result: ArrayBuffer[String]  = ArrayBuffer.empty[String]
        IO(conn.prepareStatement(query)).flatMap((statement: PreparedStatement) =>
          IO(statement.executeQuery()).flatMap((rs: ResultSet) =>
            Monad[IO].iterateWhileM(rs.next)(_ =>
              IO(result += rs.getString("file_name")) >> IO(rs.next)
            )(_ == true).map(_ => result.toList.map(n => MediaFile(n)))
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
  def all(subModuleDirectory: String, dbName: String) = new ResourceAccess {

    def getResourceByteArray(resourceName: String): Resource[IO,Array[Byte]] =
      Resource.catsEffectMonadErrorForResource(IO.ioEffect).handleError[Array[Byte]](
        Resource.catsEffectMonadErrorForResource(IO.ioEffect).handleErrorWith[Array[Byte]](
          fileSystem(subModuleDirectory).getResourceByteArray(resourceName))(_ =>
          database(subModuleDirectory, dbName).getResourceByteArray(resourceName)
        )
      )(_ => Array.empty)

    def getResourcesByKind(criteria: String): Resource[IO, List[MediaFile]] =
      Resource.catsEffectMonadErrorForResource(IO.ioEffect).handleError[List[MediaFile]](
        Resource.catsEffectMonadErrorForResource(IO.ioEffect).handleErrorWith[List[MediaFile]](
          fileSystem(subModuleDirectory).getResourcesByKind(criteria))(_ =>
          database(subModuleDirectory, dbName).getResourcesByKind(criteria)
        )
      )(_ => List.empty)
  }
}
