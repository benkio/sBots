package com.benkio.richardphjbensonbot.data

import cats.Applicative
import com.benkio.telegrambotinfrastructure.model.*

object Gif {

  def messageRepliesGifData[F[_]: Applicative]: List[ReplyBundleMessage[F]] = List(
    ReplyBundleMessage.textToGif[F](
      "gli autori"
    )(
      gif"rphjb_Autori.mp4"
    ),
    ReplyBundleMessage.textToGif[F](
      "pelle d'oca",
      "sussult",
      "brivid"
    )(
      gif"rphjb_Brivido.mp4"
    ),
    ReplyBundleMessage.textToGif[F](
      "che siamo noi",
      "pezzi di merda",
    )(
      gif"rphjb_PezziDiMerda.mp4"
    ),
    ReplyBundleMessage.textToGif[F](
      "rispondere"
    )(
      gif"rphjb_Rispondere.mp4"
    ),
    ReplyBundleMessage.textToGif[F](
      "baci",
      "limonare"
    )(
      gif"rphjb_Bacio.mp4"
    ),
    ReplyBundleMessage.textToGif[F](
      "stato brado"
    )(
      gif"rphjb_StatoBrado.mp4"
    ),
    ReplyBundleMessage.textToGif[F](
      "siamo qua"
    )(
      gif"rphjb_SiamoQua.mp4"
    ),
    ReplyBundleMessage.textToGif[F](
      "cucciolo"
    )(
      gif"rphjb_Cucciolo.mp4",
      gif"rphjb_Cucciolo2.mp4",
    ),
    ReplyBundleMessage.textToGif[F](
      "Che c'hai"
    )(
      gif"rphjb_CheCHai.mp4"
    ),
    ReplyBundleMessage.textToGif[F](
      "abbiamo vinto"
    )(
      gif"rphjb_AbbiamoVinto.mp4"
    ),
    ReplyBundleMessage.textToGif[F](
      "falsità"
    )(
      gif"rphjb_TelefonataPilotata.mp4"
    ),
    ReplyBundleMessage.textToGif[F](
      "come ha fatto a entr(à|are)".r.tr(23)
    )(
      gif"rphjb_ComeHaFattoAEntrare.mp4"
    ),
    ReplyBundleMessage.textToGif[F](
      "donna cane"
    )(
      gif"rphjb_DonnaCane.mp4"
    ),
    ReplyBundleMessage.textToGif[F](
      "menzion"
    )(
      gif"rphjb_NonMiMenzionareQuestaParola.mp4"
    ),
    ReplyBundleMessage.textToGif[F](
      "piano superiore",
      "si compete",
      "gerarca"
    )(
      gif"rphjb_PianoSuperioreCompete.mp4"
    ),
    ReplyBundleMessage.textToGif[F](
      "chi è"
    )(
      gif"rphjb_QuestaPersonaScusate.mp4"
    ),
    ReplyBundleMessage.textToGif[F](
      ";)",
      "occhiolino",
      "wink",
      "😉"
    )(
      gif"rphjb_Occhiolino.mp4"
    ),
    ReplyBundleMessage.textToGif[F](
      "soffro"
    )(
      gif"rphjb_Soffro.mp4"
    ),
    ReplyBundleMessage.textToGif[F](
      "indispettirmi",
      "oltrepassare",
      "divento cattivo"
    )(
      gif"rphjb_Indispettirmi.mp4"
    ),
    ReplyBundleMessage.textToGif[F](
      "mannaggia",
      "la salute"
    )(
      gif"rphjb_MannaggiaLaSalute.mp4"
    ),
    ReplyBundleMessage.textToGif[F](
      "mi rompi il cazzo",
      "mi dai fastidio"
    )(
      gif"rphjb_MiRompiErCazzo.mp4"
    ),
    ReplyBundleMessage.textToGif[F](
      "c'ha [pure ]?ragione",
      "o no?"
    )(
      gif"rphjb_Ragione.mp4"
    ),
    ReplyBundleMessage.textToGif[F](
      "mi diverti",
      "mi sono divertito",
    )(
      gif"rphjb_Diverti.mp4"
    ),
    ReplyBundleMessage.textToGif[F](
      "uno scherzo"
    )(
      gif"rphjb_Scherzo.mp4"
    ),
    ReplyBundleMessage.textToGif[F](
      "che si deve fà",
      "campà",
    )(
      gif"rphjb_Campa.mp4"
    ),
    ReplyBundleMessage.textToGif[F](
      "e levati((...|..|.| )dai coglioni)?".r.tr(8),
      "fuori(...|..|.| )dai coglioni".r.tr(18)
    )(
      gif"rphjb_LevatiDaiCoglioni.mp4"
    ),
    ReplyBundleMessage.textToGif[F](
      "più co(gl|j)ione".r.tr(11),
      "dice co(gl|j)ione".r.tr(12),
      "co(gl|j)ion([e]{3,}|e[!]{3,})".r.tr(9)
    )(
      gif"rphjb_CoglioneGif.mp4",
      gif"rphjb_PiuCoglione.mp4",
      gif"rphjb_Coglione.mp4"
    ),
    ReplyBundleMessage.textToGif[F](
      "bravo!!!",
      "bravooo"
    )(
      gif"rphjb_Bravo.mp4"
    ),
    ReplyBundleMessage.textToGif[F](
      "\\bmetal\\b".r.tr(5)
    )(
      gif"rphjb_Metal.mp4",
      gif"rphjb_IlMartel.mp4"
    ),
    ReplyBundleMessage.textToGif[F](
      "allucinante"
    )(
      gif"rphjb_Allucinante.mp4"
    ),
    ReplyBundleMessage.textToGif[F](
      "porca miseria",
      "facendo incazzare"
    )(
      gif"rphjb_PorcaMiseria.mp4"
    ),
    ReplyBundleMessage.textToGif[F](
      "dovete soffrire",
      "vi voglio far(e)? soffrire".r.tr(23),
    )(
      gif"rphjb_DoveteSoffrire.mp4"
    ),
    ReplyBundleMessage.textToGif[F](
      "serie z"
    )(
      gif"rphjb_CantantiSerieZ.mp4"
    ),
    ReplyBundleMessage.textToGif[F](
      "[gq]uerelare".r.tr(9),
    )(
      gif"rphjb_Querelare.mp4"
    ),
    ReplyBundleMessage.textToGif[F](
      "andati al cesso",
      "diecimila volte"
    )(
      gif"rphjb_Alcesso.mp4"
    ),
    ReplyBundleMessage.textToGif[F](
      "in fila"
    )(
      gif"rphjb_MettitiInFila.mp4"
    ),
    ReplyBundleMessage.textToGif[F](
      "non male"
    )(
      gif"rphjb_NonMale.mp4"
    ),
    ReplyBundleMessage.textToGif[F](
      "perchè si sente"
    )(
      gif"rphjb_SiSente.mp4"
    ),
    ReplyBundleMessage.textToGif[F](
      "chi cazzo sei"
    )(
      gif"rphjb_ChiCazzoSei.mp4"
    ),
    ReplyBundleMessage.textToGif[F](
      "(è|diventa) vecchi[ao]".r.tr(9),
    )(
      gif"rphjb_Vecchio.mp4"
    ),
    ReplyBundleMessage.textToGif[F](
      "scatta qualcosa",
      "proprio in quel momento",
      "nel suo cervello",
    )(
      gif"rphjb_ScattaQualcosa.mp4"
    ),
    ReplyBundleMessage.textToGif[F](
      "pure bona"
    )(
      gif"rphjb_Bona.mp4"
    ),
    ReplyBundleMessage.textToGif[F](
      "sei [gc]ambiat[oa]".r.tr(12)
    )(
      gif"rphjb_SeiCambiata.mp4"
    ),
    ReplyBundleMessage.textToGif[F](
      "mio discapito",
      "disgabido"
    )(
      gif"rphjb_Discapito.mp4"
    ),
    ReplyBundleMessage.textToGif[F](
      "abbia mai sentito"
    )(
      gif"rphjb_Squallida.mp4"
    ),
    ReplyBundleMessage.textToGif[F](
      "a quel punto"
    )(
      gif"rphjb_QuelPunto.mp4"
    ),
    ReplyBundleMessage.textToGif[F](
      "il senso"
    )(
      gif"rphjb_IlSensoCapito.mp4"
    ),
    ReplyBundleMessage.textToGif[F](
      "\\bester\\b".r.tr(5)
    )(
      gif"rphjb_Ester.mp4",
      gif"rphjb_Ester2.mp4",
    ),
    ReplyBundleMessage.textToGif[F](
      "abi(t|d)ua(t|d)o".r.tr(8),
      "proprioll(a|à)".r.tr(10),
    )(
      gif"rphjb_Propriolla.mp4"
    ),
    ReplyBundleMessage.textToGif[F](
      "non vedo questo grande problema"
    )(
      gif"rphjb_VabbeProblema.mp4",
    ),
    ReplyBundleMessage.textToGif[F](
      "il bongo",
      "non esiste un basso più pontente al mondo",
      "music man"
    )(
      gif"rphjb_IlBongo.mp4"
    ),
    ReplyBundleMessage.textToGif[F](
      "inserirlo su internet",
      "immagini ama(d|t)oriali".r.tr(19)
    )(
      gif"rphjb_Internet.mp4"
    ),
    ReplyBundleMessage.textToGif[F](
      "prendere quello l[aà]".r.tr(18),
    )(
      gif"rphjb_AaaPrendereQuelloLa.mp4"
    ),
    ReplyBundleMessage.textToGif[F](
      "col cazzo che non so suonà"
    )(
      gif"rphjb_ColCazzoSuona.mp4"
    ),
    ReplyBundleMessage.textToGif[F](
      "🤨",
      "🧐",
      "sono confuso",
      "[?]{3,}".r.tr(3),
    )(
      gif"rphjb_Confuso.mp4"
    ),
    ReplyBundleMessage.textToGif[F](
      "😑",
      "😒",
      "🫤",
      "\\bwtf[!]*\\b".r.tr(3),
    )(
      gif"rphjb_WTF.mp4",
      gif"rphjb_WTF2.mp4",
      gif"rphjb_WTF3.mp4",
      gif"rphjb_WTF4.mp4",
    ),
    ReplyBundleMessage.textToGif[F](
      "\\bsorpresa\\b".r.tr(8),
      "\\bshock\\b".r.tr(5),
      "😮",
    )(
      gif"rphjb_Sorpresa.mp4",
      gif"rphjb_Sorpresa2.mp4"
    ),
    ReplyBundleMessage.textToGif[F](
      "[u]{3,}".r.tr(3)
    )(
      gif"rphjb_Uuu.mp4"
    ),
    ReplyBundleMessage.textToGif[F](
      "☝️",
      "👆",
      "👉",
      "👇",
      "👈"
    )(
      gif"rphjb_Indicare.mp4"
    ),
    ReplyBundleMessage.textToGif[F](
      "deal with it"
    )(
      gif"rphjb_Occhiali.mp4"
    ),
    ReplyBundleMessage.textToGif[F](
      "lui si chiamava",
      "\\badolf\\b".r.tr(5),
      "hitler",
      "belle arti",
      "hitl[aà]".r.tr(5),
    )(
      gif"rphjb_AdolfHitler.mp4"
    ),
    ReplyBundleMessage.textToGif[F](
      "limoncell[io]".r.tr(5)
    )(
      gif"rphjb_LimoncelliVino.gif"
    ),
    ReplyBundleMessage.textToGif[F](
      "mic drop",
      "microfono",
      "🎤",
      "🎙",
    )(
      gif"rphjb_MicDrop.mp4"
    )
  )

}
