package com.benkio.chatcore

import cats.effect.kernel.Ref
import cats.effect.IO
import com.benkio.chatcore.config.SBotConfig
import com.benkio.chatcore.llm.LlmSettings
import com.benkio.chatcore.mocks.BackgroundJobManagerMock
import com.benkio.chatcore.mocks.DBLayerMock
import com.benkio.chatcore.mocks.FakePlatformAdapter
import com.benkio.chatcore.mocks.FakePlatformAdapter.Sent
import com.benkio.chatcore.model.reply.EffectfulKey
import com.benkio.chatcore.model.reply.EffectfulReply
import com.benkio.chatcore.model.reply.ReplyBundleCommand
import com.benkio.chatcore.model.reply.ReplyBundleMessage
import com.benkio.chatcore.model.reply.TextReply
import com.benkio.chatcore.model.CommandInstructionData
import com.benkio.chatcore.model.CommandSyntax
import com.benkio.chatcore.model.CommandTrigger
import com.benkio.chatcore.model.MediaKind
import com.benkio.chatcore.model.Message
import com.benkio.chatcore.model.MessageId
import com.benkio.chatcore.model.PlatformCapabilities
import com.benkio.chatcore.model.SBotInfo
import com.benkio.chatcore.repository.db.DBProcessedMessage
import com.benkio.chatcore.Logger.given
import com.benkio.chatcore.TestIds
import org.http4s.syntax.literals.*
import org.http4s.Uri

import java.time.Instant

/** The engine, its configuration and the helpers the specs of the sections of the [[BotEngine]] share. */
trait BotEngineFixtures { self: munit.Assertions =>

  protected val sBotInfo: SBotInfo = SBotInfo(botId = SBotInfo.SBotId("eng"), botName = SBotInfo.SBotName("EngineBot"))

  protected val sBotConfig: SBotConfig = SBotConfig(
    sBotInfo = sBotInfo,
    triggerFilename = "eng_triggers.md",
    triggerListUri = uri"https://example.com/eng_triggers.md",
    instructionsFilename = "eng_instructions.md",
    instructionsUri = uri"https://example.com/eng_instructions.md",
    token = "eng.token",
    listJsonFilename = "eng_list.json",
    repliesJsonFilename = "eng_replies.json",
    commandsJsonFilename = "eng_commands.json",
    showFilename = "eng_shows.json"
  )

  protected val messageReplies: List[ReplyBundleMessage] = List(ReplyBundleMessage.textToText("ciao")("hello"))

  protected val commandReplies: List[ReplyBundleCommand] = List(
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

  protected def message(text: String, messageId: Option[MessageId] = None): Message = Message(
    messageId = messageId,
    date = Instant.now.getEpochSecond,
    chatId = TestIds.chatId(1L),
    chatType = "private",
    text = Some(text)
  )

  protected def engineWith(
      filter: Boolean = true,
      postComputation: Message => IO[Unit] = _ => IO.unit,
      config: SBotConfig = sBotConfig,
      processedMessages: DBProcessedMessage[IO] = DBLayerMock.DBProcessedMessageMock(Ref.unsafe(Set.empty)),
      notifier: Notifier[IO] = Notifier.noop[IO],
      llmEngine: Option[LlmEngine[IO]] = None,
      llmSettings: LlmSettings[IO] = LlmSettings.noop[IO]
  ): BotEngine[IO] = new BotEngine[IO](
    sBotConfig = config,
    dbLayer = DBLayerMock.mock(
      sBotInfo.botId,
      messageReplies = messageReplies,
      commandReplies = commandReplies,
      processedMessages = processedMessages,
      llmSettings = llmSettings
    ),
    filteringMatchesMessages = (_, _) => IO.pure(filter),
    postComputation = postComputation,
    notifier = notifier,
    backgroundJobManager = BackgroundJobManagerMock.mock(),
    llmEngine = llmEngine
  )

  // A text only platform that writes the commands like Telegram, as `/command`
  protected val slashCommands: PlatformCapabilities =
    FakePlatformAdapter.textOnlyCapabilities.copy(commandSyntax = CommandSyntax.Slash)

  // The same, able to send audio files
  protected val audioCapable: PlatformCapabilities = slashCommands.copy(supportedMedia = Set(MediaKind.Audio))

  /** Runs the engine on the message and gives back what is sent and how many times the post computation ran. */
  protected def run(
      text: String,
      filter: Boolean = true,
      capabilities: PlatformCapabilities = slashCommands
  ): IO[(List[Sent], Int)] = for {
    adapter      <- FakePlatformAdapter(capabilities = capabilities)
    postComputed <- Ref.of[IO, Int](0)
    engine = engineWith(filter = filter, postComputation = _ => postComputed.update(_ + 1))
    _      <- engine.onMessage(message(text))(using adapter)
    result <- adapter.sent.get
    count  <- postComputed.get
  } yield (result, count)
}
