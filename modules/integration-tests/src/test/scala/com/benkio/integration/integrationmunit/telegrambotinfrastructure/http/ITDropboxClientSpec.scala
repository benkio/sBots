package com.benkio.integration.integrationmunit.telegrambotinfrastructure.http

import cats.effect.IO
import cats.syntax.all.*
import com.benkio.integration.DBFixture
import munit.CatsEffectSuite
import org.http4s.Uri

import java.io.File
import java.nio.file.Files

class ITDropboxClientSpec extends CatsEffectSuite with DBFixture {

  databaseFixture.test("fetch should return the expected urls content in a file if the urls is valid") { fixture =>
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
      ) -> "rphjb_Schifo.mp3",
      Uri.unsafeFromString(
        "https://www.dropbox.com/s/syd0ivnsyq1r5pk/rphjb_AmmaestrareIlDolore.mp4?dl=1"
      ) -> "rphjb_AmmaestrareIlDolore.mp4",
      Uri.unsafeFromString(
        "https://www.dropbox.com/s/fjhnlf32njs8nec/rphjb_TiDovrestiVergognare.mp3?dl=1"
      ) -> "rphjb_TiDovrestiVergognare.mp3"
    )

    val result = for {
      dropboxClient <- fixture.dropboxClientResource
      files         <- input.parTraverse { case (url, filename) => dropboxClient.fetchFile(filename, url) }
      bytess = files.map((file: File) => Files.readAllBytes(file.toPath).length)
    } yield bytess.forall(bytes => bytes > (1024 * 5))

    result.use(IO.pure).assert
  }
}
