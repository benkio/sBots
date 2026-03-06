package com.benkio.chattelegramadapter.initialization

import cats.data.NonEmptyList
import cats.effect.*
import cats.syntax.all.*
import com.benkio.chatcore.mocks.RepositoryMock
import com.benkio.chatcore.mocks.TelegramHttpRoutes
import com.benkio.chatcore.model.media.MediaResource.MediaResourceFile
import com.benkio.chatcore.repository.Repository
import com.benkio.chatcore.Logger.given
import munit.*
import org.http4s.*
import org.http4s.client.Client
import org.http4s.implicits.*

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
    val tokenFilename       = "filename.token"
    val expectedFileContent = "testtokencontent".getBytes()
    val expectedFile        = Repository.toTempFile[IO](tokenFilename, expectedFileContent)

    val repository = RepositoryMock(
      getResourceFileHandler = resourceName =>
        IO.raiseUnless(resourceName.filepath == tokenFilename)(
          Throwable(s"[RepositoryMock] getResourceByteArrayHandler input mismatch: $resourceName ≠ $tokenFilename")
        ).as(Right(NonEmptyList.one(MediaResourceFile(expectedFile))))
    )

    BotSetup
      .token[IO](
        tokenFilename = tokenFilename,
        repository = repository
      )
      .use { tokenContent =>
        assert(
          tokenContent == expectedFileContent.map(_.toChar).mkString,
          s"Expected ${expectedFileContent.map(_.toChar).mkString}, got $tokenContent"
        ).pure[IO]
      }
  }
}
