package root

import org.scalatest._
import java.nio.file.Files

import com.benkio.telegramBotInfrastructure.model.{
  MediaFile,
  TextTrigger
}

class RichardPHJBensonBotSpec extends WordSpec with Matchers {

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

  "commandRepliesData" should {
    "return a list of all triggers" when {
      "called" in {
        RichardPHJBensonBot.commandRepliesData.length shouldEqual 3
        RichardPHJBensonBot.messageRepliesData
          .flatMap(
            _.trigger match {
              case TextTrigger(lt) => lt
              case _ => ""
            }
          ).forall(s =>
            RichardPHJBensonBot.commandRepliesData.init.flatMap(_.text.text(null)).contains(s)
          )

        // RichardPHJBensonBot.commandRepliesData
        //   .last
        //   .text.text(Message(
        //     messageId = 0,
        //     date = 0,
        //     chat = Chat(id = 0, `type` = ChatType.Private)
        //   )) shouldEqual List("E PAAAARRRRRLAAAAAAAAA!!!!")
      }
    }
  }
}
