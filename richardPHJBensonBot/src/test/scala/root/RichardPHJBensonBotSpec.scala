package root

import org.scalatest._
import java.nio.file.Files
import root.infrastructure._

class RichardPHJBensonBotSpec extends WordSpec {

  def testFilename(filename : String) = {
    try {
      val path = RichardPHJBensonBot.buildPath(filename)
      val byteArray : Array[Byte] = Files.readAllBytes(path)
    } catch {
      case e : Exception => fail(s"$filename should not throw an exception: $e")
    }
  }

  "messageRepliesAudioData" should {
    "never raise an exception" when {
      "try to open the file in resounces" in {
        for {
          rb <- RichardPHJBensonBot.messageRepliesAudioData
          val mediaFiles = rb.files.asInstanceOf[Mp3Files]
          f <- mediaFiles.files
        } yield testFilename(f)
      }
    }
  }

  "messageRepliesGifsData" should {
    "never raise an exception" when {
      "try to open the file in resounces" in {
        for {
          rb <- RichardPHJBensonBot.messageRepliesGifsData
          val mediaFiles = rb.files.asInstanceOf[GifFiles]
          f <- mediaFiles.files
        } yield testFilename(f)
      }
    }
  }

  "messageRepliesSpecialData" should {
    "never raise an exception" when {
      "try to open the file in resounces" in {
        for {
          rb <- RichardPHJBensonBot.messageRepliesSpecialData
          val mediaFiles = rb.files.asInstanceOf[MultimediaFiles]
          f1 <- mediaFiles.mp3Files
          f2 <- mediaFiles.gifFiles
        } yield {
          testFilename(f1)
          testFilename(f2)
        }

      }
    }
  }
}
