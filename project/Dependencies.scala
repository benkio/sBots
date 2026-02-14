import sbt.*

import Keys.*

object Dependencies {

  lazy val versions = new {
    val caseInsensitive       = "1.5.0"
    val cats                  = "2.13.0"
    val catsEffectTime        = "0.2.1"
    val catsEffectVersion     = "3.6.3"
    val circe                 = "0.14.15"
    val cron4s                = "0.8.2"
    val doobie                = "1.0.0-RC11"
    val flyway                = "12.0.1"
    val fs2Core               = "3.12.2"
    val fs2Cron               = "0.10.3"
    val fs2IO                 = "3.12.2"
    val googleApiClient       = "2.8.1"
    val googleHttpClient      = "2.1.0"
    val googleOauthClient     = "1.39.0"
    val googleYouTubeApi      = "v3-rev20251217-2.0.0"
    val http4s                = "0.23.33"
    val logEffects            = "0.19.9"
    val logbackClassic        = "1.5.29"
    val logbackLogstash       = "9.0"
    val mules                 = "0.7.0"
    val mulesHttp4s           = "0.4.0"
    val munit                 = "1.2.0"
    val munitCatsEffect       = "2.1.0"
    val pureConfig            = "0.17.10"
    val scalacheck            = "1.19.0"
    val scalacheckEffectMunit = "1.0.4"
    val scalacheckGenRegexp   = "1.1.0"
    val scalatest             = "3.2.16"
    val shapeless             = "2.3.10"
    val sqlite                = "3.41.2.1"
    val telegramiumVersion    = "10.904.0"
    val vault                 = "3.6.0"
  }

  lazy val libs = new {
    val caseInsensitive      = "org.typelevel"                 %% "case-insensitive"        % versions.caseInsensitive
    val catsCore             = "org.typelevel"                 %% "cats-core"               % versions.cats
    val catsEffect           = "org.typelevel"                 %% "cats-effect"             % versions.catsEffectVersion
    val catsEffectKernel     = "org.typelevel"                 %% "cats-effect-kernel"      % versions.catsEffectVersion
    val catsEffectTime       = "io.chrisdavenport"             %% "cats-effect-time"        % versions.catsEffectTime
    val catsFree             = "org.typelevel"                 %% "cats-free"               % versions.cats
    val catsKernel           = "org.typelevel"                 %% "cats-kernel"             % versions.cats
    val circeCore            = "io.circe"                      %% "circe-core"              % versions.circe
    val circeGeneric         = "io.circe"                      %% "circe-generic"           % versions.circe
    val circeParser          = "io.circe"                      %% "circe-parser"            % versions.circe
    val cron4s               = "com.github.alonsodomin.cron4s" %% "cron4s-core"             % versions.cron4s
    val doobieCore           = "org.tpolecat"                  %% "doobie-core"             % versions.doobie
    val doobieFree           = "org.tpolecat"                  %% "doobie-free"             % versions.doobie
    val doobieMunit          = "org.tpolecat"                  %% "doobie-munit"            % versions.doobie % "test"
    val flyway               = "org.flywaydb"                   % "flyway-core"             % versions.flyway
    val fs2Core              = "co.fs2"                        %% "fs2-core"                % versions.fs2Core
    val fs2Cron              = "eu.timepit"                    %% "fs2-cron-cron4s"         % versions.fs2Cron
    val fs2CronCore          = "eu.timepit"                    %% "fs2-cron-core"           % versions.fs2Cron
    val fs2CronCalev         = "eu.timepit"                    %% "fs2-cron-calev"          % versions.fs2Cron
    val fs2IO                = "co.fs2"                        %% "fs2-io"                  % versions.fs2IO
    val googleApiClient      = "com.google.api-client"          % "google-api-client"       % versions.googleApiClient
    val googleHttpClient     = "com.google.http-client"         % "google-http-client"      % versions.googleHttpClient
    val googleHttpClientGson = "com.google.http-client"         % "google-http-client-gson" % versions.googleHttpClient
    val googleOauthClient = "com.google.oauth-client" % "google-oauth-client-jetty"   % versions.googleOauthClient
    val googleYouTubeApi  = "com.google.apis"         % "google-api-services-youtube" % versions.googleYouTubeApi
    val http4sCirce       = "org.http4s"             %% "http4s-circe"                % versions.http4s
    val http4sClient      = "org.http4s"             %% "http4s-client"               % versions.http4s
    val http4sCore        = "org.http4s"             %% "http4s-core"                 % versions.http4s
    val http4sDsl         = "org.http4s"             %% "http4s-dsl"                  % versions.http4s
    val http4sEmberClient = "org.http4s"             %% "http4s-ember-client"         % versions.http4s
    val http4sServer      = "org.http4s"             %% "http4s-server"               % versions.http4s
    val http4sEmberServer = "org.http4s"             %% "http4s-ember-server"         % versions.http4s
    val logEffectsCore    = "io.laserdisc"           %% "log-effect-core"             % versions.logEffects
    val logEffectsFs2     = "io.laserdisc"           %% "log-effect-fs2"              % versions.logEffects
    val logbackClassic  = "ch.qos.logback"         % "logback-classic"          % versions.logbackClassic  % Runtime
    val logbackLogstash = "net.logstash.logback"   % "logstash-logback-encoder" % versions.logbackLogstash % Runtime
    val mules           = "io.chrisdavenport"     %% "mules"                    % versions.mules
    val mulesHttp4s     = "io.chrisdavenport"     %% "mules-http4s"             % versions.mulesHttp4s
    val munit           = "org.scalameta"         %% "munit"                    % versions.munit           % "test"
    val munitCatsEffect = "org.typelevel"         %% "munit-cats-effect"        % versions.munitCatsEffect % "test"
    val munitScalacheck = "org.scalameta"         %% "munit-scalacheck"         % versions.munit           % "test"
    val pureConfig      = "com.github.pureconfig" %% "pureconfig"               % versions.pureConfig
    val pureConfigCore  = "com.github.pureconfig" %% "pureconfig-core"          % versions.pureConfig
    val scalacheck      = "org.scalacheck"        %% "scalacheck"               % versions.scalacheck
    val scalacheckEffectMunit = "org.typelevel"         %% "scalacheck-effect-munit" % versions.scalacheckEffectMunit
    val scalacheckGenRegexp   = "io.github.wolfendale"  %% "scalacheck-gen-regexp"   % versions.scalacheckGenRegexp
    val scalatest             = "org.scalatest"         %% "scalatest"               % versions.scalatest % "test"
    val sqlite                = "org.xerial"             % "sqlite-jdbc"             % versions.sqlite
    val telegramiumCore       = "io.github.apimorphism" %% "telegramium-core"        % versions.telegramiumVersion
    val telegramiumHigh       = "io.github.apimorphism" %% "telegramium-high"        % versions.telegramiumVersion
    val vault                 = "org.typelevel"         %% "vault"                   % versions.vault
  }

