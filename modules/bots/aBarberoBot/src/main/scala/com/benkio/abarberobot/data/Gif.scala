package com.benkio.abarberobot.data

import cats.Applicative
import com.benkio.telegrambotinfrastructure.model.reply.gif
import com.benkio.telegrambotinfrastructure.model.reply.ReplyBundleMessage
import com.benkio.telegrambotinfrastructure.model.tr

object Gif:

  def messageRepliesGifData[F[_]: Applicative]: List[ReplyBundleMessage[F]] = List(
    ReplyBundleMessage.textToGif[F](
      "ha ragione"
    )(
      gif"abar_HaRagione.gif"
    ),
    ReplyBundleMessage.textToGif[F](
      "seziona",
      "cadaveri",
      "morti"
    )(
      gif"abar_Cadaveri.gif"
    ),
    ReplyBundleMessage.textToGif[F](
      "strappa",
      "gli arti",
      "le braccia"
    )(
      gif"abar_Strappare.gif"
    ),
    ReplyBundleMessage.textToGif[F](
      "saltare la testa",
      "questa macchina"
    )(
      gif"abar_SaltareLaTesta.gif"
    ),
    ReplyBundleMessage.textToGif[F](
      "un po' paura"
    )(
      gif"abar_Paura.gif"
    ),
    ReplyBundleMessage.textToGif[F](
      "sega",
      "dov'è"
    )(
      gif"abar_Sega.gif"
    ),
    ReplyBundleMessage.textToGif[F](
      "potere",
      "incarichi",
      "poltrone",
      "appalti",
      "spartir"
    )(
      gif"abar_Potere.gif"
    ),
    ReplyBundleMessage.textToGif[F](
      "grandioso",
      "magnifico",
      "capolavoro"
    )(
      gif"abar_Capolavoro.gif"
    ),
    ReplyBundleMessage.textToGif[F](
      "troppo facile",
      "easy"
    )(
      gif"abar_TroppoFacile.gif"
    ),
    ReplyBundleMessage.textToGif[F](
      "chi(s| )se( )?ne( )?frega".r.tr
    )(
      gif"abar_Chissenefrega.gif"
    ),
    ReplyBundleMessage.textToGif[F](
      "buonasera"
    )(
      gif"abar_Buonasera.gif"
    ),
    ReplyBundleMessage.textToGif[F](
      " a morte",
      "\\bsi{3,}\\b".r.tr
    )(
      gif"abar_SisiAMorte.gif"
    ),
    ReplyBundleMessage.textToGif[F](
      "viva il popolo",
      "comunis"
    )(
      gif"abar_VivaIlPopolo.gif"
    ),
    ReplyBundleMessage.textToGif[F](
      "fare qualcosa"
    )(
      gif"abar_FareQualcosa.gif"
    ),
    ReplyBundleMessage.textToGif[F](
      "(no|nessun|non c'è) problem(a)?",
      "ammazziamo tutti"
    )(
      gif"abar_AmmazziamoTuttiNoProblem.gif"
    ),
    ReplyBundleMessage.textToGif[F](
      "\\bcert[o!]{3,}\\b".r.tr
    )(
      gif"abar_Certo.gif"
    ),
    ReplyBundleMessage.textToGif[F](
      "rogo"
    )(
      gif"abar_Rogo.gif"
    ),
    ReplyBundleMessage.textToGif[F](
      "semplific"
    )(
      gif"abar_Semplifico.gif"
    ),
    ReplyBundleMessage.textToGif[F](
      "bere il (suo )?sangue".r.tr,
      "taglia(re)? la gola".r.tr
    )(
      gif"abar_TaglioGolaBereSangue.gif"
    ),
    ReplyBundleMessage.textToGif[F](
      "spacco la (testa|faccia)".r.tr
    )(
      gif"abar_SpaccoLaTesta.gif"
    ),
    ReplyBundleMessage.textToGif[F](
      "dal (culo|sedere|fondo schiera|orifizio posteriore|dietro)".r.tr
    )(
      gif"abar_OrifizioPosteriore.gif"
    ),
    ReplyBundleMessage.textToGif[F](
      "faccia tosta",
      "furfante"
    )(
      gif"abar_Furfante.gif"
    ),
    ReplyBundleMessage.textToGif[F](
      "\\bbasta(a|!){2,}".r.tr
    )(
      gif"abar_Basta.gif"
    ),
    ReplyBundleMessage.textToGif[F](
      "tutti insieme",
      "ghigliottina"
    )(
      gif"abar_GhigliottinaTuttiInsieme.gif"
    ),
    ReplyBundleMessage.textToGif[F](
      "economisti"
    )(
      gif"abar_Economisti.gif"
    ),
    ReplyBundleMessage.textToGif[F](
      "vieni (un po' )?qui".r.tr
    )(
      gif"abar_VieniQui.gif"
    ),
    ReplyBundleMessage.textToGif[F](
      "si fa così"
    )(
      gif"abar_SiFaCosi.gif"
    ),
    ReplyBundleMessage.textToGif[F](
      "rapire",
      "riscatto"
    )(
      gif"abar_Riscatto.gif"
    ),
    ReplyBundleMessage.textToGif[F](
      "\\bn[o]+!\\b".r.tr,
      "non (lo )?vogli(a|o)".r.tr
    )(
      gif"abar_No.gif"
    ),
    ReplyBundleMessage.textToGif[F](
      "in un attimo",
      "in piazza"
    )(
      gif"abar_InPiazza.gif"
    ),
    ReplyBundleMessage.textToGif[F](
      "in due pezzi"
    )(
      gif"abar_InDuePezzi.gif"
    ),
    ReplyBundleMessage.textToGif[F](
      "\\bgiusto(o|!){2,}".r.tr
    )(
      gif"abar_Giusto.gif"
    ),
    ReplyBundleMessage.textToGif[F](
      "gli altri (che )?sono".r.tr
    )(
      gif"abar_GliAltri.gif"
    ),
    ReplyBundleMessage.textToGif[F](
      "👐",
      "🙌"
    )(
      gif"abar_AlzaLeManiGif.mp4"
    )
  )
end Gif
