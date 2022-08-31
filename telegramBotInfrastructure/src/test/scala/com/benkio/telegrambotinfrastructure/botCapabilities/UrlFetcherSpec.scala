package com.benkio.telegrambotinfrastructure.botCapabilities

import cats.effect._
import com.benkio.telegrambotinfrastructure.botcapabilities.UrlFetcher
import munit.CatsEffectSuite

import java.io.File
import java.net.MalformedURLException

class UrlFetcherSpec extends CatsEffectSuite {

  val urlFetcher = UrlFetcher[IO]()

  test("fetch should return the expected url content in a file if the url is valid") {
    val validUrl                                 = "https://www.dropbox.com/s/mco2gb75ldfurvy/rphjb_MaSgus.mp3?dl=1"
    val filename                                 = "rphjb_MaSgus.mp3"
    val actual: IO[Outcome[IO, Throwable, File]] = urlFetcher.fetch(filename, validUrl).flatMap(_.join)

    assertIO(actual.map(_.isSuccess), true)

  }

  test("fetch should fail if the url is malformed") {
    val invalidUrl                               = "bad url"
    val filename                                 = "rphjb_MaSgus.mp3"
    val actual: IO[Outcome[IO, Throwable, File]] = urlFetcher.fetch(filename, invalidUrl).flatMap(_.join)

    assertIO(actual.map(_.isError), true)
    assertIO(
      actual.map(_.toString),
      Outcome.errored[IO, Throwable, File](new MalformedURLException("no protocol: bad url")).toString
    )
  }
}
