package com.benkio.M0sconi

import cats.data.NonEmptyList
import cats.effect.Async
import cats.effect.IO
import cats.implicits.*
import cats.Show
import com.benkio.m0sconibot.M0sconiBot
import com.benkio.m0sconibot.M0sconiBotPolling
import com.benkio.telegrambotinfrastructure.mocks.ApiMock.given
import com.benkio.telegrambotinfrastructure.mocks.DBLayerMock
import com.benkio.telegrambotinfrastructure.mocks.RepositoryMock
import com.benkio.telegrambotinfrastructure.model.media.MediaResource.MediaResourceIFile
import com.benkio.telegrambotinfrastructure.model.reply.ReplyBundleCommand
import com.benkio.telegrambotinfrastructure.model.reply.ReplyValue
import com.benkio.telegrambotinfrastructure.model.Trigger
import com.benkio.telegrambotinfrastructure.repository.db.DBLayer
import com.benkio.telegrambotinfrastructure.repository.Repository
import com.benkio.telegrambotinfrastructure.telegram.TelegramReply
import com.benkio.telegrambotinfrastructure.BackgroundJobManager
import com.benkio.telegrambotinfrastructure.BaseBotSpec
import log.effect.fs2.SyncLogWriter.consoleLogUpToLevel
import log.effect.LogLevels
import log.effect.LogWriter
import munit.CatsEffectSuite
import telegramium.bots.high.Api
import telegramium.bots.Message

class M0sconiBotSpec extends BaseBotSpec {

  given log: LogWriter[IO]                            = consoleLogUpToLevel(LogLevels.Info)
  given telegramReplyValue: TelegramReply[ReplyValue] = new TelegramReply[ReplyValue] {
    override def reply[F[_]: Async: LogWriter: Api](
        reply: ReplyValue,
        msg: Message,
        repository: Repository[F],
        replyToMessage: Boolean
    ): F[List[Message]] =
      val _ = summon[LogWriter[F]]
      val _ = summon[Api[F]]
      Async[F].pure(List.empty[Message])
  }
  val botId = M0sconiBot.botId

  val emptyDBLayer: DBLayer[IO]             = DBLayerMock.mock(botId)
  val mediaResource: MediaResourceIFile[IO] =
    MediaResourceIFile(
      "test mediafile"
    )
  val repositoryMock = new RepositoryMock(
    getResourceByKindHandler = (_, inputBotId) =>
      IO.raiseUnless(inputBotId == botId)(
        Throwable(s"[M0sconiBotSpec] getResourceByKindHandler called with unexpected botId: $inputBotId")
      ).as(NonEmptyList.one(NonEmptyList.one(mediaResource)))
  )

  val m0sconiBot =
    BackgroundJobManager[IO](
      dbSubscription = emptyDBLayer.dbSubscription,
      dbShow = emptyDBLayer.dbShow,
      repository = repositoryMock,
      botId = botId
    ).map(bjm =>
      new M0sconiBotPolling[IO](
        repositoryInput = repositoryMock,
        dbLayer = emptyDBLayer,
        backgroundJobManager = bjm
      )
    )
  val commandRepliesData: IO[List[ReplyBundleCommand[IO]]] =
    m0sconiBot.flatMap(_.allCommandRepliesDataF)
  val messageRepliesDataPrettyPrint: IO[List[String]] =
    m0sconiBot
      .flatMap(mb =>
        for
          messageReplies <- mb.messageRepliesDataF
          prettyPrints   <- messageReplies.flatTraverse(mr => mr.reply.prettyPrint)
        yield prettyPrints
      )

  exactTriggerReturnExpectedReplyBundle(M0sconiBot.messageRepliesData[IO])
  regexTriggerLengthReturnValue(M0sconiBot.messageRepliesData[IO])

  triggerlistCommandTest(
    commandRepliesData = commandRepliesData,
    expectedReply =
      "Puoi trovare la lista dei trigger al seguente URL: https://github.com/benkio/sBots/blob/main/modules/bots/m0sconiBot/mos_triggers.txt"
  )

  test("M0sconiBot should contain the expected number of commands") {
    assertIO(commandRepliesData.map(_.length), 6)
  }

  jsonContainsFilenames(
    jsonFilename = "mos_list.json",
    botData = messageRepliesDataPrettyPrint
  )

  triggerFileContainsTriggers(
    triggerFilename = M0sconiBot.triggerFilename,
    botMediaFiles = messageRepliesDataPrettyPrint,
    botTriggers = M0sconiBot.messageRepliesData[IO].flatMap(mrd => Show[Trigger].show(mrd.trigger).split('\n'))
  )

  instructionsCommandTest(
    commandRepliesData = commandRepliesData,
    """
      |---- Instruzioni Per M0sconiBot ----
      |
      |Per segnalare problemi, scrivere a: https://t.me/Benkio
      |
      |I comandi del bot sono:
      |
      |- '/triggerlist': Restituisce un link ad un file contenente tutti i trigger a cui il bot risponderà automaticamente. Alcuni di questi sono in formato Regex
      |- '/triggersearch 《testo》': Consente di cercare se una parola o frase fa parte di un trigger
      |- '/toptwenty': Restituisce una lista di file e il loro numero totale in invii
      |- '/timeout 《intervallo》': Consente di impostare un limite di tempo tra una risposta e l'altra nella specifica chat. Formato dell'input: 00:00:00. Senza input il timeout verrà rimosso
      |- '/random': Restituisce un dato(audio/video/testo/foto) casuale riguardante il personaggio del bot
      |
      |Se si vuole disabilitare il bot per un particolare messaggio impedendo
      |che interagisca, è possibile farlo iniziando il messaggio con il
      |carattere: `!`
      |
      |! Messaggio
      |""".stripMargin,
    """
      |---- Instructions for M0sconiBot ----
      |
      |to report issues, write to: https://t.me/Benkio
      |
      |Bot commands are:
      |
      |- '/triggerlist': Return a link to a file containing all the triggers used by the bot. Bot will reply automatically to these ones. Some of them are Regex
      |- '/triggersearch 《text》': Allow you to search if a specific word or phrase is part of a trigger
      |- '/toptwenty': Return a list of files and theirs send frequency
      |- '/timeout 《time》': Allow you to set a timeout between bot's replies in the specific chat. input time format: 00:00:00. Without input the timeout will be removed
      |- '/random': Returns a random data (photo/video/audio/text) about the bot character
      |
      |if you wish to disable the bot for a specific message, blocking its reply/interaction, you can do adding the following character as prefix
      |character: `!`
      |
      |! Message
      |""".stripMargin
  )
}
