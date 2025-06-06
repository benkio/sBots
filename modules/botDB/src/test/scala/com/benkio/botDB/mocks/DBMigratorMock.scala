package com.benkio.botDB.mocks

import cats.effect.IO
import com.benkio.botDB.config.Config
import com.benkio.botDB.db.DBMigrator

class DBMigratorMock extends DBMigrator[IO] {
  override def migrate(config: Config): IO[Int] = IO(42)
}
