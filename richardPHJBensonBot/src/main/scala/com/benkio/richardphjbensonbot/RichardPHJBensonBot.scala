package com.benkio.richardphjbensonbot

import org.http4s.client.blaze._
import scala.concurrent.ExecutionContext
import com.benkio.telegrambotinfrastructure.Configurations
import com.benkio.telegrambotinfrastructure.botCapabilities._
import com.benkio.telegrambotinfrastructure._
import cats.effect._
import com.benkio.telegrambotinfrastructure.model._

import com.lightbend.emoji.ShortCodes.Implicits._
import com.lightbend.emoji.ShortCodes.Defaults._
import telegramium.bots.high._
import cats._

class RichardPHJBensonBot[F[_]]()(implicit
    timerF: Timer[F],
    parallelF: Parallel[F],
    effectF: Effect[F],
    api: telegramium.bots.high.Api[F]
) extends BotSkeleton[F]()(timerF, parallelF, effectF, api) {

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
          StringTextTriggerValue("non leggo")
        )
      ),
      List(
        MediaFile("nonLeggoQuelloCheScrivete.mp3")
      )
    ),
    ReplyBundleMessage(
      TextTrigger(
        List(
          RegexTextTriggerValue("perchè (cazzo)? mi hai fatto ven[i|ì](re)?".r)
        )
      ),
      List(
        MediaFile("percheCazzoMiHaiFattoVeni.mp3")
      )
    ),
    ReplyBundleMessage(
      TextTrigger(
        List(
          StringTextTriggerValue("devo sopportare")
        )
      ),
      List(
        MediaFile("devoSopportare.mp3")
      )
    ),
    ReplyBundleMessage(
      TextTrigger(
        List(
          StringTextTriggerValue("primo sbaglio")
        )
      ),
      List(
        MediaFile("primoSbaglio.mp3")
      )
    ),
    ReplyBundleMessage(
      TextTrigger(
        List(
          StringTextTriggerValue("non mi ricordo più")
        )
      ),
      List(
        MediaFile("nonMiRicordoPiu.mp3")
      )
    ),
    ReplyBundleMessage(
      TextTrigger(
        List(
          StringTextTriggerValue("pensato a tutto"),
          StringTextTriggerValue("accontentare tutti")
        )
      ),
      List(
        MediaFile("pensatoATutto.mp3")
      )
    ),
    ReplyBundleMessage(
      TextTrigger(
        List(
          StringTextTriggerValue("vedere"),
          StringTextTriggerValue("amico")
        )
      ),
      List(
        MediaFile("vedereAmico.mp3")
      ),
      matcher = ContainsAll
    ),
    ReplyBundleMessage(
      TextTrigger(
        List(
          StringTextTriggerValue("appuntamento")
        )
      ),
      List(
        MediaFile("appuntamento.mp3")
      )
    ),
    ReplyBundleMessage(
      TextTrigger(
        List(
          StringTextTriggerValue("perchè l'ho fatto"),
          StringTextTriggerValue("non do spiegazioni")
        )
      ),
      List(
        MediaFile("percheLHoFatto.mp3")
      )
    ),
    ReplyBundleMessage(
      TextTrigger(
        List(
          StringTextTriggerValue("non ho detto tutto"),
          StringTextTriggerValue("ascoltami")
        )
      ),
      List(
        MediaFile("nonHoDettoTutto.mp3")
      )
    ),
    ReplyBundleMessage(
      TextTrigger(
        List(
          StringTextTriggerValue("ogni volta")
        )
      ),
      List(
        MediaFile("ogniVolta.mp3")
      )
    ),
    ReplyBundleMessage(
      TextTrigger(
        List(
          StringTextTriggerValue("non mi calmo")
        )
      ),
      List(
        MediaFile("nonMiCalmo.mp3")
      )
    ),
    ReplyBundleMessage(
      TextTrigger(
        List(
          StringTextTriggerValue("devo dire che")
        )
      ),
      List(
        MediaFile("devoDireChe.mp3")
      )
    ),
    ReplyBundleMessage(
      TextTrigger(
        List(
          StringTextTriggerValue("eccomi qua")
        )
      ),
      List(
        MediaFile("eccomiQua.mp3")
      )
    ),
    ReplyBundleMessage(
      TextTrigger(
        List(
          StringTextTriggerValue("animali")
        )
      ),
      List(MediaFile("animali.mp3"))
    ),
    ReplyBundleMessage(
      TextTrigger(
        List(
          RegexTextTriggerValue("gira(ndo)? la testa".r),
          StringTextTriggerValue("lampi negli occhi")
        )
      ),
      List(MediaFile("girandoLaTesta.mp3"))
    ),
    ReplyBundleMessage(
      TextTrigger(
        List(
          RegexTextTriggerValue("(mi sento|sto) meglio".r)
        )
      ),
      List(MediaFile("miSentoMeglio.mp3"))
    ),
    ReplyBundleMessage(
      TextTrigger(
        List(
          StringTextTriggerValue("altari"),
          StringTextTriggerValue("realtà"),
        )
      ),
      List(MediaFile("altariFatiscentiRealta.mp3"))
    ),
    ReplyBundleMessage(
      TextTrigger(
        List(StringTextTriggerValue("non è vero"))
      ),
      List(
        MediaFile("nonEVero.mp3")
      )
    ),
    ReplyBundleMessage(
      TextTrigger(
        List(StringTextTriggerValue("completamente nudo"))
      ),
      List(
        MediaFile("completamenteNudo.mp3")
      )
    ),
    ReplyBundleMessage(
      TextTrigger(
        List(
          StringTextTriggerValue("che cazzo era quella roba"),
          StringTextTriggerValue("che cazzo mi avete dato"),
          RegexTextTriggerValue("girar(e|a)? la testa".r),
          RegexTextTriggerValue("insieme alla (c|g)o(c|g)a (c|g)ola".r)
        )
      ),
      List(
        MediaFile("cheCazzoEraQuellaRoba.mp3")
      )
    ),
    ReplyBundleMessage(
      TextTrigger(
        List(
          StringTextTriggerValue("dovrei lavarmelo di più"),
          StringTextTriggerValue("il cazzo me lo pulisci un'altra volta"),
        )
      ),
      List(
        MediaFile("lavareCazzo.mp3")
      )
    ),
    ReplyBundleMessage(
      TextTrigger(
        List(RegexTextTriggerValue("giù( giù)+".r))
      ),
      List(
        MediaFile("giuGiuGiu.mp3")
      )
    ),
    ReplyBundleMessage(
      TextTrigger(
        List(
          StringTextTriggerValue("viale zara"),
          RegexTextTriggerValue("cas(a|e) chius(a|e)".r)
        )
      ),
      List(
        MediaFile("vialeZara.mp3")
      )
    ),
    ReplyBundleMessage(
      TextTrigger(List(StringTextTriggerValue("tocca il culo"))),
      List(MediaFile("miToccaIlCulo.mp3"))
    ),
    ReplyBundleMessage(
      TextTrigger(
        List(
          StringTextTriggerValue("negli occhiali"),
          StringTextTriggerValue("sulla spalla"),
        )
      ),
      List(MediaFile("pannaOcchialiSpalla.mp3"))
    ),
    ReplyBundleMessage(
      TextTrigger(List(StringTextTriggerValue("state zitti"))),
      List(MediaFile("stateZittiZozziUltimi.mp3"))
    ),
    ReplyBundleMessage(
      TextTrigger(
        List(
          StringTextTriggerValue("solo io")
        )
      ),
      List(
        MediaFile("soloIo.mp3")
      )
    ),
    ReplyBundleMessage(
      TextTrigger(
        List(
          StringTextTriggerValue("vaniglia"),
          StringTextTriggerValue("pandoro"),
          RegexTextTriggerValue("crema alla [gc]io[gc]+ola[dt]a".r),
        )
      ),
      List(
        MediaFile("zuccheroVanigliaCremaCioccolataPandoro.mp3")
      )
    ),
    ReplyBundleMessage(
      TextTrigger(
        List(
          StringTextTriggerValue("saranno cazzi vostri")
        )
      ),
      List(MediaFile("sarannoCazziVostri.mp3"))
    ),
    ReplyBundleMessage(
      TextTrigger(
        List(
          RegexTextTriggerValue("poteri (ter[r]+ib[b]+ili|demoniaci)".r)
        )
      ),
      List(MediaFile("poteriDemoniaci.mp3"))
    ),
    ReplyBundleMessage(
      TextTrigger(
        List(
          RegexTextTriggerValue("sono( pure)? italiane".r),
          RegexTextTriggerValue("non so(no)? ungheresi".r)
        )
      ),
      List(MediaFile("italianeUngheresi.mp3"))
    ),
    ReplyBundleMessage(
      TextTrigger(
        List(
          StringTextTriggerValue("colpevole")
        )
      ),
      List(MediaFile("ilColpevole.mp3"))
    ),
    ReplyBundleMessage(
      TextTrigger(
        List(
          StringTextTriggerValue("vi spacco il culo")
        )
      ),
      List(MediaFile("viSpaccoIlCulo.mp3"))
    ),
    ReplyBundleMessage(
      TextTrigger(
        List(
          StringTextTriggerValue("sindaco")
        )
      ),
      List(MediaFile("sindaco.mp3"))
    ),
    ReplyBundleMessage(
      TextTrigger(
        List(
          RegexTextTriggerValue("si leva (sta roba|sto schifo)".r),
          StringTextTriggerValue("questo schifo")
        )
      ),
      List(MediaFile("questoSchifo.mp3"))
    ),
    ReplyBundleMessage(
      TextTrigger(
        List(
          StringTextTriggerValue("preservativo")
        )
      ),
      List(MediaFile("preservativo.mp3"))
    ),
    ReplyBundleMessage(
      TextTrigger(List(RegexTextTriggerValue("io nooo[o]*".r))),
      List(MediaFile("ioNo.mp3"))
    ),
    ReplyBundleMessage(
      TextTrigger(
        List(
          StringTextTriggerValue("cristo pinocchio"),
          RegexTextTriggerValue("(strade|vie) inferiori".r),
        )
      ),
      List(
        MediaFile("cristoPinocchio.mp3")
      )
    ),
    ReplyBundleMessage(
      TextTrigger(
        List(
          StringTextTriggerValue("giuda"),
          StringTextTriggerValue("chi è cristo"),
          StringTextTriggerValue("si è fatto fregare"),
          StringTextTriggerValue("bacio di un frocio"),
        )
      ),
      List(
        MediaFile("chiECristo.mp3")
      )
    ),
    ReplyBundleMessage(
      TextTrigger(
        List(
          StringTextTriggerValue("cameriera"),
          StringTextTriggerValue("moglie"),
          StringTextTriggerValue("si sposa"),
          StringTextTriggerValue("matrimonio")
        )
      ),
      List(
        MediaFile("cameriera.mp3")
      )
    ),
    ReplyBundleMessage(
      TextTrigger(
        List(
          StringTextTriggerValue("korn"),
          StringTextTriggerValue("giovanni battista"),
          StringTextTriggerValue("acque del giordano"),
          StringTextTriggerValue("battezzato"),
          StringTextTriggerValue("battesimo")
        )
      ),
      List(
        MediaFile("battesimo.mp3")
      )
    ),
    ReplyBundleMessage(
      TextTrigger(
        List(
          RegexTextTriggerValue("\\bpasqua\\b".r)
        )
      ),
      List(
        MediaFile("auguriPasqua.mp3")
      )
    ),
    ReplyBundleMessage(
      TextTrigger(
        List(
          RegexTextTriggerValue("a( )?s[s]+tronzo".r),
          RegexTextTriggerValue("stronz[o]{3,}".r)
        )
      ),
      List(
        MediaFile("aStronzo.mp3")
      )
    ),
    ReplyBundleMessage(
      TextTrigger(
        List(
          StringTextTriggerValue("ancora voi")
        )
      ),
      List(
        MediaFile("ancoraVoi.mp3")
      )
    ),
    ReplyBundleMessage(
      TextTrigger(
        List(
          StringTextTriggerValue("non ci credete?"),
          RegexTextTriggerValue("grande s[dt]ronza[dt][ea]".r)
        )
      ),
      List(
        MediaFile("nonCiCredete.gif"),
        MediaFile("nonCiCredete.mp3"),
      )
    ),
    ReplyBundleMessage(
      TextTrigger(
        List(
          StringTextTriggerValue("sborrata"),
          StringTextTriggerValue("scopare")
        )
      ),
      List(MediaFile("sborrata.mp3"))
    ),
    ReplyBundleMessage(
      TextTrigger(
        List(
          StringTextTriggerValue("finire male"),
          StringTextTriggerValue("tocca benson")
        )
      ),
      List(MediaFile("finireMale.mp3"))
    ),
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
          StringTextTriggerValue("anguille"),
          StringTextTriggerValue("polipi"),
          StringTextTriggerValue("cetrioli"),
          RegexTextTriggerValue(".*problema.*suonare.*".r)
        )
      ),
      List(MediaFile("problema.mp3"))
    ), // also in special list
    ReplyBundleMessage(TextTrigger(List(StringTextTriggerValue("continua"))), List(MediaFile("continua.mp3"))),
    ReplyBundleMessage(
      TextTrigger(
        List(
          RegexTextTriggerValue("au[ ]?de".r),
          RegexTextTriggerValue("\\btime\\b".r),
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
          StringTextTriggerValue("stanco")
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
    ReplyBundleMessage(TextTrigger(List(StringTextTriggerValue("brutto frocio"))), List(MediaFile("bruttoFrocio.mp3"))),
    ReplyBundleMessage(
      TextTrigger(
        List(
          StringTextTriggerValue("questo ragazzo"),
          StringTextTriggerValue("eccitare"),
          StringTextTriggerValue("lucio dalla")
        )
      ),
      List(MediaFile("lucioDalla.mp3"))
    ),
    ReplyBundleMessage(
      TextTrigger(
        List(StringTextTriggerValue("chiesa"), StringTextTriggerValue("preghiera"), StringTextTriggerValue("non credo"))
      ),
      List(MediaFile("chiesa.mp3"))
    ),
    ReplyBundleMessage(TextTrigger(List(StringTextTriggerValue("maledetto"))), List(MediaFile("maledetto.mp3"))),
    ReplyBundleMessage(
      TextTrigger(
        List(
          StringTextTriggerValue("..magari"),
          StringTextTriggerValue("magari.."),
        )
      ),
      List(MediaFile("magari.mp3"))
    ),
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
      List(MediaFile("figureMitologiche.mp3"))
    ),
    ReplyBundleMessage(
      TextTrigger(List(RegexTextTriggerValue("[a]+[t]{2,}[e]+[n]+[z]+[i]+[o]+[n]+[e]+[!]*[!e]$".r))),
      List(MediaFile("attenzione.mp3"))
    ),
    ReplyBundleMessage(
      TextTrigger(List(StringTextTriggerValue("poveri cretini"), StringTextTriggerValue("poveri ignoranti"))),
      List(MediaFile("poveriCretini.mp3"))
    ),
    ReplyBundleMessage(TextTrigger(List(StringTextTriggerValue("due ossa"))), List(MediaFile("dueOssa.mp3"))),
    ReplyBundleMessage(
      TextTrigger(
        List(
          RegexTextTriggerValue("mi fai( proprio)? schifo".r)
        )
      ),
      List(MediaFile("schifo.mp3"))
    ),
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
    ReplyBundleMessage(
      TextTrigger(
        List(
          RegexTextTriggerValue("(af+)?fanculo(,)? per contesia".r)
        )
      ),
      List(MediaFile("fanculoPerCortesia.gif"))
    ),
    ReplyBundleMessage(
      TextTrigger(
        List(
          StringTextTriggerValue("gli autori")
        )
      ),
      List(MediaFile("autori.gif"))
    ),
    ReplyBundleMessage(
      TextTrigger(
        List(
          StringTextTriggerValue("questo è matto")
        )
      ),
      List(MediaFile("mattoRagazzi.gif"))
    ),
    ReplyBundleMessage(
      TextTrigger(List(StringTextTriggerValue("ultimi"))),
      List(
        MediaFile("viCalpesto.gif"),
        MediaFile("ultimi.gif"),
        MediaFile("stateZittiZozziUltimi.mp3"),
      ),
      replySelection = RandomSelection
    ),
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
      List(
        MediaFile("andareAvanti.gif"),
        MediaFile("andareAvanti.mp3")
      ),
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
          StringTextTriggerValue("ed è vero")
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
      List(MediaFile("miPareLogico.gif"))
    ),
    ReplyBundleMessage(TextTrigger(List(StringTextTriggerValue("tucul"))), List(MediaFile("tuCul.gif"))),
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
      TextTrigger(List(StringTextTriggerValue("piloti d'aereo"), StringTextTriggerValue("disastri aerei"))),
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
    ReplyBundleMessage(TextTrigger(List(StringTextTriggerValue("uno scherzo"))), List(MediaFile("scherzo.gif"))),
    ReplyBundleMessage(
      TextTrigger(
        List(
          StringTextTriggerValue("vi calpesto"),
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
          RegexTextTriggerValue("ma si può dire una cosa (del genere|così)".r),
        )
      ),
      List(MediaFile("deficienza.gif"))
    ),
    ReplyBundleMessage(
      TextTrigger(
        List(
          StringTextTriggerValue("non sapere"),
          RegexTextTriggerValue("aris[dt]o[dt]ele".r)
        )
      ),
      List(MediaFile("soDiNonSapere.gif"))
    ),
    ReplyBundleMessage(
      TextTrigger(
        List(
          RegexTextTriggerValue("lo[g]+i[cg]o".r)
        )
      ),
      List(MediaFile("miPareLogico.gif"), MediaFile("sembraLogico.gif")),
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
        MediaFile("nonMeNeFotte.gif"),
        MediaFile("nonMeNeFrega.gif"),
        MediaFile("eNonMeNeFotteUnCazzo.mp3"),
        MediaFile("nonLeggoQuelloCheScrivete.mp3")
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
      List(MediaFile("levatiDaiCoglioni.gif"))
    ),
    ReplyBundleMessage(
      TextTrigger(
        List(
          RegexTextTriggerValue("(non|mica) so(no)? (un |n )?co(gl|j)ione".r),
          RegexTextTriggerValue("sarete co(gl|j)ioni voi".r)
        )
      ),
      List(MediaFile("sareteCoglioniVoi.gif"))
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
      List(MediaFile("porcaMiseria.gif"))
    ),
    ReplyBundleMessage(
      TextTrigger(List(RegexTextTriggerValue("schifos(o|!)+".r))),
      List(MediaFile("schifoso.gif"))
    ),
    ReplyBundleMessage(
      TextTrigger(List(StringTextTriggerValue("facendo soffrire"))),
      List(MediaFile("facendoSoffrire.gif"))
    ),
    ReplyBundleMessage(
      TextTrigger(List(StringTextTriggerValue("sentendo male"))),
      List(MediaFile("miStoSentendoMale.gif"))
    ),
    ReplyBundleMessage(TextTrigger(List(StringTextTriggerValue("stare male"))), List(MediaFile("miFaStareMale.gif"))),
    ReplyBundleMessage(
      TextTrigger(List(StringTextTriggerValue("lunghezza d'onda"), StringTextTriggerValue("brave persone"))),
      List(MediaFile("lunghezzaDOnda.gif"))
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
      List(MediaFile("nonVoglioNessuno.gif"))
    ),
    ReplyBundleMessage(
      TextTrigger(List(StringTextTriggerValue("andati al cesso"), StringTextTriggerValue("diecimila volte"))),
      List(MediaFile("alcesso.gif"))
    ),
    ReplyBundleMessage(
      TextTrigger(List(StringTextTriggerValue("non manca niente"), StringTextTriggerValue("c'é tutto"))),
      List(MediaFile("nonMancaNiente.gif"))
    ),
    ReplyBundleMessage(TextTrigger(List(StringTextTriggerValue("in fila"))), List(MediaFile("mettitiInFila.gif"))),
    ReplyBundleMessage(TextTrigger(List(StringTextTriggerValue("non male"))), List(MediaFile("nonMale.gif"))),
    ReplyBundleMessage(TextTrigger(List(StringTextTriggerValue("si sente"))), List(MediaFile("siSente.gif"))),
    ReplyBundleMessage(
      TextTrigger(
        List(
          StringTextTriggerValue("colpa vostra"),
          StringTextTriggerValue("pazzo"),
          RegexTextTriggerValue("(divento|diventare|sono) matto".r)
        )
      ),
      List(MediaFile("stoDiventandoPazzo.gif"))
    ),
    ReplyBundleMessage(
      TextTrigger(
        List(
          StringTextTriggerValue("sorca"),
          StringTextTriggerValue("lecciso"),
          RegexTextTriggerValue("\\bfiga\\b".r)
        )
      ),
      List(MediaFile("sorcaLecciso.gif"))
    ),
    ReplyBundleMessage(
      TextTrigger(
        List(
          StringTextTriggerValue("non li sopporto"),
          RegexTextTriggerValue("che si deve f(à|are)".r),
          StringTextTriggerValue("bisogna pure lavorà")
        )
      ),
      List(MediaFile("nonLiSopporto.gif"))
    ),
    ReplyBundleMessage(TextTrigger(List(StringTextTriggerValue("chi cazzo sei"))), List(MediaFile("chiCazzoSei.gif"))),
    ReplyBundleMessage(TextTrigger(List(StringTextTriggerValue("feste"))), List(MediaFile("feste.gif"))),
    ReplyBundleMessage(
      TextTrigger(List(StringTextTriggerValue("si ostina"), StringTextTriggerValue("foto vecchie"))),
      List(MediaFile("ostina.gif"))
    ),
    ReplyBundleMessage(
      TextTrigger(
        List(
          RegexTextTriggerValue("(è|diventa) vecchi[ao]".r),
        )
      ),
      List(MediaFile("vecchio.gif"))
    ),
    ReplyBundleMessage(
      TextTrigger(List(StringTextTriggerValue("scatta qualcosa"))),
      List(MediaFile("scattaQualcosa.gif"))
    ),
    ReplyBundleMessage(
      TextTrigger(List(RegexTextTriggerValue("sput[ao]".r))),
      List(MediaFile("sputo.gif"))
    ),
    ReplyBundleMessage(
      TextTrigger(List(StringTextTriggerValue("lasciami in pace"), RegexTextTriggerValue("\\bstronza\\b".r))),
      List(MediaFile("lasciamiInPace.gif"))
    ),
    ReplyBundleMessage(TextTrigger(List(StringTextTriggerValue("pure bona"))), List(MediaFile("bona.gif"))),
    ReplyBundleMessage(
      TextTrigger(
        List(StringTextTriggerValue(" danza"), StringTextTriggerValue("macabra"), StringTextTriggerValue(" ball"))
      ),
      List(MediaFile("danzaMacabra.gif"))
    ),
    ReplyBundleMessage(
      TextTrigger(List(RegexTextTriggerValue("sei [gc]ambiat[oa]".r))),
      List(MediaFile("seiCambiata.gif"))
    ),
    ReplyBundleMessage(
      TextTrigger(List(StringTextTriggerValue("mio discapito"), StringTextTriggerValue("disgabido"))),
      List(MediaFile("discapito.gif"))
    ),
    ReplyBundleMessage(
      TextTrigger(List(StringTextTriggerValue("peggio del peggio"))),
      List(MediaFile("peggioDelPeggio.gif"))
    ),
    ReplyBundleMessage(
      TextTrigger(List(StringTextTriggerValue("squallida"), StringTextTriggerValue("abbia mai sentito"))),
      List(MediaFile("squallida.gif"))
    ),
    ReplyBundleMessage(TextTrigger(List(StringTextTriggerValue("la verità"))), List(MediaFile("verita.gif"))),
    ReplyBundleMessage(
      TextTrigger(List(StringTextTriggerValue("ti dovresti vergognare"))),
      List(MediaFile("tiDovrestiVergognare.gif"))
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
    ReplyBundleMessage(TextTrigger(List(StringTextTriggerValue("errori"))), List(MediaFile("maiErrori.gif"))),
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
      TextTrigger(List(RegexTextTriggerValue("\\bester\\b".r), StringTextTriggerValue("esposito"))),
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
      TextTrigger(
        List(
          StringTextTriggerValue("quello che dico io")
        )
      ),
      List(
        MediaFile("quelloCheDicoIo.gif"),
        MediaFile("fannoQuelloCheDicoIo.mp3")
      )
    ),
    ReplyBundleMessage(
      TextTrigger(
        List(
          StringTextTriggerValue("zucchero")
        )
      ),
      List(
        MediaFile("ChitarraZuggherada.mp3"),
        MediaFile("zucchero.mp3"),
        MediaFile("zuccheroVanigliaCremaCioccolataPandoro.mp3"),
      ),
      replySelection = RandomSelection
    ),
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
        MediaFile("okGoodShowFriends.gif"),
        MediaFile("okGoodShowFriends2.gif"),
        MediaFile("letSGoodStateBene.mp3"),
        MediaFile("welaLetsGoodMyFriends.mp3")
      ),
      replySelection = RandomSelection
    ),
    ReplyBundleMessage(
      TextTrigger(
        List(
          RegexTextTriggerValue("vattene (af)?fanculo".r)
        )
      ),
      List(MediaFile("maVatteneAffanculo.gif"), MediaFile("maVatteneAffanculo.mp3"))
    ),
    ReplyBundleMessage(
      TextTrigger(List(StringTextTriggerValue("feelings"))),
      List(
        MediaFile("feelings.gif"),
        MediaFile("feelings2.gif"),
        MediaFile("feelings.mp3")
      ),
      replySelection = RandomSelection
    ),
    ReplyBundleMessage(
      TextTrigger(List(StringTextTriggerValue("me ne vado"))),
      List(
        MediaFile("meNeVado.mp3"),
        MediaFile("miRompiErCazzo.gif"),
        MediaFile("meNeVado.gif")
      ),
      replySelection = RandomSelection
    ),
    ReplyBundleMessage(
      TextTrigger(
        List(StringTextTriggerValue("mignotta"), StringTextTriggerValue("puttana"), StringTextTriggerValue("troia"))
      ),
      List(
        MediaFile("mignotta.mp3"),
        MediaFile("mignotta.gif"),
        MediaFile("vialeZara.mp3")
      ),
      replySelection = RandomSelection
    ),
    ReplyBundleMessage(
      TextTrigger(List(StringTextTriggerValue("ti devi spaventare"))),
      List(MediaFile("tiDeviSpaventare.mp3"), MediaFile("tiDeviSpaventare.gif"))
    ),
    ReplyBundleMessage(
      TextTrigger(List(StringTextTriggerValue("ma che cazzo sto dicendo"), StringTextTriggerValue("il martell"))),
      List(MediaFile("maCheCazzoStoDicendo.mp3"), MediaFile("maCheCazzoStoDicendo.gif"))
    ),
    ReplyBundleMessage(
      TextTrigger(List(StringTextTriggerValue("questa volta no"))),
      List(MediaFile("questaVoltaNo.mp3"), MediaFile("questaVoltaNo.gif"))
    ),
    ReplyBundleMessage(
      TextTrigger(List(StringTextTriggerValue("una vergogna"))),
      List(
        MediaFile("vergogna.mp3"),
        MediaFile("vergogna.gif"),
        MediaFile("vergogna2.gif")
      ),
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
      List(MediaFile("maSgus.mp3"), MediaFile("maSgus.gif"))
    ),
    ReplyBundleMessage(
      TextTrigger(
        List(StringTextTriggerValue("grazie gianni"), RegexTextTriggerValue("cia[o]{3,}".r))
      ),
      List(MediaFile("grazie.mp3"), MediaFile("grazie.gif"))
    ),
    ReplyBundleMessage(
      TextTrigger(List(StringTextTriggerValue("stare attenti"), StringTextTriggerValue("per strada"))),
      List(MediaFile("incontratePerStrada.mp3"), MediaFile("incontratePerStrada.gif"))
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
      List(MediaFile("pigliandoPerIlCulo.mp3"), MediaFile("pigliandoPerIlCulo.gif"))
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
      List(MediaFile("fammiQuestaCortesia.mp3"), MediaFile("fammiQuestaCortesia.gif"))
    ),
    ReplyBundleMessage(
      TextTrigger(List(StringTextTriggerValue("non mi sta bene"))),
      List(
        MediaFile("nonMiStaBene.mp3"),
        MediaFile("nonMiStaBene2.mp3"),
        MediaFile("nonMiStaBene.gif"),
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
      List(MediaFile("vitaNemico.mp3"), MediaFile("vitaNemico.gif"))
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
      List(MediaFile("ciaoComeState.gif"), MediaFile("ciaoComeState.mp3"))
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
        MediaFile("basta2.mp3"),
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
        MediaFile("hoCapito.mp3"),
        MediaFile("aveteCapito.mp3"),
        MediaFile("capito.mp3"),
        MediaFile("nonHannoCapitoUnCazzo.mp3"),
        MediaFile("aveteCapitoComeSempre.gif"),
        MediaFile("nonAveteCapitoUnCazzo.gif"),
        MediaFile("voiNonAveteCapitoUnCazzo.gif"),
        MediaFile("ilSensoCapito.gif"),
        MediaFile("capitoDoveStiamo.gif"),
        MediaFile("nonHoCapito.gif"),
        MediaFile("comeAlSolitoNonAveteCapito.gif"),
        MediaFile("capitoDoveStiamo.mp3"),
        MediaFile("capisciRidotti.mp3")
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
        MediaFile("schifosi3.mp3"),
        MediaFile("stateZittiZozziUltimi.mp3"),
        MediaFile("schifosi.gif")
      ),
      replySelection = RandomSelection
    ),
    ReplyBundleMessage(
      TextTrigger(List(StringTextTriggerValue("mortacci vostri"))),
      List(
        MediaFile("mortacciVostri.gif"),
        MediaFile("stateZittiZozziUltimi.mp3"),
      ),
      replySelection = RandomSelection
    ),
    ReplyBundleMessage(
      TextTrigger(
        List(
          StringTextTriggerValue("non vedo")
        )
      ),
      List(
        MediaFile("stanco.mp3"),
        MediaFile("pannaOcchialiSpalla.mp3"),
      ),
      replySelection = RandomSelection
    ),
    ReplyBundleMessage(
      TextTrigger(
        List(
          StringTextTriggerValue("panna")
        )
      ),
      List(
        MediaFile("problema.mp3"),
        MediaFile("pannaOcchialiSpalla.mp3")
      ),
      replySelection = RandomSelection
    ),
    ReplyBundleMessage(
      TextTrigger(
        List(
          StringTextTriggerValue("applau")
        )
      ),
      List(
        MediaFile("applauso.gif"),
        MediaFile("applauso.mp3"),
        MediaFile("applauso2.mp3"),
      ),
      replySelection = RandomSelection
    ),
    ReplyBundleMessage(
      TextTrigger(
        List(
          StringTextTriggerValue("venite qua")
        )
      ),
      List(
        MediaFile("veniteQua.gif"),
        MediaFile("veniteQua.mp3")
      )
    ),
    ReplyBundleMessage(
      TextTrigger(
        List(
          RegexTextTriggerValue("\\bpaga(re)?\\b".r),
          StringTextTriggerValue("soldi"),
          StringTextTriggerValue("bollette"),
          StringTextTriggerValue("tasse"),
          StringTextTriggerValue("bolletta"),
          StringTextTriggerValue("tassa")
        )
      ),
      List(
        MediaFile("chiCacciaISoldi.gif"),
        MediaFile("chiCacciaISoldi.mp3")
      )
    ),
    ReplyBundleMessage(
      TextTrigger(List(RegexTextTriggerValue("[od]?dio mio[,]? no".r))),
      List(
        MediaFile("oddioMioNo.gif"),
        MediaFile("oddioMioNo.mp3")
      ),
      replySelection = RandomSelection
    ),
    ReplyBundleMessage(
      TextTrigger(
        List(
          RegexTextTriggerValue("[sono ]?a[r]{1,2}iva[dt]o".r),
          StringTextTriggerValue("piacere")
        )
      ),
      List(
        MediaFile("arivato.gif"),
        MediaFile("arivato.mp3")
      )
    ),
    ReplyBundleMessage(
      TextTrigger(List(RegexTextTriggerValue("delu[sd]".r))),
      List(
        MediaFile("deluso.gif"),
        MediaFile("deluso.mp3")
      )
    ),
    ReplyBundleMessage(
      TextTrigger(
        List(
          StringTextTriggerValue("fate come vi pare"),
          RegexTextTriggerValue("sti [gc]azzi".r)
        )
      ),
      List(
        MediaFile("comeViPare.gif"),
        MediaFile("comeViPare.mp3")
      ),
      replySelection = RandomSelection
    ),
    ReplyBundleMessage(
      TextTrigger(
        List(
          StringTextTriggerValue("divento una bestia"),
          StringTextTriggerValue("incazzo")
        )
      ),
      List(
        MediaFile("diventoBestia.mp3"),
        MediaFile("incazzo.mp3"),
        MediaFile("incazzo2.mp3")
      ),
      replySelection = RandomSelection
    )
  )

  val messageRepliesData: List[ReplyBundleMessage] =
    messageRepliesAudioData ++ messageRepliesGifsData ++ messageRepliesSpecialData

  val messageReplyDataStringChunks = {
    val (triggers, lastTriggers) = messageRepliesData
      .map(_.trigger match {
        case TextTrigger(lt) => lt.mkString("[", " - ", "]")
        case _               => ""
      })
      .foldLeft((List.empty[List[String]], List.empty[String])) { case ((acc, candidate), triggerString) =>
        if ((candidate :+ triggerString).mkString("\n").length > 4090) (acc :+ candidate, List(triggerString))
        else (acc, candidate :+ triggerString)
      }
    triggers :+ lastTriggers
  }

  val commandRepliesData: List[ReplyBundleCommand] = List(
    ReplyBundleCommand(
      trigger = CommandTrigger("triggerlist"),
      text = TextReply(
        _ => messageReplyDataStringChunks,
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
              List(List(Bensonify.compute(inputTrimmed)))
            })
            .getOrElse(List(List("E PARLAAAAAAA!!!!"))),
        true
      )
    ),
    ReplyBundleCommand(
      trigger = CommandTrigger("instructions"),
      text = TextReply(
        _ => List(List(s"""
---- Instruzioni Per il Bot di Benson ----

Il bot reagisce automaticamente ai messaggi in base ai trigger che si
possono trovare dal comando:

/triggerlist

ATTENZIONE: tale comando invierà una lunga lista. Consultarlo
privatamente nella chat del bot.

Questo bot consente di convertire le frasi come le direbbe Richard
attraverso il comando:

/bensonify «Frase»

la frase è necessaria, altrimenti il bot vi risponderà in malomodo.

Infine, se si vuole disabilitare il bot per un particolare messaggio,
ad esempio per un messaggio lungo che potrebbe causare vari trigger
in una volta, è possibile farlo iniziando il messaggio con il
carattere '!':

! «Messaggio»
""")),
        false
      )
    )
  )
  def token[F[_]](implicit effectF: Effect[F]): Resource[F, String] =
    ResourceAccess.fileSystem.getResourceByteArray[F]("richardPHJBensonBot.token").map(_.map(_.toChar).mkString)

  def buildBot[F[_], A](
      executorContext: ExecutionContext,
      action: RichardPHJBensonBot[F] => F[A]
  )(implicit
      timerF: Timer[F],
      parallelF: Parallel[F],
      contextShiftF: ContextShift[F],
      concurrentEffectF: ConcurrentEffect[F]
  ): F[A] = (for {
    client <- BlazeClientBuilder[F](executorContext).resource
    tk     <- token[F]
  } yield (client, tk)).use(client_tk => {
    implicit val api: Api[F] = BotApi(client_tk._1, baseUrl = s"https://api.telegram.org/bot${client_tk._2}")
    action(new RichardPHJBensonBot[F]())
  })
}
