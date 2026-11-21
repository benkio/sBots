package com.benkio.chatcore

import cats.effect.IO
import com.benkio.chatcore.model.reply.Mp3File
import com.benkio.chatcore.model.Message
import com.benkio.chatcore.model.SBotInfo
import com.benkio.chatcore.Logger.given
import munit.CatsEffectSuite

class NotifierLoggingSpec extends CatsEffectSuite {

  private val botInfo   = SBotInfo(SBotInfo.SBotId("bot"), SBotInfo.SBotName("BotName"))
  private val message   = Message(None, 0L, TestIds.chatId(1L), "private", text = Some("hello"))
  private val mediaFile = Mp3File("a.mp3")
  private val boom      = new RuntimeException("boom")

  private val noRights =
    new RuntimeException("code=400 description=Bad Request: not enough rights to send animations to the chat")

  test("logging writes the infos and both the reportable and the missing-rights media errors") {
    val notifier = Notifier.logging[IO]

    notifier.info(botInfo, "started") *>
      notifier.mediaError(message, mediaFile, boom) *>
      notifier.mediaError(message, mediaFile, noRights)
  }

  test("logging writes both the reportable and the missing-rights errors") {
    val notifier = Notifier.logging[IO]

    notifier.error(botInfo, "a job", boom) *>
      notifier.error(botInfo, "a job", noRights)
  }
}
