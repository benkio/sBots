package com.benkio.replieseditor.server.load

import cats.effect.IO
import com.benkio.replieseditor.server.module.BotFiles

import java.nio.file.Files
import java.nio.file.Path
import scala.jdk.CollectionConverters.*

object BotScanner {
  def apply(repoRoot: Path): BotScanner = new BotScanner(repoRoot)
}

final class BotScanner(repoRoot: Path) {

  def scanBots(): IO[List[BotFiles]] = IO.blocking {
    val botsRoot = repoRoot.resolve("modules").resolve("bots")
    if !Files.isDirectory(botsRoot) then List.empty
    else {
      Files
        .list(botsRoot)
        .iterator()
        .asScala
        .toList
        .filter(Files.isDirectory(_))
        .flatMap { botDir =>
          val botName   = botDir.getFileName.toString
          val listFiles =
            Files
              .list(botDir)
              .iterator()
              .asScala
              .toList
              .filter(p => p.getFileName.toString.endsWith("_list.json"))
              .sortBy(_.getFileName.toString)

          listFiles.headOption.flatMap { listJson =>
            val botId       = listJson.getFileName.toString.stripSuffix("_list.json")
            val repliesJson =
              botDir.resolve("src").resolve("main").resolve("resources").resolve(s"${botId}_replies.json")
            val triggersTxt = botDir.resolve(s"${botId}_triggers.md")
            if Files.isRegularFile(repliesJson) && Files.isRegularFile(listJson)
            then Some(BotFiles(botId, botName, repliesJson, listJson, triggersTxt))
            else None
          }
        }
        .sortBy(_.botId)
    }
  }

  def findBot(botId: String): IO[Option[BotFiles]] =
    scanBots().map(_.find(_.botId == botId))
}
