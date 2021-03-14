package root

import com.benkio.telegramBotInfrastructure.botCapabilities.ResourceSource
import org.scalatest._
import java.nio.file.Files

import com.benkio.telegramBotInfrastructure.model.MediaFile

class CalandroBotSpec extends AnyWordSpec {

  def testFilename(filename: String) =
    if (ResourceSource
          .selectResourceAccess(CalandroBot.resourceSource)
          .getResource(filename)
          .isEmpty)
      fail(s"$filename cannot be found")
    else succeed

  "commandRepliesData" should {
    "never raise an exception" when {
      "try to open the file in resounces" in {
        CalandroBot.commandRepliesData
          .flatMap(_.mediafiles)
          .foreach((mf: MediaFile) => testFilename(mf.filename))
      }
    }
  }

  "messageRepliesData" should {
    "never raise an exception" when {
      "try to open the file in resounces" in {
        CalandroBot.messageRepliesData
          .flatMap(_.mediafiles)
          .foreach((mf: MediaFile) => testFilename(mf.filename))
      }
    }
  }
}
