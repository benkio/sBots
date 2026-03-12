package com.benkio.chatcore.repository

import cats.effect.IO
import com.benkio.chatcore.model.reply.ReplyBundleMessage
import com.benkio.chatcore.repository.Repository.RepositoryError
import com.benkio.chatcore.Logger.given
import io.circe.parser.decode
import io.circe.syntax.*
import munit.CatsEffectSuite

class JsonDataRepositorySpec extends CatsEffectSuite {

  private val repliesJsonFilename: String = "sbot_replies.json"

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
      .loadData[ReplyBundleMessage](repliesJsonFilename)
      .flatMap(assertLoadedMatchesExpected(_, expected))
  }

  test("loadData[ReplyBundleMessage] loads and decodes ReplyBundleMessages from JSON resources") {
    val expectedIO =
      IO.blocking(scala.io.Source.fromResource(repliesJsonFilename).mkString)
        .flatMap(json => IO.fromEither(decode[List[ReplyBundleMessage]](json)))

    for {
      expected <- expectedIO
      _        <- loadRepliesAndAssert(expected)
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
