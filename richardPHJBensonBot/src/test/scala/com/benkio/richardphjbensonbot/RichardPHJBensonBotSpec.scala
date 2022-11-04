package com.benkio.richardphjbensonbot

import cats.Show
import cats.effect.IO
import cats.implicits._
import com.benkio.telegrambotinfrastructure.model.LeftMemberTrigger
import com.benkio.telegrambotinfrastructure.model.NewMemberTrigger
import com.benkio.telegrambotinfrastructure.model.Trigger
import com.benkio.telegrambotinfrastructure.resources.ResourceAccess
import com.benkio.telegrambotinfrastructure.resources.db.DBLayer
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

class RichardPHJBensonBotSpec extends CatsEffectSuite {

  import com.benkio.richardphjbensonbot.data.Special.messageRepliesSpecialData

  implicit val log: LogWriter[IO] = consoleLogUpToLevel(LogLevels.Info)
  private val privateTestMessage  = Message(0, date = 0, chat = Chat(0, `type` = "private"))

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

  test("messageRepliesSpecialData should contain a LeftMemberTrigger") {
    val result =
      messageRepliesSpecialData[IO]
        .map(_.trigger match {
          case LeftMemberTrigger => true
          case _                 => false
        })
        .exists(identity(_))

    assert(result, true)
  }
  val emptyDBLayer = DBLayer[IO](null, null)

  test("triggerlist should return a list of all triggers when called") {
    val triggerlist = RichardPHJBensonBot
      .commandRepliesData[IO](ResourceAccess.fromResources[IO], emptyDBLayer, "")
      .filter(_.trigger.command == "triggerlist")
      .flatMap(_.text.text(privateTestMessage).unsafeRunSync())
      .mkString("")
    assertEquals(
      RichardPHJBensonBot.commandRepliesData[IO](ResourceAccess.fromResources[IO], emptyDBLayer, "").length,
      8
    )
    assertEquals(
      triggerlist,
      "Puoi trovare la lista dei trigger al seguente URL: https://github.com/benkio/myTelegramBot/blob/master/richardPHJBensonBot/rphjb_triggers.txt"
    )
  }

  test("instructions command should return the expected message") {
    val actual = RichardPHJBensonBot
      .commandRepliesData[IO](ResourceAccess.fromResources[IO], emptyDBLayer, "")
      .filter(_.trigger.command == "instructions")
      .flatTraverse(_.text.text(privateTestMessage))
    assertIO(
      actual,
      List(
        s"""
---- Instruzioni Per RichardPHJBensonBot ----

I comandi del bot sono:

- '/triggerlist': Restituisce un link ad un file contenente tutti i trigger a cui il bot risponderà automaticamente. Alcuni di questi sono in formato Regex
- '/triggersearch 《testo》': Consente di cercare se una parola o frase fa parte di un trigger
- '/randomshow': Restituisce un link di uno show/video riguardante il personaggio del bot
- '/randomshowkeyword 《testo》': Restituisce un link di uno show/video riguardante il personaggio del bot e contenente il testo specificato
- '/topTwentyTriggers': Restituisce una lista di file e il loro numero totale in invii
- '/timeout 《intervallo》': Consente di impostare un limite di tempo tra una risposta e l'altra nella specifica chat. Formato dell'input: 00:00:00
- '/bensonify 《testo》': Traduce il testo in input nello stesso modo in cui benson lo scriverebbe. Il testo è obbligatorio

Se si vuole disabilitare il bot per un particolare messaggio impedendo
che interagisca, è possibile farlo iniziando il messaggio con il
carattere: `!`

! Messaggio
""",
        s"""
---- Instructions for RichardPHJBensonBot ----

Bot commands are:

- '/triggerlist': Return a link to a file containing all the triggers used by the bot. Bot will reply automatically to these ones. Some of them are Regex
- '/triggersearch 《text》': Allow you to search if a specific word or phrase is part of a trigger
- '/randomshow': Return the link of one show/video about the bot's character
- '/randomshowkeyword 《text》': Return a link of a show/video about the specific bot's character and containing the specified keyword
- '/topTwentyTriggers': Return a list of files and theirs send frequency
- '/timeout 《time》': Allow you to set a timeout between bot's replies in the specific chat. input time format: 00:00:00
- '/bensonify 《text》': Translate the text in the same way benson would write it. Text input is mandatory

if you wish to disable the bot for a specific message, blocking its reply/interaction, you can do adding the following character as prefix
character: `!`

! Message
"""
      )
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
      files =>
        assert(botFile.forall(filename => {
          if (!files.contains(filename)) {
            println(s"[ERROR] missing filename: " + filename)
          }
          files.contains(filename)
        }))
    )

  }

  test("the `rphjb_triggers.txt` should contain all the triggers of the bot") {
    val listPath       = new File(".").getCanonicalPath + "/rphjb_triggers.txt"
    val triggerContent = Source.fromFile(listPath).getLines().mkString("\n")

    val botMediaFiles = RichardPHJBensonBot.messageRepliesData[IO].flatMap(_.mediafiles.map(_.show))
    val botTriggersFiles =
      RichardPHJBensonBot.messageRepliesData[IO].flatMap(mrd => Show[Trigger].show(mrd.trigger).split('\n'))

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
}
