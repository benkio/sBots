import Dependencies._

javacOptions ++= Seq("-source", "1.8", "-target", "1.8", "-Xlint")
resolvers += Resolver.typesafeIvyRepo("releases")

lazy val root = (project in file(".")).
  settings(
    inThisBuild(List(

      organization := "benkio",
                  scalaVersion := "2.13.5",
                  version      := "0.1.0-SNAPSHOT"
                )),
    name := "CalandroBot",
    libraryDependencies ++= Seq (
      scalaTest % Test,
      lightbendEmoji,
      telegramiumCore,
      telegramiumHigh,
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
