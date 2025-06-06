package com.benkio.integration.integrationmunit.botDB

trait Constants {

  val dbName: String                   = "botDB.sqlite3"
  val integrationResourcesPath: String = getClass.getResource("/").getPath
  val mainResourcesPath: String        = s"$integrationResourcesPath../../../../botDB/src/main/resources/"
  val dbPath: String                   = s"$integrationResourcesPath../$dbName"
  val dbUrl: String                    = s"jdbc:sqlite:$dbPath"

  val testApplicationConf: String      = "application.test.conf"
  val youTubeTokenFilename             = "youTubeApiKey.token"
  val youTubeTokenFilenamePath: String = s"$mainResourcesPath$youTubeTokenFilename"
}
