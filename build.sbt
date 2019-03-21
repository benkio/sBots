import Dependencies._

javacOptions ++= Seq("-source", "1.8", "-target", "1.8", "-Xlint")

lazy val root = (project in file(".")).
  settings(
    inThisBuild(List(
                  organization := "benkio",
                  scalaVersion := "2.12.8",
                  version      := "0.1.0-SNAPSHOT"
                )),
    name := "CalandroBot",
    libraryDependencies ++= Seq (
      scalaTest % Test,
      telegramBot4s,
      slf4s,
      logbackClassic,
      emojiManipulator,
      awsLambdaGiavaCore,
      awsLambdaGiavaEvents
    ),
    mainClass := Some("root.main")
  )

assemblyMergeStrategy in assembly := {
  case PathList("META-INF", xs @ _*) => MergeStrategy.discard
  case x => MergeStrategy.first
}