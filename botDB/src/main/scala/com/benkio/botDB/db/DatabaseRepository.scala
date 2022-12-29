package com.benkio.botDB.db

import cats.effect.kernel.MonadCancelThrow
import cats.implicits._
import com.benkio.botDB.db.schema.MediaEntity
import doobie._
import doobie.implicits._
import doobie.implicits.javasql._

trait DatabaseRepository[F[_]] {
  def insertMedia(mediaEntity: MediaEntity): F[Unit]
}

object DatabaseRepository {
  def apply[F[_]: MonadCancelThrow](transactor: Transactor[F]): DatabaseRepository[F] =
    new DatabaseRepositoryImpl[F](transactor = transactor)

  private class DatabaseRepositoryImpl[F[_]: MonadCancelThrow](transactor: Transactor[F])
      extends DatabaseRepository[F] {
    override def insertMedia(mediaEntity: MediaEntity): F[Unit] =
      insertSql(mediaEntity).run.transact(transactor).void.exceptSql {
        case e if e.getMessage().contains("UNIQUE constraint failed") =>
          updateOnConflictSql(mediaEntity).run.transact(transactor).void
        case e =>
          MonadCancelThrow[F].raiseError(
            new RuntimeException(s"An error occurred in inserting $mediaEntity with exception: $e")
          )
      }

    private def insertSql(mediaEntity: MediaEntity): Update0 =
      sql"INSERT INTO media (media_name, kind, mime_type, media_url, created_at, media_count) VALUES (${mediaEntity.media_name}, ${mediaEntity.kind}, ${mediaEntity.mime_type}, ${mediaEntity.media_url.toString}, ${mediaEntity.created_at}, 0);".update

    private def updateOnConflictSql(mediaEntity: MediaEntity): Update0 =
      sql"UPDATE media SET kind = ${mediaEntity.kind}, media_url = ${mediaEntity.media_url.toString} WHERE media_name = ${mediaEntity.media_name};".update
  }
}
