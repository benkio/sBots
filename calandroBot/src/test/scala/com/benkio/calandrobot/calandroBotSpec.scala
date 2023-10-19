package com.benkio.calandrobot

import com.benkio.telegrambotinfrastructure.model.MediafileSource
import cats.Show
import cats.effect.IO
import cats.implicits._
import com.benkio.telegrambotinfrastructure.model.Trigger
import munit.CatsEffectSuite

import java.io.File
import scala.io.Source
import io.circe.parser.decode

class CalandroBotSpec extends CatsEffectSuite {

  test("the `cala_list.json` should contain all the triggers of the bot") {
    val listPath      = new File(".").getCanonicalPath + "/cala_list.json"
    val jsonContent   = Source.fromFile(listPath).getLines().mkString("\n")
    val jsonFilenames = decode[List[MediafileSource]](jsonContent).map(_.map(_.filename))

    val botFile = CalandroBot.messageRepliesData[IO].flatMap(_.mediafiles.map(_.filename)) ++ CalandroBot
      .commandRepliesData[IO]
      .flatMap(_.mediafiles.map(_.filename))

    assert(jsonFilenames.isRight)
    jsonFilenames.fold(
      e => fail("test failed", e),
      files =>
        botFile.foreach(filename =>
          assert(files.contains(filename), s"$filename is not contained in calandro data file")
        )
    )

  }

  test("the `cala_triggers.txt` should contain all the triggers of the bot") {
    val listPath       = new File(".").getCanonicalPath + "/cala_triggers.txt"
    val triggerContent = Source.fromFile(listPath).getLines().mkString("\n")

    val botMediaFiles = CalandroBot.messageRepliesData[IO].flatMap(_.mediafiles.map(_.show))
    val botTriggersFiles =
      CalandroBot.messageRepliesData[IO].flatMap(mrd => Show[Trigger].show(mrd.trigger).split('\n'))

    botMediaFiles.foreach { mediaFileString =>
      assert(triggerContent.contains(mediaFileString))
    }
    botTriggersFiles.foreach { triggerString =>
      assert(triggerContent.contains(triggerString), s"$triggerString is not contained in calandro trigger file")
    }
  }
}
