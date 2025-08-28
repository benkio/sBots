package com.benkio.calandrobot

import cats.effect.IO
import cats.implicits.*
import cats.Show
import com.benkio.telegrambotinfrastructure.mocks.DBLayerMock
import com.benkio.telegrambotinfrastructure.model.reply.ReplyBundle
import com.benkio.telegrambotinfrastructure.model.Trigger
import com.benkio.telegrambotinfrastructure.repository.db.DBLayer
import com.benkio.telegrambotinfrastructure.BaseBotSpec
import log.effect.fs2.SyncLogWriter.consoleLogUpToLevel
import log.effect.LogLevels
import log.effect.LogWriter
import munit.CatsEffectSuite

class CalandroBotSpec extends BaseBotSpec {

  given log: LogWriter[IO]      = consoleLogUpToLevel(LogLevels.Info)
  val emptyDBLayer: DBLayer[IO] = DBLayerMock.mock(CalandroBot.botName)
  val excludeTriggers           = List("GIOCHI PER IL MIO PC")

  exactTriggerReturnExpectedReplyBundle(CalandroBot.messageRepliesData[IO])

  jsonContainsFilenames(
    jsonFilename = "cala_list.json",
    botData = CalandroBot
      .messageRepliesData[IO]
      .filter(ReplyBundle.containsMediaReply(_))
      .flatTraverse(_.reply.prettyPrint)
      .both(
        CalandroBot
          .commandRepliesData[IO](dbLayer = emptyDBLayer)
          .flatTraverse(_.reply.prettyPrint)
      )
      .map { case (m, c) => m ++ c }
  )

  triggerFileContainsTriggers(
    triggerFilename = CalandroBot.triggerFilename,
    botMediaFiles = CalandroBot
      .messageRepliesData[IO]
      .flatTraverse(_.reply.prettyPrint)
      .handleError(_ => List.empty)
      .map(_.filterNot(x => excludeTriggers.exists(exc => x.startsWith(exc)))),
    botTriggers = CalandroBot.messageRepliesData[IO].flatMap(mrd => Show[Trigger].show(mrd.trigger).split('\n'))
  )
}
