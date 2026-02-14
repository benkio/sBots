package com.benkio.integration.integrationmunit.abarberobot

import cats.effect.Async
import cats.effect.IO
import cats.effect.Resource
import cats.implicits.*
import cats.Parallel
import com.benkio.abarberobot.ABarberoBot
import com.benkio.abarberobot.ABarberoBotPolling
import com.benkio.integration.BotSetupFixture
import com.benkio.telegrambotinfrastructure.config.SBotConfig
import com.benkio.telegrambotinfrastructure.model.reply.MediaFile
import com.benkio.telegrambotinfrastructure.model.reply.ReplyBundle
import com.benkio.telegrambotinfrastructure.model.reply.ReplyBundleMessage
import com.benkio.telegrambotinfrastructure.repository.db.DBMedia
import doobie.implicits.*
import munit.CatsEffectSuite

class ITDBSpec extends CatsEffectSuite with BotSetupFixture {

  override def botSetupFixtureConfig: SBotConfig = ABarberoBot.sBotConfig

  botSetupFixture.test(
    "messageRepliesData should never raise an exception when try to open the file in resounces"
  ) { fixture =>
    val testAssert = for {
      botSetup           <- fixture.botSetupResource
      messageRepliesData <- Resource.eval(
        botSetup.jsonDataRepository.loadData[ReplyBundleMessage](ABarberoBot.sBotConfig.repliesJsonFilename)
      )
      aBarberoBot = new ABarberoBotPolling[IO](botSetup, messageRepliesData)(using
        Parallel[IO],
        Async[IO],
        botSetup.api,
        log
      )
      files      = aBarberoBot.messageRepliesData.flatMap(r => r.getMediaFiles)
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
}
