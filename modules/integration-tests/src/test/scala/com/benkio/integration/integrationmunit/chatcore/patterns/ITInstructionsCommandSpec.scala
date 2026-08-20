package com.benkio.integration.integrationmunit.chatcore.patterns

import cats.effect.*
import cats.syntax.all.*
import com.benkio.chatcore.config.SBotConfig
import com.benkio.chatcore.model.reply.ReplyBundleCommand
import com.benkio.chatcore.model.reply.ReplyBundleMessage
import com.benkio.chatcore.model.ChatId
import com.benkio.chatcore.model.Message
import com.benkio.chatcore.patterns.CommandPatterns.InstructionsCommand
import com.benkio.chattelegramadapter.mocks.ApiMock.given
import com.benkio.chattelegramadapter.SBot
import com.benkio.chattelegramadapter.SBotPolling
import com.benkio.integration.BotSetupFixture
import com.benkio.integration.Logger.given
import com.benkio.RichardPHJBensonBot.RichardPHJBensonBot
import munit.CatsEffectSuite

class ITInstructionsCommandSpec extends CatsEffectSuite with BotSetupFixture {

  override def botSetupFixtureConfig: SBotConfig = SBot.buildSBotConfig(RichardPHJBensonBot.sBotInfo)

  private val docsBaseUrl = "https://github.com/benkio/sBots/blob/main"

  private val expectedEnglishSnippets: List[String] = List(
    s"$docsBaseUrl/modules/chatCore/CommandsDocumentation/random.md#en",
    s"$docsBaseUrl/modules/chatCore/CommandsDocumentation/searchshow.md#en",
    s"$docsBaseUrl/modules/chatCore/CommandsDocumentation/triggerlist.md#en",
    s"$docsBaseUrl/modules/chatCore/CommandsDocumentation/triggersearch.md#en",
    s"$docsBaseUrl/modules/chatCore/CommandsDocumentation/subscribe.md#en",
    s"$docsBaseUrl/modules/chatCore/CommandsDocumentation/unsubscribe.md#en",
    s"$docsBaseUrl/modules/chatCore/CommandsDocumentation/subscriptions.md#en",
    s"$docsBaseUrl/modules/chatCore/CommandsDocumentation/toptwenty.md#en",
    s"$docsBaseUrl/modules/chatCore/CommandsDocumentation/settimeout.md#en",
    s"$docsBaseUrl/modules/chatCore/CommandsDocumentation/gettimeout.md#en",
    s"$docsBaseUrl/modules/bots/RichardPHJBensonBot/CommandsDocumentation/bensonify.md#en"
  )

  private val expectedItalianSnippets: List[String] = List(
    s"$docsBaseUrl/modules/chatCore/CommandsDocumentation/random.md#it",
    s"$docsBaseUrl/modules/chatCore/CommandsDocumentation/searchshow.md#it",
    s"$docsBaseUrl/modules/chatCore/CommandsDocumentation/triggerlist.md#it",
    s"$docsBaseUrl/modules/chatCore/CommandsDocumentation/triggersearch.md#it",
    s"$docsBaseUrl/modules/chatCore/CommandsDocumentation/subscribe.md#it",
    s"$docsBaseUrl/modules/chatCore/CommandsDocumentation/unsubscribe.md#it",
    s"$docsBaseUrl/modules/chatCore/CommandsDocumentation/subscriptions.md#it",
    s"$docsBaseUrl/modules/chatCore/CommandsDocumentation/toptwenty.md#it",
    s"$docsBaseUrl/modules/chatCore/CommandsDocumentation/settimeout.md#it",
    s"$docsBaseUrl/modules/chatCore/CommandsDocumentation/gettimeout.md#it",
    s"$docsBaseUrl/modules/bots/RichardPHJBensonBot/CommandsDocumentation/bensonify.md#it"
  )

  private def assertInstructionText(
      text: String,
      expectedSnippets: List[String],
      language: String
  ): Unit = {
    expectedSnippets.foreach { snippet =>
      assert(
        text.contains(snippet),
        s"[ITInstructionsCommandSpec] Missing $language snippet: $snippet\n$text"
      )
    }
  }

  botSetupFixture.test(
    "Instruction Command should return a TextReply with the input instructions"
  ) { fixture =>
    val resourceAssert = for {
      botSetup           <- fixture.botSetupResource
      messageRepliesData <- Resource.eval(
        botSetup.jsonDataRepository.loadData[ReplyBundleMessage](botSetup.sBotConfig.repliesJsonFilename)
      )
      commandRepliesData <- Resource.eval(
        botSetup.jsonDataRepository.loadData[ReplyBundleCommand](botSetup.sBotConfig.commandsJsonFilename)
      )
      richardBot = new SBotPolling[IO](
        botSetup,
        messageRepliesData,
        commandRepliesData,
        RichardPHJBensonBot.commandEffectfulCallback[IO]
      )
      allCommandRepliesData = richardBot.allCommandRepliesData
      _ <- Resource.eval(
        List("", "en", "🇬🇧", "🇺🇸", "🏴󠁧󠁢󠁥󠁮󠁧󠁿", "eng", "english")
          .traverse(input =>
            InstructionsCommand.instructionCommandLogic[IO](
              msg = Message(
                messageId = 0,
                date = 0,
                chatId = ChatId(0L),
                chatType = "private",
                text = Some(s"/instructions $input")
              ),
              sBotInfo = botSetup.sBotConfig.sBotInfo,
              ignoreMessagePrefix = Some("!"),
              commands = allCommandRepliesData,
              ttl = botSetup.sBotConfig.messageTimeToLive
            )
          )
          .map(_.foreach { text =>
            assert(
              text.show.contains(botSetup.sBotConfig.sBotInfo.botName.value),
              s"[ITInstructionsCommandSpec] description should contains the botname: ${text.show}"
            )
            assertInstructionText(text = text.show, expectedSnippets = expectedEnglishSnippets, language = "english")
          })
      )
      _ <- Resource.eval(
        List("it", "ita", "🇮🇹", "italian")
          .traverse(input =>
            InstructionsCommand.instructionCommandLogic[IO](
              msg = Message(
                messageId = 0,
                date = 0,
                chatId = ChatId(0L),
                chatType = "private",
                text = Some(s"/instructions $input")
              ),
              sBotInfo = botSetup.sBotConfig.sBotInfo,
              ignoreMessagePrefix = Some("!"),
              commands = allCommandRepliesData,
              ttl = botSetup.sBotConfig.messageTimeToLive
            )
          )
          .map(_.foreach { text =>
            assert(
              text.show.contains(botSetup.sBotConfig.sBotInfo.botName.value),
              s"[ITInstructionsCommandSpec] description should contains the botname: ${text.show}"
            )
            assertInstructionText(text = text.show, expectedSnippets = expectedItalianSnippets, language = "italian")
          })
      )
    } yield ()

    resourceAssert.use_
  }

}
