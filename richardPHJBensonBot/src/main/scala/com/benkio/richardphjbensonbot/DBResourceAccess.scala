package com.benkio.richardphjbensonbot

import cats.effect._
import cats.implicits._
import com.benkio.telegrambotinfrastructure.botcapabilities.ResourceAccess
import doobie._
import doobie.implicits._
import log.effect.LogWriter

import java.io.File

object DBResourceAccess {

  def apply[F[_]: Async](transactor: Transactor[F])(implicit log: LogWriter[F]): ResourceAccess[F] =
    new DBResourceAccess[F](
      transactor,
      log
    )

  private class DBResourceAccess[F[_]: Async](transactor: Transactor[F], log: LogWriter[F]) extends ResourceAccess[F] {

    private def getResourceContent(resourceName: String): Query0[Array[Byte]] =
      sql"SELECT media_content FROM media WHERE media_name = $resourceName".query[Array[Byte]]

    private def getByKind(kind: String): Query0[(String, Array[Byte])] =
      sql"SELECT media_name, media_content FROM media WHERE kind = $kind".query[(String, Array[Byte])]

    def getResourceByteArray(resourceName: String): Resource[F, Array[Byte]] =
      Resource.eval(
        log.info(s"DB fetching $resourceName") *> getResourceContent(resourceName).unique.transact(transactor)
      )
    def getResourcesByKind(criteria: String): Resource[F, List[File]] =
      Resource.eval(
        getByKind(criteria).stream.compile.toList
          .transact(transactor)
          .map(contents =>
            contents.map { case (fileName, content) =>
              ResourceAccess.toTempFile(fileName, content)
            }
          )
      )

  }
}