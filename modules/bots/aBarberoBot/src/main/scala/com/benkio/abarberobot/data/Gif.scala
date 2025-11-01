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
      gif"abar_HaRagioneGif.mp4"
    ),
    ReplyBundleMessage.textToGif[F](
      "seziona",
      "cadaveri",
      "morti"
    )(
      gif"abar_CadaveriGif.mp4"
    ),
    ReplyBundleMessage.textToGif[F](
      "strappa",
      "gli arti",
      "le braccia"
    )(
      gif"abar_StrappareGif.mp4"
    ),
    ReplyBundleMessage.textToGif[F](
      "saltare la testa",
      "questa macchina"
    )(
      gif"abar_SaltareLaTestaGif.mp4"
    ),
    ReplyBundleMessage.textToGif[F](
      "un po' paura"
    )(
      gif"abar_PauraGif.mp4"
    ),
    ReplyBundleMessage.textToGif[F](
      "sega",
      "dov'è"
    )(
      gif"abar_SegaGif.mp4"
    ),
    ReplyBundleMessage.textToGif[F](
      "potere",
      "incarichi",
      "poltrone",
      "appalti",
      "spartir"
    )(
      gif"abar_PotereGif.mp4"
    ),
    ReplyBundleMessage.textToGif[F](
      "grandioso",
      "magnifico",
      "capolavoro"
    )(
      gif"abar_CapolavoroGif.mp4"
    ),
    ReplyBundleMessage.textToGif[F](
      "troppo facile",
      "easy"
    )(
      gif"abar_TroppoFacileGif.mp4"
    ),
    ReplyBundleMessage.textToGif[F](
      "chi(s| )se( )?ne( )?frega".r.tr(13)
    )(
      gif"abar_ChissenefregaGif.mp4"
    ),
    ReplyBundleMessage.textToGif[F](
      "buonasera"
    )(
      gif"abar_BuonaseraGif.mp4"
    ),
    ReplyBundleMessage.textToGif[F](
      " a morte",
      "\\b(si[ ]?){3,}\\b".r.tr(6)
    )(
      gif"abar_SisiAMorteGif.mp4"
    ),
    ReplyBundleMessage.textToGif[F](
      "viva il popolo",
      "comunis"
    )(
      gif"abar_VivaIlPopoloGif.mp4"
    ),
    ReplyBundleMessage.textToGif[F](
      "fare qualcosa"
    )(
      gif"abar_FareQualcosaGif.mp4"
    ),
    ReplyBundleMessage.textToGif[F](
      "(no|nessun|non c'è) problem(a)?",
      "ammazziamo tutti"
    )(
      gif"abar_AmmazziamoTuttiNoProblemGif.mp4"
    ),
    ReplyBundleMessage.textToGif[F](
      "\\bcerto[o]*!\\b".r.tr(6)
    )(
      gif"abar_CertoGif.mp4"
    ),
    ReplyBundleMessage.textToGif[F](
      "rogo"
    )(
      gif"abar_RogoGif.mp4"
    ),
    ReplyBundleMessage.textToGif[F](
      "semplific"
    )(
      gif"abar_SemplificoGif.mp4"
    ),
    ReplyBundleMessage.textToGif[F](
      "bere il (suo )?sangue".r.tr(6),
      "taglia(re)? la gola".r.tr(14)
    )(
      gif"abar_TaglioGolaBereSangueGif.mp4"
    ),
    ReplyBundleMessage.textToGif[F](
      "spacco la (testa|faccia)".r.tr(15)
    )(
      gif"abar_SpaccoLaTestaGif.mp4"
    ),
    ReplyBundleMessage.textToGif[F](
      "dal (culo|sedere|fondo schiera|orifizio posteriore|dietro)".r.tr(8)
    )(
      gif"abar_OrifizioPosterioreGif.mp4"
    ),
    ReplyBundleMessage.textToGif[F](
      "faccia tosta",
      "furfante"
    )(
      gif"abar_FurfanteGif.mp4"
    ),
    ReplyBundleMessage.textToGif[F](
      "\\bbasta[a]+!".r.tr(7)
    )(
      gif"abar_BastaGif.mp4"
    ),
    ReplyBundleMessage.textToGif[F](
      "tutti insieme",
      "ghigliottina"
    )(
      gif"abar_GhigliottinaTuttiInsiemeGif.mp4"
    ),
    ReplyBundleMessage.textToGif[F](
      "economisti"
    )(
      gif"abar_EconomistiGif.mp4"
    ),
    ReplyBundleMessage.textToGif[F](
      "vieni (un po' )?qui".r.tr(14)
    )(
      gif"abar_VieniQuiGif.mp4"
    ),
    ReplyBundleMessage.textToGif[F](
      "si fa così"
    )(
      gif"abar_SiFaCosiGif.mp4"
    ),
    ReplyBundleMessage.textToGif[F](
      "rapire",
      "riscatto"
    )(
      gif"abar_RiscattoGif.mp4"
    ),
    ReplyBundleMessage.textToGif[F](
      "\\bn[o]+!\\b".r.tr(3),
      "non (lo )?vogli(a|o)".r.tr(10)
    )(
      gif"abar_NoGif.mp4"
    ),
    ReplyBundleMessage.textToGif[F](
      "in un attimo",
      "in piazza"
    )(
      gif"abar_InPiazzaGif.mp4"
    ),
    ReplyBundleMessage.textToGif[F](
      "in due pezzi"
    )(
      gif"abar_InDuePezziGif.mp4"
    ),
    ReplyBundleMessage.textToGif[F](
      "\\bgiusto[o]+!".r.tr(8)
    )(
      gif"abar_GiustoGif.mp4"
    ),
    ReplyBundleMessage.textToGif[F](
      "gli altri (che )?sono".r
    )(
      gif"abar_GliAltriGif.mp4"
    ),
    ReplyBundleMessage.textToGif[F](
      "👐",
      "🙌"
    )(
      gif"abar_AlzaLeManiGif.mp4"
    )
  )
end Gif
