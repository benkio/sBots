package com.benkio.telegrambotinfrastructure.patterns


import com.benkio.telegrambotinfrastructure.model.reply.ReplyBundleCommand

import com.benkio.telegrambotinfrastructure.model.SBotInfo.SBotId

import com.benkio.telegrambotinfrastructure.patterns.CommandPatterns.*



import org.http4s.Uri

object CommandPatternsGroup {

  object ShowGroup {

    def group(
        botId: SBotId
    ): List[ReplyBundleCommand] =
      List(
        SearchShowCommand.searchShowReplyBundleCommand(
          botId = botId
        ),
        SubscribeUnsubscribeCommand.subscribeReplyBundleCommand(botId = botId),
        SubscribeUnsubscribeCommand.unsubscribeReplyBundleCommand(botId = botId),
        SubscribeUnsubscribeCommand.subscriptionsReplyBundleCommand(botId = botId)
      )
  }

  object TriggerGroup {
    def group(
      triggerFileUri: Uri,
      botId: SBotId
    ): List[ReplyBundleCommand] =
      List(
        TriggerListCommand.triggerListReplyBundleCommand(triggerFileUri, botId = botId),
        TriggerSearchCommand
          .triggerSearchReplyBundleCommand(botId = botId),
        StatisticsCommands.topTwentyReplyBundleCommand(botId = botId),
        TimeoutCommand.timeoutReplyBundleCommand(botId = botId)
      )
  }
}
