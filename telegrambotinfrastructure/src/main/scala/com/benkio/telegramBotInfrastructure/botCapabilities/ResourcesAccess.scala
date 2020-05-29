package com.benkio.telegramBotInfrastructure.botCapabilities

import com.benkio.telegramBotInfrastructure.model.MediaFile
import scala.util._
import java.nio.file.Path
import java.nio.file.Paths
import java.nio.file.Files
import java.util.stream.Collectors
import scala.collection.JavaConversions._

sealed trait ResourceSource

case object FileSystem              extends ResourceSource
case class Database(dbName: String) extends ResourceSource
case class All(dbName: String)      extends ResourceSource

object ResourceSource {
  def selectResourceAccess[A <: ResourceSource](source: A): ResourceAccess[ResourceSource] =
    source match {
      case All(dbName)        => ResourceAccess.all(dbName)
      case Database(dbName)   => ResourceAccess.database(dbName)
      case _: FileSystem.type => ResourceAccess.fileSystem
    }
}

trait ResourceAccess[+A <: ResourceSource] {
  def getResource(resourceName: String): Array[Byte]
  def getResourcesByKind(criteria: String): List[MediaFile]
}

object ResourceAccess {
  val fileSystem = new ResourceAccess[FileSystem.type] {
    val rootPath = Paths.get("").toAbsolutePath()

    def getResource(resourceName: String): Array[Byte] =
      Files.readAllBytes(buildPath(resourceName))

    def buildPath(subResourceFilePath: String): Path =
      Paths.get(rootPath.toString(), "src", "main", "resources", subResourceFilePath)

    def getResourcesByKind(criteria: String): List[MediaFile] =
      Files
        .walk(buildPath(criteria))
        .collect(Collectors.toList())
        .toList
        .tail
        .map((fl: Path) => MediaFile(buildPath(criteria) + "/" + fl.getFileName.toString))

  }
  def database(dbName: String) = new ResourceAccess[Database] {
    import java.sql.Connection
    import java.sql.DriverManager
    import java.sql.SQLException
    import java.sql.PreparedStatement
    import java.sql.ResultSet
    import scala.collection.mutable.ArrayBuffer

    Class.forName("org.sqlite.JDBC");
    val dbPath = "jdbc:sqlite:" + Paths.get("").toAbsolutePath() + "/" + dbName
    def withConnection[A](dbComputation: Connection => A): A = {
      var conn: Connection = null
      try {
        conn = DriverManager.getConnection(dbPath)
        val result = dbComputation(conn)
        result
      } catch {
        case e: SQLException => {
          println(e.getMessage)
          println("Exception: " + e)
          throw e
        }
      } finally {
        try {
          if (conn != null) {
            conn.close()
          }
        } catch {
          case ex: SQLException => {
            println(ex.getMessage)
            println("Connection Close: " + ex)
            throw ex
          }
        }
      }
    }

    def getResource(resourceName: String): Array[Byte] = {
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
        var result: ArrayBuffer[String]  = ArrayBuffer.empty[String]
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
  def all(dbName: String) = new ResourceAccess[All] {

    def getResource(resourceName: String): Array[Byte] =
      Try(fileSystem.getResource(resourceName))
        .orElse(Try(database(dbName).getResource(resourceName)))
        .getOrElse(Array.empty)

    def getResourcesByKind(criteria: String): List[MediaFile] =
      Try(fileSystem.getResourcesByKind(criteria))
        .orElse(Try(database(dbName).getResourcesByKind(criteria)))
        .getOrElse(List.empty)
  }
}
