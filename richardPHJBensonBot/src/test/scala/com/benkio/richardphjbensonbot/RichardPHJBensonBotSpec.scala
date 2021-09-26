package com.benkio.richardphjbensonbot

import cats.effect._
import cats.implicits._
import com.benkio.telegrambotinfrastructure.botCapabilities.ResourceSource
import com.benkio.telegrambotinfrastructure.model.MediaFile
import com.benkio.telegrambotinfrastructure.model.TextTrigger
import munit.CatsEffectSuite

class RichardPHJBensonBotSpec extends CatsEffectSuite {

  def testFilename(filename: String): IO[Unit] =
    ResourceSource
      .selectResourceAccess(RichardPHJBensonBot.resourceSource)
      .getResourceByteArray[IO](filename)
      .use[Unit]((fileBytes: Array[Byte]) => assert(fileBytes.nonEmpty).pure[IO])

  test("messageRepliesAudioData should never raise an exception when try to open the file in resounces") {
    RichardPHJBensonBot.messageRepliesAudioData
      .flatMap(_.mediafiles)
      .foreach((mp3: MediaFile) => testFilename(mp3.filename))
  }

  test("messageRepliesGifData should never raise an exception when try to open the file in resounces") {
    RichardPHJBensonBot.messageRepliesGifData
      .flatMap(_.mediafiles)
      .foreach((gif: MediaFile) => testFilename(gif.filename))
  }

  test("messageRepliesVideosData should never raise an exception when try to open the file in resounces") {
    RichardPHJBensonBot.messageRepliesVideoData
      .flatMap(_.mediafiles)
      .foreach((video: MediaFile) => testFilename(video.filename))
  }

  test("messageRepliesSpecialData should never raise an exception when try to open the file in resounces") {
    for {
      rb <- RichardPHJBensonBot.messageRepliesSpecialData
      f1 <- rb.mediafiles
    } yield {
      testFilename(f1.filename)
    }
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
