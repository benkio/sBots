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
        triggerFilename = ABarberoBot.sBotConfig.triggerFilename,
        triggers = ABarberoBot.messageRepliesData
      )
      // TODO: Uncomment when migrating data to JSON and then remove
      // _ <- generateTriggersJsonFile(
      //   botModuleRelativeFolderPath = "../bots/aBarberoBot/",
      //   triggerJsonFilename = ABarberoBot.sBotConfig.triggerJsonFilename,
      //   triggers = ABarberoBot.messageRepliesData
      // )
      _ <- generateTriggerFile(
        botModuleRelativeFolderPath = "../bots/calandroBot/",
        triggerFilename = CalandroBot.sBotConfig.triggerFilename,
        triggers = CalandroBot.messageRepliesData
      )
      _ <- generateTriggersJsonFile(
        botModuleRelativeFolderPath = "../bots/calandroBot/",
        triggerJsonFilename = CalandroBot.sBotConfig.triggerJsonFilename,
        triggers = CalandroBot.messageRepliesData
      )
      _ <- generateTriggerFile(
        botModuleRelativeFolderPath = "../bots/m0sconiBot/",
        triggerFilename = M0sconiBot.sBotConfig.triggerFilename,
        triggers = M0sconiBot.messageRepliesData
      )
      // TODO: Uncomment when migrating data to JSON and then remove
      // _ <- generateTriggersJsonFile(
      //   botModuleRelativeFolderPath = "../bots/m0sconiBot/",
      //   triggerJsonFilename = M0sconiBot.sBotConfig.triggerJsonFilename,
      //   triggers = M0sconiBot.messageRepliesData
      // )
      _ <- generateTriggerFile(
        botModuleRelativeFolderPath = "../bots/richardPHJBensonBot/",
        triggerFilename = RichardPHJBensonBot.sBotConfig.triggerFilename,
        triggers = RichardPHJBensonBot.messageRepliesData
      )
      // TODO: Uncomment when migrating data to JSON and then remove
      // _ <- generateTriggersJsonFile(
      //   botModuleRelativeFolderPath = "../bots/richardPHJBensonBotBot/",
      //   triggerJsonFilename = RichardPHJBensonBotBot.sBotConfig.triggerJsonFilename,
      //   triggers = RichardPHJBensonBotBot.messageRepliesData
      // )
      _ <- generateTriggerFile(
        botModuleRelativeFolderPath = "../bots/youTuboAncheI0Bot/",
        triggerFilename = YouTuboAncheI0Bot.sBotConfig.triggerFilename,
        triggers = YouTuboAncheI0Bot.messageRepliesData
      )
      // TODO: Uncomment when migrating data to JSON and then remove
      // _ <- generateTriggersJsonFile(
      //   botModuleRelativeFolderPath = "../bots/youTuboAncheI0BotBot/",
      //   triggerJsonFilename = YouTuboAncheI0BotBot.sBotConfig.triggerJsonFilename,
      //   triggers = YouTuboAncheI0BotBot.messageRepliesData
      // )
    } yield ExitCode.Success).use(_.pure)

}
