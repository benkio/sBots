import sbt._

object Dependencies {

  lazy val scalaTestVersion             = "3.0.8"
  lazy val telegramBot4sVersion         = "3.0.16"
  lazy val slf4sVersion                 = "1.7.25"
  lazy val emojiManipulatorVersion      = "0.2.0"
  lazy val logbackClassicVersion        = "1.2.3"

  lazy val scalaTest             = "org.scalatest"    %% "scalatest" % scalaTestVersion
  lazy val telegramBot4s         = "info.mukel"       %% "telegrambot4s" % telegramBot4sVersion
  lazy val slf4s                 = "org.slf4s"        %% "slf4s-api" % slf4sVersion
  lazy val logbackClassic        = "ch.qos.logback"   % "logback-classic" % logbackClassicVersion
  lazy val emojiManipulator      = "io.github.todokr" %% "emojipolation" % emojiManipulatorVersion
}
