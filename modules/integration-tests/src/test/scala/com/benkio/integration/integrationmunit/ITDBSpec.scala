package com.benkio.integration.integrationmunit

import cats.effect.IO
import cats.effect.Resource
import cats.implicits.*
import com.benkio.chatcore.config.SBotConfig
import com.benkio.chatcore.model.media.MediaFileSource
import com.benkio.chatcore.model.reply.MediaFile
import com.benkio.chatcore.model.reply.ReplyBundleCommand
import com.benkio.chatcore.model.reply.ReplyBundleMessage
import com.benkio.chatcore.repository.db.DBMedia
import com.benkio.chattelegramadapter.SBotPolling
import com.benkio.integration.BotSetupFixture
import com.benkio.integration.DBFixture
import com.benkio.integrationtest.Logger.given
import com.benkio.main.*
import doobie.implicits.*
import io.circe.parser.decode
import munit.CatsEffectSuite

import java.nio.file.Files
import java.nio.file.Paths

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

  // list file filenames should match DB content and list items should either be in message replies or explicitly typed with kinds
  botEntries.foreach { entry =>
    databaseFixture.test(
      s"${entry.sBotInfo.botName.value}: list filenames should match DB entries and DB media should be in message replies or by kind"
    ) { dbRes =>
      runListDbAndKindCheck(dbRes, entry).use(b => assert(b, true).pure[IO])
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
      given telegramium.bots.high.Api[IO] = botSetup.api
      sBot                                = new SBotPolling[IO](
        botSetup,
        messageRepliesData,
        commandRepliesData,
        entry.commandEffectfulCallback
      )
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
      given telegramium.bots.high.Api[IO] = botSetup.api
      sBot                                = new SBotPolling[IO](
        botSetup,
        messageRepliesData,
        commandRepliesData,
        entry.commandEffectfulCallback
      )
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

  private def getBotListContent(sBotConfig: SBotConfig): Either[Throwable, List[String]] = {
    val listPath = Paths
      .get(s"../bots/${sBotConfig.sBotInfo.botName.value}/${sBotConfig.sBotInfo.botId.value}_list.json")
      .toAbsolutePath()
      .normalize()
    scala.util.Try(Files.readString(listPath)).toEither.flatMap { jsonContent =>
      decode[List[MediaFileSource]](jsonContent).map(_.map(_.filename))
    }
  }

  private def getBotListEntries(sBotConfig: SBotConfig): Either[Throwable, List[MediaFileSource]] = {
    val listPath = Paths
      .get(s"../bots/${sBotConfig.sBotInfo.botName.value}/${sBotConfig.sBotInfo.botId.value}_list.json")
      .toAbsolutePath()
      .normalize()
    scala.util.Try(Files.readString(listPath)).toEither.flatMap { jsonContent =>
      decode[List[MediaFileSource]](jsonContent)
    }
  }

  private def runCommandRepliesJsonContainmentCheck(
      dbRes: com.benkio.integration.DBFixtureResources,
      entry: com.benkio.main.BotRegistryEntry[IO]
  ): Resource[IO, Boolean] = {
    val sBotConfig = entry.sBotConfig
    val json       = getBotListContent(sBotConfig)
    for {
      botSetup           <- BotSetupFixture.botSetupResource(dbRes, sBotConfig)(using log)
      messageRepliesData <- Resource.eval(
        botSetup.jsonDataRepository.loadData[ReplyBundleMessage](botSetup.sBotConfig.repliesJsonFilename)
      )
      commandRepliesData <- Resource.eval(
        botSetup.jsonDataRepository.loadData[ReplyBundleCommand](botSetup.sBotConfig.commandsJsonFilename)
      )
      given telegramium.bots.high.Api[IO] = botSetup.api
      sBot                                = new SBotPolling[IO](
        botSetup,
        messageRepliesData,
        commandRepliesData,
        entry.commandEffectfulCallback
      )
      mediaFiles = sBot.commandRepliesData.flatMap(r => r.getMediaFiles)
      checks <- Resource.pure(
        mediaFiles
          .map((mediaFile: MediaFile) =>
            json.fold(
              e => { fail("test failed", e); false },
              jsonMediaFileSources =>
                jsonMediaFileSources.exists(mediaFilenameSource => mediaFilenameSource == mediaFile.filename)
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
    val json       = getBotListContent(sBotConfig)
    for {
      botSetup           <- BotSetupFixture.botSetupResource(dbRes, sBotConfig)(using log)
      messageRepliesData <- Resource.eval(
        botSetup.jsonDataRepository.loadData[ReplyBundleMessage](botSetup.sBotConfig.repliesJsonFilename)
      )
      commandRepliesData <- Resource.eval(
        botSetup.jsonDataRepository.loadData[ReplyBundleCommand](botSetup.sBotConfig.commandsJsonFilename)
      )
      given telegramium.bots.high.Api[IO] = botSetup.api
      sBot                                = new SBotPolling[IO](
        botSetup,
        messageRepliesData,
        commandRepliesData,
        entry.commandEffectfulCallback
      )
      mediaFiles = sBot.messageRepliesData.flatMap(r => r.getMediaFiles)
      checks <- Resource.pure(
        mediaFiles
          .map((mediaFile: MediaFile) =>
            json.fold(
              e => { fail("test failed", e); false },
              jsonMediaFileSources =>
                jsonMediaFileSources.exists(mediaFilenameSource => mediaFilenameSource == mediaFile.filename)
            )
          )
      )
    } yield checks.foldLeft(true)(_ && _)
  }

  private def runListDbAndKindCheck(
      dbRes: com.benkio.integration.DBFixtureResources,
      entry: com.benkio.main.BotRegistryEntry[IO]
  ): Resource[IO, Boolean] = {
    val sBotConfig  = entry.sBotConfig
    val listEntries = getBotListEntries(sBotConfig)
    for {
      botSetup           <- BotSetupFixture.botSetupResource(dbRes, sBotConfig)(using log)
      messageRepliesData <- Resource.eval(
        botSetup.jsonDataRepository.loadData[ReplyBundleMessage](botSetup.sBotConfig.repliesJsonFilename)
      )
      commandRepliesData <- Resource.eval(
        botSetup.jsonDataRepository.loadData[ReplyBundleCommand](botSetup.sBotConfig.commandsJsonFilename)
      )
      given telegramium.bots.high.Api[IO] = botSetup.api
      sBot                                = new SBotPolling[IO](
        botSetup,
        messageRepliesData,
        commandRepliesData,
        entry.commandEffectfulCallback
      )
      replyMediaFiles     = sBot.messageRepliesData.flatMap(_.getMediaFiles).map(_.filename).toSet
      commandMediaFiles   = sBot.commandRepliesData.flatMap(_.getMediaFiles).map(_.filename).toSet
      allReplyMediaFiles  = replyMediaFiles ++ commandMediaFiles
      mediaByKindCommands = sBot.commandRepliesData.flatMap { commandReply =>
        commandReply.reply match {
          case com.benkio.chatcore.model.reply.EffectfulReply(
                com.benkio.chatcore.model.reply.EffectfulKey.MediaByKind(key, _),
                _
              ) =>
            List(key)
          case _ => List.empty
        }
      }.toSet
      listEntriesValue <- Resource.eval(IO.fromEither(listEntries))
      filesSet      = listEntriesValue.map(_.filename).toSet
      mediaWithKind = listEntriesValue.filter(file => !allReplyMediaFiles.contains(file.filename))
      dbMedia        <- dbRes.resourceDBLayer.map(_.dbMedia)
      dbMediaEntries <- Resource.eval(
        dbMedia
          .getAllMedia(botId = Some(sBotConfig.sBotInfo.botId))
          .map(
            _.map(mediaData => (mediaData.media_name, decode[List[String]](mediaData.kinds).getOrElse(List.empty)))
          )
      )
      dbMediaNames     = dbMediaEntries.map(_._1)
      listMinusDb      = filesSet.filterNot(dbMediaNames.contains)
      dbMinusList      = dbMediaNames.filterNot(filesSet.contains)
      _ = println(s"[ITDBSpec] mediaByKindCommands: ${mediaByKindCommands}")
      uncoveredDbMedia = dbMediaEntries
        .filterNot { case (mediaName, mediaKinds) =>
          allReplyMediaFiles.contains(mediaName) || mediaKinds.exists(mediaByKindCommands.contains)
        }
      .map{ case (mediaName, mediaKinds) => {
        println(s"[ITDBSpec] mediaName: ${mediaName} - mediaKinds: $mediaKinds")
        mediaName
      }
      }
      kindMissing = mediaWithKind.filter(_.kinds.isEmpty).map(_.filename)
    } yield {
      assert(
        kindMissing.isEmpty,
        s"[ITDBSpec] Found files in list not referenced by replies and with no kinds for ${sBotConfig.sBotInfo.botName.value}: ${kindMissing.toList.sorted}"
      )
      assert(
        listMinusDb.isEmpty && dbMinusList.isEmpty,
        s"[ITDBSpec] DB/list mismatch for ${sBotConfig.sBotInfo.botName.value}." +
          s"\nIn list and not in DB: ${listMinusDb.toList.sorted}" +
          s"\nIn DB and not in list: ${dbMinusList.toList.sorted}"
      )
      assert(
        uncoveredDbMedia.isEmpty,
        s"[ITDBSpec] Found DB media entries not covered by message replies or media-by-kind commands for ${sBotConfig.sBotInfo.botName.value}: ${uncoveredDbMedia.toList.sorted}" +
          s"\nAvailable media-by-kind command names: ${mediaByKindCommands.toList.sorted}" +
          "\nMedia-by-kind commands must exist in commands JSON using `EffectfulReply -> MediaByKind`"
      )
      true
    }
  }
}
