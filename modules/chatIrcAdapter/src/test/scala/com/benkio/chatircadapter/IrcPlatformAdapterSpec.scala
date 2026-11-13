package com.benkio.chatircadapter

import cats.effect.Deferred
import cats.effect.IO
import cats.effect.Ref
import com.benkio.chatcore.mocks.BackgroundJobManagerMock
import com.benkio.chatcore.mocks.DBLayerMock
import com.benkio.chatcore.model.reply.Text
import com.benkio.chatcore.model.ChatId
import com.benkio.chatcore.model.Message
import com.benkio.chatcore.model.Platform
import com.benkio.chatcore.model.SBotInfo.SBotId
import com.benkio.chatcore.Logger.given
import com.benkio.chatircadapter.outbound.IrcApi
import munit.CatsEffectSuite

import scala.concurrent.duration.*

class IrcPlatformAdapterSpec extends CatsEffectSuite {

  private val message = Message(None, 0L, ChatId(Platform.Irc, "#sbots"), "group", text = Some("hi"))

  private def adapter(api: IO[IrcApi[IO]], start: IO[Unit] = IO.unit): IrcPlatformAdapter[IO] =
    new IrcPlatformAdapter[IO](
      ircApi = api,
      backgroundJobManager = BackgroundJobManagerMock.mock(),
      effectfulCallbacks = Map.empty,
      dbLayer = DBLayerMock.mock(SBotId("bot")),
      ttl = None,
      startConnection = start
    )

  test("the adapter is the IRC platform") {
    assertEquals(adapter(IO.never).platform, Platform.Irc)
  }

  test("run is the connection to the server") {
    Ref
      .of[IO, Boolean](false)
      .flatMap(started => adapter(IO.never, started.set(true)).run >> started.get.map(assert(_)))
  }

  test("a reply waits for the connection, then it is sent to the channel") {
    for {
      sent    <- Ref.of[IO, List[String]](List.empty)
      ready   <- Deferred[IO, IrcApi[IO]]
      sending <- adapter(ready.get).send(Text("hello"), message, replyToMessage = true).start
      _       <- IO.sleep(50.millis)
      before  <- sent.get
      _       <- ready.complete((target: String, text: String) => sent.update(_ :+ s"$target:$text"))
      _       <- sending.joinWithNever
      after   <- sent.get
    } yield {
      assertEquals(before, List.empty)
      assertEquals(after, List("#sbots:hello"))
    }
  }
}
