///////////////////////////////////////////////////////////////////////////////
//             Bot di Richard Benson, contains all the Benson's frases       //
///////////////////////////////////////////////////////////////////////////////
package root

import com.benkio.telegramBotInfrastructure._
import io.github.todokr.Emojipolation._
import com.benkio.telegramBotInfrastructure.model.MediaFile
import com.benkio.telegramBotInfrastructure.model.ReplyBundleMessage
import com.benkio.telegramBotInfrastructure.model.ReplyBundleCommand
import com.benkio.telegramBotInfrastructure.model.TextTrigger
import com.benkio.telegramBotInfrastructure.model.CommandTrigger
import com.benkio.telegramBotInfrastructure.model.TextReply
import com.benkio.telegramBotInfrastructure.model.RandomSelection

object RichardPHJBensonBot extends BotSkeleton {

  val messageRepliesAudioData: List[ReplyBundleMessage] = List(
    ReplyBundleMessage(
      TextTrigger(List("sono finito", "ultimo stadio", "stanco", "non vedo")),
      List(MediaFile("stanco.mp3"))
    ),
    ReplyBundleMessage(TextTrigger(List("negozio", "pantaloni", "shopping")), List(MediaFile("pantaloni.mp3"))),
    ReplyBundleMessage(TextTrigger(List("brutto frocio")), List(MediaFile("bruttofrocio.mp3"))),
    ReplyBundleMessage(
      TextTrigger(List("questo ragazzo", "eccitare", "lucio dalla")),
      List(MediaFile("luciodalla.mp3"))
    ),
    ReplyBundleMessage(TextTrigger(List("chiesa", "preghiera", "non credo")), List(MediaFile("chiesa.mp3"))),
    ReplyBundleMessage(TextTrigger(List("maledetto")), List(MediaFile("maledetto.mp3"))),
    ReplyBundleMessage(TextTrigger(List("io ti aiuto")), List(MediaFile("aiuto.mp3"))),
    ReplyBundleMessage(TextTrigger(List("ci sei ritornata", "ci sei ritornato")), List(MediaFile("ritornata.mp3"))),
    ReplyBundleMessage(TextTrigger(List("anche la merda", "senza culo")), List(MediaFile("merda.mp3"))),
    ReplyBundleMessage(
      TextTrigger(
        List(
          "cobelini",
          "cobbolidi",
          "elfi",
          "nani",
          "la mandragola",
          "gobellini",
          "fico sacro",
          "la betulla",
          "la canfora",
          "le ossa dei morti"
        )
      ),
      List(MediaFile("figuremitologiche.mp3"))
    ),
    ReplyBundleMessage(TextTrigger(List("attenzione!!!", "attenzioneee")), List(MediaFile("attenzione.mp3"))),
    ReplyBundleMessage(TextTrigger(List("poveri cretini", "poveri ignoranti")), List(MediaFile("povericretini.mp3"))),
    ReplyBundleMessage(TextTrigger(List("feelings")), List(MediaFile("feelings.mp3"))),
    ReplyBundleMessage(TextTrigger(List("due ossa")), List(MediaFile("dueossa.mp3"))),
    ReplyBundleMessage(TextTrigger(List("proprio schifo")), List(MediaFile("schifo.mp3"))),
    ReplyBundleMessage(TextTrigger(List("pappalardo")), List(MediaFile("pappalardo.mp3"))),
    ReplyBundleMessage(TextTrigger(List("è un ordine")), List(MediaFile("ordine.mp3"))),
    ReplyBundleMessage(TextTrigger(List("una sera")), List(MediaFile("sera.mp3"))),
    ReplyBundleMessage(TextTrigger(List("il venerdì")), List(MediaFile("venerdi.mp3"))),
    ReplyBundleMessage(TextTrigger(List("oppura")), List(MediaFile("oppura.mp3"))),
    ReplyBundleMessage(TextTrigger(List("volevo un pollo")), List(MediaFile("pollo.mp3"))),
    ReplyBundleMessage(
      TextTrigger(List("canzonette", "balera", "sagra", "condominiali", "piazza")),
      List(MediaFile("canzonette.mp3"))
    )
  )

