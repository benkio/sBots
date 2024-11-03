package com.benkio.main

import com.benkio.richardphjbensonbot.RichardPHJBensonBot
import com.benkio.m0sconibot.M0sconiBot
import com.benkio.calandrobot.CalandroBot
import com.benkio.abarberobot.ABarberoBot
import com.benkio.telegrambotinfrastructure.model.ReplyBundle
import cats.effect.Resource
import com.benkio.telegrambotinfrastructure.model.ReplyBundleMessage
import com.benkio.youtuboanchei0bot.YouTuboAncheI0Bot
import cats.effect.ExitCode
import cats.implicits._
import java.io._
import cats.effect.IO

import cats.effect.IOApp

object GenerateTriggers extends IOApp {

  // Trigger file generation ////////////////////////////////////////////////////

  // TODO: logic to generate the triggerFilename file starting from the list of ReplyBundleMessage
  def generateTriggerFile(
      botModuleRelativeFolderPath: String,
      triggerFilename: String,
      triggers: List[ReplyBundleMessage[IO]]
  ): Resource[IO, Unit] = {
    val triggerFilesPath = new File(botModuleRelativeFolderPath).getCanonicalPath + s"/$triggerFilename"

    for
      triggersStringList <- Resource.eval(
        triggers.traverse(ReplyBundle.prettyPrint)
      )
      pw <- Resource.fromAutoCloseable(IO(new PrintWriter(triggerFilesPath)))
    yield pw.write(triggersStringList.mkString(""))
  }

  def run(args: List[String]): IO[ExitCode] =
    (for {
      _ <- generateTriggerFile(
        "../aBarberoBot/",
        ABarberoBot.triggerFilename,
        ABarberoBot.messageRepliesData[IO]
      )
      $_ <- generateTriggerFile(
        "../calandroBot/",
        CalandroBot.triggerFilename,
        CalandroBot.messageRepliesData[IO]
      )
      _ <- generateTriggerFile(
        "../m0sconiBot/",
        M0sconiBot.triggerFilename,
        M0sconiBot.messageRepliesData[IO]
      )
      _ <- generateTriggerFile(
        "../richardPHJBensonBot/",
        RichardPHJBensonBot.triggerFilename,
        RichardPHJBensonBot.messageRepliesData[IO]
      )
      _ <- generateTriggerFile(
        "../youTuboAncheI0Bot/",
        YouTuboAncheI0Bot.triggerFilename,
        YouTuboAncheI0Bot.messageRepliesData[IO]
      )
    } yield ExitCode.Success).use(_.pure)

}
