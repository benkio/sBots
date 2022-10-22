package com.benkio.richardphjbensonbot.data

import cats._
import com.benkio.telegrambotinfrastructure.model._
import com.lightbend.emoji.ShortCodes.Defaults._
import com.lightbend.emoji.ShortCodes.Implicits._

object Gif {

  def messageRepliesGifData[F[_]: Applicative]: List[ReplyBundleMessage[F]] = List(
    ReplyBundleMessage(
      TextTrigger(
        RegexTextTriggerValue("\\bcontinua\\b".r)
      ),
      List(
        MediaFile("rphjb_Continua.mp3"),
        MediaFile("rphjb_Continua.mp4")
      ),
      replySelection = RandomSelection
    ),
    ReplyBundleMessage(
      TextTrigger(
        StringTextTriggerValue("schifose"),
        StringTextTriggerValue("ultime")
      ),
      List(
        MediaFile("rphjb_SchifoseUltime.gif")
      )
    ),
    ReplyBundleMessage(
      TextTrigger(
        RegexTextTriggerValue("(af+)?fanculo(,)? per contesia".r)
      ),
      List(
        MediaFile("rphjb_FanculoPerCortesia.gif")
      )
    ),
    ReplyBundleMessage(
      TextTrigger(
        StringTextTriggerValue("gli autori")
      ),
      List(
        MediaFile("rphjb_Autori.gif")
      )
    ),
    ReplyBundleMessage(
      TextTrigger(
        StringTextTriggerValue("questo è matto")
      ),
      List(
        MediaFile("rphjb_MattoRagazzi.gif")
      )
    ),
    ReplyBundleMessage(
      TextTrigger(
        StringTextTriggerValue("scivola")
      ),
      List(
        MediaFile("rphjb_SiScivola.mp3")
      )
    ),
    ReplyBundleMessage(
      TextTrigger(
        StringTextTriggerValue("graffi")
      ),
      List(
        MediaFile("rphjb_Graffi.gif")
      )
    ),
    ReplyBundleMessage(
      TextTrigger(
        StringTextTriggerValue("mi piaccio"),
        StringTextTriggerValue("impazzire"),
      ),
      List(
        MediaFile("rphjb_MiPiaccio.gif")
      )
    ),
    ReplyBundleMessage(
      TextTrigger(
        StringTextTriggerValue("pelle d'oca"),
        StringTextTriggerValue("sussult"),
        StringTextTriggerValue("brivid")
      ),
      List(
        MediaFile("rphjb_Brivido.gif")
      )
    ),
    ReplyBundleMessage(
      TextTrigger(
        StringTextTriggerValue("che siamo noi"),
        StringTextTriggerValue("pezzi di merda"),
      ),
      List(
        MediaFile("rphjb_PezziDiMerda.gif")
      )
    ),
    ReplyBundleMessage(
      TextTrigger(
        StringTextTriggerValue(" urlo"),
        StringTextTriggerValue("urlo "),
        StringTextTriggerValue("disper"),
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
        StringTextTriggerValue("rispondere")
      ),
      List(
        MediaFile("rphjb_Rispondere.gif")
      )
    ),
    ReplyBundleMessage(
      TextTrigger(
        StringTextTriggerValue("cuore in mano"),
        StringTextTriggerValue("mano nella mano"),
        StringTextTriggerValue("pelle contro la pelle")
      ),
      List(
        MediaFile("rphjb_CuoreInMano.gif")
      )
    ),
    ReplyBundleMessage(
      TextTrigger(
        StringTextTriggerValue("stato brado")
      ),
      List(
        MediaFile("rphjb_StatoBrado.gif")
      )
    ),
    ReplyBundleMessage(
      TextTrigger(
        StringTextTriggerValue("pronto dimmi")
      ),
      List(
        MediaFile("rphjb_ProntoDimmi.gif")
      )
    ),
    ReplyBundleMessage(
      TextTrigger(
        RegexTextTriggerValue(" port[a]+( |$)".r)
      ),
      List(
        MediaFile("rphjb_Porta.gif")
      )
    ),
    ReplyBundleMessage(
      TextTrigger(
        StringTextTriggerValue("prendo quello che cazzo c'è da prendere"),
        StringTextTriggerValue("prendo il motorino"),
        StringTextTriggerValue("prendo la macchina"),
        StringTextTriggerValue("prendo l'auto"),
      ),
      List(
        MediaFile("rphjb_PrendoIlNecessario.gif")
      )
    ),
    ReplyBundleMessage(
      TextTrigger(
        StringTextTriggerValue("nella gola")
      ),
      List(
        MediaFile("rphjb_NellaGola.gif")
      )
    ),
    ReplyBundleMessage(
      TextTrigger(
        StringTextTriggerValue("siamo qua")
      ),
      List(
        MediaFile("rphjb_SiamoQua.gif")
      )
    ),
    ReplyBundleMessage(
      TextTrigger(
        StringTextTriggerValue("cucciolo")
      ),
      List(
        MediaFile("rphjb_Cucciolo.gif"),
        MediaFile("rphjb_Cucciolo2.gif"),
      ),
      replySelection = RandomSelection
    ),
    ReplyBundleMessage(
      TextTrigger(
        StringTextTriggerValue("Che c'hai")
      ),
      List(
        MediaFile("rphjb_CheCHai.gif")
      )
    ),
    ReplyBundleMessage(
      TextTrigger(
        StringTextTriggerValue("06"),
        StringTextTriggerValue("prefisso")
      ),
      List(
        MediaFile("rphjb_06.gif")
      )
    ),
    ReplyBundleMessage(
      TextTrigger(
        StringTextTriggerValue("abbiamo vinto")
      ),
      List(
        MediaFile("rphjb_AbbiamoVinto.gif")
      )
    ),
    ReplyBundleMessage(
      TextTrigger(
        StringTextTriggerValue("telefonata pilotata"),
        StringTextTriggerValue("falsità")
      ),
      List(
        MediaFile("rphjb_TelefonataPilotata.gif")
      )
    ),
    ReplyBundleMessage(
      TextTrigger(
        StringTextTriggerValue("ed è vero")
      ),
      List(
        MediaFile("rphjb_Vero.gif")
      )
    ),
    ReplyBundleMessage(
      TextTrigger(
        StringTextTriggerValue("quante ore"),
        StringTextTriggerValue("quanto tempo")
      ),
      List(
        MediaFile("rphjb_QuanteOre.gif")
      )
    ),
    ReplyBundleMessage(
      TextTrigger(
        RegexTextTriggerValue("come ha fatto a entr(à|are)".r)
      ),
      List(
        MediaFile("rphjb_ComeHaFattoAEntrare.gif")
      )
    ),
    ReplyBundleMessage(
      TextTrigger(
        StringTextTriggerValue("donna cane")
      ),
      List(
        MediaFile("rphjb_DonnaCane.gif")
      )
    ),
    ReplyBundleMessage(
      TextTrigger(
        StringTextTriggerValue("menzion")
      ),
      List(
        MediaFile("rphjb_NonMiMenzionareQuestaParola.gif")
      )
    ),
    ReplyBundleMessage(
      TextTrigger(
        StringTextTriggerValue("hollywood")
      ),
      List(
        MediaFile("rphjb_Hollywood.gif")
      )
    ),
    ReplyBundleMessage(
      TextTrigger(
        StringTextTriggerValue("piano superiore"),
        StringTextTriggerValue("compete"),
        StringTextTriggerValue("gerarca")
      ),
      List(
        MediaFile("rphjb_PianoSuperioreCompete.gif")
      )
    ),
    ReplyBundleMessage(
      TextTrigger(
        StringTextTriggerValue("chi è")
      ),
      List(
        MediaFile("rphjb_QuestaPersonaScusate.gif")
      )
    ),
    ReplyBundleMessage(
      TextTrigger(
        StringTextTriggerValue("tucul")
      ),
      List(
        MediaFile("rphjb_TuCul.gif")
      )
    ),
    ReplyBundleMessage(
      TextTrigger(
        StringTextTriggerValue(";)"),
        StringTextTriggerValue("occhiolino"),
        StringTextTriggerValue("wink"),
        StringTextTriggerValue(e":wink:")
      ),
      List(
        MediaFile("rphjb_Occhiolino.gif")
      )
    ),
    ReplyBundleMessage(
      TextTrigger(
        StringTextTriggerValue("soffro")
      ),
      List(
        MediaFile("rphjb_Soffro.gif")
      )
    ),
    ReplyBundleMessage(
      TextTrigger(
        StringTextTriggerValue("indispettirmi"),
        StringTextTriggerValue("oltrepassare"),
        StringTextTriggerValue("divento cattivo")
      ),
      List(
        MediaFile("rphjb_Indispettirmi.gif")
      )
    ),
    ReplyBundleMessage(
      TextTrigger(
        StringTextTriggerValue("mannaggia"),
        StringTextTriggerValue("la salute")
      ),
      List(
        MediaFile("rphjb_MannaggiaLaSalute.gif")
      )
    ),
    ReplyBundleMessage(
      TextTrigger(
        StringTextTriggerValue("mi rompi il cazzo"),
        StringTextTriggerValue("mi dai fastidio")
      ),
      List(
        MediaFile("rphjb_MiRompiErCazzo.gif")
      )
    ),
    ReplyBundleMessage(
      TextTrigger(
        StringTextTriggerValue("dare fastidio")
      ),
      List(
        MediaFile("rphjb_DareFastidio.gif")
      )
    ),
    ReplyBundleMessage(
      TextTrigger(
        StringTextTriggerValue("succh"),
        StringTextTriggerValue("olio di croce")
      ),
      List(
        MediaFile("rphjb_OlioDiCroce.gif")
      )
    ),
    ReplyBundleMessage(
      TextTrigger(
        StringTextTriggerValue("c'ha [pure ]?ragione"),
        StringTextTriggerValue("o no?")
      ),
      List(
        MediaFile("rphjb_Ragione.gif")
      )
    ),
    ReplyBundleMessage(
      TextTrigger(
        StringTextTriggerValue("generi musicali"),
        RegexTextTriggerValue("solo il me(t|d)al".r)
      ),
      List(
        MediaFile("rphjb_GeneriMusicali.gif")
      )
    ),
    ReplyBundleMessage(
      TextTrigger(
        StringTextTriggerValue("bassista"),
        StringTextTriggerValue("slap")
      ),
      List(
        MediaFile("rphjb_Bassista.gif")
      )
    ),
    ReplyBundleMessage(
      TextTrigger(
        StringTextTriggerValue("cosa è successo")
      ),
      List(
        MediaFile("rphjb_CosaSuccesso.gif")
      )
    ),
    ReplyBundleMessage(
      TextTrigger(
        StringTextTriggerValue("perchè mi guardi")
      ),
      List(
        MediaFile("rphjb_Guardi.gif")
      )
    ),
    ReplyBundleMessage(
      TextTrigger(
        RegexTextTriggerValue("[cg]hi[td]a[r]+is[td]a [bp]referi[dt]o".r)
      ),
      List(
        MediaFile("rphjb_Chitarrista.gif")
      )
    ),
    ReplyBundleMessage(
      TextTrigger(
        StringTextTriggerValue("mi diverti"),
        StringTextTriggerValue("mi sono divertito"),
      ),
      List(
        MediaFile("rphjb_Diverti.gif")
      )
    ),
    ReplyBundleMessage(
      TextTrigger(
        RegexTextTriggerValue("e parl[a]+\\b")
      ),
      List(
        MediaFile("rphjb_Parla.gif")
      )
    ),
    ReplyBundleMessage(
      TextTrigger(
        StringTextTriggerValue("uno scherzo")
      ),
      List(
        MediaFile("rphjb_Scherzo.gif")
      )
    ),
    ReplyBundleMessage(
      TextTrigger(
        StringTextTriggerValue("che si deve fà"),
        StringTextTriggerValue("campà"),
      ),
      List(
        MediaFile("rphjb_Campa.gif")
      )
    ),
    ReplyBundleMessage(
      TextTrigger(
        StringTextTriggerValue("pensa alla deficienza"),
        RegexTextTriggerValue("ma si può dire una cosa (del genere|così)".r),
      ),
      List(
        MediaFile("rphjb_Deficienza.gif")
      )
    ),
    ReplyBundleMessage(
      TextTrigger(
        RegexTextTriggerValue("e(s|c)certo".r),
        StringTextTriggerValue("accetto le critiche"),
        StringTextTriggerValue("non me ne frega un cazzo")
      ),
      List(
        MediaFile("rphjb_Escerto.gif")
      )
    ),
    ReplyBundleMessage(
      TextTrigger(
        RegexTextTriggerValue("levati[/. ]*dai coglioni".r),
        RegexTextTriggerValue("fuori[/. ]*dai coglioni".r)
      ),
      List(
        MediaFile("rphjb_LevatiDaiCoglioni.gif")
      )
    ),
    ReplyBundleMessage(
      TextTrigger(
        RegexTextTriggerValue("più co(gl|j)ione".r),
        RegexTextTriggerValue("dice co(gl|j)ione".r),
        RegexTextTriggerValue("co(gl|j)ion([e]{3,}|e[!]{3,})".r)
      ),
      List(
        MediaFile("rphjb_Coglione.gif"),
        MediaFile("rphjb_PiuCoglione.gif"),
        MediaFile("rphjb_Coglione.mp4")
      ),
      replySelection = RandomSelection
    ),
    ReplyBundleMessage(
      TextTrigger(
        StringTextTriggerValue("bravo!!!"),
        StringTextTriggerValue("bravooo")
      ),
      List(
        MediaFile("rphjb_Bravo.gif")
      )
    ),
    ReplyBundleMessage(
      TextTrigger(
        StringTextTriggerValue("capolavoro")
      ),
      List(
        MediaFile("rphjb_Capolavoro.gif")
      )
    ),
    ReplyBundleMessage(
      TextTrigger(
        RegexTextTriggerValue("\\bmetal\\b".r)
      ),
      List(
        MediaFile("rphjb_Metal.gif"),
        MediaFile("rphjb_IlMartel.gif")
      ),
      replySelection = RandomSelection
    ),
    ReplyBundleMessage(
      TextTrigger(
        StringTextTriggerValue("allucinante")
      ),
      List(
        MediaFile("rphjb_Allucinante.gif")
      )
    ),
    ReplyBundleMessage(
      TextTrigger(
        StringTextTriggerValue("mare di cazzate"),
        StringTextTriggerValue("non è possibile")
      ),
      List(
        MediaFile("rphjb_NonPossibile.gif")
      )
    ),
    ReplyBundleMessage(
      TextTrigger(
        StringTextTriggerValue("porca miseria"),
        StringTextTriggerValue("incazzare")
      ),
      List(
        MediaFile("rphjb_PorcaMiseria.gif")
      )
    ),
    ReplyBundleMessage(
      TextTrigger(
        StringTextTriggerValue("facendo soffrire")
      ),
      List(
        MediaFile("rphjb_FacendoSoffrire.gif")
      )
    ),
    ReplyBundleMessage(
      TextTrigger(
        StringTextTriggerValue("dovete soffrire")
      ),
      List(
        MediaFile("rphjb_DoveteSoffrire.gif")
      )
    ),
    ReplyBundleMessage(
      TextTrigger(
        StringTextTriggerValue("sarete puniti")
      ),
      List(
        MediaFile("rphjb_SaretePuniti.gif")
      )
    ),
    ReplyBundleMessage(
      TextTrigger(
        StringTextTriggerValue("cantanti"),
        StringTextTriggerValue("serie z")
      ),
      List(
        MediaFile("rphjb_CantantiSerieZ.gif")
      )
    ),
    ReplyBundleMessage(
      TextTrigger(
        StringTextTriggerValue("sentendo male")
      ),
      List(
        MediaFile("rphjb_MiStoSentendoMale.gif")
      )
    ),
    ReplyBundleMessage(
      TextTrigger(
        StringTextTriggerValue("lunghezza d'onda"),
        StringTextTriggerValue("brave persone")
      ),
      List(
        MediaFile("rphjb_LunghezzaDOnda.gif")
      )
    ),
    ReplyBundleMessage(
      TextTrigger(
        StringTextTriggerValue("delirio")
      ),
      List(
        MediaFile("rphjb_Delirio.gif")
      )
    ),
    ReplyBundleMessage(
      TextTrigger(
        StringTextTriggerValue("querelare"),
        StringTextTriggerValue("guerelare")
      ),
      List(
        MediaFile("rphjb_Querelare.gif")
      )
    ),
    ReplyBundleMessage(
      TextTrigger(
        StringTextTriggerValue("cantate"),
        StringTextTriggerValue("arigliano")
      ),
      List(
        MediaFile("rphjb_Arigliano.gif")
      )
    ),
    ReplyBundleMessage(
      TextTrigger(
        StringTextTriggerValue("andati al cesso"),
        StringTextTriggerValue("diecimila volte")
      ),
      List(
        MediaFile("rphjb_Alcesso.gif")
      )
    ),
    ReplyBundleMessage(
      TextTrigger(
        StringTextTriggerValue("non manca niente"),
        StringTextTriggerValue("c'è tutto")
      ),
      List(
        MediaFile("rphjb_NonMancaNiente.gif")
      )
    ),
    ReplyBundleMessage(
      TextTrigger(
        StringTextTriggerValue("in fila")
      ),
      List(
        MediaFile("rphjb_MettitiInFila.gif")
      )
    ),
    ReplyBundleMessage(
      TextTrigger(
        StringTextTriggerValue("non male")
      ),
      List(
        MediaFile("rphjb_NonMale.gif")
      )
    ),
    ReplyBundleMessage(
      TextTrigger(
        StringTextTriggerValue("perchè si sente")
      ),
      List(
        MediaFile("rphjb_SiSente.gif")
      )
    ),
    ReplyBundleMessage(
      TextTrigger(
        StringTextTriggerValue("per colpa vostra"),
        RegexTextTriggerValue("(divento|diventare|sono) (matto|pazzo)".r)
      ),
      List(
        MediaFile("rphjb_StoDiventandoPazzo.gif")
      )
    ),
    ReplyBundleMessage(
      TextTrigger(
        StringTextTriggerValue("sorca"),
        StringTextTriggerValue("patonza"),
        StringTextTriggerValue("lecciso"),
        RegexTextTriggerValue("\\bfi[cg]a\\b".r)
      ),
      List(
        MediaFile("rphjb_SorcaLecciso.gif"),
        MediaFile("rphjb_FigaLarga.mp4"),
        MediaFile("rphjb_FragolinaFichina.mp3"),
        MediaFile("rphjb_Sorca.mp4")
      ),
      replySelection = RandomSelection
    ),
    ReplyBundleMessage(
      TextTrigger(
        StringTextTriggerValue("chi cazzo sei")
      ),
      List(
        MediaFile("rphjb_ChiCazzoSei.gif")
      )
    ),
    ReplyBundleMessage(
      TextTrigger(
        StringTextTriggerValue("feste")
      ),
      List(
        MediaFile("rphjb_Feste.gif")
      )
    ),
    ReplyBundleMessage(
      TextTrigger(
        StringTextTriggerValue("si ostina"),
        StringTextTriggerValue("foto vecchie")
      ),
      List(
        MediaFile("rphjb_Ostina.gif")
      )
    ),
    ReplyBundleMessage(
      TextTrigger(
        RegexTextTriggerValue("(è|diventa) vecchi[ao]".r),
      ),
      List(
        MediaFile("rphjb_Vecchio.gif")
      )
    ),
    ReplyBundleMessage(
      TextTrigger(
        StringTextTriggerValue("scatta qualcosa")
      ),
      List(
        MediaFile("rphjb_ScattaQualcosa.gif")
      )
    ),
    ReplyBundleMessage(
      TextTrigger(
        StringTextTriggerValue("pure bona")
      ),
      List(
        MediaFile("rphjb_Bona.gif")
      )
    ),
    ReplyBundleMessage(
      TextTrigger(
        StringTextTriggerValue("danza macabra"),
      ),
      List(
        MediaFile("rphjb_DanzaMacabra.gif"),
        MediaFile("rphjb_DanzaMacabra.mp4")
      ),
      replySelection = RandomSelection
    ),
    ReplyBundleMessage(
      TextTrigger(
        RegexTextTriggerValue("sei [gc]ambiat[oa]".r)
      ),
      List(
        MediaFile("rphjb_SeiCambiata.gif")
      )
    ),
    ReplyBundleMessage(
      TextTrigger(
        StringTextTriggerValue("mio discapito"),
        StringTextTriggerValue("disgabido")
      ),
      List(
        MediaFile("rphjb_Discapito.gif")
      )
    ),
    ReplyBundleMessage(
      TextTrigger(
        StringTextTriggerValue("squallida"),
        StringTextTriggerValue("abbia mai sentito")
      ),
      List(
        MediaFile("rphjb_Squallida.gif")
      )
    ),
    ReplyBundleMessage(
      TextTrigger(
        StringTextTriggerValue("la verità")
      ),
      List(
        MediaFile("rphjb_Verita.gif")
      )
    ),
    ReplyBundleMessage(
      TextTrigger(
        RegexTextTriggerValue("\\bn[o]{2,}\\b".r)
      ),
      List(
        MediaFile("rphjb_No.gif")
      )
    ),
    ReplyBundleMessage(
      TextTrigger(
        StringTextTriggerValue("destino"),
        StringTextTriggerValue("incontrare")
      ),
      List(
        MediaFile("rphjb_Destino.gif")
      )
    ),
    ReplyBundleMessage(
      TextTrigger(
        StringTextTriggerValue("meridionale"),
        StringTextTriggerValue("terron")
      ),
      List(
        MediaFile("rphjb_Meridionale.gif")
      )
    ),
    ReplyBundleMessage(
      TextTrigger(
        StringTextTriggerValue("baci"),
        StringTextTriggerValue("limonare")
      ),
      List(
        MediaFile("rphjb_Bacio.gif"),
        MediaFile("rphjb_DanzaMacabra.mp4")
      ),
      replySelection = RandomSelection
    ),
    ReplyBundleMessage(
      TextTrigger(
        StringTextTriggerValue("giudica")
      ),
      List(
        MediaFile("rphjb_Giudicate.gif"),
        MediaFile("rphjb_ComeFaiAGiudicare.gif")
      ),
      replySelection = RandomSelection
    ),
    ReplyBundleMessage(
      TextTrigger(
        StringTextTriggerValue("drogati"),
        StringTextTriggerValue("sostanze improprie")
      ),
      List(
        MediaFile("rphjb_DrogatiRockettari1.gif"),
        MediaFile("rphjb_DrogatiRockettari.mp4"),
        MediaFile("rphjb_EricClaptonDrogaUominiAffari.mp4"),
        MediaFile("rphjb_DrogatiRockettari2.gif"),
        MediaFile("rphjb_DrogatiPiloti.gif"),
        MediaFile("rphjb_DrogatiPiloti.mp4"),
        MediaFile("rphjb_Rampolli.mp4")
      ),
      replySelection = RandomSelection
    ),
    ReplyBundleMessage(
      TextTrigger(
        RegexTextTriggerValue("ro[ckgh]+(ch|gh|k)e(d+|t+)ari".r),
        StringTextTriggerValue("stillati")
      ),
      List(
        MediaFile("rphjb_DrogatiRockettari1.gif"),
        MediaFile("rphjb_DrogatiRockettari.mp4"),
        MediaFile("rphjb_DrogatiRockettari2.gif"),
        MediaFile("rphjb_RocchettariCapelliCortiGiaccaCravattaPassaportoStronzi.mp4")
      ),
      replySelection = RandomSelection
    ),
    ReplyBundleMessage(
      TextTrigger(
        StringTextTriggerValue("sguardo")
      ),
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
      TextTrigger(
        StringTextTriggerValue("a quel punto")
      ),
      List(
        MediaFile("rphjb_QuelPunto.gif")
      )
    ),
    ReplyBundleMessage(
      TextTrigger(
        StringTextTriggerValue("quattro solo"),
        StringTextTriggerValue("faccio in tempo")
      ),
      List(
        MediaFile("rphjb_QuattroSolo.gif")
      )
    ),
    ReplyBundleMessage(
      TextTrigger(
        StringTextTriggerValue("faccio la parte"),
        StringTextTriggerValue(" recit"),
        StringTextTriggerValue(" fing"),
        RegexTextTriggerValue("\\ba[t]{2,}[o]+re\\b".r),
        StringTextTriggerValue("attrice")
      ),
      List(
        MediaFile("rphjb_FaccioLaParte.gif")
      )
    ),
    ReplyBundleMessage(
      TextTrigger(
        StringTextTriggerValue("assolutamente no"),
        StringTextTriggerValue("non mi lamento")
      ),
      List(
        MediaFile("rphjb_NonMiLamento.gif")
      )
    ),
    ReplyBundleMessage(
      TextTrigger(
        StringTextTriggerValue("inizio della fine")
      ),
      List(
        MediaFile("rphjb_InizioDellaFine.gif"),
        MediaFile("rphjb_InizioDellaFine.mp4")
      ),
      replySelection = RandomSelection
    ),
    ReplyBundleMessage(
      TextTrigger(
        StringTextTriggerValue("il senso")
      ),
      List(
        MediaFile("rphjb_IlSensoCapito.gif")
      )
    ),
    ReplyBundleMessage(
      TextTrigger(
        RegexTextTriggerValue("\\bester\\b".r)
      ),
      List(
        MediaFile("rphjb_Ester.gif"),
        MediaFile("rphjb_Ester2.gif"),
      ),
      replySelection = RandomSelection
    ),
    ReplyBundleMessage(
      TextTrigger(
        RegexTextTriggerValue("abi[td]ua[td]o".r),
        RegexTextTriggerValue("proprioll[aà]".r),
      ),
      List(
        MediaFile("rphjb_Propriolla.gif")
      )
    ),
    ReplyBundleMessage(
      TextTrigger(
        RegexTextTriggerValue("non vedo questo grande problema".r)
      ),
      List(
        MediaFile("rphjb_VabbeProblema.gif"),
      ),
    ),
    ReplyBundleMessage(
      TextTrigger(
        StringTextTriggerValue("il bongo"),
        StringTextTriggerValue("non esiste un basso più pontente al mondo"),
        StringTextTriggerValue("music man")
      ),
      List(
        MediaFile("rphjb_IlBongo.gif")
      )
    ),
    ReplyBundleMessage(
      TextTrigger(
        StringTextTriggerValue("inserirlo su internet"),
        RegexTextTriggerValue("immagini ama[dt]oriali".r)
      ),
      List(
        MediaFile("rphjb_Internet.gif")
      )
    ),
    ReplyBundleMessage(
      TextTrigger(
        StringTextTriggerValue("decido io"),
      ),
      List(
        MediaFile("rphjb_DecidoIo.gif")
      )
    )
  )

}
