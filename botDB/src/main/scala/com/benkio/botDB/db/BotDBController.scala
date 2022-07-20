package com.benkio.botDB.db

import cats.effect.Resource
import cats.effect.Sync
import cats.implicits._
import com.benkio.botDB.Config
import com.benkio.botDB.db.schema.MediaEntity
import com.benkio.telegrambotinfrastructure.botcapabilities.ResourceAccess

import java.io.File
import scala.util.Try

sealed trait BotDBController[F[_]] {
  def build: Resource[F, Unit]

  def populateMediaTable(): Resource[F, Unit]
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

  def flattenResources(resources: List[File], kind: Option[String] = None): List[(File, Option[String])] = for {
    r <- resources
    fk <-
      if (r.isFile)
        List((r, kind))
      else
        Try(
          flattenResources(r.listFiles().toList, kind.fold(Some(r.getName))((k: String) => Some(s"${k}_${r.getName}")))
        ).getOrElse(List.empty)
  } yield fk

  private class BotDBControllerImpl[F[_]: Sync](
      cfg: Config,
      databaseRepository: DatabaseRepository[F],
      resourceAccess: ResourceAccess[F],
      migrator: DBMigrator[F]
  ) extends BotDBController[F] {
    override def build: Resource[F, Unit] = for {
      _ <- Resource.eval(migrator.migrate(cfg))
      _ <- populateMediaTable()
    } yield ()

    def populateMediaTable(): Resource[F, Unit] = for {
      resources <- resourceAccess.getResourcesByKind(cfg.resourceLocation)
      files = BotDBController.flattenResources(resources)
      _ <- Resource.eval(files.traverse_ { case (file, kind) =>
        Sync[F].delay(println(s"Inserting file $file of kind $kind")) *>
        databaseRepository.insertMedia(MediaEntity.fromFile(file, kind))
        .productL(Sync[F].delay(println(s"Inserted file $file of kind $kind successfully")))
      })
    } yield ()
  }
}
