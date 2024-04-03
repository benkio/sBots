package com.benkio.calandrobot

import com.benkio.telegrambotinfrastructure.model.ReplyBundle

import com.benkio.telegrambotinfrastructure.mocks.DBLayerMock
import com.benkio.telegrambotinfrastructure.resources.db.DBLayer
import log.effect.LogLevels
import log.effect.fs2.SyncLogWriter.consoleLogUpToLevel
import log.effect.LogWriter
import com.benkio.telegrambotinfrastructure.model.MediaFileSource
import cats.Show
import cats.effect.IO
import cats.implicits.*
import com.benkio.telegrambotinfrastructure.model.Trigger
import munit.CatsEffectSuite

import java.io.File
import scala.io.Source
import io.circe.parser.decode

class CalandroBotSpec extends CatsEffectSuite {

  given log: LogWriter[IO]      = consoleLogUpToLevel(LogLevels.Info)
  val emptyDBLayer: DBLayer[IO] = DBLayerMock.mock(CalandroBot.botName)

  test("the `cala_list.json` should contain all the triggers of the bot") {
    val listPath      = new File(".").getCanonicalPath + "/cala_list.json"
    val jsonContent   = Source.fromFile(listPath).getLines().mkString("\n")
    val jsonFilenames = decode[List[MediaFileSource]](jsonContent).map(_.map(_.filename))

    val botFile: IO[List[String]] =
      CalandroBot
        .messageRepliesData[IO]
        .filter(ReplyBundle.containsMediaReply(_))
        .flatTraverse(_.reply.prettyPrint)
        .both(
          CalandroBot
            .commandRepliesData[IO](dbLayer = emptyDBLayer)
            .flatTraverse(_.reply.prettyPrint)
        )
        .map { case (m, c) => m ++ c }

    jsonFilenames.fold(
      e => fail("test failed", e),
      files =>
        botFile
          .unsafeRunSync()
          .foreach(filename => assert(files.contains(filename), s"$filename is not contained in calandro data file"))
        assert(
          Set(files*).size == files.length,
          s"there's a duplicate filename into the json ${files.diff(Set(files*).toList)}"
        )
    )

  }

  test("the `cala_triggers.txt` should contain all the triggers of the bot") {
    val listPath        = new File(".").getCanonicalPath + "/cala_triggers.txt"
    val triggerContent  = Source.fromFile(listPath).getLines().mkString("\n")
    val excludeTriggers = List("GIOCHI PER IL MIO PC")

    val botMediaFiles = CalandroBot.messageRepliesData[IO].flatTraverse(_.reply.prettyPrint)
    val botTriggersFiles =
      CalandroBot.messageRepliesData[IO].flatMap(mrd => Show[Trigger].show(mrd.trigger).split('\n'))

    botMediaFiles.unsafeRunSync().filter(x => !excludeTriggers.exists(exc => x.startsWith(exc))).foreach {
      mediaFileString =>
        val result = triggerContent.contains(mediaFileString)
        if (!result) {
          println(s"mediaFileString: $mediaFileString")
          println(s"triggerContent: $triggerContent")
        }
        assert(result)
    }
    botTriggersFiles.foreach { triggerString =>
      assert(triggerContent.contains(triggerString), s"$triggerString is not contained in calandro trigger file")
    }
  }
}
