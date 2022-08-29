package com.benkio.richardphjbensonbot


import munit.CatsEffectSuite
import com.benkio.richardphjbensonbot.UrlFetcher
import java.io.File
import cats.effect._
import java.net.MalformedURLException

class UrlFetcherSpec extends CatsEffectSuite {

  val urlFetcher = UrlFetcher[IO]()

  test("fetch should return the expected url content in a file if the url is valid") {
    val validUrl = "https://www.dropbox.com/s/mco2gb75ldfurvy/rphjb_06.gif?dl=1"
    val filename = "rphjb_06.gif"
    val actual: IO[Outcome[IO, Throwable, File]] = urlFetcher.fetch(validUrl, filename).flatMap(_.join)

    assertIO(actual.map(_.isSuccess), true)

  }

  test("fetch should fail if the url is malformed") {
    val invalidUrl = "bad url"
    val filename = "rphjb_06.gif"
    val actual: IO[Outcome[IO, Throwable, File]] = urlFetcher.fetch(invalidUrl, filename).flatMap(_.join)

    assertIO(actual.map(_.isError), true)
    assertIO(actual.map(_.toString), Outcome.errored[IO,Throwable, File](new MalformedURLException("no protocol: bad url")).toString)
  }
}
