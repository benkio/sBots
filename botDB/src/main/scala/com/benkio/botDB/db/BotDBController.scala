package com.benkio.botDB.db

import cats.effect.Resource
import cats.effect.Sync
import cats.implicits._
import com.benkio.botDB.Config
import com.benkio.botDB.db.schema.MediaEntity
import com.benkio.telegrambotinfrastructure.botcapabilities.ResourceAccess

import java.io.File

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

  def flattenResources(resources: List[File]): List[(File, Option[String])] = {
    val (files, directories) = resources.partition(_.isFile())
    files.map(f => (f, None)) ++ directories.flatMap(dir =>
      flattenResources(dir.listFiles().toList).map {
        case (f, Some(kind)) => (f, Some(s"${dir.getName()}_" + kind))
        case (f, None)       => (f, Some(s"${dir.getName()}"))
      }
    )
  }

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
        databaseRepository.insertMedia(MediaEntity.fromFile(file, kind))
      })
    } yield ()
  }
}
