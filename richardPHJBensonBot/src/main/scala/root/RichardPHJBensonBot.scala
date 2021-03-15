package root

import org.http4s.client.blaze._
import scala.concurrent.ExecutionContext
import com.benkio.telegramBotInfrastructure.Configurations
import com.benkio.telegramBotInfrastructure.botCapabilities._
import com.benkio.telegramBotInfrastructure._
import cats.effect._
import com.benkio.telegramBotInfrastructure.model._

import com.lightbend.emoji.ShortCodes.Implicits._
import com.lightbend.emoji.ShortCodes.Defaults._
import telegramium.bots.high._
import cats._

class RichardPHJBensonBot[F[_]: Sync: Timer: Parallel]()(implicit api: Api[F]) extends BotSkeleton {

  override val resourceSource: ResourceSource = RichardPHJBensonBot.resourceSource

  override lazy val messageRepliesData: List[ReplyBundleMessage] =
    RichardPHJBensonBot.messageRepliesData

  override lazy val commandRepliesData: List[ReplyBundleCommand] = RichardPHJBensonBot.commandRepliesData
}

object RichardPHJBensonBot extends Configurations {

  val resourceSource: ResourceSource = All("rphjb.db")

  val messageRepliesAudioData: List[ReplyBundleMessage] = List(
    ReplyBundleMessage(
      TextTrigger(
        List(
          StringTextTriggerValue("musica tecnica"),
          StringTextTriggerValue("rock"),
          StringTextTriggerValue("antonacci"),
          StringTextTriggerValue("grignani"),
          StringTextTriggerValue("jovanotti"),
        )
      ),
      List(MediaFile("rock.mp3"))
    ),
    ReplyBundleMessage(
      TextTrigger(
        List(
          RegexTextTriggerValue("conosce(nza|re)".r),
          StringTextTriggerValue("il sapere"),
          StringTextTriggerValue("veri valori"),
        )
      ),
      List(MediaFile("conoscere.mp3"))
    ),
    ReplyBundleMessage(
      TextTrigger(
        List(
          RegexTextTriggerValue("stori(a|e)".r)
        )
      ),
      List(MediaFile("storie.mp3"))
    ),
    ReplyBundleMessage(
      TextTrigger(
        List(
          RegexTextTriggerValue("vo[l]+[o]*[u]+[ou]*me".r)
        )
      ),
      List(MediaFile("menoVolume.mp3"))
    ),
    ReplyBundleMessage(
      TextTrigger(List(StringTextTriggerValue("sono"), StringTextTriggerValue("ultimo"))),
      List(MediaFile("sonoUltimo.mp3")),
      matcher = ContainsAll
    ),
    ReplyBundleMessage(
      TextTrigger(
        List(
          StringTextTriggerValue("panna"),
          StringTextTriggerValue("anguille"),
          StringTextTriggerValue("polipi"),
          StringTextTriggerValue("cetrioli"),
          StringTextTriggerValue("suonare")
        )
      ),
      List(MediaFile("problema.mp3"))
    ), //also in special list
    ReplyBundleMessage(TextTrigger(List(StringTextTriggerValue("continua"))), List(MediaFile("continua.mp3"))),
    ReplyBundleMessage(
      TextTrigger(List(StringTextTriggerValue("zucchero"))),
      List(MediaFile("ChitarraZuggherada.mp3"))
    ),
    ReplyBundleMessage(
      TextTrigger(
        List(
          RegexTextTriggerValue("au[ ]?de".r),
          StringTextTriggerValue("time"),
          RegexTextTriggerValue("uir[ ]?bi[ ]?taim".r)
        )
      ),
      List(MediaFile("auDeUirbiTaim.mp3"))
    ),
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
    ReplyBundleMessage(TextTrigger(List(RegexTextTriggerValue("(..magari|magari..)".r))), List(MediaFile("magari.mp3"))),
    ReplyBundleMessage(TextTrigger(List(StringTextTriggerValue("io ti aiuto"))), List(MediaFile("aiuto.mp3"))),
    ReplyBundleMessage(TextTrigger(List(StringTextTriggerValue("faccio schifo"))), List(MediaFile("faccioSchifo.mp3"))),
    ReplyBundleMessage(
      TextTrigger(List(RegexTextTriggerValue("ci sei ritornat[ao]".r))),
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
          StringTextTriggerValue(" nani"),
          StringTextTriggerValue("nani "),
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
      TextTrigger(List(RegexTextTriggerValue("[a]+[t]{2,}[e]+[n]+[z]+[i]+[o]+[n]+[e]+[!]*[!e]$".r))),
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
    ReplyBundleMessage(TextTrigger(List(RegexTextTriggerValue("oppur[ae]".r))), List(MediaFile("oppura.mp3"))),
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
    ReplyBundleMessage(TextTrigger(List(StringTextTriggerValue("scivola"))), List(MediaFile("siScivola.mp3"))),
    ReplyBundleMessage(
      TextTrigger(
        List(
          StringTextTriggerValue("spalle"),
          StringTextTriggerValue("braccia"),
          RegexTextTriggerValue("t[ie] strozzo".r)
        )
      ),
      List(MediaFile("faccioVedereSpalleBraccia.gif"))
    ),
    ReplyBundleMessage(
      TextTrigger(
        List(
          StringTextTriggerValue("graffi")
        )
      ),
      List(MediaFile("graffi.gif"))
    ),
    ReplyBundleMessage(
      TextTrigger(
        List(
          StringTextTriggerValue("mi piaccio"),
          StringTextTriggerValue("impazzire"),
        )
      ),
      List(MediaFile("miPiaccio.gif"))
    ),
    ReplyBundleMessage(
      TextTrigger(
        List(
          StringTextTriggerValue("pelle d'oca"),
          StringTextTriggerValue("sussult"),
          StringTextTriggerValue("brivid")
        )
      ),
      List(MediaFile("brivido.gif"))
    ),
    ReplyBundleMessage(
      TextTrigger(
        List(
          StringTextTriggerValue("che siamo noi"),
          StringTextTriggerValue("pezzi di merda"),
        )
      ),
      List(MediaFile("pezziDiMerda.gif"))
    ),
    ReplyBundleMessage(
      TextTrigger(
        List(
          StringTextTriggerValue(" urlo"),
          StringTextTriggerValue("urlo "),
          StringTextTriggerValue("disper"),
        )
      ),
      List(
        MediaFile("tuffo.gif"),
        MediaFile("urlo.gif"),
        MediaFile("urlo2.gif"),
        MediaFile("urloCanaro.gif")
      ),
      replySelection = RandomSelection
    ),
    ReplyBundleMessage(
      TextTrigger(
        List(
          StringTextTriggerValue("rispondere")
        )
      ),
      List(MediaFile("rispondere.gif"))
    ),
    ReplyBundleMessage(
      TextTrigger(
        List(
          StringTextTriggerValue("cuore in mano"),
          StringTextTriggerValue("mano nella mano"),
          StringTextTriggerValue("pelle contro la pelle")
        )
      ),
      List(MediaFile("cuoreInMano.gif"))
    ),
    ReplyBundleMessage(
      TextTrigger(
        List(
          StringTextTriggerValue("stato brado")
        )
      ),
      List(MediaFile("statoBrado.gif"))
    ),
    ReplyBundleMessage(
      TextTrigger(
        List(
          StringTextTriggerValue("pronto dimmi")
        )
      ),
      List(MediaFile("prontoDimmi.gif"))
    ),
    ReplyBundleMessage(
      TextTrigger(
        List(
          RegexTextTriggerValue(" port[a]+( |$)".r)
        )
      ),
      List(MediaFile("porta.gif"))
    ),
    ReplyBundleMessage(
      TextTrigger(
        List(
          StringTextTriggerValue("prendo quello che cazzo c'è da prendere"),
          StringTextTriggerValue("prendo il motorino"),
          StringTextTriggerValue("prendo la macchina"),
          StringTextTriggerValue("prendo l'auto"),
        )
      ),
      List(MediaFile("prendoIlNecessario.gif"))
    ),
    ReplyBundleMessage(
      TextTrigger(
        List(
          StringTextTriggerValue("nella gola")
        )
      ),
      List(MediaFile("nellaGola.gif"))
    ),
    ReplyBundleMessage(
      TextTrigger(
        List(
          StringTextTriggerValue("siamo qua")
        )
      ),
      List(MediaFile("siamoQua.gif"))
    ),
    ReplyBundleMessage(
      TextTrigger(
        List(
          StringTextTriggerValue("cucciolo")
        )
      ),
      List(
        MediaFile("cucciolo.gif"),
        MediaFile("cucciolo2.gif"),
      ),
      replySelection = RandomSelection
    ),
    ReplyBundleMessage(
      TextTrigger(
        List(
          StringTextTriggerValue("applau")
        )
      ),
      List(MediaFile("applauso.gif"))
    ),
    ReplyBundleMessage(
      TextTrigger(
        List(
          StringTextTriggerValue("Che c'hai")
        )
      ),
      List(MediaFile("cheCHai.gif"))
    ),
    ReplyBundleMessage(
      TextTrigger(
        List(
          StringTextTriggerValue("stringere i denti"),
          StringTextTriggerValue("andare avanti"),
        )
      ),
      List(MediaFile("andareAvanti.gif"))
    ),
    ReplyBundleMessage(
      TextTrigger(
        List(
          StringTextTriggerValue("06"),
          StringTextTriggerValue("prefisso")
        )
      ),
      List(MediaFile("06.gif"))
    ),
    ReplyBundleMessage(
      TextTrigger(
        List(
          StringTextTriggerValue("abbiamo vinto")
        )
      ),
      List(MediaFile("abbiamoVinto.gif"))
    ),
    ReplyBundleMessage(
      TextTrigger(
        List(
          StringTextTriggerValue("telefonata pilotata"),
          StringTextTriggerValue("falsità")
        )
      ),
      List(MediaFile("telefonataPilotata.gif"))
    ),
    ReplyBundleMessage(
      TextTrigger(
        List(
          StringTextTriggerValue("è vero")
        )
      ),
      List(MediaFile("vero.gif"))
    ),
    ReplyBundleMessage(
      TextTrigger(
        List(
          StringTextTriggerValue("quante ore"),
          StringTextTriggerValue("quanto tempo")
        )
      ),
      List(MediaFile("quanteOre.gif"))
    ),
    ReplyBundleMessage(
      TextTrigger(
        List(
          StringTextTriggerValue("guerra")
        )
      ),
      List(MediaFile("guerraTotale.gif"))
    ),
    ReplyBundleMessage(
      TextTrigger(
        List(
          RegexTextTriggerValue("entrat[oa]".r)
        )
      ),
      List(MediaFile("comeHaFattoAEntrare.gif"))
    ),
    ReplyBundleMessage(
      TextTrigger(List(StringTextTriggerValue("donna cane"))),
      List(MediaFile("donnaCane.gif"))
    ),
    ReplyBundleMessage(
      TextTrigger(List(StringTextTriggerValue("menzion"))),
      List(MediaFile("nonMiMenzionareQuestaParola.gif"))
    ),
    ReplyBundleMessage(TextTrigger(List(StringTextTriggerValue("hollywood"))), List(MediaFile("hollywood.gif"))),
    ReplyBundleMessage(
      TextTrigger(
        List(
          StringTextTriggerValue("piano superiore"),
          StringTextTriggerValue("compete"),
          StringTextTriggerValue("gerarca")
        )
      ),
      List(MediaFile("pianoSuperioreCompete.gif"))
    ),
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
          StringTextTriggerValue(e":wink:")
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
      TextTrigger(
        List(
          StringTextTriggerValue("mi rompi il cazzo"),
          StringTextTriggerValue("mi dai fastidio")
        )
      ),
      List(MediaFile("miRompiErCazzo.gif"))
    ),
    ReplyBundleMessage(
      TextTrigger(
        List(
          StringTextTriggerValue("dare fastidio")
        )
      ),
      List(MediaFile("dareFastidio.gif"))
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
      TextTrigger(List(StringTextTriggerValue("c'ha [pure ]?ragione"), StringTextTriggerValue("o no?"))),
      List(MediaFile("ragione.gif"))
    ),
    ReplyBundleMessage(TextTrigger(List(StringTextTriggerValue("micetta"))), List(MediaFile("micetta.gif"))),
    ReplyBundleMessage(
      TextTrigger(List(StringTextTriggerValue("generi musicali"), RegexTextTriggerValue("solo il me(t|d)al".r))),
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
          RegexTextTriggerValue("diri[g]+en[dt]i".r),
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
        List(RegexTextTriggerValue("[cg]hi[td]a[r]+is[td]a [bp]referi[dt]o".r))
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
          RegexTextTriggerValue("[gc]io[gc]+ola[dt]a".r),
          StringTextTriggerValue("pandori"),
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
          StringTextTriggerValue(" paga"),
          StringTextTriggerValue("paga "),
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
          RegexTextTriggerValue("sti [gc]azzi".r)
        )
      ),
      List(MediaFile("comevipare.gif"))
    ),
    ReplyBundleMessage(
      TextTrigger(
        List(
          StringTextTriggerValue("non sapere"),
          RegexTextTriggerValue("aris[dt]o[dt]ele".r)
        )
      ),
      List(MediaFile("sodinonsapere.gif"))
    ),
    ReplyBundleMessage(
      TextTrigger(
        List(
          StringTextTriggerValue("non ci credete?"),
          RegexTextTriggerValue("grande s[dt]ronza[dt][ea]".r)
        )
      ),
      List(MediaFile("noncicredete.gif"))
    ),
    ReplyBundleMessage(
      TextTrigger(
        List(
          RegexTextTriggerValue("lo[g]+i[cg]o".r)
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
      List(
        MediaFile("nonmenefotte.gif"),
        MediaFile("nonMeNeFrega.gif"),
      ),
      replySelection = RandomSelection
    ),
    ReplyBundleMessage(
      TextTrigger(
        List(
          RegexTextTriggerValue("e(s|c)certo".r),
          StringTextTriggerValue("accetto le critiche"),
          StringTextTriggerValue("non me ne frega un cazzo")
        )
      ),
      List(MediaFile("escerto.gif"))
    ),
    ReplyBundleMessage(
      TextTrigger(
        List(RegexTextTriggerValue("levati[/. ]*dai coglioni".r), RegexTextTriggerValue("fuori[/. ]*dai coglioni".r))
      ),
      List(MediaFile("levatidaicoglioni.gif"))
    ),
    ReplyBundleMessage(
      TextTrigger(
        List(
          RegexTextTriggerValue("(non|mica) so(no)? (un |n )?co(gl|j)ione".r),
          RegexTextTriggerValue("sarete co(gl|j)ioni voi".r)
        )
      ),
      List(MediaFile("saretecoglionivoi.gif"))
    ),
    ReplyBundleMessage(
      TextTrigger(
        List(
          RegexTextTriggerValue("più co(gl|j)ione".r)
        )
      ),
      List(MediaFile("piuCoglione.gif"))
    ),
    ReplyBundleMessage(
      TextTrigger(
        List(
          RegexTextTriggerValue("dice co(gl|j)ione".r)
        )
      ),
      List(MediaFile("coglione.gif"))
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
      List(MediaFile("nonPossibile.gif"))
    ),
    ReplyBundleMessage(
      TextTrigger(
        List(
          StringTextTriggerValue("porca miseria"),
          StringTextTriggerValue("incazzare")
        )
      ),
      List(MediaFile("porcamiseria.gif"))
    ),
    ReplyBundleMessage(
      TextTrigger(List(RegexTextTriggerValue("schifos(o|!)+".r))),
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
    ReplyBundleMessage(TextTrigger(List(StringTextTriggerValue("si sente"))), List(MediaFile("sisente.gif"))),
    ReplyBundleMessage(
      TextTrigger(
        List(StringTextTriggerValue("colpa vostra"), StringTextTriggerValue("pazzo"), StringTextTriggerValue("matto"))
      ),
      List(MediaFile("stodiventandopazzo.gif"))
    ),
    ReplyBundleMessage(
      TextTrigger(
        List(
          StringTextTriggerValue("sorca"),
          StringTextTriggerValue("lecciso"),
          StringTextTriggerValue(" figa"),
          StringTextTriggerValue("figa ")
        )
      ),
      List(MediaFile("sorcalecciso.gif"))
    ),
    ReplyBundleMessage(
      TextTrigger(
        List(
          StringTextTriggerValue("non li sopporto"),
          RegexTextTriggerValue("che si deve f(à|are)".r),
          StringTextTriggerValue("bisogna pure lavorà")
        )
      ),
      List(MediaFile("nonlisopporto.gif"))
    ),
    ReplyBundleMessage(
      TextTrigger(List(RegexTextTriggerValue("delu[sd]".r))),
      List(MediaFile("deluso.gif"))
    ),
    ReplyBundleMessage(TextTrigger(List(StringTextTriggerValue("chi cazzo sei"))), List(MediaFile("chicazzosei.gif"))),
    ReplyBundleMessage(TextTrigger(List(StringTextTriggerValue("feste"))), List(MediaFile("feste.gif"))),
    ReplyBundleMessage(
      TextTrigger(List(StringTextTriggerValue("si ostina"), StringTextTriggerValue("foto vecchie"))),
      List(MediaFile("ostina.gif"))
    ),
    ReplyBundleMessage(
      TextTrigger(List(
        RegexTextTriggerValue("(è|diventa) vecchi[ao]".r),
      )),
      List(MediaFile("vecchio.gif"))
    ),
    ReplyBundleMessage(TextTrigger(List(StringTextTriggerValue("venite qua"))), List(MediaFile("venitequa.gif"))),
    ReplyBundleMessage(
      TextTrigger(List(StringTextTriggerValue("scatta qualcosa"))),
      List(MediaFile("scattaQualcosa.gif"))
    ),
    ReplyBundleMessage(
      TextTrigger(List(RegexTextTriggerValue("sput[ao]".r))),
      List(MediaFile("sputo.gif"))
    ),
    ReplyBundleMessage(
      TextTrigger(List(StringTextTriggerValue("lasciami in pace"), RegexTextTriggerValue("stronz[oa]".r))),
      List(MediaFile("lasciamiinpace.gif"))
    ),
    ReplyBundleMessage(
      TextTrigger(
        List(
          RegexTextTriggerValue("[sono ]?a[r]{1,2}iva[dt]o".r),
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
      TextTrigger(List(RegexTextTriggerValue("sei [gc]ambiat[oa]".r))),
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
      TextTrigger(List(StringTextTriggerValue("squallida"), StringTextTriggerValue("abbia mai sentito"))),
      List(MediaFile("squallida.gif"))
    ),
    ReplyBundleMessage(TextTrigger(List(StringTextTriggerValue("la verità"))), List(MediaFile("verita.gif"))),
    ReplyBundleMessage(
      TextTrigger(List(StringTextTriggerValue("ti dovresti vergognare"))),
      List(MediaFile("tidovrestivergognare.gif"))
    ),
    ReplyBundleMessage(
      TextTrigger(List(RegexTextTriggerValue("[od]?dio mio[,]? no".r))),
      List(MediaFile("oddiomio.gif"))
    ),
    ReplyBundleMessage(
      TextTrigger(List(RegexTextTriggerValue("nooo[o]*".r))),
      List(MediaFile("no.gif"))
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
          RegexTextTriggerValue("[cg]ri[dt]i[gc]a[dt]o".r)
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
      List(
        MediaFile("bastone1.gif"),
        MediaFile("bastone2.gif"),
        MediaFile("bastone3.gif")
      ),
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
          RegexTextTriggerValue("ro[ckgh]+(ch|gh|k)e(d+|t+)ari".r),
          StringTextTriggerValue("stillati")
        )
      ),
      List(MediaFile("drogatiRockettari1.gif"), MediaFile("drogatiRockettari2.gif")),
      replySelection = RandomSelection
    ),
    ReplyBundleMessage(
      TextTrigger(List(StringTextTriggerValue("sguardo"))),
      List(
        MediaFile("sguardo.gif"),
        MediaFile("sguardo2.gif"),
        MediaFile("sguardo3.gif"),
        MediaFile("sguardo4.gif"),
        MediaFile("sguardoCanaro.gif")
      ),
      replySelection = RandomSelection
    ),
    ReplyBundleMessage(TextTrigger(List(StringTextTriggerValue("a quel punto"))), List(MediaFile("quelPunto.gif"))),
    ReplyBundleMessage(TextTrigger(List(StringTextTriggerValue("errori"))), List(MediaFile("maierrori.gif"))),
    ReplyBundleMessage(
      TextTrigger(
        List(
          StringTextTriggerValue("quattro"),
          StringTextTriggerValue("faccio in tempo")
        )
      ),
      List(MediaFile("quattroSolo.gif"))
    ),
    ReplyBundleMessage(
      TextTrigger(
        List(
          StringTextTriggerValue("faccio la parte"),
          StringTextTriggerValue(" recit"),
          StringTextTriggerValue(" fing"),
          RegexTextTriggerValue("a[t]{2,}[o]+re".r),
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
      TextTrigger(List(RegexTextTriggerValue("[ ]?ester | ester[ ]?".r), StringTextTriggerValue("esposito"))),
      List(
        MediaFile("ester.gif"),
        MediaFile("ester2.gif"),
      ),
      replySelection = RandomSelection
    ),
    ReplyBundleMessage(
      TextTrigger(
        List(
          RegexTextTriggerValue("abi[td]ua[td]o".r),
          RegexTextTriggerValue("proprioll[aà]".r),
        )
      ),
      List(MediaFile("propriolla.gif"))
    )
  )

  val messageRepliesSpecialData: List[ReplyBundleMessage] = List(
    ReplyBundleMessage(
      TextTrigger(List(StringTextTriggerValue("problema"))),
      List(
        MediaFile("vabbeProblema.gif"),
        MediaFile("problema.mp3"),
      ),
      replySelection = RandomSelection
    ),
    ReplyBundleMessage(
      TextTrigger(
        List(StringTextTriggerValue("good"), StringTextTriggerValue("show"), StringTextTriggerValue("friends"))
      ),
      List(
        MediaFile("okgoodshowfriends.gif"),
        MediaFile("okgoodshowfriends2.gif"),
        MediaFile("letSGoodStateBene.mp3")
      ),
      replySelection = RandomSelection
    ),
    ReplyBundleMessage(
      TextTrigger(List(StringTextTriggerValue("vattene affanculo"), StringTextTriggerValue("vattene a fanculo"))),
      List(MediaFile("mavatteneaffanculo.gif"), MediaFile("mavatteneaffanculo.mp3"))
    ),
    ReplyBundleMessage(
      TextTrigger(List(StringTextTriggerValue("feelings"))),
      List(MediaFile("feelings.mp3"))
    ),
    ReplyBundleMessage(
      TextTrigger(List(StringTextTriggerValue("feelings"))),
      List(
        MediaFile("feelings.gif"),
        MediaFile("feelings2.gif"),
      ),
      replySelection = RandomSelection
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
      List(MediaFile("vergogna.mp3"), MediaFile("vergogna.gif"), MediaFile("vergogna2.gif")),
      replySelection = RandomSelection
    ),
    ReplyBundleMessage(
      TextTrigger(List(StringTextTriggerValue("mi devo trasformare"), StringTextTriggerValue("cristo canaro"))),
      List(MediaFile("trasformista.mp3"), MediaFile("trasformista.gif"))
    ),
    ReplyBundleMessage(
      TextTrigger(
        List(RegexTextTriggerValue("ma[ ]?s[cg]us[a]?".r))
      ),
      List(MediaFile("masgus.mp3"), MediaFile("masgus.gif"))
    ),
    ReplyBundleMessage(
      TextTrigger(
        List(StringTextTriggerValue("grazie gianni"), RegexTextTriggerValue("cia[o]{3,}".r))
      ),
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
      TextTrigger(List(RegexTextTriggerValue("infern[a]+l[ie]+[!]*".r))),
      List(MediaFile("infernali.mp3"), MediaFile("infernali.gif"), MediaFile("infernale.mp3")),
      replySelection = RandomSelection
    ),
    ReplyBundleMessage(
      TextTrigger(List(StringTextTriggerValue("per il culo"))),
      List(MediaFile("pigliandoperilculo.mp3"), MediaFile("pigliandoperilculo.gif"))
    ),
    ReplyBundleMessage(
      TextTrigger(
        List(
          StringTextTriggerValue(e":lol:"),
          StringTextTriggerValue(e":rofl:"),
          StringTextTriggerValue("sorriso"),
          RegexTextTriggerValue("(ah|ha){3,}".r)
        )
      ),
      List(
        MediaFile("risata.mp3"),
        MediaFile("risata.gif"),
        MediaFile("sorriso2.gif"),
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
      TextTrigger(List(RegexTextTriggerValue("(fammi|chiedere)? (una|questa)? cortesia".r))),
      List(MediaFile("fammiquestacortesia.mp3"), MediaFile("fammiquestacortesia.gif"))
    ),
    ReplyBundleMessage(
      TextTrigger(List(StringTextTriggerValue("non mi sta bene"))),
      List(
        MediaFile("nonmistabene.mp3"),
        MediaFile("nonmistabene.gif"),
        MediaFile("nonMiStaBene2.gif")
      ),
      replySelection = RandomSelection
    ),
    ReplyBundleMessage(
      TextTrigger(
        List(
          StringTextTriggerValue("le labbra"),
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
      TextTrigger(List(RegexTextTriggerValue("te[r]+[i]+[b]+[i]+l[e]+".r))),
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
      TextTrigger(
        List(
          StringTextTriggerValue("state bene")
        )
      ),
      List(MediaFile("letSGoodStateBene.mp3"))
    ),
    ReplyBundleMessage(
      TextTrigger(List(RegexTextTriggerValue("bast[a]{3,}[!]*".r))),
      List(
        MediaFile("basta.mp3"),
        MediaFile("basta.gif"),
        MediaFile("bastaSedia.gif")
      ),
      replySelection = RandomSelection
    ),
    ReplyBundleMessage(
      TextTrigger(
        List(StringTextTriggerValue("assolo"), RegexTextTriggerValue("[gc]hi[td]arra".r))
      ),
      List(
        MediaFile("assolo.mp3"),
        MediaFile("chitarra1.gif"),
        MediaFile("chitarra2.gif"),
        MediaFile("ChitarraZuggherada.mp3")
      ),
      replySelection = RandomSelection
    ),
    ReplyBundleMessage(
      TextTrigger(
        List(
          RegexTextTriggerValue("[ ]?ca[bp]i[dt]o | ca[bp]i[dt]o[ ]?".r),
          RegexTextTriggerValue("[ ]?capissi | capissi[ ]?".r),
          StringTextTriggerValue("gabido"),
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
        MediaFile("schifosi2.mp3"),
        MediaFile("schifosi.gif")
      ),
      replySelection = RandomSelection
    )
  )

  val messageRepliesData: List[ReplyBundleMessage] =
    messageRepliesAudioData ++ messageRepliesGifsData ++ messageRepliesSpecialData

  val commandRepliesData: List[ReplyBundleCommand] = List(
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
            .map(t => {
              val (_, inputTrimmed) = t.span(_ != ' ')
              List(Bensonify.compute(inputTrimmed))
            })
            .getOrElse(List("E PARLAAAAAAA!!!!")),
        true
      )
    ),
    ReplyBundleCommand(
      trigger = CommandTrigger("instructions"),
      text = TextReply(
        _ => List(s"""
---- Instruzioni Per il Bot di Benson ----

Il bot reagisce automaticamente ai messaggi in base ai triggher che si
possono trovare dal comando:

/triggerlist

ATTENZIONE: tale comando invierà una lunga lista. Consultarlo
privatamente nella chat del bot.

Questo bot consente di convertire le frasi come le direbbe Richard
attraverso il comando:

/bensonify «Frase»

la frase è necessaria, altrimenti il bot vi risponderà in malomodo.

Infine, se si vuole disabilitare il bot per un particolare messaggio,
ad esempio per un messaggio lungo che potrebbe causare vari triggher
in una volta, è possibile farlo iniziando il messaggio con il
carattere '!':

! «Messaggio»
"""),
        false
      )
    )
  )

  def buildBot[F[_]: Timer: Parallel: ContextShift: ConcurrentEffect, A](
      executorContext: ExecutionContext,
      action: RichardPHJBensonBot[F] => F[A]
  ): F[A] =
    BlazeClientBuilder[F](executorContext).resource
      .use { client =>
        implicit val api: Api[F] = BotApi(client, baseUrl = s"https://api.telegram.org/bot$token")
        action(new RichardPHJBensonBot[F]())
      }
}
