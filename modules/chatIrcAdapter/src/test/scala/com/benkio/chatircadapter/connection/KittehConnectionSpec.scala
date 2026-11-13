package com.benkio.chatircadapter.connection

import cats.effect.IO
import com.benkio.chatcore.Logger.given
import munit.CatsEffectSuite

import scala.concurrent.duration.*

/** The Kitteh connection against a fake server on a local port. */
class KittehConnectionSpec extends CatsEffectSuite {

  override val munitIOTimeout: FiniteDuration = 30.seconds

  private def config(port: Int): IrcConfig = IrcConfig(
    host = "127.0.0.1",
    port = port,
    tls = false,
    channels = List("#sbots", "#other"),
    readyTimeout = 15.seconds,
    shutdownTimeout = 5.seconds,
    reconnectBaseDelay = 100.millis,
    reconnectMaxDelay = 500.millis,
    rateLimitMessages = 5,
    rateLimitWindow = 10.seconds
  )

  private def eventually(check: => Boolean, tries: Int = 100): IO[Boolean] =
    if check then IO.pure(true)
    else if tries == 0 then IO.pure(false)
    else IO.sleep(100.millis) >> eventually(check, tries - 1)

  test("it connects, registers with the nick and joins the channels") {
    FakeIrcServer.resource.use(server =>
      IrcConnection
        .resource[IO]("TestBot", None, config(server.port))
        .use(_ =>
          eventually(server.joined.toSet == Set("#sbots", "#other")).map { joined =>
            assert(joined, server.joined.toString)
            assert(server.linesReceived.exists(_.startsWith("NICK TestBot")), server.linesReceived.toString)
            assertEquals(server.connections, 1)
          }
        )
    )
  }

  test("it connects again, and joins again, when the connection is lost") {
    FakeIrcServer.resource.use(server =>
      IrcConnection
        .resource[IO]("TestBot", None, config(server.port))
        .use(connection =>
          for {
            _         <- eventually(server.joined.size == 2)
            _         <- server.dropConnection
            again     <- eventually(server.connections == 2 && server.joined.size == 4)
            notClosed <- connection.closed.timeoutTo(200.millis, IO.pure("open")).map(_ == "open")
          } yield {
            assert(again, s"connections ${server.connections}, joined ${server.joined}")
            assert(notClosed)
          }
        )
    )
  }

  test("on release it quits and the connection is closed for good, with no reconnection") {
    FakeIrcServer.resource.use(server =>
      for {
        closedSeen <- IrcConnection
          .resource[IO]("TestBot", None, config(server.port))
          .use(connection => eventually(server.joined.size == 2).as(connection))
        closed <- closedSeen.awaitShutdown(5.seconds)
        _      <- IO.sleep(600.millis)
      } yield {
        assert(closed)
        assert(server.linesReceived.exists(_.startsWith("QUIT")), server.linesReceived.toString)
        assertEquals(server.connections, 1)
      }
    )
  }

  test("a server that cannot be reached fails after the ready timeout and does not leak the connection") {
    val unreachable = config(1).copy(readyTimeout = 1.second)
    IrcConnection.resource[IO]("TestBot", None, unreachable).use(_ => IO.unit).attempt.map { result =>
      assert(result.isLeft, result.toString)
    }
  }
}
