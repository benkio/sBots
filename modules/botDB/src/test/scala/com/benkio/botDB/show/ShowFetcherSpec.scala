package com.benkio.botDB.show

import log.effect.LogLevels
import log.effect.fs2.SyncLogWriter.consoleLogUpToLevel
import log.effect.LogWriter
import java.nio.file.Files
import io.circe.parser.decode
import com.benkio.telegrambotinfrastructure.resources.db.DBShowData
import com.benkio.botDB.config.Config
import cats.implicits.*
import java.io.File
import cats.effect.IO

import scala.concurrent.duration.*

import munit._

class ShowFetcherSpec extends CatsEffectSuite {
  val outputFileName = "./delirioBaldazzi.json"

  // TODO: make the tests faster and remove this eventually
  override val munitIOTimeout = 2.minutes
  given log: LogWriter[IO]    = consoleLogUpToLevel(LogLevels.Info)

  test("generateShowJson should return a json if the input is valid") {
    if (sys.env.get("CI").contains("true")) cancel("Skipping test in CI")

    val showFetcher = ShowFetcher[IO]()
    for
      _ <- IO(File(outputFileName).delete())
      showSource <- ShowSource[IO](
        "https://www.youtube.com/playlist?list=PLO1i4nEhzCLYvR6gBHuZJS4z28he2S8yh",
        "testBot",
        outputFileName
      )
      result1 <- showFetcher.generateShowJson(showSource)
      result2 <- showFetcher.generateShowJson(showSource)
      _       <- IO(File(outputFileName).delete())
    yield {
      assert(result1.length > 5)
      assert(result1 == result2)
    }
  }

  test("the result json in should be parsable and urls should be unique") {
    if (sys.env.get("CI").contains("true")) cancel("Skipping test in CI")

    for
      cfg <- Config.loadConfig(None)
      inputs = cfg.showConfig.showSources.map(_.outputFilePath)
      dbShowDatas <- inputs.flatTraverse(p =>
        IO.fromEither(
          decode[List[DBShowData]](
            Files.readAllBytes(File(p).toPath()).map(_.toChar).mkString
          )
        )
      )
    yield assert(
      dbShowDatas
        .groupBy(_.show_url)
        .forall { case (u, dsds) => dsds.length == 1 }
    )
  }
}
