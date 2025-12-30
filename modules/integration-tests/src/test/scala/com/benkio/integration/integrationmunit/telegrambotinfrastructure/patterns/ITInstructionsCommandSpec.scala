package com.benkio.integration.integrationmunit.telegrambotinfrastructure.patterns

import cats.effect.IO
import cats.effect.Resource
import cats.syntax.all.*
import com.benkio.integration.DBFixture
import com.benkio.m0sconibot.M0sconiBot
import com.benkio.telegrambotinfrastructure.model.SBotInfo
import com.benkio.telegrambotinfrastructure.model.SBotInfo.SBotName
import com.benkio.telegrambotinfrastructure.patterns.CommandPatterns.InstructionsCommand
import munit.CatsEffectSuite
import telegramium.bots.Chat
import telegramium.bots.Message

class ITInstructionsCommandSpec extends CatsEffectSuite with DBFixture {

  databaseFixture.test(
    "Instruction Command should return a TextReply with the input instructions"
  ) { fixture =>
    val resourceAssert = for {
      dbLayer <- fixture.resourceDBLayer
      dbMedia  = dbLayer.dbMedia
      sBotInfo = SBotInfo(M0sconiBot.sBotConfig.sBotInfo.botId, SBotName("testBot"))
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
              sBotInfo = sBotInfo,
              ignoreMessagePrefix = Some("!"),
              commands = M0sconiBot.commandRepliesData,
              ttl = M0sconiBot.sBotConfig.messageTimeToLive
            )
          )
          .map(_.flatten.foreach { text =>
            assert(
              text.value.contains(sBotInfo.botName.value),
              s"[ITInstructionsCommandSpec] description should contains the botname: ${text.value}"
            )
            assert(
              text.value.contains("'/random': Returns a random data"),
              s"[ITInstructionsCommandSpec] M0sconiBot.instructionCommandLogic should return the eng description and contain the random command: ${text.value}"
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
              sBotInfo = sBotInfo,
              ignoreMessagePrefix = Some("!"),
              commands = M0sconiBot.commandRepliesData,
              ttl = M0sconiBot.sBotConfig.messageTimeToLive
            )
          )
          .map(_.flatten.foreach { text =>
            assert(
              text.value.contains(sBotInfo.botName.value),
              s"[ITInstructionsCommandSpec] description should contains the botname: ${text.value}"
            )
            assert(
              text.value.contains("'/random': Restituisce un dato"),
              s"[ITInstructionsCommandSpec] M0sconiBot.instructionCommandLogic should return the ita description and contain the random command: ${text.value}"
            )
          })
      )
    } yield ()

    resourceAssert.use_
  }

}
