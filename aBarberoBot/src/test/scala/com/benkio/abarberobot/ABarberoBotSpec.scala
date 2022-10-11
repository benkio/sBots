package com.benkio.abarberobot

import cats.Show
import cats.effect.IO
import cats.implicits._
import com.benkio.telegrambotinfrastructure.model.Trigger
import io.chrisdavenport.cormorant._
import io.chrisdavenport.cormorant.parser._
import munit.CatsEffectSuite
import telegramium.bots.Chat
import telegramium.bots.Message

import java.io.File
import scala.io.Source

class ABarberoBotSpec extends CatsEffectSuite {

  private val privateTestMessage = Message(0, date = 0, chat = Chat(0, `type` = "private"))

  test("triggerlist should return a list of all triggers when called") {
    val triggerlist: String = ABarberoBot
      .commandRepliesData[IO]
      .filter(_.trigger.command == "triggerlist")
      .flatMap(_.text.text(privateTestMessage).unsafeRunSync())
      .mkString("\n")
    assertEquals(ABarberoBot.commandRepliesData[IO].length, 3)
    assertEquals(
      triggerlist,
      "Puoi trovare la lista dei trigger al seguente URL: https://github.com/benkio/myTelegramBot/blob/master/aBarberoBot/abar_triggers.txt"
    )
  }

  test("the `abar_list.csv` should contain all the triggers of the bot") {
    val listPath   = new File(".").getCanonicalPath + "/abar_list.csv"
    val csvContent = Source.fromFile(listPath).getLines().mkString("\n")
    val csvFile = parseComplete(csvContent).flatMap {
      case CSV.Complete(_, CSV.Rows(rows)) => Right(rows.map(row => row.l.head.x))
      case _                               => Left(new RuntimeException("Error on parsing the csv"))
    }

    val botFile = ABarberoBot.messageRepliesData[IO].flatMap(_.mediafiles.map(_.filename))

    assert(csvFile.isRight)
    csvFile.fold(
      e => fail("test failed", e),
      files => assert(botFile.forall(filename => files.contains(filename)))
    )

  }

  test("the `abar_triggers.txt` should contain all the triggers of the bot") {
    val listPath       = new File(".").getCanonicalPath + "/abar_triggers.txt"
    val triggerContent = Source.fromFile(listPath).getLines().mkString("\n")

    val botMediaFiles = ABarberoBot.messageRepliesData[IO].flatMap(_.mediafiles.map(_.show))
    val botTriggersFiles =
      ABarberoBot.messageRepliesData[IO].flatMap(mrd => Show[Trigger].show(mrd.trigger).split('\n'))

    botMediaFiles.foreach { mediaFileString =>
      assert(triggerContent.contains(mediaFileString))
    }
    botTriggersFiles.foreach { triggerString =>
      {
        val result = triggerContent.contains(triggerString)
        if (!result) {
          println(s"triggerString: " + triggerString)
          println(s"content: " + triggerContent)
        }
        assert(result)
      }
    }
  }

  test("instructions command should return the expected message") {
    val actual = ABarberoBot
      .commandRepliesData[IO]
      .filter(_.trigger.command == "instructions")
      .flatTraverse(_.text.text(privateTestMessage))
    assertIO(
      actual,
      List(
        s"""
---- Instruzioni Per ABarberoBot ----

I comandi del bot sono:

- '/triggerlist': Restituisce un link ad un file contenente tutti i trigger a cui il bot risponderà automaticamente. Alcuni di questi sono in formato Regex
- '/triggersearch 《testo》': Consente di cercare se una parola o frase fa parte di un trigger
- '/randomshow': Restituisce un link di uno show/video riguardante il personaggio del bot
- '/randomshowkeyword 《testo》': Restituisce un link di uno show/video riguardante il personaggio del bot e contenente il testo specificato

Se si vuole disabilitare il bot per un particolare messaggio impedendo
che interagisca, è possibile farlo iniziando il messaggio con il
carattere: `!`

! Messaggio
""",
        s"""
---- Instructions for ABarberoBot ----

Bot commands are:

- '/triggerlist': Return a link to a file containing all the triggers used by the bot. Bot will reply automatically to these ones. Some of them are Regex
- '/triggersearch 《text》': Allow you to search if a specific word or phrase is part of a trigger
- '/randomshow': Return the link of one show/video about the bot's character
- '/randomshowkeyword 《text》': Return a link of a show/video about the specific bot's character and containing the specified keyword

if you wish to disable the bot for a specific message, blocking its reply/interaction, you can do adding the following character as prefix
character: `!`

! Message
"""
      )
    )
  }
}
