package com.benkio.richardphjbensonbot.data

import cats.Applicative
import com.benkio.telegrambotinfrastructure.model.reply.gif
import com.benkio.telegrambotinfrastructure.model.reply.ReplyBundleMessage
import com.benkio.telegrambotinfrastructure.model.tr

object Gif {

  def messageRepliesGifData[F[_]: Applicative]: List[ReplyBundleMessage[F]] = List(
    ReplyBundleMessage.textToGif[F](
      "gli autori"
    )(
      gif"rphjb_AutoriGif.mp4"
    ),
    ReplyBundleMessage.textToGif[F](
      "che siamo noi",
      "pezzi di merda"
    )(
      gif"rphjb_PezziDiMerdaGif.mp4"
    ),
    ReplyBundleMessage.textToGif[F](
      "stato brado"
    )(
      gif"rphjb_StatoBradoGif.mp4"
    ),
    ReplyBundleMessage.textToGif[F](
      "siamo qua"
    )(
      gif"rphjb_SiamoQuaGif.mp4"
    ),
    ReplyBundleMessage.textToGif[F](
      "che c'hai"
    )(
      gif"rphjb_CheCHaiGif.mp4"
    ),
    ReplyBundleMessage.textToGif[F](
      "abbiamo vinto"
    )(
      gif"rphjb_AbbiamoVintoGif.mp4"
    ),
    ReplyBundleMessage.textToGif[F](
      "falsità"
    )(
      gif"rphjb_TelefonataPilotataGif.mp4"
    ),
    ReplyBundleMessage.textToGif[F](
      "come ha fatto a entr(à|are)".r.tr(21)
    )(
      gif"rphjb_ComeHaFattoAEntrareGif.mp4"
    ),
    ReplyBundleMessage.textToGif[F](
      ";)",
      "occhiolino",
      "wink",
      "😉"
    )(
      gif"rphjb_OcchiolinoGif.mp4"
    ),
    ReplyBundleMessage.textToGif[F](
      "soffro"
    )(
      gif"rphjb_SoffroGif.mp4"
    ),
    ReplyBundleMessage.textToGif[F](
      "indispettirmi",
      "oltrepassare",
      "divento cattivo"
    )(
      gif"rphjb_IndispettirmiGif.mp4"
    ),
    ReplyBundleMessage.textToGif[F](
      "mannaggia",
      "la salute"
    )(
      gif"rphjb_MannaggiaLaSaluteGif.mp4"
    ),
    ReplyBundleMessage.textToGif[F](
      "mi rompi il cazzo",
      "mi dai fastidio"
    )(
      gif"rphjb_MiRompiErCazzoGif.mp4"
    ),
    ReplyBundleMessage.textToGif[F](
      "mi diverti",
      "mi sono divertito"
    )(
      gif"rphjb_DivertiGif.mp4"
    ),
    ReplyBundleMessage.textToGif[F](
      "uno scherzo"
    )(
      gif"rphjb_ScherzoGif.mp4"
    ),
    ReplyBundleMessage.textToGif[F](
      " e levati((...|..|.| )dai coglioni)?".r.tr(9),
      "fuori(...|..|.| )dai coglioni".r.tr(18)
    )(
      gif"rphjb_LevatiDaiCoglioniGif.mp4"
    ),
    ReplyBundleMessage.textToGif[F](
      "più co(gl|j)ione".r,
      "dice co(gl|j)ione".r
    )(
      gif"rphjb_CoglioneGif.mp4",
      gif"rphjb_PiuCoglioneGif.mp4",
      gif"rphjb_CoglioneGif.mp4"
    ),
    ReplyBundleMessage.textToGif[F](
      "bravo!!!",
      "bravooo"
    )(
      gif"rphjb_BravoGif.mp4"
    ),
    ReplyBundleMessage.textToGif[F](
      "porca miseria",
      "facendo incazzare"
    )(
      gif"rphjb_PorcaMiseriaGif.mp4"
    ),
    ReplyBundleMessage.textToGif[F](
      "serie z"
    )(
      gif"rphjb_CantantiSerieZGif.mp4"
    ),
    ReplyBundleMessage.textToGif[F](
      "[gq]uerelare".r
    )(
      gif"rphjb_QuerelareGif.mp4"
    ),
    ReplyBundleMessage.textToGif[F](
      "chi cazzo sei"
    )(
      gif"rphjb_ChiCazzoSeiGif.mp4"
    ),
    ReplyBundleMessage.textToGif[F](
      "(è|diventa) vecchi[ao]".r
    )(
      gif"rphjb_VecchioGif.mp4"
    ),
    ReplyBundleMessage.textToGif[F](
      "scatta qualcosa",
      "proprio in quel momento",
      "nel suo cervello"
    )(
      gif"rphjb_ScattaQualcosaGif.mp4"
    ),
    ReplyBundleMessage.textToGif[F](
      "pure bona"
    )(
      gif"rphjb_BonaGif.mp4"
    ),
    ReplyBundleMessage.textToGif[F](
      "abbia mai sentito"
    )(
      gif"rphjb_SquallidaGif.mp4"
    ),
    ReplyBundleMessage.textToGif[F](
      "a quel punto"
    )(
      gif"rphjb_QuelPuntoGif.mp4"
    ),
    ReplyBundleMessage.textToGif[F](
      "il senso"
    )(
      gif"rphjb_IlSensoCapitoGif.mp4"
    ),
    ReplyBundleMessage.textToGif[F](
      "abi[dt]ua[dt]o".r
    )(
      gif"rphjb_PropriollaGif.mp4"
    ),
    ReplyBundleMessage.textToGif[F](
      "non vedo questo grande problema"
    )(
      gif"rphjb_VabbeProblemaGif.mp4"
    ),
    ReplyBundleMessage.textToGif[F](
      "il bongo",
      "non esiste un basso più pontente al mondo",
      "music man"
    )(
      gif"rphjb_IlBongoGif.mp4"
    ),
    ReplyBundleMessage.textToGif[F](
      "immagini ama[dt]oriali".r.tr(19)
    )(
      gif"rphjb_InternetGif.mp4"
    ),
    ReplyBundleMessage.textToGif[F](
      "prendere quello l[aà]".r
    )(
      gif"rphjb_AaaPrendereQuelloLaGif.mp4"
    ),
    ReplyBundleMessage.textToGif[F](
      "col cazzo",
      "non so suon[aà](re)?".r
    )(
      gif"rphjb_ColCazzoSuonaGif.mp4"
    ),
    ReplyBundleMessage.textToGif[F](
      "🤨",
      "🧐",
      "sono confuso",
      "\\?\\?[\\?]+".r
    )(
      gif"rphjb_ConfusoGif.mp4"
    ),
    ReplyBundleMessage.textToGif[F](
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
    ReplyBundleMessage.textToGif[F](
      "\\bsorpresa\\b".r,
      "\\bshock\\b".r,
      "😮"
    )(
      gif"rphjb_SorpresaGif.mp4",
      gif"rphjb_Sorpresa2Gif.mp4"
    ),
    ReplyBundleMessage.textToGif[F](
      "uu[u]+".r
    )(
      gif"rphjb_UuuGif.mp4"
    ),
    ReplyBundleMessage.textToGif[F](
      "deal with it"
    )(
      gif"rphjb_OcchialiGif.mp4"
    ),
    ReplyBundleMessage.textToGif[F](
      "così e basta",
      "bisogna accettare"
    )(
      gif"rphjb_VeritaGif.mp4"
    )
  )

}
