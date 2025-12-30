package com.benkio.integration.integrationscalatest.main

import cats.effect.unsafe.implicits.global
import cats.effect.IO
import cats.effect.Resource
import cats.implicits.*
import com.benkio.abarberobot.ABarberoBot
import com.benkio.calandrobot.CalandroBot
import com.benkio.integration.DBFixture
import com.benkio.integration.DBFixtureResources
import com.benkio.integration.SlowTest
import com.benkio.m0sconibot.M0sconiBot
import com.benkio.richardphjbensonbot.RichardPHJBensonBot
import com.benkio.telegrambotinfrastructure.mocks.ApiMock.given
import com.benkio.telegrambotinfrastructure.model.media.getMediaResourceFile
import com.benkio.telegrambotinfrastructure.model.reply.MediaFile
import com.benkio.telegrambotinfrastructure.model.reply.ReplyBundle
import com.benkio.telegrambotinfrastructure.BackgroundJobManager
import com.benkio.xahleebot.XahLeeBot
import com.benkio.youtuboanchei0bot.YouTuboAncheI0Bot
import log.effect.fs2.SyncLogWriter.consoleLogUpToLevel
import log.effect.LogLevels
import log.effect.LogWriter
import org.scalatest.*
import org.scalatest.funsuite.FixtureAnyFunSuite

class MediaIntegritySpec extends FixtureAnyFunSuite with ParallelTestExecution {

  case class FixtureParam(fixture: DBFixtureResources)

  given LogWriter[IO]                    = consoleLogUpToLevel(LogLevels.Info)
  val initialFixture: DBFixtureResources = DBFixture.fixtureSetup(null)

  val allMessageMediaFiles: Resource[IO, List[MediaFile]] =
    for {
      dbLayer                   <- initialFixture.resourceDBLayer
      repository                <- initialFixture.repositoryResource
      emptyBackgroundJobManager <- Resource.eval(
        BackgroundJobManager[IO](
          dbLayer = dbLayer,
          sBotInfo = RichardPHJBensonBot.sBotConfig.sBotInfo,
          ttl = RichardPHJBensonBot.sBotConfig.messageTimeToLive
        )
      )
      mediaFiles =
        (RichardPHJBensonBot.messageRepliesData ++
          RichardPHJBensonBot.commandRepliesData ++
          ABarberoBot.messageRepliesData ++
          ABarberoBot.commandRepliesData ++
          YouTuboAncheI0Bot.messageRepliesData ++
          YouTuboAncheI0Bot.commandRepliesData ++
          M0sconiBot.messageRepliesData ++
          M0sconiBot.commandRepliesData ++
          CalandroBot.messageRepliesData ++
          CalandroBot.commandRepliesData ++
          XahLeeBot.messageRepliesData ++
          XahLeeBot.commandRepliesData)
          .flatMap((r: ReplyBundle) => ReplyBundle.getMediaFiles(r))
    } yield mediaFiles.distinctBy(_.filename)

  def withFixture(test: OneArgTest): Outcome = {
    val fixtureParam = FixtureParam(DBFixture.fixtureSetup(null))

    try {
      withFixture(test.toNoArgTest(fixtureParam)) // "loan" the fixture to the test
    } finally DBFixture.teardownFixture(fixtureParam.fixture)
  }

  def checkFile(mf: MediaFile): IO[Unit] =
    // ignore to not run in CI, remove sometimes to check all the messages files
    test(s"${mf.filename} should return some data", SlowTest) { case FixtureParam(fixture) =>
      (for {
        repository   <- fixture.repositoryResource
        mediaSources <- repository.getResourceFile(mf)
        file         <- mediaSources.fold(
          e => Resource.eval(IO.raiseError(Throwable(s"getResourceFile throw an error $e"))),
          _.traverse(_.getMediaResourceFile.getOrElse(fail("expect a file")))
        )
      } yield assert(file.length > (5 * 1024))).use_
    }.pure[IO]

  allMessageMediaFiles.use(files => files.sortBy(_.filename).traverse(file => checkFile(file))).void.unsafeRunSync()
}
