package com.benkio.chatcore.http

import cats.effect.IO
import cats.effect.Resource
import cats.syntax.all.*
import com.benkio.chatcore.http.DropboxClient.UnexpectedDropboxResponse
import com.benkio.chatcore.mocks.DropboxServerMock
import com.benkio.chatcore.Logger.given
import munit.CatsEffectSuite
import org.http4s.ember.client.*

import java.nio.file.Files

class DropboxClientSpec extends CatsEffectSuite {

  def buildDropboxClient(): Resource[IO, DropboxClient[IO]] =
    EmberClientBuilder
      .default[IO]
      .withMaxResponseHeaderSize(8192)
      .build
      .flatMap(httpClient => Resource.eval(DropboxClient[IO](httpClient)))

  test("fetch should follow the redirect and returno the file") {
    val filename = "302TestFile"
    val expected = DropboxServerMock.servedFile.mkString

    val result = for {
      server        <- DropboxServerMock.build(expected)
      dropboxClient <- buildDropboxClient()
      file          <- dropboxClient.fetchFile(filename, server.baseUri / "302TestFile")
    } yield file

    result.use(f =>
      assertEquals(
        Files.readAllBytes(f.toAbsolutePath).toList,
        expected.getBytes.toList
      ).pure[IO]
    )
  }

  test("fetch should fail if the response is empty") {

    val filename = "emptyResponseFilename"

    val result = for {
      server        <- DropboxServerMock.build("")
      dropboxClient <- buildDropboxClient()
      file          <- dropboxClient.fetchFile(filename, server.baseUri / "emptyResponseFilename")
    } yield file

    interceptIO[UnexpectedDropboxResponse[IO]](result.use_)
  }
}
