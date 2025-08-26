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

class ITSubscribeCommandSpec extends CatsEffectSuite with DBFixture {

  val testSubscriptionId: SubscriptionId = SubscriptionId(UUID.fromString("B674CCE0-9684-4D31-8CC7-9E2A41EA0878"))
  val botName                            = RichardPHJBensonBot.botName
  val chatIdValue                        = 0L
  val chatId                             = ChatId(chatIdValue)

  val testSubscription: Subscription = Subscription(
    id = testSubscriptionId,
    chatId = chatId,
    botName = botName,
    cron = Cron.unsafeParse("* * * ? * *"),
    subscribedAt = Instant.now().truncatedTo(ChronoUnit.SECONDS)
  )

  val msg: Message = Message(
    messageId = 0,
    date = 0,
    chat = Chat(id = chatIdValue, `type` = "private")
  )

  databaseFixture.test(
    "Subscribe Command should add a new subscription"
  ) { fixture =>
    val result = for {
      dbLayer              <- fixture.resourceDBLayer
      repository       <- fixture.repositoryResource
      backgroundJobManager <- Resource.eval(
        BackgroundJobManager(
          dbSubscription = dbLayer.dbSubscription,
          dbShow = dbLayer.dbShow,
          repository = repository,
          botName = botName
        )
      )
      reply <- Resource.eval(
        SubscribeUnsubscribeCommand
          .subscribeCommandLogic[IO](
            testSubscription.cron.toString,
            backgroundJobManager,
            msg,
            botName
          )
      )
      subscriptionDatas <- Resource.eval(dbLayer.dbSubscription.getSubscriptions(botName))
      subscriptions     <- Resource.eval(
        subscriptionDatas.traverse(subscriptionData => IO.fromEither(Subscription(subscriptionData)))
      )
    } yield {
      assert(subscriptions.length == 1)
      assert(reply.length == 1)
      val testCheck = reply.zip(subscriptions).map { case (r, s) =>
        val result = r.startsWith("Subscription successfully scheduled. Next occurrence of subscription is ") &&
          r.endsWith(s"Refer to this subscription with the ID: ${s.id.value.toString}")
        if !result then println(s"[ITUnsubscribeCommandSpec:78:57]] Failed test with $r and $s") else ()
        result
      }
      assert(testCheck.forall(_ == true))
    }
    result.use_
  }
}
