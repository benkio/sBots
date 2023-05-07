package com.benkio.main

import scala.concurrent.duration.Duration
import doobie.Transactor
import java.sql.DriverManager
import cats.effect.Resource
import cats.implicits._
import com.benkio.youtuboancheiobot.YoutuboAncheIoBot
import com.benkio.abarberobot.ABarberoBot
import com.benkio.richardphjbensonbot.RichardPHJBensonBot
import cats.effect.IO
import doobie.munit.analysisspec.IOChecker
import com.benkio.telegrambotinfrastructure.DBFixture
import munit.CatsEffectSuite

class MediaIntegritySpec extends CatsEffectSuite with DBFixture with IOChecker {

  override val munitTimeout = Duration.Inf

  override def transactor: doobie.Transactor[cats.effect.IO] = {
    Class.forName("org.sqlite.JDBC")
    val conn = DriverManager.getConnection(dbUrl)
    runMigrations(dbUrl, migrationTable, migrationPath)
    val transactor = Transactor.fromConnection[IO](conn)
    transactor
  }

  val allMessageMediaFiles =
    (RichardPHJBensonBot.messageRepliesData[IO] ++
    ABarberoBot.messageRepliesData[IO] ++
    YoutuboAncheIoBot.messageRepliesData[IO]).flatMap(_.mediafiles).distinct

    databaseFixture.test(
    "All the media should return some data. HUGE TEST, run manually".only
  ) { fixture =>
    val resourceAssert = for {
      resourceAccess <- fixture.resourceAccessResource
      mediaFiles <- Resource.pure(allMessageMediaFiles)
      files  <- mediaFiles.traverse(f => resourceAccess.getResourceFile(f))
    } yield files.forall(f =>
      if (f.length > 2048) true
      else { println(s"failure: file " + f.getName() + " is less then 2KB"); false })
    resourceAssert.use(IO.pure).assert
  }

}
