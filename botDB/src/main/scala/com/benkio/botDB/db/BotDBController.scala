package com.benkio.botDB.db

import cats.effect.Resource
import cats.effect.Sync
import cats.implicits._
import com.benkio.botDB.db.schema.MediaEntity
import com.benkio.botDB.Config
import com.benkio.botDB.Input
import com.benkio.telegrambotinfrastructure.resources.ResourceAccess
import io.chrisdavenport.cormorant.implicits._
import io.chrisdavenport.cormorant.parser._

import java.sql.Timestamp
import java.time.Instant
import scala.io.Source

sealed trait BotDBController[F[_]] {
  def build: Resource[F, Unit]

  def populateMediaTable: Resource[F, Unit]
}

object BotDBController {
  def apply[F[_]: Sync](
      cfg: Config,
      databaseRepository: DatabaseRepository[F],
      resourceAccess: ResourceAccess[F],
      migrator: DBMigrator[F]
  ): BotDBController[F] =
    new BotDBControllerImpl(
      cfg = cfg,
      databaseRepository = databaseRepository,
      resourceAccess = resourceAccess,
      migrator = migrator
    )

  private class BotDBControllerImpl[F[_]: Sync](
      cfg: Config,
      databaseRepository: DatabaseRepository[F],
      resourceAccess: ResourceAccess[F],
      migrator: DBMigrator[F]
  ) extends BotDBController[F] {
    override def build: Resource[F, Unit] = for {
      _ <- Resource.eval(migrator.migrate(cfg))
      _ <- populateMediaTable
    } yield ()

    override def populateMediaTable: Resource[F, Unit] = for {
      allFiles <- cfg.csvLocation.flatTraverse(resourceAccess.getResourcesByKind)
      csvs = allFiles.filter(f => f.getName.endsWith("csv"))
      input <- Resource.eval(Sync[F].fromEither(csvs.flatTraverse(csv => {
        val fileContent = Source.fromFile(csv).getLines().mkString("\n")
        parseComplete(fileContent).flatMap(_.readLabelled[Input].sequence)
      })))
      _ <- Resource.eval(
        input.traverse_(i =>
          for {
            _ <- Sync[F].delay(println(s"Inserting file ${i.filename} of kind ${i.kind} from ${i.url}"))
            _ <- databaseRepository
              .insertMedia(
                MediaEntity(
                  media_name = i.filename,
                  kind = i.kind,
                  media_url = i.url,
                  created_at = Timestamp.from(Instant.now())
                )
              )
            _ <- Sync[F].delay(println(s"Inserted file ${i.filename} of kind ${i.kind} from ${i.url}, successfully"))
          } yield ()
        )
      )
    } yield ()
  }
}
