package com.benkio.integration.integrationmunit.abarberobot

import cats.effect.IO
import cats.implicits.*
import com.benkio.integration.DBFixture
import com.benkio.telegrambotinfrastructure.model.reply.MediaFile
import com.benkio.telegrambotinfrastructure.model.reply.ReplyBundle
import com.benkio.telegrambotinfrastructure.repository.db.DBMedia
import doobie.implicits.*
import munit.CatsEffectSuite

class ITDBSpec extends CatsEffectSuite with DBFixture {

  import com.benkio.abarberobot.data.Audio
  import com.benkio.abarberobot.data.Gif

  // File Reference Check

  databaseFixture.test(
    "messageRepliesAudioData should never raise an exception when try to open the file in resounces"
  ) { fixture =>
    val transactor            = fixture.transactor
    val mp3s: List[MediaFile] = Audio.messageRepliesAudioData.flatMap(r => ReplyBundle.getMediaFiles(r))
    val testAssert            =
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
        .map(_.foldLeft(true)(_ && _))

    assertIO(testAssert, true)
  }

  databaseFixture.test("messageRepliesGifData should never raise an exception when try to open the file in resounces") {
    fixture =>
      val transactor = fixture.transactor
      val gifs       = Gif.messageRepliesGifData.flatMap(r => ReplyBundle.getMediaFiles(r))
      val testAssert =
        gifs
          .traverse((gif: MediaFile) =>
            DBMedia
              .getMediaQueryByName(gif.filename)
              .unique
              .transact(transactor)
              .onError { case _ => IO.println("[ERROR] gif missing from the DB: " + gif) }
              .attempt
              .map(_.isRight)
          )
          .map(_.foldLeft(true)(_ && _))

      assertIO(testAssert, true)
  }
}
