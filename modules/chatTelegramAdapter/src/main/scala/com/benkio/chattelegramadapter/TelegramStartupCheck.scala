package com.benkio.chattelegramadapter

import cats.effect.Async
import cats.effect.Ref
import cats.implicits.*
import com.benkio.chatcore.http.MegaClient
import com.benkio.chatcore.model.reply.MediaFile
import com.benkio.chatcore.model.Message
import com.benkio.chatcore.model.SBotInfo
import com.benkio.chatcore.Notifier
import com.benkio.chatcore.PostDeployMarker
import com.benkio.chatcore.Probe
import com.benkio.chatcore.ProbeResult
import com.benkio.chatcore.StartupCheck
import com.benkio.chattelegramadapter.http.telegramreply.messagereply.MediaFileReply
import com.benkio.chattelegramadapter.initialization.BotSetup
import com.benkio.chattelegramadapter.model.TelegramIds
import log.effect.LogWriter
import org.http4s.implicits.*
import org.http4s.Method
import telegramium.bots.high.implicits.methodOps
import telegramium.bots.high.Api
import telegramium.bots.high.Methods
import telegramium.bots.ChatIntId

import java.time.Instant
import scala.concurrent.duration.*

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
      // GET: the Mega API never answers a HEAD
      StartupCheck.hostReachable("Mega API", setup.httpClient, MegaClient.megaApiUri, Method.GET)
    )
  }

  /** The file is fetched by the repository and sent to the ops chat like the file of a reply: the slow path that users
    * feel. It gets more time than the other probes, it is a whole reply.
    *
    * The send of a reply never raises: it logs and notifies its error and gives back nothing. So the error is taken
    * from the notifier it is given, and the chat action it starts with is done here first: both would otherwise be lost
    * and the probe could only say "not sent".
    */
  def testFileProbe[F[_]: Async: LogWriter](setup: BotSetup[F], fileName: String): Probe[F] = {
    val chatId     = TelegramIds.chatId(setup.opsChatId)
    val opsMessage = Message(
      messageId = None,
      date = Instant.now.getEpochSecond,
      chatId = chatId,
      chatType = "private"
    )
    val send = Ref.of[F, Option[Throwable]](None).flatMap { reported =>
      val capturing: Notifier[F] = new Notifier[F] {
        override def info(sBotInfo: SBotInfo, message: String): F[Unit] = setup.notifier.info(sBotInfo, message)
        override def mediaError(message: Message, mediaFile: MediaFile, error: Throwable): F[Unit] =
          reported.set(Some(error)) *> setup.notifier.mediaError(message, mediaFile, error)
        override def error(sBotInfo: SBotInfo, context: String, error: Throwable): F[Unit] =
          setup.notifier.error(sBotInfo, context, error)
      }
      for {
        _    <- Methods.sendChatAction(ChatIntId(setup.opsChatId), "upload_video").exec(using setup.api).void
        sent <- MediaFileReply.sendMediaFile[F](
          reply = MediaFile.fromString(fileName),
          msg = opsMessage,
          repository = setup.repository,
          replyToMessage = false
        )(using Async[F], summon[LogWriter[F]], setup.api, capturing)
        error <- reported.get
        _     <- Async[F].raiseWhen(sent.isEmpty)(
          error.getOrElse(new RuntimeException(s"$fileName was not sent and no error was reported"))
        )
      } yield ()
    }
    // A stalled file send must not hold the bot's start for a minute: the probe
    // reports the timeout and polling starts anyway.
    Probe(s"Test file $fileName", send, timeout = Some(10.seconds))
  }

  /** Only after a deploy: the deploy leaves one marker file per bot and the first start of each bot that finds its own
    * marker sends that bot's test file.
    */
  private def postDeployTestFile[F[_]: Async: LogWriter](setup: BotSetup[F]): F[List[ProbeResult]] = {
    val config = setup.sBotConfig.startupCheck
    val botId  = setup.sBotConfig.sBotInfo.botId.value
    config.testMedia.fold(List.empty[ProbeResult].pure[F])(fileName =>
      PostDeployMarker
        .consume[F](PostDeployMarker.fileFor(config.markerFile, botId))
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
