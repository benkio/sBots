package com.benkio.telegrambotinfrastructure

import cats.effect.IO
import cats.syntax.all.*
import com.benkio.telegrambotinfrastructure.config.SBotConfig
import com.benkio.telegrambotinfrastructure.initialization.BotSetup
import com.benkio.telegrambotinfrastructure.messagefiltering.MessageMatches
import com.benkio.telegrambotinfrastructure.model.media.MediaFileSource
import com.benkio.telegrambotinfrastructure.model.reply.*
import com.benkio.telegrambotinfrastructure.model.RegexTextTriggerValue
import com.benkio.telegrambotinfrastructure.model.StringTextTriggerValue
import com.benkio.telegrambotinfrastructure.model.TextTrigger
import com.benkio.telegrambotinfrastructure.model.Trigger
import com.benkio.telegrambotinfrastructure.patterns.CommandPatterns.InstructionsCommand
import com.benkio.telegrambotinfrastructure.repository.db.DBLayer
import com.benkio.telegrambotinfrastructure.repository.JsonDataRepository
import com.benkio.telegrambotinfrastructure.repository.Repository
import io.circe.parser.decode
import log.effect.LogWriter
import munit.*
import munit.CatsEffectSuite
import munit.ScalaCheckEffectSuite
import org.http4s.client.Client
import org.http4s.implicits.*
import org.http4s.HttpApp
import org.http4s.Response
import org.http4s.Status
import org.scalacheck.Prop.*
import telegramium.bots.high.Api
import telegramium.bots.Chat
import telegramium.bots.Message
import wolfendale.scalacheck.regexp.RegexpGen

import java.io.File
import scala.concurrent.duration.FiniteDuration
import scala.io.Source

trait BaseBotSpec extends CatsEffectSuite with ScalaCheckEffectSuite {

  def buildTestBotSetup(
      repository: Repository[IO],
      dbLayer: DBLayer[IO],
      sBotConfig: SBotConfig,
      ttl: Option[FiniteDuration]
  )(using Api[IO], LogWriter[IO]): IO[BotSetup[IO]] =
    BackgroundJobManager[IO](dbLayer = dbLayer, sBotInfo = sBotConfig.sBotInfo, ttl = ttl).map { bjm =>
      val stubClient = Client.fromHttpApp(HttpApp[IO](_ => IO.pure(Response[IO](Status.Ok))))
      BotSetup(
        token = "test",
        httpClient = stubClient,
        repository = repository,
        jsonDataRepository = JsonDataRepository[IO]( // repository
        ),
        dbLayer = dbLayer,
        backgroundJobManager = bjm,
        api = summon[Api[IO]],
        webhookUri = uri"https://localhost",
        webhookPath = uri"/",
        sBotConfig = sBotConfig
      )
    }

  private def checkContains(triggerContent: String, values: List[String]): Unit =
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

