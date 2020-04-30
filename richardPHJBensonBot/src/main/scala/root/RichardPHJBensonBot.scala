///////////////////////////////////////////////////////////////////////////////
//             Bot di Richard Benson, contains all the Benson's frases       //
///////////////////////////////////////////////////////////////////////////////
package root

import com.benkio.telegramBotInfrastructure._
import io.github.todokr.Emojipolation._
import com.benkio.telegramBotInfrastructure.model._

object RichardPHJBensonBot extends BotSkeleton {

  val messageRepliesAudioData: List[ReplyBundleMessage] = List(
    ReplyBundleMessage(
      TextTrigger(
        List(
          StringTextTriggerValue("sono finito"),
          StringTextTriggerValue("ultimo stadio"),
          StringTextTriggerValue("stanco"),
          StringTextTriggerValue("non vedo")
        )
      ),
      List(MediaFile("stanco.mp3"))
    ),
    ReplyBundleMessage(
      TextTrigger(
        List(StringTextTriggerValue("negozio"), StringTextTriggerValue("pantaloni"), StringTextTriggerValue("shopping"))
      ),
      List(MediaFile("pantaloni.mp3"))
    ),
    ReplyBundleMessage(TextTrigger(List(StringTextTriggerValue("brutto frocio"))), List(MediaFile("bruttofrocio.mp3"))),
    ReplyBundleMessage(
      TextTrigger(
        List(
          StringTextTriggerValue("questo ragazzo"),
          StringTextTriggerValue("eccitare"),
          StringTextTriggerValue("lucio dalla")
        )
      ),
      List(MediaFile("luciodalla.mp3"))
    ),
    ReplyBundleMessage(
      TextTrigger(
        List(StringTextTriggerValue("chiesa"), StringTextTriggerValue("preghiera"), StringTextTriggerValue("non credo"))
      ),
      List(MediaFile("chiesa.mp3"))
    ),
    ReplyBundleMessage(TextTrigger(List(StringTextTriggerValue("maledetto"))), List(MediaFile("maledetto.mp3"))),
    ReplyBundleMessage(TextTrigger(List(StringTextTriggerValue("io ti aiuto"))), List(MediaFile("aiuto.mp3"))),
    ReplyBundleMessage(TextTrigger(List(StringTextTriggerValue("faccio schifo"))), List(MediaFile("faccioSchifo.mp3"))),
    ReplyBundleMessage(
      TextTrigger(List(StringTextTriggerValue("ci sei ritornata"), StringTextTriggerValue("ci sei ritornato"))),
      List(MediaFile("ritornata.mp3"))
    ),
    ReplyBundleMessage(
      TextTrigger(List(StringTextTriggerValue("anche la merda"), StringTextTriggerValue("senza culo"))),
      List(MediaFile("merda.mp3"))
    ),
    ReplyBundleMessage(TextTrigger(List(StringTextTriggerValue("che succede"))), List(MediaFile("cheSuccede.mp3"))),
    ReplyBundleMessage(
      TextTrigger(
        List(
          StringTextTriggerValue("cobelini"),
          StringTextTriggerValue("cobbolidi"),
          StringTextTriggerValue("elfi"),
          StringTextTriggerValue("nani"),
          StringTextTriggerValue("la mandragola"),
          StringTextTriggerValue("gobellini"),
          StringTextTriggerValue("fico sacro"),
          StringTextTriggerValue("la betulla"),
          StringTextTriggerValue("la canfora"),
          StringTextTriggerValue("le ossa dei morti")
        )
      ),
      List(MediaFile("figuremitologiche.mp3"))
    ),
    ReplyBundleMessage(
      TextTrigger(List(StringTextTriggerValue("attenzione!!!"), StringTextTriggerValue("attenzioneee"))),
      List(MediaFile("attenzione.mp3"))
    ),
    ReplyBundleMessage(
      TextTrigger(List(StringTextTriggerValue("poveri cretini"), StringTextTriggerValue("poveri ignoranti"))),
      List(MediaFile("povericretini.mp3"))
    ),
    ReplyBundleMessage(TextTrigger(List(StringTextTriggerValue("due ossa"))), List(MediaFile("dueossa.mp3"))),
    ReplyBundleMessage(TextTrigger(List(StringTextTriggerValue("proprio schifo"))), List(MediaFile("schifo.mp3"))),
    ReplyBundleMessage(TextTrigger(List(StringTextTriggerValue("pappalardo"))), List(MediaFile("pappalardo.mp3"))),
    ReplyBundleMessage(TextTrigger(List(StringTextTriggerValue("è un ordine"))), List(MediaFile("ordine.mp3"))),
    ReplyBundleMessage(TextTrigger(List(StringTextTriggerValue("una sera"))), List(MediaFile("sera.mp3"))),
    ReplyBundleMessage(TextTrigger(List(StringTextTriggerValue("il venerdì"))), List(MediaFile("venerdi.mp3"))),
    ReplyBundleMessage(TextTrigger(List(StringTextTriggerValue("oppura"))), List(MediaFile("oppura.mp3"))),
    ReplyBundleMessage(TextTrigger(List(StringTextTriggerValue("volevo un pollo"))), List(MediaFile("pollo.mp3"))),
    ReplyBundleMessage(
      TextTrigger(
        List(
          StringTextTriggerValue("canzonette"),
          StringTextTriggerValue("balera"),
          StringTextTriggerValue("sagra"),
          StringTextTriggerValue("condominiali"),
          StringTextTriggerValue("piazza")
        )
      ),
      List(MediaFile("canzonette.mp3"))
    )
  )

