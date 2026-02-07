package com.benkio.telegrambotinfrastructure.repository

import cats.data.NonEmptyList
import cats.effect.IO
import cats.effect.Resource
import com.benkio.telegrambotinfrastructure.mocks.ApiMock.given
import com.benkio.telegrambotinfrastructure.mocks.RepositoryMock
import com.benkio.telegrambotinfrastructure.mocks.SampleWebhookBot
import com.benkio.telegrambotinfrastructure.model.media.MediaResource.MediaResourceFile
import com.benkio.telegrambotinfrastructure.model.reply.Document
import com.benkio.telegrambotinfrastructure.model.reply.MediaFile
import com.benkio.telegrambotinfrastructure.model.reply.ReplyBundleMessage
import io.circe.syntax.*
import log.effect.fs2.SyncLogWriter.consoleLogUpToLevel
import log.effect.LogLevels
import log.effect.LogWriter
import munit.CatsEffectSuite

import java.io.File

class JsonRepliesRepositorySpec extends CatsEffectSuite {

  given log: LogWriter[IO] = consoleLogUpToLevel(LogLevels.Info)

  private def repositoryWithJsonFile(tempFile: File): Repository[IO] =
    RepositoryMock(
      getResourceFileHandler = (mediaFile: MediaFile) =>
        mediaFile match {
          case Document(filename, _) if filename == SampleWebhookBot.repliesJsonFilename =>
            IO.pure(Right(NonEmptyList.one(MediaResourceFile(Resource.pure(tempFile)))))
          case x => IO.raiseError(Throwable(s"$x is not the expected Document"))
        }
    )

  private def assertLoadedMatchesExpected(
      loaded: List[ReplyBundleMessage],
      expected: List[ReplyBundleMessage]
  ): IO[Unit] =
    IO(assertEquals(loaded.length, expected.length)) >>
      IO(assertEquals(loaded.asJson, expected.asJson))

  private def loadRepliesAndAssert(
      repository: Repository[IO],
      expected: List[ReplyBundleMessage]
  ): IO[Unit] = {
    val repo = JsonRepliesRepository[IO](repository)
    repo.loadReplies(SampleWebhookBot.repliesJsonFilename).flatMap(assertLoadedMatchesExpected(_, expected))
  }

  test("loadReplies loads and decodes ReplyBundleMessages from JSON using SampleWebhookBot config") {
    SampleWebhookBot().flatMap { bot =>
      val jsonBytes = bot.messageRepliesData.asJson.noSpaces.getBytes
      Repository
        .toTempFile[IO](SampleWebhookBot.repliesJsonFilename, jsonBytes)
        .use(tempFile => loadRepliesAndAssert(repositoryWithJsonFile(tempFile), bot.messageRepliesData))
    }
  }

  test("loadReplies raises FileNotFound when the repository returns no file for the JSON filename") {
    val repository = RepositoryMock(
      getResourceFileHandler =
        (mediaFile: MediaFile) => IO.pure(Left(Repository.RepositoryError.NoResourcesFoundFile(mediaFile)))
    )
    val repo = JsonRepliesRepository[IO](repository)

    repo.loadReplies(SampleWebhookBot.repliesJsonFilename).attempt.map {
      case Left(_: JsonRepliesRepository.JsonRepliesRepositoryError.FileNotFound) => ()
      case Left(other) => fail(s"expected FileNotFound, got $other")
      case Right(_)    => fail("expected failure")
    }
  }
}
