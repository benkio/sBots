package com.benkio.abarberobot

import cats.effect.IO
import cats.implicits._
import com.benkio.telegrambotinfrastructure.botCapabilities.ResourceAccessSpec
import com.benkio.telegrambotinfrastructure.model.MediaFile
import com.benkio.telegrambotinfrastructure.model.TextTrigger
import munit.CatsEffectSuite

class ABarberoBotSpec extends CatsEffectSuite {

  test("messageRepliesAudioData should never raise an exception when try to open the file in resounces") {
    val result = ABarberoBot
      .messageRepliesAudioData[IO]
      .flatMap(_.mediafiles)
      .traverse((mp3: MediaFile) => ResourceAccessSpec.testFilename(mp3.filename, ABarberoBot.resourceSource))
      .map(_.foldLeft(true)(_ && _))

    assertIO(result, true)
  }

  test("messageRepliesGifData should never raise an exception when try to open the file in resounces") {
    val result = ABarberoBot
      .messageRepliesGifData[IO]
      .flatMap(_.mediafiles)
      .traverse((gif: MediaFile) => ResourceAccessSpec.testFilename(gif.filename, ABarberoBot.resourceSource))
      .map(_.foldLeft(true)(_ && _))

    assertIO(result, true)
  }

  test("messageRepliesSpecialData should never raise an exception when try to open the file in resounces") {
    val result = ABarberoBot
      .messageRepliesSpecialData[IO]
      .flatMap(_.mediafiles)
      .traverse(mf => ResourceAccessSpec.testFilename(mf.filename, ABarberoBot.resourceSource))
      .map(_.foldLeft(true)(_ && _))

    assertIO(result, true)
  }

  test("commandRepliesData should return a list of all triggers when called") {
    assertEquals(ABarberoBot.commandRepliesData[IO].length, 1)
    assert(
      ABarberoBot
        .messageRepliesData[IO]
        .flatMap(
          _.trigger match {
            case TextTrigger(lt @ _*) => lt.map(_.toString)
            case _                    => List.empty[String]
          }
        )
        .forall((s: String) =>
          ABarberoBot.commandRepliesData[IO].flatMap(_.text.text(null).unsafeRunSync()).mkString("\n").contains(s)
        )
    )
  }
}