      for {
        mediaFileSources <- IO.fromEither(jsonMediaFileSource)
        files = mediaFileSources.map(_.filename)
        urls  = mediaFileSources.flatMap(_.sources.collect { case Right(uri) => uri })
        filenames <- botData
      } yield {
        assert(
          jsonMediaFileSource.isRight,
          s"got an error trying to open/parse $jsonFilename @ $listPath: $jsonMediaFileSource"
        )
        filenames
          .foreach(filename => assert(files.contains(filename), s"$filename is not contained in bot data file"))

        assert(
          Set(files*).size == files.length,
          s"there's a duplicate filename into the json ${files.diff(Set(files*).toList)}"
        )
        assert(
          urls.forall(_.query.exists { case (key, optValue) => key == "dl" && optValue.fold(false)(_ == "1") })
        )
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

      }
      end for
    }

  def triggerFileContainsTriggers(
      triggerFilename: String,
      botMediaFiles: IO[List[String]],
      botTriggersIO: IO[List[String]]
  ): Unit =
    test(s"the `$triggerFilename` should contain all the triggers of the bot") {
      val listPath: String       = new File(".").getCanonicalPath + s"/$triggerFilename"
      val triggerContent: String = Source.fromFile(listPath).getLines().mkString("\n")

      for {
        mediaFileStrings <- botMediaFiles
        botTriggers      <- botTriggersIO
      } yield {
        checkContains(triggerContent, mediaFileStrings)
        checkContains(triggerContent, botTriggers)
        val noLowercaseTriggers = botTriggers.filter(s => s != s.toLowerCase)
        assert(noLowercaseTriggers.isEmpty, s"some triggers are not lowercase: $noLowercaseTriggers")
      }
    }

  def inputFileShouldRespondAsExpected(replyBundleMessages: List[ReplyBundleMessage]): Unit =
    test("The inputs in the `inputTest.txt` file returns the expected values") {
      val inputTextTxt: String = new File("./src/test/resources/inputTest.txt").getCanonicalPath
      val inputTextTxtContent: List[(String, List[String])] = Source
        .fromFile(inputTextTxt)
        .getLines()
        .map(inputLine => {
          val splitValue = inputLine.split(" -> ")
          assertEquals(
            splitValue.length,
            2,
            s"[BaseBotSpec] inputText content does not conform to expected structure: input -> filenames comma separated. $inputLine"
          )
          splitValue(0).toLowerCase -> splitValue(1).split(",").map(_.trim).toList
        })
        .toList
      val matchingFilenames: List[List[MediaFile]] = inputTextTxtContent.map { case (input, expectedMatch) =>
        val exactStringMessage = Message(
          messageId = 0,
          date = 0,
          chat = Chat(id = 0, `type` = "test"),
          text = Some(input)
        )
        replyBundleMessages
          .mapFilter(MessageMatches.doesMatch(_, exactStringMessage, None))
          .sortBy(_._1)(using Trigger.orderingInstance.reverse)
          .headOption
          .fold(fail(s"[BaseBotSpec] Expected $expectedMatch for string ${input}, but None found"))(_._2.reply match {
            case mf: MediaReply =>
              mf.mediaFiles
            case x => fail(s"[BaseBotSpec] Expected MediaReply, got $x")
          })
      }
      matchingFilenames.zip(inputTextTxtContent).foreach { case (mediaFiles, (input, expectedFilenames)) =>
        expectedFilenames.foreach { expectedFilename =>
          assert(
            mediaFiles.exists(_.filename == expectedFilename),
            s"[BaseBotSpec] $expectedFilename is not contained in $mediaFiles for $input"
          )
        }
      }
    }

  def instructionsCommandTest(
      commandRepliesDataF: IO[List[ReplyBundleCommand]],
      italianInstructions: String,
      englishInstructions: String
  ): Unit = {
    def instructionMessage(value: String): Message = Message(
      messageId = 0,
      date = 0,
      chat = Chat(id = 0, `type` = "test"),
      text = Some(s"/instructions $value")
    )
    test("instructions command should return the expected message") {
      for {
        commandRepliesData <- commandRepliesDataF
        instructionCommand <- IO.fromOption(commandRepliesData.find(_.trigger.command == "instructions"))(
          Throwable("[BaseBotSpec] can't find the `instruction` command")
        )
        engInstructionInputs = List("", "en", "🇬🇧", "🇺🇸", "🏴󠁧󠁢󠁥󠁮󠁧󠁿", "eng", "english").map(
          instructionMessage(_)
        )
        itaInstructionInputs = List("it", "ita", "italian", "🇮🇹").map(instructionMessage(_))
        (sBotInfo, ignoreMessagePrefix, commands) <- instructionCommand.reply match {
          case EffectfulReply(EffectfulKey.Instructions(sBotInfo, ignoreMessagePrefix, commands), _) =>
            IO.pure((sBotInfo, ignoreMessagePrefix, commands))
          case _ =>
            IO.raiseError(
              Throwable(
                "[BaseBotSpec] `instruction` command should be an `EffectfulReply` with `EffectfulKey.Instructions`"
              )
            )
        }
        engInstructionCommandResult <- engInstructionInputs.traverse(msg =>
          InstructionsCommand.instructionCommandLogic[IO](
            msg = msg,
            sBotInfo = sBotInfo,
            ignoreMessagePrefix = ignoreMessagePrefix,
            commands = commands,
            ttl = None
          )
        )
        itaInstructionCommandResult <- itaInstructionInputs.traverse(msg =>
          InstructionsCommand.instructionCommandLogic[IO](
            msg = msg,
            sBotInfo = sBotInfo,
            ignoreMessagePrefix = ignoreMessagePrefix,
            commands = commands,
            ttl = None
          )
        )
      } yield {
        assertEquals(
          engInstructionCommandResult.flatten.map(_.value),
          List.fill(engInstructionCommandResult.flatten.length)(englishInstructions)
        )
        assertEquals(
          itaInstructionCommandResult.flatten.map(_.value),
          List.fill(itaInstructionCommandResult.flatten.length)(italianInstructions)
        )
      }
    }
  }

  def triggerlistCommandTest(
      commandRepliesData: IO[List[ReplyBundleCommand]],
      expectedReply: String
  ): Unit =
    test("triggerlist should return a list of all triggers when called") {
      for {
        commandReplies <- commandRepliesData
        triggerListCommandPrettyPrint: List[String] = commandReplies
          .filter(_.trigger.command == "triggerlist")
          .flatMap(_.reply.prettyPrint)
      } yield {
        assertEquals(triggerListCommandPrettyPrint.length, 1)
        assertEquals(
          triggerListCommandPrettyPrint.headOption,
          expectedReply.some
        )
      }
    }

  def exactTriggerReturnExpectedReplyBundle(
      replyBundleMessages: List[ReplyBundleMessage]
  ): Unit =
    replyBundleMessages
      .flatMap(replyBundle =>
        replyBundle.trigger match {
          case TextTrigger(triggerValues*) if replyBundle.matcher == MessageMatches.ContainsOnce =>
            triggerValues.map(stringTrigger => (stringTrigger, replyBundle))
          case _ => Nil
        }
      )
      .foreach {
        case (stringTrigger: StringTextTriggerValue, replyBundle) =>
          test(s"""🔎 Only one reply bundle replies to: "${stringTrigger.show}"""") {
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
                assert(
                  tr == TextTrigger(stringTrigger),
                  s"$tr($tr.length) ≠ ${TextTrigger(stringTrigger)}(${stringTrigger.length})"
                )
                assert(rbm == replyBundle, s"$rbm ≠ $replyBundle")
              }
          }
        case (regexTrigger: RegexTextTriggerValue, replyBundle) =>
          property(s"""🔎 Only one reply bundle replies to: "${regexTrigger.trigger.toString}"""") {
            forAll(RegexpGen.from(regexTrigger.trigger.toString)) {
              case regexMatchString: String if regexTrigger.trigger.findFirstMatchIn(regexMatchString).isDefined =>
                val exactStringMessage = Message(
                  messageId = 0,
                  date = 0,
                  chat = Chat(id = 0, `type` = "test"),
                  text = Some(regexMatchString)
                )
                replyBundleMessages
                  .mapFilter(MessageMatches.doesMatch(_, exactStringMessage, None))
                  .sortBy(_._1)(using Trigger.orderingInstance.reverse)
                  .headOption
                  .fold(
                    fail(
                      s"expected a match for regex ${regexTrigger.trigger.toString}, but None found. Input: $regexMatchString"
                    )
                  ) { case (tr, rbm) =>
                    assert(
                      tr == TextTrigger(regexTrigger),
                      s"$tr($tr.length) ≠ ${TextTrigger(regexTrigger)}(${regexTrigger.length})"
                    )
                    assert(rbm == replyBundle, s"$rbm ≠ $replyBundle")
                  }
              case _ =>
                // Generated string does not match regex (e.g. empty string from shrinker), skip this sample
                ()
            }
          }
      }

}
