package com.benkio.XahLeeBot

import cats.effect.IO
import com.benkio.chatcore.config.SBotConfig
import com.benkio.chatcore.mocks.DBLayerMock
import com.benkio.chatcore.mocks.FakePlatformAdapter
import com.benkio.chatcore.model.reply.EffectfulKey
import com.benkio.chatcore.model.reply.EffectfulReply
import com.benkio.chatcore.model.reply.PlainReply
import com.benkio.chatcore.model.reply.ReplyBundleCommand
import com.benkio.chatcore.model.reply.ReplyBundleMessage
import com.benkio.chatcore.model.reply.Text
import com.benkio.chatcore.model.CommandSyntax
import com.benkio.chatcore.model.Message
import com.benkio.chatcore.repository.db.DBLayer
import com.benkio.chatcore.BaseBotSpec
import com.benkio.chatcore.BotEngine
import com.benkio.chatcore.Logger.given
import com.benkio.chatcore.TestIds

import java.time.Instant

class XahLeeBotSpec extends BaseBotSpec {

  val xahSBotConfig             = SBotConfig.fromSBotInfo(XahLeeBot.sBotInfo)
  val emptyDBLayer: DBLayer[IO] = DBLayerMock.mock(XahLeeBot.sBotInfo.botId)

  val xahLeeBot: IO[BotEngine[IO]] = buildTestEngine(dbLayer = emptyDBLayer, sBotConfig = xahSBotConfig)

  val commandRepliesData: IO[List[ReplyBundleCommand]] =
    xahLeeBot.map(_.allCommandRepliesData)
  // The files of the bot: the keywords of the commands reply with an effectful reply, there are no files to check
  val messageRepliesDataPrettyPrint: IO[List[String]] =
    xahLeeBot.map(_.messageRepliesData.map(_.reply).collect { case plain: PlainReply => plain }.flatMap(_.prettyPrint))

  botJsonsAreValid(xahSBotConfig)
  jsonContainsFilenames(
    jsonFilename = "xah_list.json",
    botData = messageRepliesDataPrettyPrint
  )

  instructionsCommandTest(
    commandRepliesDataF = commandRepliesData,
    instructionsUri = "https://github.com/benkio/sBots/blob/main/modules/bots/XahLeeBot/xah_instructions.md"
  )

  private def message(text: String): Message =
    Message(
      messageId = None,
      date = Instant.now.getEpochSecond,
      chatId = TestIds.chatId(1L),
      chatType = "private",
      text = Some(text)
    )

  /** The media kind the bot answers to the text, if the text is a keyword of a command. */
  private def keywordKind(text: String): IO[Option[String]] =
    xahLeeBot.map(engine =>
      ReplyBundleMessage
        .selectReplyBundle(
          msg = message(text),
          messageRepliesData = engine.messageRepliesData,
          ignoreMessagePrefix = xahSBotConfig.ignoreMessagePrefix,
          disableForward = xahSBotConfig.disableForward
        )
        .map(_.reply)
        .collect { case EffectfulReply(EffectfulKey.MediaByKind(kind, _), _) => kind }
    )

  test("every keyword reply does what the command with the same name does") {
    for {
      engine <- xahLeeBot
      keywordReplies = engine.messageRepliesData.map(_.reply)
      commandReplies = engine.commandRepliesData.map(_.reply)
    } yield {
      assert(keywordReplies.nonEmpty, "the bot should reply to the keywords of its commands")
      keywordReplies.foreach(reply =>
        assert(commandReplies.contains(reply), s"No command has the reply of the keyword: $reply")
      )
    }
  }

  List(
    "fuck"                    -> "fak",
    "fuck this"               -> "fak",
    "what the fuck is this"   -> "wtf",
    "what the fuck"           -> "wtf",
    "What the fak"            -> "wtf",
    "fak"                     -> "fak",
    "fuckhead"                -> "fakhead",
    "alan mckenzie"           -> "alanmackenzie",
    "Alan Mackenzie wrote it" -> "alanmackenzie",
    "richard stallman"        -> "richardstallman",
    "idiots"                  -> "idiots",
    "idiot"                   -> "idiots",
    "i use Linux btw"         -> "linux",
    "open source"             -> "opensource",
    "wtf"                     -> "wtf",
    "c++ is hard"             -> "ccpp",
    "emacs"                   -> "emacs",
    "python"                  -> "python",
    "zoomer"                  -> "zoomer",
    "windows"                 -> "windows"
  ).foreach { case (text, kind) =>
    test(s"the keyword `$text` is answered with the media of the kind `$kind`") {
      keywordKind(text).map(assertEquals(_, Some(kind)))
    }
  }

  List("a fake story", "the class", "i pass", "assume", "pineapple", "scrapbook", "operazione", "hello").foreach {
    text =>
      test(s"`$text` contains no keyword, only a part of a word, so the bot stays quiet") {
        keywordKind(text).map(assertEquals(_, None))
      }
  }

  test("a keyword is answered like the command: the effectful reply of the kind runs") {
    for {
      adapter <- FakePlatformAdapter(capabilities =
        FakePlatformAdapter.textOnlyCapabilities.copy(commandSyntax = CommandSyntax.Slash)
      )
      engine <- xahLeeBot
      _      <- engine.onMessage(message("linux"))(using adapter)
      sent   <- adapter.sent.get
    } yield assertEquals(sent.map(_.replyValue), List(Text("effectful")))
  }

}
