package com.benkio.abarberobot.data

import cats.Applicative
import com.benkio.telegrambotinfrastructure.model.reply.mp3
import com.benkio.telegrambotinfrastructure.model.reply.ReplyBundleMessage
import com.benkio.telegrambotinfrastructure.model.tr

object Audio:

  def messageRepliesAudioData[F[_]: Applicative]: List[ReplyBundleMessage[F]] = List(
    ReplyBundleMessage.textToMp3[F](
      "fuoco"
    )(
      mp3"abar_Bbq.mp3",
      mp3"abar_FerroFuocoAcquaBollenteAceto.mp3"
    ),
    ReplyBundleMessage.textToMp3[F](
      "luigi (14|quattordicesimo)".r.tr(8),
      "louis[- ]le[- ]grand".r.tr(14)
    )(
      mp3"abar_Luigi14.mp3"
    ),
    ReplyBundleMessage.textToMp3[F](
      "tedesco"
    )(
      mp3"abar_Kraft.mp3",
      mp3"abar_VonHohenheim.mp3",
      mp3"abar_Haushofer.mp3"
    ),
    ReplyBundleMessage.textToMp3[F](
      "genitali",
      "cosi e coglioni"
    )(
      mp3"abar_Cosi.mp3",
      mp3"abar_Sottaceto.mp3"
    ),
    ReplyBundleMessage.textToMp3[F](
      "kimono"
    )(
      mp3"abar_KimonoMaledetto.mp3",
      mp3"abar_KimonoStregato.mp3"
    ),
    ReplyBundleMessage.textToMp3[F](
      "particelle cadaveriche"
    )(
      mp3"abar_ParticelleCadaveriche.mp3"
    ),
    ReplyBundleMessage.textToMp3[F](
      "avrebbe (mai )?immaginato".r.tr(18)
    )(
      mp3"abar_NessunoAvrebbeImmaginato.mp3"
    ),
    ReplyBundleMessage.textToMp3[F](
      "mortalit[aà]".r.tr(9)
    )(
      mp3"abar_Mortalita.mp3"
    ),
    ReplyBundleMessage.textToMp3[F](
      "carta",
      "legno leggero"
    )(
      mp3"abar_LegnoLeggeroCarta.mp3"
    ),
    ReplyBundleMessage.textToMp3[F](
      "stregato"
    )(
      mp3"abar_KimonoStregato.mp3"
    ),
    ReplyBundleMessage.textToMp3[F](
      "maledetto"
    )(
      mp3"abar_Pestifero.mp3",
      mp3"abar_KimonoMaledetto.mp3"
    ),
    ReplyBundleMessage.textToMp3[F](
      "pestifero"
    )(
      mp3"abar_Pestifero.mp3"
    ),
    ReplyBundleMessage.textToMp3[F](
      "distrutto",
      "mangiato dai topi",
      "bruciato",
      "sepolto",
      "nel fiume",
      "innondazione"
    )(
      mp3"abar_Distrutto.mp3"
    ),
    ReplyBundleMessage.textToMp3[F](
      "\\bpratica\\b".r.tr(7)
    )(
      mp3"abar_PraticaPocoPatriotticah.mp3"
    ),
    ReplyBundleMessage.textToMp3[F](
      "\\bferro\\b".r.tr(5),
      "acqua bollente",
      "aceto"
    )(
      mp3"abar_FerroFuocoAcquaBollenteAceto.mp3"
    ),
    ReplyBundleMessage.textToMp3[F](
      "secolo"
    )(
      mp3"abar_Secolo.mp3"
    ),
    ReplyBundleMessage.textToMp3[F](
      "streghe",
      "maghi",
      "draghi",
      "roghi"
    )(
      mp3"abar_Draghi.mp3"
    ),
    ReplyBundleMessage.textToMp3[F](
      "crociate"
    )(
      mp3"abar_Crociate.mp3"
    ),
    ReplyBundleMessage.textToMp3[F](
      "wikipedia"
    )(
      mp3"abar_Wikipedia.mp3"
    ),
    ReplyBundleMessage.textToMp3[F](
      "\\beccoh\\b".r.tr(5)
    )(
      mp3"abar_Ecco.mp3",
      mp3"abar_Ecco2.mp3",
      mp3"abar_Ecco3.mp3"
    ),
    ReplyBundleMessage.textToMp3[F](
      "maglio",
      "sbriciola",
      "schiaccia"
    )(
      mp3"abar_Maglio.mp3"
    ),
    ReplyBundleMessage.textToMp3[F](
      "chiese",
      "castelli",
      "villaggi",
      "assedi"
    )(
      mp3"abar_Assedi.mp3"
    ),
    ReplyBundleMessage.textToMp3[F](
      "furore",
      "città"
    )(
      mp3"abar_Furore.mp3"
    ),
    ReplyBundleMessage.textToMp3[F](
      "demoni",
      "scatenat"
    )(
      mp3"abar_Demoni.mp3"
    ),
    ReplyBundleMessage.textToMp3[F](
      "sensei"
    )(
      mp3"abar_Sensei.mp3"
    ),
    ReplyBundleMessage.textToMp3[F](
      "miserabile"
    )(
      mp3"abar_Miserabile.mp3"
    ),
    ReplyBundleMessage.textToMp3[F](
      "omicidio",
      "cosa che capita"
    )(
      mp3"abar_CapitaOmicidio.mp3"
    ),
    ReplyBundleMessage.textToMp3[F](
      "cavallo",
      "tiriamo(lo)? giù".r.tr(11),
      "ammazziamolo"
    )(
      mp3"abar_Ammazziamolo.mp3"
    ),
    ReplyBundleMessage.textToMp3[F](
      "bruciare",
      "saccheggiare"
    )(
      mp3"abar_Bbq.mp3"
    ),
    ReplyBundleMessage.textToMp3[F](
      "cagarelli",
      "feci",
      "cacca"
    )(
      mp3"abar_Homines.mp3"
    ),
    ReplyBundleMessage.textToMp3[F](
      "monsignore",
      "vescovo",
      "in culo"
    )(
      mp3"abar_Monsu.mp3"
    ),
    ReplyBundleMessage.textToMp3[F](
      "ottimismo"
    )(
      mp3"abar_Ottimismo.mp3"
    ),
    ReplyBundleMessage.textToMp3[F](
      "latino"
    )(
      mp3"abar_Homines.mp3",
      mp3"abar_Vagdavercustis.mp3",
      mp3"abar_Yersinia.mp3",
      mp3"abar_Culagium.mp3"
    ),
    ReplyBundleMessage.textToMp3[F](
      "radetzky"
    )(
      mp3"abar_Radetzky.mp3"
    ),
    ReplyBundleMessage.textToMp3[F](
      "interrogateli",
      "tortura"
    )(
      mp3"abar_Reinterrogateli.mp3"
    ),
    ReplyBundleMessage.textToMp3[F](
      " (i|il|del) re\\b".r.tr(5),
      "decapita"
    )(
      mp3"abar_Re.mp3"
    ),
    ReplyBundleMessage.textToMp3[F](
      "\\bascia\\b".r.tr(5),
      "sangue"
    )(
      mp3"abar_Sangue.mp3"
    ),
    ReplyBundleMessage.textToMp3[F](
      "stupidi"
    )(
      mp3"abar_Stupidi.mp3"
    ),
    ReplyBundleMessage.textToMp3[F](
      "\\bsubito!".r.tr(7)
    )(
      mp3"abar_Subito.mp3"
    ),
    ReplyBundleMessage.textToMp3[F](
      "vagdavercustis"
    )(
      mp3"abar_Vagdavercustis.mp3"
    ),
    ReplyBundleMessage.textToMp3[F](
      "peste",
      "yersinia"
    )(
      mp3"abar_Yersinia.mp3"
    ),
    ReplyBundleMessage.textToMp3[F](
      "zazzera"
    )(
      mp3"abar_Zazzera.mp3"
    ),
    ReplyBundleMessage.textToMp3[F](
      "metallo"
    )(
      mp3"abar_Metallo.mp3"
    ),
    ReplyBundleMessage.textToMp3[F](
      "italiani",
      "arrendetevi"
    )(
      mp3"abar_Taliani.mp3"
    ),
    ReplyBundleMessage.textToMp3[F](
      "caa[a]*z[z]*o[o]*".r.tr(5)
    )(
      mp3"abar_Cazzo.mp3"
    ),
    ReplyBundleMessage.textToMp3[F](
      "eresia",
      "riti satanici",
      "rinnegamento di gesù cristo",
      "sputi sulla croce",
      "sodomia"
    )(
      mp3"abar_RitiSataniciSodomia.mp3"
    ),
    ReplyBundleMessage.textToMp3[F](
      "preoccupazione"
    )(
      mp3"abar_Preoccupazione.mp3"
    ),
    ReplyBundleMessage.textToMp3[F](
      "son(o)? tu[t]+e ba[l]+e".r.tr(13)
    )(
      mp3"abar_Bale.mp3"
    ),
    ReplyBundleMessage.textToMp3[F](
      "birra"
    )(
      mp3"abar_Birra.mp3"
    ),
    ReplyBundleMessage.textToMp3[F](
      "a roma",
      "gobeto"
    )(
      mp3"abar_Gobeto.mp3"
    ),
    ReplyBundleMessage.textToMp3[F](
      "mussolini",
      "fascismo",
      "fatto cose buone"
    )(
      mp3"abar_Mussolini.mp3"
    ),
    ReplyBundleMessage.textToMp3[F](
      "premio di maggioranza",
      "legge (acerbo|elettorale)",
      "parlamento",
      "maggioranza assoluta",
      "camicie nere",
      "listone mussolini",
      "(trenta|30) maggio"
    )(
      mp3"abar_LeggeAcerbo.mp3"
    ),
    ReplyBundleMessage.textToMp3[F](
      "alla fiera",
      "dei cazzi",
      "e coglioni",
      "affare"
    )(
      mp3"abar_LaVogliaDeiCazzi.mp3"
    ),
    ReplyBundleMessage.textToMp3[F](
      "dichiarazione di guerra",
      "è stata (presentata/consegnata)",
      "palazzo venezia",
      "ambasciatori"
    )(
      mp3"abar_DichiarazioneDiGuerra.mp3"
    ),
    ReplyBundleMessage.textToMp3[F](
      "schiavetti",
      "facevano sesso",
      "me lo sono fatto",
      "divertentissima"
    )(
      mp3"abar_CosaDivertentissima.mp3"
    ),
    ReplyBundleMessage.textToMp3[F](
      "ebrei",
      "bibbia",
      "diaspora",
      "mitologia",
      "religione",
      "antico testamento"
    )(
      mp3"abar_Bibbia.mp3"
    ),
    ReplyBundleMessage.textToMp3[F](
      "excelsior",
      "belle epoque",
      "nel progresso",
      "benessere",
      "\\b1914\\b".r.tr(4),
      "\\b1915\\b".r.tr(4)
    )(
      mp3"abar_BelleEpoqueProgresso.mp3"
    ),
    ReplyBundleMessage.textToMp3[F](
      "\\bgoti\\b".r.tr(4),
      "siamo (qui|dentro|tanti)".r.tr(9)
    )(
      mp3"abar_Goti.mp3"
    ),
    ReplyBundleMessage.textToMp3[F](
      "d'orleans",
      "ateo",
      "ateismo",
      "irriveren",
      "non crede in",
      "bella figura",
      "ostenta",
      "\\borge\\b".r.tr(4),
      "venerd[iì] santo".r.tr(13),
      "il diavolo"
    )(
      mp3"abar_AteoIrriverente.mp3"
    ),
    ReplyBundleMessage.textToMp3[F](
      "dal mare",
      "una bestia",
      "bestemmia",
      "del(l'orso| leone)",
      "leopardo"
    )(
      mp3"abar_BestiaBestemmie.mp3"
    ),
    ReplyBundleMessage.textToMp3[F](
      "ignobile",
      "inesperta",
      "abietta",
      "servile",
      "cognizione (governo|giustizia|religione)".r.tr(18),
      "carnali",
      "lussuria",
      "avarizia",
      "arroganza",
      "superbia"
    )(
      mp3"abar_GenteIgnobile.mp3"
    ),
    ReplyBundleMessage
      .textToMp3[F](
        "libellista",
        "sedizioso",
        "diffamatore",
        "ribelle",
        "rivoluzionario",
        "sovversivo",
        "agitatore",
        "contestatore",
        "rivoltoso",
        "sobillatore"
      )(
        mp3"abar_Libellista.mp3"
      ),
    ReplyBundleMessage
      .textToMp3[F](
        "nanerottolo",
        "furibondo",
        "furente",
        "furioso",
        "incazzosissimo",
        "\\bnano\\b".r.tr(4),
        "pigmeo",
        "piccoletto",
        "\\btappo\\b".r.tr(5),
        "lillipuziano",
        "bassotto"
      )(
        mp3"abar_Nanerottolo.mp3"
      )
  )
end Audio
