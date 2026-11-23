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
    assertIO(commandRepliesData.map(_.length), 14)
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
    instructionsUri = "https://github.com/benkio/sBots/blob/main/modules/bots/VittorioSgarbiBot/sgar_instructions.md"
  )
}
