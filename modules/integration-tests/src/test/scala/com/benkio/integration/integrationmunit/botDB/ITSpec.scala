package com.benkio.integration.integrationmunit.botDB

import com.benkio.botDB.config.Config
import com.benkio.botDB.Main
import java.nio.file.Paths
import java.nio.file.Files
import cats.effect.IO
import doobie.Transactor
import munit.*
import scala.concurrent.duration.*

import doobie.implicits.*

class ITSpec extends CatsEffectSuite with DBConstants {
  // TODO: make the tests faster and remove this eventually
  override val munitIOTimeout = 2.minutes

  test("botDB main should populate the migration with the files in resources") {

    val testApplicationConfPath = s"$resourcePath$testApplicationConf"

    for
      config <- Config.loadConfig(Some(testApplicationConfPath))
      _      <- Main.run(List(testApplicationConfPath, "test"))

      transactor = Transactor.fromDriverManager[IO](
        "org.sqlite.JDBC",
        config.url,
        "",
        "",
        None
      )
      mediaContent <- sql"SELECT media_name FROM media;".query[String].to[List].transact(transactor)
      _            <- IO(Files.deleteIfExists(Paths.get(config.dbName)))
    yield
      assert(mediaContent.length == 3, s"[ITSpec] Expected 3 content, got: ${mediaContent.length}")
      assert(mediaContent.diff(List("amazon.mp4", "facebook.mp3", "google.gif")).isEmpty)

  }
}
