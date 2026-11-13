package com.benkio.chatircadapter.outbound

import cats.effect.IO
import cats.effect.Ref
import com.benkio.chatcore.model.reply.*
import com.benkio.chatcore.model.ChatId
import com.benkio.chatcore.model.Message
import com.benkio.chatcore.model.Platform
import com.benkio.chatcore.Logger.given
import com.benkio.chatircadapter.model.IrcCapabilities
import munit.CatsEffectSuite

import scala.concurrent.duration.*

class IrcRepliesSpec extends CatsEffectSuite {

  private def message(chat: String): Message =
    Message(None, 0L, ChatId(Platform.Irc, chat), "group", text = Some("hi"))

  private def run(value: ReplyValue, chat: String = "#sbots", api: Ref[IO, List[String]] => IrcApi[IO] = recording) =
    for {
      sent <- Ref.of[IO, List[String]](List.empty)
      _    <- new IrcReplies[IO](api(sent), IrcCapabilities.value).send(value, message(chat), replyToMessage = true)
      done <- sent.get
    } yield done

  private def recording(sent: Ref[IO, List[String]]): IrcApi[IO] = (target: String, text: String) =>
    sent.update(_ :+ s"$target:$text")

  test("a text is sent to the channel, one message per line") {
    run(value = Text("one\ntwo")).map(assertEquals(_, List("#sbots:one", "#sbots:two")))
  }

  test("a private message is answered to the nick") {
    run(value = Text("hello"), chat = "alice").map(assertEquals(_, List("alice:hello")))
  }

  test("the time to live is ignored, the message is sent") {
    run(value = Text("bye", Some(1.minute))).map(assertEquals(_, List("#sbots:bye")))
  }

  test("a markdown is sent as plain text") {
    run(value = Markdown("**hi** \\. there")).map(assertEquals(_, List("#sbots:hi . there")))
  }

  test("a value that is not text is not sent") {
    run(value = Mp3File("a.mp3")).map(assertEquals(_, List.empty))
  }

  test("a failure of the server is logged, not raised") {
    val failing = (_: Ref[IO, List[String]]) =>
      new IrcApi[IO] {
        override def sendMessage(target: String, text: String): IO[Unit] = IO.raiseError(new RuntimeException("no"))
      }
    run(value = Text("hello"), api = failing).map(assertEquals(_, List.empty))
  }
}
