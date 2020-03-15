import Dependencies._

enablePlugins(JavaServerAppPackaging)

javacOptions ++= Seq("-source", "1.8", "-target", "1.8", "-Xlint")

lazy val root = (project in file(".")).
  settings(
    inThisBuild(List(

      organization := "benkio",
                  scalaVersion := "2.12.10",
                  version      := "0.1.0-SNAPSHOT"
                )),
    name := "RichardPHJBensonBot",
    libraryDependencies ++= Seq (
      scalaTest % Test,
      telegramBot4s,
      slf4s,
      logbackClassic,
      emojiManipulator,
      http4sClient,
      cats,
      catsEffect
    ),
    mainClass := Some("root.main")
  )

assemblyMergeStrategy in assembly := {
  case PathList("META-INF", xs @ _*) => MergeStrategy.discard
  case PathList("reference.conf") => MergeStrategy.concat
  case x => MergeStrategy.first
}
