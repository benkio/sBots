package root

import telegramium.bots.high._
import telegramium.bots.high.implicits._
import org.http4s.client.blaze._
import org.http4s.client._
import cats.effect._
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.ExecutionContext
import com.benkio.telegramBotInfrastructure.botCapabilities.ResourceSource
import org.scalatest._
import java.nio.file.Files
import com.benkio.telegramBotInfrastructure.model.ReplyBundleMessage
import com.benkio.telegramBotInfrastructure.model.MediaFile
import com.benkio.telegramBotInfrastructure.model.ReplyBundleCommand
import matchers.should._
import org.scalatest.wordspec.AnyWordSpec

class CalandroBotSpec extends AnyWordSpec {

  implicit val timer: Timer[IO]               = IO.timer(global)
  implicit val contextShift: ContextShift[IO] = IO.contextShift(global)

  val (resourceSource, commandRepliesData, messageRepliesData): (
      ResourceSource,
      List[ReplyBundleCommand],
      List[ReplyBundleMessage]
  ) =
    CalandroBot
      .buildBot(global)
      .map(bot => (bot.resourceSource, bot.commandRepliesData, bot.messageRepliesData))
      .unsafeRunSync()

  def testFilename(filename: String): Assertion =
    if (ResourceSource
          .selectResourceAccess(resourceSource)
          .getResourceByteArray(filename)
          .isEmpty)
      fail(s"$filename cannot be found")
    else succeed

  "commandRepliesData" should {
    "never raise an exception" when {
      "try to open the file in resounces" in {
        commandRepliesData
          .flatMap(_.mediafiles)
          .foreach((mf: MediaFile) => testFilename(mf.filename))
      }
    }
  }

  "messageRepliesData" should {
    "never raise an exception" when {
      "try to open the file in resounces" in {
        messageRepliesData
          .flatMap(_.mediafiles)
          .foreach((mf: MediaFile) => testFilename(mf.filename))
      }
    }
  }
}
