package com.benkio.telegramBotInfrastructure.botCapabilities

import java.nio.file.{Path, Paths, Files}
import java.util.stream.Collectors
import scala.collection.JavaConversions._

trait ResourcesAccess {
  val rootPath = Paths.get("").toAbsolutePath()

  def buildPath(filename : String) : Path =
    Paths.get(rootPath.toString(), "src", "main", "resources", filename)

  def directoryFiles(directoryPath : String) : List[String] =
    Files.walk(buildPath(directoryPath)).collect(Collectors.toList())
      .toList
      .map((fl : Path) => directoryPath + "/" + fl.getFileName.toString)
      .tail

}
