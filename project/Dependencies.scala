import sbt.*

import Keys.*

object Dependencies {

  private object groups {
    val apimorphism       = "io.github.apimorphism"
    val chrisdavenport    = "io.chrisdavenport"
    val circe             = "io.circe"
    val comcast           = "com.comcast"
    val cron4s            = "com.github.alonsodomin.cron4s"
    val dv8tion           = "net.dv8tion"
    val engio             = "net.engio"
    val flywaydb          = "org.flywaydb"
    val fs2               = "co.fs2"
    val googleApiClient   = "com.google.api-client"
    val googleApis        = "com.google.apis"
    val googleHttpClient  = "com.google.http-client"
    val googleOauthClient = "com.google.oauth-client"
    val http4s            = "org.http4s"
    val kittehIrc         = "org.kitteh.irc"
    val laserdisc         = "io.laserdisc"
    val loki4j            = "com.github.loki4j"
    val logstashLogback   = "net.logstash.logback"
    val pureconfig        = "com.github.pureconfig"
    val qosLogback        = "ch.qos.logback"
    val raquo             = "com.raquo"
    val scalaJs           = "org.scala-js"
    val scalacheck        = "org.scalacheck"
    val scalameta         = "org.scalameta"
    val scalatest         = "org.scalatest"
    val testcontainers    = "org.testcontainers"
    val testScope         = "test"
    val timepit           = "eu.timepit"
    val tpolecat          = "org.tpolecat"
    val tulz              = "app.tulz"
    val typelevel         = "org.typelevel"
    val wolfendale        = "io.github.wolfendale"
    val xerial            = "org.xerial"
  }

  object versions {
    val caseInsensitive       = "1.5.0"
    val cats                  = "2.13.0"
    val catsEffectTime        = "0.4.0"
    val catsEffectVersion     = "3.7.1"
    val circe                 = "0.14.17"
    val cron4s                = "0.8.2"
    val doobie                = "1.0.0-RC12"
    val flyway                = "13.13.0"
    val fs2Core               = "3.16.0"
    val fs2Cron               = "0.10.3"
    val fs2IO                 = "3.16.0"
    val googleApiClient       = "2.9.2"
    val googleHttpClient      = "2.2.0"
    val googleOauthClient     = "1.39.0"
    val googleYouTubeApi      = "v3-rev20261104-2.9.2"
    val http4s                = "0.23.39"
    val ip4s                  = "3.8.0"
    val jda                   = "6.7.0"
    val kitteh                = "9.0.0"
    val laminar               = "17.2.1"
    val logEffects            = "0.20.0"
    val logbackClassic        = "1.6.8"
    val logbackLogstash       = "9.0"
    val loki4j                = "2.1.0"
    val mbassador             = "1.3.2"
    val mules                 = "0.8.0"
    val mulesHttp4s           = "0.6.0"
    val munit                 = "1.3.1"
    val munitCatsEffect       = "2.2.1"
    val pureConfig            = "0.17.11"
    val scalacheck            = "1.20.0"
    val scalacheckEffectMunit = "2.1.0"
    val scalacheckGenRegexp   = "1.1.0"
    val scalajsDom            = "2.8.1"
    val scalatest             = "3.2.16"
    val shapeless             = "2.3.10"
    val sqlite                = "3.41.2.1"
    val telegramiumVersion    = "11.1004.0"
    val testcontainers        = "2.0.5"
    val tuplez                = "0.4.0"
    val vault                 = "3.7.0"
  }

