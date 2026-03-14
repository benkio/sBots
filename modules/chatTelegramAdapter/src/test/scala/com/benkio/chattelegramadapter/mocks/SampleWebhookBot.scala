package com.benkio.chattelegramadapter.mocks

import cats.effect.Async
import cats.effect.IO
import com.benkio.chatcore.config.SBotConfig
import com.benkio.chatcore.mocks.DBLayerMock
import com.benkio.chatcore.mocks.RepositoryMock
import com.benkio.chatcore.model.reply.ReplyBundleCommand
import com.benkio.chatcore.model.reply.ReplyBundleMessage
import com.benkio.chatcore.model.Message
import com.benkio.chatcore.model.SBotInfo
import com.benkio.chatcore.model.SBotInfo.SBotId
import com.benkio.chatcore.model.SBotInfo.SBotName
import com.benkio.chatcore.patterns.PostComputationPatterns
import com.benkio.chatcore.repository.JsonDataRepository
import com.benkio.chatcore.Logger.given
import com.benkio.chattelegramadapter.initialization.BotSetup
import com.benkio.chattelegramadapter.mocks.ApiMock.given
import com.benkio.chattelegramadapter.ISBotWebhook
import com.benkio.chattelegramadapter.TelegramBackgroundJobManager
import org.http4s.client.Client
import org.http4s.implicits.*
import org.http4s.HttpApp
import org.http4s.Response
import org.http4s.Status
import org.http4s.Uri
import telegramium.bots.high.Api

class SampleWebhookBot(
    override val sBotSetup: BotSetup[IO],
    override val messageRepliesData: List[ReplyBundleMessage],
    override val commandRepliesData: List[ReplyBundleCommand]
) extends ISBotWebhook[IO](sBotSetup) {
  override def postComputation: Message => IO[Unit] =
    PostComputationPatterns.timeoutPostComputation(dbTimeout = dbLayer.dbTimeout, sBotId = sBotConfig.sBotInfo.botId)
  override def filteringMatchesMessages: (ReplyBundleMessage, Message) => IO[Boolean] =
    // In adapter unit tests we want selection to be deterministic and not gated by timeouts.
    (_: ReplyBundleMessage, _: Message) => IO.pure(true)

}

object SampleWebhookBot {

  val sBotInfo: SBotInfo = SBotInfo(
    botName = SBotInfo.SBotName("SampleWebhookBot"),
    botId = SBotInfo.SBotId("sbot")
  )
  val triggerFilename: String      = "sbot_triggers.txt"
  val listJsonFilename: String     = "sbot_list.json"
  val showFilename: String         = "sbot_shows.json"
  val repliesJsonFilename: String  = "sbot_replies.json"
  val commandsJsonFilename: String = "sbot_commands.json"
  val token: String                = "sbot_SampleWebhookBot.token"
  val triggerListUri: Uri          =
    uri"https://github.com/benkio/sBots/blob/main/modules/bots/RichardPHJBensonBot/rphjb_triggers.txt"
  val sBotConfig: SBotConfig = SBotConfig(
    sBotInfo = SampleWebhookBot.sBotInfo,
    triggerFilename = SampleWebhookBot.triggerFilename,
    triggerListUri = SampleWebhookBot.triggerListUri,
    listJsonFilename = SampleWebhookBot.listJsonFilename,
    showFilename = SampleWebhookBot.showFilename,
    repliesJsonFilename = SampleWebhookBot.repliesJsonFilename,
    commandsJsonFilename = SampleWebhookBot.commandsJsonFilename,
    token = SampleWebhookBot.token
  )

  def apply()(using Async[IO], Api[IO]): IO[SampleWebhookBot] = {
    val repositoryMock = new RepositoryMock()
    val dbLayerMock    = DBLayerMock.mock(sBotInfo.botId)
    val stubHttpApp    = HttpApp[IO](_ => IO.pure(Response[IO](Status.Ok)))
    val stubClient     = Client.fromHttpApp(stubHttpApp)
    for {
      backgroundJobManager <- TelegramBackgroundJobManager[IO](
        repository = repositoryMock,
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
      messageCommandData <- botSetup.jsonDataRepository.loadData[ReplyBundleCommand](
        SampleWebhookBot.sBotConfig.commandsJsonFilename
      )
    } yield new SampleWebhookBot(botSetup, messageRepliesData, messageCommandData)
  }
}
