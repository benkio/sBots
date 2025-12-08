package com.benkio.main

import cats.effect.ExitCode
import cats.effect.IO
import cats.effect.IOApp
import cats.effect.Resource
import cats.implicits.*
import com.benkio.abarberobot.ABarberoBot
import com.benkio.calandrobot.CalandroBot
import com.benkio.m0sconibot.M0sconiBot
import com.benkio.richardphjbensonbot.RichardPHJBensonBot
import com.benkio.telegrambotinfrastructure.model.reply.ReplyBundle
import com.benkio.telegrambotinfrastructure.model.reply.ReplyBundleMessage
import com.benkio.youtuboanchei0bot.YouTuboAncheI0Bot
import io.circe.syntax.*

import java.io.*

object GenerateTriggers extends IOApp {

  // Trigger file generation ////////////////////////////////////////////////////

  def generateTriggerFile(
      botModuleRelativeFolderPath: String,
      triggerFilename: String,
      triggers: List[ReplyBundleMessage]
  ): Resource[IO, Unit] = {
    val triggerFilesPath = new File(botModuleRelativeFolderPath).getCanonicalPath + s"/$triggerFilename"

    for {
      _ <- Resource.eval(IO.println(s"[GenerateTriggers] Generate $botModuleRelativeFolderPath Trigger file"))
      triggersStringList = triggers.map(_.prettyPrint())
      _  <- Resource.eval(IO.println(s"[GenerateTriggers] Generate $botModuleRelativeFolderPath done"))
      pw <- Resource.fromAutoCloseable(IO(new PrintWriter(triggerFilesPath)))
    } yield pw.write(triggersStringList.mkString(""))
  }

  // TODO: 785
  def generateTriggersJsonFile(
      botModuleRelativeFolderPath: String,
      triggerJsonFilename: String,
      triggers: List[ReplyBundleMessage]
  ): Resource[IO, Unit] = {
    val triggerFilesPath = new File(botModuleRelativeFolderPath).getCanonicalPath + s"/$triggerJsonFilename"

    for {
      _ <- Resource.eval(IO.println(s"[GenerateTriggers] Generate $botModuleRelativeFolderPath JSON Trigger file"))
      triggersJsonList = triggers.map(_.asJson)
      _  <- Resource.eval(IO.println(s"[GenerateTriggers] Generate $botModuleRelativeFolderPath done"))
      pw <- Resource.fromAutoCloseable(IO(new PrintWriter(triggerFilesPath)))
    } yield pw.write(triggersJsonList.mkString(""))
  }

  def run(args: List[String]): IO[ExitCode] =
    (for {
      _ <- generateTriggerFile(
        botModuleRelativeFolderPath = "../bots/aBarberoBot/",
        triggerFilename = ABarberoBot.triggerFilename,
        triggers = ABarberoBot.messageRepliesData
      )
      _ <- generateTriggerFile(
        botModuleRelativeFolderPath = "../bots/calandroBot/",
        triggerFilename = CalandroBot.triggerFilename,
        triggers = CalandroBot.messageRepliesData
      )
      // TODO: 785
      // _ <- generateTriggersJsonFile(
      //   botModuleRelativeFolderPath = "../bots/calandroBot/",
      //   triggerFilename = CalandroBot.triggerFilename,
      //   triggers = CalandroBot.messageRepliesData[IO]
      // )
      _ <- generateTriggerFile(
        botModuleRelativeFolderPath = "../bots/m0sconiBot/",
        triggerFilename = M0sconiBot.triggerFilename,
        triggers = M0sconiBot.messageRepliesData
      )
      _ <- generateTriggerFile(
        botModuleRelativeFolderPath = "../bots/richardPHJBensonBot/",
        triggerFilename = RichardPHJBensonBot.triggerFilename,
        triggers = RichardPHJBensonBot.messageRepliesData
      )
      _ <- generateTriggerFile(
        botModuleRelativeFolderPath = "../bots/youTuboAncheI0Bot/",
        triggerFilename = YouTuboAncheI0Bot.triggerFilename,
        triggers = YouTuboAncheI0Bot.messageRepliesData
      )
    } yield ExitCode.Success).use(_.pure)

}
