package com.benkio.integration.calandrobot

import com.benkio.integration.DBFixture
import munit.CatsEffectSuite
import com.benkio.telegrambotinfrastructure.resources.db.DBMedia

import cats.effect.IO
import cats.effect.Resource
import cats.implicits._
import com.benkio.telegrambotinfrastructure.model.MediaFile
import doobie.implicits._

class ITDBSpec extends CatsEffectSuite with DBFixture {

  import com.benkio.calandrobot.CalandroBot._

  // File Reference Check

  databaseFixture.test(
    "messageRepliesData should never raise an exception when try to open the file in resounces"
  ) { fixture =>
    val transactor = fixture.transactor
    val resourceAssert = for {
      files <- Resource.pure(messageRepliesData[IO].flatMap(_.mediafiles))
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

  databaseFixture.test(
    "commandRepliesData should never raise an exception when try to open the file in resounces"
  ) { fixture =>
    val transactor = fixture.transactor
    val resourceAssert = for {

      files <- Resource.pure(commandRepliesData[IO].flatMap(_.mediafiles))
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
