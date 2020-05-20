package com.benkio.telegramBotInfrastructure.botCapabilities

import com.benkio.telegramBotInfrastructure.model.MediaFile
import scala.util._
import java.nio.file.Path
import java.nio.file.Paths
import java.nio.file.Files
import java.util.stream.Collectors
import scala.collection.JavaConversions._

sealed trait ResourceSource

case object FileSystem extends ResourceSource
case object Database   extends ResourceSource
case object All        extends ResourceSource

object ResourceSource {
  def selectResourceAccess[A <: ResourceSource](source: A): ResourceAccess[ResourceSource] =
    source match {
      case _: All.type        => ResourceAccess.all
      case _: FileSystem.type => ResourceAccess.fileSystem
      case _: Database.type   => ResourceAccess.database
    }
}

trait ResourceAccess[+A <: ResourceSource] {
  def getResource(resourceName: String): Array[Byte]
  def getResourcesByKind(criteria: String): List[MediaFile]
}

object ResourceAccess {
  val fileSystem = new ResourceAccess[FileSystem.type] {
    val rootPath = Paths.get("").toAbsolutePath()

    def getResource(resourceName: String): Array[Byte] =
      Files.readAllBytes(buildPath(resourceName))

    def buildPath(filename: String): Path =
      Paths.get(rootPath.toString(), "src", "main", "resources", filename)

    def getResourcesByKind(criteria: String): List[MediaFile] =
      Files
        .walk(buildPath(criteria))
        .collect(Collectors.toList())
        .toList
        .tail
        .map((fl: Path) => MediaFile(criteria + "/" + fl.getFileName.toString))

  }
  val database = new ResourceAccess[Database.type] {
    def getResource(resourceName: String): Array[Byte]        = ???
    def getResourcesByKind(criteria: String): List[MediaFile] = ???
  }
  val all = new ResourceAccess[All.type] {

    def getResource(resourceName: String): Array[Byte] =
      Try(fileSystem.getResource(resourceName))
        .orElse(Try(database.getResource(resourceName)))
        .getOrElse(Array.empty)

    def getResourcesByKind(criteria: String): List[MediaFile] =
      Try(fileSystem.getResourcesByKind(criteria))
        .orElse(Try(database.getResourcesByKind(criteria)))
        .getOrElse(List.empty)
  }
}
