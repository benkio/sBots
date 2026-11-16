package com.benkio.chattelegramadapter

import cats.effect.Async
import cats.implicits.*
import com.benkio.chatcore.http.MegaClient
import com.benkio.chatcore.model.reply.MediaFile
import com.benkio.chatcore.model.Message
import com.benkio.chatcore.PostDeployMarker
import com.benkio.chatcore.Probe
import com.benkio.chatcore.ProbeResult
import com.benkio.chatcore.StartupCheck
import com.benkio.chattelegramadapter.http.telegramreply.messagereply.MediaFileReply
import com.benkio.chattelegramadapter.initialization.BotSetup
import com.benkio.chattelegramadapter.model.TelegramIds
import log.effect.LogWriter
import org.http4s.implicits.*
import telegramium.bots.high.implicits.methodOps
import telegramium.bots.high.Api
import telegramium.bots.high.Methods

import java.nio.file.Path
import java.time.Instant

/** The checks of a Telegram bot when it starts: the first requests warm up the JVM, TLS and the connections, and the
  * result tells the ops chat if something is slow or broken, instead of the first user finding out.
  */
object TelegramStartupCheck {

  def probes[F[_]: Async](setup: BotSetup[F]): List[Probe[F]] = {
    given Api[F] = setup.api
    List(
      StartupCheck.dbRead(setup.dbLayer),
      Probe("Telegram getMe", Methods.getMe().exec.void),
      StartupCheck.hostReachable("Dropbox", setup.httpClient, uri"https://www.dropbox.com"),
      StartupCheck.hostReachable("Mega API", setup.httpClient, MegaClient.megaApiUri)
    )
  }

  /** The file is fetched by the repository and sent to the ops chat like the file of a reply: the slow path that users
    * feel. It gets more time than the other probes, it is a whole reply.
    */
  def testFileProbe[F[_]: Async: LogWriter](setup: BotSetup[F], fileName: String): Probe[F] = {
    val opsMessage = Message(
      messageId = None,
      date = Instant.now.getEpochSecond,
      chatId = TelegramIds.chatId(setup.opsChatId),
      chatType = "private"
    )
    val send = MediaFileReply
      .sendMediaFile[F](
        reply = MediaFile.fromString(fileName),
        msg = opsMessage,
        repository = setup.repository,
        replyToMessage = false
      )(using Async[F], summon[LogWriter[F]], setup.api, setup.notifier)
      .flatMap(sent => Async[F].raiseWhen(sent.isEmpty)(new RuntimeException(s"$fileName was not sent")))
    Probe(s"Test file $fileName", send, timeout = Some(setup.sBotConfig.startupCheck.probeTimeout * 4))
  }

  /** Only after a deploy: the deploy leaves a marker file and the first start that finds it sends the test file. */
  private def postDeployTestFile[F[_]: Async: LogWriter](setup: BotSetup[F]): F[List[ProbeResult]] = {
    val config = setup.sBotConfig.startupCheck
    config.testMedia.fold(List.empty[ProbeResult].pure[F])(fileName =>
      PostDeployMarker
        .consume[F](Path.of(config.markerFile))
        .ifM(
          StartupCheck.runProbe(testFileProbe(setup, fileName), config).map(List(_)),
          List.empty[ProbeResult].pure[F]
        )
    )
  }

  /** Runs the checks and tells the ops chat that the bot started, with the report. A check that goes wrong never
    * prevents the bot from starting: the start is notified anyway.
    */
  def run[F[_]: Async: LogWriter](setup: BotSetup[F], startMessage: String): F[Unit] = {
    val config  = setup.sBotConfig.startupCheck
    val botInfo = setup.sBotConfig.sBotInfo
    val started = setup.notifier.info(botInfo, startMessage)
    val checked = for {
      results  <- StartupCheck.run(probes(setup), config)
      testFile <- postDeployTestFile(setup)
      report = StartupCheck.report(results ++ testFile)
      _ <- LogWriter.info(s"[StartupCheck - ${botInfo.botName}] $report")
      _ <- setup.notifier.info(botInfo, s"$startMessage\n$report")
    } yield ()
    checked
      .handleErrorWith(e => LogWriter.error(s"[StartupCheck - ${botInfo.botName}] check failed: $e") *> started)
      .whenA(config.enabled) *> started.unlessA(config.enabled)
  }
}
