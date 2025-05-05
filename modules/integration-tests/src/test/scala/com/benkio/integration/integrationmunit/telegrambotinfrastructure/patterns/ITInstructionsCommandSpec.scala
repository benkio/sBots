package com.benkio.integration.integrationmunit.telegrambotinfrastructure.patterns

import cats.effect.IO
import cats.effect.Resource
import cats.syntax.all.*
import com.benkio.integration.DBFixture
import com.benkio.m0sconibot.M0sconiBot
import com.benkio.telegrambotinfrastructure.mocks.DBLayerMock
import com.benkio.telegrambotinfrastructure.patterns.CommandPatterns.InstructionsCommand
import munit.CatsEffectSuite

class ITInstructionsCommandSpec extends CatsEffectSuite with DBFixture {

  databaseFixture.test(
    "Instruction Command should return a TextReply with the input instructions"
  ) { fixture =>
    val resourceAssert = for {
      dbLayer <- fixture.resourceDBLayer
      dbMedia = dbLayer.dbMedia
      botName = "testBot"
      resultTextReply <- Resource.pure(
        InstructionsCommand.instructionCommandLogic[IO](
          botName,
          Some("!"),
          M0sconiBot.commandRepliesData[IO](DBLayerMock.mock(M0sconiBot.botName))
        )
      )
      _ <- Resource.eval(
        List("", "en", "🇬🇧", "🇺🇸", "🏴󠁧󠁢󠁥󠁮󠁧󠁿", "eng", "english")
          .flatTraverse(resultTextReply(_))
          .map(_.foreach { text =>
            assert(
              text.contains(botName),
              s"[ITInstructionsCommandSpec] description should contains the botname: $text"
            )
            assert(
              text.contains("'/random': Returns a random data"),
              s"[ITInstructionsCommandSpec] instructionCommandLogic should return the eng description: $text"
            )
          })
      )
      _ <- Resource.eval(
        List("it", "ita", "🇮🇹", "italian")
          .flatTraverse(resultTextReply(_))
          .map(_.foreach { text =>
            assert(
              text.contains(botName),
              s"[ITInstructionsCommandSpec] description should contains the botname: $text"
            )
            assert(
              text.contains("'/random': Restituisce un dato"),
              s"[ITInstructionsCommandSpec] instructionCommandLogic should return the ita description: $text"
            )
          })
      )
    } yield ()

    resourceAssert.use_
  }

}
