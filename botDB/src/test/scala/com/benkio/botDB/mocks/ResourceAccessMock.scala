package com.benkio.botDB.mocks

import cats.effect.IO
import cats.effect.Resource
import com.benkio.telegrambotinfrastructure.resources.ResourceAccess

import java.io.File

class ResourceAccessMock(returnValues: List[File]) extends ResourceAccess[IO] {

  def getResourceByteArray(resourceName: String): Resource[IO, Array[Byte]] =
    Resource.eval(IO.raiseError(new RuntimeException("Not used for testing")))
  def getResourcesByKind(criteria: String): Resource[IO, List[File]] =
    Resource.pure(returnValues)
}
