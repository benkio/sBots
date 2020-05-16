import sbt._
import Keys._

object Dependencies {

  lazy val dependenciesVersion = new {
    val scalaTest            = "3.1.1"
    val scalaCheck           = "1.13.4"
    val telegramBot4sVersion = "3.0.16"
  }

  lazy val libs = new  {
    val test          = "org.scalatest"    %% "scalatest" % dependenciesVersion.scalaTest % Test
    val check         = "org.scalacheck"   %% "scalacheck" % dependenciesVersion.scalaCheck % Test
    val telegramBot4s = "info.mukel"       %% "telegrambot4s" % dependenciesVersion.telegramBot4sVersion
  }

  val TelegramBotInfrastructureDependencies: Seq[ModuleID] = Seq(
    libs.test,
    libs.check,
    libs.telegramBot4s
  )

}
