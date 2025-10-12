package com.benkio.telegrambotinfrastructure.patterns

import cats.effect.Async
import com.benkio.telegrambotinfrastructure.model.reply.ReplyBundleCommand
import com.benkio.telegrambotinfrastructure.model.reply.ReplyBundleMessage
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
        botName: String,
        botId: String
    )(using log: LogWriter[F]): List[ReplyBundleCommand[F]] =
      List(
        SearchShowCommand.searchShowReplyBundleCommand(
          dbShow = dbShow,
          botId = botId,
          botName = botName
        ),
        SubscribeUnsubscribeCommand.subscribeReplyBundleCommand(backgroundJobManager, botId),
        SubscribeUnsubscribeCommand.unsubscribeReplyBundleCommand(backgroundJobManager, botId),
        SubscribeUnsubscribeCommand.subscriptionsReplyBundleCommand(dbSubscription, backgroundJobManager, botId)
      )
  }

  object TriggerGroup {
    def group[F[_]: Async](
        triggerFileUri: Uri,
        botId: String,
        ignoreMessagePrefix: Option[String],
        messageRepliesData: List[ReplyBundleMessage[F]],
        dbMedia: DBMedia[F],
        dbTimeout: DBTimeout[F]
    )(using log: LogWriter[F]): List[ReplyBundleCommand[F]] =
      List(
        TriggerListCommand.triggerListReplyBundleCommand(triggerFileUri),
        TriggerSearchCommand.triggerSearchReplyBundleCommand(botId, ignoreMessagePrefix, messageRepliesData),
        StatisticsCommands.topTwentyReplyBundleCommand(botId, dbMedia),
        TimeoutCommand.timeoutReplyBundleCommand(botId, dbTimeout)
      )
  }
}
