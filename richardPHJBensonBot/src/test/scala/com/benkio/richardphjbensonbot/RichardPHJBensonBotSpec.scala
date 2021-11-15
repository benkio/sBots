package com.benkio.richardphjbensonbot

import cats.implicits._
import com.benkio.telegrambotinfrastructure.botCapabilities.ResourceAccessSpec
import com.benkio.telegrambotinfrastructure.model.MediaFile
import com.benkio.telegrambotinfrastructure.model.TextTrigger
import munit.CatsEffectSuite

class RichardPHJBensonBotSpec extends CatsEffectSuite {

  test("messageRepliesAudioData should never raise an exception when try to open the file in resounces") {
    val result = RichardPHJBensonBot.messageRepliesAudioData
      .flatMap(_.mediafiles)
      .traverse((mp3: MediaFile) => ResourceAccessSpec.testFilename(mp3.filename, RichardPHJBensonBot.resourceSource))
      .map(_.foldLeft(true)(_ && _))

    assertIO(result, true)
  }

  test("messageRepliesGifData should never raise an exception when try to open the file in resounces") {
    val result = RichardPHJBensonBot.messageRepliesGifData
      .flatMap(_.mediafiles)
      .traverse((gif: MediaFile) => ResourceAccessSpec.testFilename(gif.filename, RichardPHJBensonBot.resourceSource))
      .map(_.foldLeft(true)(_ && _))

    assertIO(result, true)
  }

  test("messageRepliesVideosData should never raise an exception when try to open the file in resounces") {
    val result = RichardPHJBensonBot.messageRepliesVideoData
      .flatMap(_.mediafiles)
      .traverse((video: MediaFile) =>
        ResourceAccessSpec.testFilename(video.filename, RichardPHJBensonBot.resourceSource)
      )
      .map(_.foldLeft(true)(_ && _))

    assertIO(result, true)
  }

  test("messageRepliesMixData should never raise an exception when try to open the file in resounces") {
    val result =
      RichardPHJBensonBot.messageRepliesMixData
        .flatMap(_.mediafiles)
        .traverse(mf => ResourceAccessSpec.testFilename(mf.filename, RichardPHJBensonBot.resourceSource))
        .map(_.foldLeft(true)(_ && _))

    assertIO(result, true)
  }

  test("commandRepliesData should return a list of all triggers when called") {
    assertEquals(RichardPHJBensonBot.commandRepliesData.length, 3)
    assert(
      RichardPHJBensonBot.messageRepliesData
        .flatMap(
          _.trigger match {
            case TextTrigger(lt) => lt.map(_.toString)
            case _               => List.empty[String]
          }
        )
        .forall(s =>
          RichardPHJBensonBot.commandRepliesData
            .filter(_.trigger.command != "bensonify")
            .flatMap(_.text.text(null))
            .flatten
            .mkString("\n")
            .contains(s)
        )
    )
  }
}
