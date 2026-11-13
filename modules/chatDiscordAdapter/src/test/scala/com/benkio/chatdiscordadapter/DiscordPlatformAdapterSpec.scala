package com.benkio.chatdiscordadapter

import cats.effect.Deferred
import cats.effect.IO
import cats.effect.Ref
import com.benkio.chatcore.mocks.BackgroundJobManagerMock
import com.benkio.chatcore.mocks.DBLayerMock
import com.benkio.chatcore.mocks.NotifierMock.given
import com.benkio.chatcore.mocks.RepositoryMock
import com.benkio.chatcore.model.reply.Text
import com.benkio.chatcore.model.ChatId
import com.benkio.chatcore.model.Message
import com.benkio.chatcore.model.MessageId
import com.benkio.chatcore.model.Platform
import com.benkio.chatcore.model.SBotInfo.SBotId
import com.benkio.chatcore.Logger.given
import com.benkio.chatdiscordadapter.outbound.DiscordApi
import munit.CatsEffectSuite

import java.nio.file.Path
import scala.concurrent.duration.*

class DiscordPlatformAdapterSpec extends CatsEffectSuite {

  private val message = Message(
    messageId = Some(MessageId("111")),
    date = 0L,
    chatId = ChatId(Platform.Discord, "999"),
    chatType = "group",
    text = Some("hi")
  )

  private class RecordingApi(sent: Ref[IO, List[String]]) extends DiscordApi[IO] {
    override def sendText(channelId: String, text: String, replyToMessageId: Option[String]): IO[String] =
      sent.update(_ :+ s"$channelId:$text:${replyToMessageId.getOrElse("-")}").as("id")
    override def sendFile(channelId: String, fileName: String, file: Path, replyToMessageId: Option[String]) =
      IO.pure("id")
    override def deleteMessage(channelId: String, messageId: String): IO[Unit] = IO.unit
  }

  private def adapter(api: IO[DiscordApi[IO]], startGateway: IO[Unit] = IO.unit): DiscordPlatformAdapter[IO] =
    new DiscordPlatformAdapter[IO](
      discordApi = api,
      repository = new RepositoryMock(),
      backgroundJobManager = BackgroundJobManagerMock.mock(),
      effectfulCallbacks = Map.empty,
      dbLayer = DBLayerMock.mock(SBotId("bot")),
      ttl = None,
      startGateway = startGateway
    )

  test("the adapter is the Discord platform") {
    assertEquals(adapter(IO.never).platform, Platform.Discord)
  }

  test("run is the connection to the gateway") {
    Ref
      .of[IO, Boolean](false)
      .flatMap(started => adapter(IO.never, started.set(true)).run >> started.get.map(assert(_)))
  }

  test("a reply waits for the connection, then it is sent to the channel as a reply to the message") {
    for {
      sent    <- Ref.of[IO, List[String]](List.empty)
      ready   <- Deferred[IO, DiscordApi[IO]]
      sending <- adapter(ready.get).send(Text("hello"), message, replyToMessage = true).start
      _       <- IO.sleep(50.millis)
      before  <- sent.get
      _       <- ready.complete(new RecordingApi(sent))
      _       <- sending.joinWithNever
      after   <- sent.get
    } yield {
      assertEquals(before, List.empty)
      assertEquals(after, List("999:hello:111"))
    }
  }
}
