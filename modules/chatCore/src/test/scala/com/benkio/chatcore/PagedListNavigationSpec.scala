package com.benkio.chatcore

import cats.effect.IO
import com.benkio.chatcore.mocks.BackgroundJobManagerMock
import com.benkio.chatcore.mocks.DBLayerMock
import com.benkio.chatcore.mocks.FakePlatformAdapter
import com.benkio.chatcore.model.reply.Mp3File
import com.benkio.chatcore.model.reply.PagedListAction
import com.benkio.chatcore.model.reply.ReplyBundleCommand
import com.benkio.chatcore.model.reply.ReplyValue
import com.benkio.chatcore.model.reply.Text
import com.benkio.chatcore.model.reply.TextReply
import com.benkio.chatcore.model.CommandInstructionData
import com.benkio.chatcore.model.CommandKey
import com.benkio.chatcore.model.CommandTrigger
import com.benkio.chatcore.model.Message
import com.benkio.chatcore.model.SBotInfo
import com.benkio.chatcore.repository.db.DBShowData
import com.benkio.chatcore.Logger.given
import munit.CatsEffectSuite

class PagedListNavigationSpec extends CatsEffectSuite {

  private val botId   = SBotInfo.SBotId("bot")
  private val message = Message(None, 0L, TestIds.chatId(1L), "private")

  private def showData(id: String): DBShowData = DBShowData(
    show_id = id,
    bot_id = botId.value,
    show_title = "A show",
    show_upload_date = "2020-05-17T11:46:11.000Z",
    show_duration = 3600,
    show_description = Some("a description"),
    show_is_live = false,
    show_origin_automatic_caption = None,
    show_origin_automatic_caption_srt = ""
  )

  private def reply(
      dbShows: List[DBShowData],
      action: PagedListAction,
      commands: List[ReplyBundleCommand] = List.empty
  ): IO[ReplyValue] =
    PagedListNavigation.reply[IO](
      action = action,
      msg = message,
      allCommandRepliesData = commands,
      backgroundJobManager = BackgroundJobManagerMock.mock(),
      effectfulCallbacks = Map.empty,
      dbLayer = DBLayerMock.mock(botId, shows = dbShows),
      ttl = None,
      capabilities = FakePlatformAdapter.textOnlyCapabilities
    )

  test("nextPage and previousPage move within the pages") {
    assertEquals(PagedListNavigation.nextPage(0), 1)
    assertEquals(PagedListNavigation.previousPage(2), 1)
    assertEquals(PagedListNavigation.previousPage(0), 0)
  }

  test("a page action reruns the command for the new page") {
    val commandReply = ReplyBundleCommand(
      trigger = CommandTrigger(CommandKey.Random.asString),
      reply = TextReply(Set(Text("page content"))),
      instruction = CommandInstructionData.NoInstructions
    )

    for {
      next <- reply(List.empty, PagedListAction.NextPage(0, CommandKey.Random), List(commandReply))
      prev <- reply(List.empty, PagedListAction.PreviousPage(2, CommandKey.Random), List(commandReply))
    } yield {
      assertEquals(next, Text("page content"): ReplyValue)
      assertEquals(prev, Text("page content"): ReplyValue)
    }
  }

  test("a page action for a command that does not exist fails") {
    for {
      next <- reply(List.empty, PagedListAction.NextPage(0, CommandKey.Random)).attempt
      prev <- reply(List.empty, PagedListAction.PreviousPage(2, CommandKey.Random)).attempt
    } yield {
      assert(next.isLeft)
      assert(prev.isLeft)
    }
  }

  test("the reply of a picked media is the media file") {
    reply(dbShows = List.empty, action = PagedListAction.Media("audio.mp3")).map { value =>
      assertEquals(value, Mp3File("audio.mp3"): ReplyValue)
    }
  }

  test("the reply of a picked show is the text of the show") {
    reply(dbShows = List(showData("show-1")), action = PagedListAction.Show("show-1", None)).map {
      case text: Text =>
        assert(text.value.contains("A show"), text.value)
        assert(text.value.contains("https://www.youtube.com/watch?v=show-1"), text.value)
      case other => fail(s"expected a Text, got: $other")
    }
  }

  test("the text of a show carries the timestamp when the action has one") {
    reply(dbShows = List(showData("show-1")), action = PagedListAction.Show("show-1", Some("1m30s"))).map {
      case text: Text => assert(text.value.contains("&t=1m30s"), text.value)
      case other      => fail(s"expected a Text, got: $other")
    }
  }

  test("the text of a show that does not exist is an error message") {
    PagedListNavigation.showText[IO]("missing", None, DBLayerMock.mock(botId).dbShow).map { text =>
      assertEquals(text.value, "Unexpected error when fetching the show")
    }
  }
}
