package root

import org.scalatest._
import java.nio.file.Files

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
                RichardPHJBensonBot.messageRepliesAudioData map {
                    case (_, filename, _) => testFilename(filename)
                }
            }
        }
    }

    "messageRepliesGifsData" should {
        "never raise an exception" when {
            "try to open the file in resounces" in {
                RichardPHJBensonBot.messageRepliesGifsData map {
                    case (_, filename, _) => testFilename(filename)
                }
            }
        }
    }

    "messageRepliesSpecialData" should {
        "never raise an exception" when {
            "try to open the file in resounces" in {
                RichardPHJBensonBot.messageRepliesSpecialData map {
                    case (_, filename1, filename2, _) => {
                        testFilename(filename1)
                        testFilename(filename2)
                    }
                }
            }
        }
    }
}