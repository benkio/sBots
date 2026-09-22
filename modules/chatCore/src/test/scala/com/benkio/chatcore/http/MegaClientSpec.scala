package com.benkio.chatcore.http

import cats.effect.IO
import com.benkio.chatcore.http.MegaClient.MegaUriComponents
import com.benkio.chatcore.Arbitraries
import munit.CatsEffectSuite
import munit.ScalaCheckEffectSuite
import org.http4s.syntax.literals.*
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

  test("extractMegaUrlComponents should fail when file id segment is missing") {
    val invalidUri = uri"https://mega.nz/file#abcd"

    MegaClient.extractMegaUrlComponents[IO](invalidUri).attempt.map {
      case Left(_: MegaClient.ErrorMegaUriFileIdtNotFound) => assert(true)
      case other                                           => fail(s"Expected ErrorMegaUriFileIdtNotFound, got: $other")
    }
  }

  test("extractMegaUrlComponents should fail when decryption key is missing") {
    val invalidUri = uri"https://mega.nz/file/someFileId"

    MegaClient.extractMegaUrlComponents[IO](invalidUri).attempt.map {
      case Left(_: MegaClient.ErrorMegaUriDecryptionKeyNotFound) => assert(true)
      case other => fail(s"Expected ErrorMegaUriDecryptionKeyNotFound, got: $other")
    }
  }
}
