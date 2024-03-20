import sbt.*

import Keys.*

object Dependencies {

  lazy val versions = new {
    val caseInsensitive    = "1.4.0"
    val cats               = "2.10.0"
    val catsEffectTime     = "0.2.1"
    val catsEffectVersion  = "3.5.4"
    val circe              = "0.14.6"
    val doobie             = "1.0.0-RC5"
    val fs2Core            = "3.10.0"
    val fs2IO              = "3.10.0"
    val flyway             = "10.10.0"
    val http4s             = "0.23.26"
    val littleTime         = "4.0.2"
    val logEffects         = "0.19.0"
    val logbackClassic     = "1.5.3"
    val logbackLogstash    = "7.4"
    val mules              = "0.7.0"
    val mulesHttp4s        = "0.4.0"
    val munit              = "1.0.0-M11"
    val munitCatsEffect    = "2.0.0-M4"
    val pureConfig         = "0.17.6"
    val shapeless          = "2.3.10"
    val scalatest          = "3.2.16"
    val sqlite             = "3.41.2.1"
    val telegramiumVersion = "8.71.0"
    val vault              = "3.5.0"
  }

  lazy val libs = new {
    val caseInsensitive   = "org.typelevel"       %% "case-insensitive"         % versions.caseInsensitive
    val catsCore          = "org.typelevel"       %% "cats-core"                % versions.cats
    val catsEffect        = "org.typelevel"       %% "cats-effect"              % versions.catsEffectVersion
    val catsEffectKernel  = "org.typelevel"       %% "cats-effect-kernel"       % versions.catsEffectVersion
    val catsEffectTime    = "io.chrisdavenport"   %% "cats-effect-time"         % versions.catsEffectTime
    val catsFree          = "org.typelevel"       %% "cats-free"                % versions.cats
    val catsKernel        = "org.typelevel"       %% "cats-kernel"              % versions.cats
    val circeCore         = "io.circe"            %% "circe-core"               % versions.circe
    val circeGeneric      = "io.circe"            %% "circe-generic"            % versions.circe
    val circeParser       = "io.circe"            %% "circe-parser"             % versions.circe
    val doobieCore        = "org.tpolecat"        %% "doobie-core"              % versions.doobie
    val doobieFree        = "org.tpolecat"        %% "doobie-free"              % versions.doobie
    val doobieMunit       = "org.tpolecat"        %% "doobie-munit"             % versions.doobie          % "test"
    val flyway            = "org.flywaydb"         % "flyway-core"              % versions.flyway
    val fs2Core           = "co.fs2"              %% "fs2-core"                 % versions.fs2Core
    val fs2IO             = "co.fs2"              %% "fs2-io"                   % versions.fs2IO
    val http4sClient      = "org.http4s"          %% "http4s-client"            % versions.http4s
    val http4sCore        = "org.http4s"          %% "http4s-core"              % versions.http4s
    val http4sDsl         = "org.http4s"          %% "http4s-dsl"               % versions.http4s
    val http4sEmberClient = "org.http4s"          %% "http4s-ember-client"      % versions.http4s
    val http4sServer      = "org.http4s"          %% "http4s-server"            % versions.http4s
    val littleTime        = "com.github.losizm"   %% "little-time"              % versions.littleTime
    val logEffectsCore    = "io.laserdisc"        %% "log-effect-core"          % versions.logEffects
    val logEffectsFs2     = "io.laserdisc"        %% "log-effect-fs2"           % versions.logEffects
    val logbackClassic    = "ch.qos.logback"       % "logback-classic"          % versions.logbackClassic  % Runtime
    val logbackLogstash   = "net.logstash.logback" % "logstash-logback-encoder" % versions.logbackLogstash % Runtime

    val mules           = "io.chrisdavenport"     %% "mules"             % versions.mules
    val mulesHttp4s     = "io.chrisdavenport"     %% "mules-http4s"      % versions.mulesHttp4s
    val munit           = "org.scalameta"         %% "munit"             % versions.munit           % "test"
    val munitCatsEffect = "org.typelevel"         %% "munit-cats-effect" % versions.munitCatsEffect % "test"
    val pureConfig      = "com.github.pureconfig" %% "pureconfig"        % versions.pureConfig
    val pureConfigCore  = "com.github.pureconfig" %% "pureconfig-core"   % versions.pureConfig
    val scalatest       = "org.scalatest"         %% "scalatest"         % versions.scalatest       % "test"
    val sqlite          = "org.xerial"             % "sqlite-jdbc"       % versions.sqlite
    val telegramiumCore = "io.github.apimorphism" %% "telegramium-core"  % versions.telegramiumVersion
    val telegramiumHigh = "io.github.apimorphism" %% "telegramium-high"  % versions.telegramiumVersion
    val vault           = "org.typelevel"         %% "vault"             % versions.vault
  }

  private val CommonDependencies: Seq[ModuleID] = Seq(
    libs.catsCore,
    libs.catsEffect,
    libs.catsEffectKernel,
    libs.circeParser,
    libs.fs2IO,
    libs.http4sClient,
    libs.http4sCore,
    libs.http4sEmberClient,
    libs.http4sServer,
    libs.logbackClassic,
    libs.logbackLogstash,
    libs.logEffectsCore,
    libs.logEffectsFs2,
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
      libs.doobieCore,
      libs.doobieFree,
      libs.doobieMunit,
      libs.flyway % "test",
      libs.fs2Core,
      libs.http4sDsl,
      libs.littleTime,
      libs.mules,
      libs.mulesHttp4s,
      libs.pureConfigCore,
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
        libs.pureConfigCore
      )

  val BotDBDependencies: Seq[ModuleID] = Seq(
    libs.catsCore,
    libs.catsEffect,
    libs.catsEffectKernel,
    libs.catsFree,
    libs.circeCore,
    libs.circeParser,
    libs.doobieCore,
    libs.doobieFree,
    libs.flyway,
    libs.logbackClassic,
    libs.logbackLogstash,
    libs.logEffectsCore,
    libs.logEffectsFs2,
    libs.munit,
    libs.pureConfigCore,
  )

  val IntegrationDependencies: Seq[ModuleID] =
    Seq(
      libs.munitCatsEffect,
      libs.munit,
      libs.doobieMunit,
      libs.scalatest,
      libs.catsEffect        % "test",
      libs.catsCore          % "test",
      libs.doobieCore        % "test",
      libs.telegramiumCore   % "test",
      libs.http4sEmberClient % "test",
      libs.logbackClassic,
      libs.logbackLogstash,
      libs.logEffectsCore % "test",
      libs.flyway         % "test"
    )
}
