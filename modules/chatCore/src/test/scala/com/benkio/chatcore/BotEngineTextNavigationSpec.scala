package com.benkio.chatcore

import cats.effect.IO
import com.benkio.chatcore.config.SBotConfig
import com.benkio.chatcore.mocks.DBLayerMock
import com.benkio.chatcore.mocks.FakePlatformAdapter
import com.benkio.chatcore.model.ChatId
import com.benkio.chatcore.model.CommandInstructionData
import com.benkio.chatcore.model.CommandKey
import com.benkio.chatcore.model.CommandTrigger
import com.benkio.chatcore.model.Message
import com.benkio.chatcore.model.SBotInfo
import com.benkio.chatcore.model.reply.EffectfulKey
import com.benkio.chatcore.model.reply.EffectfulReply
import com.benkio.chatcore.model.reply.PagedItem
import com.benkio.chatcore.model.reply.PagedList
import com.benkio.chatcore.model.reply.ReplyBundleCommand
import com.benkio.chatcore.model.reply.Text
import com.benkio.chatcore.Logger.given
import com.benkio.chatcore.TestIds
import munit.CatsEffectSuite
import org.http4s.syntax.literals.*
import org.http4s.Uri

/** The engine on a platform without buttons: the text commands `!next`, `!prev` and `!pick` act on the paged list the
  * engine remembers per chat when it sends it (see [[PagedListTextCommands]]).
  */
class BotEngineTextNavigationSpec extends CatsEffectSuite {

  private val sBotInfo: SBotInfo = SBotInfo(botId = SBotInfo.SBotId("eng"), botName = SBotInfo.SBotName("EngineBot"))

  private val sBotConfig: SBotConfig = SBotConfig(
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

  private val searchShowCommand = ReplyBundleCommand(
    trigger = CommandTrigger("searchshow"),
    reply = EffectfulReply(EffectfulKey.SearchShow(sBotInfo, 0), replyToMessage = true),
    instruction = CommandInstructionData.NoInstructions
  )

  private def pagedList(page: Int): PagedList = PagedList(
    title = "Input (2):\n!searchshow foo\n\nfoo",
    items = List(PagedItem.ValueItem(Text("one")), PagedItem.ValueItem(Text("two"))),
    page = page,
    commandKey = CommandKey.SearchShow
  )

  private def message(text: String, chatId: ChatId = TestIds.chatId(1L)): Message = Message(
    messageId = None,
    date = 0L,
    chatId = chatId,
    chatType = "private",
    text = Some(text)
  )

  private def engineFor(): BotEngine[IO] = new BotEngine[IO](
    sBotConfig = sBotConfig,
    dbLayer = DBLayerMock.mock(sBotInfo.botId, commandReplies = List(searchShowCommand)),
    filteringMatchesMessages = (_, _) => IO.pure(true),
    postComputation = _ => IO.unit,
    notifier = Notifier.noop[IO]
  )

  // the paged list adapted to the platform without buttons: the text of the list, with the hint of how to move it
  private val listText: Text =
    Text(PagedList.toPlainText(pagedList(0), Some(FakePlatformAdapter.textOnlyCapabilities.commandSyntax)))

  test("the paged list is remembered when the engine sends it and !next reruns the command of the list") {
    FakePlatformAdapter(effectfulValue = pagedList(0)).flatMap { adapter =>
      val engine = engineFor()
      for {
        _         <- engine.onMessage(message("!searchshow foo"))(using adapter)
        sent      <- adapter.sent.get
        _         <- engine.onMessage(message("!next"))(using adapter)
        sentAfter <- adapter.sent.get
      } yield {
        assertEquals(sent.map(_.replyValue), List(listText))
        assertEquals(sentAfter.map(_.replyValue).distinct, List(listText))
        assertEquals(sentAfter.size, 2)
      }
    }
  }

  test("!pick 2 picks the item of the page of the remembered list") {
    FakePlatformAdapter(effectfulValue = pagedList(0)).flatMap { adapter =>
      val engine = engineFor()
      for {
        _    <- engine.onMessage(message("!searchshow foo"))(using adapter)
        _    <- engine.onMessage(message("!pick 2"))(using adapter)
        sent <- adapter.sent.get
      } yield assertEquals(sent.map(_.replyValue), List(listText, Text("two")))
    }
  }

  test("!next with no remembered list answers the clear no-list text") {
    FakePlatformAdapter(effectfulValue = pagedList(0)).flatMap { adapter =>
      val engine = engineFor()
      for {
        _    <- engine.onMessage(message("!next"))(using adapter)
        sent <- adapter.sent.get
      } yield assertEquals(sent.map(_.replyValue), List(Text(PagedListTextCommands.noListAnswer)))
    }
  }

  test("a platform with buttons gets no text commands") {
    FakePlatformAdapter(
      capabilities = FakePlatformAdapter.textOnlyCapabilities.copy(supportsButtons = true)
    ).flatMap { adapter =>
      val engine = engineFor()
      for {
        _    <- engine.onMessage(message("!next"))(using adapter)
        sent <- adapter.sent.get
      } yield assert(sent.isEmpty, sent.toString)
    }
  }
}
