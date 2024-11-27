package com.benkio.integration.integrationmunit.xahleebot

import com.benkio.telegrambotinfrastructure.mocks.ApiMock

import io.circe.parser.decode
import com.benkio.telegrambotinfrastructure.model.MediaFileSource
import scala.io.Source
import java.io.File

import com.benkio.telegrambotinfrastructure.model.ReplyBundle
import telegramium.bots.client.Method
import telegramium.bots.high.Api
import com.benkio.telegrambotinfrastructure.mocks.ResourceAccessMock
import com.benkio.xahleebot.CommandRepliesData

import com.benkio.telegrambotinfrastructure.BackgroundJobManager
import com.benkio.telegrambotinfrastructure.resources.db.DBMedia
import com.benkio.telegrambotinfrastructure.mocks.DBLayerMock

import com.benkio.integration.DBFixture
import munit.CatsEffectSuite

import cats.effect.*
import cats.implicits.*
import com.benkio.telegrambotinfrastructure.model.MediaFile
import doobie.implicits.*
import com.benkio.telegrambotinfrastructure.resources.db.DBLayer

class ITDBSpec extends CatsEffectSuite with DBFixture {

  val botName: String = "botname"
  val botPrefix: String = "xah"
  given api: Api[IO] = new ApiMock
  val emptyDBLayer: DBLayer[IO] = DBLayerMock.mock(botName)
  val resourceAccessMock        = new ResourceAccessMock(List.empty)
  val emptyBackgroundJobManager: Resource[IO, BackgroundJobManager[IO]] = Resource.eval(
    BackgroundJobManager(
      dbSubscription = emptyDBLayer.dbSubscription,
      dbShow = emptyDBLayer.dbShow,
      resourceAccess = resourceAccessMock,
      botName = botName
    )
  )

  // File Reference Check

  databaseFixture.test(
    "commandRepliesData should never raise an exception when try to open the file in resounces"
  ) { fixture =>
    val transactor = fixture.transactor
    val resourceAssert = for {
      resourceDBLayer <- fixture.resourceDBLayer
      bjm             <- emptyBackgroundJobManager
      files <- Resource.eval(
        CommandRepliesData
          .values[IO](
            botName = botName,
            botPrefix = botPrefix,
            dbLayer = resourceDBLayer,
            backgroundJobManager = bjm,
          )
          .flatTraverse((r: ReplyBundle[IO]) => ReplyBundle.getMediaFiles[IO](r))
      )
      checks <- Resource.eval(
        files
          .traverse((file: MediaFile) =>
            DBMedia
              .getMediaQueryByName(file.filename)
              .unique
              .transact(transactor)
              .onError(_ => IO.println(s"[ERROR] file missing from the DB: " + file))
              .attempt
              .map(_.isRight)
          )
      )
    } yield checks.foldLeft(true)(_ && _)

    resourceAssert.use(IO.pure).assert
  }

  // File json file check

  databaseFixture.test(
    "commandRepliesData random files should be contained in the jsons"
  ) { fixture =>
    val listPath                                   = new File(s"./../xahLeeBot").getCanonicalPath + "/xah_list.json"
    val jsonContent                                = Source.fromFile(listPath).getLines().mkString("\n")
    val json: Either[io.circe.Error, List[String]] = decode[List[MediaFileSource]](jsonContent).map(_.map(_.filename))

    val resourceAssert = for {
      resourceDBLayer <- fixture.resourceDBLayer
      bjm             <- emptyBackgroundJobManager
      mediaFiles <- Resource.eval(
        CommandRepliesData
          .values[IO](
            botName = botName,
            botPrefix = botPrefix,
            dbLayer = resourceDBLayer,
            backgroundJobManager = bjm,
          )
          .flatTraverse((r: ReplyBundle[IO]) => ReplyBundle.getMediaFiles[IO](r))
      )
      checks <- Resource.pure(
        mediaFiles
          .map((mediaFile: MediaFile) =>
            json.fold(
              e => fail("test failed", e),
              jsonMediaFileSources => {
                val result = jsonMediaFileSources.exists((mediaFilenameSource: String) =>
                  mediaFilenameSource == mediaFile.filename
                )
                if (!result) {
                  println(s"${mediaFile.filename} is not contained in the json file")
                }
                result
              }
            )
          )
      )
    } yield checks.foldLeft(true)(_ && _)

    resourceAssert.use(IO.pure).assert
  }
}
