package com.benkio.PinoScottoBot

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

class PinoScottoBotSpec extends BaseBotSpec {

  val pinoSBotConfig: SBotConfig = SBotConfig.fromSBotInfo(PinoScottoBot.sBotInfo)
  val emptyDBLayer: DBLayer[IO]  = DBLayerMock.mock(pinoSBotConfig.sBotInfo.botId)

  val templateBot: IO[BotEngine[IO]] = buildTestEngine(dbLayer = emptyDBLayer, sBotConfig = pinoSBotConfig)

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
      inputFileShouldRespondAsExpected(mrd)
    })
    .unsafeRunSync()

  test("PinoScottoBot should contain the expected number of commands") {
    assertIO(
      commandRepliesData.map(_.length),
      13,
      "PinoScottoBot should have 13 commands"
    )
  }

  jsonContainsFilenames(
    jsonFilename = "pino_list.json",
    botData = messageRepliesDataPrettyPrint
  )
  botJsonsAreValid(pinoSBotConfig)

  triggerFileContainsTriggers(
    triggerFilename = pinoSBotConfig.triggerFilename,
    botMediaFiles = messageRepliesDataPrettyPrint,
    botTriggersIO = messageRepliesData.map(_.flatMap(mrd => Show[Trigger].show(mrd.trigger).split('\n')))
  )

  instructionsCommandTest(
    commandRepliesDataF = commandRepliesData,
    instructionsUri = "https://github.com/benkio/sBots/blob/main/modules/bots/PinoScottoBot/pino_instructions.md"
  )
}
