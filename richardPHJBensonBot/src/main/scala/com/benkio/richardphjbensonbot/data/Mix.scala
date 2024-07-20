package com.benkio.richardphjbensonbot.data

import cats.Applicative
import com.benkio.telegrambotinfrastructure.messagefiltering.*
import com.benkio.telegrambotinfrastructure.model.*

object Mix {

  def messageRepliesMixData[F[_]: Applicative]: List[ReplyBundleMessage[F]] = List(
    ReplyBundleMessage
      .textToMedia[F](
        "vivi",
        "morti"
      )(
        vid"rphjb_ViviMorti.mp4"
      )
      .copy(matcher = ContainsAll),
    ReplyBundleMessage.textToMedia[F](
      "è un ordine"
    )(
      mp3"rphjb_Ordine.mp3",
      vid"rphjb_Ordine.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      "piloti d'aereo",
      "disastri aerei"
    )(
      gif"rphjb_DrogatiPilotiGif.mp4",
      vid"rphjb_EricClaptonDrogaUominiAffari.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      "\\brock\\b".r.tr(4)
    )(
      vid"rphjb_PoesiaRock.mp4",
      mp3"rphjb_Rock.mp3",
      vid"rphjb_EricClaptonDrogaUominiAffari.mp4",
      vid"rphjb_Rock.mp4",
      vid"rphjb_BluesEbbeBambinoRockNRollBeBopALulaStormyMondayButTuesdayIsJustAsBad.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      "ti distruggo"
    )(
      gif"rphjb_TiDistruggo.mp4",
      vid"rphjb_Ramarro.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      "cristo pinocchio",
      "lumicino",
      "(strade|vie) inferiori".r.tr(13)
    )(
      mp3"rphjb_CristoPinocchio.mp3",
      vid"rphjb_CristoPinocchio.mp4",
      vid"rphjb_PoesiaMaria.mp4",
      vid"rphjb_MiDragaStradeInferioriCristoPinocchioGerarchieInfernali.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      "pappalardo"
    )(
      mp3"rphjb_Pappalardo.mp3",
      vid"rphjb_Pappalardo.mp4",
      mp3"rphjb_FrocioFrocio.mp3",
      vid"rphjb_FrocioFrocio2.mp4",
      vid"rphjb_StoriaMarlinManson.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      "lasciami in pace",
      "\\bstronza\\b".r.tr(7)
    )(
      gif"rphjb_LasciamiInPace.mp4",
      vid"rphjb_LasciamiInPaceStronza.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      "rimpinzati",
      "(g|c)io(g|c)+ola(d|t)a".r.tr(9),
      "pandori",
      "ciambelloni",
      "gli amari",
      "limoncell(o|i)".r.tr(10),
      "ingrassati",
      "andati al cesso",
    )(
      gif"rphjb_Rimpinzati.mp4",
      vid"rphjb_Pasqua.mp4",
    ),
    ReplyBundleMessage.textToMedia[F](
      "stare male",
      "melensa"
    )(
      gif"rphjb_MiFaStareMale.mp4",
      vid"rphjb_PeggioDelPeggio.mp4",
      vid"rphjb_MelensaStareMale.mp4",
      vid"rphjb_RelIllusions.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      "[a]+[t]{2,}[e]+[n]+[z]+[i]+[o]+[n]+[e]+[!]*[!e]$".r.tr(10)
    )(
      mp3"rphjb_Attenzione.mp3",
      vid"rphjb_Attenzione.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      "paradosso"
    )(
      gif"rphjb_ParadossoGif.mp4",
      vid"rphjb_Paradosso.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      "\\bsput[ao]\\b".r.tr(5)
    )(
      gif"rphjb_SputoGif.mp4",
      vid"rphjb_FeelingsSputo.mp4",
      vid"rphjb_BicchiereSputoLimitazioniUomoDonna.mp4",
      vid"rphjb_EsseriUmaniZozzeriaCarnePelleSputoSudoreSpermaNonContiamoNiente.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      "cobelini",
      "cobbolidi",
      "elfi",
      "\\bnani\\b".r.tr(4),
      "gobellini",
      "ossa dei morti"
    )(
      mp3"rphjb_FigureMitologiche.mp3",
      vid"rphjb_FigureMitologiche.mp4",
      vid"rphjb_FigureMitologiche2.mp4",
      vid"rphjb_FigureMitologiche3.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      "anche la merda",
      "senza culo"
    )(
      mp3"rphjb_Merda.mp3",
      vid"rphjb_AncheLaMerda.mp4",
      vid"rphjb_StorieSonoTanteConoscerePerParlareJovanottiAntonacciCarboniGrignaniAncheLaMerdaAvrebbeValore.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      "chiama la polizia"
    )(
      gif"rphjb_ChiamaLaPoliziaGif.mp4",
      vid"rphjb_ChiamaLaPolizia.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      "stori(a|e)".r.tr(6)
    )(
      mp3"rphjb_Storie.mp3",
      mp3"rphjb_Storie2.mp3",
      vid"rphjb_StoriaNonDetta.mp4",
      vid"rphjb_StorieSonoTanteVecchiaccia.mp4",
      vid"rphjb_StoriaVeraPienaBugie.mp4",
      vid"rphjb_StoriaAmicoGrasso.mp4",
      vid"rphjb_StoriaSignorGionz.mp4",
      mp3"rphjb_StoriaMula.mp3",
      vid"rphjb_CaniAlCimitero.mp4",
      vid"rphjb_IlCiano.mp4",
      vid"rphjb_CheStoDicendoDiVoltaIlCervelloPrincipiVeneziani.mp4",
      vid"rphjb_StoriaDellaMiaVita.mp4",
      vid"rphjb_StorieSonoTanteConoscerePerParlareJovanottiAntonacciCarboniGrignaniAncheLaMerdaAvrebbeValore.mp4",
      vid"rphjb_StoriaMarlinManson.mp4",
      vid"rphjb_StorieTanteTempoPassaOlioLeccarePiuSpazio.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      "au[ ]?de".r.tr(4),
      "\\btime\\b".r.tr(4),
      "uir[ ]?bi[ ]?taim".r.tr(9)
    )(
      mp3"rphjb_Audeuirbitaim.mp3",
      mp3"rphjb_Audeuirbitaim2.mp3",
      vid"rphjb_Audeuirbitaim.mp4",
    ),
    ReplyBundleMessage.textToMedia[F](
      "micetta",
      "la morte"
    )(
      gif"rphjb_Micetta.mp4",
      vid"rphjb_LaMorteMicetta.mp4",
      vid"rphjb_LaMorte.mp4",
      vid"rphjb_LaMorte2.mp4",
      vid"rphjb_InnoAllaMorte.mp4",
      vid"rphjb_UnicoMezzoUccidereMorteMateriaSpirito.mp4",
    ),
    ReplyBundleMessage.textToMedia[F](
      "\\bspalle\\b".r.tr(6),
      "\\bbraccia\\b".r.tr(7),
      "t(i|e) strozzo".r.tr(10)
    )(
      gif"rphjb_FaccioVedereSpalleBracciaGif.mp4",
      vid"rphjb_FaccioVedereSpalleBraccia.mp4",
      vid"rphjb_UccidereUnaPersona.mp4",
    ),
    ReplyBundleMessage.textToMedia[F](
      "non sapere",
      "aris(d|t)o(d|t)ele".r.tr(10),
      "socrate"
    )(
      gif"rphjb_SoDiNonSapereGif.mp4",
      vid"rphjb_SoDiNonSapere.mp4",
      vid"rphjb_SoDiNonSapere2.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      "non è roba per me"
    )(
      gif"rphjb_RobaPerMeGif.mp4",
      vid"rphjb_RobaPerMe.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      "io \\bn[o]{2,}\\b".r.tr(6)
    )(
      mp3"rphjb_IoNo.mp3",
      vid"rphjb_GesuCoglione.mp4",
      mp3"rphjb_GesuCoglione.mp3",
    ),
    ReplyBundleMessage.textToMedia[F](
      "bastone infernale",
      "un'arma"
    )(
      gif"rphjb_Bastone1.mp4",
      gif"rphjb_Bastone2.mp4",
      gif"rphjb_Bastone3.mp4",
      vid"rphjb_BastoneInfernale.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      "vi calpesto",
      "vermi",
      "strisciate per terra"
    )(
      gif"rphjb_ViCalpestoGif.mp4",
      mp3"rphjb_ViCalpesto.mp3",
      vid"rphjb_ViCalpesto.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      "andare avanti"
    )(
      gif"rphjb_AndareAvanti.mp4",
      mp3"rphjb_AndareAvanti.mp3",
      vid"rphjb_ComposizioneIdeaFrescaInnovazioneAndareAvantiStiamoTornandoIndetro.mp4",
      vid"rphjb_AndareAvantiStringereIDenti.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      "non ci credete?",
      "grande s(d|t)ronza(d|t)(e|a)".r.tr(16)
    )(
      gif"rphjb_NonCiCredete.mp4",
      mp3"rphjb_NonCiCredete.mp3",
    ),
    ReplyBundleMessage.textToMedia[F](
      "non me ne (frega|fotte)".r.tr(15),
      "chissenefrega",
      "non mi interessa",
      "me ne (frego|sbatto)".r.tr(21)
    )(
      gif"rphjb_NonMeNeFotte.mp4",
      gif"rphjb_NonMeNeFrega.mp4",
      vid"rphjb_NonMiFregaParloIo.mp4",
      mp3"rphjb_ENonMeNeFotteUnCazzo.mp3",
      mp3"rphjb_NonLeggoQuelloCheScrivete.mp3",
      mp3"rphjb_IncidentePonte.mp3",
      vid"rphjb_EscertoCritiche.mp4",
      vid"rphjb_Escerto.mp4",
      vid"rphjb_NoRabbiaRidereMeNeFrego.mp4",
      vid"rphjb_GiovencaVarzettaSposoChissenefrega.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      "ultimi"
    )(
      gif"rphjb_ViCalpestoGif.mp4",
      mp3"rphjb_ViCalpesto.mp3",
      vid"rphjb_ViCalpesto.mp4",
      gif"rphjb_Ultimi.mp4",
      mp3"rphjb_StateZittiZozziUltimi.mp3",
      vid"rphjb_RottoIlCazzoUltimi.mp4",
      vid"rphjb_BruttiSchifosiUltimiDegliUltimiNonSonoUltimo.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      "che (cazzo )?era quella roba".r.tr(19),
      "che (cazzo |cazzo di roba )?mi avete dato".r.tr(17),
      "lampi negli occhi",
      "gira(re|ra|rà|ndo)? la testa".r.tr(13),
      "insieme alla (c|g)o(c|g)a (c|g)ola".r.tr(22)
    )(
      mp3"rphjb_CheCazzoEraQuellaRoba.mp3",
      vid"rphjb_CheCazzoEraQuellaRoba.mp4",
      vid"rphjb_CheCazzoEraQuellaRoba2.mp4",
      vid"rphjb_CheCazzoEraQuellaRoba3.mp4",
      vid"rphjb_RockMachineIntro.mp4",
      mp3"rphjb_MaCheCazzoEraQuellaRobaDroghe.mp3"
    ),
    ReplyBundleMessage.textToMedia[F](
      "canzonette",
      "balera",
      "sagra dell'uva",
      "feste condominiali",
      "feste di piazza"
    )(
      mp3"rphjb_Canzonette.mp3",
      vid"rphjb_Canzonette.mp4",
      gif"rphjb_Canzonette.gif"
    ),
    ReplyBundleMessage.textToMedia[F](
      "un pollo"
    )(
      mp3"rphjb_Pollo.mp3",
      vid"rphjb_Pollo.mp4",
      vid"rphjb_Pollo2.mp4",
      gif"rphjb_PolloGif.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      "quello che dico io"
    )(
      gif"rphjb_QuelloCheDicoIo.mp4",
      mp3"rphjb_FannoQuelloCheDicoIo.mp3",
      vid"rphjb_FannoQuelloCheDicoIo.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      "zucchero"
    )(
      mp3"rphjb_ChitarraZuggherada.mp3",
      vid"rphjb_ChitarraZuccherada.mp4",
      mp3"rphjb_ChitarraZuccheroAlgheVino.mp3",
      mp3"rphjb_Zucchero.mp3",
      mp3"rphjb_ZuccheroVanigliaCremaCioccolataPandoro.mp3",
      vid"rphjb_AuguriPerPasqua.mp4",
      vid"rphjb_SuonatoAbbastanzaBeneManicoIntrisoZuccheroLiquidiSeminaliBirreAcqua.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      "\\bgood\\b".r.tr(4),
      "\\bshow\\b".r.tr(4),
      "\\bfriends\\b".r.tr(7)
    )(
      gif"rphjb_OkGoodShowFriends.mp4",
      gif"rphjb_OkGoodShowFriends2.mp4",
      mp3"rphjb_LetSGoodStateBene.mp3",
      vid"rphjb_WelaMyFriends.mp4",
      vid"rphjb_LetsGoodMyFriends.mp4",
      vid"rphjb_NonPoteteGiudicarUrloThatsGood.mp4",
      vid"rphjb_LetsGoodMyFriendsPassport.mp4",
      vid"rphjb_LetsGoodMyFriendsForTheShowThatNeverEnds.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      "vattene (a f|a[f]*)?fanculo".r.tr(16)
    )(
      gif"rphjb_MaVatteneAffanculo.mp4",
      mp3"rphjb_MaVatteneAffanculo.mp3",
      vid"rphjb_PortlandVancuverFanculo.mp4",
      gif"rphjb_FanculoPerCortesia.mp4",
      vid"rphjb_DecidoIoMareCazzatePerCortesia.mp4",
      vid"rphjb_CambiaCanaleBruttoFrocio.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      "feelings"
    )(
      gif"rphjb_Feelings.gif",
      gif"rphjb_Feelings2.mp4",
      mp3"rphjb_Feelings.mp3",
      vid"rphjb_FeelingsIncazzarmiAndiamociSentireOriginale.mp4",
      vid"rphjb_FeelingsSguardoPreghiera.mp4",
      vid"rphjb_FeelingsSputo.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      "me ne vado"
    )(
      mp3"rphjb_MeNeVado.mp3",
      vid"rphjb_EBastaETuHaiRottoIlCazzo.mp4",
      gif"rphjb_MiRompiErCazzo.mp4",
      gif"rphjb_MeNeVado.mp4",
      vid"rphjb_MeNeVado2.mp4",
      mp3"rphjb_MeNeVado2.mp3",
    ),
    ReplyBundleMessage.textToMedia[F](
      "mignotta",
      "puttana",
      "troia"
    )(
      mp3"rphjb_Mignotta.mp3",
      gif"rphjb_Mignotta.mp4",
      mp3"rphjb_VialeZara.mp3",
      vid"rphjb_StronzoFiglioMignotta.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      "ti devi spaventare"
    )(
      mp3"rphjb_TiDeviSpaventare.mp3",
      gif"rphjb_TiDeviSpaventareGif.mp4",
      vid"rphjb_TiDeviSpaventare.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      "il martell"
    )(
      mp3"rphjb_MaCheCazzoStoDicendo.mp3",
      vid"rphjb_MaCheCazzoStoDicendo.mp4",
      gif"rphjb_MaCheCazzoStoDicendoGif.mp4",
      gif"rphjb_IlMartel.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      "questa volta no"
    )(
      mp3"rphjb_QuestaVoltaNo.mp3",
      gif"rphjb_QuestaVoltaNo.mp4",
      vid"rphjb_CervelloPensante.mp4",
      vid"rphjb_FiguraDiMerdaQuestaVoltaNo.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      "una vergogna"
    )(
      mp3"rphjb_Vergogna.mp3",
      vid"rphjb_Vergogna.mp4",
      gif"rphjb_VergognaGif.mp4",
      gif"rphjb_Vergogna2.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      "mi devo trasformare",
      "cristo canaro"
    )(
      mp3"rphjb_Trasformista.mp3",
      gif"rphjb_Trasformista.mp4",
      vid"rphjb_CristoCanaro.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      "ma[ ]?s(c|g)us[a]?".r.tr(5)
    )(
      mp3"rphjb_MaSgus.mp3",
      gif"rphjb_MaSgusGif.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      "grazie gianni",
    )(
      mp3"rphjb_Grazie.mp3",
      gif"rphjb_GrazieGif.mp4",
      vid"rphjb_Grazie.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      "cia[o]{3,}".r.tr(6)
    )(
      mp3"rphjb_Grazie.mp3",
      gif"rphjb_GrazieGif.mp4",
      vid"rphjb_Grazie.mp4",
      mp3"rphjb_ViSaluto.mp3"
    ),
    ReplyBundleMessage.textToMedia[F](
      "stare attenti",
      "per strada"
    )(
      mp3"rphjb_IncontratePerStrada.mp3",
      gif"rphjb_IncontratePerStrada.mp4",
      vid"rphjb_VergognatiMancusiPaoloBiancoTastieristaAttentiPerStradaAndreaCarpi.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      "tastierista"
    )(
      mp3"rphjb_Tastierista.mp3",
      vid"rphjb_VergognatiMancusiPaoloBiancoTastieristaAttentiPerStradaAndreaCarpi.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      "lavora tu vecchiaccia",
      "hai la pelle dura",
      "io sono creatura"
    )(
      mp3"rphjb_LavoraTu.mp3",
      vid"rphjb_LavoraTu.mp4",
      vid"rphjb_LavoraTu2.mp4",
      vid"rphjb_LavoraTu3.mp4",
      vid"rphjb_LavoraTu4.mp4",
      gif"rphjb_LavoraTuGif.mp4",
      vid"rphjb_StorieSonoTanteVecchiaccia.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      "infern[a]+l[i]+[!]*".r.tr(9)
    )(
      mp3"rphjb_Infernali.mp3",
      gif"rphjb_Infernali.mp4",
      vid"rphjb_StacchiDiBatteriaMikeTerranaInfernali.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      "per il culo"
    )(
      mp3"rphjb_PigliandoPerIlCulo.mp3",
      gif"rphjb_PigliandoPerIlCulo.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      "sorriso",
      "(😂|🤣){4,}".r.tr(4),
      "(😄|😀|😃){4,}".r.tr(4),
      "(ah|ha){7,}".r.tr(14)
    )(
      mp3"rphjb_Risata.mp3",
      vid"rphjb_Risata.mp4",
      vid"rphjb_Risata2.mp4",
      vid"rphjb_Risata3.mp4",
      vid"rphjb_RisataInfernale.mp4",
      gif"rphjb_Risata.gif",
      gif"rphjb_RisataGif.mp4",
      vid"rphjb_OrmaiRisata.mp4",
      gif"rphjb_Sorriso2.gif",
      gif"rphjb_Sorriso.mp4",
      gif"rphjb_SorrisoSognante.mp4",
      mp3"rphjb_Risata2.mp3",
      vid"rphjb_SepolturaRisata.mp4",
      gif"rphjb_RisataTrattenuta.mp4",
      vid"rphjb_CheGruppoMiRicordaRisata.mp4",
      vid"rphjb_MomentiGloria.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      "ammazza che sei",
      "quasi un frocio"
    )(
      mp3"rphjb_Frocio.mp3",
      gif"rphjb_FrocioGif.mp4",
      vid"rphjb_EduFalasciQuasiFrocio.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      "(fammi|chiedere)? (una|questa)? cortesia".r.tr(18)
    )(
      mp3"rphjb_FammiQuestaCortesia.mp3",
      gif"rphjb_FammiQuestaCortesia.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      "non mi sta bene"
    )(
      mp3"rphjb_NonMiStaBene.mp3",
      mp3"rphjb_NonMiStaBene2.mp3",
      gif"rphjb_NonMiStaBeneGif.mp4",
      gif"rphjb_NonMiStaBene2.mp4",
      vid"rphjb_NonMiStaBeneDelusioneStorica.mp4",
      vid"rphjb_NonMiStaBene.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      "le labbra",
    )(
      mp3"rphjb_Labbra.mp3",
      gif"rphjb_Labbra.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      "la vita è il nemico"
    )(
      mp3"rphjb_VitaNemico.mp3",
      gif"rphjb_VitaNemicoGif.mp4",
      vid"rphjb_VitaNemico.mp4",
      vid"rphjb_VitaNemico2.mp4",
      vid"rphjb_VitaNemico3.mp4",
      vid"rphjb_VitaNemicoCervello.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      "permettere"
    )(
      mp3"rphjb_Permettere.mp3",
      gif"rphjb_Permettere.mp4",
      vid"rphjb_Labrie.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      "le note"
    )(
      mp3"rphjb_Note.mp3",
      gif"rphjb_Note.mp4",
      vid"rphjb_TraTutteLeNote.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      "te[r]+[i]+[b]+[i]+l[e]+".r.tr(8)
    )(
      mp3"rphjb_Terribile.mp3",
      vid"rphjb_Terribile.mp4",
      gif"rphjb_TerribileGif.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      "napoli"
    )(
      mp3"rphjb_VivaNapoli.mp3",
      gif"rphjb_VivaNapoli.mp4",
      vid"rphjb_CuginiCampagnaGiardinoSempliciHomoSapiensLiscioRumbaChaChaChaCanzoneNapoletanaOsanna.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      "ciao a tutti",
      "belle gioie"
    )(
      gif"rphjb_CiaoComeStateGif.mp4",
      vid"rphjb_CiaoComeState.mp4",
      mp3"rphjb_CiaoComeState.mp3"
    ),
    ReplyBundleMessage.textToMedia[F](
      "\\bbasta(a|!){2,}".r.tr(7)
    )(
      mp3"rphjb_Basta.mp3",
      gif"rphjb_Basta.mp4",
      gif"rphjb_Basta2.mp4",
      mp3"rphjb_Basta2.mp3",
      vid"rphjb_EBastaETuHaiRottoIlCazzo.mp4",
      gif"rphjb_BastaGif.mp4",
      gif"rphjb_Basta2Gif.mp4",
      gif"rphjb_Basta3.mp4",
      gif"rphjb_Basta4.mp4",
      vid"rphjb_BastaRottoIlCazzo.mp4",
      gif"rphjb_BastaSedia.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      "assolo",
      "(g|c)hi(t|d)arra".r.tr(8),
      "(as)?solo di basso".r.tr(13)
    )(
      mp3"rphjb_Assolo.mp3",
      vid"rphjb_Assolo.mp4",
      vid"rphjb_Assolo2.mp4",
      vid"rphjb_AssoloBeat.mp4",
      vid"rphjb_AssoloSubdolo.mp4",
      vid"rphjb_Basso.mp4",
      gif"rphjb_Chitarra1.mp4",
      gif"rphjb_Chitarra2.mp4",
      gif"rphjb_Chitarra3.mp4",
      vid"rphjb_ChitarraPlettroVicoletto.mp4",
      vid"rphjb_ChitarraVicolettoPlettro2.mp4",
      mp3"rphjb_ChitarraZuggherada.mp3",
      vid"rphjb_ChitarraZuccherada.mp4",
      mp3"rphjb_ChitarraZuccheroAlgheVino.mp3",
      vid"rphjb_AssoloBasso.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      "\\b(g|c)a(b|p)i(d|t)o\\b".r.tr(6),
      "\\bcapissi\\b".r.tr(7),
    )(
      mp3"rphjb_HoCapito.mp3",
      mp3"rphjb_AveteCapito.mp3",
      vid"rphjb_AveteCapito.mp4",
      mp3"rphjb_Capito.mp3",
      mp3"rphjb_NonHannoCapitoUnCazzo.mp3",
      vid"rphjb_NonAveteCapitoUnCazzo.mp4",
      gif"rphjb_AveteCapitoComeSempre.mp4",
      gif"rphjb_NonAveteCapitoUnCazzoGif.mp4",
      gif"rphjb_VoiNonAveteCapitoUnCazzo.mp4",
      gif"rphjb_IlSensoCapito.mp4",
      gif"rphjb_CapitoDoveStiamo.mp4",
      gif"rphjb_NonHoCapito.mp4",
      gif"rphjb_AveteCapitoEh.mp4",
      gif"rphjb_ComeAlSolitoNonAveteCapito.mp4",
      mp3"rphjb_CapitoDoveStiamo.mp3",
      mp3"rphjb_CapisciRidotti.mp3",
      mp3"rphjb_CapitoCheMagagna.mp3",
      mp3"rphjb_DavantiGenteNonHaCapisceUnCazzo.mp3",
      vid"rphjb_AbbiamoCapito.mp4",
      vid"rphjb_AveteCapitoNo.mp4",
      vid"rphjb_StiamoNellaFollia.mp4",
      vid"rphjb_HaiCapitoAveteRapitoONonAveteCapitoUnCazzo.mp4",
      vid"rphjb_AveteCapito2.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      "esperiment",
      "1(,)? 2(,)? 3".r.tr(5),
      "uno(,)? due(,)? tre".r.tr(11)
    )(
      mp3"rphjb_Esperimento.mp3",
      vid"rphjb_Esperimento.mp4",
      vid"rphjb_Esperimento2.mp4",
      gif"rphjb_EsperimentoGif.mp4",
      gif"rphjb_Esperimento2Gif.mp4",
      gif"rphjb_Esperimento3.mp4",
      vid"rphjb_DiciottoAnni.mp4",
      vid"rphjb_DiciottoAnni2.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      "schifosi"
    )(
      gif"rphjb_ViCalpestoGif.mp4",
      mp3"rphjb_ViCalpesto.mp3",
      vid"rphjb_ViCalpesto.mp4",
      mp3"rphjb_Schifosi.mp3",
      vid"rphjb_Schifosi.mp4",
      mp3"rphjb_Schifosi2.mp3",
      mp3"rphjb_Schifosi3.mp3",
      mp3"rphjb_Schifosi4.mp3",
      vid"rphjb_Schifosi4.mp4",
      gif"rphjb_Schifosi3.mp4",
      vid"rphjb_SchifosoUltimi.mp4",
      mp3"rphjb_StateZittiZozziUltimi.mp3",
      gif"rphjb_SchifosiGif.mp4",
      gif"rphjb_Schifosi2.mp4",
      vid"rphjb_Vigile.mp4",
      vid"rphjb_ConQuestaTecnica.mp4",
      mp3"rphjb_ConQuestaTecnica.mp3",
      vid"rphjb_BruttiSchifosiUltimiDegliUltimiNonSonoUltimo.mp4",
      vid"rphjb_ChitarreVergognateviSchifosiGiornaliMerda.mp4",
      vid"rphjb_GenteMalvagiaDistruggereSparlaGiornalistiSchifosiCarpiMingoliAntonellaDario.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      "schifos(o)+(!)*".r.tr(8)
    )(
      gif"rphjb_Schifoso.mp4",
      vid"rphjb_Vigile.mp4",
      vid"rphjb_AmoreSuonareFreddoBuddistiSchifoso.mp4",
      gif"rphjb_BruttoSquallidoSchifosoGif.mp4",
      vid"rphjb_BruttoSquallidoSchifosoUltimoEsseriUmani.mp4",
      vid"rphjb_DanzaMacabra.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      "mortacci vostri"
    )(
      gif"rphjb_MortacciVostriGif.mp4",
      mp3"rphjb_StateZittiZozziUltimi.mp3",
      vid"rphjb_ConQuestaTecnica.mp4",
      mp3"rphjb_ConQuestaTecnica.mp3",
      vid"rphjb_MortacciVostri.mp4",
      vid"rphjb_CheCazzoEraQuellaRoba2.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      "non vedo"
    )(
      mp3"rphjb_Stanco.mp3",
      vid"rphjb_Stanco.mp4",
      mp3"rphjb_PannaOcchialiSpalla.mp3",
    ),
    ReplyBundleMessage.textToMedia[F](
      "panna"
    )(
      mp3"rphjb_Problema.mp3",
      mp3"rphjb_PannaOcchialiSpalla.mp3"
    ),
    ReplyBundleMessage.textToMedia[F](
      "\\bapplauso\\b".r.tr(8)
    )(
      gif"rphjb_Applauso.mp4",
      mp3"rphjb_Applauso.mp3",
      mp3"rphjb_Applauso2.mp3",
      vid"rphjb_ApplausoPiuForte.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      "venite qua"
    )(
      gif"rphjb_VeniteQuaGif.mp4",
      mp3"rphjb_VeniteQua.mp3",
      vid"rphjb_VeniteQua.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      "\\bpaga(re)?\\b".r.tr(4),
      "soldi",
      "bollette",
      "tasse",
      "bolletta",
      "tassa"
    )(
      gif"rphjb_ChiCacciaISoldi.mp4",
      mp3"rphjb_ChiCacciaISoldi.mp3",
      mp3"rphjb_SoldiButtatiDiscotecaLaziale.mp3",
      vid"rphjb_SoldiButtatiDiscotecaLaziale.mp4",
      vid"rphjb_BigMoney.mp4",
      vid"rphjb_InvestitoreGoverno.mp4",
      vid"rphjb_ButtareSoldiFinestra.mp4",
      vid"rphjb_CoiSoldiMiei.mp4",
      vid"rphjb_StorieTanteTempoPassaOlioLeccarePiuSpazio.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      "[od]?dio mio[,]? no".r.tr(10)
    )(
      gif"rphjb_OddioMioNoGif.mp4",
      mp3"rphjb_OddioMioNo.mp3",
      mp3"rphjb_OddioMioNo2.mp3",
      gif"rphjb_OddioMioNo.mp4",
      gif"rphjb_OddioMioNo2.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      "(sono |so )?a[r]{1,2}iva(d|t)o".r.tr(12),
      "(eccomi|ciao).*\\bpiacere\\b".r.tr(13)
    )(
      gif"rphjb_ArivatoGif.mp4",
      mp3"rphjb_Arivato.mp3",
      gif"rphjb_Arivato.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      "delu(s|d)".r.tr(5)
    )(
      gif"rphjb_Deluso.mp4",
      mp3"rphjb_Deluso.mp3",
      vid"rphjb_DeludendoQuasiTutto.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      "fate come vi pare",
      "sti (g|c)azzi".r.tr(9)
    )(
      gif"rphjb_ComeViPare.mp4",
      mp3"rphjb_ComeViPare.mp3"
    ),
    ReplyBundleMessage.textToMedia[F](
      "divento una bestia",
      "incazzo"
    )(
      mp3"rphjb_DiventoBestia.mp3",
      mp3"rphjb_Incazzo.mp3",
      mp3"rphjb_Incazzo2.mp3",
      mp3"rphjb_PrimoSbaglio.mp3",
      vid"rphjb_PrimoSbaglio.mp4",
    ),
    ReplyBundleMessage.textToMedia[F](
      "dove stiamo",
      "stiamo nella follia"
    )(
      mp3"rphjb_CapitoDoveStiamo.mp3",
      vid"rphjb_StiamoNellaFollia.mp4",
      gif"rphjb_CapitoDoveStiamo.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      "non sai molto"
    )(
      gif"rphjb_NonSaiMolto.mp4",
      vid"rphjb_RadioRockErrori.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      "errori"
    )(
      gif"rphjb_MaiErroriGif.mp4",
      vid"rphjb_MaiErrori.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      "\\bpasqua\\b".r.tr(6)
    )(
      mp3"rphjb_AuguriPasqua.mp3",
      vid"rphjb_AuguriPerPasqua.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      "vaniglia",
      "pandoro",
      "crema alla (g|c)io(g|c)+ola(d|t)a".r.tr(20),
    )(
      mp3"rphjb_ZuccheroVanigliaCremaCioccolataPandoro.mp3",
      vid"rphjb_AuguriPerPasqua.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      "c'hai timore",
      "c'hai paura",
      "diri[g]+en(d|t)i".r.tr(9),
    )(
      gif"rphjb_Dirigenti.mp4",
      vid"rphjb_AncoraNoDirigenti.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      "guerra"
    )(
      gif"rphjb_GuerraTotaleGif.mp4",
      vid"rphjb_GuerraTotale.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      "non voglio nessuno",
      "mentre lavoro"
    )(
      gif"rphjb_NonVoglioNessunoGif.mp4",
      vid"rphjb_NonVoglioNessuno.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      "peggio del peggio"
    )(
      gif"rphjb_PeggioDelPeggioGif.mp4",
      vid"rphjb_PeggioDelPeggio.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      "bebop",
      "be bop",
      "aluba",
      "my baby"
    )(
      gif"rphjb_BebopGif.mp4",
      gif"rphjb_BluesEbbeBambinoRockNRollBeBopALulaStormyMondayButTuesdayIsJustAsBad.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      "(18|diciott['o]?) anni".r.tr(7)
    )(
      vid"rphjb_DiciottoAnni.mp4",
      vid"rphjb_DiciottoAnni2.mp4",
      gif"rphjb_DiciottoAnniGif.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      "(cinque|5) dita".r.tr(6),
      "pugno"
    )(
      vid"rphjb_CinqueDita.mp4",
      mp3"rphjb_CinqueDita.mp3",
      vid"rphjb_CinqueDita2.mp4",
      vid"rphjb_RollingStonesJimiHendrixPoetiMaledettiFemministeControculturaRivoluzioniRibelioni5DitaCazzottoInFacciaGiustiziere.mp4",
      vid"rphjb_SonoAttentoVaTuttoBeneAttagliatoTempo5DitaPugno.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      "\\bratti\\b".r.tr(5),
      "topi"
    )(
      vid"rphjb_DubbioScantinatiGiocoRattoGatto.mp4",
      mp3"rphjb_ListaMaleCollo.mp3"
    ),
    ReplyBundleMessage.textToMedia[F](
      "poveri cretini",
      "poveri ignoranti"
    )(
      mp3"rphjb_PoveriCretini.mp3",
      vid"rphjb_PoveriIgnorantiLatteDrogaSoloGreggeSputo.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      "solo uno parló",
      "(c|g)ri(d|t)i(g|c)a(d|t)o".r.tr(9)
    )(
      gif"rphjb_FuCriticatoGif.mp4",
      vid"rphjb_FuCriticato.mp4",
    ),
    ReplyBundleMessage.textToMedia[F](
      "venerd[iì]".r.tr(7)
    )(
      mp3"rphjb_Venerdi.mp3",
      vid"rphjb_Venerdi.mp4",
      vid"rphjb_IlVenerdi.mp4",
      vid"rphjb_TempoAlTempo.mp4",
      vid"rphjb_VenerdiAppuntamentoFissoFica.mp4",
      vid"rphjb_UltimoListaUmaniVenerdì22.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      "discoteca laziale"
    )(
      gif"rphjb_DiscotecaLazialeGif.mp4",
      vid"rphjb_DiscotecaLaziale.mp4",
      mp3"rphjb_DiscotecaLaziale.mp3",
      mp3"rphjb_SoldiButtatiDiscotecaLaziale.mp3",
      vid"rphjb_SoldiButtatiDiscotecaLaziale.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      "calcoli",
      "matematica",
      "geometrici",
      "matematici",
      "analitici",
    )(
      gif"rphjb_MiPareLogico.mp4",
      mp3"rphjb_MiPareLogico.mp3",
      mp3"rphjb_MatematiciAnaliticiDiNoia.mp3",
      vid"rphjb_MiPareLogicoMatematiciAnaliticiDiNoia.mp4",
      vid"rphjb_CoseCheNonSopportoCalcoliSbagliati.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      "\\blo[g]+i(c|g)o\\b".r.tr(6)
    )(
      gif"rphjb_MiPareLogico.mp4",
      mp3"rphjb_MiPareLogico.mp3",
      gif"rphjb_SembraLogico.mp4",
      vid"rphjb_MiPareLogicoMatematiciAnaliticiDiNoia.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      "ti dovresti vergognare"
    )(
      gif"rphjb_TiDovrestiVergognare.mp4",
      mp3"rphjb_TiDovrestiVergognare.mp3"
    ),
    ReplyBundleMessage.textToMedia[F](
      "(non|mica) so(no)? (un |n )?co(gl|j)ione".r.tr(13),
      "sarete co(gl|j)ioni voi".r.tr(17)
    )(
      gif"rphjb_SareteCoglioniVoiGif.mp4",
      mp3"rphjb_SareteCoglioniVoi.mp3",
      vid"rphjb_SareteCoglioniVoi.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      "non li sopporto",
      "che si deve f(à|are)".r.tr(14),
      "bisogna pure lavorà"
    )(
      gif"rphjb_NonLiSopportoGif.mp4",
      mp3"rphjb_NonLiSopporto.mp3",
      vid"rphjb_NonLiSopporto.mp4",
    ),
    ReplyBundleMessage.textToMedia[F](
      "simposio"
    )(
      vid"rphjb_PellegrinaggioSimposioMetallo.mp4",
      mp3"rphjb_InnoSimposio.mp3",
      vid"rphjb_PoesiaMaria.mp4",
      vid"rphjb_IlSimposioDelMetalloCristoEdicolantePerFerireTramontoCristo.mp4",
      vid"rphjb_IoParloDicoLaVeritaContrattiFantomaticiVieniQuiFaiVedereFacciaCovoDelMetalloSimposio.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      "chi è cristo",
      "si è fatto fregare",
      "bacio di un frocio",
    )(
      mp3"rphjb_ChiECristo.mp3",
      vid"rphjb_GiudaFrocio.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      "danza macabra",
    )(
      gif"rphjb_DanzaMacabraGif.mp4",
      vid"rphjb_DanzaMacabra.mp4",
      mp3"rphjb_DanzaMacabra.mp3",
    ),
    ReplyBundleMessage.textToMedia[F](
      "steve vai",
    )(
      vid"rphjb_SteveVaiRiciclando.mp4",
      vid"rphjb_AmoreSuonareFreddoBuddistiSchifoso.mp4",
      vid"rphjb_GambeInesistentiDueOssa.mp4",
      mp3"rphjb_DueOssa.mp3",
      gif"rphjb_Note.mp4",
      vid"rphjb_TraTutteLeNote.mp4",
      vid"rphjb_Paradosso.mp4",
      vid"rphjb_RelIllusions.mp4",
      gif"rphjb_TiDeviSpaventareGif.mp4",
      mp3"rphjb_TiDeviSpaventare.mp3",
      vid"rphjb_FeelingsIncazzarmiAndiamociSentireOriginale.mp4",
      vid"rphjb_SteveVaiRamazzotti.mp4",
      vid"rphjb_FiguracceDiscoSteveVai.mp4",
      vid"rphjb_SembraCadavereFassinoRitrattoSalute.mp4",
      vid"rphjb_FesteACasaNicolaArigliano.mp4",
      vid"rphjb_FeelingsSguardoPreghiera.mp4",
      vid"rphjb_FeelingsSputo.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      "(divento|diventare|sono) (matto|pazzo)".r.tr(10)
    )(
      gif"rphjb_StoDiventandoPazzo.mp4",
      vid"rphjb_CompletamentePazzo.mp4",
      vid"rphjb_CompletamentePazzo2.mp4",
    ),
    ReplyBundleMessage.textToMedia[F](
      "vo[l]+[o]*[u]+[ou]*me".r.tr(6)
    )(
      mp3"rphjb_MenoVolume.mp3",
      vid"rphjb_VolumeTelevisori.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      "generi musicali",
      "solo il me(t|d)al".r.tr(13)
    )(
      gif"rphjb_GeneriMusicali.mp4",
      vid"rphjb_GeneriMusicali2.mp4",
    ),
    ReplyBundleMessage.textToMedia[F](
      "sorca",
      "patonza",
      "lecciso",
      "\\bfi[cg]a\\b".r.tr(4)
    )(
      gif"rphjb_SorcaLecciso.mp4",
      vid"rphjb_SorcaLecciso2.mp4",
      vid"rphjb_FigaLarga.mp4",
      mp3"rphjb_FragolinaFichina.mp3",
      vid"rphjb_Sorca.mp4",
      vid"rphjb_LeccisoOffrire.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      "schifose",
      "ultime"
    )(
      gif"rphjb_SchifoseUltime.mp4",
      vid"rphjb_SchifoseUltime.mp4",
      vid"rphjb_ImparaASputareMignottaSchifose.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      "e parl[a]+\\b".r.tr(7)
    )(
      gif"rphjb_Parla.mp4",
      vid"rphjb_Parla2.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      "cosa è successo",
      "\\bcosa[?]{1,}\\b".r.tr(5)
    )(
      gif"rphjb_CosaSuccesso.mp4",
      vid"rphjb_Cosa.mp4",
      vid"rphjb_CosaCosaSuccessoMeNeVadoFacendoSoffrire.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      "negozio",
      "pantaloni",
      "shopping"
    )(
      mp3"rphjb_Pantaloni.mp3",
      vid"rphjb_Pantaloni.mp4",
    ),
    ReplyBundleMessage.textToMedia[F](
      "sono finito",
      "ultimo stadio",
      "stanco"
    )(
      mp3"rphjb_Stanco.mp3",
      vid"rphjb_Stanco.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      "ratzinger",
      "(il|er) vaticano".r.tr(11)
    )(
      vid"rphjb_AndateDaRatzinger.mp4",
      gif"rphjb_AndateDaRatzinger2.mp4",
      mp3"rphjb_AndateDaRatzinger.mp3",
    ),
    ReplyBundleMessage.textToMedia[F](
      "non è possibile"
    )(
      gif"rphjb_NonPossibile.mp4",
      vid"rphjb_NonPossibile2.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      "moglie"
    )(
      mp3"rphjb_Cameriera.mp3",
      vid"rphjb_Cameriera.mp4",
      vid"rphjb_Attenzione.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      "cameriera",
      "si sposa",
      "matrimonio"
    )(
      mp3"rphjb_Cameriera.mp3",
      vid"rphjb_Cameriera.mp4",
    ),
    ReplyBundleMessage.textToMedia[F](
      "cos(a)? hai trovato?".r.tr(16)
    )(
      gif"rphjb_CosHaiTrovato.mp4",
      vid"rphjb_NonPossibile2.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      "accetto (le|qualsiasi) critich[ea]".r.tr(17),
    )(
      gif"rphjb_Escerto.mp4",
      vid"rphjb_CriticaNoCazzate.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      "pronto dimmi"
    )(
      gif"rphjb_ProntoDimmi.mp4",
      gif"rphjb_ProntoDimmi2.mp4",
    ),
    ReplyBundleMessage.textToMedia[F](
      "slap"
    )(
      gif"rphjb_Bassista.gif",
      gif"rphjb_Basso.mp4",
    ),
    ReplyBundleMessage.textToMedia[F](
      "bassista",
    )(
      gif"rphjb_Bassista.gif",
      gif"rphjb_Basso.mp4",
      vid"rphjb_GruppiNuoviSpaccanoCuloAbbellimentiRivoltiMordentiContrappunti.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      "è vero[!?]+".r.tr(6)
    )(
      gif"rphjb_Vero.mp4",
      vid"rphjb_EraVero.mp4",
      mp3"rphjb_SuonatoAbbastanzaBeneEVero.mp3"
    ),
    ReplyBundleMessage.textToMedia[F](
      "perchè (cazzo)? mi hai fatto ven[i|ì](re)?".r.tr(24)
    )(
      mp3"rphjb_PercheCazzoMiHaiFattoVeni.mp3",
      vid"rphjb_PercheCazzoMiHaiFattoVeni.mp4",
    ),
    ReplyBundleMessage.textToMedia[F](
      "e[sc]+erto".r.tr(6)
    )(
      gif"rphjb_Escerto.mp4",
      vid"rphjb_EscertoCritiche.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      "decido io",
    )(
      gif"rphjb_DecidoIo.mp4",
      vid"rphjb_DecidoIoMareCazzatePerCortesia.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      "mi piaccio",
      "impazzire",
    )(
      gif"rphjb_MiPiaccio.mp4",
      vid"rphjb_MiPiaccio2.mp4",
    ),
    ReplyBundleMessage.textToMedia[F](
      "giudica"
    )(
      gif"rphjb_Giudicate.mp4",
      gif"rphjb_ComeFaiAGiudicareGif.mp4",
      gif"rphjb_ComeFaiAGiudicare.mp4",
      vid"rphjb_NonPoteteGiudicarUrloThatsGood.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      "fregare come un co(gl|j)ione".r.tr(22),
      "ges[uùù]".r.tr(4)
    )(
      vid"rphjb_GesuCoglione.mp4",
      mp3"rphjb_GesuCoglione.mp3"
    ),
    ReplyBundleMessage.textToMedia[F](
      "non sono uno del branco",
      "agende",
      "figli dei figli",
      "quali fiori",
      "diluite le vostre droghe",
      "gerarchie infernali"
    )(
      vid"rphjb_GerarchieInfernali.mp4",
      mp3"rphjb_GerarchieInfernali.mp3",
      vid"rphjb_GerarchieInfernali2.mp4",
      vid"rphjb_GerarchieInfernali3.mp4",
      vid"rphjb_OcchiVistoLaDifficileGuardareTrasmissioneLetaleTiCambiaGerarchieInfernali.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      "non sono uno da sangue",
      "aghi di culto",
      "bucati[,]? ma da quale chiodo".r.tr(25),
    )(
      vid"rphjb_GerarchieInfernali.mp4",
      mp3"rphjb_GerarchieInfernali.mp3",
      vid"rphjb_GerarchieInfernali2.mp4",
      vid"rphjb_GerarchieInfernali3.mp4",
    ),
    ReplyBundleMessage.textToMedia[F](
      "con questa tecnica"
    )(
      vid"rphjb_ConQuestaTecnica.mp4",
      mp3"rphjb_ConQuestaTecnica.mp3"
    ),
    ReplyBundleMessage.textToMedia[F](
      "platinette",
      "due persone in una",
      "quando scopo me la levo",
      "mi levo tutto",
    )(
      vid"rphjb_Platinette.mp4",
      mp3"rphjb_Platinette.mp3"
    ),
    ReplyBundleMessage.textToMedia[F](
      "il mio sbadiglio",
      "donna solo per un taglio",
      "labbro superiore"
    )(
      vid"rphjb_DonnaTaglioSbadiglio.mp4",
      mp3"rphjb_DonnaTaglioSbadiglio.mp3"
    ),
    ReplyBundleMessage.textToMedia[F](
      "\\bvino\\b".r.tr(4),
    )(
      mp3"rphjb_ChitarraZuccheroAlgheVino.mp3",
      vid"rphjb_Rimpinzati.mp4",
      vid"rphjb_Pasqua.mp4",
      gif"rphjb_LimoncelliVino.gif"
    ),
    ReplyBundleMessage.textToMedia[F](
      "chi tocca (\\w)[,]? muore".r.tr(16),
      "ciao (2001|duemilauno)".r.tr(9)
    )(
      vid"rphjb_Ciao2001.mp4",
      vid"rphjb_Ciao2001_2.mp4",
      gif"rphjb_Ciao2001Gif.mp4",
    ),
    ReplyBundleMessage.textToMedia[F](
      "alle (22|ventidue)".r.tr(7)
    )(
      mp3"rphjb_Alle22.mp3",
      vid"rphjb_VenerdiAppuntamentoFissoFica.mp4",
      vid"rphjb_Alle22MercolediTelevita.mp4",
      vid"rphjb_UltimoListaUmaniVenerdì22.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      "appuntamento"
    )(
      mp3"rphjb_Appuntamento.mp3",
      vid"rphjb_VenerdiAppuntamentoFissoFica.mp4",
      vid"rphjb_RicordateviAppuntamento.mp4",
    ),
    ReplyBundleMessage.textToMedia[F](
      "succh",
      "olio di croce"
    )(
      gif"rphjb_OlioDiCroce.mp4",
      mp3"rphjb_OlioDiCroce.mp3",
      mp3"rphjb_PoesiaNatalizia.mp3"
    ),
    ReplyBundleMessage.textToMedia[F](
      "scu[-]?sa[h]? scu[-]?sa[h]?".r.tr(11)
    )(
      mp3"rphjb_Scusa.mp3",
      vid"rphjb_ScusaScusa.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      "dare fastidio"
    )(
      gif"rphjb_DareFastidio.mp4",
      vid"rphjb_Regressive.mp4",
    ),
    ReplyBundleMessage.textToMedia[F](
      "regressive"
    )(
      gif"rphjb_RegressiveGif.mp4",
      vid"rphjb_Regressive.mp4",
    ),
    ReplyBundleMessage.textToMedia[F](
      "rotto il cazzo"
    )(
      vid"rphjb_EBastaETuHaiRottoIlCazzo.mp4",
      vid"rphjb_BastaRottoIlCazzo.mp4",
      vid"rphjb_RottoIlCazzoUltimi.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      "\\bmula\\b".r.tr(4),
      "storia della mula"
    )(
      vid"rphjb_Mula.mp4",
      mp3"rphjb_StoriaMula.mp3",
      vid"rphjb_FregataFregatura.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      "primo sbaglio"
    )(
      mp3"rphjb_PrimoSbaglio.mp3",
      vid"rphjb_PrimoSbaglio.mp4",
    ),
    ReplyBundleMessage.textToMedia[F](
      "poesia"
    )(
      vid"rphjb_PoesiaMadre.mp4",
      vid"rphjb_PoesiaRock.mp4",
      vid"rphjb_Blues.mp4",
      vid"rphjb_PoesiaMaria.mp4",
      vid"rphjb_PoesiaArtistiImpiegati.mp4",
      vid"rphjb_CanzonettePoesieAuschwitzCervello.mp4",
      vid"rphjb_PoesiaDirittoPaura.mp4",
      mp3"rphjb_PoesiaNatalizia.mp3"
    ),
    ReplyBundleMessage.textToMedia[F](
      "auguri di natale",
      "buon natale",
      "merry christmas",
    )(
      vid"rphjb_AuguriDiNatale.mp4",
      mp3"rphjb_RockChristmasHappyNewYear.mp3",
      vid"rphjb_AuguriDiNataleCapodannoFeste.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      "giuda"
    )(
      mp3"rphjb_ChiECristo.mp3",
      vid"rphjb_GiudaFrocio.mp4",
      mp3"rphjb_PoesiaNatalizia.mp3"
    ),
    ReplyBundleMessage.textToMedia[F](
      "buon anno",
      "happy new year",
      "capodanno",
    )(
      mp3"rphjb_RockChristmasHappyNewYear.mp3",
      vid"rphjb_AuguriDiNataleCapodannoFeste.mp4",
      vid"rphjb_PassatoAnnoVitaContinua.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      "i rage",
      "(sentiamo|ascoltiamo|senti|ascolta) la musica".r.tr(23)
    )(
      gif"rphjb_SentiamoMusicaRageGif.mp4",
      vid"rphjb_SentiamoMusicaRage.mp4",
      mp3"rphjb_SentiamoMusicaRage.mp3"
    ),
    ReplyBundleMessage.textToMedia[F](
      "sei cambiata tutta",
      "piercing",
      "mi fai male",
      "in mezzo alle gambe",
      "proprio[ ]?[l]+a".r.tr(9)
    )(
      vid"rphjb_CambiataTuttaPiercingPropriolla.mp4",
      mp3"rphjb_CambiataTuttaPiercingPropriolla.mp3",
      gif"rphjb_CambiataTuttaPiercingPropriollaGif.mp4",
    ),
    ReplyBundleMessage.textToMedia[F](
      "luca di noia",
      "alla regia",
      "regista",
    )(
      mp3"rphjb_LucaDiNoia.mp3",
      mp3"rphjb_LucaDiNoia2.mp3",
      vid"rphjb_LucaDiNoia3.mp4",
      vid"rphjb_LucaDiNoia4.mp4",
      mp3"rphjb_MatematiciAnaliticiDiNoia.mp3",
      vid"rphjb_MiPareLogicoMatematiciAnaliticiDiNoia.mp4",
      mp3"rphjb_GrandeRegistaLucaDiNoia.mp3",
      vid"rphjb_GrandeRegistaLucaDiNoia.mp4",
      vid"rphjb_LucaDiNoiaGrandeRegista.mp4",
      vid"rphjb_LucaDiNoiaRegia.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      "angelo",
      "carpenelli",
      "via delle albizzie",
      "istinti musicali",
    )(
      vid"rphjb_AngeloTrovamelo.mp4",
      vid"rphjb_AlbizziePerlaPioggia.mp4",
      mp3"rphjb_IstintiMusicali.mp3",
      vid"rphjb_IstintiMusicali.mp4",
      vid"rphjb_GrandeAngelo.mp4",
      vid"rphjb_AngeloCarpenelliGrandeViaDelleAlbizzie22NumeroUnoImmensoInGinocchio.mp4",
      vid"rphjb_AngeloCarpenelliViaDelleAlbizzie22IstintiMusicali.mp4",
      vid"rphjb_VieSonoTanteMilioniDiMilioniMiCoglioniViaDelleAlbizzie22.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      "questa è una domanda",
      "non ti rispondo",
      "(qualche )?altra domanda".r.tr(14)
    )(
      gif"rphjb_QualcheAltraDomandaGif.mp4",
      mp3"rphjb_QualcheAltraDomanda.mp3",
      vid"rphjb_QualcheAltraDomanda.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      "occhi (come le donne )?degli anni (settanta|70)".r.tr(18)
    )(
      mp3"rphjb_OcchiDonneAnniSettanta.mp3",
      vid"rphjb_OcchiDonneAnniSettanta.mp4",
      gif"rphjb_OcchiDonneAnniSettantaGif.mp4",
      vid"rphjb_Ester2.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      "tu non mi conosci",
      "posso cambiare",
      "sono camaleontico",
      "sputarti in faccia",
    )(
      vid"rphjb_SputartiInFacciaCamaleontico.mp4",
      mp3"rphjb_SputartiInFacciaCamaleontico.mp3",
      gif"rphjb_SputartiInFacciaCamaleonticoGif.mp4",
    ),
    ReplyBundleMessage.textToMedia[F](
      "madre tortura",
      "(madre )?parrucca".r.tr(8)
    )(
      vid"rphjb_MadreTorturaParrucca.mp4",
      mp3"rphjb_MadreTorturaImprovvisata.mp3"
    ),
    ReplyBundleMessage.textToMedia[F](
      "cazzate"
    )(
      gif"rphjb_NonPossibile.mp4",
      vid"rphjb_NonPossibile2.mp4",
      vid"rphjb_DecidoIoMareCazzatePerCortesia.mp4",
      vid"rphjb_AltraCazzataVeritaSembranoCazzate.mp4",
      vid"rphjb_CriticaNoCazzate.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      "\\b06\\b".r.tr(2),
      "prefisso"
    )(
      gif"rphjb_06Gif.mp4",
      vid"rphjb_06.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      "vengognati",
    )(
      mp3"rphjb_VergognatiMatosFalasci.mp3",
      vid"rphjb_VergognatiMatosFalasci.mp4",
      vid"rphjb_VergognatiMancusiPaoloBiancoTastieristaAttentiPerStradaAndreaCarpi.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      "andre matos"
    )(
      mp3"rphjb_MatosShaman.mp3",
      vid"rphjb_AndreMatosShaman.mp4",
      mp3"rphjb_VergognatiMatosFalasci.mp3",
      vid"rphjb_VergognatiMatosFalasci.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      "edu falasci",
      "edoardo falaschi",
    )(
      mp3"rphjb_EduFalasci.mp3",
      mp3"rphjb_VergognatiMatosFalasci.mp3",
      vid"rphjb_VergognatiMatosFalasci.mp4",
      vid"rphjb_EduFalasciQuasiFrocio.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      "shaman",
    )(
      mp3"rphjb_MatosShaman.mp3",
      vid"rphjb_AndreMatosShaman.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      "assolutamente no",
      "non mi lamento"
    )(
      gif"rphjb_NonMiLamentoGif.mp4",
      vid"rphjb_NonMiLamento.mp4",
    ),
    ReplyBundleMessage.textToMedia[F](
      "fa paura pure a",
      "al di meola"
    )(
      mp3"rphjb_PauraAdAlDiMeola.mp3",
      vid"rphjb_PauraAdAlDiMeola.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      "mettermi in difficoltà",
      "amicizie (politiche| d[ie] polizia| d[ie] carabinieri| d[ei] tutt'altr[o]? genere)?".r.tr(9),
      "amici potenti"
    )(
      gif"rphjb_DifficoltaAmicizieTelefonataGif.mp4",
      vid"rphjb_DifficoltaAmicizieTelefonata.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      "in un attimo",
      "risolto tutto",
      "telefonata",
    )(
      gif"rphjb_Telefonata.mp4",
      vid"rphjb_DifficoltaAmicizieTelefonata.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      "nudo([ -]nudo)+".r.tr(4),
    )(
      mp3"rphjb_NudoFrocio.mp3",
      vid"rphjb_NudoNudo.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      "ragazza indemoniata",
    )(
      gif"rphjb_LaRagazzaIndemoniataGif.mp4",
      vid"rphjb_LaRagazzaIndemoniata.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      "non manca niente",
      "c'è tutto"
    )(
      gif"rphjb_NonMancaNienteGif.mp4",
      vid"rphjb_NonMancaNiente.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      "un avvertimento",
      "bastoni tra le ruote"
    )(
      gif"rphjb_Ciao2001Gif.mp4",
      vid"rphjb_Ciao2001_2.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      "fro(ci|sh)o([ -]fro(ci|sh)o)+".r.tr(5)
    )(
      mp3"rphjb_NudoFrocio.mp3",
      vid"rphjb_FrocioFrocio.mp4",
      vid"rphjb_FrocioFrocio2.mp4",
      mp3"rphjb_FrocioFrocio.mp3"
    ),
    ReplyBundleMessage.textToMedia[F](
      "kiko loureiro",
      "che salva la situazione"
    )(
      mp3"rphjb_KikoLoureiroSalvaSituazione.mp3",
      vid"rphjb_CapitoCheMagagnaKikoLoureiroSalvaSituazione.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      "che magagna",
      "che fregatura",
    )(
      mp3"rphjb_CapitoCheMagagna.mp3",
      vid"rphjb_CapitoCheMagagnaKikoLoureiroSalvaSituazione.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      "passaporto"
    )(
      mp3"rphjb_PassaportoRiccardoBenzoni.mp3",
      vid"rphjb_LetsGoodMyFriendsPassport.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      "quante ore",
      "quanti anni",
      "quanto tempo"
    )(
      gif"rphjb_QuanteOreGif.mp4",
      vid"rphjb_QuanteOreQuantiAnni.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      "squallido",
      "ultimo (nella lista )?degli (esseri )?umani".r.tr(18)
    )(
      gif"rphjb_BruttoSquallidoSchifosoGif.mp4",
      vid"rphjb_BruttoSquallidoSchifosoUltimoEsseriUmani.mp4",
      vid"rphjb_UltimoListaUmaniVenerdì22.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      "quattro solo",
      "faccio in tempo"
    )(
      gif"rphjb_QuattroSolo.mp4",
      vid"rphjb_4SoloTempiInTestaOrologiSfuggonoPolsi.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      "televita"
    )(
      mp3"rphjb_TelevitaSonoInizioRisata.mp3",
      vid"rphjb_Alle22MercolediTelevita.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      "mercoled[iì]".r.tr(9)
    )(
      vid"rphjb_TempoAlTempo.mp4",
      vid"rphjb_Alle22MercolediTelevita.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      "brutto frocio"
    )(
      mp3"rphjb_BruttoFrocio.mp3",
      vid"rphjb_CambiaCanaleBruttoFrocio.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      "ogni volta"
    )(
      mp3"rphjb_OgniVolta.mp3",
      vid"rphjb_OgniVolta.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      "fico sacro",
      "betulla",
      "canfora"
    )(
      mp3"rphjb_FigureMitologiche.mp3",
      vid"rphjb_FigureMitologiche.mp4",
      vid"rphjb_FigureMitologiche2.mp4",
      vid"rphjb_FigureMitologiche3.mp4",
      vid"rphjb_AssaporarePezzoMentaMandragolaFicoSacroIncensoBetullaCanforaSaporeStrano.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      "mandragola"
    )(
      mp3"rphjb_FigureMitologiche.mp3",
      vid"rphjb_FigureMitologiche.mp4",
      vid"rphjb_FigureMitologiche2.mp4",
      vid"rphjb_FigureMitologiche3.mp4",
      vid"rphjb_AssaporarePezzoMentaMandragolaFicoSacroIncensoBetullaCanforaSaporeStrano.mp4",
      vid"rphjb_Streghe.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      "assaporare",
      "incenso",
      "\\bmenta\\b".r.tr(5),
      "sapore (strano|indefinito)".r.tr(13),
    )(
      vid"rphjb_AssaporarePezzoMentaMandragolaFicoSacroIncensoBetullaCanforaSaporeStrano.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      "fai schifo",
      "sei l'ultimo",
    )(
      gif"rphjb_FaiSchifoSeiUltimoGif.mp4",
      vid"rphjb_FaiSchifoSeiUltimo.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      "graffi"
    )(
      gif"rphjb_Graffi.mp4",
      mp3"rphjb_SentireMaleBeneCarezzaOppostoGraffiareGraceJonesMagari.mp3"
    ),
    ReplyBundleMessage.textToMedia[F](
      "voce da uomo"
    )(
      mp3"rphjb_VoceDaUomo.mp3",
      vid"rphjb_CantanteDonnaVoceDaUomoDueBocceCosiInnamoratoPerdutamente.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      "delirio"
    )(
      gif"rphjb_Delirio.mp4",
      vid"rphjb_DelirioDelSabatoSera.mp4",
      vid"rphjb_CarrellataInfernaleDelirioPureNellaGolaTrasmissioneDaUrloEchoDelayVitaNaturalDurante.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      "nella gola"
    )(
      gif"rphjb_NellaGola.mp4",
      vid"rphjb_CarrellataInfernaleDelirioPureNellaGolaTrasmissioneDaUrloEchoDelayVitaNaturalDurante.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      "ma che (cazzo )?sto dicendo".r.tr(18)
    )(
      mp3"rphjb_MaCheCazzoStoDicendo.mp3",
      vid"rphjb_MaCheCazzoStoDicendo.mp4",
      gif"rphjb_MaCheCazzoStoDicendoGif.mp4",
      gif"rphjb_IlMartel.mp4",
      vid"rphjb_CheStoDicendoDiVoltaIlCervelloPrincipiVeneziani.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      "come state"
    )(
      gif"rphjb_CiaoComeStateGif.mp4",
      mp3"rphjb_CiaoComeState.mp3",
      vid"rphjb_CiaoComeState.mp4",
      vid"rphjb_Arivato.mp4",
      mp3"rphjb_LetSGoodStateBene.mp3"
    ),
    ReplyBundleMessage.textToMedia[F](
      "(af)?fanculo in una maniera pazzesca".r.tr(33),
      "altro che quel coglione",
    )(
      gif"rphjb_AffanculoManieraPazzescaGif.mp4",
      vid"rphjb_AffanculoManieraPazzesca.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      "facendo soffrire"
    )(
      gif"rphjb_FacendoSoffrire.mp4",
      vid"rphjb_CosaCosaSuccessoMeNeVadoFacendoSoffrire.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      "\\bn[o]{2,}!\\b".r.tr(3)
    )(
      gif"rphjb_No.mp4",
      vid"rphjb_FolliaQueenNo.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      "\\bport[a]+\\b".r.tr(5)
    )(
      gif"rphjb_Porta.mp4",
      vid"rphjb_NonApriteQuellaPorta.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      "a questo punto",
      "andiamo[ci]? a sentire".r.tr(18),
      "l'originale",
    )(
      gif"rphjb_SentireOriginale.mp4",
      vid"rphjb_FeelingsIncazzarmiAndiamociSentireOriginale.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      "gigi d'alessio"
    )(
      mp3"rphjb_GigiDAlessioAnnaTatangelo.mp3",
      vid"rphjb_GianniCelesteMeglioGigiDAlessio.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      "gianni celeste"
    )(
      gif"rphjb_GianniCelesteMeglioGigiDAlessio.mp4",
      vid"rphjb_RapMusicaMelodicaListaCantanti.mp4",
      vid"rphjb_QuesitoRegaloOtelloProfazioMarioLanzaTullioPaneLucianoTaglioliGianniCeleste.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      "negri",
      "sezione ritmica"
    )(
      mp3"rphjb_NegriSezioneRitmica.mp3",
      vid"rphjb_DueNegriMostruosi.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      "\\bcontinua\\b".r.tr(8)
    )(
      mp3"rphjb_Continua.mp3",
      vid"rphjb_Continua.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      "drogati"
    )(
      gif"rphjb_DrogatiRockettari1.mp4",
      vid"rphjb_DrogatiRockettari.mp4",
      vid"rphjb_EricClaptonDrogaUominiAffari.mp4",
      gif"rphjb_DrogatiPilotiGif.mp4",
      vid"rphjb_Rampolli.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      "sostanze improprie"
    )(
      vid"rphjb_DrogatiRockettari.mp4",
      vid"rphjb_EricClaptonDrogaUominiAffari.mp4",
      gif"rphjb_DrogatiRockettari1.mp4",
      vid"rphjb_VecchiAmiciAnni70VeranoSostanzeImproprieNonSonoMaiMorto.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      "(a[f]+)?fanculo(,)? per contesia".r.tr(20)
    )(
      gif"rphjb_FanculoPerCortesia.mp4",
      vid"rphjb_DecidoIoMareCazzatePerCortesia.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      "capolavoro"
    )(
      gif"rphjb_CapolavoroGif.mp4",
      vid"rphjb_Capolavoro.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      "stillati"
    )(
      gif"rphjb_DrogatiRockettari1.mp4",
      vid"rphjb_DrogatiRockettari.mp4",
      gif"rphjb_DrogatiRockettari2.mp4",
      vid"rphjb_RocchettariCapelliCortiGiaccaCravattaPassaportoStronzi.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      "ro[ckgh]+(ch|gh|k)e(d+|t+)ari".r.tr(10),
    )(
      gif"rphjb_DrogatiRockettari1.mp4",
      vid"rphjb_DrogatiRockettari.mp4",
      gif"rphjb_DrogatiRockettari2.mp4",
      vid"rphjb_RocchettariCapelliCortiGiaccaCravattaPassaportoStronzi.mp4",
      vid"rphjb_RockettariComeBestieCravattaPassaportoStronzi.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      "inizio della fine"
    )(
      gif"rphjb_InizioDellaFineGif.mp4",
      vid"rphjb_InizioDellaFine.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      "(s(ono|o'|ò)?|saranno) cazzi vostri".r.tr(15)
    )(
      mp3"rphjb_SarannoCazziVostri.mp3",
      vid"rphjb_SoCazziVostriStasera.mp4",
      vid"rphjb_SoCazziVostriGuaioPureCazziMia.mp4",
    ),
    ReplyBundleMessage.textToMedia[F](
      "vi spacco il culo"
    )(
      mp3"rphjb_ViSpaccoIlCulo.mp3",
      vid"rphjb_ViSpaccoIlCulo.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      "altari",
      "realtà",
    )(
      mp3"rphjb_AltariFatiscentiRealta.mp3",
      vid"rphjb_AltariFatiscentiRealta.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      "animali"
    )(
      mp3"rphjb_Animali.mp3",
      vid"rphjb_Animali.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      "\\bcolla\\b".r.tr(5),
      "\\bserp[ie]\\b".r.tr(5)
    )(
      vid"rphjb_CollaSerpe.mp4",
      mp3"rphjb_CollaSerpe.mp3",
      vid"rphjb_CollaSerpeSigarettePercussionista.mp4",
      vid"rphjb_FossaCollaSerpeSerpeFelicitaMusica.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      "battezzato"
    )(
      vid"rphjb_Blues.mp4",
      mp3"rphjb_Battesimo.mp3"
    ),
    ReplyBundleMessage.textToMedia[F](
      "stringere i denti",
    )(
      gif"rphjb_AndareAvanti.mp4",
      mp3"rphjb_AndareAvanti.mp3",
      vid"rphjb_AndareAvantiStringereIDenti.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      "si ostina",
      "foto vecchie"
    )(
      gif"rphjb_Ostina.mp4",
      vid"rphjb_FotoMalmsteen.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      "questo ragazzo"
    )(
      mp3"rphjb_LucioDalla.mp3",
      vid"rphjb_FotoMalmsteen.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      "due ossa"
    )(
      mp3"rphjb_DueOssa.mp3",
      vid"rphjb_GambeInesistentiDueOssa.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      "where are you going?"
    )(
      gif"rphjb_WhereAreYouGoing.mp4",
      vid"rphjb_TommyAldridgeYngwieMalmsteenWhereAreYouGoing.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      "infern[a]+l[e]+[!]*".r.tr(9)
    )(
      gif"rphjb_Infernale.mp4",
      mp3"rphjb_Infernale.mp3",
      vid"rphjb_CarrellataInfernaleDelirioPureNellaGolaTrasmissioneDaUrloEchoDelayVitaNaturalDurante.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      "lunghezza d'onda",
      "brave persone"
    )(
      gif"rphjb_LunghezzaDOnda.mp4",
      vid"rphjb_GiudizioParolaFine.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      "korn"
    )(
      mp3"rphjb_Battesimo.mp3",
      vid"rphjb_ParlandoDeiKorn.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      "preghier(a|ina)".r.tr(9),
      "io non credo",
      "la medicina",
      "andare dal dottore",
      "\\billusi\\b".r.tr(6),
    )(
      mp3"rphjb_Chiesa.mp3",
      vid"rphjb_PoveriIllusiChiesaPreghierinaPreteManfrineDottoreMedicina.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      "manfrin[ea]".r.tr(8),
    )(
      mp3"rphjb_Chiesa.mp3",
      vid"rphjb_PoveriIllusiChiesaPreghierinaPreteManfrineDottoreMedicina.mp4",
      vid"rphjb_VostraMenteAbbiettaCalpestataNoDirettiveEstremismoMafiaPoliticaPartitiStessaManfrina.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      "sarete puniti"
    )(
      gif"rphjb_SaretePuniti.mp4",
      vid"rphjb_SaretePunitiPoteriTerribili.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      "poteri te[r]+i[b]+ili".r.tr(17),
      "sono arrivati poteri"
    )(
      vid"rphjb_SaretePunitiPoteriTerribili.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      "perchè mi guardi",
      "maniera strana"
    )(
      gif"rphjb_Guardi.mp4",
      vid"rphjb_PercheGuardiCosiManieraStrana.mp4",
    ),
    ReplyBundleMessage.textToMedia[F](
      "hollywood"
    )(
      gif"rphjb_Hollywood.mp4",
      vid"rphjb_DaHollywood.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      "[cg]hi[td]a[r]+is[td]a [bp]referi[dt]o".r.tr(21)
    )(
      gif"rphjb_Chitarrista.mp4",
      vid"rphjb_PeggioDelPeggio.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      "sentendo male"
    )(
      gif"rphjb_MiStoSentendoMale.mp4",
      vid"rphjb_PeggioDelPeggio.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      "incrinata la voce",
      "parlo come un(a specie di)? frocio".r.tr(20)
    )(
      mp3"rphjb_IncrinataLaVoceFrocio.mp3",
      vid"rphjb_IncrinataLaVoceFrocio.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      "feste"
    )(
      gif"rphjb_Feste.mp4",
      vid"rphjb_FesteACasaNicolaArigliano.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      "\\burlo\\b".r.tr(4),
      "\\b[a]{5,}",
    )(
      gif"rphjb_Tuffo.mp4",
      gif"rphjb_Urlo.mp4",
      gif"rphjb_Urlo3.mp4",
      gif"rphjb_Urlo4.mp4",
      gif"rphjb_Urlo2.mp4",
      gif"rphjb_UrloCanaro.mp4",
      gif"rphjb_UrloRiso.mp4",
      vid"rphjb_UrloSignorGionz.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      "cantate",
      "arigliano"
    )(
      gif"rphjb_Arigliano.mp4",
      vid"rphjb_FesteACasaNicolaArigliano.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      "sbattuto[ ]?[l]+[aà]".r.tr(10),
      "sono abituato"
    )(
      vid"rphjb_SbatteControPiselloSonoAbituatoEssereSbattutoLa.mp4",
      gif"rphjb_SbattutoLaGif.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      "lo sapevo"
    )(
      mp3"rphjb_LoSapevoIo.mp3",
      vid"rphjb_LoSapevoIoMaleDiMenteTimoTolki.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      "🙏"
    )(
      gif"rphjb_Prega.mp4",
      gif"rphjb_Prega2.mp4",
      vid"rphjb_FeelingsSguardoPreghiera.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      "sguardo"
    )(
      gif"rphjb_Sguardo.mp4",
      gif"rphjb_Sguardo2.mp4",
      gif"rphjb_Confuso.mp4",
      gif"rphjb_Sguardo3.mp4",
      gif"rphjb_Sguardo4.mp4",
      gif"rphjb_SguardoCanaro.mp4",
      vid"rphjb_FeelingsSguardoPreghiera.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      "non capisco"
    )(
      gif"rphjb_IlSensoCapito.mp4",
      vid"rphjb_IlCervelloStaFondendoNonCapiscoUnCazzo.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      "via zara",
      "sei brava a truccare",
      "non vali niente",
      "sei l'ultima",
      "manco trucc[aà](re)? sai".r.tr(16),
      "marciapiede",
      "truccatrice"
    )(
      mp3"rphjb_TruccareViaZara.mp3",
      vid"rphjb_LabbraTruccatriceNuovaUltimaDelleDonneViaZara.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      "faccio la parte",
      " recit",
      " fing",
      "\\ba[t]{2,}[o]+re\\b".r.tr(7),
      "attrice"
    )(
      gif"rphjb_FaccioLaParte.mp4",
      vid"rphjb_GaioInGiallo.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      "meridionale",
      "terron"
    )(
      gif"rphjb_Meridionale.mp4",
      vid"rphjb_GaioInGiallo.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      "destino",
      "incontrare"
    )(
      gif"rphjb_Destino.mp4",
      vid"rphjb_GaioInGiallo.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      "tu( )?cul".r.tr(6)
    )(
      gif"rphjb_TuCul.mp4",
      vid"rphjb_GaioInGiallo.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      "perchè l'ho fatto",
      "non do spiegazioni"
    )(
      mp3"rphjb_PercheLHoFatto.mp3",
      vid"rphjb_PercheLHoFatto.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      "giù( giù)+".r.tr(7)
    )(
      mp3"rphjb_GiuGiuGiu.mp3",
      vid"rphjb_MiSentoPezzoDiMerdaUltimoGiùGiù.mp4"
    ),
    ReplyBundleMessage(
      trigger = TextTrigger(
        stt"sono",
        stt"ultimo"
      ),
      reply = MediaReply.fromList[F](
        List(
          mp3"rphjb_SonoUltimo.mp3",
          mp3"rphjb_SonoIoUltimo.mp3",
          vid"rphjb_MiSentoPezzoDiMerdaUltimoGiùGiù.mp4",
          vid"rphjb_UltimoListaUmaniVenerdì22.mp4"
        )
      ),
      matcher = ContainsAll
    ),
    ReplyBundleMessage.textToMedia[F](
      "covi il male",
      "invidia",
      "livore"
    )(
      gif"rphjb_CoviMaleInvidiaLivoreGif.mp4",
      vid"rphjb_CoviMaleInvidiaLivore.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      "mike terrana",
      "stacchi di batteria"
    )(
      vid"rphjb_StacchiDiBatteriaMikeTerranaInfernali.mp4",
      mp3"rphjb_Infernali.mp3"
    ),
    ReplyBundleMessage.textToMedia[F](
      "ridare i soldi",
      "stronzi"
    )(
      vid"rphjb_MeNeVado2.mp4",
      mp3"rphjb_MeNeVado2.mp3"
    ),
    ReplyBundleMessage.textToMedia[F](
      "proprio a me\\b".r.tr(12)
    )(
      gif"rphjb_ProprioAMe.mp4",
      vid"rphjb_ProprioAMe2.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      "mi auguro"
    )(
      mp3"rphjb_IoMiAuguro.mp3",
      vid"rphjb_MiAuguroTimoTolkiTourneeMondiale.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      "un casino",
      "il ritardo",
      "non c'entro"
    )(
      vid"rphjb_RitardoCasinoFuoriPolizia.mp4",
      mp3"rphjb_RitardoCasinoFuoriPolizia.mp3",
    ),
    ReplyBundleMessage.textToMedia[F](
      "colpa mia"
    )(
      vid"rphjb_TuttaColpaMia.mp4",
      vid"rphjb_RitardoCasinoFuoriPolizia.mp4",
      mp3"rphjb_RitardoCasinoFuoriPolizia.mp3",
      vid"rphjb_MiaColpaColpaMia.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      "suonato (abbastanza )?bene".r.tr(12),
    )(
      mp3"rphjb_SuonatoAbbastanzaBeneEVero.mp3",
      vid"rphjb_SuonatoAbbastanzaBeneManicoIntrisoZuccheroLiquidiSeminaliBirreAcqua.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      "quello che ti meriti",
      "fino alla fine"
    )(
      gif"rphjb_QuelloCheTiMeriti.mp4",
      vid"rphjb_QuelloCheTiMeritiFinoAllaFineDistruttoTotalmente.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      "cuore in mano",
      "mano nella mano",
      "pelle contro la pelle"
    )(
      gif"rphjb_CuoreInManoGif.mp4",
      gif"rphjb_CuoreInMano.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      "squallida"
    )(
      gif"rphjb_Squallida.mp4",
      vid"rphjb_SquallidaScorfanoRaganaCatafalcoAmbulante.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      "musica tecnica",
      "antonacci",
      "grignani",
      "jovanotti",
    )(
      mp3"rphjb_Rock.mp3",
      vid"rphjb_StorieSonoTanteConoscerePerParlareJovanottiAntonacciCarboniGrignaniAncheLaMerdaAvrebbeValore.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      "conosce(nza|re)".r.tr(9),
      "il sapere",
      "veri valori",
    )(
      mp3"rphjb_Conoscere.mp3",
      vid"rphjb_StorieSonoTanteConoscerePerParlareJovanottiAntonacciCarboniGrignaniAncheLaMerdaAvrebbeValore.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      "prendo quello che( cazzo)? c'è da prendere",
      "prendo (il motorino|il coso|la macchina|l'auto)",
      "\\bvengo\\b".r.tr(5),
      "non vengo\\b".r.tr(9)
    )(
      gif"rphjb_PrendoIlNecessario.mp4",
      vid"rphjb_VengoNonVengoPrendoCosoAutoMacchinaMotorino.mp4"
    ),
    ReplyBundleMessage.textToMp3[F](
      "non parlare",
      "non hai il diritto",
      "la trasmissione è la mia",
    )(
      mp3"rphjb_NonParlareTeTrasmissioneMia.mp3",
      vid"rphjb_NonParlareTeDirittoIoTrasmissioneMia.mp4"
    )
  )

}
