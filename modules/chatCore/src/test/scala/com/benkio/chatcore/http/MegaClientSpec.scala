package com.benkio.chatcore.http

import cats.effect.IO
import cats.effect.Resource
import cats.syntax.all.*
import com.benkio.chatcore.http.MegaClient.InvalidMegaApiResponse
import com.benkio.chatcore.mocks.MegaServerMock
import com.benkio.chatcore.Logger.given
import munit.CatsEffectSuite
import org.http4s.ember.client.*
import org.http4s.Uri

import java.nio.file.Files

class MegaClientSpec extends CatsEffectSuite {

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

  test("fetch should fallback to non-v2 request when v2 returns cloudraid urls") {
    val filename = "cloudraidFallback"
    val expected = "cloudraid fallback content"

    val result = for {
      server     <- MegaServerMock.buildCloudraidThenFallback(expected)
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
