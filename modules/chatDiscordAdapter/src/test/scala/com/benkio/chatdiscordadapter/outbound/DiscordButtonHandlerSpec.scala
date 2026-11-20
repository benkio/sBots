package com.benkio.chatdiscordadapter.outbound

import cats.effect.kernel.Ref
import cats.effect.IO
import com.benkio.chatcore.model.reply.Mp3File
import com.benkio.chatcore.model.reply.PagedItem
import com.benkio.chatcore.model.reply.PagedList
import com.benkio.chatcore.model.reply.PagedListAction
import com.benkio.chatcore.model.reply.ReplyValue
import com.benkio.chatcore.model.reply.Text
import com.benkio.chatcore.model.ChatId
import com.benkio.chatcore.model.CommandKey
import com.benkio.chatcore.model.Message
import com.benkio.chatcore.model.Platform
import com.benkio.chatcore.Logger.given
import munit.CatsEffectSuite

import java.nio.file.Path

class DiscordButtonHandlerSpec extends CatsEffectSuite {

  private val request = Message(None, 0L, ChatId(Platform.Discord, "9"), "group", text = Some("/toptwenty"))

  private def pagedList(page: Int): PagedList =
    PagedList("Top", (1 to 12).toList.map(i => PagedItem.ValueItem(Mp3File(s"f$i.mp3"))), page, CommandKey.TopTwenty)

  private class RecordingApi(calls: Ref[IO, List[String]]) extends DiscordApi[IO] {
    override def sendText(channelId: String, text: String, replyToMessageId: Option[String]) = IO.pure("x")
    override def sendFile(channelId: String, fileName: String, file: Path, replyToMessageId: Option[String]) =
      IO.pure("x")
    override def deleteMessage(channelId: String, messageId: String) = calls.update(_ :+ s"delete:$messageId")
    override def sendButtons(channelId: String, message: DiscordButtonMessage, replyToMessageId: Option[String]) =
      IO.pure("x")
    override def editButtons(channelId: String, messageId: String, message: DiscordButtonMessage) =
      calls.update(_ :+ s"edit:$messageId:${message.rows.head.map(_.label).mkString(",")}")
  }

  private case class Fixture(handler: DiscordButtonHandler[IO], calls: Ref[IO, List[String]], lists: DiscordButtonLists[IO])

  private def fixture: IO[Fixture] =
    for {
      calls <- Ref.of[IO, List[String]](List.empty)
      lists <- DiscordButtonLists.create[IO]()
      _     <- lists.put("m1", DiscordButtonList(Some("alice"), request, pagedList(0)))
      handler = new DiscordButtonHandler[IO](
        api = new RecordingApi(calls),
        lists = lists,
        navigate = (action, _) =>
          IO.pure(
            newPage(action)
              .fold[ReplyValue](Text(s"picked $action"))(page => pagedList(page))
          ),
        replies = (value, _) => calls.update(_ :+ s"reply:$value")
      )
    } yield Fixture(handler, calls, lists)

  private def newPage(action: PagedListAction): Option[Int] =
    com.benkio.chatcore.PagedListNavigation.newPage(action)

  private def press(user: String, action: PagedListAction, messageId: String = "m1"): DiscordButtonPress =
    DiscordButtonPress("9", messageId, user, action.toId.get)

  test("next edits the message in place with the next page, the list is remembered at the new page") {
    for {
      f      <- fixture
      d      <- f.handler.decide(press(user = "alice", action = PagedListAction.NextPage(0, CommandKey.TopTwenty)))
      _      <- d.toOption.get
      calls  <- f.calls.get
      stored <- f.lists.get("m1")
    } yield {
      assertEquals(calls, List("edit:m1:6,7,8,9,10"))
      assertEquals(stored.map(_.pagedList.page), Some(1))
    }
  }

  test("previous at the first page stays at the first page") {
    for {
      f     <- fixture
      d     <- f.handler.decide(press(user = "alice", action = PagedListAction.PreviousPage(0, CommandKey.TopTwenty)))
      _     <- d.toOption.get
      calls <- f.calls.get
    } yield assertEquals(calls, List("edit:m1:1,2,3,4,5"))
  }

  test("a pick deletes the list message and sends the item") {
    for {
      f      <- fixture
      action = PagedListAction.Media("f3.mp3")
      d      <- f.handler.decide(press(user = "alice", action = action))
      _      <- d.toOption.get
      calls  <- f.calls.get
      stored <- f.lists.get("m1")
    } yield {
      assertEquals(calls, List("delete:m1", s"reply:Text(picked $action,None)"))
      assertEquals(stored, None)
    }
  }

  test("only who asked can use the buttons, nothing changes for the others") {
    for {
      f     <- fixture
      d     <- f.handler.decide(press(user = "bob", action = PagedListAction.NextPage(0, CommandKey.TopTwenty)))
      calls <- f.calls.get
    } yield {
      assertEquals(d.left.toOption, Some(f.handler.notYours))
      assertEquals(calls, List.empty)
    }
  }

  test("a list that is gone and a button that is not valid get a clear answer") {
    for {
      f       <- fixture
      gone    <- f.handler.decide(press(user = "alice", action = PagedListAction.NextPage(0, CommandKey.TopTwenty), messageId = "other"))
      invalid <- f.handler.decide(DiscordButtonPress("9", "m1", "alice", "nonsense"))
    } yield {
      assertEquals(gone.left.toOption, Some(f.handler.listGone))
      assertEquals(invalid.left.toOption, Some(f.handler.unknownButton))
    }
  }
}
