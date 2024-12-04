package com.benkio.main

import cats.effect.ExitCode
import cats.effect.IO
import cats.effect.IOApp
import cats.effect.Resource
import cats.implicits._
import com.benkio.abarberobot.ABarberoBot
import com.benkio.calandrobot.CalandroBot
import com.benkio.m0sconibot.M0sconiBot
import com.benkio.richardphjbensonbot.RichardPHJBensonBot
import com.benkio.telegrambotinfrastructure.model.ReplyBundle
import com.benkio.telegrambotinfrastructure.model.ReplyBundleMessage
import com.benkio.youtuboanchei0bot.YouTuboAncheI0Bot
import java.io._

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
