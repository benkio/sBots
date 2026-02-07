package com.benkio.telegrambotinfrastructure.mocks

import cats.effect.Async
import cats.effect.IO
import com.benkio.telegrambotinfrastructure.config.SBotConfig
import com.benkio.telegrambotinfrastructure.messagefiltering.FilteringTimeout
import com.benkio.telegrambotinfrastructure.mocks.ApiMock.given
import com.benkio.telegrambotinfrastructure.model.reply.gif
import com.benkio.telegrambotinfrastructure.model.reply.mp3
import com.benkio.telegrambotinfrastructure.model.reply.vid
import com.benkio.telegrambotinfrastructure.model.reply.ReplyBundleCommand
import com.benkio.telegrambotinfrastructure.model.reply.ReplyBundleMessage
import com.benkio.telegrambotinfrastructure.model.reply.TextReply
import com.benkio.telegrambotinfrastructure.model.CommandInstructionData
import com.benkio.telegrambotinfrastructure.model.CommandTrigger
import com.benkio.telegrambotinfrastructure.model.SBotInfo
import com.benkio.telegrambotinfrastructure.model.SBotInfo.SBotId
import com.benkio.telegrambotinfrastructure.model.SBotInfo.SBotName
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
import telegramium.bots.high.Api
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
    extends SBotWebhook[IO](uri, path, webhookCertificate) {
  override def repository: Repository[IO] =
    repositoryInput
  override def postComputation: Message => IO[Unit] =
    PostComputationPatterns.timeoutPostComputation(dbTimeout = dbLayer.dbTimeout, sBotId = sBotConfig.sBotInfo.botId)
  override def filteringMatchesMessages: (ReplyBundleMessage, Message) => IO[Boolean] =
    FilteringTimeout.filter(dbLayer, sBotConfig.sBotInfo.botId)

  override val sBotConfig: SBotConfig = SampleWebhookBot.sBotConfig

  override val messageRepliesData: IO[List[ReplyBundleMessage]] = IO.pure(List(
    ReplyBundleMessage.textToMp3(
      "cosa preferisci",
      "ragazzetta",
      "carne bianca"
    )(
      mp3"rphjb_RagazzettaCarne.mp3"
    ),
    ReplyBundleMessage.textToVideo(
      "brooklyn",
      "carne morta",
      "manhattan",
      "cane da guerra"
    )(
      vid"rphjb_PrimoSbaglio.mp4"
    ),
    ReplyBundleMessage.textToVideo(
      "non siamo niente",
      "siamo esseri umani",
      "sudore",
      "pelle",
      "zozzeria",
      "carne",
      "sperma",
      "da togliere",
      "levare d[ia] dosso".r,
      "non contiamo niente"
    )(
      vid"rphjb_EsseriUmaniZozzeriaCarnePelleSputoSudoreSpermaNonContiamoNiente.mp4"
    ),
    ReplyBundleMessage.textToMedia(
      "carne saporita"
    )(
      mp3"rphjb_RagazzettaCarne.mp3",
      mp3"rphjb_CarneFrescaSaporita.mp3",
      vid"rphjb_CarneFrescaSaporita.mp4",
      gif"rphjb_CarneFrescaSaporitaGif.mp4"
    ),
    ReplyBundleMessage.textToMedia(
      "carne (dura|vecchia|fresca)".r
    )(
      mp3"rphjb_CarneFrescaSaporita.mp3",
      vid"rphjb_CarneFrescaSaporita.mp4",
      gif"rphjb_CarneFrescaSaporitaGif.mp4"
    )
  ))

  override val commandRepliesData: List[ReplyBundleCommand] =
    List(
      ReplyBundleCommand(
        trigger = CommandTrigger("testcommand"),
        reply = TextReply.fromList(
          "test command reply"
        )(false),
        instruction = CommandInstructionData.NoInstructions
      )
    )
}

object SampleWebhookBot {

  val sBotInfo: SBotInfo = SBotInfo(
    botName = SBotInfo.SBotName("SampleWebhookBot"),
    botId = SBotInfo.SBotId("sbot")
  )
  val triggerFilename: String     = "sbot_triggers.txt"
  val repliesJsonFilename: String = "sbot_replies.json"
  val triggerListUri: Uri         =
    uri"https://github.com/benkio/sBots/blob/main/modules/bots/richardPHJBensonBot/rphjb_triggers.txt"
  val sBotConfig: SBotConfig = SBotConfig(
    sBotInfo = SampleWebhookBot.sBotInfo,
    triggerFilename = SampleWebhookBot.triggerFilename,
    triggerListUri = SampleWebhookBot.triggerListUri,
    repliesJsonFilename = SampleWebhookBot.repliesJsonFilename
  )
  given log: LogWriter[IO] = consoleLogUpToLevel(LogLevels.Info)

  def apply()(using Async[IO], Api[IO]): IO[SampleWebhookBot] = {
    val repositoryMock             = new RepositoryMock()
    val dbLayerMock                = DBLayerMock.mock(sBotInfo.botId)
    val ioBackgroundJobManagerMock = BackgroundJobManager[IO](
      dbLayer = dbLayerMock,
      sBotInfo = sBotInfo,
      ttl = None
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
