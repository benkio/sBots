import sbt._

import Keys._

object Dependencies {

  lazy val versions = new {
    val caseInsensitive    = "1.3.0"
    val cats               = "2.9.0"
    val catsEffectTime     = "0.2.0"
    val catsEffectVersion  = "3.4.8"
    val cormorant          = "0.5.0-M1"
    val cron4sCore         = "0.6.1"
    val doobie             = "1.0.0-RC2"
    val emojiVersion       = "1.3.0"
    val fs2Core            = "3.6.1"
    val fs2IO              = "3.6.1"
    val flyway             = "9.15.1"
    val fs2Cron            = "0.8.0"
    val http4s             = "0.23.18"
    val logEffects         = "0.17.0"
    val mules              = "0.5.0"
    val mulesHttp4s        = "0.3.0"
    val munit              = "0.7.29"
    val munitCatsEffect    = "1.0.7"
    val pureConfig         = "0.17.2"
    val shapeless          = "2.3.10"
    val sqlite             = "3.41.0.0"
    val telegramiumVersion = "7.66.0"
    val vault              = "3.5.0"
  }

  lazy val libs = new {
    val caseInsensitive   = "org.typelevel"                 %% "case-insensitive"    % versions.caseInsensitive
    val catsCore          = "org.typelevel"                 %% "cats-core"           % versions.cats
    val catsEffect        = "org.typelevel"                 %% "cats-effect"         % versions.catsEffectVersion
    val catsEffectKernel  = "org.typelevel"                 %% "cats-effect-kernel"  % versions.catsEffectVersion
    val catsEffectTime    = "io.chrisdavenport"             %% "cats-effect-time"    % versions.catsEffectTime
    val catsFree          = "org.typelevel"                 %% "cats-free"           % versions.cats
    val catsKernel        = "org.typelevel"                 %% "cats-kernel"         % versions.cats
    val cormorantCore     = "io.chrisdavenport"             %% "cormorant-core"      % versions.cormorant
    val cormorantGeneric  = "io.chrisdavenport"             %% "cormorant-generic"   % versions.cormorant
    val cormorantParser   = "io.chrisdavenport"             %% "cormorant-parser"    % versions.cormorant
    val cron4sCore        = "com.github.alonsodomin.cron4s" %% "cron4s-core"         % versions.cron4sCore
    val doobieCore        = "org.tpolecat"                  %% "doobie-core"         % versions.doobie
    val doobieFree        = "org.tpolecat"                  %% "doobie-free"         % versions.doobie
    val doobieMunit       = "org.tpolecat"                  %% "doobie-munit"        % versions.doobie
    val emoji             = "com.lightbend"                 %% "emoji"               % versions.emojiVersion
    val flyway            = "org.flywaydb"                   % "flyway-core"         % versions.flyway
    val fs2Core           = "co.fs2"                        %% "fs2-core"            % versions.fs2Core
    val fs2CronCore       = "eu.timepit"                    %% "fs2-cron-core"       % versions.fs2Cron
    val fs2CronCron4s     = "eu.timepit"                    %% "fs2-cron-cron4s"     % versions.fs2Cron
    val fs2IO             = "co.fs2"                        %% "fs2-io"              % versions.fs2IO
    val http4sClient      = "org.http4s"                    %% "http4s-client"       % versions.http4s
    val http4sCore        = "org.http4s"                    %% "http4s-core"         % versions.http4s
    val http4sDsl         = "org.http4s"                    %% "http4s-dsl"          % versions.http4s
    val http4sEmberClient = "org.http4s"                    %% "http4s-ember-client" % versions.http4s
    val http4sServer      = "org.http4s"                    %% "http4s-server"       % versions.http4s
    val logEffectsCore    = "io.laserdisc"                  %% "log-effect-core"     % versions.logEffects
    val logEffectsFs2     = "io.laserdisc"                  %% "log-effect-fs2"      % versions.logEffects
    val mules             = "io.chrisdavenport"             %% "mules"               % versions.mules
    val mulesHttp4s       = "io.chrisdavenport"             %% "mules-http4s"        % versions.mulesHttp4s
    val munit             = "org.scalameta"                 %% "munit"               % versions.munit % "it, test"
    val munitCatsEffect   = "org.typelevel"         %% "munit-cats-effect-3" % versions.munitCatsEffect % "it, test"
    val pureConfig        = "com.github.pureconfig" %% "pureconfig"          % versions.pureConfig
    val pureConfigCore    = "com.github.pureconfig" %% "pureconfig-core"     % versions.pureConfig
    val pureConfigGeneric = "com.github.pureconfig" %% "pureconfig-generic"  % versions.pureConfig
    val pureConfigGenericBase = "com.github.pureconfig" %% "pureconfig-generic-base" % versions.pureConfig
    val shapeless             = "com.chuusai"           %% "shapeless"               % versions.shapeless
    val sqlite                = "org.xerial"             % "sqlite-jdbc"             % versions.sqlite
    val telegramiumCore       = "io.github.apimorphism" %% "telegramium-core"        % versions.telegramiumVersion
    val telegramiumHigh       = "io.github.apimorphism" %% "telegramium-high"        % versions.telegramiumVersion
    val vault                 = "org.typelevel"         %% "vault"                   % versions.vault
  }

  private val CommonDependencies: Seq[ModuleID] = Seq(
    libs.catsCore,
    libs.catsEffect,
    libs.catsEffectKernel,
    libs.cormorantCore   % "test",
    libs.cormorantParser % "test",
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
      libs.doobieMunit % "test",
      libs.flyway      % "test",
      libs.fs2Core,
      libs.fs2CronCore,
      libs.fs2CronCron4s,
      libs.http4sClient,
      libs.http4sCore,
      libs.http4sDsl,
      libs.http4sEmberClient % "test",
      libs.http4sServer,
      libs.logEffectsCore,
      libs.logEffectsFs2 % "test",
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
      libs.sqlite,
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
    libs.telegramiumCore,
    libs.telegramiumHigh
  )

  val BotDBDependencies: Seq[ModuleID] = Seq(
    libs.catsCore,
    libs.catsEffect,
    libs.catsEffectKernel,
    libs.catsFree,
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
