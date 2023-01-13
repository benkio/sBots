package com.benkio.botDB

import java.nio.file.Paths
import java.nio.file.Files
import cats.effect.IO
import doobie.Transactor

import munit._
import cats.effect.unsafe.implicits.global

import doobie.implicits._

class ITSpec extends FunSuite with DBConstants {

  def setEnv(key: String, value: String) = {
    val field = System.getenv().getClass.getDeclaredField("m")
    field.setAccessible(true)
    val map = field.get(System.getenv()).asInstanceOf[java.util.Map[java.lang.String, java.lang.String]]
    map.put(key, value)
  }

  test("botDB main should populate the migration with the files in resources") {

    val _ = setEnv("DB_CONNECTION_URL", dbUrl)

    val _ = Main.run(List(s"$resourcePath$testApplicationConf", "it")).unsafeRunSync()

    val transactor = Transactor.fromDriverManager[IO](
      "org.sqlite.JDBC",
      dbUrl,
      "",
      ""
    )
    val mediaContent = sql"SELECT media_name FROM media;".query[String].to[List].transact(transactor).unsafeRunSync()
    Files.deleteIfExists(Paths.get(dbPath))

    assert(mediaContent.length == 3)
    assert(mediaContent.diff(List("amazon.mp4", "facebook.mp3", "google.gif")).isEmpty)

  }
}
