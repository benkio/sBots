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
      insertSql(mediaEntity).run.transact(transactor).void

    private def insertSql(mediaEntity: MediaEntity): Update0 =
      sql"REPLACE INTO media (media_name, kind, media_content, created_at) VALUES ($mediaEntity);".update
  }
}
