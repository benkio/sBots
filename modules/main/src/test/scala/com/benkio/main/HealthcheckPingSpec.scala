package com.benkio.main

import cats.effect.kernel.Ref
import cats.effect.IO
import com.benkio.chatcore.repository.db.DBLog
import com.benkio.chatcore.repository.db.DBLogData
import com.benkio.chatcore.Logger.given
import fs2.Stream
import munit.CatsEffectSuite
import org.http4s.client.Client
import org.http4s.syntax.literals.*
import org.http4s.EntityDecoder.text
import org.http4s.HttpApp
import org.http4s.Method
import org.http4s.Request
import org.http4s.Response
import org.http4s.Status
import org.http4s.Uri

import scala.concurrent.duration.*

class HealthcheckPingSpec extends CatsEffectSuite {

  test("sendHealthcheckEndpoint should POST latest log and return true on success") {
    val dbLog = new DBLog[IO] {
      override def writeLog(logMessage: String): IO[Unit] = IO.unit
      override def getLastLog(): IO[Option[DBLogData]]    = IO.pure(Some(DBLogData(123L, "all good")))
    }
    val endpoint = uri"https://healthcheck.local/ping"

    val result = for {
      captured <- Ref.of[IO, Option[(Method, Uri, String)]](None)
      app: HttpApp[IO] = HttpApp[IO] { (request: Request[IO]) =>
        for {
          body <- request.as[String]
          _    <- captured.set(Some((request.method, request.uri, body)))
        } yield Response[IO](Status.Ok)
      }
      client = Client.fromHttpApp[IO](app)
      isSuccessful <- HealthcheckPing.sendHealthcheckEndpoint[IO](dbLog, client, endpoint)
      requestData  <- captured.get
    } yield (isSuccessful, requestData)

    result.map { case (isSuccessful, requestData) =>
      assert(isSuccessful)
      assertEquals(requestData.map(_._1), Some(Method.POST))
      assertEquals(requestData.map(_._2), Some(endpoint))
      assertEquals(requestData.map(_._3), Some("Latest log: Some(DBLogData(123,all good))"))
    }
  }

  test("sendHealthcheckEndpoint should return false on non successful response") {
    val dbLog = new DBLog[IO] {
      override def writeLog(logMessage: String): IO[Unit] = IO.unit
      override def getLastLog(): IO[Option[DBLogData]]    = IO.pure(None)
    }
    val endpoint = uri"https://healthcheck.local/ping"
    val client   = Client.fromHttpApp[IO](HttpApp[IO] { (_: Request[IO]) =>
      IO.pure(Response[IO](Status.InternalServerError))
    })

    val result = HealthcheckPing.sendHealthcheckEndpoint[IO](dbLog, client, endpoint)
    assertIO(result, false)
  }

  test("resilient should keep pinging after an error: the heartbeat starts again") {
    val calls = Ref.unsafe[IO, Int](0)
    val ping  = calls.getAndUpdate(_ + 1).flatMap(n => IO.raiseWhen(n == 1)(new RuntimeException("transient")))
    val ticks = Stream.repeatEval(IO.unit)
    // the second ping fails: the three that follow the restart are all sent
    val result = HealthcheckPing.resilient[IO](ticks, ping, restartDelay = 1.milli).take(4).compile.toList
    assertIO(result.flatMap(_ => calls.get), 5)
  }

  test("resilient should start again when the ticks themselves fail") {
    val started = Ref.unsafe[IO, Int](0)
    val ticks   = Stream.eval(started.updateAndGet(_ + 1)).flatMap { run =>
      if run == 1 then Stream.raiseError[IO](new RuntimeException("scheduler down")) else Stream.emits(List((), ()))
    }
    val result = HealthcheckPing.resilient[IO](ticks, IO.unit, restartDelay = 1.milli).take(2).compile.toList
    assertIO(result.map(_.size), 2)
  }

  test("start and fail ping the /start and /fail urls of the check, with the reason as the body") {
    val endpoint = uri"https://hc-ping.com/uuid"
    for {
      captured <- Ref.of[IO, List[(Method, String, String)]](List.empty)
      app: HttpApp[IO] = HttpApp[IO] { (request: Request[IO]) =>
        request
          .as[String]
          .flatMap(body =>
            captured.update(_ :+ ((request.method, request.uri.renderString, body))).as(Response[IO](Status.Ok))
          )
      }
      client = Client.fromHttpApp[IO](app)
      _    <- HealthcheckPing.start[IO](client, endpoint)
      _    <- HealthcheckPing.fail[IO](client, endpoint, "boom")
      sent <- captured.get
    } yield assertEquals(
      sent,
      List(
        (Method.POST, "https://hc-ping.com/uuid/start", "Starting"),
        (Method.POST, "https://hc-ping.com/uuid/fail", "boom")
      )
    )
  }

  test("a ping that cannot be sent is not an error") {
    val client   = Client.fromHttpApp[IO](HttpApp[IO](_ => IO.raiseError(new RuntimeException("no network"))))
    val endpoint = uri"https://hc-ping.com/uuid"
    HealthcheckPing.fail[IO](client, endpoint, "boom") >> HealthcheckPing.start[IO](client, endpoint)
  }
}
