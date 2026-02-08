package com.benkio.integration.integrationmunit.richardphjbensonbot

import cats.Parallel
import cats.effect.Async
import cats.effect.IO
import cats.effect.Resource
import cats.implicits.*
import com.benkio.integration.BotSetupFixture
import com.benkio.richardphjbensonbot.RichardPHJBensonBot
import com.benkio.richardphjbensonbot.data.Audio.messageRepliesAudioData
import com.benkio.richardphjbensonbot.data.Gif.messageRepliesGifData
import com.benkio.richardphjbensonbot.data.Mix.messageRepliesMixData
import com.benkio.richardphjbensonbot.data.Special.messageRepliesSpecialData
import com.benkio.richardphjbensonbot.data.Video.messageRepliesVideoData
import com.benkio.telegrambotinfrastructure.config.SBotConfig
import com.benkio.telegrambotinfrastructure.model.reply.MediaFile
import com.benkio.telegrambotinfrastructure.model.reply.ReplyBundle
import com.benkio.telegrambotinfrastructure.repository.db.DBMedia
import doobie.implicits.*
import munit.CatsEffectSuite
import com.benkio.richardphjbensonbot.RichardPHJBensonBotPolling

class ITDBSpec extends CatsEffectSuite with BotSetupFixture {

  override def botSetupFixtureConfig: SBotConfig = RichardPHJBensonBot.sBotConfig

  // File Reference Check

  botSetupFixture.test(
    "messageRepliesData should never raise an exception when try to open the file in resounces"
  ) { fixture =>
    val testAssert = for {
      botSetup <- fixture.botSetupResource
      richardBot = new RichardPHJBensonBotPolling[IO](botSetup)(using Parallel[IO], Async[IO], botSetup.api, log)
      files      <- Resource.eval(richardBot.messageRepliesData.map(_.flatMap(r => r.getMediaFiles)))
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
