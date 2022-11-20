package com.benkio.xahbot

import com.benkio.telegrambotinfrastructure.BackgroundJobManager
import com.benkio.telegrambotinfrastructure.mocks.DBLayerMock
import telegramium.bots.Message

import com.benkio.telegrambotinfrastructure.default.Actions.Action
import com.benkio.telegrambotinfrastructure.model.Reply
import com.benkio.telegrambotinfrastructure.resources.ResourceAccess
import com.benkio.telegrambotinfrastructure.DBFixture
import munit.CatsEffectSuite

import cats.effect.IO
import cats.effect.Resource
import cats.implicits._
import com.benkio.telegrambotinfrastructure.model.MediaFile
import doobie.implicits._

class ITDBSpec extends CatsEffectSuite with DBFixture {

  implicit val noAction: Action[IO] = (_: Reply) => (_: Message) => IO.pure(List.empty[Message])
  val emptyDBLayer                  = DBLayerMock.mock()
  val botName: String               = "botname"
  val emptyBackgroundJobManager = BackgroundJobManager(
    dbSubscription = emptyDBLayer.dbSubscription,
    resourceAccess = ResourceAccess.fromResources[IO](),
    youtubeLinkSources = "",
    botName = botName
  ).unsafeRunSync()

  // File Reference Check

  databaseFixture.test(
    "commandRepliesData should never raise an exception when try to open the file in resounces"
  ) { fixture =>
    val transactor = fixture.transactor
    val resourceAssert = for {
      resourceDBMedia <- fixture.resourceDBLayer.map(_.dbMedia)
      files <- Resource.pure(
        CommandRepliesData
          .values[IO](
            resourceAccess = ResourceAccess.fromResources[IO](),
            botName = botName,
            backgroundJobManager = emptyBackgroundJobManager,
            linkSources = ""
          )
          .flatMap(_.mediafiles)
      )
      checks <- Resource.eval(
        files
          .traverse((file: MediaFile) =>
            resourceDBMedia
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
}
