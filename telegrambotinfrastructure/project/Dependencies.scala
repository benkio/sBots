import sbt._
import Keys._

object Dependencies {

  lazy val dependenciesVersion = new {
    val scalaTestVersion     = "3.1.1"
    val scalaCheckVersion    = "1.13.4"
    val telegramBot4sVersion = "3.0.16"
    val sqliteJdbcVersion    = "3.31.1"
  }

  lazy val libs = new  {
    val test          = "org.scalatest"    %% "scalatest" % dependenciesVersion.scalaTestVersion % Test
    val check         = "org.scalacheck"   %% "scalacheck" % dependenciesVersion.scalaCheckVersion % Test
    val telegramBot4s = "info.mukel"       %% "telegrambot4s" % dependenciesVersion.telegramBot4sVersion
    val sqliteJdbc    = "org.xerial"       %  "sqlite-jdbc" % dependenciesVersion.sqliteJdbcVersion
  }

  val TelegramBotInfrastructureDependencies: Seq[ModuleID] = Seq(
    libs.test,
    libs.check,
    libs.telegramBot4s,
    libs.sqliteJdbc
  )

}
