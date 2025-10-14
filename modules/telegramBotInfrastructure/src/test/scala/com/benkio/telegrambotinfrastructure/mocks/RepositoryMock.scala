package com.benkio.telegrambotinfrastructure.mocks

import cats.data.NonEmptyList
import cats.effect.IO
import cats.effect.Resource
import com.benkio.telegrambotinfrastructure.model.media.MediaResource
import com.benkio.telegrambotinfrastructure.model.reply.MediaFile
import com.benkio.telegrambotinfrastructure.model.SBotId
import com.benkio.telegrambotinfrastructure.repository.Repository
import com.benkio.telegrambotinfrastructure.repository.Repository.RepositoryError

class RepositoryMock(
    getResourceByKindHandler: (String, SBotId) => IO[NonEmptyList[NonEmptyList[MediaResource[IO]]]] = (_, _) =>
      IO.raiseError(Throwable("[RepositoryMock] getResourceByKindHandler call unexpected")),
    getResourceFileHandler: MediaFile => IO[NonEmptyList[MediaResource[IO]]] = _ =>
      IO.raiseError(Throwable("[RepositoryMock] getResourceByteArray call unexpected"))
) extends Repository[IO] {

  override def getResourceFile(
      mediaFile: MediaFile
  ): Resource[IO, Either[RepositoryError, NonEmptyList[MediaResource[IO]]]] =
    Resource.eval(getResourceFileHandler(mediaFile).map(Right(_)))
  override def getResourcesByKind(
      criteria: String,
      botId: SBotId
  ): Resource[IO, Either[RepositoryError, NonEmptyList[NonEmptyList[MediaResource[IO]]]]] =
    Resource.eval(getResourceByKindHandler(criteria, botId).map(Right(_)))
}
