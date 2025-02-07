package com.benkio.integration.integrationmunit.telegrambotinfrastructure.resources.db

import cats.effect.IO
import cats.effect.Resource
import com.benkio.integration.DBFixture
import com.benkio.telegrambotinfrastructure.model.show.RandomQuery
import com.benkio.telegrambotinfrastructure.model.show.ShowQuery
import com.benkio.telegrambotinfrastructure.resources.db.DBShow
import com.benkio.telegrambotinfrastructure.resources.db.DBShowData
import doobie.munit.analysisspec.IOChecker
import doobie.Transactor
import io.circe.parser.decode
import munit.CatsEffectSuite

import java.sql.DriverManager

class ITDBShowSpec extends CatsEffectSuite with DBFixture with IOChecker {

  val botName = "RichardPHJBensonBot"

  val testShowRaw: String =
    """{
      |    "show_url": "https://www.youtube.com/watch?v=J60iupukb6c",
      |    "bot_name": "RichardPHJBensonBot",
      |    "show_title": "Cocktail Micidiale 25 febbraio 2005 (puntata completa) l'ultima regia di Ghent",
      |    "show_upload_date": "20180611",
      |    "show_duration": 831,
      |    "show_description": "#RichardBenson #CocktailMicidiale",
      |    "show_is_live": false,
      |    "show_origin_automatic_caption": "https://www.youtube.com/api/timedtext?v=J60iupukb6c&ei=rz6lZ_-LHPm3kucP1dfk6Ao&caps=asr&opi=112496729&xoaf=4&hl=en&ip=0.0.0.0&ipbits=0&expire=1738907935&sparams=ip%2Cipbits%2Cexpire%2Cv%2Cei%2Ccaps%2Copi%2Cxoaf&signature=C2C1973F010B5F08BC840B19B71964CCA36BA458.C0E4842069419D32BED2B351101080B1FB5A94A3&key=yt8&kind=asr&lang=it&fmt=json3"
      |  }""".stripMargin

  override def transactor: doobie.Transactor[cats.effect.IO] = {
    Class.forName("org.sqlite.JDBC")
    val conn = DriverManager.getConnection(DBFixture.dbUrl)
    DBFixture.runMigrations(DBFixture.dbUrl, DBFixture.migrationTable, DBFixture.migrationPath)
    val transactor = Transactor.fromConnection[IO](conn, None)
    transactor
  }

  test(
    "DBShow queries should check"
  ) {
    check(DBShow.getShowsQuery("botName"))
    check(DBShow.getShowByShowQueryQuery(RandomQuery, "botName"))
    check(
      DBShow.getShowByShowQueryQuery(ShowQuery("title=paul+gilbert&description=samurai&minDuration=300"), "botName")
    )
  }

  databaseFixture.test(
    "DBShow: should return the test show sample"
  ) { fixture =>
    val resourceAssert = for {
      testShow    <- Resource.eval(IO.fromEither(decode[DBShowData](testShowRaw)))
      dbShow      <- fixture.resourceDBLayer.map(_.dbShow)
      bensonShows <- Resource.eval(dbShow.getShows(botName))
      bensonShowsByKeyword <- Resource.eval(
        dbShow.getShowByShowQuery(ShowQuery("title=Cocktail+Micidiale&title=ghent"), botName)
      )
    } yield (bensonShows, bensonShowsByKeyword, testShow)

    resourceAssert.use { case (bensonShows, bensonShowsByKeyword, testShow) =>
      IO {
        assert(bensonShows.exists(_ == testShow))
        assert(bensonShowsByKeyword.exists(_ == testShow))
      }
    }
  }

  databaseFixture.test(
    "DBShow: should insert the test show sample"
  ) { fixture =>
    val testShowInput = """{
                          |  "show_url": "https://www.youtube.com/watch?v=test!",
                          |  "show_title": "Richard Benson Bot Test",
                          |  "show_upload_date": "20250108",
                          |  "show_duration": 3453,
                          |  "show_description": "Test del bot!!!",
                          |  "show_is_live": false,
                          |  "show_origin_automatic_caption": "",
                          |  "bot_name": "RichardPHJBensonBot"
                          |}""".stripMargin
    val resourceAssert = for {
      testShow <- Resource.eval(IO.fromEither(decode[DBShowData](testShowInput)))
      dbShow   <- fixture.resourceDBLayer.map(_.dbShow)
      _        <- Resource.eval(dbShow.insertShow(testShow))
      bensonShowsByKeyword <- Resource.eval(
        dbShow.getShowByShowQuery(ShowQuery("richard benson bot test"), botName)
      )
      _ <- Resource.eval(dbShow.deleteShow(testShow))
    } yield (bensonShowsByKeyword, testShow)

    resourceAssert.use { case (bensonShowsByKeyword, testShow) =>
      IO.pure(assert(bensonShowsByKeyword.exists(_ == testShow)))
    }
  }
}
