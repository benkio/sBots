import sbt._

object Dependencies {

  lazy val scalaTestVersion             = "3.2.5"
  lazy val lightbendEmojiVersion        = "1.2.1"
  lazy val telegramiumVersion           = "4.51.0"
  lazy val catsEffectVersion            = "2.3.1"
  lazy val catsCoreVersion              = "2.4.2"

  lazy val scalaTest             = "org.scalatest"    %% "scalatest" % scalaTestVersion
  lazy val lightbendEmoji      = "com.lightbend" %% "emoji" % lightbendEmojiVersion
  lazy val telegramiumCore = "io.github.apimorphism" %% "telegramium-core" % telegramiumVersion
  lazy val telegramiumHigh = "io.github.apimorphism" %% "telegramium-high" % telegramiumVersion
  lazy val catsEffect = "org.typelevel" %% "cats-effect" % catsEffectVersion
  lazy val cats = "org.typelevel" %% "cats-core" % catsCoreVersion
}