  val messageRepliesGifsData: List[ReplyBundleMessage] = List(
    ReplyBundleMessage(TextTrigger(List(StringTextTriggerValue("hollywood"))), List(MediaFile("hollywood.gif"))),
    ReplyBundleMessage(TextTrigger(List(StringTextTriggerValue("chi è"))), List(MediaFile("questaPersonaScusate.gif"))),
    ReplyBundleMessage(
      TextTrigger(List(StringTextTriggerValue("calcolo"), StringTextTriggerValue("matematica"))),
      List(MediaFile("miparelogico.gif"))
    ),
    ReplyBundleMessage(TextTrigger(List(StringTextTriggerValue("tucul"))), List(MediaFile("tucul.gif"))),
    ReplyBundleMessage(
      TextTrigger(
        List(
          StringTextTriggerValue(";)"),
          StringTextTriggerValue("occhiolino"),
          StringTextTriggerValue("wink"),
          StringTextTriggerValue(emoji":wink:")
        )
      ),
      List(MediaFile("occhiolino.gif"))
    ),
    ReplyBundleMessage(TextTrigger(List(StringTextTriggerValue("soffro"))), List(MediaFile("soffro.gif"))),
    ReplyBundleMessage(
      TextTrigger(
        List(
          StringTextTriggerValue("indispettirmi"),
          StringTextTriggerValue("oltrepassare"),
          StringTextTriggerValue("divento cattivo")
        )
      ),
      List(MediaFile("indispettirmi.gif"))
    ),
    ReplyBundleMessage(
      TextTrigger(List(StringTextTriggerValue("mannaggia"), StringTextTriggerValue("la salute"))),
      List(MediaFile("mannaggiaLaSalute.gif"))
    ),
    ReplyBundleMessage(
      TextTrigger(List(StringTextTriggerValue("mi rompi il cazzo"), StringTextTriggerValue("mi dai fastidio"))),
      List(MediaFile("miRompiErCazzo.gif"))
    ),
    ReplyBundleMessage(
      TextTrigger(
        List(
          StringTextTriggerValue("bebop"),
          StringTextTriggerValue("be bop"),
          StringTextTriggerValue("aluba"),
          StringTextTriggerValue("my baby")
        )
      ),
      List(MediaFile("bebop.gif"))
    ),
    ReplyBundleMessage(
      TextTrigger(List(StringTextTriggerValue("succh"), StringTextTriggerValue("olio di croce"))),
      List(MediaFile("olioDiCroce.gif"))
    ),
    ReplyBundleMessage(
      TextTrigger(List(StringTextTriggerValue("c'ha ragione"), StringTextTriggerValue("o no?"))),
      List(MediaFile("ragione.gif"))
    ),
    ReplyBundleMessage(TextTrigger(List(StringTextTriggerValue("micetta"))), List(MediaFile("micetta.gif"))),
    ReplyBundleMessage(
      TextTrigger(List(StringTextTriggerValue("vabbè"), StringTextTriggerValue("problema"))),
      List(MediaFile("vabbeProblema.gif"))
    ),
    ReplyBundleMessage(
      TextTrigger(List(StringTextTriggerValue("altri generi musicali"))),
      List(MediaFile("generiMusicali.gif"))
    ),
    ReplyBundleMessage(TextTrigger(List(StringTextTriggerValue("ti distruggo"))), List(MediaFile("tiDistruggo.gif"))),
    ReplyBundleMessage(
      TextTrigger(List(StringTextTriggerValue("bassista"), StringTextTriggerValue("slap"))),
      List(MediaFile("bassista.gif"))
    ),
    ReplyBundleMessage(
      TextTrigger(List(StringTextTriggerValue("discoteca laziale"))),
      List(MediaFile("discotecaLaziale.gif"))
    ),
    ReplyBundleMessage(
      TextTrigger(List(StringTextTriggerValue("piloti d'aereo"), StringTextTriggerValue("disasti aerei"))),
      List(MediaFile("drogatiPiloti.gif"))
    ),
    ReplyBundleMessage(
      TextTrigger(
        List(
          StringTextTriggerValue("timore"),
          StringTextTriggerValue("paura"),
          StringTextTriggerValue("dirigenti"),
          StringTextTriggerValue("diriggendi"),
          StringTextTriggerValue("diriggenti")
        )
      ),
      List(MediaFile("dirigenti.gif"))
    ),
    ReplyBundleMessage(
      TextTrigger(List(StringTextTriggerValue("cosa è successo"))),
      List(MediaFile("cosaSuccesso.gif"))
    ),
    ReplyBundleMessage(TextTrigger(List(StringTextTriggerValue("perchè mi guardi"))), List(MediaFile("guardi.gif"))),
    ReplyBundleMessage(
      TextTrigger(List(StringTextTriggerValue("non è roba per me"))),
      List(MediaFile("robaPerMe.gif"))
    ),
    ReplyBundleMessage(
      TextTrigger(
        List(StringTextTriggerValue("chitarrista preferito"), StringTextTriggerValue("ghidarrista preferito"))
      ),
      List(MediaFile("chitarrista.gif"))
    ),
    ReplyBundleMessage(TextTrigger(List(StringTextTriggerValue("non sai molto"))), List(MediaFile("nonSaiMolto.gif"))),
    ReplyBundleMessage(TextTrigger(List(StringTextTriggerValue("divert"))), List(MediaFile("diverti.gif"))),
    ReplyBundleMessage(TextTrigger(List(StringTextTriggerValue("e parla"))), List(MediaFile("parla.gif"))),
    ReplyBundleMessage(
      TextTrigger(List(StringTextTriggerValue("quello che dico io"))),
      List(MediaFile("quelloCheDicoIo.gif"))
    ),
    ReplyBundleMessage(TextTrigger(List(StringTextTriggerValue("uno scherzo"))), List(MediaFile("scherzo.gif"))),
    ReplyBundleMessage(
      TextTrigger(
        List(
          StringTextTriggerValue("vi calpesto"),
          StringTextTriggerValue("ultimi degli ultimi"),
          StringTextTriggerValue("vermi"),
          StringTextTriggerValue("strisciate")
        )
      ),
      List(MediaFile("viCalpesto.gif"))
    ),
    ReplyBundleMessage(
      TextTrigger(
        List(
          StringTextTriggerValue("rimpinzati"),
          StringTextTriggerValue("cioccolata"),
          StringTextTriggerValue("pandori"),
          StringTextTriggerValue("gioggolada")
        )
      ),
      List(MediaFile("rimpinzati.gif"))
    ),
    ReplyBundleMessage(
      TextTrigger(
        List(
          StringTextTriggerValue("pensa alla deficienza"),
          StringTextTriggerValue("ma si può dire una cosa del genere")
        )
      ),
      List(MediaFile("deficienza.gif"))
    ),
    ReplyBundleMessage(
      TextTrigger(
        List(
          StringTextTriggerValue("pagare"),
          StringTextTriggerValue("paga"),
          StringTextTriggerValue("soldi"),
          StringTextTriggerValue("bollette"),
          StringTextTriggerValue("tasse"),
          StringTextTriggerValue("bolletta"),
          StringTextTriggerValue("tassa")
        )
      ),
      List(MediaFile("cacciaisoldi.gif"))
    ),
    ReplyBundleMessage(
      TextTrigger(
        List(
          StringTextTriggerValue("fate come vi pare"),
          StringTextTriggerValue("sti cazzi"),
          StringTextTriggerValue("sti gazzi")
        )
      ),
      List(MediaFile("comevipare.gif"))
    ),
    ReplyBundleMessage(
      TextTrigger(
        List(
          StringTextTriggerValue("non sapere"),
          StringTextTriggerValue("aristotele"),
          StringTextTriggerValue("aristodele")
        )
      ),
      List(MediaFile("sodinonsapere.gif"))
    ),
    ReplyBundleMessage(
      TextTrigger(
        List(
          StringTextTriggerValue("non ci credete?"),
          StringTextTriggerValue("grande stronzata"),
          StringTextTriggerValue("grande stronzate")
        )
      ),
      List(MediaFile("noncicredete.gif"))
    ),
    ReplyBundleMessage(
      TextTrigger(
        List(
          StringTextTriggerValue("mi pare logico"),
          StringTextTriggerValue("mi sembra logico"),
          StringTextTriggerValue("me pare logico"),
          StringTextTriggerValue("me sembra logico"),
          StringTextTriggerValue("loggigo"),
          StringTextTriggerValue("logigo")
        )
      ),
      List(MediaFile("miparelogico.gif"), MediaFile("sembraLogico.gif")),
      replySelection = RandomSelection
    ),
    ReplyBundleMessage(
      TextTrigger(
        List(
          StringTextTriggerValue("non me ne fotte"),
          StringTextTriggerValue("chissenefrega"),
          StringTextTriggerValue("non mi interessa")
        )
      ),
      List(MediaFile("nonmenefotte.gif"))
    ),
    ReplyBundleMessage(
      TextTrigger(
        List(
          StringTextTriggerValue("certo"),
          StringTextTriggerValue("escerto"),
          StringTextTriggerValue("critiche"),
          StringTextTriggerValue("non me ne frega un cazzo")
        )
      ),
      List(MediaFile("escerto.gif"))
    ),
    ReplyBundleMessage(
      TextTrigger(List(StringTextTriggerValue("levati dai coglioni"), StringTextTriggerValue("fuori dai coglioni"))),
      List(MediaFile("levatidaicoglioni.gif"))
    ),
    ReplyBundleMessage(
      TextTrigger(
        List(
          StringTextTriggerValue("non sono coglione"),
          StringTextTriggerValue("non sono mica coglione"),
          StringTextTriggerValue("non sono cojone"),
          StringTextTriggerValue("non sono mica cojone"),
          StringTextTriggerValue("sarete coglioni voi"),
          StringTextTriggerValue("sarete cojoni voi"),
          StringTextTriggerValue("mica so coglione"),
          StringTextTriggerValue("mica sono coglione"),
          StringTextTriggerValue("mica so cojone"),
          StringTextTriggerValue("mica sono cojone")
        )
      ),
      List(MediaFile("saretecoglionivoi.gif"))
    ),
    ReplyBundleMessage(
      TextTrigger(List(StringTextTriggerValue("bravo!!!"), StringTextTriggerValue("bravooo"))),
      List(MediaFile("bravo.gif"))
    ),
    ReplyBundleMessage(TextTrigger(List(StringTextTriggerValue("capolavoro"))), List(MediaFile("capolavoro.gif"))),
    ReplyBundleMessage(TextTrigger(List(StringTextTriggerValue(" metal"))), List(MediaFile("metal.gif"))),
    ReplyBundleMessage(
      TextTrigger(List(StringTextTriggerValue("chiama la polizia"))),
      List(MediaFile("chiamaLaPolizia.gif"))
    ),
    ReplyBundleMessage(TextTrigger(List(StringTextTriggerValue("allucinante"))), List(MediaFile("allucinante.gif"))),
    ReplyBundleMessage(
      TextTrigger(List(StringTextTriggerValue("mare di cazzate"), StringTextTriggerValue("non è possibile"))),
      List(MediaFile("nonépossibile.gif"))
    ),
    ReplyBundleMessage(TextTrigger(List(StringTextTriggerValue("porca miseria"))), List(MediaFile("porcamiseria.gif"))),
    ReplyBundleMessage(
      TextTrigger(List(StringTextTriggerValue("schifoso!!!"), StringTextTriggerValue("schifosooo"))),
      List(MediaFile("schifoso.gif"))
    ),
    ReplyBundleMessage(
      TextTrigger(List(StringTextTriggerValue("facendo soffrire"))),
      List(MediaFile("facendosoffrire.gif"))
    ),
    ReplyBundleMessage(
      TextTrigger(List(StringTextTriggerValue("sentendo male"))),
      List(MediaFile("mistosentendomale.gif"))
    ),
    ReplyBundleMessage(TextTrigger(List(StringTextTriggerValue("stare male"))), List(MediaFile("mifastaremale.gif"))),
    ReplyBundleMessage(
      TextTrigger(List(StringTextTriggerValue("lunghezza d'onda"), StringTextTriggerValue("brave persone"))),
      List(MediaFile("lunghezzadonda.gif"))
    ),
    ReplyBundleMessage(TextTrigger(List(StringTextTriggerValue("delirio"))), List(MediaFile("delirio.gif"))),
    ReplyBundleMessage(TextTrigger(List(StringTextTriggerValue("paradosso"))), List(MediaFile("paradosso.gif"))),
    ReplyBundleMessage(
      TextTrigger(List(StringTextTriggerValue("querelare"), StringTextTriggerValue("guerelare"))),
      List(MediaFile("querelare.gif"))
    ),
    ReplyBundleMessage(
      TextTrigger(List(StringTextTriggerValue("cantate"), StringTextTriggerValue("arigliano"))),
      List(MediaFile("arigliano.gif"))
    ),
    ReplyBundleMessage(
      TextTrigger(List(StringTextTriggerValue("non voglio nessuno"))),
      List(MediaFile("nonvoglionessuno.gif"))
    ),
    ReplyBundleMessage(
      TextTrigger(List(StringTextTriggerValue("andati al cesso"), StringTextTriggerValue("diecimila volte"))),
      List(MediaFile("alcesso.gif"))
    ),
    ReplyBundleMessage(
      TextTrigger(List(StringTextTriggerValue("non manca niente"), StringTextTriggerValue("c'é tutto"))),
      List(MediaFile("nonmancaniente.gif"))
    ),
    ReplyBundleMessage(TextTrigger(List(StringTextTriggerValue("in fila"))), List(MediaFile("mettitiinfila.gif"))),
    ReplyBundleMessage(TextTrigger(List(StringTextTriggerValue("non male"))), List(MediaFile("nonmale.gif"))),
    ReplyBundleMessage(TextTrigger(List(StringTextTriggerValue("perché si sente"))), List(MediaFile("sisente.gif"))),
    ReplyBundleMessage(
      TextTrigger(
        List(StringTextTriggerValue("colpa vostra"), StringTextTriggerValue("pazzo"), StringTextTriggerValue("matto"))
      ),
      List(MediaFile("stodiventandopazzo.gif"))
    ),
    ReplyBundleMessage(
      TextTrigger(
        List(StringTextTriggerValue("sorca"), StringTextTriggerValue("lecciso"), StringTextTriggerValue("figa"))
      ),
      List(MediaFile("sorcalecciso.gif"))
    ),
    ReplyBundleMessage(
      TextTrigger(
        List(
          StringTextTriggerValue("non li sopporto"),
          StringTextTriggerValue("che si deve fare"),
          StringTextTriggerValue("che se deve fa per"),
          StringTextTriggerValue("bisogna pure lavorà")
        )
      ),
      List(MediaFile("nonlisopporto.gif"))
    ),
    ReplyBundleMessage(
      TextTrigger(
        List(StringTextTriggerValue("good"), StringTextTriggerValue("show"), StringTextTriggerValue("friends"))
      ),
      List(MediaFile("okgoodshowfriends.gif"))
    ),
    ReplyBundleMessage(
      TextTrigger(List(StringTextTriggerValue("delus"), StringTextTriggerValue("delud"))),
      List(MediaFile("deluso.gif"))
    ),
    ReplyBundleMessage(TextTrigger(List(StringTextTriggerValue("chi cazzo sei"))), List(MediaFile("chicazzosei.gif"))),
    ReplyBundleMessage(TextTrigger(List(StringTextTriggerValue("feste"))), List(MediaFile("feste.gif"))),
    ReplyBundleMessage(
      TextTrigger(List(StringTextTriggerValue("si ostina"), StringTextTriggerValue("foto vecchie"))),
      List(MediaFile("ostina.gif"))
    ),
    ReplyBundleMessage(
      TextTrigger(List(StringTextTriggerValue("vecchio"), StringTextTriggerValue("vecchia"))),
      List(MediaFile("vecchio.gif"))
    ),
    ReplyBundleMessage(TextTrigger(List(StringTextTriggerValue("venite qua"))), List(MediaFile("venitequa.gif"))),
    ReplyBundleMessage(
      TextTrigger(List(StringTextTriggerValue("scatta qualcosa"))),
      List(MediaFile("scattaQualcosa.gif"))
    ),
    ReplyBundleMessage(
      TextTrigger(List(StringTextTriggerValue("sputo"), StringTextTriggerValue("sputa"))),
      List(MediaFile("sputo.gif"))
    ),
    ReplyBundleMessage(
      TextTrigger(List(StringTextTriggerValue("lasciami in pace"), StringTextTriggerValue("stronz"))),
      List(MediaFile("lasciamiinpace.gif"))
    ),
    ReplyBundleMessage(
      TextTrigger(
        List(
          StringTextTriggerValue("sono arrivato"),
          StringTextTriggerValue("arivado"),
          StringTextTriggerValue("piacere")
        )
      ),
      List(MediaFile("arivado.gif"))
    ),
    ReplyBundleMessage(TextTrigger(List(StringTextTriggerValue("pure bona"))), List(MediaFile("bona.gif"))),
    ReplyBundleMessage(
      TextTrigger(List(StringTextTriggerValue("mortacci vostri"))),
      List(MediaFile("mortaccivostri.gif"))
    ),
    ReplyBundleMessage(
      TextTrigger(
        List(StringTextTriggerValue(" danza"), StringTextTriggerValue("macabra"), StringTextTriggerValue(" ball"))
      ),
      List(MediaFile("danzamacabra.gif"))
    ),
    ReplyBundleMessage(
      TextTrigger(List(StringTextTriggerValue("sei cambiat"), StringTextTriggerValue("sei gambiat"))),
      List(MediaFile("seicambiata.gif"))
    ),
    ReplyBundleMessage(
      TextTrigger(List(StringTextTriggerValue("mio discapito"), StringTextTriggerValue("disgabido"))),
      List(MediaFile("discapito.gif"))
    ),
    ReplyBundleMessage(
      TextTrigger(List(StringTextTriggerValue("peggio del peggio"))),
      List(MediaFile("peggiodelpeggio.gif"))
    ),
    ReplyBundleMessage(
      TextTrigger(List(StringTextTriggerValue("cosa squallida"), StringTextTriggerValue("abbia mai sentito"))),
      List(MediaFile("squallida.gif"))
    ),
    ReplyBundleMessage(TextTrigger(List(StringTextTriggerValue("la verità"))), List(MediaFile("verita.gif"))),
    ReplyBundleMessage(
      TextTrigger(List(StringTextTriggerValue("ti dovresti vergognare"))),
      List(MediaFile("tidovrestivergognare.gif"))
    ),
    ReplyBundleMessage(
      TextTrigger(List(StringTextTriggerValue("oddio mio no"), StringTextTriggerValue("dio mio no"))),
      List(MediaFile("oddiomio.gif"))
    ),
    ReplyBundleMessage(
      TextTrigger(List(StringTextTriggerValue("destino"), StringTextTriggerValue("incontrare"))),
      List(MediaFile("destino.gif"))
    ),
    ReplyBundleMessage(
      TextTrigger(List(StringTextTriggerValue("meridionale"), StringTextTriggerValue("terron"))),
      List(MediaFile("meridionale.gif"))
    ),
    ReplyBundleMessage(
      TextTrigger(
        List(StringTextTriggerValue("baci"), StringTextTriggerValue("limonare"), StringTextTriggerValue("peggio cose"))
      ),
      List(MediaFile("bacio.gif"))
    ),
    ReplyBundleMessage(
      TextTrigger(
        List(
          StringTextTriggerValue("solo uno parló"),
          StringTextTriggerValue("criticato"),
          StringTextTriggerValue("gridigado")
        )
      ),
      List(MediaFile("fuCriticato.gif"))
    ),
    ReplyBundleMessage(
      TextTrigger(List(StringTextTriggerValue("giudica"))),
      List(MediaFile("giudicate.gif"), MediaFile("comeFaiAGiudicare.gif")),
      replySelection = RandomSelection
    ),
    ReplyBundleMessage(
      TextTrigger(List(StringTextTriggerValue("bastone infernale"), StringTextTriggerValue("un'arma"))),
      List(MediaFile("bastone1.gif"), MediaFile("bastone2.gif")),
      replySelection = RandomSelection
    ),
    ReplyBundleMessage(
      TextTrigger(List(StringTextTriggerValue("droga"))),
      List(MediaFile("drogatiRockettari1.gif"), MediaFile("drogatiRockettari2.gif"), MediaFile("drogatiPiloti.gif")),
      replySelection = RandomSelection
    ),
    ReplyBundleMessage(
      TextTrigger(
        List(
          StringTextTriggerValue("rockettari"),
          StringTextTriggerValue("rocchettari"),
          StringTextTriggerValue("stillati")
        )
      ),
      List(MediaFile("drogatiRockettari1.gif"), MediaFile("drogatiRockettari2.gif")),
      replySelection = RandomSelection
    ),
    ReplyBundleMessage(
      TextTrigger(List(StringTextTriggerValue("sguardo"))),
      List(
        MediaFile("sguardo1.gif"),
        MediaFile("sguardo2.gif"),
        MediaFile("sguardo3.gif"),
        MediaFile("sguardo4.gif")
      ),
      replySelection = RandomSelection
    ),
    ReplyBundleMessage(TextTrigger(List(StringTextTriggerValue("a quel punto"))), List(MediaFile("quelPunto.gif"))),
    ReplyBundleMessage(TextTrigger(List(StringTextTriggerValue("errori"))), List(MediaFile("maierrori.gif"))),
    ReplyBundleMessage(
      TextTrigger(
        List(StringTextTriggerValue("quattro"), StringTextTriggerValue(" 4 "), StringTextTriggerValue("in tempo"))
      ),
      List(MediaFile("quattroSolo.gif"))
    ),
    ReplyBundleMessage(
      TextTrigger(
        List(
          StringTextTriggerValue("la parte"),
          StringTextTriggerValue("recitare"),
          StringTextTriggerValue("attore"),
          StringTextTriggerValue("attrice")
        )
      ),
      List(MediaFile("faccioLaParte.gif"))
    ),
    ReplyBundleMessage(
      TextTrigger(List(StringTextTriggerValue("assolutamente no"), StringTextTriggerValue("non mi lamento"))),
      List(MediaFile("nonMiLamento.gif"))
    ),
    ReplyBundleMessage(
      TextTrigger(List(StringTextTriggerValue("inizio della fine"))),
      List(MediaFile("inizioDellaFine.gif"))
    ),
    ReplyBundleMessage(TextTrigger(List(StringTextTriggerValue("il senso"))), List(MediaFile("ilSensoCapito.gif"))),
    ReplyBundleMessage(
      TextTrigger(List(StringTextTriggerValue(" ester "), StringTextTriggerValue("esposito"))),
      List(MediaFile("ester.gif"))
    ),
    ReplyBundleMessage(
      TextTrigger(
        List(
          StringTextTriggerValue("abituato"),
          StringTextTriggerValue("abiduado"),
          StringTextTriggerValue("propriollà"),
          StringTextTriggerValue("propriolla")
        )
      ),
      List(MediaFile("propriolla.gif"))
    )
  )

