import sbt._

import Keys._

object Dependencies {

  lazy val dependenciesVersion = new {
    val catsEffectVersion     = "3.3.12"
    val catsVersion           = "2.7.0"
    val lightbendEmojiVersion = "1.2.3"
    val logEffects            = "0.16.3"
    val munit                 = "0.7.29"
    val munitCatsEffect       = "1.0.7"
    val pureConfig            = "0.17.1"
    val sqliteJdbcVersion     = "3.36.0.3"
    val telegramiumVersion    = "7.60.0"
  }

  lazy val libs = new {
    val cats            = "org.typelevel" %% "cats-core"           % dependenciesVersion.catsVersion
    val catsEffect      = "org.typelevel" %% "cats-effect"         % dependenciesVersion.catsEffectVersion
    val lightbendEmoji  = "com.lightbend" %% "emoji"               % dependenciesVersion.lightbendEmojiVersion
    val logEffects      = "io.laserdisc"  %% "log-effect-fs2"      % dependenciesVersion.logEffects
    val munit           = "org.scalameta" %% "munit"               % dependenciesVersion.munit           % Test
    val munitCatsEffect = "org.typelevel" %% "munit-cats-effect-3" % dependenciesVersion.munitCatsEffect % "test"
    val pureConfig      = "com.github.pureconfig" %% "pureconfig"       % dependenciesVersion.pureConfig
    val sqliteJdbc      = "org.xerial"             % "sqlite-jdbc"      % dependenciesVersion.sqliteJdbcVersion
    val telegramiumCore = "io.github.apimorphism" %% "telegramium-core" % dependenciesVersion.telegramiumVersion
    val telegramiumHigh = "io.github.apimorphism" %% "telegramium-high" % dependenciesVersion.telegramiumVersion
  }

  val TelegramBotInfrastructureDependencies: Seq[ModuleID] = Seq(
    libs.cats,
    libs.catsEffect,
    libs.logEffects,
    libs.munit,
    libs.munitCatsEffect,
    libs.sqliteJdbc,
    libs.telegramiumCore,
    libs.telegramiumHigh
  )

  val CalandroBotDependencies: Seq[ModuleID] = Seq(
    libs.catsEffect,
    libs.lightbendEmoji,
    libs.logEffects,
    libs.munit,
    libs.munitCatsEffect,
    libs.telegramiumCore,
    libs.telegramiumHigh
  )

  val ABarberoBotDependencies: Seq[ModuleID] = Seq(
    libs.catsEffect,
    libs.logEffects,
    libs.munit,
    libs.munitCatsEffect,
    libs.telegramiumCore,
    libs.telegramiumHigh
  )

  val RichardPHJBensonBotDependencies: Seq[ModuleID] = Seq(
    libs.catsEffect,
    libs.lightbendEmoji,
    libs.logEffects,
    libs.munit,
    libs.munitCatsEffect,
    libs.telegramiumCore,
    libs.telegramiumHigh
  )

  val XahBotDependencies: Seq[ModuleID] = Seq(
    libs.catsEffect,
    libs.logEffects,
    libs.munit,
    libs.munitCatsEffect,
    libs.telegramiumCore,
    libs.telegramiumHigh
  )

  val MainDependencies: Seq[ModuleID] = Seq(
    libs.catsEffect,
    libs.logEffects,
    libs.pureConfig
  )
}
