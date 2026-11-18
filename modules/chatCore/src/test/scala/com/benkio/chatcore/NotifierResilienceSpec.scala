package com.benkio.chatcore

import cats.effect.Deferred
import cats.effect.IO
import com.benkio.chatcore.mocks.NotifierMock
import com.benkio.chatcore.model.reply.MediaFile
import com.benkio.chatcore.model.reply.Mp3File
import com.benkio.chatcore.model.Message
import com.benkio.chatcore.model.SBotInfo
import com.benkio.chatcore.Logger.given
import munit.CatsEffectSuite

import scala.concurrent.duration.*

class NotifierResilienceSpec extends CatsEffectSuite {

  private val botInfo = SBotInfo(SBotInfo.SBotId("bot"), SBotInfo.SBotName("BotName"))
  private val boom    = new RuntimeException("boom")

  private val failing: Notifier[IO] = new Notifier[IO] {
    override def info(sBotInfo: SBotInfo, message: String): IO[Unit]                            = IO.raiseError(boom)
    override def mediaError(message: Message, mediaFile: MediaFile, error: Throwable): IO[Unit] =
      IO.raiseError(boom)
    override def error(sBotInfo: SBotInfo, context: String, error: Throwable): IO[Unit] = IO.raiseError(boom)
  }

  test("guarded swallows the failure of the notification, whatever it is") {
    val notifier = Notifier.guarded(failing)
    val message  = Message(None, 0L, TestIds.chatId(1L), "private")
    for {
      _ <- notifier.info(botInfo, "started")
      _ <- notifier.mediaError(message, Mp3File("a.mp3"), boom)
      _ <- notifier.error(botInfo, "a job", boom)
    } yield ()
  }

  test("throttled sends a repeated error once and, when the window ends, how many more there were") {
    for {
      recording <- NotifierMock.recording
      window    <- Deferred[IO, Unit]
      throttle  <- Throttle.withWindow[IO](window.get)
      notifier = Notifier.throttled(recording, throttle)
      _       <- notifier.error(botInfo, "job", boom) >> notifier.error(botInfo, "job", boom) >> notifier.error(botInfo, "job", boom)
      first   <- recording.errors.get
      _       <- window.complete(())
      infos   <- recording.infos.get.iterateUntil(_.nonEmpty).timeout(5.seconds)
    } yield {
      assertEquals(first.map(_._2), List("job"))
      assertEquals(infos.size, 1)
      assert(infos.head._2.startsWith("2 more of: job"), infos.head._2)
    }
  }

  test("throttled does not merge errors of another context, error or bot") {
    for {
      recording <- NotifierMock.recording
      window    <- Deferred[IO, Unit]
      throttle  <- Throttle.withWindow[IO](window.get)
      notifier = Notifier.throttled(recording, throttle)
      other    = SBotInfo(SBotInfo.SBotId("other"), SBotInfo.SBotName("Other"))
      _      <- notifier.error(botInfo, "job", boom)
      _      <- notifier.error(botInfo, "another job", boom)
      _      <- notifier.error(botInfo, "job", new IllegalStateException("different"))
      _      <- notifier.error(other, "job", boom)
      errors <- recording.errors.get
    } yield assertEquals(errors.size, 4)
  }

  test("throttled leaves the infos and the media errors alone") {
    for {
      recording <- NotifierMock.recording
      window    <- Deferred[IO, Unit]
      throttle  <- Throttle.withWindow[IO](window.get)
      notifier = Notifier.throttled(recording, throttle)
      _     <- notifier.info(botInfo, "started") >> notifier.info(botInfo, "started")
      infos <- recording.infos.get
    } yield assertEquals(infos.size, 2)
  }

  test("resilient throttles and never fails") {
    for {
      notifier <- Notifier.resilient(failing, 1.hour)
      _        <- notifier.error(botInfo, "job", boom) >> notifier.error(botInfo, "job", boom)
    } yield ()
  }
}
