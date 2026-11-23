package com.benkio.TemplateBot

import cats.effect.IO
import cats.syntax.all.*
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

class TemplateBotSpec extends BaseBotSpec {

  val tplSBotConfig: SBotConfig             = SBotConfig.fromSBotInfo(TemplateBot.sBotInfo)
  val emptyDBLayer: DBLayer[IO]             = DBLayerMock.mock(tplSBotConfig.sBotInfo.botId)

  val templateBot: IO[BotEngine[IO]] = buildTestEngine(dbLayer = emptyDBLayer, sBotConfig = tplSBotConfig)

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

  test("TemplateBot should contain the expected number of commands") {
    assertIO(
      commandRepliesData.map(_.length),
      12,
      "TemplateBot should have 12 commands"
    )
  }

  jsonContainsFilenames(
    jsonFilename = "tpl_list.json",
    botData = messageRepliesDataPrettyPrint
  )
  botJsonsAreValid(tplSBotConfig)

  triggerFileContainsTriggers(
    triggerFilename = tplSBotConfig.triggerFilename,
    botMediaFiles = messageRepliesDataPrettyPrint,
    botTriggersIO = messageRepliesData.map(_.flatMap(mrd => Show[Trigger].show(mrd.trigger).split('\n')))
  )

  instructionsCommandTest(
    commandRepliesDataF = commandRepliesData,
    instructionsUri = "https://github.com/benkio/sBots/blob/main/modules/bots/TemplateBot/tpl_instructions.md"
  )
}
