package com.benkio.integration.integrationmunit.telegrambotinfrastructure.patterns

import cats.effect.IO
import cats.effect.Resource
import cats.syntax.all.*
import com.benkio.integration.DBFixture
import com.benkio.richardphjbensonbot.RichardPHJBensonBot
import com.benkio.telegrambotinfrastructure.mocks.ApiMock.given
import com.benkio.telegrambotinfrastructure.model.SBotId
import com.benkio.telegrambotinfrastructure.patterns.CommandPatterns.SubscribeUnsubscribeCommand
import com.benkio.telegrambotinfrastructure.repository.db.DBSubscriptionData
import com.benkio.telegrambotinfrastructure.BackgroundJobManager
import munit.CatsEffectSuite
import telegramium.bots.Chat
import telegramium.bots.Message

import java.util.UUID

class ITSubscriptionsCommandSpec extends CatsEffectSuite with DBFixture {

  val testSubscriptionId = "B674CCE0-9684-4D31-8CC7-9E2A41EA0878"
  val botName            = RichardPHJBensonBot.botName
  val botId              = RichardPHJBensonBot.botId
  val chatIdValue        = 0L

  val testSubscriptions: List[DBSubscriptionData] = List(
    DBSubscriptionData(
      id = testSubscriptionId,
      chat_id = chatIdValue,
      bot_id = botId.value,
      cron = "* * * ? * *",
      subscribed_at = "1746195008"
    ),
    DBSubscriptionData(
      id = "3c301111-ca9c-4909-8281-c9d171e9bf7d",
      chat_id = chatIdValue,
      bot_id = "anotherbot",
      cron = "* * * ? * *",
      subscribed_at = "1746195008"
    ),
    DBSubscriptionData(
      id = "0be5a0f9-ce63-4a78-a972-5f40758f2275",
      chat_id = 1L,
      bot_id = botId.value,
      cron = "* * * ? * *",
      subscribed_at = "1746195008"
    ),
    DBSubscriptionData(
      id = "94dc19a7-fde7-49eb-af02-1de51c44b8b1",
      chat_id = 1L,
      bot_id = "anotherbot",
      cron = "* * * ? * *",
      subscribed_at = "1746195008"
    )
  )

  val msg: Message = Message(
    messageId = 0,
    date = 0,
    chat = Chat(id = chatIdValue, `type` = "private")
  )

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
        BackgroundJobManager(
          dbSubscription = dbLayer.dbSubscription,
          dbShow = dbLayer.dbShow,
          repository = repository,
          botId = botId
        )
      )
      subscriptionsFromCommandResult <- Resource.eval(
        SubscribeUnsubscribeCommand.subscriptionsCommandLogic(
          dbSubscription = dbSubscription,
          backgroundJobManager = backgroundJobManager,
          botId = botId,
          m = msg
        )
      )
      _ <- Resource.eval(
        testSubscriptions.traverse(testSubscription =>
          dbSubscription.deleteSubscription(UUID.fromString(testSubscription.id))
        )
      )
    } yield assertEquals(
      subscriptionsFromCommandResult,
      """There are 1 stored subscriptions for this chat:
        |Subscription Id: b674cce0-9684-4d31-8cc7-9e2a41ea0878 - cron value: * * * ? * *
        |There are 1/2 scheduled subscriptions for this chat:
        |Subscription Id: b674cce0-9684-4d31-8cc7-9e2a41ea0878 - chat id: 0""".stripMargin
    )
    resourceAssert.use_
  }

}
