package com.benkio.chatcore

import cats.effect.IO
import com.benkio.chatcore.model.reply.Mp3File
import com.benkio.chatcore.model.Message
import com.benkio.chatcore.model.SBotInfo
import com.benkio.chatcore.repository.Repository.RepositoryError
import com.benkio.chatcore.TestIds
import munit.CatsEffectSuite
import munit.ScalaCheckSuite
import org.scalacheck.Gen
import org.scalacheck.Prop.*

class NotifierSpec extends ScalaCheckSuite {

  private val message: Message =
    Message(
      Some(TestIds.messageId(1)),
      0L,
      TestIds.chatId(1L),
      "private",
      text = Some("hello"),
      caption = Some("caption")
    )
  private val mediaFile = Mp3File("audio.mp3")
  private val botInfo   = SBotInfo(SBotInfo.SBotId("bot"), SBotInfo.SBotName("BotName"))

  property("formatMediaError includes the text and the caption of the message, the media file and the error message") {
    forAll(Gen.alphaStr) { (errMsg: String) =>
      val formatted = Notifier.formatMediaError(message, mediaFile, new RuntimeException(errMsg))
      assert(formatted.contains("hello"))
      assert(formatted.contains("caption"))
      assert(formatted.contains(mediaFile.toString) || formatted.contains("audio.mp3"))
      assert(formatted.contains(errMsg))
    }
  }

  property("formatInfo prefixes the name of the bot") {
    forAll(Gen.alphaStr) { (msg: String) =>
      assertEquals(Notifier.formatInfo(botInfo, msg), s"[${botInfo.botName}] $msg")
    }
  }

  test("the errors of a file that does not exist are not reported, the others are") {
    assert(!Notifier.isReportable(RepositoryError.NoResourcesFoundFile(mediaFile)))
    assert(Notifier.isReportable(new RuntimeException("boom")))
  }
}

class NotifierNoopSpec extends CatsEffectSuite {

  test("the noop notifier does nothing and does not fail") {
    val notifier = Notifier.noop[IO]
    val message  = Message(Some(TestIds.messageId(1)), 0L, TestIds.chatId(1L), "private")
    notifier.info(SBotInfo(SBotInfo.SBotId("bot"), SBotInfo.SBotName("BotName")), "started") *>
      notifier.mediaError(message, Mp3File("a.mp3"), new RuntimeException("boom"))
  }
}
