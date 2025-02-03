package com.benkio.botDB.show

import java.io.File
import cats.effect.IO
import scala.concurrent.duration.*

import munit._

class ShowFetcherSpec extends CatsEffectSuite {
  val outputFileName = "./delirioBaldazzi.json"

  // JUST FOR NOW, Remove it and make it fast
  override val munitIOTimeout = 2.minutes

  test("generateShowJson should return a json if the input is valid") {
    val showFetcher = ShowFetcher[IO]()
    for
      _ <- IO(File(outputFileName).delete())
      showSource <- ShowSource[IO]("https://www.youtube.com/playlist?list=PLO1i4nEhzCLYvR6gBHuZJS4z28he2S8yh", "testBot", outputFileName)
      result1 <- showFetcher.generateShowJson(showSource)
      result2 <- showFetcher.generateShowJson(showSource)
      _ <- IO(File(outputFileName).delete())
    yield {
      assert(result1.length > 5)
      assert(result1 == result2)
    }
  }
}
