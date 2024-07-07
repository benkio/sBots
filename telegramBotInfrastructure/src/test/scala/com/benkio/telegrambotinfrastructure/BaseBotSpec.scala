package com.benkio.telegrambotinfrastructure

import com.benkio.telegrambotinfrastructure.model.ReplyBundleCommand
import cats.effect.IO
import cats.implicits.*
import io.circe.parser.decode

import scala.io.Source
import java.io.File
import com.benkio.telegrambotinfrastructure.model.MediaFileSource
import munit.*

trait BaseBotSpec extends CatsEffectSuite:
  def jsonContainsFilenames(
      jsonFilename: String,
      botData: List[String]
  ): Unit =
    test(s"the `$jsonFilename` should contain all the triggers of the bot") {
      val listPath            = new File(".").getCanonicalPath + s"/$jsonFilename"
      val jsonContent         = Source.fromFile(listPath).getLines().mkString("\n")
      val jsonMediaFileSource = decode[List[MediaFileSource]](jsonContent)

      assert(jsonMediaFileSource.isRight)
      jsonMediaFileSource.fold(
        e => fail("test failed", e),
        mediaFileSources =>
          val files = mediaFileSources.map(_.filename)
          val urls  = mediaFileSources.map(_.uri)
          botData.foreach(filename => assert(files.contains(filename), s"$filename is not contained in bot data file"))
          assert(
            Set(files*).size == files.length,
            s"there's a duplicate filename into the json ${files.diff(Set(files*).toList)}"
          )
          assert(
            urls.forall(_.query.exists { case (key, optValue) => key == "dl" && optValue.fold(false)(_ == "1") })
          )
      )

    }

  def triggerFileContainsTriggers(
      triggerFilename: String,
      botMediaFiles: List[String],
      botTriggers: List[String]
  ): Unit =
    test(s"the `$triggerFilename` should contain all the triggers of the bot") {
      val listPath       = new File(".").getCanonicalPath + s"/$triggerFilename"
      val triggerContent = Source.fromFile(listPath).getLines().mkString("\n")

      botMediaFiles.foreach { mediaFileString =>
        val result = triggerContent.contains(mediaFileString)
        if (!result) {
          println(s"Mediafile missing: $mediaFileString")
        }
        assert(result)
      }
      botTriggers.foreach { triggerString =>
        assert(triggerContent.contains(triggerString), s"$triggerString is not contained in richard trigger file")
      }
    }

  def instructionsCommandTest(
      commandRepliesData: List[ReplyBundleCommand[IO]],
      italianInstructions: String,
      englishInstructions: String
  ): Unit =
    test("instructions command should return the expected message") {
      val actual = commandRepliesData
        .filter(_.trigger.command == "instructions")
        .flatTraverse(_.reply.prettyPrint)
      assertIO(
        actual,
        List(
          italianInstructions,
          englishInstructions
        )
      )
    }

  def triggerlistCommandTest(
      commandRepliesData: List[ReplyBundleCommand[IO]],
      expectedReply: String
  ): Unit =
    test("triggerlist should return a list of all triggers when called") {
      val triggerlist = commandRepliesData
        .filter(_.trigger.command == "triggerlist")
        .flatMap(_.reply.prettyPrint.unsafeRunSync())
        .mkString("")

      assertEquals(
        triggerlist,
        expectedReply
      )
    }