  object libs {
    val caseInsensitive      = groups.typelevel       %% "case-insensitive"        % versions.caseInsensitive
    val catsCore             = groups.typelevel       %% "cats-core"               % versions.cats
    val catsEffect           = groups.typelevel       %% "cats-effect"             % versions.catsEffectVersion
    val catsEffectKernel     = groups.typelevel       %% "cats-effect-kernel"      % versions.catsEffectVersion
    val catsEffectStd        = groups.typelevel       %% "cats-effect-std"         % versions.catsEffectVersion
    val catsEffectTime       = groups.chrisdavenport  %% "cats-effect-time"        % versions.catsEffectTime
    val catsFree             = groups.typelevel       %% "cats-free"               % versions.cats
    val catsKernel           = groups.typelevel       %% "cats-kernel"             % versions.cats
    val circeCore            = groups.circe           %% "circe-core"              % versions.circe
    val circeGeneric         = groups.circe           %% "circe-generic"           % versions.circe
    val circeParser          = groups.circe           %% "circe-parser"            % versions.circe
    val cron4s               = groups.cron4s          %% "cron4s-core"             % versions.cron4s
    val doobieCore           = groups.tpolecat        %% "doobie-core"             % versions.doobie
    val doobieFree           = groups.tpolecat        %% "doobie-free"             % versions.doobie
    val doobieMunit          = groups.tpolecat        %% "doobie-munit"            % versions.doobie % groups.testScope
    val flyway               = groups.flywaydb         % "flyway-core"             % versions.flyway
    val fs2Core              = groups.fs2             %% "fs2-core"                % versions.fs2Core
    val fs2Cron              = groups.timepit         %% "fs2-cron-cron4s"         % versions.fs2Cron
    val fs2CronCalev         = groups.timepit         %% "fs2-cron-calev"          % versions.fs2Cron
    val fs2CronCore          = groups.timepit         %% "fs2-cron-core"           % versions.fs2Cron
    val fs2IO                = groups.fs2             %% "fs2-io"                  % versions.fs2IO
    val googleApiClient      = groups.googleApiClient  % "google-api-client"       % versions.googleApiClient
    val googleHttpClient     = groups.googleHttpClient % "google-http-client"      % versions.googleHttpClient
    val googleHttpClientGson = groups.googleHttpClient % "google-http-client-gson" % versions.googleHttpClient
    val googleOauthClient = groups.googleOauthClient % "google-oauth-client-jetty"   % versions.googleOauthClient
    val googleYouTubeApi  = groups.googleApis        % "google-api-services-youtube" % versions.googleYouTubeApi
    val http4sCirce       = groups.http4s           %% "http4s-circe"                % versions.http4s
    val http4sClient      = groups.http4s           %% "http4s-client"               % versions.http4s
    val http4sCore        = groups.http4s           %% "http4s-core"                 % versions.http4s
    val http4sDsl         = groups.http4s           %% "http4s-dsl"                  % versions.http4s
    val http4sEmberClient = groups.http4s           %% "http4s-ember-client"         % versions.http4s
    val http4sEmberServer = groups.http4s           %% "http4s-ember-server"         % versions.http4s
    val http4sServer      = groups.http4s           %% "http4s-server"               % versions.http4s
    val ip4sCore          = groups.comcast          %% "ip4s-core"                   % versions.ip4s
    val jda               = groups.dv8tion           % "JDA"                         % versions.jda
    val kitteh            = groups.kittehIrc         % "client-lib"                  % versions.kitteh
    val logEffectsCore    = groups.laserdisc        %% "log-effect-core"             % versions.logEffects
    val logEffectsFs2     = groups.laserdisc        %% "log-effect-fs2"              % versions.logEffects
    val logbackClassic    = groups.qosLogback        % "logback-classic"             % versions.logbackClassic % Runtime
    val logbackLogstash = groups.logstashLogback % "logstash-logback-encoder" % versions.logbackLogstash % Runtime
    val loki4jLogback   = groups.loki4j          % "loki-logback-appender"    % versions.loki4j          % Runtime
    val mbassador       = groups.engio           % "mbassador"                % versions.mbassador
    val mules           = groups.chrisdavenport %% "mules"                    % versions.mules
    val mulesCaffeine   = groups.chrisdavenport %% "mules-caffeine"           % versions.mules
    val mulesHttp4s     = groups.chrisdavenport %% "mules-http4s"             % versions.mulesHttp4s
    val munit            = groups.scalameta  %% "munit"             % versions.munit           % groups.testScope
    val munitCatsEffect  = groups.typelevel  %% "munit-cats-effect" % versions.munitCatsEffect % groups.testScope
    val munitScalacheck  = groups.scalameta  %% "munit-scalacheck"  % versions.munit           % groups.testScope
    val pureConfig       = groups.pureconfig %% "pureconfig"        % versions.pureConfig
    val pureConfigCore   = groups.pureconfig %% "pureconfig-core"   % versions.pureConfig
    val pureConfigCron4s = groups.pureconfig %% "pureconfig-cron4s" % versions.pureConfig
    val pureConfigHttp4s = groups.pureconfig %% "pureconfig-http4s" % versions.pureConfig
    val scalacheck       = groups.scalacheck %% "scalacheck"        % versions.scalacheck      % Test
    val scalacheckEffectMunit = groups.typelevel  %% "scalacheck-effect-munit" % versions.scalacheckEffectMunit % Test
    val scalacheckGenRegexp   = groups.wolfendale %% "scalacheck-gen-regexp"   % versions.scalacheckGenRegexp   % Test
    val scalatest       = groups.scalatest     %% "scalatest"        % versions.scalatest % groups.testScope
    val sqlite          = groups.xerial         % "sqlite-jdbc"      % versions.sqlite
    val telegramiumCore = groups.apimorphism   %% "telegramium-core" % versions.telegramiumVersion
    val testcontainers  = groups.testcontainers % "testcontainers"   % versions.testcontainers
    val telegramiumHigh = groups.apimorphism   %% "telegramium-high" % versions.telegramiumVersion
    val vault           = groups.typelevel     %% "vault"            % versions.vault

