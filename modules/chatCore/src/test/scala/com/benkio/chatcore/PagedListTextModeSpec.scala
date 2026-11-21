package com.benkio.chatcore

import cats.effect.IO
import com.benkio.chatcore.mocks.DBLayerMock
import com.benkio.chatcore.mocks.FakePlatformAdapter
import com.benkio.chatcore.model.reply.EffectfulKey
import com.benkio.chatcore.model.reply.EffectfulReply
import com.benkio.chatcore.model.reply.PagedItem
import com.benkio.chatcore.model.reply.PagedList
import com.benkio.chatcore.model.reply.ReplyBundleCommand
import com.benkio.chatcore.model.reply.Text
import com.benkio.chatcore.model.ChatId
import com.benkio.chatcore.model.CommandInstructionData
import com.benkio.chatcore.model.CommandKey
import com.benkio.chatcore.model.CommandTrigger
import com.benkio.chatcore.model.Message
import com.benkio.chatcore.model.SBotInfo
import com.benkio.chatcore.TestIds
import munit.CatsEffectSuite

import scala.concurrent.duration.*

/** The text mode owns the remembered pages: the commands act on the list the component itself stored. */
class PagedListTextModeSpec extends CatsEffectSuite {

  private val sBotInfo: SBotInfo = SBotInfo(botId = SBotInfo.SBotId("eng"), botName = SBotInfo.SBotName("EngineBot"))

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

  private def textMode = new PagedListTextMode[IO](
    botName = sBotInfo.botName,
    allCommandRepliesData = () => List(searchShowCommand),
    dbLayer = DBLayerMock.mock(sBotInfo.botId, commandReplies = List(searchShowCommand)),
    expiry = 10.minutes,
    maxChats = 100
  )

  private val listText: Text =
    Text(PagedList.toPlainText(pagedList(0), Some(FakePlatformAdapter.textOnlyCapabilities.commandSyntax)))

  test("a paged list sent by the component is remembered and !next reruns its command") {
    FakePlatformAdapter(effectfulValue = pagedList(0)).flatMap { adapter =>
      val mode = textMode
      for {
        _     <- mode.remember(message("!searchshow foo"), pagedList(0))(using adapter)
        sent  <- adapter.sent.get
        _     <- mode.handleCommand(message("!next"))(using adapter)
        after <- adapter.sent.get
      } yield {
        assertEquals(sent, Nil)
        assertEquals(after.map(_.replyValue), List(listText))
      }
    }
  }

  test("!pick picks the item of the remembered page") {
    FakePlatformAdapter(effectfulValue = pagedList(0)).flatMap { adapter =>
      val mode = textMode
      for {
        _       <- mode.remember(message("!searchshow foo"), pagedList(0))(using adapter)
        handled <- mode.handleCommand(message("!pick 2"))(using adapter)
        sent    <- adapter.sent.get
      } yield {
        assertEquals(handled, true)
        assertEquals(sent.map(_.replyValue), List(Text("two")))
      }
    }
  }

  test("!next with no remembered list answers the clear no-list text") {
    FakePlatformAdapter(effectfulValue = pagedList(0)).flatMap { adapter =>
      for {
        handled <- textMode.handleCommand(message("!next"))(using adapter)
        sent    <- adapter.sent.get
      } yield {
        assertEquals(handled, true)
        assertEquals(sent.map(_.replyValue), List(Text(PagedListTextCommands.noListAnswer)))
      }
    }
  }

  test("a platform with buttons is left alone, and its lists are not remembered for text commands") {
    val capabilities = FakePlatformAdapter.textOnlyCapabilities.copy(supportsButtons = true)
    FakePlatformAdapter(capabilities = capabilities, effectfulValue = pagedList(0)).flatMap { adapter =>
      val mode = textMode
      for {
        handled <- mode.handleCommand(message("!next"))(using adapter)
        _       <- mode.remember(message("!searchshow foo"), pagedList(0))(using adapter)
        sent    <- adapter.sent.get
      } yield {
        assertEquals(handled, false)
        assertEquals(sent, Nil)
      }
    }
  }
}
