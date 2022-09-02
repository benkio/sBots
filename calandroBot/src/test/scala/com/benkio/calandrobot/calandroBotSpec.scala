package com.benkio.calandrobot

import cats.effect.IO
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
      files => assert(botFile.forall(filename => files.contains(filename)))
    )

  }

}
