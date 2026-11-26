package com.benkio.botDB.db

import cats.effect.IO
import com.benkio.botDB.TestData.*
import munit.CatsEffectSuite

class DBMigratorSpec extends CatsEffectSuite {

  val dbMigrator: DBMigrator[IO] = DBMigrator.apply[IO]

  test("Migrate build should run without raising exceptions") {
    dbMigrator
      .migrate(config)
      .attempt
      .map(
        _.fold(
          e => fail(s"Unexpected error: ${e.getMessage}"),
          _ => assert(true, "DBMigrator.migrate didn't throw an error")
        )
      )
  }
}
