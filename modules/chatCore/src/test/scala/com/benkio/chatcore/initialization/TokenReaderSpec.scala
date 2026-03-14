package com.benkio.chatcore.initialization

import cats.data.NonEmptyList
import cats.effect.IO
import cats.effect.Resource
import com.benkio.chatcore.initialization.TokenReader.TokenError
import com.benkio.chatcore.mocks.RepositoryMock
import com.benkio.chatcore.model.media.MediaResource.MediaResourceFile
import com.benkio.chatcore.model.reply.Document
import com.benkio.chatcore.repository.Repository
import com.benkio.chatcore.Logger.given
import munit.CatsEffectSuite
import munit.ScalaCheckSuite

import java.nio.file.Files

class TokenReaderSpec extends CatsEffectSuite with ScalaCheckSuite {

  test("TokenReader.token should return TokenNotFound in case the filename is not found") {
    val tokenFilename: String = "MissingToken"
    val repository            = RepositoryMock(
      getResourceFileHandler =
        _ => IO.pure(Left(Repository.RepositoryError.NoResourcesFoundFile(Document(tokenFilename))))
    )
    val actual: IO[Either[Throwable, String]] = TokenReader
      .token[IO](
        tokenFilename = tokenFilename,
        repository = repository
      )
      .use(IO.pure)
      .attempt

    assertIO(actual, Left(TokenError.TokenNotFound(tokenFilename)))
  }

  test("TokenReader.token should return TokenEmpty in case the file is empty") {
    val tokenFilename: String                 = "emptyToken.token"
    val actual: IO[Either[Throwable, String]] = for {
      tokenPath <- IO.blocking(Files.createTempFile("empty-token-", ".token"))
      _         <- IO.blocking(Files.write(tokenPath, Array.emptyByteArray))
      repository = RepositoryMock(
        getResourceFileHandler = _ => IO.pure(Right(NonEmptyList.one(MediaResourceFile(Resource.pure(tokenPath)))))
      )
      result <- TokenReader
        .token[IO](
          tokenFilename = tokenFilename,
          repository = repository
        )
        .use(IO.pure)
        .attempt
      _ <- IO.blocking(Files.deleteIfExists(tokenPath))
    } yield result

    assertIO(actual, Left(TokenError.TokenIsEmpty(tokenFilename)))
  }
}
