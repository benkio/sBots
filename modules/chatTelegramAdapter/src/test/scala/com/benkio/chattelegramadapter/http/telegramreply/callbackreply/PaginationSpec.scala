package com.benkio.chattelegramadapter.http.telegramreply.callbackreply

import com.benkio.chatcore.model.reply.ReplyBundleCommand
import com.benkio.chatcore.model.reply.Text
import com.benkio.chatcore.model.reply.TextReply
import com.benkio.chatcore.model.CommandInstructionData
import com.benkio.chatcore.model.CommandKey
import com.benkio.chatcore.model.CommandTrigger
import com.benkio.chattelegramadapter.Arbitraries.given
import munit.ScalaCheckSuite
import org.scalacheck.Arbitrary.arbitrary
import org.scalacheck.Gen
import org.scalacheck.Prop.*
import telegramium.bots.MaybeInaccessibleMessage

class PaginationSpec extends ScalaCheckSuite {

  property("commandMessageText formats /command input") {
    forAll(arbitrary[CommandKey], Gen.alphaStr) { (commandKey: CommandKey, input: String) =>
      assertEquals(Pagination.commandMessageText(commandKey, input), s"/${commandKey.asString} $input")
    }
  }

  property("prepare returns Left when command is missing from reply data") {
    forAll { (msg: MaybeInaccessibleMessage, commandKey: CommandKey) =>
      val result = Pagination.prepare(msg, commandKey, List.empty)
      assert(result.isLeft)
    }
  }

  property("prepare returns Right with rebuilt command text when command exists") {
    forAll { (msg: MaybeInaccessibleMessage, commandKey: CommandKey) =>
      val commandReply = ReplyBundleCommand(
        trigger = CommandTrigger(commandKey.asString),
        reply = TextReply(Set(Text("ok"))),
        instruction = CommandInstructionData.NoInstructions
      )
      val result = Pagination.prepare(msg, commandKey, List(commandReply))
      assert(result.isRight)
      result.foreach { ctx =>
        assertEquals(ctx.commandReply, commandReply)
        assert(ctx.modelMessage.text.exists(_.startsWith(s"/${commandKey.asString}")))
      }
    }
  }
}
