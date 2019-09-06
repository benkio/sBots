package root

import org.scalatest._
import java.nio.file.Files

import root.infrastructure.model.{GifFile, Mp3File}

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
          .flatMap(_.mp3files)
          .foreach((mp3 : Mp3File) => testFilename(mp3.filename))
      }
    }
  }

  "messageRepliesGifsData" should {
    "never raise an exception" when {
      "try to open the file in resounces" in {
        RichardPHJBensonBot.messageRepliesGifsData
          .flatMap(_.giffiles)
          .foreach((gif : GifFile) => testFilename(gif.filename))
      }
    }
  }

  "messageRepliesSpecialData" should {
    "never raise an exception" when {
      "try to open the file in resounces" in {
        for {
          rb <- RichardPHJBensonBot.messageRepliesSpecialData
          f1 <- rb.mp3files
          f2 <- rb.giffiles
        } yield {
          testFilename(f1.filename)
          testFilename(f2.filename)
        }

      }
    }
  }
}
