package com.benkio.abarberobot

import telegramium.bots.client.Method
import telegramium.bots.high.Api
import com.benkio.telegrambotinfrastructure.resources.ResourceAccess
import com.benkio.telegrambotinfrastructure.mocks.ResourceAccessMock
import cats.effect.Async
import com.benkio.telegrambotinfrastructure.model.ReplyValue
import com.benkio.telegrambotinfrastructure.telegram.TelegramReply
import com.benkio.telegrambotinfrastructure.model.MediaFileSource
import cats.Show
import cats.effect.IO
import cats.implicits.*
import com.benkio.telegrambotinfrastructure.BackgroundJobManager
import com.benkio.telegrambotinfrastructure.mocks.DBLayerMock
import com.benkio.telegrambotinfrastructure.model.Reply
import com.benkio.telegrambotinfrastructure.model.Trigger
import log.effect.fs2.SyncLogWriter.consoleLogUpToLevel
import log.effect.LogLevels
import log.effect.LogWriter
import munit.CatsEffectSuite
import telegramium.bots.Message
import io.circe.parser.decode

import java.io.File
import scala.io.Source
import com.benkio.telegrambotinfrastructure.resources.db.DBLayer

class ABarberoBotSpec extends CatsEffectSuite {

  given log: LogWriter[IO]      = consoleLogUpToLevel(LogLevels.Info)
  val emptyDBLayer: DBLayer[IO] = DBLayerMock.mock(ABarberoBot.botName)
  val resourceAccessMock        = new ResourceAccessMock(List.empty)
  given telegramReplyValue: TelegramReply[ReplyValue] = new TelegramReply[ReplyValue] {
    def reply[F[_]: Async: LogWriter: Api](
        reply: ReplyValue,
        msg: Message,
        resourceAccess: ResourceAccess[F],
        replyToMessage: Boolean
    ): F[List[Message]] = Async[F].pure(List.empty[Message])
  }
  given api: Api[IO] = new Api[IO] {
    def execute[Res](method: Method[Res]): IO[Res] = IO(???)
  }

  val emptyBackgroundJobManager: BackgroundJobManager[IO] = BackgroundJobManager[IO](
    emptyDBLayer.dbSubscription,
    emptyDBLayer.dbShow,
    resourceAccessMock,
    "ABarberoBot"
  ).unsafeRunSync()

  test("triggerlist should return a list of all triggers when called") {
    val triggerlist: String = ABarberoBot
      .commandRepliesData[IO](
        backgroundJobManager = emptyBackgroundJobManager,
        dbLayer = emptyDBLayer
      )
      .filter(_.trigger.command == "triggerlist")
      .flatMap(_.reply.prettyPrint.unsafeRunSync())
      .mkString("\n")
    assertEquals(
      ABarberoBot
        .commandRepliesData[IO](
          backgroundJobManager = emptyBackgroundJobManager,
          dbLayer = emptyDBLayer
        )
        .length,
      9
    )
    assertEquals(
      triggerlist,
      "Puoi trovare la lista dei trigger al seguente URL: https://github.com/benkio/sBots/blob/master/aBarberoBot/abar_triggers.txt"
    )
  }

  test("the `abar_list.json` should contain all the triggers of the bot") {
    val listPath      = new File(".").getCanonicalPath + "/abar_list.json"
    val jsonContent   = Source.fromFile(listPath).getLines().mkString("\n")
    val jsonFilenames = decode[List[MediaFileSource]](jsonContent).map(_.map(_.filename))

    val botFile = ABarberoBot.messageRepliesData[IO].flatTraverse(_.reply.prettyPrint)

    assert(jsonFilenames.isRight)
    jsonFilenames.fold(
      e => fail("test failed", e),
      files =>
        botFile
          .unsafeRunSync()
          .foreach(filename => assert(files.contains(filename), s"$filename is not contained in barbero data file"))
        assert(
          Set(files*).size == files.length,
          s"there's a duplicate filename into the json ${files.diff(Set(files*).toList)}"
        )
    )

  }

  test("the `abar_triggers.txt` should contain all the triggers of the bot") {
    val listPath       = new File(".").getCanonicalPath + "/abar_triggers.txt"
    val triggerContent = Source.fromFile(listPath).getLines().mkString("\n")

    val botMediaFiles = ABarberoBot.messageRepliesData[IO].flatTraverse(_.reply.prettyPrint)
    val botTriggersFiles =
      ABarberoBot.messageRepliesData[IO].flatMap(mrd => Show[Trigger].show(mrd.trigger).split('\n'))

    botMediaFiles.unsafeRunSync().foreach { mediaFileString =>
      assert(triggerContent.contains(mediaFileString))
    }
    botTriggersFiles.foreach { triggerString =>
      assert(triggerContent.contains(triggerString), s"$triggerString is not contained in barbero trigger file")
    }
  }

