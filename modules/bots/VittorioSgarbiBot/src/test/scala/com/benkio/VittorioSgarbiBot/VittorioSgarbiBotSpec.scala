package com.benkio.VittorioSgarbiBot

import cats.effect.IO
import cats.Show
import com.benkio.chatcore.config.SBotConfig
import com.benkio.chatcore.mocks.DBLayerMock
import com.benkio.chatcore.model.reply.ReplyBundleCommand
import com.benkio.chatcore.model.reply.ReplyBundleMessage
import com.benkio.chatcore.model.Trigger
import com.benkio.chatcore.repository.db.DBLayer
import com.benkio.chatcore.BaseBotSpec
import com.benkio.chatcore.BotEngine
import com.benkio.chatcore.Logger.given

class VittorioSgarbiBotSpec extends BaseBotSpec {

  val sgarSBotConfig: SBotConfig = SBotConfig.fromSBotInfo(VittorioSgarbiBot.sBotInfo)
  val emptyDBLayer: DBLayer[IO]  = DBLayerMock.mock(sgarSBotConfig.sBotInfo.botId)

  val vittorioSgarbiBot: IO[BotEngine[IO]] = buildTestEngine(dbLayer = emptyDBLayer, sBotConfig = sgarSBotConfig)

  val messageRepliesData: IO[List[ReplyBundleMessage]] =
    vittorioSgarbiBot.map(_.messageRepliesData)
  val commandRepliesData: IO[List[ReplyBundleCommand]] =
    vittorioSgarbiBot.map(_.allCommandRepliesData)
  val messageRepliesDataPrettyPrint: IO[List[String]] =
    messageRepliesData.map(_.flatMap(mr => mr.reply.prettyPrint))

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
      "Puoi trovare la lista dei trigger al seguente URL: https://github.com/benkio/sBots/blob/main/modules/bots/VittorioSgarbiBot/sgar_triggers.md"
  )

  test("VittorioSgarbiBot should return the expected number of commands") {
    assertIO(commandRepliesData.map(_.length), 13)
  }

  botJsonsAreValid(sgarSBotConfig)
  jsonContainsFilenames(
    jsonFilename = "sgar_list.json",
    botData = messageRepliesDataPrettyPrint
  )

  triggerFileContainsTriggers(
    triggerFilename = sgarSBotConfig.triggerFilename,
    botMediaFiles = messageRepliesDataPrettyPrint,
    botTriggersIO = messageRepliesData.map(_.flatMap(mrd => Show[Trigger].show(mrd.trigger).split('\n')))
  )

  instructionsCommandTest(
    commandRepliesDataF = commandRepliesData,
    """
      |---- Instruzioni Per VittorioSgarbiBot ----
      |
      |Per segnalare problemi, scrivere a: https://t.me/Benkio
      |
      |I comandi del bot sono:
      |
      |- '/searchshow': Documentazione: https://github.com/benkio/sBots/blob/main/modules/chatCore/CommandsDocumentation/searchshow.md#it
      |- '/subscribe': Documentazione: https://github.com/benkio/sBots/blob/main/modules/chatCore/CommandsDocumentation/subscribe.md#it
      |- '/unsubscribe': Documentazione: https://github.com/benkio/sBots/blob/main/modules/chatCore/CommandsDocumentation/unsubscribe.md#it
      |- '/subscriptions': Documentazione: https://github.com/benkio/sBots/blob/main/modules/chatCore/CommandsDocumentation/subscriptions.md#it
      |- '/loadcalendar': Documentazione: https://github.com/benkio/sBots/blob/main/modules/chatCore/CommandsDocumentation/loadcalendar.md#it
      |- '/showcalendar': Documentazione: https://github.com/benkio/sBots/blob/main/modules/chatCore/CommandsDocumentation/showcalendar.md#it
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
      |---- Instructions for VittorioSgarbiBot ----
      |
      |to report issues, write to: https://t.me/Benkio
      |
      |Bot commands are:
      |
      |- '/searchshow': Documentation: https://github.com/benkio/sBots/blob/main/modules/chatCore/CommandsDocumentation/searchshow.md#en
      |- '/subscribe': Documentation: https://github.com/benkio/sBots/blob/main/modules/chatCore/CommandsDocumentation/subscribe.md#en
      |- '/unsubscribe': Documentation: https://github.com/benkio/sBots/blob/main/modules/chatCore/CommandsDocumentation/unsubscribe.md#en
      |- '/subscriptions': Documentation: https://github.com/benkio/sBots/blob/main/modules/chatCore/CommandsDocumentation/subscriptions.md#en
      |- '/loadcalendar': Documentation: https://github.com/benkio/sBots/blob/main/modules/chatCore/CommandsDocumentation/loadcalendar.md#en
      |- '/showcalendar': Documentation: https://github.com/benkio/sBots/blob/main/modules/chatCore/CommandsDocumentation/showcalendar.md#en
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
