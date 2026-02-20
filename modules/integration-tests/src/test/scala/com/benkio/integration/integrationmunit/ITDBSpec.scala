package com.benkio.integration.integrationmunit

import cats.effect.Async
import cats.effect.IO
import cats.effect.Resource
import cats.implicits.*
import cats.Parallel
import com.benkio.integration.BotSetupFixture
import com.benkio.integration.DBFixture
import com.benkio.main.*
import com.benkio.telegrambotinfrastructure.model.media.MediaFileSource
import com.benkio.telegrambotinfrastructure.model.reply.MediaFile
import com.benkio.telegrambotinfrastructure.model.reply.ReplyBundleCommand
import com.benkio.telegrambotinfrastructure.model.reply.ReplyBundleMessage
import com.benkio.telegrambotinfrastructure.repository.db.DBMedia
import com.benkio.telegrambotinfrastructure.SBotPolling
import doobie.implicits.*
import io.circe.parser.decode
import munit.CatsEffectSuite

import java.io.File
import scala.io.Source

/** Unified IT DB spec: runs the same DB/media checks for every bot in the registry. */
class ITDBSpec extends CatsEffectSuite with DBFixture {

  private val botEntries = BotRegistry.toList(BotRegistry.value)

  // messageRepliesData: all media files referenced in message replies must exist in DB
  botEntries.foreach { entry =>
    databaseFixture.test(
      s"${entry.sBotInfo.botName.value}: messageRepliesData should never raise an exception when try to open the file in resources"
    ) { dbRes =>
      runMessageRepliesFileCheck(dbRes, entry).use(b => assert(b, true).pure[IO])
    }
  }

  // commandRepliesData: all media files referenced in command replies must exist in DB
  botEntries.foreach { entry =>
    databaseFixture.test(
      s"${entry.sBotInfo.botName.value}: commandRepliesData should never raise an exception when try to open the file in resources"
    ) { dbRes =>
      runCommandRepliesFileCheck(dbRes, entry).use(b => assert(b, true).pure[IO])
    }
  }

  // commandRepliesData files should be contained in the jsons
  botEntries
    .foreach { entry =>
      databaseFixture.test(
        s"${entry.sBotInfo.botName.value}: commandRepliesData files should be contained in the jsons"
      ) { dbRes =>
        runCommandRepliesJsonContainmentCheck(dbRes, entry).use(b => assertIO(IO.pure(b), true))
      }
    }

  // messageRepliesData files should be contained in the jsons
  botEntries
    .foreach { entry =>
      databaseFixture.test(
        s"${entry.sBotInfo.botName.value}: messageRepliesData files should be contained in the jsons"
      ) { dbRes =>
        runMessageRepliesJsonContainmentCheck(dbRes, entry).use(b => assertIO(IO.pure(b), true))
      }
    }

  private def runMessageRepliesFileCheck(
      dbRes: com.benkio.integration.DBFixtureResources,
      entry: com.benkio.main.BotRegistryEntry[IO]
  ): Resource[IO, Boolean] = {
    val sBotConfig = entry.sBotConfig
    for {
      botSetup           <- BotSetupFixture.botSetupResource(dbRes, sBotConfig)(using log)
      messageRepliesData <- Resource.eval(
        botSetup.jsonDataRepository.loadData[ReplyBundleMessage](botSetup.sBotConfig.repliesJsonFilename)
      )
      commandRepliesData <- Resource.eval(
        botSetup.jsonDataRepository.loadData[ReplyBundleCommand](botSetup.sBotConfig.commandsJsonFilename)
      )
      sBot = new SBotPolling[IO](
        botSetup,
        messageRepliesData,
        commandRepliesData,
        entry.commandEffectfulCallback
      )(using Parallel[IO], Async[IO], botSetup.api, log)
      files = sBot.messageRepliesData.flatMap(r => r.getMediaFiles)
      checks <- Resource.eval(
        files
          .traverse((file: MediaFile) =>
            DBMedia
              .getMediaQueryByName(file.filename)
              .unique
              .transact(dbRes.transactor)
              .onError { case _ => IO.println("[ERROR] file missing from the DB: " + file) }
              .attempt
              .map(_.isRight)
          )
      )
    } yield checks.foldLeft(true)(_ && _)
  }

