package com.benkio.integration.integrationmunit.youtuboanchei0bot

import cats.effect.Async
import cats.effect.IO
import cats.effect.Resource
import cats.implicits.*
import cats.Parallel
import com.benkio.integration.BotSetupFixture
import com.benkio.telegrambotinfrastructure.config.SBotConfig
import com.benkio.telegrambotinfrastructure.model.reply.MediaFile
import com.benkio.telegrambotinfrastructure.model.reply.ReplyBundle
import com.benkio.telegrambotinfrastructure.model.reply.ReplyBundleMessage
import com.benkio.telegrambotinfrastructure.repository.db.DBMedia
import com.benkio.YouTuboAncheI0Bot.YouTuboAncheI0Bot
import com.benkio.YouTuboAncheI0Bot.YouTuboAncheI0BotPolling
import doobie.implicits.*
import munit.CatsEffectSuite

class ITDBSpec extends CatsEffectSuite with BotSetupFixture {

  override def botSetupFixtureConfig: SBotConfig = YouTuboAncheI0Bot.sBotConfig

  // File Reference Check

  botSetupFixture.test(
    "messageRepliesData should never raise an exception when try to open the file in bot"
  ) { fixture =>
    val testAssert = for {
      botSetup           <- fixture.botSetupResource
      messageRepliesData <- Resource.eval(
        botSetup.jsonDataRepository.loadData[ReplyBundleMessage](YouTuboAncheI0Bot.sBotConfig.repliesJsonFilename)
      )
      youTuboBot = new YouTuboAncheI0BotPolling[IO](botSetup, messageRepliesData)(using
        Parallel[IO],
        Async[IO],
        botSetup.api,
        log
      )
      files      = youTuboBot.messageRepliesData.flatMap(r => r.getMediaFiles)
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
