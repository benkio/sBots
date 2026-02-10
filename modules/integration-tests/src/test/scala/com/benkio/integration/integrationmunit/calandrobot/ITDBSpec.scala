package com.benkio.integration.integrationmunit.calandrobot

import cats.effect.Async
import cats.effect.IO
import cats.effect.Resource
import cats.implicits.*
import cats.Parallel
import com.benkio.calandrobot.CalandroBot
import com.benkio.calandrobot.CalandroBotPolling
import com.benkio.integration.BotSetupFixture
import com.benkio.telegrambotinfrastructure.config.SBotConfig
import com.benkio.telegrambotinfrastructure.model.reply.MediaFile
import com.benkio.telegrambotinfrastructure.model.reply.ReplyBundle
import com.benkio.telegrambotinfrastructure.repository.db.DBMedia
import doobie.implicits.*
import munit.CatsEffectSuite

class ITDBSpec extends CatsEffectSuite with BotSetupFixture {

  override def botSetupFixtureConfig: SBotConfig = CalandroBot.sBotConfig

  // File Reference Check

  botSetupFixture.test(
    "messageRepliesData should never raise an exception when try to open the file in resounces"
  ) { fixture =>
    val testAssert = for {
      botSetup           <- fixture.botSetupResource
      messageRepliesData <- Resource.eval(
        botSetup.jsonRepliesRepository.loadReplies(CalandroBot.sBotConfig.repliesJsonFilename)
      )
      calandroBot = new CalandroBotPolling[IO](botSetup, messageRepliesData)(using
        Parallel[IO],
        Async[IO],
        botSetup.api,
        log
      )
      files      = calandroBot.messageRepliesData.flatMap(r => r.getMediaFiles)
      transactor = fixture.dbResources.transactor
      checks <- Resource.eval(
        files
          .traverse((file: MediaFile) =>
            DBMedia
              .getMediaQueryByName(file.filename)
              .unique
              .transact(transactor)
              .onError { case _ => IO.println("[ERROR] file missing from the DB: " + file) }
              .attempt
              .map(_.isRight)
          )
      )
    } yield checks.foldLeft(true)(_ && _)

    testAssert.use(assert(_, true).pure[IO])
  }

  botSetupFixture.test(
    "commandRepliesData should never raise an exception when try to open the file in resounces"
  ) { fixture =>
    val testAssert = for {
      botSetup           <- fixture.botSetupResource
      messageRepliesData <- Resource.eval(
        botSetup.jsonRepliesRepository.loadReplies(CalandroBot.sBotConfig.repliesJsonFilename)
      )
      calandroBot = new CalandroBotPolling[IO](botSetup, messageRepliesData)(using
        Parallel[IO],
        Async[IO],
        botSetup.api,
        log
      )
      transactor = fixture.dbResources.transactor
      dbLayer <- fixture.dbResources.resourceDBLayer
      files = calandroBot.commandRepliesData.flatMap(r => r.getMediaFiles)
      checks <- Resource.eval(
        files
          .traverse((file: MediaFile) =>
            DBMedia
              .getMediaQueryByName(file.filename)
              .unique
              .transact(transactor)
              .onError { case _ => IO.println("[ERROR] file missing from the DB: " + file) }
              .attempt
              .map(_.isRight)
          )
      )
    } yield checks.foldLeft(true)(_ && _)

    testAssert.use(assert(_, true).pure[IO])
  }
}
