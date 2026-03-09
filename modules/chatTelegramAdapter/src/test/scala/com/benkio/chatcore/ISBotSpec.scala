package com.benkio.chattelegramadapter

import cats.effect.IO
import cats.implicits.*
import com.benkio.chatcore.mocks.ApiMock.given
import com.benkio.chatcore.mocks.SampleWebhookBot
import com.benkio.chatcore.model.reply.ReplyBundleCommand
import com.benkio.chatcore.model.reply.ReplyBundleMessage
import com.benkio.chatcore.model.reply.TextReply
import com.benkio.chatcore.model.ChatId
import com.benkio.chatcore.model.CommandInstructionData
import com.benkio.chatcore.model.CommandTrigger
import com.benkio.chatcore.model.Message
import munit.CatsEffectSuite

import java.time.Instant

class ISBotSpec extends CatsEffectSuite {

  test("selectReplyBundle should return all the expected `ReplyBundleMessage` respecting the trigger ordering") {
    val inputMessage: Message = Message(
      messageId = 0,
      date = Instant.now.getEpochSecond(),
      chatId = ChatId(0L),
      chatType = "test",
      text = Some("test")
    )
    val expected = ReplyBundleMessage.textToText(
      "test"
    )("testReply1")

    SampleWebhookBot().map(sampleWebhookBot => {
      val resultOpt = ReplyBundleMessage.selectReplyBundle(
        msg = inputMessage,
        messageRepliesData = sampleWebhookBot.messageRepliesData,
        ignoreMessagePrefix = sampleWebhookBot.sBotConfig.ignoreMessagePrefix,
        disableForward = sampleWebhookBot.sBotConfig.disableForward
      )
      val result = resultOpt.fold(Throwable("SBotSpec expected Some, got None").raiseError[IO, String]) {
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
      text = Some("/testcommand")
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
      val resultOpt = ReplyBundleCommand.selectCommandReplyBundle(
        msg = inputMessage,
        allCommandRepliesData = sampleWebhookBot.allCommandRepliesData,
        botName = sampleWebhookBot.sBotConfig.sBotInfo.botName
      )
      val result = resultOpt.fold(Throwable("SBotSpec expected Some, got None").raiseError[IO, String]) {
        _.prettyPrint()
      }
      val expectedPP = expected.prettyPrint()
      assertEquals(result, expectedPP)
    })
  }
}
