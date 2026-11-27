package com.benkio.chatllm

import cats.effect.kernel.Ref
import cats.effect.IO
import com.benkio.chatcore.llm.Persona
import com.benkio.chatcore.model.reply.Text
import com.benkio.chatcore.model.ChatId
import com.benkio.chatcore.model.Message
import com.benkio.chatcore.model.Platform
import munit.CatsEffectSuite

class LlmEngineSpec extends CatsEffectSuite {

  private val msg = Message(
    messageId = None,
    date = 0L,
    chatId = ChatId(Platform.Telegram, "1"),
    chatType = "private",
    text = Some("hello")
  )

  /** A responder that records the persona and the message it is asked about and answers with the given text. */
  private def recordingResponder(answer: Option[Text]): IO[(LlmResponder[IO], Ref[IO, List[(Persona, Message)]])] =
    Ref.of[IO, List[(Persona, Message)]](List.empty).map { asked =>
      val responder = new LlmResponder[IO] {
        override def respond(persona: Persona, msg: Message): IO[Option[Text]] =
          asked.update(_ :+ (persona -> msg)).as(answer)
      }
      (responder, asked)
    }

  test("the engine asks the responder to reply as the persona of the bot, and returns its answer") {
    for {
      (responder, asked) <- recordingResponder(Some(Text("hi")))
      persona = Persona("Sei il bot di prova.")
      reply    <- LlmEngine.engine[IO](responder, persona).reply(msg)
      askedFor <- asked.get
    } yield {
      assertEquals(reply, Some(Text("hi")))
      assertEquals(askedFor, List(persona -> msg))
    }
  }

  test("the engine has nothing to say when the responder has none") {
    for {
      (responder, _) <- recordingResponder(None)
      reply          <- LlmEngine.engine[IO](responder, Persona("Sei il bot di prova.")).reply(msg)
    } yield assertEquals(reply, None)
  }
}
