package com.benkio.telegrambotinfrastructure

import com.benkio.telegrambotinfrastructure.model.ReplyBundleCommand
import cats.effect.IO
import cats.syntax.all.*
import io.circe.parser.decode

import scala.io.Source
import java.io.File
import com.benkio.telegrambotinfrastructure.model.MediaFileSource
import munit.*

trait BaseBotSpec extends CatsEffectSuite:
  def jsonContainsFilenames(
      jsonFilename: String,
      botData: IO[List[String]]
  ): Unit =
    test(s"the `$jsonFilename` should contain all the triggers of the bot") {
      val listPath            = new File(".").getCanonicalPath + s"/$jsonFilename"
      val jsonContent         = Source.fromFile(listPath).getLines().mkString("\n")
      val jsonMediaFileSource = decode[List[MediaFileSource]](jsonContent)

      for
        _                <- assert(jsonMediaFileSource.isRight).pure[IO]
        mediaFileSources <- IO.fromEither(jsonMediaFileSource)
        files = mediaFileSources.map(_.filename)
        urls  = mediaFileSources.map(_.uri)
        filenames <- botData
        _ <- filenames
          .foreach(filename => assert(files.contains(filename), s"$filename is not contained in bot data file"))
          .pure[IO]
        _ <- assert(
          Set(files*).size == files.length,
          s"there's a duplicate filename into the json ${files.diff(Set(files*).toList)}"
        ).pure[IO]
        _ <- assert(
          urls.forall(_.query.exists { case (key, optValue) => key == "dl" && optValue.fold(false)(_ == "1") })
        ).pure[IO]
      yield ()
    }

  def triggerFileContainsTriggers(
      triggerFilename: String,
      botMediaFiles: IO[List[String]],
      botTriggers: List[String]
  ): Unit =
    test(s"the `$triggerFilename` should contain all the triggers of the bot") {
      val listPath       = new File(".").getCanonicalPath + s"/$triggerFilename"
      val triggerContent = Source.fromFile(listPath).getLines().mkString("\n")

      botMediaFiles.map(_.foreach { mediaFileString =>
        val result = triggerContent.contains(mediaFileString)
        if (!result) {
          println(s"Mediafile missing: $mediaFileString")
        }
        assert(result)
      }) >>
        botTriggers
          .foreach { triggerString =>
            assert(triggerContent.contains(triggerString), s"$triggerString is not contained in richard trigger file")
          }
          .pure[IO]
    }

  def instructionsCommandTest(
      commandRepliesData: IO[List[ReplyBundleCommand[IO]]],
      italianInstructions: String,
      englishInstructions: String
  ): Unit =
    test("instructions command should return the expected message") {
      for
        data <- commandRepliesData
        instructionCommand = data.filter(_.trigger.command == "instructions")
        instructionCommandPrettyPrint <- instructionCommand.flatTraverse(_.reply.prettyPrint)
      yield assertEquals(
        instructionCommandPrettyPrint,
        List(
          italianInstructions,
          englishInstructions
        )
      )
    }

  def triggerlistCommandTest(
      commandRepliesData: IO[List[ReplyBundleCommand[IO]]],
      expectedReply: String
  ): Unit =
    test("triggerlist should return a list of all triggers when called") {
      val triggerlist = commandRepliesData.map(
        _.filter(_.trigger.command == "triggerlist")
          .flatMap(_.reply.prettyPrint.unsafeRunSync())
          .mkString("")
      )

      assertIO(
        triggerlist,
        expectedReply
      )
    }
