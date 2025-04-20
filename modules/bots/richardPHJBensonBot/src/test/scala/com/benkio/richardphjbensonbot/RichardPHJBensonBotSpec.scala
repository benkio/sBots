package com.benkio.richardphjbensonbot

import cats.data.NonEmptyList
import cats.effect.Async
import cats.effect.IO
import cats.implicits.*
import cats.Show
import com.benkio.telegrambotinfrastructure.mocks.ApiMock.given
import com.benkio.telegrambotinfrastructure.mocks.DBLayerMock
import com.benkio.telegrambotinfrastructure.mocks.ResourceAccessMock
import com.benkio.telegrambotinfrastructure.model.media.MediaResource.MediaResourceIFile
import com.benkio.telegrambotinfrastructure.model.reply.Reply
import com.benkio.telegrambotinfrastructure.model.reply.ReplyBundleCommand
import com.benkio.telegrambotinfrastructure.model.reply.ReplyValue
import com.benkio.telegrambotinfrastructure.model.LeftMemberTrigger
import com.benkio.telegrambotinfrastructure.model.NewMemberTrigger
import com.benkio.telegrambotinfrastructure.model.Trigger
import com.benkio.telegrambotinfrastructure.resources.db.DBLayer
import com.benkio.telegrambotinfrastructure.resources.ResourceAccess
import com.benkio.telegrambotinfrastructure.telegram.TelegramReply
import com.benkio.telegrambotinfrastructure.BackgroundJobManager
import com.benkio.telegrambotinfrastructure.BaseBotSpec
import log.effect.fs2.SyncLogWriter.consoleLogUpToLevel
import log.effect.LogLevels
import log.effect.LogWriter
import munit.CatsEffectSuite
import telegramium.bots.high.Api
import telegramium.bots.Message

class RichardPHJBensonBotSpec extends BaseBotSpec {

  import com.benkio.richardphjbensonbot.data.Special.messageRepliesSpecialData

  given log: LogWriter[IO] = consoleLogUpToLevel(LogLevels.Info)
  val mediaResource: MediaResourceIFile[IO] =
    MediaResourceIFile(
      "test mediafile"
    )
  val resourceAccessMock = new ResourceAccessMock(_ => NonEmptyList.one(NonEmptyList.one(mediaResource)).pure[IO])
  given telegramReplyValue: TelegramReply[ReplyValue] = new TelegramReply[ReplyValue] {
    def reply[F[_]: Async: LogWriter: Api](
        reply: ReplyValue,
        msg: Message,
        resourceAccess: ResourceAccess[F],
        replyToMessage: Boolean
    ): F[List[Message]] = Async[F].pure(List.empty[Message])
  }
  val emptyDBLayer: DBLayer[IO] = DBLayerMock.mock(RichardPHJBensonBot.botName)
  val commandRepliesData: IO[List[ReplyBundleCommand[IO]]] = BackgroundJobManager[IO](
    dbSubscription = emptyDBLayer.dbSubscription,
    dbShow = emptyDBLayer.dbShow,
    resourceAccessMock,
    botName = "RichardPHJBensonBot"
  ).map(bjm =>
    RichardPHJBensonBot
      .commandRepliesData[IO](
        backgroundJobManager = bjm,
        dbLayer = emptyDBLayer
      )
  )
  val messageRepliesDataPrettyPrint: IO[List[String]] =
    RichardPHJBensonBot.messageRepliesData[IO].flatTraverse(_.reply.prettyPrint)

  exactTriggerReturnExpectedReplyBundle(RichardPHJBensonBot.messageRepliesData[IO])

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

  triggerlistCommandTest(
    commandRepliesData = commandRepliesData,
    expectedReply =
      "Puoi trovare la lista dei trigger al seguente URL: https://github.com/benkio/sBots/blob/master/modules/bots/richardPHJBensonBot/rphjb_triggers.txt"
  )

  test("RichardPHJBensonBot should contain the expected number of commands") {
    assertIO(commandRepliesData.map(_.length), 11)
  }

  jsonContainsFilenames(
    jsonFilename = "rphjb_list.json",
    botData = messageRepliesDataPrettyPrint
  )

  triggerFileContainsTriggers(
    triggerFilename = RichardPHJBensonBot.triggerFilename,
    botMediaFiles = messageRepliesDataPrettyPrint,
    botTriggers = RichardPHJBensonBot.messageRepliesData[IO].flatMap(mrd => Show[Trigger].show(mrd.trigger).split('\n'))
  )

  instructionsCommandTest(
    commandRepliesData = commandRepliesData,
    italianInstructions = """
---- Instruzioni Per RichardPHJBensonBot ----

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
- '/subscribe 《cron time》': Iscrizione all'invio randomico di una puntata alla frequenza specificato nella chat corrente. Per il formato dell'input utilizzare questo codice come riferimento: https://scastie.scala-lang.org/ir5llpyPS5SmzU0zd46uLA oppure questo sito: https://www.freeformatter.com/cron-expression-generator-quartz.html#cronexpressionexamples Attenzione, la libreria usata richiede anche i secondi come riportato nella documentazione: https://www.alonsodomin.me/cron4s/userguide/index.html
- '/unsubscribe': Disiscrizione della chat corrente dall'invio di puntate. Disiscriviti da una sola iscrizione inviando l'UUID relativo o da tutte le sottoscrizioni per la chat corrente se non viene inviato nessun input
- '/subscriptions': Restituisce la lista delle iscrizioni correnti per la chat corrente
- '/timeout 《intervallo》': Consente di impostare un limite di tempo tra una risposta e l'altra nella specifica chat. Formato dell'input: 00:00:00. Senza input il timeout verrà rimosso
- '/bensonify 《testo》': Traduce il testo in input nello stesso modo in cui benson lo scriverebbe. Il testo è obbligatorio
- '/random': Restituisce un dato(audio/video/testo/foto) casuale riguardante il personaggio del bot

Se si vuole disabilitare il bot per un particolare messaggio impedendo
che interagisca, è possibile farlo iniziando il messaggio con il
carattere: `!`

! Messaggio
""",
    englishInstructions = """
---- Instructions for RichardPHJBensonBot ----

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
- '/subscribe 《cron time》': Subscribe to a random show at the specified frequency in the current chat. For the input format check the following code snippet: https://scastie.scala-lang.org/ir5llpyPS5SmzU0zd46uLA oppure questo sito: https://www.freeformatter.com/cron-expression-generator-quartz.html#cronexpressionexamples You can find the docs here: https://www.alonsodomin.me/cron4s/userguide/index.html
- '/unsubscribe': Unsubscribe the current chat from random shows. With a UUID as input, the specific subscription will be deleted. With no input, all the subscriptions for the current chat will be deleted
- '/subscriptions': Return the amout of subscriptions for the current chat
- '/timeout 《time》': Allow you to set a timeout between bot's replies in the specific chat. input time format: 00:00:00. Without input the timeout will be removed
- '/bensonify 《text》': Translate the text in the same way benson would write it. Text input is mandatory
- '/random': Returns a data (photo/video/audio/text) random about the bot character

if you wish to disable the bot for a specific message, blocking its reply/interaction, you can do adding the following character as prefix
character: `!`

! Message
"""
  )

}
