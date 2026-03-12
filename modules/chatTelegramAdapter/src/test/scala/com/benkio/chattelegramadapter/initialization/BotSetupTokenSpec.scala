package com.benkio.chattelegramadapter.initialization

import cats.data.NonEmptyList
import cats.effect.IO
import cats.effect.Resource
import com.benkio.chatcore.model.media.MediaResource
import com.benkio.chatcore.model.reply.Document
import com.benkio.chatcore.model.SBotInfo.SBotId
import com.benkio.chatcore.repository.Repository
import com.benkio.chatcore.repository.Repository.RepositoryError
import com.benkio.chatcore.Logger.given
import munit.CatsEffectSuite

import java.nio.charset.StandardCharsets
import java.nio.file.Files

class BotSetupTokenSpec extends CatsEffectSuite {

  private final class RepoStub(
      onGetResourceFile: Resource[IO, Either[RepositoryError, NonEmptyList[MediaResource[IO]]]]
  ) extends Repository[IO] {
    override def getResourcesByKind(
        criteria: String,
        botId: SBotId
    ): Resource[IO, Either[RepositoryError, NonEmptyList[NonEmptyList[MediaResource[IO]]]]] =
      Resource.eval(IO.raiseError(new RuntimeException("Unexpected call")))

    override def getResourceFile(
        mediaFile: com.benkio.chatcore.model.reply.MediaFile
    ): Resource[IO, Either[RepositoryError, NonEmptyList[MediaResource[IO]]]] =
      onGetResourceFile
  }

  test("BotSetup.token should raise TokenNotFound when repository returns Left") {
    val tokenFilename = "missing.token"
    val repo          = RepoStub(
      Resource.pure(
        Left(RepositoryError.NoResourcesFoundFile(Document(tokenFilename)))
      )
    )

    interceptIO[BotSetup.BotSetupError.TokenNotFound](
      BotSetup.token[IO](tokenFilename, repo).use_
    )
  }

  test("BotSetup.token should raise TokenNotFound when no file resources are returned") {
    val tokenFilename = "nofile.token"
    val repo          = RepoStub(
      Resource.pure(
        Right(NonEmptyList.one(MediaResource.MediaResourceIFile("some-telegram-file-id")))
      )
    )

    interceptIO[BotSetup.BotSetupError.TokenNotFound](
      BotSetup.token[IO](tokenFilename, repo).use_
    )
  }

  test("BotSetup.token should raise TokenIsEmpty when file content is empty") {
    val tokenFilename = "empty.token"
    val res           =
      Resource.make {
        IO.blocking {
          val p = Files.createTempFile("sbots-token", ".txt")
          Files.write(p, Array.emptyByteArray)
          p
        }
      }(p => IO.blocking(Files.deleteIfExists(p)).void)

    val repo = RepoStub(
      Resource.pure(Right(NonEmptyList.one(MediaResource.MediaResourceFile(res))))
    )

    interceptIO[BotSetup.BotSetupError.TokenIsEmpty](
      BotSetup.token[IO](tokenFilename, repo).use_
    )
  }

  test("BotSetup.token should return file content when present") {
    val tokenFilename = "ok.token"
    val expected      = "my-secret-token"
    val res           =
      Resource.make {
        IO.blocking {
          val p = Files.createTempFile("sbots-token", ".txt")
          Files.write(p, expected.getBytes(StandardCharsets.UTF_8))
          p
        }
      }(p => IO.blocking(Files.deleteIfExists(p)).void)

    val repo = RepoStub(
      Resource.pure(Right(NonEmptyList.one(MediaResource.MediaResourceFile(res))))
    )

    BotSetup
      .token[IO](tokenFilename, repo)
      .use(token => IO(assertEquals(token, expected)))
  }
}
