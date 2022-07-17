package com.benkio.botDB.db

import cats.effect.{Resource, Sync}
import cats.implicits._
import com.benkio.botDB.Config
import com.benkio.telegrambotinfrastructure.botcapabilities.ResourceAccess

import java.io.File

sealed trait BotDBController[F[_]] {
  def build: Resource[F, Unit]
}

object BotDBController {
  def apply[F[_]: Sync](cfg: Config, databaseRepository: DatabaseRepository[F], resourceAccess: ResourceAccess[F], migrator: DBMigrator[F]) : BotDBController[F] =
    new BotDBControllerImpl(
      cfg = cfg,
      databaseRepository = databaseRepository,
      resourceAccess = resourceAccess,
      migrator = migrator
    )

  def flattenResources(resources: List[File]): List[File] = ???

  private class BotDBControllerImpl[F[_]: Sync](cfg: Config, databaseRepository: DatabaseRepository[F], resourceAccess: ResourceAccess[F], migrator: DBMigrator[F]) extends BotDBController[F] {
    override def build: Resource[F,Unit] = for {
      _ <- Resource.eval(migrator.migrate(cfg))
      _ <- populateMediaTable()
    } yield ()

    def populateMediaTable(): Resource[F, Unit] = for {
      resources <- resourceAccess.getResourcesByKind("")
      files = BotDBController.flattenResources(resources)
      _ <- Resource.eval(files.traverse_(databaseRepository.insertMedia))
    } yield ()
  }
}
