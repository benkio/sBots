package com.benkio.chatllm

import cats.effect.IO
import com.benkio.chatcore.llm.LlmSettings
import com.benkio.chatcore.model.ChatId
import com.benkio.chatcore.model.Message
import com.benkio.chatcore.model.Platform
import com.benkio.chatcore.model.SBotInfo.SBotId
import com.benkio.chatcore.Logger.given
import com.benkio.chatllm.llm.Completion
import com.benkio.chatllm.llm.CompletionStatus
import com.benkio.chatllm.llm.LlmClientApi
import com.benkio.chatllm.llm.LlmConfig
import com.benkio.chatllm.llm.LlmProvider
import com.benkio.chatllm.llm.ProviderConnection
import com.benkio.chatllm.llm.Sensitive
import doobie.util.transactor.Transactor
import munit.CatsEffectSuite

class ChatLlmSpec extends CatsEffectSuite {

  test("the unconfigured responder never answers") {
    ChatLlm
      .responder[IO]
      .respond(
        Message(messageId = None, date = 0L, chatId = ChatId(Platform.Telegram, "1"), chatType = "private"),
        List.empty
      )
      .assertEquals(None)
  }

  test("the unconfigured settings report the LLM disabled") {
    ChatLlm.settings[IO].status(ChatId(Platform.Telegram, "1")).assertEquals(LlmSettings.Status(enabled = false))
  }

  private val client: LlmClientApi[IO] = new LlmClientApi[IO] {
    def complete(prompt: String): IO[Completion] =
      IO.pure(Completion(prompt, LlmProvider.Gemini, CompletionStatus.Silent, None, Nil))
  }
  private val transactor = Transactor.fromDriverManager[IO]("org.sqlite.JDBC", "jdbc:sqlite::memory:", "", "", None)

  private def config(keys: Boolean): LlmConfig = LlmConfig(
    gemini = ProviderConnection("https://gemini.example", apiKey = Option.when(keys)(Sensitive("g"))),
    openrouter = ProviderConnection("https://openrouter.example", apiKey = Option.when(keys)(Sensitive("o")))
  )

  test("without the API keys of both providers the bot gets the noop responder") {
    ChatLlm
      .wiring[IO](config(keys = false), client)
      .engine(transactor, SBotId("wiretest"))
      .map(engine => assert(!engine.responder.answers))
  }

  test("with the keys but no persona and catalogue for the bot the bot gets the noop responder") {
    ChatLlm
      .wiring[IO](config(keys = true), client)
      .engine(transactor, SBotId("nosuchbot"))
      .map(engine => assert(!engine.responder.answers))
  }

  test("with the keys, the persona and the catalogue the bot gets a responder that answers, with the llm timeout") {
    ChatLlm.wiring[IO](config(keys = true), client).engine(transactor, SBotId("wiretest")).map { engine =>
      assert(engine.responder.answers)
      assertEquals(engine.timeout, config(keys = true).timeout)
    }
  }
}
