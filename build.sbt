import Settings._

name := "telegramBots"
organization := "com.benkio"
scalaVersion := "2.13.6"

// PROJECTS

lazy val global = project
  .in(file("."))
  .settings(Settings.settings: _*)
  .disablePlugins(AssemblyPlugin)
  .aggregate(
    telegramBotInfrastructure,
    calandroBot
   // multi2
  )

lazy val telegramBotInfrastructure =
  Project("telegramBotInfrastructure", file("telegramBotInfrastructure"))
  .settings(Settings.settings: _*)
  .settings(Settings.TelegramBotInfrastructureSettings: _*)
  .disablePlugins(AssemblyPlugin)

lazy val calandroBot =
  Project("calandroBot", file("calandroBot"))
  .settings(Settings.settings: _*)
  .settings(Settings.CalandroBotSettings: _*)
  .dependsOn(telegramBotInfrastructure)

// lazy val multi1 = project
//   .settings(
//     name := "multi1",
//     settings,
//     assemblySettings,
//     libraryDependencies ++= commonDependencies ++ Seq(
//       dependencies.monocleCore,
//       dependencies.monocleMacro
//     )
//   )
//   .dependsOn(
//     common
//   )

// lazy val multi2 = project
//   .settings(
//     name := "multi2",
//     settings,
//     assemblySettings,
//     libraryDependencies ++= commonDependencies ++ Seq(
//       dependencies.pureconfig
//     )
//   )
//   .dependsOn(
//     common
//   )

// // DEPENDENCIES

// lazy val dependencies =
//   new {
//     val logbackV        = "1.2.3"
//     val logstashV       = "4.11"
//     val scalaLoggingV   = "3.7.2"
//     val slf4jV          = "1.7.25"
//     val typesafeConfigV = "1.3.1"
//     val pureconfigV     = "0.8.0"
//     val monocleV        = "1.4.0"
//     val akkaV           = "2.5.6"
//     val scalatestV      = "3.0.4"
//     val scalacheckV     = "1.13.5"

//     val logback        = "ch.qos.logback"             % "logback-classic"          % logbackV
//     val logstash       = "net.logstash.logback"       % "logstash-logback-encoder" % logstashV
//     val scalaLogging   = "com.typesafe.scala-logging" %% "scala-logging"           % scalaLoggingV
//     val slf4j          = "org.slf4j"                  % "jcl-over-slf4j"           % slf4jV
//     val typesafeConfig = "com.typesafe"               % "config"                   % typesafeConfigV
//     val akka           = "com.typesafe.akka"          %% "akka-stream"             % akkaV
//     val monocleCore    = "com.github.julien-truffaut" %% "monocle-core"            % monocleV
//     val monocleMacro   = "com.github.julien-truffaut" %% "monocle-macro"           % monocleV
//     val pureconfig     = "com.github.pureconfig"      %% "pureconfig"              % pureconfigV
//     val scalatest      = "org.scalatest"              %% "scalatest"               % scalatestV
//     val scalacheck     = "org.scalacheck"             %% "scalacheck"              % scalacheckV
//   }

// lazy val commonDependencies = Seq(
//   dependencies.logback,
//   dependencies.logstash,
//   dependencies.scalaLogging,
//   dependencies.slf4j,
//   dependencies.typesafeConfig,
//   dependencies.akka,
//   dependencies.scalatest  % "test",
//   dependencies.scalacheck % "test"
// )

// // SETTINGS

// lazy val settings =
// commonSettings ++
// wartremoverSettings ++
// scalafmtSettings

// lazy val compilerOptions = Seq(
//   "-unchecked",
//   "-feature",
//   "-language:existentials",
//   "-language:higherKinds",
//   "-language:implicitConversions",
//   "-language:postfixOps",
//   "-deprecation",
//   "-encoding",
//   "utf8"
// )

// lazy val commonSettings = Seq(
//   scalacOptions ++= compilerOptions,
//   resolvers ++= Seq(
//     "Local Maven Repository" at "file://" + Path.userHome.absolutePath + "/.m2/repository",
//     Resolver.sonatypeRepo("releases"),
//     Resolver.sonatypeRepo("snapshots")
//   )
// )

lazy val assemblySettings = Seq(
  assembly / assemblyJarName := name.value + ".jar",
  assembly / assemblyMergeStrategy := {
    case PathList("META-INF", xs @ _*) => MergeStrategy.discard
    case "application.conf"            => MergeStrategy.concat
    case x =>
      val oldStrategy = (assembly / assemblyMergeStrategy).value
      oldStrategy(x)
  }
)
