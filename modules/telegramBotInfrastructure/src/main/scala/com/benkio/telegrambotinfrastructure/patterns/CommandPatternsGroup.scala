package com.benkio.telegrambotinfrastructure.patterns

import com.benkio.telegrambotinfrastructure.model.SBotInfo

import com.benkio.telegrambotinfrastructure.model.reply.ReplyBundleCommand

import com.benkio.telegrambotinfrastructure.patterns.CommandPatterns.*



import org.http4s.Uri

object CommandPatternsGroup {

  object ShowGroup {

    def group(
        sBotInfo: SBotInfo
    ): List[ReplyBundleCommand] =
      List(
        SearchShowCommand.searchShowReplyBundleCommand(
          sBotInfo = sBotInfo
        ),
        SubscribeUnsubscribeCommand.subscribeReplyBundleCommand(sBotInfo = sBotInfo),
        SubscribeUnsubscribeCommand.unsubscribeReplyBundleCommand(sBotInfo = sBotInfo),
        SubscribeUnsubscribeCommand.subscriptionsReplyBundleCommand(sBotInfo = sBotInfo)
      )
  }

  object TriggerGroup {
    def group(
      triggerFileUri: Uri,
      sBotInfo: SBotInfo
    ): List[ReplyBundleCommand] =
      List(
        TriggerListCommand.triggerListReplyBundleCommand(
          triggerFileUri = triggerFileUri),
        TriggerSearchCommand
          .triggerSearchReplyBundleCommand(sBotInfo = sBotInfo),
        StatisticsCommands.topTwentyReplyBundleCommand(sBotInfo = sBotInfo),
        TimeoutCommand.timeoutReplyBundleCommand(sBotInfo = sBotInfo)
      )
  }
}
