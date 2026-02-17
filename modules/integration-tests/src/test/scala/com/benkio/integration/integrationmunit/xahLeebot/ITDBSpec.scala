package com.benkio.integration.integrationmunit.xahleebot

import cats.effect.Async
import cats.effect.IO
import cats.effect.Resource
import cats.implicits.*
import cats.Parallel
import com.benkio.integration.BotSetupFixture
import com.benkio.telegrambotinfrastructure.config.SBotConfig
import com.benkio.telegrambotinfrastructure.model.media.MediaFileSource
import com.benkio.telegrambotinfrastructure.model.reply.MediaFile
import com.benkio.telegrambotinfrastructure.model.reply.ReplyBundle
import com.benkio.telegrambotinfrastructure.model.reply.ReplyBundleCommand
import com.benkio.telegrambotinfrastructure.model.reply.ReplyBundleMessage
import com.benkio.telegrambotinfrastructure.repository.db.DBMedia
import com.benkio.telegrambotinfrastructure.SBot
import com.benkio.telegrambotinfrastructure.SBotPolling
import com.benkio.XahLeeBot.XahLeeBot
import doobie.implicits.*
import io.circe.parser.decode
import munit.CatsEffectSuite

import java.io.File
import scala.io.Source

class ITDBSpec extends CatsEffectSuite with BotSetupFixture {

  override def botSetupFixtureConfig: SBotConfig = SBot.buildSBotConfig(XahLeeBot.sBotInfo)

  // File Reference Check

  botSetupFixture.test(
    "commandRepliesData should never raise an exception when try to open the file in resounces"
  ) { fixture =>
    val resourceAssert = for {
      botSetup           <- fixture.botSetupResource
      messageRepliesData <- Resource.eval(
        botSetup.jsonDataRepository.loadData[ReplyBundleMessage](botSetup.sBotConfig.repliesJsonFilename)
      )
      commandRepliesData <- Resource.eval(
        botSetup.jsonDataRepository.loadData[ReplyBundleCommand](botSetup.sBotConfig.commandsJsonFilename)
      )
      xahLeeBot = new SBotPolling[IO](botSetup, messageRepliesData, commandRepliesData)(using
        Parallel[IO],
        Async[IO],
        botSetup.api,
        log
      )
      transactor = fixture.dbResources.transactor
      files      = xahLeeBot.commandRepliesData.flatMap(r => r.getMediaFiles)
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

    resourceAssert.use(b => assertIO(IO.pure(b), true))
  }

  // File json file check

  botSetupFixture.test(
    "commandRepliesData random files should be contained in the jsons"
  ) { fixture =>
    val listPath    = new File("./../bots/XahLeeBot").getCanonicalPath + "/xah_list.json"
    val jsonContent = Source.fromFile(listPath).getLines().mkString("\n")
    val json        = decode[List[MediaFileSource]](jsonContent).map(_.map(_.filename))

    val resourceAssert = for {
      botSetup           <- fixture.botSetupResource
      messageRepliesData <- Resource.eval(
        botSetup.jsonDataRepository.loadData[ReplyBundleMessage](botSetup.sBotConfig.repliesJsonFilename)
      )
      commandRepliesData <- Resource.eval(
        botSetup.jsonDataRepository.loadData[ReplyBundleCommand](botSetup.sBotConfig.commandsJsonFilename)
      )
      xahLeeBot = new SBotPolling[IO](botSetup, messageRepliesData, commandRepliesData)(using
        Parallel[IO],
        Async[IO],
        botSetup.api,
        log
      )
      mediaFiles = xahLeeBot.commandRepliesData.flatMap(r => r.getMediaFiles)
      checks <- Resource.pure(
        mediaFiles
          .map((mediaFile: MediaFile) =>
            json.fold(
              e => fail("test failed", e),
              jsonMediaFileSources => {
                val result = jsonMediaFileSources.exists((mediaFilenameSource: String) =>
                  mediaFilenameSource == mediaFile.filename
                )
                if !result then {
                  println(s"${mediaFile.filename} is not contained in the json file")
                }
                result
              }
            )
          )
      )
    } yield checks.foldLeft(true)(_ && _)

    resourceAssert.use(b => assertIO(IO.pure(b), true))
  }
}
