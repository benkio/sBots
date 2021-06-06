import sbt._

object Dependencies {

  lazy val scalaTestVersion             = "3.2.9"
  lazy val lightbendEmojiVersion        = "1.2.3"
  lazy val telegramiumVersion           = "4.52.2"
  lazy val catsEffectVersion            = "2.5.1"

  lazy val scalaTest             = "org.scalatest"    %% "scalatest" % scalaTestVersion
  lazy val lightbendEmoji      = "com.lightbend" %% "emoji" % lightbendEmojiVersion
  lazy val telegramiumCore = "io.github.apimorphism" %% "telegramium-core" % telegramiumVersion
  lazy val telegramiumHigh = "io.github.apimorphism" %% "telegramium-high" % telegramiumVersion
  lazy val catsEffect = "org.typelevel" %% "cats-effect" % catsEffectVersion
}
