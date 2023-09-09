package com.benkio.richardphjbensonbot

import cats.Show
import cats.effect.IO
import cats.implicits._
import com.benkio.telegrambotinfrastructure.BackgroundJobManager
import com.benkio.telegrambotinfrastructure.default.Actions.Action
import com.benkio.telegrambotinfrastructure.mocks.DBLayerMock
import com.benkio.telegrambotinfrastructure.model.LeftMemberTrigger
import com.benkio.telegrambotinfrastructure.model.NewMemberTrigger
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

class RichardPHJBensonBotSpec extends CatsEffectSuite {

  import com.benkio.richardphjbensonbot.data.Special.messageRepliesSpecialData

  implicit val log: LogWriter[IO]   = consoleLogUpToLevel(LogLevels.Info)
  implicit val noAction: Action[IO] = (_: Reply) => (_: Message) => IO.pure(List.empty[Message])

  private val privateTestMessage = Message(0, date = 0, chat = Chat(0, `type` = "private"))
  val emptyDBLayer               = DBLayerMock.mock(RichardPHJBensonBot.botName)
  val emptyBackgroundJobManager = BackgroundJobManager(
    dbSubscription = emptyDBLayer.dbSubscription,
    dbShow = emptyDBLayer.dbShow,
    botName = "RichardPHJBensonBot"
  ).unsafeRunSync()

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

  test("triggerlist should return a list of all triggers when called") {
    val triggerlist = RichardPHJBensonBot
      .commandRepliesData[IO](
        backgroundJobManager = emptyBackgroundJobManager,
        dbLayer = emptyDBLayer
      )
      .filter(_.trigger.command == "triggerlist")
      .flatMap(_.text.get.text(privateTestMessage).unsafeRunSync())
      .mkString("")
    assertEquals(
      RichardPHJBensonBot
        .commandRepliesData[IO](
          backgroundJobManager = emptyBackgroundJobManager,
          dbLayer = emptyDBLayer
        )
        .length,
      10
    )
    assertEquals(
      triggerlist,
      "Puoi trovare la lista dei trigger al seguente URL: https://github.com/benkio/sBots/blob/master/richardPHJBensonBot/rphjb_triggers.txt"
    )
  }

