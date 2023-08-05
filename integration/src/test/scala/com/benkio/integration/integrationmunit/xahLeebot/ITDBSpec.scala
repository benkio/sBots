package com.benkio.integration.integrationmunit.xahleebot

import com.benkio.xahleebot.CommandRepliesData

import com.benkio.telegrambotinfrastructure.BackgroundJobManager
import com.benkio.telegrambotinfrastructure.resources.db.DBMedia
import com.benkio.telegrambotinfrastructure.mocks.DBLayerMock
import telegramium.bots.Message

import com.benkio.telegrambotinfrastructure.default.Actions.Action
import com.benkio.telegrambotinfrastructure.model.Reply
import com.benkio.integration.DBFixture
import munit.CatsEffectSuite

import cats.effect.IO
import cats.effect.Resource
import cats.implicits._
import com.benkio.telegrambotinfrastructure.model.MediaFile
import doobie.implicits._

class ITDBSpec extends CatsEffectSuite with DBFixture {

  implicit val noAction: Action[IO] = (_: Reply) => (_: Message) => IO.pure(List.empty[Message])
  val botName: String               = "botname"
  val emptyDBLayer                  = DBLayerMock.mock(botName)
  val emptyBackgroundJobManager = BackgroundJobManager(
    dbSubscription = emptyDBLayer.dbSubscription,
    dbShow = emptyDBLayer.dbShow,
    botName = botName
  ).unsafeRunSync()

  // File Reference Check

  databaseFixture.test(
    "commandRepliesData should never raise an exception when try to open the file in resounces"
  ) { fixture =>
    val transactor = fixture.transactor
    val resourceAssert = for {
      resourceDBLayer <- fixture.resourceDBLayer
      files <- Resource.pure(
        CommandRepliesData
          .values[IO](
            botName = botName,
            dbLayer = resourceDBLayer,
            backgroundJobManager = emptyBackgroundJobManager,
          )
          .flatMap(_.mediafiles)
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
}
