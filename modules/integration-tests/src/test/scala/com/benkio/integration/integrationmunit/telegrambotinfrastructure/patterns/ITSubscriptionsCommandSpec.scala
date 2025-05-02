package com.benkio.integration.integrationmunit.telegrambotinfrastructure.patterns

import cats.effect.IO
import cats.effect.Resource
import cats.syntax.all.*
import com.benkio.integration.DBFixture
import com.benkio.richardphjbensonbot.RichardPHJBensonBot
import com.benkio.telegrambotinfrastructure.mocks.ApiMock.given
import com.benkio.telegrambotinfrastructure.model.ChatId
import com.benkio.telegrambotinfrastructure.model.Subscription
import com.benkio.telegrambotinfrastructure.model.SubscriptionId
import com.benkio.telegrambotinfrastructure.patterns.CommandPatterns.SubscribeUnsubscribeCommand
import com.benkio.telegrambotinfrastructure.BackgroundJobManager
import cron4s.Cron
import munit.CatsEffectSuite
import telegramium.bots.Chat
import telegramium.bots.Message

import java.time.temporal.ChronoUnit
import java.time.Instant
import java.util.UUID

class ITSubscriptionsCommandSpec extends CatsEffectSuite with DBFixture {

  val testSubscriptionId: SubscriptionId = SubscriptionId(UUID.fromString("B674CCE0-9684-4D31-8CC7-9E2A41EA0878"))
  val botName                            = RichardPHJBensonBot.botName
  val chatIdValue                        = 0L
  val chatId                             = ChatId(chatIdValue)

  val testSubscription: DBSubscriptionData = DBSubscriptionData(
    id = testSubscriptionId,
    chat_id = chatId,
    bot_name = botName,
    cron = "0 0 0 12 4 *",
    subscribed_at = "2022-11-06T19:54:46Z"
  )

  val msg: Message = Message(
    messageId = 0,
    date = 0,
    chat = Chat(id = chatIdValue, `type` = "private")
  )

  databaseFixture.test(
    "Subscriptions Command should return all the subscription by bot"
  ) { fixture =>
    val resourceAssert = for {
      dbSubscription <- fixture.resourceDBLayer.map(_.dbSubscription)
      _              <- Resource.eval(dbSubscription.insertSubscription(testSubscription))
      backgroundJobManager <- Resource.eval(
        BackgroundJobManager(
          dbSubscription = dbLayer.dbSubscription,
          dbShow = dbLayer.dbShow,
          resourceAccess = resourceAccess,
          botName = botName
        )
      )
      subscriptions <- Resource.eval(dbSubscription.getSubscriptions(botName))
      subscriptionsFromCommand <- subscriptionsCommandLogic(dbSubscription, backgroundJobManager, botName, msg)
      } yield assertEquals(true, false)
  }

}
