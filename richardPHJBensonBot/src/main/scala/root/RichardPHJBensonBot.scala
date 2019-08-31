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
    handler : MessageHandler,
    matcher : MessageMatches = ContainsOnce) : Option[MessageHandler] = matcher match {
    case ContainsOnce if (keys.exists(k => messageText.toLowerCase() contains k)) => Some(handler)
    case ContainsAll if (keys.forall(k => messageText.toLowerCase() contains k)) => Some(handler)
    case _ => None
  }
}

//wrapper around the handler
case class MessageHandler(handler: Message => Future[Message])

object RichardPHJBensonBot extends TelegramBot
    with Polling
    with Commands
    with ChatActions
    with Messages {

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


  val messageRepliesAudioData : List[(List[String], String, MessageMatches)] = List(
    (List("sarai maledetto", "tu sia maledetto"  ), "maledetto.mp3",            ContainsOnce),
    (List("io ti aiuto"                          ), "aiuto.mp3",                ContainsOnce),
    (List("assolo", "chitarra", "ghidarra"       ), "assolo.mp3",               ContainsOnce),
    (List("ci sei ritornata" , "ci sei ritornato"), "ritornata.mp3",            ContainsOnce),
    (List("anche la merda", "senza culo"         ), "merda.mp3",                ContainsOnce),
    (List("cobelini", "cobbolidi", "elfi",
      "nani", "la mandragola",
      "fico sacro", " la betulla", "la canfora",
      "le ossa dei morti"                        ), "figuremitologiche.mp3",    ContainsOnce),
    (List("ma che cazzo sto dicendo", "il martell"), "machecazzostodicendo.mp3",ContainsOnce),
    (List("attenzione!!!", "attenzioneee"        ), "attenzione.mp3",           ContainsOnce),
    (List("poveri cretini", "poveri ignoranti"   ), "povericretini.mp3",        ContainsOnce),
    (List("ho capito", "ho gabido"               ), "hocapito.mp3",             ContainsOnce),
    (List("avete capito", "avete gabido"         ), "avetecapito.mp3",          ContainsOnce),
    (List("feelings"                             ), "feelings.mp3",             ContainsAll),
    (List("due ossa"                             ), "dueossa.mp3",              ContainsAll),
    (List("proprio schifo"                       ), "schifo.mp3",               ContainsAll),
    (List("pappalardo"                           ), "pappalardo.mp3",           ContainsAll),
    (List("è un ordine"                          ), "ordine.mp3",               ContainsAll),
    (List("una sera"                             ), "sera.mp3",                 ContainsAll),
    (List("il venerdì"                           ), "venerdi.mp3",              ContainsAll),
    (List("oppura"                               ), "oppura.mp3",               ContainsOnce),
    (List("volevo un pollo"                      ), "pollo.mp3",                ContainsAll),
    (List("canzonette", "balera", "sagra",
      "condominiali", "piazza"                   ), "canzonette.mp3",           ContainsOnce)
  )

  val messageRepliesAudio : List[(List[String], MessageHandler, MessageMatches)] =
    messageRepliesAudioData.map {
      case (words, mp3file, matcher) =>
        (words, MessageHandler((m : Message) => sendAudioBenson(mp3file)(m)), matcher)
    }

  val messageRepliesGifsData : List[(List[String], String, MessageMatches)]  = List(
    (List("bravo!!!", "bravooo"                 ), "bravo.gif",                ContainsOnce),
    (List("capolavoro"                          ), "capolavoro.gif",           ContainsOnce),
    (List(" metal"                              ), "metal.gif",                ContainsOnce),
    (List("allucinante"                         ), "allucinante.gif",          ContainsOnce),
    (List("mare di cazzate", "non è possibile"  ), "nonépossibile.gif",        ContainsOnce),
    (List("porca miseria"                       ), "porcamiseria.gif",         ContainsOnce),
    (List("schifoso!!!", "schifosooo"           ), "schifoso.gif",             ContainsOnce),
    (List("vattene affanculo",
      "vattene a fanculo"                       ), "mavatteneaffanculo.gif",   ContainsOnce),
    (List("rimpinzati",
      "cioccolata", "pandori", "gioggolada"     ), "rimpinzati.gif",           ContainsOnce),
    (List("facendo soffrire"                    ), "facendosoffrire.gif",      ContainsOnce),
    (List("sentendo male"                       ), "mistosentendomale.gif",    ContainsOnce),
    (List("stare male"                          ), "mifastaremale.gif",        ContainsOnce),
    (List("lunghezza d'onda"                    ), "lunghezzadonda.gif",       ContainsOnce),
    (List("delirio"                             ), "delirio.gif",              ContainsOnce),
    (List("paradosso"                           ), "paradosso.gif",            ContainsOnce),
    (List("pensa alla deficienza",
      "ma si può dire una cosa del genere"      ), "deficienza.gif",           ContainsOnce),
    (List("querelare", "guerelare"              ), "querelare.gif",            ContainsOnce),
    (List("cantate", "arigliano"                ), "arigliano.gif",            ContainsOnce),
    (List("non voglio nessuno"                  ), "nonvoglionessuno.gif",     ContainsOnce),
    (List("andati al cesso", "diecimila volte"  ), "alcesso.gif",              ContainsOnce),
    (List("ciao a tutti",
      "come state", "belle gioie"               ), "ciaocomestate.gif",        ContainsOnce),
    (List("non manca niente", "c'é tutto"       ), "nonmancaniente.gif",       ContainsOnce),
    (List("in fila"                             ), "mettitiinfila.gif",        ContainsOnce),
    (List("non male"                            ), "nonmale.gif",              ContainsOnce),
    (List("perché si sente"                     ), "sisente.gif",              ContainsOnce),
    (List("non ci credete?", "grande stronzata",
      "grande stronzate"                        ), "noncicredete.gif",         ContainsOnce),
    (List("sto diventando pazzo",
      "sto diventando matto"                    ), "stodiventandopazzo.gif",   ContainsOnce),
    (List("mi pare logico", "calcolo",
      "matematica", "loggigo"                   ), "miparelogico.gif",         ContainsOnce),
    (List("sorca", "lecciso", "figa"            ), "sorcalecciso.gif",         ContainsOnce),
    (List("non li sopporto", "che si deve fare" ), "nonlisopporto.gif",        ContainsOnce),
    (List("ok", "good", "show", "friends"       ), "okgoodshowfriends.gif",    ContainsOnce),
    (List("delus", "delud"                      ), "deluso.gif",               ContainsOnce),
    (List("chi cazzo sei"                       ), "chicazzosei.gif",          ContainsOnce),
    (List("non me ne fotte", "chissenefrega",
      "non mi interessa"                        ), "nonmenefotte.gif",         ContainsOnce),
    (List("feste"                               ), "feste.gif",                ContainsOnce),
    (List("si ostina", "foto vecchie"           ), "ostina.gif",               ContainsOnce),
    (List("non sapere", "aristotele",
      "aristodele"                              ), "sodinonsapere.gif",        ContainsOnce),
    (List("vecchio", "vecchia"                  ), "vecchio.gif",              ContainsOnce),
    (List("pagare", "paga", "soldi", "bollette",
      "tasse",  "bolletta", "tassa"             ),"cacciaisoldi.gif",          ContainsOnce),
    (List("fate come vi pare", "sti cazzi",
      "sti gazzi"                               ), "comevipare.gif",           ContainsOnce),
    (List("venite qua"                          ), "venitequa.gif",            ContainsOnce),
    (List("sputo", "sputa"                      ), "sputo.gif",                ContainsOnce),
    (List("certo", "escerto", "critiche",
      "non me ne frega un cazzo"                ), "escerto.gif",              ContainsOnce),
    (List("sorriso", emoji":smile:"             ), "sorriso.gif",              ContainsOnce),
    (List("lasciami in pace", "stronz"          ), "lasciamiinpace.gif",       ContainsOnce),
    (List("mortacci vostri"                     ), "mortaccivostri.gif",       ContainsOnce),
    (List(" danza", "macabra", " ball"          ), "danzamacabra.gif",         ContainsOnce),
    (List("sei cambiat", "sei gambiat"          ), "seicambiata.gif",          ContainsOnce),
    (List("levati dai coglioni",
      "fuori dai coglioni"                      ), "levatidaicoglioni.gif",    ContainsOnce),
    (List("mio discapito", "disgabido"          ), "discapito.gif",            ContainsOnce),
    (List("peggio del peggio"                   ), "peggiodelpeggio.gif",      ContainsOnce),
    (List("non sono coglione", "non sono mica coglione", "sarete coglioni voi"
      "sarete cojoni voi"                       ), "saretecoglionivoi.gif",    ContainsOnce),
    (List("cosa squallida", "abbia mai sentito" ), "squallida.gif",            ContainsOnce),
    (List("la verità"                           ), "verita.gif",               ContainsOnce),
    (List("ti dovresti vergognare"              ), "tidovrestivergognare.gif", ContainsOnce),
    (List("oddio mio no", "dio mio no"          ), "oddiomio.gif",             ContainsOnce),
    (List("destino", "incontrare"               ), "destino.gif",              ContainsOnce),
    (List("meridionale", "terron"               ), "meridionale.gif",          ContainsOnce),
    (List("baci", "limonare", "peggio cose"     ), "bacio.gif",                ContainsOnce),
    (List("esperiment", "1, 2, 3",
      "uno, due, tre"                           ), "esperimento.gif",          ContainsOnce),
    (List("giudica"                             ), "giudicate.gif",            ContainsOnce),
    (List("ester", "esposito"                   ), "ester.gif",                ContainsOnce)
  )

  val messageRepliesGifs : List[(List[String], MessageHandler, MessageMatches)] =
    messageRepliesGifsData map {
      case (words, gifFile, matcher) =>
        (words, MessageHandler((m : Message) => sendGifBenson(gifFile)(m)), matcher)
    }

  val messageRepliesSpecialData : List[(List[String], String, String, MessageMatches)] = List(
    (List("basta!!!"                                                       ), "basta.gif", "basta.mp3",                             ContainsOnce),
    (List("ti devi spaventare"                                             ), "tidevispaventare.gif", "tidevispaventare.mp3",       ContainsOnce),
    (List("questa volta no"                                                ), "questavoltano.gif", "questavoltano.mp3",             ContainsAll),
    (List("una vergogna"                                                   ), "vergogna.gif", "vergogna.mp3",                       ContainsOnce),
    (List("mi devo trasformare", "cristo canaro"                           ), "trasformista.gif", "trasformista.mp3",               ContainsOnce),
    (List("masgus", "ma sgus", "ma scusa"                                  ), "masgus.gif", "masgus.mp3",                           ContainsOnce),
    (List("grazie", "gianni", "ciaoo"                                      ), "grazie.gif", "grazie.mp3",                           ContainsOnce),
    (List("me ne vado"                                                     ), "menevado.gif", "menevado.mp3",                       ContainsOnce),
    (List("stare attenti", "per strada"                                    ), "incontrateperstrada.gif", "incontrateperstrada.mp3", ContainsOnce),
    (List("lavora tu vecchiaccia", "hai la pelle dura", "io sono creatura" ), "lavoratu.gif", "lavoratu.mp3",                       ContainsOnce),
    (List("infernali!!!!", "infernaliii"                                   ), "infernali.gif", "infernali.mp3",                     ContainsOnce),
    (List("per il culo"                                                    ), "pigliandoperilculo.gif", "pigliandoperilculo.mp3",   ContainsOnce),
    (List(emoji":lol:", emoji":rofl:"                                      ), "risata.gif", "risata.mp3",                           ContainsOnce),
    (List("ammazza che sei", "quasi un frocio"                             ), "frocio.gif", "frocio.mp3",                           ContainsOnce),
    (List("fammi questa cortesia"                                          ), "fammiquestacortesia.gif", "fammiquestacortesia.mp3", ContainsOnce),
    (List("non mi sta bene"                                                ), "nonmistabene.gif", "nonmistabene.mp3",               ContainsOnce),
    (List("sai fare le labbra", "manco le labbra",
      "neanche le labbra"                                                  ), "labbra.gif", "labbra.mp3",                           ContainsOnce),
    (List("la vita è il nemico"                                            ), "vitanemico.gif", "vitanemico.mp3",                   ContainsOnce),
    (List("permettere"                                                     ), "permettere.gif", "permettere.mp3",                   ContainsOnce),
    (List("le note"                                                        ), "note.gif", "note.mp3",                               ContainsOnce),
    (List("viva napoli"                                                    ), "vivaNapoli.gif", "vivanapoli.mp3",                   ContainsOnce)
  )

  val messageRepliesSpecial : List[(List[String], MessageHandler, MessageMatches)] =
    messageRepliesSpecialData map {
      case (words, gifFile, mp3file, matcher) =>
        (words, MessageHandler((m : Message) => {
          sendAudioBenson(mp3file)(m)
          sendGifBenson(gifFile)(m)
        }), matcher)
    }

  // Map contains the list of keywords to match, the related messageHandler and
  // the Message matches.
  val messageReplies : List[(List[String], MessageHandler, MessageMatches)] =
    messageRepliesAudio ++ messageRepliesGifs ++ messageRepliesSpecial

  onMessage((message : Message) =>
    message.text.map { m =>
      messageReplies
        .flatMap(t => MessageMatches.getHandler(t._1, m, t._2, t._3).toList)
        .foreach(_.handler(message))
    }
  )
}
