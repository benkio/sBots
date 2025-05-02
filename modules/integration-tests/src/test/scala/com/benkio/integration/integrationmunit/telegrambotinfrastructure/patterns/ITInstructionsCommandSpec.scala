package com.benkio.integration.integrationmunit.telegrambotinfrastructure.patterns

import com.benkio.telegrambotinfrastructure.patterns.CommandPatterns.InstructionsCommand
import cats.effect.IO
import cats.effect.Resource
import com.benkio.integration.DBFixture

import munit.CatsEffectSuite

class ITInstructionsCommandSpec extends CatsEffectSuite with DBFixture {

  databaseFixture.test(
    "Instruction Command should return a TextReply with the input instructions"
  ) { fixture =>
    val resourceAssert = for {
      dbLayer <- fixture.resourceDBLayer
      dbMedia   = dbLayer.dbMedia
      botName = "testBot"
      resultTextReply <- Resource.pure(
        InstructionsCommand.instructionCommandLogic[IO](
          botName,
          Some("!"),
          List("ita instructions"),
          List("eng instructions"),
        )
      )
    } yield
      assert(!resultTextReply.replyToMessage)
      assertEquals(resultTextReply.text.length, 2)
      resultTextReply.text.foreach {
        text =>
          assert(text.value.contains(botName))
          assert(text.value.contains("ita instructions") || text.value.contains("eng instructions"))
      }
    resourceAssert.use_
  }

}
