package com.benkio.CalandroBot

import cats.effect.IO
import cats.Show
import com.benkio.chatcore.config.SBotConfig
import com.benkio.chatcore.mocks.DBLayerMock
import com.benkio.chatcore.model.reply.MediaReply
import com.benkio.chatcore.model.reply.ReplyBundleCommand
import com.benkio.chatcore.model.reply.ReplyBundleMessage
import com.benkio.chatcore.model.Trigger
import com.benkio.chatcore.repository.db.DBLayer
import com.benkio.chatcore.BaseBotSpec
import com.benkio.chatcore.BotEngine
import com.benkio.chatcore.Logger.given

class CalandroBotSpec extends BaseBotSpec {

  val calaSBotConfig: SBotConfig = SBotConfig.fromSBotInfo(CalandroBot.sBotInfo)
  val emptyDBLayer: DBLayer[IO]  = DBLayerMock.mock(calaSBotConfig.sBotInfo.botId)

  val Calandrobot: IO[BotEngine[IO]] = buildTestEngine(dbLayer = emptyDBLayer, sBotConfig = calaSBotConfig)

  val messageRepliesData: IO[List[ReplyBundleMessage]] =
    Calandrobot
      .map(ab => ab.messageRepliesData)
  val commandRepliesData: IO[List[ReplyBundleCommand]] =
    Calandrobot
      .map(_.allCommandRepliesData)
  val messageRepliesDataPrettyPrint: IO[List[String]] =
    messageRepliesData.map(_.flatMap(mr => mr.reply.prettyPrint))
  val messageRepliesDataTriggers =
    messageRepliesData.map(_.flatMap(mrd => Show[Trigger].show(mrd.trigger).split('\n')))
  val excludeTriggers              = List("GIOCHI PER IL MIO PC")
  val messageRepliesDataMediaFiles = messageRepliesData.map(
    _.collect(replyBundleMessage =>
      replyBundleMessage.reply match {
        case mediaReply: MediaReply => mediaReply.mediaFiles.map(_.filename)
      }
    ).flatten
  )

  messageRepliesData
    .map(mrds => {
      exactTriggerReturnExpectedReplyBundle(mrds)
      noOverlappingReplyBundles(mrds)
    })
    .unsafeRunSync()

  test("CalandroBot should contain the expected number of commands") {
    assertIO(
      commandRepliesData.map(_.length),
      35,
      "CalandroBot should have 35 commands"
    )
  }

  botJsonsAreValid(calaSBotConfig)
  jsonContainsFilenames(
    jsonFilename = "cala_list.json",
    botData = messageRepliesDataMediaFiles
  )

  triggerFileContainsTriggers(
    triggerFilename = calaSBotConfig.triggerFilename,
    botMediaFiles =
      messageRepliesDataPrettyPrint.map(_.filterNot(x => excludeTriggers.exists(exc => x.startsWith(exc)))),
    botTriggersIO = messageRepliesDataTriggers
  )
}
