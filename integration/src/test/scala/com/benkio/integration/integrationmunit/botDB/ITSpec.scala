package com.benkio.integration.integrationmunit.botDB

import com.benkio.botDB.Config
import com.benkio.botDB.Main
import java.nio.file.Paths
import java.nio.file.Files
import cats.effect.IO
import doobie.Transactor
import munit.*
import cats.effect.unsafe.implicits.global
import doobie.implicits.*

class ITSpec extends FunSuite with DBConstants {

  test("botDB main should populate the migration with the files in resources") {

    // val _                       = setEnv("DB_CONNECTION_URL", dbUrl)
    val testApplicationConfPath = s"$resourcePath$testApplicationConf"
    val config                  = Config.loadConfig(Some(testApplicationConfPath)).unsafeRunSync()
    val _                       = Main.run(List(testApplicationConfPath, "test")).unsafeRunSync()

    val transactor = Transactor.fromDriverManager[IO](
      "org.sqlite.JDBC",
      config.url,
      "",
      "",
      None
    )
    val mediaContent = sql"SELECT media_name FROM media;".query[String].to[List].transact(transactor).unsafeRunSync()
    Files.deleteIfExists(Paths.get(config.dbName))

    assert(mediaContent.length == 3)
    assert(mediaContent.diff(List("amazon.mp4", "facebook.mp3", "google.gif")).isEmpty)

  }
}
