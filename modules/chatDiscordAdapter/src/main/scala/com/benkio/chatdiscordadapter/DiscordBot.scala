package com.benkio.chatdiscordadapter

import cats.effect.std.Dispatcher
import cats.effect.std.Random
import cats.effect.Async
import cats.effect.Deferred
import cats.syntax.all.*
import com.benkio.chatcore.config.SBotConfig
import com.benkio.chatcore.initialization.BotCoreSetup
import com.benkio.chatcore.model.reply.ReplyValue
import com.benkio.chatcore.model.Message
import com.benkio.chatcore.model.SBotInfo
import com.benkio.chatcore.repository.ResourcesRepository
import com.benkio.chatcore.BotEngine
import com.benkio.chatcore.CronBackgroundJobManager
import com.benkio.chatcore.Notifier
import com.benkio.chatdiscordadapter.gateway.DiscordGateway
import com.benkio.chatdiscordadapter.gateway.DiscordGatewayConfig
import com.benkio.chatdiscordadapter.gateway.DiscordToken
import com.benkio.chatdiscordadapter.inbound.DiscordInboundListener
import com.benkio.chatdiscordadapter.inbound.DiscordSlashCommands
import com.benkio.chatdiscordadapter.model.DiscordCapabilities
import com.benkio.chatdiscordadapter.outbound.DiscordApi
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
      commandEffectfulCallback: Map[String, Message => F[ReplyValue]],
      gatewayConfig: DiscordGatewayConfig,
      notifier: Notifier[F]
  )(using log: LogWriter[F]): F[Unit] = {
    val sBotConfig    = SBotConfig.fromSBotInfo(sBotInfo)
    given Notifier[F] = notifier
    (for {
      token      <- DiscordToken.read[F](sBotInfo, ResourcesRepository.fromResources[F]())
      core       <- BotCoreSetup[F](httpClient, sBotConfig)
      dispatcher <- Dispatcher.parallel[F]
    } yield (token, core, dispatcher)).use { (token, core, dispatcher) =>
      val capabilities = DiscordCapabilities.value
      val repository   = core.repository.usableFor(capabilities)
      for {
        apiReady <- Deferred[F, DiscordApi[F]]
        random   <- Random.scalaUtilRandom[F]
        engine = BotEngine[F](sBotConfig, core.dbLayer)
        // the subscriptions send to a chat without a message to reply to, once the connection is up
        sendToChat = (chatId: com.benkio.chatcore.model.ChatId, replyValue: ReplyValue, replyToMessage: Boolean) =>
          apiReady.get.flatMap(api =>
            DiscordReplies[F](api, repository, core.dbLayer, capabilities)
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
            ttl = sBotConfig.messageTimeToLive,
            capabilities = capabilities,
            send = sendToChat
          )
        }
        _ <- {
          lazy val adapter: DiscordPlatformAdapter[F] = new DiscordPlatformAdapter[F](
            discordApi = apiReady.get,
            repository = core.repository,
            backgroundJobManager = backgroundJobManager,
            effectfulCallbacks = commandEffectfulCallback,
            dbLayer = core.dbLayer,
            ttl = sBotConfig.messageTimeToLive,
            startGateway = startGateway
          )
          // deferred: the listener needs the adapter, which needs this
          def startGateway: F[Unit] = Async[F].defer {
            val listener = new DiscordInboundListener[F](
              dispatcher,
              capabilities.commandSyntax,
              message => engine.onMessage(message)(using adapter)
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
