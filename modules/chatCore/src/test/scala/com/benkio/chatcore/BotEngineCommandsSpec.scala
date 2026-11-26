package com.benkio.chatcore

import com.benkio.chatcore.mocks.FakePlatformAdapter
import com.benkio.chatcore.model.reply.Mp3File
import com.benkio.chatcore.model.reply.Text
import com.benkio.chatcore.model.CommandSyntax
import munit.CatsEffectSuite

class BotEngineCommandsSpec extends CatsEffectSuite with BotEngineFixtures {

  test("a command gets the reply of the command bundle, replying to the message when asked") {
    run(text = "/hi").map { case (sent, _) =>
      assertEquals(sent.map(s => (s.replyValue, s.replyToMessage)), List((Text("command reply"), true)))
    }
  }

  test("an unknown command gets no reply") {
    run(text = "/unknown").map { case (sent, _) => assertEquals(sent, List.empty) }
  }

  test("an effectful command is computed by the platform and its value is sent") {
    run(text = "/effect").map { case (sent, _) =>
      assertEquals(sent.map(s => (s.replyValue, s.replyToMessage)), List((Text("effectful"), true)))
    }
  }

  test("a file request sends the media file, replying to the message") {
    run(text = "eng_file.mp3", capabilities = audioCapable).map { case (sent, _) =>
      assertEquals(sent.map(s => (s.replyValue, s.replyToMessage)), List((Mp3File("eng_file.mp3"), true)))
    }
  }

  test("the commands of the bot include the fixed ones: the bundles and the instructions") {
    val engine = engineWith()
    assertEquals(engine.commandRepliesData, commandReplies)
    assertEquals(engine.allCommandRepliesData, commandReplies ++ engine.fixedCommands)
    assert(engine.fixedCommands.exists(_.trigger.command == "instructions"))
  }

  test("the commands are written as the platform writes them: `!command` where the prefix is `!`") {
    // FakePlatformAdapter is a text only platform with the `!` prefix
    for {
      bang       <- run(text = "!hi", capabilities = FakePlatformAdapter.textOnlyCapabilities)
      slash      <- run(text = "/hi", capabilities = FakePlatformAdapter.textOnlyCapabilities)
      mention    <- run(text = "!hi@EngineBot", capabilities = FakePlatformAdapter.textOnlyCapabilities)
      effectful  <- run(text = "!effect", capabilities = FakePlatformAdapter.textOnlyCapabilities)
      telegram   <- run(text = "!hi")
      telegramHi <- run(text = "/hi@EngineBot")
    } yield {
      assertEquals(bang._1.map(_.replyValue), List(Text("command reply")))
      assertEquals(slash._1, List.empty)
      assertEquals(mention._1, List.empty)
      assertEquals(effectful._1.map(_.replyValue), List(Text("effectful")))
      assertEquals(telegram._1, List.empty)
      assertEquals(telegramHi._1.map(_.replyValue), List(Text("command reply")))
    }
  }

  test("a file request starts with the marker of the platform") {
    val withMarker = audioCapable.copy(commandSyntax = CommandSyntax(prefix = "!", fileRequestMarker = "+"))
    for {
      marked <- run(text = "+eng_file.mp3", capabilities = withMarker)
      plain  <- run(text = "eng_file.mp3", capabilities = withMarker)
    } yield {
      assertEquals(marked._1.map(s => (s.replyValue, s.replyToMessage)), List((Mp3File("eng_file.mp3"), true)))
      assertEquals(plain._1, List.empty)
    }
  }
}
