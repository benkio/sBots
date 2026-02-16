package com.benkio.integration.integrationmunit.telegrambotinfrastructure.patterns

import cats.effect.*
import cats.syntax.all.*
import cats.Parallel
import com.benkio.integration.BotSetupFixture
import com.benkio.telegrambotinfrastructure.config.SBotConfig
import com.benkio.telegrambotinfrastructure.model.reply.ReplyBundleMessage
import com.benkio.telegrambotinfrastructure.model.SBotInfo
import com.benkio.telegrambotinfrastructure.model.SBotInfo.SBotName
import com.benkio.telegrambotinfrastructure.patterns.CommandPatterns.InstructionsCommand
import com.benkio.RichardPHJBensonBot.RichardPHJBensonBot
import com.benkio.RichardPHJBensonBot.RichardPHJBensonBotPolling
import munit.CatsEffectSuite
import telegramium.bots.Chat
import telegramium.bots.Message

class ITInstructionsCommandSpec extends CatsEffectSuite with BotSetupFixture {

  override def botSetupFixtureConfig: SBotConfig = RichardPHJBensonBot.sBotConfig

  botSetupFixture.test(
    "Instruction Command should return a TextReply with the input instructions"
  ) { fixture =>
    val resourceAssert = for {
      botSetup           <- fixture.botSetupResource
      messageRepliesData <- Resource.eval(
        botSetup.jsonDataRepository.loadData[ReplyBundleMessage](RichardPHJBensonBot.sBotConfig.repliesJsonFilename)
      )
      richardBot = new RichardPHJBensonBotPolling[IO](botSetup, messageRepliesData)(using
        Parallel[IO],
        Async[IO],
        botSetup.api,
        log
      )
      commandRepliesData = richardBot.allCommandRepliesData
      _ <- Resource.eval(
        List("", "en", "🇬🇧", "🇺🇸", "🏴󠁧󠁢󠁥󠁮󠁧󠁿", "eng", "english")
          .traverse(input =>
            InstructionsCommand.instructionCommandLogic[IO](
              msg = Message(
                messageId = 0,
                date = 0,
                chat = Chat(id = 0, `type` = "private"),
                text = Some(s"/instructions $input")
              ),
              sBotInfo = RichardPHJBensonBot.sBotConfig.sBotInfo,
              ignoreMessagePrefix = Some("!"),
              commands = commandRepliesData,
              ttl = RichardPHJBensonBot.sBotConfig.messageTimeToLive
            )
          )
          .map(_.flatten.foreach { text =>
            assert(
              text.value.contains(RichardPHJBensonBot.sBotConfig.sBotInfo.botName.value),
              s"[ITInstructionsCommandSpec] description should contains the botname: ${text.value}"
            )
            assert(
              text.value.contains("'/random': Returns a random data"),
              s"[ITInstructionsCommandSpec] RichardPHJBensonBot.instructionCommandLogic should return the eng description and contain the random command: ${text.value}"
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
                chat = Chat(id = 0, `type` = "private"),
                text = Some(s"/instructions $input")
              ),
              sBotInfo = RichardPHJBensonBot.sBotConfig.sBotInfo,
              ignoreMessagePrefix = Some("!"),
              commands = commandRepliesData,
              ttl = RichardPHJBensonBot.sBotConfig.messageTimeToLive
            )
          )
          .map(_.flatten.foreach { text =>
            assert(
              text.value.contains(RichardPHJBensonBot.sBotConfig.sBotInfo.botName.value),
              s"[ITInstructionsCommandSpec] description should contains the botname: ${text.value}"
            )
            assert(
              text.value.contains("'/random': Restituisce un dato"),
              s"[ITInstructionsCommandSpec] RichardPHJBensonBot.instructionCommandLogic should return the ita description and contain the random command: ${text.value}"
            )
          })
      )
    } yield ()

    resourceAssert.use_
  }

}