  test("instructions command should return the expected message") {
    val actual = RichardPHJBensonBot
      .commandRepliesData[IO](
        backgroundJobManager = emptyBackgroundJobManager,
        dbLayer = emptyDBLayer
      )
      .filter(_.trigger.command == "instructions")
      .flatTraverse(_.text.get.text(privateTestMessage))
    assertIO(
      actual,
      List(
        s"""
---- Instruzioni Per RichardPHJBensonBot ----

Per segnalare problemi, scrivere a: https://t.me/Benkio

I comandi del bot sono:

- '/triggerlist': Restituisce un link ad un file contenente tutti i trigger a cui il bot risponderà automaticamente. Alcuni di questi sono in formato Regex
- '/triggersearch 《testo》': Consente di cercare se una parola o frase fa parte di un trigger
- '/searchshow 《testo》': Restituisce un link di uno show/video riguardante il personaggio del bot e contenente il testo specificato.
Input come query string:
  - No input: restituisce uno show random
  - 'title=keyword: restituisce uno show contenente la keyword nel titolo. Campo obbligatorio. Il campo può essere specificato più volte, si cercherà uno show contenente tutte le keywords. Esempio: 'title=Paul+Gilbert&title=dissacrazione'
  - 'description=keyword: restituisce uno show contenente la keyword nella descrizione. Campo opzionale. Il campo può essere specificato più volte, si cercherà uno show contenente tutte le keywords.  Esempio: 'description=Cris+Impellitteri&description=ramarro'
  - 'minduration=X': restituisce uno show di durata minima pari a X secondi. Campo opzionale. Esempio: 'minduration=300'
  - 'maxduration=X': restituisce uno show di durata massima pari a X secondi. Campo opzionale. Esempio: 'maxduration=1000'
  - 'mindate=YYYYMMDD': restituisce uno show più recente della data specificata. Campo opzionale. Esempio: 'mindate=20200101'
  - 'maxdate=YYYYMMDD': restituisce uno show più vecchio della data specificata. Campo opzionale. Esempio: 'mandate=20220101'
  I campi possono essere concatenati. Esempio: 'title=Cocktail+Micidiale&description=steve+vai&minduration=300'
- '/topTwentyTriggers': Restituisce una lista di file e il loro numero totale in invii
- '/subscribe 《cron time》': Iscrizione all'invio randomico di una puntata alla frequenza specificato nella chat corrente. Per il formato dell'input utilizzare questo codice come riferimento: https://scastie.scala-lang.org/hwpZ3fvcQ7q4xlfjoTjTvw. Attenzione, la libreria usata richiede anche i secondi come riportato nella documentazione: https://www.alonsodomin.me/cron4s/userguide/index.html
- '/unsubscribe': Disiscrizione della chat corrente dall'invio di puntate. Disiscriviti da una sola iscrizione inviando l'UUID relativo o da tutte le sottoscrizioni per la chat corrente se non viene inviato nessun input
- '/subscriptions': Restituisce la lista delle iscrizioni correnti per la chat corrente
- '/timeout 《intervallo》': Consente di impostare un limite di tempo tra una risposta e l'altra nella specifica chat. Formato dell'input: 00:00:00
- '/bensonify 《testo》': Traduce il testo in input nello stesso modo in cui benson lo scriverebbe. Il testo è obbligatorio

Se si vuole disabilitare il bot per un particolare messaggio impedendo
che interagisca, è possibile farlo iniziando il messaggio con il
carattere: `!`

! Messaggio
""",
        s"""
---- Instructions for RichardPHJBensonBot ----

to report issues, write to: https://t.me/Benkio

Bot commands are:

- '/triggerlist': Return a link to a file containing all the triggers used by the bot. Bot will reply automatically to these ones. Some of them are Regex
- '/triggersearch 《text》': Allow you to search if a specific word or phrase is part of a trigger
- '/searchshow 《text》': Return a link of a show/video about the specific bot's character and containing the specified keyword.
Input as query string:
  - No input: returns a random show
  - 'title=keyword: returns a show with the keyword in the title. Mandatory. The field can be specified multiple times, the show will contain all the keywords. Example: 'title=Paul+Gilbert&title=dissacrazione'
  - 'description=keyword: returns a show with the keyword in the description. Optional. The field can be specified multiple times, the show will contain all the keywords.  Example: 'description=Cris+Impellitteri&description=ramarro'
  - 'minduration=X': returns a show with minimal duration of X seconds. Optional. Example: 'minduration=300'
  - 'maxduration=X': returns a show with maximal duration of X seconds. Optional. Example: 'maxduration=1000'
  - 'mindate=YYYYMMDD': returns a show newer than the specified date. Optional. Example: 'mindate=20200101'
  - 'maxdate=YYYYMMDD': returns a show older than the specified date. Optional. Example: 'mandate=20220101'
  Fields can be concatenated. Example: 'title=Cocktail+Micidiale&description=steve+vai&minduration=300'
- '/topTwentyTriggers': Return a list of files and theirs send frequency
- '/subscribe 《cron time》': Subscribe to a random show at the specified frequency in the current chat. For the input format check the following code snippet: https://scastie.scala-lang.org/hwpZ3fvcQ7q4xlfjoTjTvw. You can find the docs here: https://www.alonsodomin.me/cron4s/userguide/index.html
- '/unsubscribe': Unsubscribe the current chat from random shows. With a UUID as input, the specific subscription will be deleted. With no input, all the subscriptions for the current chat will be deleted
- '/subscriptions': Return the amout of subscriptions for the current chat
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
        botFile.foreach(filename =>
          assert(files.contains(filename), s"$filename is not contained in richard data file")
        )
    )

  }

  test("the `rphjb_triggers.txt` should contain all the triggers of the bot") {
    val listPath       = new File(".").getCanonicalPath + "/rphjb_triggers.txt"
    val triggerContent = Source.fromFile(listPath).getLines().mkString("\n")

    val botMediaFiles = RichardPHJBensonBot.messageRepliesData[IO].flatMap(_.mediafiles.map(_.show))
    val botTriggersFiles =
      RichardPHJBensonBot.messageRepliesData[IO].flatMap(mrd => Show[Trigger].show(mrd.trigger).split('\n'))

    botMediaFiles.foreach { mediaFileString =>
      val result = triggerContent.contains(mediaFileString)
      if (!result) {
        println(s"Mediafile missing: $mediaFileString")
      }
      assert(result)
    }
    botTriggersFiles.foreach { triggerString =>
      assert(triggerContent.contains(triggerString), s"$triggerString is not contained in richard trigger file")
    }
  }
}
