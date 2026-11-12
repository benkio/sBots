package com.benkio.M0sconi

import cats.effect.IO
import cats.Show
import com.benkio.chatcore.mocks.DBLayerMock
import com.benkio.chatcore.model.reply.ReplyBundleCommand
import com.benkio.chatcore.model.reply.ReplyBundleMessage
import com.benkio.chatcore.model.Trigger
import com.benkio.chatcore.repository.db.DBLayer
import com.benkio.chatcore.BaseBotSpec
import com.benkio.chatcore.BotEngine
import com.benkio.chatcore.config.SBotConfig
import com.benkio.chatcore.Logger.given
import com.benkio.M0sconiBot.M0sconiBot

class M0sconiBotSpec extends BaseBotSpec {

  val mosSBotConfig                         = SBotConfig.fromSBotInfo(M0sconiBot.sBotInfo)
  val emptyDBLayer: DBLayer[IO]             = DBLayerMock.mock(mosSBotConfig.sBotInfo.botId)

  val m0sconiBot: IO[BotEngine[IO]] = buildTestEngine(dbLayer = emptyDBLayer, sBotConfig = mosSBotConfig)
  val commandRepliesData: IO[List[ReplyBundleCommand]] =
    m0sconiBot.map(_.allCommandRepliesData)
  val messageRepliesData: IO[List[ReplyBundleMessage]] =
    m0sconiBot.map(_.messageRepliesData)

  val messageRepliesDataPrettyPrint: IO[List[String]] =
    messageRepliesData.map(_.flatMap(_.reply.prettyPrint))

  messageRepliesData
    .map(mrd => {
      exactTriggerReturnExpectedReplyBundle(mrd)
      noOverlappingReplyBundles(mrd)
      inputFileShouldRespondAsExpected(mrd)
    })
    .unsafeRunSync()

  triggerlistCommandTest(
    commandRepliesData = commandRepliesData,
    expectedReply =
      "Puoi trovare la lista dei trigger al seguente URL: https://github.com/benkio/sBots/blob/main/modules/bots/M0sconiBot/mos_triggers.md"
  )

  test("M0sconiBot should contain the expected number of commands") {
    assertIO(commandRepliesData.map(_.length), 7)
  }

  botJsonsAreValid(mosSBotConfig)
  jsonContainsFilenames(
    jsonFilename = "mos_list.json",
    botData = messageRepliesDataPrettyPrint
  )

  triggerFileContainsTriggers(
    triggerFilename = mosSBotConfig.triggerFilename,
    botMediaFiles = messageRepliesData.map(_.flatMap(mr => mr.reply.prettyPrint)),
    botTriggersIO = messageRepliesData.map(_.flatMap(mrd => Show[Trigger].show(mrd.trigger).split('\n')))
  )

  instructionsCommandTest(
    commandRepliesDataF = commandRepliesData,
    """
      |---- Instruzioni Per M0sconiBot ----
      |
      |Per segnalare problemi, scrivere a: https://t.me/Benkio
      |
      |I comandi del bot sono:
      |
      |- '/random': Documentazione: https://github.com/benkio/sBots/blob/main/modules/chatCore/CommandsDocumentation/random.md#it
      |- '/triggerlist': Documentazione: https://github.com/benkio/sBots/blob/main/modules/chatCore/CommandsDocumentation/triggerlist.md#it
      |- '/triggersearch': Documentazione: https://github.com/benkio/sBots/blob/main/modules/chatCore/CommandsDocumentation/triggersearch.md#it
      |- '/toptwenty': Documentazione: https://github.com/benkio/sBots/blob/main/modules/chatCore/CommandsDocumentation/toptwenty.md#it
      |- '/settimeout': Documentazione: https://github.com/benkio/sBots/blob/main/modules/chatCore/CommandsDocumentation/settimeout.md#it
      |- '/gettimeout': Documentazione: https://github.com/benkio/sBots/blob/main/modules/chatCore/CommandsDocumentation/gettimeout.md#it
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
      |- '/random': Documentation: https://github.com/benkio/sBots/blob/main/modules/chatCore/CommandsDocumentation/random.md#en
      |- '/triggerlist': Documentation: https://github.com/benkio/sBots/blob/main/modules/chatCore/CommandsDocumentation/triggerlist.md#en
      |- '/triggersearch': Documentation: https://github.com/benkio/sBots/blob/main/modules/chatCore/CommandsDocumentation/triggersearch.md#en
      |- '/toptwenty': Documentation: https://github.com/benkio/sBots/blob/main/modules/chatCore/CommandsDocumentation/toptwenty.md#en
      |- '/settimeout': Documentation: https://github.com/benkio/sBots/blob/main/modules/chatCore/CommandsDocumentation/settimeout.md#en
      |- '/gettimeout': Documentation: https://github.com/benkio/sBots/blob/main/modules/chatCore/CommandsDocumentation/gettimeout.md#en
      |
      |if you wish to disable the bot for a specific message, blocking its reply/interaction, you can do adding the following character as prefix
      |character: `!`
      |
      |! Message
      |""".stripMargin
  )
}
