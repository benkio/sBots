package com.benkio.richardphjbensonbot

import cats.effect.IO
import cats.implicits._
import com.benkio.telegrambotinfrastructure.model.NewMemberTrigger
import com.benkio.telegrambotinfrastructure.model.TextTrigger
import io.chrisdavenport.cormorant._
import io.chrisdavenport.cormorant.parser._
import munit.CatsEffectSuite
import telegramium.bots.Chat
import telegramium.bots.Message

import java.io.File
import scala.io.Source

class RichardPHJBensonBotSpec extends CatsEffectSuite {

  import com.benkio.richardphjbensonbot.data.Special.messageRepliesSpecialData

  private val privateTestMessage = Message(0, date = 0, chat = Chat(0, `type` = "private"))

  test("messageRepliesSpecialData should contain a NewMemberTrigger") {
    val result =
      messageRepliesSpecialData[IO]
        .map(_.trigger match {
          case NewMemberTrigger => true
          case _                => false
        })
        .exists(identity(_))

    assert(result, true)
  }

  test("triggerlist should return a list of all triggers when called") {
    assertEquals(RichardPHJBensonBot.commandRepliesData[IO](null).length, 5)
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
            .commandRepliesData[IO](null)
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
      .commandRepliesData[IO](null)
      .filter(_.trigger.command == "triggerlist")
      .flatTraverse(_.text.text(nonPrivateTestMessage))
    assertIO(
      actual,
      List("NON TE LO PUOI PERMETTERE!!!(puoi usare questo comando solo in chat privata)")
    )
  }
  test("instructions command should return the expected message") {
    val actual = RichardPHJBensonBot
      .commandRepliesData[IO](null)
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

  test("the `rphjb_list.csv` should contain all the triggers of the bot") {
    val listPath   = new File(".").getCanonicalPath + "/rphjb_list.csv"
    val csvContent = Source.fromFile(listPath).getLines().mkString("\n")
    val csvFile = parseComplete(csvContent).flatMap {
      case CSV.Complete(_, CSV.Rows(rows)) => Right(rows.map(row => row.l.head.x))
      case _                               => Left(new RuntimeException("Error on parsing the csv"))
    }

    val botFile = RichardPHJBensonBot.messageRepliesData[IO].flatMap(_.mediafiles.map(_.filename))

    assert(csvFile.isRight)
    csvFile.fold(
      e => fail("test failed", e),
      files => assert(botFile.forall(filename => files.contains(filename)))
    )

  }
}
