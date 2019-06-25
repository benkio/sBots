///////////////////////////////////////////////////////////////////////////////
//             Bot DjJacob, contains all the Jacob's  frases                 //
///////////////////////////////////////////////////////////////////////////////
package root

import info.mukel.telegrambot4s._, api._, methods._, models._, declarative._
import java.nio.file.Path
import java.nio.file.Paths
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
                 handler : DjJacobBot.MessageHandler,
                 matcher : MessageMatches = ContainsOnce) : Option[DjJacobBot.MessageHandler] = matcher match {
    case ContainsOnce if (keys.exists(k => messageText.toLowerCase() contains k)) => Some(handler)
    case ContainsAll if (keys.forall(k => messageText.toLowerCase() contains k)) => Some(handler)
    case _ => None
  }
}

object DjJacobBot extends TelegramBot
    with Polling
    with Commands
    with ChatActions
    with Messages{

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
   * sendAudioJacob
   * @param filename filename of the audio in the resources
   * @param msg message to reply to
   * @return Send the audio
   */
  def sendAudioJacob(filename : String)(implicit msg : Message) = {
    uploadingAudio
    val path = buildPath(filename)
    val mp3 = InputFile(path)
    request(SendAudio(msg.source, mp3))
  }

  // Mapping from command name to filename
  val commands : List[(String, String)] =
    List (("vivaglisposi"       , "VivaGliSposi.mp3"),
          ("supermegamix"       , "SuperMegaMix.mp3"),
          ("simonasalutami"     , "SimonaSalutami.mp3"),
          ("simonasalutami"     , "SimonaSalutami.mp3"),
          ("simonaaspettando"   , "SimonaAspettando.mp3"),
          ("pasticceradove"     , "PasticceraDove.mp3"),
          ("frappeconocoppetta" , "FrappeConoCoppetta.mp3"),
          ("discolancio"        , "DiscoLancio.mp3"),
          ("convocatoconsole"   , "ConvocatoConsole.mp3"),
          ("cineseincoma"       , "CineseInComa.mp3"),
          ("billo"              , "Billo.mp3"),
          ("auguri"             , "Auguri.mp3"))

  commands.foreach(t => {
                     onCommand(t._1) { implicit msg =>
                       sendAudioJacob(t._2)
                     }
                   })

  // Message Replies ////////////////////////////////////////////////////////////

  // Map contains the list of keywords to match, the related messageHandler and
  // the Message matches.
  val messageReplies : List[(List[String], MessageHandler, MessageMatches)] =
    List()

  /**
    * countHandler
    * @param textLength Length of the input text message
    * @param messageId id of the message
    * @return The message handler for the input message
    */
  def countHandler(textLength : Int, messageId : Int) : MessageHandler =
    if (textLengthCondition(textLength)) textLengthExceedHandler(textLength, messageId)
    else (m : Message) => Future.successful(m)

  /**
    * textLengthCondition
    * @param textLength length of the message
    * @return If the message match the condition
    */
  def textLengthCondition(textLength : Int) : Boolean = textLength > 280
  /**
    * textLengthExceedHandler
    * @param textLength length of the message
    * @param messageId if of the message
    */
  def textLengthExceedHandler(textLength : Int, messageId : Int) : MessageHandler = (m : Message) => Future.successful(m)
  //reply(s"""""",
  //      replyToMessageId = Some(messageId))(m)

  onMessage((message : Message) =>
    message.text.map { m =>
      messageReplies
        .flatMap(t => MessageMatches.getHandler(t._1, m, t._2, t._3).toList)
        .foreach(_(message))

      //countHandler(m.length, message.messageId)(message)
    }
  )
}
