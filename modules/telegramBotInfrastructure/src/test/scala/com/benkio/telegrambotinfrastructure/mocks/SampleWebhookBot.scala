package com.benkio.telegrambotinfrastructure.mocks

import cats.effect.Async
import cats.effect.IO
import cats.syntax.all.*
import cats.Applicative
import com.benkio.telegrambotinfrastructure.messagefiltering.FilteringTimeout
import com.benkio.telegrambotinfrastructure.mocks.ApiMock.given
import com.benkio.telegrambotinfrastructure.model.reply.gif
import com.benkio.telegrambotinfrastructure.model.reply.mp3
import com.benkio.telegrambotinfrastructure.model.reply.vid
import com.benkio.telegrambotinfrastructure.model.reply.ReplyBundleCommand
import com.benkio.telegrambotinfrastructure.model.reply.ReplyBundleMessage
import com.benkio.telegrambotinfrastructure.model.reply.TextReply
import com.benkio.telegrambotinfrastructure.model.tr
import com.benkio.telegrambotinfrastructure.model.CommandTrigger
import com.benkio.telegrambotinfrastructure.patterns.PostComputationPatterns
import com.benkio.telegrambotinfrastructure.resources.db.DBLayer
import com.benkio.telegrambotinfrastructure.resources.ResourceAccess
import com.benkio.telegrambotinfrastructure.BackgroundJobManager
import com.benkio.telegrambotinfrastructure.BotSkeletonWebhook
import log.effect.fs2.SyncLogWriter.consoleLogUpToLevel
import log.effect.LogLevels
import log.effect.LogWriter
import org.http4s.implicits.*
import org.http4s.Uri
import telegramium.bots.InputPartFile
import telegramium.bots.Message

class SampleWebhookBot(
    uri: Uri,
    resourceAccess: ResourceAccess[IO],
    val dbLayer: DBLayer[IO],
    val backgroundJobManager: BackgroundJobManager[IO],
    path: Uri = uri"/",
    webhookCertificate: Option[InputPartFile] = None
)(using logWriterIO: LogWriter[IO])
    extends BotSkeletonWebhook[IO](uri, path, webhookCertificate, resourceAccess) {
  override def resourceAccess(using AsyncF: Async[IO], log: LogWriter[IO]): ResourceAccess[IO] = resourceAccess
  override def postComputation(using appIO: Applicative[IO]): Message => IO[Unit] =
    PostComputationPatterns.timeoutPostComputation(dbTimeout = dbLayer.dbTimeout, botName = botName)
  override def filteringMatchesMessages(using
      applicativeIO: Applicative[IO]
  ): (ReplyBundleMessage[IO], Message) => IO[Boolean] =
    FilteringTimeout.filter(dbLayer, botName)

  override val botName: String                     = "SampleWebhookBot"
  override val botPrefix: String                   = "sbot"
  override val ignoreMessagePrefix: Option[String] = Some("!")
  override val triggerFilename: String             = "sbot_triggers.txt"
  override val triggerListUri: Uri =
    uri"https://github.com/benkio/sBots/blob/master/modules/bots/richardPHJBensonBot/rphjb_triggers.txt"

  override def messageRepliesDataF(using
      applicativeIO: Applicative[IO],
      log: LogWriter[IO]
  ): IO[List[ReplyBundleMessage[IO]]] = List(
    ReplyBundleMessage.textToMp3[IO](
      "cosa preferisci",
      "ragazzetta",
      "carne bianca"
    )(
      mp3"rphjb_RagazzettaCarne.mp3"
    ),
    ReplyBundleMessage.textToVideo[IO](
      "brooklyn",
      "carne morta",
      "manhattan",
      "cane da guerra"
    )(
      vid"rphjb_PrimoSbaglio.mp4"
    ),
    ReplyBundleMessage.textToVideo[IO](
      "non siamo niente",
      "siamo esseri umani",
      "sudore",
      "pelle",
      "zozzeria",
      "carne",
      "sperma",
      "da togliere",
      "levare d[ia] dosso".r.tr(15),
      "non contiamo niente"
    )(
      vid"rphjb_EsseriUmaniZozzeriaCarnePelleSputoSudoreSpermaNonContiamoNiente.mp4"
    ),
    ReplyBundleMessage.textToMedia[IO](
      "carne saporita"
    )(
      mp3"rphjb_RagazzettaCarne.mp3",
      mp3"rphjb_CarneFrescaSaporita.mp3",
      vid"rphjb_CarneFrescaSaporita.mp4",
      gif"rphjb_CarneFrescaSaporitaGif.mp4"
    ),
    ReplyBundleMessage.textToMedia[IO](
      "carne (dura|vecchia|fresca)".r.tr(10)
    )(
      mp3"rphjb_CarneFrescaSaporita.mp3",
      vid"rphjb_CarneFrescaSaporita.mp4",
      gif"rphjb_CarneFrescaSaporitaGif.mp4"
    )
  ).pure[IO]

  override def commandRepliesDataF(using asyncIO: Async[IO], log: LogWriter[IO]): IO[List[ReplyBundleCommand[IO]]] =
    List(
      ReplyBundleCommand(
        trigger = CommandTrigger("testcommand"),
        reply = TextReply.fromList[IO](
          "test command reply"
        )(false)
      )
    ).pure[IO]
}

object SampleWebhookBot {

  given log: LogWriter[IO] = consoleLogUpToLevel(LogLevels.Info)

  def apply(): IO[SampleWebhookBot] = {
    val resourceAccessMock = new ResourceAccessMock()
    val dbLayerMock        = DBLayerMock.mock("SampleWebhookBot")
    val ioBackgroundJobManagerMock = BackgroundJobManager(
      dbSubscription = dbLayerMock.dbSubscription,
      dbShow = dbLayerMock.dbShow,
      resourceAccess = resourceAccessMock,
      botName = "SampleWebhookBot"
    )
    ioBackgroundJobManagerMock.map(backgroundJobManagerMock =>
      new SampleWebhookBot(
        uri = uri"https://localhost",
        resourceAccess = resourceAccessMock,
        dbLayer = dbLayerMock,
        backgroundJobManager = backgroundJobManagerMock
      )
    )
  }
}
