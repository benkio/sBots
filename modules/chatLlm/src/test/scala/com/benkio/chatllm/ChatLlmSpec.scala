package com.benkio.chatllm

import cats.effect.IO
import com.benkio.chatcore.llm.LlmSettings
import com.benkio.chatcore.model.ChatId
import com.benkio.chatcore.model.Message
import com.benkio.chatcore.model.Platform
import munit.CatsEffectSuite

class ChatLlmSpec extends CatsEffectSuite {

  test("the unconfigured responder never answers") {
    ChatLlm
      .responder[IO]
      .respond(Message(messageId = None, date = 0L, chatId = ChatId(Platform.Telegram, "1"), chatType = "private"), List.empty)
      .assertEquals(None)
  }

  test("the unconfigured settings report the LLM disabled") {
    ChatLlm.settings[IO].status(ChatId(Platform.Telegram, "1")).assertEquals(LlmSettings.Status(enabled = false))
  }
}
