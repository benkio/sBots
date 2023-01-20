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
        RegexTextTriggerValue("(a[f]+)?fanculo(,)? per contesia".r, 20)
      ),
      List(
        GifFile("rphjb_FanculoPerCortesia.mp4"),
        MediaFile("rphjb_DecidoIoMareCazzatePerCortesia.mp4")
      )
    ),
    ReplyBundleMessage(
      TextTrigger(
        StringTextTriggerValue("gli autori")
      ),
      List(
        GifFile("rphjb_Autori.mp4")
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
        GifFile("rphjb_Graffi.mp4")
      )
    ),
    ReplyBundleMessage(
      TextTrigger(
        StringTextTriggerValue("pelle d'oca"),
        StringTextTriggerValue("sussult"),
        StringTextTriggerValue("brivid")
      ),
      List(
        GifFile("rphjb_Brivido.mp4")
      )
    ),
    ReplyBundleMessage(
      TextTrigger(
        StringTextTriggerValue("che siamo noi"),
        StringTextTriggerValue("pezzi di merda"),
      ),
      List(
        GifFile("rphjb_PezziDiMerda.mp4")
      )
    ),
    ReplyBundleMessage(
      TextTrigger(
        RegexTextTriggerValue("\\burlo\\b".r, 4),
        StringTextTriggerValue("\\b[a]{5,}"),
      ),
      List(
        GifFile("rphjb_Tuffo.mp4"),
        GifFile("rphjb_Urlo.mp4"),
        GifFile("rphjb_Urlo3.mp4"),
        GifFile("rphjb_Urlo4.mp4"),
        GifFile("rphjb_Urlo2.mp4"),
        GifFile("rphjb_UrloCanaro.mp4"),
        GifFile("rphjb_UrloRiso.mp4")
      ),
      replySelection = RandomSelection
    ),
    ReplyBundleMessage(
      TextTrigger(
        StringTextTriggerValue("rispondere")
      ),
      List(
        GifFile("rphjb_Rispondere.mp4")
      )
    ),
    ReplyBundleMessage(
      TextTrigger(
        StringTextTriggerValue("cuore in mano"),
        StringTextTriggerValue("mano nella mano"),
        StringTextTriggerValue("pelle contro la pelle")
      ),
      List(
        GifFile("rphjb_CuoreInMano.mp4")
      )
    ),
    ReplyBundleMessage(
      TextTrigger(
        StringTextTriggerValue("stato brado")
      ),
      List(
        GifFile("rphjb_StatoBrado.mp4")
      )
    ),
    ReplyBundleMessage(
      TextTrigger(
        RegexTextTriggerValue("\\bport[a]+\\b".r, 5)
      ),
      List(
        GifFile("rphjb_Porta.mp4")
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
        GifFile("rphjb_PrendoIlNecessario.mp4")
      )
    ),
    ReplyBundleMessage(
      TextTrigger(
        StringTextTriggerValue("nella gola")
      ),
      List(
        GifFile("rphjb_NellaGola.mp4")
      )
    ),
    ReplyBundleMessage(
      TextTrigger(
        StringTextTriggerValue("siamo qua")
      ),
      List(
        GifFile("rphjb_SiamoQua.mp4")
      )
    ),
    ReplyBundleMessage(
      TextTrigger(
        StringTextTriggerValue("cucciolo")
      ),
      List(
        GifFile("rphjb_Cucciolo.mp4"),
        GifFile("rphjb_Cucciolo2.mp4"),
      ),
      replySelection = RandomSelection
    ),
    ReplyBundleMessage(
      TextTrigger(
        StringTextTriggerValue("Che c'hai")
      ),
      List(
        GifFile("rphjb_CheCHai.mp4")
      )
    ),
    ReplyBundleMessage(
      TextTrigger(
        RegexTextTriggerValue("\\b06\\b".r, 2),
        StringTextTriggerValue("prefisso")
      ),
      List(
        GifFile("rphjb_06.mp4")
      )
    ),
    ReplyBundleMessage(
      TextTrigger(
        StringTextTriggerValue("abbiamo vinto")
      ),
      List(
        GifFile("rphjb_AbbiamoVinto.mp4")
      )
    ),
    ReplyBundleMessage(
      TextTrigger(
        StringTextTriggerValue("telefonata pilotata"),
        StringTextTriggerValue("falsità")
      ),
      List(
        GifFile("rphjb_TelefonataPilotata.mp4")
      )
    ),
    ReplyBundleMessage(
      TextTrigger(
        StringTextTriggerValue("quante ore"),
        StringTextTriggerValue("quanto tempo")
      ),
      List(
        GifFile("rphjb_QuanteOre.mp4")
      )
    ),
    ReplyBundleMessage(
      TextTrigger(
        RegexTextTriggerValue("come ha fatto a entr(à|are)".r, 23)
      ),
      List(
        GifFile("rphjb_ComeHaFattoAEntrare.mp4")
      )
    ),
    ReplyBundleMessage(
      TextTrigger(
        StringTextTriggerValue("donna cane")
      ),
      List(
        GifFile("rphjb_DonnaCane.mp4")
      )
    ),
    ReplyBundleMessage(
      TextTrigger(
        StringTextTriggerValue("menzion")
      ),
      List(
        GifFile("rphjb_NonMiMenzionareQuestaParola.mp4")
      )
    ),
    ReplyBundleMessage(
      TextTrigger(
        StringTextTriggerValue("hollywood")
      ),
      List(
        GifFile("rphjb_Hollywood.mp4")
      )
    ),
    ReplyBundleMessage(
      TextTrigger(
        StringTextTriggerValue("piano superiore"),
        StringTextTriggerValue("compete"),
        StringTextTriggerValue("gerarca")
      ),
      List(
        GifFile("rphjb_PianoSuperioreCompete.mp4")
      )
    ),
    ReplyBundleMessage(
      TextTrigger(
        StringTextTriggerValue("chi è")
      ),
      List(
        GifFile("rphjb_QuestaPersonaScusate.mp4")
      )
    ),
    ReplyBundleMessage(
      TextTrigger(
        StringTextTriggerValue("tucul")
      ),
      List(
        GifFile("rphjb_TuCul.mp4")
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
        GifFile("rphjb_Occhiolino.mp4")
      )
    ),
    ReplyBundleMessage(
      TextTrigger(
        StringTextTriggerValue("soffro")
      ),
      List(
        GifFile("rphjb_Soffro.mp4")
      )
    ),
    ReplyBundleMessage(
      TextTrigger(
        StringTextTriggerValue("indispettirmi"),
        StringTextTriggerValue("oltrepassare"),
        StringTextTriggerValue("divento cattivo")
      ),
      List(
        GifFile("rphjb_Indispettirmi.mp4")
      )
    ),
    ReplyBundleMessage(
      TextTrigger(
        StringTextTriggerValue("mannaggia"),
        StringTextTriggerValue("la salute")
      ),
      List(
        GifFile("rphjb_MannaggiaLaSalute.mp4")
      )
    ),
    ReplyBundleMessage(
      TextTrigger(
        StringTextTriggerValue("mi rompi il cazzo"),
        StringTextTriggerValue("mi dai fastidio")
      ),
      List(
        GifFile("rphjb_MiRompiErCazzo.mp4")
      )
    ),
    ReplyBundleMessage(
      TextTrigger(
        StringTextTriggerValue("dare fastidio")
      ),
      List(
        GifFile("rphjb_DareFastidio.mp4")
      )
    ),
    ReplyBundleMessage(
      TextTrigger(
        StringTextTriggerValue("succh"),
        StringTextTriggerValue("olio di croce")
      ),
      List(
        GifFile("rphjb_OlioDiCroce.mp4")
      )
    ),
    ReplyBundleMessage(
      TextTrigger(
        StringTextTriggerValue("c'ha [pure ]?ragione"),
        StringTextTriggerValue("o no?")
      ),
      List(
        GifFile("rphjb_Ragione.mp4")
      )
    ),
    ReplyBundleMessage(
      TextTrigger(
        StringTextTriggerValue("perchè mi guardi")
      ),
      List(
        GifFile("rphjb_Guardi.mp4")
      )
    ),
    ReplyBundleMessage(
      TextTrigger(
        RegexTextTriggerValue("[cg]hi[td]a[r]+is[td]a [bp]referi[dt]o".r, 21)
      ),
      List(
        GifFile("rphjb_Chitarrista.mp4")
      )
    ),
    ReplyBundleMessage(
      TextTrigger(
        StringTextTriggerValue("mi diverti"),
        StringTextTriggerValue("mi sono divertito"),
      ),
      List(
        GifFile("rphjb_Diverti.mp4")
      )
    ),
    ReplyBundleMessage(
      TextTrigger(
        StringTextTriggerValue("uno scherzo")
      ),
      List(
        GifFile("rphjb_Scherzo.mp4")
      )
    ),
    ReplyBundleMessage(
      TextTrigger(
        StringTextTriggerValue("che si deve fà"),
        StringTextTriggerValue("campà"),
      ),
      List(
        GifFile("rphjb_Campa.mp4")
      )
    ),
    ReplyBundleMessage(
      TextTrigger(
        StringTextTriggerValue("pensa alla deficienza"),
        RegexTextTriggerValue("ma si può dire una cosa (del genere|così)".r, 28),
      ),
      List(
        GifFile("rphjb_Deficienza.mp4")
      )
    ),
    ReplyBundleMessage(
      TextTrigger(
        RegexTextTriggerValue("levati[\\. ]*dai coglioni".r, 19),
        RegexTextTriggerValue("fuori[\\. ]*dai coglioni".r, 18)
      ),
      List(
        GifFile("rphjb_LevatiDaiCoglioni.mp4")
      )
    ),
    ReplyBundleMessage(
      TextTrigger(
        RegexTextTriggerValue("più co(gl|j)ione".r, 11),
        RegexTextTriggerValue("dice co(gl|j)ione".r, 12),
        RegexTextTriggerValue("co(gl|j)ion([e]{3,}|e[!]{3,})".r, 9)
      ),
      List(
        GifFile("rphjb_CoglioneGif.mp4"),
        GifFile("rphjb_PiuCoglione.mp4"),
        GifFile("rphjb_Coglione.mp4")
      ),
      replySelection = RandomSelection
    ),
    ReplyBundleMessage(
      TextTrigger(
        StringTextTriggerValue("bravo!!!"),
        StringTextTriggerValue("bravooo")
      ),
      List(
        GifFile("rphjb_Bravo.mp4")
      )
    ),
    ReplyBundleMessage(
      TextTrigger(
        StringTextTriggerValue("capolavoro")
      ),
      List(
        GifFile("rphjb_Capolavoro.mp4")
      )
    ),
    ReplyBundleMessage(
      TextTrigger(
        RegexTextTriggerValue("\\bmetal\\b".r, 5)
      ),
      List(
        GifFile("rphjb_Metal.mp4"),
        GifFile("rphjb_IlMartel.mp4")
      ),
      replySelection = RandomSelection
    ),
    ReplyBundleMessage(
      TextTrigger(
        StringTextTriggerValue("allucinante")
      ),
      List(
        GifFile("rphjb_Allucinante.mp4")
      )
    ),
    ReplyBundleMessage(
      TextTrigger(
        StringTextTriggerValue("porca miseria"),
        StringTextTriggerValue("facendo incazzare")
      ),
      List(
        GifFile("rphjb_PorcaMiseria.mp4")
      )
    ),
    ReplyBundleMessage(
      TextTrigger(
        StringTextTriggerValue("facendo soffrire")
      ),
      List(
        GifFile("rphjb_FacendoSoffrire.mp4")
      )
    ),
    ReplyBundleMessage(
      TextTrigger(
        StringTextTriggerValue("dovete soffrire"),
        RegexTextTriggerValue("vi voglio far(e)? soffrire".r, 23),
      ),
      List(
        GifFile("rphjb_DoveteSoffrire.mp4")
      )
    ),
    ReplyBundleMessage(
      TextTrigger(
        StringTextTriggerValue("sarete puniti")
      ),
      List(
        GifFile("rphjb_SaretePuniti.mp4")
      )
    ),
    ReplyBundleMessage(
      TextTrigger(
        StringTextTriggerValue("cantanti"),
        StringTextTriggerValue("serie z")
      ),
      List(
        GifFile("rphjb_CantantiSerieZ.mp4")
      )
    ),
    ReplyBundleMessage(
      TextTrigger(
        StringTextTriggerValue("sentendo male")
      ),
      List(
        GifFile("rphjb_MiStoSentendoMale.mp4")
      )
    ),
    ReplyBundleMessage(
      TextTrigger(
        StringTextTriggerValue("lunghezza d'onda"),
        StringTextTriggerValue("brave persone")
      ),
      List(
        GifFile("rphjb_LunghezzaDOnda.mp4")
      )
    ),
    ReplyBundleMessage(
      TextTrigger(
        StringTextTriggerValue("delirio")
      ),
      List(
        GifFile("rphjb_Delirio.mp4")
      )
    ),
    ReplyBundleMessage(
      TextTrigger(
        RegexTextTriggerValue("[gq]uerelare".r, 9),
      ),
      List(
        GifFile("rphjb_Querelare.mp4")
      )
    ),
    ReplyBundleMessage(
      TextTrigger(
        StringTextTriggerValue("cantate"),
        StringTextTriggerValue("arigliano")
      ),
      List(
        GifFile("rphjb_Arigliano.mp4")
      )
    ),
    ReplyBundleMessage(
      TextTrigger(
        StringTextTriggerValue("andati al cesso"),
        StringTextTriggerValue("diecimila volte")
      ),
      List(
        GifFile("rphjb_Alcesso.mp4")
      )
    ),
    ReplyBundleMessage(
      TextTrigger(
        StringTextTriggerValue("non manca niente"),
        StringTextTriggerValue("c'è tutto")
      ),
      List(
        GifFile("rphjb_NonMancaNiente.mp4")
      )
    ),
    ReplyBundleMessage(
      TextTrigger(
        StringTextTriggerValue("in fila")
      ),
      List(
        GifFile("rphjb_MettitiInFila.mp4")
      )
    ),
    ReplyBundleMessage(
      TextTrigger(
        StringTextTriggerValue("non male")
      ),
      List(
        GifFile("rphjb_NonMale.mp4")
      )
    ),
    ReplyBundleMessage(
      TextTrigger(
        StringTextTriggerValue("perchè si sente")
      ),
      List(
        GifFile("rphjb_SiSente.mp4")
      )
    ),
    ReplyBundleMessage(
      TextTrigger(
        StringTextTriggerValue("chi cazzo sei")
      ),
      List(
        GifFile("rphjb_ChiCazzoSei.mp4")
      )
    ),
    ReplyBundleMessage(
      TextTrigger(
        StringTextTriggerValue("feste")
      ),
      List(
        GifFile("rphjb_Feste.mp4")
      )
    ),
    ReplyBundleMessage(
      TextTrigger(
        StringTextTriggerValue("si ostina"),
        StringTextTriggerValue("foto vecchie")
      ),
      List(
        GifFile("rphjb_Ostina.mp4")
      )
    ),
    ReplyBundleMessage(
      TextTrigger(
        RegexTextTriggerValue("(è|diventa) vecchi[ao]".r, 9),
      ),
      List(
        GifFile("rphjb_Vecchio.mp4")
      )
    ),
    ReplyBundleMessage(
      TextTrigger(
        StringTextTriggerValue("scatta qualcosa")
      ),
      List(
        GifFile("rphjb_ScattaQualcosa.mp4")
      )
    ),
    ReplyBundleMessage(
      TextTrigger(
        StringTextTriggerValue("pure bona")
      ),
      List(
        GifFile("rphjb_Bona.mp4")
      )
    ),
    ReplyBundleMessage(
      TextTrigger(
        RegexTextTriggerValue("sei [gc]ambiat[oa]".r, 12)
      ),
      List(
        GifFile("rphjb_SeiCambiata.mp4")
      )
    ),
    ReplyBundleMessage(
      TextTrigger(
        StringTextTriggerValue("mio discapito"),
        StringTextTriggerValue("disgabido")
      ),
      List(
        GifFile("rphjb_Discapito.mp4")
      )
    ),
    ReplyBundleMessage(
      TextTrigger(
        StringTextTriggerValue("squallida"),
        StringTextTriggerValue("abbia mai sentito")
      ),
      List(
        GifFile("rphjb_Squallida.mp4")
      )
    ),
    ReplyBundleMessage(
      TextTrigger(
        StringTextTriggerValue("la verità")
      ),
      List(
        GifFile("rphjb_Verita.mp4")
      )
    ),
    ReplyBundleMessage(
      TextTrigger(
        RegexTextTriggerValue("\\bn[o]{2,}\\b".r, 3)
      ),
      List(
        GifFile("rphjb_No.mp4")
      )
    ),
    ReplyBundleMessage(
      TextTrigger(
        StringTextTriggerValue("destino"),
        StringTextTriggerValue("incontrare")
      ),
      List(
        GifFile("rphjb_Destino.mp4")
      )
    ),
    ReplyBundleMessage(
      TextTrigger(
        StringTextTriggerValue("meridionale"),
        StringTextTriggerValue("terron")
      ),
      List(
        GifFile("rphjb_Meridionale.mp4")
      )
    ),
    ReplyBundleMessage(
      TextTrigger(
        StringTextTriggerValue("baci"),
        StringTextTriggerValue("limonare")
      ),
      List(
        GifFile("rphjb_Bacio.mp4"),
        MediaFile("rphjb_DanzaMacabra.mp4")
      ),
      replySelection = RandomSelection
    ),
    ReplyBundleMessage(
      TextTrigger(
        StringTextTriggerValue("giudica")
      ),
      List(
        GifFile("rphjb_Giudicate.mp4"),
        GifFile("rphjb_ComeFaiAGiudicare.mp4")
      ),
      replySelection = RandomSelection
    ),
    ReplyBundleMessage(
      TextTrigger(
        StringTextTriggerValue("drogati"),
        StringTextTriggerValue("sostanze improprie")
      ),
      List(
        GifFile("rphjb_DrogatiRockettari1.mp4"),
        MediaFile("rphjb_DrogatiRockettari.mp4"),
        MediaFile("rphjb_EricClaptonDrogaUominiAffari.mp4"),
        GifFile("rphjb_DrogatiRockettari2.mp4"),
        GifFile("rphjb_DrogatiPilotiGif.mp4"),
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
        GifFile("rphjb_DrogatiRockettari1.mp4"),
        MediaFile("rphjb_DrogatiRockettari.mp4"),
        GifFile("rphjb_DrogatiRockettari2.mp4"),
        MediaFile("rphjb_RocchettariCapelliCortiGiaccaCravattaPassaportoStronzi.mp4")
      ),
      replySelection = RandomSelection
    ),
    ReplyBundleMessage(
      TextTrigger(
        StringTextTriggerValue("sguardo")
      ),
      List(
        GifFile("rphjb_Sguardo.mp4"),
        GifFile("rphjb_Sguardo2.mp4"),
        MediaFile("rphjb_Sguardo3.gif"),
        GifFile("rphjb_Sguardo4.mp4"),
        GifFile("rphjb_SguardoCanaro.mp4")
      ),
      replySelection = RandomSelection
    ),
    ReplyBundleMessage(
      TextTrigger(
        StringTextTriggerValue("a quel punto")
      ),
      List(
        GifFile("rphjb_QuelPunto.mp4")
      )
    ),
    ReplyBundleMessage(
      TextTrigger(
        StringTextTriggerValue("quattro solo"),
        StringTextTriggerValue("faccio in tempo")
      ),
      List(
        GifFile("rphjb_QuattroSolo.mp4")
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
        GifFile("rphjb_FaccioLaParte.mp4")
      )
    ),
    ReplyBundleMessage(
      TextTrigger(
        StringTextTriggerValue("assolutamente no"),
        StringTextTriggerValue("non mi lamento")
      ),
      List(
        GifFile("rphjb_NonMiLamento.mp4")
      )
    ),
    ReplyBundleMessage(
      TextTrigger(
        StringTextTriggerValue("inizio della fine")
      ),
      List(
        GifFile("rphjb_InizioDellaFineGif.mp4"),
        MediaFile("rphjb_InizioDellaFine.mp4")
      ),
      replySelection = RandomSelection
    ),
    ReplyBundleMessage(
      TextTrigger(
        StringTextTriggerValue("il senso")
      ),
      List(
        GifFile("rphjb_IlSensoCapito.mp4")
      )
    ),
    ReplyBundleMessage(
      TextTrigger(
        RegexTextTriggerValue("\\bester\\b".r, 5)
      ),
      List(
        GifFile("rphjb_Ester.mp4"),
        GifFile("rphjb_Ester2.mp4"),
      ),
      replySelection = RandomSelection
    ),
    ReplyBundleMessage(
      TextTrigger(
        RegexTextTriggerValue("abi(t|d)ua(t|d)o".r, 8),
        RegexTextTriggerValue("proprioll(a|à)".r, 10),
      ),
      List(
        GifFile("rphjb_Propriolla.mp4")
      )
    ),
    ReplyBundleMessage(
      TextTrigger(
        StringTextTriggerValue("non vedo questo grande problema")
      ),
      List(
        GifFile("rphjb_VabbeProblema.mp4"),
      ),
    ),
    ReplyBundleMessage(
      TextTrigger(
        StringTextTriggerValue("il bongo"),
        StringTextTriggerValue("non esiste un basso più pontente al mondo"),
        StringTextTriggerValue("music man")
      ),
      List(
        GifFile("rphjb_IlBongo.mp4")
      )
    ),
    ReplyBundleMessage(
      TextTrigger(
        StringTextTriggerValue("inserirlo su internet"),
        RegexTextTriggerValue("immagini ama(d|t)oriali".r, 19)
      ),
      List(
        GifFile("rphjb_Internet.mp4")
      )
    ),
    ReplyBundleMessage(
      TextTrigger(
        RegexTextTriggerValue("prendere quello l[aà]".r, 18),
      ),
      List(
        GifFile("rphjb_AaaPrendereQuelloLa.mp4")
      )
    ),
    ReplyBundleMessage(
      TextTrigger(
        RegexTextTriggerValue("(af)?fanculo in una maniera pazzesca".r, 33),
        StringTextTriggerValue("altro che quel coglione"),
      ),
      List(
        GifFile("rphjb_AffanculoManieraPazzesca.mp4")
      )
    ),
    ReplyBundleMessage(
      TextTrigger(
        StringTextTriggerValue("col cazzo che non so suonà")
      ),
      List(
        GifFile("rphjb_ColCazzoSuona.mp4")
      )
    ),
    ReplyBundleMessage(
      TextTrigger(
        RegexTextTriggerValue("proprio a me\\b".r, 12)
      ),
      List(
        GifFile("rphjb_ProprioAMe.mp4")
      )
    ),
    ReplyBundleMessage(
      TextTrigger(
        StringTextTriggerValue("where are you going?")
      ),
      List(
        GifFile("rphjb_WhereAreYouGoing.mp4")
      )
    ),
  )

}
