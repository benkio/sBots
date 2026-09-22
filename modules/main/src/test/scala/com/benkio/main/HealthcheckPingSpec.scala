package com.benkio.main

import cats.effect.kernel.Ref
import cats.effect.IO
import com.benkio.chatcore.repository.db.DBLog
import com.benkio.chatcore.repository.db.DBLogData
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
}
