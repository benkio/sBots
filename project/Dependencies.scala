import sbt._

import Keys._

object Dependencies {

  lazy val versions = new {
    val catsEffectVersion     = "3.4.1"
    val catsEffectTime        = "0.2.0"
    val cats                  = "2.9.0"
    val caseInsensitive       = "1.3.0"
    val cormorant             = "0.5.0-M1"
    val cron4sCore            = "0.6.1"
    val doobie                = "1.0.0-RC2"
    val flyway                = "9.8.3"
    val fs2Core               = "3.4.0"
    val fs2Cron               = "0.7.2"
    val fs2IO                 = "3.3.0"
    val http4s                = "0.23.16"
    val lightbendEmojiVersion = "1.3.0"
    val logEffects            = "0.16.4"
    val mules                 = "0.5.0-M1"
    val mulesHttp4s           = "0.3.0-M1"
    val munit                 = "0.7.29"
    val munitCatsEffect       = "1.0.7"
    val pureConfig            = "0.17.2"
    val shapeless             = "2.3.10"
    val sqlite                = "3.40.0.0"
    val telegramiumVersion    = "7.63.0"
    val vault                 = "3.3.0"
  }

  lazy val libs = new {
    val catsCore              = "org.typelevel"                 %% "cats-core"               % versions.cats
    val catsKernel            = "org.typelevel"                 %% "cats-kernel"             % versions.cats
    val catsFree              = "org.typelevel"                 %% "cats-free"               % versions.cats
    val catsEffect            = "org.typelevel"                 %% "cats-effect"             % versions.catsEffectVersion
    val catsEffectKernel      = "org.typelevel"                 %% "cats-effect-kernel"      % versions.catsEffectVersion
    val catsEffectTime        = "io.chrisdavenport"             %% "cats-effect-time"        % versions.catsEffectTime
    val caseInsensitive       = "org.typelevel"                 %% "case-insensitive"        % versions.caseInsensitive
    val cormorantCore         = "io.chrisdavenport"             %% "cormorant-core"          % versions.cormorant
    val cormorantGeneric      = "io.chrisdavenport"             %% "cormorant-generic"       % versions.cormorant
    val cormorantParser       = "io.chrisdavenport"             %% "cormorant-parser"        % versions.cormorant
    val cron4sCore            = "com.github.alonsodomin.cron4s" %% "cron4s-core"             % versions.cron4sCore
    val doobieCore            = "org.tpolecat"                  %% "doobie-core"             % versions.doobie
    val doobieFree            = "org.tpolecat"                  %% "doobie-free"             % versions.doobie
    val doobieMunit           = "org.tpolecat"                  %% "doobie-munit"            % versions.doobie
    val flyway                = "org.flywaydb"                   % "flyway-core"             % versions.flyway
    val fs2CronCron4s         = "eu.timepit"                    %% "fs2-cron-cron4s"         % versions.fs2Cron
    val fs2CronCore           = "eu.timepit"                    %% "fs2-cron-core"           % versions.fs2Cron
    val fs2IO                 = "co.fs2"                        %% "fs2-io"                  % versions.fs2IO
    val fs2Core               = "co.fs2"                        %% "fs2-core"                % versions.fs2Core
    val http4sCore            = "org.http4s"                    %% "http4s-core"             % versions.http4s
    val http4sDsl             = "org.http4s"                    %% "http4s-dsl"              % versions.http4s
    val http4sClient          = "org.http4s"                    %% "http4s-client"           % versions.http4s
    val http4sServer          = "org.http4s"                    %% "http4s-server"           % versions.http4s
    val http4sEmberClient     = "org.http4s"                    %% "http4s-ember-client"     % versions.http4s
    val emoji        = "com.lightbend"                 %% "emoji"                   % versions.lightbendEmojiVersion
    val logEffectsCore        = "io.laserdisc"                  %% "log-effect-core"         % versions.logEffects
    val logEffectsFs2         = "io.laserdisc"                  %% "log-effect-fs2"          % versions.logEffects
    val mules                 = "io.chrisdavenport"             %% "mules"                   % versions.mules
    val mulesHttp4s           = "io.chrisdavenport"             %% "mules-http4s"            % versions.mulesHttp4s
    val munit                 = "org.scalameta"                 %% "munit"                   % versions.munit           % "it, test"
    val munitCatsEffect       = "org.typelevel"                 %% "munit-cats-effect-3"     % versions.munitCatsEffect % "it, test"
    val pureConfig            = "com.github.pureconfig"         %% "pureconfig"              % versions.pureConfig
    val pureConfigCore        = "com.github.pureconfig"         %% "pureconfig-core"         % versions.pureConfig
    val pureConfigGeneric     = "com.github.pureconfig"         %% "pureconfig-generic"      % versions.pureConfig
    val pureConfigGenericBase = "com.github.pureconfig"         %% "pureconfig-generic-base" % versions.pureConfig
    val sqlite                = "org.xerial"                     % "sqlite-jdbc"             % versions.sqlite
    val shapeless             = "com.chuusai"                   %% "shapeless"               % versions.shapeless
    val telegramiumCore       = "io.github.apimorphism"         %% "telegramium-core"        % versions.telegramiumVersion
    val telegramiumHigh       = "io.github.apimorphism"         %% "telegramium-high"        % versions.telegramiumVersion
    val vault                 = "org.typelevel"                 %% "vault"                   % versions.vault
  }

