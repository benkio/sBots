///////////////////////////////////////////////////////////////////////////////
//             Bot Calandico, contains all the calandric frases              //
///////////////////////////////////////////////////////////////////////////////
package root

import info.mukel.telegrambot4s._, api._, methods._, models._, declarative._
import java.nio.file.Path
import java.nio.file.Paths
import scala.io.Source

object CalandroBot extends TelegramBot
    with Polling
    with Commands
    with ChatActions{

  lazy val token = scala.util.Properties
    .envOrNone("BOT_TOKEN")
    .getOrElse(Source.fromResource("bot.token").getLines().mkString)

  override val ignoreCommandReceiver = true

  val rootPath = Paths.get("").toAbsolutePath()

  val commands : List[(String, String)] =
    List(("porcoladro"          , "PorcoLadro.mp3"),
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
         ("risata"              , "risata.mp3"))

  val messageReplies : List[(String, Message => Unit)] =
    List(("sbrighi", (m : Message) => request(SendMessage(m.source, "Passo")) ))

  commands.foreach(t => {
                     onCommand(t._1) { implicit msg =>
                       sendAudioCalandrico(t._2)
                     }
                   })

  def sendAudioCalandrico(filename : String)(implicit msg : Message) = {
    uploadingAudio
    val path = buildPath(filename)
    val mp3 = InputFile(path)
    request(SendAudio(msg.source, mp3))
  }

  def buildPath(filename : String) : Path =
    Paths.get(rootPath.toString(), "src", "main", "resources", filename)

  override def receiveMessage(message: Message) = {
    if (message.text.isDefined) {
      messageReplies.filter(t => message.text.get.toLowerCase() contains t._1).foreach(_._2(message))
    }
  }
}