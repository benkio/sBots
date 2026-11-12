package com.benkio.integration.integrationmunit.chatcore.patterns

import cats.effect.IO
import cats.effect.Resource
import cats.syntax.all.*
import com.benkio.chatcore.mocks.NotifierMock.given
import com.benkio.chatcore.model.Message
import com.benkio.chatcore.model.SBotInfo.SBotId
import com.benkio.chatcore.patterns.CommandPatterns.SubscribeUnsubscribeCommand
import com.benkio.chatcore.repository.db.DBSubscriptionData
import com.benkio.chatcore.TestIds
import com.benkio.chattelegramadapter.mocks.ApiMock.given
import com.benkio.chattelegramadapter.SBot
import com.benkio.chattelegramadapter.TelegramBackgroundJobManager
import com.benkio.integration.DBFixture
import com.benkio.integration.Logger.given
import com.benkio.RichardPHJBensonBot.RichardPHJBensonBot
import munit.CatsEffectSuite

import java.util.UUID

class ITSubscriptionsCommandSpec extends CatsEffectSuite with DBFixture {

  val sBotConfig         = SBot.buildSBotConfig(RichardPHJBensonBot.sBotInfo)
  val testSubscriptionId = "B674CCE0-9684-4D31-8CC7-9E2A41EA0878"
  val botName            = sBotConfig.sBotInfo.botName
  val botId              = sBotConfig.sBotInfo.botId
  val chatIdValue        = 0L

  val testSubscriptions: List[DBSubscriptionData] = List(
    DBSubscriptionData(
      id = testSubscriptionId,
      platform = "telegram",
      chat_id = chatIdValue.toString,
      bot_id = botId.value,
      cron = "* * * ? * *",
      subscribed_at = "1746195008"
    ),
    DBSubscriptionData(
      id = "3c301111-ca9c-4909-8281-c9d171e9bf7d",
      platform = "telegram",
      chat_id = chatIdValue.toString,
      bot_id = "anotherbot",
      cron = "* * * ? * *",
      subscribed_at = "1746195008"
    ),
    DBSubscriptionData(
      id = "0be5a0f9-ce63-4a78-a972-5f40758f2275",
      platform = "telegram",
      chat_id = "1",
      bot_id = botId.value,
      cron = "* * * ? * *",
      subscribed_at = "1746195008"
    ),
    DBSubscriptionData(
      id = "94dc19a7-fde7-49eb-af02-1de51c44b8b1",
      platform = "telegram",
      chat_id = "1",
      bot_id = "anotherbot",
      cron = "* * * ? * *",
      subscribed_at = "1746195008"
    )
  )

  val msg: Message = Message(messageId = None, date = 0, chatId = TestIds.chatId(chatIdValue), chatType = "private")

  databaseFixture.test(
    "Subscriptions Command should return all the subscription by bot and chat"
  ) { fixture =>
    val resourceAssert = for {
      dbLayer    <- fixture.resourceDBLayer
      repository <- fixture.repositoryResource
      dbSubscription = dbLayer.dbSubscription
      _ <- Resource.eval(
        testSubscriptions.traverse(testSubscription => dbSubscription.insertSubscription(testSubscription))
      )
      backgroundJobManager <- Resource.eval(
        TelegramBackgroundJobManager(
          repository = repository,
          dbLayer = dbLayer,
          sBotInfo = sBotConfig.sBotInfo,
          ttl = sBotConfig.messageTimeToLive
        )
      )
      subscriptionsFromCommandResult <- Resource.eval(
        SubscribeUnsubscribeCommand.subscriptionsCommandLogic(
          dbSubscription = dbSubscription,
          backgroundJobManager = backgroundJobManager,
          sBotInfo = sBotConfig.sBotInfo,
          m = msg
        )
      )
      _ <- Resource.eval(
        testSubscriptions.traverse(testSubscription =>
          dbSubscription.deleteSubscription(UUID.fromString(testSubscription.id))
        )
      )
    } yield {
      val text = subscriptionsFromCommandResult.show
      assert(text.startsWith("Subscriptions of this chat (1), times are server time:"), text)
      assert(text.contains("| Type | Query | Next trigger | Cron | ID |"), text)
      // a random show every second: the next trigger is a time, the subscription is scheduled
      assert(
        text.matches(
          "(?s).*\\| show \\| _random_ \\| \\d{4}-\\d{2}-\\d{2} \\d{2}:\\d{2} \\| `\\* \\* \\* \\? \\* \\*` \\| `b674cce0-9684-4d31-8cc7-9e2a41ea0878` \\|.*"
        ),
        text
      )
      assert(!text.contains("not scheduled"), text)
    }
    resourceAssert.use_
  }

  databaseFixture.test(
    "Subscriptions Command should show the type and the target of the subscriptions"
  ) { fixture =>
    val withTarget = DBSubscriptionData(
      id = "5c1b3a62-7d3c-4a44-9b0e-2f0e3a6e1c11",
      platform = "telegram",
      chat_id = chatIdValue.toString,
      bot_id = botId.value,
      subscription_type = "show",
      target = Some("pino scotto"),
      cron = "0 0 0 1 1 ?",
      subscribed_at = "1746195008"
    )
    val resourceAssert = for {
      dbLayer              <- fixture.resourceDBLayer
      repository           <- fixture.repositoryResource
      _                    <- Resource.eval(dbLayer.dbSubscription.insertSubscription(withTarget))
      backgroundJobManager <- Resource.eval(
        TelegramBackgroundJobManager(
          repository = repository,
          dbLayer = dbLayer,
          sBotInfo = sBotConfig.sBotInfo,
          ttl = sBotConfig.messageTimeToLive
        )
      )
      result <- Resource.eval(
        SubscribeUnsubscribeCommand.subscriptionsCommandLogic(
          dbSubscription = dbLayer.dbSubscription,
          backgroundJobManager = backgroundJobManager,
          sBotInfo = sBotConfig.sBotInfo,
          m = msg
        )
      )
    } yield {
      val text = result.show
      assert(text.contains("Subscriptions of this chat (1)"), text)
      assert(text.contains("| show | pino scotto | "), text)
      // the next 1st of January at midnight
      assert(text.contains("-01-01 00:00 | `0 0 0 1 1 ?` | `5c1b3a62-7d3c-4a44-9b0e-2f0e3a6e1c11` |"), text)
    }
    resourceAssert.use_
  }
}
