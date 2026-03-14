package com.benkio.integration.integrationmunit.chatcore.patterns

import cats.effect.*
import cats.syntax.all.*
import com.benkio.chatcore.config.SBotConfig
import com.benkio.chatcore.model.reply.ReplyBundleCommand
import com.benkio.chatcore.model.reply.ReplyBundleMessage
import com.benkio.chatcore.model.ChatId
import com.benkio.chatcore.model.Message
import com.benkio.chatcore.patterns.CommandPatterns.InstructionsCommand
import com.benkio.chattelegramadapter.SBot
import com.benkio.chattelegramadapter.SBotPolling
import com.benkio.integration.BotSetupFixture
import com.benkio.RichardPHJBensonBot.RichardPHJBensonBot
import munit.CatsEffectSuite

class ITInstructionsCommandSpec extends CatsEffectSuite with BotSetupFixture {

  override def botSetupFixtureConfig: SBotConfig = SBot.buildSBotConfig(RichardPHJBensonBot.sBotInfo)

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
      given telegramium.bots.high.Api[IO]   = botSetup.api
      given _root_.log.effect.LogWriter[IO] = log
      richardBot                            = new SBotPolling[IO](
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
            assert(
              text.show.contains("'/random': Returns a random data"),
              s"[ITInstructionsCommandSpec] RichardPHJBensonBot.instructionCommandLogic should return the eng description and contain the random command: ${text.show}"
            )
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
            assert(
              text.show.contains("'/random': Restituisce un dato"),
              s"[ITInstructionsCommandSpec] RichardPHJBensonBot.instructionCommandLogic should return the ita description and contain the random command: ${text.show}"
            )
          })
      )
    } yield ()

    resourceAssert.use_
  }

}
