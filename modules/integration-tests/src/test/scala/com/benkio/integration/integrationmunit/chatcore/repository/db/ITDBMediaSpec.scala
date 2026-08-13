package com.benkio.integration.integrationmunit.chatcore.repository.db

import cats.effect.IO
import cats.effect.Resource
import com.benkio.chatcore.repository.db.DBMedia
import com.benkio.chatcore.repository.db.DBMediaData
import com.benkio.chattelegramadapter.SBot
import com.benkio.integration.DBFixture
import com.benkio.RichardPHJBensonBot.RichardPHJBensonBot
import com.benkio.XahLeeBot.XahLeeBot
import doobie.munit.analysisspec.IOChecker
import doobie.Transactor
import munit.CatsEffectSuite

import java.sql.DriverManager

class ITDBMediaSpec extends CatsEffectSuite with DBFixture with IOChecker {

  val sBotConfig             = SBot.buildSBotConfig(RichardPHJBensonBot.sBotInfo)
  val testMediaName          = "rphjb_MaSgus.mp3"
  val testMediaKind          = "some kind"
  val testMediaId            = sBotConfig.sBotInfo.botId
  val testMedia: DBMediaData = DBMediaData(
    media_name = testMediaName,
    bot_id = testMediaId.value,
    kinds = """"[]"""",
    media_sources = """"[\"https://mega.nz/file/zOAHWaAJ#PhxBHN2vUOyPVBlrrdjJgZiEGGotu5xTmrV2SJKFZvA\"]"""",
    media_count = 0,
    created_at = "1669122662279",
    mime_type = "image/gif"
  )

  def checkMedia(actual: DBMediaData, expected: DBMediaData): Boolean = {
    val result =
      actual.media_name == expected.media_name &&
        actual.bot_id == expected.bot_id &&
        actual.media_sources == expected.media_sources &&
        actual.kinds == expected.kinds &&
        actual.media_count == expected.media_count
    if !result then {
      println(s"checkMedia test failure: $actual ≄ $expected")
      println(s"test 1 - ${actual.media_name == expected.media_name}")
      println(s"test 2 - ${actual.bot_id == expected.bot_id}")
      println(s"test 3 - ${actual.media_sources == expected.media_sources}")
      println(s"test 4 - ${actual.kinds == expected.kinds}")
      println(s"test 5 - ${actual.media_count == expected.media_count}")
    }
    result
  }

  override def transactor: doobie.Transactor[cats.effect.IO] = {
    Class.forName("org.sqlite.JDBC")
    val conn = DriverManager.getConnection(DBFixture.dbUrl)
    DBFixture.runMigrations(DBFixture.dbUrl, DBFixture.migrationTable, DBFixture.migrationPath)
    val transactor = Transactor.fromConnection[IO](conn, None)
    transactor
  }

  test(
    "DBMedia queries should check"
  ) {
    check(DBMedia.getMediaQueryByName(testMediaName))
    check(DBMedia.getMediaQueryByRandom(testMediaId))
    check(DBMedia.getMediaQueryByKind(kind = testMediaKind, botId = testMediaId))
    check(DBMedia.getMediaQueryByMediaCount(limit = 20, botId = Some(testMediaId)))
  }

  databaseFixture.test(
    "DBMedia.getMedia should return the expected media"
  ) { fixture =>
    val resourceAssert = for {
      dbMedia  <- fixture.resourceDBLayer.map(_.dbMedia)
      mediaOpt <- Resource.eval(dbMedia.getMedia(filename = testMediaName, cache = false))
    } yield mediaOpt.fold(false)(checkMedia(_, testMedia))
    resourceAssert.use(IO.pure).assert
  }

  databaseFixture.test(
    "DBMedia.getMediaByKind should return the expected media list"
  ) { fixture =>
    val xahBotId            = SBot.buildSBotConfig(XahLeeBot.sBotInfo).sBotInfo.botId
    val testKind            = "alanmackenzie"
    val expectedSampleFiles = Set("xah_AlanFak.mp3", "xah_AlanMackenzieFak.mp3", "xah_AlanMackenzieFak2.mp3")
    val resourceAssert      = for {
      dbMedia <- fixture.resourceDBLayer.map(_.dbMedia)
      medias  <- Resource.eval(dbMedia.getMediaByKind(kind = testKind, botId = xahBotId))
    } yield {
      assert(
        medias.nonEmpty,
        s"Expected at least one media for kind '$testKind' and botId '${xahBotId.value}'"
      )
      assert(medias.forall(_.bot_id == xahBotId.value), "All medias should belong to XahLeeBot")
      assert(medias.forall(_.kinds.contains(testKind)), s"All medias should contain kind '$testKind'")
      val filenames = medias.map(_.media_name).toSet
      assert(
        expectedSampleFiles.subsetOf(filenames),
        s"Expected sample files missing for kind '$testKind': ${expectedSampleFiles.diff(filenames)}"
      )
      true
    }
    resourceAssert.use(IO.pure).assert
  }

  databaseFixture.test(
    "DBMedia.getMediaByMediaCount should return the expected list of media"
  ) { fixture =>
    val expected: List[DBMediaData] = List(
      DBMediaData(
        media_name = "rphjb_06Gif.mp4",
        bot_id = testMediaId.value,
        kinds = """"[]"""",
        media_sources = """"[\"https://mega.nz/file/mPYCWJwY#q0_7BNEG0Uj2qxDTzr3W8AxfvbGr_ZMwr8F-_yJQh-8\"]"""",
        media_count = 0,
        created_at = "1710379153288",
        mime_type = "image/gif"
      ),
      DBMediaData(
        media_name = "rphjb_3Minuti.mp4",
        bot_id = testMediaId.value,
        kinds = """"[]"""",
        media_sources = """"[\"https://mega.nz/file/PaY0lIIY#seJaOyoxTNqIi1oGVE2dWL0GSvLRZAtfUYQDk2KU0UM\"]"""",
        media_count = 0,
        created_at = "1674248160242",
        mime_type = "video/mp4"
      ),
      DBMediaData(
        media_name = "rphjb_9MesiUscireRientrare.mp3",
        bot_id = testMediaId.value,
        kinds = """"[]"""",
        media_sources = """"[\"https://mega.nz/file/6HpUxC5a#2_tLJMV6r4PiUVrr1TX_Au-lt17w0LTlaHNnwBF8vKg\"]"""",
        media_count = 0,
        created_at = "1681990713607",
        mime_type = "audio/mpeg"
      )
    )

    val resourceAssert = for {
      dbMedia <- fixture.resourceDBLayer.map(_.dbMedia)
      medias  <- Resource.eval(dbMedia.getMediaByMediaCount(limit = 3, botId = Some(testMediaId)))
      _       <- Resource.eval(IO.println(medias))
    } yield medias.zip(expected).foldLeft(true) { case (acc, (actual, exp)) =>
      acc && checkMedia(actual, exp)
    }
    resourceAssert.use(IO.pure).assert
  }

  databaseFixture.test(
    "DBMedia.getMedia should return the expected media"
  ) { fixture =>
    val resourceAssert = for {
      dbMedia <- fixture.resourceDBLayer.map(_.dbMedia)
      media   <- Resource.eval(dbMedia.getRandomMedia(testMediaId))
    } yield {
      assertEquals(media.map(_.bot_id), Some(testMediaId.value))
      assert(media.map(_.media_name.startsWith(testMediaId.value)).getOrElse(false))
    }
    resourceAssert.use(IO.pure)
  }

}