    // ScalaJS-enabled projects already append the "_sjs1" cross-version suffix via `%%`.
    // The replies editor UI is Scala.js-only, so portability isn't needed here.
    val laminarSjs1         = groups.raquo   %% "laminar"           % versions.laminar
    val airstreamSjs1       = groups.raquo   %% "airstream"         % versions.laminar
    val tuplezFullLightSjs1 = groups.tulz    %% "tuplez-full-light" % versions.tuplez
    val scalajsDomSjs1      = groups.scalaJs %% "scalajs-dom"       % versions.scalajsDom
    val circeCoreSjs1       = groups.circe   %% "circe-core"        % versions.circe
    val circeGenericSjs1    = groups.circe   %% "circe-generic"     % versions.circe
    val circeParserSjs1     = groups.circe   %% "circe-parser"      % versions.circe
  }

  val ChatCoreDependencies: Seq[ModuleID] = Seq(
    libs.catsCore,
    libs.catsEffect,
    libs.catsEffectKernel,
    libs.catsEffectStd,
    libs.catsFree,
    libs.catsKernel,
    libs.caseInsensitive,
    libs.circeCore,
    libs.circeGeneric,
    libs.circeParser,
    libs.cron4s,
    libs.doobieCore,
    libs.doobieFree,
    libs.doobieMunit,
    libs.flyway % groups.testScope,
    libs.sqlite % groups.testScope,
    libs.fs2Core,
    libs.fs2Cron,
    libs.fs2CronCore,
    libs.http4sCirce,
    libs.http4sClient,
    libs.http4sCore,
    libs.http4sDsl,
    libs.http4sEmberClient % groups.testScope,
    libs.http4sEmberServer % groups.testScope,
    libs.logEffectsCore,
    libs.logEffectsFs2,
    libs.logbackClassic,
    libs.logbackLogstash,
    libs.mules,
    libs.mulesCaffeine,
    libs.mulesHttp4s,
    libs.munit,
    libs.munitCatsEffect,
    libs.munitScalacheck,
    libs.pureConfigCore,
    libs.scalacheck,
    libs.scalacheckEffectMunit,
    libs.scalacheckGenRegexp,
    libs.vault
  )

  val ChatTelegramAdapterDependencies: Seq[ModuleID] = Seq(
    libs.catsCore,
    libs.catsEffect,
    libs.catsEffectKernel,
    libs.catsEffectStd,
    libs.circeCore,
    libs.fs2Core,
    libs.fs2IO,
    libs.http4sClient,
    libs.http4sCore,
    libs.http4sEmberClient,
    libs.http4sServer,
    libs.logEffectsCore,
    libs.logbackClassic,
    libs.logbackLogstash,
    libs.munit,
    libs.munitCatsEffect,
    libs.telegramiumCore,
    libs.telegramiumHigh,
    libs.vault
  )

  val ChatDiscordAdapterDependencies: Seq[ModuleID] = Seq(
    libs.catsCore,
    libs.catsEffect,
    libs.catsEffectKernel,
    libs.catsEffectStd,
    libs.http4sClient,
    libs.jda,
    libs.logEffectsCore,
    libs.logbackClassic,
    libs.logbackLogstash,
    libs.munit,
    libs.munitCatsEffect,
    libs.pureConfigCore
  )

  val ChatIrcAdapterDependencies: Seq[ModuleID] = Seq(
    libs.catsCore,
    libs.catsEffect,
    libs.catsEffectKernel,
    libs.catsEffectStd,
    libs.http4sClient,
    libs.kitteh,
    libs.logEffectsCore,
    libs.logbackClassic,
    libs.logbackLogstash,
    libs.mbassador,
    libs.munit,
    libs.munitCatsEffect,
    libs.pureConfigCore
  )

