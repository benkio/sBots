package com.benkio.chatcore.mocks

import cats.data.NonEmptyList
import cats.effect.IO
import cats.effect.Resource
import com.benkio.chatcore.model.media.MediaResource
import com.benkio.chatcore.model.reply.MediaFile
import com.benkio.chatcore.model.SBotInfo.SBotId
import com.benkio.chatcore.repository.Repository
import com.benkio.chatcore.repository.Repository.RepositoryError

class RepositoryMock(
    getResourceByKindHandler: (String, SBotId) => IO[NonEmptyList[NonEmptyList[MediaResource[IO]]]] = (_, _) =>
      IO.raiseError(Throwable("[RepositoryMock] getResourceByKindHandler call unexpected")),
    getResourceFileHandler: MediaFile => IO[Either[RepositoryError, NonEmptyList[MediaResource[IO]]]] = _ =>
      IO.raiseError(Throwable("[RepositoryMock] getResourceByteArray call unexpected"))
) extends Repository[IO] {

  override def getResourceFile(
      mediaFile: MediaFile
  ): Resource[IO, Either[RepositoryError, NonEmptyList[MediaResource[IO]]]] =
    Resource.eval(getResourceFileHandler(mediaFile))
  override def getResourcesByKind(
      criteria: String,
      botId: SBotId
  ): Resource[IO, Either[RepositoryError, NonEmptyList[NonEmptyList[MediaResource[IO]]]]] =
    Resource.eval(getResourceByKindHandler(criteria, botId).map(Right(_)))
}
