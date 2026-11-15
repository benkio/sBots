package com.benkio.ABarberoBot

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

class ABarberoBotSpec extends BaseBotSpec {

  val abarSBotConfig            = SBotConfig.fromSBotInfo(ABarberoBot.sBotInfo)
  val emptyDBLayer: DBLayer[IO] = DBLayerMock.mock(abarSBotConfig.sBotInfo.botId)

  val aBarberoBot: IO[BotEngine[IO]] = buildTestEngine(dbLayer = emptyDBLayer, sBotConfig = abarSBotConfig)

  val messageRepliesData: IO[List[ReplyBundleMessage]] =
    aBarberoBot.map(_.messageRepliesData)
  val commandRepliesData: IO[List[ReplyBundleCommand]] =
    aBarberoBot.map(_.allCommandRepliesData)
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
      "Puoi trovare la lista dei trigger al seguente URL: https://github.com/benkio/sBots/blob/main/modules/bots/ABarberoBot/abar_triggers.md"
  )

  test("ABarberoBot should contain the expected number of commands") {
    assertIO(
      commandRepliesData.map(_.length),
      13,
      "ABarberoBot should have 11 commands"
    )
  }

  jsonContainsFilenames(
    jsonFilename = "abar_list.json",
    botData = messageRepliesDataPrettyPrint
  )
  botJsonsAreValid(abarSBotConfig)

  triggerFileContainsTriggers(
    triggerFilename = abarSBotConfig.triggerFilename,
    botMediaFiles = messageRepliesDataPrettyPrint,
    botTriggersIO = messageRepliesData.map(_.flatMap(mrd => Show[Trigger].show(mrd.trigger).split('\n')))
  )

  instructionsCommandTest(
    commandRepliesDataF = commandRepliesData,
    instructionsUri = "https://github.com/benkio/sBots/blob/main/modules/bots/ABarberoBot/abar_instructions.md"
  )
}
