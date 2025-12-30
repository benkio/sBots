package com.benkio.integration.integrationmunit.telegrambotinfrastructure.repository.db

import cats.effect.IO
import cats.effect.Resource
import com.benkio.integration.DBFixture
import com.benkio.richardphjbensonbot.RichardPHJBensonBot
import com.benkio.telegrambotinfrastructure.repository.db.DBMedia
import com.benkio.telegrambotinfrastructure.repository.db.DBMediaData
import doobie.munit.analysisspec.IOChecker
import doobie.Transactor
import munit.CatsEffectSuite

import java.sql.DriverManager

class ITDBMediaSpec extends CatsEffectSuite with DBFixture with IOChecker {

  val testMediaName          = "rphjb_MaSgus.mp3"
  val testMediaKind          = "some kind"
  val testMediaId            = RichardPHJBensonBot.sBotConfig.sBotInfo.botId
  val testMedia: DBMediaData = DBMediaData(
    media_name = testMediaName,
    bot_id = testMediaId.value,
    kinds = """"[]"""",
    media_sources =
      """"[\"https://www.dropbox.com/scl/fi/t5t952kwidqdyol4mutwv/rphjb_MaSgus.mp3?rlkey=f1fjff8ls4vjhs013plj1hrvs&dl=1\"]"""",
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
    check(DBMedia.getMediaQueryByMediaCount(botId = Some(testMediaId)))
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
    val expected: List[DBMediaData] = List(
      DBMediaData(
        media_name = "rphjb_ancheLaRabbiaHaUnCuore.txt",
        bot_id = testMediaId.value,
        kinds = """"[\"rphjb_LinkSources\"]"""",
        media_sources =
          """"[\"https://www.dropbox.com/sh/xqaatugvq8zcoyu/AABLDyXAOThfUrS3EoR3kL6ma/rphjb_LinkSources/ancheLaRabbiaHaUnCuore.txt?dl=1\"]"""",
        media_count = 0,
        created_at = "1669122665179",
        mime_type = "application/octet-stream"
      ),
      DBMediaData(
        media_name = "rphjb_live.txt",
        bot_id = testMediaId.value,
        kinds = """"[\"rphjb_LinkSources\"]"""",
        media_sources =
          """"[\"https://www.dropbox.com/sh/xqaatugvq8zcoyu/AACKI915JzajxuCSLy4spvbYa/rphjb_LinkSources/live.txt?dl=1\"]"""",
        media_count = 0,
        created_at = "1669122665277",
        mime_type = "application/octet-stream"
      ),
      DBMediaData(
        media_name = "rphjb_perCordeEGrida.txt",
        bot_id = testMediaId.value,
        kinds = """"[\"rphjb_LinkSources\"]"""",
        media_sources =
          """"[\"https://www.dropbox.com/sh/xqaatugvq8zcoyu/AAA6aMpu41wxHF3wFrYZTXGba/rphjb_LinkSources/perCordeEGrida.txt?dl=1\"]"""",
        media_count = 0,
        created_at = "1669122665311",
        mime_type = "application/octet-stream"
      ),
      DBMediaData(
        media_name = "rphjb_puntateCocktailMicidiale.txt",
        bot_id = testMediaId.value,
        kinds = """"[\"rphjb_LinkSources\"]"""",
        media_sources =
          """"[\"https://www.dropbox.com/sh/xqaatugvq8zcoyu/AAAfPoTfoPzhKys-DPI0YV8aa/rphjb_LinkSources/puntateCocktailMicidiale.txt?dl=1\"]"""",
        media_count = 0,
        created_at = "1669122665322",
        mime_type = "application/octet-stream"
      ),
      DBMediaData(
        media_name = "rphjb_puntateRockMachine.txt",
        bot_id = testMediaId.value,
        kinds = """"[\"rphjb_LinkSources\"]"""",
        media_sources =
          """"[\"https://www.dropbox.com/sh/xqaatugvq8zcoyu/AABSjYo7uJwDeQqKe3bA5cXea/rphjb_LinkSources/puntateRockMachine.txt?dl=1\"]"""",
        media_count = 0,
        created_at = "1669122665412",
        mime_type = "application/octet-stream"
      )
    )

    val resourceAssert = for {
      dbMedia <- fixture.resourceDBLayer.map(_.dbMedia)
      medias  <- Resource.eval(dbMedia.getMediaByKind(kind = "rphjb_LinkSources", botId = testMediaId))
      _ = println(s"[ITDBMediaSpec] TODO WTF $medias")
    } yield medias.zip(expected).foldLeft(true) { case (acc, (actual, exp)) => acc && checkMedia(actual, exp) }
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
        media_sources =
          """"[\"https://www.dropbox.com/scl/fi/zy8omnl7nj63l7ff350qf/rphjb_06Gif.mp4?rlkey=w88ow3t4ktru6txkgw2vuc7xk&dl=1\"]"""",
        media_count = 0,
        created_at = "1710379153288",
        mime_type = "image/gif"
      ),
      DBMediaData(
        media_name = "rphjb_3Minuti.mp4",
        bot_id = testMediaId.value,
        kinds = """"[]"""",
        media_sources =
          """"[\"https://www.dropbox.com/scl/fi/kzq7lkgzyle3tsp0ix292/rphjb_3Minuti.mp4?rlkey=pkaxiy7ue3w86ddczoz34gtc8&dl=1\"]"""",
        media_count = 0,
        created_at = "1674248160242",
        mime_type = "video/mp4"
      ),
      DBMediaData(
        media_name = "rphjb_9MesiUscireRientrare.mp3",
        bot_id = testMediaId.value,
        kinds = """"[]"""",
        media_sources =
          """"[\"https://www.dropbox.com/scl/fi/u4wuuygbhgsqs1qq50q98/rphjb_9MesiUscireRientrare.mp3?rlkey=unm5uv3mvzihzrekih3umhmt6&dl=1\"]"""",
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
