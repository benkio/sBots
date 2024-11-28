package com.benkio.telegrambotinfrastructure.mocks

import cats.effect.IO
import cats.effect.Resource
import com.benkio.telegrambotinfrastructure.resources.ResourceAccess

import java.io.File

class ResourceAccessMock(
    returnValues: List[File] = List(),
    getResourceByteArrayHandler: String => IO[Array[Byte]] = _ =>
      IO.raiseError(Throwable(s"[ResourceAccessMock] getResourceByteArray call unexpected"))
) extends ResourceAccess[IO] {

  override def getResourceByteArray(resourceName: String): Resource[IO, Array[Byte]] =
    Resource.eval(getResourceByteArrayHandler(resourceName))
  override def getResourcesByKind(criteria: String): Resource[IO, List[File]] =
    Resource.pure(returnValues)
}
