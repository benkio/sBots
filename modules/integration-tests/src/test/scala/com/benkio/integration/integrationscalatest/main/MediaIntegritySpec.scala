package com.benkio.integration.integrationscalatest.main

import cats.effect.unsafe.implicits.global
import cats.effect.Async
import cats.effect.IO
import cats.effect.Resource
import cats.implicits.*
import cats.Parallel
import com.benkio.ABarberoBot.ABarberoBot
import com.benkio.ABarberoBot.ABarberoBotPolling
import com.benkio.CalandroBot.CalandroBot
import com.benkio.CalandroBot.CalandroBotPolling
import com.benkio.integration.BotSetupFixture
import com.benkio.integration.DBFixture
import com.benkio.integration.DBFixtureResources
import com.benkio.integration.SlowTest
import com.benkio.M0sconiBot.M0sconiBot
import com.benkio.M0sconiBot.M0sconiBotPolling
import com.benkio.richardphjbensonbot.RichardPHJBensonBot
import com.benkio.richardphjbensonbot.RichardPHJBensonBotPolling
import com.benkio.telegrambotinfrastructure.config.SBotConfig
import com.benkio.telegrambotinfrastructure.initialization.BotSetup
import com.benkio.telegrambotinfrastructure.model.media.getMediaResourceFile
import com.benkio.telegrambotinfrastructure.model.reply.MediaFile
import com.benkio.telegrambotinfrastructure.model.reply.ReplyBundle
import com.benkio.telegrambotinfrastructure.model.reply.ReplyBundleCommand
import com.benkio.telegrambotinfrastructure.model.reply.ReplyBundleMessage
import com.benkio.telegrambotinfrastructure.ISBot
import com.benkio.xahleebot.XahLeeBot
import com.benkio.xahleebot.XahLeeBotPolling
import com.benkio.youtuboanchei0bot.YouTuboAncheI0Bot
import com.benkio.youtuboanchei0bot.YouTuboAncheI0BotPolling
import log.effect.fs2.SyncLogWriter.consoleLogUpToLevel
import log.effect.LogLevels
import log.effect.LogWriter
import org.scalatest.*
import org.scalatest.funsuite.FixtureAnyFunSuite

class MediaIntegritySpec extends FixtureAnyFunSuite with ParallelTestExecution {

  case class FixtureParam(fixture: DBFixtureResources)

  given log: LogWriter[IO]               = consoleLogUpToLevel(LogLevels.Info)
  val initialFixture: DBFixtureResources = DBFixture.fixtureSetup(null)

  def mediaFilesFromBot(
      config: SBotConfig,
      mkBot: (BotSetup[IO], List[ReplyBundleMessage], List[ReplyBundleCommand]) => ISBot[IO]
  ): IO[List[MediaFile]] =
    BotSetupFixture
      .botSetupResource(initialFixture, config)
      .use { setup =>
        val messageRepliesData =
          if config.sBotInfo.botId == XahLeeBot.sBotConfig.sBotInfo.botId then IO.pure(List.empty[ReplyBundleMessage])
          else
            setup.jsonDataRepository.loadData[ReplyBundleMessage](config.repliesJsonFilename)
        val commandRepliesData =
          if config.sBotInfo.botId == CalandroBot.sBotConfig.sBotInfo.botId then setup.jsonDataRepository
            .loadData[ReplyBundleCommand](config.commandsJsonFilename)
          else IO.pure(List.empty[ReplyBundleCommand])
        (messageRepliesData, commandRepliesData).tupled.map { case (msgData, cmdData) =>
          val bot = mkBot(setup, msgData, cmdData)
          (bot.messageRepliesData ++ bot.allCommandRepliesData).flatMap(r => r.getMediaFiles)
        }
      }

  val allMessageMediaFiles: Resource[IO, List[MediaFile]] =
    for {
      _             <- initialFixture.resourceDBLayer
      _             <- initialFixture.repositoryResource
      abarberoFiles <- Resource.eval(
        mediaFilesFromBot(
          ABarberoBot.sBotConfig,
          (setup, msgData, _) =>
            new ABarberoBotPolling[IO](setup, msgData)(using Parallel[IO], Async[IO], setup.api, log)
        )
      )
      calandroFiles <- Resource.eval(
        mediaFilesFromBot(
          CalandroBot.sBotConfig,
          (setup, msgData, cmdData) =>
            new CalandroBotPolling[IO](setup, msgData, cmdData)(using Parallel[IO], Async[IO], setup.api, log)
        )
      )
      m0sconiFiles <- Resource.eval(
        mediaFilesFromBot(
          M0sconiBot.sBotConfig,
          (setup, msgData, _) =>
            new M0sconiBotPolling[IO](setup, msgData)(using Parallel[IO], Async[IO], setup.api, log)
        )
      )
      richardFiles <- Resource.eval(
        mediaFilesFromBot(
          RichardPHJBensonBot.sBotConfig,
          (setup, msgData, _) =>
            new RichardPHJBensonBotPolling[IO](setup, msgData)(using Parallel[IO], Async[IO], setup.api, log)
        )
      )
      youTuboFiles <- Resource.eval(
        mediaFilesFromBot(
          YouTuboAncheI0Bot.sBotConfig,
          (setup, msgData, _) =>
            new YouTuboAncheI0BotPolling[IO](setup, msgData)(using Parallel[IO], Async[IO], setup.api, log)
        )
      )
      xahLeeFiles <- Resource.eval(
        mediaFilesFromBot(
          XahLeeBot.sBotConfig,
          (setup, _, _) => new XahLeeBotPolling[IO](setup, List.empty)(using Parallel[IO], Async[IO], setup.api, log)
        )
      )
      allFiles = (abarberoFiles ++ calandroFiles ++ m0sconiFiles ++ richardFiles ++ youTuboFiles ++ xahLeeFiles)
        .distinctBy(_.filename)
    } yield allFiles

  def withFixture(test: OneArgTest): Outcome = {
    val fixtureParam = FixtureParam(DBFixture.fixtureSetup(null))
    try withFixture(test.toNoArgTest(fixtureParam))
    finally DBFixture.teardownFixture(fixtureParam.fixture)
  }

  def checkFile(mf: MediaFile): IO[Unit] =
    test(s"${mf.filename} should return some data", SlowTest) { case FixtureParam(fixture) =>
      (for {
        repository   <- fixture.repositoryResource
        mediaSources <- repository.getResourceFile(mf)
        files        <- mediaSources.fold(
          e => Resource.eval(IO.raiseError(Throwable(s"getResourceFile throw an error $e"))),
          list =>
            list.traverse(mr =>
              mr.getMediaResourceFile.getOrElse(Resource.eval(IO.raiseError(new Exception("expect a file"))))
            )
        )
      } yield assert(files.forall(_.length() > 5 * 1024))).use_
    }.pure[IO]

  allMessageMediaFiles.use(files => files.sortBy(_.filename).traverse(file => checkFile(file))).void.unsafeRunSync()
}
