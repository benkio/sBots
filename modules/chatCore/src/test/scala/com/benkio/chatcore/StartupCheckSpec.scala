package com.benkio.chatcore

import cats.effect.IO
import com.benkio.chatcore.config.StartupCheckConfig
import com.benkio.chatcore.mocks.DBLayerMock
import com.benkio.chatcore.model.SBotInfo
import munit.CatsEffectSuite
import org.http4s.client.Client
import org.http4s.implicits.*
import org.http4s.HttpApp
import org.http4s.Method
import org.http4s.Response
import org.http4s.Status

import java.nio.file.Files
import scala.concurrent.duration.*

class StartupCheckSpec extends CatsEffectSuite {

  private val config = StartupCheckConfig(probeTimeout = 1.seconds, slowThreshold = 500.millis)

  private def outcome(probe: Probe[IO]): IO[ProbeOutcome] = StartupCheck.runProbe(probe, config).map(_.outcome)

  test("a probe that answers fast is ok") {
    outcome(Probe("fast", IO.unit)).map(o => assert(o.isInstanceOf[ProbeOutcome.Ok], o.toString))
  }

  test("a probe that takes more than the slow threshold is slow, not failed") {
    outcome(Probe("slow", IO.sleep(config.slowThreshold + 50.millis))).map(o =>
      assert(o.isInstanceOf[ProbeOutcome.Slow], o.toString)
    )
  }

  test("a probe that raises is failed with the message of the error") {
    outcome(Probe("broken", IO.raiseError(new RuntimeException("boom")))).map {
      case ProbeOutcome.Failed(_, error) => assertEquals(error, "boom")
      case other                         => fail(s"expected Failed, got $other")
    }
  }

  test("an error without a message is described with its class") {
    outcome(Probe("broken", IO.raiseError(new IllegalStateException()))).map {
      case ProbeOutcome.Failed(_, error) => assertEquals(error, "IllegalStateException")
      case other                         => fail(s"expected Failed, got $other")
    }
  }

  test("a probe that never answers times out after the timeout of the configuration") {
    outcome(Probe("hang", IO.never)).map(o => assertEquals(o, ProbeOutcome.TimedOut(config.probeTimeout)))
  }

  test("a probe can have a timeout of its own") {
    outcome(Probe("hang", IO.never, timeout = Some(60.millis))).map(o =>
      assertEquals(o, ProbeOutcome.TimedOut(60.millis))
    )
  }

  test("the probes run one after the other and a broken one does not stop the next") {
    StartupCheck
      .run(List(Probe("a", IO.unit), Probe("b", IO.raiseError(new RuntimeException("x"))), Probe("c", IO.unit)), config)
      .map(results => assertEquals(results.map(r => (r.name, r.isOk)), List(("a", true), ("b", false), ("c", true))))
  }

  test("the durations are written in ms below a second and in seconds above") {
    assertEquals(StartupCheck.formatDuration(999.millis), "999 ms")
    assertEquals(StartupCheck.formatDuration(1200.millis), "1.2 s")
  }

  test("the report says all is fine when every probe is ok") {
    val report = StartupCheck.report(List(ProbeResult("DB read", ProbeOutcome.Ok(12.millis))))
    assertEquals(report, "Startup checks OK\n✅ DB read: 12 ms")
  }

  test("the report marks what needs attention: slow, failed and timed out") {
    val report = StartupCheck.report(
      List(
        ProbeResult("DB read", ProbeOutcome.Ok(12.millis)),
        ProbeResult("Dropbox", ProbeOutcome.Slow(3400.millis)),
        ProbeResult("Mega API", ProbeOutcome.Failed(200.millis, "connection refused")),
        ProbeResult("Telegram getMe", ProbeOutcome.TimedOut(15.seconds))
      )
    )
    assert(report.startsWith("⚠️ Startup checks: 3 of 4 need attention"), report)
    assert(report.contains("🐢 Dropbox: 3.4 s (slow)"), report)
    assert(report.contains("❌ Mega API: failed after 200 ms: connection refused"), report)
    assert(report.contains("⏱️ Telegram getMe: no answer after 15.0 s"), report)
  }

  test("the report of no probe says it") {
    assertEquals(StartupCheck.report(List.empty), "No startup check ran")
  }

  test("the DB probe is ok on a DB that answers") {
    outcome(StartupCheck.dbRead(DBLayerMock.mock(SBotInfo.SBotId("bot")))).map(o =>
      assert(o.isInstanceOf[ProbeOutcome.Ok], o.toString)
    )
  }

  test("the host probe is ok on any answer, even an error status: what counts is to be reached") {
    val client = Client.fromHttpApp(HttpApp[IO](_ => IO.pure(Response[IO](Status.NotFound))))
    outcome(StartupCheck.hostReachable("Host", client, uri"https://example.com")).map(o =>
      assert(o.isInstanceOf[ProbeOutcome.Ok], o.toString)
    )
  }

  test("the host probe asks the host with the method it is given: some endpoints never answer a HEAD") {
    // like the Mega API: a HEAD hangs, a GET answers
    val client = Client.fromHttpApp(HttpApp[IO] { request =>
      if request.method == Method.HEAD then IO.never else IO.pure(Response[IO](Status.Ok))
    })
    for {
      head <- outcome(StartupCheck.hostReachable("Host", client, uri"https://example.com", Method.HEAD))
      get  <- outcome(StartupCheck.hostReachable("Host", client, uri"https://example.com", Method.GET))
    } yield {
      assertEquals(head, ProbeOutcome.TimedOut(config.probeTimeout))
      assert(get.isInstanceOf[ProbeOutcome.Ok], get.toString)
    }
  }

  test("the host probe is failed when the host can't be reached") {
    val client = Client.fromHttpApp(HttpApp[IO](_ => IO.raiseError(new java.net.ConnectException("unreachable"))))
    outcome(StartupCheck.hostReachable("Host", client, uri"https://example.com")).map {
      case ProbeOutcome.Failed(_, error) => assertEquals(error, "unreachable")
      case other                         => fail(s"expected Failed, got $other")
    }
  }

  test("the post deploy marker is consumed once: the second start does not see it") {
    for {
      marker <- IO.blocking(Files.createTempFile("post-deploy", ".marker"))
      first  <- PostDeployMarker.consume[IO](marker)
      second <- PostDeployMarker.consume[IO](marker)
    } yield {
      assert(first, "the first start finds the marker")
      assert(!second, "the marker is gone for the next one")
      assert(!Files.exists(marker))
    }
  }

  test("without a marker there is nothing to consume") {
    IO.blocking(Files.createTempDirectory("no-marker").resolve("missing.marker"))
      .flatMap(PostDeployMarker.consume[IO])
      .map(consumed => assert(!consumed))
  }
}
