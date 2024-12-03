package com.benkio.telegrambotinfrastructure

import cats.effect.IO
import cats.implicits.*
import com.benkio.telegrambotinfrastructure.mocks.ApiMock.given
import com.benkio.telegrambotinfrastructure.mocks.SampleWebhookBot
import com.benkio.telegrambotinfrastructure.model.*
import com.benkio.telegrambotinfrastructure.model.ReplyBundle
import java.time.Instant
import log.effect.LogLevels
import log.effect.LogWriter
import log.effect.fs2.SyncLogWriter.consoleLogUpToLevel
import munit.CatsEffectSuite
import telegramium.bots.Chat
import telegramium.bots.Message

class BotSkeletonSpec extends CatsEffectSuite {

  given log: LogWriter[IO] = consoleLogUpToLevel(LogLevels.Info)

  /* TODO: test the function with the RichardPHJBensonBot sample data.
   *       be sure to include:
   *         - a case with only one selection
   *         - a case with multiple selection
   *         - the `carne` cornercase (multiple selection with longer foreign triggers and shorter targer trigger. check the issue 582)
   */
  test("selectReplyBundle should return all the expected `ReplyBundleMessage` in the right order") {
    val inputMessage: Message = Message(
      messageId = 0,
      date = Instant.now.getEpochSecond().toInt,
      chat = Chat(id = 0, `type` = "test"),
      text = Some(s"carne dura")
    )
    val expected = ReplyBundleMessage
      .textToMedia[IO](
        "carne (dura|vecchia|fresca)".r.tr(10),
      )(
        mp3"rphjb_CarneFrescaSaporita.mp3",
        vid"rphjb_CarneFrescaSaporita.mp4",
        gif"rphjb_CarneFrescaSaporitaGif.mp4"
      )

    for
      sampleWebhookBot <- SampleWebhookBot()
      resultOpt        <- sampleWebhookBot.selectReplyBundle(inputMessage)
      result <- resultOpt.fold(Throwable("BotSkeletonSpec expected Some, got None").raiseError[IO, String]) {
        _.prettyPrint()
      }
      expectedPP <- expected.prettyPrint()
    yield assertEquals(result, expectedPP)
  }
}
