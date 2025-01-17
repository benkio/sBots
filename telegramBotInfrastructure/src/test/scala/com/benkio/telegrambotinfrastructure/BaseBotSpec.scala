package com.benkio.telegrambotinfrastructure

import com.benkio.telegrambotinfrastructure.model.*
import cats.effect.IO
import cats.syntax.all.*
import io.circe.parser.decode

import scala.io.Source
import java.io.File
import com.benkio.telegrambotinfrastructure.model.MediaFileSource
import munit.*
import telegramium.bots.Chat
import telegramium.bots.Message
import com.benkio.telegrambotinfrastructure.messagefiltering.MessageMatches

trait BaseBotSpec extends CatsEffectSuite:
  def checkContains(triggerContent: String, values: List[String]): Unit =
    values.foreach { value =>
      assert(triggerContent.contains(value), s"$value is not contained in trigger file")
    }

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
        _ <- // TODO: fix this once online
          mediaFileSources
            .foreach(mfs =>
              assert(
                mfs.uri.toString.contains(mfs.filename),
                s"${mfs.uri} doesn't contain the filename: ${mfs.filename}"
              )
            )
            .pure[IO]
      yield ()
    }

  def triggerFileContainsTriggers(
      triggerFilename: String,
      botMediaFiles: IO[List[String]],
      botTriggers: List[String]
  ): Unit =
    test(s"the `$triggerFilename` should contain all the triggers of the bot") {
      val listPath: String       = new File(".").getCanonicalPath + s"/$triggerFilename"
      val triggerContent: String = Source.fromFile(listPath).getLines().mkString("\n")

      for mediaFileStrings <- botMediaFiles
      yield {
        checkContains(triggerContent, mediaFileStrings)
        checkContains(triggerContent, botTriggers)
        val noLowercaseTriggers = botTriggers.filter(s => s != s.toLowerCase)
        assert(noLowercaseTriggers.isEmpty, s"some triggers are not lowercase: $noLowercaseTriggers")
      }
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
      for
        commands <- commandRepliesData
        triggerListCommand = commands.filter(_.trigger.command == "triggerlist")
        triggerListCommandPrettyPrint <- triggerListCommand.flatTraverse(_.reply.prettyPrint)
      yield {
        assert(triggerListCommandPrettyPrint.length == 1)
        assertEquals(
          triggerListCommandPrettyPrint.headOption,
          expectedReply.some
        )
      }
    }

  def exactTriggerReturnExpectedReplyBundle(replyBundleMessages: List[ReplyBundleMessage[IO]]): Unit =
    replyBundleMessages
      .flatMap(replyBundle =>
        replyBundle.trigger match {
          case TextTrigger(triggerValues @ _*) =>
            triggerValues.filter(_.isStringTriggerValue).map(stringTrigger => (stringTrigger, replyBundle))
          case _ => Nil
        }
      )
      .foreach { case (stringTrigger, replyBundle) =>
        test(s"Triggering exactly the string ${stringTrigger.show} should return the expected reply bundle") {
          val exactStringMessage = Message(
            messageId = 0,
            date = 0,
            chat = Chat(id = 0, `type` = "test"),
            text = Some(stringTrigger.show)
          )
          replyBundleMessages
            .mapFilter(MessageMatches.doesMatch(_, exactStringMessage, None))
            .sortBy(_._1)(Trigger.orderingInstance.reverse)
            .headOption
            .fold(fail(s"expected a match for string ${stringTrigger.show}, but None found")) { case (tr, rbm) =>
              assert(tr == TextTrigger(stringTrigger), s"$tr ≠ ${TextTrigger(stringTrigger)}")
              assert(rbm == replyBundle, s"$rbm ≠ $replyBundle")
            }
        }
      }
