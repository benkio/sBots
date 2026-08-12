package com.benkio.chatcore.http

import cats.effect.IO
import cats.effect.Resource
import cats.syntax.all.*
import com.benkio.chatcore.http.MegaClient.InvalidMegaApiResponse
import com.benkio.chatcore.mocks.MegaServerMock
import com.benkio.chatcore.Logger.given
import munit.CatsEffectSuite
import org.http4s.client.Client
import org.http4s.ember.client.*
import org.http4s.ParseFailure
import org.http4s.Request
import org.http4s.Response
import org.http4s.Uri

import java.nio.file.Files
import scala.concurrent.duration.Duration

class MegaClientSpec extends CatsEffectSuite {
  override val munitIOTimeout = Duration(2, "m")

  def buildMegaClient(megaApiUri: Uri): Resource[IO, MegaClient[IO]] =
    EmberClientBuilder
      .default[IO]
      .withMaxResponseHeaderSize(8192)
      .build
      .flatMap(httpClient => Resource.eval(MegaClient[IO](httpClient, megaApiUri)))

  test("fetch should resolve mega api url and return the downloaded file") {
    val filename = "megaTestFile"
    val expected = MegaServerMock.servedFile.mkString

    val result = for {
      server     <- MegaServerMock.build(expected)
      megaClient <- buildMegaClient(server.baseUri / "cs")
      file       <- megaClient.fetchFile(filename, Uri.unsafeFromString(MegaServerMock.testMegaLink))
    } yield file

    result.use(f =>
      assertEquals(
        Files.readAllBytes(f.toAbsolutePath).toList,
        expected.getBytes.toList
      ).pure[IO]
    )
  }

  test("fetch should fail if mega api response does not contain download url") {
    val filename = "invalidMegaApiResponse"

    val result = for {
      server     <- MegaServerMock.buildInvalidApiResponse("{}")
      megaClient <- buildMegaClient(server.baseUri / "cs")
      file       <- megaClient.fetchFile(filename, Uri.unsafeFromString(MegaServerMock.testMegaLink))
    } yield file

    interceptIO[InvalidMegaApiResponse](result.use_)
  }

  test("fetch should fallback to JDK client on InvalidHeaderWhitespace") {
    val filename = "megaFallbackJdk"
    val expected = "fallback-mega-content"

    def alwaysFailDownloadClient(baseClient: Client[IO]): Client[IO] =
      Client[IO] { (request: Request[IO]) =>
        val isDownloadRequest = request.uri.path.renderString.contains("/download/")
        if isDownloadRequest then Resource.raiseError[IO, Response[IO], Throwable](
          ParseFailure(
            sanitized = "Encountered Error Attempting to Parse Headers",
            details = "InvalidHeaderWhitespace"
          )
        )
        else baseClient.run(request)
      }

    val result = for {
      server     <- MegaServerMock.build(expected)
      baseClient <- EmberClientBuilder
        .default[IO]
        .withMaxResponseHeaderSize(8192)
        .build
      megaClient <- Resource.eval(
        MegaClient[IO](
          httpClient = alwaysFailDownloadClient(baseClient),
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

  test("fetch should retry when mega api responds with -9") {
    val filename = "megaApiRetry"
    val expected = "mega-api-retry-content"

    val result = for {
      server     <- MegaServerMock.buildRateLimitedThenSuccess(expected, failures = 1)
      megaClient <- buildMegaClient(server.baseUri / "cs")
      file       <- megaClient.fetchFile(filename, Uri.unsafeFromString(MegaServerMock.testMegaLink))
    } yield file

    result.use(f =>
      assertEquals(
        Files.readAllBytes(f.toAbsolutePath).toList,
        expected.getBytes.toList
      ).pure[IO]
    )
  }
}