  private val CommonDependencies: Seq[ModuleID] = Seq(
    libs.catsCore,
    libs.catsEffect,
    libs.catsEffectKernel,
    libs.circeParser,
    libs.fs2IO,
    libs.http4sCirce,
    libs.http4sClient,
    libs.http4sCore,
    libs.http4sEmberClient,
    libs.http4sServer,
    libs.logEffectsCore,
    libs.logEffectsFs2,
    libs.logbackClassic,
    libs.logbackLogstash,
    libs.munit,
    libs.munitCatsEffect,
    libs.telegramiumCore,
    libs.telegramiumHigh
  )

  val TelegramBotInfrastructureDependencies: Seq[ModuleID] =
    CommonDependencies ++ Seq(
      libs.caseInsensitive,
      libs.catsEffectTime,
      libs.catsFree,
      libs.catsKernel,
      libs.circeCore,
      libs.circeGeneric,
      libs.cron4s,
      libs.doobieCore,
      libs.doobieFree,
      libs.doobieMunit,
      libs.flyway % "test",
      libs.fs2Core,
      libs.fs2Cron,
      libs.fs2CronCalev,
      libs.fs2CronCore,
      libs.http4sDsl,
      libs.http4sEmberServer % "test",
      libs.mules,
      libs.mulesHttp4s,
      libs.munitScalacheck,
      libs.pureConfigCore,
      libs.scalacheck,
      libs.scalacheckEffectMunit,
      libs.scalacheckGenRegexp,
      libs.sqlite,
      libs.vault
    )

  val CalandroBotDependencies: Seq[ModuleID] = CommonDependencies

  val ABarberoBotDependencies: Seq[ModuleID] = CommonDependencies

  val XahLeeBotDependencies: Seq[ModuleID] = CommonDependencies

  val YouTuboAncheI0BotDependencies: Seq[ModuleID] = CommonDependencies

  val M0sconiBotDependencies: Seq[ModuleID] = CommonDependencies

  val RichardPHJBensonBotDependencies: Seq[ModuleID] = CommonDependencies

  val MainDependencies: Seq[ModuleID] =
    CommonDependencies ++
      Seq(
        libs.doobieMunit,
        libs.fs2Core,
        libs.pureConfigCore,
        libs.vault,
        libs.circeCore
      )

  val BotDBDependencies: Seq[ModuleID] = Seq(
    libs.catsCore,
    libs.catsEffect,
    libs.catsEffectKernel,
    libs.catsFree,
    libs.catsKernel,
    libs.circeCore,
    libs.circeParser,
    libs.doobieCore,
    libs.doobieFree,
    libs.flyway,
    libs.googleApiClient,
    libs.googleHttpClient,
    libs.googleHttpClientGson,
    libs.googleOauthClient,
    libs.googleYouTubeApi,
    libs.http4sCore,
    libs.logEffectsCore,
    libs.logEffectsFs2,
    libs.logbackClassic,
    libs.logbackLogstash,
    libs.munit,
    libs.pureConfigCore
  )

  val IntegrationDependencies: Seq[ModuleID] =
    Seq(
      libs.munitCatsEffect,
      libs.munit,
      libs.doobieMunit,
      libs.scalatest,
      libs.catsEffect        % "test",
      libs.catsCore          % "test",
      libs.cron4s            % "test",
      libs.doobieCore        % "test",
      libs.telegramiumCore   % "test",
      libs.http4sEmberClient % "test",
      libs.googleApiClient   % "test",
      libs.googleOauthClient % "test",
      libs.googleYouTubeApi  % "test",
      libs.logbackClassic,
      libs.logbackLogstash,
      libs.logEffectsCore % "test",
      libs.flyway         % "test"
    )
}
