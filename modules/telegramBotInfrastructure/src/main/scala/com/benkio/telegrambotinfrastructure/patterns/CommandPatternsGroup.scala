package com.benkio.telegrambotinfrastructure.patterns

import cats.effect.Async
import com.benkio.telegrambotinfrastructure.model.reply.ReplyBundleCommand
import com.benkio.telegrambotinfrastructure.model.reply.ReplyBundleMessage
import com.benkio.telegrambotinfrastructure.model.SBotId
import com.benkio.telegrambotinfrastructure.model.SBotName
import com.benkio.telegrambotinfrastructure.patterns.CommandPatterns.*
import com.benkio.telegrambotinfrastructure.repository.db.*
import com.benkio.telegrambotinfrastructure.BackgroundJobManager
import log.effect.LogWriter
import org.http4s.Uri

object CommandPatternsGroup {

  object ShowGroup {

    def group[F[_]: Async](
        dbShow: DBShow[F],
        dbSubscription: DBSubscription[F],
        backgroundJobManager: BackgroundJobManager[F],
        botName: SBotName,
        botId: SBotId
    )(using log: LogWriter[F]): List[ReplyBundleCommand[F]] =
      List(
        SearchShowCommand.searchShowReplyBundleCommand(
          dbShow = dbShow,
          botId = botId,
          botName = botName
        ),
        SubscribeUnsubscribeCommand.subscribeReplyBundleCommand(backgroundJobManager, botName = botName, botId = botId),
        SubscribeUnsubscribeCommand.unsubscribeReplyBundleCommand(backgroundJobManager, botName = botName),
        SubscribeUnsubscribeCommand.subscriptionsReplyBundleCommand(dbSubscription, backgroundJobManager, botId)
      )
  }

  object TriggerGroup {
    def group[F[_]: Async](
        triggerFileUri: Uri,
        botId: SBotId,
        botName: SBotName,
        ignoreMessagePrefix: Option[String],
        messageRepliesData: List[ReplyBundleMessage[F]],
        dbMedia: DBMedia[F],
        dbTimeout: DBTimeout[F]
    )(using log: LogWriter[F]): List[ReplyBundleCommand[F]] =
      List(
        TriggerListCommand.triggerListReplyBundleCommand(triggerFileUri),
        TriggerSearchCommand
          .triggerSearchReplyBundleCommand(botName = botName, ignoreMessagePrefix, messageRepliesData),
        StatisticsCommands.topTwentyReplyBundleCommand(botId = botId, dbMedia),
        TimeoutCommand.timeoutReplyBundleCommand(botName = botName, botId = botId, dbTimeout = dbTimeout)
      )
  }
}
