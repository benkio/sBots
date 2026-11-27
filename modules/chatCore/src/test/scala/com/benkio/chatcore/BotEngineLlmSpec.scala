package com.benkio.chatcore

import cats.effect.kernel.Ref
import cats.effect.IO
import com.benkio.chatcore.mocks.FakePlatformAdapter
import com.benkio.chatcore.model.reply.Text
import com.benkio.chatcore.model.Message
import com.benkio.chatcore.model.MessageId
import munit.CatsEffectSuite

/** The LLM section of the [[BotEngine]]: it asks the [[LlmEngine]] only for the messages the reactive part left
  * unanswered, and delivers the text the engine returns.
  */
class BotEngineLlmSpec extends CatsEffectSuite with BotEngineFixtures {

  /** An LLM engine that records the messages it is asked about and answers with the given text. */
  private def recordingLlm(answer: Option[Text]): IO[(LlmEngine[IO], Ref[IO, List[String]])] =
    Ref.of[IO, List[String]](List.empty).map { asked =>
      val llm = new LlmEngine[IO] {
        override def reply(msg: Message): IO[Option[Text]] =
          asked.update(_ :+ msg.text.getOrElse("")).as(answer)
      }
      (llm, asked)
    }

  test("an unanswered message is delivered the text of the LLM as a reply to the message") {
    for {
      (llm, asked) <- recordingLlm(Some(Text("llm answer")))
      adapter      <- FakePlatformAdapter(capabilities = slashCommands)
      engine = engineWith(llmEngine = Some(llm))
      _       <- engine.onMessage(message("something else"))(using adapter)
      sent    <- adapter.sent.get
      askedOn <- asked.get
    } yield {
      assertEquals(sent.map(_.replyValue), List(Text("llm answer")))
      assertEquals(sent.map(_.replyToMessage), List(true))
      assertEquals(askedOn, List("something else"))
    }
  }

  test("an LLM with nothing to say sends nothing") {
    for {
      (llm, asked) <- recordingLlm(None)
      adapter      <- FakePlatformAdapter(capabilities = slashCommands)
      engine = engineWith(llmEngine = Some(llm))
      _       <- engine.onMessage(message("something else"))(using adapter)
      sent    <- adapter.sent.get
      askedOn <- asked.get
    } yield {
      assertEquals(sent, List.empty)
      assertEquals(askedOn, List("something else"))
    }
  }

  test("a message answered by the reactive part is not handed to the LLM") {
    for {
      (llm, asked) <- recordingLlm(Some(Text("llm answer")))
      adapter      <- FakePlatformAdapter(capabilities = slashCommands)
      engine = engineWith(llmEngine = Some(llm))
      _       <- engine.onMessage(message("ciao"))(using adapter)
      sent    <- adapter.sent.get
      askedOn <- asked.get
    } yield {
      assertEquals(sent.map(_.replyValue), List(Text("hello")))
      assertEquals(askedOn, List.empty)
    }
  }

  test("a duplicate message is not handed to the LLM again") {
    for {
      (llm, asked) <- recordingLlm(Some(Text("llm answer")))
      adapter      <- FakePlatformAdapter(capabilities = slashCommands)
      engine = engineWith(llmEngine = Some(llm))
      _       <- engine.onMessage(message("something else", Some(MessageId("1"))))(using adapter)
      _       <- engine.onMessage(message("something else", Some(MessageId("1"))))(using adapter)
      sent    <- adapter.sent.get
      askedOn <- asked.get
    } yield {
      assertEquals(sent.map(_.replyValue), List(Text("llm answer")))
      assertEquals(askedOn.size, 1)
    }
  }

  test("a failing LLM is swallowed: nothing is sent and the message is still handled") {
    val failing = new LlmEngine[IO] {
      override def reply(msg: Message): IO[Option[Text]] = IO.raiseError(new RuntimeException("llm down"))
    }
    for {
      adapter <- FakePlatformAdapter(capabilities = slashCommands)
      engine = engineWith(llmEngine = Some(failing))
      result <- engine.onMessage(message("something else"))(using adapter).attempt
      sent   <- adapter.sent.get
    } yield {
      assertEquals(result, Right(()))
      assertEquals(sent, List.empty)
    }
  }
}
