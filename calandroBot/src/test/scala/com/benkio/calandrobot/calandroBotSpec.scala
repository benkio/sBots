package com.benkio.calandrobot

import com.benkio.telegrambotinfrastructure.botCapabilities.ResourceSource
import org.scalatest._
import com.benkio.telegrambotinfrastructure.model.MediaFile
import org.scalatest.wordspec.AnyWordSpec

class CalandroBotSpec extends AnyWordSpec {

  def testFilename(filename: String): Assertion =
    if (
      ResourceSource
        .selectResourceAccess(CalandroBot.resourceSource)
        .getResourceByteArray(filename)
        .isEmpty
    )
      fail(s"$filename cannot be found")
    else succeed

  "commandRepliesData" should {
    "never raise an exception" when {
      "try to open the file in resounces" in {
        CalandroBot.commandRepliesData
          .flatMap(_.mediafiles)
          .foreach((mf: MediaFile) => testFilename(mf.filename))
      }
    }
  }

  "messageRepliesData" should {
    "never raise an exception" when {
      "try to open the file in resounces" in {
        CalandroBot.messageRepliesData
          .flatMap(_.mediafiles)
          .foreach((mf: MediaFile) => testFilename(mf.filename))
      }
    }
  }
}
