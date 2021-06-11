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
    calandroBot,
    aBarberoBot,
    richardPHJBensonBot
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

lazy val aBarberoBot =
  Project("aBarberoBot", file("aBarberoBot"))
  .settings(Settings.settings: _*)
  .settings(Settings.ABarberoBotSettings: _*)
    .dependsOn(telegramBotInfrastructure)

lazy val richardPHJBensonBot =
  Project("richardPHJBensonBot", file("richardPHJBensonBot"))
  .settings(Settings.settings: _*)
  .settings(Settings.RichardPHJBensonBotSettings: _*)
  .dependsOn(telegramBotInfrastructure)


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
