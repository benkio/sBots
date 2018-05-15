///////////////////////////////////////////////////////////////////////////////
//             Bot Calandico, contains all the calandric frases              //
///////////////////////////////////////////////////////////////////////////////
package root

import info.mukel.telegrambot4s._, api._, methods._, models._, declarative._
import java.nio.file.Path
import java.nio.file.Paths
import scala.io.Source
import io.github.todokr.Emojipolation._

object CalandroBot extends TelegramBot
    with Polling
    with Commands
    with ChatActions
    with Messages{

  lazy val token = scala.util.Properties
    .envOrNone("BOT_TOKEN")
    .getOrElse(Source.fromResource("bot.token").getLines().mkString)

  override val ignoreCommandReceiver = true

  val rootPath = Paths.get("").toAbsolutePath()

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

  val messageReplies : List[(List[String], Message => Unit)] =
    List((List("sbrighi"), (m : Message) => reply("Passo")(m)),
         (List("gay", "frocio", "culattone", "ricchione"), (m : Message) => reply("CHE SCHIFO!!!")(m)),
         (List("caldo", "scotta"), (m : Message) => reply("Come i carbofreni della Brembo!!")(m)),
         (List("ciao", "buongiorno", "salve"), (m : Message) => reply("Buongiorno Signori")(m)),
         (List("film"), (m : Message) => reply("Lo riguardo volentieri")(m)),
         (List("stasera"), (m : Message) => reply("Facciamo qualcosa tutti assieme?")(m)),
         (List("hd", "nitido", "nitidezza", "alta definizione"), (m : Message) => reply("Eh sì, vedi...si nota l'indecisione dell'immagine  ")(m)),
         (List("qualità"), (m : Message) => reply("A 48x masterizza meglio")(m)),
         (List("macchina", "automobile"), (m : Message) => reply("Hai visto l'ultima puntata di \"Top Gear\"?")(m)),
         (List("figa"), (m : Message) => reply("Io so come fare con le donne...ho letto tutto...")(m)),
         (List("ambulanza", emoji":ambulance:"), (m : Message) => reply(emoji":triumph: :horns_sign: :hand_with_index_and_middle_fingers_crossed: :hand_with_index_and_middle_fingers_crossed: :horns_sign: :triumph:")(m)),
         (List("pc", "computer"), (m : Message) => reply("Il fisso performa meglio rispetto al portatile!!!")(m)))

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

  onMessage((message : Message) =>
    message.text.map { m =>
      messageReplies.filter(t =>
        t._1.exists( k =>
          m.toLowerCase() contains k))
        .foreach(_._2(message))
    }
  )
}