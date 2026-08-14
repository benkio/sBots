package com.benkio.integration.integrationscalatest.main

import cats.effect.unsafe.implicits.global
import cats.effect.IO
import cats.effect.Resource
import cats.implicits.*
import com.benkio.chatcore.config.SBotConfig
import com.benkio.chatcore.model.media.getMediaResourceFile
import com.benkio.chatcore.model.reply.MediaFile
import com.benkio.chatcore.model.reply.ReplyBundle
import com.benkio.chatcore.model.reply.ReplyBundleCommand
import com.benkio.chatcore.model.reply.ReplyBundleMessage
import com.benkio.chattelegramadapter.initialization.BotSetup
import com.benkio.chattelegramadapter.ISBot
import com.benkio.chattelegramadapter.SBot
import com.benkio.chattelegramadapter.SBotPolling
import com.benkio.integration.BotSetupFixture
import com.benkio.integration.DBFixture
import com.benkio.integration.DBFixtureResources
import com.benkio.integration.SlowTest
import com.benkio.integration.WarnLogger.given
import com.benkio.Alessandro0rlandoBot.Alessandro0rlandoBot
import com.benkio.RichardPHJBensonBot.RichardPHJBensonBot
import com.benkio.XahLeeBot.XahLeeBot
import org.scalatest.*
import org.scalatest.funsuite.FixtureAnyFunSuite

import java.nio.file.Files

class MediaIntegritySeqSpec extends FixtureAnyFunSuite {

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
      alessandro0rlandoFiles <- Resource.eval(
        mediaFilesFromBot(
          SBot.buildSBotConfig(Alessandro0rlandoBot.sBotInfo),
          (setup, msgData, cmdData) => {
            given telegramium.bots.high.Api[IO] = setup.api
            new SBotPolling[IO](setup, msgData, cmdData)
          }
        )
      )
      allFiles =
        (richardFiles ++ alessandro0rlandoFiles)
          .distinctBy(_.filename)
    } yield allFiles

  def withFixture(test: OneArgTest): Outcome = {
    val fixtureParam = FixtureParam(DBFixture.fixtureSetup(null))
    try withFixture(test.toNoArgTest(fixtureParam))
    finally DBFixture.teardownFixture(fixtureParam.fixture)
  }

  def checkFile(mf: MediaFile): IO[Unit] =
    test(s"✅ ${mf.filename}", SlowTest) { case FixtureParam(fixture) =>
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
