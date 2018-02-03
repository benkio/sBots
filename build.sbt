import Dependencies._

lazy val root = (project in file(".")).
  settings(
    inThisBuild(List(
      organization := "com.example",
      scalaVersion := "2.12.4",
      version      := "0.1.0-SNAPSHOT"
    )),
    name := "CalandroBot",
    libraryDependencies ++= Seq (
      scalaTest % Test,
      "info.mukel" %% "telegrambot4s" % "3.0.12",
      "org.slf4j" % "slf4j-api" % "1.7.25",
      "org.slf4j" % "log4j-over-slf4j" % "1.7.1",
      "io.github.todokr" %% "emojipolation" % "0.2.0"
    ),
    herokuAppName in Compile := "CalandroBot",
    mainClass := Some("root.main")
  )

enablePlugins(JavaServerAppPackaging)
