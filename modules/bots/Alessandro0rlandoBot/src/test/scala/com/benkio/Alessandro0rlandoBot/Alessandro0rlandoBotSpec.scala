package com.benkio.Alessandro0rlandoBot

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

class Alessandro0rlandoBotSpec extends BaseBotSpec {

  val orlSBotConfig: SBotConfig = SBotConfig.fromSBotInfo(Alessandro0rlandoBot.sBotInfo)
  val emptyDBLayer: DBLayer[IO] = DBLayerMock.mock(orlSBotConfig.sBotInfo.botId)

  val templateBot: IO[BotEngine[IO]] = buildTestEngine(dbLayer = emptyDBLayer, sBotConfig = orlSBotConfig)

  val messageRepliesData: IO[List[ReplyBundleMessage]] =
    templateBot.map(_.messageRepliesData)
  val commandRepliesData: IO[List[ReplyBundleCommand]] =
    templateBot.map(_.allCommandRepliesData)
  val messageRepliesDataPrettyPrint: IO[List[String]] =
    messageRepliesData.map(_.flatMap(mr => mr.reply.prettyPrint))

  messageRepliesData
    .map(mrd => {
      exactTriggerReturnExpectedReplyBundle(mrd)
      noOverlappingReplyBundles(mrd)
    })
    .unsafeRunSync()

  test("Alessandro0rlandoBot should contain the expected number of commands") {
    assertIO(
      commandRepliesData.map(_.length),
      14,
      "Alessandro0rlandoBot should have 14 commands"
    )
  }

  jsonContainsFilenames(
    jsonFilename = "orl_list.json",
    botData = messageRepliesDataPrettyPrint
  )
  botJsonsAreValid(orlSBotConfig)

  triggerFileContainsTriggers(
    triggerFilename = orlSBotConfig.triggerFilename,
    botMediaFiles = messageRepliesDataPrettyPrint,
    botTriggersIO = messageRepliesData.map(_.flatMap(mrd => Show[Trigger].show(mrd.trigger).split('\n')))
  )

  instructionsCommandTest(
    commandRepliesDataF = commandRepliesData,
    instructionsUri = "https://github.com/benkio/sBots/blob/main/modules/bots/Alessandro0rlandoBot/orl_instructions.md"
  )
}
