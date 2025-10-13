package com.benkio.integration.integrationmunit.telegrambotinfrastructure.repository.db

import cats.effect.IO
import cats.effect.Resource
import com.benkio.integration.DBFixture
import com.benkio.telegrambotinfrastructure.model.show.RandomQuery
import com.benkio.telegrambotinfrastructure.model.show.ShowQuery
import com.benkio.telegrambotinfrastructure.model.SBotId
import com.benkio.telegrambotinfrastructure.repository.db.DBShow
import com.benkio.telegrambotinfrastructure.repository.db.DBShowData
import doobie.munit.analysisspec.IOChecker
import doobie.Transactor
import io.circe.parser.decode
import munit.CatsEffectSuite

import java.sql.DriverManager

class ITDBShowSpec extends CatsEffectSuite with DBFixture with IOChecker {

  val botId = SBotId("testbot")

  val testShowRaw: String =
    """{
      |    "show_id": "test",
      |    "bot_id": "testbot",
      |    "show_title": "Test Show Title",
      |    "show_upload_date": "2025-04-24T12:01:24.000Z",
      |    "show_duration": 10,
      |    "show_description": "Test Show Description",
      |    "show_is_live": false,
      |    "show_origin_automatic_caption": "Lorem ipsum dolor sit amet, consectetuer adipiscing elit.  Donec hendrerit tempor tellus.  Donec pretium posuere tellus.  Proin quam nisl, tincidunt et, mattis eget, convallis nec, purus.  Cum sociis natoque penatibus et magnis dis parturient montes, nascetur ridiculus mus.  Nulla posuere.  Donec vitae dolor.  Nullam tristique diam non turpis.  Cras placerat accumsan nulla.  Nullam rutrum.  Nam vestibulum accumsan nisl."
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
    val botId = SBotId("testBot")
    check(DBShow.getShowsQuery(botId))
    check(DBShow.getRandomShowQuery(botId))
    check(DBShow.getShowByShowQueryQuery(RandomQuery, botId))
    check(
      DBShow.getShowByShowQueryQuery(
        ShowQuery("title=test+show&description=description&caption=caption&minDuration=1"),
        botId
      )
    )
  }

  databaseFixture.test(
    "DBShow: should return the test show sample"
  ) { fixture =>
    val resourceAssert = for {
      testShow           <- Resource.eval(IO.fromEither(decode[DBShowData](testShowRaw)))
      dbShow             <- fixture.resourceDBLayer.map(_.dbShow)
      testShows          <- Resource.eval(dbShow.getShows(botId))
      testShowsByKeyword <- Resource.eval(
        dbShow.getShowByShowQuery(ShowQuery("title=test+show&title=title"), botId)
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
        |    "bot_id": "testbot",
        |    "show_title": "Test 2 Show Title",
        |    "show_upload_date": "2025-04-24T12:01:24.000Z",
        |    "show_duration": 10,
        |    "show_description": "Test 2 Show Description",
        |    "show_is_live": false,
        |    "show_origin_automatic_caption": "Lorem ipsum dolor sit amet, consectetuer adipiscing elit.  Donec hendrerit tempor tellus.  Donec pretium posuere tellus.  Proin quam nisl, tincidunt et, mattis eget, convallis nec, purus.  Cum sociis natoque penatibus et magnis dis parturient montes, nascetur ridiculus mus.  Nulla posuere.  Donec vitae dolor.  Nullam tristique diam non turpis.  Cras placerat accumsan nulla.  Nullam rutrum.  Nam vestibulum accumsan nisl."
        |  }""".stripMargin
    val resourceAssert = for {
      testShow           <- Resource.eval(IO.fromEither(decode[DBShowData](testShowRaw2)))
      dbShow             <- fixture.resourceDBLayer.map(_.dbShow)
      _                  <- Resource.eval(dbShow.insertShow(testShow))
      testShowsByKeyword <- Resource.eval(
        dbShow.getShowByShowQuery(ShowQuery("test 2"), botId)
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
        dbShow.getRandomShow(botId)
      )
    } yield {
      assertEquals(randomShow.map(_.bot_id), Some(botId.value))
    }

    resourceAssert.use_
  }
}
