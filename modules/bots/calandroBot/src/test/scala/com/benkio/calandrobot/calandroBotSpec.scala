package com.benkio.calandrobot

import cats.data.NonEmptyList
import cats.effect.Async
import cats.effect.IO
import cats.syntax.all.*
import cats.Parallel
import cats.Show
import com.benkio.telegrambotinfrastructure.mocks.ApiMock.given
import com.benkio.telegrambotinfrastructure.mocks.DBLayerMock
import com.benkio.telegrambotinfrastructure.mocks.RepositoryMock
import com.benkio.telegrambotinfrastructure.model.media.MediaResource.MediaResourceIFile
import com.benkio.telegrambotinfrastructure.model.reply.Document
import com.benkio.telegrambotinfrastructure.model.reply.MediaFile
import com.benkio.telegrambotinfrastructure.model.reply.MediaReply
import com.benkio.telegrambotinfrastructure.model.reply.ReplyBundleCommand
import com.benkio.telegrambotinfrastructure.model.reply.ReplyBundleMessage
import com.benkio.telegrambotinfrastructure.model.Trigger
import com.benkio.telegrambotinfrastructure.repository.db.DBLayer
import com.benkio.telegrambotinfrastructure.repository.Repository.RepositoryError
import com.benkio.telegrambotinfrastructure.repository.ResourcesRepository
import com.benkio.telegrambotinfrastructure.BaseBotSpec
import log.effect.fs2.SyncLogWriter.consoleLogUpToLevel
import log.effect.LogLevels
import log.effect.LogWriter
import munit.CatsEffectSuite

class CalandroBotSpec extends BaseBotSpec {

  given log: LogWriter[IO] = consoleLogUpToLevel(LogLevels.Info)

  val emptyDBLayer: DBLayer[IO]             = DBLayerMock.mock(CalandroBot.sBotConfig.sBotInfo.botId)
  val mediaResource: MediaResourceIFile[IO] =
    MediaResourceIFile(
      "test mediafile"
    )
  val repositoryMock = new RepositoryMock(
    getResourceByKindHandler = (_, botId) =>
      IO.raiseUnless(botId == CalandroBot.sBotConfig.sBotInfo.botId)(
        Throwable(s"[CalandroBotSpec] getResourceByKindHandler called with unexpected botId: $botId")
      ).as(NonEmptyList.one(NonEmptyList.one(mediaResource))),
    getResourceFileHandler = (mediaFile: MediaFile) =>
      mediaFile match {
        case Document(v, _) if v == CalandroBot.sBotConfig.repliesJsonFilename =>
          ResourcesRepository.fromResources[IO]().getResourceFile(mediaFile).use(IO.pure)
        case _ => Left(RepositoryError.NoResourcesFoundFile(mediaFile)).pure[IO]
      }
  )

  val calandroBot = for {
    botSetup <- buildTestBotSetup(
      repository = repositoryMock,
      dbLayer = emptyDBLayer,
      sBotConfig = CalandroBot.sBotConfig,
      ttl = CalandroBot.sBotConfig.messageTimeToLive
    )
    // messageRepliesData <- botSetup.jsonRepliesRepository.loadReplies(CalandroBot.sBotConfig.repliesJsonFilename)
  } yield new CalandroBotPolling[IO](
    botSetup, // messageRepliesData
    List.empty
  )(using Parallel[IO], Async[IO], botSetup.api, log)

  val messageRepliesData: IO[List[ReplyBundleMessage]] =
    calandroBot
      .map(ab => ab.messageRepliesData)
  val commandRepliesData: IO[List[ReplyBundleCommand]] =
    calandroBot
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
      regexTriggerLengthReturnValue(mrds)
    })
    .unsafeRunSync()

  test("CalandroBot should contain the expected number of commands") {
    assertIO(
      commandRepliesData.map(_.length),
      30,
      "CalandroBot should have 10 commands"
    )
  }

  jsonContainsFilenames(
    jsonFilename = "cala_list.json",
    botData = messageRepliesDataMediaFiles
  )

  triggerFileContainsTriggers(
    triggerFilename = CalandroBot.sBotConfig.triggerFilename,
    botMediaFiles =
      messageRepliesDataPrettyPrint.map(_.filterNot(x => excludeTriggers.exists(exc => x.startsWith(exc)))),
    botTriggersIO = messageRepliesDataTriggers
  )
}
