package com.benkio.botDB.show

import cats.data.NonEmptyList
import cats.effect.IO
import cats.implicits.*
import com.benkio.botDB.config.Config
import com.benkio.telegrambotinfrastructure.mocks.DBLayerMock
import com.benkio.telegrambotinfrastructure.mocks.ResourceAccessMock
import com.benkio.telegrambotinfrastructure.model.media.MediaResource.MediaResourceIFile
import com.benkio.telegrambotinfrastructure.resources.db.DBMediaData
import com.benkio.telegrambotinfrastructure.resources.db.DBShowData
import io.circe.parser.decode
import log.effect.fs2.SyncLogWriter.consoleLogUpToLevel
import log.effect.LogLevels
import log.effect.LogWriter
import munit.*

import java.io.File
import java.nio.file.Files
import scala.concurrent.duration.*

class ShowUpdaterSpec extends CatsEffectSuite {

  import com.benkio.botDB.TestData.*

  val outputFileName = "./test.json"
  val youTubeApiKey  = "youTubeApiKey"
  // TODO: make the tests faster and remove this eventually
  override val munitIOTimeout          = 1.minutes
  given log: LogWriter[IO]             = consoleLogUpToLevel(LogLevels.Info)
  val ciEnvVar                         = sys.env.get("CI")
  val mediaEntities: List[DBMediaData] = List(google, amazon, facebook)
  val mediaResource: MediaResourceIFile[IO] =
    MediaResourceIFile(
      "test mediafile"
    )
  val resourceAccessMock = new ResourceAccessMock(_ => NonEmptyList.one(NonEmptyList.one(mediaResource)).pure[IO])
  val dbLayerMock = DBLayerMock.mock(
    botName = "testBot",
    medias = mediaEntities
  )

  // test("updateShow should return a json if the input is valid") {
  //   assume(ciEnvVar.contains("false") || ciEnvVar.isEmpty)

  //   val showUpdater = ShowUpdater[IO](
  //     config = config,
  //     dbLayer = dbLayerMock,
  //     resourceAccess = resourceAccessMock,
  //     youTubeApiKey = youTubeApiKey
  //   )
  //   val test = for
  //     _ <- Resource.eval(IO(File(outputFileName).delete()))
  //     showSource = ShowSource(
  //       List("PL1hlX04-g75DGniSXtYRSlMBaroamq96d").map(YouTubeSource(_)),
  //       "testBot",
  //       outputFileName
  //     )
  //     result1 <- showUpdater.updateShow
  //     result2 <- showUpdater.updateShow
  //     _       <- Resource.eval(IO(File(outputFileName).delete()))
  //   yield {
  //     assert(result1.length == 3)
  //     assert(result1 == result2)
  //   }
  //   test.use(_.pure[IO])
  // }

  test("the result json in should be parsable and urls should be unique") {
    assume(ciEnvVar.contains("false") || ciEnvVar.isEmpty)

    for
      config <- Config.loadConfig(None)
      inputs = config.showConfig.showSources.map(_.outputFilePath)
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
