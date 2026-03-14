package com.benkio.chatcore.patterns

import cats.effect.IO
import com.benkio.chatcore.mocks.DBLayerMock
import com.benkio.chatcore.model.ChatId
import com.benkio.chatcore.model.Message
import com.benkio.chatcore.model.SBotInfo.SBotId
import com.benkio.chatcore.repository.db.DBTimeoutData
import munit.CatsEffectSuite

class PostComputationPatternsSpec extends CatsEffectSuite {

  val sBotId: SBotId   = SBotId("testbot")
  val message: Message = Message(messageId = 0, date = 0L, chatId = ChatId(123L), chatType = "private")
  val existingTimeout  = DBTimeoutData(
    chat_id = 123L,
    bot_id = sBotId.value,
    timeout_value = "0",
    last_interaction = "1"
  )

  test("timeoutPostComputation should update last interaction for matching timeout row") {
    val dbLayer         = DBLayerMock.mock(botId = sBotId, timeouts = List(existingTimeout))
    val postComputation = PostComputationPatterns.timeoutPostComputation[IO](dbLayer.dbTimeout, sBotId)

    val check = for {
      before <- dbLayer.dbTimeout.getOrDefault(message.chatId.value, sBotId).map(_.last_interaction.toLong)
      _      <- postComputation(message)
      after  <- dbLayer.dbTimeout.getOrDefault(message.chatId.value, sBotId).map(_.last_interaction.toLong)
    } yield after > before

    assertIO(check, true)
  }

  test("timeoutPostComputation should fail when bot id does not match the DB layer bot id") {
    val dbLayer         = DBLayerMock.mock(botId = sBotId, timeouts = List(existingTimeout))
    val anotherBot      = SBotId("anotherbot")
    val postComputation = PostComputationPatterns.timeoutPostComputation[IO](dbLayer.dbTimeout, anotherBot)

    val check: IO[Boolean] = postComputation(message).attempt.map(_.isLeft)
    assertIO(check, true)
  }

  test("timeoutPostComputation should leave unrelated timeout rows unchanged") {
    val unrelatedTimeout = DBTimeoutData(
      chat_id = 124L,
      bot_id = sBotId.value,
      timeout_value = "0",
      last_interaction = "7"
    )
    val dbLayer         = DBLayerMock.mock(botId = sBotId, timeouts = List(existingTimeout, unrelatedTimeout))
    val postComputation = PostComputationPatterns.timeoutPostComputation[IO](dbLayer.dbTimeout, sBotId)

    val check = for {
      beforeUnrelated <- dbLayer.dbTimeout.getOrDefault(124L, sBotId).map(_.last_interaction.toLong)
      _               <- postComputation(message.copy(chatId = ChatId(999L)))
      afterUnrelated  <- dbLayer.dbTimeout.getOrDefault(124L, sBotId).map(_.last_interaction.toLong)
    } yield beforeUnrelated == afterUnrelated

    assertIO(check, true)
  }
}
