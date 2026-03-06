package com.benkio.telegrambotinfrastructure

import cats.effect.IO
import cats.implicits.*
import com.benkio.telegrambotinfrastructure.mocks.ApiMock.given
import com.benkio.telegrambotinfrastructure.mocks.SampleWebhookBot
import com.benkio.telegrambotinfrastructure.model.reply.ReplyBundle
import com.benkio.telegrambotinfrastructure.model.reply.ReplyBundleCommand
import com.benkio.telegrambotinfrastructure.model.reply.ReplyBundleMessage
import com.benkio.telegrambotinfrastructure.model.reply.TextReply
import com.benkio.telegrambotinfrastructure.model.ChatId
import com.benkio.telegrambotinfrastructure.model.CommandInstructionData
import com.benkio.telegrambotinfrastructure.model.CommandTrigger
import com.benkio.telegrambotinfrastructure.model.Message
import munit.CatsEffectSuite

import java.time.Instant

class ISBotSpec extends CatsEffectSuite {

  test("selectReplyBundle should return all the expected `ReplyBundleMessage` respecting the trigger ordering") {
    val inputMessage: Message = Message(
      messageId = 0,
      date = Instant.now.getEpochSecond(),
      chatId = ChatId(0L),
      chatType = "test",
      text = Some("test"),
      caption = None,
      newChatMembers = List.empty,
      leftChatMember = None,
      forwardOrigin = None
    )
    val expected = ReplyBundleMessage.textToText(
      "test"
    )("testReply1")

    SampleWebhookBot().map(sampleWebhookBot => {
      val resultOpt = sampleWebhookBot.selectReplyBundle(inputMessage)
      val result    = resultOpt.fold(Throwable("SBotSpec expected Some, got None").raiseError[IO, String]) {
        _.prettyPrint()
      }
      val expectedPP = expected.prettyPrint()
      assertEquals(result, expectedPP)
    })
  }

  test("selectCommandReplyBundle should return all the expected `ReplyBundleCommand`") {
    val inputMessage: Message = Message(
      messageId = 0,
      date = Instant.now.getEpochSecond(),
      chatId = ChatId(0L),
      chatType = "test",
      text = Some("/testcommand"),
      caption = None,
      newChatMembers = List.empty,
      leftChatMember = None,
      forwardOrigin = None
    )
    val expected =
      ReplyBundleCommand(
        trigger = CommandTrigger("testcommand"),
        reply = TextReply.fromList(
          "test command reply"
        )(false),
        instruction = CommandInstructionData.NoInstructions
      )
    SampleWebhookBot().map(sampleWebhookBot => {
      val resultOpt = sampleWebhookBot.selectCommandReplyBundle(inputMessage)
      val result    = resultOpt.fold(Throwable("SBotSpec expected Some, got None").raiseError[IO, String]) {
        _.prettyPrint()
      }
      val expectedPP = expected.prettyPrint()
      assertEquals(result, expectedPP)
    })
  }
}
