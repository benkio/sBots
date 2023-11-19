package com.benkio.integration.integrationmunit.calandrobot

import com.benkio.calandrobot.CalandroBot
import com.benkio.telegrambotinfrastructure.model.ReplyBundle
import com.benkio.integration.DBFixture
import munit.CatsEffectSuite
import com.benkio.telegrambotinfrastructure.resources.db.DBMedia

import cats.effect.IO
import cats.effect.Resource
import cats.implicits.*
import com.benkio.telegrambotinfrastructure.model.MediaFile
import doobie.implicits.*

class ITDBSpec extends CatsEffectSuite with DBFixture {

  import com.benkio.calandrobot.CalandroBot.*

  // File Reference Check

  databaseFixture.test(
    "messageRepliesData should never raise an exception when try to open the file in resounces"
  ) { fixture =>
    val transactor = fixture.transactor
    val testAssert = for {
      files <- messageRepliesData[IO].flatTraverse((r: ReplyBundle[IO]) => ReplyBundle.getMediaFiles[IO](r))
      checks <-
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
    } yield checks.foldLeft(true)(_ && _)

    testAssert.assert
  }

  databaseFixture.test(
    "commandRepliesData should never raise an exception when try to open the file in resounces"
  ) { fixture =>
    val transactor = fixture.transactor
    val testAssert = for {
      dbLayer <- fixture.resourceDBLayer
      files <- Resource.eval(commandRepliesData[IO](dbLayer).flatTraverse((r: ReplyBundle[IO]) => ReplyBundle.getMediaFiles[IO](r)))
      checks <-
        Resource.eval(
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

    testAssert.use(IO.pure).assert
  }
}
