package com.benkio.botDB.db

import cats.effect.{Resource, Sync}
import cats.implicits._
import com.benkio.botDB.db.schema.MediaEntity
import com.benkio.botDB.{Config, Input}

import java.sql.Timestamp
import java.time.Instant




sealed trait BotDBController[F[_]] {
  def build: Resource[F, Unit]

  def populateMediaTable(input: List[Input]): Resource[F, Unit]
}

object BotDBController {
  def apply[F[_]: Sync](
      cfg: Config,
      databaseRepository: DatabaseRepository[F],
      migrator: DBMigrator[F]
  ): BotDBController[F] =
    new BotDBControllerImpl(
      cfg = cfg,
      databaseRepository = databaseRepository,
      migrator = migrator
    )

  private class BotDBControllerImpl[F[_]: Sync](
      cfg: Config,
      databaseRepository: DatabaseRepository[F],
      migrator: DBMigrator[F]
  ) extends BotDBController[F] {
    override def build: Resource[F, Unit] = for {
      _ <- Resource.eval(migrator.migrate(cfg))
      input <- Resource.eval(Sync[F].fromEither(readInput(cfg.csvLocation)))
      _ <- populateMediaTable(input)
    } yield ()

    private def readInput(inputPath: String): Either[Error, List[Input]] = ???

    override def populateMediaTable(input: List[Input]): Resource[F, Unit] =
      Resource.eval(
        input.foreach { case Input(filename, kind, url) =>
        Sync[F].delay(println(s"Inserting filename $filename of kind $kind from $url")) *>
          databaseRepository
            .insertMedia(MediaEntity(
              media_name = filename,
              kind = kind,
              media_url = url,
              created_at = Timestamp.from(Instant.now())
            ))
            .productL(Sync[F].delay(println(s"Inserted file $filename of kind $kind from $url, successfully")))
        }.pure[F]
      )
  }
}
