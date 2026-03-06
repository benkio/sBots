package com.benkio.TemplateBot

import cats.data.NonEmptyList
import cats.effect.Async
import cats.effect.IO
import cats.syntax.all.*
import cats.Parallel
import cats.Show
import com.benkio.chatcore.mocks.ApiMock.given
import com.benkio.chatcore.mocks.DBLayerMock
import com.benkio.chatcore.mocks.RepositoryMock
import com.benkio.chatcore.model.media.MediaResource.MediaResourceIFile
import com.benkio.chatcore.model.reply.Document
import com.benkio.chatcore.model.reply.MediaFile
import com.benkio.chatcore.model.reply.ReplyBundleCommand
import com.benkio.chatcore.model.reply.ReplyBundleMessage
import com.benkio.chatcore.model.Trigger
import com.benkio.chatcore.repository.db.DBLayer
import com.benkio.chatcore.repository.Repository.RepositoryError
import com.benkio.chatcore.repository.ResourcesRepository
import com.benkio.chatcore.config.SBotConfig
import com.benkio.chatcore.BaseBotSpec
import com.benkio.chatcore.Logger.given
import com.benkio.chatcore.SBot
import com.benkio.chatcore.SBotPolling
import munit.CatsEffectSuite

class TemplateBotSpec extends BaseBotSpec {

  val tplSBotConfig: SBotConfig = SBot.buildSBotConfig(TemplateBot.sBotInfo)
  val emptyDBLayer: DBLayer[IO]              = DBLayerMock.mock(tplSBotConfig.sBotInfo.botId)
  val mediaResource: MediaResourceIFile[IO] =
    MediaResourceIFile(
      "test mediafile"
    )
  val repositoryMock = new RepositoryMock(
    getResourceByKindHandler = (_, botId) =>
      IO.raiseUnless(botId == tplSBotConfig.sBotInfo.botId)(
        Throwable(s"[TemplateBotSpec] getResourceByKindHandler called with unexpected botId: $botId")
      ).as(NonEmptyList.one(NonEmptyList.one(mediaResource))),
    getResourceFileHandler = (mediaFile: MediaFile) =>
      mediaFile match {
        case Document(v, _) if v == tplSBotConfig.repliesJsonFilename =>
          ResourcesRepository.fromResources[IO]().getResourceFile(mediaFile).use(IO.pure)
        case _ => Left(RepositoryError.NoResourcesFoundFile(mediaFile)).pure[IO]
      }
  )

  val templateBot: IO[SBotPolling[IO]] = for {
    botSetup <- buildTestBotSetup(
      repository = repositoryMock,
      dbLayer = emptyDBLayer,
      sBotConfig = tplSBotConfig,
      ttl = tplSBotConfig.messageTimeToLive
    )
    messageRepliesData <- botSetup.jsonDataRepository.loadData[ReplyBundleMessage](
      tplSBotConfig.repliesJsonFilename
    )
    commandRepliesData <- botSetup.jsonDataRepository.loadData[ReplyBundleCommand](
      tplSBotConfig.commandsJsonFilename
    )
  } yield new SBotPolling[IO](botSetup, messageRepliesData, commandRepliesData)(using
    Parallel[IO],
    botSetup.api,
    Async[IO],
    log
  )

  val messageRepliesData: IO[List[ReplyBundleMessage]] =
    templateBot.map(_.messageRepliesData)
  val commandRepliesData: IO[List[ReplyBundleCommand]] =
    templateBot.map(_.allCommandRepliesData)
  val messageRepliesDataPrettyPrint: IO[List[String]] =
    messageRepliesData.map(_.flatMap(mr => mr.reply.prettyPrint))

  messageRepliesData
    .map(mrd => {
      exactTriggerReturnExpectedReplyBundle(mrd)
    })
    .unsafeRunSync()

  test("TemplateBot should contain the expected number of commands") {
    assertIO(
      commandRepliesData.map(_.length),
      10,
      "TemplateBot should have 5 commands"
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
}
