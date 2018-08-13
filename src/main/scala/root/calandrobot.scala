///////////////////////////////////////////////////////////////////////////////
//             Bot Calandico, contains all the calandric frases              //
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
                 handler : CalandroBot.MessageHandler,
                 matcher : MessageMatches = ContainsOnce) : Option[CalandroBot.MessageHandler] = matcher match {
    case ContainsOnce if (keys.exists(k => messageText.toLowerCase() contains k)) => Some(handler)
    case ContainsAll if (keys.forall(k => messageText.toLowerCase() contains k)) => Some(handler)
    case _ => None
  }
}

object CalandroBot extends TelegramBot
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
   * sendAudioCalandrico
   * @param filename filename of the audio in the resources
   * @param msg message to reply to
   * @return Send the audio
   */
  def sendAudioCalandrico(filename : String)(implicit msg : Message) = {
    uploadingAudio
    val path = buildPath(filename)
    val mp3 = InputFile(path)
    request(SendAudio(msg.source, mp3))
  }

  // Mapping from command name to filename
  val commands : List[(String, String)] =
    List (("porcoladro"         , "PorcoLadro.mp3"),
          ("unoduetre"           , "unoduetre.mp3"),
          ("ancorauna"           , "AncoraUnaDoveLaMetto.mp3"),
          ("lacipolla"           , "CipollaCalandrica.mp3"),
          ("lavorogiusto"        , "IlLavoroVaPagato.mp3"),
          ("motivazioniinternet" , "InternetMotivazioniCalandriche.mp3"),
          ("cazzomene"           , "IoSonVaccinato.mp3"),
          ("arrivoarrivo"        , "SubmissionCalandra.mp3"),
          ("vaginadepilata"      , "VaginaDepilataCalandra.mp3"),
          ("whawha_fallout4"     , "waawahaawha.mp3"),
          ("whawha_short"        , "wwhaaawhaaa Singolo.mp3"),
          ("daccordissimo"       , "d_accordissimo.mp3"),
          ("stocazzo"            , "stocazzo.mp3"),
          ("cazzodibudda"        , "cazzoDiBudda.mp3"),
          ("personapulita"       , "personaPulita.mp3"),
          ("losquirt"            , "loSquirt.mp3"),
          ("fuoridalmondo"       , "fuoriDalMondo.mp3"),
          ("qualitaOlive"        , "qualitáOlive.mp3"),
          ("gioielli"            , "gioielli.mp3"),
          ("risata"              , "risata.mp3"),
          ("sonocosternato"      , "sonoCosternato.mp3"),
          ("demenza"             , "laDemenzaDiUnUomo.mp3"),
          ("wha"                 , "whaSecco.mp3"),
          ("imparatounafava"     , "imparatoUnaFava.mp3"),
          ("lesbiche"            , "SieteLesbiche.mp3"),
          ("firstlesson"         , "firstLessonPlease.mp3"),
          ("noprogrammato"       , "noGrazieProgrammato.mp3"))

  commands.foreach(t => {
                     onCommand(t._1) { implicit msg =>
                       sendAudioCalandrico(t._2)
                     }
                   })

  // Message Replies ////////////////////////////////////////////////////////////

  // Map contains the list of keywords to match, the related messageHandler and
  // the Message matches.
  val messageReplies : List[(List[String], MessageHandler, MessageMatches)] =
    List((List("sbrighi"                                       ), (m : Message) => reply("Passo")(                                                                                                                                        m), ContainsOnce),
         (List("gay", "frocio", "culattone", "ricchione"       ), (m : Message) => reply("CHE SCHIFO!!!")(                                                                                                                                m), ContainsOnce),
         (List("caldo", "scotta"                               ), (m : Message) => reply("Come i carbofreni della Brembo!!")(                                                                                                             m), ContainsOnce),
         (List("ciao", "buongiorno", "salve"                   ), (m : Message) => reply("Buongiorno Signori")(                                                                                                                           m), ContainsOnce),
         (List("film"                                          ), (m : Message) => reply("Lo riguardo volentieri")(                                                                                                                       m), ContainsOnce),
         (List("stasera", "?"                                  ), (m : Message) => reply("Facciamo qualcosa tutti assieme?")(                                                                                                             m), ContainsAll),
         (List(" hd", "nitido", "nitidezza", "alta definizione"), (m : Message) => reply("Eh sì, vedi...si nota l'indecisione dell'immagine")(                                                                                          m), ContainsOnce),
         (List("qualità"                                       ), (m : Message) => reply("A 48x masterizza meglio")(                                                                                                                      m), ContainsOnce),
         (List("macchina", "automobile"                        ), (m : Message) => reply("Hai visto l'ultima puntata di \"Top Gear\"?")(                                                                                                  m), ContainsOnce),
         (List(" figa ", " fregna ", " gnocca ", " patacca "   ), (m : Message) => reply("Io so come fare con le donne...ho letto tutto...")(                                                                                             m), ContainsOnce),
         (List("ambulanza", emoji":ambulance:"                 ), (m : Message) => reply(emoji":triumph: :horns_sign: :hand_with_index_and_middle_fingers_crossed: :hand_with_index_and_middle_fingers_crossed: :horns_sign: :triumph:")( m), ContainsOnce),
         (List("pc", "computer"                                ), (m : Message) => reply("Il fisso performa meglio rispetto al portatile!!!")(                                                                                            m), ContainsOnce))

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
  def textLengthExceedHandler(textLength : Int, messageId : Int) : MessageHandler = (m : Message) =>
  reply(s"""WARNING: The length of this message exceed the "Calandric" Limit of 280 chars($textLength / 280), beware of your calandric level!""",
        replyToMessageId = Some(messageId))(m)

  onMessage((message : Message) =>
    message.text.map { m =>
      messageReplies
        .flatMap(t => MessageMatches.getHandler(t._1, m, t._2, t._3).toList)
        .foreach(_(message))

      countHandler(m.length, message.messageId)(message)
    }
  )
}
