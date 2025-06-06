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

  val botName = "TestBot"

  val testShowRaw: String =
    """{
      |    "show_id": "test",
      |    "bot_name": "TestBot",
      |    "show_title": "Test Show Title",
      |    "show_upload_date": "2025-04-24T12:01:24.000Z",
      |    "show_duration": 10,
      |    "show_description": "Test Show Description",
      |    "show_is_live": false,
      |    "show_origin_automatic_caption": "https://www.youtube.com/api/timedtext?v=test"
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
    check(DBShow.getShowsQuery("TestBot"))
    check(DBShow.getRandomShowQuery("TestBot"))
    check(DBShow.getShowByShowQueryQuery(RandomQuery, "TestBot"))
    check(
      DBShow.getShowByShowQueryQuery(ShowQuery("title=test+show&description=description&minDuration=1"), "TestBot")
    )
  }

  databaseFixture.test(
    "DBShow: should return the test show sample"
  ) { fixture =>
    val resourceAssert = for {
      testShow           <- Resource.eval(IO.fromEither(decode[DBShowData](testShowRaw)))
      dbShow             <- fixture.resourceDBLayer.map(_.dbShow)
      testShows          <- Resource.eval(dbShow.getShows(botName))
      testShowsByKeyword <- Resource.eval(
        dbShow.getShowByShowQuery(ShowQuery("title=test+show&title=title"), botName)
      )
    } yield (testShows, testShowsByKeyword, testShow)

    resourceAssert.use { case (testShows, testShowsByKeyword, testShow) =>
      IO {
        assertEquals(testShows, List(testShow))
        assertEquals(testShowsByKeyword, List(testShow))
      }
    }
  }

  databaseFixture.test(
    "DBShow: should insert the test show sample"
  ) { fixture =>
    val testShowRaw2: String =
      """{
        |    "show_id": "https://www.youtube.com/watch?v=test2",
        |    "bot_name": "TestBot",
        |    "show_title": "Test 2 Show Title",
        |    "show_upload_date": "2025-04-24T12:01:24.000Z",
        |    "show_duration": 10,
        |    "show_description": "Test 2 Show Description",
        |    "show_is_live": false,
        |    "show_origin_automatic_caption": "https://www.youtube.com/api/timedtext?v=test2"
        |  }""".stripMargin
    val resourceAssert = for {
      testShow           <- Resource.eval(IO.fromEither(decode[DBShowData](testShowRaw2)))
      dbShow             <- fixture.resourceDBLayer.map(_.dbShow)
      _                  <- Resource.eval(dbShow.insertShow(testShow))
      testShowsByKeyword <- Resource.eval(
        dbShow.getShowByShowQuery(ShowQuery("test 2"), botName)
      )
      _ <- Resource.eval(dbShow.deleteShow(testShow))
    } yield (testShowsByKeyword, testShow)

    resourceAssert.use { case (testShowsByKeyword, testShow) =>
      IO.pure(assertEquals(testShowsByKeyword, List(testShow)))
    }
  }

  databaseFixture.test(
    "DBShow: get random show should return a show"
  ) { fixture =>
    val resourceAssert = for {
      dbShow     <- fixture.resourceDBLayer.map(_.dbShow)
      randomShow <- Resource.eval(
        dbShow.getRandomShow(botName)
      )
    } yield {
      assertEquals(randomShow.map(_.bot_name), Some(botName))
    }

    resourceAssert.use_
  }
}
