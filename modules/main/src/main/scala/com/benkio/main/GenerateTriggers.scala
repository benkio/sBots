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

import java.io.*

object GenerateTriggers extends IOApp {

  // Trigger file generation ////////////////////////////////////////////////////

  def generateTriggerFile(
      botModuleRelativeFolderPath: String,
      triggerFilename: String,
      triggers: List[ReplyBundleMessage[IO]]
  ): Resource[IO, Unit] = {
    val triggerFilesPath = new File(botModuleRelativeFolderPath).getCanonicalPath + s"/$triggerFilename"

    for
      triggersStringList <- Resource.eval(
        triggers.traverse(_.prettyPrint())
      )
      pw <- Resource.fromAutoCloseable(IO(new PrintWriter(triggerFilesPath)))
    yield pw.write(triggersStringList.mkString(""))
  }

  def run(args: List[String]): IO[ExitCode] =
    (for {
      _ <- generateTriggerFile(
        "../bots/aBarberoBot/",
        ABarberoBot.triggerFilename,
        ABarberoBot.messageRepliesData[IO]
      )
      $_ <- generateTriggerFile(
        "../bots/calandroBot/",
        CalandroBot.triggerFilename,
        CalandroBot.messageRepliesData[IO]
      )
      _ <- generateTriggerFile(
        "../bots/m0sconiBot/",
        M0sconiBot.triggerFilename,
        M0sconiBot.messageRepliesData[IO]
      )
      _ <- generateTriggerFile(
        "../bots/richardPHJBensonBot/",
        RichardPHJBensonBot.triggerFilename,
        RichardPHJBensonBot.messageRepliesData[IO]
      )
      _ <- generateTriggerFile(
        "../bots/youTuboAncheI0Bot/",
        YouTuboAncheI0Bot.triggerFilename,
        YouTuboAncheI0Bot.messageRepliesData[IO]
      )
    } yield ExitCode.Success).use(_.pure)

}
