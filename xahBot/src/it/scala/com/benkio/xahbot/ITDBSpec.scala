package com.benkio.xahbot

import com.benkio.telegrambotinfrastructure.resources.db.DBLayer
import com.benkio.telegrambotinfrastructure.DBFixture
import munit.CatsEffectSuite

import cats.effect.IO
import cats.effect.Resource
import cats.implicits._
import com.benkio.telegrambotinfrastructure.model.MediaFile
import doobie.implicits._

class ITDBSpec extends CatsEffectSuite with DBFixture {

  val emptyDBLayer = DBLayer[IO](null, null, null)
  // File Reference Check

  databaseFixture.test(
    "commandRepliesData should never raise an exception when try to open the file in resounces"
  ) { fixture =>
    val transactor = fixture.transactor
    val resourceAssert = for {
      resourceDBMedia <- fixture.resourceDBMedia
      files           <- Resource.pure(CommandRepliesData.values[IO](emptyDBLayer, "").flatMap(_.mediafiles))
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
