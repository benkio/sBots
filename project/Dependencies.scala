import sbt._

import Keys._

object Dependencies {

  lazy val dependenciesVersion = new {
    val catsVersion           = "2.6.1"
    val catsEffectVersion     = "3.2.9"
    val scalaTestVersion      = "3.2.10"
    val telegramiumVersion    = "7.53.0"
    val sqliteJdbcVersion     = "3.36.0.3"
    val lightbendEmojiVersion = "1.2.3"
    val munit                 = "0.7.29"
    val munitCatsEffect       = "1.0.0"
  }
  lazy val libs = new {
    val scalaTest       = "org.scalatest"         %% "scalatest"            % dependenciesVersion.scalaTestVersion % Test
    val munit           = "org.scalameta"         %% "munit"                % dependenciesVersion.munit % Test
    val munitCatsEffect = "org.typelevel"         %% "munit-cats-effect-3"  % dependenciesVersion.munitCatsEffect % "test"
    val telegramiumCore = "io.github.apimorphism" %% "telegramium-core"     % dependenciesVersion.telegramiumVersion
    val telegramiumHigh = "io.github.apimorphism" %% "telegramium-high"     % dependenciesVersion.telegramiumVersion
    val catsEffect      = "org.typelevel"         %% "cats-effect"          % dependenciesVersion.catsEffectVersion
    val cats            = "org.typelevel"         %% "cats-core"            % dependenciesVersion.catsVersion
    val sqliteJdbc      = "org.xerial"            % "sqlite-jdbc"           % dependenciesVersion.sqliteJdbcVersion
    val lightbendEmoji  = "com.lightbend"         %% "emoji"                % dependenciesVersion.lightbendEmojiVersion
  }

  val TelegramBotInfrastructureDependencies: Seq[ModuleID] = Seq(
    libs.munit,
    libs.munitCatsEffect,
    libs.telegramiumCore,
    libs.telegramiumHigh,
    libs.sqliteJdbc,
    libs.catsEffect,
    libs.cats
  )

  val CalandroBotDependencies: Seq[ModuleID] = Seq(
    libs.scalaTest,
    libs.lightbendEmoji,
    libs.telegramiumCore,
    libs.telegramiumHigh,
    libs.catsEffect
  )

  val ABarberoBotDependencies: Seq[ModuleID] = Seq(
    libs.scalaTest,
    libs.telegramiumCore,
    libs.telegramiumHigh,
    libs.catsEffect
  )

  val RichardPHJBensonBotDependencies: Seq[ModuleID] = Seq(
    libs.scalaTest,
    libs.lightbendEmoji,
    libs.telegramiumCore,
    libs.telegramiumHigh,
    libs.catsEffect
  )

  val XahBotDependencies: Seq[ModuleID] = Seq(
    libs.scalaTest,
    libs.telegramiumCore,
    libs.telegramiumHigh,
    libs.catsEffect
  )

  val MainDependencies: Seq[ModuleID] = Seq(
    libs.catsEffect
  )
}
