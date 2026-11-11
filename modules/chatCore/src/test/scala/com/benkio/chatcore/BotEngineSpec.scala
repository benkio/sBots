package com.benkio.chatcore

import cats.effect.kernel.Ref
import cats.effect.IO
import cats.syntax.all.*
import com.benkio.chatcore.config.SBotConfig
import com.benkio.chatcore.mocks.DBLayerMock
import com.benkio.chatcore.mocks.FakePlatformAdapter
import com.benkio.chatcore.mocks.FakePlatformAdapter.Sent
import com.benkio.chatcore.model.reply.EffectfulKey
import com.benkio.chatcore.model.reply.EffectfulReply
import com.benkio.chatcore.model.reply.Mp3File
import com.benkio.chatcore.model.reply.ReplyBundleCommand
import com.benkio.chatcore.model.reply.ReplyBundleMessage
import com.benkio.chatcore.model.reply.Text
import com.benkio.chatcore.model.reply.TextReply
import com.benkio.chatcore.model.ChatId
import com.benkio.chatcore.model.CommandInstructionData
import com.benkio.chatcore.model.CommandSyntax
import com.benkio.chatcore.model.CommandTrigger
import com.benkio.chatcore.model.MediaKind
import com.benkio.chatcore.model.Message
import com.benkio.chatcore.model.PlatformCapabilities
import com.benkio.chatcore.model.SBotInfo
import com.benkio.chatcore.Logger.given
import munit.CatsEffectSuite
import org.http4s.syntax.literals.*
import org.http4s.Uri

import java.time.Instant

class BotEngineSpec extends CatsEffectSuite {

  private val sBotInfo: SBotInfo = SBotInfo(botId = SBotInfo.SBotId("eng"), botName = SBotInfo.SBotName("EngineBot"))

  private val sBotConfig: SBotConfig = SBotConfig(
    sBotInfo = sBotInfo,
    triggerFilename = "eng_triggers.md",
    triggerListUri = uri"https://example.com/eng_triggers.md",
    token = "eng.token",
    listJsonFilename = "eng_list.json",
    repliesJsonFilename = "eng_replies.json",
    commandsJsonFilename = "eng_commands.json",
    showFilename = "eng_shows.json"
  )

  private val messageReplies: List[ReplyBundleMessage] = List(ReplyBundleMessage.textToText("ciao")("hello"))

  private val commandReplies: List[ReplyBundleCommand] = List(
    ReplyBundleCommand(
      trigger = CommandTrigger("hi"),
      reply = TextReply.fromList("command reply")(true),
      instruction = CommandInstructionData.NoInstructions
    ),
    ReplyBundleCommand(
      trigger = CommandTrigger("effect"),
      reply = EffectfulReply(EffectfulKey.GetTimeout(sBotInfo), replyToMessage = true),
      instruction = CommandInstructionData.NoInstructions
    )
  )

  private def message(text: String): Message = Message(
    messageId = 0,
    date = Instant.now.getEpochSecond,
    chatId = ChatId.telegram(1L),
    chatType = "private",
    text = Some(text)
  )

  private def engineWith(
      filter: Boolean = true,
      postComputation: Message => IO[Unit] = _ => IO.unit
  ): BotEngine[IO] = new BotEngine[IO](
    sBotConfig = sBotConfig,
    dbLayer = DBLayerMock.mock(sBotInfo.botId, messageReplies = messageReplies, commandReplies = commandReplies),
    filteringMatchesMessages = (_, _) => IO.pure(filter),
    postComputation = postComputation
  )

  // A text only platform that writes the commands like Telegram, as `/command`
  private val slashCommands: PlatformCapabilities =
    FakePlatformAdapter.textOnlyCapabilities.copy(commandSyntax = CommandSyntax.Telegram)

  // The same, able to send audio files
  private val audioCapable: PlatformCapabilities = slashCommands.copy(supportedMedia = Set(MediaKind.Audio))

  /** Runs the engine on the message and gives back what is sent and how many times the post computation ran. */
  private def run(
      text: String,
      filter: Boolean = true,
      capabilities: PlatformCapabilities = slashCommands
  ): IO[(List[Sent], Int)] = for {
    adapter      <- FakePlatformAdapter(capabilities = capabilities)
    postComputed <- Ref.of[IO, Int](0)
    engine = engineWith(filter, _ => postComputed.update(_ + 1))
    _      <- engine.onMessage(message(text))(using adapter)
    result <- adapter.sent.get
    count  <- postComputed.get
  } yield (result, count)

  test("a message that matches a trigger gets the reply of the bundle") {
    run(text = "ciao").map { case (sent, _) =>
      assertEquals(sent.map(s => (s.replyValue, s.replyToMessage)), List((Text("hello"), false)))
      assertEquals(sent.map(_.message.text), List(Some("ciao")))
    }
  }

  test("a message that matches no trigger gets no reply") {
    run(text = "something else").map { case (sent, _) => assertEquals(sent, List.empty) }
  }

  test("a reply bundle that is filtered out gets no reply") {
    run(text = "ciao", filter = false).map { case (sent, _) => assertEquals(sent, List.empty) }
  }

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

  test("the post computation runs once for every message, whether it got a reply or not") {
    List("ciao", "something else", "/hi", "/unknown", "eng_file.mp3").traverse(run(_)).map { results =>
      assertEquals(results.map(_._2), List.fill(5)(1))
    }
  }

  test("the commands of the bot include the fixed ones: the bundles and the instructions") {
    val engine = engineWith()
    assertEquals(engine.commandRepliesData, commandReplies)
    assertEquals(engine.allCommandRepliesData, commandReplies ++ engine.fixedCommands)
    assert(engine.fixedCommands.exists(_.trigger.command == "instructions"))
  }

  test("the messages the platform delivers when it runs are handled by the engine and answered through the platform") {
    for {
      adapter <- FakePlatformAdapter(
        inbound = List(message("ciao"), message("/hi"), message("something else")),
        capabilities = slashCommands
      )
      engine = engineWith()
      _    <- adapter.onInbound(msg => engine.onMessage(msg)(using adapter))
      _    <- adapter.run
      sent <- adapter.sent.get
    } yield assertEquals(sent.map(_.replyValue), List(Text("hello"), Text("command reply")))
  }

  test("a platform that runs without an engine fails") {
    FakePlatformAdapter().flatMap(_.run).intercept[IllegalStateException]
  }

  test("the platform of the adapter is the one of its capabilities") {
    FakePlatformAdapter().map(adapter => assertEquals(adapter.platform, adapter.capabilities.platform))
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
