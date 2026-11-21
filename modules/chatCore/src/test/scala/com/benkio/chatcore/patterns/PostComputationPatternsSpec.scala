package com.benkio.chatcore.patterns

import cats.effect.kernel.Ref
import cats.effect.IO
import com.benkio.chatcore.mocks.DBLayerMock
import com.benkio.chatcore.mocks.NotifierMock
import com.benkio.chatcore.model.ChatId
import com.benkio.chatcore.model.Message
import com.benkio.chatcore.model.SBotInfo
import com.benkio.chatcore.model.SBotInfo.SBotId
import com.benkio.chatcore.repository.db.DBTimeout
import com.benkio.chatcore.repository.db.DBTimeoutData
import com.benkio.chatcore.TestIds
import munit.CatsEffectSuite

class PostComputationPatternsSpec extends CatsEffectSuite {

  val sBotId: SBotId     = SBotId("testbot")
  val sBotInfo: SBotInfo = SBotInfo(sBotId, SBotInfo.SBotName("Test Bot"))
  val message: Message   = Message(messageId = None, date = 0L, chatId = TestIds.chatId(123L), chatType = "private")
  val existingTimeout    = DBTimeoutData(
    platform = "telegram",
    chat_id = "123",
    bot_id = sBotId.value,
    timeout_value = "0",
    last_interaction = "1"
  )

  test("timeoutPostComputation should update last interaction for matching timeout row") {
    val dbLayer         = DBLayerMock.mock(botId = sBotId, timeouts = List(existingTimeout))
    val postComputation =
      PostComputationPatterns.timeoutPostComputation[IO](dbLayer.dbTimeout, sBotInfo, NotifierMock.noop)

    val check = for {
      before <- dbLayer.dbTimeout.getOrDefault(message.chatId, sBotId).map(_.last_interaction.toLong)
      _      <- postComputation(message)
      after  <- dbLayer.dbTimeout.getOrDefault(message.chatId, sBotId).map(_.last_interaction.toLong)
    } yield after > before

    assertIO(check, true)
  }

  test("timeoutPostComputation should notify the operators when bot id does not match the DB layer bot id") {
    val dbLayer        = DBLayerMock.mock(botId = sBotId, timeouts = List(existingTimeout))
    val anotherBotInfo = SBotInfo(SBotId("anotherbot"), SBotInfo.SBotName("Another Bot"))
    for {
      notifier <- NotifierMock.recording
      postComputation = PostComputationPatterns.timeoutPostComputation[IO](dbLayer.dbTimeout, anotherBotInfo, notifier)
      _      <- postComputation(message)
      errors <- notifier.errors.get
    } yield assertEquals(errors.length, 1)
  }

  test("timeoutPostComputation should leave unrelated timeout rows unchanged") {
    val unrelatedTimeout = DBTimeoutData(
      platform = "telegram",
      chat_id = "124",
      bot_id = sBotId.value,
      timeout_value = "0",
      last_interaction = "7"
    )
    val dbLayer         = DBLayerMock.mock(botId = sBotId, timeouts = List(existingTimeout, unrelatedTimeout))
    val postComputation =
      PostComputationPatterns.timeoutPostComputation[IO](dbLayer.dbTimeout, sBotInfo, NotifierMock.noop)

    val check = for {
      beforeUnrelated <- dbLayer.dbTimeout.getOrDefault(TestIds.chatId(124L), sBotId).map(_.last_interaction.toLong)
      _               <- postComputation(message.copy(chatId = TestIds.chatId(999L)))
      afterUnrelated  <- dbLayer.dbTimeout.getOrDefault(TestIds.chatId(124L), sBotId).map(_.last_interaction.toLong)
    } yield beforeUnrelated == afterUnrelated

    assertIO(check, true)
  }

  test("timeoutPostComputation should notify without writing a timeout row when the update fails") {
    val failure = new RuntimeException("db write failed")
    for {
      notifier <- NotifierMock.recording
      writes   <- Ref.of[IO, List[DBTimeoutData]](List.empty)
      failing = new DBTimeout[IO] {
        override def getOrDefault(chatId: ChatId, botId: SBotId): IO[DBTimeoutData] = IO.pure(existingTimeout)
        override def setTimeout(timeout: DBTimeoutData): IO[Unit]                   = writes.update(_ :+ timeout)
        override def removeTimeout(chatId: ChatId, botId: SBotId): IO[Unit]         = IO.unit
        override def logLastInteraction(chatId: ChatId, botId: SBotId): IO[Unit]    = IO.raiseError(failure)
      }
      postComputation = PostComputationPatterns.timeoutPostComputation[IO](failing, sBotInfo, notifier)
      _        <- postComputation(message)
      errors   <- notifier.errors.get
      inserted <- writes.get
      after    <- failing.getOrDefault(message.chatId, sBotId)
    } yield {
      assertEquals(errors.map(_._1), List(sBotInfo))
      assertEquals(errors.map(_._2), List("logging the last interaction failed (chatId=123)"))
      assertEquals(errors.map(_._3), List(failure))
      assertEquals(inserted, Nil)
      assertEquals(after, existingTimeout)
    }
  }
}
