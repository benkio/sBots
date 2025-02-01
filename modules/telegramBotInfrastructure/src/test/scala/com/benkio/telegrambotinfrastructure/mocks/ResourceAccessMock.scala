package com.benkio.telegrambotinfrastructure.mocks

import log.effect.LogWriter
import cats.effect.Sync
import com.benkio.telegrambotinfrastructure.model.reply.MediaFile
import com.benkio.telegrambotinfrastructure.model.media.MediaResource
import cats.effect.IO
import cats.effect.Resource
import com.benkio.telegrambotinfrastructure.resources.ResourceAccess

class ResourceAccessMock(
    returnValues: List[MediaResource] = List(),
    getResourceFileHandler: MediaFile => IO[MediaResource] = _ =>
      IO.raiseError(Throwable(s"[ResourceAccessMock] getResourceByteArray call unexpected"))
) extends ResourceAccess[IO] {

  def getResourceFile(mediaFile: MediaFile)(using syncF: Sync[IO], log: LogWriter[IO]): Resource[IO, MediaResource] =
    Resource.eval(getResourceFileHandler(mediaFile))
  override def getResourcesByKind(criteria: String): Resource[IO, List[MediaResource]] =
    Resource.pure(returnValues)
}
