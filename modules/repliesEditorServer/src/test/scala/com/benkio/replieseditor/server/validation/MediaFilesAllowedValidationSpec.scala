package com.benkio.replieseditor.server.validation

import com.benkio.chatcore.model.reply.PhotoFile
import com.benkio.chatcore.model.reply.ReplyBundleMessage
import munit.FunSuite

class MediaFilesAllowedValidationSpec extends FunSuite {

  test("validateAllFilesAreAllowed returns Right when all are allowed") {
    val replies = List(ReplyBundleMessage.textToMedia("a")(PhotoFile("cala_ok.jpg")))
    val res     = MediaFilesAllowedValidation.validateAllFilesAreAllowed(replies, allowed = Set("cala_ok.jpg"))
    assertEquals(res, Right(()))
  }

  test("validateAllFilesAreAllowed returns Left with invalidFiles details") {
    val replies = List(ReplyBundleMessage.textToMedia("a")(PhotoFile("cala_bad.jpg")))
    val res     = MediaFilesAllowedValidation.validateAllFilesAreAllowed(replies, allowed = Set("cala_ok.jpg"))
    assert(res.isLeft)
    val err = res.left.toOption.get
    assertEquals(err.error, "Some media files are not present in *_list.json")
    val invalid = err.details.flatMap(_.hcursor.downField("invalidFiles").as[List[String]].toOption).getOrElse(Nil)
    assertEquals(invalid, List("cala_bad.jpg"))
  }
}