  val messageRepliesGifsData: List[ReplyBundleMessage] = List(
    ReplyBundleMessage(TextTrigger(List("hollywood")), List(MediaFile("hollywood.gif"))),
    ReplyBundleMessage(TextTrigger(List("bebop", "be bop", "aluba", "my baby")), List(MediaFile("bebop.gif"))),
    ReplyBundleMessage(TextTrigger(List("succh", "olio di croce")), List(MediaFile("olioDiCroce.gif"))),
    ReplyBundleMessage(TextTrigger(List("c'ha ragione", "o no?")), List(MediaFile("ragione.gif"))),
    ReplyBundleMessage(TextTrigger(List("micetta")), List(MediaFile("micetta.gif"))),
    ReplyBundleMessage(TextTrigger(List("altri generi musicali")), List(MediaFile("generiMusicali.gif"))),
    ReplyBundleMessage(TextTrigger(List("ti distruggo")), List(MediaFile("tiDistruggo.gif"))),
    ReplyBundleMessage(TextTrigger(List("bassista", "slap")), List(MediaFile("bassista.gif"))),
    ReplyBundleMessage(TextTrigger(List("discoteca laziale")), List(MediaFile("discotecaLaziale.gif"))),
    ReplyBundleMessage(TextTrigger(List("piloti d'aereo", "disasti aerei")), List(MediaFile("drogatiPiloti.gif"))),
    ReplyBundleMessage(
      TextTrigger(List("timore", "paura", "dirigenti", "diriggendi", "diriggenti")),
      List(MediaFile("dirigenti.gif"))
    ),
    ReplyBundleMessage(TextTrigger(List("cosa è successo")), List(MediaFile("cosaSuccesso.gif"))),
    ReplyBundleMessage(TextTrigger(List("perchè mi guardi")), List(MediaFile("guardi.gif"))),
    ReplyBundleMessage(TextTrigger(List("non è roba per me")), List(MediaFile("robaPerMe.gif"))),
    ReplyBundleMessage(
      TextTrigger(List("chitarrista preferito", "ghidarrista preferito")),
      List(MediaFile("chitarrista.gif"))
    ),
    ReplyBundleMessage(TextTrigger(List("non sai molto")), List(MediaFile("nonSaiMolto.gif"))),
    ReplyBundleMessage(TextTrigger(List("divert")), List(MediaFile("diverti.gif"))),
    ReplyBundleMessage(TextTrigger(List("e parla")), List(MediaFile("parla.gif"))),
    ReplyBundleMessage(TextTrigger(List("quello che dico io")), List(MediaFile("quelloCheDicoIo.gif"))),
    ReplyBundleMessage(TextTrigger(List("uno scherzo")), List(MediaFile("scherzo.gif"))),
    ReplyBundleMessage(
      TextTrigger(List("vi calpesto", "ultimi degli ultimi", "vermi", "strisciate")),
      List(MediaFile("viCalpesto.gif"))
    ),
    ReplyBundleMessage(
      TextTrigger(List("rimpinzati", "cioccolata", "pandori", "gioggolada")),
      List(MediaFile("rimpinzati.gif"))
    ),
    ReplyBundleMessage(
      TextTrigger(List("pensa alla deficienza", "ma si può dire una cosa del genere")),
      List(MediaFile("deficienza.gif"))
    ),
    ReplyBundleMessage(
      TextTrigger(List("pagare", "paga", "soldi", "bollette", "tasse", "bolletta", "tassa")),
      List(MediaFile("cacciaisoldi.gif"))
    ),
    ReplyBundleMessage(
      TextTrigger(List("fate come vi pare", "sti cazzi", "sti gazzi")),
      List(MediaFile("comevipare.gif"))
    ),
    ReplyBundleMessage(
      TextTrigger(List("non sapere", "aristotele", "aristodele")),
      List(MediaFile("sodinonsapere.gif"))
    ),
    ReplyBundleMessage(
      TextTrigger(List("non ci credete?", "grande stronzata", "grande stronzate")),
      List(MediaFile("noncicredete.gif"))
    ),
    ReplyBundleMessage(
      TextTrigger(List("mi pare logico", "calcolo", "matematica", "loggigo")),
      List(MediaFile("miparelogico.gif"))
    ),
    ReplyBundleMessage(
      TextTrigger(List("non me ne fotte", "chissenefrega", "non mi interessa")),
      List(MediaFile("nonmenefotte.gif"))
    ),
    ReplyBundleMessage(
      TextTrigger(List("certo", "escerto", "critiche", "non me ne frega un cazzo")),
      List(MediaFile("escerto.gif"))
    ),
    ReplyBundleMessage(
      TextTrigger(List("levati dai coglioni", "fuori dai coglioni")),
      List(MediaFile("levatidaicoglioni.gif"))
    ),
    ReplyBundleMessage(
      TextTrigger(
        List(
          "non sono coglione",
          "non sono mica coglione",
          "non sono cojone",
          "non sono mica cojone",
          "sarete coglioni voi",
          "sarete cojoni voi",
          "mica so coglione",
          "mica sono coglione",
          "mica so cojone",
          "mica sono cojone"
        )
      ),
      List(MediaFile("saretecoglionivoi.gif"))
    ),
    ReplyBundleMessage(TextTrigger(List("esperiment", "1, 2, 3", "uno, due, tre")), List(MediaFile("esperimento.gif"))),
    ReplyBundleMessage(TextTrigger(List("bravo!!!", "bravooo")), List(MediaFile("bravo.gif"))),
    ReplyBundleMessage(TextTrigger(List("capolavoro")), List(MediaFile("capolavoro.gif"))),
    ReplyBundleMessage(TextTrigger(List(" metal")), List(MediaFile("metal.gif"))),
    ReplyBundleMessage(TextTrigger(List("allucinante")), List(MediaFile("allucinante.gif"))),
    ReplyBundleMessage(TextTrigger(List("mare di cazzate", "non è possibile")), List(MediaFile("nonépossibile.gif"))),
    ReplyBundleMessage(TextTrigger(List("porca miseria")), List(MediaFile("porcamiseria.gif"))),
    ReplyBundleMessage(TextTrigger(List("schifoso!!!", "schifosooo")), List(MediaFile("schifoso.gif"))),
    ReplyBundleMessage(TextTrigger(List("facendo soffrire")), List(MediaFile("facendosoffrire.gif"))),
    ReplyBundleMessage(TextTrigger(List("sentendo male")), List(MediaFile("mistosentendomale.gif"))),
    ReplyBundleMessage(TextTrigger(List("stare male")), List(MediaFile("mifastaremale.gif"))),
    ReplyBundleMessage(TextTrigger(List("lunghezza d'onda", "brave persone")), List(MediaFile("lunghezzadonda.gif"))),
    ReplyBundleMessage(TextTrigger(List("delirio")), List(MediaFile("delirio.gif"))),
    ReplyBundleMessage(TextTrigger(List("paradosso")), List(MediaFile("paradosso.gif"))),
    ReplyBundleMessage(TextTrigger(List("querelare", "guerelare")), List(MediaFile("querelare.gif"))),
    ReplyBundleMessage(TextTrigger(List("cantate", "arigliano")), List(MediaFile("arigliano.gif"))),
    ReplyBundleMessage(TextTrigger(List("non voglio nessuno")), List(MediaFile("nonvoglionessuno.gif"))),
    ReplyBundleMessage(TextTrigger(List("andati al cesso", "diecimila volte")), List(MediaFile("alcesso.gif"))),
    ReplyBundleMessage(TextTrigger(List("non manca niente", "c'é tutto")), List(MediaFile("nonmancaniente.gif"))),
    ReplyBundleMessage(TextTrigger(List("in fila")), List(MediaFile("mettitiinfila.gif"))),
    ReplyBundleMessage(TextTrigger(List("non male")), List(MediaFile("nonmale.gif"))),
    ReplyBundleMessage(TextTrigger(List("perché si sente")), List(MediaFile("sisente.gif"))),
    ReplyBundleMessage(TextTrigger(List("colpa vostra", "pazzo", "matto")), List(MediaFile("stodiventandopazzo.gif"))),
    ReplyBundleMessage(TextTrigger(List("sorca", "lecciso", "figa")), List(MediaFile("sorcalecciso.gif"))),
    ReplyBundleMessage(
      TextTrigger(List("non li sopporto", "che si deve fare", "che se deve fa per", "bisogna pure lavorà")),
      List(MediaFile("nonlisopporto.gif"))
    ),
    ReplyBundleMessage(TextTrigger(List("good", "show", "friends")), List(MediaFile("okgoodshowfriends.gif"))),
    ReplyBundleMessage(TextTrigger(List("delus", "delud")), List(MediaFile("deluso.gif"))),
    ReplyBundleMessage(TextTrigger(List("chi cazzo sei")), List(MediaFile("chicazzosei.gif"))),
    ReplyBundleMessage(TextTrigger(List("feste")), List(MediaFile("feste.gif"))),
    ReplyBundleMessage(TextTrigger(List("si ostina", "foto vecchie")), List(MediaFile("ostina.gif"))),
    ReplyBundleMessage(TextTrigger(List("vecchio", "vecchia")), List(MediaFile("vecchio.gif"))),
    ReplyBundleMessage(TextTrigger(List("venite qua")), List(MediaFile("venitequa.gif"))),
    ReplyBundleMessage(TextTrigger(List("scatta qualcosa")), List(MediaFile("scattaQualcosa.gif"))),
    ReplyBundleMessage(TextTrigger(List("sputo", "sputa")), List(MediaFile("sputo.gif"))),
    ReplyBundleMessage(TextTrigger(List("lasciami in pace", "stronz")), List(MediaFile("lasciamiinpace.gif"))),
    ReplyBundleMessage(TextTrigger(List("sono arrivato", "arivado", "piacere")), List(MediaFile("arivado.gif"))),
    ReplyBundleMessage(TextTrigger(List("pure bona")), List(MediaFile("bona.gif"))),
    ReplyBundleMessage(TextTrigger(List("mortacci vostri")), List(MediaFile("mortaccivostri.gif"))),
    ReplyBundleMessage(TextTrigger(List(" danza", "macabra", " ball")), List(MediaFile("danzamacabra.gif"))),
    ReplyBundleMessage(TextTrigger(List("sei cambiat", "sei gambiat")), List(MediaFile("seicambiata.gif"))),
    ReplyBundleMessage(TextTrigger(List("mio discapito", "disgabido")), List(MediaFile("discapito.gif"))),
    ReplyBundleMessage(TextTrigger(List("peggio del peggio")), List(MediaFile("peggiodelpeggio.gif"))),
    ReplyBundleMessage(TextTrigger(List("cosa squallida", "abbia mai sentito")), List(MediaFile("squallida.gif"))),
    ReplyBundleMessage(TextTrigger(List("la verità")), List(MediaFile("verita.gif"))),
    ReplyBundleMessage(TextTrigger(List("ti dovresti vergognare")), List(MediaFile("tidovrestivergognare.gif"))),
    ReplyBundleMessage(TextTrigger(List("oddio mio no", "dio mio no")), List(MediaFile("oddiomio.gif"))),
    ReplyBundleMessage(TextTrigger(List("destino", "incontrare")), List(MediaFile("destino.gif"))),
    ReplyBundleMessage(TextTrigger(List("meridionale", "terron")), List(MediaFile("meridionale.gif"))),
    ReplyBundleMessage(TextTrigger(List("baci", "limonare", "peggio cose")), List(MediaFile("bacio.gif"))),
    ReplyBundleMessage(
      TextTrigger(List("solo uno parló", "criticato", "gridigado")),
      List(MediaFile("fuCriticato.gif"))
    ),
    ReplyBundleMessage(
      TextTrigger(List("giudica")),
      List(MediaFile("giudicate.gif"), MediaFile("comeFaiAGiudicare.gif")),
      replySelection = RandomSelection
    ),
    ReplyBundleMessage(
      TextTrigger(List("bastone infernale", "un'arma")),
      List(MediaFile("bastone1.gif"), MediaFile("bastone2.gif")),
      replySelection = RandomSelection
    ),
    ReplyBundleMessage(
      TextTrigger(List("droga")),
      List(MediaFile("drogatiRockettari1.gif"), MediaFile("drogatiRockettari2.gif"), MediaFile("drogatiPiloti.gif")),
      replySelection = RandomSelection
    ),
    ReplyBundleMessage(
      TextTrigger(List("rockettari", "rocchettari", "stillati")),
      List(MediaFile("drogatiRockettari1.gif"), MediaFile("drogatiRockettari2.gif")),
      replySelection = RandomSelection
    ),
    ReplyBundleMessage(
      TextTrigger(List("sguardo")),
      List(
        MediaFile("sguardo1.gif"),
        MediaFile("sguardo2.gif"),
        MediaFile("sguardo3.gif"),
        MediaFile("sguardo4.gif")
      ),
      replySelection = RandomSelection
    ),
    ReplyBundleMessage(TextTrigger(List("a quel punto")), List(MediaFile("quelPunto.gif"))),
    ReplyBundleMessage(TextTrigger(List("errori")), List(MediaFile("maierrori.gif"))),
    ReplyBundleMessage(TextTrigger(List("quattro", " 4 ", "in tempo")), List(MediaFile("quattroSolo.gif"))),
    ReplyBundleMessage(
      TextTrigger(List("la parte", "recitare", "attore", "attrice")),
      List(MediaFile("faccioLaParte.gif"))
    ),
    ReplyBundleMessage(TextTrigger(List("assolutamente no", "non mi lamento")), List(MediaFile("nonMiLamento.gif"))),
    ReplyBundleMessage(TextTrigger(List("inizio della fine")), List(MediaFile("inizioDellaFine.gif"))),
    ReplyBundleMessage(TextTrigger(List("il senso")), List(MediaFile("ilSensoCapito.gif"))),
    ReplyBundleMessage(TextTrigger(List(" ester ", "esposito")), List(MediaFile("ester.gif"))),
    ReplyBundleMessage(
      TextTrigger(List("abituato", "abiduado", "propriollà", "propriolla")),
      List(MediaFile("propriolla.gif"))
    )
  )

