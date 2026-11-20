package com.benkio.chattelegramadapter

import cats.*
import cats.data.OptionT
import cats.effect.*
import cats.syntax.all.*
import com.benkio.chatcore.config.SBotConfig
import com.benkio.chatcore.messagefiltering.*
import com.benkio.chatcore.model.reply.ReplyBundleCommand
import com.benkio.chatcore.model.reply.ReplyBundleMessage
import com.benkio.chatcore.model.Message as ModelMessage
import com.benkio.chatcore.patterns.PostComputationPatterns
import com.benkio.chatcore.repository.db.DBLayer
import com.benkio.chatcore.repository.Repository
import com.benkio.chatcore.AutoTimeout
import com.benkio.chatcore.BackgroundJobManager
import com.benkio.chatcore.BotEngine
import com.benkio.chatcore.EffectfulCallback
import com.benkio.chatcore.Notifier
import com.benkio.chatcore.PlatformAdapter
import com.benkio.chattelegramadapter.conversions.MessageConversions.*
import com.benkio.chattelegramadapter.http.telegramreply.callbackreply.TelegramCallbackReply
import com.benkio.chattelegramadapter.http.TelegramChatInfo
import com.benkio.chattelegramadapter.initialization.BotSetup
import com.benkio.chattelegramadapter.model.TelegramCallbackData
import com.benkio.chattelegramadapter.model.TelegramToken.botId
import log.effect.LogWriter
import telegramium.bots.high.*
import telegramium.bots.high.implicits.methodOps
import telegramium.bots.CallbackQuery
import telegramium.bots.InputPartFile
import telegramium.bots.Message as TMessage

import java.nio.file.Path

abstract class ISBotPolling[F[_]: Parallel: Api](
    override val sBotSetup: BotSetup[F]
)(using Async[F], LogWriter[F])
    extends LongPollBot[F](summon[Api[F]])
    with ISBot[F] {
  protected override def api: Api[F]                          = summon[Api[F]]
  protected override def startUpdates: F[Unit]                = start()
  override def onMessage(msg: TMessage): F[Unit]              = onMessageLogic(msg.toModel(sBotSetup.token.botId))
  override def onCallbackQuery(query: CallbackQuery): F[Unit] = onCallbackLogic(query)
}

abstract class ISBotWebhook[F[_]: Api](
    override val sBotSetup: BotSetup[F],
    webhookCertificate: Option[Path] = None
)(using Async[F], LogWriter[F])
    extends WebhookBot[F](
      summon[Api[F]],
      sBotSetup.webhookUri.renderString,
      sBotSetup.webhookPath.renderString,
      certificate = webhookCertificate.map(p => InputPartFile(p.toFile))
    )
    with ISBot[F] {
  protected override def api: Api[F] = summon[Api[F]]

  /** The updates arrive on the HTTP server that composes the webhook bots ([[webhook.TelegramWebhookServer]]), which is
    * what registers the webhook: the bot has no connection of its own to start, it runs until it is cancelled.
    */
  protected override def startUpdates: F[Unit]                = Async[F].never
  override def onMessage(msg: TMessage): F[Unit]              = onMessageLogic(msg.toModel(sBotSetup.token.botId))
  override def onCallbackQuery(query: CallbackQuery): F[Unit] = onCallbackLogic(query)
}

trait ISBot[F[_]: Async: LogWriter] {

  val sBotSetup: BotSetup[F]

  // Configuration values & functions (from BotSetup) ///////////////////////////////////
  def repository: Repository[F]                                                  = sBotSetup.repository
  def sBotConfig: SBotConfig                                                     = sBotSetup.sBotConfig
  def dbLayer: DBLayer[F]                                                        = sBotSetup.dbLayer
  def backgroundJobManager: BackgroundJobManager[F]                              = sBotSetup.backgroundJobManager
  def filteringMatchesMessages: (ReplyBundleMessage, ModelMessage) => F[Boolean] =
    FilteringTimeout.filter(dbLayer, sBotConfig.sBotInfo.botId)
  def postComputation: ModelMessage => F[Unit] =
    PostComputationPatterns.timeoutPostComputation(dbTimeout = dbLayer.dbTimeout, sBotId = sBotConfig.sBotInfo.botId)

