package com.benkio.telegramBotInfrastructure.botCapabilities

import java.nio.file.Path
import java.nio.file.Paths

trait ResourcesAccess {
  val rootPath = Paths.get("").toAbsolutePath()

  def buildPath(filename : String) : Path =
    Paths.get(rootPath.toString(), "src", "main", "resources", filename)

}
