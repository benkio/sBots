package com.benkio.telegrambotinfrastructure.patterns

import com.benkio.telegrambotinfrastructure.model.reply.ReplyBundleMessage
import com.benkio.telegrambotinfrastructure.patterns.CommandPatterns.*
import com.benkio.telegrambotinfrastructure.model.reply.ReplyBundleCommand
import cats.effect.Async





import com.benkio.telegrambotinfrastructure.resources.db.*
import com.benkio.telegrambotinfrastructure.BackgroundJobManager



import log.effect.LogWriter
import org.http4s.Uri


object CommandPatternsGroup {

  object ShowGroup {

    def group[F[_]: Async](
      dbShow: DBShow[F],
      dbSubscription: DBSubscription[F],
      backgroundJobManager: BackgroundJobManager[F],
      botName: String
    )(using log: LogWriter[F]): List[ReplyBundleCommand[F]] =
      List(
        SearchShowCommand.searchShowReplyBundleCommand(dbShow, botName),
        SubscribeUnsubscribeCommand.subscribeReplyBundleCommand(backgroundJobManager, botName),
        SubscribeUnsubscribeCommand.unsubscribeReplyBundleCommand(backgroundJobManager, botName),
        SubscribeUnsubscribeCommand.subscriptionsReplyBundleCommand(dbSubscription, backgroundJobManager, botName),

    )
  }

  object TriggerGroup {
    def group[F[_]: Async](
      triggerFileUri: Uri,
      botName: String,
      ignoreMessagePrefix: Option[String],
      messageRepliesData: List[ReplyBundleMessage[F]],
      botPrefix: String,
      dbMedia: DBMedia[F],
      dbTimeout: DBTimeout[F],
    )(using log: LogWriter[F]) : List[ReplyBundleCommand[F]] =
      List(
        TriggerListCommand.triggerListReplyBundleCommand(triggerFileUri),
        TriggerSearchCommand.triggerSearchReplyBundleCommand(botName, ignoreMessagePrefix, messageRepliesData),
        StatisticsCommands.topTwentyReplyBundleCommand(botPrefix, dbMedia),
        TimeoutCommand.timeoutReplyBundleCommand(botName, dbTimeout)
      )
  }
}
