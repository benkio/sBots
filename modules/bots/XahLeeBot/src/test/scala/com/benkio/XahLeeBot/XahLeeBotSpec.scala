package com.benkio.XahLeeBot

import cats.effect.IO
import com.benkio.chatcore.config.SBotConfig
import com.benkio.chatcore.mocks.DBLayerMock
import com.benkio.chatcore.model.reply.ReplyBundleCommand
import com.benkio.chatcore.repository.db.DBLayer
import com.benkio.chatcore.BaseBotSpec
import com.benkio.chatcore.BotEngine
import com.benkio.chatcore.Logger.given

class XahLeeBotSpec extends BaseBotSpec {

  val xahSBotConfig             = SBotConfig.fromSBotInfo(XahLeeBot.sBotInfo)
  val emptyDBLayer: DBLayer[IO] = DBLayerMock.mock(XahLeeBot.sBotInfo.botId)

  val xahLeeBot: IO[BotEngine[IO]] = buildTestEngine(dbLayer = emptyDBLayer, sBotConfig = xahSBotConfig)

  val commandRepliesData: IO[List[ReplyBundleCommand]] =
    xahLeeBot.map(_.allCommandRepliesData)
  val messageRepliesDataPrettyPrint: IO[List[String]] =
    xahLeeBot.map(_.messageRepliesData.flatMap(_.reply.prettyPrint))

  botJsonsAreValid(xahSBotConfig)
  jsonContainsFilenames(
    jsonFilename = "xah_list.json",
    botData = messageRepliesDataPrettyPrint
  )

  instructionsCommandTest(
    commandRepliesDataF = commandRepliesData,
    instructionsUri = "https://github.com/benkio/sBots/blob/main/modules/bots/XahLeeBot/xah_instructions.md"
  )

}
