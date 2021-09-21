import sbt._
import Keys._

object Dependencies {

  lazy val dependenciesVersion = new {
    val catsVersion           = "2.2.0"
    val catsEffectVersion     = "2.5.1"
    val scalaTestVersion      = "3.2.10"
    val telegramiumVersion    = "6.53.0"
    val sqliteJdbcVersion     = "3.34.0"
    val randomVersion         = "0.0.2"
    val lightbendEmojiVersion = "1.2.3"
  }
  lazy val libs = new {
    val test            = "org.scalatest"         %% "scalatest"        % dependenciesVersion.scalaTestVersion % Test
    val telegramiumCore = "io.github.apimorphism" %% "telegramium-core" % dependenciesVersion.telegramiumVersion
    val telegramiumHigh = "io.github.apimorphism" %% "telegramium-high" % dependenciesVersion.telegramiumVersion
    val catsEffect      = "org.typelevel"         %% "cats-effect"      % dependenciesVersion.catsEffectVersion
    val cats            = "org.typelevel"         %% "cats-core"        % dependenciesVersion.catsVersion
    val sqliteJdbc      = "org.xerial"             % "sqlite-jdbc"      % dependenciesVersion.sqliteJdbcVersion
    val random          = "io.chrisdavenport"      % "random_2.13"      % dependenciesVersion.randomVersion
    val lightbendEmoji  = "com.lightbend"         %% "emoji"            % dependenciesVersion.lightbendEmojiVersion
  }

  val TelegramBotInfrastructureDependencies: Seq[ModuleID] = Seq(
    libs.test,
    libs.telegramiumCore,
    libs.telegramiumHigh,
    libs.sqliteJdbc,
    libs.catsEffect,
    libs.cats,
    libs.random
  )

  val CalandroBotDependencies: Seq[ModuleID] = Seq(
    libs.test,
    libs.lightbendEmoji,
    libs.telegramiumCore,
    libs.telegramiumHigh,
    libs.catsEffect
  )

  val ABarberoBotDependencies: Seq[ModuleID] = Seq(
    libs.test,
    libs.telegramiumCore,
    libs.telegramiumHigh,
    libs.catsEffect
  )

  val RichardPHJBensonBotDependencies: Seq[ModuleID] = Seq(
    libs.test,
    libs.lightbendEmoji,
    libs.telegramiumCore,
    libs.telegramiumHigh,
    libs.catsEffect
  )

  val XahBotDependencies: Seq[ModuleID] = Seq(
    libs.test,
    libs.telegramiumCore,
    libs.telegramiumHigh,
    libs.catsEffect
  )

  val MainDependencies: Seq[ModuleID] = Seq(
    libs.catsEffect
  )
}
