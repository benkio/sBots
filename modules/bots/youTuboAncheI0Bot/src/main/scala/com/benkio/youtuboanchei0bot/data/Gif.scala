package com.benkio.youtuboanchei0bot.data


import com.benkio.telegrambotinfrastructure.model.reply.gif
import com.benkio.telegrambotinfrastructure.model.reply.ReplyBundleMessage
import com.benkio.telegrambotinfrastructure.model.tr

object Gif {

  def messageRepliesGifData: List[ReplyBundleMessage] = List(
    ReplyBundleMessage.textToGif(
      "reputazione"
    )(
      gif"ytai_LaReputazioneGif.mp4",
      gif"ytai_CheVergognaGif.mp4"
    ),
    ReplyBundleMessage.textToGif(
      "fatica",
      "sudore",
      "sudato"
    )(
      gif"ytai_AffaticamentoGif.mp4"
    ),
    ReplyBundleMessage.textToGif(
      "ascolta (queste|le) mie parole".r,
      "amareggiati",
      "dedicaci (il tuo tempo|le tue notti)".r
    )(
      gif"ytai_AmareggiatiGif.mp4"
    ),
    ReplyBundleMessage.textToGif(
      "\\barchitetta\\b".r,
      "\\bnotaia\\b".r,
      "\\bministra\\b".r,
      "\\bavvocata\\b".r
    )(
      gif"ytai_ArchitettaGif.mp4"
    ),
    ReplyBundleMessage.textToGif(
      "bel sogno"
    )(
      gif"ytai_BelSognoGif.mp4"
    ),
    ReplyBundleMessage.textToGif(
      "brivido",
      "fremito",
      "tremito",
      "tremore"
    )(
      gif"ytai_BrividoGif.mp4"
    ),
    ReplyBundleMessage.textToGif(
      "buonanotte"
    )(
      gif"ytai_BuonanotteGif.mp4",
      gif"ytai_BuonanotteBrunchPlusGif.mp4",
      gif"ytai_BuonanotteFollowersGif.mp4"
    ),
    ReplyBundleMessage.textToGif(
      "buonasera"
    )(
      gif"ytai_BuonaseraGif.mp4"
    ),
    ReplyBundleMessage.textToGif(
      "che spettacolo"
    )(
      gif"ytai_CheSpettacoloGif.mp4"
    ),
    ReplyBundleMessage.textToGif(
      "ciao ragazzi",
      "cari saluti"
    )(
      gif"ytai_CiaoRagazziGif.mp4"
    ),
    ReplyBundleMessage.textToGif(
      "ci divertiremo",
      "bel percorso"
    )(
      gif"ytai_CiDivertiremoPercorsoGif.mp4"
    ),
    ReplyBundleMessage.textToGif(
      "corpulenti",
      "ciccioni"
    )(
      gif"ytai_CorpulentiCiccioniGif.mp4"
    ),
    ReplyBundleMessage.textToGif(
      "culetto"
    )(
      gif"ytai_CulettoGif.mp4"
    ),
    ReplyBundleMessage.textToGif(
      "ed allora"
    )(
      gif"ytai_EdAlloraGif.mp4"
    ),
    ReplyBundleMessage.textToGif(
      "fai pure"
    )(
      gif"ytai_FaiPureGif.mp4"
    ),
    ReplyBundleMessage.textToGif(
      "filet[ -]?o[ -]?fish".r
    )(
      gif"ytai_FiletOFishGif.mp4",
      gif"ytai_FiletOFish2Gif.mp4",
      gif"ytai_BagnarloAcquaGif.mp4"
    ),
    ReplyBundleMessage.textToGif(
      "(deluso|insoddisfatto|inappagato|abbattuto|scoraggiato|demoralizzato|depresso|demotivato|avvilito|scocciato)".r
        .tr(6)
    )(
      gif"ytai_FrustrazioneGif.mp4"
    ),
    ReplyBundleMessage.textToGif(
      "ci sto (gi[aà] )?pensando".r
    )(
      gif"ytai_GiaPensandoGif.mp4"
    ),
    ReplyBundleMessage.textToGif(
      "sono grande",
      "sono corpulento"
    )(
      gif"ytai_GrandeCorpulentoGif.mp4"
    ),
    ReplyBundleMessage.textToGif(
      "grazie dottore"
    )(
      gif"ytai_GrazieDottoreGif.mp4"
    ),
    ReplyBundleMessage.textToGif(
      "sconforto grave"
    )(
      gif"ytai_GrazieTanteGif.mp4"
    ),
    ReplyBundleMessage.textToGif(
      "incredibile profumo",
      "incredibile aroma"
    )(
      gif"ytai_IncredibileAromaProfumoGif.mp4"
    ),
    ReplyBundleMessage.textToGif(
      "incredibile",
      "inimitabile",
      "the number (one|1)".r
    )(
      gif"ytai_IncredibileInimitabileGif.mp4"
    ),
    ReplyBundleMessage.textToGif(
      "non lo so"
    )(
      gif"ytai_IoNonLoSoGif.mp4"
    ),
    ReplyBundleMessage.textToGif(
      "loro dovere",
      "vostro diritto"
    )(
      gif"ytai_LoroDovereVostroDirittoGif.mp4"
    ),
    ReplyBundleMessage.textToGif(
      "lo so (bene )?anche io".r
    )(
      gif"ytai_LoSoAncheIoGif.mp4"
    ),
    ReplyBundleMessage.textToGif(
      "mah"
    )(
      gif"ytai_MahGif.mp4",
      gif"ytai_Mah2Gif.mp4",
      gif"ytai_Mah3Gif.mp4",
      gif"ytai_ZoomMahGif.mp4"
    ),
    ReplyBundleMessage.textToGif(
      "meraviglioso"
    )(
      gif"ytai_MeravigliosoGif.mp4"
    ),
    ReplyBundleMessage.textToGif(
      "molla",
      "grassone",
      "ancora non sei morto"
    )(
      gif"ytai_MollaGif.mp4"
    ),
    ReplyBundleMessage.textToGif(
      "molto buona"
    )(
      gif"ytai_MoltoBuonaGif.mp4",
      gif"ytai_MoltoBuona2Gif.mp4",
      gif"ytai_BuonaGif.mp4"
    ),
    ReplyBundleMessage.textToGif(
      "monoporzioni",
      "mezzo (chilo|kg)".r
    )(
      gif"ytai_MonoporzioniTiramisuGif.mp4"
    ),
    ReplyBundleMessage.textToGif(
      "non fa male"
    )(
      gif"ytai_NonFaMaleGif.mp4",
      gif"ytai_AcquaMiglioreDrinkGif.mp4"
    ),
    ReplyBundleMessage.textToGif(
      "non la crede nessuno questa cosa",
      "non ci crede nessuno"
    )(
      gif"ytai_NonLaCredeNessunoGif.mp4"
    ),
    ReplyBundleMessage.textToGif(
      "non pensiamoci più"
    )(
      gif"ytai_NonPensiamociGif.mp4"
    ),
    ReplyBundleMessage.textToGif(
      "incomprensione",
      "non vi capiscono"
    )(
      gif"ytai_NonViCapisconoGif.mp4"
    ),
    ReplyBundleMessage.textToGif(
      "occhiolino",
      ";)",
      "😉"
    )(
      gif"ytai_OcchiolinoGif.mp4",
      gif"ytai_Occhiolino2Gif.mp4",
      gif"ytai_Occhiolino3Gif.mp4",
      gif"ytai_OcchiolinoTestaDondolanteGif.mp4"
    ),
    ReplyBundleMessage.textToGif(
      "olè"
    )(
      gif"ytai_OleGif.mp4"
    ),
    ReplyBundleMessage.textToGif(
      "olè anche io"
    )(
      gif"ytai_OleAncheIoGif.mp4"
    ),
    ReplyBundleMessage.textToGif(
      "la perfezione",
      "la nostra tendenza"
    )(
      gif"ytai_PerfezioneTendenzaGif.mp4"
    ),
    ReplyBundleMessage.textToGif(
      "profumo meraviglioso"
    )(
      gif"ytai_ProfumoMeravigliosoGif.mp4",
      gif"ytai_ProfumoGamberettiSalmoneGif.mp4"
    ),
    ReplyBundleMessage.textToGif(
      "sentiamo il profumo"
    )(
      gif"ytai_ProfumoMeravigliosoGif.mp4"
    ),
    ReplyBundleMessage.textToGif(
      "ringraziamento"
    )(
      gif"ytai_RingraziamentoGif.mp4"
    ),
    ReplyBundleMessage.textToGif(
      "se non mi aiuta",
      "cosa mi aiuta"
    )(
      gif"ytai_SentiteCheRobaGif.mp4"
    ),
    ReplyBundleMessage.textToGif(
      "🤷"
    )(
      gif"ytai_ShrugGif.mp4"
    ),
    ReplyBundleMessage.textToGif(
      "sta per dire qualcosa"
    )(
      gif"ytai_SilenzioGif.mp4"
    ),
    ReplyBundleMessage.textToGif(
      "silenzio[,]? silenzio".r
    )(
      gif"ytai_SilenzioGif.mp4"
    ),
    ReplyBundleMessage.textToGif(
      "si v[àa] finch[eé] si v[aà]".r,
      "quando non si potrà andare più",
      "è tanto facile"
    )(
      gif"ytai_SiVaFincheSiVaGif.mp4"
    ),
    ReplyBundleMessage.textToGif(
      "sorprendente"
    )(
      gif"ytai_SorprendenteGif.mp4",
      gif"ytai_SecondoBocconeSorprendenteGif.mp4"
    ),
    ReplyBundleMessage.textToGif(
      "l'unica cosa che sai fare"
    )(
      gif"ytai_UnicaCosaMangiareGif.mp4"
    ),
    ReplyBundleMessage.textToGif(
      "uno o due"
    )(
      gif"ytai_UnoODueGif.mp4"
    ),
    ReplyBundleMessage.textToGif(
      "zenzero",
      "mia risposta"
    )(
      gif"ytai_ZenzeroGif.mp4"
    ),
    ReplyBundleMessage.textToGif(
      "zoom",
      "guardo da vicino"
    )(
      gif"ytai_ZoomGif.mp4",
      gif"ytai_ZoomMahGif.mp4"
    ),
    ReplyBundleMessage.textToGif(
      "auguri di gusto"
    )(
      gif"ytai_AuguriDiGustoGif.mp4"
    ),
    ReplyBundleMessage.textToGif(
      "bagnarlo"
    )(
      gif"ytai_BagnarloAcquaGif.mp4"
    ),
    ReplyBundleMessage.textToGif(
      "benvenuti",
      "ora e sempre"
    )(
      gif"ytai_BenvenutiGif.mp4"
    ),
    ReplyBundleMessage.textToGif(
      "\\bdiploma\\b".r,
      "per pisciare",
      "ma (che )?stiamo scherzando".r
    )(
      gif"ytai_DiplomaPisciareGif.mp4"
    ),
    ReplyBundleMessage.textToGif(
      "non (mi sento|sto) bene".r
    )(
      gif"ytai_DiversiGioniNonStoBeneGif.mp4"
    ),
    ReplyBundleMessage.textToGif(
      "(grazie e )?arrivederci".r
    )(
      gif"ytai_GrazieArrivederciGif.mp4"
    ),
    ReplyBundleMessage.textToGif(
      "eccitato"
    )(
      gif"ytai_MoltoEccitatoGif.mp4"
    ),
    ReplyBundleMessage.textToGif(
      "no pain",
      "no gain"
    )(
      gif"ytai_NoPainNoGainGif.mp4"
    ),
    ReplyBundleMessage.textToGif(
      "non hai più scuse",
      "riprenditi",
      "sei in gamba"
    )(
      gif"ytai_NoScuseGif.mp4"
    ),
    ReplyBundleMessage.textToGif(
      "sono (grosso|sono (quasi )?enorme|una palla di lardo)".r
    )(
      gif"ytai_PallaDiLardoGif.mp4"
    ),
    ReplyBundleMessage.textToGif(
      "mette paura"
    )(
      gif"ytai_PauraGif.mp4"
    ),
    ReplyBundleMessage.textToGif(
      "per voi"
    )(
      gif"ytai_PerVoiGif.mp4"
    ),
    ReplyBundleMessage.textToGif(
      "alimenti",
      "allegria"
    )(
      gif"ytai_PizzaAllegriaGif.mp4"
    ),
    ReplyBundleMessage.textToGif(
      "gamberetti"
    )(
      gif"ytai_ProfumoGamberettiSalmoneGif.mp4",
      gif"ytai_SorpresaGif.mp4"
    ),
    ReplyBundleMessage.textToGif(
      "\\bproviamo\\b".r,
      "senza morire"
    )(
      gif"ytai_ProviamoSenzaMorireGif.mp4"
    ),
    ReplyBundleMessage.textToGif(
      "quello che riesco a fare"
    )(
      gif"ytai_RiescoAFareGif.mp4"
    ),
    ReplyBundleMessage.textToGif(
      "sentendo (davvero )?male".r
    )(
      gif"ytai_SentendoDavveroMaleGif.mp4"
    ),
    ReplyBundleMessage.textToGif(
      "momento magico"
    )(
      gif"ytai_SilenzioMomentoMagicoGif.mp4"
    ),
    ReplyBundleMessage.textToGif(
      "sopraffino"
    )(
      gif"ytai_SopraffinoGif.mp4"
    ),
    ReplyBundleMessage.textToGif(
      "😮",
      "😯"
    )(
      gif"ytai_SorpresaGif.mp4"
    ),
    ReplyBundleMessage.textToGif(
      "staccato (quasi )?il naso".r
    )(
      gif"ytai_StaccatoNasoGif.mp4"
    ),
    ReplyBundleMessage.textToGif(
      "cuc[uù]+".r
    )(
      gif"ytai_CucuGif.mp4"
    ),
    ReplyBundleMessage.textToGif(
      "al[e]+ [o]{2,}".r.tr(6)
    )(
      gif"ytai_AleOooGif.mp4"
    ),
    ReplyBundleMessage.textToGif(
      "fare senza",
      "faenza"
    )(
      gif"ytai_SenzaFaenzaGif.mp4"
    ),
    ReplyBundleMessage.textToGif(
      "capolavoro",
      "\\bmeravigliosa\\b".r
    )(
      gif"ytai_CapolavoroMeravigliosaGif.mp4"
    ),
    ReplyBundleMessage.textToGif(
      "ce la puoi fare"
    )(
      gif"ytai_CeLaPuoiFareGif.mp4"
    ),
    ReplyBundleMessage.textToGif(
      "che profumo"
    )(
      gif"ytai_CheProfumoGif.mp4",
      gif"ytai_ECheProfumoGif.mp4"
    ),
    ReplyBundleMessage.textToGif(
      "eccolo qua"
    )(
      gif"ytai_EccoloQuaGif.mp4"
    ),
    ReplyBundleMessage.textToGif(
      "non mi piacciono"
    )(
      gif"ytai_NonMiPiaccionoQuesteCoseGif.mp4"
    ),
    ReplyBundleMessage.textToGif(
      "nonno"
    )(
      gif"ytai_NonnoMitoGif.mp4"
    ),
    ReplyBundleMessage.textToGif(
      "soltanto per questo",
      "denaro",
      "guadagno"
    )(
      gif"ytai_SoltantoPerQuestoGif.mp4"
    ),
    ReplyBundleMessage.textToGif(
      "dubbi enciclopedici",
      "rifletteteci"
    )(
      gif"ytai_DubbiEnciclopediciGif.mp4"
    ),
    ReplyBundleMessage.textToGif(
      "miei limiti"
    )(
      gif"ytai_LimitiGif.mp4"
    ),
    ReplyBundleMessage.textToGif(
      "è uno spettacolo"
    )(
      gif"ytai_PaninoBuonoSpuntitoGif.mp4"
    ),
    ReplyBundleMessage.textToGif(
      "m[ ]?&[ ]?m['s]?".r,
      "rotear",
      "ruotar"
    )(
      gif"ytai_MnMsGif.mp4",
      gif"ytai_MnMsLoopGif.mp4"
    ),
    ReplyBundleMessage.textToGif(
      "(😂|🤣){4,}".r.tr(4),
      "(ah|ha){5,}".r.tr(10)
    )(
      gif"ytai_RisataGif.mp4"
    ),
    ReplyBundleMessage.textToGif(
      "obeso",
      "te lo meriti",
      "mangi tanto"
    )(
      gif"ytai_CiccioneObesoMangiTantoGif.mp4"
    ),
    ReplyBundleMessage.textToGif(
      "si vede"
    )(
      gif"ytai_SiVedeGif.mp4"
    ),
    ReplyBundleMessage.textToGif(
      "correre non serve",
      "\\bfretta\\b".r
    )(
      gif"ytai_CorrereNonServeGif.mp4"
    ),
    ReplyBundleMessage.textToGif(
      "marmellata",
      "santa rosa"
    )(
      gif"ytai_TorreMarmellataGif.mp4"
    ),
    ReplyBundleMessage.textToGif(
      "(la|una) torre".r
    )(
      gif"ytai_TorreMarmellataGif.mp4",
      gif"ytai_TorreKinderFettaLatteGif.mp4"
    ),
    ReplyBundleMessage.textToGif(
      "salmone",
      "🐟"
    )(
      gif"ytai_SalmoneUnicoGif.mp4",
      gif"ytai_TartinaSalmoneGif.mp4",
      gif"ytai_SalmoneLoopGif.mp4"
    ),
    ReplyBundleMessage.textToGif(
      "un successo",
      "agrodolci"
    )(
      gif"ytai_SaporiAgrodolciSpettacolariGif.mp4"
    ),
    ReplyBundleMessage.textToGif(
      "non parlate di",
      "questioni filosofiche",
      "non ci azzecc"
    )(
      gif"ytai_QuestioniFilosoficheGif.mp4"
    ),
    ReplyBundleMessage.textToGif(
      "mang[ai] in (mia )?compagnia".r
    )(
      gif"ytai_InCompagniaGif.mp4"
    ),
    ReplyBundleMessage.textToGif(
      "mi incontrate",
      "la pancia",
      "in imbarazzo"
    )(
      gif"ytai_IncontratePanciaImbarazzoGif.mp4"
    ),
    ReplyBundleMessage.textToGif(
      "caro amico",
      "chiarire"
    )(
      gif"ytai_GrazieAmicoChiarireGif.mp4"
    ),
    ReplyBundleMessage.textToGif(
      "comm(uo|o)vendo".r
    )(
      gif"ytai_CommovendoGif.mp4",
      gif"ytai_CommuovendoFareQuelloChePiaceGif.mp4"
    ),
    ReplyBundleMessage.textToGif(
      "coda"
    )(
      gif"ytai_CodaLungaGif.mp4"
    ),
    ReplyBundleMessage.textToGif(
      "brindare",
      "drink"
    )(
      gif"ytai_AcquaMiglioreDrinkGif.mp4"
    ),
    ReplyBundleMessage.textToGif(
      "forchetta"
    )(
      gif"ytai_MancaForchettaGif.mp4"
    ),
    ReplyBundleMessage.textToGif(
      "solo?[?]+".r.tr(6)
    )(
      gif"ytai_SoloGif.mp4"
    ),
    ReplyBundleMessage.textToGif(
      "sei uno sfigato"
    )(
      gif"ytai_SeiUnoSfigatoGif.mp4"
    ),
    ReplyBundleMessage.textToGif(
      "prendetevi confidenze",
      "inteso darvi"
    )(
      gif"ytai_ConfidenzeGif.mp4"
    ),
    ReplyBundleMessage.textToGif(
      "illegale"
    )(
      gif"ytai_IllegaleFuorileggeGif.mp4"
    ),
    ReplyBundleMessage.textToGif(
      "ne ho già parlato",
      "tornare sugli stessi punti",
      "lamentato con me"
    )(
      gif"ytai_NeHoGiaParlatoGif.mp4"
    ),
    ReplyBundleMessage.textToGif(
      "grazie ragazzi",
      "grazie a tutti"
    )(
      gif"ytai_GrazieRagazziGif.mp4"
    ),
    ReplyBundleMessage.textToGif(
      "mi (reputi|consideri) intelligente".r,
      "mi (reputi|consideri) sensibile".r,
      "sensibile e intelligente"
    )(
      gif"ytai_SensibileIntelligenteGif.mp4"
    ),
    ReplyBundleMessage.textToGif(
      "dico basta"
    )(
      gif"ytai_AdessoViDicoBastaGif.mp4"
    ),
    ReplyBundleMessage.textToGif(
      "che bontà",
      "eccoli qua"
    )(
      gif"ytai_ECheProfumoGif.mp4"
    ),
    ReplyBundleMessage.textToGif(
      "parlo poco",
      "ingozzo",
      "non ve la prendete"
    )(
      gif"ytai_SeParloPocoGif.mp4"
    ),
    ReplyBundleMessage.textToGif(
      "meno di un minuto"
    )(
      gif"ytai_TiramisuMenoDiUnMinutoGif.mp4"
    ),
    ReplyBundleMessage.textToGif(
      "fetta al latte"
    )(
      gif"ytai_TorreKinderFettaLatteGif.mp4",
      gif"ytai_KinderFettaAlLatteGif.mp4"
    ),
    ReplyBundleMessage.textToGif(
      "lasciate libera",
      "linea telefonica",
      "per cose reali",
      "che mi serve"
    )(
      gif"ytai_LineaTelefonicaGif.mp4"
    ),
    ReplyBundleMessage.textToGif(
      "ce l'ho fatta",
      "(\\b|^)p(à|a')(\\b|$)".r
    )(
      gif"ytai_CeLhoFattaGif.mp4"
    ),
    ReplyBundleMessage.textToGif(
      "grazie tante"
    )(
      gif"ytai_GrazieTanteGif.mp4",
      gif"ytai_GrazieTante2Gif.mp4"
    ),
    ReplyBundleMessage.textToGif(
      "beviamoci sopra",
      "non c'è alcohol"
    )(
      gif"ytai_BeviamociSopraNoAlcoholGif.mp4"
    ),
    ReplyBundleMessage.textToGif(
      "cercherò di prepararlo"
    )(
      gif"ytai_CercheroDiPrepararloGif.mp4"
    ),
    ReplyBundleMessage.textToGif(
      "dipende da come mi sento"
    )(
      gif"ytai_DipendeDaComeMiSentoGif.mp4"
    ),
    ReplyBundleMessage.textToGif(
      "eccellente"
    )(
      gif"ytai_EccellenteGif.mp4"
    ),
    ReplyBundleMessage.textToGif(
      "non fatelo più"
    )(
      gif"ytai_NonFateloPiuGif.mp4"
    ),
    ReplyBundleMessage.textToGif(
      "molto dolce",
      "molto buono"
    )(
      gif"ytai_MoltoDolceMoltoBuonoGif.mp4"
    ),
    ReplyBundleMessage.textToGif(
      "ne avevo proprio voglia"
    )(
      gif"ytai_NeAvevoProprioVogliaGif.mp4"
    ),
    ReplyBundleMessage.textToGif(
      "c'entra (quasi )?sempre".r
    )(
      gif"ytai_CentraQuasiSempreGif.mp4"
    ),
    ReplyBundleMessage.textToGif(
      "riesco a darvi",
      "imparare (anche io )?(un po' )?di più".r
    )(
      gif"ytai_DarviImparareGif.mp4"
    ),
    ReplyBundleMessage.textToGif(
      "forte profumo"
    )(
      gif"ytai_ForteProfumoMieleGif.mp4"
    ),
    ReplyBundleMessage.textToGif(
      "😋",
      "yum",
      "gustoso"
    )(
      gif"ytai_GestoGustosoGif.mp4",
      gif"ytai_MoltoGustosoGif.mp4"
    ),
    ReplyBundleMessage.textToGif(
      "mia filosofia",
      "risol(to|vere) con tutti".r
    )(
      gif"ytai_HoRisoltoConTuttiGif.mp4"
    ),
    ReplyBundleMessage.textToGif(
      "vengono le parole",
      "intendi dire"
    )(
      gif"ytai_NoParoleMostraIntenzioniGif.mp4"
    ),
    ReplyBundleMessage.textToGif(
      "quello che ci voleva",
      "bibita (bella )?fresca".r
    )(
      gif"ytai_QuestaBibitaBellaFrescaGif.mp4"
    ),
    ReplyBundleMessage.textToGif(
      "secondo boccone"
    )(
      gif"ytai_SecondoBocconeSorprendenteGif.mp4"
    ),
    ReplyBundleMessage.textToGif(
      "veterano",
      "chat day"
    )(
      gif"ytai_VeteranoChatDaysGif.mp4"
    ),
    ReplyBundleMessage.textToGif(
      "vivete questo momento"
    )(
      gif"ytai_ViveteQuestoMomentoConMeGif.mp4"
    ),
    ReplyBundleMessage.textToGif(
      "\\btremo\\b".r,
      "le mie condizioni"
    )(
      gif"ytai_TremoGif.mp4"
    ),
    ReplyBundleMessage.textToGif(
      "fallo anche (te|tu)".r,
      "\\bcome me\\b".r
    )(
      gif"ytai_FalloAncheTeComeMeGif.mp4",
      gif"ytai_FalloAncheTuGif.mp4"
    ),
    ReplyBundleMessage.textToGif(
      "cercando di fare",
      "del mio meglio"
    )(
      gif"ytai_FareDelMioMeglioGif.mp4"
    ),
    ReplyBundleMessage.textToGif(
      "limitato molto",
      "essere privato",
      "questi soldi"
    )(
      gif"ytai_PrivatoSoldiLimitatoMoltoGif.mp4"
    ),
    ReplyBundleMessage.textToGif(
      "\\bballo\\b".r,
      "\\bballare\\b".r,
      "\\bballi\\b".r,
      "\\bdanza\\b".r,
      "\\bdanzare\\b".r,
      "💃",
      "🕺"
    )(
      gif"ytai_BalloGif.mp4",
      gif"ytai_LoopBalloGif.mp4"
    ),
    ReplyBundleMessage.textToGif(
      "zebra",
      "giraffa"
    )(
      gif"ytai_ZebraGiraffaGif.mp4"
    ),
    ReplyBundleMessage.textToGif(
      "smart"
    )(
      gif"ytai_SpuntinoSmartGif.mp4"
    ),
    ReplyBundleMessage.textToGif(
      "ipocrita"
    )(
      gif"ytai_SonoIpocritaGif.mp4"
    ),
    ReplyBundleMessage.textToGif(
      "imbarazz(o|ato)".r
    )(
      gif"ytai_SentireInImbarazzoGif.mp4",
      gif"ytai_LeggermenteImbarazzatoGif.mp4"
    ),
    ReplyBundleMessage.textToGif(
      "se volete sapere",
      "100%",
      "non va per me"
    )(
      gif"ytai_SapereTuttoNonVaGif.mp4"
    ),
    ReplyBundleMessage.textToGif(
      "molto aromatico",
      "affumicatura",
      "pepe nero"
    )(
      gif"ytai_MoltoAromaticoGif.mp4"
    ),
    ReplyBundleMessage.textToGif(
      "ci riusciremo"
    )(
      gif"ytai_NonViPreoccupateCiRiusciremoGif.mp4"
    ),
    ReplyBundleMessage.textToGif(
      "non vi preoccupate"
    )(
      gif"ytai_NonViPreoccupateCiRiusciremoGif.mp4",
      gif"ytai_BeviamociSopraNoAlcoholGif.mp4"
    ),
    ReplyBundleMessage.textToGif(
      "fuorilegge"
    )(
      gif"ytai_NonSonoFuorileggeNecessitaGif.mp4",
      gif"ytai_IllegaleFuorileggeGif.mp4"
    ),
    ReplyBundleMessage.textToGif(
      "ho necessità"
    )(
      gif"ytai_NonSonoFuorileggeNecessitaGif.mp4"
    ),
    ReplyBundleMessage.textToGif(
      "mi fa (tanto )?piacere".r
    )(
      gif"ytai_MiFaTantoPiacereGif.mp4"
    ),
    ReplyBundleMessage.textToGif(
      "\\bshow\\b".r
    )(
      gif"ytai_LoShowDeveContunuareGif.mp4"
    ),
    ReplyBundleMessage.textToGif(
      "felice di ritrovarvi"
    )(
      gif"ytai_FeliceDiRitrovarviGif.mp4"
    ),
    ReplyBundleMessage.textToGif(
      "fa(re|rti|tti) (due |i )?conti".r,
      "il lavoro che fai"
    )(
      gif"ytai_FattiDueContiGif.mp4"
    ),
    ReplyBundleMessage.textToGif(
      "inquietante"
    )(
      gif"ytai_NonInquietanteGif.mp4"
    ),
    ReplyBundleMessage.textToGif(
      "vediamo un po'"
    )(
      gif"ytai_VediamoUnPoGif.mp4"
    ),
    ReplyBundleMessage.textToGif(
      "rinfrescante",
      "s(\\.)?r(\\.)?l(\\.)?".r
    )(
      gif"ytai_RinfrescanteDiCalabriaGif.mp4"
    ),
    ReplyBundleMessage.textToGif(
      "prima o poi"
    )(
      gif"ytai_NonViPreoccupateCiRiusciremoGif.mp4",
      gif"ytai_PassioneAllevareGallineGif.mp4"
    ),
    ReplyBundleMessage.textToGif(
      "allevare",
      "galline",
      "mi[ae] passion[ie]".r
    )(
      gif"ytai_PassioneAllevareGallineGif.mp4"
    ),
    ReplyBundleMessage.textToGif(
      "com'è venuto",
      "perfetto"
    )(
      gif"ytai_PerfettoGif.mp4"
    ),
    ReplyBundleMessage.textToGif(
      "più piccoline",
      "sono dolci",
      "al punto giusto"
    )(
      gif"ytai_DolciAlPuntoGiustoGif.mp4"
    ),
    ReplyBundleMessage.textToGif(
      "amarognolo",
      "si intona",
      "non guasta",
      "contrasto"
    )(
      gif"ytai_ContrastoAmarognoloGif.mp4"
    ),
    ReplyBundleMessage.textToGif(
      "bella fresca"
    )(
      gif"ytai_BellaFrescaGif.mp4"
    ),
    ReplyBundleMessage.textToGif(
      "piace mangiare così",
      "critiche"
    )(
      gif"ytai_MangiareCriticheGif.mp4"
    ),
    ReplyBundleMessage.textToGif(
      "ostica",
      "insalata"
    )(
      gif"ytai_OsticaInsalataGif.mp4"
    ),
    ReplyBundleMessage.textToGif(
      "\\blotte\\b".r,
      "condivido"
    )(
      gif"ytai_PersonaliLotteFollowersGif.mp4"
    ),
    ReplyBundleMessage.textToGif(
      "condizione umana",
      "patologico",
      "individuo che comunque vive"
    )(
      gif"ytai_CondizioneUmanaGif.mp4"
    ),
    ReplyBundleMessage.textToGif(
      "occhiali",
      "👓",
      "🤓"
    )(
      gif"ytai_SistemazioneOcchialiGif.mp4"
    ),
    ReplyBundleMessage.textToGif(
      "miele"
    )(
      gif"ytai_ForteProfumoMieleGif.mp4",
      gif"ytai_AppiccicaticcioMieleGif.mp4"
    ),
    ReplyBundleMessage.textToGif(
      "appiccicaticc"
    )(
      gif"ytai_AppiccicaticcioMieleGif.mp4"
    ),
    ReplyBundleMessage.textToGif(
      "scaldato",
      "panini",
      "sono ottimi"
    )(
      gif"ytai_GrazieScaldatoPaniniGif.mp4"
    ),
    ReplyBundleMessage.textToGif(
      "\\bterminato\\b".r,
      "facilit[aàá]".r
    )(
      gif"ytai_EstremaFacilitaGif.mp4"
    ),
    ReplyBundleMessage.textToGif(
      "notturna"
    )(gif"ytai_FameSeteNotturnaGif.mp4"),
    ReplyBundleMessage.textToGif(
      "alpino",
      "eccolo qui",
      "si chiama"
    )(gif"ytai_PaninoAlpinoGif.mp4"),
    ReplyBundleMessage.textToGif(
      "waffel",
      "inzuppati",
      "profumo gradevol(e|issimo)".r
    )(gif"ytai_WaffelInzuppatiGif.mp4"),
    ReplyBundleMessage.textToGif(
      "oliva"
    )(
      gif"ytai_MoltoGustosaOlivaGif.mp4"
    ),
    ReplyBundleMessage.textToGif(
      "mpincia",
      "cosa (mi )bevo".r
    )(
      gif"ytai_MpinciaGif.mp4"
    ),
    ReplyBundleMessage.textToGif(
      "calabria"
    )(
      gif"ytai_MpinciaGif.mp4",
      gif"ytai_RinfrescanteDiCalabriaGif.mp4"
    ),
    ReplyBundleMessage.textToGif("galbanone")(
      gif"ytai_LoopPaninoCottoGalbanoneGif.mp4"
    ),
    ReplyBundleMessage
      .textToGif(
        "scuotere",
        "squotimento"
      )(
        gif"ytai_LoopBalloGif.mp4"
      ),
    ReplyBundleMessage.textToGif(
      "scatol"
    )(
      gif"ytai_LoopScatolaGif.mp4"
    ),
    ReplyBundleMessage
      .textToGif(
        "bottiglia",
        "san[ ]?benedetto".r
      )(
        gif"ytai_LoopBottigliaGif.mp4"
      )
  )
} // end Gif
