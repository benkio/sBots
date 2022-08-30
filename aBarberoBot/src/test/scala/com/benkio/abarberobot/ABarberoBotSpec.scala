package com.benkio.abarberobot

import cats.effect.IO
import cats.implicits._
import com.benkio.telegrambotinfrastructure.botcapabilities.ResourceAccess
import com.benkio.telegrambotinfrastructure.botcapabilities.ResourceAccessSpec
import com.benkio.telegrambotinfrastructure.model.MediaFile
import com.benkio.telegrambotinfrastructure.model.TextTrigger
import munit.CatsEffectSuite
import telegramium.bots.Chat
import telegramium.bots.Message

class ABarberoBotSpec extends CatsEffectSuite {

  private val privateTestMessage = Message(0, date = 0, chat = Chat(0, `type` = "private"))
  implicit val resourceAccess    = ResourceAccess.fromResources[IO]

  test("messageRepliesAudioData should never raise an exception when try to open the file in resounces") {
    val result = ABarberoBot
      .messageRepliesAudioData[IO]
      .flatMap(_.mediafiles)
      .traverse((mp3: MediaFile) => ResourceAccessSpec.testFilename(mp3.filename))
      .map(_.foldLeft(true)(_ && _))

    assertIO(result, true)
  }

  test("messageRepliesGifData should never raise an exception when try to open the file in resounces") {
    val result = ABarberoBot
      .messageRepliesGifData[IO]
      .flatMap(_.mediafiles)
      .traverse((gif: MediaFile) => ResourceAccessSpec.testFilename(gif.filename))
      .map(_.foldLeft(true)(_ && _))

    assertIO(result, true)
  }

  test("messageRepliesSpecialData should never raise an exception when try to open the file in resounces") {
    val result = ABarberoBot
      .messageRepliesSpecialData[IO]
      .flatMap(_.mediafiles)
      .traverse(mf => ResourceAccessSpec.testFilename(mf.filename))
      .map(_.foldLeft(true)(_ && _))

    assertIO(result, true)
  }

  test("triggerlist should return a list of all triggers when called") {
    assertEquals(ABarberoBot.commandRepliesData[IO].length, 2)
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
          ABarberoBot
            .commandRepliesData[IO]
            .flatMap(_.text.text(privateTestMessage).unsafeRunSync())
            .mkString("\n")
            .contains(s)
        )
    )
  }

  test("triggerlist command should return the warning message if the input message is not a private chat") {
    val nonPrivateTestMessage = Message(0, date = 0, chat = Chat(0, `type` = "group"))
    val actual = ABarberoBot
      .commandRepliesData[IO]
      .filter(_.trigger.command == "triggerlist")
      .flatTraverse(_.text.text(nonPrivateTestMessage))
    assertIO(
      actual,
      List("puoi usare questo comando solo in chat privata")
    )
  }
}
