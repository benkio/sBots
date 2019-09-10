package com.benkio.telegramBotInfrastructure.botCapabilities

import java.nio.file.{Path, Paths, Files}
import java.util.stream.Collectors

trait ResourcesAccess {
  val rootPath = Paths.get("").toAbsolutePath()

  def buildPath(filename : String) : Path =
    Paths.get(rootPath.toString(), "src", "main", "resources", filename)

  def directoryFiles(directoryPath : String) =
    Files.walk(buildPath("cards")).collect(Collectors.toList())

}
