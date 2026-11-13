package com.benkio.chatircadapter.outbound

import cats.data.NonEmptyList
import cats.effect.IO
import cats.effect.Ref
import cats.effect.Resource
import com.benkio.chatcore.mocks.NotifierMock.given
import com.benkio.chatcore.mocks.RepositoryMock
import com.benkio.chatcore.model.media.MediaResource.MediaResourceFile
import com.benkio.chatcore.model.reply.*
import com.benkio.chatcore.model.ChatId
import com.benkio.chatcore.model.Message
import com.benkio.chatcore.model.Platform
import com.benkio.chatcore.repository.Repository.RepositoryError
import com.benkio.chatcore.Logger.given
import com.benkio.chatircadapter.model.IrcCapabilities
import munit.CatsEffectSuite

import java.nio.file.Files
import scala.concurrent.duration.*

class IrcRepliesSpec extends CatsEffectSuite {

  private def message(chat: String): Message =
    Message(None, 0L, ChatId(Platform.Irc, chat), "group", text = Some("hi"))

  private def run(
      value: ReplyValue,
      chat: String = "#sbots",
      api: Ref[IO, List[String]] => IrcApi[IO] = recording,
      repository: RepositoryMock = new RepositoryMock()
  ) =
    for {
      sent <- Ref.of[IO, List[String]](List.empty)
      _    <- new IrcReplies[IO](api(sent), repository, IrcCapabilities.value)
        .send(value, message(chat), replyToMessage = true)
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

  private def fileOf(content: String): RepositoryMock = {
    val path = Files.createTempFile("ircMarkdown", ".md")
    Files.writeString(path, content)
    path.toFile.deleteOnExit()
    new RepositoryMock(getResourceFileHandler =
      _ => IO.pure(Right(NonEmptyList.one(MediaResourceFile[IO](Resource.pure(path)))))
    )
  }

  test("a markdown file is sent as its content in plain text, one message per line") {
    run(value = MarkdownFile("bot_doc.md"), repository = fileOf("# Title\n\n**bold** line\nlast"))
      .map(assertEquals(_, List("#sbots:Title", "#sbots:bold line", "#sbots:last")))
  }

  test("a markdown file that cannot be read is not sent") {
    run(
      value = MarkdownFile("bot_doc.md"),
      repository = new RepositoryMock(getResourceFileHandler =
        mediaFile => IO.pure(Left(RepositoryError.NoResourcesFoundFile(mediaFile)))
      )
    ).map(assertEquals(_, List.empty))
  }

  test("an empty markdown file is not sent") {
    run(value = MarkdownFile("bot_doc.md"), repository = fileOf("  \n")).map(assertEquals(_, List.empty))
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
