package com.benkio.richardphjbensonbot

import cats.effect._
import com.benkio.richardphjbensonbot.UrlFetcher
import munit.CatsEffectSuite
import org.http4s.blaze.client._

import java.io.File
import java.nio.file.Files

class UrlFetcherSpec extends CatsEffectSuite {

  def buildUrlFetcher(): Resource[IO, UrlFetcher[IO]] =
    BlazeClientBuilder[IO].resource.map(httpClient => UrlFetcher[IO](httpClient))

  test("fetch should return the expected url content in a file if the url is valid") {
    val validUrl = "https://www.dropbox.com/s/mco2gb75ldfurvy/rphjb_06.gif?dl=1"
    val filename = "rphjb_06.gif"

    val actual: IO[File] = buildUrlFetcher().use(_.fetchFromDropbox(filename, validUrl))

    assertIO(
      actual.map(f => {
        val bytes = Files.readAllBytes(f.toPath).length
        bytes > 1000000
      }),
      true
    )

  }

  test("fetch should fail if the url is malformed") {
    val invalidUrl       = "bad url"
    val filename         = "rphjb_06.gif"
    val actual: IO[File] = buildUrlFetcher().use(_.fetchFromDropbox(filename, invalidUrl))

    interceptIO[Throwable](actual)
  }
}
