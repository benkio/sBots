package com.benkio.integration.integrationmunit.chatcore.http

import cats.effect.IO
import cats.effect.Ref
import cats.effect.Resource
import cats.syntax.all.*
import com.benkio.chatcore.http.MegaClient
import com.benkio.chatcore.mocks.MegaServerMock
import com.benkio.integration.DBFixture
import com.benkio.integrationtest.Logger.given
import munit.CatsEffectSuite
import org.http4s.client.Client
import org.http4s.ember.client.EmberClientBuilder
import org.http4s.syntax.literals.*
import org.http4s.ParseFailure
import org.http4s.Request
import org.http4s.Response
import org.http4s.Uri

import java.nio.file.Files
import java.nio.file.Path

class ITMegaClientSpec extends CatsEffectSuite with DBFixture {

  databaseFixture.test("fetch should return the expected urls content in a file if the urls is valid") { fixture =>
    val input = List(
      uri"https://mega.nz/file/LDxATYLQ#cAdB5jxfJov_BFr-bu548G7cXtLgEsdih4e69PmJ6OI" -> "mega_test_file"
    )

    val result = for {
      megaClient <- fixture.megaClientResource
      files      <- input.parTraverse { case (url, filename) => megaClient.fetchFile(filename, url) }
      fileBytes       = files.map((path: Path) => Files.readAllBytes(path))
      isNotHtml       = fileBytes.forall(bytes => !String(bytes.take(64)).toLowerCase.contains("<!doctype html"))
      hasData         = fileBytes.forall(bytes => bytes.length > 1000)
      hasMp3Id3Header = fileBytes.forall(bytes =>
        bytes.length >= 3 && bytes(0) == 'I'.toByte && bytes(1) == 'D'.toByte && bytes(2) == '3'.toByte
      )
    } yield isNotHtml && hasData && hasMp3Id3Header

    result.use(IO.pure).assert
  }

  test("fetch should recover when first download attempt fails with InvalidHeaderWhitespace") {
    val filename = "megaRetryInvalidHeaderWhitespace"
    val expected = MegaServerMock.servedFile.mkString

    def flakyClient(baseClient: Client[IO]): Resource[IO, Client[IO]] =
      Resource.eval(Ref.of[IO, Int](0)).map { firstDownloadAttempt =>
        Client[IO] { (request: Request[IO]) =>
          val isDownloadRequest = request.uri.path.renderString.contains("/download/")
          if isDownloadRequest then Resource.eval(firstDownloadAttempt.getAndUpdate(_ + 1)).flatMap { attemptIndex =>
            if attemptIndex == 0 then Resource.raiseError[IO, Response[IO], Throwable](
              ParseFailure(
                sanitized = "Encountered Error Attempting to Parse Headers",
                details = "InvalidHeaderWhitespace"
              )
            )
            else baseClient.run(request)
          }
          else baseClient.run(request)
        }
      }

    val result = for {
      server     <- MegaServerMock.build(expected)
      client     <- EmberClientBuilder.default[IO].withMaxResponseHeaderSize(8192).build
      flaky      <- flakyClient(client)
      megaClient <- Resource.eval(
        MegaClient[IO](
          httpClient = flaky,
          megaApiUri = server.baseUri / "cs"
        )
      )
      file <- megaClient.fetchFile(filename, Uri.unsafeFromString(MegaServerMock.testMegaLink))
    } yield file

    result.use(f =>
      assertEquals(
        Files.readAllBytes(f.toAbsolutePath).toList,
        expected.getBytes.toList
      ).pure[IO]
    )
  }
}
