import sbt._

import Keys._

object Dependencies {

  lazy val dependenciesVersion = new {
    val catsVersion           = "2.7.0"
    val catsEffectVersion     = "3.3.3"
    val telegramiumVersion    = "7.54.1"
    val sqliteJdbcVersion     = "3.36.0.3"
    val lightbendEmojiVersion = "1.2.3"
    val munit                 = "0.7.29"
    val munitCatsEffect       = "1.0.7"
    val pureConfig            = "0.17.1"
  }
  lazy val libs = new {
    val munit           = "org.scalameta" %% "munit"               % dependenciesVersion.munit           % Test
    val munitCatsEffect = "org.typelevel" %% "munit-cats-effect-3" % dependenciesVersion.munitCatsEffect % "test"
    val telegramiumCore = "io.github.apimorphism" %% "telegramium-core" % dependenciesVersion.telegramiumVersion
    val telegramiumHigh = "io.github.apimorphism" %% "telegramium-high" % dependenciesVersion.telegramiumVersion
    val catsEffect      = "org.typelevel"         %% "cats-effect"      % dependenciesVersion.catsEffectVersion
    val cats            = "org.typelevel"         %% "cats-core"        % dependenciesVersion.catsVersion
    val sqliteJdbc      = "org.xerial"             % "sqlite-jdbc"      % dependenciesVersion.sqliteJdbcVersion
    val lightbendEmoji  = "com.lightbend"         %% "emoji"            % dependenciesVersion.lightbendEmojiVersion
    val pureConfig      = "com.github.pureconfig" %% "pureconfig"       % dependenciesVersion.pureConfig
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
    libs.munit,
    libs.munitCatsEffect,
    libs.lightbendEmoji,
    libs.telegramiumCore,
    libs.telegramiumHigh,
    libs.catsEffect
  )

  val ABarberoBotDependencies: Seq[ModuleID] = Seq(
    libs.munit,
    libs.munitCatsEffect,
    libs.telegramiumCore,
    libs.telegramiumHigh,
    libs.catsEffect
  )

  val RichardPHJBensonBotDependencies: Seq[ModuleID] = Seq(
    libs.munit,
    libs.munitCatsEffect,
    libs.lightbendEmoji,
    libs.telegramiumCore,
    libs.telegramiumHigh,
    libs.catsEffect
  )

  val XahBotDependencies: Seq[ModuleID] = Seq(
    libs.munit,
    libs.munitCatsEffect,
    libs.telegramiumCore,
    libs.telegramiumHigh,
    libs.catsEffect
  )

  val MainDependencies: Seq[ModuleID] = Seq(
    libs.catsEffect,
    libs.pureConfig
  )
}
