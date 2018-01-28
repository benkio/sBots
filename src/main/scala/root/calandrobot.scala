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

  onCommand("/porcoladro", "/porcoladro@CalandroBot") { implicit msg =>
    sendAudioCalandrico("PorcoLadro.mp3")
  }

  onCommand("/unoduetre") { implicit msg =>
    sendAudioCalandrico("unoduetre.mp3")
  }

  onCommand("/ancorauna") { implicit msg =>
    sendAudioCalandrico("AncoraUnaDoveLaMetto.mp3")
  }

  onCommand("/lacipolla") { implicit msg =>
    sendAudioCalandrico("CipollaCalandrica.mp3")
  }

  onCommand("/lavorogiusto") { implicit msg =>
    sendAudioCalandrico("IlLavoroVaPagato.mp3")
  }

  onCommand("/motivazioniinternet") { implicit msg =>
    sendAudioCalandrico("InternetMotivazioniCalandriche.mp3")
  }

  onCommand("/cazzomene") { implicit msg =>
    sendAudioCalandrico("IoSonVaccinato.mp3")
  }

  onCommand("/arrivoarrivo") { implicit msg =>
    sendAudioCalandrico("SubmissionCalandra.mp3")
  }

  onCommand("/vaginadepilata") { implicit msg =>
    sendAudioCalandrico("VaginaDepilataCalandra.mp3")
  }

  onCommand("/whawha_fallout4") { implicit msg =>
    sendAudioCalandrico("waawahaawha.mp3")
  }

  onCommand("/whawha_short") { implicit msg =>
    sendAudioCalandrico("wwhaaawhaaa Singolo.mp3")
  }

  def sendAudioCalandrico(filename : String)(implicit msg : Message) = {
    uploadingAudio
    val path = buildPath(filename)
    val mp3 = InputFile(path)
    request(SendAudio(msg.source, mp3))
  }

  def buildPath(filename : String) : Path =
    Paths.get(rootPath.toString(), "src", "main", "resources", filename)
}