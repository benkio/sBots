package com.benkio.abarberobot.data

import cats.Applicative
import com.benkio.telegrambotinfrastructure.model.reply.gif
import com.benkio.telegrambotinfrastructure.model.reply.mp3
import com.benkio.telegrambotinfrastructure.model.reply.vid
import com.benkio.telegrambotinfrastructure.model.reply.ReplyBundleMessage
import com.benkio.telegrambotinfrastructure.model.tr
object Mix:

  def messageRepliesMixData[F[_]: Applicative]: List[ReplyBundleMessage[F]] = List(
    ReplyBundleMessage.textToMedia[F](
      "\\bfrancesi\\b".r.tr(8)
    )(
      gif"abar_Francesi.gif",
      mp3"abar_Luigi14.mp3"
    ),
    ReplyBundleMessage.textToMedia[F](
      "(figlio|fijo) (di|de) (mignotta|puttana|troia)".r.tr(13)
    )(
      gif"abar_FiglioDi.gif",
      gif"abar_FiglioDi2.gif",
      mp3"abar_FiglioDi3.mp3"
    ),
    ReplyBundleMessage.textToMedia[F](
      "sgozza"
    )(
      mp3"abar_Sgozzamento.mp3",
      gif"abar_Sgozzamento.gif"
    ),
    ReplyBundleMessage.textToMedia[F](
      "bruciargli",
      "la casa"
    )(
      mp3"abar_Bruciare.mp3",
      gif"abar_Bruciare.gif"
    ),
    ReplyBundleMessage.textToMedia[F](
      "a pezzi",
      "a pezzettini"
    )(
      mp3"abar_APezzettini.mp3",
      gif"abar_APezzettini.gif"
    ),
    ReplyBundleMessage.textToMedia[F](
      "allarme",
      "priori"
    )(
      mp3"abar_Priori.mp3",
      gif"abar_Priori.gif"
    ),
    ReplyBundleMessage.textToMedia[F](
      "carne"
    )(
      mp3"abar_Bbq.mp3",
      mp3"abar_Priori.mp3",
      gif"abar_Priori.gif"
    ),
    ReplyBundleMessage.textToMedia[F](
      "zagaglia",
      "nemico"
    )(
      mp3"abar_Zagaglia.mp3",
      gif"abar_Zagaglia.gif"
    ),
    ReplyBundleMessage.textToMedia[F](
      "guerra"
    )(
      vid"abar_ParoleLongobarde.mp4",
      mp3"abar_Guerra.mp3"
    ),
    ReplyBundleMessage.textToMedia[F](
      "faida"
    )(
      vid"abar_ParoleLongobarde.mp4",
      gif"abar_Faida.gif"
    ),
    ReplyBundleMessage.textToMedia[F](
      "spranga"
    )(
      gif"abar_Spranga.gif",
      vid"abar_ParoleLongobarde.mp4",
      mp3"abar_Spranga.mp3"
    ),
    ReplyBundleMessage.textToMedia[F](
      "trappola"
    )(
      gif"abar_Trappola.gif",
      vid"abar_ParoleLongobarde.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      "pistola",
      "mitragliatrice",
      "fucile da caccia",
      "calibro",
      "beretta",
      "salame",
      "mortadella",
      "provolone",
      "marmellata",
      "burro",
      "(dadi|pomodori) star".r.tr(9),
      "valigett[ae] 24[ ]?ore".r.tr(15),
      "giubbotto anti[ ]?proiettile".r.tr(24),
      "libri (gialli|fantascienza)".r.tr(12),
      "fumetti",
      "charlie brown",
      "documenti d'identità",
      "targhe di auto (rubate)?".r.tr(15),
      "timbri",
      "(divise|palette) della polizia".r.tr(14),
      "pacchetti di sigarette",
      "piselli de rica",
      "fagioli cirio",
      "pasta (buitoni|barilla|corta)".r.tr(11),
      "spaghetti"
    )(
      vid"abar_ListaSpesaPartigiani.mp4",
      mp3"abar_ListaSpesaPartigiani.mp3"
    ),
    ReplyBundleMessage.textToMedia[F](
      "tonnellate",
      "zirconio",
      "carbone",
      "acciaio",
      "oli minerali",
      "legname",
      "\\brame\\b".r.tr(4),
      "nitrato di sodio",
      "sali potassici",
      "\\bgomma\\b".r.tr(5),
      "toluolo",
      "trementina",
      "piombo",
      "stagno",
      "nichelio",
      "molibdeno",
      "tungsteno",
      "titanio"
    )(
      mp3"abar_ListaMolibdeno.mp3",
      vid"abar_ListaMolibdeno.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      "error[ie]".r.tr(6),
      "pernicios[oi]".r.tr(10),
      "scandalos[oi]".r.tr(10),
      "penstilenzial[ie]".r.tr(14),
      "velenosis[s]+imo".r.tr(13)
    )(
      vid"abar_ErrorePestilenzialeVelenosissimo.mp4",
      mp3"abar_ErrorePestilenzialeVelenosissimo.mp3"
    )
  )
end Mix
