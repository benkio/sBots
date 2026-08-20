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

  private val expectedEnglishSnippets: List[String] = List(
    "'/random': Send a random content",
    "'/searchshow [query]': Search shows and send a link.",
    "'/triggerlist': Show the link to the file with all automatic triggers",
    "'/triggersearch <text>': Check whether a word or phrase matches a trigger.",
    "'/subscribe <cron time>': Subscribe this chat to random show messages",
    "'/unsubscribe [uuid]': Unsubscribe the current chat.",
    "'/subscriptions': Show current subscriptions for this chat",
    "'/toptwenty': Show the most sent files, ordered by frequency.",
    "'/settimeout [HH:MM:SS]': Set the minimum time between bot replies in this chat.",
    "'/gettimeout': Show the active timeout for the current chat."
  )

  private val expectedItalianSnippets: List[String] = List(
    "'/random': Invia un contenuto casuale",
    "'/searchshow [query]': Cerca uno show e invia un link.",
    "'/triggerlist': Mostra il link al file con tutti i trigger automatici",
    "'/triggersearch <testo>': Cerca se una parola o frase corrisponde a un trigger.",
    "'/subscribe <cron time>': Iscrive la chat all'invio casuale di puntate.",
    "'/unsubscribe [uuid]': Disiscrive la chat corrente.",
    "'/subscriptions': Mostra le iscrizioni correnti della chat",
    "'/toptwenty': Mostra i file piu inviati, ordinati per frequenza.",
    "'/settimeout [HH:MM:SS]': Imposta il tempo minimo tra le risposte del bot in questa chat.",
    "'/gettimeout': Mostra il timeout attivo per la chat corrente."
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
            assertInstructionText(text.show, expectedEnglishSnippets, "english")
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
            assertInstructionText(text.show, expectedItalianSnippets, "italian")
          })
      )
    } yield ()

    resourceAssert.use_
  }

}
