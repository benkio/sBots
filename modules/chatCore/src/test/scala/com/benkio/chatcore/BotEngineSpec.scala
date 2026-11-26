package com.benkio.chatcore

import cats.effect.kernel.Ref
import cats.effect.IO
import cats.syntax.all.*
import com.benkio.chatcore.mocks.FakePlatformAdapter
import com.benkio.chatcore.mocks.NotifierMock
import com.benkio.chatcore.model.reply.Text
import com.benkio.chatcore.model.ChatId
import com.benkio.chatcore.model.MessageId
import com.benkio.chatcore.model.SBotInfo
import com.benkio.chatcore.repository.db.DBProcessedMessage
import munit.CatsEffectSuite

import scala.concurrent.duration.*

class BotEngineSpec extends CatsEffectSuite with BotEngineFixtures {

  test("the messages the platform delivers when it runs are handled by the engine and answered through the platform") {
    for {
      adapter <- FakePlatformAdapter(
        inbound = List(message("ciao"), message("/hi"), message("something else")),
        capabilities = slashCommands
      )
      engine = engineWith()
      _    <- adapter.onInbound(msg => engine.onMessage(msg)(using adapter))
      _    <- adapter.run
      sent <- adapter.sent.get
    } yield assertEquals(sent.map(_.replyValue), List(Text("hello"), Text("command reply")))
  }

  test("a platform that runs without an engine fails") {
    FakePlatformAdapter().flatMap(_.run).intercept[IllegalStateException]
  }

  test("the platform of the adapter is the one of its capabilities") {
    FakePlatformAdapter().map(adapter => assertEquals(adapter.platform, adapter.capabilities.platform))
  }

  test("a request that completes within the configured timeout is processed normally") {
    val config = sBotConfig.copy(requestProcessingTimeout = Some(2.seconds))
    for {
      adapter <- FakePlatformAdapter(capabilities = slashCommands)
      engine = engineWith(config = config)
      _    <- engine.onMessage(message("ciao"))(using adapter)
      sent <- adapter.sent.get
    } yield assertEquals(sent.map(_.replyValue), List(Text("hello")))
  }

  test("a request that exceeds the configured timeout is dropped without failing the engine") {
    val config = sBotConfig.copy(requestProcessingTimeout = Some(50.millis))
    for {
      adapter      <- FakePlatformAdapter(capabilities = slashCommands)
      postComputed <- Ref.of[IO, Boolean](false)
      engine = engineWith(
        config = config,
        postComputation = _ => IO.sleep(1.second) *> postComputed.set(true)
      )
      _        <- engine.onMessage(message("ciao"))(using adapter)
      computed <- postComputed.get
    } yield assert(!computed, "post computation should not complete after timeout")
  }

  test("a request that exceeds the configured timeout is notified once, with its context") {
    val config = sBotConfig.copy(requestProcessingTimeout = Some(50.millis))
    for {
      adapter   <- FakePlatformAdapter(capabilities = slashCommands)
      recording <- NotifierMock.recording
      engine = engineWith(config = config, postComputation = _ => IO.sleep(1.second), notifier = recording)
      _      <- engine.onMessage(message("ciao"))(using adapter)
      errors <- recording.errors.get
    } yield {
      assertEquals(errors.map(_._1), List(sBotInfo))
      assertEquals(errors.map(_._3), List(BotEngine.RequestTimedOut(50.millis)))
      assert(errors.head._2.contains("content=Some(ciao)"), errors.head._2)
    }
  }

  test("a request that completes in time notifies nothing") {
    val config = sBotConfig.copy(requestProcessingTimeout = Some(2.seconds))
    for {
      adapter   <- FakePlatformAdapter(capabilities = slashCommands)
      recording <- NotifierMock.recording
      _         <- engineWith(config = config, notifier = recording).onMessage(message("ciao"))(using adapter)
      errors    <- recording.errors.get
    } yield assertEquals(errors, List.empty)
  }

  test("a failing duplicate check is notified and the message is processed anyway") {
    val failing = new DBProcessedMessage[IO] {
      override def markIfNew(chatId: ChatId, botId: SBotInfo.SBotId, messageId: MessageId): IO[Boolean] =
        IO.raiseError(new RuntimeException("db down"))
      override def deleteOlderThan(epochSeconds: Long): IO[Int] = IO.pure(0)
    }
    for {
      adapter   <- FakePlatformAdapter(capabilities = slashCommands)
      recording <- NotifierMock.recording
      engine = engineWith(processedMessages = failing, notifier = recording)
      _      <- engine.onMessage(message("ciao", Some(MessageId("1"))))(using adapter)
      sent   <- adapter.sent.get
      errors <- recording.errors.get
    } yield {
      assertEquals(sent.map(_.replyValue), List(Text("hello")))
      assertEquals(errors.map(_._3.getMessage), List("db down"))
    }
  }

  test("the same message delivered twice is answered and post computed once") {
    for {
      adapter      <- FakePlatformAdapter(capabilities = slashCommands)
      postComputed <- Ref.of[IO, Int](0)
      engine = engineWith(postComputation = _ => postComputed.update(_ + 1))
      msg    = message("ciao", Some(MessageId("1")))
      _        <- engine.onMessage(msg)(using adapter)
      _        <- engine.onMessage(msg)(using adapter)
      sent     <- adapter.sent.get
      computed <- postComputed.get
    } yield {
      assertEquals(sent.map(_.replyValue), List(Text("hello")))
      assertEquals(computed, 1)
    }
  }

  test("the same message delivered concurrently is answered once") {
    for {
      adapter <- FakePlatformAdapter(capabilities = slashCommands)
      engine = engineWith()
      msg    = message("ciao", Some(MessageId("1")))
      _    <- List.fill(10)(engine.onMessage(msg)(using adapter)).parSequence_
      sent <- adapter.sent.get
    } yield assertEquals(sent.map(_.replyValue), List(Text("hello")))
  }

  test("different messages of the same chat are all answered") {
    for {
      adapter <- FakePlatformAdapter(capabilities = slashCommands)
      engine = engineWith()
      _    <- engine.onMessage(message("ciao", Some(MessageId("1"))))(using adapter)
      _    <- engine.onMessage(message("ciao", Some(MessageId("2"))))(using adapter)
      sent <- adapter.sent.get
    } yield assertEquals(sent.map(_.replyValue), List(Text("hello"), Text("hello")))
  }

  test("messages without a message id are never deduplicated") {
    for {
      adapter <- FakePlatformAdapter(capabilities = slashCommands)
      engine = engineWith()
      _    <- engine.onMessage(message("ciao"))(using adapter)
      _    <- engine.onMessage(message("ciao"))(using adapter)
      sent <- adapter.sent.get
    } yield assertEquals(sent.map(_.replyValue), List(Text("hello"), Text("hello")))
  }

  test("a failing duplicate check does not prevent the message from being processed") {
    val failing = new DBProcessedMessage[IO] {
      def markIfNew(chatId: ChatId, botId: SBotInfo.SBotId, messageId: MessageId): IO[Boolean] =
        IO.raiseError(new RuntimeException("db down"))
      def deleteOlderThan(epochSeconds: Long): IO[Int] = IO.pure(0)
    }
    for {
      adapter <- FakePlatformAdapter(capabilities = slashCommands)
      engine = engineWith(processedMessages = failing)
      _    <- engine.onMessage(message("ciao", Some(MessageId("1"))))(using adapter)
      sent <- adapter.sent.get
    } yield assertEquals(sent.map(_.replyValue), List(Text("hello")))
  }
}
