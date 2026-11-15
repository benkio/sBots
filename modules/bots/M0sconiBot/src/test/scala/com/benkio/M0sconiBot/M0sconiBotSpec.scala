package com.benkio.M0sconi

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
import com.benkio.M0sconiBot.M0sconiBot

class M0sconiBotSpec extends BaseBotSpec {

  val mosSBotConfig             = SBotConfig.fromSBotInfo(M0sconiBot.sBotInfo)
  val emptyDBLayer: DBLayer[IO] = DBLayerMock.mock(mosSBotConfig.sBotInfo.botId)

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
    instructionsUri = "https://github.com/benkio/sBots/blob/main/modules/bots/M0sconiBot/mos_instructions.md"
  )
}
