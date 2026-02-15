package com.benkio.telegrambotinfrastructure.repository

import cats.effect.IO
import com.benkio.telegrambotinfrastructure.mocks.ApiMock.given
import com.benkio.telegrambotinfrastructure.mocks.SampleWebhookBot
import com.benkio.telegrambotinfrastructure.model.reply.ReplyBundleMessage
import com.benkio.telegrambotinfrastructure.repository.Repository.RepositoryError
import io.circe.syntax.*
import log.effect.fs2.SyncLogWriter.consoleLogUpToLevel
import log.effect.LogLevels
import log.effect.LogWriter
import munit.CatsEffectSuite

class JsonDataRepositorySpec extends CatsEffectSuite {

  given log: LogWriter[IO] = consoleLogUpToLevel(LogLevels.Info)

  private def assertLoadedMatchesExpected(
      loaded: List[ReplyBundleMessage],
      expected: List[ReplyBundleMessage]
  ): IO[Unit] =
    IO(assertEquals(loaded.length, expected.length)) >>
      IO(assertEquals(loaded.asJson, expected.asJson))

  private def loadRepliesAndAssert(
      expected: List[ReplyBundleMessage]
  ): IO[Unit] = {
    val repo = JsonDataRepository[IO]()
    repo
      .loadData[ReplyBundleMessage](SampleWebhookBot.repliesJsonFilename)
      .flatMap(assertLoadedMatchesExpected(_, expected))
  }

  test("loadData[ReplyBundleMessage] loads and decodes ReplyBundleMessages from JSON using SampleWebhookBot config") {
    for {
      bot    <- SampleWebhookBot()
      result <- loadRepliesAndAssert(bot.messageRepliesData)
    } yield ()
  }

  test("loadData[ReplyBundleMessage] raises FileNotFound when the repository returns no file for the JSON filename") {
    val repo = JsonDataRepository[IO]()

    repo.loadData[ReplyBundleMessage]("non_existent_file.json").attempt.map {
      case Left(_: RepositoryError.NoResourcesFoundByteArray) => ()
      case e => fail(s"expected NoResourcesFoundByteArray failure, got: $e")
    }
  }
}
