import sbt._
import Keys._

object Dependencies {

  lazy val dependenciesVersion = new {
    val catsVersion          = "2.1.1"
    val scalaTestVersion     = "3.2.5"
    val scalaCheckVersion    = "1.13.4"
    val telegramiumVersion   = "4.51.0"
    val sqliteJdbcVersion    = "3.31.1"
    val randomVersion        = "0.0.2"
  }
  lazy val libs = new  {
    val test          = "org.scalatest" %% "scalatest" % dependenciesVersion.scalaTestVersion % Test
    val check         = "org.scalacheck" %% "scalacheck" % dependenciesVersion.scalaCheckVersion % Test
    val telegramiumCore = "io.github.apimorphism" %% "telegramium-core" % dependenciesVersion.telegramiumVersion
    val telegramiumHigh = "io.github.apimorphism" %% "telegramium-high" % dependenciesVersion.telegramiumVersion
    val catsEffect = "org.typelevel" %% "cats-effect" % dependenciesVersion.catsVersion
    val cats = "org.typelevel" %% "cats-core" % dependenciesVersion.catsVersion
    val sqliteJdbc    = "org.xerial" %  "sqlite-jdbc" % dependenciesVersion.sqliteJdbcVersion
    val random = "io.chrisdavenport" % "random_2.12" % dependenciesVersion.randomVersion
  }

  val TelegramBotInfrastructureDependencies: Seq[ModuleID] = Seq(
    libs.test,
    libs.check,
    libs.telegramiumCore,
    libs.telegramiumHigh,
    libs.sqliteJdbc,
    libs.catsEffect,
    libs.cats,
    libs.random
  )

}
