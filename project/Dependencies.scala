import sbt._

import Keys._

object Dependencies {

  lazy val dependenciesVersion = new {
    val catsEffectVersion     = "3.3.13"
    val catsVersion           = "2.8.0"
    val doobie                = "1.0.0-RC1"
    val flyway                = "7.2.0"
    val lightbendEmojiVersion = "1.2.3"
    val logEffects            = "0.16.3"
    val mysqlConnector        = "8.0.22"
    val munit                 = "0.7.29"
    val munitCatsEffect       = "1.0.7"
    val pureConfig            = "0.17.1"
    val testcontainers        = "0.40.9"
    val telegramiumVersion    = "7.61.0"
  }

  lazy val libs = new {
    val cats            = "org.typelevel" %% "cats-core"            % dependenciesVersion.catsVersion
    val catsEffect      = "org.typelevel" %% "cats-effect"          % dependenciesVersion.catsEffectVersion
    val doobie          = "org.tpolecat"  %% "doobie-core"          % dependenciesVersion.doobie
    val flyway          = "org.flywaydb"   % "flyway-core"          % dependenciesVersion.flyway
    val lightbendEmoji  = "com.lightbend" %% "emoji"                % dependenciesVersion.lightbendEmojiVersion
    val logEffects      = "io.laserdisc"  %% "log-effect-fs2"       % dependenciesVersion.logEffects
    val mysqlConnector  = "mysql"          % "mysql-connector-java" % dependenciesVersion.mysqlConnector
    val munit           = "org.scalameta" %% "munit"                % dependenciesVersion.munit           % "it, test"
    val munitCatsEffect = "org.typelevel" %% "munit-cats-effect-3"  % dependenciesVersion.munitCatsEffect % "it, test"
    val pureConfig      = "com.github.pureconfig" %% "pureconfig"       % dependenciesVersion.pureConfig
    val telegramiumCore = "io.github.apimorphism" %% "telegramium-core" % dependenciesVersion.telegramiumVersion
    val telegramiumHigh = "io.github.apimorphism" %% "telegramium-high" % dependenciesVersion.telegramiumVersion
    val testcontainersMunit =
      "com.dimafeng" %% "testcontainers-scala-munit" % dependenciesVersion.testcontainers % "it, test"
    val testcontainersCore =
      "com.dimafeng" %% "testcontainers-scala-core" % dependenciesVersion.testcontainers % "it, test"
  }

  val TelegramBotInfrastructureDependencies: Seq[ModuleID] = Seq(
    libs.cats,
    libs.catsEffect,
    libs.logEffects,
    libs.munit,
    libs.munitCatsEffect,
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

  val BotDBDependencies: Seq[ModuleID] = Seq(
    libs.doobie,
    libs.flyway,
    libs.munit,
    libs.mysqlConnector,
    libs.pureConfig,
    libs.testcontainersMunit
  )
}
