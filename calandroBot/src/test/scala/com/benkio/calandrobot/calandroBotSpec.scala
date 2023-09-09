package com.benkio.calandrobot

import cats.Show
import cats.effect.IO
import cats.implicits._
import com.benkio.telegrambotinfrastructure.model.Trigger
import io.chrisdavenport.cormorant._
import io.chrisdavenport.cormorant.parser._
import munit.CatsEffectSuite

import java.io.File
import scala.io.Source

class CalandroBotSpec extends CatsEffectSuite {

  test("the `cala_list.csv` should contain all the triggers of the bot") {
    val listPath   = new File(".").getCanonicalPath + "/cala_list.csv"
    val csvContent = Source.fromFile(listPath).getLines().mkString("\n")
    val csvFile = parseComplete(csvContent).flatMap {
      case CSV.Complete(_, CSV.Rows(rows)) => Right(rows.map(row => row.l.head.x))
      case _                               => Left(new RuntimeException("Error on parsing the csv"))
    }

    val botFile = CalandroBot.messageRepliesData[IO].flatMap(_.mediafiles.map(_.filename)) ++ CalandroBot
      .commandRepliesData[IO]
      .flatMap(_.mediafiles.map(_.filename))

    assert(csvFile.isRight)
    csvFile.fold(
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
