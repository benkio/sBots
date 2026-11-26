package com.benkio.chatcore.repository.db

import cats.effect.Async
import cats.implicits.*
import com.benkio.chatcore.config.SBotConfig
import com.benkio.chatcore.llm.LlmSettings
import com.benkio.chatcore.llm.LlmWiring
import com.benkio.chatcore.model.reply.ReplyBundleCommand
import com.benkio.chatcore.model.reply.ReplyBundleMessage
import com.benkio.chatcore.model.SBotInfo
import com.benkio.chatcore.repository.JsonDataRepository
import com.benkio.chatcore.PagedListTextStore
import com.benkio.chatcore.SubscriptionDraftStore
import doobie.Transactor
import log.effect.LogWriter

import scala.concurrent.duration.FiniteDuration

final case class DBLayer[F[_]](
    dbTimeout: DBTimeout[F],
    dbMedia: DBMedia[F],
    dbSubscription: DBSubscription[F],
    dbShow: DBShow[F],
    dbProcessedMessage: DBProcessedMessage[F],
    dbSubscriptionDraft: DBSubscriptionDraft[F],
    dbSubscriptionFlowPrompt: DBSubscriptionFlowPrompt[F],
    dbPagedListText: DBPagedListText[F],
    llmSettings: LlmSettings[F],
    dbMessageReplies: DBMessageReplies = DBMessageReplies.empty,
    dbCommandReplies: DBCommandReplies = DBCommandReplies.empty
)

object DBLayer {

  /** The replies and the commands of the bot are loaded once, from its JSON files, and served from memory by the
    * `DBLayer`: to the background job manager and to the bot.
    */
  def loadReplies[F[_]: Async](
      dbLayer: DBLayer[F],
      jsonDataRepository: JsonDataRepository[F],
      sBotConfig: SBotConfig
  ): F[DBLayer[F]] = for {
    messageReplies <- jsonDataRepository.loadData[ReplyBundleMessage](sBotConfig.repliesJsonFilename)
    commandReplies <- jsonDataRepository.loadData[ReplyBundleCommand](sBotConfig.commandsJsonFilename)
  } yield dbLayer.copy(
    dbMessageReplies = DBMessageReplies(messageReplies),
    dbCommandReplies = DBCommandReplies(commandReplies)
  )

  def make[F[_]: Async](
      transactor: Transactor[F],
      botId: SBotInfo.SBotId,
      subscriptionDraftExpiry: FiniteDuration = SubscriptionDraftStore.defaultExpiry,
      subscriptionDraftMaxDrafts: Int = SubscriptionDraftStore.defaultMaxDrafts,
      pagedListTextModeExpiry: FiniteDuration = PagedListTextStore.defaultExpiry,
      pagedListTextModeMaxChats: Int = PagedListTextStore.defaultMaxChats,
      llmWiring: Option[LlmWiring[F]] = None
  )(using log: LogWriter[F]): F[DBLayer[F]] = {
    val wiring = llmWiring.getOrElse(LlmWiring.noop[F])
    for {
      dbMedia <- DBMedia[F](transactor)
      dbSubscription = DBSubscription[F](transactor)
      dbTimeout      = new DBTimeout.DBTimeoutImpl[F](
        transactor,
        log
      )
      dbShow = DBShow(transactor)
      dbProcessedMessage <- DBProcessedMessage[F](transactor)
    } yield DBLayer[F](
      dbTimeout = dbTimeout,
      dbSubscription = dbSubscription,
      dbMedia = dbMedia,
      dbShow = dbShow,
      dbProcessedMessage = dbProcessedMessage,
      llmSettings = wiring.settings(transactor, botId),
      dbSubscriptionDraft =
        DBSubscriptionDraft[F](transactor, botId, subscriptionDraftExpiry, subscriptionDraftMaxDrafts),
      dbSubscriptionFlowPrompt =
        DBSubscriptionFlowPrompt[F](transactor, botId, subscriptionDraftExpiry, subscriptionDraftMaxDrafts),
      dbPagedListText = DBPagedListText[F](transactor, botId, pagedListTextModeExpiry, pagedListTextModeMaxChats)
    )
  }
}
