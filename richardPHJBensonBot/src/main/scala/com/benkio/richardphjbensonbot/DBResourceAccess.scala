package com.benkio.richardphjbensonbot

import cats.effect._
import cats.implicits._
import com.benkio.telegrambotinfrastructure.botcapabilities.ResourceAccess
import doobie._
import doobie.implicits._

import java.io.File
import java.nio.file.Files

object DBResourceAccess {

  def apply[F[_]: Async](config: Config): ResourceAccess[F] = new DBResourceAccess[F]( Transactor.fromDriverManager[F](
    driver = config.driver,
    url    = config.url,
    user   = config.user,
    pass   = config.password
  ))

  private class DBResourceAccess[F[_]: Async](transactor: Transactor[F]) extends ResourceAccess[F] {

    private def getResourceContent(resourceName: String): Query0[Array[Byte]] =
      sql"SELECT media_content FROM media WHERE media_name = $resourceName".query[Array[Byte]]

    private def getByKind(kind: String): Query0[(String, Array[Byte])] =
      sql"SELECT media_name, media_content FROM media WHERE kind = $kind".query[(String, Array[Byte])]

    def getResourceByteArray(resourceName: String): Resource[F, Array[Byte]] =
      Resource.eval(
        getResourceContent(resourceName).unique.transact(transactor)
      )
    def getResourcesByKind(criteria: String): Resource[F, List[File]] =
      Resource.eval(
        getByKind(criteria).stream.compile.toList.transact(transactor).map(contents => contents.map {
          case (fileName, content) => toTempFile(fileName, content)
        })
      )

  }

  def toTempFile(fileName: String, content: Array[Byte]): File = {
    val (name, ext) = fileName.span(_ != '.')
    val tempFile = File.createTempFile(name, ext)
    Files.write(tempFile.toPath(), content)
    tempFile
  }
}
