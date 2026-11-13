package com.benkio.chatdiscordadapter.gateway

import cats.effect.IO
import cats.effect.Ref
import com.benkio.chatcore.model.SBotInfo
import com.benkio.chatcore.model.SBotInfo.SBotId
import com.benkio.chatcore.model.SBotInfo.SBotName
import com.benkio.chatcore.Logger.given
import munit.CatsEffectSuite
import net.dv8tion.jda.api.requests.GatewayIntent
import pureconfig.ConfigSource

import scala.concurrent.duration.*

class DiscordGatewaySpec extends CatsEffectSuite {

  /** A connection that records what it is asked, and that closes in time or not. */
  private class FakeClient(
      calls: Ref[IO, List[String]],
      ready: IO[Unit] = IO.unit,
      closesInTime: Boolean = true
  ) extends GatewayClient[IO] {
    override def awaitReady: IO[Unit]                                = calls.update("awaitReady" :: _) >> ready
    override def closed: IO[Unit]                                    = IO.unit
    override def shutdown: IO[Unit]                                  = calls.update("shutdown" :: _)
    override def awaitShutdown(timeout: FiniteDuration): IO[Boolean] =
      calls.update("awaitShutdown" :: _).as(closesInTime)
    override def shutdownNow: IO[Unit] = calls.update("shutdownNow" :: _)
  }

  private def run(client: Ref[IO, List[String]] => FakeClient): IO[(Either[Throwable, Unit], List[String])] =
    for {
      calls  <- Ref.of[IO, List[String]](List.empty)
      result <- DiscordGateway.managed[IO](IO.pure(client(calls)), 1.second).use(_ => IO.unit).attempt
      done   <- calls.get.map(_.reverse)
    } yield (result, done)

  test("the connection is awaited, then asked to shut down on release, without forcing when it closes in time") {
    run(new FakeClient(_)).map { case (result, calls) =>
      assertEquals(result, Right(()))
      assertEquals(calls, List("awaitReady", "shutdown", "awaitShutdown"))
    }
  }

  test("a connection that does not close in time is closed without waiting") {
    run(new FakeClient(_, closesInTime = false)).map { case (_, calls) =>
      assertEquals(calls, List("awaitReady", "shutdown", "awaitShutdown", "shutdownNow"))
    }
  }

  test("a connection that never gets ready fails and is closed anyway") {
    run(new FakeClient(_, ready = IO.raiseError(new RuntimeException("boom")))).map { case (result, calls) =>
      assertEquals(result.left.map(_.getMessage), Left("boom"))
      assertEquals(calls, List("awaitReady", "shutdown", "awaitShutdown"))
    }
  }

  test("a failure while closing is not raised") {
    val failing = (calls: Ref[IO, List[String]]) =>
      new FakeClient(calls) {
        override def shutdown: IO[Unit] = IO.raiseError(new RuntimeException("no"))
      }
    run(failing).map { case (result, _) => assertEquals(result, Right(())) }
  }

  test("the token file of a bot is named after it") {
    assertEquals(
      DiscordToken.filename(SBotInfo(SBotId("abc"), SBotName("TestBot"))),
      "abc_TestBot_discord.token"
    )
  }

  test("the message content intent is asked only when the config says so") {
    val base = DiscordGatewayConfig(shutdownTimeout = 10.seconds, messageContent = false)
    assertEquals(base.intents, Set(GatewayIntent.GUILD_MESSAGES, GatewayIntent.DIRECT_MESSAGES))
    assert(base.copy(messageContent = true).intents.contains(GatewayIntent.MESSAGE_CONTENT))
  }

  test("the config is read from the discord block") {
    val source = ConfigSource.string("shutdown-timeout = 5 seconds\nmessage-content = true")
    assertEquals(source.load[DiscordGatewayConfig], Right(DiscordGatewayConfig(5.seconds, true)))
  }
}
