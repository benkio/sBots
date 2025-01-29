package com.benkio.integration.integrationmunit.botDB

trait DBConstants {

  val dbName: String              = "botDB.sqlite3"
  val resourcePath: String        = getClass.getResource("/").getPath
  val dbPath: String              = s"$resourcePath$dbName"
  val dbUrl: String               = s"jdbc:sqlite:$dbPath"
  val testApplicationConf: String = "application.test.conf"

}
