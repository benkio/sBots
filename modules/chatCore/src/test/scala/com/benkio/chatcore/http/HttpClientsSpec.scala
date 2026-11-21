package com.benkio.chatcore.http

import cats.effect.IO
import cats.effect.Resource
import com.benkio.chatcore.mocks.DropboxClientMock
import com.benkio.chatcore.mocks.MegaClientMock
import munit.CatsEffectSuite
import org.http4s.syntax.literals.*
import org.http4s.Uri

import java.nio.file.Path

class HttpClientsSpec extends CatsEffectSuite {

  private val unusedFile: (String, Uri) => Resource[IO, Path] =
    (_, _) => Resource.pure(Path.of("unused"))

  test("expectedClient should return a typed error for unsupported clients") {
    val megaUri    = uri"https://mega.nz/robots.txt"
    val dropboxUri = uri"https://www.dropbox.com/s/syd0ivnsyq1r5pk/file.mp4?dl=1"
    val otherUri   = uri"https://example.com/file.mp4"

    assertEquals(HttpClients.expectedClient(megaUri), Right(HttpClients.ExpectedClient.Mega))
    assertEquals(HttpClients.expectedClient(dropboxUri), Right(HttpClients.ExpectedClient.Dropbox))
    assertEquals(HttpClients.expectedClient(otherUri), Left(HttpClients.UnsupportedClient(otherUri)))
  }

  test("fetchFile should raise the typed error in F for an unsupported uri") {
    val otherUri = uri"https://example.com/file.mp4"
    val clients  = HttpClients[IO](DropboxClientMock.mock(unusedFile), MegaClientMock.mock(unusedFile))

    HttpClients.fetchFile(clients, "file.mp4", otherUri).use(IO.pure).attempt.map {
      case Left(HttpClients.UnsupportedClient(`otherUri`)) => assert(true)
      case other                                           => fail(s"expected UnsupportedClient, got $other")
    }
  }
}
