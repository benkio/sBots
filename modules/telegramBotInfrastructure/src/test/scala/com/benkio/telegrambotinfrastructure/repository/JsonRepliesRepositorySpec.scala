package com.benkio.telegrambotinfrastructure.repository

import cats.effect.IO
import log.effect.fs2.SyncLogWriter.consoleLogUpToLevel
import log.effect.LogLevels
import log.effect.LogWriter
import munit.CatsEffectSuite


class JsonRepliesRepositorySpec extends CatsEffectSuite {

  given log: LogWriter[IO] = consoleLogUpToLevel(LogLevels.Info)

  // private def repositoryWithRepliesJson(replies: List[ReplyBundleMessage]): Repository[IO] = {
  //   val json = replies.asJson.noSpaces
  //   new RepositoryMock(
  //     getResourceFileHandler = {
  //       case Document(SampleWebhookBot.triggerJsonFilename) =>
  //         IO {
  //           val f = File.createTempFile("sbot_replies", ".json")
  //           Files.writeString(f.toPath, json, StandardCharsets.UTF_8)
  //           f.deleteOnExit()
  //           f
  //         }.map(f => NonEmptyList.one(MediaResourceFile(Resource.pure(f))))
  //       case _ =>
  //         IO.raiseError(Throwable("[JsonRepliesRepositorySpec] getResourceFile called with unexpected file"))
  //     }
  //   )
  // }

  // test("loadReplies loads and decodes ReplyBundleMessages from JSON using SampleWebhookBot config") {
  //   for {
  //     bot        <- SampleWebhookBot()
  //     repository  = repositoryWithRepliesJson(bot.messageRepliesData)
  //     repo       = JsonRepliesRepository[IO](repository)
  //     loaded    <- repo.loadReplies(SampleWebhookBot.sBotConfig)
  //     _         <- IO(assertEquals(loaded.length, bot.messageRepliesData.length))
  //     _         <- IO(assertEquals(loaded, bot.messageRepliesData))
  //   } yield ()
  // }

  // test("loadReplies raises FileNotFound when the repository returns no file for the JSON filename") {
  //   val repository = new Repository[IO] {
  //     override def getResourceFile(
  //         mediaFile: MediaFile
  //     ): Resource[IO, Either[Repository.RepositoryError, NonEmptyList[MediaResource[IO]]]] =
  //       Resource.eval(IO.pure(Left(Repository.RepositoryError.NoResourcesFoundFile(mediaFile))))
  //     override def getResourcesByKind(
  //         criteria: String,
  //         botId: SBotId
  //     ): Resource[IO, Either[Repository.RepositoryError, NonEmptyList[NonEmptyList[MediaResource[IO]]]]] =
  //       Resource.eval(IO.raiseError(Throwable("not used in this test")))
  //   }
  //   val repo = JsonRepliesRepository[IO](repository)

  //   repo.loadReplies(SampleWebhookBot.sBotConfig).attempt.map {
  //     case Left(_: JsonRepliesRepository.JsonRepliesRepositoryError.FileNotFound) => ()
  //     case Left(other)  => fail(s"expected FileNotFound, got $other")
  //     case Right(_)     => fail("expected failure")
  //   }
  // }
}
