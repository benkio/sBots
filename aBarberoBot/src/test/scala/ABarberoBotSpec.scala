package root

import cats.effect._
import scala.concurrent.ExecutionContext.Implicits.global
import com.benkio.telegramBotInfrastructure.botCapabilities.ResourceSource
import org.scalatest._
import matchers.should._
import com.benkio.telegramBotInfrastructure.model.TextTrigger
import com.benkio.telegramBotInfrastructure.model.ReplyBundleMessage
import com.benkio.telegramBotInfrastructure.model.MediaFile
import com.benkio.telegramBotInfrastructure.model.ReplyBundleCommand
import org.scalatest.wordspec.AnyWordSpec

class ABarberoBotSpec extends AnyWordSpec with Matchers {

  implicit val timer: Timer[IO]               = IO.timer(global)
  implicit val contextShift: ContextShift[IO] = IO.contextShift(global)

  val (
    resourceSource,
    commandRepliesData,
    messageRepliesData,
    messageRepliesAudioData,
    messageRepliesGifsData,
    messageRepliesSpecialData
  ): (
      ResourceSource,
      List[ReplyBundleCommand],
      List[ReplyBundleMessage],
      List[ReplyBundleMessage],
      List[ReplyBundleMessage],
      List[ReplyBundleMessage],
  ) =
    ABarberoBot
      .buildBot(
        global,
        (bot: ABarberoBot[IO]) =>
          IO.pure(
            (
              bot.resourceSource,
              bot.commandRepliesData,
              bot.messageRepliesData,
              bot.messageRepliesAudioData,
              bot.messageRepliesGifsData,
              bot.messageRepliesSpecialData
            )
          )
      )
      .unsafeRunSync()

  def testFilename(filename: String): Assertion =
    if (ResourceSource
          .selectResourceAccess(resourceSource)
          .getResourceByteArray(filename)
          .isEmpty)
      fail(s"$filename cannot be found")
    else succeed

  "messageRepliesAudioData" should {
    "never raise an exception" when {
      "try to open the file in resounces" in {
        messageRepliesAudioData
          .flatMap(_.mediafiles)
          .foreach((mp3: MediaFile) => testFilename(mp3.filename))
      }
    }
  }

  "messageRepliesGifsData" should {
    "never raise an exception" when {
      "try to open the file in resounces" in {
        messageRepliesGifsData
          .flatMap(_.mediafiles)
          .foreach((gif: MediaFile) => testFilename(gif.filename))
      }
    }
  }

  "messageRepliesSpecialData" should {
    "never raise an exception" when {
      "try to open the file in resounces" in {
        for {
          rb <- messageRepliesSpecialData
          f1 <- rb.mediafiles
        } yield {
          testFilename(f1.filename)
        }
      }
    }
  }

  "commandRepliesData" should {
    "return a list of all triggers" when {
      "called" in {
        commandRepliesData.length should be(1)
        messageRepliesData
          .flatMap(
            _.trigger match {
              case TextTrigger(lt) => lt
              case _               => ""
            }
          )
          .forall(s => commandRepliesData.init.flatMap(_.text.text(null)).contains(s))
      }
    }
  }
}
