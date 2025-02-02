package com.benkio.telegrambotinfrastructure.web

import org.http4s.Uri
import cats.effect.*
import cats.implicits.*
import com.benkio.telegrambotinfrastructure.web.UrlFetcher.UnexpectedDropboxResponse
import log.effect.fs2.SyncLogWriter.consoleLogUpToLevel
import log.effect.LogLevels
import log.effect.LogWriter
import munit.CatsEffectSuite

import org.http4s.ember.client.*

import java.io.File
import java.nio.file.Files

class UrlFetcherSpec extends CatsEffectSuite {

  given log: LogWriter[IO] = consoleLogUpToLevel(LogLevels.Info)

  def buildUrlFetcher(): Resource[IO, UrlFetcher[IO]] =
    EmberClientBuilder
      .default[IO]
      .withMaxResponseHeaderSize(8192)
      .build
      .flatMap(httpClient => Resource.eval(UrlFetcher[IO](httpClient)))

  test("fetch should return the expected url content in a file if the url is valid") {
    val input = List(
      (
        "rphjb_AmmaestrareIlDolore.mp4",
        Uri.unsafeFromString("https://www.dropbox.com/s/syd0ivnsyq1r5pk/rphjb_AmmaestrareIlDolore.mp4?dl=1"),
        1024 * 10
      ),
      (
        "rphjb_TiDovrestiVergognare.mp3",
        Uri.unsafeFromString("https://www.dropbox.com/s/fjhnlf32njs8nec/rphjb_TiDovrestiVergognare.mp3?dl=1"),
        1024 * 10
      )
    )

    def check(f: String, u: Uri, size: Int) = for {
      urlFetcher <- buildUrlFetcher()
      file       <- urlFetcher.fetchFromDropbox(f, u)
      bytes = Files.readAllBytes(file.toPath).length
    } yield bytes > size

    input
      .traverse { case (f, u, s) =>
        check(f, u, s)
      }
      .use(rs => IO.pure(rs.forall(identity)))
      .assert
  }

  test("fetch should return the expected urls content in a file if the urls is valid") {
    val input = List(
      Uri.unsafeFromString(
        "https://www.dropbox.com/scl/fi/t5t952kwidqdyol4mutwv/rphjb_MaSgus.mp3?rlkey=f1fjff8ls4vjhs013plj1hrvs&dl=1"
      ) -> "rphjb_MaSgus.mp3",
      Uri.unsafeFromString(
        "https://www.dropbox.com/scl/fi/eb3h61camy0bsomtmxyn8/rphjb_MeNeVado.mp3?rlkey=ugnmubcnpig5phlluzonvy1wk&dl=1"
      ) -> "rphjb_MeNeVado.mp3",
      Uri.unsafeFromString(
        "https://www.dropbox.com/scl/fi/crwwhqhlf8d61jav1gaw4/rphjb_Ritornata.mp3?rlkey=n4u6rwqfaj1pnotavsu1t1z0j&dl=1"
      ) -> "rphjb_Ritornata.mp3",
      Uri.unsafeFromString(
        "https://www.dropbox.com/scl/fi/bbapesg0ghop422fqq4tm/rphjb_Schifo.mp3?rlkey=u1ol7p0i1xnvxahz150eiytq2&dl=1"
      ) -> "rphjb_Schifo.mp3"
    )

    val result = for {
      urlFetcher <- buildUrlFetcher()
      files      <- input.parTraverse { case (url, filename) => urlFetcher.fetchFromDropbox(filename, url) }
      bytess = files.map((file: File) => Files.readAllBytes(file.toPath).length)
    } yield bytess.forall(bytes => bytes > (1024 * 5))

    result.use(IO.pure).assert
  }

  test("fetch should fail if the response is empty") {
    val emptyUrl = Uri.unsafeFromString("https://httpbin.org/status/200")
    val filename = "whaeverfilename"
    val result = for {
      urlFetcher <- buildUrlFetcher()
      file       <- urlFetcher.fetchFromDropbox(filename, emptyUrl)
    } yield file

    interceptIO[UnexpectedDropboxResponse[IO]](result.use_)
  }
}
