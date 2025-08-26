package com.benkio.telegrambotinfrastructure.mocks

import cats.data.NonEmptyList
import cats.effect.IO
import cats.effect.Resource
import com.benkio.telegrambotinfrastructure.model.media.MediaResource
import com.benkio.telegrambotinfrastructure.model.reply.MediaFile
import com.benkio.telegrambotinfrastructure.repository.Repository

class RepositoryMock(
    getResourceByKindHandler: String => IO[NonEmptyList[NonEmptyList[MediaResource[IO]]]] = _ =>
      IO.raiseError(Throwable("[RepositoryMock] getResourceByKindHandler call unexpected")),
    getResourceFileHandler: MediaFile => IO[NonEmptyList[MediaResource[IO]]] = _ =>
      IO.raiseError(Throwable("[RepositoryMock] getResourceByteArray call unexpected"))
) extends Repository[IO] {

  override def getResourceFile(mediaFile: MediaFile): Resource[IO, NonEmptyList[MediaResource[IO]]] =
    Resource.eval(getResourceFileHandler(mediaFile))
  override def getResourcesByKind(criteria: String): Resource[IO, NonEmptyList[NonEmptyList[MediaResource[IO]]]] =
    Resource.eval(getResourceByKindHandler(criteria))
}
