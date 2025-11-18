package com.benkio.xahleebot

import cats.data.NonEmptyList
import cats.effect.Async
import cats.effect.IO
import cats.implicits.*
import com.benkio.telegrambotinfrastructure.mocks.ApiMock.given
import com.benkio.telegrambotinfrastructure.mocks.DBLayerMock
import com.benkio.telegrambotinfrastructure.mocks.RepositoryMock
import com.benkio.telegrambotinfrastructure.model.media.MediaResource.MediaResourceIFile
import com.benkio.telegrambotinfrastructure.model.reply.Reply
import com.benkio.telegrambotinfrastructure.model.reply.ReplyBundleCommand
import com.benkio.telegrambotinfrastructure.model.reply.ReplyValue
import com.benkio.telegrambotinfrastructure.repository.db.DBLayer
import com.benkio.telegrambotinfrastructure.repository.Repository
import com.benkio.telegrambotinfrastructure.telegram.TelegramReply
import com.benkio.telegrambotinfrastructure.BackgroundJobManager
import com.benkio.telegrambotinfrastructure.BaseBotSpec
import log.effect.fs2.SyncLogWriter.consoleLogUpToLevel
import log.effect.LogLevels
import log.effect.LogWriter
import telegramium.bots.high.Api
import telegramium.bots.Message

class XahLeeBotSpec extends BaseBotSpec {

  given log: LogWriter[IO]                            = consoleLogUpToLevel(LogLevels.Info)
  given telegramReplyValue: TelegramReply[ReplyValue] = new TelegramReply[ReplyValue] {
    override def reply[F[_]: Async: LogWriter: Api](
        reply: ReplyValue,
        msg: Message,
        repository: Repository[F],
        replyToMessage: Boolean
    ): F[List[Message]] = {
      val _ = summon[LogWriter[F]]
      val _ = summon[Api[F]]
      Async[F].pure(List.empty[Message])
    }
  }
  val botId = XahLeeBot.botId

  val emptyDBLayer: DBLayer[IO]             = DBLayerMock.mock(botId)
  val mediaResource: MediaResourceIFile[IO] =
    MediaResourceIFile(
      "test mediafile"
    )
  val repositoryMock = new RepositoryMock(getResourceByKindHandler =
    (_, inputBotId) =>
      IO.raiseUnless(inputBotId == botId)(
        Throwable(s"[M0sconiBotSpec] getResourceByKindHandler called with unexpected botId: $inputBotId")
      ).as(NonEmptyList.one(NonEmptyList.one(mediaResource)))
  )

  val xahLeeBot = BackgroundJobManager[IO](
    dbSubscription = emptyDBLayer.dbSubscription,
    dbShow = emptyDBLayer.dbShow,
    repository = repositoryMock,
    botId = botId
  ).map(bjm =>
    new XahLeeBotPolling[IO](
      repositoryInput = repositoryMock,
      dbLayer = emptyDBLayer,
      backgroundJobManager = bjm
    )
  )

  val commandRepliesData: IO[List[ReplyBundleCommand[IO]]] =
    xahLeeBot.flatMap(_.allCommandRepliesDataF)
  val messageRepliesDataPrettyPrint: IO[List[String]] =
    xahLeeBot
      .flatMap(xlb =>
        for {
          messageReplies <- xlb.messageRepliesDataF
          prettyPrints   <- messageReplies.flatTraverse(mr => mr.reply.prettyPrint)
        } yield prettyPrints
      )

  exactTriggerReturnExpectedReplyBundle(XahLeeBot.messageRepliesData[IO])
  regexTriggerLengthReturnValue(XahLeeBot.messageRepliesData[IO])

  jsonContainsFilenames(
    jsonFilename = "xah_list.json",
    botData = messageRepliesDataPrettyPrint
  )

