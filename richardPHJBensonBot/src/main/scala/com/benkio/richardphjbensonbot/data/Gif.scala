package com.benkio.richardphjbensonbot.data

import cats.Applicative
import com.benkio.telegrambotinfrastructure.model.*

object Gif {

  def messageRepliesGifData[F[_]: Applicative]: List[ReplyBundleMessage[F]] = List(
    ReplyBundleMessage.textToMedia[F](
      "\\bcontinua\\b".r.tr(8)
    )(
      mf"rphjb_Continua.mp3",
      mf"rphjb_Continua.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      "(a[f]+)?fanculo(,)? per contesia".r.tr(20)
    )(
      gif"rphjb_FanculoPerCortesia.mp4",
      mf"rphjb_DecidoIoMareCazzatePerCortesia.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      stt"gli autori"
    )(
      gif"rphjb_Autori.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      stt"questo è matto"
    )(
      mf"rphjb_MattoRagazzi.gif"
    ),
    ReplyBundleMessage.textToMedia[F](
      stt"scivola"
    )(
      mf"rphjb_SiScivola.mp3"
    ),
    ReplyBundleMessage.textToMedia[F](
      stt"graffi"
    )(
      gif"rphjb_Graffi.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      stt"pelle d'oca",
      stt"sussult",
      stt"brivid"
    )(
      gif"rphjb_Brivido.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      stt"che siamo noi",
      stt"pezzi di merda",
    )(
      gif"rphjb_PezziDiMerda.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      "\\burlo\\b".r.tr(4),
      stt"\\b[a]{5,}",
    )(
      gif"rphjb_Tuffo.mp4",
      gif"rphjb_Urlo.mp4",
      gif"rphjb_Urlo3.mp4",
      gif"rphjb_Urlo4.mp4",
      gif"rphjb_Urlo2.mp4",
      gif"rphjb_UrloCanaro.mp4",
      gif"rphjb_UrloRiso.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      stt"rispondere"
    )(
      gif"rphjb_Rispondere.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      stt"cuore in mano",
      stt"mano nella mano",
      stt"pelle contro la pelle"
    )(
      gif"rphjb_CuoreInMano.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      stt"stato brado"
    )(
      gif"rphjb_StatoBrado.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      "\\bport[a]+\\b".r.tr(5)
    )(
      gif"rphjb_Porta.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      stt"prendo quello che cazzo c'è da prendere",
      stt"prendo il motorino",
      stt"prendo la macchina",
      stt"prendo l'auto",
    )(
      gif"rphjb_PrendoIlNecessario.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      stt"nella gola"
    )(
      gif"rphjb_NellaGola.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      stt"siamo qua"
    )(
      gif"rphjb_SiamoQua.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      stt"cucciolo"
    )(
      gif"rphjb_Cucciolo.mp4",
      gif"rphjb_Cucciolo2.mp4",
    ),
    ReplyBundleMessage.textToMedia[F](
      stt"Che c'hai"
    )(
      gif"rphjb_CheCHai.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      stt"abbiamo vinto"
    )(
      gif"rphjb_AbbiamoVinto.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      stt"telefonata pilotata",
      stt"falsità"
    )(
      gif"rphjb_TelefonataPilotata.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      "come ha fatto a entr(à|are)".r.tr(23)
    )(
      gif"rphjb_ComeHaFattoAEntrare.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      stt"donna cane"
    )(
      gif"rphjb_DonnaCane.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      stt"menzion"
    )(
      gif"rphjb_NonMiMenzionareQuestaParola.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      stt"hollywood"
    )(
      gif"rphjb_Hollywood.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      stt"piano superiore",
      stt"si compete",
      stt"gerarca"
    )(
      gif"rphjb_PianoSuperioreCompete.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      stt"chi è"
    )(
      gif"rphjb_QuestaPersonaScusate.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      stt"tucul"
    )(
      gif"rphjb_TuCul.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      stt";)",
      stt"occhiolino",
      stt"wink",
      stt"😉"
    )(
      gif"rphjb_Occhiolino.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      stt"soffro"
    )(
      gif"rphjb_Soffro.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      stt"indispettirmi",
      stt"oltrepassare",
      stt"divento cattivo"
    )(
      gif"rphjb_Indispettirmi.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      stt"mannaggia",
      stt"la salute"
    )(
      gif"rphjb_MannaggiaLaSalute.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      stt"mi rompi il cazzo",
      stt"mi dai fastidio"
    )(
      gif"rphjb_MiRompiErCazzo.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      stt"c'ha [pure ]?ragione",
      stt"o no?"
    )(
      gif"rphjb_Ragione.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      stt"perchè mi guardi"
    )(
      gif"rphjb_Guardi.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      "[cg]hi[td]a[r]+is[td]a [bp]referi[dt]o".r.tr(21)
    )(
      gif"rphjb_Chitarrista.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      stt"mi diverti",
      stt"mi sono divertito",
    )(
      gif"rphjb_Diverti.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      stt"uno scherzo"
    )(
      gif"rphjb_Scherzo.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      stt"che si deve fà",
      stt"campà",
    )(
      gif"rphjb_Campa.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      stt"pensa alla deficienza",
      "ma si può dire una cosa (del genere|così)".r.tr(28),
    )(
      gif"rphjb_Deficienza.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      "levati[\\. ]*dai coglioni".r.tr(19),
      "fuori[\\. ]*dai coglioni".r.tr(18)
    )(
      gif"rphjb_LevatiDaiCoglioni.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      "più co(gl|j)ione".r.tr(11),
      "dice co(gl|j)ione".r.tr(12),
      "co(gl|j)ion([e]{3,}|e[!]{3,})".r.tr(9)
    )(
      gif"rphjb_CoglioneGif.mp4",
      gif"rphjb_PiuCoglione.mp4",
      gif"rphjb_Coglione.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      stt"bravo!!!",
      stt"bravooo"
    )(
      gif"rphjb_Bravo.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      stt"capolavoro"
    )(
      gif"rphjb_CapolavoroGif.mp4",
      mf"rphjb_Capolavoro.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      "\\bmetal\\b".r.tr(5)
    )(
      gif"rphjb_Metal.mp4",
      gif"rphjb_IlMartel.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      stt"allucinante"
    )(
      gif"rphjb_Allucinante.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      stt"porca miseria",
      stt"facendo incazzare"
    )(
      gif"rphjb_PorcaMiseria.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      stt"facendo soffrire"
    )(
      gif"rphjb_FacendoSoffrire.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      stt"dovete soffrire",
      "vi voglio far(e)? soffrire".r.tr(23),
    )(
      gif"rphjb_DoveteSoffrire.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      stt"sarete puniti"
    )(
      gif"rphjb_SaretePuniti.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      stt"cantanti",
      stt"serie z"
    )(
      gif"rphjb_CantantiSerieZ.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      stt"sentendo male"
    )(
      gif"rphjb_MiStoSentendoMale.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      stt"lunghezza d'onda",
      stt"brave persone"
    )(
      gif"rphjb_LunghezzaDOnda.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      stt"delirio"
    )(
      gif"rphjb_Delirio.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      "[gq]uerelare".r.tr(9),
    )(
      gif"rphjb_Querelare.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      stt"cantate",
      stt"arigliano"
    )(
      gif"rphjb_Arigliano.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      stt"andati al cesso",
      stt"diecimila volte"
    )(
      gif"rphjb_Alcesso.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      stt"in fila"
    )(
      gif"rphjb_MettitiInFila.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      stt"non male"
    )(
      gif"rphjb_NonMale.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      stt"perchè si sente"
    )(
      gif"rphjb_SiSente.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      stt"chi cazzo sei"
    )(
      gif"rphjb_ChiCazzoSei.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      stt"feste"
    )(
      gif"rphjb_Feste.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      stt"si ostina",
      stt"foto vecchie"
    )(
      gif"rphjb_Ostina.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      "(è|diventa) vecchi[ao]".r.tr(9),
    )(
      gif"rphjb_Vecchio.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      stt"scatta qualcosa",
      stt"proprio in quel momento",
      stt"nel suo cervello",
    )(
      gif"rphjb_ScattaQualcosa.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      stt"pure bona"
    )(
      gif"rphjb_Bona.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      "sei [gc]ambiat[oa]".r.tr(12)
    )(
      gif"rphjb_SeiCambiata.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      stt"mio discapito",
      stt"disgabido"
    )(
      gif"rphjb_Discapito.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      stt"squallida",
      stt"abbia mai sentito"
    )(
      gif"rphjb_Squallida.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      stt"la verità"
    )(
      gif"rphjb_Verita.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      "\\bn[o]{2,}!\\b".r.tr(3)
    )(
      gif"rphjb_No.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      stt"destino",
      stt"incontrare"
    )(
      gif"rphjb_Destino.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      stt"meridionale",
      stt"terron"
    )(
      gif"rphjb_Meridionale.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      stt"baci",
      stt"limonare"
    )(
      gif"rphjb_Bacio.mp4",
      mf"rphjb_DanzaMacabra.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      stt"drogati",
      stt"sostanze improprie"
    )(
      gif"rphjb_DrogatiRockettari1.mp4",
      mf"rphjb_DrogatiRockettari.mp4",
      mf"rphjb_EricClaptonDrogaUominiAffari.mp4",
      gif"rphjb_DrogatiRockettari2.mp4",
      gif"rphjb_DrogatiPilotiGif.mp4",
      mf"rphjb_DrogatiPiloti.mp4",
      mf"rphjb_Rampolli.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      "ro[ckgh]+(ch|gh|k)e(d+|t+)ari".r.tr(10),
      stt"stillati"
    )(
      gif"rphjb_DrogatiRockettari1.mp4",
      mf"rphjb_DrogatiRockettari.mp4",
      gif"rphjb_DrogatiRockettari2.mp4",
      mf"rphjb_RocchettariCapelliCortiGiaccaCravattaPassaportoStronzi.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      stt"sguardo"
    )(
      gif"rphjb_Sguardo.mp4",
      gif"rphjb_Sguardo2.mp4",
      mf"rphjb_Confuso.mp4",
      gif"rphjb_Sguardo3.mp4",
      gif"rphjb_Sguardo4.mp4",
      gif"rphjb_SguardoCanaro.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      stt"a quel punto"
    )(
      gif"rphjb_QuelPunto.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      stt"faccio la parte",
      stt" recit",
      stt" fing",
      "\\ba[t]{2,}[o]+re\\b".r.tr(7),
      stt"attrice"
    )(
      gif"rphjb_FaccioLaParte.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      stt"inizio della fine"
    )(
      gif"rphjb_InizioDellaFineGif.mp4",
      mf"rphjb_InizioDellaFine.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      stt"il senso"
    )(
      gif"rphjb_IlSensoCapito.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      "\\bester\\b".r.tr(5)
    )(
      gif"rphjb_Ester.mp4",
      gif"rphjb_Ester2.mp4",
    ),
    ReplyBundleMessage.textToMedia[F](
      "abi(t|d)ua(t|d)o".r.tr(8),
      "proprioll(a|à)".r.tr(10),
    )(
      gif"rphjb_Propriolla.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      stt"non vedo questo grande problema"
    )(
      gif"rphjb_VabbeProblema.mp4",
    ),
    ReplyBundleMessage.textToMedia[F](
      stt"il bongo",
      stt"non esiste un basso più pontente al mondo",
      stt"music man"
    )(
      gif"rphjb_IlBongo.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      stt"inserirlo su internet",
      "immagini ama(d|t)oriali".r.tr(19)
    )(
      gif"rphjb_Internet.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      "prendere quello l[aà]".r.tr(18),
    )(
      gif"rphjb_AaaPrendereQuelloLa.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      "(af)?fanculo in una maniera pazzesca".r.tr(33),
      stt"altro che quel coglione",
    )(
      gif"rphjb_AffanculoManieraPazzesca.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      stt"col cazzo che non so suonà"
    )(
      gif"rphjb_ColCazzoSuona.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      "proprio a me\\b".r.tr(12)
    )(
      gif"rphjb_ProprioAMe.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      stt"where are you going?"
    )(
      gif"rphjb_WhereAreYouGoing.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      stt"🤨",
      stt"🧐",
      stt"sono confuso",
      "[?]{3,}".r.tr(3),
    )(
      gif"rphjb_Confuso.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      stt"😑",
      stt"😒",
      stt"🫤",
      "\\bwtf[!]*\\b".r.tr(3),
    )(
      gif"rphjb_WTF.mp4",
      gif"rphjb_WTF2.mp4",
      gif"rphjb_WTF3.mp4",
      gif"rphjb_WTF4.mp4",
    ),
    ReplyBundleMessage.textToMedia[F](
      stt"🙏"
    )(
      gif"rphjb_Prega.mp4",
      gif"rphjb_Prega2.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      "\\bsorpresa\\b".r.tr(8),
      "\\bshock\\b".r.tr(5),
      stt"😮",
    )(
      gif"rphjb_Sorpresa.mp4",
      gif"rphjb_Sorpresa2.mp4"
    ),
    ReplyBundleMessage(
      trigger = TextTrigger(
        stt"quello che ti meriti",
        stt"fino alla fine",
      ),
      reply = MediaReply.fromList[F](
        List(
          gif"rphjb_QuelloCheTiMeriti.mp4"
        )
      )
    ),
    ReplyBundleMessage.textToMedia[F](
      "[u]{3,}".r.tr(3)
    )(
      gif"rphjb_Uuu.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      stt"☝️",
      stt"👆",
      stt"👉",
      stt"👇",
      stt"👈"
    )(
      gif"rphjb_Indicare.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      stt"deal with it"
    )(
      gif"rphjb_Occhiali.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      stt"covi il male",
      stt"invidia",
      stt"livore"
    )(
      gif"rphjb_CoviMaleInvidiaLivore.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      stt"a questo punto",
      "andiamo[ci]? a sentire".r.tr(18),
      stt"l'originale",
    )(
      gif"rphjb_SentireOriginale.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      stt"fai schifo",
      stt"sei l'ultimo",
    )(
      gif"rphjb_FaiSchifoSeiUltimo.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      stt"lui si chiamava",
      "\\badolf\\b".r.tr(5),
      stt"hitler",
      stt"belle arti",
      "hitl[aà]".r.tr(5),
    )(
      gif"rphjb_AdolfHitler.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      "limoncell[io]".r.tr(5)
    )(
      gif"rphjb_LimoncelliVino.gif"
    ),
  )

}
