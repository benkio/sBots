package com.benkio.botDB.mocks

import cats.effect.{IO, Resource}
import com.benkio.telegrambotinfrastructure.botcapabilities.ResourceAccess

import java.io.File

class ResourceAccessMock(returnValues: List[File]) extends ResourceAccess[IO] {
  println(returnValues)
  def getResourceByteArray(resourceName: String): Resource[IO, Array[Byte]] = ???
  def getResourcesByKind(criteria: String): Resource[IO, List[File]] = ???
}
