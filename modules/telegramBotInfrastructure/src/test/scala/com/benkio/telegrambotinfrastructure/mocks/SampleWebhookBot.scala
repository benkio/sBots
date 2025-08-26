package com.benkio.telegrambotinfrastructure.mocks

import cats.effect.IO
import cats.syntax.all.*
import com.benkio.telegrambotinfrastructure.messagefiltering.FilteringTimeout
import com.benkio.telegrambotinfrastructure.mocks.ApiMock.given
import com.benkio.telegrambotinfrastructure.model.reply.gif
import com.benkio.telegrambotinfrastructure.model.reply.mp3
import com.benkio.telegrambotinfrastructure.model.reply.vid
import com.benkio.telegrambotinfrastructure.model.reply.ReplyBundleCommand
import com.benkio.telegrambotinfrastructure.model.reply.ReplyBundleMessage
import com.benkio.telegrambotinfrastructure.model.reply.TextReply
import com.benkio.telegrambotinfrastructure.model.tr
import com.benkio.telegrambotinfrastructure.model.CommandInstructionData
import com.benkio.telegrambotinfrastructure.model.CommandTrigger
import com.benkio.telegrambotinfrastructure.patterns.PostComputationPatterns
import com.benkio.telegrambotinfrastructure.repository.db.DBLayer
import com.benkio.telegrambotinfrastructure.repository.Repository
import com.benkio.telegrambotinfrastructure.BackgroundJobManager
import com.benkio.telegrambotinfrastructure.SBotWebhook
import log.effect.fs2.SyncLogWriter.consoleLogUpToLevel
import log.effect.LogLevels
import log.effect.LogWriter
import org.http4s.implicits.*
import org.http4s.Uri
import telegramium.bots.InputPartFile
import telegramium.bots.Message

class SampleWebhookBot(
    uri: Uri,
    repositoryInput: Repository[IO],
    val dbLayer: DBLayer[IO],
    val backgroundJobManager: BackgroundJobManager[IO],
    path: Uri = uri"/",
    webhookCertificate: Option[InputPartFile] = None
)(using logWriterIO: LogWriter[IO])
    extends SBotWebhook[IO](uri, path, webhookCertificate, repositoryInput) {
  override def repository: Repository[IO] =
    repositoryInput
  override def postComputation: Message => IO[Unit] =
    PostComputationPatterns.timeoutPostComputation(dbTimeout = dbLayer.dbTimeout, botName = botName)
  override def filteringMatchesMessages: (ReplyBundleMessage[IO], Message) => IO[Boolean] =
    FilteringTimeout.filter(dbLayer, botName)

  override val botName: String                     = "SampleWebhookBot"
  override val botPrefix: String                   = "sbot"
  override val ignoreMessagePrefix: Option[String] = Some("!")
  override val triggerFilename: String             = "sbot_triggers.txt"
  override val triggerListUri: Uri                 =
    uri"https://github.com/benkio/sBots/blob/main/modules/bots/richardPHJBensonBot/rphjb_triggers.txt"

  override def messageRepliesDataF: IO[List[ReplyBundleMessage[IO]]] = List(
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

  override def commandRepliesDataF: IO[List[ReplyBundleCommand[IO]]] =
    List(
      ReplyBundleCommand(
        trigger = CommandTrigger("testcommand"),
        reply = TextReply.fromList[IO](
          "test command reply"
        )(false),
        instruction = CommandInstructionData.NoInstructions
      )
    ).pure[IO]
}

object SampleWebhookBot {

  given log: LogWriter[IO] = consoleLogUpToLevel(LogLevels.Info)

  def apply(): IO[SampleWebhookBot] = {
    val repositoryMock         = new RepositoryMock()
    val dbLayerMock                = DBLayerMock.mock("SampleWebhookBot")
    val ioBackgroundJobManagerMock = BackgroundJobManager(
      dbSubscription = dbLayerMock.dbSubscription,
      dbShow = dbLayerMock.dbShow,
      repository = repositoryMock,
      botName = "SampleWebhookBot"
    )
    ioBackgroundJobManagerMock.map(backgroundJobManagerMock =>
      new SampleWebhookBot(
        uri = uri"https://localhost",
        repositoryInput = repositoryMock,
        dbLayer = dbLayerMock,
        backgroundJobManager = backgroundJobManagerMock
      )
    )
  }
}
