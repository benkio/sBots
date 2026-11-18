package com.benkio.chattelegramadapter

import cats.data.NonEmptyList
import cats.effect.IO
import com.benkio.chatcore.config.StartupCheckConfig
import com.benkio.chatcore.mocks.RepositoryMock
import com.benkio.chatcore.model.media.MediaResource
import com.benkio.chatcore.model.media.MediaSource
import com.benkio.chatcore.model.Platform
import com.benkio.chatcore.model.SBotInfo
import com.benkio.chatcore.repository.Repository
import com.benkio.chatcore.Logger.given
import com.benkio.chatcore.PostDeployMarker
import com.benkio.chattelegramadapter.http.TelegramNotifier
import com.benkio.chattelegramadapter.initialization.BotSetup
import com.benkio.chattelegramadapter.mocks.ApiMock.given
import com.benkio.chattelegramadapter.mocks.PayloadRecordingApi
import com.benkio.chattelegramadapter.mocks.SampleWebhookBot
import io.circe.Json
import munit.CatsEffectSuite
import org.http4s.client.Client
import org.http4s.HttpApp
import org.http4s.Response
import org.http4s.Status

import java.nio.file.Files
import scala.concurrent.duration.*

class TelegramStartupCheckSpec extends CatsEffectSuite {

  private val opsChatId: Long = -1001L
  private val startMessage    = "Start Test Bot Successful ✅"
  // what the notifier writes for the start of the bot
  private val started       = s"[${SampleWebhookBot.sBotInfo.botName}] $startMessage"
  private val testMedia     = "test_file.mp3"
  private val fileAvailable = new RepositoryMock(getResourceFileHandler =
    _ =>
      IO.pure(
        Right(
          NonEmptyList.one(MediaResource.MediaResourceIFile(MediaSource.FileRef(Platform.Telegram, "telegram-file-id")))
        )
      )
  )

  /** The setup of a sample bot over a recording api: what the notifier sends to the ops chat is in `calls`. */
  private def setupOver(
      startupCheck: StartupCheckConfig,
      repository: com.benkio.chatcore.repository.Repository[IO] = fileAvailable
  ): IO[(BotSetup[IO], IO[List[(String, Json)]])] =
    for {
      apiAndRecording <- PayloadRecordingApi()
      (api, recorded) = apiAndRecording
      bot <- SampleWebhookBot()
      setup = bot.sBotSetup.copy(
        api = api,
        notifier = new TelegramNotifier[IO](opsChatId)(using summon, api, summon),
        opsChatId = opsChatId,
        repository = repository,
        sBotConfig = bot.sBotSetup.sBotConfig.copy(startupCheck = startupCheck)
      )
    } yield (setup, recorded)

  private def opsTexts(calls: List[(String, Json)]): List[String] =
    calls.collect { case ("sendMessage", payload) => payload.hcursor.downField("text").as[String].toOption }.flatten

  test("the start is notified with the report of the checks: the DB, Telegram and the file hosts") {
    for {
      setupAndCalls <- setupOver(StartupCheckConfig())
      (setup, recorded) = setupAndCalls
      _     <- TelegramStartupCheck.run(setup, startMessage)
      calls <- recorded
    } yield {
      val texts = opsTexts(calls)
      assertEquals(texts.size, 1, texts.toString)
      val text = texts.head
      assert(text.startsWith(started), text)
      List("Startup checks OK", "DB read", "Telegram getMe", "Dropbox", "Mega API").foreach(expected =>
        assert(text.contains(expected), s"`$expected` is missing in: $text")
      )
      assert(!text.contains("Test file"), "no marker, no test file")
      assert(!calls.exists(_._1 == "sendAudio"), "no test file is sent without the marker")
    }
  }

  test("with the checks disabled only the start is notified") {
    for {
      setupAndCalls <- setupOver(StartupCheckConfig(enabled = false))
      (setup, recorded) = setupAndCalls
      _     <- TelegramStartupCheck.run(setup, startMessage)
      calls <- recorded
    } yield assertEquals(opsTexts(calls), List(started))
  }

  test("after a deploy the test file is sent to the ops chat once and the bot's marker is consumed") {
    for {
      marker <- IO.blocking(Files.createTempFile("post-deploy", ".marker"))
      botMarker = PostDeployMarker.fileFor(marker.toString, SampleWebhookBot.sBotInfo.botId.value)
      _             <- IO.blocking(Files.createFile(botMarker))
      setupAndCalls <- setupOver(StartupCheckConfig(testMedia = Some(testMedia), markerFile = marker.toString))
      (setup, recorded) = setupAndCalls
      _      <- TelegramStartupCheck.run(setup, startMessage)
      first  <- recorded
      _      <- TelegramStartupCheck.run(setup, startMessage)
      second <- recorded
    } yield {
      assertEquals(first.count(_._1 == "sendAudio"), 1, "the test file is sent to the ops chat")
      assert(opsTexts(first).head.contains(s"Test file $testMedia"), opsTexts(first).toString)
      assertEquals(second.count(_._1 == "sendAudio"), 1, "the second start does not send it again")
      assert(!Files.exists(botMarker), "the bot's marker is consumed")
      assert(Files.exists(marker), "the shared marker is left to the other bots")
    }
  }

