package com.benkio.telegrambotinfrastructure.mocks

import cats.data.NonEmptyList
import cats.effect.IO
import cats.effect.Resource
import com.benkio.telegrambotinfrastructure.model.media.MediaResource[F]
import com.benkio.telegrambotinfrastructure.model.reply.MediaFile
import com.benkio.telegrambotinfrastructure.resources.ResourceAccess

class ResourceAccessMock(
    returnValues: List[MediaResource[F]] = List(),
    getResourceFileHandler: MediaFile => IO[MediaResource[F]] = _ =>
      IO.raiseError(Throwable("[ResourceAccessMock] getResourceByteArray call unexpected"))
) extends ResourceAccess[IO] {

  override def getResourceFile(mediaFile: MediaFile): Resource[IO, NonEmptyList[MediaResource[F]]] =
    Resource.eval(getResourceFileHandler(mediaFile))
  override def getResourcesByKind(criteria: String): Resource[IO, NonEmptyList[NonEmptyList[MediaResource[F]]]] =
    Resource.pure(returnValues)
}
