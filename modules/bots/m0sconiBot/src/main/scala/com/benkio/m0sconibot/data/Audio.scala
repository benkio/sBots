package com.benkio.m0sconibot.data

import com.benkio.telegrambotinfrastructure.model.reply.mp3
import com.benkio.telegrambotinfrastructure.model.reply.ReplyBundleMessage
import com.benkio.telegrambotinfrastructure.model.tr

object Audio {

  def messageRepliesAudioData: List[ReplyBundleMessage] = List(
    ReplyBundleMessage.textToMedia(
      "va l[aà]".r
    )(
      mp3"mos_ChiudiQuellaPortaPerFavore.mp3",
      mp3"mos_AspettaUnSecondoDioCane.mp3",
      mp3"mos_DaiVaLaRipartiamoSubito.mp3",
      mp3"mos_VaffanuloDai.mp3"
    ),
    ReplyBundleMessage.textToMedia(
      "(quella|la) porta".r
    )(
      mp3"mos_AvantiNdrioConQuellaPortaLi.mp3",
      mp3"mos_ChiEQuelMonaSbatteLaPortaChiudeUrlando.mp3",
      mp3"mos_ChiudiQuellaPortaDai.mp3",
      mp3"mos_ChiudiQuellaPortaPerFavore.mp3",
      mp3"mos_SerraLaPortaDioCane.mp3"
    ),
    ReplyBundleMessage.textToMedia(
      "\\bmona\\b".r
    )(
      mp3"mos_AndateInMona.mp3",
      mp3"mos_ChiEQuelMonaSbatteLaPortaChiudeUrlando.mp3",
      mp3"mos_EntraDentroMona.mp3",
      mp3"mos_NoVaInMonaNonTornoIndrio.mp3",
      mp3"mos_VaInMonaCortesiaSimpatiaPorcoDioVaInCasinoVaffanculo.mp3"
    ),
    ReplyBundleMessage.textToMedia(
      "dio cane"
    )(
      mp3"mos_AndateInMona.mp3",
      mp3"mos_AntonioFossoCarteColla.mp3",
      mp3"mos_AspettaUnSecondoDioCane.mp3",
      mp3"mos_AvantiNdrioConQuellaPortaLi.mp3",
      mp3"mos_BasketDioCaneDipenDipanDaCapo.mp3",
      mp3"mos_DioCane.mp3",
      mp3"mos_DioCane2.mp3",
      mp3"mos_DioCaneLaMadonna.mp3",
      mp3"mos_DioCaneMaNonEPossibile.mp3",
      mp3"mos_DioCaneX3MadonnaPuttana.mp3",
      mp3"mos_DioSchifosoCan.mp3",
      mp3"mos_InPrimoPianoIlCalcioDioSchifosoCan.mp3",
      mp3"mos_Innervosire.mp3",
      mp3"mos_MagistratoCuratoreFallimentareExDioPorcoDioCane.mp3",
      mp3"mos_OrcoDioMadonnaDeDioCaneTuttoDaCapoNonVeniteDentroDistrattoDioBonoDeDio.mp3",
      mp3"mos_SerraLaPortaDioCane.mp3"
    ),
    ReplyBundleMessage.textToMedia(
      "madonna"
    )(
      mp3"mos_DioCaneLaMadonna.mp3",
      mp3"mos_DioCaneX3MadonnaPuttana.mp3",
      mp3"mos_DioPorcaMadonnaDeDio.mp3",
      mp3"mos_MaEPossibilePortannaLaMadonna.mp3",
      mp3"mos_MadonnaDeDio.mp3",
      mp3"mos_MadonnaPuttana.mp3",
      mp3"mos_MadonnaPuttina.mp3",
      mp3"mos_OrcoDioMadonnaDeDioCaneTuttoDaCapoNonVeniteDentroDistrattoDioBonoDeDio.mp3",
      mp3"mos_TennisVaffanculoDioPorcoMadonnaPuttana.mp3"
    ),
    ReplyBundleMessage.textToMedia(
      "dio porco"
    )(
      mp3"mos_BasketSeiVantaggiDioPorco.mp3",
      mp3"mos_BresciaFoggia.mp3",
      mp3"mos_CittaDioPorco.mp3",
      mp3"mos_CoppaDelleCoppeSampdoriaDioPorco.mp3",
      mp3"mos_DioPorcaMadonnaDeDio.mp3",
      mp3"mos_DioPorco.mp3",
      mp3"mos_DioPorco2.mp3",
      mp3"mos_DioPorco3.mp3",
      mp3"mos_DioPorcoCheNotiziaDioCaneBoia.mp3",
      mp3"mos_MagistratoCuratoreFallimentareExDioPorcoDioCane.mp3",
      mp3"mos_SeVeniteAvantiAncoraViDoUnPunio.mp3",
      mp3"mos_TennisVaffanculoDioPorcoMadonnaPuttana.mp3",
      mp3"mos_VaffanculoDioPorco.mp3",
      mp3"mos_VaffanuloDai.mp3"
    ),
    ReplyBundleMessage.textToMedia(
      "dio boia"
    )(
      mp3"mos_DioPorcoCheNotiziaDioCaneBoia.mp3",
      mp3"mos_NoVaInMonaNonTornoIndrio.mp3",
      mp3"mos_DioBoiaSenzaMaiuscolePunti.mp3"
    ),
    ReplyBundleMessage.textToMedia(
      "de dio"
    )(
      mp3"mos_BresciaFoggia.mp3",
      mp3"mos_DioPorcaMadonnaDeDio.mp3",
      mp3"mos_MadonnaDeDio.mp3",
      mp3"mos_OrcoDioMadonnaDeDioCaneTuttoDaCapoNonVeniteDentroDistrattoDioBonoDeDio.mp3",
      mp3"mos_PilotaRomanoAndreaDeCesariDioCanariaDeDio.mp3"
    ),
    ReplyBundleMessage.textToMedia(
      "a[h]? n[o]?n lo so".r
    )(
      mp3"mos_AhNonLoSo.mp3"
    ),
    ReplyBundleMessage.textToMedia(
      "non posso (mica )?improvvisar(e|me)".r.tr(22)
    )(
      mp3"mos_AndateInMona.mp3"
    ),
    ReplyBundleMessage.textToMedia(
      "andiamo dai",
      "lo fa apposta"
    )(
      mp3"mos_AndiamoDai.mp3",
      mp3"mos_MarioFerrettoPorcoDioRumoreLiFaAposta.mp3"
    ),
    ReplyBundleMessage.textToMedia(
      "\\b(in)?colla\\b".r,
      "le carte"
    )(
      mp3"mos_DioBonDiUnDioLeSeaTagaENonLeSeStaga.mp3",
      mp3"mos_AntonioFossoCarteColla.mp3",
      mp3"mos_GuardaCheRobaLaCollaAttaccataAlleCarte.mp3"
    ),
    ReplyBundleMessage.textToMedia(
      "dio bono"
    )(
      mp3"mos_DioBonDiUnDioLeSeaTagaENonLeSeStaga.mp3",
      mp3"mos_DioBono.mp3"
    ),
    ReplyBundleMessage.textToMedia(
      "antonio fosso",
      "neo brigatista",
      "rigurgito",
      "terrorismo"
    )(
      mp3"mos_AntonioFossoCarteColla.mp3"
    ),
    ReplyBundleMessage.textToMedia(
      "ostia"
    )(
      mp3"mos_OrcoDioMadonnaDeDioCaneTuttoDaCapoNonVeniteDentroDistrattoDioBonoDeDio.mp3",
      mp3"mos_AntonioFossoCarteColla.mp3"
    ),
    ReplyBundleMessage.textToMedia(
      "aspetta un secondo"
    )(
      mp3"mos_AspettaUnSecondoDioCane.mp3"
    ),
    ReplyBundleMessage.textToMedia(
      "avanti (indrio|indietro)".r
    )(
      mp3"mos_AvantiNdrioConQuellaPortaLi.mp3"
    ),
    ReplyBundleMessage.textToMedia(
      "marco bisagno",
      "la minoranza",
      "assemblea",
      "irregolare"
    )(
      mp3"mos_AvvocatoMarcoBisagnoAssembleaIrregolareMinoranze.mp3"
    ),
    ReplyBundleMessage.textToMedia(
      "basket",
      "pallacanestro"
    )(
      mp3"mos_BasketSeiVantaggiDioPorco.mp3",
      mp3"mos_BasketDioCaneDipenDipanDaCapo.mp3"
    ),
    ReplyBundleMessage.textToMedia(
      "palasport",
      "piazzale olimpia",
      "udine",
      "di[tp]an".r,
      "diche",
      "\\bdipen\\b".r
    )(
      mp3"mos_BasketDioCaneDipenDipanDaCapo.mp3"
    ),
    ReplyBundleMessage.textToMedia(
      "manovre offersive",
      "imprecisi al tiro"
    )(
      mp3"mos_BasketSeiVantaggiDioPorco.mp3"
    ),
    ReplyBundleMessage.textToMedia(
      "quadro completo",
      "tanto tempo anche",
      "brescia(-| )foggia".r,
      "cagliari(-| )roma".r,
      "fiorentina(-| )milan".r,
      "genova(-| )atalanta".r,
      "\\binter\\b".r
    )(
      mp3"mos_BresciaFoggia.mp3"
    ),
    ReplyBundleMessage.textToMedia(
      "buongiorno"
    )(
      mp3"mos_Buongiorno.mp3",
      mp3"mos_Buongiorno2.mp3"
    ),
    ReplyBundleMessage.textToMedia(
      "stata una giornata"
    )(
      mp3"mos_Buongiorno2.mp3"
    ),
    ReplyBundleMessage.textToMedia(
      "but(tato|ta) per aria tutto".r.tr(20)
    )(
      mp3"mos_ButtaPellAria.mp3"
    ),
    ReplyBundleMessage.textToMedia(
      "cos'è che è caduto"
    )(
      mp3"mos_Caduto.mp3"
    ),
    ReplyBundleMessage.textToMedia(
      "non si riesce a capire un cazzo"
    )(
      mp3"mos_CapireUnCazzo.mp3"
    ),
    ReplyBundleMessage.textToMedia(
      "chiude urlando"
    )(
      mp3"mos_ChiEQuelMonaSbatteLaPortaChiudeUrlando.mp3"
    ),
    ReplyBundleMessage.textToMedia(
      "chissà che non m(i |')incazzo".r.tr(24)
    )(
      mp3"mos_ChissaCheNonMIncazzaEh.mp3"
    ),
    ReplyBundleMessage.textToMedia(
      "mancalacqua",
      "lugagnano",
      "vigasio",
      "poveliano",
      "tosse"
    )(
      mp3"mos_CiclismoAllieviDio.mp3",
      mp3"mos_TosseDio.mp3"
    ),
    ReplyBundleMessage.textToMedia(
      "ciclismo"
    )(
      mp3"mos_CiclismoAllieviDio.mp3",
      mp3"mos_CiclismoGianniBugnoRitardo.mp3"
    ),
    ReplyBundleMessage.textToMedia(
      "nostra città"
    )(
      mp3"mos_CittaDioPorco.mp3"
    ),
    ReplyBundleMessage.textToMedia(
      "coppa delle coppe",
      "sampdoria"
    )(
      mp3"mos_CoppaDelleCoppeSampdoriaDioPorco.mp3"
    ),
    ReplyBundleMessage.textToMedia(
      "(\\b|^)orco dio(\\b|^)".r.tr(8)
    )(
      mp3"mos_CortiOrcoDio.mp3",
      mp3"mos_Formula1DonningtonPrimaSessioneVaffanculo.mp3",
      mp3"mos_OrcoDio.mp3",
      mp3"mos_OrcoDioMadonnaDeDioCaneTuttoDaCapoNonVeniteDentroDistrattoDioBonoDeDio.mp3"
    ),
    ReplyBundleMessage.textToMedia(
      "che cazzo ([cg]he )è qua sotto".r.tr(25)
    )(
      mp3"mos_CosaCheCazzoGheE.mp3"
    ),
    ReplyBundleMessage.textToMedia(
      "ripartiamo subito"
    )(
      mp3"mos_DaiVaLaRipartiamoSubito.mp3"
    ),
    ReplyBundleMessage.textToMedia(
      "\\bsigla\\b".r
    )(
      mp3"mos_DallaSiglaDai.mp3",
      mp3"mos_Sigla.mp3"
    ),
    ReplyBundleMessage.textToMedia(
      "\\b(difa|ddbb|babba)\\b".r,
      "farfugl(i|a|iare|iamento)".r
    )(
      mp3"mos_Difabbddffbbaa.mp3"
    ),
    ReplyBundleMessage.textToMedia(
      "non è possibile"
    )(
      mp3"mos_DioCaneMaNonEPossibile.mp3"
    ),
    ReplyBundleMessage.textToMedia(
      "dio (pa[ ]?){3,}".r.tr(10)
    )(
      mp3"mos_DioPaPaPaPaPaPa.mp3"
    ),
    ReplyBundleMessage.textToMedia(
      "che notizia"
    )(
      mp3"mos_DioPorcoCheNotiziaDioCaneBoia.mp3"
    ),
    ReplyBundleMessage.textToMedia(
      "dio schifoso"
    )(
      mp3"mos_DioSchifosoCan.mp3",
      mp3"mos_InPrimoPianoIlCalcioDioSchifosoCan.mp3"
    ),
    ReplyBundleMessage.textToMedia(
      "dios"
    )(
      mp3"mos_Dios.mp3",
      mp3"mos_RiprendoDallaTabella.mp3"
    ),
    ReplyBundleMessage.textToMedia(
      "entra dentro"
    )(
      mp3"mos_EntraDentroMona.mp3"
    ),
    ReplyBundleMessage.textToMedia(
      "formula (uno|1)".r,
      "donington",
      "prima sessione"
    )(
      mp3"mos_Formula1DonningtonPrimaSessioneVaffanculo.mp3"
    ),
    ReplyBundleMessage.textToMedia(
      "vaffanculo"
    )(
      mp3"mos_Formula1DonningtonPrimaSessioneVaffanculo.mp3",
      mp3"mos_TennisVaffanculoDioPorcoMadonnaPuttana.mp3",
      mp3"mos_VaInMonaCortesiaSimpatiaPorcoDioVaInCasinoVaffanculo.mp3",
      mp3"mos_Vaffanculo.mp3",
      mp3"mos_VaffanculoDioPorco.mp3"
    ),
    ReplyBundleMessage.textToMedia(
      "ges[uù] cristo".r,
      "no nessuno"
    )(
      mp3"mos_GesuCristoNoNessuno.mp3"
    ),
    ReplyBundleMessage.textToMedia(
      "giochi olimpici",
      "giorgio mazzetta",
      "giorgio mazzetta",
      "m[ie] fermo un (secondo|attimo)".r.tr(18)
    )(
      mp3"mos_GiochiOlimpiciAspettaCheMeFermoUnAttimo.mp3"
    ),
    ReplyBundleMessage.textToMedia(
      "girato le foto"
    )(
      mp3"mos_GiratoLeFoto.mp3"
    ),
    ReplyBundleMessage.textToMedia(
      "imbecilli"
    )(
      mp3"mos_Imbecilli.mp3"
    ),
    ReplyBundleMessage.textToMedia(
      "in primo piano"
    )(
      mp3"mos_InPrimoPianoIlCalcioDioSchifosoCan.mp3"
    ),
    ReplyBundleMessage.textToMedia(
      "mi ha fatto innervosire",
      "gli spacco la testa"
    )(
      mp3"mos_Innervosire.mp3"
    ),
    ReplyBundleMessage.textToMedia(
      "juventus",
      "austria",
      "vienna"
    )(
      mp3"mos_JuventusAustriaViennaPorcoDio.mp3"
    ),
    ReplyBundleMessage.textToMedia(
      "(\\b|^)porco[ ]?[d]+[i]+o".r.tr(8)
    )(
      mp3"mos_MaPorcoDio.mp3",
      mp3"mos_JuventusAustriaViennaPorcoDio.mp3",
      mp3"mos_MarioFerrettoPorcoDioRumoreLiFaAposta.mp3",
      mp3"mos_PiovanelliPorcoDio.mp3",
      mp3"mos_PorcoDio4.mp3",
      mp3"mos_QuotazioniPorcoDio.mp3",
      mp3"mos_TelefonoPorcoDio.mp3",
      mp3"mos_VaInMonaCortesiaSimpatiaPorcoDioVaInCasinoVaffanculo.mp3"
    ),
    ReplyBundleMessage.textToMedia(
      "lo troverò",
      "prima o dopo"
    )(
      mp3"mos_LoTroveroDeficiente.mp3"
    ),
    ReplyBundleMessage.textToMedia(
      "ma è possibile"
    )(
      mp3"mos_MaEPossibilePortannaLaMadonna.mp3"
    ),
    ReplyBundleMessage.textToMedia(
      "puttina"
    )(
      mp3"mos_MadonnaPuttina.mp3"
    ),
    ReplyBundleMessage.textToMedia(
      "il commercialista",
      "il magistrato",
      "gianfranco bertani"
    )(
      mp3"mos_MagistratoCuratoreFallimentareExDioPorcoDioCane.mp3"
    ),
    ReplyBundleMessage.textToMedia(
      "mario ferretto",
      "accolto con sorpresa",
      "rumore li"
    )(
      mp3"mos_MarioFerrettoPorcoDioRumoreLiFaAposta.mp3"
    ),
    ReplyBundleMessage.textToMedia(
      "mi vedo già"
    )(
      mp3"mos_MeVedoGia.mp3"
    ),
    ReplyBundleMessage.textToMedia(
      "mi sono distratto",
      "mi hai distratto"
    )(
      mp3"mos_MiSonoDistratto.mp3",
      mp3"mos_OrcoDioMadonnaDeDioCaneTuttoDaCapoNonVeniteDentroDistrattoDioBonoDeDio.mp3"
    ),
    ReplyBundleMessage.textToMedia(
      "non torno (indietro|indrio)".r
    )(
      mp3"mos_NoVaInMonaNonTornoIndrio.mp3",
      mp3"mos_NonTornoIndietroDonadoni.mp3"
    ),
    ReplyBundleMessage.textToMedia(
      "monitor"
    )(
      mp3"mos_NonCapiscoPiuNienteMonitor.mp3"
    ),
    ReplyBundleMessage.textToMedia(
      "non capisco più niente",
      "non riesco a capire più niente"
    )(
      mp3"mos_NonRiescoACapireNiente.mp3",
      mp3"mos_NonCapiscoPiuNienteMonitor.mp3"
    ),
    ReplyBundleMessage.textToMedia(
      "donadoni"
    )(
      mp3"mos_NonTornoIndietroDonadoni.mp3"
    ),
    ReplyBundleMessage.textToMedia(
      "in maiuscolo"
    )(
      mp3"mos_NotizieInMaiuscolo.mp3"
    ),
    ReplyBundleMessage.textToMedia(
      "notizie"
    )(
      mp3"mos_NotizieScritteDaCul.mp3",
      mp3"mos_NotizieInMaiuscolo.mp3"
    ),
    ReplyBundleMessage.textToMedia(
      "da capo",
      "venite dentro"
    )(
      mp3"mos_OrcoDioMadonnaDeDioCaneTuttoDaCapoNonVeniteDentroDistrattoDioBonoDeDio.mp3"
    ),
    ReplyBundleMessage.textToMedia(
      "orientarmi"
    )(
      mp3"mos_Orientarmi.mp3"
    ),
    ReplyBundleMessage.textToMedia(
      "andrea de cesari",
      "romano",
      "dio canaia",
      "il pilota"
    )(
      mp3"mos_PilotaRomanoAndreaDeCesariDioCanariaDeDio.mp3"
    ),
    ReplyBundleMessage.textToMedia(
      "piovanelli",
      "atalanta"
    )(
      mp3"mos_PiovanelliPorcoDio.mp3"
    ),
    ReplyBundleMessage.textToMedia(
      "dio bub[uù]".r
    )(
      mp3"mos_PiproviamoDioBubu.mp3"
    ),
    ReplyBundleMessage.textToMedia(
      "quotazioni"
    )(
      mp3"mos_QuotazioniPorcoDio.mp3"
    ),
    ReplyBundleMessage.textToMedia(
      "ricominciamo da capo"
    )(
      mp3"mos_RicominciamoDaCapo.mp3"
    ),
    ReplyBundleMessage.textToMedia(
      "dalla tabella"
    )(
      mp3"mos_RiprendoDallaTabella.mp3"
    ),
    ReplyBundleMessage.textToMedia(
      "sbattere le porte"
    )(
      mp3"mos_SbattereLePorte.mp3"
    ),
    ReplyBundleMessage.textToMedia(
      "se non bestemmio"
    )(
      mp3"mos_SeNonBestemmioGuarda.mp3"
    ),
    ReplyBundleMessage.textToMedia(
      "vi do un pu[g]?nio".r,
      "se venite avanti"
    )(
      mp3"mos_SeVeniteAvantiAncoraViDoUnPunio.mp3"
    ),
    ReplyBundleMessage.textToMedia(
      "(sei|6) vantaggi".r
    )(
      mp3"mos_SeiVantaggiDiLunghezza.mp3",
      mp3"mos_BasketSeiVantaggiDioPorco.mp3"
    ),
    ReplyBundleMessage.textToMedia(
      "tecnocop",
      "verona"
    )(
      mp3"mos_TecnocopVerona.mp3"
    ),
    ReplyBundleMessage.textToMedia(
      "telefono"
    )(
      mp3"mos_TelefonoPorcoDio.mp3"
    ),
    ReplyBundleMessage.textToMedia(
      "tennis"
    )(
      mp3"mos_TennisVaffanculoDioPorcoMadonnaPuttana.mp3"
    ),
    ReplyBundleMessage.textToMedia(
      "tocca ferro"
    )(
      mp3"mos_ToccaFerro.mp3"
    ),
    ReplyBundleMessage.textToMedia(
      "tutte le volte che"
    )(
      mp3"mos_TutteLeVolteCheParto.mp3"
    ),
    ReplyBundleMessage.textToMedia(
      "cortesia",
      "simpatia",
      "va in casino"
    )(
      mp3"mos_VaInMonaCortesiaSimpatiaPorcoDioVaInCasinoVaffanculo.mp3"
    ),
    ReplyBundleMessage.textToMedia(
      "vice sindaco"
    )(
      mp3"mos_ViceSindaco.mp3"
    ),
    ReplyBundleMessage.textToMedia(
      "vedo tutto tranne",
      "quello che dovrei vedere"
    )(
      mp3"mos_VedoTuttoMenoQuelloCheDovreiVedere.mp3"
    ),
    ReplyBundleMessage.textToMedia(
      "cesena",
      "\\btorino\\b".r,
      "avellino",
      "walter shakner",
      "austriaco"
    )(
      mp3"mos_WalterShaknerAustriacoCesenaTorinoAvellino.mp3"
    ),
    ReplyBundleMessage.textToMedia(
      "gaetano"
    )(
      mp3"mos_Gaetano.mp3"
    ),
    ReplyBundleMessage
      .textToMp3(
        "maiuscole",
        "\\bpunti\\b".r
      )(
        mp3"mos_DioBoiaSenzaMaiuscolePunti.mp3"
      )
  )
} // end Audio
