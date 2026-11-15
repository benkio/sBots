package com.benkio.integration.integrationmunit.chatcore.patterns

import cats.effect.*
import cats.syntax.all.*
import com.benkio.chatcore.config.SBotConfig
import com.benkio.chatcore.model.Message
import com.benkio.chatcore.patterns.CommandPatterns.InstructionsCommand
import com.benkio.chatcore.TestIds
import com.benkio.chattelegramadapter.SBot
import com.benkio.integration.BotSetupFixture
import com.benkio.RichardPHJBensonBot.RichardPHJBensonBot
import munit.CatsEffectSuite

class ITInstructionsCommandSpec extends CatsEffectSuite with BotSetupFixture {

  override def botSetupFixtureConfig: SBotConfig = SBot.buildSBotConfig(RichardPHJBensonBot.sBotInfo)

  botSetupFixture.test(
    "Instruction Command should return a TextReply with the instructions link"
  ) { fixture =>
    val resourceAssert = for {
      botSetup <- fixture.botSetupResource
      expected = s"Puoi trovare le istruzioni del bot al seguente URL: ${botSetup.sBotConfig.instructionsUri}"
      _ <- Resource.eval(
        InstructionsCommand
          .instructionCommandLogic[IO](
            msg = Message(
              messageId = None,
              date = 0,
              chatId = TestIds.chatId(0L),
              chatType = "private",
              text = Some("/instructions")
            ),
            sBotInfo = botSetup.sBotConfig.sBotInfo,
            instructionsUri = botSetup.sBotConfig.instructionsUri,
            ttl = botSetup.sBotConfig.messageTimeToLive
          )
          .map(text => assertEquals(text.show, expected))
      )
    } yield ()

    resourceAssert.use_
  }

}