  instructionsCommandTest(
    commandRepliesData = commandRepliesData,
    italianInstructions =
      """
        |---- Instruzioni Per XahLeeBot ----
        |
        |Per segnalare problemi, scrivere a: https://t.me/Benkio
        |
        |I comandi del bot sono:
        |
        |- '/searchshow 《testo》': Restituisce un link di uno show/video riguardante il personaggio del bot e contenente il testo specificato.
        |Input come query string:
        |  - No input: restituisce uno show random
        |  - 'title=keyword: restituisce uno show contenente la keyword nel titolo. Il campo può essere specificato più volte, si cercherà uno show contenente tutte le keywords. Esempio: 'title=Paul+Gilbert&title=dissacrazione'
        |  - 'description=keyword: restituisce uno show contenente la keyword nella descrizione. Il campo può essere specificato più volte, si cercherà uno show contenente tutte le keywords.  Esempio: 'description=Cris+Impellitteri&description=ramarro'
        |  - 'caption=keyword: restituisce uno show contenente la keyword nella caption automatica. Il campo può essere specificato più volte, si cercherà uno show contenente tutte le keywords.  Esempio: 'caption=Cris+Impellitteri&caption=ramarro'
        |  - 'minduration=X': restituisce uno show di durata minima pari a X secondi. Esempio: 'minduration=300'
        |  - 'maxduration=X': restituisce uno show di durata massima pari a X secondi. Esempio: 'maxduration=1000'
        |  - 'mindate=YYYYMMDD': restituisce uno show più recente della data specificata. Esempio: 'mindate=20200101'
        |  - 'maxdate=YYYYMMDD': restituisce uno show più vecchio della data specificata. Esempio: 'mandate=20220101'
        |  In caso di input non riconosciuto, verrà considerato come titolo.
        |  I campi possono essere concatenati. Esempio: 'title=Cocktail+Micidiale&description=steve+vai&minduration=300'
        |- '/subscribe 《cron time》': Iscrizione all'invio randomico di una puntata alla frequenza specificato nella chat corrente. Per il formato dell'input utilizzare questo codice come riferimento: https://scastie.scala-lang.org/ir5llpyPS5SmzU0zd46uLA oppure questo sito: https://www.freeformatter.com/cron-expression-generator-quartz.html#cronexpressionexamples Attenzione, la libreria usata richiede anche i secondi come riportato nella documentazione: https://www.alonsodomin.me/cron4s/userguide/index.html
        |- '/unsubscribe': Disiscrizione della chat corrente dall'invio di puntate. Disiscriviti da una sola iscrizione inviando l'UUID relativo o da tutte le sottoscrizioni per la chat corrente se non viene inviato nessun input
        |- '/subscriptions': Restituisce la lista delle iscrizioni correnti per la chat corrente
        |- '/random': Restituisce un dato(audio/video/testo/foto) casuale riguardante il personaggio del bot
        |- '/alanmackenzie': Restituisce un media file correlato a Alan Mackenzie"
        |- '/ass': Restituisce un media file correlato alla parola "ass"
        |- '/ccpp': Restituisce un media file correlato al C e C++
        |- '/crap': Restituisce un media file correlato alla parola "crap"
        |- '/emacs': Restituisce un media file correlato a Emacs
        |- '/extra': Restituisce un media file extra
        |- '/fak': Restituisce un media file correlato alla parola "fak"
        |- '/fakhead': Restituisce un media file correlato alla parola "fakhead"
        |- '/google': Restituisce un media file correlato a Google
        |- '/idiocy': Restituisce un media file correlato alla parola "idiocy"
        |- '/idiots': Restituisce un media file correlato alla parola "idiots"
        |- '/laugh': Restituisce un media file correlato alla risata di Xah Lee
        |- '/linux': Restituisce un media file correlato a Linux
        |- '/millennial': Restituisce un media file correlato ai millennials
        |- '/opensource': Restituisce un media file correlato all'open source
        |- '/opera': Restituisce un media file correlato a Opera
        |- '/python': Restituisce un media file correlato a Python
        |- '/rantcompilation': Restituisce una delle compilation di rant
        |- '/richardstallman': Restituisce un media file correlato a Richard Stallman
        |- '/sucks': Restituisce un media file correlato alla parola "sucks"
        |- '/unix': Restituisce un media file correlato a Unix
        |- '/wtf': Restituisce un media file correlato all'espressione "what the fak"
        |- '/zoomer': Restituisce un media file correlato ai zoomers
        |
        |Se si vuole disabilitare il bot per un particolare messaggio impedendo
        |che interagisca, è possibile farlo iniziando il messaggio con il
        |carattere: `!`
        |
        |! Messaggio
        |""".stripMargin,
    englishInstructions =
      """
        |---- Instructions for XahLeeBot ----
        |
        |to report issues, write to: https://t.me/Benkio
        |
        |Bot commands are:
        |
        |- '/searchshow 《text》': Return a link of a show/video about the specific bot's character and containing the specified keyword.
        |Input as query string:
        |  - No input: returns a random show
        |  - 'title=keyword: returns a show with the keyword in the title. The field can be specified multiple times, the show will contain all the keywords. Example: 'title=Paul+Gilbert&title=dissacrazione'
        |  - 'description=keyword: returns a show with the keyword in the description. The field can be specified multiple times, the show will contain all the keywords.  Example: 'description=Cris+Impellitteri&description=ramarro'
        |  - 'caption=keyword: returns a show with the keyword in the caption. The field can be specified multiple times, the show will contain all the keywords.  Example: 'caption=Cris+Impellitteri&caption=ramarro'
        |  - 'minduration=X': returns a show with minimal duration of X seconds.  Example: 'minduration=300'
        |  - 'maxduration=X': returns a show with maximal duration of X seconds.  Example: 'maxduration=1000'
        |  - 'mindate=YYYYMMDD': returns a show newer than the specified date.  Example: 'mindate=20200101'
        |  - 'maxdate=YYYYMMDD': returns a show older than the specified date.  Example: 'mandate=20220101'
        |  If the input is not recognized it will be considered as a title.
        |  Fields can be concatenated. Example: 'title=Cocktail+Micidiale&description=steve+vai&minduration=300'
        |- '/subscribe 《cron time》': Subscribe to a random show at the specified frequency in the current chat. For the input format check the following code snippet: https://scastie.scala-lang.org/ir5llpyPS5SmzU0zd46uLA oppure questo sito: https://www.freeformatter.com/cron-expression-generator-quartz.html#cronexpressionexamples You can find the docs here: https://www.alonsodomin.me/cron4s/userguide/index.html
        |- '/unsubscribe': Unsubscribe the current chat from random shows. With a UUID as input, the specific subscription will be deleted. With no input, all the subscriptions for the current chat will be deleted
        |- '/subscriptions': Return the amout of subscriptions for the current chat
        |- '/random': Returns a random data (photo/video/audio/text) about the bot character
        |- '/alanmackenzie': Returns a media file related to Alan Mackenzie
        |- '/ass': Returns a media file related to the word "ass"
        |- '/ccpp': Returns a media file related to the C and C++
        |- '/crap': Returns a media file related to the word "crap"
        |- '/emacs': Returns a media file related to Emacs
        |- '/extra': Returns an extra media file
        |- '/fak': Returns a media file related to the word "fak"
        |- '/fakhead': Returns a media file related to the word "fakhead"
        |- '/google': Returns a media file related to Google
        |- '/idiocy': Returns a media file related to the word "idiocy"
        |- '/idiots': Returns a media file related to the word "idiots"
        |- '/laugh': Returns a Xah Lee's laugh
        |- '/linux': Returns a media file related to Linux
        |- '/millennial': Returns a media file related to the millennials
        |- '/opensource': Returns a media file related to open source
        |- '/opera': Returns a media file related to Opera
        |- '/python': Returns a media file related to Python
        |- '/rantcompilation': Returns a Xah Lee's rant compilation
        |- '/richardstallman': Returns a media file related to Richard Stallman
        |- '/sucks': Returns a media file related to the word "sucks"
        |- '/unix': Returns a media file related to Unix
        |- '/wtf': Returns a media file related to the expression "what the fak"
        |- '/zoomer': Returns a media file related to zoomers
        |
        |if you wish to disable the bot for a specific message, blocking its reply/interaction, you can do adding the following character as prefix
        |character: `!`
        |
        |! Message
        |""".stripMargin
  )

}
