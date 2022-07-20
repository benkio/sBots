package com.benkio.botDB.db

import cats.effect.kernel.MonadCancelThrow
import cats.implicits._
import com.benkio.botDB.db.schema.MediaEntity
import doobie._
import doobie.implicits._
import doobie.implicits.javasql._
import doobie.postgres.sqlstate.class23.UNIQUE_VIOLATION

trait DatabaseRepository[F[_]] {
  def insertMedia(mediaEntity: MediaEntity): F[Unit]
}

object DatabaseRepository {
  def apply[F[_]: MonadCancelThrow](transactor: Transactor[F]): DatabaseRepository[F] =
    new DatabaseRepositoryImpl[F](transactor = transactor)

  private class DatabaseRepositoryImpl[F[_]: MonadCancelThrow](transactor: Transactor[F])
      extends DatabaseRepository[F] {
    override def insertMedia(mediaEntity: MediaEntity): F[Unit] =
      insertSql(mediaEntity).run.transact(transactor).void.exceptSqlState {
        case UNIQUE_VIOLATION => updateOnConflictSql(mediaEntity).run.transact(transactor).void
        case sqlState =>
          MonadCancelThrow[F].raiseError(
            new RuntimeException(s"An error occurred in inserting $mediaEntity with the sql state: $sqlState")
          )
      }

    private def insertSql(mediaEntity: MediaEntity): Update0 =
      sql"INSERT INTO media (media_name, kind, media_content, created_at) VALUES ($mediaEntity);".update

    private def updateOnConflictSql(mediaEntity: MediaEntity): Update0 =
      sql"UPDATE media SET kind = ${mediaEntity.kind}, media_content = ${mediaEntity.media_content} WHERE media_name = ${mediaEntity.media_name};".update
  }
}
