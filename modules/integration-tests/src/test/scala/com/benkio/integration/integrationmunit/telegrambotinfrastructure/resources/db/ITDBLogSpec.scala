package com.benkio.integration.integrationmunit.telegrambotinfrastructure.resources.db

import cats.effect.IO
import cats.effect.Resource
import com.benkio.integration.DBFixture
import doobie.munit.analysisspec.IOChecker
import doobie.Transactor
import munit.CatsEffectSuite

import java.sql.DriverManager

class ITDBLogSpec extends CatsEffectSuite with DBFixture with IOChecker {

  override def transactor: doobie.Transactor[cats.effect.IO] = {
    Class.forName("org.sqlite.JDBC")
    val conn = DriverManager.getConnection(DBFixture.dbUrl)
    DBFixture.runMigrations(DBFixture.dbUrl, DBFixture.migrationTable, DBFixture.migrationPath)
    val transactor = Transactor.fromConnection[IO](conn, None)
    transactor
  }

  databaseFixture.test(
    "DBLog.getLastLog should return the no log if the table is empty"
  ) { fixture =>
    val resourceAssert = for {
      dbLog   <- fixture.resourceDBLayer.map(_.dbLog)
      testLog <- Resource.eval(dbLog.getLastLog())
    } yield assert(None == testLog)
    resourceAssert.use(IO.pure).assert
  }

  databaseFixture.test(
    "DBLog.writeLog should write the log"
  ) { fixture =>
    val logMessage     = "Test Message"
    val resourceAssert = for {
      dbLog   <- fixture.resourceDBLayer.map(_.dbLog)
      _       <- Resource.eval(dbLog.writeLog(logMessage = logMessage))
      testLog <- Resource.eval(dbLog.getLastLog())
    } yield assert(Some(logMessage) == testLog.map(_.message))
    resourceAssert.use(IO.pure).assert
  }

  databaseFixture.test(
    "DBLog.getLastLog should return the last log"
  ) { fixture =>
    val logMessage     = "Test Message"
    val logMessage2    = "Test Message 2"
    val resourceAssert = for {
      dbLog   <- fixture.resourceDBLayer.map(_.dbLog)
      _       <- Resource.eval(dbLog.writeLog(logMessage = logMessage))
      _       <- Resource.eval(dbLog.writeLog(logMessage = logMessage2))
      testLog <- Resource.eval(dbLog.getLastLog())
    } yield assert(Some(logMessage2) == testLog.map(_.message))
    resourceAssert.use(IO.pure).assert
  }
}
