package com.benkio.telegrambotinfrastructure.initialization

import com.benkio.telegrambotinfrastructure.mocks.ResourceAccessMock
import org.http4s.*
import org.http4s.implicits.*
import com.benkio.telegrambotinfrastructure.mocks.TelegramHttpRoutes
import munit.*
import cats.effect.*
import cats.syntax.all.*
import org.http4s.client.Client

class BotSetupSpec extends CatsEffectSuite {
  test("deleteWebhook should send the expected request to TelegramApi") {
    val token              = "expectedToken"
    val httpApp            = TelegramHttpRoutes[IO](token).orNotFound
    val client: Client[IO] = Client.fromHttpApp(httpApp)

    assertIO(
      BotSetup
        .deleteWebhooks[IO](
          httpClient = client,
          token = token
        )
        .use(_.status.pure[IO]),
      Status.Ok
    )
  }

  test("token should call resource access with the input token filename") {
    val tokenFilename = "filename.token"
    val resourceAccess = ResourceAccessMock(
      getResourceByteArrayHandler = resourceName =>
        IO.raiseUnless(resourceName == tokenFilename)(
          Throwable(s"[ResourceAccessMock] getResourceByteArrayHandler input mismatch: $resourceName ≠ $tokenFilename")
        ).as(Array.fill(20)((scala.util.Random.nextInt(256) - 128).toByte))
    )

    BotSetup
      .token[IO](
        tokenFilename = tokenFilename,
        resourceAccess = resourceAccess
      )
      .use(s => assert(s.length == 20, s"Expected a string of 20 chars, got $s").pure[IO])
  }
}
