package com.benkio.botDB.mocks

import cats.effect.IO
import com.benkio.botDB.{Config, DBMigrator}

class MigratorMock extends DBMigrator[IO] {
  override def migrate(config: Config): IO[Int] = IO(42)
}
