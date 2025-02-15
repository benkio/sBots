package com.benkio.youtuboanchei0bot.data

import cats.Applicative
import com.benkio.telegrambotinfrastructure.model.reply.gif
import com.benkio.telegrambotinfrastructure.model.reply.ReplyBundleMessage
import com.benkio.telegrambotinfrastructure.model.tr

object Gif:

  def messageRepliesGifData[
      F[_]: Applicative
  ]: List[ReplyBundleMessage[F]] = List(
    ReplyBundleMessage.textToGif[F](
      "reputazione"
    )(
      gif"ytai_LaReputazioneGif.mp4",
      gif"ytai_CheVergognaGif.mp4"
    ),
    ReplyBundleMessage.textToGif[F](
      "ciccione"
    )(
      gif"ytai_MollaGif.mp4",
      gif"ytai_CiccioneObesoMangiTantoGif.mp4"
    ),
    ReplyBundleMessage.textToGif[F](
      "fatica",
      "sudore",
      "sudato"
    )(
      gif"ytai_AffaticamentoGif.mp4"
    ),
    ReplyBundleMessage.textToGif[F](
      "ascolta (queste|le) mie parole".r.tr(21),
      "amareggiati",
      "dedicaci (il tuo tempo|le tue notti)".r.tr(21)
    )(
      gif"ytai_AmareggiatiGif.mp4"
    ),
    ReplyBundleMessage.textToGif[F](
      "\\barchitetta\\b".r.tr(10),
      "\\bnotaia\\b".r.tr(6),
      "\\bministra\\b".r.tr(8),
      "\\bavvocata\\b".r.tr(8)
    )(
      gif"ytai_ArchitettaGif.mp4"
    ),
    ReplyBundleMessage.textToGif[F](
      "bel sogno"
    )(
      gif"ytai_BelSognoGif.mp4"
    ),
    ReplyBundleMessage.textToGif[F](
      "brivido",
      "fremito",
      "tremito",
      "tremore"
    )(
      gif"ytai_BrividoGif.mp4"
    ),
    ReplyBundleMessage.textToGif[F](
      "buonanotte"
    )(
      gif"ytai_BuonanotteGif.mp4",
      gif"ytai_BuonanotteBrunchPlusGif.mp4",
      gif"ytai_BuonanotteFollowersGif.mp4"
    ),
    ReplyBundleMessage.textToGif[F](
      "buonasera"
    )(
      gif"ytai_BuonaseraGif.mp4"
    ),
    ReplyBundleMessage.textToGif[F](
      "che spettacolo"
    )(
      gif"ytai_CheSpettacoloGif.mp4"
    ),
    ReplyBundleMessage.textToGif[F](
      "ciao ragazzi",
      "cari saluti"
    )(
      gif"ytai_CiaoRagazziGif.mp4"
    ),
    ReplyBundleMessage.textToGif[F](
      "ci divertiremo",
      "bel percorso"
    )(
      gif"ytai_CiDivertiremoPercorsoGif.mp4"
    ),
    ReplyBundleMessage.textToGif[F](
      "corpulenti",
      "ciccioni"
    )(
      gif"ytai_CorpulentiCiccioniGif.mp4"
    ),
    ReplyBundleMessage.textToGif[F](
      "culetto"
    )(
      gif"ytai_CulettoGif.mp4"
    ),
    ReplyBundleMessage.textToGif[F](
      "ed allora"
    )(
      gif"ytai_EdAlloraGif.mp4"
    ),
    ReplyBundleMessage.textToGif[F](
      "fai pure"
    )(
      gif"ytai_FaiPureGif.mp4"
    ),
    ReplyBundleMessage.textToGif[F](
      "fallo anche (tu|te)".r.tr(14)
    )(
      gif"ytai_FalloAncheTuGif.mp4"
    ),
    ReplyBundleMessage.textToGif[F](
      "filet[ -]?o[ -]?fish".r.tr(10)
    )(
      gif"ytai_FiletOFishGif.mp4",
      gif"ytai_FiletOFish2Gif.mp4",
      gif"ytai_BagnarloAcquaGif.mp4"
    ),
    ReplyBundleMessage.textToGif[F](
      "(deluso|insoddisfatto|inappagato|abbattuto|scoraggiato|demoralizzato|depresso|demotivato|avvilito|scocciato)".r
        .tr(6)
    )(
      gif"ytai_FrustrazioneGif.mp4"
    ),
    ReplyBundleMessage.textToGif[F](
      "ci sto già pensando"
    )(
      gif"ytai_GiaPensandoGif.mp4"
    ),
    ReplyBundleMessage.textToGif[F](
      "sono grande",
      "sono corpulento"
    )(
      gif"ytai_GrandeCorpulentoGif.mp4"
    ),
    ReplyBundleMessage.textToGif[F](
      "grazie dottore"
    )(
      gif"ytai_GrazieDottoreGif.mp4"
    ),
    ReplyBundleMessage.textToGif[F](
      "sconforto grave"
    )(
      gif"ytai_GrazieTanteGif.mp4"
    ),
    ReplyBundleMessage.textToGif[F](
      "incredibile profumo",
      "incredibile aroma"
    )(
      gif"ytai_IncredibileAromaProfumoGif.mp4"
    ),
    ReplyBundleMessage.textToGif[F](
      "incredibile",
      "inimitabile",
      "the number (one|1)".r.tr(12)
    )(
      gif"ytai_IncredibileInimitabileGif.mp4"
    ),
    ReplyBundleMessage.textToGif[F](
      "non lo so"
    )(
      gif"ytai_IoNonLoSoGif.mp4"
    ),
    ReplyBundleMessage.textToGif[F](
      "loro dovere",
      "vostro diritto"
    )(
      gif"ytai_LoroDovereVostroDirittoGif.mp4"
    ),
    ReplyBundleMessage.textToGif[F](
      "lo so (bene )?anche io".r.tr(14)
    )(
      gif"ytai_LoSoAncheIoGif.mp4"
    ),
    ReplyBundleMessage.textToGif[F](
      "mah"
    )(
      gif"ytai_MahGif.mp4",
      gif"ytai_Mah2Gif.mp4",
      gif"ytai_Mah3Gif.mp4",
      gif"ytai_ZoomMahGif.mp4"
    ),
    ReplyBundleMessage.textToGif[F](
      "meraviglioso"
    )(
      gif"ytai_MeravigliosoGif.mp4"
    ),
    ReplyBundleMessage.textToGif[F](
      "molla",
      "grassone",
      "ancora non sei morto"
    )(
      gif"ytai_MollaGif.mp4"
    ),
    ReplyBundleMessage.textToGif[F](
      "molto buona"
    )(
      gif"ytai_MoltoBuonaGif.mp4",
      gif"ytai_MoltoBuona2Gif.mp4",
      gif"ytai_BuonaGif.mp4"
    ),
    ReplyBundleMessage.textToGif[F](
      "monoporzioni",
      "mezzo (chilo|kg)".r.tr(8)
    )(
      gif"ytai_MonoporzioniTiramisuGif.mp4"
    ),
    ReplyBundleMessage.textToGif[F](
      "non fa male"
    )(
      gif"ytai_NonFaMaleGif.mp4",
      gif"ytai_AcquaMiglioreDrinkGif.mp4"
    ),
    ReplyBundleMessage.textToGif[F](
      "non la crede nessuno questa cosa",
      "non ci crede nessuno"
    )(
      gif"ytai_NonLaCredeNessunoGif.mp4"
    ),
    ReplyBundleMessage.textToGif[F](
      "non pensiamoci più"
    )(
      gif"ytai_NonPensiamociGif.mp4"
    ),
    ReplyBundleMessage.textToGif[F](
      "incomprensione",
      "non vi capiscono"
    )(
      gif"ytai_NonViCapisconoGif.mp4"
    ),
    ReplyBundleMessage.textToGif[F](
      "occhiolino",
      ";)",
      "😉"
    )(
      gif"ytai_OcchiolinoGif.mp4",
      gif"ytai_Occhiolino2Gif.mp4",
      gif"ytai_Occhiolino3Gif.mp4",
      gif"ytai_OcchiolinoTestaDondolanteGif.mp4"
    ),
    ReplyBundleMessage.textToGif[F](
      "olè"
    )(
      gif"ytai_OleGif.mp4"
    ),
    ReplyBundleMessage.textToGif[F](
      "olè anche io"
    )(
      gif"ytai_OleAncheIoGif.mp4"
    ),
    ReplyBundleMessage.textToGif[F](
      "la perfezione",
      "la nostra tendenza"
    )(
      gif"ytai_PerfezioneTendenzaGif.mp4"
    ),
    ReplyBundleMessage.textToGif[F](
      "profumo meraviglioso"
    )(
      gif"ytai_ProfumoMeravigliosoGif.mp4",
      gif"ytai_ProfumoGamberettiSalmoneGif.mp4"
    ),
    ReplyBundleMessage.textToGif[F](
      "sentiamo il profumo"
    )(
      gif"ytai_ProfumoMeravigliosoGif.mp4"
    ),
    ReplyBundleMessage.textToGif[F](
      "ringraziamento"
    )(
      gif"ytai_RingraziamentoGif.mp4"
    ),
    ReplyBundleMessage.textToGif[F](
      "se non mi aiuta",
      "cosa mi aiuta"
    )(
      gif"ytai_SentiteCheRobaGif.mp4"
    ),
    ReplyBundleMessage.textToGif[F](
      "sete",
      "(sorso|bicchiere) d'acqua".r.tr(13)
    )(
      gif"ytai_SeteGif.mp4",
      gif"ytai_AcquaMeravigliosaGif.mp4",
      gif"ytai_FameSeteNotturnaGif.mp4"
    ),
    ReplyBundleMessage.textToGif[F](
      "🤷"
    )(
      gif"ytai_ShrugGif.mp4"
    ),
    ReplyBundleMessage.textToGif[F](
      "sta per dire qualcosa"
    )(
      gif"ytai_SilenzioGif.mp4"
    ),
    ReplyBundleMessage.textToGif[F](
      "silenzio[,]? silenzio".r.tr(17)
    )(
      gif"ytai_SilenzioGif.mp4",
      gif"ytai_SilenzioMomentoMagicoGif.mp4"
    ),
    ReplyBundleMessage.textToGif[F](
      "si v[àa] finch[eé] si v[aà]".r.tr(18),
      "quando non si potrà andare più",
      "è tanto facile"
    )(
      gif"ytai_SiVaFincheSiVaGif.mp4"
    ),
    ReplyBundleMessage.textToGif[F](
      "sorprendente"
    )(
      gif"ytai_SorprendenteGif.mp4",
      gif"ytai_SecondoBocconeSorprendenteGif.mp4"
    ),
    ReplyBundleMessage.textToGif[F](
      "(😄|😀|😃){3,}".r.tr(3),
      "sorriso"
    )(
      gif"ytai_SorrisoGif.mp4",
      gif"ytai_Sorriso2Gif.mp4"
    ),
    ReplyBundleMessage.textToGif[F](
      "spuntino"
    )(
      gif"ytai_SpuntinoConMeGif.mp4",
      gif"ytai_SpuntinoConMe2Gif.mp4",
      gif"ytai_SpuntinoConMe3Gif.mp4",
      gif"ytai_BuonoSpuntinoGif.mp4",
      gif"ytai_PaninoBuonoSpuntitoGif.mp4",
      gif"ytai_SpuntinoSmartGif.mp4",
      gif"ytai_BuonoSpuntinoAncheATeGif.mp4"
    ),
    ReplyBundleMessage.textToGif[F](
      "l'unica cosa che sai fare"
    )(
      gif"ytai_UnicaCosaMangiareGif.mp4"
    ),
    ReplyBundleMessage.textToGif[F](
      "uno o due"
    )(
      gif"ytai_UnoODueGif.mp4"
    ),
    ReplyBundleMessage.textToGif[F](
      "zenzero",
      "mia risposta"
    )(
      gif"ytai_ZenzeroGif.mp4"
    ),
    ReplyBundleMessage.textToGif[F](
      "zoom",
      "guardo da vicino"
    )(
      gif"ytai_ZoomGif.mp4",
      gif"ytai_ZoomMahGif.mp4"
    ),
    ReplyBundleMessage.textToGif[F](
      "tanti auguri"
    )(
      gif"ytai_TantiAuguriGif.mp4"
    ),
    ReplyBundleMessage.textToGif[F](
      "auguri di gusto"
    )(
      gif"ytai_AuguriDiGustoGif.mp4"
    ),
    ReplyBundleMessage.textToGif[F](
      "bagnarlo"
    )(
      gif"ytai_BagnarloAcquaGif.mp4"
    ),
    ReplyBundleMessage.textToGif[F](
      "benvenuti",
      "ora e sempre"
    )(
      gif"ytai_BenvenutiGif.mp4"
    ),
    ReplyBundleMessage.textToGif[F](
      "diploma",
      "per pisciare",
      "ma (che )?stiamo scherzando".r.tr(20)
    )(
      gif"ytai_DiplomaPisciareGif.mp4"
    ),
    ReplyBundleMessage.textToGif[F](
      "non (mi sento|sto) bene".r.tr(12)
    )(
      gif"ytai_DiversiGioniNonStoBeneGif.mp4"
    ),
    ReplyBundleMessage.textToGif[F](
      "(grazie e )?arrivederci".r.tr(11)
    )(
      gif"ytai_GrazieArrivederciGif.mp4"
    ),
    ReplyBundleMessage.textToGif[F](
      "eccitato"
    )(
      gif"ytai_MoltoEccitatoGif.mp4"
    ),
    ReplyBundleMessage.textToGif[F](
      "no pain",
      "no gain"
    )(
      gif"ytai_NoPainNoGainGif.mp4"
    ),
    ReplyBundleMessage.textToGif[F](
      "non hai più scuse",
      "riprenditi",
      "sei in gamba"
    )(
      gif"ytai_NoScuseGif.mp4"
    ),
    ReplyBundleMessage.textToGif[F](
      "caspita",
      "sono (grosso|sono (quasi )?enorme|una palla di lardo)".r.tr(11)
    )(
      gif"ytai_PallaDiLardoGif.mp4"
    ),
    ReplyBundleMessage.textToGif[F](
      "mette paura"
    )(
      gif"ytai_PauraGif.mp4"
    ),
    ReplyBundleMessage.textToGif[F](
      "per voi"
    )(
      gif"ytai_PerVoiGif.mp4"
    ),
    ReplyBundleMessage.textToGif[F](
      "alimenti",
      "allegria"
    )(
      gif"ytai_PizzaAllegriaGif.mp4"
    ),
    ReplyBundleMessage.textToGif[F](
      "gamberetti"
    )(
      gif"ytai_ProfumoGamberettiSalmoneGif.mp4",
      gif"ytai_SorpresaGif.mp4"
    ),
    ReplyBundleMessage.textToGif[F](
      "\\bproviamo\\b".r.tr(8),
      "senza morire"
    )(
      gif"ytai_ProviamoSenzaMorireGif.mp4"
    ),
    ReplyBundleMessage.textToGif[F](
      "quello che riesco a fare"
    )(
      gif"ytai_RiescoAFareGif.mp4"
    ),
    ReplyBundleMessage.textToGif[F](
      "sentendo (davvero )?male".r.tr(13)
    )(
      gif"ytai_SentendoDavveroMaleGif.mp4"
    ),
    ReplyBundleMessage.textToGif[F](
      "momento magico"
    )(
      gif"ytai_SilenzioMomentoMagicoGif.mp4"
    ),
    ReplyBundleMessage.textToGif[F](
      "sopraffino"
    )(
      gif"ytai_SopraffinoGif.mp4"
    ),
    ReplyBundleMessage.textToGif[F](
      "😮",
      "😯"
    )(
      gif"ytai_SorpresaGif.mp4"
    ),
    ReplyBundleMessage.textToGif[F](
      "staccato (quasi )?il naso".r.tr(16)
    )(
      gif"ytai_StaccatoNasoGif.mp4"
    ),
    ReplyBundleMessage.textToGif[F](
      "cuc[uù]+".r.tr(4)
    )(
      gif"ytai_CucuGif.mp4"
    ),
    ReplyBundleMessage.textToGif[F](
      "al[e]+ [o]{2,}".r.tr(6)
    )(
      gif"ytai_AleOooGif.mp4"
    ),
    ReplyBundleMessage.textToGif[F](
      "fare senza",
      "faenza"
    )(
      gif"ytai_SenzaFaenzaGif.mp4"
    ),
    ReplyBundleMessage.textToGif[F](
      "capolavoro",
      "\\bmeravigliosa\\b".r.tr(12)
    )(
      gif"ytai_CapolavoroMeravigliosaGif.mp4"
    ),
    ReplyBundleMessage.textToGif[F](
      "ce la puoi fare"
    )(
      gif"ytai_CeLaPuoiFareGif.mp4"
    ),
    ReplyBundleMessage.textToGif[F](
      "che profumo"
    )(
      gif"ytai_CheProfumoGif.mp4",
      gif"ytai_ECheProfumoGif.mp4"
    ),
    ReplyBundleMessage.textToGif[F](
      "eccolo qua"
    )(
      gif"ytai_EccoloQuaGif.mp4"
    ),
    ReplyBundleMessage.textToGif[F](
      "non mi piacciono"
    )(
      gif"ytai_NonMiPiaccionoQuesteCoseGif.mp4"
    ),
    ReplyBundleMessage.textToGif[F](
      "nonno"
    )(
      gif"ytai_NonnoMitoGif.mp4"
    ),
    ReplyBundleMessage.textToGif[F](
      "soltanto per questo",
      "denaro",
      "guadagno"
    )(
      gif"ytai_SoltantoPerQuestoGif.mp4"
    ),
    ReplyBundleMessage.textToGif[F](
      "dubbi enciclopedici",
      "rifletteteci"
    )(
      gif"ytai_DubbiEnciclopediciGif.mp4"
    ),
    ReplyBundleMessage.textToGif[F](
      "miei limiti"
    )(
      gif"ytai_LimitiGif.mp4"
    ),
    ReplyBundleMessage.textToGif[F](
      "panino"
    )(
      gif"ytai_PaninoBuonoSpuntitoGif.mp4",
      gif"ytai_PaninoGif.mp4",
      gif"ytai_PaninoAlpinoGif.mp4",
      gif"ytai_LoopPaninoCottoGalbanoneGif.mp4"
    ),
    ReplyBundleMessage.textToGif[F](
      "m[ ]?&[ ]?m['s]?".r.tr(3),
      "rotear",
      "ruotar"
    )(
      gif"ytai_M_MsGif.mp4",
      gif"ytai_M_MsLoopGif.mp4"
    ),
    ReplyBundleMessage.textToGif[F](
      "(😂|🤣){3,}".r.tr(4),
      "(ah|ha){5,}".r.tr(14)
    )(
      gif"ytai_RisataGif.mp4"
    ),
    ReplyBundleMessage.textToGif[F](
      "obeso",
      "te lo meriti",
      "mangi tanto"
    )(
      gif"ytai_CiccioneObesoMangiTantoGif.mp4"
    ),
    ReplyBundleMessage.textToGif[F](
      "si vede"
    )(
      gif"ytai_SiVedeGif.mp4"
    ),
    ReplyBundleMessage.textToGif[F](
      "correre non serve",
      "\\bfretta\\b".r.tr(6)
    )(
      gif"ytai_CorrereNonServeGif.mp4"
    ),
    ReplyBundleMessage.textToGif[F](
      "marmellata",
      "santa rosa"
    )(
      gif"ytai_TorreMarmellataGif.mp4"
    ),
    ReplyBundleMessage.textToGif[F](
      "(la|una) torre".r.tr(8)
    )(
      gif"ytai_TorreMarmellataGif.mp4",
      gif"ytai_TorreKinderFettaLatteGif.mp4"
    ),
    ReplyBundleMessage.textToGif[F](
      "salmone",
      "🐟"
    )(
      gif"ytai_SalmoneUnicoGif.mp4",
      gif"ytai_TartinaSalmoneGif.mp4",
      gif"ytai_SalmoneLoopGif.mp4"
    ),
    ReplyBundleMessage.textToGif[F](
      "un successo",
      "agrodolci"
    )(
      gif"ytai_SaporiAgrodolciSpettacolariGif.mp4"
    ),
    ReplyBundleMessage.textToGif[F](
      "non parlate di",
      "questioni filosofiche",
      "non ci azzecc"
    )(
      gif"ytai_QuestioniFilosoficheGif.mp4"
    ),
    ReplyBundleMessage.textToGif[F](
      "mang[ai] in (mia )?compagnia".r.tr(18)
    )(
      gif"ytai_InCompagniaGif.mp4"
    ),
    ReplyBundleMessage.textToGif[F](
      "mi incontrate",
      "la pancia",
      "in imbarazzo"
    )(
      gif"ytai_IncontratePanciaImbarazzoGif.mp4"
    ),
    ReplyBundleMessage.textToGif[F](
      "caro amico",
      "chiarire"
    )(
      gif"ytai_GrazieAmicoChiarireGif.mp4"
    ),
    ReplyBundleMessage.textToGif[F](
      "comm(uo|o)vendo".r.tr(10)
    )(
      gif"ytai_CommovendoGif.mp4",
      gif"ytai_CommuovendoFareQuelloChePiaceGif.mp4"
    ),
    ReplyBundleMessage.textToGif[F](
      "coda"
    )(
      gif"ytai_CodaLungaGif.mp4"
    ),
    ReplyBundleMessage.textToGif[F](
      "brindare",
      "drink"
    )(
      gif"ytai_AcquaMiglioreDrinkGif.mp4"
    ),
    ReplyBundleMessage.textToGif[F](
      "forchetta"
    )(
      gif"ytai_MancaForchettaGif.mp4"
    ),
    ReplyBundleMessage.textToGif[F](
      "solo[?]{2,}".r.tr(5)
    )(
      gif"ytai_SoloGif.mp4"
    ),
    ReplyBundleMessage.textToGif[F](
      "ottimo[!]+".r.tr(5)
    )(
      gif"ytai_OttimoGif.mp4"
    ),
    ReplyBundleMessage.textToGif[F](
      "sei uno sfigato"
    )(
      gif"ytai_SeiUnoSfigatoGif.mp4"
    ),
    ReplyBundleMessage.textToGif[F](
      "non prendetevi confidenze",
      "inteso di darvi"
    )(
      gif"ytai_ConfidenzeGif.mp4"
    ),
    ReplyBundleMessage.textToGif[F](
      "illegale"
    )(
      gif"ytai_IllegaleFuorileggeGif.mp4"
    ),
    ReplyBundleMessage.textToGif[F](
      "ne ho già parlato",
      "tornare sugli stessi punti",
      "lamentato con me"
    )(
      gif"ytai_NeHoGiaParlatoGif.mp4"
    ),
    ReplyBundleMessage.textToGif[F](
      "alla salute"
    )(
      gif"ytai_AllaSaluteGif.mp4"
    ),
    ReplyBundleMessage.textToGif[F](
      "grazie ragazzi",
      "grazie a tutti"
    )(
      gif"ytai_GrazieRagazziGif.mp4"
    ),
    ReplyBundleMessage.textToGif[F](
      "mi (reputi|consideri) intelligente".r.tr(22),
      "mi (reputi|consideri) sensibile".r.tr(19),
      "sensibile e intelligente"
    )(
      gif"ytai_SensibileIntelligenteGif.mp4"
    ),
    ReplyBundleMessage.textToGif[F](
      "dico basta"
    )(
      gif"ytai_AdessoViDicoBastaGif.mp4"
    ),
    ReplyBundleMessage.textToGif[F](
      "che bontà",
      "eccoli qua"
    )(
      gif"ytai_ECheProfumoGif.mp4"
    ),
    ReplyBundleMessage.textToGif[F](
      "parlo poco",
      "ingozzo",
      "non ve la prendete"
    )(
      gif"ytai_SeParloPocoGif.mp4"
    ),
    ReplyBundleMessage.textToGif[F](
      "meno di un minuto"
    )(
      gif"ytai_TiramisuMenoDiUnMinutoGif.mp4"
    ),
    ReplyBundleMessage.textToGif[F](
      "fetta al latte"
    )(
      gif"ytai_TorreKinderFettaLatteGif.mp4",
      gif"ytai_KinderFettaAlLatteGif.mp4"
    ),
    ReplyBundleMessage.textToGif[F](
      "lasciate libera",
      "linea telefonica",
      "per cose reali",
      "che mi serve"
    )(
      gif"ytai_LineaTelefonicaGif.mp4"
    ),
    ReplyBundleMessage.textToGif[F](
      "ce l'ho fatta",
      "\\bp(à|a')\\b".r.tr(2)
    )(
      gif"ytai_CeLhoFattaGif.mp4"
    ),
    ReplyBundleMessage.textToGif[F](
      "grazie tante"
    )(
      gif"ytai_GrazieTanteGif.mp4",
      gif"ytai_GrazieTante2Gif.mp4"
    ),
    ReplyBundleMessage.textToGif[F](
      "beviamoci sopra",
      "non c'è alcohol"
    )(
      gif"ytai_BeviamociSopraNoAlcoholGif.mp4"
    ),
    ReplyBundleMessage.textToGif[F](
      "cercherò di prepararlo"
    )(
      gif"ytai_CercheroDiPrepararloGif.mp4"
    ),
    ReplyBundleMessage.textToGif[F](
      "dipende da come mi sento"
    )(
      gif"ytai_DipendeDaComeMiSentoGif.mp4"
    ),
    ReplyBundleMessage.textToGif[F](
      "eccellente"
    )(
      gif"ytai_EccellenteGif.mp4"
    ),
    ReplyBundleMessage.textToGif[F](
      "non fatelo più"
    )(
      gif"ytai_NonFateloPiuGif.mp4"
    ),
    ReplyBundleMessage.textToGif[F](
      "molto dolce",
      "molto buono"
    )(
      gif"ytai_MoltoDolceMoltoBuonoGif.mp4"
    ),
    ReplyBundleMessage.textToGif[F](
      "ne avevo proprio voglia"
    )(
      gif"ytai_NeAvevoProprioVogliaGif.mp4"
    ),
    ReplyBundleMessage.textToGif[F](
      "c'entra (quasi )?sempre".r.tr(14)
    )(
      gif"ytai_CentraQuasiSempreGif.mp4"
    ),
    ReplyBundleMessage.textToGif[F](
      "riesco a darvi",
      "imparare (anche io )?(un po' )?di più".r.tr(15)
    )(
      gif"ytai_DarviImparareGif.mp4"
    ),
    ReplyBundleMessage.textToGif[F](
      "forte profumo"
    )(
      gif"ytai_ForteProfumoMieleGif.mp4"
    ),
    ReplyBundleMessage.textToGif[F](
      "😋",
      "yum",
      "gustoso"
    )(
      gif"ytai_GestoGustosoGif.mp4",
      gif"ytai_MoltoGustosoGif.mp4"
    ),
    ReplyBundleMessage.textToGif[F](
      "mia filosofia",
      "risol(to|vere) con tutti".r.tr(15)
    )(
      gif"ytai_HoRisoltoConTuttiGif.mp4"
    ),
    ReplyBundleMessage.textToGif[F](
      "sentendo in compagnia"
    )(
      gif"ytai_MiStoSentendoInCompagniaGif.mp4"
    ),
    ReplyBundleMessage.textToGif[F](
      "vengono le parole",
      "intendi dire"
    )(
      gif"ytai_NoParoleMostraIntenzioniGif.mp4"
    ),
    ReplyBundleMessage.textToGif[F](
      "quello che ci voleva",
      "bibita (bella )?fresca".r.tr(13)
    )(
      gif"ytai_QuestaBibitaBellaFrescaGif.mp4"
    ),
    ReplyBundleMessage.textToGif[F](
      "secondo boccone"
    )(
      gif"ytai_SecondoBocconeSorprendenteGif.mp4"
    ),
    ReplyBundleMessage.textToGif[F](
      "veterano",
      "chat day"
    )(
      gif"ytai_VeteranoChatDaysGif.mp4"
    ),
    ReplyBundleMessage.textToGif[F](
      "vivete questo momento"
    )(
      gif"ytai_ViveteQuestoMomentoConMeGif.mp4"
    ),
    ReplyBundleMessage.textToGif[F](
      "\\btremo\\b".r.tr(5),
      "le mie condizioni"
    )(
      gif"ytai_TremoGif.mp4"
    ),
    ReplyBundleMessage.textToGif[F](
      "fallo anche (te|tu)".r.tr(14),
      "\\bcome me\\b".r.tr(5)
    )(
      gif"ytai_FalloAncheTeComeMeGif.mp4"
    ),
    ReplyBundleMessage.textToGif[F](
      "cercando di fare",
      "del mio meglio"
    )(
      gif"ytai_FareDelMioMeglioGif.mp4"
    ),
    ReplyBundleMessage.textToGif[F](
      "limitato molto",
      "essere privato",
      "questi soldi"
    )(
      gif"ytai_PrivatoSoldiLimitatoMoltoGif.mp4"
    ),
    ReplyBundleMessage.textToGif[F](
      "\\bball(o|are|i)\\b".r.tr(5),
      "\\bdanz(a|are|i)\\b".r.tr(5),
      "💃",
      "🕺"
    )(
      gif"ytai_BalloGif.mp4"
    ),
    ReplyBundleMessage.textToGif[F](
      "zebra",
      "giraffa"
    )(
      gif"ytai_ZebraGiraffaGif.mp4"
    ),
    ReplyBundleMessage.textToGif[F](
      "smart"
    )(
      gif"ytai_SpuntinoSmartGif.mp4"
    ),
    ReplyBundleMessage.textToGif[F](
      "ipocrita"
    )(
      gif"ytai_SonoIpocritaGif.mp4"
    ),
    ReplyBundleMessage.textToGif[F](
      "imbarazz(o|ato)".r.tr(9)
    )(
      gif"ytai_SentireInImbarazzoGif.mp4",
      gif"ytai_LeggermenteImbarazzatoGif.mp4"
    ),
    ReplyBundleMessage.textToGif[F](
      "nel bene",
      "nel male"
    )(
      gif"ytai_CiaoFollowersNelBeneNelMaleGif.mp4"
    ),
    ReplyBundleMessage.textToGif[F](
      "se volete sapere",
      "100%",
      "non va per me"
    )(
      gif"ytai_SapereTuttoNonVaGif.mp4"
    ),
    ReplyBundleMessage.textToGif[F](
      "molto aromatico",
      "affumicatura",
      "pepe nero"
    )(
      gif"ytai_MoltoAromaticoGif.mp4"
    ),
    ReplyBundleMessage.textToGif[F](
      "ci riusciremo"
    )(
      gif"ytai_NonViPreoccupateCiRiusciremoGif.mp4"
    ),
    ReplyBundleMessage.textToGif[F](
      "non vi preoccupate"
    )(
      gif"ytai_NonViPreoccupateCiRiusciremoGif.mp4",
      gif"ytai_BeviamociSopraNoAlcoholGif.mp4"
    ),
    ReplyBundleMessage.textToGif[F](
      "fuorilegge"
    )(
      gif"ytai_NonSonoFuorileggeNecessitaGif.mp4",
      gif"ytai_IllegaleFuorileggeGif.mp4"
    ),
    ReplyBundleMessage.textToGif[F](
      "ho necessità"
    )(
      gif"ytai_NonSonoFuorileggeNecessitaGif.mp4"
    ),
    ReplyBundleMessage.textToGif[F](
      "mi fa (tanto )?piacere".r.tr(12)
    )(
      gif"ytai_MiFaTantoPiacereGif.mp4"
    ),
    ReplyBundleMessage.textToGif[F](
      "\\bshow\\b".r.tr(4)
    )(
      gif"ytai_LoShowDeveContunuareGif.mp4"
    ),
    ReplyBundleMessage.textToGif[F](
      "felice di ritrovarvi"
    )(
      gif"ytai_FeliceDiRitrovarviGif.mp4"
    ),
    ReplyBundleMessage.textToGif[F](
      "fa(re|rti|tti) (due |i )?conti".r.tr(9),
      "il lavoro che fai"
    )(
      gif"ytai_FattiDueContiGif.mp4"
    ),
    ReplyBundleMessage.textToGif[F](
      "inquietante"
    )(
      gif"ytai_NonInquietanteGif.mp4"
    ),
    ReplyBundleMessage.textToGif[F](
      "vediamo un po'"
    )(
      gif"ytai_VediamoUnPoGif.mp4"
    ),
    ReplyBundleMessage.textToGif[F](
      "rinfrescante",
      "s(\\.)?r(\\.)?l(\\.)?".r.tr(3) // s.r.l.
    )(
      gif"ytai_RinfrescanteDiCalabriaGif.mp4"
    ),
    ReplyBundleMessage.textToGif[F](
      "prima o poi"
    )(
      gif"ytai_NonViPreoccupateCiRiusciremoGif.mp4",
      gif"ytai_PassioneAllevareGallineGif.mp4"
    ),
    ReplyBundleMessage.textToGif[F](
      "allevare",
      "galline",
      "mi[ae] passion[ie]".r.tr(12)
    )(
      gif"ytai_PassioneAllevareGallineGif.mp4"
    ),
    ReplyBundleMessage.textToGif[F](
      "com'è venuto",
      "perfetto"
    )(
      gif"ytai_PerfettoGif.mp4"
    ),
    ReplyBundleMessage.textToGif[F](
      "più piccoline",
      "sono dolci",
      "al punto giusto"
    )(
      gif"ytai_DolciAlPuntoGiustoGif.mp4"
    ),
    ReplyBundleMessage.textToGif[F](
      "amarognolo",
      "si intona",
      "non guasta",
      "contrasto"
    )(
      gif"ytai_ContrastoAmarognoloGif.mp4"
    ),
    ReplyBundleMessage.textToGif[F](
      "bella fresca"
    )(
      gif"ytai_BellaFrescaGif.mp4"
    ),
    ReplyBundleMessage.textToGif[F](
      "piace mangiare così",
      "critiche"
    )(
      gif"ytai_MangiareCriticheGif.mp4"
    ),
    ReplyBundleMessage.textToGif[F](
      "ostica",
      "insalata"
    )(
      gif"ytai_OsticaInsalataGif.mp4"
    ),
    ReplyBundleMessage.textToGif[F](
      "\\blotte\\b".r.tr(5),
      "condivido"
    )(
      gif"ytai_PersonaliLotteFollowersGif.mp4"
    ),
    ReplyBundleMessage.textToGif[F](
      "condizione umana",
      "patologico",
      "individuo che comunque vive"
    )(
      gif"ytai_CondizioneUmanaGif.mp4"
    ),
    ReplyBundleMessage.textToGif[F](
      "occhiali",
      "👓",
      "🤓"
    )(
      gif"ytai_SistemazioneOcchialiGif.mp4"
    ),
    ReplyBundleMessage.textToGif[F](
      "miele"
    )(
      gif"ytai_ForteProfumoMieleGif.mp4",
      gif"ytai_AppiccicaticcioMieleGif.mp4"
    ),
    ReplyBundleMessage.textToGif[F](
      "appiccicaticcio"
    )(
      gif"ytai_AppiccicaticcioMieleGif.mp4"
    ),
    ReplyBundleMessage.textToGif[F](
      "scaldato",
      "panini",
      "sono ottimi"
    )(
      gif"ytai_GrazieScaldatoPaniniGif.mp4"
    ),
    ReplyBundleMessage.textToGif[F](
      "\\bterminato\\b".r.tr(9),
      "facilit[aàá]".r.tr(7)
    )(
      gif"ytai_EstremaFacilitaGif.mp4"
    ),
    ReplyBundleMessage.textToGif[F](
      "notturna"
    )(gif"ytai_FameSeteNotturnaGif.mp4"),
    ReplyBundleMessage.textToGif[F](
      "alpino",
      "eccolo qui",
      "si chiama"
    )(gif"ytai_PaninoAlpinoGif.mp4"),
    ReplyBundleMessage.textToGif[F](
      "waffel",
      "inzuppati",
      "profumo gradevol(e|issimo)".r.tr(16)
    )(gif"ytai_WaffelInzuppatiGif.mp4"),
    ReplyBundleMessage.textToGif[F](
      "oliva"
    )(
      gif"ytai_MoltoGustosaOlivaGif.mp4"
    ),
    ReplyBundleMessage.textToGif[F](
      "mpincia",
      "cosa (mi )bevo".r.tr(9)
    )(
      gif"ytai_MpinciaGif.mp4"
    ),
    ReplyBundleMessage.textToGif[F](
      "calabria"
    )(
      gif"ytai_MpinciaGif.mp4",
      gif"ytai_RinfrescanteDiCalabriaGif.mp4"
    ),
    ReplyBundleMessage.textToGif[F]("galbanone")(
      gif"ytai_LoopPaninoCottoGalbanoneGif.mp4"
    )
  )
end Gif
