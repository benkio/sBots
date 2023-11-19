package com.benkio.integration.integrationmunit.xahleebot

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

import cats.effect._
import cats.implicits._
import com.benkio.telegrambotinfrastructure.model.MediaFile
import doobie.implicits._
import com.benkio.telegrambotinfrastructure.resources.db.DBLayer

class ITDBSpec extends CatsEffectSuite with DBFixture {

  val botName: String               = "botname"
  given api: Api[IO] = new Api[IO] {
    def execute[Res](method: Method[Res]): IO[Res] = IO(???)
  }
  val emptyDBLayer: DBLayer[IO]     = DBLayerMock.mock(botName)
  val resourceAccessMock = new ResourceAccessMock(List.empty)
  val emptyBackgroundJobManager: BackgroundJobManager[IO] = BackgroundJobManager(
    dbSubscription = emptyDBLayer.dbSubscription,
    dbShow = emptyDBLayer.dbShow,
    resourceAccess = resourceAccessMock,
    botName = botName
  ).unsafeRunSync()

  // File Reference Check

  databaseFixture.test(
    "commandRepliesData should never raise an exception when try to open the file in resounces"
  ) { fixture =>
    val transactor = fixture.transactor
    val resourceAssert = for {
      resourceDBLayer <- fixture.resourceDBLayer
      files <- Resource.eval(
        CommandRepliesData
          .values[IO](
            botName = botName,
            dbLayer = resourceDBLayer,
            backgroundJobManager = emptyBackgroundJobManager,
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
    val listPath      = new File(".").getCanonicalPath + "/xah_list.json"
    val jsonContent   = Source.fromFile(listPath).getLines().mkString("\n")
    val json : Either[io.circe.Error, List[String]] = decode[List[MediaFileSource]](jsonContent).map(_.map(_.filename))

    val resourceAssert = for {
      resourceDBLayer <- fixture.resourceDBLayer
      mediaFiles <- Resource.eval(
        CommandRepliesData
          .values[IO](
            botName = botName,
            dbLayer = resourceDBLayer,
            backgroundJobManager = emptyBackgroundJobManager,
          )
          .flatTraverse((r: ReplyBundle[IO]) => ReplyBundle.getMediaFiles[IO](r))
      )
      checks <- Resource.pure(
        mediaFiles
          .map((mediaFile: MediaFile) =>
            json.fold(
              e => fail("test failed", e),
              jsonMediaFileSources =>{
                val result = jsonMediaFileSources.exists((mediaFilenameSource: String) => mediaFilenameSource == mediaFile.filename)
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
