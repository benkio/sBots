package com.benkio.M0sconi

import com.benkio.telegrambotinfrastructure.BaseBotSpec
import telegramium.bots.high.Api
import cats.effect.Async
import com.benkio.telegrambotinfrastructure.resources.ResourceAccess
import com.benkio.telegrambotinfrastructure.model.ReplyValue
import com.benkio.telegrambotinfrastructure.telegram.TelegramReply

import cats.Show
import cats.effect.IO
import cats.implicits.*
import com.benkio.m0sconibot.M0sconiBot
import com.benkio.telegrambotinfrastructure.mocks.DBLayerMock

import com.benkio.telegrambotinfrastructure.model.Trigger
import log.effect.fs2.SyncLogWriter.consoleLogUpToLevel
import log.effect.LogLevels
import log.effect.LogWriter
import munit.CatsEffectSuite

import telegramium.bots.Message

import com.benkio.telegrambotinfrastructure.resources.db.DBLayer

class M0sconiBotSpec extends BaseBotSpec {

  given log: LogWriter[IO]      = consoleLogUpToLevel(LogLevels.Info)
  val emptyDBLayer: DBLayer[IO] = DBLayerMock.mock(M0sconiBot.botName)
  given telegramReplyValue: TelegramReply[ReplyValue] = new TelegramReply[ReplyValue] {
    def reply[F[_]: Async: LogWriter: Api](
        reply: ReplyValue,
        msg: Message,
        resourceAccess: ResourceAccess[F],
        replyToMessage: Boolean
    ): F[List[Message]] = Async[F].pure(List.empty[Message])
  }

  triggerlistCommandTest(
    commandRepliesData = M0sconiBot
      .commandRepliesData[IO](
        dbLayer = emptyDBLayer
      ),
    expectedReply =
      "Puoi trovare la lista dei trigger al seguente URL: https://github.com/benkio/sBots/blob/master/m0sconiBot/mos_triggers.txt"
  )

  test("M0sconiBot should contain the expected number of commands") {
    assertEquals(
      M0sconiBot
        .commandRepliesData[IO](
          dbLayer = emptyDBLayer
        )
        .length,
      5
    )
  }

  jsonContainsFilenames(
    jsonFilename = "mos_list.json",
    botData = M0sconiBot.messageRepliesData[IO].flatTraverse(_.reply.prettyPrint).unsafeRunSync()
  )

  triggerFileContainsTriggers(
    triggerFilename = "mos_triggers.txt",
    botMediaFiles = M0sconiBot.messageRepliesData[IO].flatTraverse(_.reply.prettyPrint).unsafeRunSync(),
    botTriggers = M0sconiBot.messageRepliesData[IO].flatMap(mrd => Show[Trigger].show(mrd.trigger).split('\n'))
  )

  instructionsCommandTest(
    commandRepliesData = M0sconiBot
      .commandRepliesData[IO](
        dbLayer = emptyDBLayer
      ),
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
}
