package com.benkio.telegrambotinfrastructure.http

import cats.effect.IO
import cats.effect.Resource
import cats.syntax.all.*
import com.benkio.telegrambotinfrastructure.http.DropboxClient.UnexpectedDropboxResponse
import com.benkio.telegrambotinfrastructure.mocks.DropboxServerMock
import log.effect.fs2.SyncLogWriter.consoleLogUpToLevel
import log.effect.LogLevels
import log.effect.LogWriter
import munit.CatsEffectSuite
import org.http4s.ember.client.*
import org.http4s.syntax.literals.*
import org.http4s.Uri

import java.nio.file.Files

class DropboxClientSpec extends CatsEffectSuite {

  given log: LogWriter[IO] = consoleLogUpToLevel(LogLevels.Info)

  def buildDropboxClient(): Resource[IO, DropboxClient[IO]] =
    EmberClientBuilder
      .default[IO]
      .withMaxResponseHeaderSize(8192)
      .build
      .flatMap(httpClient => Resource.eval(DropboxClient[IO](httpClient)))

  test("fetch should follow the redirect and returno the file") {
    val emptyUrl = uri"http://0.0.0.0:8080/302TestFile"
    val filename = "302TestFile"
    val expected = DropboxServerMock.servedFile.mkString

    val result = for {
      server        <- DropboxServerMock.build(expected)
      dropboxClient <- buildDropboxClient()
      file          <- dropboxClient.fetchFile(filename, emptyUrl)
    } yield file

    result.use(f =>
      assertEquals(
        Files.readAllBytes(f.toPath.toAbsolutePath).toList,
        expected.getBytes.toList
      ).pure[IO]
    )
  }

  test("fetch should fail if the response is empty") {

    val emptyUrl = uri"http://0.0.0.0:8080/emptyResponseFilename"
    val filename = "emptyResponseFilename"

    val result = for {
      server        <- DropboxServerMock.build("")
      dropboxClient <- buildDropboxClient()
      file          <- dropboxClient.fetchFile(filename, emptyUrl)
    } yield file

    interceptIO[UnexpectedDropboxResponse[IO]](result.use_)
  }
}
