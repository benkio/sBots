package com.benkio.integration.integrationscalatest.main

import cats.effect.Resource
import com.benkio.xahleebot.XahLeeBot
import telegramium.bots.client.Method
import telegramium.bots.high.Api
import com.benkio.telegrambotinfrastructure.BackgroundJobManager
import com.benkio.calandrobot.CalandroBot
import com.benkio.telegrambotinfrastructure.model.ReplyBundle
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
import cats.syntax.all._
import com.benkio.integration.DBFixture
import org.scalatest._
import org.scalatest.funsuite.FixtureAnyFunSuite

class MediaIntegritySpec extends FixtureAnyFunSuite with ParallelTestExecution {

  case class FixtureParam(fixture: DBFixtureResources)

  given Api[IO] = new Api[IO] {
    def execute[Res](method: Method[Res]): IO[Res] = IO(???)
  }
  given LogWriter[IO]                    = consoleLogUpToLevel(LogLevels.Info)
  val initialFixture: DBFixtureResources = DBFixture.fixtureSetup(null)

  val allMessageMediaFiles: List[MediaFile] =
    (for
      dbLayer        <- initialFixture.resourceDBLayer
      resourceAccess <- initialFixture.resourceAccessResource
      emptyBackgroundJobManager <- Resource.eval(
        BackgroundJobManager[IO](
          dbLayer.dbSubscription,
          dbLayer.dbShow,
          resourceAccess,
          ""
        )
      )
      mediaFiles <- Resource.eval(
        (RichardPHJBensonBot.messageRepliesData[IO] ++
          RichardPHJBensonBot.commandRepliesData[IO](emptyBackgroundJobManager, dbLayer) ++
          ABarberoBot.messageRepliesData[IO] ++
          ABarberoBot.commandRepliesData[IO](emptyBackgroundJobManager, dbLayer) ++
          YouTuboAncheI0Bot.messageRepliesData[IO] ++
          YouTuboAncheI0Bot.commandRepliesData[IO](emptyBackgroundJobManager, dbLayer) ++
          M0sconiBot.messageRepliesData[IO] ++
          M0sconiBot.commandRepliesData[IO](dbLayer) ++
          CalandroBot.messageRepliesData[IO] ++
          CalandroBot.commandRepliesData[IO](dbLayer) ++
          XahLeeBot.messageRepliesData[IO] ++
          XahLeeBot.commandRepliesData[IO](emptyBackgroundJobManager, dbLayer))
          .flatTraverse((r: ReplyBundle[IO]) => ReplyBundle.getMediaFiles[IO](r))
      )
    yield mediaFiles.distinctBy(_.filename))
      .use(IO.pure)
      .unsafeRunSync()

  def withFixture(test: OneArgTest): Outcome = {
    val fixtureParam = FixtureParam(DBFixture.fixtureSetup(null))

    try {
      withFixture(test.toNoArgTest(fixtureParam)) // "loan" the fixture to the test
    } finally DBFixture.teardownFixture(fixtureParam.fixture)
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
