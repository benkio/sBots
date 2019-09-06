package root

import org.scalatest._
import java.nio.file.Files

import com.benkio.telegramBotInfrastructure.model.MediaFile

class RichardPHJBensonBotSpec extends WordSpec {

  def testFilename(filename : String) = {
    try {
      val path = RichardPHJBensonBot.buildPath(filename)
      val _ : Array[Byte] = Files.readAllBytes(path)
    } catch {
      case e : Exception => fail(s"$filename should not throw an exception: $e")
    }
  }

  "messageRepliesAudioData" should {
    "never raise an exception" when {
      "try to open the file in resounces" in {
        RichardPHJBensonBot.messageRepliesAudioData
          .flatMap(_.mediafiles)
          .foreach((mp3 : MediaFile) => testFilename(mp3.filename))
      }
    }
  }

  "messageRepliesGifsData" should {
    "never raise an exception" when {
      "try to open the file in resounces" in {
        RichardPHJBensonBot.messageRepliesGifsData
          .flatMap(_.mediafiles)
          .foreach((gif : MediaFile) => testFilename(gif.filename))
      }
    }
  }

  "messageRepliesSpecialData" should {
    "never raise an exception" when {
      "try to open the file in resounces" in {
        for {
          rb <- RichardPHJBensonBot.messageRepliesSpecialData
          f1 <- rb.mediafiles
        } yield {
          testFilename(f1.filename)
        }

      }
    }
  }
}
