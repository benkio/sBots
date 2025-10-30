package com.benkio.youtuboanchei0bot.data

import cats.Applicative
import com.benkio.telegrambotinfrastructure.model.reply.gif
import com.benkio.telegrambotinfrastructure.model.reply.mp3
import com.benkio.telegrambotinfrastructure.model.reply.pho
import com.benkio.telegrambotinfrastructure.model.reply.sticker
import com.benkio.telegrambotinfrastructure.model.reply.vid
import com.benkio.telegrambotinfrastructure.model.reply.ReplyBundleMessage
import com.benkio.telegrambotinfrastructure.model.tr

object Mix:

  def messageRepliesMixData[
      F[_]: Applicative
  ]: List[ReplyBundleMessage[F]] = List(
    ReplyBundleMessage.textToMedia[F](
      "colore"
    )(
      gif"ytai_ColoreMeravigliosoGif.mp4",
      mp3"ytai_VeritaPegnoPeperoncino.mp3",
      gif"ytai_VeritaPegnoPeperoncinoGif.mp4",
      vid"ytai_VeritaPegnoPeperoncino.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      "velit[aà]".r.tr(6),
      "pagare pegno",
      "per intero",
      "peperoncino"
    )(
      mp3"ytai_VeritaPegnoPeperoncino.mp3",
      gif"ytai_VeritaPegnoPeperoncinoGif.mp4",
      vid"ytai_VeritaPegnoPeperoncino.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      "prezzemolo",
      "profumo"
    )(
      mp3"ytai_PrezzemoloProfumoIncantevole.mp3",
      gif"ytai_PrezzemoloProfumoIncantevoleGif.mp4",
      vid"ytai_PrezzemoloProfumoIncantevole.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      "parma",
      "eccellenza",
      "bel paese"
    )(
      mp3"ytai_CrudoParma.mp3",
      vid"ytai_CrudoParma.mp4",
      gif"ytai_CrudoParmaGif.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      "ricordami fino a domani"
    )(
      gif"ytai_RicordamiGif.mp4",
      vid"ytai_RicordamiFinoADomani.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      "ho perso (di nuovo )?qualcosa".r.tr(17)
    )(
      gif"ytai_HoPersoQualcosaGif.mp4",
      mp3"ytai_HoPersoQualcosa.mp3"
    ),
    ReplyBundleMessage.textToMedia[F](
      "che vergogna",
      "non ce l'ho",
      "sopracciglia",
      "tutti (quanti )?mi criticheranno".r.tr(22)
    )(
      gif"ytai_CheVergognaGif.mp4",
      mp3"ytai_CheVergogna.mp3"
    ),
    ReplyBundleMessage.textToMedia[F](
      "ti voglio (tanto )?bene".r.tr(14)
    )(
      gif"ytai_TVTBGif.mp4",
      mp3"ytai_AncheIoTiVoglioTantoBene.mp3"
    ),
    ReplyBundleMessage.textToMedia[F](
      "vi voglio (tanto )*bene".r.tr(14)
    )(
      gif"ytai_ViVoglioTantoBeneGif.mp4",
      vid"ytai_ViVoglioTantoBene.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      "pu[oò] capitare".r.tr(12)
    )(
      gif"ytai_PuoCapitareGif.mp4",
      vid"ytai_PuoCapitare.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      "strudel"
    )(
      mp3"ytai_30CentimentriStrudelBuonissimo.mp3",
      vid"ytai_30CentimentriStrudelBuonissimo.mp4",
      gif"ytai_30CentimentriStrudelBuonissimoGif.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      "boscaiolo",
      "come si presenta"
    )(
      mp3"ytai_BoscaioloPresentaColFungo.mp3",
      vid"ytai_BoscaioloPresentaColFungo.mp4",
      gif"ytai_BoscaioloPresentaColFungoGif.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      "buon salame"
    )(
      mp3"ytai_BuonSalameCariAmici.mp3",
      vid"ytai_BuonSalameCariAmici.mp4",
      gif"ytai_BuonSalameCariAmiciGif.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      "cari amici"
    )(
      mp3"ytai_BuonSalameCariAmici.mp3",
      vid"ytai_BuonSalameCariAmici.mp4",
      gif"ytai_BuonSalameCariAmiciGif.mp4",
      mp3"ytai_BuonCompleannoCariAmici.mp3",
      vid"ytai_BuonCompleannoCariAmici.mp4",
      gif"ytai_BuonCompleannoCariAmiciGif.mp4",
      gif"ytai_CariAmiciFollowersBuongiornoSabatoGif.mp4",
      vid"ytai_CariAmiciFollowersBuongiornoSabato.mp4",
      mp3"ytai_CariAmiciFollowersBuongiornoSabato.mp3",
      gif"ytai_SpezzanoAlbaneseGif.mp4",
      vid"ytai_SpezzanoAlbanese.mp4",
      mp3"ytai_SpezzanoAlbanese.mp3",
      gif"ytai_NelMioCuoreContinuazioneCariAmiciFollowersGif.mp4",
      vid"ytai_NelMioCuoreContinuazioneCariAmiciFollowers.mp4",
      mp3"ytai_NelMioCuoreContinuazioneCariAmiciFollowers.mp3"
    ),
    ReplyBundleMessage.textToMedia[F](
      "buono!"
    )(
      mp3"ytai_Buono.mp3",
      vid"ytai_Buono.mp4",
      gif"ytai_BuonoGif.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      "ragù"
    )(
      mp3"ytai_BuonoRagu.mp3",
      vid"ytai_BuonoRagu.mp4",
      gif"ytai_BuonoRaguGif.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      "non ci arrivo",
      "dopo (che ho )?mangiato".r.tr(13)
    )(
      mp3"ytai_CiaoNonCiArrivo.mp3",
      vid"ytai_CiaoNonCiArrivo.mp4",
      gif"ytai_CiaoNonCiArrivoGif.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      "cocco e cioccolata",
      "unione strepitosa"
    )(
      mp3"ytai_CoccoCioccolataUnioneStrepitosa.mp3",
      vid"ytai_CoccoCioccolataUnioneStrepitosa.mp4",
      gif"ytai_CoccoCioccolataUnioneStrepitosaGif.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      "cotto bene",
      "croccante"
    )(
      mp3"ytai_CottoBeneMoltoCroccante.mp3",
      vid"ytai_CottoBeneMoltoCroccante.mp4",
      gif"ytai_CottoBeneMoltoCroccanteGif.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      "vivete questo momento con me",
      "sempre sul tubo"
    )(
      mp3"ytai_CredeteSempreSulTuboViveteMomentoConMe.mp3",
      vid"ytai_CredeteSempreSulTuboViveteMomentoConMe.mp4",
      gif"ytai_CredeteSempreSulTuboViveteMomentoConMeGif.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      "cucchiaio",
      "da battaglia"
    )(
      mp3"ytai_CucchiaioBattagliaTiramisu.mp3",
      vid"ytai_CucchiaioBattagliaTiramisu.mp4",
      gif"ytai_CucchiaioBattagliaTiramisuGif.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      "champignon",
      "in un paio d'ore",
      "rovinato lo stomaco",
      "stetti male",
      "continuando a bere"
    )(
      vid"ytai_StoriaChampignon.mp4",
      gif"ytai_FungoChampignonRovinatoStomacoGif.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      "ho spinto a bere",
      "un bicchiere tira l'altro",
      "aspettavo che portassero la carne",
      "cibi di sostanza"
    )(
      vid"ytai_StoriaChampignon.mp4",
      gif"ytai_HoSpintoABereGif.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      "sapete come si chiama?",
      "incoscienza",
      "non pensarci",
      "ha un'altro nome"
    )(
      mp3"ytai_NonIncoscienzaNonPensarci.mp3",
      vid"ytai_NonIncoscienzaNonPensarci.mp4",
      gif"ytai_NonIncoscienzaNonPensarciGif.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      "prosciutto crudo"
    )(
      mp3"ytai_ProsciuttoCrudoSpettacolo.mp3",
      vid"ytai_ProsciuttoCrudoSpettacolo.mp4",
      gif"ytai_ProsciuttoCrudoSpettacoloGif.mp4",
      mp3"ytai_ProsciuttoCrudoParla.mp3",
      vid"ytai_ProsciuttoCrudoParla.mp4",
      gif"ytai_ProsciuttoCrudoParlaGif.mp4",
      vid"ytai_CrudoParma.mp4",
      mp3"ytai_CrudoParma.mp3",
      gif"ytai_CrudoParmaGif.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      "rafforzare lo stomaco",
      "polletto"
    )(
      mp3"ytai_RafforzareStomacoPolletto.mp3",
      vid"ytai_RafforzareStomacoPolletto.mp4",
      gif"ytai_RafforzareStomacoPollettoGif.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      "(30|trenta) centimetri"
    )(
      mp3"ytai_30CentimentriStrudelBuonissimo.mp3",
      vid"ytai_30CentimentriStrudelBuonissimo.mp4",
      gif"ytai_30CentimentriStrudelBuonissimoGif.mp4",
      mp3"ytai_Serpentello30Centimetri.mp3",
      vid"ytai_Serpentello30Centimetri.mp4",
      gif"ytai_Serpentello30CentimetriGif.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      "serpentello"
    )(
      mp3"ytai_Serpentello30Centimetri.mp3",
      vid"ytai_Serpentello30Centimetri.mp4",
      gif"ytai_Serpentello30CentimetriGif.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      "matrimonio"
    )(
      vid"ytai_StoriaChampignon.mp4",
      mp3"ytai_StoriaChampignon.mp3"
    ),
    ReplyBundleMessage.textToMedia[F](
      "superiamo questi limiti",
      "limiti di pensiero",
      "andiamo oltre"
    )(
      vid"ytai_SuperiamoLimitiPensiero.mp4",
      mp3"ytai_SuperiamoLimitiPensiero.mp3",
      gif"ytai_SuperiamoLimitiPensieroGif.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      "rovesciata l'acqua",
      "\\bops\\b".r.tr(3)
    )(
      mp3"ytai_OpsRovesciataAcqua.mp3",
      gif"ytai_OpsRovesciataAcquaGif.mp4",
      vid"ytai_OpsRovesciataAcqua.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      "ho fatto bene a farlo",
      "non lo volevo fare",
      "mi sto sentendo (bene|in compagnia)".r.tr(20)
    )(
      gif"ytai_NoVideoHoFattoBeneCompagniaGif.mp4",
      mp3"ytai_NoVideoHoFattoBeneCompagnia.mp3",
      vid"ytai_NoVideoHoFattoBeneCompagnia.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      "(25|venticinque) (milioni|mila euro)".r.tr(10),
      "\\b25000\\b".r.tr(5),
      "visualizzazioni"
    )(
      gif"ytai_25MilioniVisualizzazioni25MilaEuroGif.mp4",
      vid"ytai_25MilioniVisualizzazioni25MilaEuro.mp4",
      mp3"ytai_25MilioniVisualizzazioni25MilaEuro.mp3"
    ),
    ReplyBundleMessage.textToMedia[F](
      "autori",
      "massicci piedi"
    )(
      gif"ytai_AutoriMassicciPiediGif.mp4",
      vid"ytai_AutoriMassicciPiedi.mp4",
      mp3"ytai_AutoriMassicciPiedi.mp3"
    ),
    ReplyBundleMessage.textToMedia[F](
      "criceto",
      "🐹"
    )(
      gif"ytai_BellaTecnicaDelCricetoGif.mp4",
      vid"ytai_BellaTecnicaDelCriceto.mp4",
      mp3"ytai_BellaTecnicaDelCriceto.mp3",
      gif"ytai_TecnicaDelCricetoGif.mp4",
      vid"ytai_TecnicaDelCriceto.mp4",
      mp3"ytai_TecnicaDelCriceto.mp3"
    ),
    ReplyBundleMessage.textToMedia[F](
      "followers"
    )(
      vid"ytai_CiaoFollowersNelBeneNelMale.mp4",
      gif"ytai_CiaoFollowersNelBeneNelMaleGif.mp4",
      mp3"ytai_CiaoFollowersNelBeneNelMale.mp3",
      gif"ytai_BuonanotteFollowersGif.mp4",
      gif"ytai_CiaoCariAmiciFollowersGif.mp4",
      gif"ytai_PersonaliLotteFollowersGif.mp4",
      gif"ytai_CariAmiciFollowersBuongiornoSabatoGif.mp4",
      vid"ytai_CariAmiciFollowersBuongiornoSabato.mp4",
      mp3"ytai_CariAmiciFollowersBuongiornoSabato.mp3",
      gif"ytai_SpezzanoAlbaneseGif.mp4",
      vid"ytai_SpezzanoAlbanese.mp4",
      mp3"ytai_SpezzanoAlbanese.mp3",
      gif"ytai_NelMioCuoreContinuazioneCariAmiciFollowersGif.mp4",
      vid"ytai_NelMioCuoreContinuazioneCariAmiciFollowers.mp4",
      mp3"ytai_NelMioCuoreContinuazioneCariAmiciFollowers.mp3"
    ),
    ReplyBundleMessage.textToMedia[F](
      "allora l[iì]".r.tr(9)
    )(
      gif"ytai_AlloraLiGif.mp4",
      mp3"ytai_AlloraLi.mp3"
    ),
    ReplyBundleMessage.textToMedia[F](
      "donazioni"
    )(
      gif"ytai_DonazioniGif.mp4",
      mp3"ytai_Donazioni.mp3"
    ),
    ReplyBundleMessage.textToMedia[F](
      "a me niente va bene",
      "non [tm]i va bene niente".r.tr(21)
    )(
      gif"ytai_NienteVaBeneGif.mp4",
      mp3"ytai_NienteVaBene.mp3"
    ),
    ReplyBundleMessage.textToMedia[F](
      "mi assomiglia",
      "stessa forma",
      "pomodoro",
      "sta da solo",
      "bisogno di sostegno"
    )(
      mp3"ytai_PomodoroNanoStessaFormaSostegno.mp3",
      vid"ytai_PomodoroNanoStessaFormaSostegno.mp4",
      gif"ytai_PomodoroNanoStessaFormaSostegnoGif.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      "verza",
      "cavolo cappuccio",
      "giuseppe",
      "ma che m(i |')hai detto".r.tr(18)
    )(
      gif"ytai_VerzaGiuseppeGif.mp4",
      vid"ytai_VerzaGiuseppe.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      "per cortesia",
      "dottori",
      "dentist[ia]".r.tr(8),
      "ho (ancora )?tanta fame".r.tr(13)
    )(
      mp3"ytai_DentistiDottoriFame.mp3",
      vid"ytai_DentistiDottoriFame.mp4",
      gif"ytai_DentistiDottoriFameGif.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      "tiramisù"
    )(
      gif"ytai_MonoporzioniTiramisuGif.mp4",
      gif"ytai_TiramisuGif.mp4",
      mp3"ytai_CucchiaioBattagliaTiramisu.mp3",
      vid"ytai_CucchiaioBattagliaTiramisu.mp4",
      gif"ytai_CucchiaioBattagliaTiramisuGif.mp4",
      gif"ytai_LoopAperturaTiramisuGif.mp4"
    ),
    ReplyBundleMessage.textToMedia[F]("ciao!")(
      gif"ytai_CiaoGif.mp4",
      gif"ytai_Ciao2Gif.mp4",
      gif"ytai_Ciao3Gif.mp4",
      gif"ytai_CiaoRagazziGif.mp4",
      vid"ytai_CiaoFollowersNelBeneNelMale.mp4",
      gif"ytai_CiaoFollowersNelBeneNelMaleGif.mp4",
      mp3"ytai_CiaoFollowersNelBeneNelMale.mp3",
      gif"ytai_CiaoCariAmiciFollowersGif.mp4",
      mp3"ytai_CiaoNonCiArrivo.mp3",
      vid"ytai_CiaoNonCiArrivo.mp4",
      gif"ytai_CiaoNonCiArrivoGif.mp4",
      sticker"ytai_CiaoYtancheio.sticker"
    ),
    ReplyBundleMessage.textToMedia[F](
      "buongiorno",
      "buon sabato"
    )(
      gif"ytai_CariAmiciFollowersBuongiornoSabatoGif.mp4",
      vid"ytai_CariAmiciFollowersBuongiornoSabato.mp4",
      mp3"ytai_CariAmiciFollowersBuongiornoSabato.mp3"
    ),
    ReplyBundleMessage.textToMedia[F](
      "eh (no|allora)".r.tr(5),
      "imbeccare",
      "bengalini"
    )(
      gif"ytai_ImbeccareComeBengaliniGif.mp4",
      vid"ytai_ImbeccareComeBengalini.mp4",
      mp3"ytai_ImbeccareComeBengalini.mp3"
    ),
    ReplyBundleMessage.textToMedia[F](
      "hamburger",
      "🍔"
    )(
      gif"ytai_TecnicaDelCricetoGif.mp4",
      vid"ytai_TecnicaDelCriceto.mp4",
      mp3"ytai_TecnicaDelCriceto.mp3"
    ),
    ReplyBundleMessage.textToMedia[F](
      "\\btermina\\b".r.tr(7),
      "nella vita"
    )(
      gif"ytai_TuttoNellaVitaTerminaGif.mp4",
      vid"ytai_TuttoNellaVitaTermina.mp4",
      mp3"ytai_TuttoNellaVitaTermina.mp3"
    ),
    ReplyBundleMessage.textToMedia[F](
      "torcia",
      "🔦"
    )(
      gif"ytai_TorciaGif.mp4",
      vid"ytai_Torcia.mp4",
      mp3"ytai_Torcia.mp3"
    ),
    ReplyBundleMessage.textToMedia[F](
      "molto emozionato",
      "cominciare subito"
    )(
      gif"ytai_MoltoEmozionatoCominciareSubitoGif.mp4",
      vid"ytai_MoltoEmozionatoCominciareSubito.mp4",
      mp3"ytai_MoltoEmozionatoCominciareSubito.mp3"
    ),
    ReplyBundleMessage.textToMedia[F](
      "non posso dire niente",
      "restate iscritti",
      "mi raccomando"
    )(
      gif"ytai_NonPossoDireNienteRestateIscrittiGif.mp4",
      vid"ytai_NonPossoDireNienteRestateIscritti.mp4",
      mp3"ytai_NonPossoDireNienteRestateIscritti.mp3"
    ),
    ReplyBundleMessage.textToMedia[F](
      "spese (inattese|pretestuose)".r.tr(14),
      "non ho soldi",
      "pagare"
    )(
      gif"ytai_SpeseInattesePretestuoseGif.mp4",
      vid"ytai_SpeseInattesePretestuose.mp4",
      mp3"ytai_SpeseInattesePretestuose.mp3"
    ),
    ReplyBundleMessage.textToMedia[F](
      "in un video",
      "ti riescono",
      "si blocca tutto"
    )(
      gif"ytai_VideoImprevistiGif.mp4",
      vid"ytai_VideoImprevisti.mp4",
      mp3"ytai_VideoImprevisti.mp3"
    ),
    ReplyBundleMessage.textToMedia[F](
      "benvenuto",
      "convenevoli",
      "salerno[ -]?reggio[- ]?calabria".r.tr(21),
      "sibari",
      "spezzano",
      "albanese",
      "(700|settecento) metri".r.tr(9)
    )(
      gif"ytai_SpezzanoAlbaneseGif.mp4",
      vid"ytai_SpezzanoAlbanese.mp4",
      mp3"ytai_SpezzanoAlbanese.mp3"
    ),
    ReplyBundleMessage.textToMedia[F](
      "sono fermo"
    )(
      gif"ytai_SpezzanoAlbaneseGif.mp4",
      vid"ytai_SpezzanoAlbanese.mp4",
      mp3"ytai_SpezzanoAlbanese.mp3",
      gif"ytai_IncidenteGif.mp4",
      vid"ytai_Incidente.mp4",
      mp3"ytai_Incidente.mp3"
    ),
    ReplyBundleMessage.textToMedia[F](
      "incidente",
      "feriti",
      "gravi o meno",
      "non gravi",
      "intrappolato nelle lamiere",
      "nell'ordine",
      "sono venuti",
      "macchine della polizia",
      "camion anas",
      "ambulanza",
      "carabinieri",
      "elicottero"
    )(
      gif"ytai_IncidenteGif.mp4",
      vid"ytai_Incidente.mp4",
      mp3"ytai_Incidente.mp3"
    ),
    ReplyBundleMessage.textToMedia[F](
      "inquadro",
      "fare riprese",
      "censura",
      "non riesco a fare",
      "collaborare",
      "iphone",
      "collaboratore",
      "a distanza",
      "trasferire i file",
      "wetransfer",
      "dropbox",
      "telegram"
    )(
      gif"ytai_CensuraIphoneDropboxTelegramGif.mp4",
      vid"ytai_CensuraIphoneDropboxTelegram.mp4",
      mp3"ytai_CensuraIphoneDropboxTelegram.mp3"
    ),
    ReplyBundleMessage.textToMedia[F](
      "\\b(ar)?rabbi([oa]|at[oa])\\b".r.tr(6),
      "collera",
      "indignato",
      "[🤬😡😠]".r.tr(1)
    )(
      pho"ytai_Rabbia.jpg",
      gif"ytai_LoopArrabbiatoIndignatoGif.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      "[😦😧]".r.tr(1),
      "shock"
    )(
      pho"ytai_Shock.jpg",
      gif"ytai_LoopArrabbiatoIndignatoGif.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      "obesit[àa]".r.tr(7),
      "scusate"
    )(
      vid"ytai_SalutoDaObesita.mp4",
      gif"ytai_SalutoDaObesitaGif.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      "un (caro )?saluto".r.tr(9)
    )(
      vid"ytai_SalutoDaObesita.mp4",
      gif"ytai_SalutoDaObesitaGif.mp4",
      gif"ytai_NelMioCuoreContinuazioneCariAmiciFollowersGif.mp4",
      vid"ytai_NelMioCuoreContinuazioneCariAmiciFollowers.mp4",
      mp3"ytai_NelMioCuoreContinuazioneCariAmiciFollowers.mp3"
    ),
    ReplyBundleMessage.textToMedia[F](
      "\\btazza\\b".r.tr(5),
      "che differenza"
    )(
      mp3"ytai_TazzaGrande.mp3",
      gif"ytai_TazzaGrandeGif.mp4",
      vid"ytai_TazzaGrande.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      "buon appetito"
    )(
      gif"ytai_BuonAppetitoGif.mp4",
      gif"ytai_BuonAppetito2Gif.mp4",
      vid"ytai_BuonAppetitoCompagnia.mp4",
      mp3"ytai_BuonAppetitoCompagnia.mp3",
      gif"ytai_BuonAppetitoCompagniaGif.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      "vostra compagnia"
    )(
      gif"ytai_SeParloPocoGif.mp4",
      vid"ytai_BuonAppetitoCompagnia.mp4",
      mp3"ytai_BuonAppetitoCompagnia.mp3",
      gif"ytai_BuonAppetitoCompagniaGif.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      "sarei (stato )?da solo"
    )(
      vid"ytai_BuonAppetitoCompagnia.mp4",
      mp3"ytai_BuonAppetitoCompagnia.mp3",
      gif"ytai_BuonAppetitoCompagniaGif.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      "(buona )?pizza".r.tr(5),
      "🍕"
    )(
      gif"ytai_BuonaPizzaGif.mp4",
      gif"ytai_PizzaAllegriaGif.mp4",
      sticker"ytai_PizzaYtancheio.sticker"
    ),
    ReplyBundleMessage.textToMedia[F](
      "acqua calabria"
    )(
      gif"ytai_AcquaSguardoGif.mp4",
      gif"ytai_SeteGif.mp4",
      gif"ytai_AcquaCalabriaGif.mp4",
      gif"ytai_AcquaCalabriaOttimaGif.mp4",
      gif"ytai_AcquaMeravigliosaGif.mp4",
      sticker"ytai_AcquaYtancheio.sticker",
      sticker"ytai_Acqua2Ytancheio.sticker"
    ),
    ReplyBundleMessage.textToMedia[F](
      "\\btopolin[oi]\\b".r.tr(8)
    )(
      mp3"ytai_Topolino.mp3",
      sticker"ytai_TopolinoYtancheio.sticker"
    ),
    ReplyBundleMessage.textToMedia[F](
      "caffè",
      "☕"
    )(
      gif"ytai_BuonCaffeATuttiGif.mp4",
      sticker"ytai_CaffeYtancheio.sticker",
      vid"ytai_ZuccheroCaffeOttimo.mp4",
      mp3"ytai_ZuccheroCaffeOttimo.mp3",
      gif"ytai_ZuccheroCaffeOttimoGif.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      "ciotola",
      "🍜",
      "🥣",
      "🍲"
    )(
      gif"ytai_LoopCiotolaGif.mp4",
      gif"ytai_LoopCiotola2Gif.mp4",
      sticker"ytai_CiotolaYtancheio.sticker"
    ),
    ReplyBundleMessage.textToMedia[F](
      "buonissimo"
    )(
      mp3"ytai_Buonissimo.mp3",
      vid"ytai_Buonissimo.mp4",
      gif"ytai_BuonissimoGif.mp4",
      mp3"ytai_30CentimentriStrudelBuonissimo.mp3",
      vid"ytai_30CentimentriStrudelBuonissimo.mp4",
      gif"ytai_30CentimentriStrudelBuonissimoGif.mp4",
      gif"ytai_Buonissimo2Gif.mp4",
      mp3"ytai_Buonissimo2.mp3",
      vid"ytai_Buonissimo2.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      "passi da gigante"
    )(
      mp3"ytai_PassiDaGigante.mp3",
      vid"ytai_PassiDaGigante.mp4",
      gif"ytai_PassiDaGiganteGif.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      "(😄|😀|😃){3,}".r.tr(3),
      "sorriso"
    )(
      gif"ytai_SorrisoGif.mp4",
      gif"ytai_Sorriso2Gif.mp4",
      pho"ytai_SorrisoTortaFelice.jpg"
    ),
    ReplyBundleMessage
      .textToMedia[F](
        "\\ba dopo\\b".r.tr(6)
      )(
        gif"ytai_NelMioCuoreContinuazioneCariAmiciFollowersGif.mp4",
        vid"ytai_NelMioCuoreContinuazioneCariAmiciFollowers.mp4",
        mp3"ytai_NelMioCuoreContinuazioneCariAmiciFollowers.mp3"
      ),
    ReplyBundleMessage.textToMedia[F](
      "natale",
      "augurato",
      "a voi(, |\\.\\.\\.| )tutti".r.tr(11)
    )(
      gif"ytai_SerenoNataleAVoiTuttiGif.mp4",
      vid"ytai_SerenoNataleAVoiTutti.mp4",
      mp3"ytai_SerenoNataleAVoiTutti.mp3"
    ),
    ReplyBundleMessage
      .textToMedia[F](
        "prelibatezza"
      )(
        gif"ytai_PrelibatezzaIncantevoleGif.mp4",
        vid"ytai_PrelibatezzaIncantevole.mp4",
        mp3"ytai_PrelibatezzaIncantevole.mp3"
      ),
    ReplyBundleMessage.textToMedia[F](
      "incantevole"
    )(
      mp3"ytai_PrezzemoloProfumoIncantevole.mp3",
      gif"ytai_PrezzemoloProfumoIncantevoleGif.mp4",
      vid"ytai_PrezzemoloProfumoIncantevole.mp4",
      gif"ytai_PrelibatezzaIncantevoleGif.mp4",
      vid"ytai_PrelibatezzaIncantevole.mp4",
      mp3"ytai_PrelibatezzaIncantevole.mp3"
    ),
    ReplyBundleMessage
      .textToMedia[F](
        "come ci voleva",
        "quest'acqua"
      )(
        gif"ytai_CiVolevaQuestAcquaGif.mp4",
        vid"ytai_CiVolevaQuestAcqua.mp4",
        mp3"ytai_CiVolevaQuestAcqua.mp3"
      ),
    ReplyBundleMessage.textToMedia[F](
      "tanti(ssimi)? auguri".r.tr(12)
    )(
      mp3"ytai_TantiAuguri.mp3",
      vid"ytai_TantiAuguri.mp4",
      gif"ytai_TantiAuguriGif.mp4",
      gif"ytai_TantiAuguri2Gif.mp4",
      gif"ytai_TantissimiAuguriGif.mp4",
      vid"ytai_TantissimiAuguri.mp4",
      mp3"ytai_TantissimiAuguri.mp3"
    ),
    ReplyBundleMessage
      .textToMedia[F](
        "youtubo anche io"
      )(
        gif"ytai_TantissimiAuguriGif.mp4",
        vid"ytai_TantissimiAuguri.mp4",
        mp3"ytai_TantissimiAuguri.mp3",
        gif"ytai_SpuntinoColPaninoYoutuboAncheIoGif.mp4",
        mp3"ytai_SpuntinoColPaninoYoutuboAncheIo.mp3",
        vid"ytai_SpuntinoColPaninoYoutuboAncheIo.mp4"
      ),
    ReplyBundleMessage
      .textToMedia[F](
        "ginevra",
        "rigorosamente",
        "non dimentichiamoci mai"
      )(
        gif"ytai_AcquaGinevraGif.mp4",
        vid"ytai_AcquaGinevra.mp4",
        mp3"ytai_AcquaGinevra.mp3"
      ),
    ReplyBundleMessage
      .textToMedia[F](
        "guardate"
      )(
        vid"ytai_OliveGrandiMeravigliose.mp4",
        mp3"ytai_OliveGrandiMeravigliose.mp3",
        gif"ytai_OliveGrandiMeraviglioseGif.mp4",
        vid"ytai_BelloRavanello.mp4",
        mp3"ytai_BelloRavanello.mp3",
        gif"ytai_BelloRavanelloGif.mp4"
      ),
    ReplyBundleMessage
      .textToMedia[F](
        "meravigliose",
        "olive"
      )(
        vid"ytai_OliveGrandiMeravigliose.mp4",
        mp3"ytai_OliveGrandiMeravigliose.mp3",
        gif"ytai_OliveGrandiMeraviglioseGif.mp4"
      ),
    ReplyBundleMessage
      .textToMedia[F](
        "zucchero",
        "come volevasi (confermare|dimostrare)".r.tr(24)
      )(
        vid"ytai_ZuccheroCaffeOttimo.mp4",
        mp3"ytai_ZuccheroCaffeOttimo.mp3",
        gif"ytai_ZuccheroCaffeOttimoGif.mp4"
      ),
    ReplyBundleMessage.textToMedia[F](
      "ottimo[!]+".r.tr(7)
    )(
      gif"ytai_OttimoGif.mp4",
      vid"ytai_ZuccheroCaffeOttimo.mp4",
      mp3"ytai_ZuccheroCaffeOttimo.mp3",
      gif"ytai_ZuccheroCaffeOttimoGif.mp4"
    ),
    ReplyBundleMessage
      .textToMedia[F](
        "wikipedia",
        "apprendere",
        "notizie"
      )(
        vid"ytai_Wikipedia.mp4",
        mp3"ytai_Wikipedia.mp3",
        gif"ytai_WikipediaGif.mp4"
      ),
    ReplyBundleMessage
      .textToMedia[F](
        "\\bmorso\\b".r.tr(5),
        "di unico\\b".r.tr(8)
      )(
        gif"ytai_MorsoUnicoGif.mp4",
        mp3"ytai_MorsoUnico.mp3",
        vid"ytai_MorsoUnico.mp4"
      ),
    ReplyBundleMessage.textToMedia[F](
      "il silenzio"
    )(
      gif"ytai_SilenzioMomentoMagicoGif.mp4",
      gif"ytai_SilenzioVirtuGif.mp4",
      mp3"ytai_SilenzioVirtu.mp3",
      vid"ytai_SilenzioVirtu.mp4"
    ),
    ReplyBundleMessage
      .textToMedia[F](
        "virtù",
        "quando si mangia"
      )(
        gif"ytai_SilenzioVirtuGif.mp4",
        mp3"ytai_SilenzioVirtu.mp3",
        vid"ytai_SilenzioVirtu.mp4"
      ),
    ReplyBundleMessage
      .textToMedia[F](
        "ravanello",
        "vedete",
        "quant'è bello"
      )(
        gif"ytai_BelloRavanelloGif.mp4",
        mp3"ytai_BelloRavanello.mp3",
        vid"ytai_BelloRavanello.mp4"
      ),
    ReplyBundleMessage
      .textToMedia[F](
        "pecore",
        "pecora",
        "🐑",
        "loro latte",
        "loro compagnia",
        "casa con giardino",
        "uccellini"
      )(
        gif"ytai_AmoLePecoreGif.mp4",
        mp3"ytai_AmoLePecore.mp3",
        vid"ytai_AmoLePecore.mp4"
      ),
    ReplyBundleMessage.textToMedia[F](
      "ciccione"
    )(
      gif"ytai_MollaGif.mp4",
      gif"ytai_CiccioneObesoMangiTantoGif.mp4",
      gif"ytai_CiccioneGif.mp4",
      mp3"ytai_Ciccione.mp3",
      vid"ytai_Ciccione.mp4"
    ),
    ReplyBundleMessage
      .textToMedia[F](
        "scarpett[ae]".r.tr(9),
        "perchè no?"
      )(
        gif"ytai_CiccioneGif.mp4",
        mp3"ytai_Ciccione.mp3",
        vid"ytai_Ciccione.mp4"
      ),
    ReplyBundleMessage
      .textToMedia[F](
        "pazienza",
        "disiscriverete",
        "ripartirò",
        "da (cento[ ]?quaranta|140|130|cento[ ]?trenta|20|venti|diecimila|10000)\\b".r.tr(5)
      )(
        gif"ytai_ViDisisrivetePazienzaGif.mp4",
        mp3"ytai_ViDisisrivetePazienza.mp3",
        vid"ytai_ViDisisrivetePazienza.mp4"
      ),
    ReplyBundleMessage
      .textToGif[F](
        "\\bmacchia\\b".r.tr(7),
        "pulire",
        "camicia"
      )(
        gif"ytai_LoopCamiciaGif.mp4"
      ),
    ReplyBundleMessage
      .textToMedia[F](
        "fiatone",
        "stanco",
        "non potete (neanche )?immaginare".r.tr(21),
        "le tensioni",
        "le questioni",
        "in sospeso"
      )(
        vid"ytai_FiatoneStanco.mp4",
        mp3"ytai_FiatoneStanco.mp3",
        gif"ytai_FiatoneStancoGif.mp4"
      ),
    ReplyBundleMessage
      .textToMedia[F](
        "farcita",
        "abbondantemente",
        "generosamente"
      )(
        vid"ytai_FarcitaAbbondantementeGenerosamente.mp4",
        mp3"ytai_FarcitaAbbondantementeGenerosamente.mp3",
        gif"ytai_FarcitaAbbondantementeGenerosamenteGif.mp4"
      ),
    ReplyBundleMessage.textToMedia[F](
      "caspita"
    )(
      gif"ytai_PallaDiLardoGif.mp4",
      vid"ytai_FarcitaAbbondantementeGenerosamente.mp4",
      mp3"ytai_FarcitaAbbondantementeGenerosamente.mp3",
      gif"ytai_FarcitaAbbondantementeGenerosamenteGif.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      "lo chiudiamo",
      "non dovesse scappare",
      "sottraendosi",
      "fauci"
    )(
      vid"ytai_SottraendosiAlleMieFauci.mp4",
      mp3"ytai_SottraendosiAlleMieFauci.mp3",
      gif"ytai_SottraendosiAlleMieFauciGif.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      "nel mio cuore"
    )(
      vid"ytai_SempreNelMioCuore.mp4",
      mp3"ytai_SempreNelMioCuore.mp3",
      gif"ytai_SempreNelMioCuoreGif.mp4",
      gif"ytai_NelMioCuoreContinuazioneCariAmiciFollowersGif.mp4",
      vid"ytai_NelMioCuoreContinuazioneCariAmiciFollowers.mp4",
      mp3"ytai_NelMioCuoreContinuazioneCariAmiciFollowers.mp3"
    ),
    ReplyBundleMessage.textToMedia[F](
      "crostatina",
      "ananas"
    )(
      vid"ytai_CrostatinaAnanas.mp4",
      mp3"ytai_CrostatinaAnanas.mp3",
      gif"ytai_CrostatinaAnanasGif.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      "sete",
      "(sorso|bicchiere) d'acqua".r.tr(13)
    )(
      gif"ytai_SeteGif.mp4",
      gif"ytai_AcquaMeravigliosaGif.mp4",
      gif"ytai_FameSeteNotturnaGif.mp4",
      pho"ytai_Acqua.jpg"
    ),
    ReplyBundleMessage.textToMedia[F](
      "per il momento",
      "cannolo"
    )(
      vid"ytai_Cannolo.mp4",
      mp3"ytai_Cannolo.mp3",
      gif"ytai_CannoloGif.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      "buon compleanno"
    )(
      mp3"ytai_BuonCompleannoCariAmici.mp3",
      vid"ytai_BuonCompleannoCariAmici.mp4",
      gif"ytai_BuonCompleannoCariAmiciGif.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      "alla (tua )?salute".r.tr(11)
    )(
      gif"ytai_AllaSaluteGif.mp4",
      mp3"ytai_AllaTuaSalute.mp3",
      vid"ytai_AllaTuaSalute.mp4",
      gif"ytai_AllaTuaSaluteGif.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      "stemperata",
      "ottimamente"
    )(
      vid"ytai_AcquaQuasiOttimamenteStemperata.mp4",
      mp3"ytai_AcquaQuasiOttimamenteStemperata.mp3",
      gif"ytai_AcquaQuasiOttimamenteStemperataGif.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      "spuntino"
    )(
      gif"ytai_SpuntinoConMeGif.mp4",
      gif"ytai_SpuntinoConMe2Gif.mp4",
      gif"ytai_SpuntinoConMe3Gif.mp4",
      gif"ytai_BuonoSpuntinoGif.mp4",
      gif"ytai_PaninoBuonoSpuntitoGif.mp4",
      gif"ytai_SpuntinoSmartGif.mp4",
      gif"ytai_BuonoSpuntinoAncheATeGif.mp4",
      gif"ytai_SpuntinoColPaninoYoutuboAncheIoGif.mp4",
      mp3"ytai_SpuntinoColPaninoYoutuboAncheIo.mp3",
      vid"ytai_SpuntinoColPaninoYoutuboAncheIo.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      "panino"
    )(
      gif"ytai_PaninoBuonoSpuntitoGif.mp4",
      gif"ytai_PaninoGif.mp4",
      gif"ytai_PaninoAlpinoGif.mp4",
      gif"ytai_LoopPaninoCottoGalbanoneGif.mp4",
      gif"ytai_SpuntinoColPaninoYoutuboAncheIoGif.mp4",
      mp3"ytai_SpuntinoColPaninoYoutuboAncheIo.mp3",
      vid"ytai_SpuntinoColPaninoYoutuboAncheIo.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      "nel bene",
      "nel male"
    )(
      vid"ytai_CiaoFollowersNelBeneNelMaleGif.mp4",
      vid"ytai_CiaoFollowersNelBeneNelMale.mp4",
      mp3"ytai_CiaoFollowersNelBeneNelMale.mp3"
    )
  )
end Mix
