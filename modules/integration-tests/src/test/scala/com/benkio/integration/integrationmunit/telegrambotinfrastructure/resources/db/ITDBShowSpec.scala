package com.benkio.integration.integrationmunit.telegrambotinfrastructure.resources.db

import io.circe.parser.decode
import com.benkio.telegrambotinfrastructure.resources.db.DBShow
import com.benkio.telegrambotinfrastructure.model.show.ShowQuery
import com.benkio.telegrambotinfrastructure.model.show.RandomQuery
import com.benkio.telegrambotinfrastructure.resources.db.DBShowData
import doobie.Transactor
import java.sql.DriverManager
import cats.effect.Resource
import com.benkio.integration.DBFixture
import munit.CatsEffectSuite
import doobie.munit.analysisspec.IOChecker

import cats.effect.IO

class ITDBShowSpec extends CatsEffectSuite with DBFixture with IOChecker {

  val botName = "RichardPHJBensonBot"

  val testShowRaw: String =
    """{
      |    "show_url": "https://www.youtube.com/watch?v=t3kx8KMfdKo",
      |    "show_title": "Richard Benson | Ottava Nota | Alex Britti e Mario Magnotta da L'Aquila (8 Gennaio 1997) [INEDITA]",
      |    "show_upload_date": "20250108",
      |    "show_duration": 3453,
      |    "show_description": "Si ringrazia Renzo Di Pietro, che ha messo a disposizione per le Brigate Benson il suo prezioso archivio di nastri di Ottava Nota. \n\nGRUPPO TELEGRAM: https://bit.ly/brigate-benson-gruppo-telegram\nCANALE TELEGRAM: https://bit.ly/brigate-benson-canale-telegram\nPAGINA FACEBOOK: https://bit.ly/brigate-benson-facebook\n#richardbenson #ottavanota",
      |    "show_is_live": false,
      |    "show_origin_automatic_caption": "https://www.youtube.com/api/timedtext?v=t3kx8KMfdKo&ei=MnehZ7efN9y-kucP0sHU-AU&caps=asr&opi=112496729&xoaf=4&hl=en&ip=0.0.0.0&ipbits=0&expire=1738660258&sparams=ip%2Cipbits%2Cexpire%2Cv%2Cei%2Ccaps%2Copi%2Cxoaf&signature=6799F2EA1441D4000C3E778D7ADE93FB77D663BA.D97F90383702A3D5EE65D14648B99350951A1C7F&key=yt8&kind=asr&lang=it&fmt=json3",
      |    "bot_name": "RichardPHJBensonBot"
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
        dbShow.getShowByShowQuery(ShowQuery("title=Alex+Britti+e+Mario+Magnotta"), botName)
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