  private def runCommandRepliesFileCheck(
      dbRes: com.benkio.integration.DBFixtureResources,
      entry: com.benkio.main.BotRegistryEntry[IO]
  ): Resource[IO, Boolean] = {
    val sBotConfig = entry.sBotConfig
    for {
      botSetup           <- BotSetupFixture.botSetupResource(dbRes, sBotConfig)(using log)
      messageRepliesData <- Resource.eval(
        botSetup.jsonDataRepository.loadData[ReplyBundleMessage](botSetup.sBotConfig.repliesJsonFilename)
      )
      commandRepliesData <- Resource.eval(
        botSetup.jsonDataRepository.loadData[ReplyBundleCommand](botSetup.sBotConfig.commandsJsonFilename)
      )
      sBot = new SBotPolling[IO](
        botSetup,
        messageRepliesData,
        commandRepliesData,
        entry.commandEffectfulCallback
      )(using Parallel[IO], Async[IO], botSetup.api, log)
      files = sBot.commandRepliesData.flatMap(r => r.getMediaFiles)
      checks <- Resource.eval(
        files
          .traverse((file: MediaFile) =>
            DBMedia
              .getMediaQueryByName(file.filename)
              .unique
              .transact(dbRes.transactor)
              .onError { case _ => IO.println("[ERROR] file missing from the DB: " + file) }
              .attempt
              .map(_.isRight)
          )
      )
    } yield checks.foldLeft(true)(_ && _)
  }

  private def runCommandRepliesJsonContainmentCheck(
      dbRes: com.benkio.integration.DBFixtureResources,
      entry: com.benkio.main.BotRegistryEntry[IO]
  ): Resource[IO, Boolean] = {
    val sBotConfig = entry.sBotConfig
    val listPath   = new File(
      s"./../bots/${sBotConfig.sBotInfo.botName.value}"
    ).getCanonicalPath + s"/${sBotConfig.sBotInfo.botId.value}_list.json"
    val jsonContent = Source.fromFile(listPath).getLines().mkString("\n")
    val json        = decode[List[MediaFileSource]](jsonContent).map(_.map(_.filename))
    for {
      botSetup           <- BotSetupFixture.botSetupResource(dbRes, sBotConfig)(using log)
      messageRepliesData <- Resource.eval(
        botSetup.jsonDataRepository.loadData[ReplyBundleMessage](botSetup.sBotConfig.repliesJsonFilename)
      )
      commandRepliesData <- Resource.eval(
        botSetup.jsonDataRepository.loadData[ReplyBundleCommand](botSetup.sBotConfig.commandsJsonFilename)
      )
      sBot = new SBotPolling[IO](
        botSetup,
        messageRepliesData,
        commandRepliesData,
        entry.commandEffectfulCallback
      )(using Parallel[IO], Async[IO], botSetup.api, log)
      mediaFiles = sBot.commandRepliesData.flatMap(r => r.getMediaFiles)
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
  }

  private def runMessageRepliesJsonContainmentCheck(
      dbRes: com.benkio.integration.DBFixtureResources,
      entry: com.benkio.main.BotRegistryEntry[IO]
  ): Resource[IO, Boolean] = {
    val sBotConfig = entry.sBotConfig
    val listPath   = new File(
      s"./../bots/${sBotConfig.sBotInfo.botName.value}"
    ).getCanonicalPath + s"/${sBotConfig.sBotInfo.botId.value}_list.json"
    val jsonContent = Source.fromFile(listPath).getLines().mkString("\n")
    val json        = decode[List[MediaFileSource]](jsonContent).map(_.map(_.filename))
    for {
      botSetup           <- BotSetupFixture.botSetupResource(dbRes, sBotConfig)(using log)
      messageRepliesData <- Resource.eval(
        botSetup.jsonDataRepository.loadData[ReplyBundleMessage](botSetup.sBotConfig.repliesJsonFilename)
      )
      commandRepliesData <- Resource.eval(
        botSetup.jsonDataRepository.loadData[ReplyBundleCommand](botSetup.sBotConfig.commandsJsonFilename)
      )
      sBot = new SBotPolling[IO](
        botSetup,
        messageRepliesData,
        commandRepliesData,
        entry.commandEffectfulCallback
      )(using Parallel[IO], Async[IO], botSetup.api, log)
      mediaFiles = sBot.messageRepliesData.flatMap(r => r.getMediaFiles)
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
  }
}
