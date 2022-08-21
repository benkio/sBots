import sbt._

import Keys._

object Dependencies {

  lazy val dependenciesVersion = new {
    val catsEffectVersion     = "3.3.14"
    val catsVersion           = "2.8.0"
    val flyway                = "9.1.6"
    val doobie                = "1.0.0-RC2"
    val lightbendEmojiVersion = "1.3.0"
    val logEffects            = "0.16.3"
    val munit                 = "0.7.29"
    val munitCatsEffect       = "1.0.7"
    val postgresVersion       = "42.4.1"
    val pureConfig            = "0.17.1"
    val testcontainers        = "0.40.10"
    val telegramiumVersion    = "7.61.0"
  }

  lazy val libs = new {
    val cats            = "org.typelevel" %% "cats-core"           % dependenciesVersion.catsVersion
    val catsEffect      = "org.typelevel" %% "cats-effect"         % dependenciesVersion.catsEffectVersion
    val doobie          = "org.tpolecat"  %% "doobie-core"         % dependenciesVersion.doobie
    val doobiePostgres  = "org.tpolecat"  %% "doobie-postgres"     % dependenciesVersion.doobie
    val flyway          = "org.flywaydb"   % "flyway-core"         % dependenciesVersion.flyway
    val lightbendEmoji  = "com.lightbend" %% "emoji"               % dependenciesVersion.lightbendEmojiVersion
    val logEffects      = "io.laserdisc"  %% "log-effect-fs2"      % dependenciesVersion.logEffects
    val munit           = "org.scalameta" %% "munit"               % dependenciesVersion.munit           % "it, test"
    val munitCatsEffect = "org.typelevel" %% "munit-cats-effect-3" % dependenciesVersion.munitCatsEffect % "it, test"
    val postgres        = "org.postgresql" % "postgresql"          % dependenciesVersion.postgresVersion
    val pureConfig      = "com.github.pureconfig" %% "pureconfig"       % dependenciesVersion.pureConfig
    val telegramiumCore = "io.github.apimorphism" %% "telegramium-core" % dependenciesVersion.telegramiumVersion
    val telegramiumHigh = "io.github.apimorphism" %% "telegramium-high" % dependenciesVersion.telegramiumVersion
    val testcontainersMunit =
      "com.dimafeng" %% "testcontainers-scala-munit" % dependenciesVersion.testcontainers % "it, test"
    val testContainersPostgres =
      "com.dimafeng" %% "testcontainers-scala-postgresql" % dependenciesVersion.testcontainers % "it, test"
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
    libs.doobie,
    libs.doobiePostgres,
    libs.lightbendEmoji,
    libs.logEffects,
    libs.munit,
    libs.munitCatsEffect,
    libs.pureConfig,
    libs.telegramiumCore,
    libs.telegramiumHigh,
    libs.testcontainersMunit
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
    libs.doobiePostgres,
    libs.flyway,
    libs.munit,
    libs.postgres,
    libs.pureConfig,
    libs.testcontainersMunit,
    libs.testContainersPostgres
  )
}
