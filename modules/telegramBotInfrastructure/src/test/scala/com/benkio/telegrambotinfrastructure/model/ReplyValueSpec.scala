package com.benkio.telegrambotinfrastructure.model

import com.benkio.telegrambotinfrastructure.model.reply.ReplyValue
import munit.FunSuite
import io.circe.parser.decode
import io.circe.syntax.*

class ReplyValueSpec extends FunSuite {

  test("ReplyValue JSON decode/encode should work as expected") {
    val jsonInputs = List(
      """{
        |  "Text" : {
        |    "value" : "testText"
        |  }
        |}""".stripMargin,
      """{
        |  "Mp3File" : {
        |    "filepath" : "testFilePath.mp3",
        |    "replyToMessage" : false
        |  }
        |}""".stripMargin,
      """{
        |  "GifFile" : {
        |    "filepath" : "testFilePath.gif",
        |    "replyToMessage" : false
        |  }
        |}""".stripMargin,
      """{
        |  "PhotoFile" : {
        |    "filepath" : "testFilePath.jpg",
        |    "replyToMessage" : false
        |  }
        |}""".stripMargin,
      """{
        |  "VideoFile" : {
        |    "filepath" : "testFilePath.mp4",
        |    "replyToMessage" : false
        |  }
        |}""".stripMargin,
    )

    for inputString <- jsonInputs
    yield {
      val eitherMessageTrigger = decode[ReplyValue](inputString)
      eitherMessageTrigger.fold(
        e => fail("failed in parsing the input string as replyValue", e),
        ms => assertEquals(ms.asJson.toString, inputString)
      )
    }
  }
}
