package com.benkio.integration.integrationmunit.xahleebot

import cats.data.NonEmptyList
import cats.effect.*
import cats.implicits.*
import com.benkio.integration.DBFixture
import com.benkio.telegrambotinfrastructure.mocks.ApiMock.given
import com.benkio.telegrambotinfrastructure.mocks.DBLayerMock
import com.benkio.telegrambotinfrastructure.mocks.RepositoryMock
import com.benkio.telegrambotinfrastructure.model.media.MediaFileSource
import com.benkio.telegrambotinfrastructure.model.media.MediaResource.MediaResourceIFile
import com.benkio.telegrambotinfrastructure.model.reply.MediaFile
import com.benkio.telegrambotinfrastructure.model.reply.ReplyBundle
import com.benkio.telegrambotinfrastructure.model.SBotId
import com.benkio.telegrambotinfrastructure.model.SBotName
import com.benkio.telegrambotinfrastructure.repository.db.DBLayer
import com.benkio.telegrambotinfrastructure.repository.db.DBMedia
import com.benkio.telegrambotinfrastructure.BackgroundJobManager
import com.benkio.xahleebot.CommandRepliesData
import com.benkio.xahleebot.XahLeeBot
import doobie.implicits.*
import io.circe.parser.decode
import munit.CatsEffectSuite

import java.io.File
import scala.io.Source

class ITDBSpec extends CatsEffectSuite with DBFixture {

  val botName: SBotName                     = XahLeeBot.botName
  val botId: SBotId                         = XahLeeBot.botId
  val emptyDBLayer: DBLayer[IO]             = DBLayerMock.mock(botId)
  val mediaResource: MediaResourceIFile[IO] =
    MediaResourceIFile(
      "test mediafile"
    )
  val repositoryMock = RepositoryMock(_ => NonEmptyList.one(NonEmptyList.one(mediaResource)).pure[IO])
  val emptyBackgroundJobManager: Resource[IO, BackgroundJobManager[IO]] = Resource.eval(
    BackgroundJobManager(
      dbSubscription = emptyDBLayer.dbSubscription,
      dbShow = emptyDBLayer.dbShow,
      repository = repositoryMock,
      botId = botId
    )
  )

  // File Reference Check

  databaseFixture.test(
    "commandRepliesData should never raise an exception when try to open the file in resounces"
  ) { fixture =>
    val transactor     = fixture.transactor
    val resourceAssert = for {
      resourceDBLayer <- fixture.resourceDBLayer
      bjm             <- emptyBackgroundJobManager
      files           <- Resource.eval(
        CommandRepliesData
          .values[IO](
            botId = botId,
            botName = botName,
            dbLayer = resourceDBLayer,
            backgroundJobManager = bjm
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
              .onError { case _ => IO.println("[ERROR] file missing from the DB: " + file) }
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
    val listPath                                   = new File("./../bots/xahLeeBot").getCanonicalPath + "/xah_list.json"
    val jsonContent                                = Source.fromFile(listPath).getLines().mkString("\n")
    val json: Either[io.circe.Error, List[String]] = decode[List[MediaFileSource]](jsonContent).map(_.map(_.filename))

    val resourceAssert = for {
      resourceDBLayer <- fixture.resourceDBLayer
      bjm             <- emptyBackgroundJobManager
      mediaFiles      <- Resource.eval(
        CommandRepliesData
          .values[IO](
            botId = botId,
            botName = botName,
            dbLayer = resourceDBLayer,
            backgroundJobManager = bjm
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
                if !result then {
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
