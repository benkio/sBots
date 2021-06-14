package com.benkio.telegrambotinfrastructure.botCapabilities

import com.benkio.telegrambotinfrastructure.model.MediaFile
import scala.jdk.CollectionConverters._
import scala.util._
import java.nio.file.Paths
import java.nio.file.Files
import java.nio.file.Path
import java.io.FileOutputStream
import java.io.File
import cats.effect._

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
  def getResourcesByKind(criteria: String): List[MediaFile]
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
  def fileSystem(subModuleDirectory: String) = new ResourceAccess[FileSystem] {
    val rootPath = Paths.get(subModuleDirectory).toAbsolutePath()

    def getResourceByteArray(resourceName: String): Array[Byte] =
      Resource.make(new FileInputStream(File(buildPath(resourceName))))(fis => fis.close()).evalMap(fis =>
        for (
          nextByte <- IO(fis.read())
          if readByte != -1
        ) {
          
        }
      )

    def buildPath(subResourceFilePath: String): Path =
      Paths.get(rootPath.toString(), "src", "main", "resources", subResourceFilePath)

    def getResourcesByKind(criteria: String): List[MediaFile] =
      Files
        .walk(buildPath(criteria))
        .iterator
        .asScala
        .toList
        .tail
        .map((fl: Path) => MediaFile(buildPath(criteria).toString + "/" + fl.getFileName.toString))

  }
  def database(subModuleDirectory: String, dbName: String) = new ResourceAccess[Database] {
    import java.sql.Connection
    import java.sql.DriverManager
    import java.sql.SQLException
    import java.sql.PreparedStatement
    import java.sql.ResultSet
    import scala.collection.mutable.ArrayBuffer

    Class.forName("org.sqlite.JDBC");
    val dbPath = "jdbc:sqlite:" + Paths.get(subModuleDirectory).toAbsolutePath() + "/" + dbName
    def withConnection[A](dbComputation: Connection => A): A = {
      var conn: Connection = null
      try {
        conn = DriverManager.getConnection(dbPath)
        val result = dbComputation(conn)
        result
      } catch {
        case e: SQLException => {
          throw e
        }
      } finally {
        try {
          if (conn != null) {
            conn.close()
          }
        } catch {
          case ex: SQLException => {
            throw ex
          }
        }
      }
    }

    def getResourceByteArray(resourceName: String): Array[Byte] = {
      val compute: Connection => Array[Byte] = conn => {
        val query: String                = s"SELECT file_data FROM Mediafile WHERE file_name LIKE '$resourceName'"
        val statement: PreparedStatement = conn.prepareStatement(query)
        val rs: ResultSet                = statement.executeQuery()
        rs.next
        rs.getBytes("file_data")
      }
      withConnection(compute)
    }
    def getResourcesByKind(criteria: String): List[MediaFile] = {
      val compute: Connection => List[MediaFile] = conn => {
        val query: String                = "SELECT file_name FROM Mediafile WHERE file_type LIKE '" + criteria + "'"
        val statement: PreparedStatement = conn.prepareStatement(query)
        val rs: ResultSet                = statement.executeQuery()
        val result: ArrayBuffer[String]  = ArrayBuffer.empty[String]
        while (rs.next) {
          val name: String = rs.getString("file_name")
          result += name
        }
        result.toList.map(n => MediaFile(n))
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

    def getResourceByteArray(resourceName: String): Array[Byte] =
      Try(fileSystem(subModuleDirectory).getResourceByteArray(resourceName))
        .orElse(Try(database(subModuleDirectory, dbName).getResourceByteArray(resourceName)))
        .getOrElse(Array.empty)

    def getResourcesByKind(criteria: String): List[MediaFile] =
      Try(fileSystem(subModuleDirectory).getResourcesByKind(criteria))
        .orElse(Try(database(subModuleDirectory, dbName).getResourcesByKind(criteria)))
        .getOrElse(List.empty)
  }
}
