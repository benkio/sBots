import Dependencies._

lazy val root = (project in file(".")).
  settings(
    inThisBuild(List(
                  organization := "com.example",
                  scalaVersion := "2.12.4",
                  version      := "0.1.0-SNAPSHOT"
                )),
    name := "DjJacobBot",
    libraryDependencies ++= Seq (
      scalaTest % Test,
      telegramBot4s,
      slf4s,
      logbackClassic,
      emojiManipulator
    ),
    herokuAppName in Compile := "DjJacobBot",
    mainClass := Some("root.main")
  )

enablePlugins(JavaServerAppPackaging)