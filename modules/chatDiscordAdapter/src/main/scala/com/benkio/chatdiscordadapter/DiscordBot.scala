package com.benkio.chatdiscordadapter

import cats.effect.std.Dispatcher
import cats.effect.std.Random
import cats.effect.Async
import cats.effect.Deferred
import cats.syntax.all.*
import com.benkio.chatcore.config.SBotConfig
import com.benkio.chatcore.initialization.BotCoreSetup
import com.benkio.chatcore.llm.LlmWiring
import com.benkio.chatcore.model.reply.ReplyValue
import com.benkio.chatcore.model.ChatId
import com.benkio.chatcore.model.Message
import com.benkio.chatcore.model.Platform
import com.benkio.chatcore.model.SBotInfo
import com.benkio.chatcore.repository.ResourcesRepository
import com.benkio.chatcore.AutoTimeout
import com.benkio.chatcore.BotEngine
import com.benkio.chatcore.CronBackgroundJobManager
import com.benkio.chatcore.EffectfulCallback
import com.benkio.chatcore.Notifier
import com.benkio.chatcore.PagedListNavigation
import com.benkio.chatcore.SubscriptionFlowPromptStore
import com.benkio.chatdiscordadapter.gateway.DiscordGateway
import com.benkio.chatdiscordadapter.gateway.DiscordGatewayConfig
import com.benkio.chatdiscordadapter.gateway.DiscordToken
import com.benkio.chatdiscordadapter.inbound.DiscordInboundListener
import com.benkio.chatdiscordadapter.inbound.DiscordSlashCommands
import com.benkio.chatdiscordadapter.model.DiscordCapabilities
import com.benkio.chatdiscordadapter.outbound.DiscordApi
import com.benkio.chatdiscordadapter.outbound.DiscordButtonHandler
import com.benkio.chatdiscordadapter.outbound.DiscordButtonLists
import com.benkio.chatdiscordadapter.outbound.DiscordReplies
import com.benkio.chatdiscordadapter.outbound.JdaDiscordApi
import log.effect.LogWriter
import org.http4s.client.Client

/** Runs a bot on Discord: the engine, the Discord adapter, the subscriptions, and the connection to the gateway, until
  * the connection ends.
  */
object DiscordBot {

  def run[F[_]: Async](
      httpClient: Client[F],
      sBotInfo: SBotInfo,
      commandEffectfulCallback: Map[String, EffectfulCallback[F]],
      gatewayConfig: DiscordGatewayConfig,
      notifier: Notifier[F],
      llmWiring: Option[LlmWiring[F]] = None
  )(using log: LogWriter[F]): F[Unit] = {
    val sBotConfig = SBotConfig.fromSBotInfo(sBotInfo)
    (for {
      token <- DiscordToken.read[F](sBotInfo, ResourcesRepository.fromResources[F]())
      core  <- BotCoreSetup[F](httpClient, sBotConfig, llmWiring)
    } yield (token, core)).use((token, core) =>
      runWith[F](
        core = core,
        token = token,
        commandEffectfulCallback = commandEffectfulCallback,
        gatewayConfig = gatewayConfig,
        notifier = notifier
      )
    )
  }

  /** The bot with its DB, repository and token already there. */
  def runWith[F[_]: Async](
      core: BotCoreSetup[F],
      token: String,
      commandEffectfulCallback: Map[String, EffectfulCallback[F]],
      gatewayConfig: DiscordGatewayConfig,
      notifier: Notifier[F]
  )(using log: LogWriter[F]): F[Unit] = {
    given Notifier[F] = notifier
    Dispatcher.parallel[F].use { dispatcher =>
      val effectiveSBotConfig = core.sBotConfig
      val sBotInfo            = effectiveSBotConfig.sBotInfo
      val capabilities        = DiscordCapabilities.value
      val repository          = core.repository.usableFor(capabilities)
      for {
        apiReady <- Deferred[F, DiscordApi[F]]
        random   <- Random.scalaUtilRandom[F]
        lists    <- DiscordButtonLists.create[F]()
        // the guided flow prompts are handles to the messages of the subscription setup: edited at every step
        flowPrompts = SubscriptionFlowPromptStore.db[F](core.dbLayer.dbSubscriptionFlowPrompt)
        // the subscriptions send to a chat without a message to reply to, once the connection is up
        sendToChat = (chatId: com.benkio.chatcore.model.ChatId, replyValue: ReplyValue, replyToMessage: Boolean) =>
          apiReady.get.flatMap(api =>
            DiscordReplies[F](api, repository, core.dbLayer, capabilities, lists, flowPrompts)
              .send(
                replyValue,
                Message(messageId = None, date = 0L, chatId = chatId, chatType = "private"),
                replyToMessage
              )
          )
        backgroundJobManager <- {
          given Random[F] = random
          CronBackgroundJobManager[F](
            dbLayer = core.dbLayer,
            sBotInfo = sBotInfo,
            ttl = effectiveSBotConfig.messageTimeToLive,
            capabilities = capabilities,
            send = sendToChat,
            notifier = notifier
          )
        }
        engine = BotEngine[F](effectiveSBotConfig, core.dbLayer, notifier, backgroundJobManager)
        _ <- {
          lazy val adapter: DiscordPlatformAdapter[F] = new DiscordPlatformAdapter[F](
            discordApi = apiReady.get,
            repository = core.repository,
            backgroundJobManager = backgroundJobManager,
            effectfulCallbacks = commandEffectfulCallback,
            dbLayer = core.dbLayer,
            ttl = effectiveSBotConfig.messageTimeToLive,
            startGateway = startGateway,
            lists = lists,
            flowPrompts = flowPrompts
          )
          // deferred: the listener needs the adapter, which needs this
          def startGateway: F[Unit] = Async[F].defer {
            val listener = new DiscordInboundListener[F](
              dispatcher,
              capabilities.commandSyntax,
              message => engine.onMessage(message)(using adapter),
              join =>
                join.channelIds.traverse_(channelId =>
                  AutoTimeout.set(
                    core.dbLayer.dbTimeout,
                    ChatId(Platform.Discord, channelId),
                    sBotInfo.botId,
                    join.memberCount
                  )
                ),
              press =>
                apiReady.get.flatMap(api =>
                  new DiscordButtonHandler[F](
                    api = api,
                    lists = lists,
                    navigate = (action, request) =>
                      PagedListNavigation.reply[F](
                        action = action,
                        msg = request,
                        allCommandRepliesData = engine.allCommandRepliesData,
                        backgroundJobManager = backgroundJobManager,
                        effectfulCallbacks = commandEffectfulCallback,
                        dbLayer = core.dbLayer,
                        ttl = effectiveSBotConfig.messageTimeToLive,
                        capabilities = capabilities
                      ),
                    replies = (replyValue, request) => adapter.send(replyValue, request, replyToMessage = false),
                    flowPrompts = flowPrompts,
                    onFlowAction =
                      (msg, action, requester) => engine.onSubscriptionFlowAction(msg, action, requester)(using adapter)
                  ).decide(press)
                )
            )
            DiscordGateway.resource[F](token, gatewayConfig, List(listener)).use { client =>
              apiReady.complete(new JdaDiscordApi[F](client.jda)) >>
                DiscordSlashCommands
                  .register[F](client.jda, DiscordSlashCommands.specs(engine.allCommandRepliesData))
                  .handleErrorWith(e => log.error(s"[DiscordBot] Error while registering the slash commands: $e")) >>
                notifier.info(sBotInfo, "started on Discord") >>
                client.closed
            }
          }
          adapter.run
        }
      } yield ()
    }
  }
}
