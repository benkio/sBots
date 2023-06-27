package com.benkio.integration.telegrambotinfrastructure.resources.db

import doobie.Transactor
import java.sql.DriverManager
import com.benkio.telegrambotinfrastructure.resources.db.DBMediaData
import com.benkio.telegrambotinfrastructure.resources.db.DBMedia
import cats.effect.Resource
import com.benkio.telegrambotinfrastructure.DBFixture
import munit.CatsEffectSuite
import doobie.munit.analysisspec.IOChecker

import cats.effect.IO

class ITDBMediaSpec extends CatsEffectSuite with DBFixture with IOChecker {

  val testMediaName   = "rphjb_MaSgus.mp3"
  val testMediaKind   = "some kind"
  val testMediaPrefix = "rphjb"
  val testMedia: DBMediaData = DBMediaData(
    testMediaName,
    None,
    "https://www.dropbox.com/sh/xqaatugvq8zcoyu/AACBnRH33traQAKBGy9bidu0a/rphjb_MaSgus.mp3?dl=1",
    0,
    "1669122662279"
  )

  def checkMedia(actual: DBMediaData, expected: DBMediaData): Boolean = {
    val result = actual.media_name == expected.media_name &&
      actual.media_url == expected.media_url &&
      actual.kind == expected.kind &&
      actual.media_count == expected.media_count
    if (!result) println(s"checkMedia test failure: $actual ≄ $expected")
    result
  }

  override def transactor: doobie.Transactor[cats.effect.IO] = {
    Class.forName("org.sqlite.JDBC")
    val conn = DriverManager.getConnection(dbUrl)
    runMigrations(dbUrl, migrationTable, migrationPath)
    val transactor = Transactor.fromConnection[IO](conn, None)
    transactor
  }

  test(
    "DBMedia queries should check"
  ) {
    check(DBMedia.getMediaQueryByName(testMediaName))
    check(DBMedia.getMediaQueryByKind(testMediaKind))
    check(DBMedia.getMediaQueryByMediaCount(mediaNamePrefix = Some(testMediaPrefix)))
  }

  databaseFixture.test(
    "DBMedia.getMedia should return the expected media"
  ) { fixture =>
    val resourceAssert = for {
      dbMedia <- fixture.resourceDBLayer.map(_.dbMedia)
      media   <- Resource.eval(dbMedia.getMedia(filename = testMediaName, cache = false))
    } yield checkMedia(media, testMedia)
    resourceAssert.use(IO.pure).assert
  }

  databaseFixture.test(
    "DBMedia.getMediaByKind should return the expected media list"
  ) { fixture =>
    val expected: List[DBMediaData] = List(
      DBMediaData(
        "ancheLaRabbiaHaUnCuore.txt",
        Some("rphjb_LinkSources"),
        "https://www.dropbox.com/sh/xqaatugvq8zcoyu/AABLDyXAOThfUrS3EoR3kL6ma/rphjb_LinkSources/ancheLaRabbiaHaUnCuore.txt?dl=1",
        0,
        "1669122665179"
      ),
      DBMediaData(
        "live.txt",
        Some("rphjb_LinkSources"),
        "https://www.dropbox.com/sh/xqaatugvq8zcoyu/AACKI915JzajxuCSLy4spvbYa/rphjb_LinkSources/live.txt?dl=1",
        0,
        "1669122665277"
      ),
      DBMediaData(
        "perCordeEGrida.txt",
        Some("rphjb_LinkSources"),
        "https://www.dropbox.com/sh/xqaatugvq8zcoyu/AAA6aMpu41wxHF3wFrYZTXGba/rphjb_LinkSources/perCordeEGrida.txt?dl=1",
        0,
        "1669122665311"
      ),
      DBMediaData(
        "puntateCocktailMicidiale.txt",
        Some("rphjb_LinkSources"),
        "https://www.dropbox.com/sh/xqaatugvq8zcoyu/AAAfPoTfoPzhKys-DPI0YV8aa/rphjb_LinkSources/puntateCocktailMicidiale.txt?dl=1",
        0,
        "1669122665322"
      ),
      DBMediaData(
        "puntateRockMachine.txt",
        Some("rphjb_LinkSources"),
        "https://www.dropbox.com/sh/xqaatugvq8zcoyu/AABSjYo7uJwDeQqKe3bA5cXea/rphjb_LinkSources/puntateRockMachine.txt?dl=1",
        0,
        "1669122665412"
      )
    )

    val resourceAssert = for {
      dbMedia <- fixture.resourceDBLayer.map(_.dbMedia)
      medias  <- Resource.eval(dbMedia.getMediaByKind(kind = "rphjb_LinkSources"))
    } yield medias.zip(expected).foldLeft(true) { case (acc, (actual, exp)) => acc && checkMedia(actual, exp) }
    resourceAssert.use(IO.pure).assert
  }

  databaseFixture.test(
    "DBMedia.getMediaByMediaCount should return the expected list of media"
  ) { fixture =>
    val expected: List[DBMediaData] = List(
      DBMediaData(
        "rphjb_06.mp4",
        None,
        "https://www.dropbox.com/s/xndnmq4firkc9lu/rphjb_06.mp4?dl=1",
        0,
        "1674248160144"
      ),
      DBMediaData(
        "rphjb_3Minuti.mp4",
        None,
        "https://www.dropbox.com/s/axd2jcti4pxd1n7/rphjb_3Minuti.mp4?dl=1",
        0,
        "1674248160242"
      ),
      DBMediaData(
        "rphjb_9MesiUscireRientrare.mp3",
        None,
        "https://www.dropbox.com/s/fwvtyo9vxtxt2p5/rphjb_9MesiUscireRientrare.mp3?dl=1",
        0,
        "1681990713607"
      )
    )

    val resourceAssert = for {
      dbMedia <- fixture.resourceDBLayer.map(_.dbMedia)
      medias  <- Resource.eval(dbMedia.getMediaByMediaCount(limit = 3))
    } yield medias.zip(expected).foldLeft(true) { case (acc, (actual, exp)) =>
      acc && checkMedia(actual, exp)
    }
    resourceAssert.use(IO.pure).assert
  }

}
