package com.benkio.telegrambotinfrastructure.mocks

import cats.effect.Async
import cats.effect.IO
import com.benkio.telegrambotinfrastructure.config.SBotConfig
import com.benkio.telegrambotinfrastructure.initialization.BotSetup
import com.benkio.telegrambotinfrastructure.messagefiltering.FilteringTimeout
import com.benkio.telegrambotinfrastructure.mocks.ApiMock.given
import com.benkio.telegrambotinfrastructure.model.reply.ReplyBundleCommand
import com.benkio.telegrambotinfrastructure.model.reply.ReplyBundleMessage
import com.benkio.telegrambotinfrastructure.model.reply.TextReply
import com.benkio.telegrambotinfrastructure.model.CommandInstructionData
import com.benkio.telegrambotinfrastructure.model.CommandTrigger
import com.benkio.telegrambotinfrastructure.model.SBotInfo
import com.benkio.telegrambotinfrastructure.model.SBotInfo.SBotId
import com.benkio.telegrambotinfrastructure.model.SBotInfo.SBotName
import com.benkio.telegrambotinfrastructure.patterns.PostComputationPatterns
import com.benkio.telegrambotinfrastructure.repository.JsonDataRepository
import com.benkio.telegrambotinfrastructure.BackgroundJobManager
import com.benkio.telegrambotinfrastructure.SBotWebhook
import log.effect.fs2.SyncLogWriter.consoleLogUpToLevel
import log.effect.LogLevels
import log.effect.LogWriter
import org.http4s.client.Client
import org.http4s.implicits.*
import org.http4s.HttpApp
import org.http4s.Response
import org.http4s.Status
import org.http4s.Uri
import telegramium.bots.high.Api
import telegramium.bots.Message

class SampleWebhookBot(
    override val sBotSetup: BotSetup[IO],
    override val messageRepliesData: List[ReplyBundleMessage]
)(using logWriterIO: LogWriter[IO])
    extends SBotWebhook[IO](sBotSetup) {
  override def postComputation: Message => IO[Unit] =
    PostComputationPatterns.timeoutPostComputation(dbTimeout = dbLayer.dbTimeout, sBotId = sBotConfig.sBotInfo.botId)
  override def filteringMatchesMessages: (ReplyBundleMessage, Message) => IO[Boolean] =
    FilteringTimeout.filter(dbLayer, sBotConfig.sBotInfo.botId)

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
  val triggerFilename: String      = "sbot_triggers.txt"
  val repliesJsonFilename: String  = "sbot_replies.json"
  val commandsJsonFilename: String = "sbot_commands.json"
  val triggerListUri: Uri          =
    uri"https://github.com/benkio/sBots/blob/main/modules/bots/richardPHJBensonBot/rphjb_triggers.txt"
  val sBotConfig: SBotConfig = SBotConfig(
    sBotInfo = SampleWebhookBot.sBotInfo,
    triggerFilename = SampleWebhookBot.triggerFilename,
    triggerListUri = SampleWebhookBot.triggerListUri,
    repliesJsonFilename = SampleWebhookBot.repliesJsonFilename,
    commandsJsonFilename = SampleWebhookBot.commandsJsonFilename
  )
  given log: LogWriter[IO] = consoleLogUpToLevel(LogLevels.Info)

  def apply()(using Async[IO], Api[IO]): IO[SampleWebhookBot] = {
    val repositoryMock = new RepositoryMock()
    val dbLayerMock    = DBLayerMock.mock(sBotInfo.botId)
    val stubHttpApp    = HttpApp[IO](_ => IO.pure(Response[IO](Status.Ok)))
    val stubClient     = Client.fromHttpApp(stubHttpApp)
    for {
      backgroundJobManager <- BackgroundJobManager[IO](
        dbLayer = dbLayerMock,
        sBotInfo = sBotInfo,
        ttl = None
      )(using Async[IO], summon[Api[IO]], log)
      botSetup = BotSetup(
        token = "test",
        httpClient = stubClient,
        repository = repositoryMock,
        jsonDataRepository = JsonDataRepository[IO]( // repositoryMock
        ),
        dbLayer = dbLayerMock,
        backgroundJobManager = backgroundJobManager,
        api = summon[Api[IO]],
        webhookUri = uri"https://localhost",
        webhookPath = uri"/",
        sBotConfig = sBotConfig
      )
      // not memoized or resourced as it's just a test bot. other bot should be under Resource
      messageRepliesData <- botSetup.jsonDataRepository.loadData[ReplyBundleMessage](
        SampleWebhookBot.sBotConfig.repliesJsonFilename
      )
    } yield new SampleWebhookBot(botSetup, messageRepliesData)
  }
}