  val messageRepliesSpecialData: List[ReplyBundleMessage] = List(
    ReplyBundleMessage(
      TextTrigger(List("vattene affanculo", "vattene a fanculo")),
      List(MediaFile("mavatteneaffanculo.gif"), MediaFile("mavatteneaffanculo.mp3"))
    ),
    ReplyBundleMessage(
      TextTrigger(List("mignotta", "puttana", "troia")),
      List(MediaFile("mignotta.mp3"), MediaFile("mignotta.gif"))
    ),
    ReplyBundleMessage(
      TextTrigger(List("ti devi spaventare")),
      List(MediaFile("tidevispaventare.mp3"), MediaFile("tidevispaventare.gif"))
    ),
    ReplyBundleMessage(
      TextTrigger(List("ma che cazzo sto dicendo", "il martell")),
      List(MediaFile("machecazzostodicendo.mp3"), MediaFile("machecazzostodicendo.gif"))
    ),
    ReplyBundleMessage(
      TextTrigger(List("questa volta no")),
      List(MediaFile("questavoltano.mp3"), MediaFile("questavoltano.gif"))
    ),
    ReplyBundleMessage(TextTrigger(List("una vergogna")), List(MediaFile("vergogna.mp3"), MediaFile("vergogna.gif"))),
    ReplyBundleMessage(
      TextTrigger(List("mi devo trasformare", "cristo canaro")),
      List(MediaFile("trasformista.mp3"), MediaFile("trasformista.gif"))
    ),
    ReplyBundleMessage(
      TextTrigger(List("masgus", "ma sgus", "ma scusa")),
      List(MediaFile("masgus.mp3"), MediaFile("masgus.gif"))
    ),
    ReplyBundleMessage(TextTrigger(List("gianni", "ciaoo")), List(MediaFile("grazie.mp3"), MediaFile("grazie.gif"))),
    ReplyBundleMessage(TextTrigger(List("me ne vado")), List(MediaFile("menevado.mp3"), MediaFile("menevado.gif"))),
    ReplyBundleMessage(
      TextTrigger(List("stare attenti", "per strada")),
      List(MediaFile("incontrateperstrada.mp3"), MediaFile("incontrateperstrada.gif"))
    ),
    ReplyBundleMessage(
      TextTrigger(List("lavora tu vecchiaccia", "hai la pelle dura", "io sono creatura")),
      List(MediaFile("lavoratu.mp3"), MediaFile("lavoratu.gif"))
    ),
    ReplyBundleMessage(
      TextTrigger(List("infernali!!!!", "infernaliii")),
      List(MediaFile("infernali.mp3"), MediaFile("infernali.gif"))
    ),
    ReplyBundleMessage(
      TextTrigger(List("per il culo")),
      List(MediaFile("pigliandoperilculo.mp3"), MediaFile("pigliandoperilculo.gif"))
    ),
    ReplyBundleMessage(
      TextTrigger(List(emoji":lol:", emoji":rofl:", "sorriso", emoji":smile:")),
      List(
        MediaFile("risata.mp3"),
        MediaFile("risata.gif"),
        MediaFile("sorriso2.gif"),
        MediaFile("sorriso3.gif"),
        MediaFile("sorriso.gif")
      ),
      replySelection = RandomSelection
    ),
    ReplyBundleMessage(
      TextTrigger(List("ammazza che sei", "quasi un frocio")),
      List(MediaFile("frocio.mp3"), MediaFile("frocio.gif"))
    ),
    ReplyBundleMessage(
      TextTrigger(List("cortesia")),
      List(MediaFile("fammiquestacortesia.mp3"), MediaFile("fammiquestacortesia.gif"))
    ),
    ReplyBundleMessage(
      TextTrigger(List("non mi sta bene")),
      List(MediaFile("nonmistabene.mp3"), MediaFile("nonmistabene.gif"))
    ),
    ReplyBundleMessage(
      TextTrigger(List("sai fare le labbra", "manco le labbra", "neanche le labbra")),
      List(MediaFile("labbra.mp3"), MediaFile("labbra.gif"))
    ),
    ReplyBundleMessage(
      TextTrigger(List("la vita è il nemico")),
      List(MediaFile("vitanemico.mp3"), MediaFile("vitanemico.gif"))
    ),
    ReplyBundleMessage(TextTrigger(List("permettere")), List(MediaFile("permettere.mp3"), MediaFile("permettere.gif"))),
    ReplyBundleMessage(TextTrigger(List("le note")), List(MediaFile("note.mp3"), MediaFile("note.gif"))),
    ReplyBundleMessage(TextTrigger(List("terribile")), List(MediaFile("terribile.mp3"), MediaFile("terribile.gif"))),
    ReplyBundleMessage(
      TextTrigger(List("viva napoli")),
      List(MediaFile("vivaNapoli.mp3"), MediaFile("vivaNapoli.gif"))
    ),
    ReplyBundleMessage(
      TextTrigger(List("ciao a tutti", "come state", "belle gioie")),
      List(MediaFile("ciaocomestate.gif"), MediaFile("ciaocomestate.mp3"))
    ),
    ReplyBundleMessage(
      TextTrigger(List("basta!!!", "bastaaa")),
      List(
        MediaFile("basta.mp3"),
        MediaFile("basta.gif"),
        MediaFile("bastaSedia.gif")
      ),
      replySelection = RandomSelection
    ),
    ReplyBundleMessage(
      TextTrigger(
        List("assolo", "chitarra", "ghidarra")
      ),
      List(
        MediaFile("assolo.mp3"),
        MediaFile("chitarra1.gif"),
        MediaFile("chitarra2.gif")
      ),
      replySelection = RandomSelection
    ),
    ReplyBundleMessage(
      TextTrigger(List("capito ", " gabido", " capito", "gabido ", " capissi", "capissi ")),
      List(
        MediaFile("hocapito.mp3"),
        MediaFile("avetecapito.mp3"),
        MediaFile("capito.mp3"),
        MediaFile("AveteCapitoComeSempre.gif"),
        MediaFile("NonAveteCapitoUnCazzo.gif"),
        MediaFile("voiNonAveteCapitoUnCazzo.gif"),
        MediaFile("ilSensoCapito.gif"),
        MediaFile("capitoDoveStiamo.gif"),
        MediaFile("nonHoCapito.gif")
      ),
      replySelection = RandomSelection
    ),
    ReplyBundleMessage(
      TextTrigger(List("schifosi")),
      List(
        MediaFile("viCalpesto.gif"),
        MediaFile("schifosi.mp3"),
        MediaFile("schifosi.gif")
      ),
      replySelection = RandomSelection
    )
  )

