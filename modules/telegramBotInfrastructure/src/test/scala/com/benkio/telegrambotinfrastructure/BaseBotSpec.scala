package com.benkio.telegrambotinfrastructure

import cats.effect.IO
import cats.syntax.all.*
import com.benkio.telegrambotinfrastructure.messagefiltering.MessageMatches
import com.benkio.telegrambotinfrastructure.model.isStringTriggerValue
import com.benkio.telegrambotinfrastructure.model.media.MediaFileSource
import com.benkio.telegrambotinfrastructure.model.reply.*
import com.benkio.telegrambotinfrastructure.model.TextTrigger
import com.benkio.telegrambotinfrastructure.model.Trigger
import io.circe.parser.decode
import munit.*
import telegramium.bots.Chat
import telegramium.bots.Message

import java.io.File
import scala.io.Source

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
        _ <- assert(
          jsonMediaFileSource.isRight,
          s"got an error trying to open/parse $jsonFilename @ $listPath: $jsonMediaFileSource"
        ).pure[IO]
        mediaFileSources <- IO.fromEither(jsonMediaFileSource)
        files = mediaFileSources.map(_.filename)
        urls  = mediaFileSources.flatMap(_.sources.collect { case Right(uri) => uri })
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
              mfs.sources.foreach {
                case Right(uri) =>
                  assert(
                    uri.toString.contains(mfs.filename),
                    s"$uri doesn't contain the filename: ${mfs.filename}"
                  )
                case _ => assert(true)
              }
            )
            .pure[IO]
      yield ()
      end for
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
    def instructionMessage(value: String): Message = Message(
      messageId = 0,
      date = 0,
      chat = Chat(id = 0, `type` = "test"),
      text = Some(s"/instructions $value")
    )
    test("instructions command should return the expected message") {
      for
        data <- commandRepliesData
        instructionCommand <- IO.fromOption(data.find(_.trigger.command == "instructions"))(
          Throwable("[BaseBotSpec] can't find the `instruction` command")
        )
        engInstructionInputs = List("", "en", "🇬🇧", "🇺🇸", "🏴󠁧󠁢󠁥󠁮󠁧󠁿", "eng", "english").map(
          instructionMessage(_)
        )
        itaInstructionInputs = List("it", "ita", "italian", "🇮🇹").map(instructionMessage(_))
        engInstructionCommandResult <- instructionCommand.reply match {
          case TextReplyM(textM, _) => engInstructionInputs.flatTraverse(textM(_))
          case _ => IO.raiseError(Throwable("[BaseBotSpec] `instruction` command sholud be a `TextReplyM`"))
        }
        itaInstructionCommandResult <- instructionCommand.reply match {
          case TextReplyM(textM, _) => itaInstructionInputs.flatTraverse(textM(_))
          case _ => IO.raiseError(Throwable("[BaseBotSpec] `instruction` command sholud be a `TextReplyM`"))
        }
      yield
        assertEquals(
          engInstructionCommandResult.map(_.value),
          List.fill(engInstructionCommandResult.length)(englishInstructions)
        )
        assertEquals(
          itaInstructionCommandResult.map(_.value),
          List.fill(itaInstructionCommandResult.length)(italianInstructions)
        )
    }
  end instructionsCommandTest

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
          case TextTrigger(triggerValues*) if replyBundle.matcher == MessageMatches.ContainsOnce =>
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
            .sortBy(_._1)(using Trigger.orderingInstance.reverse)
            .headOption
            .fold(fail(s"expected a match for string ${stringTrigger.show}, but None found")) { case (tr, rbm) =>
              assert(tr == TextTrigger(stringTrigger), s"$tr ≠ ${TextTrigger(stringTrigger)}")
              assert(rbm == replyBundle, s"$rbm ≠ $replyBundle")
            }
        }
      }
end BaseBotSpec