  test("two bots with the same marker configuration each send their own test file after a deploy") {
    val config = StartupCheckConfig(testMedia = Some(testMedia))
    for {
      marker <- IO.blocking(Files.createTempFile("post-deploy", ".marker"))
      markerPath  = marker.toString
      setupForBot = (botId: String) =>
        for {
          _             <- IO.blocking(Files.createFile(PostDeployMarker.fileFor(markerPath, botId)))
          setupAndCalls <- setupOver(config.copy(markerFile = markerPath))
          (setup, recorded) = setupAndCalls
          info              = SBotInfo(
            botId = SBotInfo.SBotId(botId),
            botName = SBotInfo.SBotName(s"${botId}_Bot")
          )
        } yield (setup.copy(sBotConfig = setup.sBotConfig.copy(sBotInfo = info)), recorded)
      (firstSetup, firstCalls)   <- setupForBot("sbot")
      (secondSetup, secondCalls) <- setupForBot("other")
      _                          <- TelegramStartupCheck.run(firstSetup, startMessage)
      _                          <- TelegramStartupCheck.run(secondSetup, startMessage)
      first                      <- firstCalls
      second                     <- secondCalls
    } yield {
      assertEquals(first.count(_._1 == "sendAudio"), 1, "the first bot sends its test file")
      assertEquals(second.count(_._1 == "sendAudio"), 1, "the second bot sends its test file")
    }
  }

  test("a test file that can't be fetched is a failed check with the real reason, the start is notified anyway") {
    val missing = new RepositoryMock(getResourceFileHandler = _ => IO.raiseError(new RuntimeException("not found")))
    for {
      marker <- IO.blocking(Files.createTempFile("post-deploy", ".marker"))
      botMarker = PostDeployMarker.fileFor(marker.toString, SampleWebhookBot.sBotInfo.botId.value)
      _             <- IO.blocking(Files.createFile(botMarker))
      setupAndCalls <- setupOver(
        StartupCheckConfig(testMedia = Some(testMedia), markerFile = marker.toString, probeTimeout = 2.seconds),
        repository = missing
      )
      (setup, recorded) = setupAndCalls
      _     <- TelegramStartupCheck.run(setup, startMessage)
      calls <- recorded
    } yield {
      // the media error of the failed file goes to the ops chat first, then the report with the start
      val text = opsTexts(calls).last
      assert(text.startsWith(started), text)
      assert(text.contains("⚠️ Startup checks"), text)
      assert(text.contains(s"❌ Test file $testMedia"), text)
      assert(text.contains("not found"), s"the reason is missing in: $text")
    }
  }

  test("a test file that is not in the DB is a failed check with the reason, not a generic `not sent`") {
    // the notifier does not report this error to the ops chat (a user typing a wrong name): the report is the only place
    val notInDb = new RepositoryMock(getResourceFileHandler =
      mediaFile => IO.pure(Left(Repository.RepositoryError.NoResourcesFoundFile(mediaFile)))
    )
    for {
      marker <- IO.blocking(Files.createTempFile("post-deploy", ".marker"))
      botMarker = PostDeployMarker.fileFor(marker.toString, SampleWebhookBot.sBotInfo.botId.value)
      _             <- IO.blocking(Files.createFile(botMarker))
      setupAndCalls <- setupOver(
        StartupCheckConfig(testMedia = Some(testMedia), markerFile = marker.toString, probeTimeout = 2.seconds),
        repository = notInDb
      )
      (setup, recorded) = setupAndCalls
      _     <- TelegramStartupCheck.run(setup, startMessage)
      calls <- recorded
    } yield {
      val text = opsTexts(calls).last
      assert(text.contains(s"❌ Test file $testMedia"), text)
      assert(text.contains("getResourceFile returned no results"), s"the reason is missing in: $text")
    }
  }

  test("the Mega API is asked with a GET: it never answers a HEAD") {
    val methodsByHost = new java.util.concurrent.ConcurrentHashMap[String, String]()
    val recording     = Client.fromHttpApp(HttpApp[IO] { request =>
      IO(methodsByHost.put(request.uri.host.map(_.value).getOrElse("?"), request.method.name))
        .as(Response[IO](Status.Ok))
    })
    for {
      setupAndCalls <- setupOver(StartupCheckConfig())
      (setup, _) = setupAndCalls
      _ <- TelegramStartupCheck.run(setup.copy(httpClient = recording), startMessage)
    } yield {
      assertEquals(methodsByHost.get("g.api.mega.co.nz"), "GET")
      assertEquals(methodsByHost.get("www.dropbox.com"), "HEAD")
    }
  }
}