  override lazy val messageRepliesData: List[ReplyBundleMessage] =
    messageRepliesAudioData ++ messageRepliesGifsData ++ messageRepliesSpecialData

  override lazy val commandRepliesData: List[ReplyBundleCommand] = List(
    ReplyBundleCommand(
      trigger = CommandTrigger("triggerlist"),
      text = TextReply(
        _ =>
          messageRepliesData
            .take(messageRepliesData.length / 2)
            .map(_.trigger match {
              case TextTrigger(lt) => lt.mkString("[", " - ", "]")
              case _               => ""
            }),
        false
      )
    ),
    ReplyBundleCommand(
      trigger = CommandTrigger("triggerlist"),
      text = TextReply(
        _ =>
          messageRepliesData
            .drop(messageRepliesData.length / 2)
            .map(_.trigger match {
              case TextTrigger(lt) => lt.mkString("[", " - ", "]")
              case _               => ""
            }),
        false
      )
    ),
    ReplyBundleCommand(
      trigger = CommandTrigger("bensonify"),
      text = TextReply(
        msg => msg.text
          .filterNot(t => t.trim == "/bensonify" || t.trim == "/bensonify@RichardPHJBensonBot")
          .map(t => List(Bensonify.compute(t.drop(11)))).getOrElse(List("E PARLAAAAAAA!!!!")),
        true
      )
    )
  )
}
