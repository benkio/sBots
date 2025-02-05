package com.benkio.telegrambotinfrastructure.mocks

import com.benkio.telegrambotinfrastructure.model.reply.MediaFile
import com.benkio.telegrambotinfrastructure.model.media.MediaResource
import cats.effect.IO
import cats.effect.Resource
import com.benkio.telegrambotinfrastructure.resources.ResourceAccess

class ResourceAccessMock(
    returnValues: List[MediaResource] = List(),
    getResourceFileHandler: MediaFile => IO[MediaResource] = _ =>
      IO.raiseError(Throwable("[ResourceAccessMock] getResourceByteArray call unexpected"))
) extends ResourceAccess[IO] {

  override def getResourceFile(mediaFile: MediaFile): Resource[IO, MediaResource] =
    Resource.eval(getResourceFileHandler(mediaFile))
  override def getResourcesByKind(criteria: String): Resource[IO, List[MediaResource]] =
    Resource.pure(returnValues)
}
