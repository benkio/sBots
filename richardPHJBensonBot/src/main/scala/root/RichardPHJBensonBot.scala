///////////////////////////////////////////////////////////////////////////////
//             Bot di Richard Benson, contains all the Benson's frases       //
///////////////////////////////////////////////////////////////////////////////
package root

import info.mukel.telegrambot4s._, api._, methods._, models._, declarative._
import java.nio.file.Path
import java.nio.file.Paths
import java.nio.file.Files
import scala.io.Source
import scala.concurrent.Future
import io.github.todokr.Emojipolation._

// Types for managing the matching of words
trait MessageMatches

object ContainsOnce extends MessageMatches
object ContainsAll extends MessageMatches

object MessageMatches {
  /**
    * getHandler
    * @param keys List of keys to check for existance in the input text
    * @param messageText Input Text
    * @param handler How to handle the input text in case of match
    * @param matcher Drive the logic in which the match should be performed
    * @return the messageReplies handling the request
    */
  def getHandler(keys : List[String],
                 messageText : String,
                 handler : RichardPHJBensonBot.MessageHandler,
                 matcher : MessageMatches = ContainsOnce) : Option[RichardPHJBensonBot.MessageHandler] = matcher match {
    case ContainsOnce if (keys.exists(k => messageText.toLowerCase() contains k)) => Some(handler)
    case ContainsAll if (keys.forall(k => messageText.toLowerCase() contains k)) => Some(handler)
    case _ => None
  }
}

object RichardPHJBensonBot extends TelegramBot
    with Polling
    with Commands
    with ChatActions
    with Messages {

  type MessageHandler = Message => Future[Message]

  // Configuration Stuff //////////////////////////////////////////////////////
  lazy val token = scala.util.Properties
    .envOrNone("BOT_TOKEN")
    .getOrElse(Source.fromResource("bot.token").getLines().mkString)
  override val ignoreCommandReceiver = true
  val rootPath = Paths.get("").toAbsolutePath()

  def buildPath(filename : String) : Path =
    Paths.get(rootPath.toString(), "src", "main", "resources", filename)

  // Commands & Audio Replies /////////////////////////////////////////////////

  /*
   * sendAudioBenson
   * @param filename filename of the audio in the resources
   * @param msg message to reply to
   * @return Send the audio
   */
  def sendAudioBenson(filename : String)(implicit msg : Message) : Future[Message] = {
    uploadingAudio
    val path = buildPath(filename)
    val mp3 = InputFile(path)
    request(SendAudio(msg.source, mp3))
  }

  def sendGifBenson(filename : String)(implicit msg : Message) : Future[Message] = {
    uploadingDocument
    val path = buildPath(filename)
    val byteArray : Array[Byte] = Files.readAllBytes(path)
    val gif = InputFile("benson.gif", byteArray)
    request(SendDocument(msg.source, gif))

  }

  // Message Replies ////////////////////////////////////////////////////////////


  val messageRepliesAudio = List(
      (List("napoli"), (m : Message)                   => sendAudioBenson("vivaNapoli.mp3")(m), ContainsOnce),
      (List("maledetto"), (m : Message)                => sendAudioBenson("maledetto.mp3")(m), ContainsOnce),
      (List("aiuto", "aiutatemi"), (m : Message)       => sendAudioBenson("aiuto.mp3")(m), ContainsOnce),
      (List("ritornata"), (m : Message)                => sendAudioBenson("ritornata.mp3")(m), ContainsOnce),
      (List("ma che cazzo sto dicendo"), (m : Message) => sendAudioBenson("machecazzostodicendo.mp3")(m), ContainsAll),
      (List("questa volta no"), (m : Message)          => sendAudioBenson("questavoltano.mp3")(m), ContainsAll),
      (List("poveri cretini"), (m : Message)           => sendAudioBenson("povericretini.mp3")(m), ContainsAll),
      (List("ho capito"), (m : Message)                => sendAudioBenson("hocapito.mp3")(m), ContainsAll),
      (List("feelings"), (m : Message)                 => sendAudioBenson("feelings.mp3")(m), ContainsAll),
      (List("avete capito"), (m : Message)             => sendAudioBenson("avetecapito.mp3")(m), ContainsAll)
  )

  val messageRepliesGifs = List(
    (List("bravo"), (m : Message)                    => sendGifBenson("bravo.gif")(m), ContainsOnce),
    (List("masgus"), (m : Message)                   => sendGifBenson("masgus.gif")(m), ContainsOnce),
    (List("capolavoro"), (m : Message)               => sendGifBenson("capolavoro.gif")(m), ContainsOnce),
    (List("mare di cazzate"), (m : Message)          => sendGifBenson("nonépossibile.gif")(m), ContainsOnce),
    (List("porca miseria"), (m : Message)            => sendGifBenson("porcamiseria.gif")(m), ContainsOnce),
    (List("schifoso"), (m : Message)                 => sendGifBenson("schifoso.gif")(m), ContainsOnce),
    (List("fammi questa cortesia"), (m : Message)    => sendGifBenson("fammiquestacortesia.gif")(m), ContainsOnce),
    (List("ma vattene affanculo"), (m : Message)     => sendGifBenson("mavatteneaffanculo.gif")(m), ContainsOnce),
    (List("rimpinzati"), (m : Message)               => sendGifBenson("rimpinzati.gif")(m), ContainsOnce),
    (List("fancedo soffrire"), (m : Message)         => sendGifBenson("facendosoffrire.gif")(m), ContainsOnce),
    (List("mi sto sentendo male"), (m : Message)     => sendGifBenson("mistosentendomale.gif")(m), ContainsOnce),
    (List("mi fa stare male"), (m : Message)         => sendGifBenson("mifastaremale.gif")(m), ContainsOnce),
    (List("lunghezza d'onda"), (m : Message)         => sendGifBenson("lunghezzadonda.gif")(m), ContainsOnce)
  )

  val messageRepliesSpecial = List(
    (List("basta"), (m : Message)                   => {
      sendGifBenson("basta.gif")(m)
      sendAudioBenson("basta.mp3")(m)
    }, ContainsOnce)
  )

  // Map contains the list of keywords to match, the related messageHandler and
  // the Message matches.
  val messageReplies : List[(List[String], MessageHandler, MessageMatches)] =
    messageRepliesAudio ++ messageRepliesGifs ++ messageRepliesSpecial

  onMessage((message : Message) =>
     message.text.map { m =>
       messageReplies
         .flatMap(t => MessageMatches.getHandler(t._1, m, t._2, t._3).toList)
         .foreach(_(message))
     }
  )
}
