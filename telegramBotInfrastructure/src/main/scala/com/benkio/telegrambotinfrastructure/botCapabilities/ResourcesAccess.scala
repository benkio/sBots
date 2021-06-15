package com.benkio.telegrambotinfrastructure.botCapabilities

import com.benkio.telegrambotinfrastructure.model.MediaFile
import scala.jdk.CollectionConverters._
import java.nio.file.Paths
import java.nio.file.Files
import java.nio.file.Path
import java.io.FileOutputStream
import java.io.FileInputStream
import java.io.File
import java.io.ByteArrayOutputStream
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
  def getResourceByteArray[F[_]](resourceName: String)(implicit F: Effect[F]): Resource[F, Array[Byte]]
  def getResourcesByKind[F[_]](criteria: String)(implicit F: Effect[F]): Resource[F, List[MediaFile]]
  def getResourceFile[F[_]](mediaFile: MediaFile)(implicit F: Effect[F]): Resource[F, File] = {
    for {
      fileContent <- getResourceByteArray(mediaFile.filepath)
      tempFile = File.createTempFile(mediaFile.filename, mediaFile.extension, null)
      fos <- Resource.make(F.delay(new FileOutputStream(tempFile)))(fos => F.delay(fos.close()))
    } yield {
      fos.write(fileContent)
      tempFile
    }
  }
}

object ResourceAccess {
  def fileSystem(subModuleDirectory: String) = new ResourceAccess {
    val rootPath = Paths.get(subModuleDirectory).toAbsolutePath()

    def getResourceByteArray[F[_]](resourceName: String)(implicit F: Effect[F]): Resource[F, Array[Byte]] = (for {
      fis <- Resource.make(F.delay(new FileInputStream(new File(buildPath(resourceName).toString))))(fis =>
        F.delay(fis.close())
      )
      bais <- Resource.make(F.delay(new ByteArrayOutputStream()))(bais => F.delay(bais.close()))
    } yield (fis, bais)).evalMap { case (fis, bais) =>
      val tempArray = new Array[Byte](16384)
      for {
        firstChunk <- F.delay(fis.read(tempArray, 0, tempArray.length))
        _ <- Monad[F].iterateWhileM(firstChunk)(chunk =>
          F.delay(bais.write(tempArray, 0, chunk)) *> F.delay(fis.read(tempArray, 0, tempArray.length))
        )(_ != -1)
      } yield bais.toByteArray()
    }

    def buildPath(subResourceFilePath: String): Path =
      Paths.get(rootPath.toString(), "src", "main", "resources", subResourceFilePath)

    def getResourcesByKind[F[_]: Effect](criteria: String): Resource[F, List[MediaFile]] =
      Resource.pure[F, List[MediaFile]](
        Files
          .walk(buildPath(criteria))
          .iterator
          .asScala
          .toList
          .tail
          .map((fl: Path) => MediaFile(criteria + "/" + fl.getFileName.toString))
      )

  }
  def database(subModuleDirectory: String, dbName: String) = new ResourceAccess {
    import java.sql.Connection
    import java.sql.DriverManager
    import java.sql.PreparedStatement
    import java.sql.ResultSet
    import scala.collection.mutable.ArrayBuffer

    Class.forName("org.sqlite.JDBC");
    val dbPath = "jdbc:sqlite:" + Paths.get(subModuleDirectory).toAbsolutePath() + "/" + dbName
    private def withConnection[F[_], A](dbComputation: Connection => F[A])(implicit F: Effect[F]): Resource[F, A] =
      Resource
        .make(F.delay(DriverManager.getConnection(dbPath)))(conn => F.delay(conn.close()))
        .evalMap(
          dbComputation
        )

    def getResourceByteArray[F[_]](resourceName: String)(implicit F: Effect[F]): Resource[F, Array[Byte]] = {
      val compute: Connection => F[Array[Byte]] = conn => {
        val query: String = s"SELECT file_data FROM Mediafile WHERE file_name LIKE '$resourceName'"
        F.delay(conn.prepareStatement(query))
          .flatMap((statement: PreparedStatement) =>
            F.delay(statement.executeQuery())
              .flatMap((rs: ResultSet) => F.delay(rs.next) >> F.delay(rs.getBytes("file_data")))
          )
      }
      withConnection(compute)
    }

    def getResourcesByKind[F[_]](criteria: String)(implicit F: Effect[F]): Resource[F, List[MediaFile]] = {
      val compute: Connection => F[List[MediaFile]] = conn => {
        val query: String               = "SELECT file_name FROM Mediafile WHERE file_type LIKE '" + criteria + "'"
        val result: ArrayBuffer[String] = ArrayBuffer.empty[String]
        F.delay(conn.prepareStatement(query))
          .flatMap((statement: PreparedStatement) =>
            F.delay(statement.executeQuery())
              .flatMap((rs: ResultSet) =>
                Monad[F]
                  .iterateWhileM(rs.next)(_ => F.delay(result += rs.getString("file_name")) >> F.delay(rs.next))(
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
  def all(subModuleDirectory: String, dbName: String) = new ResourceAccess {

    def getResourceByteArray[F[_]](resourceName: String)(implicit effectF: Effect[F]): Resource[F, Array[Byte]] =
      Resource
        .catsEffectMonadErrorForResource(effectF)
        .handleError[Array[Byte]](
          Resource
            .catsEffectMonadErrorForResource(effectF)
            .handleErrorWith[Array[Byte]](fileSystem(subModuleDirectory).getResourceByteArray(resourceName))(_ =>
              database(subModuleDirectory, dbName).getResourceByteArray(resourceName)
            )
        )(_ => Array.empty)

    def getResourcesByKind[F[_]](criteria: String)(implicit effectF: Effect[F]): Resource[F, List[MediaFile]] =
      Resource
        .catsEffectMonadErrorForResource(effectF)
        .handleError[List[MediaFile]](
          Resource
            .catsEffectMonadErrorForResource(effectF)
            .handleErrorWith[List[MediaFile]](fileSystem(subModuleDirectory).getResourcesByKind(criteria))(_ =>
              database(subModuleDirectory, dbName).getResourcesByKind(criteria)
            )
        )(_ => List.empty)
  }
}
