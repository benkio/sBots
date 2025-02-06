package com.benkio.botDB.show

import cats.effect.IO
import cats.implicits.*
import com.benkio.botDB.config.Config
import com.benkio.telegrambotinfrastructure.resources.db.DBShowData
import io.circe.parser.decode
import log.effect.fs2.SyncLogWriter.consoleLogUpToLevel
import log.effect.LogLevels
import log.effect.LogWriter
import munit.*

import java.io.File
import java.nio.file.Files
import scala.concurrent.duration.*

class ShowFetcherSpec extends CatsEffectSuite {
  val outputFileName = "./test.json"

  // TODO: make the tests faster and remove this eventually
  override val munitIOTimeout = 1.minutes
  given log: LogWriter[IO]    = consoleLogUpToLevel(LogLevels.Info)
  val ciEnvVar                = sys.env.get("CI")

  test("generateShowJson should return a json if the input is valid") {
    assume(ciEnvVar.contains("false") || ciEnvVar.isEmpty)

    val showFetcher = ShowFetcher[IO]()
    for
      _ <- IO(File(outputFileName).delete())
      showSource <- ShowSource[IO](
        List("https://www.youtube.com/playlist?list=PL1hlX04-g75DGniSXtYRSlMBaroamq96d"),
        "testBot",
        outputFileName
      )
      result1 <- showFetcher.generateShowJson(showSource)
      result2 <- showFetcher.generateShowJson(showSource)
      _       <- IO(File(outputFileName).delete())
    yield {
      assert(result1.length == 3)
      assert(result1 == result2)
    }
  }

  test("the result json in should be parsable and urls should be unique") {
    assume(ciEnvVar.contains("false") || ciEnvVar.isEmpty)

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
