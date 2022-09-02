package com.benkio.calandrobot

import com.benkio.telegrambotinfrastructure.DBFixture
import munit.CatsEffectSuite

import cats.effect.IO
import cats.effect.Resource
import cats.implicits._
import com.benkio.telegrambotinfrastructure.model.MediaFile
import doobie.implicits._

class ITDBSpec extends CatsEffectSuite with DBFixture {

  import CalandroBot._

  // File Reference Check

  databaseFixture.test(
    "messageRepliesData should never raise an exception when try to open the file in resounces"
  ) { connectionResourceAccess =>
    val transactor = connectionResourceAccess._3
    val resourceAssert = for {
      dbResourceAccess <- connectionResourceAccess._2
      files            <- Resource.pure(messageRepliesData[IO].flatMap(_.mediafiles))
      checks <- Resource.eval(
        files
          .traverse((file: MediaFile) =>
            dbResourceAccess
              .getUrlByName(file.filename)
              .unique
              .transact(transactor)
              .map { case (dbFilename, _) => file.filename == dbFilename }
          )
      )
    } yield checks.foldLeft(true)(_ && _)

    resourceAssert.use(IO.pure).assert
  }

  databaseFixture.test(
    "commandRepliesData should never raise an exception when try to open the file in resounces"
  ) { connectionResourceAccess =>
    val transactor = connectionResourceAccess._3
    val resourceAssert = for {
      dbResourceAccess <- connectionResourceAccess._2
      files            <- Resource.pure(commandRepliesData[IO].flatMap(_.mediafiles))
      checks <- Resource.eval(
        files
          .traverse((file: MediaFile) =>
            dbResourceAccess
              .getUrlByName(file.filename)
              .unique
              .transact(transactor)
              .map { case (dbFilename, _) => file.filename == dbFilename }
          )
      )
    } yield checks.foldLeft(true)(_ && _)

    resourceAssert.use(IO.pure).assert
  }
}