  /** Shared dependencies for all bot projects. Use this in Settings.botProjectSettings. */
  val BotDependencies: Seq[ModuleID] = Seq(
    libs.catsCore,
    libs.catsEffect,
    libs.logbackClassic,
    libs.logbackLogstash,
    libs.munit,
    libs.munitCatsEffect
  )

  /** CalandroBot only needs logging and test deps at compile time. */
  val CalandroBotDependencies: Seq[ModuleID] = Seq(
    libs.logbackClassic,
    libs.logbackLogstash,
    libs.munit,
    libs.munitCatsEffect
  )

  val MainDependencies: Seq[ModuleID] = Seq(
    libs.catsCore,
    libs.catsEffect,
    libs.catsEffectKernel,
    libs.catsEffectStd,
    libs.circeCore,
    libs.circeParser,
    libs.cron4s,
    libs.doobieCore,
    libs.doobieFree,
    libs.doobieMunit,
    libs.fs2Core,
    libs.fs2Cron,
    libs.fs2CronCore,
    libs.fs2IO,
    libs.http4sClient,
    libs.http4sCore,
    libs.http4sEmberClient,
    libs.http4sServer,
    libs.logEffectsCore,
    libs.logEffectsFs2,
    libs.logbackClassic,
    libs.logbackLogstash,
    libs.loki4jLogback,
    libs.munit,
    libs.munitCatsEffect,
    libs.pureConfigCore,
    libs.pureConfigCron4s,
    libs.pureConfigHttp4s,
    libs.sqlite % Runtime,
    libs.vault
  )

  val BotDBDependencies: Seq[ModuleID] = Seq(
    libs.catsCore,
    libs.catsEffect,
    libs.catsEffectKernel,
    libs.catsKernel,
    libs.circeCore,
    libs.circeParser,
    libs.doobieCore,
    libs.doobieFree,
    libs.flyway,
    libs.googleApiClient,
    libs.googleHttpClient,
    libs.googleHttpClientGson,
    libs.googleOauthClient % Runtime,
    libs.googleYouTubeApi,
    libs.http4sCore,
    libs.logEffectsCore,
    libs.logEffectsFs2,
    libs.logbackClassic,
    libs.logbackLogstash,
    libs.munit,
    libs.pureConfigCore,
    libs.sqlite
  )

  val RepliesEditorServerDependencies: Seq[ModuleID] = Seq(
    libs.caseInsensitive,
    libs.catsCore,
    libs.catsEffect,
    libs.catsEffectKernel,
    libs.catsKernel,
    libs.circeCore,
    libs.circeGeneric,
    libs.circeParser,
    libs.fs2IO,
    libs.http4sCirce,
    libs.http4sCore,
    libs.http4sDsl,
    libs.http4sEmberServer,
    libs.http4sServer,
    libs.ip4sCore,
    libs.logbackClassic,
    libs.logbackLogstash,
    libs.munit,
    libs.munitCatsEffect
  )

  val RepliesEditorUiDependencies: Def.Initialize[Seq[ModuleID]] =
    Def.setting(
      Seq(
        libs.laminarSjs1,
        libs.airstreamSjs1,
        libs.tuplezFullLightSjs1,
        libs.scalajsDomSjs1,
        libs.circeCoreSjs1,
        libs.circeGenericSjs1,
        libs.circeParserSjs1,
        libs.munit
      )
    )

  val IntegrationDependencies: Seq[ModuleID] =
    Seq(
      libs.catsCore   % groups.testScope,
      libs.catsEffect % groups.testScope,
      libs.cron4s     % groups.testScope,
      libs.doobieCore % groups.testScope,
      libs.doobieMunit,
      libs.flyway            % groups.testScope,
      libs.googleApiClient   % groups.testScope,
      libs.googleOauthClient % groups.testScope,
      libs.googleYouTubeApi  % groups.testScope,
      libs.http4sEmberClient % groups.testScope,
      libs.logEffectsCore    % groups.testScope,
      libs.logbackClassic,
      libs.logbackLogstash,
      libs.munit,
      libs.munitCatsEffect,
      libs.scalatest,
      libs.telegramiumCore % groups.testScope,
      libs.testcontainers  % groups.testScope
    )
}
