package com.benkio.chatcore.model

import com.benkio.chatcore.Arbitraries.given
import com.benkio.chatcore.Generators.mediaNameForMimeGen
import munit.ScalaCheckSuite
import org.scalacheck.Arbitrary.arbitrary
import org.scalacheck.Gen
import org.scalacheck.Prop.*

class MimeTypeSpec extends ScalaCheckSuite {

  property("explicit known mime type wins over filename") {
    forAll(arbitrary[MimeType], Gen.alphaNumStr) { (mime: MimeType, name: String) =>
      assertEquals(MimeTypeOps.mimeTypeOrDefault(name, Some(mime.value)), mime)
    }
  }

  property("filename extension infers mime type when mime is absent") {
    forAll(mediaNameForMimeGen) { case (name, expected) =>
      assertEquals(MimeTypeOps.mimeTypeOrDefault(name, None), expected)
    }
  }

  property("unrecognized mime and extension fall back to DOC") {
    forAll(Gen.alphaNumStr.map(_ + ".abc")) { (name: String) =>
      assertEquals(MimeTypeOps.mimeTypeOrDefault(name, Some("invalid/type")), MimeType.DOC)
      assertEquals(MimeTypeOps.mimeTypeOrDefault(name, None), MimeType.DOC)
    }
  }
}
