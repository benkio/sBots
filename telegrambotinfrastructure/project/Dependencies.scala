import sbt._
import Keys._

object Dependencies {

  lazy val version = new {
    val scalaTest            = "3.0.0"
    val scalaCheck           = "1.13.4"
    val telegramBot4sVersion = "3.0.16"
  }

  lazy val libs = new  {
    val test          = "org.scalatest"    %% "scalatest" % version.scalaTest % Test
    val check         = "org.scalacheck"   %% "scalacheck" % version.scalaCheck % Test
    val telegramBot4s = "info.mukel"       %% "telegrambot4s" % version.telegramBot4sVersion
  }

  val TelegramBotInfrastructureDependencies: Seq[ModuleID] = Seq(
    libs.test,
    libs.check,
    libs.telegramBot4s
  )

}