  private val CommonDependencies: Seq[ModuleID] = Seq(
    libs.catsCore,
    libs.catsEffect,
    libs.catsEffectKernel,
    libs.fs2IO,
    libs.http4sClient,
    libs.http4sCore,
    libs.http4sEmberClient,
    libs.http4sServer,
    libs.logEffectsCore,
    libs.logEffectsFs2,
    libs.munit,
    libs.munitCatsEffect,
    libs.telegramiumCore,
    libs.telegramiumHigh
  )

  val TelegramBotInfrastructureDependencies: Seq[ModuleID] =
    Seq(
      libs.caseInsensitive,
      libs.catsCore,
      libs.catsEffect,
      libs.catsEffectKernel,
      libs.catsEffectTime,
      libs.catsFree,
      libs.catsKernel,
      libs.cron4sCore,
      libs.doobieCore,
      libs.doobieFree,
      libs.fs2Core,
      libs.fs2CronCore,
      libs.fs2CronCron4s,
      libs.http4sClient,
      libs.http4sCore,
      libs.http4sDsl,
      libs.http4sServer,
      libs.logEffectsCore,
      libs.mules,
      libs.mulesHttp4s,
      libs.munit,
      libs.munitCatsEffect,
      libs.pureConfigCore,
      libs.pureConfigGeneric,
      libs.pureConfigGenericBase,
      libs.shapeless,
      libs.telegramiumCore,
      libs.telegramiumHigh,
      libs.vault
    )

  val CalandroBotDependencies: Seq[ModuleID] = CommonDependencies ++ Seq(
        libs.emoji
  )

  val ABarberoBotDependencies: Seq[ModuleID] = CommonDependencies

  val XahBotDependencies: Seq[ModuleID] = CommonDependencies

  val YoutuboAncheIoBotDependencies: Seq[ModuleID] = CommonDependencies ++ Seq(
        libs.emoji
  )

  val RichardPHJBensonBotDependencies: Seq[ModuleID] =
    CommonDependencies ++ Seq(
      libs.emoji
    )

  val MainDependencies: Seq[ModuleID] = Seq(
    libs.catsCore,
    libs.catsEffect,
    libs.catsEffectKernel,
    libs.fs2IO,
    libs.http4sClient,
    libs.http4sEmberClient,
    libs.http4sServer,
    libs.logEffectsCore,
    libs.logEffectsFs2,
    libs.pureConfigCore,
    libs.pureConfigGeneric,
    libs.pureConfigGenericBase,
    libs.shapeless,
    libs.telegramiumHigh
  )

  val BotDBDependencies: Seq[ModuleID] = Seq(
    libs.catsEffect,
    libs.catsEffectKernel,
    libs.catsFree,
    libs.catsCore,
    libs.cormorantCore,
    libs.cormorantGeneric,
    libs.cormorantParser,
    libs.doobieCore,
    libs.doobieFree,
    libs.flyway,
    libs.munit,
    libs.pureConfigCore,
    libs.pureConfigGeneric,
    libs.pureConfigGenericBase,
    libs.shapeless,
  )
}