  test("instructions command should return the expected message") {
    val actual = ABarberoBot
      .commandRepliesData[IO](
        backgroundJobManager = emptyBackgroundJobManager,
        dbLayer = emptyDBLayer
      )
      .filter(_.trigger.command == "instructions")
      .flatTraverse(_.reply.prettyPrint)
    assertIO(
      actual,
      List(
        s"""
---- Instruzioni Per ABarberoBot ----

Per segnalare problemi, scrivere a: https://t.me/Benkio

I comandi del bot sono:

- '/triggerlist': Restituisce un link ad un file contenente tutti i trigger a cui il bot risponderà automaticamente. Alcuni di questi sono in formato Regex
- '/triggersearch 《testo》': Consente di cercare se una parola o frase fa parte di un trigger
- '/searchshow 《testo》': Restituisce un link di uno show/video riguardante il personaggio del bot e contenente il testo specificato.
Input come query string:
  - No input: restituisce uno show random
  - 'title=keyword: restituisce uno show contenente la keyword nel titolo. Il campo può essere specificato più volte, si cercherà uno show contenente tutte le keywords. Esempio: 'title=Paul+Gilbert&title=dissacrazione'
  - 'description=keyword: restituisce uno show contenente la keyword nella descrizione. Il campo può essere specificato più volte, si cercherà uno show contenente tutte le keywords.  Esempio: 'description=Cris+Impellitteri&description=ramarro'
  - 'minduration=X': restituisce uno show di durata minima pari a X secondi. Esempio: 'minduration=300'
  - 'maxduration=X': restituisce uno show di durata massima pari a X secondi. Esempio: 'maxduration=1000'
  - 'mindate=YYYYMMDD': restituisce uno show più recente della data specificata. Esempio: 'mindate=20200101'
  - 'maxdate=YYYYMMDD': restituisce uno show più vecchio della data specificata. Esempio: 'mandate=20220101'
  In caso di input non riconosciuto, verrà considerato come titolo.
  I campi possono essere concatenati. Esempio: 'title=Cocktail+Micidiale&description=steve+vai&minduration=300'
- '/topTwentyTriggers': Restituisce una lista di file e il loro numero totale in invii
- '/subscribe 《cron time》': Iscrizione all'invio randomico di una puntata alla frequenza specificato nella chat corrente. Per il formato dell'input utilizzare questo codice come riferimento: https://scastie.scala-lang.org/hwpZ3fvcQ7q4xlfjoTjTvw. Attenzione, la libreria usata richiede anche i secondi come riportato nella documentazione: https://www.alonsodomin.me/cron4s/userguide/index.html
- '/unsubscribe': Disiscrizione della chat corrente dall'invio di puntate. Disiscriviti da una sola iscrizione inviando l'UUID relativo o da tutte le sottoscrizioni per la chat corrente se non viene inviato nessun input
- '/subscriptions': Restituisce la lista delle iscrizioni correnti per la chat corrente
- '/timeout 《intervallo》': Consente di impostare un limite di tempo tra una risposta e l'altra nella specifica chat. Formato dell'input: 00:00:00

Se si vuole disabilitare il bot per un particolare messaggio impedendo
che interagisca, è possibile farlo iniziando il messaggio con il
carattere: `!`

! Messaggio
""",
        s"""
---- Instructions for ABarberoBot ----

to report issues, write to: https://t.me/Benkio

Bot commands are:

- '/triggerlist': Return a link to a file containing all the triggers used by the bot. Bot will reply automatically to these ones. Some of them are Regex
- '/triggersearch 《text》': Allow you to search if a specific word or phrase is part of a trigger
- '/searchshow 《text》': Return a link of a show/video about the specific bot's character and containing the specified keyword.
Input as query string:
  - No input: returns a random show
  - 'title=keyword: returns a show with the keyword in the title. The field can be specified multiple times, the show will contain all the keywords. Example: 'title=Paul+Gilbert&title=dissacrazione'
  - 'description=keyword: returns a show with the keyword in the description. The field can be specified multiple times, the show will contain all the keywords.  Example: 'description=Cris+Impellitteri&description=ramarro'
  - 'minduration=X': returns a show with minimal duration of X seconds.  Example: 'minduration=300'
  - 'maxduration=X': returns a show with maximal duration of X seconds.  Example: 'maxduration=1000'
  - 'mindate=YYYYMMDD': returns a show newer than the specified date.  Example: 'mindate=20200101'
  - 'maxdate=YYYYMMDD': returns a show older than the specified date.  Example: 'mandate=20220101'
  If the input is not recognized it will be considered as a title.
  Fields can be concatenated. Example: 'title=Cocktail+Micidiale&description=steve+vai&minduration=300'
- '/topTwentyTriggers': Return a list of files and theirs send frequency
- '/subscribe 《cron time》': Subscribe to a random show at the specified frequency in the current chat. For the input format check the following code snippet: https://scastie.scala-lang.org/hwpZ3fvcQ7q4xlfjoTjTvw. You can find the docs here: https://www.alonsodomin.me/cron4s/userguide/index.html
- '/unsubscribe': Unsubscribe the current chat from random shows. With a UUID as input, the specific subscription will be deleted. With no input, all the subscriptions for the current chat will be deleted
- '/subscriptions': Return the amout of subscriptions for the current chat
- '/timeout 《time》': Allow you to set a timeout between bot's replies in the specific chat. input time format: 00:00:00

if you wish to disable the bot for a specific message, blocking its reply/interaction, you can do adding the following character as prefix
character: `!`

! Message
"""
      )
    )
  }
}
