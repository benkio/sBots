package com.benkio.chatcore.http

import cats.effect.IO
import com.benkio.chatcore.http.MegaClient.MegaUriComponents
import com.benkio.chatcore.Arbitraries
import munit.CatsEffectSuite
import munit.ScalaCheckEffectSuite
import org.http4s.Uri
import org.scalacheck.effect.PropF

class MegaClientSpec extends CatsEffectSuite with ScalaCheckEffectSuite with Arbitraries {

  test("extractMegaUrlComponents should correctly get the url parts") {
    PropF.forAllF { (megaUriComponents: MegaUriComponents) =>
      {
        assertIO(
          obtained = MegaClient.extractMegaUrlComponents[IO](
            Uri.unsafeFromString(s"https://mega.nz/file/${megaUriComponents.fileId}#${megaUriComponents.decryptKey}")
          ),
          returns = megaUriComponents,
          clue = "[MegaClientSpec] extractMegaUrlComponents didn't returned the expected value"
        )
      }
    }
  }
}
