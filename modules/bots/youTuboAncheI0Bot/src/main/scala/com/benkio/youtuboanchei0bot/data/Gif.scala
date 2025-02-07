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
      gif"ytai_LaReputazione.mp4",
      gif"ytai_CheVergogna.mp4"
    ),
    ReplyBundleMessage.textToGif[F](
      "ciccione"
    )(
      gif"ytai_Molla.mp4",
      gif"ytai_CiccioneObesoMangiTanto.mp4"
    ),
    ReplyBundleMessage.textToGif[F](
      "fatica",
      "sudore",
      "sudato"
    )(
      gif"ytai_Affaticamento.mp4"
    ),
    ReplyBundleMessage.textToGif[F](
      "ascolta (queste|le) mie parole".r.tr(21),
      "amareggiati",
      "dedicaci (il tuo tempo|le tue notti)".r.tr(21)
    )(
      gif"ytai_Amareggiati.mp4"
    ),
    ReplyBundleMessage.textToGif[F](
      "\\barchitetta\\b".r.tr(10),
      "\\bnotaia\\b".r.tr(6),
      "\\bministra\\b".r.tr(8),
      "\\bavvocata\\b".r.tr(8)
    )(
      gif"ytai_Architetta.mp4"
    ),
    ReplyBundleMessage.textToGif[F](
      "bel sogno"
    )(
      gif"ytai_BelSogno.mp4"
    ),
    ReplyBundleMessage.textToGif[F](
      "brivido",
      "fremito",
      "tremito",
      "tremore"
    )(
      gif"ytai_Brivido.mp4"
    ),
    ReplyBundleMessage.textToGif[F](
      "buonanotte"
    )(
      gif"ytai_Buonanotte.mp4",
      gif"ytai_BuonanotteBrunchPlus.mp4",
      gif"ytai_BuonanotteFollowers.mp4"
    ),
    ReplyBundleMessage.textToGif[F](
      "buonasera"
    )(
      gif"ytai_Buonasera.mp4"
    ),
    ReplyBundleMessage.textToGif[F](
      "che spettacolo"
    )(
      gif"ytai_CheSpettacolo.mp4"
    ),
    ReplyBundleMessage.textToGif[F](
      "ciao ragazzi",
      "cari saluti"
    )(
      gif"ytai_CiaoRagazzi.mp4"
    ),
    ReplyBundleMessage.textToGif[F](
      "ci divertiremo",
      "bel percorso"
    )(
      gif"ytai_CiDivertiremoPercorso.mp4"
    ),
    ReplyBundleMessage.textToGif[F](
      "corpulenti",
      "ciccioni"
    )(
      gif"ytai_CorpulentiCiccioni.mp4"
    ),
    ReplyBundleMessage.textToGif[F](
      "culetto"
    )(
      gif"ytai_Culetto.mp4"
    ),
    ReplyBundleMessage.textToGif[F](
      "ed allora"
    )(
      gif"ytai_EdAllora.mp4"
    ),
    ReplyBundleMessage.textToGif[F](
      "fai pure"
    )(
      gif"ytai_FaiPure.mp4"
    ),
    ReplyBundleMessage.textToGif[F](
      "fallo anche (tu|te)".r.tr(14)
    )(
      gif"ytai_FalloAncheTu.mp4"
    ),
    ReplyBundleMessage.textToGif[F](
      "filet[ -]?o[ -]?fish".r.tr(10)
    )(
      gif"ytai_FiletOFish.mp4",
      gif"ytai_FiletOFish2.mp4",
      gif"ytai_BagnarloAcqua.mp4"
    ),
    ReplyBundleMessage.textToGif[F](
      "(deluso|insoddisfatto|inappagato|abbattuto|scoraggiato|demoralizzato|depresso|demotivato|avvilito|scocciato)".r
        .tr(6)
    )(
      gif"ytai_Frustrazione.mp4"
    ),
    ReplyBundleMessage.textToGif[F](
      "ci sto già pensando"
    )(
      gif"ytai_GiaPensando.mp4"
    ),
    ReplyBundleMessage.textToGif[F](
      "sono grande",
      "sono corpulento"
    )(
      gif"ytai_GrandeCorpulento.mp4"
    ),
    ReplyBundleMessage.textToGif[F](
      "grazie dottore"
    )(
      gif"ytai_GrazieDottore.mp4"
    ),
    ReplyBundleMessage.textToGif[F](
      "sconforto grave"
    )(
      gif"ytai_GrazieTante.mp4"
    ),
    ReplyBundleMessage.textToGif[F](
      "incredibile profumo",
      "incredibile aroma"
    )(
      gif"ytai_IncredibileAromaProfumo.mp4"
    ),
    ReplyBundleMessage.textToGif[F](
      "incredibile",
      "inimitabile",
      "the number (one|1)".r.tr(12)
    )(
      gif"ytai_IncredibileInimitabile.mp4"
    ),
    ReplyBundleMessage.textToGif[F](
      "non lo so"
    )(
      gif"ytai_IoNonLoSo.mp4"
    ),
    ReplyBundleMessage.textToGif[F](
      "loro dovere",
      "vostro diritto"
    )(
      gif"ytai_LoroDovereVostroDiritto.mp4"
    ),
    ReplyBundleMessage.textToGif[F](
      "lo so (bene )?anche io".r.tr(14)
    )(
      gif"ytai_LoSoAncheIo.mp4"
    ),
    ReplyBundleMessage.textToGif[F](
      "mah"
    )(
      gif"ytai_Mah.mp4",
      gif"ytai_Mah2.mp4",
      gif"ytai_Mah3.mp4",
      gif"ytai_ZoomMah.mp4"
    ),
    ReplyBundleMessage.textToGif[F](
      "meraviglioso"
    )(
      gif"ytai_Meraviglioso.mp4"
    ),
    ReplyBundleMessage.textToGif[F](
      "molla",
      "grassone",
      "ancora non sei morto"
    )(
      gif"ytai_Molla.mp4"
    ),
    ReplyBundleMessage.textToGif[F](
      "molto buona"
    )(
      gif"ytai_MoltoBuona.mp4",
      gif"ytai_MoltoBuona2.mp4",
      gif"ytai_Buona.mp4"
    ),
    ReplyBundleMessage.textToGif[F](
      "monoporzioni",
      "mezzo (chilo|kg)".r.tr(8)
    )(
      gif"ytai_MonoporzioniTiramisu.mp4"
    ),
    ReplyBundleMessage.textToGif[F](
      "non fa male"
    )(
      gif"ytai_NonFaMale.mp4",
      gif"ytai_AcquaMiglioreDrink.mp4"
    ),
    ReplyBundleMessage.textToGif[F](
      "non la crede nessuno questa cosa",
      "non ci crede nessuno"
    )(
      gif"ytai_NonLaCredeNessuno.mp4"
    ),
    ReplyBundleMessage.textToGif[F](
      "non pensiamoci più"
    )(
      gif"ytai_NonPensiamoci.mp4"
    ),
    ReplyBundleMessage.textToGif[F](
      "incomprensione",
      "non vi capiscono"
    )(
      gif"ytai_NonViCapiscono.mp4"
    ),
    ReplyBundleMessage.textToGif[F](
      "occhiolino",
      ";)",
      "😉"
    )(
      gif"ytai_Occhiolino.mp4",
      gif"ytai_Occhiolino2.mp4",
      gif"ytai_Occhiolino3.mp4",
      gif"ytai_OcchiolinoTestaDondolante.mp4"
    ),
    ReplyBundleMessage.textToGif[F](
      "olè"
    )(
      gif"ytai_Ole.mp4"
    ),
    ReplyBundleMessage.textToGif[F](
      "olè anche io"
    )(
      gif"ytai_OleAncheIo.mp4"
    ),
    ReplyBundleMessage.textToGif[F](
      "la perfezione",
      "la nostra tendenza"
    )(
      gif"ytai_PerfezioneTendenza.mp4"
    ),
    ReplyBundleMessage.textToGif[F](
      "profumo meraviglioso"
    )(
      gif"ytai_ProfumoMeraviglioso.mp4",
      gif"ytai_ProfumoGamberettiSalmone.mp4"
    ),
    ReplyBundleMessage.textToGif[F](
      "sentiamo il profumo"
    )(
      gif"ytai_ProfumoMeraviglioso.mp4"
    ),
    ReplyBundleMessage.textToGif[F](
      "ringraziamento"
    )(
      gif"ytai_Ringraziamento.mp4"
    ),
    ReplyBundleMessage.textToGif[F](
      "se non mi aiuta",
      "cosa mi aiuta"
    )(
      gif"ytai_SentiteCheRoba.mp4"
    ),
    ReplyBundleMessage.textToGif[F](
      "sete",
      "(sorso|bicchiere) d'acqua".r.tr(13)
    )(
      gif"ytai_Sete.mp4",
      gif"ytai_AcquaMeravigliosa.mp4",
      gif"ytai_FameSeteNotturna.mp4"
    ),
    ReplyBundleMessage.textToGif[F](
      "🤷"
    )(
      gif"ytai_Shrug.mp4"
    ),
    ReplyBundleMessage.textToGif[F](
      "sta per dire qualcosa"
    )(
      gif"ytai_Silenzio.mp4"
    ),
    ReplyBundleMessage.textToGif[F](
      "silenzio[,]? silenzio".r.tr(17)
    )(
      gif"ytai_Silenzio.mp4",
      gif"ytai_SilenzioMomentoMagico.mp4"
    ),
    ReplyBundleMessage.textToGif[F](
      "si v[àa] finch[eé] si v[aà]".r.tr(18),
      "quando non si potrà andare più",
      "è tanto facile"
    )(
      gif"ytai_SiVaFincheSiVa.mp4"
    ),
    ReplyBundleMessage.textToGif[F](
      "sorprendente"
    )(
      gif"ytai_Sorprendente.mp4",
      gif"ytai_SecondoBocconeSorprendente.mp4"
    ),
    ReplyBundleMessage.textToGif[F](
      "(😄|😀|😃){3,}".r.tr(3),
      "sorriso"
    )(
      gif"ytai_Sorriso.mp4",
      gif"ytai_Sorriso2.mp4"
    ),
    ReplyBundleMessage.textToGif[F](
      "spuntino"
    )(
      gif"ytai_SpuntinoConMe.mp4",
      gif"ytai_SpuntinoConMe2.mp4",
      gif"ytai_SpuntinoConMe3.mp4",
      gif"ytai_BuonoSpuntino.mp4",
      gif"ytai_PaninoBuonoSpuntito.mp4",
      gif"ytai_SpuntinoSmart.mp4",
      gif"ytai_BuonoSpuntinoAncheATe.mp4"
    ),
    ReplyBundleMessage.textToGif[F](
      "l'unica cosa che sai fare"
    )(
      gif"ytai_UnicaCosaMangiare.mp4"
    ),
    ReplyBundleMessage.textToGif[F](
      "uno o due"
    )(
      gif"ytai_UnoODue.mp4"
    ),
    ReplyBundleMessage.textToGif[F](
      "zenzero",
      "mia risposta"
    )(
      gif"ytai_Zenzero.mp4"
    ),
    ReplyBundleMessage.textToGif[F](
      "zoom",
      "guardo da vicino"
    )(
      gif"ytai_Zoom.mp4",
      gif"ytai_ZoomMah.mp4"
    ),
    ReplyBundleMessage.textToGif[F](
      "tanti auguri"
    )(
      gif"ytai_TantiAuguri.mp4"
    ),
    ReplyBundleMessage.textToGif[F](
      "auguri di gusto"
    )(
      gif"ytai_AuguriDiGusto.mp4"
    ),
    ReplyBundleMessage.textToGif[F](
      "bagnarlo"
    )(
      gif"ytai_BagnarloAcqua.mp4"
    ),
    ReplyBundleMessage.textToGif[F](
      "benvenuti",
      "ora e sempre"
    )(
      gif"ytai_Benvenuti.mp4"
    ),
    ReplyBundleMessage.textToGif[F](
      "diploma",
      "per pisciare",
      "ma (che )?stiamo scherzando".r.tr(20)
    )(
      gif"ytai_DiplomaPisciare.mp4"
    ),
    ReplyBundleMessage.textToGif[F](
      "non (mi sento|sto) bene".r.tr(12)
    )(
      gif"ytai_DiversiGioniNonStoBene.mp4"
    ),
    ReplyBundleMessage.textToGif[F](
      "(grazie e )?arrivederci".r.tr(11)
    )(
      gif"ytai_GrazieArrivederci.mp4"
    ),
    ReplyBundleMessage.textToGif[F](
      "eccitato"
    )(
      gif"ytai_MoltoEccitato.mp4"
    ),
    ReplyBundleMessage.textToGif[F](
      "no pain",
      "no gain"
    )(
      gif"ytai_NoPainNoGain.mp4"
    ),
    ReplyBundleMessage.textToGif[F](
      "non hai più scuse",
      "riprenditi",
      "sei in gamba"
    )(
      gif"ytai_NoScuse.mp4"
    ),
    ReplyBundleMessage.textToGif[F](
      "caspita",
      "sono (grosso|sono (quasi )?enorme|una palla di lardo)".r.tr(11)
    )(
      gif"ytai_PallaDiLardo.mp4"
    ),
    ReplyBundleMessage.textToGif[F](
      "mette paura"
    )(
      gif"ytai_Paura.mp4"
    ),
    ReplyBundleMessage.textToGif[F](
      "per voi"
    )(
      gif"ytai_PerVoi.mp4"
    ),
    ReplyBundleMessage.textToGif[F](
      "alimenti",
      "allegria"
    )(
      gif"ytai_PizzaAllegria.mp4"
    ),
    ReplyBundleMessage.textToGif[F](
      "gamberetti"
    )(
      gif"ytai_ProfumoGamberettiSalmone.mp4",
      gif"ytai_Sorpresa.mp4"
    ),
    ReplyBundleMessage.textToGif[F](
      "\\bproviamo\\b".r.tr(8),
      "senza morire"
    )(
      gif"ytai_ProviamoSenzaMorire.mp4"
    ),
    ReplyBundleMessage.textToGif[F](
      "quello che riesco a fare"
    )(
      gif"ytai_RiescoAFare.mp4"
    ),
    ReplyBundleMessage.textToGif[F](
      "sentendo (davvero )?male".r.tr(13)
    )(
      gif"ytai_SentendoDavveroMale.mp4"
    ),
    ReplyBundleMessage.textToGif[F](
      "momento magico"
    )(
      gif"ytai_SilenzioMomentoMagico.mp4"
    ),
    ReplyBundleMessage.textToGif[F](
      "sopraffino"
    )(
      gif"ytai_Sopraffino.mp4"
    ),
    ReplyBundleMessage.textToGif[F](
      "😮",
      "😯"
    )(
      gif"ytai_Sorpresa.mp4"
    ),
    ReplyBundleMessage.textToGif[F](
      "staccato (quasi )?il naso".r.tr(16)
    )(
      gif"ytai_StaccatoNaso.mp4"
    ),
    ReplyBundleMessage.textToGif[F](
      "cuc[uù]+".r.tr(4)
    )(
      gif"ytai_Cucu.mp4"
    ),
    ReplyBundleMessage.textToGif[F](
      "al[e]+ [o]{2,}".r.tr(6)
    )(
      gif"ytai_AleOoo.mp4"
    ),
    ReplyBundleMessage.textToGif[F](
      "fare senza",
      "faenza"
    )(
      gif"ytai_SenzaFaenza.mp4"
    ),
    ReplyBundleMessage.textToGif[F](
      "capolavoro",
      "\\bmeravigliosa\\b".r.tr(12)
    )(
      gif"ytai_CapolavoroMeravigliosa.mp4"
    ),
    ReplyBundleMessage.textToGif[F](
      "ce la puoi fare"
    )(
      gif"ytai_CeLaPuoiFare.mp4"
    ),
    ReplyBundleMessage.textToGif[F](
      "che profumo"
    )(
      gif"ytai_CheProfumo.mp4",
      gif"ytai_ECheProfumo.mp4"
    ),
    ReplyBundleMessage.textToGif[F](
      "eccolo qua"
    )(
      gif"ytai_EccoloQua.mp4"
    ),
    ReplyBundleMessage.textToGif[F](
      "non mi piacciono"
    )(
      gif"ytai_NonMiPiaccionoQuesteCose.mp4"
    ),
    ReplyBundleMessage.textToGif[F](
      "nonno"
    )(
      gif"ytai_NonnoMito.mp4"
    ),
    ReplyBundleMessage.textToGif[F](
      "soltanto per questo",
      "denaro",
      "guadagno"
    )(
      gif"ytai_SoltantoPerQuesto.mp4"
    ),
    ReplyBundleMessage.textToGif[F](
      "dubbi enciclopedici",
      "rifletteteci"
    )(
      gif"ytai_DubbiEnciclopedici.mp4"
    ),
    ReplyBundleMessage.textToGif[F](
      "miei limiti"
    )(
      gif"ytai_Limiti.mp4"
    ),
    ReplyBundleMessage.textToGif[F](
      "panino"
    )(
      gif"ytai_PaninoBuonoSpuntito.mp4",
      gif"ytai_Panino.mp4",
      gif"ytai_PaninoAlpino.mp4",
      gif"ytai_LoopPaninoCottoGalbanone.mp4"
    ),
    ReplyBundleMessage.textToGif[F](
      "m[ ]?&[ ]?m['s]?".r.tr(3),
      "rotear",
      "ruotar"
    )(
      gif"ytai_M_Ms.mp4",
      gif"ytai_M_MsLoop.mp4"
    ),
    ReplyBundleMessage.textToGif[F](
      "(😂|🤣){3,}".r.tr(4),
      "(ah|ha){5,}".r.tr(14)
    )(
      gif"ytai_Risata.mp4"
    ),
    ReplyBundleMessage.textToGif[F](
      "obeso",
      "te lo meriti",
      "mangi tanto"
    )(
      gif"ytai_CiccioneObesoMangiTanto.mp4"
    ),
    ReplyBundleMessage.textToGif[F](
      "si vede"
    )(
      gif"ytai_SiVede.mp4"
    ),
    ReplyBundleMessage.textToGif[F](
      "correre non serve",
      "\\bfretta\\b".r.tr(6)
    )(
      gif"ytai_CorrereNonServe.mp4"
    ),
    ReplyBundleMessage.textToGif[F](
      "marmellata",
      "santa rosa"
    )(
      gif"ytai_TorreMarmellata.mp4"
    ),
    ReplyBundleMessage.textToGif[F](
      "(la|una) torre".r.tr(8)
    )(
      gif"ytai_TorreMarmellata.mp4",
      gif"ytai_TorreKinderFettaLatte.mp4"
    ),
    ReplyBundleMessage.textToGif[F](
      "salmone",
      "🐟"
    )(
      gif"ytai_SalmoneUnico.mp4",
      gif"ytai_TartinaSalmone.mp4",
      gif"ytai_SalmoneLoop.mp4"
    ),
    ReplyBundleMessage.textToGif[F](
      "un successo",
      "agrodolci"
    )(
      gif"ytai_SaporiAgrodolciSpettacolari.mp4"
    ),
    ReplyBundleMessage.textToGif[F](
      "non parlate di",
      "questioni filosofiche",
      "non ci azzecc"
    )(
      gif"ytai_QuestioniFilosofiche.mp4"
    ),
    ReplyBundleMessage.textToGif[F](
      "mang[ai] in (mia )?compagnia".r.tr(18)
    )(
      gif"ytai_InCompagnia.mp4"
    ),
    ReplyBundleMessage.textToGif[F](
      "mi incontrate",
      "la pancia",
      "in imbarazzo"
    )(
      gif"ytai_IncontratePanciaImbarazzo.mp4"
    ),
    ReplyBundleMessage.textToGif[F](
      "caro amico",
      "chiarire"
    )(
      gif"ytai_GrazieAmicoChiarire.mp4"
    ),
    ReplyBundleMessage.textToGif[F](
      "comm(uo|o)vendo".r.tr(10)
    )(
      gif"ytai_Commovendo.mp4",
      gif"ytai_CommuovendoFareQuelloChePiace.mp4"
    ),
    ReplyBundleMessage.textToGif[F](
      "coda"
    )(
      gif"ytai_CodaLunga.mp4"
    ),
    ReplyBundleMessage.textToGif[F](
      "brindare",
      "drink"
    )(
      gif"ytai_AcquaMiglioreDrink.mp4"
    ),
    ReplyBundleMessage.textToGif[F](
      "forchetta"
    )(
      gif"ytai_MancaForchetta.mp4"
    ),
    ReplyBundleMessage.textToGif[F](
      "solo[?]{2,}".r.tr(5)
    )(
      gif"ytai_Solo.mp4"
    ),
    ReplyBundleMessage.textToGif[F](
      "ottimo[!]+".r.tr(5)
    )(
      gif"ytai_Ottimo.mp4"
    ),
    ReplyBundleMessage.textToGif[F](
      "sei uno sfigato"
    )(
      gif"ytai_SeiUnoSfigato.mp4"
    ),
    ReplyBundleMessage.textToGif[F](
      "non prendetevi confidenze",
      "inteso di darvi"
    )(
      gif"ytai_Confidenze.mp4"
    ),
    ReplyBundleMessage.textToGif[F](
      "illegale"
    )(
      gif"ytai_IllegaleFuorilegge.mp4"
    ),
    ReplyBundleMessage.textToGif[F](
      "ne ho già parlato",
      "tornare sugli stessi punti",
      "lamentato con me"
    )(
      gif"ytai_NeHoGiaParlato.mp4"
    ),
    ReplyBundleMessage.textToGif[F](
      "alla salute"
    )(
      gif"ytai_AllaSalute.mp4"
    ),
    ReplyBundleMessage.textToGif[F](
      "grazie ragazzi",
      "grazie a tutti"
    )(
      gif"ytai_GrazieRagazzi.mp4"
    ),
    ReplyBundleMessage.textToGif[F](
      "mi (reputi|consideri) intelligente".r.tr(22),
      "mi (reputi|consideri) sensibile".r.tr(19),
      "sensibile e intelligente"
    )(
      gif"ytai_SensibileIntelligente.mp4"
    ),
    ReplyBundleMessage.textToGif[F](
      "dico basta"
    )(
      gif"ytai_AdessoViDicoBasta.mp4"
    ),
    ReplyBundleMessage.textToGif[F](
      "che bontà",
      "eccoli qua"
    )(
      gif"ytai_ECheProfumo.mp4"
    ),
    ReplyBundleMessage.textToGif[F](
      "parlo poco",
      "ingozzo",
      "non ve la prendete"
    )(
      gif"ytai_SeParloPoco.mp4"
    ),
    ReplyBundleMessage.textToGif[F](
      "meno di un minuto"
    )(
      gif"ytai_TiramisuMenoDiUnMinuto.mp4"
    ),
    ReplyBundleMessage.textToGif[F](
      "fetta al latte"
    )(
      gif"ytai_TorreKinderFettaLatte.mp4",
      gif"ytai_KinderFettaAlLatte.mp4"
    ),
    ReplyBundleMessage.textToGif[F](
      "lasciate libera",
      "linea telefonica",
      "per cose reali",
      "che mi serve"
    )(
      gif"ytai_LineaTelefonica.mp4"
    ),
    ReplyBundleMessage.textToGif[F](
      "ce l'ho fatta",
      "\\bp(à|a')\\b".r.tr(2)
    )(
      gif"ytai_CeLhoFatta.mp4"
    ),
    ReplyBundleMessage.textToGif[F](
      "grazie tante"
    )(
      gif"ytai_GrazieTante.mp4",
      gif"ytai_GrazieTante2.mp4"
    ),
    ReplyBundleMessage.textToGif[F](
      "beviamoci sopra",
      "non c'è alcohol"
    )(
      gif"ytai_BeviamociSopraNoAlcohol.mp4"
    ),
    ReplyBundleMessage.textToGif[F](
      "cercherò di prepararlo"
    )(
      gif"ytai_CercheroDiPrepararlo.mp4"
    ),
    ReplyBundleMessage.textToGif[F](
      "dipende da come mi sento"
    )(
      gif"ytai_DipendeDaComeMiSento.mp4"
    ),
    ReplyBundleMessage.textToGif[F](
      "eccellente"
    )(
      gif"ytai_Eccellente.mp4"
    ),
    ReplyBundleMessage.textToGif[F](
      "non fatelo più"
    )(
      gif"ytai_NonFateloPiu.mp4"
    ),
    ReplyBundleMessage.textToGif[F](
      "molto dolce",
      "molto buono"
    )(
      gif"ytai_MoltoDolceMoltoBuono.mp4"
    ),
    ReplyBundleMessage.textToGif[F](
      "ne avevo proprio voglia"
    )(
      gif"ytai_NeAvevoProprioVoglia.mp4"
    ),
    ReplyBundleMessage.textToGif[F](
      "c'entra (quasi )?sempre".r.tr(14)
    )(
      gif"ytai_CentraQuasiSempre.mp4"
    ),
    ReplyBundleMessage.textToGif[F](
      "riesco a darvi",
      "imparare (anche io )?(un po' )?di più".r.tr(15)
    )(
      gif"ytai_DarviImparare.mp4"
    ),
    ReplyBundleMessage.textToGif[F](
      "forte profumo"
    )(
      gif"ytai_ForteProfumoMiele.mp4"
    ),
    ReplyBundleMessage.textToGif[F](
      "😋",
      "yum",
      "gustoso"
    )(
      gif"ytai_GestoGustoso.mp4",
      gif"ytai_MoltoGustoso.mp4"
    ),
    ReplyBundleMessage.textToGif[F](
      "mia filosofia",
      "risol(to|vere) con tutti".r.tr(15)
    )(
      gif"ytai_HoRisoltoConTutti.mp4"
    ),
    ReplyBundleMessage.textToGif[F](
      "sentendo in compagnia"
    )(
      gif"ytai_MiStoSentendoInCompagnia.mp4"
    ),
    ReplyBundleMessage.textToGif[F](
      "vengono le parole",
      "intendi dire"
    )(
      gif"ytai_NoParoleMostraIntenzioni.mp4"
    ),
    ReplyBundleMessage.textToGif[F](
      "quello che ci voleva",
      "bibita (bella )?fresca".r.tr(13)
    )(
      gif"ytai_QuestaBibitaBellaFresca.mp4"
    ),
    ReplyBundleMessage.textToGif[F](
      "secondo boccone"
    )(
      gif"ytai_SecondoBocconeSorprendente.mp4"
    ),
    ReplyBundleMessage.textToGif[F](
      "veterano",
      "chat day"
    )(
      gif"ytai_VeteranoChatDays.mp4"
    ),
    ReplyBundleMessage.textToGif[F](
      "vivete questo momento"
    )(
      gif"ytai_ViveteQuestoMomentoConMe.mp4"
    ),
    ReplyBundleMessage.textToGif[F](
      "\\btremo\\b".r.tr(5),
      "le mie condizioni"
    )(
      gif"ytai_Tremo.mp4"
    ),
    ReplyBundleMessage.textToGif[F](
      "fallo anche (te|tu)".r.tr(14),
      "\\bcome me\\b".r.tr(5)
    )(
      gif"ytai_FalloAncheTeComeMe.mp4"
    ),
    ReplyBundleMessage.textToGif[F](
      "cercando di fare",
      "del mio meglio"
    )(
      gif"ytai_FareDelMioMeglio.mp4"
    ),
    ReplyBundleMessage.textToGif[F](
      "limitato molto",
      "essere privato",
      "questi soldi"
    )(
      gif"ytai_PrivatoSoldiLimitatoMolto.mp4"
    ),
    ReplyBundleMessage.textToGif[F](
      "\\bball(o|are|i)\\b".r.tr(5),
      "\\bdanz(a|are|i)\\b".r.tr(5)
    )(
      gif"ytai_Ballo.mp4"
    ),
    ReplyBundleMessage.textToGif[F](
      "zebra",
      "giraffa"
    )(
      gif"ytai_ZebraGiraffa.mp4"
    ),
    ReplyBundleMessage.textToGif[F](
      "smart"
    )(
      gif"ytai_SpuntinoSmart.mp4"
    ),
    ReplyBundleMessage.textToGif[F](
      "ipocrita"
    )(
      gif"ytai_SonoIpocrita.mp4"
    ),
    ReplyBundleMessage.textToGif[F](
      "imbarazz(o|ato)".r.tr(9)
    )(
      gif"ytai_SentireInImbarazzo.mp4",
      gif"ytai_LeggermenteImbarazzato.mp4"
    ),
    ReplyBundleMessage.textToGif[F](
      "nel bene",
      "nel male"
    )(
      gif"ytai_CiaoFollowersNelBeneNelMale.mp4"
    ),
    ReplyBundleMessage.textToGif[F](
      "se volete sapere",
      "100%",
      "non va per me"
    )(
      gif"ytai_SapereTuttoNonVa.mp4"
    ),
    ReplyBundleMessage.textToGif[F](
      "molto aromatico",
      "affumicatura",
      "pepe nero"
    )(
      gif"ytai_MoltoAromatico.mp4"
    ),
    ReplyBundleMessage.textToGif[F](
      "ci riusciremo"
    )(
      gif"ytai_NonViPreoccupateCiRiusciremo.mp4"
    ),
    ReplyBundleMessage.textToGif[F](
      "non vi preoccupate"
    )(
      gif"ytai_NonViPreoccupateCiRiusciremo.mp4",
      gif"ytai_BeviamociSopraNoAlcohol.mp4"
    ),
    ReplyBundleMessage.textToGif[F](
      "fuorilegge"
    )(
      gif"ytai_NonSonoFuorileggeNecessita.mp4",
      gif"ytai_IllegaleFuorilegge.mp4"
    ),
    ReplyBundleMessage.textToGif[F](
      "ho necessità"
    )(
      gif"ytai_NonSonoFuorileggeNecessita.mp4"
    ),
    ReplyBundleMessage.textToGif[F](
      "mi fa (tanto )?piacere".r.tr(12)
    )(
      gif"ytai_MiFaTantoPiacere.mp4"
    ),
    ReplyBundleMessage.textToGif[F](
      "\\bshow\\b".r.tr(4)
    )(
      gif"ytai_LoShowDeveContunuare.mp4"
    ),
    ReplyBundleMessage.textToGif[F](
      "felice di ritrovarvi"
    )(
      gif"ytai_FeliceDiRitrovarvi.mp4"
    ),
    ReplyBundleMessage.textToGif[F](
      "fa(re|rti|tti) (due |i )?conti".r.tr(9),
      "il lavoro che fai"
    )(
      gif"ytai_FattiDueConti.mp4"
    ),
    ReplyBundleMessage.textToGif[F](
      "inquietante"
    )(
      gif"ytai_NonInquietante.mp4"
    ),
    ReplyBundleMessage.textToGif[F](
      "vediamo un po'"
    )(
      gif"ytai_VediamoUnPo.mp4"
    ),
    ReplyBundleMessage.textToGif[F](
      "rinfrescante",
      "s(\\.)?r(\\.)?l(\\.)?".r.tr(3) // s.r.l.
    )(
      gif"ytai_RinfrescanteDiCalabria.mp4"
    ),
    ReplyBundleMessage.textToGif[F](
      "prima o poi"
    )(
      gif"ytai_NonViPreoccupateCiRiusciremo.mp4",
      gif"ytai_PassioneAllevareGalline.mp4"
    ),
    ReplyBundleMessage.textToGif[F](
      "allevare",
      "galline",
      "mi[ae] passion[ie]".r.tr(12)
    )(
      gif"ytai_PassioneAllevareGalline.mp4"
    ),
    ReplyBundleMessage.textToGif[F](
      "com'è venuto",
      "perfetto"
    )(
      gif"ytai_Perfetto.mp4"
    ),
    ReplyBundleMessage.textToGif[F](
      "più piccoline",
      "sono dolci",
      "al punto giusto"
    )(
      gif"ytai_DolciAlPuntoGiusto.mp4"
    ),
    ReplyBundleMessage.textToGif[F](
      "amarognolo",
      "si intona",
      "non guasta",
      "contrasto"
    )(
      gif"ytai_ContrastoAmarognolo.mp4"
    ),
    ReplyBundleMessage.textToGif[F](
      "bella fresca"
    )(
      gif"ytai_BellaFresca.mp4"
    ),
    ReplyBundleMessage.textToGif[F](
      "piace mangiare così",
      "critiche"
    )(
      gif"ytai_MangiareCritiche.mp4"
    ),
    ReplyBundleMessage.textToGif[F](
      "ostica",
      "insalata"
    )(
      gif"ytai_OsticaInsalata.mp4"
    ),
    ReplyBundleMessage.textToGif[F](
      "\\blotte\\b".r.tr(5),
      "condivido"
    )(
      gif"ytai_PersonaliLotteFollowers.mp4"
    ),
    ReplyBundleMessage.textToGif[F](
      "condizione umana",
      "patologico",
      "individuo che comunque vive"
    )(
      gif"ytai_CondizioneUmana.mp4"
    ),
    ReplyBundleMessage.textToGif[F](
      "occhiali",
      "👓",
      "🤓"
    )(
      gif"ytai_SistemazioneOcchiali.mp4"
    ),
    ReplyBundleMessage.textToGif[F](
      "miele"
    )(
      gif"ytai_ForteProfumoMiele.mp4",
      gif"ytai_AppiccicaticcioMiele.mp4"
    ),
    ReplyBundleMessage.textToGif[F](
      "appiccicaticcio"
    )(
      gif"ytai_AppiccicaticcioMiele.mp4"
    ),
    ReplyBundleMessage.textToGif[F](
      "scaldato",
      "panini",
      "sono ottimi"
    )(
      gif"ytai_GrazieScaldatoPanini.mp4"
    ),
    ReplyBundleMessage.textToGif[F](
      "\\bterminato\\b".r.tr(9),
      "facilit[aàá]".r.tr(7)
    )(
      gif"ytai_EstremaFacilita.mp4"
    ),
    ReplyBundleMessage.textToGif[F](
      "notturna"
    )(gif"ytai_FameSeteNotturna.mp4"),
    ReplyBundleMessage.textToGif[F](
      "alpino",
      "eccolo qui",
      "si chiama"
    )(gif"ytai_PaninoAlpino.mp4"),
    ReplyBundleMessage.textToGif[F](
      "waffel",
      "inzuppati",
      "profumo gradevol(e|issimo)".r.tr(16)
    )(gif"ytai_WaffelInzuppati.mp4"),
    ReplyBundleMessage.textToGif[F](
      "oliva"
    )(
      gif"ytai_MoltoGustosaOliva.mp4"
    ),
    ReplyBundleMessage.textToGif[F](
      "mpincia",
      "cosa (mi )bevo".r.tr(9)
    )(
      gif"ytai_Mpincia.mp4"
    ),
    ReplyBundleMessage.textToGif[F](
      "calabria"
    )(
      gif"ytai_Mpincia.mp4",
      gif"ytai_RinfrescanteDiCalabria.mp4"
    ),
    ReplyBundleMessage.textToGif[F]("galbanone")(
      gif"ytai_LoopPaninoCottoGalbanone.mp4"
    )
  )
end Gif