  val messageRepliesSpecialData: List[ReplyBundleMessage] = List(
    ReplyBundleMessage(
      TextTrigger(List(StringTextTriggerValue("vattene affanculo"), StringTextTriggerValue("vattene a fanculo"))),
      List(MediaFile("mavatteneaffanculo.gif"), MediaFile("mavatteneaffanculo.mp3"))
    ),
    ReplyBundleMessage(
      TextTrigger(List(StringTextTriggerValue("feelings"))),
      List(MediaFile("feelings.mp3"), MediaFile("feelings.gif"))
    ),
    ReplyBundleMessage(
      TextTrigger(List(StringTextTriggerValue("me ne vado"))),
      List(
        MediaFile("menevado.mp3"),
        MediaFile("miRompiErCazzo.gif"),
        MediaFile("menevado.gif")
      ),
      replySelection = RandomSelection
    ),
    ReplyBundleMessage(
      TextTrigger(
        List(StringTextTriggerValue("mignotta"), StringTextTriggerValue("puttana"), StringTextTriggerValue("troia"))
      ),
      List(MediaFile("mignotta.mp3"), MediaFile("mignotta.gif"))
    ),
    ReplyBundleMessage(
      TextTrigger(List(StringTextTriggerValue("ti devi spaventare"))),
      List(MediaFile("tidevispaventare.mp3"), MediaFile("tidevispaventare.gif"))
    ),
    ReplyBundleMessage(
      TextTrigger(List(StringTextTriggerValue("ma che cazzo sto dicendo"), StringTextTriggerValue("il martell"))),
      List(MediaFile("machecazzostodicendo.mp3"), MediaFile("machecazzostodicendo.gif"))
    ),
    ReplyBundleMessage(
      TextTrigger(List(StringTextTriggerValue("questa volta no"))),
      List(MediaFile("questavoltano.mp3"), MediaFile("questavoltano.gif"))
    ),
    ReplyBundleMessage(
      TextTrigger(List(StringTextTriggerValue("una vergogna"))),
      List(MediaFile("vergogna.mp3"), MediaFile("vergogna.gif"))
    ),
    ReplyBundleMessage(
      TextTrigger(List(StringTextTriggerValue("mi devo trasformare"), StringTextTriggerValue("cristo canaro"))),
      List(MediaFile("trasformista.mp3"), MediaFile("trasformista.gif"))
    ),
    ReplyBundleMessage(
      TextTrigger(
        List(StringTextTriggerValue("masgus"), StringTextTriggerValue("ma sgus"), StringTextTriggerValue("ma scusa"))
      ),
      List(MediaFile("masgus.mp3"), MediaFile("masgus.gif"))
    ),
    ReplyBundleMessage(
      TextTrigger(List(StringTextTriggerValue("gianni"), StringTextTriggerValue("ciaoo"))),
      List(MediaFile("grazie.mp3"), MediaFile("grazie.gif"))
    ),
    ReplyBundleMessage(
      TextTrigger(List(StringTextTriggerValue("stare attenti"), StringTextTriggerValue("per strada"))),
      List(MediaFile("incontrateperstrada.mp3"), MediaFile("incontrateperstrada.gif"))
    ),
    ReplyBundleMessage(
      TextTrigger(
        List(
          StringTextTriggerValue("lavora tu vecchiaccia"),
          StringTextTriggerValue("hai la pelle dura"),
          StringTextTriggerValue("io sono creatura")
        )
      ),
      List(MediaFile("lavoratu.mp3"), MediaFile("lavoratu.gif"))
    ),
    ReplyBundleMessage(
      TextTrigger(List(StringTextTriggerValue("infernali!!!!"), StringTextTriggerValue("infernaliii"))),
      List(MediaFile("infernali.mp3"), MediaFile("infernali.gif"))
    ),
    ReplyBundleMessage(
      TextTrigger(List(StringTextTriggerValue("per il culo"))),
      List(MediaFile("pigliandoperilculo.mp3"), MediaFile("pigliandoperilculo.gif"))
    ),
    ReplyBundleMessage(
      TextTrigger(
        List(
          StringTextTriggerValue(emoji":lol:"),
          StringTextTriggerValue(emoji":rofl:"),
          StringTextTriggerValue("sorriso"),
          StringTextTriggerValue(emoji":smile:")
        )
      ),
      List(
        MediaFile("risata.mp3"),
        MediaFile("risata.gif"),
        MediaFile("sorriso2.gif"),
        MediaFile("sorriso3.gif"),
        MediaFile("sorriso.gif"),
        MediaFile("sorrisoSognante.gif")
      ),
      replySelection = RandomSelection
    ),
    ReplyBundleMessage(
      TextTrigger(List(StringTextTriggerValue("ammazza che sei"), StringTextTriggerValue("quasi un frocio"))),
      List(MediaFile("frocio.mp3"), MediaFile("frocio.gif"))
    ),
    ReplyBundleMessage(
      TextTrigger(List(StringTextTriggerValue("cortesia"))),
      List(MediaFile("fammiquestacortesia.mp3"), MediaFile("fammiquestacortesia.gif"))
    ),
    ReplyBundleMessage(
      TextTrigger(List(StringTextTriggerValue("non mi sta bene"))),
      List(MediaFile("nonmistabene.mp3"), MediaFile("nonmistabene.gif"))
    ),
    ReplyBundleMessage(
      TextTrigger(
        List(
          StringTextTriggerValue("sai fare le labbra"),
          StringTextTriggerValue("manco le labbra"),
          StringTextTriggerValue("neanche le labbra")
        )
      ),
      List(MediaFile("labbra.mp3"), MediaFile("labbra.gif"))
    ),
    ReplyBundleMessage(
      TextTrigger(List(StringTextTriggerValue("la vita è il nemico"))),
      List(MediaFile("vitanemico.mp3"), MediaFile("vitanemico.gif"))
    ),
    ReplyBundleMessage(
      TextTrigger(List(StringTextTriggerValue("permettere"))),
      List(MediaFile("permettere.mp3"), MediaFile("permettere.gif"))
    ),
    ReplyBundleMessage(
      TextTrigger(List(StringTextTriggerValue("le note"))),
      List(MediaFile("note.mp3"), MediaFile("note.gif"))
    ),
    ReplyBundleMessage(
      TextTrigger(List(StringTextTriggerValue("terribile"))),
      List(MediaFile("terribile.mp3"), MediaFile("terribile.gif"))
    ),
    ReplyBundleMessage(
      TextTrigger(List(StringTextTriggerValue("viva napoli"))),
      List(MediaFile("vivaNapoli.mp3"), MediaFile("vivaNapoli.gif"))
    ),
    ReplyBundleMessage(
      TextTrigger(
        List(
          StringTextTriggerValue("ciao a tutti"),
          StringTextTriggerValue("come state"),
          StringTextTriggerValue("belle gioie")
        )
      ),
      List(MediaFile("ciaocomestate.gif"), MediaFile("ciaocomestate.mp3"))
    ),
    ReplyBundleMessage(
      TextTrigger(List(StringTextTriggerValue("basta!!!"), StringTextTriggerValue("bastaaa"))),
      List(
        MediaFile("basta.mp3"),
        MediaFile("basta.gif"),
        MediaFile("bastaSedia.gif")
      ),
      replySelection = RandomSelection
    ),
    ReplyBundleMessage(
      TextTrigger(
        List(StringTextTriggerValue("assolo"), StringTextTriggerValue("chitarra"), StringTextTriggerValue("ghidarra"))
      ),
      List(
        MediaFile("assolo.mp3"),
        MediaFile("chitarra1.gif"),
        MediaFile("chitarra2.gif")
      ),
      replySelection = RandomSelection
    ),
    ReplyBundleMessage(
      TextTrigger(
        List(
          StringTextTriggerValue("capito "),
          StringTextTriggerValue(" gabido"),
          StringTextTriggerValue(" capito"),
          StringTextTriggerValue("gabido "),
          StringTextTriggerValue(" capissi"),
          StringTextTriggerValue("capissi ")
        )
      ),
      List(
        MediaFile("hocapito.mp3"),
        MediaFile("avetecapito.mp3"),
        MediaFile("capito.mp3"),
        MediaFile("AveteCapitoComeSempre.gif"),
        MediaFile("NonAveteCapitoUnCazzo.gif"),
        MediaFile("voiNonAveteCapitoUnCazzo.gif"),
        MediaFile("ilSensoCapito.gif"),
        MediaFile("capitoDoveStiamo.gif"),
        MediaFile("nonHoCapito.gif"),
        MediaFile("comeAlSolitoNonAveteCapito.gif")
      ),
      replySelection = RandomSelection
    ),
    ReplyBundleMessage(
      TextTrigger(
        List(
          StringTextTriggerValue("esperiment"),
          StringTextTriggerValue("1, 2, 3"),
          StringTextTriggerValue("uno, due, tre")
        )
      ),
      List(
        MediaFile("esperimento.mp3"),
        MediaFile("esperimento.gif"),
        MediaFile("esperimento2.gif"),
        MediaFile("esperimento3.gif"),
      ),
      replySelection = RandomSelection
    ),
    ReplyBundleMessage(
      TextTrigger(List(StringTextTriggerValue("schifosi"))),
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
        msg =>
          msg.text
            .filterNot(t => t.trim == "/bensonify" || t.trim == "/bensonify@RichardPHJBensonBot")
            .map(t => List(Bensonify.compute(t.drop(11))))
            .getOrElse(List("E PARLAAAAAAA!!!!")),
        true
      )
    )
  )
}
