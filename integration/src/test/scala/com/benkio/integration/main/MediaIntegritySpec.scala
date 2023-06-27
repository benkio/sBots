package com.benkio.integration.main

import com.benkio.telegrambotinfrastructure.model.MediaFile
import scala.concurrent.duration.Duration
import doobie.Transactor
import java.sql.DriverManager
import com.benkio.youtuboancheiobot.YoutuboAncheIoBot
import com.benkio.abarberobot.ABarberoBot
import com.benkio.richardphjbensonbot.RichardPHJBensonBot
import cats.effect.IO
import doobie.munit.analysisspec.IOChecker
import com.benkio.integration.DBFixture
import munit.CatsEffectSuite

class MediaIntegritySpec extends CatsEffectSuite with DBFixture with IOChecker {

  override val munitIOTimeout = Duration.Inf

  override def transactor: doobie.Transactor[cats.effect.IO] = {
    Class.forName("org.sqlite.JDBC")
    val conn = DriverManager.getConnection(dbUrl)
    runMigrations(dbUrl, migrationTable, migrationPath)
    val transactor = Transactor.fromConnection[IO](conn, None)
    transactor
  }

  val allMessageMediaFiles: List[MediaFile] =
    (RichardPHJBensonBot.messageRepliesData[IO] ++
      ABarberoBot.messageRepliesData[IO] ++
      YoutuboAncheIoBot.messageRepliesData[IO]).flatMap(_.mediafiles).distinct

  def checkFile(mf: MediaFile): Unit =
    databaseFixture.test(
      s"${mf.filename} should return some data".ignore // ignore to not run in CI, remove sometimes to check all the messages files
    ) { fixture =>
      val resourceAssert = for {
        resourceAccess <- fixture.resourceAccessResource
        file           <- resourceAccess.getResourceFile(mf)
      } yield file.length > (5 * 1024)
      resourceAssert.use(IO.pure).assert
    }

  allMessageMediaFiles.foreach(checkFile)

}
