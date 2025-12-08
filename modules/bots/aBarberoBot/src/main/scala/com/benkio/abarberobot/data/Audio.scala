package com.benkio.abarberobot.data

import com.benkio.telegrambotinfrastructure.model.reply.mp3
import com.benkio.telegrambotinfrastructure.model.reply.ReplyBundleMessage
import com.benkio.telegrambotinfrastructure.model.tr

object Audio {

  def messageRepliesAudioData: List[ReplyBundleMessage] = List(
    ReplyBundleMessage.textToMp3(
      "fuoco"
    )(
      mp3"abar_Bbq.mp3",
      mp3"abar_FerroFuocoAcquaBollenteAceto.mp3"
    ),
    ReplyBundleMessage.textToMp3(
      "luigi (14|quattordicesimo)".r.tr(8),
      "louis[- ]le[- ]grand".r.tr(14)
    )(
      mp3"abar_Luigi14.mp3"
    ),
    ReplyBundleMessage.textToMp3(
      "tedesco"
    )(
      mp3"abar_Kraft.mp3",
      mp3"abar_VonHohenheim.mp3",
      mp3"abar_Haushofer.mp3"
    ),
    ReplyBundleMessage.textToMp3(
      "genitali",
      "cosi e coglioni"
    )(
      mp3"abar_Cosi.mp3",
      mp3"abar_Sottaceto.mp3"
    ),
    ReplyBundleMessage.textToMp3(
      "kimono"
    )(
      mp3"abar_KimonoMaledetto.mp3",
      mp3"abar_KimonoStregato.mp3"
    ),
    ReplyBundleMessage.textToMp3(
      "particelle cadaveriche"
    )(
      mp3"abar_ParticelleCadaveriche.mp3"
    ),
    ReplyBundleMessage.textToMp3(
      "avrebbe (mai )?immaginato".r.tr(18)
    )(
      mp3"abar_NessunoAvrebbeImmaginato.mp3"
    ),
    ReplyBundleMessage.textToMp3(
      "mortalit[aà]".r.tr(9)
    )(
      mp3"abar_Mortalita.mp3"
    ),
    ReplyBundleMessage.textToMp3(
      "carta",
      "legno leggero"
    )(
      mp3"abar_LegnoLeggeroCarta.mp3"
    ),
    ReplyBundleMessage.textToMp3(
      "stregato"
    )(
      mp3"abar_KimonoStregato.mp3"
    ),
    ReplyBundleMessage.textToMp3(
      "maledetto"
    )(
      mp3"abar_Pestifero.mp3",
      mp3"abar_KimonoMaledetto.mp3"
    ),
    ReplyBundleMessage.textToMp3(
      "pestifero"
    )(
      mp3"abar_Pestifero.mp3"
    ),
    ReplyBundleMessage.textToMp3(
      "distrutto",
      "mangiato dai topi",
      "bruciato",
      "sepolto",
      "nel fiume",
      "innondazione"
    )(
      mp3"abar_Distrutto.mp3"
    ),
    ReplyBundleMessage.textToMp3(
      "\\bpratica\\b".r.tr(7)
    )(
      mp3"abar_PraticaPocoPatriotticah.mp3"
    ),
    ReplyBundleMessage.textToMp3(
      "\\bferro\\b".r.tr(5),
      "acqua bollente",
      "aceto"
    )(
      mp3"abar_FerroFuocoAcquaBollenteAceto.mp3"
    ),
    ReplyBundleMessage.textToMp3(
      "secolo"
    )(
      mp3"abar_Secolo.mp3"
    ),
    ReplyBundleMessage.textToMp3(
      "streghe",
      "maghi",
      "draghi",
      "roghi"
    )(
      mp3"abar_Draghi.mp3"
    ),
    ReplyBundleMessage.textToMp3(
      "crociate"
    )(
      mp3"abar_Crociate.mp3"
    ),
    ReplyBundleMessage.textToMp3(
      "wikipedia"
    )(
      mp3"abar_Wikipedia.mp3"
    ),
    ReplyBundleMessage.textToMp3(
      "\\beccoh\\b".r.tr(5)
    )(
      mp3"abar_Ecco.mp3",
      mp3"abar_Ecco2.mp3",
      mp3"abar_Ecco3.mp3"
    ),
    ReplyBundleMessage.textToMp3(
      "maglio",
      "sbriciola",
      "schiaccia"
    )(
      mp3"abar_Maglio.mp3"
    ),
    ReplyBundleMessage.textToMp3(
      "villaggi",
      "assedi"
    )(
      mp3"abar_Assedi.mp3"
    ),
    ReplyBundleMessage.textToMp3(
      "furore",
      "città"
    )(
      mp3"abar_Furore.mp3"
    ),
    ReplyBundleMessage.textToMp3(
      "demoni",
      "scatenat"
    )(
      mp3"abar_Demoni.mp3"
    ),
    ReplyBundleMessage.textToMp3(
      "sensei"
    )(
      mp3"abar_Sensei.mp3"
    ),
    ReplyBundleMessage.textToMp3(
      "miserabile"
    )(
      mp3"abar_Miserabile.mp3"
    ),
    ReplyBundleMessage.textToMp3(
      "omicidio",
      "cosa che capita"
    )(
      mp3"abar_CapitaOmicidio.mp3"
    ),
    ReplyBundleMessage.textToMp3(
      "cavallo",
      "tiriamo(lo)? giù".r.tr(11),
      "ammazziamolo"
    )(
      mp3"abar_Ammazziamolo.mp3"
    ),
    ReplyBundleMessage.textToMp3(
      "bruciare",
      "saccheggiare"
    )(
      mp3"abar_Bbq.mp3"
    ),
    ReplyBundleMessage.textToMp3(
      "cagarelli",
      "feci",
      "cacca"
    )(
      mp3"abar_Homines.mp3"
    ),
    ReplyBundleMessage.textToMp3(
      "monsignore",
      "vescovo",
      "in culo"
    )(
      mp3"abar_Monsu.mp3"
    ),
    ReplyBundleMessage.textToMp3(
      "ottimismo"
    )(
      mp3"abar_Ottimismo.mp3"
    ),
    ReplyBundleMessage.textToMp3(
      "latino"
    )(
      mp3"abar_Homines.mp3",
      mp3"abar_Vagdavercustis.mp3",
      mp3"abar_Yersinia.mp3",
      mp3"abar_Culagium.mp3"
    ),
    ReplyBundleMessage.textToMp3(
      "radetzky"
    )(
      mp3"abar_Radetzky.mp3"
    ),
    ReplyBundleMessage.textToMp3(
      "interrogateli",
      "tortura"
    )(
      mp3"abar_Reinterrogateli.mp3"
    ),
    ReplyBundleMessage.textToMp3(
      " (i|il|del) re\\b".r.tr(5),
      "decapita"
    )(
      mp3"abar_Re.mp3"
    ),
    ReplyBundleMessage.textToMp3(
      "\\bascia\\b".r.tr(5)
    )(
      mp3"abar_Sangue.mp3"
    ),
    ReplyBundleMessage.textToMp3(
      "stupidi"
    )(
      mp3"abar_Stupidi.mp3"
    ),
    ReplyBundleMessage.textToMp3(
      "\\bsubito!".r.tr(7)
    )(
      mp3"abar_Subito.mp3"
    ),
    ReplyBundleMessage.textToMp3(
      "vagdavercustis"
    )(
      mp3"abar_Vagdavercustis.mp3"
    ),
    ReplyBundleMessage.textToMp3(
      "peste",
      "yersinia"
    )(
      mp3"abar_Yersinia.mp3"
    ),
    ReplyBundleMessage.textToMp3(
      "zazzera"
    )(
      mp3"abar_Zazzera.mp3"
    ),
    ReplyBundleMessage.textToMp3(
      "metallo"
    )(
      mp3"abar_Metallo.mp3"
    ),
    ReplyBundleMessage.textToMp3(
      "italiani",
      "arrendetevi"
    )(
      mp3"abar_Taliani.mp3"
    ),
    ReplyBundleMessage.textToMp3(
      "caa[a]*z[z]*o[o]*".r.tr(5)
    )(
      mp3"abar_Cazzo.mp3"
    ),
    ReplyBundleMessage.textToMp3(
      "eresia",
      "riti satanici",
      "rinnegamento di gesù cristo",
      "sputi sulla croce",
      "sodomia"
    )(
      mp3"abar_RitiSataniciSodomia.mp3"
    ),
    ReplyBundleMessage.textToMp3(
      "preoccupazione"
    )(
      mp3"abar_Preoccupazione.mp3"
    ),
    ReplyBundleMessage.textToMp3(
      "son(o)? tu[t]+e ba[l]+e".r.tr(13)
    )(
      mp3"abar_Bale.mp3"
    ),
    ReplyBundleMessage.textToMp3(
      "birra"
    )(
      mp3"abar_Birra.mp3"
    ),
    ReplyBundleMessage.textToMp3(
      "a roma",
      "gobeto"
    )(
      mp3"abar_Gobeto.mp3"
    ),
    ReplyBundleMessage.textToMp3(
      "mussolini",
      "fascismo",
      "fatto cose buone"
    )(
      mp3"abar_Mussolini.mp3"
    ),
    ReplyBundleMessage.textToMp3(
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
    ReplyBundleMessage.textToMp3(
      "alla fiera",
      "dei cazzi",
      "e coglioni",
      "affare"
    )(
      mp3"abar_LaVogliaDeiCazzi.mp3"
    ),
    ReplyBundleMessage.textToMp3(
      "dichiarazione di guerra",
      "è stata (presentata/consegnata)",
      "palazzo venezia",
      "ambasciatori"
    )(
      mp3"abar_DichiarazioneDiGuerra.mp3"
    ),
    ReplyBundleMessage.textToMp3(
      "schiavetti",
      "facevano sesso",
      "me lo sono fatto",
      "divertentissima"
    )(
      mp3"abar_CosaDivertentissima.mp3"
    ),
    ReplyBundleMessage.textToMp3(
      "ebrei",
      "bibbia",
      "diaspora",
      "mitologia",
      "religione",
      "antico testamento"
    )(
      mp3"abar_Bibbia.mp3"
    ),
    ReplyBundleMessage.textToMp3(
      "excelsior",
      "belle epoque",
      "nel progresso",
      "benessere",
      "\\b1914\\b".r.tr(4),
      "\\b1915\\b".r.tr(4)
    )(
      mp3"abar_BelleEpoqueProgresso.mp3"
    ),
    ReplyBundleMessage.textToMp3(
      "\\bgoti\\b".r.tr(4),
      "siamo (qui|dentro|tanti)".r.tr(9)
    )(
      mp3"abar_Goti.mp3"
    ),
    ReplyBundleMessage.textToMp3(
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
    ReplyBundleMessage.textToMp3(
      "dal mare",
      "una bestia",
      "bestemmia",
      "del(l'orso| leone)",
      "leopardo"
    )(
      mp3"abar_BestiaBestemmie.mp3"
    ),
    ReplyBundleMessage.textToMp3(
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
      .textToMp3(
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
      .textToMp3(
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
} // end Audio
