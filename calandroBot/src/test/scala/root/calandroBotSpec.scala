package root

import org.scalatest._
import java.nio.file.Files

import com.benkio.telegramBotInfrastructure.model.MediaFile

class CalandroBotSpec extends WordSpec {

  def testFilename(filename : String) = {
    try {
      val path = CalandroBot.buildPath(filename)
      val _ : Array[Byte] = Files.readAllBytes(path)
    } catch {
      case e : Exception => fail(s"$filename should not throw an exception: $e")
    }
  }

  "commandRepliesData" should {
    "never raise an exception" when {
      "try to open the file in resounces" in {
        CalandroBot.commandRepliesData
          .flatMap(_.mediafiles)
          .foreach((mf : MediaFile) => testFilename(mf.filename))
      }
    }
  }

  "messageRepliesData" should {
    "never raise an exception" when {
      "try to open the file in resounces" in {
        CalandroBot.messageRepliesData
          .flatMap(_.mediafiles)
          .foreach((mf : MediaFile) => testFilename(mf.filename))
      }
    }
  }
}
