package com.benkio.chatircadapter.inbound

import cats.effect.std.Dispatcher
import cats.effect.IO
import cats.effect.Ref
import com.benkio.chatcore.model.ChatId
import com.benkio.chatcore.model.Message
import com.benkio.chatcore.model.Platform
import com.benkio.chatcore.Logger.given
import com.benkio.chatircadapter.connection.FakeIrcServer
import com.benkio.chatircadapter.connection.IrcConfig
import com.benkio.chatircadapter.connection.IrcConnection
import munit.CatsEffectSuite

import scala.concurrent.duration.*

/** The messages of a fake server reach the callback through the real library. */
class IrcInboundListenerSpec extends CatsEffectSuite {

  override val munitIOTimeout: FiniteDuration = 30.seconds

  private def config(port: Int): IrcConfig = IrcConfig(
    host = "127.0.0.1",
    port = port,
    tls = false,
    channels = List("#sbots"),
    readyTimeout = 15.seconds,
    shutdownTimeout = 5.seconds,
    reconnectBaseDelay = 100.millis,
    reconnectMaxDelay = 500.millis,
    messageDelay = 50.millis,
    rateLimitMessages = 2,
    rateLimitWindow = 30.seconds
  )

  private def eventually(check: IO[Boolean], tries: Int = 100): IO[Boolean] =
    check.flatMap(ok =>
      if ok then IO.pure(true)
      else if tries == 0 then IO.pure(false)
      else IO.sleep(100.millis) >> eventually(check, tries - 1)
    )

  private def received(messages: Ref[IO, List[Message]]): IO[List[(ChatId, String)]] =
    messages.get.map(_.map(m => m.chatId -> m.text.getOrElse("")))

  test("channel and private messages reach the callback, the ones of the bot itself do not, the flood is limited") {
    for {
      messages <- Ref.of[IO, List[Message]](List.empty)
      _        <- FakeIrcServer.resource.use(server =>
        Dispatcher
          .parallel[IO]
          .use(dispatcher =>
            for {
              limiter <- RateLimiter[IO](2, 30.seconds)
              listener = new IrcInboundListener[IO](dispatcher, limiter, m => messages.update(_ :+ m))
              _ <- IrcConnection
                .resource[IO]("TestBot", None, config(server.port), List(listener))
                .use(_ =>
                  for {
                    _ <- eventually(IO(server.joined.nonEmpty))
                    _ <- server.push(":alice!a@host PRIVMSG #SBots :hello bot")
                    _ <- server.push(":bob!b@host PRIVMSG TestBot :!hi")
                    _ <- server.push(":TestBot!t@host PRIVMSG #sbots :my own message")
                    _ <- server.push(":carol!c@host PRIVMSG #sbots :one")
                    _ <- server.push(":carol!c@host PRIVMSG #sbots :two")
                    _ <- server.push(":carol!c@host PRIVMSG #sbots :three")
                    _ <- eventually(messages.get.map(_.size >= 5))
                    _ <- IO.sleep(500.millis)
                  } yield ()
                )
            } yield ()
          )
      )
      result <- received(messages)
    } yield {
      assert(result.contains(ChatId(Platform.Irc, "#sbots") -> "hello bot"), result.toString)
      assert(result.contains(ChatId(Platform.Irc, "bob") -> "!hi"), result.toString)
      assert(!result.exists(_._2 == "my own message"), result.toString)
      // two of the three messages of the same sender: the parallel dispatcher does not keep their order, so not which
      assertEquals(result.count(m => Set("one", "two", "three").contains(m._2)), 2, result.toString)
    }
  }
}
