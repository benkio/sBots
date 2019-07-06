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


  val messageRepliesAudio : List[(List[String], MessageHandler, MessageMatches)] = List(
    (List("maledetto")                            , "maledetto.mp3"            , ContainsOnce) ,
    (List("aiuto" , "aiutatemi" , "aiutatelo")    , "aiuto.mp3"                , ContainsOnce) ,
    (List("assolo")    , "assolo.mp3"                , ContainsOnce) ,
    (List("ritornata" , "ritornato")              , "ritornata.mp3"            , ContainsOnce) ,
    (List("merda")                                , "merda.mp3"                , ContainsOnce) ,
    (List("cobelini", "cobbolidi", "elfi", "eoni", "nani", "mandragola", "fico sacro", "betulla", "canfura", "ossa dei morti", "erbe", "radici", "messico")                                , "figuremitologiche.mp3"                , ContainsOnce) ,
    (List("ma che cazzo sto dicendo")             , "machecazzostodicendo.mp3" , ContainsAll)  ,
    (List("poveri cretini")                       , "povericretini.mp3"        , ContainsAll)  ,
    (List("ho capito")                            , "hocapito.mp3"             , ContainsAll)  ,
    (List("feelings")                             , "feelings.mp3"             , ContainsAll)  ,
    (List("avete capito")                         , "avetecapito.mp3"          , ContainsAll)  ,
    (List("due ossa")                             , "dueossa.mp3"              , ContainsAll)  ,
    (List("schifo")                               , "schifo.mp3"               , ContainsAll)  ,
    (List("pappalardo")                           , "pappalardo.mp3"           , ContainsAll)  ,
    (List("attenzione")                           , "attenzione.mp3"           , ContainsAll)  ,
    (List("ordine")                           , "ordine.mp3"           , ContainsAll)  ,
    (List("sera")                           , "sera.mp3"           , ContainsAll)  ,
    (List("venerd")                           , "venerdi.mp3"           , ContainsAll)  ,
    (List("oppura", "oppure")                           , "oppura.mp3"           , ContainsAll)  ,
    (List("pollo")                                , "pollo.mp3"                , ContainsAll)
  ).map {
    case (words, mp3file, matcher) =>
      (words, MessageHandler((m : Message) => sendAudioBenson(mp3file)(m)), matcher)
  }

  val messageRepliesGifs = List(
    (List("bravo"                                                                  ), "bravo.gif",              ContainsOnce ),
    (List("capolavoro"                                                             ), "capolavoro.gif",         ContainsOnce ),
    (List("metal"                                                             ), "metal.gif",         ContainsOnce ),
    (List("allucinante"                                                             ), "allucinante.gif",         ContainsOnce ),

    (List("mare di cazzate"                                                        ), "nonépossibile.gif",      ContainsOnce ),
    (List("porca miseria"                                                          ), "porcamiseria.gif",       ContainsOnce ),
    (List("schifoso"                                                               ), "schifoso.gif",           ContainsOnce ),
    (List("ma vattene affanculo"                                                   ), "mavatteneaffanculo.gif", ContainsOnce ),
    (List("rimpinzati", "cioccolata"                                               ), "rimpinzati.gif",         ContainsOnce ),
    (List("facendo soffrire"                                                       ), "facendosoffrire.gif",    ContainsOnce ),
    (List("mi sto sentendo male"                                                   ), "mistosentendomale.gif",  ContainsOnce ),
    (List("mi fa stare male"                                                       ), "mifastaremale.gif",      ContainsOnce ),
    (List("lunghezza d'onda"                                                       ), "lunghezzadonda.gif",     ContainsOnce ),
    (List("delirio"                                                                ), "delirio.gif",            ContainsOnce ),
    (List("paradosso"                                                              ), "paradosso.gif",          ContainsOnce ),
    (List("deficienza", "deficiente"                                               ), "deficienza.gif",         ContainsOnce ),
    (List("querelare"                                                              ), "querelare.gif",          ContainsOnce ),
    (List("non voglio nessuno"                                                     ), "nonvoglionessuno.gif",   ContainsOnce ),
    (List("cesso", "bagno"                                                         ), "alcesso.gif",            ContainsOnce ),
    (List("ciao a tutti", "ciao", "come state", "belle gioie"                      ), "ciaocomestate.gif",      ContainsOnce ),
    (List("non manca niente", "tutto"                                              ), "nonmancaniente.gif",     ContainsOnce ),
    (List("in fila"                                                                ), "mettitiinfila.gif",      ContainsOnce ),
    (List("non male"                                                               ), "nonmale.gif",            ContainsOnce ),
    (List("si sente"                                                               ), "sisente.gif",            ContainsOnce ),
    (List("non ci credete", "stronzata", "stronzate"                               ), "noncicredete.gif",       ContainsOnce ),
    (List("sto diventando pazzo", "sto diventando matto"                           ), "stodiventandopazzo.gif", ContainsOnce ),
    (List("mi pare logico", "calcolo", "matematica"                                ), "miparelogico.gif",       ContainsOnce ),
    (List("paradosso"                                     ), "paradosso.gif",          ContainsOnce ),
    (List("sorca", "lecciso", "figa"                                               ), "sorcalecciso.gif",       ContainsOnce ),
    (List("non li sopporto", "che si deve fare"                                                        ), "nonlisopporto.gif",      ContainsOnce ),
    (List("ok", "good", "show", "friends"                                          ), "okgoodshowfriends.gif",  ContainsOnce ),
    (List("delus", "delud"                                             ), "deluso.gif",             ContainsOnce ),
    (List("chi cazzo sei"                                                          ), "chicazzosei.gif",        ContainsOnce ),
    (List("non me ne fotte", "chissenefrega", "non mi importa", "non mi interessa" ), "nonmenefotte.gif",       ContainsOnce ),
    (List("feste"                                                                  ), "feste.gif",              ContainsOnce ),
    (List("ostina", "foto vecchie", "ostini"                                       ), "ostina.gif",             ContainsOnce ),
    (List("sapere", "sapevo", "sapesse", "saperlo", "non lo so", "aristotele"      ), "sodinonsapere.gif",      ContainsOnce ),
    (List("vecchio", "vecchia"                                                     ), "vecchio.gif",            ContainsOnce ),
    (List("pagare", "paga", "soldi", "bollette", "tasse",  "bolletta", "tassa"     ),"cacciaisoldi.gif",        ContainsOnce ),
    (List("venite qua"                                                             ), "venitequa.gif",          ContainsOnce ),
    (List("lasciami in pace"                                          ), "lasciamiinpace.gif",          ContainsOnce ),
    (List("mortacci vostri"                                                             ), "mortaccivostri.gif",          ContainsOnce ),
    (List("danz", "macabr", "ballar" ), "danzamacabra.gif",          ContainsOnce ),

    (List("sei cambiat"                                                             ), "seicambiata.gif",          ContainsOnce ),
    (List("levati"                                                             ), "levatidaicoglioni.gif",          ContainsOnce ),
    (List("discapito"                                                             ), "discapito.gif",          ContainsOnce ),
    (List("sarete coglioni voi"                                                    ), "saretecoglionivoi.gif",  ContainsOnce )
  ) map {
    case (words, gifFile, matcher) =>
      (words, MessageHandler((m : Message) => sendGifBenson(gifFile)(m)), matcher)
  }

  val messageRepliesSpecial = List(
    (List("basta"                         ), "basta.gif", "basta.mp3",                             ContainsOnce ),
    (List("spaventare"                    ), "tidevispaventare.gif", "tidevispaventare.mp3",       ContainsOnce ),
    (List("questa volta no"               ), "questavoltano.gif", "questavoltano.mp3",             ContainsAll  ),
    (List("vergogna" ), "vergogna.gif", "vergogna.mp3",          ContainsOnce ),
    (List("trasform", "cristo canaro"     ), "trasformista.gif", "trasformista.mp3",             ContainsAll  ),
    (List("masgus", "ma sgus", "ma scusa" ), "masgus.gif", "masgus.mp3",                           ContainsOnce ),
    (List("grazie", "gianni"              ), "grazie.gif", "grazie.mp3",                           ContainsOnce ),
    (List("me ne vado"                    ), "menevado.gif", "menevado.mp3",                       ContainsOnce ),
    (List("incontr"                       ), "incontrateperstrada.gif", "incontrateperstrada.mp3", ContainsOnce ),
    (List("lavora", "vecchiaccia", "pelle dura", "creatura" ), "lavoratu.gif", "lavoratu.mp3", ContainsOnce ),
    (List("infernali"                     ), "infernali.gif", "infernali.mp3",                     ContainsOnce ),
    (List("per il culo"                   ), "pigliandoperilculo.gif", "pigliandoperilculo.mp3"  ,ContainsOnce ),
    (List(emoji":lol:", emoji":rofl:"     ), "risata.gif", "risata.mp3",ContainsOnce ),
    (List("ammazza", "frocio" ), "frocio.gif", "frocio.mp3",             ContainsOnce ),
    (List("fammi questa cortesia"         ), "fammiquestacortesia.gif", "fammiquestacortesia.mp3", ContainsOnce ),
    (List("non mi sta bene"               ), "nonmistabene.gif", "nonmistabene.mp3",               ContainsOnce ),
    (List("labbra", "labbro"               ), "labbra.gif", "labbra.mp3",               ContainsOnce ),
    (List("la vita"                       ), "vitanemico.gif", "vitanemico.mp3",ContainsOnce ),
    (List("permettere"                       ), "permettere.gif", "permettere.mp3",ContainsOnce ),
    (List("le note"                       ), "note.gif", "note.mp3",ContainsOnce ),
    (List("napoli"                        ), "vivaNapoli.gif", "vivanapoli.mp3",                   ContainsOnce )
  ) map {
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