  val commandEffectfulCallback: Map[String, EffectfulCallback[F]] = Map.empty

  // The logic of the bot is platform agnostic: this is only the Telegram shell around it ////////////////////////

  lazy val engine: BotEngine[F] = new BotEngine[F](
    sBotConfig = sBotConfig,
    dbLayer = dbLayer,
    filteringMatchesMessages = filteringMatchesMessages,
    postComputation = postComputation,
    notifier = sBotSetup.notifier
  )

  def messageRepliesData: List[ReplyBundleMessage]    = engine.messageRepliesData
  def commandRepliesData: List[ReplyBundleCommand]    = engine.commandRepliesData
  def allCommandRepliesData: List[ReplyBundleCommand] = engine.allCommandRepliesData

  /** The Telegram API of the bot. */
  protected def api: Api[F]

  /** How Telegram delivers the updates: the long polling, or the registration of the webhook. */
  protected def startUpdates: F[Unit]

  lazy val platformAdapter: PlatformAdapter[F] =
    new TelegramPlatformAdapter[F](
      repository = repository,
      backgroundJobManager = backgroundJobManager,
      effectfulCallbacks = commandEffectfulCallback,
      dbLayer = dbLayer,
      ttl = sBotConfig.messageTimeToLive,
      startUpdates = startUpdates
    )(using summon[Async[F]], summon[LogWriter[F]], api, sBotSetup.notifier)

  def messageLogic(msg: ModelMessage): F[Unit] =
    engine.messageLogic(msg)(using platformAdapter)

  def commandLogic(msg: ModelMessage): F[Unit] =
    engine.commandLogic(msg)(using platformAdapter)

  /** When the bot is added to a group the welcome goes first, then the chat gets the timeout from its member count. A
    * failure to read the count is logged and leaves the chat without timeout.
    */
  def onMessageLogic(msg: ModelMessage): F[Unit] =
    engine.onMessage(msg)(using platformAdapter) *> autoTimeoutOnJoin(msg)

  private def autoTimeoutOnJoin(msg: ModelMessage): F[Unit] =
    if msg.botAdded && msg.chatType != "private" then TelegramChatInfo
      .memberCount(msg.chatId)(using api)
      .flatMap(count => AutoTimeout.set(dbLayer.dbTimeout, msg.chatId, sBotConfig.sBotInfo.botId, count))
      .handleErrorWith(e => LogWriter.error(s"[AutoTimeout] Error while setting the timeout of ${msg.chatId}: $e"))
    else Applicative[F].unit

  // Telegram only: the callbacks of the inline keyboards ////////////////////////////////////////////////////////

  def onCallbackLogic(query: CallbackQuery): F[Unit] = {
    given Api[F]      = api
    given Notifier[F] = sBotSetup.notifier
    (for {
      callbackDataString <- OptionT.fromOption(query.data)
      callbackData = TelegramCallbackData.decode(callbackDataString)
      msg <- OptionT.fromOption(query.message)
      _   <- OptionT.liftF(Methods.answerCallbackQuery(callbackQueryId = query.id).exec)
      _   <- OptionT.liftF(
        TelegramCallbackReply.reply(
          msg = msg,
          callbackData = callbackData,
          repository = repository,
          allCommandRepliesData = allCommandRepliesData,
          backgroundJobManager = backgroundJobManager,
          effectfulCallbacks = commandEffectfulCallback,
          dbLayer = dbLayer,
          ttl = sBotConfig.messageTimeToLive
        )
      )
    } yield ()).value.void
  }
}
