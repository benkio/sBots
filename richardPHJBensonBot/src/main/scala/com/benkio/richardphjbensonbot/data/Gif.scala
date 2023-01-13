package com.benkio.richardphjbensonbot.data

import com.benkio.telegrambotinfrastructure.model._
import com.lightbend.emoji.ShortCodes.Defaults._
import com.lightbend.emoji.ShortCodes.Implicits._

object Gif {

  def messageRepliesGifData[F[_]]: List[ReplyBundleMessage[F]] = List(
    ReplyBundleMessage(
      TextTrigger(
        RegexTextTriggerValue("\\bcontinua\\b".r, 8)
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
        MediaFile("rphjb_SchifoseUltime.mp4")
      )
    ),
    ReplyBundleMessage(
      TextTrigger(
        RegexTextTriggerValue("(a[f]+)?fanculo(,)? per contesia".r, 20)
      ),
      List(
        MediaFile("rphjb_FanculoPerCortesia.mp4")
      )
    ),
    ReplyBundleMessage(
      TextTrigger(
        StringTextTriggerValue("gli autori")
      ),
      List(
        MediaFile("rphjb_Autori.mp4")
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
        MediaFile("rphjb_Graffi.mp4")
      )
    ),
    ReplyBundleMessage(
      TextTrigger(
        StringTextTriggerValue("mi piaccio"),
        StringTextTriggerValue("impazzire"),
      ),
      List(
        MediaFile("rphjb_MiPiaccio.mp4")
      )
    ),
    ReplyBundleMessage(
      TextTrigger(
        StringTextTriggerValue("pelle d'oca"),
        StringTextTriggerValue("sussult"),
        StringTextTriggerValue("brivid")
      ),
      List(
        MediaFile("rphjb_Brivido.mp4")
      )
    ),
    ReplyBundleMessage(
      TextTrigger(
        StringTextTriggerValue("che siamo noi"),
        StringTextTriggerValue("pezzi di merda"),
      ),
      List(
        MediaFile("rphjb_PezziDiMerda.mp4")
      )
    ),
    ReplyBundleMessage(
      TextTrigger(
        StringTextTriggerValue(" urlo"),
        StringTextTriggerValue("urlo "),
        StringTextTriggerValue("disper"),
      ),
      List(
        MediaFile("rphjb_Tuffo.mp4"),
        MediaFile("rphjb_Urlo.mp4"),
        MediaFile("rphjb_Urlo3.mp4"),
        MediaFile("rphjb_Urlo2.mp4"),
        MediaFile("rphjb_UrloCanaro.mp4")
      ),
      replySelection = RandomSelection
    ),
    ReplyBundleMessage(
      TextTrigger(
        StringTextTriggerValue("rispondere")
      ),
      List(
        MediaFile("rphjb_Rispondere.mp4")
      )
    ),
    ReplyBundleMessage(
      TextTrigger(
        StringTextTriggerValue("cuore in mano"),
        StringTextTriggerValue("mano nella mano"),
        StringTextTriggerValue("pelle contro la pelle")
      ),
      List(
        MediaFile("rphjb_CuoreInMano.mp4")
      )
    ),
    ReplyBundleMessage(
      TextTrigger(
        StringTextTriggerValue("stato brado")
      ),
      List(
        MediaFile("rphjb_StatoBrado.mp4")
      )
    ),
    ReplyBundleMessage(
      TextTrigger(
        StringTextTriggerValue("pronto dimmi")
      ),
      List(
        MediaFile("rphjb_ProntoDimmi.mp4")
      )
    ),
    ReplyBundleMessage(
      TextTrigger(
        RegexTextTriggerValue("\\bport[a]+\\b".r, 5)
      ),
      List(
        MediaFile("rphjb_Porta.mp4")
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
        MediaFile("rphjb_PrendoIlNecessario.mp4")
      )
    ),
    ReplyBundleMessage(
      TextTrigger(
        StringTextTriggerValue("nella gola")
      ),
      List(
        MediaFile("rphjb_NellaGola.mp4")
      )
    ),
    ReplyBundleMessage(
      TextTrigger(
        StringTextTriggerValue("siamo qua")
      ),
      List(
        MediaFile("rphjb_SiamoQua.mp4")
      )
    ),
    ReplyBundleMessage(
      TextTrigger(
        StringTextTriggerValue("cucciolo")
      ),
      List(
        MediaFile("rphjb_Cucciolo.mp4"),
        MediaFile("rphjb_Cucciolo2.mp4"),
      ),
      replySelection = RandomSelection
    ),
    ReplyBundleMessage(
      TextTrigger(
        StringTextTriggerValue("Che c'hai")
      ),
      List(
        MediaFile("rphjb_CheCHai.mp4")
      )
    ),
    ReplyBundleMessage(
      TextTrigger(
        RegexTextTriggerValue("\\b06\\b".r, 2),
        StringTextTriggerValue("prefisso")
      ),
      List(
        MediaFile("rphjb_06.mp4")
      )
    ),
    ReplyBundleMessage(
      TextTrigger(
        StringTextTriggerValue("abbiamo vinto")
      ),
      List(
        MediaFile("rphjb_AbbiamoVinto.mp4")
      )
    ),
    ReplyBundleMessage(
      TextTrigger(
        StringTextTriggerValue("telefonata pilotata"),
        StringTextTriggerValue("falsità")
      ),
      List(
        MediaFile("rphjb_TelefonataPilotata.mp4")
      )
    ),
    ReplyBundleMessage(
      TextTrigger(
        StringTextTriggerValue("ed è vero")
      ),
      List(
        MediaFile("rphjb_Vero.mp4")
      )
    ),
    ReplyBundleMessage(
      TextTrigger(
        StringTextTriggerValue("quante ore"),
        StringTextTriggerValue("quanto tempo")
      ),
      List(
        MediaFile("rphjb_QuanteOre.mp4")
      )
    ),
    ReplyBundleMessage(
      TextTrigger(
        RegexTextTriggerValue("come ha fatto a entr(à|are)".r, 23)
      ),
      List(
        MediaFile("rphjb_ComeHaFattoAEntrare.mp4")
      )
    ),
    ReplyBundleMessage(
      TextTrigger(
        StringTextTriggerValue("donna cane")
      ),
      List(
        MediaFile("rphjb_DonnaCane.mp4")
      )
    ),
    ReplyBundleMessage(
      TextTrigger(
        StringTextTriggerValue("menzion")
      ),
      List(
        MediaFile("rphjb_NonMiMenzionareQuestaParola.mp4")
      )
    ),
    ReplyBundleMessage(
      TextTrigger(
        StringTextTriggerValue("hollywood")
      ),
      List(
        MediaFile("rphjb_Hollywood.mp4")
      )
    ),
    ReplyBundleMessage(
      TextTrigger(
        StringTextTriggerValue("piano superiore"),
        StringTextTriggerValue("compete"),
        StringTextTriggerValue("gerarca")
      ),
      List(
        MediaFile("rphjb_PianoSuperioreCompete.mp4")
      )
    ),
    ReplyBundleMessage(
      TextTrigger(
        StringTextTriggerValue("chi è")
      ),
      List(
        MediaFile("rphjb_QuestaPersonaScusate.mp4")
      )
    ),
    ReplyBundleMessage(
      TextTrigger(
        StringTextTriggerValue("tucul")
      ),
      List(
        MediaFile("rphjb_TuCul.mp4")
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
        MediaFile("rphjb_Occhiolino.mp4")
      )
    ),
    ReplyBundleMessage(
      TextTrigger(
        StringTextTriggerValue("soffro")
      ),
      List(
        MediaFile("rphjb_Soffro.mp4")
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
        MediaFile("rphjb_MannaggiaLaSalute.mp4")
      )
    ),
    ReplyBundleMessage(
      TextTrigger(
        StringTextTriggerValue("mi rompi il cazzo"),
        StringTextTriggerValue("mi dai fastidio")
      ),
      List(
        MediaFile("rphjb_MiRompiErCazzo.mp4")
      )
    ),
    ReplyBundleMessage(
      TextTrigger(
        StringTextTriggerValue("dare fastidio")
      ),
      List(
        MediaFile("rphjb_DareFastidio.mp4")
      )
    ),
    ReplyBundleMessage(
      TextTrigger(
        StringTextTriggerValue("succh"),
        StringTextTriggerValue("olio di croce")
      ),
      List(
        MediaFile("rphjb_OlioDiCroce.mp4")
      )
    ),
    ReplyBundleMessage(
      TextTrigger(
        StringTextTriggerValue("c'ha [pure ]?ragione"),
        StringTextTriggerValue("o no?")
      ),
      List(
        MediaFile("rphjb_Ragione.mp4")
      )
    ),
    ReplyBundleMessage(
      TextTrigger(
        StringTextTriggerValue("generi musicali"),
        RegexTextTriggerValue("solo il me(t|d)al".r, 13)
      ),
      List(
        MediaFile("rphjb_GeneriMusicali.mp4")
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
        MediaFile("rphjb_CosaSuccesso.mp4")
      )
    ),
    ReplyBundleMessage(
      TextTrigger(
        StringTextTriggerValue("perchè mi guardi")
      ),
      List(
        MediaFile("rphjb_Guardi.mp4")
      )
    ),
    ReplyBundleMessage(
      TextTrigger(
        RegexTextTriggerValue("[cg]hi[td]a[r]+is[td]a [bp]referi[dt]o".r, 21)
      ),
      List(
        MediaFile("rphjb_Chitarrista.mp4")
      )
    ),
    ReplyBundleMessage(
      TextTrigger(
        StringTextTriggerValue("mi diverti"),
        StringTextTriggerValue("mi sono divertito"),
      ),
      List(
        MediaFile("rphjb_Diverti.mp4")
      )
    ),
    ReplyBundleMessage(
      TextTrigger(
        RegexTextTriggerValue("e parl[a]+\\b".r, 7)
      ),
      List(
        MediaFile("rphjb_Parla.mp4")
      )
    ),
    ReplyBundleMessage(
      TextTrigger(
        StringTextTriggerValue("uno scherzo")
      ),
      List(
        MediaFile("rphjb_Scherzo.mp4")
      )
    ),
    ReplyBundleMessage(
      TextTrigger(
        StringTextTriggerValue("che si deve fà"),
        StringTextTriggerValue("campà"),
      ),
      List(
        MediaFile("rphjb_Campa.mp4")
      )
    ),
    ReplyBundleMessage(
      TextTrigger(
        StringTextTriggerValue("pensa alla deficienza"),
        RegexTextTriggerValue("ma si può dire una cosa (del genere|così)".r, 28),
      ),
      List(
        MediaFile("rphjb_Deficienza.mp4")
      )
    ),
    ReplyBundleMessage(
      TextTrigger(
        RegexTextTriggerValue("e(s|c)certo".r, 6),
        StringTextTriggerValue("accetto le critiche"),
        StringTextTriggerValue("non me ne frega un cazzo")
      ),
      List(
        MediaFile("rphjb_Escerto.mp4")
      )
    ),
    ReplyBundleMessage(
      TextTrigger(
        RegexTextTriggerValue("levati[\\. ]*dai coglioni".r, 19),
        RegexTextTriggerValue("fuori[\\. ]*dai coglioni".r, 18)
      ),
      List(
        MediaFile("rphjb_LevatiDaiCoglioni.mp4")
      )
    ),
    ReplyBundleMessage(
      TextTrigger(
        RegexTextTriggerValue("più co(gl|j)ione".r, 11),
        RegexTextTriggerValue("dice co(gl|j)ione".r, 12),
        RegexTextTriggerValue("co(gl|j)ion([e]{3,}|e[!]{3,})".r, 9)
      ),
      List(
        MediaFile("rphjb_CoglioneGif.mp4"),
        MediaFile("rphjb_PiuCoglione.mp4"),
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
        MediaFile("rphjb_Bravo.mp4")
      )
    ),
    ReplyBundleMessage(
      TextTrigger(
        StringTextTriggerValue("capolavoro")
      ),
      List(
        MediaFile("rphjb_Capolavoro.mp4")
      )
    ),
    ReplyBundleMessage(
      TextTrigger(
        RegexTextTriggerValue("\\bmetal\\b".r, 5)
      ),
      List(
        MediaFile("rphjb_Metal.mp4"),
        MediaFile("rphjb_IlMartel.mp4")
      ),
      replySelection = RandomSelection
    ),
    ReplyBundleMessage(
      TextTrigger(
        StringTextTriggerValue("allucinante")
      ),
      List(
        MediaFile("rphjb_Allucinante.mp4")
      )
    ),
    ReplyBundleMessage(
      TextTrigger(
        StringTextTriggerValue("mare di cazzate"),
        StringTextTriggerValue("non è possibile")
      ),
      List(
        MediaFile("rphjb_NonPossibile.mp4")
      )
    ),
    ReplyBundleMessage(
      TextTrigger(
        StringTextTriggerValue("porca miseria"),
        StringTextTriggerValue("facendo incazzare")
      ),
      List(
        MediaFile("rphjb_PorcaMiseria.mp4")
      )
    ),
    ReplyBundleMessage(
      TextTrigger(
        StringTextTriggerValue("facendo soffrire")
      ),
      List(
        MediaFile("rphjb_FacendoSoffrire.mp4")
      )
    ),
    ReplyBundleMessage(
      TextTrigger(
        StringTextTriggerValue("dovete soffrire"),
        RegexTextTriggerValue("vi voglio far(e)? soffrire".r, 23),
      ),
      List(
        MediaFile("rphjb_DoveteSoffrire.mp4")
      )
    ),
    ReplyBundleMessage(
      TextTrigger(
        StringTextTriggerValue("sarete puniti")
      ),
      List(
        MediaFile("rphjb_SaretePuniti.mp4")
      )
    ),
    ReplyBundleMessage(
      TextTrigger(
        StringTextTriggerValue("cantanti"),
        StringTextTriggerValue("serie z")
      ),
      List(
        MediaFile("rphjb_CantantiSerieZ.mp4")
      )
    ),
    ReplyBundleMessage(
      TextTrigger(
        StringTextTriggerValue("sentendo male")
      ),
      List(
        MediaFile("rphjb_MiStoSentendoMale.mp4")
      )
    ),
    ReplyBundleMessage(
      TextTrigger(
        StringTextTriggerValue("lunghezza d'onda"),
        StringTextTriggerValue("brave persone")
      ),
      List(
        MediaFile("rphjb_LunghezzaDOnda.mp4")
      )
    ),
    ReplyBundleMessage(
      TextTrigger(
        StringTextTriggerValue("delirio")
      ),
      List(
        MediaFile("rphjb_Delirio.mp4")
      )
    ),
    ReplyBundleMessage(
      TextTrigger(
        RegexTextTriggerValue("[gq]uerelare".r, 9),
      ),
      List(
        MediaFile("rphjb_Querelare.mp4")
      )
    ),
    ReplyBundleMessage(
      TextTrigger(
        StringTextTriggerValue("cantate"),
        StringTextTriggerValue("arigliano")
      ),
      List(
        MediaFile("rphjb_Arigliano.mp4")
      )
    ),
    ReplyBundleMessage(
      TextTrigger(
        StringTextTriggerValue("andati al cesso"),
        StringTextTriggerValue("diecimila volte")
      ),
      List(
        MediaFile("rphjb_Alcesso.mp4")
      )
    ),
    ReplyBundleMessage(
      TextTrigger(
        StringTextTriggerValue("non manca niente"),
        StringTextTriggerValue("c'è tutto")
      ),
      List(
        MediaFile("rphjb_NonMancaNiente.mp4")
      )
    ),
    ReplyBundleMessage(
      TextTrigger(
        StringTextTriggerValue("in fila")
      ),
      List(
        MediaFile("rphjb_MettitiInFila.mp4")
      )
    ),
    ReplyBundleMessage(
      TextTrigger(
        StringTextTriggerValue("non male")
      ),
      List(
        MediaFile("rphjb_NonMale.mp4")
      )
    ),
    ReplyBundleMessage(
      TextTrigger(
        StringTextTriggerValue("perchè si sente")
      ),
      List(
        MediaFile("rphjb_SiSente.mp4")
      )
    ),
    ReplyBundleMessage(
      TextTrigger(
        StringTextTriggerValue("per colpa vostra"),
        RegexTextTriggerValue("(divento|diventare|sono) (matto|pazzo)".r, 10)
      ),
      List(
        MediaFile("rphjb_StoDiventandoPazzo.mp4")
      )
    ),
    ReplyBundleMessage(
      TextTrigger(
        StringTextTriggerValue("sorca"),
        StringTextTriggerValue("patonza"),
        StringTextTriggerValue("lecciso"),
        RegexTextTriggerValue("\\bfi[cg]a\\b".r, 4)
      ),
      List(
        MediaFile("rphjb_SorcaLecciso.mp4"),
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
        MediaFile("rphjb_ChiCazzoSei.mp4")
      )
    ),
    ReplyBundleMessage(
      TextTrigger(
        StringTextTriggerValue("feste")
      ),
      List(
        MediaFile("rphjb_Feste.mp4")
      )
    ),
    ReplyBundleMessage(
      TextTrigger(
        StringTextTriggerValue("si ostina"),
        StringTextTriggerValue("foto vecchie")
      ),
      List(
        MediaFile("rphjb_Ostina.mp4")
      )
    ),
    ReplyBundleMessage(
      TextTrigger(
        RegexTextTriggerValue("(è|diventa) vecchi[ao]".r, 9),
      ),
      List(
        MediaFile("rphjb_Vecchio.mp4")
      )
    ),
    ReplyBundleMessage(
      TextTrigger(
        StringTextTriggerValue("scatta qualcosa")
      ),
      List(
        MediaFile("rphjb_ScattaQualcosa.mp4")
      )
    ),
    ReplyBundleMessage(
      TextTrigger(
        StringTextTriggerValue("pure bona")
      ),
      List(
        MediaFile("rphjb_Bona.mp4")
      )
    ),
    ReplyBundleMessage(
      TextTrigger(
        RegexTextTriggerValue("sei [gc]ambiat[oa]".r, 12)
      ),
      List(
        MediaFile("rphjb_SeiCambiata.mp4")
      )
    ),
    ReplyBundleMessage(
      TextTrigger(
        StringTextTriggerValue("mio discapito"),
        StringTextTriggerValue("disgabido")
      ),
      List(
        MediaFile("rphjb_Discapito.mp4")
      )
    ),
    ReplyBundleMessage(
      TextTrigger(
        StringTextTriggerValue("squallida"),
        StringTextTriggerValue("abbia mai sentito")
      ),
      List(
        MediaFile("rphjb_Squallida.mp4")
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
        RegexTextTriggerValue("\\bn[o]{2,}\\b".r, 3)
      ),
      List(
        MediaFile("rphjb_No.mp4")
      )
    ),
    ReplyBundleMessage(
      TextTrigger(
        StringTextTriggerValue("destino"),
        StringTextTriggerValue("incontrare")
      ),
      List(
        MediaFile("rphjb_Destino.mp4")
      )
    ),
    ReplyBundleMessage(
      TextTrigger(
        StringTextTriggerValue("meridionale"),
        StringTextTriggerValue("terron")
      ),
      List(
        MediaFile("rphjb_Meridionale.mp4")
      )
    ),
    ReplyBundleMessage(
      TextTrigger(
        StringTextTriggerValue("baci"),
        StringTextTriggerValue("limonare")
      ),
      List(
        MediaFile("rphjb_Bacio.mp4"),
        MediaFile("rphjb_DanzaMacabra.mp4")
      ),
      replySelection = RandomSelection
    ),
    ReplyBundleMessage(
      TextTrigger(
        StringTextTriggerValue("giudica")
      ),
      List(
        MediaFile("rphjb_Giudicate.mp4"),
        MediaFile("rphjb_ComeFaiAGiudicare.mp4")
      ),
      replySelection = RandomSelection
    ),
    ReplyBundleMessage(
      TextTrigger(
        StringTextTriggerValue("drogati"),
        StringTextTriggerValue("sostanze improprie")
      ),
      List(
        MediaFile("rphjb_DrogatiRockettari1.mp4"),
        MediaFile("rphjb_DrogatiRockettari.mp4"),
        MediaFile("rphjb_EricClaptonDrogaUominiAffari.mp4"),
        MediaFile("rphjb_DrogatiRockettari2.mp4"),
        MediaFile("rphjb_DrogatiPiloti.gif"),
        MediaFile("rphjb_DrogatiPiloti.mp4"),
        MediaFile("rphjb_Rampolli.mp4")
      ),
      replySelection = RandomSelection
    ),
    ReplyBundleMessage(
      TextTrigger(
        RegexTextTriggerValue("ro[ckgh]+(ch|gh|k)e(d+|t+)ari".r, 10),
        StringTextTriggerValue("stillati")
      ),
      List(
        MediaFile("rphjb_DrogatiRockettari1.mp4"),
        MediaFile("rphjb_DrogatiRockettari.mp4"),
        MediaFile("rphjb_DrogatiRockettari2.mp4"),
        MediaFile("rphjb_RocchettariCapelliCortiGiaccaCravattaPassaportoStronzi.mp4")
      ),
      replySelection = RandomSelection
    ),
    ReplyBundleMessage(
      TextTrigger(
        StringTextTriggerValue("sguardo")
      ),
      List(
        MediaFile("rphjb_Sguardo.mp4"),
        MediaFile("rphjb_Sguardo2.mp4"),
        MediaFile("rphjb_Sguardo3.gif"),
        MediaFile("rphjb_Sguardo4.mp4"),
        MediaFile("rphjb_SguardoCanaro.mp4")
      ),
      replySelection = RandomSelection
    ),
    ReplyBundleMessage(
      TextTrigger(
        StringTextTriggerValue("a quel punto")
      ),
      List(
        MediaFile("rphjb_QuelPunto.mp4")
      )
    ),
    ReplyBundleMessage(
      TextTrigger(
        StringTextTriggerValue("quattro solo"),
        StringTextTriggerValue("faccio in tempo")
      ),
      List(
        MediaFile("rphjb_QuattroSolo.mp4")
      )
    ),
    ReplyBundleMessage(
      TextTrigger(
        StringTextTriggerValue("faccio la parte"),
        StringTextTriggerValue(" recit"),
        StringTextTriggerValue(" fing"),
        RegexTextTriggerValue("\\ba[t]{2,}[o]+re\\b".r, 7),
        StringTextTriggerValue("attrice")
      ),
      List(
        MediaFile("rphjb_FaccioLaParte.mp4")
      )
    ),
    ReplyBundleMessage(
      TextTrigger(
        StringTextTriggerValue("assolutamente no"),
        StringTextTriggerValue("non mi lamento")
      ),
      List(
        MediaFile("rphjb_NonMiLamento.mp4")
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
        MediaFile("rphjb_IlSensoCapito.mp4")
      )
    ),
    ReplyBundleMessage(
      TextTrigger(
        RegexTextTriggerValue("\\bester\\b".r, 5)
      ),
      List(
        MediaFile("rphjb_Ester.mp4"),
        MediaFile("rphjb_Ester2.mp4"),
      ),
      replySelection = RandomSelection
    ),
    ReplyBundleMessage(
      TextTrigger(
        RegexTextTriggerValue("abi(t|d)ua(t|d)o".r, 8),
        RegexTextTriggerValue("proprioll(a|à)".r, 10),
      ),
      List(
        MediaFile("rphjb_Propriolla.mp4")
      )
    ),
    ReplyBundleMessage(
      TextTrigger(
        StringTextTriggerValue("non vedo questo grande problema")
      ),
      List(
        MediaFile("rphjb_VabbeProblema.mp4"),
      ),
    ),
    ReplyBundleMessage(
      TextTrigger(
        StringTextTriggerValue("il bongo"),
        StringTextTriggerValue("non esiste un basso più pontente al mondo"),
        StringTextTriggerValue("music man")
      ),
      List(
        MediaFile("rphjb_IlBongo.mp4")
      )
    ),
    ReplyBundleMessage(
      TextTrigger(
        StringTextTriggerValue("inserirlo su internet"),
        RegexTextTriggerValue("immagini ama(d|t)oriali".r, 19)
      ),
      List(
        MediaFile("rphjb_Internet.mp4")
      )
    ),
    ReplyBundleMessage(
      TextTrigger(
        StringTextTriggerValue("decido io"),
      ),
      List(
        MediaFile("rphjb_DecidoIo.mp4")
      )
    )
  )

}
