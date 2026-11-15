package com.benkio.YouTuboAncheI0Bot

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

class YouTuboAncheI0BotSpec extends BaseBotSpec {

  val ytaiSBotConfig            = SBotConfig.fromSBotInfo(YouTuboAncheI0Bot.sBotInfo)
  val emptyDBLayer: DBLayer[IO] = DBLayerMock.mock(ytaiSBotConfig.sBotInfo.botId)

  val youTuboAncheI0Bot: IO[BotEngine[IO]] = buildTestEngine(dbLayer = emptyDBLayer, sBotConfig = ytaiSBotConfig)

  val messageRepliesData: IO[List[ReplyBundleMessage]] =
    youTuboAncheI0Bot.map(_.messageRepliesData)
  val commandRepliesData: IO[List[ReplyBundleCommand]] =
    youTuboAncheI0Bot.map(_.allCommandRepliesData)
  val messageRepliesDataPrettyPrint: IO[List[String]] = messageRepliesData.map(_.flatMap(_.reply.prettyPrint))

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
      "Puoi trovare la lista dei trigger al seguente URL: https://github.com/benkio/sBots/blob/main/modules/bots/YouTuboAncheI0Bot/ytai_triggers.md"
  )

  test("YoutuboAncheI0Bot should return the expected number of commands") {
    assertIO(commandRepliesData.map(_.length), 13)
  }

  botJsonsAreValid(ytaiSBotConfig)
  jsonContainsFilenames(
    jsonFilename = "ytai_list.json",
    botData = messageRepliesDataPrettyPrint
  )

  triggerFileContainsTriggers(
    triggerFilename = ytaiSBotConfig.triggerFilename,
    botMediaFiles = messageRepliesDataPrettyPrint,
    botTriggersIO = messageRepliesData.map(_.flatMap(mrd => Show[Trigger].show(mrd.trigger).split('\n')))
  )

  instructionsCommandTest(
    commandRepliesDataF = commandRepliesData,
    instructionsUri = "https://github.com/benkio/sBots/blob/main/modules/bots/YouTuboAncheI0Bot/ytai_instructions.md"
  )
}
