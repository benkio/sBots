package com.benkio.calandrobot

import cats.data.NonEmptyList
import cats.effect.IO
import cats.Show
import com.benkio.telegrambotinfrastructure.mocks.ApiMock.given
import com.benkio.telegrambotinfrastructure.mocks.DBLayerMock
import com.benkio.telegrambotinfrastructure.mocks.RepositoryMock
import com.benkio.telegrambotinfrastructure.model.media.MediaResource.MediaResourceIFile
import com.benkio.telegrambotinfrastructure.model.reply.MediaReply
import com.benkio.telegrambotinfrastructure.model.reply.ReplyBundleCommand
import com.benkio.telegrambotinfrastructure.model.reply.ReplyBundleMessage
import com.benkio.telegrambotinfrastructure.model.Trigger
import com.benkio.telegrambotinfrastructure.repository.db.DBLayer
import com.benkio.telegrambotinfrastructure.BackgroundJobManager
import com.benkio.telegrambotinfrastructure.BaseBotSpec
import log.effect.fs2.SyncLogWriter.consoleLogUpToLevel
import log.effect.LogLevels
import log.effect.LogWriter
import munit.CatsEffectSuite

class CalandroBotSpec extends BaseBotSpec {

  given log: LogWriter[IO] = consoleLogUpToLevel(LogLevels.Info)

  val emptyDBLayer: DBLayer[IO]             = DBLayerMock.mock(CalandroBot.sBotInfo.botId)
  val mediaResource: MediaResourceIFile[IO] =
    MediaResourceIFile(
      "test mediafile"
    )
  val repositoryMock = new RepositoryMock(
    getResourceByKindHandler = (_, botId) =>
      IO.raiseUnless(botId == CalandroBot.sBotInfo.botId)(
        Throwable(s"[CalandroBotSpec] getResourceByKindHandler called with unexpected botId: $botId")
      ).as(NonEmptyList.one(NonEmptyList.one(mediaResource)))
  )

  val calandroBot =
    BackgroundJobManager[IO](
      dbLayer = emptyDBLayer,
      sBotInfo = CalandroBot.sBotInfo
    ).map(bjm =>
      new CalandroBotPolling[IO](
        repository = repositoryMock,
        dbLayer = emptyDBLayer,
        backgroundJobManager = bjm
      )
    )

  val messageRepliesData: IO[List[ReplyBundleMessage]] =
    calandroBot
      .map(ab => ab.messageRepliesData)
  val commandRepliesData: IO[List[ReplyBundleCommand]] =
    calandroBot
      .map(_.allCommandRepliesData)
  val messageRepliesDataPrettyPrint: IO[List[String]] =
    messageRepliesData.map(_.flatMap(mr => mr.reply.prettyPrint))
  val excludeTriggers              = List("GIOCHI PER IL MIO PC")
  val messageRepliesDataMediaFiles = messageRepliesData.map(
    _.collect(replyBundleMessage =>
      replyBundleMessage.reply match {
        case mediaReply: MediaReply => mediaReply.mediaFiles.map(_.filename)
      }
    ).flatten
  )

  exactTriggerReturnExpectedReplyBundle(CalandroBot.messageRepliesData)
  regexTriggerLengthReturnValue(CalandroBot.messageRepliesData)

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
    triggerFilename = CalandroBot.triggerFilename,
    botMediaFiles =
      messageRepliesDataPrettyPrint.map(_.filterNot(x => excludeTriggers.exists(exc => x.startsWith(exc)))),
    botTriggers = CalandroBot.messageRepliesData
      .flatMap(mrd => Show[Trigger].show(mrd.trigger).split('\n'))
  )
}
