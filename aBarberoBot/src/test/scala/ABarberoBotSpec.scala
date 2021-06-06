package root

import com.benkio.telegramBotInfrastructure.botCapabilities.ResourceSource
import org.scalatest._
import matchers.should._
import com.benkio.telegramBotInfrastructure.model.TextTrigger
import com.benkio.telegramBotInfrastructure.model.MediaFile
import org.scalatest.wordspec.AnyWordSpec

class ABarberoBotSpec extends AnyWordSpec with Matchers {

  def testFilename(filename: String): Assertion =
    if (
      ResourceSource
        .selectResourceAccess(ABarberoBot.resourceSource)
        .getResourceByteArray(filename)
        .isEmpty
    )
      fail(s"$filename cannot be found")
    else succeed

  "messageRepliesAudioData" should {
    "never raise an exception" when {
      "try to open the file in resounces" in {
        ABarberoBot.messageRepliesAudioData
          .flatMap(_.mediafiles)
          .foreach((mp3: MediaFile) => testFilename(mp3.filename))
      }
    }
  }

  "messageRepliesGifsData" should {
    "never raise an exception" when {
      "try to open the file in resounces" in {
        ABarberoBot.messageRepliesGifsData
          .flatMap(_.mediafiles)
          .foreach((gif: MediaFile) => testFilename(gif.filename))
      }
    }
  }

  "messageRepliesSpecialData" should {
    "never raise an exception" when {
      "try to open the file in resounces" in {
        for {
          rb <- ABarberoBot.messageRepliesSpecialData
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
        ABarberoBot.commandRepliesData.length should be(1)
        ABarberoBot.messageRepliesData
          .flatMap(
            _.trigger match {
              case TextTrigger(lt) => lt
              case _               => ""
            }
          )
          .forall(s => ABarberoBot.commandRepliesData.init.flatMap(_.text.text(null)).contains(s))
      }
    }
  }
}
