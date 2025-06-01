package com.benkio.integration.integrationmunit.M0sconi

import cats.effect.IO
import cats.implicits.*
import com.benkio.integration.DBFixture
import com.benkio.m0sconibot.M0sconiBot
import com.benkio.telegrambotinfrastructure.model.reply.MediaFile
import com.benkio.telegrambotinfrastructure.model.reply.ReplyBundle
import com.benkio.telegrambotinfrastructure.resources.db.DBMedia
import doobie.implicits.*
import munit.CatsEffectSuite

class ITDBSpec extends CatsEffectSuite with DBFixture {

  import M0sconiBot.*

  // File Reference Check

  databaseFixture.test(
    "messageRepliesAudioData should never raise an exception when try to open the file in resounces"
  ) { fixture =>
    val transactor = fixture.transactor
    val testAssert = for {
      mp3s   <- messageRepliesAudioData[IO].flatTraverse((r: ReplyBundle[IO]) => ReplyBundle.getMediaFiles[IO](r))
      checks <-
        mp3s
          .traverse((mp3: MediaFile) =>
            DBMedia
              .getMediaQueryByName(mp3.filename)
              .unique
              .transact(transactor)
              .onError { case _ => IO.println("[ERROR] mp3 missing from the DB: " + mp3) }
              .attempt
              .map(_.isRight)
          )
    } yield checks.foldLeft(true)(_ && _)

    testAssert.assert
  }

}
