package com.benkio.chatircadapter

import cats.effect.std.Dispatcher
import cats.effect.std.Random
import cats.effect.Async
import cats.effect.Deferred
import cats.effect.Resource
import cats.syntax.all.*
import com.benkio.chatcore.config.SBotConfig
import com.benkio.chatcore.initialization.BotCoreSetup
import com.benkio.chatcore.model.reply.ReplyValue
import com.benkio.chatcore.model.ChatId
import com.benkio.chatcore.model.Message
import com.benkio.chatcore.model.SBotInfo
import com.benkio.chatcore.repository.ResourcesRepository
import com.benkio.chatcore.BotEngine
import com.benkio.chatcore.CronBackgroundJobManager
import com.benkio.chatcore.Notifier
import com.benkio.chatircadapter.connection.IrcConfig
import com.benkio.chatircadapter.connection.IrcConnection
import com.benkio.chatircadapter.connection.IrcCredentials
import com.benkio.chatircadapter.inbound.IrcInboundListener
import com.benkio.chatircadapter.inbound.RateLimiter
import com.benkio.chatircadapter.model.IrcCapabilities
import com.benkio.chatircadapter.outbound.IrcApi
import com.benkio.chatircadapter.outbound.IrcReplies
import com.benkio.chatircadapter.outbound.KittehIrcApi
import log.effect.LogWriter
import org.http4s.client.Client

/** Runs a bot on IRC: the engine, the IRC adapter, the subscriptions, and the connection to the server, until the
  * connection ends.
  */
object IrcBot {

  /** The bot with what it needs from the outside: the token document of the password of its account, the DB, the media
    * repository and the config of the bot are loaded here.
    */
  def run[F[_]: Async](
      httpClient: Client[F],
      sBotInfo: SBotInfo,
      commandEffectfulCallback: Map[String, Message => F[ReplyValue]],
      config: IrcConfig,
      notifier: Notifier[F]
  )(using log: LogWriter[F]): F[Unit] = {
    val sBotConfig = SBotConfig.fromSBotInfo(sBotInfo)
    (for {
      password <- IrcCredentials.password[F](sBotInfo, ResourcesRepository.fromResources[F]())
      core     <- BotCoreSetup[F](httpClient, sBotConfig)
    } yield (password, core)).use((password, core) =>
      runWith[F](
        core = core,
        sBotConfig = sBotConfig,
        password = password,
        commandEffectfulCallback = commandEffectfulCallback,
        config = config,
        notifier = notifier
      )
    )
  }

  /** The bot with its DB and repository already there. */
  def runWith[F[_]: Async](
      core: BotCoreSetup[F],
      sBotConfig: SBotConfig,
      password: Option[String],
      commandEffectfulCallback: Map[String, Message => F[ReplyValue]],
      config: IrcConfig,
      notifier: Notifier[F]
  )(using log: LogWriter[F]): F[Unit] = {
    given Notifier[F] = notifier
    val capabilities  = IrcCapabilities.value
    val repository    = core.repository.usableFor(capabilities)
    val nick          = IrcCredentials.nick(sBotConfig.sBotInfo)
    (for {
      dispatcher <- Dispatcher.parallel[F]
      limiter    <- Resource.eval(RateLimiter[F](config.rateLimitMessages, config.rateLimitWindow))
    } yield (dispatcher, limiter)).use((dispatcher, limiter) =>
      for {
        apiReady <- Deferred[F, IrcApi[F]]
        random   <- Random.scalaUtilRandom[F]
        engine = BotEngine[F](sBotConfig, core.dbLayer)
        // the subscriptions send to a chat without a message to reply to, once the connection is up
        sendToChat = (chatId: ChatId, replyValue: ReplyValue, replyToMessage: Boolean) =>
          apiReady.get.flatMap(api =>
            IrcReplies[F](api, repository, capabilities).send(
              replyValue,
              Message(messageId = None, date = 0L, chatId = chatId, chatType = "private"),
              replyToMessage
            )
          )
        backgroundJobManager <- {
          given Random[F] = random
          CronBackgroundJobManager[F](
            dbLayer = core.dbLayer,
            sBotInfo = sBotConfig.sBotInfo,
            ttl = sBotConfig.messageTimeToLive,
            capabilities = capabilities,
            send = sendToChat
          )
        }
        _ <- {
          lazy val adapter: IrcPlatformAdapter[F] = new IrcPlatformAdapter[F](
            ircApi = apiReady.get,
            repository = core.repository,
            backgroundJobManager = backgroundJobManager,
            effectfulCallbacks = commandEffectfulCallback,
            dbLayer = core.dbLayer,
            ttl = sBotConfig.messageTimeToLive,
            startConnection = startConnection
          )
          // deferred: the listener needs the adapter, which needs this
          def startConnection: F[Unit] = Async[F].defer {
            val listener =
              new IrcInboundListener[F](dispatcher, limiter, message => engine.onMessage(message)(using adapter))
            IrcConnection.resource[F](nick, password, config, List(listener)).use { connection =>
              apiReady.complete(new KittehIrcApi[F](connection.client)) >>
                notifier.info(sBotConfig.sBotInfo, s"started on IRC as $nick") >>
                connection.closed
            }
          }
          adapter.run
        }
      } yield ()
    )
  }
}
