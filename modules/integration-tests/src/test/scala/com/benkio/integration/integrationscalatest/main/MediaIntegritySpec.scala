package com.benkio.integration.integrationscalatest.main

import cats.effect.unsafe.implicits.global
import cats.effect.IO
import cats.effect.Resource
import cats.implicits.*
import com.benkio.chatcore.config.SBotConfig
import com.benkio.chatcore.initialization.BotSetup
import com.benkio.chatcore.model.media.getMediaResourceFile
import com.benkio.chatcore.model.reply.MediaFile
import com.benkio.chatcore.model.reply.ReplyBundle
import com.benkio.chatcore.model.reply.ReplyBundleCommand
import com.benkio.chatcore.model.reply.ReplyBundleMessage
import com.benkio.chatcore.ISBot
import com.benkio.chatcore.SBot
import com.benkio.chatcore.SBotPolling
import com.benkio.integration.BotSetupFixture
import com.benkio.integration.DBFixture
import com.benkio.integration.DBFixtureResources
import com.benkio.integration.SlowTest
import com.benkio.integrationtest.Logger.given
import com.benkio.ABarberoBot.ABarberoBot
import com.benkio.CalandroBot.CalandroBot
import com.benkio.M0sconiBot.M0sconiBot
import com.benkio.RichardPHJBensonBot.RichardPHJBensonBot
import com.benkio.XahLeeBot.XahLeeBot
import com.benkio.YouTuboAncheI0Bot.YouTuboAncheI0Bot
import org.scalatest.*
import org.scalatest.funsuite.FixtureAnyFunSuite

import java.nio.file.Files

class MediaIntegritySpec extends FixtureAnyFunSuite with ParallelTestExecution {

  case class FixtureParam(fixture: DBFixtureResources)

  val initialFixture: DBFixtureResources = DBFixture.fixtureSetup(null)

  def mediaFilesFromBot(
      config: SBotConfig,
      mkBot: (BotSetup[IO], List[ReplyBundleMessage], List[ReplyBundleCommand]) => ISBot[IO]
  ): IO[List[MediaFile]] =
    BotSetupFixture
      .botSetupResource(initialFixture, config)
      .use { setup =>
        val messageRepliesData =
          if config.sBotInfo.botId == XahLeeBot.sBotInfo.botId then IO.pure(List.empty[ReplyBundleMessage])
          else
            setup.jsonDataRepository.loadData[ReplyBundleMessage](config.repliesJsonFilename)
        val commandRepliesData = setup.jsonDataRepository.loadData[ReplyBundleCommand](config.commandsJsonFilename)
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
          SBot.buildSBotConfig(ABarberoBot.sBotInfo),
          (setup, msgData, cmdData) => {
            given telegramium.bots.high.Api[IO] = setup.api
            new SBotPolling[IO](setup, msgData, cmdData)
          }
        )
      )
      calandroFiles <- Resource.eval(
        mediaFilesFromBot(
          SBot.buildSBotConfig(CalandroBot.sBotInfo),
          (setup, msgData, cmdData) => {
            given telegramium.bots.high.Api[IO] = setup.api
            new SBotPolling[IO](setup, msgData, cmdData)
          }
        )
      )
      m0sconiFiles <- Resource.eval(
        mediaFilesFromBot(
          SBot.buildSBotConfig(M0sconiBot.sBotInfo),
          (setup, msgData, cmdData) => {
            given telegramium.bots.high.Api[IO] = setup.api
            new SBotPolling[IO](setup, msgData, cmdData)
          }
        )
      )
      richardFiles <- Resource.eval(
        mediaFilesFromBot(
          SBot.buildSBotConfig(RichardPHJBensonBot.sBotInfo),
          (setup, msgData, cmdData) => {
            given telegramium.bots.high.Api[IO] = setup.api
            new SBotPolling[IO](
              setup,
              msgData,
              cmdData,
              RichardPHJBensonBot.commandEffectfulCallback[IO]
            )
          }
        )
      )
      youTuboFiles <- Resource.eval(
        mediaFilesFromBot(
          SBot.buildSBotConfig(YouTuboAncheI0Bot.sBotInfo),
          (setup, msgData, cmdData) => {
            given telegramium.bots.high.Api[IO] = setup.api
            new SBotPolling[IO](setup, msgData, cmdData)
          }
        )
      )
      xahLeeFiles <- Resource.eval(
        mediaFilesFromBot(
          SBot.buildSBotConfig(XahLeeBot.sBotInfo),
          (setup, msgData, cmdData) => {
            given telegramium.bots.high.Api[IO] = setup.api
            new SBotPolling[IO](setup, msgData, cmdData)
          }
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
      } yield assert(files.forall(Files.readAllBytes(_).length > 5 * 1024))).use_
    }.pure[IO]

  allMessageMediaFiles.use(files => files.sortBy(_.filename).traverse(file => checkFile(file))).void.unsafeRunSync()
}
