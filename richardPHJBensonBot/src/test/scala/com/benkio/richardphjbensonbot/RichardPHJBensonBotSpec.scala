package com.benkio.richardphjbensonbot

import cats.effect.IO
import cats.implicits._
import com.benkio.telegrambotinfrastructure.botcapabilities.ResourceAccessSpec
import com.benkio.telegrambotinfrastructure.model.MediaFile
import com.benkio.telegrambotinfrastructure.model.NewMemberTrigger
import com.benkio.telegrambotinfrastructure.model.TextTrigger
import munit.CatsEffectSuite
import telegramium.bots.Chat
import telegramium.bots.Message

class RichardPHJBensonBotSpec extends CatsEffectSuite {

  private val privateTestMessage = Message(0, date = 0, chat = Chat(0, `type` = "private"))

  test("messageRepliesAudioData should never raise an exception when try to open the file in resounces") {
    val result = RichardPHJBensonBot
      .messageRepliesAudioData[IO]
      .flatMap(_.mediafiles)
      .traverse((mp3: MediaFile) => ResourceAccessSpec.testFilename(mp3.filename, RichardPHJBensonBot.resourceSource))
      .map(_.foldLeft(true)(_ && _))

    assertIO(result, true)
  }

  test("messageRepliesGifData should never raise an exception when try to open the file in resounces") {
    val result = RichardPHJBensonBot
      .messageRepliesGifData[IO]
      .flatMap(_.mediafiles)
      .traverse((gif: MediaFile) => ResourceAccessSpec.testFilename(gif.filename, RichardPHJBensonBot.resourceSource))
      .map(_.foldLeft(true)(_ && _))

    assertIO(result, true)
  }

  test("messageRepliesVideosData should never raise an exception when try to open the file in resounces") {
    val result = RichardPHJBensonBot
      .messageRepliesVideoData[IO]
      .flatMap(_.mediafiles)
      .traverse((video: MediaFile) =>
        ResourceAccessSpec.testFilename(video.filename, RichardPHJBensonBot.resourceSource)
      )
      .map(_.foldLeft(true)(_ && _))

    assertIO(result, true)
  }

  test("messageRepliesMixData should never raise an exception when try to open the file in resounces") {
    val result =
      RichardPHJBensonBot
        .messageRepliesMixData[IO]
        .flatMap(_.mediafiles)
        .traverse(mf => ResourceAccessSpec.testFilename(mf.filename, RichardPHJBensonBot.resourceSource))
        .map(_.foldLeft(true)(_ && _))

    assertIO(result, true)
  }

  test("messageRepliesSpecialData should never raise an exception when try to open the file in resounces") {
    val result =
      RichardPHJBensonBot
        .messageRepliesSpecialData[IO]
        .flatMap(_.mediafiles)
        .traverse(mf => ResourceAccessSpec.testFilename(mf.filename, RichardPHJBensonBot.resourceSource))
        .map(_.foldLeft(true)(_ && _))

    assertIO(result, true)
  }

  test("messageRepliesSpecialData should contain a NewMemberTrigger") {
    val result =
      RichardPHJBensonBot
        .messageRepliesSpecialData[IO]
        .map(_.trigger match {
          case NewMemberTrigger => true
          case _                => false
        })
        .exists(identity(_))

    assert(result, true)
  }

  test("triggerlist should return a list of all triggers when called") {
    assertEquals(RichardPHJBensonBot.commandRepliesData[IO].length, 3)
    assert(
      RichardPHJBensonBot
        .messageRepliesData[IO]
        .flatMap(
          _.trigger match {
            case TextTrigger(lt @ _*) => lt.map(_.toString)
            case _                    => List.empty[String]
          }
        )
        .forall(s =>
          RichardPHJBensonBot
            .commandRepliesData[IO]
            .filter(_.trigger.command == "triggerlist")
            .flatMap(_.text.text(privateTestMessage).unsafeRunSync())
            .mkString("\n")
            .contains(s)
        )
    )
  }

  test("triggerlist command should return the warning message if the input message is not a private chat") {
    val nonPrivateTestMessage = Message(0, date = 0, chat = Chat(0, `type` = "group"))
    val actual = RichardPHJBensonBot
      .commandRepliesData[IO]
      .filter(_.trigger.command == "triggerlist")
      .flatTraverse(_.text.text(nonPrivateTestMessage))
    assertIO(
      actual,
      List("NON TE LO PUOI PERMETTERE!!!(puoi usare questo comando solo in chat privata)")
    )
  }
  test("instructions command should return the expected message") {
    val actual = RichardPHJBensonBot
      .commandRepliesData[IO]
      .filter(_.trigger.command == "instructions")
      .flatTraverse(_.text.text(privateTestMessage))
    assertIO(
      actual,
      List(s"""
---- Instruzioni Per il Bot di Benson ----

Il bot reagisce automaticamente ai messaggi in base ai trigger che si
possono trovare dal comando:

/triggerlist

ATTENZIONE: tale comando invierà una lunga lista. Consultarlo
privatamente nella chat del bot.

Questo bot consente di convertire le frasi come le direbbe Richard
attraverso il comando:

/bensonify «Frase»

la frase è necessaria, altrimenti il bot vi risponderà in malomodo.

Infine, se si vuole disabilitare il bot per un particolare messaggio,
ad esempio per un messaggio lungo che potrebbe causare vari trigger
in una volta, è possibile farlo iniziando il messaggio con il
carattere '!':

! «Messaggio»
""")
    )
  }
}
