package com.benkio.richardphjbensonbot.data

import com.benkio.telegrambotinfrastructure.model.reply.gif
import com.benkio.telegrambotinfrastructure.model.reply.ReplyBundleMessage
import com.benkio.telegrambotinfrastructure.model.tr

object Gif {

  def messageRepliesGifData: List[ReplyBundleMessage] = List(
    ReplyBundleMessage.textToGif(
      "gli autori"
    )(
      gif"rphjb_AutoriGif.mp4"
    ),
    ReplyBundleMessage.textToGif(
      "che siamo noi",
      "pezzi di merda"
    )(
      gif"rphjb_PezziDiMerdaGif.mp4"
    ),
    ReplyBundleMessage.textToGif(
      "stato brado"
    )(
      gif"rphjb_StatoBradoGif.mp4"
    ),
    ReplyBundleMessage.textToGif(
      "siamo qua"
    )(
      gif"rphjb_SiamoQuaGif.mp4"
    ),
    ReplyBundleMessage.textToGif(
      "che c'hai"
    )(
      gif"rphjb_CheCHaiGif.mp4"
    ),
    ReplyBundleMessage.textToGif(
      "abbiamo vinto"
    )(
      gif"rphjb_AbbiamoVintoGif.mp4"
    ),
    ReplyBundleMessage.textToGif(
      "falsità"
    )(
      gif"rphjb_TelefonataPilotataGif.mp4"
    ),
    ReplyBundleMessage.textToGif(
      "come ha fatto a entr(à|are)".r.tr(21)
    )(
      gif"rphjb_ComeHaFattoAEntrareGif.mp4"
    ),
    ReplyBundleMessage.textToGif(
      ";)",
      "occhiolino",
      "wink",
      "😉"
    )(
      gif"rphjb_OcchiolinoGif.mp4"
    ),
    ReplyBundleMessage.textToGif(
      "soffro"
    )(
      gif"rphjb_SoffroGif.mp4"
    ),
    ReplyBundleMessage.textToGif(
      "indispettirmi",
      "oltrepassare",
      "divento cattivo"
    )(
      gif"rphjb_IndispettirmiGif.mp4"
    ),
    ReplyBundleMessage.textToGif(
      "mannaggia",
      "la salute"
    )(
      gif"rphjb_MannaggiaLaSaluteGif.mp4"
    ),
    ReplyBundleMessage.textToGif(
      "mi rompi il cazzo",
      "mi dai fastidio"
    )(
      gif"rphjb_MiRompiErCazzoGif.mp4"
    ),
    ReplyBundleMessage.textToGif(
      "mi diverti",
      "mi sono divertito"
    )(
      gif"rphjb_DivertiGif.mp4"
    ),
    ReplyBundleMessage.textToGif(
      "uno scherzo"
    )(
      gif"rphjb_ScherzoGif.mp4"
    ),
    ReplyBundleMessage.textToGif(
      "(\\b|^)e levati((\\.\\.\\.|\\.\\.|\\.| )dai coglioni)?".r.tr(8),
      "fuori(\\.\\.\\.|\\.\\.|\\.| )dai coglioni".r.tr(18)
    )(
      gif"rphjb_LevatiDaiCoglioniGif.mp4"
    ),
    ReplyBundleMessage.textToGif(
      "più co(gl|j)ione".r,
      "dice co(gl|j)ione".r
    )(
      gif"rphjb_CoglioneGif.mp4",
      gif"rphjb_PiuCoglioneGif.mp4",
      gif"rphjb_CoglioneGif.mp4"
    ),
    ReplyBundleMessage.textToGif(
      "bravo!!!",
      "bravooo"
    )(
      gif"rphjb_BravoGif.mp4"
    ),
    ReplyBundleMessage.textToGif(
      "porca miseria",
      "facendo incazzare"
    )(
      gif"rphjb_PorcaMiseriaGif.mp4"
    ),
    ReplyBundleMessage.textToGif(
      "serie z"
    )(
      gif"rphjb_CantantiSerieZGif.mp4"
    ),
    ReplyBundleMessage.textToGif(
      "[gq]uerelare".r
    )(
      gif"rphjb_QuerelareGif.mp4"
    ),
    ReplyBundleMessage.textToGif(
      "chi cazzo sei"
    )(
      gif"rphjb_ChiCazzoSeiGif.mp4"
    ),
    ReplyBundleMessage.textToGif(
      "(è|diventa) vecchi[ao]".r
    )(
      gif"rphjb_VecchioGif.mp4"
    ),
    ReplyBundleMessage.textToGif(
      "scatta qualcosa",
      "proprio in quel momento",
      "nel suo cervello"
    )(
      gif"rphjb_ScattaQualcosaGif.mp4"
    ),
    ReplyBundleMessage.textToGif(
      "pure bona"
    )(
      gif"rphjb_BonaGif.mp4"
    ),
    ReplyBundleMessage.textToGif(
      "abbia mai sentito"
    )(
      gif"rphjb_SquallidaGif.mp4"
    ),
    ReplyBundleMessage.textToGif(
      "a quel punto"
    )(
      gif"rphjb_QuelPuntoGif.mp4"
    ),
    ReplyBundleMessage.textToGif(
      "il senso"
    )(
      gif"rphjb_IlSensoCapitoGif.mp4"
    ),
    ReplyBundleMessage.textToGif(
      "abi[dt]ua[dt]o".r
    )(
      gif"rphjb_PropriollaGif.mp4"
    ),
    ReplyBundleMessage.textToGif(
      "non vedo questo grande problema"
    )(
      gif"rphjb_VabbeProblemaGif.mp4"
    ),
    ReplyBundleMessage.textToGif(
      "il bongo",
      "non esiste un basso più pontente al mondo",
      "music man"
    )(
      gif"rphjb_IlBongoGif.mp4"
    ),
    ReplyBundleMessage.textToGif(
      "immagini ama[dt]oriali".r.tr(19)
    )(
      gif"rphjb_InternetGif.mp4"
    ),
    ReplyBundleMessage.textToGif(
      "prendere quello l[aà]".r
    )(
      gif"rphjb_AaaPrendereQuelloLaGif.mp4"
    ),
    ReplyBundleMessage.textToGif(
      "col cazzo",
      "non so suon[aà](re)?".r
    )(
      gif"rphjb_ColCazzoSuonaGif.mp4"
    ),
    ReplyBundleMessage.textToGif(
      "🤨",
      "🧐",
      "sono confuso",
      "\\?\\?[\\?]+".r
    )(
      gif"rphjb_ConfusoGif.mp4"
    ),
    ReplyBundleMessage.textToGif(
      "😑",
      "😒",
      "🫤",
      "\\bwtf[!]*\\b".r
    )(
      gif"rphjb_WTFGif.mp4",
      gif"rphjb_WTF2Gif.mp4",
      gif"rphjb_WTF3Gif.mp4",
      gif"rphjb_WTF4Gif.mp4"
    ),
    ReplyBundleMessage.textToGif(
      "\\bsorpresa\\b".r,
      "\\bshock\\b".r,
      "😮"
    )(
      gif"rphjb_SorpresaGif.mp4",
      gif"rphjb_Sorpresa2Gif.mp4"
    ),
    ReplyBundleMessage.textToGif(
      "uu[u]+".r
    )(
      gif"rphjb_UuuGif.mp4"
    ),
    ReplyBundleMessage.textToGif(
      "deal with it"
    )(
      gif"rphjb_OcchialiGif.mp4"
    ),
    ReplyBundleMessage.textToGif(
      "così e basta",
      "bisogna accettare"
    )(
      gif"rphjb_VeritaGif.mp4"
    )
  )

}
