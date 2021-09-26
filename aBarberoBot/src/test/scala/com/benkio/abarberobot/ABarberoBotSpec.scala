package com.benkio.abarberobot

import cats.effect._
import cats.implicits._
import com.benkio.telegrambotinfrastructure.botCapabilities.ResourceSource
import com.benkio.telegrambotinfrastructure.model.MediaFile
import com.benkio.telegrambotinfrastructure.model.TextTrigger
import munit.CatsEffectSuite

class ABarberoBotSpec extends CatsEffectSuite {

  def testFilename(filename: String): IO[Unit] =
    ResourceSource
      .selectResourceAccess(ABarberoBot.resourceSource)
      .getResourceByteArray[IO](filename)
      .use[Unit](fileBytes => assert(fileBytes.nonEmpty).pure[IO])

  test("messageRepliesAudioData should never raise an exception when try to open the file in resounces") {
    ABarberoBot.messageRepliesAudioData
      .flatMap(_.mediafiles)
      .foreach((mp3: MediaFile) => testFilename(mp3.filename))
  }

  test("messageRepliesGifData should never raise an exception when try to open the file in resounces") {
    ABarberoBot.messageRepliesGifData
      .flatMap(_.mediafiles)
      .foreach((gif: MediaFile) => testFilename(gif.filename))
  }

  test("messageRepliesSpecialData should never raise an exception when try to open the file in resounces") {
    for {
      rb <- ABarberoBot.messageRepliesSpecialData
      f1 <- rb.mediafiles
    } yield {
      testFilename(f1.filename)
    }
  }

  test("commandRepliesData should return a list of all triggers when called") {
    assertEquals(ABarberoBot.commandRepliesData.length, 1)
    assert(
      ABarberoBot.messageRepliesData
        .flatMap(
          _.trigger match {
            case TextTrigger(lt) => lt.map(_.toString)
            case _               => List.empty[String]
          }
        )
        .forall((s: String) =>
          ABarberoBot.commandRepliesData.flatMap(_.text.text(null)).flatten.mkString("\n").contains(s)
        )
    )
  }
}
