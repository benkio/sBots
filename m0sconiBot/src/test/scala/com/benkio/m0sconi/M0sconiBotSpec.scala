package com.benkio.M0sconi

import cats.Show
import cats.effect.IO
import cats.implicits._
import com.benkio.m0sconibot.M0sconiBot
import com.benkio.telegrambotinfrastructure.default.Actions.Action
import com.benkio.telegrambotinfrastructure.mocks.DBLayerMock
import com.benkio.telegrambotinfrastructure.model.Reply
import com.benkio.telegrambotinfrastructure.model.Trigger
import io.chrisdavenport.cormorant._
import io.chrisdavenport.cormorant.parser._
import log.effect.fs2.SyncLogWriter.consoleLogUpToLevel
import log.effect.LogLevels
import log.effect.LogWriter
import munit.CatsEffectSuite
import telegramium.bots.Chat
import telegramium.bots.Message

import java.io.File
import scala.io.Source

class M0sconiBotSpec extends CatsEffectSuite {

  implicit val noAction: Action[IO] = (_: Reply) => (_: Message) => IO.pure(List.empty)
  implicit val log: LogWriter[IO]   = consoleLogUpToLevel(LogLevels.Info)
  private val privateTestMessage    = Message(0, date = 0, chat = Chat(0, `type` = "private"))
  val emptyDBLayer                  = DBLayerMock.mock(M0sconiBot.botName)

  test("triggerlist should return the link to the trigger txt file") {
    val triggerlistUrl = M0sconiBot
      .commandRepliesData[IO](
        dbLayer = emptyDBLayer
      )
      .filter(_.trigger.command == "triggerlist")
      .flatMap(_.text.get.text(privateTestMessage).unsafeRunSync())
      .mkString("")
    assertEquals(
      M0sconiBot
        .commandRepliesData[IO](
          dbLayer = emptyDBLayer
        )
        .length,
      5
    )
    assertEquals(
      triggerlistUrl,
      "Puoi trovare la lista dei trigger al seguente URL: https://github.com/benkio/sBots/blob/master/m0sconiBot/mos_triggers.txt"
    )

  }

  test("the `mos_list.csv` should contain all the triggers of the bot") {
    val listPath   = new File(".").getCanonicalPath + "/mos_list.csv"
    val csvContent = Source.fromFile(listPath).getLines().mkString("\n")
    val csvFile = parseComplete(csvContent).flatMap {
      case CSV.Complete(_, CSV.Rows(rows)) => Right(rows.map(row => row.l.head.x))
      case _                               => Left(new RuntimeException("Error on parsing the csv"))
    }

    val botFile = M0sconiBot.messageRepliesData[IO].flatMap(_.mediafiles.map(_.filename))

    assert(csvFile.isRight)
    csvFile.fold(
      e => fail("test failed", e),
      files =>
        botFile.foreach(filename =>
          assert(files.contains(filename), s"$filename is not contained in mosconi data file")
        )
    )

  }

  test("the `mos_triggers.txt` should contain all the triggers of the bot") {
    val listPath       = new File(".").getCanonicalPath + "/mos_triggers.txt"
    val triggerContent = Source.fromFile(listPath).getLines().mkString("\n")

    val botMediaFiles = M0sconiBot.messageRepliesData[IO].flatMap(_.mediafiles.map(_.show))
    val botTriggersFiles =
      M0sconiBot.messageRepliesData[IO].flatMap(mrd => Show[Trigger].show(mrd.trigger).split('\n'))

    botMediaFiles.foreach { mediaFileString =>
      assert(triggerContent.contains(mediaFileString))
    }
    botTriggersFiles.foreach { triggerString =>
      assert(triggerContent.contains(triggerString), s"$triggerString is not contained in mosconi trigger file")
    }
  }

  test("instructions command should return the expected message") {
    val actual = M0sconiBot
      .commandRepliesData[IO](
        dbLayer = emptyDBLayer
      )
      .filter(_.trigger.command == "instructions")
      .flatTraverse(_.text.get.text(privateTestMessage))
    assertIO(
      actual,
      List(
        s"""
---- Instruzioni Per M0sconiBot ----

Per segnalare problemi, scrivere a: https://t.me/Benkio

I comandi del bot sono:

- '/triggerlist': Restituisce un link ad un file contenente tutti i trigger a cui il bot risponderà automaticamente. Alcuni di questi sono in formato Regex
- '/triggersearch 《testo》': Consente di cercare se una parola o frase fa parte di un trigger
- '/topTwentyTriggers': Restituisce una lista di file e il loro numero totale in invii
- '/timeout 《intervallo》': Consente di impostare un limite di tempo tra una risposta e l'altra nella specifica chat. Formato dell'input: 00:00:00

Se si vuole disabilitare il bot per un particolare messaggio impedendo
che interagisca, è possibile farlo iniziando il messaggio con il
carattere: `!`

! Messaggio
""",
        s"""
---- Instructions for M0sconiBot ----

to report issues, write to: https://t.me/Benkio

Bot commands are:

- '/triggerlist': Return a link to a file containing all the triggers used by the bot. Bot will reply automatically to these ones. Some of them are Regex
- '/triggersearch 《text》': Allow you to search if a specific word or phrase is part of a trigger
- '/topTwentyTriggers': Return a list of files and theirs send frequency
- '/timeout 《time》': Allow you to set a timeout between bot's replies in the specific chat. input time format: 00:00:00

if you wish to disable the bot for a specific message, blocking its reply/interaction, you can do adding the following character as prefix
character: `!`

! Message
"""
      )
    )
  }
}
