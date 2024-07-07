package com.benkio.calandrobot

import com.benkio.telegrambotinfrastructure.BaseBotSpec
import com.benkio.telegrambotinfrastructure.model.ReplyBundle

import com.benkio.telegrambotinfrastructure.mocks.DBLayerMock
import com.benkio.telegrambotinfrastructure.resources.db.DBLayer
import log.effect.LogLevels
import log.effect.fs2.SyncLogWriter.consoleLogUpToLevel
import log.effect.LogWriter

import cats.Show
import cats.effect.IO
import cats.implicits.*
import com.benkio.telegrambotinfrastructure.model.Trigger
import munit.CatsEffectSuite

class CalandroBotSpec extends BaseBotSpec {

  given log: LogWriter[IO]      = consoleLogUpToLevel(LogLevels.Info)
  val emptyDBLayer: DBLayer[IO] = DBLayerMock.mock(CalandroBot.botName)
  val excludeTriggers           = List("GIOCHI PER IL MIO PC")

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
      .unsafeRunSync(),
  )

  triggerFileContainsTriggers(
    triggerFilename = "cala_triggers.txt",
    botMediaFiles = CalandroBot
      .messageRepliesData[IO]
      .flatTraverse(_.reply.prettyPrint)
      .unsafeRunSync()
      .filter(x => !excludeTriggers.exists(exc => x.startsWith(exc))),
    botTriggers = CalandroBot.messageRepliesData[IO].flatMap(mrd => Show[Trigger].show(mrd.trigger).split('\n')),
  )
}
