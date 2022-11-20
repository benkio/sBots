package com.benkio.botDB

trait DBConstants {

  val dbName: String       = "botDB.db"
  val resourcePath: String = getClass.getResource("/").getPath
  val dbPath: String       = s"$resourcePath$dbName"
  val dbUrl: String        = s"jdbc:sqlite:$dbPath"
  val testApplicationConf: String = "application.test.conf"

}
