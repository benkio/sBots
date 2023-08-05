package com.benkio.integration.integrationscalatest.main

import com.benkio.integration.SlowTest
import cats.effect.unsafe.implicits.global
import log.effect.LogLevels
import log.effect.fs2.SyncLogWriter.consoleLogUpToLevel
import log.effect.LogWriter
import com.benkio.integration.DBFixtureResources
import com.benkio.telegrambotinfrastructure.model.MediaFile
import com.benkio.youtuboanchei0bot.YouTuboAncheI0Bot
import com.benkio.m0sconibot.M0sconiBot
import com.benkio.abarberobot.ABarberoBot
import com.benkio.richardphjbensonbot.RichardPHJBensonBot
import cats.effect.IO
import com.benkio.integration.DBFixture
import org.scalatest._
import org.scalatest.funsuite.FixtureAnyFunSuite

class MediaIntegritySpec extends FixtureAnyFunSuite with ParallelTestExecution {

  case class FixtureParam(fixture: DBFixtureResources)

  implicit val log: LogWriter[IO] = consoleLogUpToLevel(LogLevels.Info)

  // TODO: find a way to include all the bots 
  val allMessageMediaFiles: List[MediaFile] =
    (RichardPHJBensonBot.messageRepliesData[IO] ++
      ABarberoBot.messageRepliesData[IO] ++
      YouTuboAncheI0Bot.messageRepliesData[IO] ++
      M0sconiBot.messageRepliesData[IO]).flatMap(_.mediafiles).distinctBy(_.filename)

  def withFixture(test: OneArgTest) = {
    val fixtureParam = FixtureParam(DBFixture.fixtureSetup(null))

    try {
      withFixture(test.toNoArgTest(fixtureParam)) // "loan" the fixture to the test
    }
    finally DBFixture.teardownFixture(fixtureParam.fixture)
  }

  def checkFile(mf: MediaFile): Unit =
    // ignore to not run in CI, remove sometimes to check all the messages files
    test(s"${mf.filename} should return some data", SlowTest) { case FixtureParam(fixture) =>
      val resourceAssert = for {
        resourceAccess <- fixture.resourceAccessResource
        file           <- resourceAccess.getResourceFile(mf)
      } yield file.length > (5 * 1024)
      assert(resourceAssert.use(IO.pure).unsafeRunSync())
    }

  allMessageMediaFiles.foreach(checkFile)
}
