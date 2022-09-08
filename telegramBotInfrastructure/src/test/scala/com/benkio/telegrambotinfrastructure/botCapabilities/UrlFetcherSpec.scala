package com.benkio.telegrambotinfrastructure.botCapabilities

import cats.effect._
import cats.implicits._
import com.benkio.telegrambotinfrastructure.botcapabilities.UrlFetcher
import log.effect.fs2.SyncLogWriter.consoleLogUpToLevel
import log.effect.LogLevels
import log.effect.LogWriter
import munit.CatsEffectSuite
import org.http4s.ember.client._

import java.io.File
import java.nio.file.Files

class UrlFetcherSpec extends CatsEffectSuite {

  implicit val log: LogWriter[IO] = consoleLogUpToLevel(LogLevels.Info)

  def buildUrlFetcher(): Resource[IO, UrlFetcher[IO]] =
    EmberClientBuilder.default[IO].build.flatMap(httpClient => Resource.eval(UrlFetcher[IO](httpClient)))

  test("fetch should return the expected url content in a file if the url is valid") {
    val validUrl = "https://www.dropbox.com/s/cy0onu1oq8dyyzs/rphjb_MaSgus.mp3?dl=1"
    val filename = "rphjb_MaSgus.mp3"

    val result = for {
      urlFetcher <- buildUrlFetcher()
      file       <- urlFetcher.fetchFromDropbox(filename, validUrl)
      bytes = Files.readAllBytes(file.toPath).length
    } yield bytes > (1024 * 5)

    result.use(IO.pure).assert
  }

  test("fetch should return the expected urls content in a file if the urls is valid") {
    val input = List(
      "https://www.dropbox.com/s/cy0onu1oq8dyyzs/rphjb_MaSgus.mp3?dl=1"   -> "rphjb_MaSgus.mp3",
      "https://www.dropbox.com/s/efpsh6zt3qpn91s/rphjb_MeNeVado.mp3?dl=1" -> "rphjb_MeNeVado.mp3",
      "https://www.dropbox.com/sh/xqaatugvq8zcoyu/AAAsGKVUNNSJ_TzDZuzFmHJya/rphjb_Ritornata.mp3?dl=1" -> "rphjb_Ritornata.mp3",
      "https://www.dropbox.com/sh/xqaatugvq8zcoyu/AAAfdMdV-91EhLTSJHmLN6Lca/rphjb_Schifo.mp3?dl=1" -> "rphjb_Schifo.mp3"
    )

    val result = for {
      urlFetcher <- buildUrlFetcher()
      files      <- input.traverse { case (url, filename) => urlFetcher.fetchFromDropbox(filename, url) }
      bytess = files.map((file: File) => Files.readAllBytes(file.toPath).length)
    } yield bytess.forall(bytes => bytes > (1024 * 5))

    result.use(IO.pure).assert
  }

  test("fetch should fail if the url is malformed") {
    val invalidUrl = "bad url"
    val filename   = "rphjb_06.gif"
    val result = for {
      urlFetcher <- buildUrlFetcher()
      file       <- urlFetcher.fetchFromDropbox(filename, invalidUrl)
    } yield file

    interceptIO[Throwable](result.use_)
  }
}
