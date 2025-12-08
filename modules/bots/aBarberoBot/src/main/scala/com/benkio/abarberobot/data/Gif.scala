package com.benkio.abarberobot.data

import com.benkio.telegrambotinfrastructure.model.reply.gif
import com.benkio.telegrambotinfrastructure.model.reply.ReplyBundleMessage
import com.benkio.telegrambotinfrastructure.model.tr

object Gif {

  def messageRepliesGifData: List[ReplyBundleMessage] = List(
    ReplyBundleMessage.textToGif(
      "ha ragione"
    )(
      gif"abar_HaRagioneGif.mp4"
    ),
    ReplyBundleMessage.textToGif(
      "seziona",
      "cadaveri",
      "morti"
    )(
      gif"abar_CadaveriGif.mp4"
    ),
    ReplyBundleMessage.textToGif(
      "strappa",
      "gli arti",
      "le braccia"
    )(
      gif"abar_StrappareGif.mp4"
    ),
    ReplyBundleMessage.textToGif(
      "saltare la testa",
      "questa macchina"
    )(
      gif"abar_SaltareLaTestaGif.mp4"
    ),
    ReplyBundleMessage.textToGif(
      "un po' paura"
    )(
      gif"abar_PauraGif.mp4"
    ),
    ReplyBundleMessage.textToGif(
      "sega",
      "dov'è"
    )(
      gif"abar_SegaGif.mp4"
    ),
    ReplyBundleMessage.textToGif(
      "potere",
      "incarichi",
      "poltrone",
      "appalti",
      "spartir"
    )(
      gif"abar_PotereGif.mp4"
    ),
    ReplyBundleMessage.textToGif(
      "grandioso",
      "magnifico",
      "capolavoro"
    )(
      gif"abar_CapolavoroGif.mp4"
    ),
    ReplyBundleMessage.textToGif(
      "troppo facile",
      "easy"
    )(
      gif"abar_TroppoFacileGif.mp4"
    ),
    ReplyBundleMessage.textToGif(
      "chi(s| )se( )?ne( )?frega".r.tr(13)
    )(
      gif"abar_ChissenefregaGif.mp4"
    ),
    ReplyBundleMessage.textToGif(
      "buonasera"
    )(
      gif"abar_BuonaseraGif.mp4"
    ),
    ReplyBundleMessage.textToGif(
      " a morte",
      "\\b(si[ ]?){3,}\\b".r.tr(6)
    )(
      gif"abar_SisiAMorteGif.mp4"
    ),
    ReplyBundleMessage.textToGif(
      "viva il popolo",
      "comunis"
    )(
      gif"abar_VivaIlPopoloGif.mp4"
    ),
    ReplyBundleMessage.textToGif(
      "fare qualcosa"
    )(
      gif"abar_FareQualcosaGif.mp4"
    ),
    ReplyBundleMessage.textToGif(
      "(no|nessun|non c'è) problem(a)?",
      "ammazziamo tutti"
    )(
      gif"abar_AmmazziamoTuttiNoProblemGif.mp4"
    ),
    ReplyBundleMessage.textToGif(
      "(\\b|^)certo[o]*!(\\b|$)".r.tr(6)
    )(
      gif"abar_CertoGif.mp4"
    ),
    ReplyBundleMessage.textToGif(
      "rogo"
    )(
      gif"abar_RogoGif.mp4"
    ),
    ReplyBundleMessage.textToGif(
      "semplific"
    )(
      gif"abar_SemplificoGif.mp4"
    ),
    ReplyBundleMessage.textToGif(
      "taglia(re)? la gola".r.tr(14)
    )(
      gif"abar_TaglioGolaBereSangueGif.mp4"
    ),
    ReplyBundleMessage.textToGif(
      "spacco la (testa|faccia)".r.tr(15)
    )(
      gif"abar_SpaccoLaTestaGif.mp4"
    ),
    ReplyBundleMessage.textToGif(
      "dal (culo|sedere|fondo schiera|orifizio posteriore|dietro)".r.tr(8)
    )(
      gif"abar_OrifizioPosterioreGif.mp4"
    ),
    ReplyBundleMessage.textToGif(
      "faccia tosta",
      "furfante"
    )(
      gif"abar_FurfanteGif.mp4"
    ),
    ReplyBundleMessage.textToGif(
      "\\bbasta[a]+!".r.tr(7)
    )(
      gif"abar_BastaGif.mp4"
    ),
    ReplyBundleMessage.textToGif(
      "tutti insieme",
      "ghigliottina"
    )(
      gif"abar_GhigliottinaTuttiInsiemeGif.mp4"
    ),
    ReplyBundleMessage.textToGif(
      "economisti"
    )(
      gif"abar_EconomistiGif.mp4"
    ),
    ReplyBundleMessage.textToGif(
      "vieni (un po' )?qui".r.tr(14)
    )(
      gif"abar_VieniQuiGif.mp4"
    ),
    ReplyBundleMessage.textToGif(
      "si fa così"
    )(
      gif"abar_SiFaCosiGif.mp4"
    ),
    ReplyBundleMessage.textToGif(
      "rapire",
      "riscatto"
    )(
      gif"abar_RiscattoGif.mp4"
    ),
    ReplyBundleMessage.textToGif(
      "(\\b|^)n[o]+!(\\b|$)".r.tr(3),
      "non (lo )?vogli(a|o)".r.tr(10)
    )(
      gif"abar_NoGif.mp4"
    ),
    ReplyBundleMessage.textToGif(
      "in un attimo",
      "in piazza"
    )(
      gif"abar_InPiazzaGif.mp4"
    ),
    ReplyBundleMessage.textToGif(
      "in due pezzi"
    )(
      gif"abar_InDuePezziGif.mp4"
    ),
    ReplyBundleMessage.textToGif(
      "\\bgiusto[o]+!".r.tr(8)
    )(
      gif"abar_GiustoGif.mp4"
    ),
    ReplyBundleMessage.textToGif(
      "gli altri (che )?sono".r
    )(
      gif"abar_GliAltriGif.mp4"
    ),
    ReplyBundleMessage.textToGif(
      "👐",
      "🙌"
    )(
      gif"abar_AlzaLeManiGif.mp4"
    )
  )
} // end Gif
