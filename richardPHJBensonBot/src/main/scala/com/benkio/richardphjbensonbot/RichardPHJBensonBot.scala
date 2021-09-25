package com.benkio.richardphjbensonbot

import cats._
import cats.effect._
import com.benkio.telegrambotinfrastructure.botCapabilities._
import com.benkio.telegrambotinfrastructure.model._
import com.benkio.telegrambotinfrastructure.Configurations
import com.benkio.telegrambotinfrastructure._
import com.lightbend.emoji.ShortCodes.Defaults._
import com.lightbend.emoji.ShortCodes.Implicits._
import org.http4s.blaze.client._
import telegramium.bots.high._

import scala.concurrent.ExecutionContext

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
          StringTextTriggerValue("tastierista")
        ),
      ),
      List(
        MediaFile("rphjb_Tastierista.mp3")
      )
    ),
    ReplyBundleMessage(
      TextTrigger(
        List(
          StringTextTriggerValue("caprette"),
          StringTextTriggerValue("acidi"),
          StringTextTriggerValue("pomodori"),
          StringTextTriggerValue("legumi"),
          StringTextTriggerValue("ratti"),
          StringTextTriggerValue("topi"),
          StringTextTriggerValue("ragni"),
          StringTextTriggerValue("male il collo"),
        )
      ),
      List(
        MediaFile("rphjb_ListaMaleCollo.mp3")
      )
    ),
    ReplyBundleMessage(
      TextTrigger(
        List(
          RegexTextTriggerValue("scu[-]?sa[h]? scu[-]?sa[h]?".r)
        )
      ),
      List(
        MediaFile("rphjb_Scusa.mp3")
      )
    ),
    ReplyBundleMessage(
      TextTrigger(
        List(
          StringTextTriggerValue("lo sapevo")
        )
      ),
      List(
        MediaFile("rphjb_LoSapevoIo.mp3")
      )
    ),
    ReplyBundleMessage(
      TextTrigger(
        List(
          StringTextTriggerValue("mi auguro")
        )
      ),
      List(
        MediaFile("rphjb_IoMiAuguro.mp3")
      )
    ),
    ReplyBundleMessage(
      TextTrigger(
        List(
          StringTextTriggerValue("non leggo")
        )
      ),
      List(
        MediaFile("rphjb_NonLeggoQuelloCheScrivete.mp3")
      )
    ),
    ReplyBundleMessage(
      TextTrigger(
        List(
          RegexTextTriggerValue("perchè (cazzo)? mi hai fatto ven[i|ì](re)?".r)
        )
      ),
      List(
        MediaFile("rphjb_PercheCazzoMiHaiFattoVeni.mp3")
      )
    ),
    ReplyBundleMessage(
      TextTrigger(
        List(
          StringTextTriggerValue("devo sopportare")
        )
      ),
      List(
        MediaFile("rphjb_DevoSopportare.mp3")
      )
    ),
    ReplyBundleMessage(
      TextTrigger(
        List(
          StringTextTriggerValue("primo sbaglio")
        )
      ),
      List(
        MediaFile("rphjb_PrimoSbaglio.mp3")
      )
    ),
    ReplyBundleMessage(
      TextTrigger(
        List(
          StringTextTriggerValue("non mi ricordo più")
        )
      ),
      List(
        MediaFile("rphjb_NonMiRicordoPiu.mp3")
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
        MediaFile("rphjb_PensatoATutto.mp3")
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
        MediaFile("rphjb_VedereAmico.mp3")
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
        MediaFile("rphjb_Appuntamento.mp3")
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
        MediaFile("rphjb_PercheLHoFatto.mp3")
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
        MediaFile("rphjb_NonHoDettoTutto.mp3")
      )
    ),
    ReplyBundleMessage(
      TextTrigger(
        List(
          StringTextTriggerValue("ogni volta")
        )
      ),
      List(
        MediaFile("rphjb_OgniVolta.mp3")
      )
    ),
    ReplyBundleMessage(
      TextTrigger(
        List(
          StringTextTriggerValue("non mi calmo")
        )
      ),
      List(
        MediaFile("rphjb_NonMiCalmo.mp3")
      )
    ),
    ReplyBundleMessage(
      TextTrigger(
        List(
          StringTextTriggerValue("devo dire che")
        )
      ),
      List(
        MediaFile("rphjb_DevoDireChe.mp3")
      )
    ),
    ReplyBundleMessage(
      TextTrigger(
        List(
          StringTextTriggerValue("eccomi qua")
        )
      ),
      List(
        MediaFile("rphjb_EccomiQua.mp3")
      )
    ),
    ReplyBundleMessage(
      TextTrigger(
        List(
          StringTextTriggerValue("animali")
        )
      ),
      List(MediaFile("rphjb_Animali.mp3"))
    ),
    ReplyBundleMessage(
      TextTrigger(
        List(
          RegexTextTriggerValue("(mi sento|sto) meglio".r)
        )
      ),
      List(MediaFile("rphjb_MiSentoMeglio.mp3"))
    ),
    ReplyBundleMessage(
      TextTrigger(
        List(
          StringTextTriggerValue("altari"),
          StringTextTriggerValue("realtà"),
        )
      ),
      List(MediaFile("rphjb_AltariFatiscentiRealta.mp3"))
    ),
    ReplyBundleMessage(
      TextTrigger(
        List(StringTextTriggerValue("non è vero"))
      ),
      List(
        MediaFile("rphjb_NonEVero.mp3")
      )
    ),
    ReplyBundleMessage(
      TextTrigger(
        List(StringTextTriggerValue("completamente nudo"))
      ),
      List(
        MediaFile("rphjb_CompletamenteNudo.mp3")
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
        MediaFile("rphjb_LavareCazzo.mp3")
      )
    ),
    ReplyBundleMessage(
      TextTrigger(
        List(RegexTextTriggerValue("giù( giù)+".r))
      ),
      List(
        MediaFile("rphjb_GiuGiuGiu.mp3")
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
        MediaFile("rphjb_VialeZara.mp3")
      )
    ),
    ReplyBundleMessage(
      TextTrigger(List(StringTextTriggerValue("tocca il culo"))),
      List(MediaFile("rphjb_MiToccaIlCulo.mp3"))
    ),
    ReplyBundleMessage(
      TextTrigger(
        List(
          StringTextTriggerValue("negli occhiali"),
          StringTextTriggerValue("sulla spalla"),
        )
      ),
      List(MediaFile("rphjb_PannaOcchialiSpalla.mp3"))
    ),
    ReplyBundleMessage(
      TextTrigger(List(StringTextTriggerValue("state zitti"))),
      List(MediaFile("rphjb_StateZittiZozziUltimi.mp3"))
    ),
    ReplyBundleMessage(
      TextTrigger(
        List(
          StringTextTriggerValue("solo io")
        )
      ),
      List(
        MediaFile("rphjb_SoloIo.mp3")
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
        MediaFile("rphjb_ZuccheroVanigliaCremaCioccolataPandoro.mp3")
      )
    ),
    ReplyBundleMessage(
      TextTrigger(
        List(
          StringTextTriggerValue("saranno cazzi vostri")
        )
      ),
      List(MediaFile("rphjb_SarannoCazziVostri.mp3"))
    ),
    ReplyBundleMessage(
      TextTrigger(
        List(
          RegexTextTriggerValue("poteri (ter[r]+ib[b]+ili|demoniaci)".r)
        )
      ),
      List(MediaFile("rphjb_PoteriDemoniaci.mp3"))
    ),
    ReplyBundleMessage(
      TextTrigger(
        List(
          RegexTextTriggerValue("sono( pure)? italiane".r),
          RegexTextTriggerValue("non so(no)? ungheresi".r)
        )
      ),
      List(MediaFile("rphjb_ItalianeUngheresi.mp3"))
    ),
    ReplyBundleMessage(
      TextTrigger(
        List(
          StringTextTriggerValue("colpevole")
        )
      ),
      List(MediaFile("rphjb_IlColpevole.mp3"))
    ),
    ReplyBundleMessage(
      TextTrigger(
        List(
          StringTextTriggerValue("vi spacco il culo")
        )
      ),
      List(MediaFile("rphjb_ViSpaccoIlCulo.mp3"))
    ),
    ReplyBundleMessage(
      TextTrigger(
        List(
          StringTextTriggerValue("sindaco")
        )
      ),
      List(MediaFile("rphjb_Sindaco.mp3"))
    ),
    ReplyBundleMessage(
      TextTrigger(
        List(
          RegexTextTriggerValue("si leva (sta roba|sto schifo)".r),
          StringTextTriggerValue("questo schifo")
        )
      ),
      List(MediaFile("rphjb_QuestoSchifo.mp3"))
    ),
    ReplyBundleMessage(
      TextTrigger(
        List(
          StringTextTriggerValue("preservativo")
        )
      ),
      List(MediaFile("rphjb_Preservativo.mp3"))
    ),
    ReplyBundleMessage(
      TextTrigger(List(RegexTextTriggerValue("io nooo[o]*".r))),
      List(MediaFile("rphjb_IoNo.mp3"))
    ),
    ReplyBundleMessage(
      TextTrigger(
        List(
          StringTextTriggerValue("cristo pinocchio"),
          RegexTextTriggerValue("(strade|vie) inferiori".r),
        )
      ),
      List(
        MediaFile("rphjb_CristoPinocchio.mp3")
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
        MediaFile("rphjb_ChiECristo.mp3")
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
        MediaFile("rphjb_Cameriera.mp3")
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
        MediaFile("rphjb_Battesimo.mp3")
      )
    ),
    ReplyBundleMessage(
      TextTrigger(
        List(
          RegexTextTriggerValue("\\bpasqua\\b".r)
        )
      ),
      List(
        MediaFile("rphjb_AuguriPasqua.mp3")
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
        MediaFile("rphjb_AStronzo.mp3")
      )
    ),
    ReplyBundleMessage(
      TextTrigger(
        List(
          StringTextTriggerValue("ancora voi")
        )
      ),
      List(
        MediaFile("rphjb_AncoraVoi.mp3")
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
        MediaFile("rphjb_NonCiCredete.gif"),
        MediaFile("rphjb_NonCiCredete.mp3"),
      )
    ),
    ReplyBundleMessage(
      TextTrigger(
        List(
          StringTextTriggerValue("sborrata"),
          StringTextTriggerValue("scopare")
        )
      ),
      List(MediaFile("rphjb_Sborrata.mp3"))
    ),
    ReplyBundleMessage(
      TextTrigger(
        List(
          StringTextTriggerValue("finire male"),
          StringTextTriggerValue("tocca benson")
        )
      ),
      List(MediaFile("rphjb_FinireMale.mp3"))
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
      List(MediaFile("rphjb_Rock.mp3"))
    ),
    ReplyBundleMessage(
      TextTrigger(
        List(
          RegexTextTriggerValue("conosce(nza|re)".r),
          StringTextTriggerValue("il sapere"),
          StringTextTriggerValue("veri valori"),
        )
      ),
      List(MediaFile("rphjb_Conoscere.mp3"))
    ),
    ReplyBundleMessage(
      TextTrigger(
        List(
          RegexTextTriggerValue("stori(a|e)".r)
        )
      ),
      List(
        MediaFile("rphjb_Storie.mp3"),
        MediaFile("rphjb_Storie2.mp3"),
      ),
      replySelection = RandomSelection
    ),
    ReplyBundleMessage(
      TextTrigger(
        List(
          RegexTextTriggerValue("vo[l]+[o]*[u]+[ou]*me".r)
        )
      ),
      List(MediaFile("rphjb_MenoVolume.mp3"))
    ),
    ReplyBundleMessage(
      TextTrigger(List(StringTextTriggerValue("sono"), StringTextTriggerValue("ultimo"))),
      List(MediaFile("rphjb_SonoUltimo.mp3")),
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
      List(MediaFile("rphjb_Problema.mp3"))
    ), // also in special list
    ReplyBundleMessage(
      TextTrigger(
        List(
          RegexTextTriggerValue("\\bcontinua\\b".r)
        )
      ),
      List(
        MediaFile("rphjb_Continua.mp3")
      )
    ),
    ReplyBundleMessage(
      TextTrigger(
        List(
          RegexTextTriggerValue("au[ ]?de".r),
          RegexTextTriggerValue("\\btime\\b".r),
          RegexTextTriggerValue("uir[ ]?bi[ ]?taim".r)
        )
      ),
      List(MediaFile("rphjb_AuDeUirbiTaim.mp3"))
    ),
    ReplyBundleMessage(
      TextTrigger(
        List(
          StringTextTriggerValue("sono finito"),
          StringTextTriggerValue("ultimo stadio"),
          StringTextTriggerValue("stanco")
        )
      ),
      List(MediaFile("rphjb_Stanco.mp3"))
    ),
    ReplyBundleMessage(
      TextTrigger(
        List(StringTextTriggerValue("negozio"), StringTextTriggerValue("pantaloni"), StringTextTriggerValue("shopping"))
      ),
      List(MediaFile("rphjb_Pantaloni.mp3"))
    ),
    ReplyBundleMessage(
      TextTrigger(List(StringTextTriggerValue("brutto frocio"))),
      List(MediaFile("rphjb_BruttoFrocio.mp3"))
    ),
    ReplyBundleMessage(
      TextTrigger(
        List(
          StringTextTriggerValue("questo ragazzo"),
          StringTextTriggerValue("eccitare"),
          StringTextTriggerValue("lucio dalla")
        )
      ),
      List(MediaFile("rphjb_LucioDalla.mp3"))
    ),
    ReplyBundleMessage(
      TextTrigger(
        List(StringTextTriggerValue("chiesa"), StringTextTriggerValue("preghiera"), StringTextTriggerValue("non credo"))
      ),
      List(MediaFile("rphjb_Chiesa.mp3"))
    ),
    ReplyBundleMessage(TextTrigger(List(StringTextTriggerValue("maledetto"))), List(MediaFile("rphjb_Maledetto.mp3"))),
    ReplyBundleMessage(
      TextTrigger(
        List(
          StringTextTriggerValue("..magari"),
          StringTextTriggerValue("magari.."),
        )
      ),
      List(MediaFile("rphjb_Magari.mp3"))
    ),
    ReplyBundleMessage(TextTrigger(List(StringTextTriggerValue("io ti aiuto"))), List(MediaFile("rphjb_Aiuto.mp3"))),
    ReplyBundleMessage(
      TextTrigger(List(StringTextTriggerValue("faccio schifo"))),
      List(MediaFile("rphjb_FaccioSchifo.mp3"))
    ),
    ReplyBundleMessage(
      TextTrigger(List(RegexTextTriggerValue("ci sei ritornat[ao]".r))),
      List(MediaFile("rphjb_Ritornata.mp3"))
    ),
    ReplyBundleMessage(
      TextTrigger(List(StringTextTriggerValue("anche la merda"), StringTextTriggerValue("senza culo"))),
      List(MediaFile("rphjb_Merda.mp3"))
    ),
    ReplyBundleMessage(
      TextTrigger(List(StringTextTriggerValue("che succede"))),
      List(MediaFile("rphjb_CheSuccede.mp3"))
    ),
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
      List(MediaFile("rphjb_FigureMitologiche.mp3"))
    ),
    ReplyBundleMessage(
      TextTrigger(List(RegexTextTriggerValue("[a]+[t]{2,}[e]+[n]+[z]+[i]+[o]+[n]+[e]+[!]*[!e]$".r))),
      List(MediaFile("rphjb_Attenzione.mp3"))
    ),
    ReplyBundleMessage(
      TextTrigger(List(StringTextTriggerValue("poveri cretini"), StringTextTriggerValue("poveri ignoranti"))),
      List(MediaFile("rphjb_PoveriCretini.mp3"))
    ),
    ReplyBundleMessage(TextTrigger(List(StringTextTriggerValue("due ossa"))), List(MediaFile("rphjb_DueOssa.mp3"))),
    ReplyBundleMessage(
      TextTrigger(
        List(
          RegexTextTriggerValue("m[ie] fai( proprio)? schifo".r)
        )
      ),
      List(MediaFile("rphjb_Schifo.mp3"))
    ),
    ReplyBundleMessage(
      TextTrigger(List(StringTextTriggerValue("pappalardo"))),
      List(MediaFile("rphjb_Pappalardo.mp3"))
    ),
    ReplyBundleMessage(TextTrigger(List(StringTextTriggerValue("è un ordine"))), List(MediaFile("rphjb_Ordine.mp3"))),
    ReplyBundleMessage(TextTrigger(List(StringTextTriggerValue("una sera"))), List(MediaFile("rphjb_Sera.mp3"))),
    ReplyBundleMessage(TextTrigger(List(StringTextTriggerValue("il venerdì"))), List(MediaFile("rphjb_Venerdi.mp3"))),
    ReplyBundleMessage(TextTrigger(List(RegexTextTriggerValue("oppur[ae]".r))), List(MediaFile("rphjb_Oppura.mp3")))
  )

  val messageRepliesGifData: List[ReplyBundleMessage] = List(
    ReplyBundleMessage(
      TextTrigger(
        List(
          RegexTextTriggerValue("(af+)?fanculo(,)? per contesia".r)
        )
      ),
      List(MediaFile("rphjb_FanculoPerCortesia.gif"))
    ),
    ReplyBundleMessage(
      TextTrigger(
        List(
          StringTextTriggerValue("gli autori")
        )
      ),
      List(MediaFile("rphjb_Autori.gif"))
    ),
    ReplyBundleMessage(
      TextTrigger(
        List(
          StringTextTriggerValue("questo è matto")
        )
      ),
      List(MediaFile("rphjb_MattoRagazzi.gif"))
    ),
    ReplyBundleMessage(
      TextTrigger(List(StringTextTriggerValue("ultimi"))),
      List(
        MediaFile("rphjb_ViCalpesto.gif"),
        MediaFile("rphjb_Ultimi.gif"),
        MediaFile("rphjb_StateZittiZozziUltimi.mp3"),
      ),
      replySelection = RandomSelection
    ),
    ReplyBundleMessage(TextTrigger(List(StringTextTriggerValue("scivola"))), List(MediaFile("rphjb_SiScivola.mp3"))),
    ReplyBundleMessage(
      TextTrigger(
        List(
          StringTextTriggerValue("spalle"),
          StringTextTriggerValue("braccia"),
          RegexTextTriggerValue("t[ie] strozzo".r)
        )
      ),
      List(MediaFile("rphjb_FaccioVedereSpalleBraccia.gif"))
    ),
    ReplyBundleMessage(
      TextTrigger(
        List(
          StringTextTriggerValue("graffi")
        )
      ),
      List(MediaFile("rphjb_Graffi.gif"))
    ),
    ReplyBundleMessage(
      TextTrigger(
        List(
          StringTextTriggerValue("mi piaccio"),
          StringTextTriggerValue("impazzire"),
        )
      ),
      List(MediaFile("rphjb_MiPiaccio.gif"))
    ),
    ReplyBundleMessage(
      TextTrigger(
        List(
          StringTextTriggerValue("pelle d'oca"),
          StringTextTriggerValue("sussult"),
          StringTextTriggerValue("brivid")
        )
      ),
      List(MediaFile("rphjb_Brivido.gif"))
    ),
    ReplyBundleMessage(
      TextTrigger(
        List(
          StringTextTriggerValue("che siamo noi"),
          StringTextTriggerValue("pezzi di merda"),
        )
      ),
      List(MediaFile("rphjb_PezziDiMerda.gif"))
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
        MediaFile("rphjb_Tuffo.gif"),
        MediaFile("rphjb_Urlo.gif"),
        MediaFile("rphjb_Urlo3.gif"),
        MediaFile("rphjb_Urlo2.gif"),
        MediaFile("rphjb_UrloCanaro.gif")
      ),
      replySelection = RandomSelection
    ),
    ReplyBundleMessage(
      TextTrigger(
        List(
          StringTextTriggerValue("rispondere")
        )
      ),
      List(MediaFile("rphjb_Rispondere.gif"))
    ),
    ReplyBundleMessage(
      TextTrigger(
        List(
          StringTextTriggerValue("cuore in mano"),
          StringTextTriggerValue("mano nella mano"),
          StringTextTriggerValue("pelle contro la pelle")
        )
      ),
      List(MediaFile("rphjb_CuoreInMano.gif"))
    ),
    ReplyBundleMessage(
      TextTrigger(
        List(
          StringTextTriggerValue("stato brado")
        )
      ),
      List(MediaFile("rphjb_StatoBrado.gif"))
    ),
    ReplyBundleMessage(
      TextTrigger(
        List(
          StringTextTriggerValue("pronto dimmi")
        )
      ),
      List(MediaFile("rphjb_ProntoDimmi.gif"))
    ),
    ReplyBundleMessage(
      TextTrigger(
        List(
          RegexTextTriggerValue(" port[a]+( |$)".r)
        )
      ),
      List(MediaFile("rphjb_Porta.gif"))
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
      List(MediaFile("rphjb_PrendoIlNecessario.gif"))
    ),
    ReplyBundleMessage(
      TextTrigger(
        List(
          StringTextTriggerValue("nella gola")
        )
      ),
      List(MediaFile("rphjb_NellaGola.gif"))
    ),
    ReplyBundleMessage(
      TextTrigger(
        List(
          StringTextTriggerValue("siamo qua")
        )
      ),
      List(MediaFile("rphjb_SiamoQua.gif"))
    ),
    ReplyBundleMessage(
      TextTrigger(
        List(
          StringTextTriggerValue("cucciolo")
        )
      ),
      List(
        MediaFile("rphjb_Cucciolo.gif"),
        MediaFile("rphjb_Cucciolo2.gif"),
      ),
      replySelection = RandomSelection
    ),
    ReplyBundleMessage(
      TextTrigger(
        List(
          StringTextTriggerValue("Che c'hai")
        )
      ),
      List(MediaFile("rphjb_CheCHai.gif"))
    ),
    ReplyBundleMessage(
      TextTrigger(
        List(
          StringTextTriggerValue("stringere i denti"),
          StringTextTriggerValue("andare avanti"),
        )
      ),
      List(
        MediaFile("rphjb_AndareAvanti.gif"),
        MediaFile("rphjb_AndareAvanti.mp3")
      ),
    ),
    ReplyBundleMessage(
      TextTrigger(
        List(
          StringTextTriggerValue("06"),
          StringTextTriggerValue("prefisso")
        )
      ),
      List(MediaFile("rphjb_06.gif"))
    ),
    ReplyBundleMessage(
      TextTrigger(
        List(
          StringTextTriggerValue("abbiamo vinto")
        )
      ),
      List(MediaFile("rphjb_AbbiamoVinto.gif"))
    ),
    ReplyBundleMessage(
      TextTrigger(
        List(
          StringTextTriggerValue("telefonata pilotata"),
          StringTextTriggerValue("falsità")
        )
      ),
      List(MediaFile("rphjb_TelefonataPilotata.gif"))
    ),
    ReplyBundleMessage(
      TextTrigger(
        List(
          StringTextTriggerValue("ed è vero")
        )
      ),
      List(MediaFile("rphjb_Vero.gif"))
    ),
    ReplyBundleMessage(
      TextTrigger(
        List(
          StringTextTriggerValue("quante ore"),
          StringTextTriggerValue("quanto tempo")
        )
      ),
      List(MediaFile("rphjb_QuanteOre.gif"))
    ),
    ReplyBundleMessage(
      TextTrigger(
        List(
          StringTextTriggerValue("guerra")
        )
      ),
      List(MediaFile("rphjb_GuerraTotale.gif"))
    ),
    ReplyBundleMessage(
      TextTrigger(
        List(
          RegexTextTriggerValue("entrat[oa]".r)
        )
      ),
      List(MediaFile("rphjb_ComeHaFattoAEntrare.gif"))
    ),
    ReplyBundleMessage(
      TextTrigger(List(StringTextTriggerValue("donna cane"))),
      List(MediaFile("rphjb_DonnaCane.gif"))
    ),
    ReplyBundleMessage(
      TextTrigger(List(StringTextTriggerValue("menzion"))),
      List(MediaFile("rphjb_NonMiMenzionareQuestaParola.gif"))
    ),
    ReplyBundleMessage(TextTrigger(List(StringTextTriggerValue("hollywood"))), List(MediaFile("rphjb_Hollywood.gif"))),
    ReplyBundleMessage(
      TextTrigger(
        List(
          StringTextTriggerValue("piano superiore"),
          StringTextTriggerValue("compete"),
          StringTextTriggerValue("gerarca")
        )
      ),
      List(MediaFile("rphjb_PianoSuperioreCompete.gif"))
    ),
    ReplyBundleMessage(
      TextTrigger(List(StringTextTriggerValue("chi è"))),
      List(MediaFile("rphjb_QuestaPersonaScusate.gif"))
    ),
    ReplyBundleMessage(
      TextTrigger(List(StringTextTriggerValue("calcolo"), StringTextTriggerValue("matematica"))),
      List(MediaFile("rphjb_MiPareLogico.gif"))
    ),
    ReplyBundleMessage(TextTrigger(List(StringTextTriggerValue("tucul"))), List(MediaFile("rphjb_TuCul.gif"))),
    ReplyBundleMessage(
      TextTrigger(
        List(
          StringTextTriggerValue(";)"),
          StringTextTriggerValue("occhiolino"),
          StringTextTriggerValue("wink"),
          StringTextTriggerValue(e":wink:")
        )
      ),
      List(MediaFile("rphjb_Occhiolino.gif"))
    ),
    ReplyBundleMessage(TextTrigger(List(StringTextTriggerValue("soffro"))), List(MediaFile("rphjb_Soffro.gif"))),
    ReplyBundleMessage(
      TextTrigger(
        List(
          StringTextTriggerValue("indispettirmi"),
          StringTextTriggerValue("oltrepassare"),
          StringTextTriggerValue("divento cattivo")
        )
      ),
      List(MediaFile("rphjb_Indispettirmi.gif"))
    ),
    ReplyBundleMessage(
      TextTrigger(List(StringTextTriggerValue("mannaggia"), StringTextTriggerValue("la salute"))),
      List(MediaFile("rphjb_MannaggiaLaSalute.gif"))
    ),
    ReplyBundleMessage(
      TextTrigger(
        List(
          StringTextTriggerValue("mi rompi il cazzo"),
          StringTextTriggerValue("mi dai fastidio")
        )
      ),
      List(MediaFile("rphjb_MiRompiErCazzo.gif"))
    ),
    ReplyBundleMessage(
      TextTrigger(
        List(
          StringTextTriggerValue("dare fastidio")
        )
      ),
      List(MediaFile("rphjb_DareFastidio.gif"))
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
      List(MediaFile("rphjb_Bebop.gif"))
    ),
    ReplyBundleMessage(
      TextTrigger(List(StringTextTriggerValue("succh"), StringTextTriggerValue("olio di croce"))),
      List(MediaFile("rphjb_OlioDiCroce.gif"))
    ),
    ReplyBundleMessage(
      TextTrigger(List(StringTextTriggerValue("c'ha [pure ]?ragione"), StringTextTriggerValue("o no?"))),
      List(MediaFile("rphjb_Ragione.gif"))
    ),
    ReplyBundleMessage(TextTrigger(List(StringTextTriggerValue("micetta"))), List(MediaFile("rphjb_Micetta.gif"))),
    ReplyBundleMessage(
      TextTrigger(List(StringTextTriggerValue("generi musicali"), RegexTextTriggerValue("solo il me(t|d)al".r))),
      List(MediaFile("rphjb_GeneriMusicali.gif"))
    ),
    ReplyBundleMessage(
      TextTrigger(List(StringTextTriggerValue("ti distruggo"))),
      List(MediaFile("rphjb_TiDistruggo.gif"))
    ),
    ReplyBundleMessage(
      TextTrigger(List(StringTextTriggerValue("bassista"), StringTextTriggerValue("slap"))),
      List(MediaFile("rphjb_Bassista.gif"))
    ),
    ReplyBundleMessage(
      TextTrigger(List(StringTextTriggerValue("discoteca laziale"))),
      List(MediaFile("rphjb_DiscotecaLaziale.gif"))
    ),
    ReplyBundleMessage(
      TextTrigger(List(StringTextTriggerValue("piloti d'aereo"), StringTextTriggerValue("disastri aerei"))),
      List(MediaFile("rphjb_DrogatiPiloti.gif"))
    ),
    ReplyBundleMessage(
      TextTrigger(
        List(
          StringTextTriggerValue("timore"),
          StringTextTriggerValue("paura"),
          RegexTextTriggerValue("diri[g]+en[dt]i".r),
        )
      ),
      List(MediaFile("rphjb_Dirigenti.gif"))
    ),
    ReplyBundleMessage(
      TextTrigger(List(StringTextTriggerValue("cosa è successo"))),
      List(MediaFile("rphjb_CosaSuccesso.gif"))
    ),
    ReplyBundleMessage(
      TextTrigger(List(StringTextTriggerValue("perchè mi guardi"))),
      List(MediaFile("rphjb_Guardi.gif"))
    ),
    ReplyBundleMessage(
      TextTrigger(List(StringTextTriggerValue("non è roba per me"))),
      List(MediaFile("rphjb_RobaPerMe.gif"))
    ),
    ReplyBundleMessage(
      TextTrigger(
        List(RegexTextTriggerValue("[cg]hi[td]a[r]+is[td]a [bp]referi[dt]o".r))
      ),
      List(MediaFile("rphjb_Chitarrista.gif"))
    ),
    ReplyBundleMessage(
      TextTrigger(List(StringTextTriggerValue("non sai molto"))),
      List(MediaFile("rphjb_NonSaiMolto.gif"))
    ),
    ReplyBundleMessage(TextTrigger(List(StringTextTriggerValue("divert"))), List(MediaFile("rphjb_Diverti.gif"))),
    ReplyBundleMessage(TextTrigger(List(StringTextTriggerValue("e parla"))), List(MediaFile("rphjb_Parla.gif"))),
    ReplyBundleMessage(TextTrigger(List(StringTextTriggerValue("uno scherzo"))), List(MediaFile("rphjb_Scherzo.gif"))),
    ReplyBundleMessage(
      TextTrigger(
        List(
          StringTextTriggerValue("che si deve fà"),
          StringTextTriggerValue("campà"),
        )
      ),
      List(MediaFile("rphjb_Campa.gif"))
    ),
    ReplyBundleMessage(
      TextTrigger(
        List(
          StringTextTriggerValue("vi calpesto"),
          StringTextTriggerValue("vermi"),
          StringTextTriggerValue("strisciate")
        )
      ),
      List(MediaFile("rphjb_ViCalpesto.gif"))
    ),
    ReplyBundleMessage(
      TextTrigger(
        List(
          StringTextTriggerValue("rimpinzati"),
          RegexTextTriggerValue("[gc]io[gc]+ola[dt]a".r),
          StringTextTriggerValue("pandori"),
        )
      ),
      List(MediaFile("rphjb_Rimpinzati.gif"))
    ),
    ReplyBundleMessage(
      TextTrigger(
        List(
          StringTextTriggerValue("pensa alla deficienza"),
          RegexTextTriggerValue("ma si può dire una cosa (del genere|così)".r),
        )
      ),
      List(MediaFile("rphjb_Deficienza.gif"))
    ),
    ReplyBundleMessage(
      TextTrigger(
        List(
          StringTextTriggerValue("non sapere"),
          RegexTextTriggerValue("aris[dt]o[dt]ele".r)
        )
      ),
      List(MediaFile("rphjb_SoDiNonSapere.gif"))
    ),
    ReplyBundleMessage(
      TextTrigger(
        List(
          RegexTextTriggerValue("lo[g]+i[cg]o".r)
        )
      ),
      List(MediaFile("rphjb_MiPareLogico.gif"), MediaFile("rphjb_SembraLogico.gif")),
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
        MediaFile("rphjb_NonMeNeFotte.gif"),
        MediaFile("rphjb_NonMeNeFrega.gif"),
        MediaFile("rphjb_ENonMeNeFotteUnCazzo.mp3"),
        MediaFile("rphjb_NonLeggoQuelloCheScrivete.mp3")
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
      List(MediaFile("rphjb_Escerto.gif"))
    ),
    ReplyBundleMessage(
      TextTrigger(
        List(RegexTextTriggerValue("levati[/. ]*dai coglioni".r), RegexTextTriggerValue("fuori[/. ]*dai coglioni".r))
      ),
      List(MediaFile("rphjb_LevatiDaiCoglioni.gif"))
    ),
    ReplyBundleMessage(
      TextTrigger(
        List(
          RegexTextTriggerValue("(non|mica) so(no)? (un |n )?co(gl|j)ione".r),
          RegexTextTriggerValue("sarete co(gl|j)ioni voi".r)
        )
      ),
      List(MediaFile("rphjb_SareteCoglioniVoi.gif"))
    ),
    ReplyBundleMessage(
      TextTrigger(
        List(
          RegexTextTriggerValue("più co(gl|j)ione".r)
        )
      ),
      List(MediaFile("rphjb_PiuCoglione.gif"))
    ),
    ReplyBundleMessage(
      TextTrigger(
        List(
          RegexTextTriggerValue("dice co(gl|j)ione".r)
        )
      ),
      List(MediaFile("rphjb_Coglione.gif"))
    ),
    ReplyBundleMessage(
      TextTrigger(List(StringTextTriggerValue("bravo!!!"), StringTextTriggerValue("bravooo"))),
      List(MediaFile("rphjb_Bravo.gif"))
    ),
    ReplyBundleMessage(
      TextTrigger(List(StringTextTriggerValue("capolavoro"))),
      List(MediaFile("rphjb_Capolavoro.gif"))
    ),
    ReplyBundleMessage(TextTrigger(List(StringTextTriggerValue(" metal"))), List(MediaFile("rphjb_Metal.gif"))),
    ReplyBundleMessage(
      TextTrigger(List(StringTextTriggerValue("chiama la polizia"))),
      List(MediaFile("rphjb_ChiamaLaPolizia.gif"))
    ),
    ReplyBundleMessage(
      TextTrigger(List(StringTextTriggerValue("allucinante"))),
      List(MediaFile("rphjb_Allucinante.gif"))
    ),
    ReplyBundleMessage(
      TextTrigger(List(StringTextTriggerValue("mare di cazzate"), StringTextTriggerValue("non è possibile"))),
      List(MediaFile("rphjb_NonPossibile.gif"))
    ),
    ReplyBundleMessage(
      TextTrigger(
        List(
          StringTextTriggerValue("porca miseria"),
          StringTextTriggerValue("incazzare")
        )
      ),
      List(MediaFile("rphjb_PorcaMiseria.gif"))
    ),
    ReplyBundleMessage(
      TextTrigger(List(RegexTextTriggerValue("schifos(o|!)+".r))),
      List(MediaFile("rphjb_Schifoso.gif"))
    ),
    ReplyBundleMessage(
      TextTrigger(List(StringTextTriggerValue("facendo soffrire"))),
      List(MediaFile("rphjb_FacendoSoffrire.gif"))
    ),
    ReplyBundleMessage(
      TextTrigger(List(StringTextTriggerValue("dovete soffrire"))),
      List(MediaFile("rphjb_DoveteSoffrire.gif"))
    ),
    ReplyBundleMessage(
      TextTrigger(List(StringTextTriggerValue("sarete puniti"))),
      List(MediaFile("rphjb_SaretePuniti.gif"))
    ),
    ReplyBundleMessage(
      TextTrigger(
        List(
          StringTextTriggerValue("cantanti"),
          StringTextTriggerValue("serie z")
        )
      ),
      List(MediaFile("rphjb_CantantiSerieZ.gif"))
    ),
    ReplyBundleMessage(
      TextTrigger(List(StringTextTriggerValue("sentendo male"))),
      List(MediaFile("rphjb_MiStoSentendoMale.gif"))
    ),
    ReplyBundleMessage(
      TextTrigger(List(StringTextTriggerValue("stare male"))),
      List(MediaFile("rphjb_MiFaStareMale.gif"))
    ),
    ReplyBundleMessage(
      TextTrigger(List(StringTextTriggerValue("lunghezza d'onda"), StringTextTriggerValue("brave persone"))),
      List(MediaFile("rphjb_LunghezzaDOnda.gif"))
    ),
    ReplyBundleMessage(TextTrigger(List(StringTextTriggerValue("delirio"))), List(MediaFile("rphjb_Delirio.gif"))),
    ReplyBundleMessage(TextTrigger(List(StringTextTriggerValue("paradosso"))), List(MediaFile("rphjb_Paradosso.gif"))),
    ReplyBundleMessage(
      TextTrigger(List(StringTextTriggerValue("querelare"), StringTextTriggerValue("guerelare"))),
      List(MediaFile("rphjb_Querelare.gif"))
    ),
    ReplyBundleMessage(
      TextTrigger(List(StringTextTriggerValue("cantate"), StringTextTriggerValue("arigliano"))),
      List(MediaFile("rphjb_Arigliano.gif"))
    ),
    ReplyBundleMessage(
      TextTrigger(List(StringTextTriggerValue("non voglio nessuno"))),
      List(MediaFile("rphjb_NonVoglioNessuno.gif"))
    ),
    ReplyBundleMessage(
      TextTrigger(List(StringTextTriggerValue("andati al cesso"), StringTextTriggerValue("diecimila volte"))),
      List(MediaFile("rphjb_Alcesso.gif"))
    ),
    ReplyBundleMessage(
      TextTrigger(List(StringTextTriggerValue("non manca niente"), StringTextTriggerValue("c'è tutto"))),
      List(MediaFile("rphjb_NonMancaNiente.gif"))
    ),
    ReplyBundleMessage(
      TextTrigger(List(StringTextTriggerValue("in fila"))),
      List(MediaFile("rphjb_MettitiInFila.gif"))
    ),
    ReplyBundleMessage(TextTrigger(List(StringTextTriggerValue("non male"))), List(MediaFile("rphjb_NonMale.gif"))),
    ReplyBundleMessage(TextTrigger(List(StringTextTriggerValue("si sente"))), List(MediaFile("rphjb_SiSente.gif"))),
    ReplyBundleMessage(
      TextTrigger(
        List(
          StringTextTriggerValue("colpa vostra"),
          StringTextTriggerValue("pazzo"),
          RegexTextTriggerValue("(divento|diventare|sono) matto".r)
        )
      ),
      List(MediaFile("rphjb_StoDiventandoPazzo.gif"))
    ),
    ReplyBundleMessage(
      TextTrigger(
        List(
          StringTextTriggerValue("sorca"),
          StringTextTriggerValue("lecciso"),
          RegexTextTriggerValue("\\bfiga\\b".r)
        )
      ),
      List(MediaFile("rphjb_SorcaLecciso.gif"))
    ),
    ReplyBundleMessage(
      TextTrigger(
        List(
          StringTextTriggerValue("non li sopporto"),
          RegexTextTriggerValue("che si deve f(à|are)".r),
          StringTextTriggerValue("bisogna pure lavorà")
        )
      ),
      List(MediaFile("rphjb_NonLiSopporto.gif"))
    ),
    ReplyBundleMessage(
      TextTrigger(List(StringTextTriggerValue("chi cazzo sei"))),
      List(MediaFile("rphjb_ChiCazzoSei.gif"))
    ),
    ReplyBundleMessage(TextTrigger(List(StringTextTriggerValue("feste"))), List(MediaFile("rphjb_Feste.gif"))),
    ReplyBundleMessage(
      TextTrigger(List(StringTextTriggerValue("si ostina"), StringTextTriggerValue("foto vecchie"))),
      List(MediaFile("rphjb_Ostina.gif"))
    ),
    ReplyBundleMessage(
      TextTrigger(
        List(
          RegexTextTriggerValue("(è|diventa) vecchi[ao]".r),
        )
      ),
      List(MediaFile("rphjb_Vecchio.gif"))
    ),
    ReplyBundleMessage(
      TextTrigger(List(StringTextTriggerValue("scatta qualcosa"))),
      List(MediaFile("rphjb_ScattaQualcosa.gif"))
    ),
    ReplyBundleMessage(
      TextTrigger(List(RegexTextTriggerValue("sput[ao]".r))),
      List(MediaFile("rphjb_Sputo.gif"))
    ),
    ReplyBundleMessage(
      TextTrigger(List(StringTextTriggerValue("lasciami in pace"), RegexTextTriggerValue("\\bstronza\\b".r))),
      List(MediaFile("rphjb_LasciamiInPace.gif"))
    ),
    ReplyBundleMessage(TextTrigger(List(StringTextTriggerValue("pure bona"))), List(MediaFile("rphjb_Bona.gif"))),
    ReplyBundleMessage(
      TextTrigger(
        List(StringTextTriggerValue(" danza"), StringTextTriggerValue("macabra"), StringTextTriggerValue(" ball"))
      ),
      List(MediaFile("rphjb_DanzaMacabra.gif"))
    ),
    ReplyBundleMessage(
      TextTrigger(List(RegexTextTriggerValue("sei [gc]ambiat[oa]".r))),
      List(MediaFile("rphjb_SeiCambiata.gif"))
    ),
    ReplyBundleMessage(
      TextTrigger(List(StringTextTriggerValue("mio discapito"), StringTextTriggerValue("disgabido"))),
      List(MediaFile("rphjb_Discapito.gif"))
    ),
    ReplyBundleMessage(
      TextTrigger(List(StringTextTriggerValue("peggio del peggio"))),
      List(MediaFile("rphjb_PeggioDelPeggio.gif"))
    ),
    ReplyBundleMessage(
      TextTrigger(List(StringTextTriggerValue("squallida"), StringTextTriggerValue("abbia mai sentito"))),
      List(MediaFile("rphjb_Squallida.gif"))
    ),
    ReplyBundleMessage(TextTrigger(List(StringTextTriggerValue("la verità"))), List(MediaFile("rphjb_Verita.gif"))),
    ReplyBundleMessage(
      TextTrigger(List(StringTextTriggerValue("ti dovresti vergognare"))),
      List(MediaFile("rphjb_TiDovrestiVergognare.gif"))
    ),
    ReplyBundleMessage(
      TextTrigger(List(RegexTextTriggerValue("nooo[o]*".r))),
      List(MediaFile("rphjb_No.gif"))
    ),
    ReplyBundleMessage(
      TextTrigger(List(StringTextTriggerValue("destino"), StringTextTriggerValue("incontrare"))),
      List(MediaFile("rphjb_Destino.gif"))
    ),
    ReplyBundleMessage(
      TextTrigger(List(StringTextTriggerValue("meridionale"), StringTextTriggerValue("terron"))),
      List(MediaFile("rphjb_Meridionale.gif"))
    ),
    ReplyBundleMessage(
      TextTrigger(
        List(StringTextTriggerValue("baci"), StringTextTriggerValue("limonare"), StringTextTriggerValue("peggio cose"))
      ),
      List(MediaFile("rphjb_Bacio.gif"))
    ),
    ReplyBundleMessage(
      TextTrigger(
        List(
          StringTextTriggerValue("solo uno parló"),
          RegexTextTriggerValue("[cg]ri[dt]i[gc]a[dt]o".r)
        )
      ),
      List(MediaFile("rphjb_FuCriticato.gif"))
    ),
    ReplyBundleMessage(
      TextTrigger(List(StringTextTriggerValue("giudica"))),
      List(MediaFile("rphjb_Giudicate.gif"), MediaFile("rphjb_ComeFaiAGiudicare.gif")),
      replySelection = RandomSelection
    ),
    ReplyBundleMessage(
      TextTrigger(List(StringTextTriggerValue("bastone infernale"), StringTextTriggerValue("un'arma"))),
      List(
        MediaFile("rphjb_Bastone1.gif"),
        MediaFile("rphjb_Bastone2.gif"),
        MediaFile("rphjb_Bastone3.gif")
      ),
      replySelection = RandomSelection
    ),
    ReplyBundleMessage(
      TextTrigger(List(StringTextTriggerValue("drogati"))),
      List(
        MediaFile("rphjb_DrogatiRockettari1.gif"),
        MediaFile("rphjb_DrogatiRockettari2.gif"),
        MediaFile("rphjb_DrogatiPiloti.gif")
      ),
      replySelection = RandomSelection
    ),
    ReplyBundleMessage(
      TextTrigger(
        List(
          RegexTextTriggerValue("ro[ckgh]+(ch|gh|k)e(d+|t+)ari".r),
          StringTextTriggerValue("stillati")
        )
      ),
      List(MediaFile("rphjb_DrogatiRockettari1.gif"), MediaFile("rphjb_DrogatiRockettari2.gif")),
      replySelection = RandomSelection
    ),
    ReplyBundleMessage(
      TextTrigger(List(StringTextTriggerValue("sguardo"))),
      List(
        MediaFile("rphjb_Sguardo.gif"),
        MediaFile("rphjb_Sguardo2.gif"),
        MediaFile("rphjb_Sguardo3.gif"),
        MediaFile("rphjb_Sguardo4.gif"),
        MediaFile("rphjb_SguardoCanaro.gif")
      ),
      replySelection = RandomSelection
    ),
    ReplyBundleMessage(
      TextTrigger(List(StringTextTriggerValue("a quel punto"))),
      List(MediaFile("rphjb_QuelPunto.gif"))
    ),
    ReplyBundleMessage(TextTrigger(List(StringTextTriggerValue("errori"))), List(MediaFile("rphjb_MaiErrori.gif"))),
    ReplyBundleMessage(
      TextTrigger(
        List(
          StringTextTriggerValue("quattro"),
          StringTextTriggerValue("faccio in tempo")
        )
      ),
      List(MediaFile("rphjb_QuattroSolo.gif"))
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
      List(MediaFile("rphjb_FaccioLaParte.gif"))
    ),
    ReplyBundleMessage(
      TextTrigger(List(StringTextTriggerValue("assolutamente no"), StringTextTriggerValue("non mi lamento"))),
      List(MediaFile("rphjb_NonMiLamento.gif"))
    ),
    ReplyBundleMessage(
      TextTrigger(List(StringTextTriggerValue("inizio della fine"))),
      List(MediaFile("rphjb_InizioDellaFine.gif"))
    ),
    ReplyBundleMessage(
      TextTrigger(List(StringTextTriggerValue("il senso"))),
      List(MediaFile("rphjb_IlSensoCapito.gif"))
    ),
    ReplyBundleMessage(
      TextTrigger(List(RegexTextTriggerValue("\\bester\\b".r), StringTextTriggerValue("esposito"))),
      List(
        MediaFile("rphjb_Ester.gif"),
        MediaFile("rphjb_Ester2.gif"),
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
      List(MediaFile("rphjb_Propriolla.gif"))
    )
  )

  val messageRepliesVideoData: List[ReplyBundleMessage] = List(
    ReplyBundleMessage(
      TextTrigger(
        List(
          RegexTextTriggerValue("(roba|droga) tagliata male".r),
          StringTextTriggerValue("one television"),
          RegexTextTriggerValue("devo fare (un po'|un attimo) (di|de) esercitazione".r)
        ),
      ),
      List(
        MediaFile("rphjb_RockMachineIntro.mp4")
      )
    ),
    ReplyBundleMessage(
      TextTrigger(
        List(
          StringTextTriggerValue("poesia"),
          StringTextTriggerValue("madre")
        ),
      ),
      List(
        MediaFile("rphjb_PoesiaMadre.mp4")
      )
    )
  )

  val messageRepliesSpecialData: List[ReplyBundleMessage] = List(
    ReplyBundleMessage(
      TextTrigger(
        List(
          RegexTextTriggerValue("che (cazzo )?era quella roba".r),
          RegexTextTriggerValue("che (cazzo |cazzo di roba )?mi avete dato".r),
          StringTextTriggerValue("lampi negli occhi"),
          RegexTextTriggerValue("gira(re|ra|rà|ndo)? la testa".r),
          RegexTextTriggerValue("insieme alla (c|g)o(c|g)a (c|g)ola".r)
        )
      ),
      List(
        MediaFile("rphjb_CheCazzoEraQuellaRoba.mp3"),
        MediaFile("rphjb_RockMachineIntro.mp4"),
      ),
      replySelection = RandomSelection
    ),
    ReplyBundleMessage(
      TextTrigger(
        List(
          StringTextTriggerValue("canzonette"),
          StringTextTriggerValue("balera"),
          StringTextTriggerValue("sagra dell'uva"),
          StringTextTriggerValue("feste condominiali"),
          StringTextTriggerValue("feste di piazza")
        )
      ),
      List(
        MediaFile("rphjb_Canzonette.mp3"),
        MediaFile("rphjb_Canzonette.gif")
      )
    ),
    ReplyBundleMessage(
      TextTrigger(List(StringTextTriggerValue("un pollo"))),
      List(
        MediaFile("rphjb_Pollo.mp3"),
        MediaFile("rphjb_Pollo.gif")
      )
    ),
    ReplyBundleMessage(
      TextTrigger(
        List(
          StringTextTriggerValue("quello che dico io")
        )
      ),
      List(
        MediaFile("rphjb_QuelloCheDicoIo.gif"),
        MediaFile("rphjb_FannoQuelloCheDicoIo.mp3")
      )
    ),
    ReplyBundleMessage(
      TextTrigger(
        List(
          StringTextTriggerValue("zucchero")
        )
      ),
      List(
        MediaFile("rphjb_ChitarraZuggherada.mp3"),
        MediaFile("rphjb_Zucchero.mp3"),
        MediaFile("rphjb_ZuccheroVanigliaCremaCioccolataPandoro.mp3"),
      ),
      replySelection = RandomSelection
    ),
    ReplyBundleMessage(
      TextTrigger(List(StringTextTriggerValue("problema"))),
      List(
        MediaFile("rphjb_VabbeProblema.gif"),
        MediaFile("rphjb_Problema.mp3"),
      ),
      replySelection = RandomSelection
    ),
    ReplyBundleMessage(
      TextTrigger(
        List(StringTextTriggerValue("good"), StringTextTriggerValue("show"), StringTextTriggerValue("friends"))
      ),
      List(
        MediaFile("rphjb_OkGoodShowFriends.gif"),
        MediaFile("rphjb_OkGoodShowFriends2.gif"),
        MediaFile("rphjb_LetSGoodStateBene.mp3"),
        MediaFile("rphjb_WelaLetsGoodMyFriends.mp3")
      ),
      replySelection = RandomSelection
    ),
    ReplyBundleMessage(
      TextTrigger(
        List(
          RegexTextTriggerValue("vattene (af)?fanculo".r)
        )
      ),
      List(MediaFile("rphjb_MaVatteneAffanculo.gif"), MediaFile("rphjb_MaVatteneAffanculo.mp3"))
    ),
    ReplyBundleMessage(
      TextTrigger(List(StringTextTriggerValue("feelings"))),
      List(
        MediaFile("rphjb_Feelings.gif"),
        MediaFile("rphjb_Feelings2.gif"),
        MediaFile("rphjb_Feelings.mp3")
      ),
      replySelection = RandomSelection
    ),
    ReplyBundleMessage(
      TextTrigger(List(StringTextTriggerValue("me ne vado"))),
      List(
        MediaFile("rphjb_MeNeVado.mp3"),
        MediaFile("rphjb_MiRompiErCazzo.gif"),
        MediaFile("rphjb_MeNeVado.gif")
      ),
      replySelection = RandomSelection
    ),
    ReplyBundleMessage(
      TextTrigger(
        List(StringTextTriggerValue("mignotta"), StringTextTriggerValue("puttana"), StringTextTriggerValue("troia"))
      ),
      List(
        MediaFile("rphjb_Mignotta.mp3"),
        MediaFile("rphjb_Mignotta.gif"),
        MediaFile("rphjb_VialeZara.mp3")
      ),
      replySelection = RandomSelection
    ),
    ReplyBundleMessage(
      TextTrigger(List(StringTextTriggerValue("ti devi spaventare"))),
      List(MediaFile("rphjb_TiDeviSpaventare.mp3"), MediaFile("rphjb_TiDeviSpaventare.gif"))
    ),
    ReplyBundleMessage(
      TextTrigger(List(StringTextTriggerValue("ma che cazzo sto dicendo"), StringTextTriggerValue("il martell"))),
      List(MediaFile("rphjb_MaCheCazzoStoDicendo.mp3"), MediaFile("rphjb_MaCheCazzoStoDicendo.gif"))
    ),
    ReplyBundleMessage(
      TextTrigger(List(StringTextTriggerValue("questa volta no"))),
      List(MediaFile("rphjb_QuestaVoltaNo.mp3"), MediaFile("rphjb_QuestaVoltaNo.gif"))
    ),
    ReplyBundleMessage(
      TextTrigger(List(StringTextTriggerValue("una vergogna"))),
      List(
        MediaFile("rphjb_Vergogna.mp3"),
        MediaFile("rphjb_Vergogna.gif"),
        MediaFile("rphjb_Vergogna2.gif")
      ),
      replySelection = RandomSelection
    ),
    ReplyBundleMessage(
      TextTrigger(List(StringTextTriggerValue("mi devo trasformare"), StringTextTriggerValue("cristo canaro"))),
      List(MediaFile("rphjb_Trasformista.mp3"), MediaFile("rphjb_Trasformista.gif"))
    ),
    ReplyBundleMessage(
      TextTrigger(
        List(RegexTextTriggerValue("ma[ ]?s[cg]us[a]?".r))
      ),
      List(MediaFile("rphjb_MaSgus.mp3"), MediaFile("rphjb_MaSgus.gif"))
    ),
    ReplyBundleMessage(
      TextTrigger(
        List(StringTextTriggerValue("grazie gianni"), RegexTextTriggerValue("cia[o]{3,}".r))
      ),
      List(MediaFile("rphjb_Grazie.mp3"), MediaFile("rphjb_Grazie.gif"))
    ),
    ReplyBundleMessage(
      TextTrigger(List(StringTextTriggerValue("stare attenti"), StringTextTriggerValue("per strada"))),
      List(MediaFile("rphjb_IncontratePerStrada.mp3"), MediaFile("rphjb_IncontratePerStrada.gif"))
    ),
    ReplyBundleMessage(
      TextTrigger(
        List(
          StringTextTriggerValue("lavora tu vecchiaccia"),
          StringTextTriggerValue("hai la pelle dura"),
          StringTextTriggerValue("io sono creatura")
        )
      ),
      List(MediaFile("rphjb_Lavoratu.mp3"), MediaFile("rphjb_Lavoratu.gif"))
    ),
    ReplyBundleMessage(
      TextTrigger(List(RegexTextTriggerValue("infern[a]+l[ie]+[!]*".r))),
      List(MediaFile("rphjb_Infernali.mp3"), MediaFile("rphjb_Infernali.gif"), MediaFile("rphjb_Infernale.mp3")),
      replySelection = RandomSelection
    ),
    ReplyBundleMessage(
      TextTrigger(List(StringTextTriggerValue("per il culo"))),
      List(MediaFile("rphjb_PigliandoPerIlCulo.mp3"), MediaFile("rphjb_PigliandoPerIlCulo.gif"))
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
        MediaFile("rphjb_Risata.mp3"),
        MediaFile("rphjb_Risata.gif"),
        MediaFile("rphjb_Sorriso2.gif"),
        MediaFile("rphjb_Sorriso.gif"),
        MediaFile("rphjb_SorrisoSognante.gif"),
        MediaFile("rphjb_Risata2.mp3")
      ),
      replySelection = RandomSelection
    ),
    ReplyBundleMessage(
      TextTrigger(List(StringTextTriggerValue("ammazza che sei"), StringTextTriggerValue("quasi un frocio"))),
      List(MediaFile("rphjb_Frocio.mp3"), MediaFile("rphjb_Frocio.gif"))
    ),
    ReplyBundleMessage(
      TextTrigger(List(RegexTextTriggerValue("(fammi|chiedere)? (una|questa)? cortesia".r))),
      List(MediaFile("rphjb_FammiQuestaCortesia.mp3"), MediaFile("rphjb_FammiQuestaCortesia.gif"))
    ),
    ReplyBundleMessage(
      TextTrigger(List(StringTextTriggerValue("non mi sta bene"))),
      List(
        MediaFile("rphjb_NonMiStaBene.mp3"),
        MediaFile("rphjb_NonMiStaBene2.mp3"),
        MediaFile("rphjb_NonMiStaBene.gif"),
        MediaFile("rphjb_NonMiStaBene2.gif")
      ),
      replySelection = RandomSelection
    ),
    ReplyBundleMessage(
      TextTrigger(
        List(
          StringTextTriggerValue("le labbra"),
        )
      ),
      List(MediaFile("rphjb_Labbra.mp3"), MediaFile("rphjb_Labbra.gif"))
    ),
    ReplyBundleMessage(
      TextTrigger(List(StringTextTriggerValue("la vita è il nemico"))),
      List(MediaFile("rphjb_VitaNemico.mp3"), MediaFile("rphjb_VitaNemico.gif"))
    ),
    ReplyBundleMessage(
      TextTrigger(List(StringTextTriggerValue("permettere"))),
      List(MediaFile("rphjb_Permettere.mp3"), MediaFile("rphjb_Permettere.gif"))
    ),
    ReplyBundleMessage(
      TextTrigger(List(StringTextTriggerValue("le note"))),
      List(MediaFile("rphjb_Note.mp3"), MediaFile("rphjb_Note.gif"))
    ),
    ReplyBundleMessage(
      TextTrigger(List(RegexTextTriggerValue("te[r]+[i]+[b]+[i]+l[e]+".r))),
      List(MediaFile("rphjb_Terribile.mp3"), MediaFile("rphjb_Terribile.gif"))
    ),
    ReplyBundleMessage(
      TextTrigger(List(StringTextTriggerValue("viva napoli"))),
      List(MediaFile("rphjb_VivaNapoli.mp3"), MediaFile("rphjb_VivaNapoli.gif"))
    ),
    ReplyBundleMessage(
      TextTrigger(
        List(
          StringTextTriggerValue("ciao a tutti"),
          StringTextTriggerValue("come state"),
          StringTextTriggerValue("belle gioie")
        )
      ),
      List(MediaFile("rphjb_CiaoComeState.gif"), MediaFile("rphjb_CiaoComeState.mp3"))
    ),
    ReplyBundleMessage(
      TextTrigger(
        List(
          StringTextTriggerValue("state bene")
        )
      ),
      List(MediaFile("rphjb_LetSGoodStateBene.mp3"))
    ),
    ReplyBundleMessage(
      TextTrigger(List(RegexTextTriggerValue("bast[a]{3,}[!]*".r))),
      List(
        MediaFile("rphjb_Basta.mp3"),
        MediaFile("rphjb_Basta2.mp3"),
        MediaFile("rphjb_Basta.gif"),
        MediaFile("rphjb_Basta2.gif"),
        MediaFile("rphjb_Basta3.gif"),
        MediaFile("rphjb_BastaSedia.gif")
      ),
      replySelection = RandomSelection
    ),
    ReplyBundleMessage(
      TextTrigger(
        List(StringTextTriggerValue("assolo"), RegexTextTriggerValue("[gc]hi[td]arra".r))
      ),
      List(
        MediaFile("rphjb_Assolo.mp3"),
        MediaFile("rphjb_Chitarra1.gif"),
        MediaFile("rphjb_Chitarra2.gif"),
        MediaFile("rphjb_ChitarraZuggherada.mp3")
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
        MediaFile("rphjb_HoCapito.mp3"),
        MediaFile("rphjb_AveteCapito.mp3"),
        MediaFile("rphjb_Capito.mp3"),
        MediaFile("rphjb_NonHannoCapitoUnCazzo.mp3"),
        MediaFile("rphjb_AveteCapitoComeSempre.gif"),
        MediaFile("rphjb_NonAveteCapitoUnCazzo.gif"),
        MediaFile("rphjb_VoiNonAveteCapitoUnCazzo.gif"),
        MediaFile("rphjb_IlSensoCapito.gif"),
        MediaFile("rphjb_CapitoDoveStiamo.gif"),
        MediaFile("rphjb_NonHoCapito.gif"),
        MediaFile("rphjb_AveteCapitoEh.gif"),
        MediaFile("rphjb_ComeAlSolitoNonAveteCapito.gif"),
        MediaFile("rphjb_CapitoDoveStiamo.mp3"),
        MediaFile("rphjb_CapisciRidotti.mp3")
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
        MediaFile("rphjb_Esperimento.mp3"),
        MediaFile("rphjb_Esperimento.gif"),
        MediaFile("rphjb_Esperimento2.gif"),
        MediaFile("rphjb_Esperimento3.gif"),
      ),
      replySelection = RandomSelection
    ),
    ReplyBundleMessage(
      TextTrigger(List(StringTextTriggerValue("schifosi"))),
      List(
        MediaFile("rphjb_ViCalpesto.gif"),
        MediaFile("rphjb_Schifosi.mp3"),
        MediaFile("rphjb_Schifosi2.mp3"),
        MediaFile("rphjb_Schifosi3.mp3"),
        MediaFile("rphjb_Schifosi3.gif"),
        MediaFile("rphjb_StateZittiZozziUltimi.mp3"),
        MediaFile("rphjb_Schifosi.gif"),
        MediaFile("rphjb_Schifosi2.gif")
      ),
      replySelection = RandomSelection
    ),
    ReplyBundleMessage(
      TextTrigger(List(StringTextTriggerValue("mortacci vostri"))),
      List(
        MediaFile("rphjb_MortacciVostri.gif"),
        MediaFile("rphjb_StateZittiZozziUltimi.mp3"),
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
        MediaFile("rphjb_Stanco.mp3"),
        MediaFile("rphjb_PannaOcchialiSpalla.mp3"),
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
        MediaFile("rphjb_Problema.mp3"),
        MediaFile("rphjb_PannaOcchialiSpalla.mp3")
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
        MediaFile("rphjb_Applauso.gif"),
        MediaFile("rphjb_Applauso.mp3"),
        MediaFile("rphjb_Applauso2.mp3"),
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
        MediaFile("rphjb_VeniteQua.gif"),
        MediaFile("rphjb_VeniteQua.mp3")
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
        MediaFile("rphjb_ChiCacciaISoldi.gif"),
        MediaFile("rphjb_ChiCacciaISoldi.mp3")
      )
    ),
    ReplyBundleMessage(
      TextTrigger(List(RegexTextTriggerValue("[od]?dio mio[,]? no".r))),
      List(
        MediaFile("rphjb_OddioMioNo.gif"),
        MediaFile("rphjb_OddioMioNo.mp3")
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
        MediaFile("rphjb_Arivato.gif"),
        MediaFile("rphjb_Arivato.mp3")
      )
    ),
    ReplyBundleMessage(
      TextTrigger(List(RegexTextTriggerValue("delu[sd]".r))),
      List(
        MediaFile("rphjb_Deluso.gif"),
        MediaFile("rphjb_Deluso.mp3")
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
        MediaFile("rphjb_ComeViPare.gif"),
        MediaFile("rphjb_ComeViPare.mp3")
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
        MediaFile("rphjb_DiventoBestia.mp3"),
        MediaFile("rphjb_Incazzo.mp3"),
        MediaFile("rphjb_Incazzo2.mp3")
      ),
      replySelection = RandomSelection
    )
  )

  val messageRepliesData: List[ReplyBundleMessage] =
    messageRepliesAudioData ++ messageRepliesGifData ++ messageRepliesVideoData ++ messageRepliesSpecialData

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
    ResourceAccess.fileSystem.getResourceByteArray[F]("rphjb_RichardPHJBensonBot.token").map(_.map(_.toChar).mkString)

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
