package com.benkio.chatcore

import cats.effect.IO
import com.benkio.chatcore.config.SBotConfig
import com.benkio.chatcore.mocks.DBLayerMock
import com.benkio.chatcore.mocks.FakePlatformAdapter
import com.benkio.chatcore.model.reply.GifFile
import com.benkio.chatcore.model.reply.Mp3File
import com.benkio.chatcore.model.reply.PagedItem
import com.benkio.chatcore.model.reply.PagedList
import com.benkio.chatcore.model.reply.ReplyBundleMessage
import com.benkio.chatcore.model.reply.ReplyValue
import com.benkio.chatcore.model.reply.Text
import com.benkio.chatcore.model.CommandKey
import com.benkio.chatcore.model.CommandSyntax
import com.benkio.chatcore.model.MediaKind
import com.benkio.chatcore.model.Message
import com.benkio.chatcore.model.PlatformCapabilities
import com.benkio.chatcore.model.SBotInfo
import com.benkio.chatcore.repository.db.DBMediaData
import com.benkio.chatcore.Logger.given
import com.benkio.chatcore.TestIds
import munit.CatsEffectSuite
import org.http4s.syntax.literals.*
import org.http4s.Uri

import java.time.Instant

/** What reaches the platform: the replies of the engine are adapted to what the platform can do. */
class BotEngineAdaptationSpec extends CatsEffectSuite {

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

  private def mediaData(name: String): DBMediaData = DBMediaData(
    media_name = name,
    bot_id = "eng",
    kinds = "[]",
    mime_type = "audio/mpeg",
    media_sources = s"""["https://example.com/$name"]""",
    media_count = 0,
    created_at = "1"
  )

  private val messageReplies = List(
    ReplyBundleMessage.textToMp3("song")(Mp3File("eng_song.mp3")),
    ReplyBundleMessage.textToGif("clip")(GifFile("eng_unknownGif.mp4"))
  )

  private val textOnly: PlatformCapabilities = FakePlatformAdapter.textOnlyCapabilities.copy(
    commandSyntax = CommandSyntax.Slash
  )
  private val withButtonsAndAudio: PlatformCapabilities =
    textOnly.copy(supportsButtons = true, supportedMedia = Set(MediaKind.Audio))

  private def message(text: String): Message =
    Message(None, Instant.now.getEpochSecond, TestIds.chatId(1L), "private", text = Some(text))

  /** What the platform receives when the engine handles the text. */
  private def received(
      text: String,
      capabilities: PlatformCapabilities,
      effectful: ReplyValue = Text("effectful")
  ): IO[List[ReplyValue]] = for {
    adapter <- FakePlatformAdapter(capabilities = capabilities, effectfulValue = effectful)
    engine = new BotEngine[IO](
      sBotConfig = sBotConfig,
      dbLayer = DBLayerMock.mock(
        sBotInfo.botId,
        medias = List(mediaData("eng_song.mp3")),
        messageReplies = messageReplies,
        commandReplies = List(
          com.benkio.chatcore.model.reply.ReplyBundleCommand(
            trigger = com.benkio.chatcore.model.CommandTrigger("list"),
            reply = com.benkio.chatcore.model.reply.EffectfulReply(
              com.benkio.chatcore.model.reply.EffectfulKey.TopTwenty(sBotInfo)
            ),
            instruction = com.benkio.chatcore.model.CommandInstructionData.NoInstructions
          )
        )
      ),
      filteringMatchesMessages = (_, _) => IO.pure(true),
      postComputation = _ => IO.unit
    )
    _    <- engine.onMessage(message(text))(using adapter)
    sent <- adapter.sent.get
  } yield sent.map(_.replyValue)

  private val items: PagedList = PagedList(
    title = "Headline",
    items = List(PagedItem.ValueItem(Text("first")), PagedItem.ValueItem(Text("second"))),
    page = 0,
    commandKey = CommandKey.TopTwenty
  )

  test("a paged list is sent as a numbered text to a platform without buttons") {
    received(text = "/list", capabilities = textOnly, effectful = items).map(sent =>
      assertEquals(sent, List(Text("Headline\n1. first\n2. second")))
    )
  }

  test("a paged list is sent as it is to a platform with buttons") {
    received(text = "/list", capabilities = withButtonsAndAudio, effectful = items).map(sent =>
      assertEquals(sent, List(items))
    )
  }

  test("a media the platform cannot send is sent as its link") {
    received(text = "song", capabilities = textOnly).map(sent =>
      assertEquals(sent, List(Text("https://example.com/eng_song.mp3")))
    )
  }

  test("a media the platform cannot send and the DB does not know is not sent") {
    received(text = "clip", capabilities = textOnly).map(sent => assertEquals(sent, List.empty))
  }

  test("a media the platform can send is sent as it is") {
    received(text = "song", capabilities = withButtonsAndAudio).map(sent =>
      assertEquals(sent, List(Mp3File("eng_song.mp3")))
    )
  }

  test("a file request for a media the platform cannot send is answered with its link") {
    received(text = "eng_song.mp3", capabilities = textOnly).map(sent =>
      assertEquals(sent, List(Text("https://example.com/eng_song.mp3")))
    )
  }
}
