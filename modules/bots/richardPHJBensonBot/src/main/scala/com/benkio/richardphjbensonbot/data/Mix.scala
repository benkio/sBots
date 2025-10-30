package com.benkio.richardphjbensonbot.data

import cats.Applicative
import com.benkio.telegrambotinfrastructure.messagefiltering.MessageMatches
import com.benkio.telegrambotinfrastructure.model.reply.gif
import com.benkio.telegrambotinfrastructure.model.reply.mp3
import com.benkio.telegrambotinfrastructure.model.reply.sticker
import com.benkio.telegrambotinfrastructure.model.reply.vid
import com.benkio.telegrambotinfrastructure.model.reply.MediaReply
import com.benkio.telegrambotinfrastructure.model.reply.ReplyBundleMessage
import com.benkio.telegrambotinfrastructure.model.stt
import com.benkio.telegrambotinfrastructure.model.tr
import com.benkio.telegrambotinfrastructure.model.TextTrigger

object Mix {

  def messageRepliesMixData[F[_]: Applicative]: List[ReplyBundleMessage[F]] =
    messageRepliesMixData1[F] ++
      messageRepliesMixData2[F] ++
      messageRepliesMixData3[F] ++
      messageRepliesMixData4[F] ++
      messageRepliesMixData5[F]

  private def messageRepliesMixData1[F[_]: Applicative]: List[ReplyBundleMessage[F]] = List(
    ReplyBundleMessage
      .textToMedia[F](
        "vivi",
        "morti"
      )(
        vid"rphjb_ViviMorti.mp4"
      )
      .copy(matcher = MessageMatches.ContainsAll),
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
      vid"rphjb_BluesEbbeBambinoRockNRollBeBopALulaStormyMondayButTuesdayIsJustAsBad.mp4",
      vid"rphjb_SonyVaMaleMetalRock.mp4",
      mp3"rphjb_SonyVaMaleMetalRock.mp3",
      vid"rphjb_CervelloSuperaLinguaListaGruppiAnni60.mp4",
      mp3"rphjb_CervelloSuperaLinguaListaGruppiAnni60.mp3",
      mp3"rphjb_DoveStannoGliAppassionatiTanteMusiche.mp3",
      vid"rphjb_DoveStannoGliAppassionatiTanteMusiche.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      "ti distruggo"
    )(
      gif"rphjb_TiDistruggoGif.mp4",
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
      vid"rphjb_MiDragaStradeInferioriCristoPinocchioGerarchieInfernali.mp4",
      vid"rphjb_SolangeSfuggire.mp4",
      mp3"rphjb_SolangeSfuggire.mp3"
    ),
    ReplyBundleMessage.textToMedia[F](
      "pappalardo"
    )(
      mp3"rphjb_Pappalardo.mp3",
      vid"rphjb_Pappalardo.mp4",
      mp3"rphjb_FrocioFrocio.mp3",
      vid"rphjb_FrocioFrocio2.mp4",
      vid"rphjb_StoriaMarlinManson.mp4",
      vid"rphjb_TelefonataPappalardoFanculo.mp4",
      vid"rphjb_PappalardoGeorgeMichaelFreddyMercuryFrocio.mp4",
      mp3"rphjb_PappalardoGeorgeMichaelFreddyMercuryFrocio.mp3",
      vid"rphjb_ChiCazzoLHaDettoPappalardo.mp4",
      gif"rphjb_ChiCazzoLHaDettoPappalardoGif.mp4",
      mp3"rphjb_ChiCazzoLHaDettoPappalardo.mp3"
    ),
    ReplyBundleMessage.textToMedia[F](
      "lasciami in pace",
      "\\bstronza\\b".r.tr(7)
    )(
      gif"rphjb_LasciamiInPaceGif.mp4",
      vid"rphjb_LasciamiInPaceStronza.mp4",
      sticker"rphjb_LasciamiInPaceBensoniani.sticker"
    ),
    ReplyBundleMessage.textToMedia[F](
      "rimpinzati",
      "(g|c)io(g|c)+ola(d|t)a".r.tr(9),
      "pandori",
      "goldoni",
      "ciambelloni",
      "ciambellina",
      "gli amari",
      "abbacchi",
      "limoncell(o|i)".r.tr(10),
      "ingrassati",
      "andati al cesso",
      "(diecimila|10000) volte".r.tr(11)
    )(
      gif"rphjb_ConseguenzeDellaPasquaGif.mp4",
      mp3"rphjb_ConseguenzeDellaPasqua.mp3",
      vid"rphjb_ConseguenzeDellaPasqua.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      "stare male",
      "melensa"
    )(
      gif"rphjb_MiFaStareMaleGif.mp4",
      vid"rphjb_PeggioDelPeggio.mp4",
      vid"rphjb_MelensaStareMale.mp4",
      vid"rphjb_RelIllusions.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      "[a]+[t]{2,}[e]+[n]+[z]+[i]+[o]+[n]+[e]+[!]*[!e]$".r.tr(10)
    )(
      mp3"rphjb_Attenzione.mp3",
      vid"rphjb_Attenzione.mp4",
      vid"rphjb_AttenzioneSarcinaCuoia.mp4",
      mp3"rphjb_AttenzioneSarcinaCuoia.mp3",
      gif"rphjb_AttenzioneSarcinaCuoiaGif.mp4"
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
      gif"rphjb_FeelingsSputoLoopGif.mp4",
      gif"rphjb_FeelingsSputoGif.mp4",
      vid"rphjb_FeelingsSputo.mp4",
      vid"rphjb_BicchiereSputoLimitazioniUomoDonna.mp4",
      vid"rphjb_PoveriIgnorantiLatteDrogaSoloGreggeSputo.mp4",
      vid"rphjb_BicchiereSputoLimitazioniUomoDonna.mp4",
      sticker"rphjb_SputoBensoniani.sticker",
      vid"rphjb_EsseriUmaniZozzeriaCarnePelleSputoSudoreSpermaNonContiamoNiente.mp4",
      vid"rphjb_GenteStranaBicchiereSputo.mp4",
      mp3"rphjb_GenteStranaBicchiereSputo.mp3",
      gif"rphjb_CiSaroSempreIoDentroDiVoiEVoiDentroDiMeGif.mp4",
      vid"rphjb_CiSaroSempreIoDentroDiVoiEVoiDentroDiMe.mp4",
      mp3"rphjb_CiSaroSempreIoDentroDiVoiEVoiDentroDiMe.mp3"
    ),
    ReplyBundleMessage.textToMedia[F](
      "[gc]o[b]+e[l]+ini".r.tr(8),
      "co[b]+oldi".r.tr(8),
      "elfi",
      "\\bnani\\b".r.tr(4),
      "ossa dei morti"
    )(
      mp3"rphjb_FigureMitologiche.mp3",
      vid"rphjb_FigureMitologiche.mp4",
      vid"rphjb_FigureMitologiche2.mp4",
      vid"rphjb_FigureMitologiche3.mp4",
      vid"rphjb_FigureMitologicheLive.mp4",
      gif"rphjb_FigureMitologicheLiveGif.mp4",
      mp3"rphjb_FigureMitologicheLive.mp3",
      sticker"rphjb_ElfiBensoniani.sticker"
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
      "\\bstori(a|e)\\b".r.tr(6)
    )(
      mp3"rphjb_Storie.mp3",
      mp3"rphjb_StorieSonoTanteVecchiaccia.mp3",
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
      vid"rphjb_StorieTanteTempoPassaOlioLeccarePiuSpazio.mp4",
      vid"rphjb_StoriaIncidenteMotoAmico.mp4",
      mp3"rphjb_StoriaIncidenteMotoAmico.mp3",
      mp3"rphjb_RifiutatoLavorareStoriaMusicista.mp3",
      vid"rphjb_RifiutatoLavorareStoriaMusicista.mp4",
      vid"rphjb_DentiScazzottata.mp4",
      mp3"rphjb_DentiScazzottata.mp3",
      vid"rphjb_StoriaBambiniBiondi.mp4",
      mp3"rphjb_StoriaBambiniBiondi.mp3",
      vid"rphjb_StoriaBambiniBiondi2.mp4",
      mp3"rphjb_StoriaBambiniBiondi2.mp3",
      vid"rphjb_StoriaNapoliBambinaMutande.mp4",
      mp3"rphjb_StoriaNapoliBambinaMutande.mp3",
      vid"rphjb_ParteDiRomeo.mp4",
      vid"rphjb_TroppeStorieRaccontare.mp4",
      gif"rphjb_TroppeStorieRaccontareGif.mp4",
      mp3"rphjb_TroppeStorieRaccontare.mp3",
      vid"rphjb_StorieSonoTanteTroppoAlMioCervello.mp4",
      gif"rphjb_StorieSonoTanteTroppoAlMioCervelloGif.mp4",
      mp3"rphjb_StorieSonoTanteTroppoAlMioCervello.mp3",
      mp3"rphjb_TeLoVoglioRaccontareAncheSeNonCentraNiente.mp3",
      gif"rphjb_TeLoVoglioRaccontareAncheSeNonCentraNienteGif.mp4",
      vid"rphjb_TeLoVoglioRaccontareAncheSeNonCentraNiente.mp4",
      mp3"rphjb_ViDevoRaccontareUnAltraStoria.mp3",
      gif"rphjb_ViDevoRaccontareUnAltraStoriaGif.mp4",
      vid"rphjb_ViDevoRaccontareUnAltraStoria.mp4",
      mp3"rphjb_AdolfHitler.mp3",
      vid"rphjb_AdolfHitler.mp4",
      vid"rphjb_RumoreDeiCapelliCheCascavano.mp4",
      mp3"rphjb_RumoreDeiCapelliCheCascavano.mp3"
    ),
    ReplyBundleMessage.textToMedia[F](
      "au[ ]?de".r.tr(4),
      "\\btime\\b".r.tr(4),
      "uir[ ]?bi[ ]?taim".r.tr(9)
    )(
      mp3"rphjb_Audeuirbitaim.mp3",
      mp3"rphjb_Audeuirbitaim2.mp3",
      vid"rphjb_Audeuirbitaim.mp4",
      gif"rphjb_AudeuirbitaimGif.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      "micetta",
      "la morte"
    )(
      gif"rphjb_MicettaGif.mp4",
      vid"rphjb_LaMorteMicetta.mp4",
      vid"rphjb_LaMorte.mp4",
      vid"rphjb_LaMorte2.mp4",
      vid"rphjb_InnoAllaMorte.mp4",
      vid"rphjb_UnicoMezzoUccidereMorteMateriaSpirito.mp4",
      sticker"rphjb_Morte1Bensoniani.sticker",
      sticker"rphjb_Morte2Bensoniani.sticker"
    ),
    ReplyBundleMessage.textToMedia[F](
      "\\bspalle\\b".r.tr(6),
      "\\bbraccia\\b".r.tr(7),
      "t(i|e) strozzo".r.tr(10)
    )(
      gif"rphjb_FaccioVedereSpalleBracciaGif.mp4",
      vid"rphjb_FaccioVedereSpalleBraccia.mp4",
      mp3"rphjb_FaccioVedereSpalleBraccia.mp3",
      vid"rphjb_UccidereUnaPersona.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      "brutto stronzo",
      "fatti avanti",
      "hai tirato sta roba",
      "vieni qua\\b".r.tr(9)
    )(
      gif"rphjb_FaccioVedereSpalleBracciaGif.mp4",
      vid"rphjb_FaccioVedereSpalleBraccia.mp4",
      mp3"rphjb_FaccioVedereSpalleBraccia.mp3"
    ),
    ReplyBundleMessage.textToMedia[F](
      "non sapere",
      "aris[dt]o[dt][ie]le".r.tr(10),
      "socrate"
    )(
      gif"rphjb_SoDiNonSapereGif.mp4",
      vid"rphjb_SoDiNonSapere.mp4",
      vid"rphjb_SoDiNonSapere2.mp4",
      vid"rphjb_NonHoIlSapereQuelloCheNonSo.mp4",
      gif"rphjb_NonHoIlSapereQuelloCheNonSoGif.mp4",
      mp3"rphjb_NonHoIlSapereQuelloCheNonSo.mp3"
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
      mp3"rphjb_GesuCoglione.mp3"
    ),
    ReplyBundleMessage.textToMedia[F](
      "bastone infernale",
      "un'arma"
    )(
      mp3"rphjb_DueTrasmissioniColtelliBastoneInfernale.mp3",
      vid"rphjb_DueTrasmissioniColtelliBastoneInfernale.mp4",
      gif"rphjb_Bastone1Gif.mp4",
      gif"rphjb_Bastone3Gif.mp4",
      vid"rphjb_BastoneInfernale.mp4",
      vid"rphjb_BastoneArmaMicidiale.mp4",
      mp3"rphjb_BastoneArmaMicidiale.mp3",
      gif"rphjb_BastoneInfernaleArtigianiBeccoMetalloGif.mp4",
      vid"rphjb_BastoneInfernaleArtigianiBeccoMetallo.mp4",
      mp3"rphjb_BastoneInfernaleArtigianiBeccoMetallo.mp3"
    ),
    ReplyBundleMessage.textToMedia[F](
      "vi calpesto",
      "strisciate per terra"
    )(
      gif"rphjb_ViCalpestoGif.mp4",
      mp3"rphjb_ViCalpesto.mp3",
      vid"rphjb_ViCalpesto.mp4",
      sticker"rphjb_ViCalpestoBensoniani.sticker"
    ),
    ReplyBundleMessage.textToMedia[F](
      "vermi"
    )(
      vid"rphjb_Schifosi4.mp4",
      gif"rphjb_ViCalpestoGif.mp4",
      mp3"rphjb_ViCalpesto.mp3",
      vid"rphjb_ViCalpesto.mp4",
      sticker"rphjb_ViCalpestoBensoniani.sticker"
    ),
    ReplyBundleMessage.textToMedia[F](
      "andare avanti"
    )(
      gif"rphjb_AndareAvantiGif.mp4",
      mp3"rphjb_AndareAvanti.mp3",
      gif"rphjb_AndareAvanti2Gif.mp4",
      mp3"rphjb_AndareAvanti2.mp3",
      vid"rphjb_AndareAvanti2.mp4",
      vid"rphjb_ComposizioneIdeaFrescaInnovazioneAndareAvantiStiamoTornandoIndetro.mp4",
      vid"rphjb_AndareAvantiStringereIDenti.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      "non ci credete?",
      "grande s(d|t)ronza(d|t)(e|a)".r.tr(16)
    )(
      gif"rphjb_NonCiCredeteGif.mp4",
      mp3"rphjb_NonCiCredete.mp3"
    ),
    ReplyBundleMessage.textToMedia[F](
      "non me ne (frega|fotte)".r.tr(15),
      "chissenefrega",
      "non mi interessa",
      "me ne (frego|sbatto)".r.tr(21)
    )(
      gif"rphjb_NonMeNeFotteGif.mp4",
      gif"rphjb_NonMeNeFregaGif.mp4",
      vid"rphjb_NonMiFregaParloIo.mp4",
      mp3"rphjb_ENonMeNeFotteUnCazzo.mp3",
      mp3"rphjb_NonLeggoQuelloCheScrivete.mp3",
      mp3"rphjb_IncidentePonte.mp3",
      vid"rphjb_EscertoCritiche.mp4",
      gif"rphjb_EscertoGif.mp4",
      vid"rphjb_NoRabbiaRidereMeNeFrego.mp4",
      vid"rphjb_GiovencaVarzettaSposoChissenefrega.mp4",
      vid"rphjb_NonMiInteressano.mp4",
      mp3"rphjb_NonMiInteressa.mp3",
      vid"rphjb_DiventoViolento.mp4",
      mp3"rphjb_DiventoViolento.mp3"
    ),
    ReplyBundleMessage.textToMedia[F](
      "ultimi"
    )(
      mp3"rphjb_IstintiMusicaliIlPuntoDArrivo.mp3",
      vid"rphjb_IstintiMusicaliIlPuntoDArrivo.mp4",
      gif"rphjb_IstintiMusicaliIlPuntoDArrivoGif.mp4",
      gif"rphjb_ViCalpestoGif.mp4",
      mp3"rphjb_ViCalpesto.mp3",
      vid"rphjb_ViCalpesto.mp4",
      mp3"rphjb_StateZittiZozziUltimi.mp3",
      vid"rphjb_RottoIlCazzoUltimi.mp4",
      vid"rphjb_BruttiSchifosiUltimiDegliUltimiNonSonoUltimo.mp4",
      gif"rphjb_InFondoInBrancoSulPalcoDaSoliGif.mp4",
      vid"rphjb_InFondoInBrancoSulPalcoDaSoli.mp4",
      mp3"rphjb_InFondoInBrancoSulPalcoDaSoli.mp3",
      vid"rphjb_Ultimi.mp4",
      gif"rphjb_UltimiGif.mp4",
      gif"rphjb_Ultimi2Gif.mp4",
      mp3"rphjb_Ultimi.mp3",
      vid"rphjb_OttavaNotaRobaVecchiaSchifosi.mp4",
      mp3"rphjb_OttavaNotaRobaVecchiaSchifosi.mp3"
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
      "balera",
      "sagra dell'uva",
      "feste condominiali",
      "feste di piazza"
    )(
      mp3"rphjb_Canzonette.mp3",
      vid"rphjb_Canzonette.mp4",
      gif"rphjb_CanzonetteGif.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      "canzonette"
    )(
      mp3"rphjb_Canzonette.mp3",
      vid"rphjb_Canzonette.mp4",
      gif"rphjb_CanzonetteGif.mp4",
      vid"rphjb_CanzonettePoesieAuschwitzCervello.mp4"
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
      gif"rphjb_QuelloCheDicoIoGif.mp4",
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
      gif"rphjb_OkGoodShowFriendsGif.mp4",
      gif"rphjb_OkGoodShowFriends2Gif.mp4",
      mp3"rphjb_LetSGoodStateBene.mp3",
      vid"rphjb_WelaMyFriends.mp4",
      vid"rphjb_LetsGoodMyFriends.mp4",
      vid"rphjb_NonPoteteGiudicarUrloThatsGood.mp4",
      vid"rphjb_LetsGoodMyFriendsPassport.mp4",
      vid"rphjb_LetsGoodMyFriendsForTheShowThatNeverEnds.mp4",
      sticker"rphjb_WelaBensoniani.sticker"
    ),
    ReplyBundleMessage.textToMedia[F](
      "[a]?[ ]?[f]*fanculo".r.tr(7)
    )(
      vid"rphjb_AndateAFanculo.mp4",
      gif"rphjb_MaVatteneAffanculoGif.mp4",
      mp3"rphjb_MaVatteneAffanculo.mp3",
      vid"rphjb_PortlandVancuverFanculo.mp4",
      gif"rphjb_FanculoPerCortesiaGif.mp4",
      vid"rphjb_DecidoIoMareCazzatePerCortesia.mp4",
      vid"rphjb_CambiaCanaleBruttoFrocio.mp4",
      vid"rphjb_TelefonataPappalardoFanculo.mp4",
      gif"rphjb_VatteneAFanculoGif.mp4",
      mp3"rphjb_TeNeVaiAFanculo.mp3",
      gif"rphjb_AffanculoManieraPazzescaGif.mp4",
      vid"rphjb_AffanculoManieraPazzesca.mp4",
      mp3"rphjb_AffanculoManieraPazzesca.mp3",
      sticker"rphjb_IndicaAffanculoPazzescaBensoniani.sticker"
    ),
    ReplyBundleMessage.textToMedia[F](
      "feelings"
    )(
      mp3"rphjb_Feelings.mp3",
      vid"rphjb_FeelingsIncazzarmiAndiamociSentireOriginale.mp4",
      gif"rphjb_FeelingsIncazzarmiAndiamociSentireOriginaleGif.mp4",
      mp3"rphjb_FeelingsIncazzarmiAndiamociSentireOriginale.mp3",
      gif"rphjb_FeelingsPreghieraGif.mp4",
      vid"rphjb_FeelingsPreghiera.mp4",
      gif"rphjb_FeelingsATerraGif.mp4",
      vid"rphjb_FeelingsATerra.mp4",
      gif"rphjb_FeelingsSputoLoopGif.mp4",
      gif"rphjb_FeelingsSputoGif.mp4",
      vid"rphjb_FeelingsSputo.mp4",
      gif"rphjb_FeelingsSguardoGif.mp4",
      vid"rphjb_FeelingsSguardo.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      "me ne vado"
    )(
      mp3"rphjb_MeNeVado.mp3",
      vid"rphjb_EBastaETuHaiRottoIlCazzo.mp4",
      gif"rphjb_MiRompiErCazzoGif.mp4",
      gif"rphjb_MeNeVadoGif.mp4",
      vid"rphjb_MeNeVado2.mp4",
      mp3"rphjb_MeNeVado2.mp3"
    ),
    ReplyBundleMessage.textToMedia[F](
      "mignotta",
      "puttana",
      "troia"
    )(
      mp3"rphjb_Mignotta.mp3",
      gif"rphjb_MignottaGif.mp4",
      mp3"rphjb_VialeZara.mp3",
      vid"rphjb_StronzoFiglioMignotta.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      "ti devi spaventare"
    )(
      mp3"rphjb_TiDeviSpaventare.mp3",
      gif"rphjb_TiDeviSpaventareGif.mp4",
      vid"rphjb_TiDeviSpaventare.mp4",
      sticker"rphjb_TiDeviSpaventareBensoniani.sticker"
    ),
    ReplyBundleMessage.textToMedia[F](
      "il martell"
    )(
      mp3"rphjb_MaCheCazzoStoDicendo.mp3",
      vid"rphjb_MaCheCazzoStoDicendo.mp4",
      gif"rphjb_MaCheCazzoStoDicendoGif.mp4",
      gif"rphjb_IlMartelGif.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      "questa volta no"
    )(
      mp3"rphjb_QuestaVoltaNo.mp3",
      gif"rphjb_QuestaVoltaNoGif.mp4",
      mp3"rphjb_CervelloPensante.mp3",
      vid"rphjb_CervelloPensante.mp4",
      vid"rphjb_FiguraDiMerdaQuestaVoltaNo.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      "una vergogna"
    )(
      mp3"rphjb_Vergogna.mp3",
      vid"rphjb_Vergogna.mp4",
      gif"rphjb_VergognaGif.mp4",
      gif"rphjb_Vergogna2Gif.mp4",
      sticker"rphjb_UnaVergognaBensoniani.sticker"
    ),
    ReplyBundleMessage.textToMedia[F](
      "mi devo trasformare",
      "cristo canaro"
    )(
      mp3"rphjb_Trasformista.mp3",
      gif"rphjb_TrasformistaGif.mp4",
      vid"rphjb_CristoCanaro.mp4",
      sticker"rphjb_CristoCanaroBensoniani.sticker"
    ),
    ReplyBundleMessage.textToMedia[F](
      "ma[ ]?s(c|g)us[a]?".r.tr(5)
    )(
      mp3"rphjb_MaSgus.mp3",
      gif"rphjb_MaSgusGif.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      "grazie gianni"
    )(
      mp3"rphjb_Grazie.mp3",
      gif"rphjb_GrazieGif.mp4",
      vid"rphjb_Grazie.mp4",
      sticker"rphjb_GrazieGianniBensoniani.sticker"
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
      "per strada"
    )(
      mp3"rphjb_IncontratePerStrada.mp3",
      gif"rphjb_IncontratePerStradaGif.mp4",
      vid"rphjb_VergognatiMancusiPaoloBiancoTastieristaAttentiPerStradaAndreaCarpi.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      "stare attenti"
    )(
      mp3"rphjb_IncontratePerStrada.mp3",
      gif"rphjb_IncontratePerStradaGif.mp4",
      vid"rphjb_VergognatiMancusiPaoloBiancoTastieristaAttentiPerStradaAndreaCarpi.mp4",
      gif"rphjb_QuelliCheParlanoPocoGif.mp4",
      vid"rphjb_QuelliCheParlanoPoco.mp4",
      mp3"rphjb_QuelliCheParlanoPoco.mp3"
    ),
    ReplyBundleMessage.textToMedia[F](
      "tastierista"
    )(
      mp3"rphjb_Tastierista.mp3",
      vid"rphjb_VergognatiMancusiPaoloBiancoTastieristaAttentiPerStradaAndreaCarpi.mp4",
      mp3"rphjb_GuerraPiuTotale.mp3",
      gif"rphjb_GuerraPiuTotaleGif.mp4",
      vid"rphjb_GuerraPiuTotale.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      "lavora tu\\b".r.tr(9),
      "vecchiaccia",
      "hai la pelle dura",
      "io sono creatura"
    )(
      mp3"rphjb_LavoraTu.mp3",
      vid"rphjb_LavoraTu.mp4",
      vid"rphjb_LavoraTu2.mp4",
      vid"rphjb_LavoraTu3.mp4",
      vid"rphjb_LavoraTu4.mp4",
      gif"rphjb_LavoraTuGif.mp4",
      vid"rphjb_StorieSonoTanteVecchiaccia.mp4",
      sticker"rphjb_LavoraTuBensoniani.sticker"
    ),
    ReplyBundleMessage.textToMedia[F](
      "infern[a]+l[i]+[!]*".r.tr(9)
    )(
      mp3"rphjb_Infernali.mp3",
      gif"rphjb_InfernaliGif.mp4",
      vid"rphjb_StacchiDiBatteriaMikeTerranaInfernali.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      "per il culo"
    )(
      mp3"rphjb_PigliandoPerIlCulo.mp3",
      gif"rphjb_PigliandoPerIlCuloGif.mp4"
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
      gif"rphjb_SorrisoGif.mp4",
      gif"rphjb_SorrisoSognanteGif.mp4",
      mp3"rphjb_Risata2.mp3",
      vid"rphjb_SepolturaRisata.mp4",
      gif"rphjb_RisataTrattenutaGif.mp4",
      vid"rphjb_CheGruppoMiRicordaRisata.mp4",
      vid"rphjb_MomentiGloria.mp4",
      vid"rphjb_GeneriMusicali2.mp4",
      sticker"rphjb_RisataBensoniani.sticker",
      sticker"rphjb_Risata2Bensoniani.sticker",
      sticker"rphjb_Risata3Bensoniani.sticker",
      gif"rphjb_NonEMaleGif.mp4",
      mp3"rphjb_NonEMale.mp3",
      vid"rphjb_NonEMale.mp4",
      gif"rphjb_OrmaiRisataGif.mp4",
      mp3"rphjb_OrmaiRisata.mp3",
      vid"rphjb_OrmaiRisata.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      "ammazza che sei",
      "(quasi|proprio) un frocio".r.tr(15)
    )(
      mp3"rphjb_Frocio.mp3",
      gif"rphjb_FrocioGif.mp4",
      vid"rphjb_EduFalasciQuasiFrocio.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      "non mi sta bene"
    )(
      mp3"rphjb_NonMiStaBene.mp3",
      mp3"rphjb_NonMiStaBene2.mp3",
      gif"rphjb_NonMiStaBeneGif.mp4",
      gif"rphjb_NonMiStaBene2Gif.mp4",
      vid"rphjb_NonMiStaBeneDelusioneStorica.mp4",
      vid"rphjb_NonMiStaBene.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      "le labbra",
      "rossetto",
      "💄"
    )(
      mp3"rphjb_Labbra.mp3",
      gif"rphjb_LabbraGif.mp4",
      sticker"rphjb_RossettoBensoniani.sticker"
    ),
    ReplyBundleMessage.textToMedia[F](
      "la vita è il nemico"
    )(
      mp3"rphjb_VitaNemico.mp3",
      gif"rphjb_VitaNemicoGif.mp4",
      vid"rphjb_VitaNemico.mp4",
      vid"rphjb_VitaNemico2.mp4",
      vid"rphjb_VitaNemico3.mp4",
      vid"rphjb_VitaNemicoCervello.mp4",
      mp3"rphjb_PoesiaDolcezzaViolenta.mp3",
      vid"rphjb_PoesiaDolcezzaViolenta.mp4",
      gif"rphjb_IncrementoDelSessoGif.mp4",
      vid"rphjb_IncrementoDelSesso.mp4",
      mp3"rphjb_IncrementoDelSesso.mp3"
    ),
    ReplyBundleMessage.textToMedia[F](
      "permettere"
    )(
      mp3"rphjb_Permettere.mp3",
      gif"rphjb_PermettereGif.mp4",
      vid"rphjb_Labrie.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      "le note"
    )(
      mp3"rphjb_Note.mp3",
      gif"rphjb_NoteGif.mp4",
      vid"rphjb_TraTutteLeNote.mp4",
      sticker"rphjb_TraTutteLeNoteBensoniani.sticker",
      mp3"rphjb_SceltaDelleNote.mp3",
      gif"rphjb_SceltaDelleNoteGif.mp4",
      vid"rphjb_SceltaDelleNote.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      "te[r]+[i]+[b]+[i]+l[e]+".r.tr(8)
    )(
      mp3"rphjb_Terribile.mp3",
      vid"rphjb_Terribile.mp4",
      gif"rphjb_TerribileGif.mp4",
      vid"rphjb_Audeuirbitaim.mp4",
      gif"rphjb_AudeuirbitaimGif.mp4",
      vid"rphjb_PoesiaMaria.mp4",
      vid"rphjb_MiDragaStradeInferioriCristoPinocchioGerarchieInfernali.mp4",
      gif"rphjb_UnaCosaSchifosaTerribileGif.mp4",
      vid"rphjb_UnaCosaSchifosaTerribile.mp4",
      mp3"rphjb_UnaCosaSchifosaTerribile.mp3"
    ),
    ReplyBundleMessage.textToMedia[F](
      "napoli"
    )(
      mp3"rphjb_VengoANapoli.mp3",
      vid"rphjb_VengoANapoli.mp4",
      gif"rphjb_VengoANapoliGif.mp4",
      mp3"rphjb_VivaNapoli.mp3",
      gif"rphjb_VivaNapoliGif.mp4",
      vid"rphjb_CuginiCampagnaGiardinoSempliciHomoSapiensLiscioRumbaChaChaChaCanzoneNapoletanaOsanna.mp4",
      vid"rphjb_StoriaBambiniBiondi.mp4",
      mp3"rphjb_StoriaBambiniBiondi.mp3",
      vid"rphjb_StoriaBambiniBiondi2.mp4",
      mp3"rphjb_StoriaBambiniBiondi2.mp3",
      vid"rphjb_StoriaNapoliBambinaMutande.mp4",
      mp3"rphjb_StoriaNapoliBambinaMutande.mp3",
      vid"rphjb_MusicaNapoletanaRockLista.mp4",
      mp3"rphjb_MusicaNapoletanaRockLista.mp3",
      vid"rphjb_ImpegniListaCitta.mp4",
      mp3"rphjb_ImpegniListaCitta.mp3"
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
      gif"rphjb_BastaGif.mp4",
      gif"rphjb_Basta2Gif.mp4",
      mp3"rphjb_Basta2.mp3",
      vid"rphjb_EBastaETuHaiRottoIlCazzo.mp4",
      gif"rphjb_BastaGif.mp4",
      gif"rphjb_Basta2Gif.mp4",
      gif"rphjb_Basta3Gif.mp4",
      vid"rphjb_Basta4.mp4",
      vid"rphjb_BastaRottoIlCazzo.mp4",
      gif"rphjb_BastaSediaGif.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      "(assol[io]|(un|il) solo di)".r.tr(6)
    )(
      mp3"rphjb_Assolo.mp3",
      vid"rphjb_Assolo.mp4",
      vid"rphjb_Assolo2.mp4",
      vid"rphjb_AssoloBeat.mp4",
      vid"rphjb_AssoloSubdolo.mp4",
      gif"rphjb_Bassista2Gif.mp4",
      vid"rphjb_Bassista2.mp4",
      gif"rphjb_Chitarra1Gif.mp4",
      gif"rphjb_Chitarra2Gif.mp4",
      gif"rphjb_Chitarra3Gif.mp4",
      vid"rphjb_AssoloBasso.mp4",
      mp3"rphjb_AllucinanteAssolo.mp3",
      vid"rphjb_AllucinanteAssolo.mp4",
      gif"rphjb_AllucinanteAssoloGif.mp4",
      sticker"rphjb_AssoloBensoniani.sticker",
      vid"rphjb_AssoliFrancese.mp4",
      mp3"rphjb_AssoliFrancese.mp3",
      vid"rphjb_AncoraUnAltraCassa.mp4",
      mp3"rphjb_AncoraUnAltraCassa.mp3"
    ),
    ReplyBundleMessage.textToMedia[F](
      "(g|c)hi(t|d)arra".r.tr(8)
    )(
      mp3"rphjb_Assolo.mp3",
      vid"rphjb_Assolo.mp4",
      vid"rphjb_Assolo2.mp4",
      vid"rphjb_AssoloBeat.mp4",
      vid"rphjb_AssoloSubdolo.mp4",
      gif"rphjb_Chitarra1Gif.mp4",
      gif"rphjb_Chitarra2Gif.mp4",
      gif"rphjb_Chitarra3Gif.mp4",
      vid"rphjb_ChitarraPlettroVicoletto.mp4",
      vid"rphjb_ChitarraVicolettoPlettro2.mp4",
      mp3"rphjb_ChitarraZuggherada.mp3",
      vid"rphjb_ChitarraZuccherada.mp4",
      mp3"rphjb_ChitarraZuccheroAlgheVino.mp3",
      mp3"rphjb_AllucinanteAssolo.mp3",
      vid"rphjb_AllucinanteAssolo.mp4",
      gif"rphjb_AllucinanteAssoloGif.mp4",
      vid"rphjb_TiraFuoriIlCazzo.mp4",
      mp3"rphjb_TiraFuoriIlCazzo.mp3",
      gif"rphjb_TiraFuoriIlCazzoGif.mp4",
      sticker"rphjb_AssoloBensoniani.sticker",
      vid"rphjb_AncoraUnAltraCassa.mp4",
      mp3"rphjb_AncoraUnAltraCassa.mp3"
    ),
    ReplyBundleMessage.textToMedia[F](
      "il basso\\b".r.tr(5)
    )(
      gif"rphjb_Bassista2Gif.mp4",
      vid"rphjb_Bassista2.mp4",
      vid"rphjb_AssoloBasso.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      "\\b(g|c)a(b|p)i(d|t)o\\b".r.tr(6),
      "\\bcapissi\\b".r.tr(7)
    )(
      mp3"rphjb_HoCapito.mp3",
      mp3"rphjb_AveteCapito.mp3",
      vid"rphjb_AveteCapito.mp4",
      mp3"rphjb_Capito.mp3",
      mp3"rphjb_NonHannoCapitoUnCazzo.mp3",
      vid"rphjb_NonAveteCapitoUnCazzo.mp4",
      mp3"rphjb_NonAveteCapitoUnCazzo.mp3",
      gif"rphjb_NonAveteCapitoUnCazzoGif.mp4",
      mp3"rphjb_VoiNonAveteCapitoUnCazzo.mp3",
      vid"rphjb_VoiNonAveteCapitoUnCazzo.mp4",
      gif"rphjb_VoiNonAveteCapitoUnCazzoGif.mp4",
      gif"rphjb_IlSensoCapitoGif.mp4",
      gif"rphjb_CapitoDoveStiamoGif.mp4",
      gif"rphjb_NonHoCapitoGif.mp4",
      gif"rphjb_AveteCapitoEhGif.mp4",
      gif"rphjb_ComeAlSolitoNonAveteCapitoGif.mp4",
      mp3"rphjb_CapitoDoveStiamo.mp3",
      mp3"rphjb_CapisciRidotti.mp3",
      mp3"rphjb_CapitoCheMagagnaKikoLoureiroSalvaSituazione.mp3",
      vid"rphjb_CapitoCheMagagnaKikoLoureiroSalvaSituazione.mp4",
      gif"rphjb_CapitoCheMagagnaKikoLoureiroSalvaSituazioneGif.mp4",
      mp3"rphjb_DavantiGenteNonHaCapisceUnCazzo.mp3",
      vid"rphjb_AbbiamoCapito.mp4",
      vid"rphjb_AveteCapitoNo.mp4",
      vid"rphjb_StiamoNellaFollia.mp4",
      vid"rphjb_HaiCapitoAveteRapitoONonAveteCapitoUnCazzo.mp4",
      vid"rphjb_AveteCapito2.mp4",
      gif"rphjb_AveteCapitoUnCazzoDiNuovoComeSempreGif.mp4",
      vid"rphjb_AveteCapitoUnCazzoDiNuovoComeSempre.mp4",
      mp3"rphjb_AveteCapitoUnCazzoDiNuovoComeSempre.mp3",
      vid"rphjb_IlBastoneDiGesu.mp4",
      mp3"rphjb_IlBastoneDiGesu.mp3"
    ),
    ReplyBundleMessage.textToMedia[F](
      "esperiment",
      "1(,)? 2(,)? 3".r.tr(5),
      "uno(,)? due(,)? tre".r.tr(11),
      "porco[ ]?[d]+[i]+o".r.tr(8)
    )(
      mp3"rphjb_UrlareLaRabbia.mp3",
      vid"rphjb_UrlareLaRabbia.mp4",
      mp3"rphjb_Esperimento.mp3",
      vid"rphjb_Esperimento.mp4",
      vid"rphjb_Esperimento2.mp4",
      gif"rphjb_EsperimentoGif.mp4",
      gif"rphjb_Esperimento2Gif.mp4",
      gif"rphjb_Esperimento3Gif.mp4",
      vid"rphjb_DiciottoAnni.mp4",
      vid"rphjb_DiciottoAnni2.mp4",
      mp3"rphjb_EsperimentoSuYoutube.mp3",
      gif"rphjb_EsperimentoSuYoutubeGif.mp4",
      mp3"rphjb_Avremo18AnniLong.mp3",
      vid"rphjb_Avremo18AnniLong.mp4"
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
      mp3"rphjb_SchifosiCheSchifo.mp3",
      vid"rphjb_SchifosiCheSchifo.mp4",
      gif"rphjb_SchifosiCheSchifoGif.mp4",
      vid"rphjb_Schifosi4.mp4",
      gif"rphjb_Schifosi3Gif.mp4",
      vid"rphjb_SchifosoUltimi.mp4",
      mp3"rphjb_StateZittiZozziUltimi.mp3",
      gif"rphjb_SchifosiGif.mp4",
      gif"rphjb_Schifosi2Gif.mp4",
      vid"rphjb_Vigile.mp4",
      vid"rphjb_ConQuestaTecnica.mp4",
      mp3"rphjb_ConQuestaTecnica.mp3",
      vid"rphjb_BruttiSchifosiUltimiDegliUltimiNonSonoUltimo.mp4",
      vid"rphjb_ChitarreVergognateviSchifosiGiornaliMerda.mp4",
      vid"rphjb_DiventoPazzoMattoSchifosiUltimi.mp4",
      mp3"rphjb_DiventoPazzoMattoSchifosiUltimi.mp3",
      vid"rphjb_GenteMalvagiaDistruggereSparlaGiornalistiSchifosiCarpiMingoliAntonellaDario.mp4",
      sticker"rphjb_SchifosiBensoniani.sticker",
      mp3"rphjb_ProsciuttoNelleOrecchiePallePadreSchifosi.mp3",
      vid"rphjb_ProsciuttoNelleOrecchiePallePadreSchifosi.mp4",
      gif"rphjb_ProsciuttoNelleOrecchiePallePadreSchifosiGif.mp4",
      gif"rphjb_AllargareLeVeduteGif.mp4",
      mp3"rphjb_AllargareLeVedute.mp3",
      vid"rphjb_AllargareLeVedute.mp4",
      mp3"rphjb_SchifosiNonMeCeFaPensa.mp3",
      gif"rphjb_SchifosiNonMeCeFaPensaGif.mp4",
      vid"rphjb_SchifosiNonMeCeFaPensa.mp4",
      vid"rphjb_OttavaNotaRobaVecchiaSchifosi.mp4",
      mp3"rphjb_OttavaNotaRobaVecchiaSchifosi.mp3"
    ),
    ReplyBundleMessage.textToMedia[F](
      "schifos(o)+(!)*".r.tr(8)
    )(
      gif"rphjb_SchifosoGif.mp4",
      vid"rphjb_Vigile.mp4",
      vid"rphjb_AmoreSuonareFreddoBuddistiSchifoso.mp4",
      vid"rphjb_BruttoSquallidoSchifoso.mp4",
      mp3"rphjb_BruttoSquallidoSchifoso.mp3",
      gif"rphjb_BruttoSquallidoSchifosoGif.mp4",
      vid"rphjb_DanzaMacabra.mp4",
      vid"rphjb_DiventoViolento.mp4",
      mp3"rphjb_DiventoViolento.mp3"
    ),
    ReplyBundleMessage.textToMedia[F](
      "mortacci vostri"
    )(
      gif"rphjb_MortacciVostriGif.mp4",
      mp3"rphjb_StateZittiZozziUltimi.mp3",
      vid"rphjb_ConQuestaTecnica.mp4",
      mp3"rphjb_ConQuestaTecnica.mp3",
      vid"rphjb_MortacciVostri.mp4",
      vid"rphjb_CheCazzoEraQuellaRoba2.mp4",
      gif"rphjb_PiuIncazzatoPiuFeliciMortacciVostriGif.mp4",
      vid"rphjb_PiuIncazzatoPiuFeliciMortacciVostri.mp4",
      mp3"rphjb_PiuIncazzatoPiuFeliciMortacciVostri.mp3"
    ),
    ReplyBundleMessage.textToMedia[F](
      "non vedo"
    )(
      mp3"rphjb_Stanco.mp3",
      vid"rphjb_Stanco.mp4",
      mp3"rphjb_PannaOcchialiSpalla.mp3"
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
      gif"rphjb_ApplausoGif.mp4",
      mp3"rphjb_Applauso.mp3",
      mp3"rphjb_Applauso2.mp3",
      vid"rphjb_ApplausoPiuForte.mp4",
      vid"rphjb_ApplausoPiuNutrito.mp4",
      gif"rphjb_ApplausoPiuNutritoGif.mp4",
      mp3"rphjb_ApplausoPiuNutrito.mp3"
    ),
    ReplyBundleMessage.textToMedia[F](
      "venite qua"
    )(
      gif"rphjb_VeniteQuaGif.mp4",
      mp3"rphjb_VeniteQua.mp3",
      vid"rphjb_VeniteQua.mp4",
      sticker"rphjb_VeniteQuaBensoniani.sticker"
    ),
    ReplyBundleMessage.textToMedia[F](
      "\\bpaga(re)?\\b".r.tr(4),
      "sold[oi]".r.tr(5),
      "bollette",
      "tasse",
      "bolletta",
      "tassa"
    )(
      gif"rphjb_ChiCacciaISoldiGif.mp4",
      mp3"rphjb_ChiCacciaISoldi.mp3",
      vid"rphjb_ChiCacciaISoldi.mp4",
      mp3"rphjb_SoldiButtatiDiscotecaLaziale.mp3",
      vid"rphjb_SoldiButtatiDiscotecaLaziale.mp4",
      vid"rphjb_BigMoney.mp4",
      vid"rphjb_InvestitoreGoverno.mp4",
      vid"rphjb_ButtareSoldiFinestra.mp4",
      vid"rphjb_CoiSoldiMiei.mp4",
      gif"rphjb_CoiSoldiMieiGif.mp4",
      mp3"rphjb_CoiSoldiMiei.mp3",
      vid"rphjb_StorieTanteTempoPassaOlioLeccarePiuSpazio.mp4",
      gif"rphjb_PoiVoglionoISoldiGif.mp4",
      vid"rphjb_PoiVoglionoISoldi.mp4",
      mp3"rphjb_PoiVoglionoISoldi.mp3",
      gif"rphjb_QuelliCheParlanoPocoGif.mp4",
      vid"rphjb_QuelliCheParlanoPoco.mp4",
      mp3"rphjb_QuelliCheParlanoPoco.mp3",
      vid"rphjb_RagazzettaDiProvincia.mp4",
      mp3"rphjb_RagazzettaDiProvincia.mp3",
      gif"rphjb_RagazzettaDiProvinciaGif.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      "[od]?dio mio[,]? no".r.tr(10)
    )(
      mp3"rphjb_OddioMioNo.mp3",
      mp3"rphjb_OddioMioNo2.mp3",
      vid"rphjb_OddioMioNo.mp4",
      vid"rphjb_OddioMioNo2.mp4",
      gif"rphjb_OddioMioNoGif.mp4",
      sticker"rphjb_DioMioBensoniani.sticker",
      mp3"rphjb_SentirGiovanePamelaAnderson.mp3",
      vid"rphjb_SentirGiovanePamelaAnderson.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      "(sono |so )?a[r]{1,2}iva(d|t)o".r.tr(12),
      "(eccomi|ciao).*\\bpiacere\\b".r.tr(13)
    )(
      gif"rphjb_ArivatoGif.mp4",
      mp3"rphjb_Arivato.mp3",
      gif"rphjb_ArivatoGif.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      "delu(s|d)".r.tr(5)
    )(
      gif"rphjb_DelusoGif.mp4",
      mp3"rphjb_Deluso.mp3",
      vid"rphjb_DeludendoQuasiTutto.mp4",
      vid"rphjb_GambaleCHaDeluso.mp4",
      sticker"rphjb_DeludendoBensoniani.sticker"
    ),
    ReplyBundleMessage.textToMedia[F](
      "fate come vi pare",
      "sti[ ]?(g|c)azzi".r.tr(8)
    )(
      gif"rphjb_ComeViPareGif.mp4",
      mp3"rphjb_ComeViPare.mp3"
    ),
    ReplyBundleMessage.textToMedia[F](
      "(divento|come) una bestia".r.tr(15),
      "incazzo"
    )(
      mp3"rphjb_DiventoBestia.mp3",
      mp3"rphjb_Incazzo.mp3",
      mp3"rphjb_Incazzo2.mp3",
      mp3"rphjb_PrimoSbaglio.mp3",
      vid"rphjb_PrimoSbaglio.mp4",
      gif"rphjb_MiIncazzoComeUnaBestiaGif.mp4",
      vid"rphjb_MiIncazzoComeUnaBestia.mp4",
      mp3"rphjb_MiIncazzoComeUnaBestia.mp3",
      vid"rphjb_PiuPacatoMiIncazzo.mp4",
      gif"rphjb_PiuPacatoMiIncazzoGif.mp4",
      mp3"rphjb_PiuPacatoMiIncazzo.mp3",
      mp3"rphjb_MenteSuperioreInferioreLucaDiNoia.mp3",
      vid"rphjb_MenteSuperioreInferioreLucaDiNoia.mp4",
      mp3"rphjb_IncazzoComeUnaBestia.mp3",
      gif"rphjb_IncazzoComeUnaBestiaGif.mp4",
      vid"rphjb_IncazzoComeUnaBestia.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      "dove stiamo",
      "stiamo nella follia"
    )(
      mp3"rphjb_CapitoDoveStiamo.mp3",
      vid"rphjb_StiamoNellaFollia.mp4",
      gif"rphjb_CapitoDoveStiamoGif.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      "non sai molto"
    )(
      gif"rphjb_NonSaiMoltoGif.mp4",
      vid"rphjb_RadioRockErrori.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      "errori",
      "non posso essere l'unico"
    )(
      gif"rphjb_MaiErroriGif.mp4",
      vid"rphjb_MaiErrori.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      "\\bpasqua\\b".r.tr(6)
    )(
      mp3"rphjb_AuguriPasqua.mp3",
      vid"rphjb_AuguriPerPasqua.mp4",
      vid"rphjb_PasquaInsiemeRisorti.mp4",
      gif"rphjb_PasquaInsiemeRisortiGif.mp4",
      mp3"rphjb_PasquaInsiemeRisorti.mp3"
    ),
    ReplyBundleMessage.textToMedia[F](
      "vaniglia",
      "pandoro",
      "crema alla (g|c)io(g|c)+ola(d|t)a".r.tr(20),
      "intrise"
    )(
      mp3"rphjb_ZuccheroVanigliaCremaCioccolataPandoro.mp3",
      vid"rphjb_AuguriPerPasqua.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      "c'hai timore",
      "c'hai paura",
      "mi hai detto (all'orecchio )prima".r.tr(18),
      "diri[g]+en(d|t)i".r.tr(9)
    )(
      mp3"rphjb_TimoreDirigenti.mp3",
      vid"rphjb_TimoreDirigenti.mp4",
      gif"rphjb_TimoreDirigentiGif.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      "guerra pi[uù] totale".r.tr(17),
      "trasferito in america",
      "formazione micidiale",
      "quartetto di questo genere"
    )(
      mp3"rphjb_GuerraPiuTotale.mp3",
      gif"rphjb_GuerraPiuTotaleGif.mp4",
      vid"rphjb_GuerraPiuTotale.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      "non voglio nessuno",
      "mentre lavoro"
    )(
      gif"rphjb_NonVoglioNessunoGif.mp4",
      vid"rphjb_NonVoglioNessuno.mp4",
      vid"rphjb_NonApriteQuellaPorta.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      "peggio del peggio"
    )(
      gif"rphjb_PeggioDelPeggioGif.mp4",
      vid"rphjb_PeggioDelPeggio.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      "aluba",
      "my baby"
    )(
      gif"rphjb_BebopGif.mp4",
      vid"rphjb_BluesEbbeBambinoRockNRollBeBopALulaStormyMondayButTuesdayIsJustAsBad.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      "anni (settanta|70)".r.tr(7)
    )(
      vid"rphjb_RingrazioPersoneAttenteDonneToccavanoSeniAnni70LettiPieniErbaCoca.mp4",
      vid"rphjb_VecchiAmiciAnni70VeranoSostanzeImproprieNonSonoMaiMorto.mp4",
      vid"rphjb_CervelloSuperaLinguaListaGruppiAnni60.mp4",
      mp3"rphjb_CervelloSuperaLinguaListaGruppiAnni60.mp3",
      vid"rphjb_ListaGruppi.mp4",
      mp3"rphjb_ListaGruppi.mp3",
      vid"rphjb_Vestiti.mp4",
      mp3"rphjb_Vestiti.mp3"
    ),
    ReplyBundleMessage.textToMedia[F](
      "anni (sessanta|60)".r.tr(7)
    )(
      vid"rphjb_CervelloSuperaLinguaListaGruppiAnni60.mp4",
      mp3"rphjb_CervelloSuperaLinguaListaGruppiAnni60.mp3",
      vid"rphjb_Vestiti.mp4",
      mp3"rphjb_Vestiti.mp3"
    ),
    ReplyBundleMessage.textToMedia[F](
      "bebop",
      "be bop"
    )(
      gif"rphjb_BebopGif.mp4",
      vid"rphjb_BluesEbbeBambinoRockNRollBeBopALulaStormyMondayButTuesdayIsJustAsBad.mp4",
      vid"rphjb_CervelloSuperaLinguaListaGruppiAnni60.mp4",
      mp3"rphjb_CervelloSuperaLinguaListaGruppiAnni60.mp3",
      mp3"rphjb_DoveStannoGliAppassionatiTanteMusiche.mp3",
      vid"rphjb_DoveStannoGliAppassionatiTanteMusiche.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      "(18|diciott['o]?) anni".r.tr(7)
    )(
      vid"rphjb_DiciottoAnni.mp4",
      vid"rphjb_DiciottoAnni2.mp4",
      gif"rphjb_DiciottoAnniGif.mp4",
      gif"rphjb_Avremo18anniPerSempreGif.mp4",
      vid"rphjb_Avremo18anniPerSempre.mp4",
      mp3"rphjb_Avremo18anniPerSempre.mp3",
      mp3"rphjb_Avremo18AnniLong.mp3",
      vid"rphjb_Avremo18AnniLong.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      "(cinque|5) dita".r.tr(6),
      "pugno"
    )(
      vid"rphjb_CinqueDita.mp4",
      mp3"rphjb_CinqueDita.mp3",
      vid"rphjb_CinqueDita2.mp4",
      vid"rphjb_RollingStonesJimiHendrixPoetiMaledettiFemministeControculturaRivoluzioniRibelioni5DitaCazzottoInFacciaGiustiziere.mp4",
      vid"rphjb_SonoAttentoVaTuttoBeneAttagliatoTempo5DitaPugno.mp4",
      gif"rphjb_5DitaRivoltaGif.mp4",
      vid"rphjb_5DitaRivolta.mp4",
      mp3"rphjb_5DitaRivolta.mp3",
      mp3"rphjb_PoesiaDolcezzaViolenta.mp3",
      vid"rphjb_PoesiaDolcezzaViolenta.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      "rivolta"
    )(
      gif"rphjb_5DitaRivoltaGif.mp4",
      vid"rphjb_5DitaRivolta.mp4",
      mp3"rphjb_5DitaRivolta.mp3"
    ),
    ReplyBundleMessage.textToMedia[F](
      "\\bratti\\b".r.tr(5),
      "topi"
    )(
      vid"rphjb_DubbioScantinatiGiocoRattoGatto.mp4",
      mp3"rphjb_ListaMaleCollo.mp3",
      gif"rphjb_IlPrimoInCantinaGif.mp4",
      vid"rphjb_IlPrimoInCantina.mp4",
      mp3"rphjb_IlPrimoInCantina.mp3"
    ),
    ReplyBundleMessage.textToMedia[F](
      "poveri cretini",
      "poveri ignoranti"
    )(
      mp3"rphjb_PoveriCretini.mp3",
      vid"rphjb_PoveriIgnorantiLatteDrogaSoloGreggeSputo.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      "solo uno parl[oóò]".r.tr(14),
      "uno solo parl[oóò]".r.tr(14),
      "(c|g)ri(d|t)i(g|c)a(d|t)o".r.tr(9),
      "sapevano tutto",
      "nessuno parlava"
    )(
      gif"rphjb_SapevanoTuttoFuCriticatoGif.mp4",
      vid"rphjb_SapevanoTuttoFuCriticato.mp4",
      mp3"rphjb_SapevanoTuttoFuCriticato.mp3"
    ),
    ReplyBundleMessage.textToMedia[F](
      "venerd[iì]".r.tr(7)
    )(
      mp3"rphjb_VenerdiUscirePeggioCoseDanno.mp3",
      vid"rphjb_VenerdiUscirePeggioCoseDanno.mp4",
      gif"rphjb_VenerdiUscirePeggioCoseDannoGif.mp4",
      mp3"rphjb_Yodle.mp3",
      vid"rphjb_Yodle.mp4",
      mp3"rphjb_DueTrasmissioniColtelliBastoneInfernale.mp3",
      vid"rphjb_DueTrasmissioniColtelliBastoneInfernale.mp4",
      vid"rphjb_Venerdi.mp4",
      vid"rphjb_IlVenerdi.mp4",
      vid"rphjb_TempoAlTempo.mp4",
      vid"rphjb_VenerdiAppuntamentoFissoFica.mp4",
      vid"rphjb_UltimoListaUmaniVenerdi22.mp4",
      vid"rphjb_SteveVai.mp4",
      mp3"rphjb_SteveVai.mp3",
      gif"rphjb_SteveVaiGif.mp4",
      vid"rphjb_BiscionePiatti.mp4",
      vid"rphjb_Alle22MercolediTelevita.mp4",
      mp3"rphjb_Alle22MercolediTelevita.mp3",
      gif"rphjb_CiSaroSempreIoDentroDiVoiEVoiDentroDiMeGif.mp4",
      vid"rphjb_CiSaroSempreIoDentroDiVoiEVoiDentroDiMe.mp4",
      mp3"rphjb_CiSaroSempreIoDentroDiVoiEVoiDentroDiMe.mp3"
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
      "analitici"
    )(
      vid"rphjb_CoseCheNonSopportoCalcoliSbagliati.mp4",
      mp3"rphjb_MiPareLogicoMatematiciAnaliticiDiNoia.mp3",
      vid"rphjb_MiPareLogicoMatematiciAnaliticiDiNoia.mp4",
      gif"rphjb_MiPareLogicoMatematiciAnaliticiDiNoiaGif.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      "\\blo[g]+i(c|g)o\\b".r.tr(6)
    )(
      gif"rphjb_TukulGif.mp4",
      mp3"rphjb_Tukul.mp3",
      vid"rphjb_Tukul.mp4",
      mp3"rphjb_MiPareLogicoMatematiciAnaliticiDiNoia.mp3",
      vid"rphjb_MiPareLogicoMatematiciAnaliticiDiNoia.mp4",
      gif"rphjb_MiPareLogicoMatematiciAnaliticiDiNoiaGif.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      "ti dovresti vergognare"
    )(
      gif"rphjb_TiDovrestiVergognareGif.mp4",
      vid"rphjb_TiDovrestiVergognare.mp4",
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
      "bisogna pure lavorà"
    )(
      gif"rphjb_NonLiSopportoGif.mp4",
      mp3"rphjb_NonLiSopporto.mp3",
      vid"rphjb_NonLiSopporto.mp4"
    )
  )

  private def messageRepliesMixData2[F[_]: Applicative]: List[ReplyBundleMessage[F]] = List(
    ReplyBundleMessage.textToMedia[F](
      "\\bmanager\\b".r.tr(7)
    )(
      vid"rphjb_ManagerAmericanoGrignianiShit.mp4",
      mp3"rphjb_ManagerAmericanoGrignianiShit.mp3"
    ),
    ReplyBundleMessage.textToMedia[F](
      "artigian(i|ale)".r.tr(9),
      "avorio",
      "teschio",
      "intarsi(ato)?".r.tr(7)
    )(
      gif"rphjb_BastoneInfernaleArtigianiBeccoMetalloGif.mp4",
      vid"rphjb_BastoneInfernaleArtigianiBeccoMetallo.mp4",
      mp3"rphjb_BastoneInfernaleArtigianiBeccoMetallo.mp3"
    ),
    ReplyBundleMessage.textToMedia[F](
      "di metallo"
    )(
      gif"rphjb_BastoneInfernaleArtigianiBeccoMetalloGif.mp4",
      vid"rphjb_BastoneInfernaleArtigianiBeccoMetallo.mp4",
      mp3"rphjb_BastoneInfernaleArtigianiBeccoMetallo.mp3",
      vid"rphjb_Vestiti.mp4",
      mp3"rphjb_Vestiti.mp3"
    ),
    ReplyBundleMessage.textToMedia[F](
      "valletta"
    )(
      vid"rphjb_PiaccionoBelleDonneVallettaGianniNeriGrandeAmico.mp4",
      vid"rphjb_BisognoValletta.mp4",
      mp3"rphjb_BisognoValletta.mp3"
    ),
    ReplyBundleMessage.textToMedia[F](
      "corpicini",
      "da (succhiare|mangiare)".r.tr(11),
      "in jeans",
      "vanno sempre bene",
      "m[ei] sta tutto bene".r.tr(17),
      "vita (rock|dura|violenta|piena di gioie|piena di ferite)".r.tr(9)
    )(
      vid"rphjb_BisognoValletta.mp4",
      mp3"rphjb_BisognoValletta.mp3"
    ),
    ReplyBundleMessage.textToMedia[F](
      "scarpe da tennis"
    )(
      vid"rphjb_GhentScarpeDaTennis.mp4",
      mp3"rphjb_GhentScarpeDaTennis.mp3",
      vid"rphjb_BisognoValletta.mp4",
      mp3"rphjb_BisognoValletta.mp3"
    ),
    ReplyBundleMessage.textToMedia[F](
      "allucinante"
    )(
      gif"rphjb_AllucinanteGif.mp4",
      vid"rphjb_DentiScazzottata.mp4",
      mp3"rphjb_DentiScazzottata.mp3",
      mp3"rphjb_AllucinanteAssolo.mp3",
      vid"rphjb_AllucinanteAssolo.mp4",
      gif"rphjb_AllucinanteAssoloGif.mp4",
      vid"rphjb_BarzellettaTotti.mp4",
      mp3"rphjb_BarzellettaTotti.mp3"
    ),
    ReplyBundleMessage.textToMedia[F](
      "scusatemi per i denti",
      "a botte\\b".r.tr(7),
      "cazzott[oi]".r.tr(8),
      "bastonata",
      "dare pugni",
      "aggredisce",
      "per nessun motivo lo[g]+ico".r.tr(24),
      "essere alterato",
      "battibecco"
    )(
      vid"rphjb_DentiScazzottata.mp4",
      mp3"rphjb_DentiScazzottata.mp3"
    ),
    ReplyBundleMessage.textToMedia[F](
      "femminis",
      "contro( )?cultura".r.tr(13)
    )(
      vid"rphjb_OcchiAnniSettantaFemmismoControcultura.mp4",
      mp3"rphjb_OcchiAnniSettantaFemmismoControcultura.mp3",
      vid"rphjb_RollingStonesJimiHendrixPoetiMaledettiFemministeControculturaRivoluzioniRibelioni5DitaCazzottoInFacciaGiustiziere.mp4",
      vid"rphjb_CorteiFemministiAmplessiMacisti.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      "donne (vissute|con le palle)".r.tr(13),
      "groupies"
    )(
      vid"rphjb_OcchiAnniSettantaFemmismoControcultura.mp4",
      mp3"rphjb_OcchiAnniSettantaFemmismoControcultura.mp3"
    ),
    ReplyBundleMessage.textToMedia[F](
      "partiti politici",
      "politica"
    )(
      vid"rphjb_BarzellettaPoliticaGinecologo.mp4",
      vid"rphjb_OcchiAnniSettantaFemmismoControcultura.mp4",
      mp3"rphjb_OcchiAnniSettantaFemmismoControcultura.mp3",
      vid"rphjb_VostraMenteAbbiettaCalpestataNoDirettiveEstremismoMafiaPoliticaPartitiStessaManfrina.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      "bin laden",
      "torri gemelle",
      "(11|undici) settembre".r.tr(12)
    )(
      mp3"rphjb_911TorriGemelleBinLaden.mp3",
      vid"rphjb_911TorriGemelleBinLaden.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      "\\bosanna\\b".r.tr(6)
    )(
      vid"rphjb_StoriaBambiniBiondi.mp4",
      mp3"rphjb_StoriaBambiniBiondi.mp3",
      vid"rphjb_StoriaBambiniBiondi2.mp4",
      mp3"rphjb_StoriaBambiniBiondi2.mp3",
      vid"rphjb_CuginiCampagnaGiardinoSempliciHomoSapiensLiscioRumbaChaChaChaCanzoneNapoletanaOsanna.mp4",
      vid"rphjb_MusicaNapoletanaRockLista.mp4",
      mp3"rphjb_MusicaNapoletanaRockLista.mp3"
    ),
    ReplyBundleMessage.textToMedia[F](
      "tony esposito",
      "bambini biondi"
    )(
      vid"rphjb_StoriaBambiniBiondi.mp4",
      mp3"rphjb_StoriaBambiniBiondi.mp3",
      vid"rphjb_StoriaBambiniBiondi2.mp4",
      mp3"rphjb_StoriaBambiniBiondi2.mp3"
    ),
    ReplyBundleMessage.textToMedia[F](
      "balletto di bronzo"
    )(
      vid"rphjb_MusicaNapoletanaRockLista.mp4",
      mp3"rphjb_MusicaNapoletanaRockLista.mp3",
      vid"rphjb_StoriaBambiniBiondi.mp4",
      mp3"rphjb_StoriaBambiniBiondi.mp3",
      vid"rphjb_StoriaBambiniBiondi2.mp4",
      mp3"rphjb_StoriaBambiniBiondi2.mp3"
    ),
    ReplyBundleMessage.textToMedia[F](
      "commesso viaggiatore",
      "bambina in mutande"
    )(
      vid"rphjb_StoriaNapoliBambinaMutande.mp4",
      mp3"rphjb_StoriaNapoliBambinaMutande.mp3"
    ),
    ReplyBundleMessage.textToMedia[F](
      "ma è tutto qui?",
      "vomitaste",
      "rantolando",
      "l'ultima delle bestie",
      "in cerca di cibo"
    )(
      gif"rphjb_VomitasteAnimaGif.mp4",
      vid"rphjb_VomitasteAnima.mp4",
      mp3"rphjb_VomitasteAnima.mp3"
    ),
    ReplyBundleMessage.textToMedia[F](
      "\\banima\\b".r.tr(5)
    )(
      gif"rphjb_VomitasteAnimaGif.mp4",
      vid"rphjb_VomitasteAnima.mp4",
      mp3"rphjb_VomitasteAnima.mp3",
      vid"rphjb_AnimaGarageVarazze.mp4",
      gif"rphjb_ColpirannoAnimaGif.mp4",
      vid"rphjb_ColpirannoAnima.mp4",
      mp3"rphjb_ColpirannoAnima.mp3"
    ),
    ReplyBundleMessage.textToMedia[F](
      "benissimo e malissimo",
      "la cosa migliore"
    )(
      gif"rphjb_BenissimoEMalissimoGif.mp4",
      vid"rphjb_BenissimoEMalissimo.mp4",
      mp3"rphjb_BenissimoEMalissimo.mp3"
    ),
    ReplyBundleMessage.textToMedia[F](
      "vi (assicuro|colpiranno)".r.tr(11),
      "in realtà non esiste"
    )(
      gif"rphjb_ColpirannoAnimaGif.mp4",
      vid"rphjb_ColpirannoAnima.mp4",
      mp3"rphjb_ColpirannoAnima.mp3"
    ),
    ReplyBundleMessage.textToMedia[F](
      "freddie mercury"
    )(
      vid"rphjb_NelRettoBrianMay.mp4",
      vid"rphjb_PappalardoGeorgeMichaelFreddyMercuryFrocio.mp4",
      mp3"rphjb_PappalardoGeorgeMichaelFreddyMercuryFrocio.mp3"
    ),
    ReplyBundleMessage.textToMedia[F](
      "brian may",
      "non è (uno )?stolto".r.tr(12),
      "spranga",
      "nel retto"
    )(
      vid"rphjb_NelRettoBrianMay.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      "mic drop",
      "microfono",
      "🎤",
      "🎙"
    )(
      gif"rphjb_MicDropGif.mp4",
      vid"rphjb_NelRettoBrianMay.mp4",
      gif"rphjb_RispondereGif.mp4",
      mp3"rphjb_Rispondere.mp3",
      vid"rphjb_Rispondere.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      "james senese",
      "elio d'anna"
    )(
      vid"rphjb_MusicaNapoletanaRockLista.mp4",
      mp3"rphjb_MusicaNapoletanaRockLista.mp3",
      vid"rphjb_CuginiCampagnaGiardinoSempliciHomoSapiensLiscioRumbaChaChaChaCanzoneNapoletanaOsanna.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      "merola",
      "murolo",
      "nino d'angelo",
      "bennato",
      "pino daniele",
      "gli showmen",
      "città frontale",
      "battitori selvaggi",
      "napoli centrale",
      "enzo (avitabile|granagnello)".r.tr(14),
      "joe amoruso",
      "ernesto vitolo",
      "tullio de piscopo"
    )(
      vid"rphjb_MusicaNapoletanaRockLista.mp4",
      mp3"rphjb_MusicaNapoletanaRockLista.mp3"
    ),
    ReplyBundleMessage.textToMedia[F](
      "il cervello"
    )(
      vid"rphjb_MusicaNapoletanaRockLista.mp4",
      mp3"rphjb_MusicaNapoletanaRockLista.mp3",
      vid"rphjb_CervelloSuperaLinguaListaGruppiAnni60.mp4",
      mp3"rphjb_CervelloSuperaLinguaListaGruppiAnni60.mp3",
      gif"rphjb_MioGenioGif.mp4",
      vid"rphjb_MioGenio.mp4",
      mp3"rphjb_MioGenio.mp3"
    ),
    ReplyBundleMessage.textToMedia[F](
      "la lingua"
    )(
      vid"rphjb_CervelloSuperaLinguaListaGruppiAnni60.mp4",
      mp3"rphjb_CervelloSuperaLinguaListaGruppiAnni60.mp3",
      vid"rphjb_ViSalutaLinguaSuDonnaGiusta.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      "errore allucinante",
      "arrovella",
      "camaleonti",
      "equipe (84|ottantaquattro)".r.tr(9),
      "the (sorrows|rocks)".r.tr(9),
      "i (balordi|bisonti|giganti|jaguars|profeti|bad boys|primitives)".r.tr(8),
      "ragazzi dai capelli verdi",
      "da polenta"
    )(
      vid"rphjb_CervelloSuperaLinguaListaGruppiAnni60.mp4",
      mp3"rphjb_CervelloSuperaLinguaListaGruppiAnni60.mp3"
    ),
    ReplyBundleMessage.textToMedia[F](
      "i (delfini|satelliti|nomadi)".r.tr(8)
    )(
      vid"rphjb_CervelloSuperaLinguaListaGruppiAnni60.mp4",
      mp3"rphjb_CervelloSuperaLinguaListaGruppiAnni60.mp3",
      vid"rphjb_ListaGruppi.mp4",
      mp3"rphjb_ListaGruppi.mp3"
    ),
    ReplyBundleMessage.textToMedia[F](
      "i (casuals|rocks|manfred mann|birds|buffalo springfield|dave clark five|soft (machine|heap)|gilgamesh|matching mole|magma|gong|headhunters|free spirits|return to forever)".r
        .tr(8),
      "robert wyatt",
      "univers zero",
      "etron fou leloublan",
      "herbie hancock",
      "larry coryell",
      "chick corea",
      "mahavishnu orchestra",
      "john mclaughlin",
      "billy cobham"
    )(
      vid"rphjb_ListaGruppi.mp4",
      mp3"rphjb_ListaGruppi.mp3"
    ),
    ReplyBundleMessage.textToMedia[F](
      "\\bfusion\\b".r.tr(6)
    )(
      mp3"rphjb_Tastieristi.mp3",
      vid"rphjb_Tastieristi.mp4",
      vid"rphjb_CervelloSuperaLinguaListaGruppiAnni60.mp4",
      mp3"rphjb_CervelloSuperaLinguaListaGruppiAnni60.mp3",
      mp3"rphjb_DoveStannoGliAppassionatiTanteMusiche.mp3",
      vid"rphjb_DoveStannoGliAppassionatiTanteMusiche.mp4"
    ),
    ReplyBundleMessage.textToMedia[F]("progressiv[oe]".r.tr(11))(
      gif"rphjb_MusicaEtichetteWhiskeyGif.mp4",
      vid"rphjb_MusicaEtichetteWhiskey.mp4",
      mp3"rphjb_MusicaEtichetteWhiskey.mp3",
      vid"rphjb_Regressive.mp4",
      mp3"rphjb_DoveStannoGliAppassionatiTanteMusiche.mp3",
      vid"rphjb_DoveStannoGliAppassionatiTanteMusiche.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      "blues"
    )(
      gif"rphjb_MusicaEtichetteWhiskeyGif.mp4",
      vid"rphjb_MusicaEtichetteWhiskey.mp4",
      mp3"rphjb_MusicaEtichetteWhiskey.mp3",
      vid"rphjb_Blues.mp4",
      vid"rphjb_BluesEbbeBambinoRockNRollBeBopALulaStormyMondayButTuesdayIsJustAsBad.mp4",
      vid"rphjb_CervelloSuperaLinguaListaGruppiAnni60.mp4",
      mp3"rphjb_CervelloSuperaLinguaListaGruppiAnni60.mp3"
    ),
    ReplyBundleMessage.textToMedia[F](
      "whiskey"
    )(
      gif"rphjb_MusicaEtichetteWhiskeyGif.mp4",
      vid"rphjb_MusicaEtichetteWhiskey.mp4",
      mp3"rphjb_MusicaEtichetteWhiskey.mp3",
      vid"rphjb_EtichetteSulleBottiglieDiWhiskeyAlpheus.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      "etichette"
    )(
      gif"rphjb_MusicaEtichetteWhiskeyGif.mp4",
      vid"rphjb_MusicaEtichetteWhiskey.mp4",
      mp3"rphjb_MusicaEtichetteWhiskey.mp3",
      vid"rphjb_EtichetteSupermercatoSputatiMondo.mp4",
      vid"rphjb_EtichetteSulleBottiglieDiWhiskeyAlpheus.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      "musica (sinfonica|acustica)".r.tr(15)
    )(
      gif"rphjb_MusicaEtichetteWhiskeyGif.mp4",
      vid"rphjb_MusicaEtichetteWhiskey.mp4",
      mp3"rphjb_MusicaEtichetteWhiskey.mp3",
      mp3"rphjb_DoveStannoGliAppassionatiTanteMusiche.mp3",
      vid"rphjb_DoveStannoGliAppassionatiTanteMusiche.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      "jazz"
    )(
      gif"rphjb_MusicaEtichetteWhiskeyGif.mp4",
      vid"rphjb_MusicaEtichetteWhiskey.mp4",
      mp3"rphjb_MusicaEtichetteWhiskey.mp3",
      vid"rphjb_CervelloSuperaLinguaListaGruppiAnni60.mp4",
      mp3"rphjb_CervelloSuperaLinguaListaGruppiAnni60.mp3",
      mp3"rphjb_Tastieristi.mp3",
      vid"rphjb_Tastieristi.mp4",
      mp3"rphjb_DoveStannoGliAppassionatiTanteMusiche.mp3",
      vid"rphjb_DoveStannoGliAppassionatiTanteMusiche.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      "consideravo degli amici",
      "inchinatevi",
      "che avete fatto voi?",
      "passaggio terreno",
      "criticare",
      "trasportatori",
      "doganieri"
    )(
      vid"rphjb_ExAmiciReMetalloTrasportatori.mp4",
      mp3"rphjb_ExAmiciReMetalloTrasportatori.mp3"
    ),
    ReplyBundleMessage.textToMedia[F](
      "tempo libero",
      "flotta navale",
      "(secondo|doppio) lavoro".r.tr(13),
      "gioco infernale"
    )(
      vid"rphjb_CapitanoMarinaMilitareSecondoLavoro.mp4",
      mp3"rphjb_CapitanoMarinaMilitareSecondoLavoro.mp3"
    ),
    ReplyBundleMessage.textToMedia[F](
      "su youtube"
    )(
      mp3"rphjb_EsperimentoSuYoutube.mp3",
      gif"rphjb_EsperimentoSuYoutubeGif.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      "co(gl|j)ione([- ]co(gl|j)ione)+"
    )(
      vid"rphjb_FrocioCoglione.mp4",
      mp3"rphjb_FrocioCoglione.mp3"
    ),
    ReplyBundleMessage.textToMedia[F](
      "sconvolto"
    )(
      gif"rphjb_SconvoltoGif.mp4",
      vid"rphjb_Sconvolto.mp4",
      mp3"rphjb_Sconvolto.mp3"
    ),
    ReplyBundleMessage.textToMedia[F](
      "pi[uù] (siete|sono) felici".r.tr(16)
    )(
      gif"rphjb_PiuIncazzatoPiuFeliciMortacciVostriGif.mp4",
      vid"rphjb_PiuIncazzatoPiuFeliciMortacciVostri.mp4",
      mp3"rphjb_PiuIncazzatoPiuFeliciMortacciVostri.mp3",
      gif"rphjb_IncazzatoFeliciGif.mp4",
      vid"rphjb_IncazzatoFelici.mp4",
      mp3"rphjb_IncazzatoFelici.mp3"
    ),
    ReplyBundleMessage.textToMedia[F](
      "incazzat"
    )(
      gif"rphjb_PiuIncazzatoPiuFeliciMortacciVostriGif.mp4",
      vid"rphjb_PiuIncazzatoPiuFeliciMortacciVostri.mp4",
      mp3"rphjb_PiuIncazzatoPiuFeliciMortacciVostri.mp3",
      vid"rphjb_PiuPacatoMiIncazzo.mp4",
      gif"rphjb_PiuPacatoMiIncazzoGif.mp4",
      mp3"rphjb_PiuPacatoMiIncazzo.mp3",
      gif"rphjb_IncazzatoFeliciGif.mp4",
      vid"rphjb_IncazzatoFelici.mp4",
      mp3"rphjb_IncazzatoFelici.mp3"
    ),
    ReplyBundleMessage.textToMedia[F](
      "w[e]+[l]+[a]+".r.tr(4)
    )(
      vid"rphjb_WelaMyFriends.mp4",
      vid"rphjb_WelaHeyHeyHeyDiNuovoInsieme.mp4",
      gif"rphjb_WelaCiaoSonoRichardBensonGif.mp4",
      vid"rphjb_WelaCiaoSonoRichardBenson.mp4",
      mp3"rphjb_WelaCiaoSonoRichardBenson.mp3"
    ),
    ReplyBundleMessage.textToMedia[F](
      "richard (philip henry john )?benson".r.tr(14)
    )(
      gif"rphjb_WelaCiaoSonoRichardBensonGif.mp4",
      vid"rphjb_WelaCiaoSonoRichardBenson.mp4",
      mp3"rphjb_WelaCiaoSonoRichardBenson.mp3",
      mp3"rphjb_PassaportoRiccardoBenzoni.mp3",
      mp3"rphjb_FotoDocumentoCheComprova.mp3",
      vid"rphjb_FotoDocumentoCheComprova.mp4",
      gif"rphjb_FotoDocumentoCheComprovaGif.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      "\\bmetal\\b".r.tr(5)
    )(
      gif"rphjb_MetalGif.mp4",
      gif"rphjb_IlMartelGif.mp4",
      vid"rphjb_SonyVaMaleMetalRock.mp4",
      mp3"rphjb_SonyVaMaleMetalRock.mp3",
      mp3"rphjb_DoveStannoGliAppassionatiTanteMusiche.mp3",
      vid"rphjb_DoveStannoGliAppassionatiTanteMusiche.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      "sony",
      "\\bbond[s]?\\b".r.tr(4),
      "\\bazion(i|ario)\\b".r.tr(6),
      "obbligazioni",
      "in tutti i campi",
      "va male",
      "hanno chiamato me"
    )(
      vid"rphjb_SonyVaMaleMetalRock.mp4",
      mp3"rphjb_SonyVaMaleMetalRock.mp3"
    ),
    ReplyBundleMessage.textToMedia[F](
      "desertici"
    )(
      vid"rphjb_IlBastoneDiGesu.mp4",
      mp3"rphjb_IlBastoneDiGesu.mp3"
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
      "\\bvino\\b".r.tr(4)
    )(
      mp3"rphjb_ChitarraZuccheroAlgheVino.mp3",
      gif"rphjb_ConseguenzeDellaPasquaGif.mp4",
      mp3"rphjb_ConseguenzeDellaPasqua.mp3",
      vid"rphjb_ConseguenzeDellaPasqua.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      "chi tocca \\w+(,|...)?[ ]?muore".r.tr(16),
      "ciao (2001|duemilauno)".r.tr(9)
    )(
      vid"rphjb_Ciao2001FarsaManson.mp4",
      vid"rphjb_Ciao2001.mp4",
      mp3"rphjb_Ciao2001.mp3",
      gif"rphjb_Ciao2001Gif.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      "alle (ore )?(22|ventidue)".r.tr(7)
    )(
      mp3"rphjb_Alle22.mp3",
      vid"rphjb_VenerdiAppuntamentoFissoFica.mp4",
      vid"rphjb_Alle22MercolediTelevita.mp4",
      vid"rphjb_UltimoListaUmaniVenerdi22.mp4",
      vid"rphjb_SteveVai.mp4",
      mp3"rphjb_SteveVai.mp3",
      mp3"rphjb_Yodle.mp3",
      vid"rphjb_Yodle.mp4",
      gif"rphjb_SteveVaiGif.mp4",
      vid"rphjb_Alle22MercolediTelevita.mp4",
      mp3"rphjb_Alle22MercolediTelevita.mp3",
      gif"rphjb_CiSaroSempreIoDentroDiVoiEVoiDentroDiMeGif.mp4",
      vid"rphjb_CiSaroSempreIoDentroDiVoiEVoiDentroDiMe.mp4",
      mp3"rphjb_CiSaroSempreIoDentroDiVoiEVoiDentroDiMe.mp3"
    ),
    ReplyBundleMessage.textToMedia[F](
      "appuntamento"
    )(
      mp3"rphjb_Appuntamento.mp3",
      vid"rphjb_VenerdiAppuntamentoFissoFica.mp4",
      vid"rphjb_RicordateviAppuntamento.mp4",
      vid"rphjb_ImpegniListaCitta.mp4",
      mp3"rphjb_ImpegniListaCitta.mp3"
    ),
    ReplyBundleMessage.textToMedia[F](
      "succh",
      "olio di croce"
    )(
      gif"rphjb_OlioDiCroceGif.mp4",
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
      gif"rphjb_DareFastidioGif.mp4",
      vid"rphjb_Regressive.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      "regressive"
    )(
      gif"rphjb_RegressiveGif.mp4",
      vid"rphjb_Regressive.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      "rotto il cazzo"
    )(
      vid"rphjb_EBastaETuHaiRottoIlCazzo.mp4",
      vid"rphjb_BastaRottoIlCazzo.mp4",
      vid"rphjb_RottoIlCazzoUltimi.mp4",
      gif"rphjb_RottoIlCazzoGif.mp4",
      vid"rphjb_RottoIlCazzo.mp4",
      mp3"rphjb_RottoIlCazzo.mp3"
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
      vid"rphjb_PrimoSbaglio.mp4"
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
      mp3"rphjb_PoesiaNatalizia.mp3",
      mp3"rphjb_PoesiaDolcezzaViolenta.mp3",
      vid"rphjb_PoesiaDolcezzaViolenta.mp4",
      mp3"rphjb_PoesiaStrappareUnaLacrima.mp3",
      vid"rphjb_PoesiaStrappareUnaLacrima.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      "merry christmas"
    )(
      vid"rphjb_AuguriDiNatale.mp4",
      mp3"rphjb_RockChristmasHappyNewYear.mp3",
      vid"rphjb_AuguriDiNataleCapodannoFeste.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      "natale"
    )(
      vid"rphjb_AuguriDiNatale.mp4",
      mp3"rphjb_RockChristmasHappyNewYear.mp3",
      vid"rphjb_AuguriDiNataleCapodannoFeste.mp4",
      vid"rphjb_RifiutatoLavorareStoriaMusicista.mp4",
      mp3"rphjb_RifiutatoLavorareStoriaMusicista.mp3"
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
      "happy new year"
    )(
      mp3"rphjb_RockChristmasHappyNewYear.mp3",
      vid"rphjb_AuguriDiNataleCapodannoFeste.mp4",
      vid"rphjb_PassatoAnnoVitaContinua.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      "capodanno"
    )(
      mp3"rphjb_RockChristmasHappyNewYear.mp3",
      vid"rphjb_AuguriDiNataleCapodannoFeste.mp4",
      vid"rphjb_PassatoAnnoVitaContinua.mp4",
      mp3"rphjb_RifiutatoLavorareStoriaMusicista.mp3",
      vid"rphjb_RifiutatoLavorareStoriaMusicista.mp4"
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
      "pier[sc]ing".r.tr(8),
      "mi fai male",
      "in mezzo alle gambe",
      "proprio[ ]?[l]+a".r.tr(9),
      "dove lo devo infil[aà]".r.tr(19)
    )(
      vid"rphjb_CambiataTuttaPiercingPropriolla.mp4",
      mp3"rphjb_CambiataTuttaPiercingPropriolla.mp3",
      gif"rphjb_CambiataTuttaPiercingPropriollaGif.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      "luca di noia",
      "alla regia",
      "regista"
    )(
      mp3"rphjb_LucaDiNoia.mp3",
      mp3"rphjb_LucaDiNoia2.mp3",
      vid"rphjb_LucaDiNoia3.mp4",
      vid"rphjb_LucaDiNoia4.mp4",
      mp3"rphjb_MiPareLogicoMatematiciAnaliticiDiNoia.mp3",
      vid"rphjb_MiPareLogicoMatematiciAnaliticiDiNoia.mp4",
      gif"rphjb_MiPareLogicoMatematiciAnaliticiDiNoiaGif.mp4",
      mp3"rphjb_GrandeRegistaLucaDiNoia.mp3",
      vid"rphjb_GrandeRegistaLucaDiNoia.mp4",
      vid"rphjb_LucaDiNoiaGrandeRegista.mp4",
      vid"rphjb_LucaDiNoiaRegia.mp4",
      vid"rphjb_GrandeRegistaLucaDiNoia2.mp4",
      mp3"rphjb_GrandeRegistaLucaDiNoia2.mp3",
      gif"rphjb_GrandeRegistaLucaDiNoia2Gif.mp4",
      vid"rphjb_DotiTecnicheIngegneristicheDiNoiaCaramelline.mp4",
      mp3"rphjb_DotiTecnicheIngegneristicheDiNoiaCaramelline.mp3",
      vid"rphjb_Alle22MercolediTelevita.mp4",
      mp3"rphjb_Alle22MercolediTelevita.mp3",
      mp3"rphjb_MenteSuperioreInferioreLucaDiNoia.mp3",
      vid"rphjb_MenteSuperioreInferioreLucaDiNoia.mp4",
      gif"rphjb_LucaDiNoiaControDemonioGif.mp4",
      vid"rphjb_LucaDiNoiaControDemonio.mp4",
      mp3"rphjb_LucaDiNoiaControDemonio.mp3"
    ),
    ReplyBundleMessage.textToMedia[F](
      "angelo",
      "carpenelli",
      "via delle albizzie",
      "istinti musicali"
    )(
      mp3"rphjb_IstintiMusicaliIlPuntoDArrivo.mp3",
      vid"rphjb_IstintiMusicaliIlPuntoDArrivo.mp4",
      gif"rphjb_IstintiMusicaliIlPuntoDArrivoGif.mp4",
      vid"rphjb_AngeloTrovamelo.mp4",
      vid"rphjb_AlbizziePerlaPioggia.mp4",
      mp3"rphjb_IstintiMusicali.mp3",
      vid"rphjb_IstintiMusicali.mp4",
      vid"rphjb_GrandeAngelo.mp4",
      vid"rphjb_AngeloCarpenelliGrandeViaDelleAlbizzie22NumeroUnoImmensoInGinocchio.mp4",
      vid"rphjb_AngeloCarpenelliViaDelleAlbizzie22IstintiMusicali.mp4",
      vid"rphjb_VieSonoTanteMilioniDiMilioniMiCoglioniViaDelleAlbizzie22.mp4",
      mp3"rphjb_Avremo18AnniLong.mp3",
      vid"rphjb_Avremo18AnniLong.mp4",
      vid"rphjb_PellegrinaggioSimposioMetallo.mp4",
      vid"rphjb_AngeloRimediamelo.mp4",
      mp3"rphjb_AngeloRimediamelo.mp3",
      mp3"rphjb_SeNonAndateDalCarpenelliViTrucido.mp3",
      vid"rphjb_SeNonAndateDalCarpenelliViTrucido.mp4",
      vid"rphjb_RingraziareAngelo.mp4",
      mp3"rphjb_RingraziareAngelo.mp3",
      vid"rphjb_AngeloCarpenelliArrivaALui.mp4",
      mp3"rphjb_AngeloCarpenelliArrivaALui.mp3",
      gif"rphjb_AngeloHaVintoSuTuttiGif.mp4",
      vid"rphjb_AngeloHaVintoSuTutti.mp4",
      mp3"rphjb_AngeloHaVintoSuTutti.mp3",
      vid"rphjb_BraccioDestroAngelo.mp4",
      mp3"rphjb_BraccioDestroAngelo.mp3",
      gif"rphjb_BraccioDestroAngeloGif.mp4"
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
      vid"rphjb_OcchiAnniSettantaFemmismoControcultura.mp4",
      mp3"rphjb_OcchiAnniSettantaFemmismoControcultura.mp3",
      gif"rphjb_OcchiDonneAnniSettantaGif.mp4",
      gif"rphjb_Ester2Gif.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      "tu non mi conosci",
      "posso cambiare",
      "sono camaleontico",
      "sputarti in faccia"
    )(
      vid"rphjb_SputartiInFacciaCamaleontico.mp4",
      mp3"rphjb_SputartiInFacciaCamaleontico.mp3",
      gif"rphjb_SputartiInFacciaCamaleonticoGif.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      "madre tortura"
    )(
      vid"rphjb_MadreTorturaParrucca.mp4",
      mp3"rphjb_MadreTorturaImprovvisata.mp3"
    ),
    ReplyBundleMessage.textToMedia[F](
      "\\bpa[r]+u[c]+a\\b".r.tr(8)
    )(
      vid"rphjb_MadreTorturaParrucca.mp4",
      mp3"rphjb_MadreTorturaImprovvisata.mp3",
      gif"rphjb_IncazzatoFeliciGif.mp4",
      vid"rphjb_IncazzatoFelici.mp4",
      mp3"rphjb_IncazzatoFelici.mp3"
    ),
    ReplyBundleMessage.textToMedia[F](
      "cazzate"
    )(
      gif"rphjb_NonPossibileGif.mp4",
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
      "vengognati"
    )(
      mp3"rphjb_VergognatiMatosFalasci.mp3",
      vid"rphjb_VergognatiMatosFalasci.mp4",
      gif"rphjb_VergognatiMatosFalasciGif.mp4",
      vid"rphjb_VergognatiMancusiPaoloBiancoTastieristaAttentiPerStradaAndreaCarpi.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      "andre matos"
    )(
      mp3"rphjb_AndreMatosShaman.mp3",
      vid"rphjb_AndreMatosShaman.mp4",
      gif"rphjb_AndreMatosShamanGif.mp4",
      mp3"rphjb_VergognatiMatosFalasci.mp3",
      vid"rphjb_VergognatiMatosFalasci.mp4",
      gif"rphjb_VergognatiMatosFalasciGif.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      "edu falasci",
      "edoardo falaschi"
    )(
      mp3"rphjb_EduFalasci.mp3",
      mp3"rphjb_VergognatiMatosFalasci.mp3",
      vid"rphjb_VergognatiMatosFalasci.mp4",
      gif"rphjb_VergognatiMatosFalasciGif.mp4",
      vid"rphjb_EduFalasciQuasiFrocio.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      "shaman"
    )(
      mp3"rphjb_AndreMatosShaman.mp3",
      vid"rphjb_AndreMatosShaman.mp4",
      gif"rphjb_AndreMatosShamanGif.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      "assolutamente no",
      "non mi lamento"
    )(
      gif"rphjb_NonMiLamentoGif.mp4",
      vid"rphjb_NonMiLamento.mp4"
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
      mp3"rphjb_DifficoltaAmicizieTelefonata.mp3",
      vid"rphjb_DifficoltaAmicizieTelefonata.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      "in un attimo",
      "risolto tutto"
    )(
      gif"rphjb_TelefonataGif.mp4",
      gif"rphjb_DifficoltaAmicizieTelefonataGif.mp4",
      vid"rphjb_DifficoltaAmicizieTelefonata.mp4",
      mp3"rphjb_DifficoltaAmicizieTelefonata.mp3"
    ),
    ReplyBundleMessage.textToMedia[F](
      "telefonata"
    )(
      gif"rphjb_TelefonataPilotataGif.mp4",
      gif"rphjb_TelefonataGif.mp4",
      gif"rphjb_DifficoltaAmicizieTelefonataGif.mp4",
      vid"rphjb_DifficoltaAmicizieTelefonata.mp4",
      mp3"rphjb_DifficoltaAmicizieTelefonata.mp3",
      vid"rphjb_TelefonataPappalardoFanculo.mp4",
      vid"rphjb_TelefonataInLinea.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      "nudo([ -]nudo)+".r.tr(4)
    )(
      mp3"rphjb_NudoFrocio.mp3",
      vid"rphjb_NudoNudo.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      "ragazza indemoniata"
    )(
      gif"rphjb_LaRagazzaIndemoniataGif.mp4",
      vid"rphjb_LaRagazzaIndemoniata.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      "non manca niente",
      "c'è tutto"
    )(
      gif"rphjb_CeTuttoNonMancaNienteGif.mp4",
      vid"rphjb_CeTuttoNonMancaNiente.mp4",
      mp3"rphjb_CeTuttoNonMancaNiente.mp3"
    ),
    ReplyBundleMessage.textToMedia[F](
      "un avvertimento",
      "bastoni tra le ruote"
    )(
      gif"rphjb_Ciao2001Gif.mp4",
      vid"rphjb_Ciao2001.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      "fro(ci|sh)o([ -]fro(ci|sh)o)+".r.tr(5)
    )(
      mp3"rphjb_NudoFrocio.mp3",
      vid"rphjb_FrocioFrocio.mp4",
      vid"rphjb_FrocioFrocio2.mp4",
      mp3"rphjb_FrocioFrocio.mp3",
      vid"rphjb_FrocioCoglione.mp4",
      mp3"rphjb_FrocioCoglione.mp3"
    ),
    ReplyBundleMessage.textToMedia[F](
      "magagna",
      "fregatura",
      "salva la situazione"
    )(
      mp3"rphjb_CapitoCheMagagnaKikoLoureiroSalvaSituazione.mp3",
      vid"rphjb_CapitoCheMagagnaKikoLoureiroSalvaSituazione.mp4",
      gif"rphjb_CapitoCheMagagnaKikoLoureiroSalvaSituazioneGif.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      "kiko loureiro"
    )(
      mp3"rphjb_CapitoCheMagagnaKikoLoureiroSalvaSituazione.mp3",
      vid"rphjb_CapitoCheMagagnaKikoLoureiroSalvaSituazione.mp4",
      gif"rphjb_CapitoCheMagagnaKikoLoureiroSalvaSituazioneGif.mp4",
      mp3"rphjb_TracciaNuoveStradeKikoLoureiro.mp3",
      gif"rphjb_TracciaNuoveStradeKikoLoureiroGif.mp4",
      vid"rphjb_TracciaNuoveStradeKikoLoureiro.mp4"
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
      vid"rphjb_BruttoSquallidoSchifoso.mp4",
      mp3"rphjb_BruttoSquallidoSchifoso.mp3",
      gif"rphjb_BruttoSquallidoSchifosoGif.mp4",
      vid"rphjb_UltimoListaUmaniVenerdi22.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      "quattro solo",
      "faccio in tempo"
    )(
      vid"rphjb_4SoloTempiInTestaOrologiSfuggonoPolsi.mp4",
      gif"rphjb_4SoloTempiInTestaOrologiSfuggonoPolsiGif.mp4",
      mp3"rphjb_4SoloTempiInTestaOrologiSfuggonoPolsi.mp3"
    ),
    ReplyBundleMessage.textToMedia[F](
      "televita"
    )(
      mp3"rphjb_Yodle.mp3",
      vid"rphjb_Yodle.mp4",
      mp3"rphjb_TelevitaSonoInizioRisata.mp3",
      vid"rphjb_Alle22MercolediTelevita.mp4",
      vid"rphjb_EmuleReteInternettariaSitoScoppia.mp4",
      mp3"rphjb_EmuleReteInternettariaSitoScoppia.mp3",
      gif"rphjb_AccompagnaviRitardoFiguracciaGif.mp4",
      vid"rphjb_AccompagnaviRitardoFiguraccia.mp4",
      mp3"rphjb_AccompagnaviRitardoFiguraccia.mp3",
      vid"rphjb_OttavaNotaRobaVecchiaSchifosi.mp4",
      mp3"rphjb_OttavaNotaRobaVecchiaSchifosi.mp3",
      vid"rphjb_ImpegniListaCitta.mp4",
      mp3"rphjb_ImpegniListaCitta.mp3",
      gif"rphjb_VolumeAlMassimoGif.mp4",
      vid"rphjb_VolumeAlMassimo.mp4",
      mp3"rphjb_VolumeAlMassimo.mp3"
    ),
    ReplyBundleMessage.textToMedia[F](
      "mercoled[iì]".r.tr(9)
    )(
      mp3"rphjb_Yodle.mp3",
      vid"rphjb_Yodle.mp4",
      mp3"rphjb_DueTrasmissioniColtelliBastoneInfernale.mp3",
      vid"rphjb_DueTrasmissioniColtelliBastoneInfernale.mp4",
      vid"rphjb_TempoAlTempo.mp4",
      vid"rphjb_Alle22MercolediTelevita.mp4",
      mp3"rphjb_FineSettimanaMercolediInizio.mp3",
      vid"rphjb_FineSettimanaMercolediInizio.mp4",
      gif"rphjb_FineSettimanaMercolediInizioGif.mp4",
      vid"rphjb_Alle22MercolediTelevita.mp4",
      mp3"rphjb_Alle22MercolediTelevita.mp3"
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
      vid"rphjb_AssaporarePezzoMentaMandragolaFicoSacroIncensoBetullaCanforaSaporeStrano.mp4",
      vid"rphjb_FigureMitologicheLive.mp4",
      gif"rphjb_FigureMitologicheLiveGif.mp4",
      mp3"rphjb_FigureMitologicheLive.mp3"
    ),
    ReplyBundleMessage.textToMedia[F](
      "mandragola"
    )(
      mp3"rphjb_FigureMitologiche.mp3",
      vid"rphjb_FigureMitologiche.mp4",
      vid"rphjb_FigureMitologiche2.mp4",
      vid"rphjb_FigureMitologiche3.mp4",
      vid"rphjb_AssaporarePezzoMentaMandragolaFicoSacroIncensoBetullaCanforaSaporeStrano.mp4",
      vid"rphjb_Streghe.mp4",
      vid"rphjb_FigureMitologicheLive.mp4",
      gif"rphjb_FigureMitologicheLiveGif.mp4",
      mp3"rphjb_FigureMitologicheLive.mp3"
    ),
    ReplyBundleMessage.textToMedia[F](
      "assaporare",
      "incenso",
      "\\bmenta\\b".r.tr(5),
      "sapore (strano|indefinito)".r.tr(13)
    )(
      vid"rphjb_AssaporarePezzoMentaMandragolaFicoSacroIncensoBetullaCanforaSaporeStrano.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      "fai schifo",
      "sei l'ultimo"
    )(
      gif"rphjb_FaiSchifoSeiUltimoGif.mp4",
      vid"rphjb_FaiSchifoSeiUltimo.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      "graffi"
    )(
      gif"rphjb_GraffiGif.mp4",
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
      gif"rphjb_DelirioGif.mp4",
      vid"rphjb_DelirioDelSabatoSera.mp4",
      vid"rphjb_CarrellataInfernaleDelirioPureNellaGolaTrasmissioneDaUrloEchoDelayVitaNaturalDurante.mp4",
      gif"rphjb_UnaCosaSchifosaTerribileGif.mp4",
      vid"rphjb_UnaCosaSchifosaTerribile.mp4",
      mp3"rphjb_UnaCosaSchifosaTerribile.mp3"
    ),
    ReplyBundleMessage.textToMedia[F](
      "nella gola"
    )(
      gif"rphjb_NellaGolaGif.mp4",
      vid"rphjb_CarrellataInfernaleDelirioPureNellaGolaTrasmissioneDaUrloEchoDelayVitaNaturalDurante.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      "ma che (cazzo )?sto dicendo".r.tr(18)
    )(
      mp3"rphjb_MaCheCazzoStoDicendo.mp3",
      vid"rphjb_MaCheCazzoStoDicendo.mp4",
      gif"rphjb_MaCheCazzoStoDicendoGif.mp4",
      gif"rphjb_IlMartelGif.mp4",
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
      "maniera pazzesca",
      "altro che quel coglione"
    )(
      gif"rphjb_AffanculoManieraPazzescaGif.mp4",
      vid"rphjb_AffanculoManieraPazzesca.mp4",
      mp3"rphjb_AffanculoManieraPazzesca.mp3",
      sticker"rphjb_IndicaAffanculoPazzescaBensoniani.sticker"
    ),
    ReplyBundleMessage.textToMedia[F](
      "facendo soffrire"
    )(
      gif"rphjb_FacendoSoffrireGif.mp4",
      vid"rphjb_CosaCosaSuccessoMeNeVadoFacendoSoffrire.mp4"
    )
  )

  private def messageRepliesMixData3[F[_]: Applicative]: List[ReplyBundleMessage[F]] = List(
    ReplyBundleMessage.textToMedia[F](
      "\\bn[o]{2,}!\\b".r.tr(3)
    )(
      gif"rphjb_NoGif.mp4",
      vid"rphjb_FolliaQueenNo.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      "\\bport[a]+\\b".r.tr(5)
    )(
      gif"rphjb_PortaGif.mp4",
      vid"rphjb_NonApriteQuellaPorta.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      "a questo punto",
      "andiamo[ci]? a sentire".r.tr(18),
      "l'originale"
    )(
      gif"rphjb_SentireOriginaleGif.mp4",
      vid"rphjb_FeelingsIncazzarmiAndiamociSentireOriginale.mp4",
      gif"rphjb_FeelingsIncazzarmiAndiamociSentireOriginaleGif.mp4",
      mp3"rphjb_FeelingsIncazzarmiAndiamociSentireOriginale.mp3"
    ),
    ReplyBundleMessage.textToMedia[F](
      "gigi d'alessio"
    )(
      mp3"rphjb_GigiDAlessioAnnaTatangelo.mp3",
      vid"rphjb_GianniCelesteMeglioGigiDAlessio.mp4",
      vid"rphjb_MusicaNapoletanaRockLista.mp4",
      mp3"rphjb_MusicaNapoletanaRockLista.mp3",
      gif"rphjb_ViControlloDAlessioTatangeloFalsiMetallariGif.mp4",
      vid"rphjb_ViControlloDAlessioTatangeloFalsiMetallari.mp4",
      mp3"rphjb_ViControlloDAlessioTatangeloFalsiMetallari.mp3"
    ),
    ReplyBundleMessage.textToMedia[F](
      "gianni celeste"
    )(
      vid"rphjb_GianniCelesteMeglioGigiDAlessio.mp4",
      vid"rphjb_RapMusicaMelodicaListaCantanti.mp4",
      vid"rphjb_QuesitoRegaloOtelloProfazioMarioLanzaTullioPaneLucianoTaglioliGianniCeleste.mp4",
      vid"rphjb_PappalardoGeorgeMichaelFreddyMercuryFrocio.mp4",
      mp3"rphjb_PappalardoGeorgeMichaelFreddyMercuryFrocio.mp3",
      gif"rphjb_SolitoPremioGianniCelesteGif.mp4",
      vid"rphjb_SolitoPremioGianniCeleste.mp4",
      mp3"rphjb_SolitoPremioGianniCeleste.mp3"
    ),
    ReplyBundleMessage.textToMedia[F](
      "negri"
    )(
      mp3"rphjb_DueNegriMostruosi.mp3",
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
      gif"rphjb_DrogatiRockettari1Gif.mp4",
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
      gif"rphjb_DrogatiRockettari1Gif.mp4",
      vid"rphjb_VecchiAmiciAnni70VeranoSostanzeImproprieNonSonoMaiMorto.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      "a[ ]?[f]*fanculo(,)? per contesia".r.tr(20)
    )(
      gif"rphjb_FanculoPerCortesiaGif.mp4",
      vid"rphjb_DecidoIoMareCazzatePerCortesia.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      "capolavoro"
    )(
      mp3"rphjb_GuerraPiuTotale.mp3",
      gif"rphjb_GuerraPiuTotaleGif.mp4",
      vid"rphjb_GuerraPiuTotale.mp4",
      vid"rphjb_Capolavoro.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      "stillati"
    )(
      gif"rphjb_DrogatiRockettari1Gif.mp4",
      vid"rphjb_DrogatiRockettari.mp4",
      gif"rphjb_DrogatiRockettari2Gif.mp4",
      vid"rphjb_RocchettariCapelliCortiGiaccaCravattaPassaportoStronzi.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      "ro[ckgh]+(ch|gh|k)e(d+|t+)ari".r.tr(10)
    )(
      gif"rphjb_DrogatiRockettari1Gif.mp4",
      vid"rphjb_DrogatiRockettari.mp4",
      gif"rphjb_DrogatiRockettari2Gif.mp4",
      vid"rphjb_RocchettariCapelliCortiGiaccaCravattaPassaportoStronzi.mp4",
      vid"rphjb_RockettariComeBestieCravattaPassaportoStronzi.mp4",
      vid"rphjb_StoriaBambiniBiondi.mp4",
      mp3"rphjb_StoriaBambiniBiondi.mp3",
      vid"rphjb_StoriaBambiniBiondi2.mp4",
      mp3"rphjb_StoriaBambiniBiondi2.mp3"
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
      vid"rphjb_SoCazziVostriGuaioPureCazziMia.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      "spacco il culo"
    )(
      mp3"rphjb_ViSpaccoIlCulo.mp3",
      vid"rphjb_ViSpaccoIlCulo.mp4",
      vid"rphjb_FacevoSchifoOraSpacco.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      "altari",
      "realtà"
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
      "\\b(in)?colla\\b".r.tr(5),
      "\\bserp[ie]\\b".r.tr(5)
    )(
      vid"rphjb_CollaSerpe.mp4",
      mp3"rphjb_CollaSerpe.mp3",
      vid"rphjb_CollaSerpeSigarettePercussionista.mp4",
      vid"rphjb_FossaCollaSerpeSerpeFelicitaMusica.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      "battezz(ato|are)".r.tr(10),
      "battesimo"
    )(
      vid"rphjb_Blues.mp4",
      mp3"rphjb_Battesimo.mp3"
    ),
    ReplyBundleMessage.textToMedia[F](
      "stringere i denti"
    )(
      gif"rphjb_AndareAvantiGif.mp4",
      mp3"rphjb_AndareAvanti.mp3",
      vid"rphjb_AndareAvantiStringereIDenti.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      "si ostina",
      "foto vecchie"
    )(
      gif"rphjb_OstinaGif.mp4",
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
      gif"rphjb_WhereAreYouGoingGif.mp4",
      vid"rphjb_TommyAldridgeYngwieMalmsteenWhereAreYouGoing.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      "infern[a]+l[e]+[!]*".r.tr(9)
    )(
      vid"rphjb_Infernale.mp4",
      mp3"rphjb_Infernale.mp3",
      vid"rphjb_CarrellataInfernaleDelirioPureNellaGolaTrasmissioneDaUrloEchoDelayVitaNaturalDurante.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      "lunghezza d'onda",
      "brave persone"
    )(
      gif"rphjb_LunghezzaDOndaGif.mp4",
      vid"rphjb_GiudizioParolaFine.mp4",
      sticker"rphjb_LunghezzaDOndaBensoniani.sticker"
    ),
    ReplyBundleMessage.textToMedia[F](
      "korn"
    )(
      mp3"rphjb_Battesimo.mp3",
      vid"rphjb_ParlandoDeiKorn.mp4",
      vid"rphjb_PappalardoGeorgeMichaelFreddyMercuryFrocio.mp4",
      mp3"rphjb_PappalardoGeorgeMichaelFreddyMercuryFrocio.mp3"
    ),
    ReplyBundleMessage.textToMedia[F](
      "preghier"
    )(
      vid"rphjb_IlBastoneDiGesu.mp4",
      mp3"rphjb_IlBastoneDiGesu.mp3",
      gif"rphjb_FeelingsPreghieraGif.mp4",
      vid"rphjb_FeelingsPreghiera.mp4",
      mp3"rphjb_Chiesa.mp3",
      vid"rphjb_PoveriIllusiChiesaPreghierinaPreteManfrineDottoreMedicina.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      "io non credo",
      "la medicina",
      "andare dal dottore",
      "\\billusi\\b".r.tr(6)
    )(
      mp3"rphjb_Chiesa.mp3",
      vid"rphjb_PoveriIllusiChiesaPreghierinaPreteManfrineDottoreMedicina.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      "manfrin[ea]".r.tr(8)
    )(
      mp3"rphjb_Chiesa.mp3",
      vid"rphjb_PoveriIllusiChiesaPreghierinaPreteManfrineDottoreMedicina.mp4",
      vid"rphjb_VostraMenteAbbiettaCalpestataNoDirettiveEstremismoMafiaPoliticaPartitiStessaManfrina.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      "chiesa"
    )(
      mp3"rphjb_PoesiaNatalizia.mp3",
      mp3"rphjb_Chiesa.mp3",
      vid"rphjb_PoveriIllusiChiesaPreghierinaPreteManfrineDottoreMedicina.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      "sarete puniti"
    )(
      gif"rphjb_SaretePunitiGif.mp4",
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
      gif"rphjb_GuardiGif.mp4",
      vid"rphjb_PercheGuardiCosiManieraStrana.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      "hollywood"
    )(
      gif"rphjb_HollywoodGif.mp4",
      vid"rphjb_DaHollywood.mp4",
      mp3"rphjb_DaHollywood.mp3",
      sticker"rphjb_LavoraTuBensoniani.sticker"
    ),
    ReplyBundleMessage.textToMedia[F](
      "[cg]hi[td]a[r]+is[td][ai]".r.tr(10)
    )(
      gif"rphjb_ChitarristaGif.mp4",
      vid"rphjb_PeggioDelPeggio.mp4",
      vid"rphjb_GruppiNuoviSpaccanoCuloAbbellimentiRivoltiMordentiContrappunti.mp4",
      vid"rphjb_ChitarristiProsciuttoOrecchie.mp4",
      mp3"rphjb_ChitarristiProsciuttoOrecchie.mp3"
    ),
    ReplyBundleMessage.textToMedia[F](
      "sentendo male"
    )(
      gif"rphjb_MiStoSentendoMaleGif.mp4",
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
      gif"rphjb_FesteGif.mp4",
      vid"rphjb_FesteACasaNicolaArigliano.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      "\\burlo\\b".r.tr(4),
      "\\b[u]*[a]{5,}[h]*\\b".r.tr(5)
    )(
      mp3"rphjb_Tuffo.mp3",
      vid"rphjb_Tuffo.mp4",
      gif"rphjb_TuffoGif.mp4",
      gif"rphjb_TuffoReverseGif.mp4",
      gif"rphjb_UrloGif.mp4",
      gif"rphjb_Urlo2Gif.mp4",
      gif"rphjb_Urlo3Gif.mp4",
      gif"rphjb_Urlo4Gif.mp4",
      vid"rphjb_Urlo5.mp4",
      gif"rphjb_UrloCanaroGif.mp4",
      gif"rphjb_UrloRisoGif.mp4",
      vid"rphjb_UrloSignorGionz.mp4",
      vid"rphjb_FigureMitologicheLive.mp4",
      gif"rphjb_FigureMitologicheLiveGif.mp4",
      mp3"rphjb_FigureMitologicheLive.mp3",
      sticker"rphjb_Urlo1Bensoniani.sticker",
      sticker"rphjb_Urlo2Bensoniani.sticker",
      sticker"rphjb_Urlo3Bensoniani.sticker",
      sticker"rphjb_Urlo4Bensoniani.sticker"
    ),
    ReplyBundleMessage.textToMedia[F](
      "cantate",
      "arigliano"
    )(
      gif"rphjb_AriglianoGif.mp4",
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
      gif"rphjb_PregaGif.mp4",
      gif"rphjb_Prega2Gif.mp4",
      gif"rphjb_FeelingsPreghieraGif.mp4",
      vid"rphjb_FeelingsPreghiera.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      "sguardo"
    )(
      gif"rphjb_SguardoGif.mp4",
      gif"rphjb_Sguardo2Gif.mp4",
      gif"rphjb_ConfusoGif.mp4",
      gif"rphjb_Sguardo3Gif.mp4",
      gif"rphjb_Sguardo4Gif.mp4",
      gif"rphjb_SguardoCanaroGif.mp4",
      gif"rphjb_FeelingsSguardoGif.mp4",
      vid"rphjb_FeelingsSguardo.mp4",
      vid"rphjb_Messaggio.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      "non capisco"
    )(
      gif"rphjb_IlSensoCapitoGif.mp4",
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
      mp3"rphjb_LabbraTruccatriceNuovaUltimaDelleDonneViaZara.mp3",
      vid"rphjb_LabbraTruccatriceNuovaUltimaDelleDonneViaZara.mp4",
      gif"rphjb_LabbraTruccatriceNuovaUltimaDelleDonneViaZaraGif.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      "faccio la parte",
      " recit",
      " fing",
      "attrice",
      "\\ba[t]{2,}[o]+re\\b".r.tr(7)
    )(
      vid"rphjb_ParteDiRomeo.mp4",
      vid"rphjb_DaHollywood.mp4",
      mp3"rphjb_DaHollywood.mp3",
      gif"rphjb_FaccioLaParteGif.mp4",
      vid"rphjb_GaioInGiallo.mp4",
      vid"rphjb_GrandeMelGibsonRinunciaATutto.mp4",
      mp3"rphjb_GrandeMelGibsonRinunciaATutto.mp3"
    ),
    ReplyBundleMessage.textToMedia[F](
      "vi salut[ao]".r.tr(9)
    )(
      mp3"rphjb_ViSaluto.mp3",
      vid"rphjb_ViSalutaLinguaSuDonnaGiusta.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      "meridionale",
      "terron"
    )(
      vid"rphjb_GaioInGiallo.mp4",
      gif"rphjb_MeridionaleGif.mp4",
      mp3"rphjb_Meridionale.mp3",
      vid"rphjb_Meridionale.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      "destino",
      "incontrare",
      "signor benson"
    )(
      vid"rphjb_GaioInGiallo.mp4",
      sticker"rphjb_DestinoBensoniani.sticker",
      gif"rphjb_SignorBensonDestinoGif.mp4",
      mp3"rphjb_SignorBensonDestino.mp3",
      vid"rphjb_SignorBensonDestino.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      "tu( )?[ck]ul".r.tr(6)
    )(
      gif"rphjb_TukulGif.mp4",
      mp3"rphjb_Tukul.mp3",
      vid"rphjb_Tukul.mp4",
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
      vid"rphjb_MiSentoPezzoDiMerdaUltimoGiuGiu.mp4",
      gif"rphjb_MiSentoPezzoDiMerdaUltimoGiuGiuGif.mp4",
      mp3"rphjb_MiSentoPezzoDiMerdaUltimoGiuGiu.mp3"
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
          vid"rphjb_MiSentoPezzoDiMerdaUltimoGiuGiu.mp4",
          gif"rphjb_MiSentoPezzoDiMerdaUltimoGiuGiuGif.mp4",
          mp3"rphjb_MiSentoPezzoDiMerdaUltimoGiuGiu.mp3",
          vid"rphjb_UltimoListaUmaniVenerdi22.mp4"
        )
      ),
      matcher = MessageMatches.ContainsAll
    ),
    ReplyBundleMessage.textToMedia[F](
      "covi il male",
      "invidia",
      "livore"
    )(
      gif"rphjb_CoviMaleInvidiaLivoreGif.mp4",
      mp3"rphjb_CoviMaleInvidiiaLivore.mp3",
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
      gif"rphjb_ProprioAMeGif.mp4",
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
      mp3"rphjb_RitardoCasinoFuoriPolizia.mp3"
    ),
    ReplyBundleMessage.textToMedia[F](
      "colpa mia"
    )(
      vid"rphjb_TuttaColpaMia.mp4",
      vid"rphjb_RitardoCasinoFuoriPolizia.mp4",
      mp3"rphjb_RitardoCasinoFuoriPolizia.mp3",
      vid"rphjb_MiaColpaColpaMia.mp4",
      vid"rphjb_PienoDiDischiNovitaTempoInferioreSuperiore.mp4",
      mp3"rphjb_PienoDiDischiNovitaTempoInferioreSuperiore.mp3",
      gif"rphjb_PienoDiDischiNovitaTempoInferioreSuperioreGif.mp4",
      vid"rphjb_ImpegniListaCitta.mp4",
      mp3"rphjb_ImpegniListaCitta.mp3"
    ),
    ReplyBundleMessage.textToMedia[F](
      "suonato (abbastanza )?bene".r.tr(12)
    )(
      mp3"rphjb_SuonatoAbbastanzaBeneEVero.mp3",
      vid"rphjb_SuonatoAbbastanzaBeneManicoIntrisoZuccheroLiquidiSeminaliBirreAcqua.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      "fino alla fine"
    )(
      gif"rphjb_QuelloCheTiMeritiFinoAllaFineDistruttoTotalmenteGif.mp4",
      mp3"rphjb_QuelloCheTiMeritiFinoAllaFineDistruttoTotalmente.mp3",
      vid"rphjb_QuelloCheTiMeritiFinoAllaFineDistruttoTotalmente.mp4",
      vid"rphjb_FinoAllaFine.mp4",
      vid"rphjb_ParteDiRomeo.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      "cuore in mano",
      "mano nella mano",
      "pelle contro la pelle"
    )(
      gif"rphjb_CuoreInManoGif.mp4",
      gif"rphjb_CuoreInManoGif.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      "squallida"
    )(
      gif"rphjb_SquallidaGif.mp4",
      vid"rphjb_SquallidaScorfanoRaganaCatafalcoAmbulante.mp4",
      gif"rphjb_IlPrimoInCantinaGif.mp4",
      vid"rphjb_IlPrimoInCantina.mp4",
      mp3"rphjb_IlPrimoInCantina.mp3"
    ),
    ReplyBundleMessage.textToMedia[F](
      "musica tecnica",
      "carboni"
    )(
      mp3"rphjb_Rock.mp3",
      vid"rphjb_StorieSonoTanteConoscerePerParlareJovanottiAntonacciCarboniGrignaniAncheLaMerdaAvrebbeValore.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      "grignani"
    )(
      mp3"rphjb_Rock.mp3",
      vid"rphjb_StorieSonoTanteConoscerePerParlareJovanottiAntonacciCarboniGrignaniAncheLaMerdaAvrebbeValore.mp4",
      vid"rphjb_ManagerAmericanoGrignianiShit.mp4",
      mp3"rphjb_ManagerAmericanoGrignianiShit.mp3"
    ),
    ReplyBundleMessage.textToMedia[F](
      "conosce(nza|re)".r.tr(9),
      "veri valori"
    )(
      mp3"rphjb_Conoscere.mp3",
      vid"rphjb_StorieSonoTanteConoscerePerParlareJovanottiAntonacciCarboniGrignaniAncheLaMerdaAvrebbeValore.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      "il sapere"
    )(
      mp3"rphjb_Conoscere.mp3",
      vid"rphjb_StorieSonoTanteConoscerePerParlareJovanottiAntonacciCarboniGrignaniAncheLaMerdaAvrebbeValore.mp4",
      gif"rphjb_NonHoIlSapereQuelloCheNonSoGif.mp4",
      mp3"rphjb_NonHoIlSapereQuelloCheNonSo.mp3",
      vid"rphjb_NonHoIlSapereQuelloCheNonSo.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      "che( cazzo)? c'è da prendere".r.tr(19),
      "prend(o|ere) ((il|er) motorino|(il|er) coso|la macchina|l'auto)".r.tr(12),
      "\\bvengo\\b".r.tr(5),
      "non vengo\\b".r.tr(9)
    )(
      gif"rphjb_PrendoIlNecessarioGif.mp4",
      vid"rphjb_VengoNonVengoPrendoCosoAutoMacchinaMotorino.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      "non parlare",
      "non hai il diritto",
      "la trasmissione è la mia"
    )(
      mp3"rphjb_NonParlareTeTrasmissioneMia.mp3",
      vid"rphjb_NonParlareTeDirittoIoTrasmissioneMia.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      "pensa alla deficienza",
      "ma si può dire una cosa (del genere|così)".r.tr(28)
    )(
      gif"rphjb_DeficienzaGif.mp4",
      vid"rphjb_Deficienza.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      "incidente"
    )(
      mp3"rphjb_IncidentePonte.mp3",
      vid"rphjb_StoriaIncidenteMotoAmico.mp4",
      mp3"rphjb_StoriaIncidenteMotoAmico.mp3",
      mp3"rphjb_PoesiaStrappareUnaLacrima.mp3",
      vid"rphjb_PoesiaStrappareUnaLacrima.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      "\\bmoto\\b".r.tr(4),
      "cilindrata",
      "rincoglionito",
      "pronto soccorso",
      "non mi sono fatto niente",
      "una roba da poco",
      "a far[st]i vedere".r.tr(14),
      "il casco",
      "si apre in due"
    )(
      vid"rphjb_StoriaIncidenteMotoAmico.mp4",
      mp3"rphjb_StoriaIncidenteMotoAmico.mp3"
    ),
    ReplyBundleMessage.textToMedia[F](
      "rifiutato",
      "lavor(a|à|are) troppo".r.tr(13),
      "non (mi va di|ho voglia di|voglio) lavor(a|à|are)".r.tr(17),
      "andare a lavorare",
      "grande divertimento"
    )(
      mp3"rphjb_RifiutatoLavorareStoriaMusicista.mp3",
      vid"rphjb_RifiutatoLavorareStoriaMusicista.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      "cantanti"
    )(
      gif"rphjb_CantantiSerieZGif.mp4",
      vid"rphjb_PappalardoGeorgeMichaelFreddyMercuryFrocio.mp4",
      mp3"rphjb_PappalardoGeorgeMichaelFreddyMercuryFrocio.mp3",
      gif"rphjb_AllargareLeVeduteGif.mp4",
      mp3"rphjb_AllargareLeVedute.mp3",
      vid"rphjb_AllargareLeVedute.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      "dove ce lo metti",
      "george michael",
      "cesso pubblico",
      "sbaciucchia",
      "tutti nudi",
      "lo soffocano",
      "non riesce più a (parlà|cantà|respirà)".r.tr(22),
      "nazario saurio",
      "eddy napoli",
      "ray gelato",
      "toni santagata",
      "frate cionfoli",
      "papa roach",
      "\\bsauro\\b".r.tr(5),
      "\\bnazario\\b".r.tr(7),
      "rovinato (la vita|(pure )?il culo)".r.tr(16)
    )(
      vid"rphjb_PappalardoGeorgeMichaelFreddyMercuryFrocio.mp4",
      mp3"rphjb_PappalardoGeorgeMichaelFreddyMercuryFrocio.mp3"
    ),
    ReplyBundleMessage.textToMedia[F](
      "riccardo benzoni"
    )(
      mp3"rphjb_PassaportoRiccardoBenzoni.mp3",
      mp3"rphjb_FotoDocumentoCheComprova.mp3",
      vid"rphjb_FotoDocumentoCheComprova.mp4",
      gif"rphjb_FotoDocumentoCheComprovaGif.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      "e soprattutto",
      "foto del (mio )?documento".r.tr(18),
      "dopo quella foto",
      "documento ufficiale",
      "che comprova",
      "il mio nome è"
    )(
      mp3"rphjb_FotoDocumentoCheComprova.mp3",
      vid"rphjb_FotoDocumentoCheComprova.mp4",
      gif"rphjb_FotoDocumentoCheComprovaGif.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      "c'ho un cuore",
      "tra le palle",
      "dovrei ritrovarlo",
      "da qualche parte",
      "andato giù",
      "tirare (s[uù]|i fili)".r.tr(9)
    )(
      mp3"rphjb_CHoUnCuoreDovreiRitrovarloTirareIFili.mp3",
      vid"rphjb_CHoUnCuoreDovreiRitrovarloTirareIFili.mp4",
      gif"rphjb_CHoUnCuoreDovreiRitrovarloTirareIFiliGif.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      "dov'è andato a (finire|filare)".r.tr(21)
    )(
      vid"rphjb_RicercaGianni.mp4",
      mp3"rphjb_RicercaGianni.mp3",
      mp3"rphjb_CHoUnCuoreDovreiRitrovarloTirareIFili.mp3",
      vid"rphjb_CHoUnCuoreDovreiRitrovarloTirareIFili.mp4",
      gif"rphjb_CHoUnCuoreDovreiRitrovarloTirareIFiliGif.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      "futurista",
      "che (volevo|cerco) io".r.tr(12),
      "il (futuro|passato)".r.tr(9),
      "mi ha rotto"
    )(
      mp3"rphjb_FuturistaPassatoRotto.mp3",
      vid"rphjb_FuturistaPassatoRotto.mp4",
      gif"rphjb_FuturistaPassatoRottoGif.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      "ho un messaggio",
      "aspetta!",
      "non resisto più",
      "non so più resistere",
      "aprite le finestre",
      "buio spaventoso",
      "non ho mai pace",
      "supplizio",
      "martirio",
      "strapparmi (gli occhi|le bende)".r.tr(19),
      "nell'oscurità",
      "sempre vedo"
    )(
      mp3"rphjb_Messaggio.mp3",
      vid"rphjb_Messaggio.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      "ammazz(are|o) il tempo".r.tr(16)
    )(
      gif"rphjb_AmmazzareIlTempoGif.mp4",
      vid"rphjb_AmmazzareIlTempo.mp4",
      mp3"rphjb_AmmazzareIlTempo.mp3",
      vid"rphjb_PassaIlTempo.mp4",
      mp3"rphjb_PassaIlTempo.mp3",
      gif"rphjb_PassaIlTempoGif.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      "jovanotti",
      "lorenzo cherubini"
    )(
      vid"rphjb_JovanottiUltimo.mp4",
      mp3"rphjb_Rock.mp3",
      vid"rphjb_StorieSonoTanteConoscerePerParlareJovanottiAntonacciCarboniGrignaniAncheLaMerdaAvrebbeValore.mp4",
      mp3"rphjb_AntonacciJovanottiRamazzotti.mp3",
      vid"rphjb_AntonacciJovanottiRamazzotti.mp4",
      gif"rphjb_AntonacciJovanottiRamazzottiGif.mp4",
      gif"rphjb_NonHoPauraDiNessunoGif.mp4",
      vid"rphjb_NonHoPauraDiNessuno.mp4",
      mp3"rphjb_NonHoPauraDiNessuno.mp3"
    ),
    ReplyBundleMessage.textToMedia[F](
      "antonacci"
    )(
      mp3"rphjb_Rock.mp3",
      vid"rphjb_StorieSonoTanteConoscerePerParlareJovanottiAntonacciCarboniGrignaniAncheLaMerdaAvrebbeValore.mp4",
      mp3"rphjb_AntonacciJovanottiRamazzotti.mp3",
      vid"rphjb_AntonacciJovanottiRamazzotti.mp4",
      gif"rphjb_AntonacciJovanottiRamazzottiGif.mp4",
      gif"rphjb_NonHoPauraDiNessunoGif.mp4",
      vid"rphjb_NonHoPauraDiNessuno.mp4",
      mp3"rphjb_NonHoPauraDiNessuno.mp3"
    ),
    ReplyBundleMessage.textToMedia[F](
      "ramazzotti"
    )(
      vid"rphjb_SteveVaiRamazzotti.mp4",
      mp3"rphjb_AntonacciJovanottiRamazzotti.mp3",
      vid"rphjb_AntonacciJovanottiRamazzotti.mp4",
      gif"rphjb_AntonacciJovanottiRamazzottiGif.mp4",
      gif"rphjb_NonHoPauraDiNessunoGif.mp4",
      vid"rphjb_NonHoPauraDiNessuno.mp4",
      mp3"rphjb_NonHoPauraDiNessuno.mp3"
    ),
    ReplyBundleMessage.textToMedia[F](
      "tutti uguali"
    )(
      mp3"rphjb_AntonacciJovanottiRamazzotti.mp3",
      vid"rphjb_AntonacciJovanottiRamazzotti.mp4",
      gif"rphjb_AntonacciJovanottiRamazzottiGif.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      "cocktail micidiale",
      "ignoranza",
      "presunzione"
    )(
      gif"rphjb_CocktailMicidialeGif.mp4",
      vid"rphjb_CocktailMicidiale.mp4",
      mp3"rphjb_CocktailMicidiale.mp3",
      gif"rphjb_CocktailMicidiale2Gif.mp4",
      vid"rphjb_CocktailMicidiale2.mp4",
      mp3"rphjb_CocktailMicidiale2.mp3"
    ),
    ReplyBundleMessage.textToMedia[F](
      "sul palco",
      "da soli",
      "in (fondo|branco)".r.tr(8)
    )(
      gif"rphjb_InFondoInBrancoSulPalcoDaSoliGif.mp4",
      vid"rphjb_InFondoInBrancoSulPalcoDaSoli.mp4",
      mp3"rphjb_InFondoInBrancoSulPalcoDaSoli.mp3"
    ),
    ReplyBundleMessage.textToMedia[F](
      "stato mentale",
      "tante vite in una sola",
      "da quello che hai dentro"
    )(
      gif"rphjb_InFondoInBrancoSulPalcoDaSoliGif.mp4",
      vid"rphjb_InFondoInBrancoSulPalcoDaSoli.mp4",
      mp3"rphjb_InFondoInBrancoSulPalcoDaSoli.mp3"
    ),
    ReplyBundleMessage.textToMedia[F](
      "questo è matto",
      "l[uü]g[h]?er".r.tr(6)
    )(
      gif"rphjb_QuestoMatto6ColoreLugherGif.mp4",
      vid"rphjb_QuestoMatto6ColoreLugher.mp4",
      mp3"rphjb_QuestoMatto6ColoreLugher.mp3"
    ),
    ReplyBundleMessage.textToMedia[F](
      "di colore"
    )(
      gif"rphjb_QuestoMatto6ColoreLugherGif.mp4",
      vid"rphjb_QuestoMatto6ColoreLugher.mp4",
      mp3"rphjb_QuestoMatto6ColoreLugher.mp3",
      vid"rphjb_PasseggiataAgireSubito.mp4",
      mp3"rphjb_PasseggiataAgireSubito.mp3"
    ),
    ReplyBundleMessage.textToMedia[F](
      "colpo in testa",
      "aquila",
      "volatile",
      "tra i più"
    )(
      vid"rphjb_BeccoTraIPiu.mp4",
      mp3"rphjb_BeccoTraIPiu.mp3"
    ),
    ReplyBundleMessage.textToMedia[F](
      "\\bbecco\\b".r.tr(5)
    )(
      vid"rphjb_BeccoTraIPiu.mp4",
      mp3"rphjb_BeccoTraIPiu.mp3",
      gif"rphjb_BastoneInfernaleArtigianiBeccoMetalloGif.mp4",
      vid"rphjb_BastoneInfernaleArtigianiBeccoMetallo.mp4",
      mp3"rphjb_BastoneInfernaleArtigianiBeccoMetallo.mp3"
    ),
    ReplyBundleMessage.textToMedia[F](
      "da mario",
      "il liga",
      "ligabue",
      "amico intimo"
    )(
      vid"rphjb_AmicoIntimoTavernaDaMario.mp4",
      mp3"rphjb_AmicoIntimoTavernaDaMario.mp3"
    ),
    ReplyBundleMessage.textToMedia[F](
      "amico del cuore"
    )(
      vid"rphjb_AmicoIntimoTavernaDaMario.mp4",
      mp3"rphjb_AmicoIntimoTavernaDaMario.mp3",
      vid"rphjb_AmicoDelCuoreLasciatoNellaMerdaParoleSubliminaliPoesiaAmiciziaVera.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      "agire subito",
      "passeggiata",
      "ossigena i polmoni",
      "in sedia a rotelle",
      "pensa(re|to) (due volte|un secondo)".r.tr(17)
    )(
      vid"rphjb_PasseggiataAgireSubito.mp4",
      mp3"rphjb_PasseggiataAgireSubito.mp3"
    ),
    ReplyBundleMessage.textToMedia[F](
      "uomini pecora",
      "ogni minuto è importante"
    )(
      gif"rphjb_UominiPecoraGif.mp4",
      vid"rphjb_UominiPecora.mp4",
      mp3"rphjb_UominiPecora.mp3"
    ),
    ReplyBundleMessage.textToMedia[F](
      "non sopporto"
    )(
      gif"rphjb_UominiPecoraGif.mp4",
      vid"rphjb_UominiPecora.mp4",
      mp3"rphjb_UominiPecora.mp3",
      vid"rphjb_SquallidaScorfanoRaganaCatafalcoAmbulante.mp4",
      vid"rphjb_CoseCheNonSopportoCalcoliSbagliati.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      "emule"
    )(
      mp3"rphjb_Emule.mp3",
      vid"rphjb_Emule.mp4",
      gif"rphjb_EmuleGif.mp4",
      vid"rphjb_EmuleReteInternettariaSitoScoppia.mp4",
      mp3"rphjb_EmuleReteInternettariaSitoScoppia.mp3"
    ),
    ReplyBundleMessage.textToMedia[F](
      "milioni"
    )(
      mp3"rphjb_Milioni.mp3",
      vid"rphjb_Milioni.mp4",
      gif"rphjb_MilioniGif.mp4",
      mp3"rphjb_UndiciMilioni.mp3",
      vid"rphjb_UndiciMilioni.mp4",
      gif"rphjb_UndiciMilioniGif.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      "va curato",
      "aiutatelo"
    )(
      mp3"rphjb_TosseInvernaleAiutatelo.mp3",
      vid"rphjb_TosseInvernaleAiutatelo.mp4",
      gif"rphjb_TosseInvernaleAiutateloGif.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      "aiutatemi"
    )(
      vid"rphjb_Aiutatemi.mp4",
      mp3"rphjb_TosseInvernaleAiutatelo.mp3",
      vid"rphjb_TosseInvernaleAiutatelo.mp4",
      gif"rphjb_TosseInvernaleAiutateloGif.mp4",
      mp3"rphjb_NonDormoQuasiTuttaLaNotteAiutatemi.mp3",
      vid"rphjb_NonDormoQuasiTuttaLaNotteAiutatemi.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      "tosse",
      "scusate"
    )(
      vid"rphjb_ScusateEssereUmanoTosse.mp4",
      mp3"rphjb_ScusateEssereUmanoTosse.mp3",
      mp3"rphjb_TosseInvernaleAiutatelo.mp3",
      vid"rphjb_TosseInvernaleAiutatelo.mp4",
      gif"rphjb_TosseInvernaleAiutateloGif.mp4",
      vid"rphjb_TosseRospoInGola.mp4",
      gif"rphjb_TosseRospoInGolaGif.mp4",
      mp3"rphjb_TosseRospoInGola.mp3"
    ),
    ReplyBundleMessage.textToMedia[F](
      "detesto",
      "con tutto me stesso",
      "adesione",
      "(levare|togliere) dalle palle".r.tr(18)
    )(
      mp3"rphjb_LiDetestoLevareDallePalle.mp3",
      vid"rphjb_LiDetestoLevareDallePalle.mp4",
      gif"rphjb_LiDetestoLevareDallePalleGif.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      "carne saporita"
    )(
      mp3"rphjb_RagazzettaCarne.mp3",
      mp3"rphjb_CarneFrescaSaporita.mp3",
      vid"rphjb_CarneFrescaSaporita.mp4",
      gif"rphjb_CarneFrescaSaporitaGif.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      "carne (dura|vecchia|fresca)".r.tr(10)
    )(
      mp3"rphjb_CarneFrescaSaporita.mp3",
      vid"rphjb_CarneFrescaSaporita.mp4",
      gif"rphjb_CarneFrescaSaporitaGif.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      "dolcezza violenta",
      "gemiti da bestia",
      "sembravano fate",
      "distingue la tua razza",
      "tutto quello che c'è"
    )(
      mp3"rphjb_PoesiaDolcezzaViolenta.mp3",
      vid"rphjb_PoesiaDolcezzaViolenta.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      "ammaestrare il dolore"
    )(
      vid"rphjb_AmmaestrareIlDolore.mp4",
      mp3"rphjb_PoesiaDolcezzaViolenta.mp3",
      vid"rphjb_PoesiaDolcezzaViolenta.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      "sfuggono",
      "\\bpols[io]\\b".r.tr(5),
      "\\borolog[io]\\b".r.tr(7)
    )(
      vid"rphjb_4SoloTempiInTestaOrologiSfuggonoPolsi.mp4",
      gif"rphjb_4SoloTempiInTestaOrologiSfuggonoPolsiGif.mp4",
      mp3"rphjb_4SoloTempiInTestaOrologiSfuggonoPolsi.mp3",
      mp3"rphjb_PoesiaDolcezzaViolenta.mp3",
      vid"rphjb_PoesiaDolcezzaViolenta.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      "giacobbe",
      "gli angeli",
      "le scale",
      "gradin[oi]".r.tr(7),
      "le ali"
    )(
      mp3"rphjb_AngeliDiGiacobbe.mp3",
      vid"rphjb_AngeliDiGiacobbe.mp4",
      gif"rphjb_AngeliDiGiacobbe2Gif.mp4",
      vid"rphjb_AngeliDiGiacobbe2.mp4",
      mp3"rphjb_AngeliDiGiacobbe2.mp3"
    ),
    ReplyBundleMessage.textToMedia[F](
      "bibbia",
      "sergio",
      "stefano",
      "babilonia",
      "non s[ie] capisce".r.tr(14),
      "quale ruolo"
    )(
      mp3"rphjb_AngeliDiGiacobbe.mp3",
      vid"rphjb_AngeliDiGiacobbe.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      "raccontare"
    )(
      vid"rphjb_QuanteCoseViPotreiRaccontare.mp4",
      vid"rphjb_TroppeStorieRaccontare.mp4",
      gif"rphjb_TroppeStorieRaccontareGif.mp4",
      mp3"rphjb_TroppeStorieRaccontare.mp3",
      mp3"rphjb_TeLoVoglioRaccontareAncheSeNonCentraNiente.mp3",
      gif"rphjb_TeLoVoglioRaccontareAncheSeNonCentraNienteGif.mp4",
      vid"rphjb_TeLoVoglioRaccontareAncheSeNonCentraNiente.mp4",
      mp3"rphjb_ViDevoRaccontareUnAltraStoria.mp3",
      gif"rphjb_ViDevoRaccontareUnAltraStoriaGif.mp4",
      vid"rphjb_ViDevoRaccontareUnAltraStoria.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      "sulla punta della lingua"
    )(
      vid"rphjb_TroppeStorieRaccontare.mp4",
      gif"rphjb_TroppeStorieRaccontareGif.mp4",
      mp3"rphjb_TroppeStorieRaccontare.mp3",
      vid"rphjb_CaniAlCimitero.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      "internet"
    )(
      gif"rphjb_InternetGif.mp4",
      vid"rphjb_ReteInternettaria.mp4",
      gif"rphjb_ReteInternettariaGif.mp4",
      mp3"rphjb_ReteInternettaria.mp3",
      vid"rphjb_EmuleReteInternettariaSitoScoppia.mp4",
      mp3"rphjb_EmuleReteInternettariaSitoScoppia.mp3",
      mp3"rphjb_MappaInternettaria.mp3",
      vid"rphjb_MappaInternettaria.mp4",
      gif"rphjb_InPiuUnaCosaCheMiHaScocciatoGif.mp4",
      vid"rphjb_InPiuUnaCosaCheMiHaScocciato.mp4",
      mp3"rphjb_InPiuUnaCosaCheMiHaScocciato.mp3",
      vid"rphjb_ImpegniListaCitta.mp4",
      mp3"rphjb_ImpegniListaCitta.mp3",
      vid"rphjb_MessaggioInternet.mp4",
      mp3"rphjb_MessaggioInternet.mp3"
    ),
    ReplyBundleMessage.textToMedia[F](
      "una rete fitta"
    )(
      gif"rphjb_InternetGif.mp4",
      vid"rphjb_ReteInternettaria.mp4",
      gif"rphjb_ReteInternettariaGif.mp4",
      mp3"rphjb_ReteInternettaria.mp3",
      vid"rphjb_EmuleReteInternettariaSitoScoppia.mp4",
      mp3"rphjb_EmuleReteInternettariaSitoScoppia.mp3"
    ),
    ReplyBundleMessage.textToMedia[F](
      "eric clapton"
    )(
      vid"rphjb_EricClaptonDrogaUominiAffari.mp4",
      vid"rphjb_ChitarristiProsciuttoOrecchie.mp4",
      mp3"rphjb_ChitarristiProsciuttoOrecchie.mp3"
    ),
    ReplyBundleMessage.textToMedia[F](
      "jimi hendrix"
    )(
      vid"rphjb_RollingStonesJimiHendrixPoetiMaledettiFemministeControculturaRivoluzioniRibelioni5DitaCazzottoInFacciaGiustiziere.mp4",
      vid"rphjb_ChitarristiProsciuttoOrecchie.mp4",
      mp3"rphjb_ChitarristiProsciuttoOrecchie.mp3"
    )
  )

  private def messageRepliesMixData4[F[_]: Applicative]: List[ReplyBundleMessage[F]] =
    List(
      ReplyBundleMessage.textToMedia[F](
        "gilmour",
        "sono sempre quelli",
        "jimmy page",
        "blackmore",
        "knopfler",
        "c'è molto di più",
        "dove stavate"
      )(
        vid"rphjb_ChitarristiProsciuttoOrecchie.mp4",
        mp3"rphjb_ChitarristiProsciuttoOrecchie.mp3"
      ),
      ReplyBundleMessage.textToMedia[F](
        "nelle palle di (vostro|loro) padre".r.tr(25),
        "prosciutto nelle orecchie"
      )(
        vid"rphjb_ChitarristiProsciuttoOrecchie.mp4",
        mp3"rphjb_ChitarristiProsciuttoOrecchie.mp3",
        mp3"rphjb_ProsciuttoNelleOrecchiePallePadreSchifosi.mp3",
        vid"rphjb_ProsciuttoNelleOrecchiePallePadreSchifosi.mp4",
        gif"rphjb_ProsciuttoNelleOrecchiePallePadreSchifosiGif.mp4"
      ),
      ReplyBundleMessage.textToMedia[F](
        "chi è"
      )(
        gif"rphjb_QuestaPersonaScusateGif.mp4",
        vid"rphjb_TelefonataInLinea.mp4"
      ),
      ReplyBundleMessage.textToMedia[F](
        "ghent",
        "albania"
      )(
        vid"rphjb_GhentPiattiAlbania.mp4",
        vid"rphjb_PiattiGhentAlbaniaCiPensa.mp4",
        vid"rphjb_PiattiGhentDischiVolantiAlbaniaPortaCenere.mp4",
        vid"rphjb_GhentScarpeDaTennis.mp4",
        mp3"rphjb_GhentScarpeDaTennis.mp3"
      ),
      ReplyBundleMessage.textToMedia[F](
        "sponsor",
        "figuraccia"
      )(
        vid"rphjb_GhentScarpeDaTennis.mp4",
        mp3"rphjb_GhentScarpeDaTennis.mp3",
        gif"rphjb_AccompagnaviRitardoFiguracciaGif.mp4",
        vid"rphjb_AccompagnaviRitardoFiguraccia.mp4",
        mp3"rphjb_AccompagnaviRitardoFiguraccia.mp3"
      ),
      ReplyBundleMessage.textToMedia[F](
        "sarcina"
      )(
        vid"rphjb_GhentScarpeDaTennis.mp4",
        mp3"rphjb_GhentScarpeDaTennis.mp3",
        vid"rphjb_AttenzioneSarcinaCuoia.mp4",
        mp3"rphjb_AttenzioneSarcinaCuoia.mp3",
        gif"rphjb_AttenzioneSarcinaCuoiaGif.mp4"
      ),
      ReplyBundleMessage.textToMedia[F](
        "è pieno"
      )(
        vid"rphjb_PienoDiDischi.mp4",
        gif"rphjb_EPienoGif.mp4"
      ),
      ReplyBundleMessage.textToMedia[F](
        "musicalmente",
        "tanta gente"
      )(
        vid"rphjb_SvegiareMusicalmente.mp4",
        mp3"rphjb_SvegiareMusicalmente.mp3"
      ),
      ReplyBundleMessage.textToMedia[F](
        "non capisce un cazzo"
      )(
        vid"rphjb_SvegiareMusicalmente.mp4",
        mp3"rphjb_SvegiareMusicalmente.mp3",
        mp3"rphjb_DavantiGenteNonHaCapisceUnCazzo.mp3"
      ),
      ReplyBundleMessage.textToMedia[F](
        "caramell"
      )(
        vid"rphjb_SucchiarviCaramelleFumarviCalpestareTacchiASpilloDominatore.mp4",
        vid"rphjb_DotiTecnicheIngegneristicheDiNoiaCaramelline.mp4",
        mp3"rphjb_DotiTecnicheIngegneristicheDiNoiaCaramelline.mp3"
      ),
      ReplyBundleMessage.textToMedia[F](
        "doti (tecniche|ingegneristiche)".r.tr(13),
        "computer",
        "aggeggi",
        "bere un bicchiere d'acqua"
      )(
        vid"rphjb_DotiTecnicheIngegneristicheDiNoiaCaramelline.mp4",
        mp3"rphjb_DotiTecnicheIngegneristicheDiNoiaCaramelline.mp3"
      ),
      ReplyBundleMessage.textToMedia[F](
        "lo sai fare"
      )(
        vid"rphjb_NonSempliceDonnaLavoranteAffettoMascherinaSaiFare.mp4",
        mp3"rphjb_NonSempliceDonnaLavoranteAffettoMascherinaSaiFare.mp3",
        gif"rphjb_OLoSaiFareONonLoSaiFareGif.mp4"
      ),
      ReplyBundleMessage.textToMedia[F](
        "non è semplice",
        "(richiedo|voglio) il massimo",
        "lavorante",
        "non maschero",
        "mascher(a|ina)".r.tr(8),
        "veneziana",
        "non c'è problema",
        "fidatevi"
      )(
        vid"rphjb_NonSempliceDonnaLavoranteAffettoMascherinaSaiFare.mp4",
        mp3"rphjb_NonSempliceDonnaLavoranteAffettoMascherinaSaiFare.mp3"
      ),
      ReplyBundleMessage.textToMedia[F](
        "micidiale"
      )(
        gif"rphjb_MicidialeGif.mp4",
        mp3"rphjb_Micidiale.mp3",
        vid"rphjb_Micidiale.mp4"
      ),
      ReplyBundleMessage.textToMedia[F](
        "rospo in gola",
        "fantasmagorico",
        "soffocavo"
      )(
        gif"rphjb_TosseRospoInGolaGif.mp4",
        mp3"rphjb_TosseRospoInGola.mp3",
        vid"rphjb_TosseRospoInGola.mp4"
      ),
      ReplyBundleMessage.textToMedia[F](
        "vi controllo"
      )(
        vid"rphjb_SeguireTuttiListaPersone.mp4",
        gif"rphjb_NonViScordateViControlloGif.mp4",
        mp3"rphjb_NonViScordateViControllo.mp3",
        vid"rphjb_NonViScordateViControllo.mp4",
        gif"rphjb_ViControlloDAlessioTatangeloFalsiMetallariGif.mp4",
        vid"rphjb_ViControlloDAlessioTatangeloFalsiMetallari.mp4",
        mp3"rphjb_ViControlloDAlessioTatangeloFalsiMetallari.mp3"
      ),
      ReplyBundleMessage.textToMedia[F](
        "scordate"
      )(
        gif"rphjb_NonViScordateViControlloGif.mp4",
        mp3"rphjb_NonViScordateViControllo.mp3",
        vid"rphjb_NonViScordateViControllo.mp4"
      ),
      ReplyBundleMessage.textToMedia[F](
        "cuoia"
      )(
        vid"rphjb_AttenzioneSarcinaCuoia.mp4",
        mp3"rphjb_AttenzioneSarcinaCuoia.mp3",
        gif"rphjb_AttenzioneSarcinaCuoiaGif.mp4"
      ),
      ReplyBundleMessage.textToMedia[F](
        "nutrito"
      )(
        vid"rphjb_ApplausoPiuNutrito.mp4",
        gif"rphjb_ApplausoPiuNutritoGif.mp4",
        mp3"rphjb_ApplausoPiuNutrito.mp3"
      ),
      ReplyBundleMessage.textToMedia[F](
        "questo n[o]{2,}".r.tr(10)
      )(
        vid"rphjb_QuestoNoETroppoIndicibileSchifosa.mp4",
        vid"rphjb_DelirioDelSabatoSera.mp4",
        vid"rphjb_ChiCazzoLHaDettoPappalardo.mp4",
        gif"rphjb_ChiCazzoLHaDettoPappalardoGif.mp4",
        mp3"rphjb_ChiCazzoLHaDettoPappalardo.mp3"
      ),
      ReplyBundleMessage.textToMedia[F](
        "chi (cazzo )?l'ha detto".r.tr(14)
      )(
        vid"rphjb_ChiCazzoLHaDettoPappalardo.mp4",
        gif"rphjb_ChiCazzoLHaDettoPappalardoGif.mp4",
        mp3"rphjb_ChiCazzoLHaDettoPappalardo.mp3"
      ),
      ReplyBundleMessage.textToMedia[F](
        "alla ricerca"
      )(
        gif"rphjb_CuoreAffogatoNelMetalloGif.mp4",
        vid"rphjb_CuoreAffogatoNelMetallo.mp4",
        mp3"rphjb_CuoreAffogatoNelMetallo.mp3",
        vid"rphjb_RicercaGianni.mp4",
        mp3"rphjb_RicercaGianni.mp3",
        vid"rphjb_CuoreAffogatoNelMetalloRicercaCanzoneFeriscaNelCervello.mp4",
        vid"rphjb_CuoreIntrisoMetalloCanzoneFeriscaCervello.mp4",
        gif"rphjb_CuoreIntrisoMetalloCanzoneFeriscaCervelloGif.mp4",
        mp3"rphjb_CuoreIntrisoMetalloCanzoneFeriscaCervello.mp3",
        vid"rphjb_GruppiNuoviSpaccanoCuloAbbellimentiRivoltiMordentiContrappunti.mp4"
      ),
      ReplyBundleMessage.textToMedia[F](
        "gianni neri"
      )(
        vid"rphjb_RingraziareGianniTraffico.mp4",
        vid"rphjb_GianniNeriCoppiaMiciciale.mp4",
        vid"rphjb_GianniNeriCheFineHaiFatto.mp4",
        vid"rphjb_MigliorAmicoCoppiaMicidialeGianniNeri.mp4",
        vid"rphjb_PiaccionoBelleDonneVallettaGianniNeriGrandeAmico.mp4",
        vid"rphjb_RicercaGianni.mp4",
        mp3"rphjb_RicercaGianni.mp3",
        sticker"rphjb_GianniNeriBensoniani.sticker"
      ),
      ReplyBundleMessage.textToMedia[F](
        "trovare"
      )(
        vid"rphjb_CommissionatoMeLoDeviTrovare.mp4",
        vid"rphjb_MoltoDifficileDaTrovare.mp4",
        vid"rphjb_RicercaGianni.mp4",
        mp3"rphjb_RicercaGianni.mp3"
      ),
      ReplyBundleMessage.textToMedia[F](
        "adinolfi",
        "forze dell'ordine"
      )(
        vid"rphjb_RicercaGianni.mp4",
        mp3"rphjb_RicercaGianni.mp3"
      ),
      ReplyBundleMessage.textToMedia[F](
        "trastevere",
        "tor marancia",
        "tor pagnotta",
        "fidene",
        "mandrione",
        "parioli",
        "in coro"
      )(
        mp3"rphjb_Avremo18AnniLong.mp3",
        vid"rphjb_Avremo18AnniLong.mp4"
      ),
      ReplyBundleMessage.textToMedia[F](
        "chiedere troppo",
        "al mio cervello"
      )(
        vid"rphjb_StorieSonoTanteTroppoAlMioCervello.mp4",
        gif"rphjb_StorieSonoTanteTroppoAlMioCervelloGif.mp4",
        mp3"rphjb_StorieSonoTanteTroppoAlMioCervello.mp3"
      ),
      ReplyBundleMessage.textToMedia[F](
        "cuore (affogato|intriso)".r.tr(13),
        "affogato nel metallo",
        "feri(sca|to) nel cervello".r.tr(19)
      )(
        gif"rphjb_CuoreAffogatoNelMetalloGif.mp4",
        vid"rphjb_CuoreAffogatoNelMetallo.mp4",
        mp3"rphjb_CuoreAffogatoNelMetallo.mp3",
        vid"rphjb_CuoreAffogatoNelMetalloRicercaCanzoneFeriscaNelCervello.mp4",
        vid"rphjb_CuoreIntrisoMetalloCanzoneFeriscaCervello.mp4",
        gif"rphjb_CuoreIntrisoMetalloCanzoneFeriscaCervelloGif.mp4",
        mp3"rphjb_CuoreIntrisoMetalloCanzoneFeriscaCervello.mp3"
      ),
      ReplyBundleMessage.textToMedia[F](
        "in cerca"
      )(
        gif"rphjb_CuoreAffogatoNelMetalloGif.mp4",
        vid"rphjb_CuoreAffogatoNelMetallo.mp4",
        mp3"rphjb_CuoreAffogatoNelMetallo.mp3",
        vid"rphjb_CuoreAffogatoNelMetalloRicercaCanzoneFeriscaNelCervello.mp4",
        vid"rphjb_CuoreIntrisoMetalloCanzoneFeriscaCervello.mp4",
        gif"rphjb_CuoreIntrisoMetalloCanzoneFeriscaCervelloGif.mp4",
        mp3"rphjb_CuoreIntrisoMetalloCanzoneFeriscaCervello.mp3",
        vid"rphjb_InCercaDellAccordoPerduto.mp4",
        mp3"rphjb_InCercaDellAccordoPerduto.mp3",
        gif"rphjb_InCercaDellAccordoPerdutoGif.mp4"
      ),
      ReplyBundleMessage.textToMedia[F](
        "gente strana"
      )(
        mp3"rphjb_GenteStranaBicchiereSputo.mp3",
        vid"rphjb_GenteStranaBicchiereSputo.mp4"
      ),
      ReplyBundleMessage.textToMedia[F](
        "com'è possibile",
        "il cuore",
        "la mente",
        "gli occhi",
        "le orecchie",
        "le vene",
        "corda sensibile",
        "centro del motore",
        "non vi arriva"
      )(
        mp3"rphjb_ComePossibileCentroMotore.mp3",
        vid"rphjb_ComePossibileCentroMotore.mp4",
        vid"rphjb_CordeCheVibranoCentroMotoreFattiDiTutto.mp4",
        mp3"rphjb_CordeCheVibranoCentroMotoreFattiDiTutto.mp3"
      ),
      ReplyBundleMessage.textToMedia[F](
        "\\bsperma\\b".r.tr(5)
      )(
        mp3"rphjb_DonneSperma.mp3",
        vid"rphjb_EsseriUmaniZozzeriaCarnePelleSputoSudoreSpermaNonContiamoNiente.mp4",
        mp3"rphjb_Fazzoletti.mp3",
        vid"rphjb_IlPubblicoDavanti.mp4",
        mp3"rphjb_IlPubblicoDavanti.mp3",
        vid"rphjb_ConsigliSulPacco.mp4"
      ),
      ReplyBundleMessage.textToMedia[F](
        "pelle",
        "carne"
      )(
        vid"rphjb_EsseriUmaniZozzeriaCarnePelleSputoSudoreSpermaNonContiamoNiente.mp4",
        vid"rphjb_CordeCheVibranoCentroMotoreFattiDiTutto.mp4",
        mp3"rphjb_CordeCheVibranoCentroMotoreFattiDiTutto.mp3"
      ),
      ReplyBundleMessage.textToMedia[F](
        "decodificatore",
        "diecimila persone",
        "giappone",
        "finlandia",
        "norvegia",
        "asia",
        "etiopia"
      )(
        vid"rphjb_EmuleReteInternettariaSitoScoppia.mp4",
        mp3"rphjb_EmuleReteInternettariaSitoScoppia.mp3"
      ),
      ReplyBundleMessage.textToMedia[F](
        "australia"
      )(
        vid"rphjb_EmuleReteInternettariaSitoScoppia.mp4",
        mp3"rphjb_EmuleReteInternettariaSitoScoppia.mp3",
        mp3"rphjb_MappaInternettaria.mp3",
        vid"rphjb_MappaInternettaria.mp4",
        vid"rphjb_MessaggioInternet.mp4",
        mp3"rphjb_MessaggioInternet.mp3"
      ),
      ReplyBundleMessage.textToMedia[F]("telecomand[oi]".r.tr(11))(
        mp3"rphjb_TelecomandoCambiareCanaleDischiNuovi.mp3",
        vid"rphjb_TelecomandoCambiareCanaleDischiNuovi.mp4",
        vid"rphjb_OttavaNotaRobaVecchiaSchifosi.mp4",
        mp3"rphjb_OttavaNotaRobaVecchiaSchifosi.mp3"
      ),
      ReplyBundleMessage.textToMedia[F](
        "pausini",
        "non ho paura",
        "se mi amate",
        "mi seguite",
        "via discorrendo"
      )(
        gif"rphjb_NonHoPauraDiNessunoGif.mp4",
        vid"rphjb_NonHoPauraDiNessuno.mp4",
        mp3"rphjb_NonHoPauraDiNessuno.mp3"
      ),
      ReplyBundleMessage.textToMedia[F](
        "arsenale",
        "maglietta",
        "giubbotti",
        "mostri",
        "pugnali",
        "maschere",
        "co[lr]telli".r.tr(8),
        "non (l[iì] )vendo".r.tr(9)
      )(
        mp3"rphjb_MagliettaBiancaNonVendoArsenale.mp3",
        vid"rphjb_MagliettaBiancaNonVendoArsenale.mp4"
      ),
      ReplyBundleMessage.textToMedia[F](
        "latte droga",
        "solo gregge",
        "gregge da discoteca"
      )(
        vid"rphjb_PoveriIgnorantiLatteDrogaSoloGreggeSputo.mp4",
        mp3"rphjb_PoesiaAltroSenso.mp3",
        vid"rphjb_PoesiaAltroSenso.mp4"
      ),
      ReplyBundleMessage.textToMedia[F](
        "sintesi del sintetico",
        "ecstasi",
        "cannabinoidi",
        "sconquassati",
        "la realt[aà] sanguin[oò]".r.tr(18)
      )(
        mp3"rphjb_PoesiaAltroSenso.mp3",
        vid"rphjb_PoesiaAltroSenso.mp4"
      ),
      ReplyBundleMessage.textToMedia[F](
        "drog(a|he) (legger[ae]|pesant[ei])".r.tr(14),
        "ammoniaca",
        "(sprecano|allungano) le foglie".r.tr(18),
        "veleno per topi",
        "borotalco",
        "contraffatt[ao]".r.tr(12)
      )(
        vid"rphjb_DrogheLeggere.mp4",
        gif"rphjb_DrogaLeggeraFoglieGif.mp4",
        mp3"rphjb_DrogaLeggeraFoglie.mp3"
      ),
      ReplyBundleMessage.textToMedia[F](
        "fine( )?settimana".r.tr(13),
        "weekend",
        "sta all'inizio"
      )(
        mp3"rphjb_FineSettimanaMercolediInizio.mp3",
        vid"rphjb_FineSettimanaMercolediInizio.mp4",
        gif"rphjb_FineSettimanaMercolediInizioGif.mp4"
      ),
      ReplyBundleMessage.textToMedia[F](
        "donna cane"
      )(
        gif"rphjb_DonnaCaneGif.mp4",
        vid"rphjb_DonnaCane.mp4",
        mp3"rphjb_DonnaCane.mp3"
      ),
      ReplyBundleMessage.textToMedia[F](
        "guinzaglio",
        "e la tiro"
      )(
        vid"rphjb_DonnaCane.mp4",
        mp3"rphjb_DonnaCane.mp3"
      ),
      ReplyBundleMessage.textToMedia[F](
        "di una volta"
      )(
        vid"rphjb_GambaleCHaDeluso.mp4",
        vid"rphjb_DonneDiUnaVoltaSeniCuomoMadonna.mp4",
        mp3"rphjb_DonneDiUnaVoltaSeniCuomoMadonna.mp3"
      ),
      ReplyBundleMessage.textToMedia[F](
        "fatti lecc(are|a|à)\\b".r.tr(11),
        "katy monique cuomo",
        "mi vergogno",
        "porno[ ]?(diva|star|)".r.tr(9)
      )(
        vid"rphjb_DonneDiUnaVoltaSeniCuomoMadonna.mp4",
        mp3"rphjb_DonneDiUnaVoltaSeniCuomoMadonna.mp3"
      ),
      ReplyBundleMessage.textToMedia[F](
        "(pubblico|gente) davanti".r.tr(13),
        "\\ba fro(ci|sh)o\\b".r.tr(8),
        "facc[ei] ved(e|ere) (il|er) culo".r.tr(18),
        "quando scopi",
        "te lo faccio vedere",
        "oxford"
      )(
        vid"rphjb_IlPubblicoDavanti.mp4",
        mp3"rphjb_IlPubblicoDavanti.mp3"
      ),
      ReplyBundleMessage.textToMedia[F](
        "\\btega\\b".r.tr(4),
        "(il|er) baffo".r.tr(9)
      )(
        mp3"rphjb_RaccondaStoriaTegaBaffoPeliCulo.mp3",
        vid"rphjb_IlPubblicoDavanti.mp4",
        mp3"rphjb_IlPubblicoDavanti.mp3"
      ),
      ReplyBundleMessage.textToMedia[F](
        "sto tavolo",
        "piena (di|de) curve",
        "tira fuori il"
      )(
        vid"rphjb_TiraFuoriIlCazzo.mp4",
        mp3"rphjb_TiraFuoriIlCazzo.mp3",
        gif"rphjb_TiraFuoriIlCazzoGif.mp4"
      ),
      ReplyBundleMessage.textToMedia[F](
        "non lo so",
        "idee più chiare",
        "idee (molto )?confuse".r.tr(12)
      )(
        vid"rphjb_IdeeConfuse.mp4",
        mp3"rphjb_IdeeConfuse.mp3",
        gif"rphjb_IdeeConfuseGif.mp4"
      ),
      ReplyBundleMessage.textToMedia[F](
        "sei [gc]ambiat[oa]".r.tr(12),
        "che (ti |t')è successo?".r.tr(15),
        "non sei più (rock|metal)".r.tr(16)
      )(
        gif"rphjb_SeiCambiataGif.mp4",
        mp3"rphjb_SeiCambiata.mp3",
        vid"rphjb_SeiCambiata.mp4",
        sticker"rphjb_SeiCambiataBensoniani.sticker"
      ),
      ReplyBundleMessage.textToMedia[F](
        "☝️",
        "👆",
        "👉",
        "👇",
        "👈"
      )(
        gif"rphjb_IndicareGif.mp4",
        sticker"rphjb_IndicaBensoniani.sticker",
        sticker"rphjb_IndicaAffanculoPazzescaBensoniani.sticker"
      ),
      ReplyBundleMessage.textToMedia[F](
        "lui si chiamava",
        "\\badolf\\b".r.tr(5),
        "hitler",
        "belle arti",
        "\\bhitl[aà]\\b".r.tr(5)
      )(
        gif"rphjb_AdolfHitlerGif.mp4",
        sticker"rphjb_LuiSiChiamavaBensoniani.sticker",
        mp3"rphjb_AdolfHitler.mp3",
        vid"rphjb_AdolfHitler.mp4"
      ),
      ReplyBundleMessage.textToMedia[F](
        "distruggere il proprio sesso"
      )(
        vid"rphjb_AmmaestrareIlDolore.mp4",
        sticker"rphjb_DistruggereSessoBensoniani.sticker"
      ),
      ReplyBundleMessage.textToMedia[F](
        "baci",
        "limonare"
      )(
        gif"rphjb_BacioGif.mp4",
        sticker"rphjb_BacioBensoniani.sticker"
      ),
      ReplyBundleMessage.textToMedia[F](
        "obama"
      )(
        vid"rphjb_Obama.mp4",
        vid"rphjb_ObamaRichardBensonInsieme.mp4",
        sticker"rphjb_ObamaBensoniani.sticker"
      ),
      ReplyBundleMessage.textToMedia[F](
        "divento violento",
        "mtv",
        "inginocchiati"
      )(
        vid"rphjb_DiventoViolento.mp4",
        mp3"rphjb_DiventoViolento.mp3"
      ),
      ReplyBundleMessage.textToMedia[F](
        "stronzo"
      )(
        vid"rphjb_StronzoFiglioMignotta.mp4",
        vid"rphjb_DiventoViolento.mp4",
        mp3"rphjb_DiventoViolento.mp3"
      ),
      ReplyBundleMessage.textToMedia[F](
        "passa in fretta",
        "prima che (il tempo )?ammazzi noi".r.tr(21)
      )(
        vid"rphjb_PassaIlTempo.mp4",
        mp3"rphjb_PassaIlTempo.mp3",
        gif"rphjb_PassaIlTempoGif.mp4"
      ),
      ReplyBundleMessage.textToMedia[F](
        "dischi"
      )(
        vid"rphjb_PienoDiDischi.mp4",
        vid"rphjb_PienoDiDischiNovitaTempoInferioreSuperiore.mp4",
        mp3"rphjb_PienoDiDischiNovitaTempoInferioreSuperiore.mp3",
        gif"rphjb_PienoDiDischiNovitaTempoInferioreSuperioreGif.mp4",
        mp3"rphjb_UnoDeiPiuGrandiDischi.mp3",
        vid"rphjb_UnoDeiPiuGrandiDischi.mp4",
        mp3"rphjb_Tastieristi.mp3",
        vid"rphjb_Tastieristi.mp4"
      ),
      ReplyBundleMessage.textToMedia[F](
        "sono pieno",
        "la roba che ho",
        "le novit[aà]".r.tr(9)
      )(
        vid"rphjb_PienoDiDischiNovitaTempoInferioreSuperiore.mp4",
        mp3"rphjb_PienoDiDischiNovitaTempoInferioreSuperiore.mp3",
        gif"rphjb_PienoDiDischiNovitaTempoInferioreSuperioreGif.mp4"
      ),
      ReplyBundleMessage.textToMedia[F](
        "sfuggire a",
        "persona (simpatica|gradevole)".r.tr(17)
      )(
        vid"rphjb_SolangeSfuggire.mp4",
        mp3"rphjb_SolangeSfuggire.mp3"
      ),
      ReplyBundleMessage.textToMedia[F](
        "solange"
      )(
        vid"rphjb_AlzareLAudienceOspitiGeroglifico.mp4",
        mp3"rphjb_AlzareLAudienceOspitiGeroglifico.mp3",
        vid"rphjb_SolangeSfuggire.mp4",
        mp3"rphjb_SolangeSfuggire.mp3"
      ),
      ReplyBundleMessage.textToMedia[F](
        "piatti"
      )(
        vid"rphjb_BiscionePiatti.mp4",
        vid"rphjb_GhentPiattiAlbania.mp4",
        vid"rphjb_PiattiGhentAlbaniaCiPensa.mp4",
        vid"rphjb_PiattiGhentDischiVolantiAlbaniaPortaCenere.mp4",
        sticker"rphjb_PiattiBensoniani.sticker"
      ),
      ReplyBundleMessage
        .textToMedia[F](
          "verita",
          "verità"
        )(
          vid"rphjb_AltraCazzataVeritaSembranoCazzate.mp4",
          vid"rphjb_IoParloDicoLaVeritaContrattiFantomaticiVieniQuiFaiVedereFacciaCovoDelMetalloSimposio.mp4",
          gif"rphjb_VeritaGif.mp4"
        ),
      ReplyBundleMessage.textToMedia[F](
        "richie kotzen",
        "al manicomio"
      )(
        mp3"rphjb_AlManicomioRichieKotzen.mp3",
        vid"rphjb_AlManicomioRichieKotzen.mp4",
        gif"rphjb_AlManicomioRichieKotzenGif.mp4"
      ),
      ReplyBundleMessage.textToMedia[F](
        "completamente libero"
      )(
        mp3"rphjb_CompletamenteLibero.mp3",
        vid"rphjb_CompletamenteLibero.mp4",
        gif"rphjb_CompletamenteLiberoGif.mp4"
      ),
      ReplyBundleMessage.textToMedia[F](
        "che schifo!",
        "che( )?(s)+chifo".r.tr(10)
      )(
        mp3"rphjb_SchifosiCheSchifo.mp3",
        vid"rphjb_SchifosiCheSchifo.mp4",
        gif"rphjb_SchifosiCheSchifoGif.mp4"
      ),
      ReplyBundleMessage.textToMedia[F](
        "pacato"
      )(
        vid"rphjb_PiuPacatoMiIncazzo.mp4",
        gif"rphjb_PiuPacatoMiIncazzoGif.mp4",
        mp3"rphjb_PiuPacatoMiIncazzo.mp3"
      ),
      ReplyBundleMessage.textToMedia[F](
        "tatangelo"
      )(
        mp3"rphjb_GigiDAlessioAnnaTatangelo.mp3",
        gif"rphjb_ViControlloDAlessioTatangeloFalsiMetallariGif.mp4",
        vid"rphjb_ViControlloDAlessioTatangeloFalsiMetallari.mp4",
        mp3"rphjb_ViControlloDAlessioTatangeloFalsiMetallari.mp3"
      ),
      ReplyBundleMessage.textToMedia[F](
        "uno per uno",
        "falsi metallari",
        "non sono mai cambiato",
        "sono (solo )?evoluto".r.tr(12)
      )(
        gif"rphjb_ViControlloDAlessioTatangeloFalsiMetallariGif.mp4",
        vid"rphjb_ViControlloDAlessioTatangeloFalsiMetallari.mp4",
        mp3"rphjb_ViControlloDAlessioTatangeloFalsiMetallari.mp3"
      ),
      ReplyBundleMessage.textToMedia[F](
        "audience",
        "ora tarda",
        "geroglifico",
        "alba parietti",
        "clemente mastella",
        "lotito",
        "claudia gerini",
        "corvaglia",
        "la russa"
      )(
        vid"rphjb_AlzareLAudienceOspitiGeroglifico.mp4",
        mp3"rphjb_AlzareLAudienceOspitiGeroglifico.mp3"
      ),
      ReplyBundleMessage.textToMedia[F](
        "\\blook\\b".r.tr(4)
      )(
        vid"rphjb_FotoLookDreamTheater.mp4",
        gif"rphjb_ViControlloDAlessioTatangeloFalsiMetallariGif.mp4",
        vid"rphjb_ViControlloDAlessioTatangeloFalsiMetallari.mp4",
        mp3"rphjb_ViControlloDAlessioTatangeloFalsiMetallari.mp3",
        vid"rphjb_MortoPippoBaudo.mp4",
        mp3"rphjb_MortoPippoBaudo.mp3"
      ),
      ReplyBundleMessage.textToMedia[F](
        "mel gibson",
        "rinunciato"
      )(
        vid"rphjb_GrandeMelGibsonRinunciaATutto.mp4",
        mp3"rphjb_GrandeMelGibsonRinunciaATutto.mp3"
      ),
      ReplyBundleMessage.textToMedia[F](
        "\\baffari\\b".r.tr(6)
      )(
        gif"rphjb_GiubbottiModaAffariTrasformistaGif.mp4",
        vid"rphjb_GiubbottiModaAffariTrasformista.mp4",
        mp3"rphjb_GiubbottiModaAffariTrasformista.mp3",
        vid"rphjb_ImpegniListaCitta.mp4",
        mp3"rphjb_ImpegniListaCitta.mp3"
      ),
      ReplyBundleMessage.textToMedia[F](
        "costruisco giubbotti",
        "\\bmoda\\b".r.tr(4),
        "faccio tant(e|issime) cose".r.tr(17),
        "trasform(ismo|armi)".r.tr(12)
      )(
        gif"rphjb_GiubbottiModaAffariTrasformistaGif.mp4",
        vid"rphjb_GiubbottiModaAffariTrasformista.mp4",
        mp3"rphjb_GiubbottiModaAffariTrasformista.mp3"
      ),
      ReplyBundleMessage.textToMedia[F](
        "appassionat[oi] di musica".r.tr(22)
      )(
        gif"rphjb_GiubbottiModaAffariTrasformistaGif.mp4",
        vid"rphjb_GiubbottiModaAffariTrasformista.mp4",
        mp3"rphjb_GiubbottiModaAffariTrasformista.mp3",
        mp3"rphjb_DoveStannoGliAppassionatiTanteMusiche.mp3",
        vid"rphjb_DoveStannoGliAppassionatiTanteMusiche.mp4"
      ),
      ReplyBundleMessage.textToMedia[F](
        "vostri cuori",
        "vostre emozioni",
        "ci sarò sempre io",
        "dentro di (voi|me)".r.tr(12),
        "vostro (sesso|cervello)".r.tr(12)
      )(
        gif"rphjb_CiSaroSempreIoDentroDiVoiEVoiDentroDiMeGif.mp4",
        vid"rphjb_CiSaroSempreIoDentroDiVoiEVoiDentroDiMe.mp4",
        mp3"rphjb_CiSaroSempreIoDentroDiVoiEVoiDentroDiMe.mp3"
      ),
      ReplyBundleMessage.textToMedia[F](
        "rimediamelo"
      )(
        vid"rphjb_AngeloRimediamelo.mp4",
        mp3"rphjb_AngeloRimediamelo.mp3"
      ),
      ReplyBundleMessage.textToMedia[F](
        "mente (superiore|inferiore)".r.tr(15)
      )(
        mp3"rphjb_MenteSuperioreInferioreLucaDiNoia.mp3",
        vid"rphjb_MenteSuperioreInferioreLucaDiNoia.mp4"
      ),
      ReplyBundleMessage.textToMedia[F](
        "che ho mai sentito",
        "in vita mia"
      )(
        mp3"rphjb_UnoDeiPiuGrandiDischi.mp3",
        vid"rphjb_UnoDeiPiuGrandiDischi.mp4"
      ),
      ReplyBundleMessage.textToMedia[F](
        "centocelle",
        "trucido",
        "martone",
        "il messia"
      )(
        mp3"rphjb_SeNonAndateDalCarpenelliViTrucido.mp3",
        vid"rphjb_SeNonAndateDalCarpenelliViTrucido.mp4"
      ),
      ReplyBundleMessage.textToMedia[F](
        "mi serve quella cosa",
        "che mi serve?",
        "ritocchino",
        "parte del corpo"
      )(
        mp3"rphjb_RitocchinoParteDelCorpo.mp3",
        vid"rphjb_RitocchinoParteDelCorpo.mp4",
        gif"rphjb_RitocchinoParteDelCorpoGif.mp4"
      ),
      ReplyBundleMessage.textToMedia[F](
        "alcatraz",
        "fiumicino",
        "missiva",
        "degno erede",
        "ti chiedo solo una cosa",
        "(fammi|chiedere)? (una|questa)? cortesia".r.tr(18),
        "incassi",
        "sarà (più bravo di me|bravissimo)".r.tr(15)
      )(
        mp3"rphjb_PepeAlcatrazMissivaDegnoErede.mp3",
        vid"rphjb_PepeAlcatrazMissivaDegnoErede.mp4",
        gif"rphjb_PepeAlcatrazMissivaDegnoEredeGif.mp4"
      ),
      ReplyBundleMessage.textToMedia[F](
        "truccato"
      )(
        vid"rphjb_StoriaMarlinManson.mp4",
        mp3"rphjb_StaseraSeLoVedoTruccatoMetallaro.mp3",
        vid"rphjb_StaseraSeLoVedoTruccatoMetallaro.mp4",
        gif"rphjb_StaseraSeLoVedoTruccatoMetallaroGif.mp4"
      ),
      ReplyBundleMessage.textToMedia[F](
        "tastiera",
        "derek sherinian",
        "mike pinella",
        "otmaro ruiz"
      )(
        mp3"rphjb_Tastieristi.mp3",
        vid"rphjb_Tastieristi.mp4"
      ),
      ReplyBundleMessage.textToMedia[F](
        "plettro"
      )(
        vid"rphjb_ChitarraPlettroVicoletto.mp4",
        vid"rphjb_ChitarraVicolettoPlettro2.mp4",
        vid"rphjb_CollaSerpe.mp4",
        mp3"rphjb_SuoniBeneOMale.mp3",
        vid"rphjb_SuoniBeneOMale.mp4"
      ),
      ReplyBundleMessage.textToMedia[F](
        "ma come (cazzo )(soni|suoni)".r.tr(12),
        "suoni (bene|male)",
        "offesa",
        "a due mani",
        "fammi (senti|sentire)".r.tr(11)
      )(
        mp3"rphjb_SuoniBeneOMale.mp3",
        vid"rphjb_SuoniBeneOMale.mp4"
      ),
      ReplyBundleMessage.textToMedia[F](
        "da metallaro",
        "se lo vedo"
      )(
        mp3"rphjb_StaseraSeLoVedoTruccatoMetallaro.mp3",
        vid"rphjb_StaseraSeLoVedoTruccatoMetallaro.mp4",
        gif"rphjb_StaseraSeLoVedoTruccatoMetallaroGif.mp4"
      ),
      ReplyBundleMessage.textToMedia[F](
        "cucciolo"
      )(
        gif"rphjb_CuccioloGif.mp4",
        gif"rphjb_Cucciolo2Gif.mp4",
        mp3"rphjb_SoloUnCuccioloMenzionare.mp3",
        vid"rphjb_SoloUnCuccioloMenzionare.mp4",
        gif"rphjb_SoloUnCuccioloMenzionareGif.mp4"
      ),
      ReplyBundleMessage.textToMedia[F](
        "menzion",
        "fi[b]+ri[l]+azioni".r.tr(13),
        "al cuore"
      )(
        mp3"rphjb_SoloUnCuccioloMenzionare.mp3",
        vid"rphjb_SoloUnCuccioloMenzionare.mp4",
        gif"rphjb_SoloUnCuccioloMenzionareGif.mp4"
      ),
      ReplyBundleMessage.textToMedia[F](
        "love rock",
        "sentir[e]? pi[uù] giovane".r.tr(19)
      )(
        mp3"rphjb_SentirGiovanePamelaAnderson.mp3",
        vid"rphjb_SentirGiovanePamelaAnderson.mp4"
      ),
      ReplyBundleMessage.textToMedia[F](
        "pamela anderson"
      )(
        vid"rphjb_SquallidaScorfanoRaganaCatafalcoAmbulante.mp4",
        mp3"rphjb_SentirGiovanePamelaAnderson.mp3",
        vid"rphjb_SentirGiovanePamelaAnderson.mp4"
      ),
      ReplyBundleMessage.textToMedia[F](
        "ringrazio"
      )(
        vid"rphjb_RingrazioPersoneAttenteDonneToccavanoSeniAnni70LettiPieniErbaCoca.mp4",
        mp3"rphjb_SanValentinoArrivederci.mp3",
        vid"rphjb_SanValentinoArrivederci.mp4"
      ),
      ReplyBundleMessage.textToMedia[F](
        "san valentino",
        "pain of salvation"
      )(
        mp3"rphjb_SanValentinoArrivederci.mp3",
        vid"rphjb_SanValentinoArrivederci.mp4"
      ),
      ReplyBundleMessage.textToMedia[F](
        "\\bripet\\b".r.tr(5)
      )(
        mp3"rphjb_RipetitivitaRottoICoglioni.mp3",
        vid"rphjb_RipetitivitaRottoICoglioni.mp4",
        vid"rphjb_NonPiaceEssereRipetitivoVarzettaStorieAmoreMeNePuoFregaDeMeno.mp4",
        vid"rphjb_RipeteRipete.mp4",
        mp3"rphjb_RipeteRipete.mp3",
        gif"rphjb_RipeteRipeteGif.mp4"
      ),
      ReplyBundleMessage.textToMedia[F](
        "rotto i coglioni"
      )(
        mp3"rphjb_RipetitivitaRottoICoglioni.mp3",
        vid"rphjb_RipetitivitaRottoICoglioni.mp4"
      ),
      ReplyBundleMessage.textToMedia[F](
        "sempre le stesse cose"
      )(
        mp3"rphjb_RipetitivitaRottoICoglioni.mp3",
        vid"rphjb_RipetitivitaRottoICoglioni.mp4",
        mp3"rphjb_TracciaNuoveStradeKikoLoureiro.mp3",
        gif"rphjb_TracciaNuoveStradeKikoLoureiroGif.mp4",
        vid"rphjb_TracciaNuoveStradeKikoLoureiro.mp4"
      ),
      ReplyBundleMessage.textToMedia[F](
        "re del metallo"
      )(
        mp3"rphjb_ReDelMetallo.mp3",
        vid"rphjb_ReDelMetallo.mp4",
        gif"rphjb_ReDelMetalloGif.mp4"
      ),
      ReplyBundleMessage.textToMedia[F](
        "(non|manco) se lo ricordano".r.tr(19),
        "liberatevi (l'anima|la mente)".r.tr(18)
      )(
        mp3"rphjb_ProsciuttoNelleOrecchiePallePadreSchifosi.mp3",
        vid"rphjb_ProsciuttoNelleOrecchiePallePadreSchifosi.mp4",
        gif"rphjb_ProsciuttoNelleOrecchiePallePadreSchifosiGif.mp4"
      ),
      ReplyBundleMessage.textToMedia[F](
        "milano",
        "amsterdam"
      )(
        mp3"rphjb_MappaInternettaria.mp3",
        vid"rphjb_MappaInternettaria.mp4",
        vid"rphjb_ImpegniListaCitta.mp4",
        mp3"rphjb_ImpegniListaCitta.mp3"
      ),
      ReplyBundleMessage.textToMedia[F](
        "la mappa\\b".r.tr(8),
        "ramificazio",
        "tokio",
        "spagna",
        "bolzano",
        "cremona",
        "reggio calabria",
        "siracusa"
      )(
        mp3"rphjb_MappaInternettaria.mp3",
        vid"rphjb_MappaInternettaria.mp4"
      ),
      ReplyBundleMessage.textToMedia[F](
        "helsinki"
      )(
        mp3"rphjb_MappaInternettaria.mp3",
        vid"rphjb_MappaInternettaria.mp4",
        vid"rphjb_MessaggioInternet.mp4",
        mp3"rphjb_MessaggioInternet.mp3"
      ),
      ReplyBundleMessage.textToMedia[F](
        "\\bboccia\\b".r.tr(6),
        "riempi[oe] d[ie] botte".r.tr(16),
        "rai (2|due)".r.tr(5),
        "la settimana scorsa",
        "fuori d[ei] testa".r.tr(14)
      )(
        mp3"rphjb_LoRiempioDeBotte.mp3",
        vid"rphjb_LoRiempioDeBotte.mp4",
        gif"rphjb_LoRiempioDeBotteGif.mp4"
      ),
      ReplyBundleMessage.textToMedia[F](
        "canzoni italiane",
        "lirismo italiano",
        "mario del monaco"
      )(
        mp3"rphjb_LirismoItaliano.mp3",
        vid"rphjb_LirismoItaliano.mp4"
      ),
      ReplyBundleMessage.textToMedia[F](
        "le dico in faccia"
      )(
        mp3"rphjb_LeCoseLeDicoInFaccia.mp3",
        vid"rphjb_LeCoseLeDicoInFaccia.mp4",
        gif"rphjb_LeCoseLeDicoInFacciaGif.mp4"
      ),
      ReplyBundleMessage.textToMedia[F](
        "punto d'arrivo",
        "in cima"
      )(
        mp3"rphjb_IstintiMusicaliIlPuntoDArrivo.mp3",
        vid"rphjb_IstintiMusicaliIlPuntoDArrivo.mp4",
        gif"rphjb_IstintiMusicaliIlPuntoDArrivoGif.mp4"
      ),
      ReplyBundleMessage.textToMedia[F](
        "faccio un casino",
        "(mantenuto|molto) calmo".r.tr(11),
        "poi dopo!"
      )(
        mp3"rphjb_FaccioUnCasinoMoltoCalmoPoiDopo.mp3",
        vid"rphjb_FaccioUnCasinoMoltoCalmoPoiDopo.mp4",
        gif"rphjb_FaccioUnCasinoMoltoCalmoPoiDopoGif.mp4"
      ),
      ReplyBundleMessage.textToMedia[F](
        "due[,]? non una",
        "coltello",
        "vecchi guerrieri",
        "su mia direzione"
      )(
        mp3"rphjb_DueTrasmissioniColtelliBastoneInfernale.mp3",
        vid"rphjb_DueTrasmissioniColtelliBastoneInfernale.mp4"
      ),
      ReplyBundleMessage.textToMedia[F](
        "san[ ]?(remo|romolo)".r.tr(7),
        "che volete di pi[uù]".r.tr(17),
        "innervosire",
        "arrabbiare"
      )(
        mp3"rphjb_CheVoleteDiPiuInnervosireSanRemo.mp3",
        vid"rphjb_CheVoleteDiPiuInnervosireSanRemo.mp4"
      ),
      ReplyBundleMessage.textToMedia[F](
        "michael harris",
        "randy coven",
        "john maccaluso"
      )(
        mp3"rphjb_CheVoleteDiPiuInnervosireSanRemo.mp3",
        vid"rphjb_CheVoleteDiPiuInnervosireSanRemo.mp4",
        gif"rphjb_FormazioneDaGuerraVitalijGif.mp4",
        mp3"rphjb_FormazioneDaGuerraVitalij.mp3",
        vid"rphjb_FormazioneDaGuerraVitalij.mp4",
        mp3"rphjb_GuerraPiuTotale.mp3",
        gif"rphjb_GuerraPiuTotaleGif.mp4",
        vid"rphjb_GuerraPiuTotale.mp4"
      ),
      ReplyBundleMessage.textToMedia[F](
        "vitalij kuprij"
      )(
        mp3"rphjb_CheVoleteDiPiuInnervosireSanRemo.mp3",
        vid"rphjb_CheVoleteDiPiuInnervosireSanRemo.mp4",
        gif"rphjb_FormazioneDaGuerraVitalijGif.mp4",
        mp3"rphjb_FormazioneDaGuerraVitalij.mp3",
        vid"rphjb_FormazioneDaGuerraVitalij.mp4",
        gif"rphjb_ConForzaDinamismoRabbiaVitalijGif.mp4",
        mp3"rphjb_ConForzaDinamismoRabbiaVitalij.mp3",
        vid"rphjb_ConForzaDinamismoRabbiaVitalij.mp4",
        mp3"rphjb_GuerraPiuTotale.mp3",
        gif"rphjb_GuerraPiuTotaleGif.mp4",
        vid"rphjb_GuerraPiuTotale.mp4"
      ),
      ReplyBundleMessage.textToMedia[F](
        "ancora no!"
      )(
        mp3"rphjb_AncoraNo.mp3",
        vid"rphjb_AncoraNo.mp4",
        gif"rphjb_AncoraNoGif.mp4"
      ),
      ReplyBundleMessage.textToMedia[F](
        "yodel",
        "yodle",
        "lugano",
        "che vogliono di pi[uù]".r.tr(19),
        "svizzera",
        "alex masi"
      )(
        mp3"rphjb_Yodle.mp3",
        vid"rphjb_Yodle.mp4"
      ),
      ReplyBundleMessage.textToMedia[F](
        "precede il sa[b]+ato".r.tr(17),
        "induce ancora di pi[uù]".r.tr(20),
        "c'ha qualcosa in pi[uù]".r.tr(20),
        "fare danno"
      )(
        mp3"rphjb_VenerdiUscirePeggioCoseDanno.mp3",
        vid"rphjb_VenerdiUscirePeggioCoseDanno.mp4",
        gif"rphjb_VenerdiUscirePeggioCoseDannoGif.mp4"
      ),
      ReplyBundleMessage.textToMedia[F](
        "ad uscire"
      )(
        mp3"rphjb_VenerdiUscirePeggioCoseDanno.mp3",
        vid"rphjb_VenerdiUscirePeggioCoseDanno.mp4",
        gif"rphjb_VenerdiUscirePeggioCoseDannoGif.mp4",
        gif"rphjb_IlPrimoInCantinaGif.mp4",
        vid"rphjb_IlPrimoInCantina.mp4",
        mp3"rphjb_IlPrimoInCantina.mp3"
      ),
      ReplyBundleMessage.textToMedia[F](
        "gente vera",
        "urlare fuori",
        "la rabbia"
      )(
        mp3"rphjb_UrlareLaRabbia.mp3",
        vid"rphjb_UrlareLaRabbia.mp4"
      ),
      ReplyBundleMessage.textToMedia[F](
        "\\btuffo\\b".r.tr(5)
      )(
        mp3"rphjb_Tuffo.mp3",
        vid"rphjb_Tuffo.mp4",
        gif"rphjb_TuffoGif.mp4",
        gif"rphjb_TuffoReverseGif.mp4"
      ),
      ReplyBundleMessage.textToMedia[F](
        "tullio pane",
        "otello profazio",
        "mario lanza"
      )(
        vid"rphjb_RapMusicaMelodicaListaCantanti.mp4",
        vid"rphjb_QuesitoRegaloOtelloProfazioMarioLanzaTullioPaneLucianoTaglioliGianniCeleste.mp4",
        mp3"rphjb_LirismoItaliano.mp3",
        vid"rphjb_LirismoItaliano.mp4"
      ),
      ReplyBundleMessage.textToMedia[F](
        "jordan rudess"
      )(
        vid"rphjb_RadioRockErrori.mp4",
        vid"rphjb_FotoLookDreamTheater.mp4",
        mp3"rphjb_Tastieristi.mp3",
        vid"rphjb_Tastieristi.mp4"
      ),
      ReplyBundleMessage.textToMedia[F](
        "peggio[ ]?cose".r.tr(10)
      )(
        vid"rphjb_Venerdi.mp4",
        vid"rphjb_DanzaMacabra.mp4",
        mp3"rphjb_VenerdiUscirePeggioCoseDanno.mp3",
        vid"rphjb_VenerdiUscirePeggioCoseDanno.mp4",
        gif"rphjb_VenerdiUscirePeggioCoseDannoGif.mp4",
        gif"rphjb_SuccedonoLePeggioCoseGif.mp4",
        vid"rphjb_SuccedonoLePeggioCose.mp4",
        mp3"rphjb_SuccedonoLePeggioCose.mp3"
      ),
      ReplyBundleMessage.textToMedia[F](
        "schifosa"
      )(
        vid"rphjb_GenteSchifosa.mp4",
        vid"rphjb_QuestoNoETroppoIndicibileSchifosa.mp4",
        mp3"rphjb_TimoreDirigenti.mp3",
        vid"rphjb_TimoreDirigenti.mp4",
        gif"rphjb_TimoreDirigentiGif.mp4",
        gif"rphjb_UnaCosaSchifosaTerribileGif.mp4",
        vid"rphjb_UnaCosaSchifosaTerribile.mp4",
        mp3"rphjb_UnaCosaSchifosaTerribile.mp3"
      )
    )

  private def messageRepliesMixData5[F[_]: Applicative]: List[ReplyBundleMessage[F]] =
    List(
      ReplyBundleMessage.textToMedia[F](
        "che si deve f(à|are)".r.tr(14),
        "campà"
      )(
        gif"rphjb_NonLiSopportoGif.mp4",
        mp3"rphjb_NonLiSopporto.mp3",
        vid"rphjb_NonLiSopporto.mp4"
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
        "ancora ne parliamo",
        "dopo tutti questi anni"
      )(
        mp3"rphjb_ChiECristo.mp3",
        vid"rphjb_GiudaFrocio.mp4"
      ),
      ReplyBundleMessage.textToMedia[F](
        "danza macabra",
        "💃",
        "🕺"
      )(
        gif"rphjb_DanzaMacabraGif.mp4",
        vid"rphjb_DanzaMacabra.mp4",
        mp3"rphjb_DanzaMacabra.mp3"
      ),
      ReplyBundleMessage.textToMedia[F](
        "steve vai"
      )(
        vid"rphjb_SteveVaiRiciclando.mp4",
        vid"rphjb_AmoreSuonareFreddoBuddistiSchifoso.mp4",
        vid"rphjb_GambeInesistentiDueOssa.mp4",
        mp3"rphjb_DueOssa.mp3",
        gif"rphjb_NoteGif.mp4",
        vid"rphjb_TraTutteLeNote.mp4",
        vid"rphjb_Paradosso.mp4",
        vid"rphjb_RelIllusions.mp4",
        gif"rphjb_TiDeviSpaventareGif.mp4",
        mp3"rphjb_TiDeviSpaventare.mp3",
        vid"rphjb_SteveVaiRamazzotti.mp4",
        vid"rphjb_FiguracceDiscoSteveVai.mp4",
        vid"rphjb_SembraCadavereFassinoRitrattoSalute.mp4",
        vid"rphjb_FesteACasaNicolaArigliano.mp4",
        vid"rphjb_SteveVai.mp4",
        mp3"rphjb_SteveVai.mp3",
        gif"rphjb_SteveVaiGif.mp4",
        mp3"rphjb_Feelings.mp3",
        vid"rphjb_FeelingsIncazzarmiAndiamociSentireOriginale.mp4",
        gif"rphjb_FeelingsIncazzarmiAndiamociSentireOriginaleGif.mp4",
        mp3"rphjb_FeelingsIncazzarmiAndiamociSentireOriginale.mp3",
        gif"rphjb_FeelingsPreghieraGif.mp4",
        vid"rphjb_FeelingsPreghiera.mp4",
        gif"rphjb_FeelingsATerraGif.mp4",
        vid"rphjb_FeelingsATerra.mp4",
        gif"rphjb_FeelingsSputoLoopGif.mp4",
        gif"rphjb_FeelingsSputoGif.mp4",
        vid"rphjb_FeelingsSputo.mp4",
        gif"rphjb_FeelingsSguardoGif.mp4",
        vid"rphjb_FeelingsSguardo.mp4"
      ),
      ReplyBundleMessage.textToMedia[F](
        "(matto|pazzo)".r.tr(5)
      )(
        gif"rphjb_StoDiventandoPazzoGif.mp4",
        vid"rphjb_CompletamentePazzo.mp4",
        vid"rphjb_CompletamentePazzo2.mp4",
        vid"rphjb_DiventoPazzoMattoSchifosiUltimi.mp4",
        mp3"rphjb_DiventoPazzoMattoSchifosiUltimi.mp3",
        sticker"rphjb_DiventandoPazzoBensoniani.sticker"
      ),
      ReplyBundleMessage.textToMedia[F](
        "vo[l]+[o]*[u]+[ou]*me".r.tr(6)
      )(
        mp3"rphjb_MenoVolume.mp3",
        vid"rphjb_VolumeTelevisori.mp4",
        gif"rphjb_VolumeAlMassimoGif.mp4",
        vid"rphjb_VolumeAlMassimo.mp4",
        mp3"rphjb_VolumeAlMassimo.mp3"
      ),
      ReplyBundleMessage.textToMedia[F](
        "televisor"
      )(
        vid"rphjb_VolumeTelevisori.mp4",
        gif"rphjb_VolumeAlMassimoGif.mp4",
        vid"rphjb_VolumeAlMassimo.mp4",
        mp3"rphjb_VolumeAlMassimo.mp3"
      ),
      ReplyBundleMessage.textToMedia[F](
        "solo il me(t|d)al".r.tr(13)
      )(
        gif"rphjb_GeneriMusicaliGif.mp4",
        vid"rphjb_GeneriMusicali2.mp4"
      ),
      ReplyBundleMessage.textToMedia[F](
        "generi musicali"
      )(
        gif"rphjb_GeneriMusicaliGif.mp4",
        vid"rphjb_GeneriMusicali2.mp4",
        mp3"rphjb_DoveStannoGliAppassionatiTanteMusiche.mp3",
        vid"rphjb_DoveStannoGliAppassionatiTanteMusiche.mp4"
      ),
      ReplyBundleMessage.textToMedia[F](
        "sorca",
        "patonza",
        "\\bfi[cg]a\\b".r.tr(4)
      )(
        gif"rphjb_SorcaLeccisoGif.mp4",
        vid"rphjb_SorcaLecciso2.mp4",
        vid"rphjb_FigaLarga.mp4",
        mp3"rphjb_FragolinaFichina.mp3",
        vid"rphjb_Sorca.mp4",
        vid"rphjb_LeccisoOffrire.mp4",
        gif"rphjb_VePiaceLaSorcaGif.mp4",
        vid"rphjb_VePiaceLaSorca.mp4",
        mp3"rphjb_VePiaceLaSorca.mp3",
        gif"rphjb_BisognoDiSorcaGif.mp4",
        vid"rphjb_BisognoDiSorca.mp4",
        mp3"rphjb_BisognoDiSorca.mp3",
        vid"rphjb_TiSeiFattaVedere.mp4",
        mp3"rphjb_TiSeiFattaVedere.mp3",
        gif"rphjb_TiSeiFattaVedereGif.mp4"
      ),
      ReplyBundleMessage.textToMedia[F](
        "lecciso"
      )(
        gif"rphjb_SorcaLeccisoGif.mp4",
        vid"rphjb_SorcaLecciso2.mp4",
        vid"rphjb_LeccisoOffrire.mp4"
      ),
      ReplyBundleMessage.textToMedia[F](
        "schifose",
        "ultime"
      )(
        gif"rphjb_SchifoseUltimeGif.mp4",
        vid"rphjb_SchifoseUltime2.mp4",
        vid"rphjb_ImparaASputareMignottaSchifose.mp4"
      ),
      ReplyBundleMessage.textToMedia[F](
        "e parl[a]+\\b".r.tr(7)
      )(
        gif"rphjb_ParlaGif.mp4",
        vid"rphjb_Parla2.mp4"
      ),
      ReplyBundleMessage.textToMedia[F](
        "cosa è successo",
        "\\bcosa[?]{1,}\\b".r.tr(5)
      )(
        gif"rphjb_CosaSuccessoGif.mp4",
        gif"rphjb_CosaGif.mp4",
        vid"rphjb_CosaCosaSuccessoMeNeVadoFacendoSoffrire.mp4",
        sticker"rphjb_CosaBensoniani.sticker"
      ),
      ReplyBundleMessage.textToMedia[F](
        "negozio",
        "pantaloni",
        "shopping"
      )(
        mp3"rphjb_Pantaloni.mp3",
        vid"rphjb_Pantaloni.mp4"
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
        gif"rphjb_AndateDaRatzinger2Gif.mp4",
        mp3"rphjb_AndateDaRatzinger.mp3"
      ),
      ReplyBundleMessage.textToMedia[F](
        "non è possibile"
      )(
        gif"rphjb_NonPossibileGif.mp4",
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
        vid"rphjb_Cameriera.mp4"
      ),
      ReplyBundleMessage.textToMedia[F](
        "cos(a)? hai trovato?".r.tr(16)
      )(
        gif"rphjb_CosHaiTrovatoGif.mp4",
        vid"rphjb_NonPossibile2.mp4"
      ),
      ReplyBundleMessage.textToMedia[F](
        "accetto (le|qualsiasi) critich[ea]".r.tr(17)
      )(
        gif"rphjb_EscertoGif.mp4",
        gif"rphjb_EscertoCritiche.mp4",
        vid"rphjb_CriticaNoCazzate.mp4"
      ),
      ReplyBundleMessage.textToMedia[F](
        "pronto[,]? dimmi".r.tr(12)
      )(
        vid"rphjb_ProntoDimmi2.mp4",
        gif"rphjb_ProntoDimmiGif.mp4"
      ),
      ReplyBundleMessage.textToMedia[F](
        "slap"
      )(
        gif"rphjb_Bassista2Gif.mp4",
        vid"rphjb_Bassista2.mp4",
        vid"rphjb_Bassista.mp4",
        mp3"rphjb_Bassista.mp3"
      ),
      ReplyBundleMessage.textToMedia[F](
        "bassista"
      )(
        vid"rphjb_Bassista2.mp4",
        gif"rphjb_Bassista2Gif.mp4",
        vid"rphjb_GruppiNuoviSpaccanoCuloAbbellimentiRivoltiMordentiContrappunti.mp4",
        vid"rphjb_Bassista.mp4",
        mp3"rphjb_Bassista.mp3"
      ),
      ReplyBundleMessage.textToMedia[F](
        "è vero[!?]+".r.tr(6)
      )(
        gif"rphjb_VeroGif.mp4",
        vid"rphjb_EraVero.mp4",
        mp3"rphjb_SuonatoAbbastanzaBeneEVero.mp3"
      ),
      ReplyBundleMessage.textToMedia[F](
        "perchè (cazzo)? mi hai fatto ven[i|ì](re)?".r.tr(24)
      )(
        mp3"rphjb_PercheCazzoMiHaiFattoVeni.mp3",
        gif"rphjb_PercheCazzoMiHaiFattoVeniGif.mp4"
      ),
      ReplyBundleMessage.textToMedia[F](
        "e[sc]+erto".r.tr(6)
      )(
        gif"rphjb_EscertoGif.mp4",
        vid"rphjb_EscertoCritiche.mp4"
      ),
      ReplyBundleMessage.textToMedia[F](
        "decido io"
      )(
        gif"rphjb_DecidoIoGif.mp4",
        vid"rphjb_DecidoIoMareCazzatePerCortesia.mp4"
      ),
      ReplyBundleMessage.textToMedia[F](
        "mi piaccio",
        "impazzire"
      )(
        gif"rphjb_MiPiaccioGif.mp4",
        vid"rphjb_MiPiaccio2.mp4"
      ),
      ReplyBundleMessage.textToMedia[F](
        "giudica"
      )(
        gif"rphjb_GiudicateGif.mp4",
        gif"rphjb_ComeFaiAGiudicareGif.mp4",
        gif"rphjb_ComeFaiAGiudicareGif.mp4",
        vid"rphjb_NonPoteteGiudicarUrloThatsGood.mp4",
        mp3"rphjb_SareteVoiAGiudicare.mp3",
        gif"rphjb_SareteVoiAGiudicareGif.mp4",
        vid"rphjb_SareteVoiAGiudicare.mp4"
      ),
      ReplyBundleMessage.textToMedia[F](
        "fregare come un co(gl|j)ione".r.tr(22)
      )(
        vid"rphjb_GesuCoglione.mp4",
        mp3"rphjb_GesuCoglione.mp3"
      ),
      ReplyBundleMessage.textToMedia[F](
        "ges[uùù]".r.tr(4)
      )(
        vid"rphjb_GesuCoglione.mp4",
        mp3"rphjb_GesuCoglione.mp3",
        vid"rphjb_IlBastoneDiGesu.mp4",
        mp3"rphjb_IlBastoneDiGesu.mp3",
        gif"rphjb_NoCriticaComeGesuCristoGif.mp4",
        vid"rphjb_NoCriticaComeGesuCristo.mp4",
        mp3"rphjb_NoCriticaComeGesuCristo.mp3"
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
        "bucati[,]? ma da quale chiodo".r.tr(25)
      )(
        vid"rphjb_GerarchieInfernali.mp4",
        mp3"rphjb_GerarchieInfernali.mp3",
        vid"rphjb_GerarchieInfernali2.mp4",
        vid"rphjb_GerarchieInfernali3.mp4"
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
        "m[ei] levo tutto".r.tr(13)
      )(
        vid"rphjb_Platinette.mp4",
        mp3"rphjb_Platinette.mp3"
      ),
      ReplyBundleMessage.textToMedia[F](
        "abbellimenti",
        "mordenti",
        "rivolti"
      )(
        vid"rphjb_AbbellimentiRivoltiRivoliMordentiImpennateColori.mp4",
        vid"rphjb_GruppiNuoviSpaccanoCuloAbbellimentiRivoltiMordentiContrappunti.mp4",
        vid"rphjb_PreparazioneRivoltiMordentiAlterazioniContrappunti.mp4",
        mp3"rphjb_PreparazioneRivoltiMordentiAlterazioniContrappunti.mp3",
        mp3"rphjb_SceltaDelleNote.mp3",
        gif"rphjb_SceltaDelleNoteGif.mp4",
        vid"rphjb_SceltaDelleNote.mp4"
      ),
      ReplyBundleMessage.textToMedia[F](
        "contrappunt[oi]".r.tr(12),
        "alterazioni",
        "armoni[ae]".r.tr(7)
      )(
        vid"rphjb_GruppiNuoviSpaccanoCuloAbbellimentiRivoltiMordentiContrappunti.mp4",
        vid"rphjb_PreparazioneRivoltiMordentiAlterazioniContrappunti.mp4",
        mp3"rphjb_PreparazioneRivoltiMordentiAlterazioniContrappunti.mp3",
        mp3"rphjb_SceltaDelleNote.mp3",
        gif"rphjb_SceltaDelleNoteGif.mp4",
        vid"rphjb_SceltaDelleNote.mp4"
      ),
      ReplyBundleMessage
        .textToMedia[F](
          "accordi",
          "tutto quello che volete",
          "la scelta di tutto"
        )(
          mp3"rphjb_SceltaDelleNote.mp3",
          gif"rphjb_SceltaDelleNoteGif.mp4",
          vid"rphjb_SceltaDelleNote.mp4"
        ),
      ReplyBundleMessage
        .textToMedia[F](
          "non c'entra niente"
        )(
          mp3"rphjb_TeLoVoglioRaccontareAncheSeNonCentraNiente.mp3",
          gif"rphjb_TeLoVoglioRaccontareAncheSeNonCentraNienteGif.mp4",
          vid"rphjb_TeLoVoglioRaccontareAncheSeNonCentraNiente.mp4"
        ),
      ReplyBundleMessage.textToMedia[F](
        "ciao bella",
        "\\bcome va\\b".r.tr(7),
        "bella gioia"
      )(
        vid"rphjb_CiaoBellaCameVaBellaGioia.mp4",
        mp3"rphjb_CiaoBellaGioia.mp3",
        gif"rphjb_CiaoBellaGioiaGif.mp4",
        vid"rphjb_CiaoBellaGioia.mp4"
      ),
      ReplyBundleMessage
        .textToMedia[F](
          "le vedute",
          "i piedi in testa"
        )(
          gif"rphjb_AllargareLeVeduteGif.mp4",
          mp3"rphjb_AllargareLeVedute.mp3",
          vid"rphjb_AllargareLeVedute.mp4"
        ),
      ReplyBundleMessage
        .textToMedia[F](
          "country",
          "bluegrass"
        )(
          mp3"rphjb_DoveStannoGliAppassionatiTanteMusiche.mp3",
          vid"rphjb_DoveStannoGliAppassionatiTanteMusiche.mp4"
        ),
      ReplyBundleMessage
        .textToMedia[F](
          "accontentarvi",
          "nessuno (si |s')accontenta".r.tr(20)
        )(
          mp3"rphjb_ComeFateAdAccontentarvi.mp3",
          gif"rphjb_ComeFateAdAccontentarviGif.mp4",
          vid"rphjb_ComeFateAdAccontentarvi.mp4"
        ),
      ReplyBundleMessage
        .textToMedia[F](
          "non m[ei] c[ei] f[aà](r|re)? pens[aà](re)?".r.tr(18),
          "non farmici pens[aà](re)?".r.tr(19) // non farmici pensare
        )(
          mp3"rphjb_IncazzoComeUnaBestia.mp3",
          gif"rphjb_IncazzoComeUnaBestiaGif.mp4",
          vid"rphjb_IncazzoComeUnaBestia.mp4",
          mp3"rphjb_SchifosiNonMeCeFaPensa.mp3",
          gif"rphjb_SchifosiNonMeCeFaPensaGif.mp4",
          vid"rphjb_SchifosiNonMeCeFaPensa.mp4"
        ),
      ReplyBundleMessage.textToMedia[F](
        "come mai"
      )(
        mp3"rphjb_ComeMai.mp3",
        gif"rphjb_ComeMaiGif.mp4",
        vid"rphjb_ComeMai.mp4"
      ),
      ReplyBundleMessage.textToMedia[F](
        "completamente nudo",
        "prossimo (concerto|live)".r.tr(13)
      )(
        mp3"rphjb_CompletamenteNudo.mp3",
        vid"rphjb_DifettiLeucemiaNudoFrocio.mp4",
        gif"rphjb_DifettiLeucemiaNudoFrocioGif.mp4",
        mp3"rphjb_DifettiLeucemiaNudoFrocioAudio.mp3"
      ),
      ReplyBundleMessage
        .textToMedia[F](
          "difetto",
          "leucemia",
          "stai male",
          "nascondere (molto )?bene"
        )(
          vid"rphjb_DifettiLeucemiaNudoFrocio.mp4",
          gif"rphjb_DifettiLeucemiaNudoFrocioGif.mp4",
          mp3"rphjb_DifettiLeucemiaNudoFrocioAudio.mp3"
        ),
      ReplyBundleMessage
        .textToMedia[F](
          "formazione da guerra",
          "quartetto da guerra"
        )(
          gif"rphjb_FormazioneDaGuerraVitalijGif.mp4",
          mp3"rphjb_FormazioneDaGuerraVitalij.mp3",
          vid"rphjb_FormazioneDaGuerraVitalij.mp4"
        ),
      ReplyBundleMessage
        .textToMedia[F](
          "non dorm[eo] (quasi )?tutta la notte".r.tr(24)
        )(
          mp3"rphjb_NonDormoQuasiTuttaLaNotteAiutatemi.mp3",
          vid"rphjb_NonDormoQuasiTuttaLaNotteAiutatemi.mp4"
        ),
      ReplyBundleMessage
        .textToMedia[F](
          "si (è )?dimostra(to)? (gentile|volenteroso)".r.tr(19),
          "tutto il contrario",
          "schifo aberrante",
          "mi contraddico"
        )(
          gif"rphjb_PersonaVoltafacciaMiContraddicoGif.mp4",
          mp3"rphjb_PersonaVoltafacciaMiContraddico.mp3",
          vid"rphjb_PersonaVoltafacciaMiContraddico.mp4"
        ),
      ReplyBundleMessage
        .textToMedia[F](
          "troppo famoso",
          "conoscono in troppi",
          "nuove leve"
        )(
          mp3"rphjb_TroppoFamosoNuoveLeve.mp3",
          gif"rphjb_TroppoFamosoNuoveLeveGif.mp4",
          vid"rphjb_TroppoFamosoNuoveLeve.mp4"
        ),
      ReplyBundleMessage
        .textToMedia[F](
          "basta and[aà](re)? a cerc[aà](re)?".r.tr(18),
          "non v[ei] v[aà] d[ei] f[aà] un cazzo".r.tr(24)
        )(
          mp3"rphjb_BastaAndaACercaNonViVa.mp3",
          gif"rphjb_BastaAndaACercaNonViVaGif.mp4",
          vid"rphjb_BastaAndaACercaNonViVa.mp4"
        ),
      ReplyBundleMessage
        .textToMedia[F](
          "mi ripeto",
          "orecchie attizzate",
          "sentire quello che dico",
          "formule musicali",
          "sotto terra",
          "pi[uù] commerciali".r.tr(15),
          "\\bbieche\\b".r.tr(6),
          "va ripetuta",
          "ho gi[aà] detto".r.tr(12)
        )(
          mp3"rphjb_MiRipetoFormuleMusicaliBiecheCommerciali6PiediSottoTerra.mp3",
          vid"rphjb_MiRipetoFormuleMusicaliBiecheCommerciali6PiediSottoTerra.mp4"
        ),
      ReplyBundleMessage.textToMedia[F](
        "pelle d'oca",
        "sussult",
        "brivid"
      )(
        gif"rphjb_BrividoGif.mp4",
        mp3"rphjb_PoesiaStrappareUnaLacrima.mp3",
        vid"rphjb_PoesiaStrappareUnaLacrima.mp4"
      ),
      ReplyBundleMessage
        .textToMedia[F](
          "strappare un sentimento",
          "intonaco dei palazzi",
          "grigio perla",
          "un'atmosfera (ambigua|contorta)".r.tr(20),
          "in un'ospedale",
          "in rabbia",
          "in aggressivit[aà]".r.tr(15),
          "voglia di distruzione",
          "voltato le spalle",
          "momento del bisogno"
        )(
          mp3"rphjb_PoesiaStrappareUnaLacrima.mp3",
          vid"rphjb_PoesiaStrappareUnaLacrima.mp4"
        ),
      ReplyBundleMessage
        .textToMedia[F](
          "una lacrima"
        )(
          mp3"rphjb_PoesiaStrappareUnaLacrima.mp3",
          vid"rphjb_PoesiaStrappareUnaLacrima.mp4",
          gif"rphjb_LacrimaSullaGuanciaGif.mp4",
          vid"rphjb_LacrimaSullaGuancia.mp4",
          mp3"rphjb_LacrimaSullaGuancia.mp3"
        ),
      ReplyBundleMessage.textToMedia[F]("(col|con il) sangue".r.tr(10))(
        vid"rphjb_ColSangue.mp4",
        gif"rphjb_ConForzaDinamismoRabbiaVitalijGif.mp4",
        mp3"rphjb_ConForzaDinamismoRabbiaVitalij.mp3",
        vid"rphjb_ConForzaDinamismoRabbiaVitalij.mp4",
        vid"rphjb_RollingStonesJimiHendrixPoetiMaledettiFemministeControculturaRivoluzioniRibelioni5DitaCazzottoInFacciaGiustiziere.mp4"
      ),
      ReplyBundleMessage
        .textToMedia[F](
          "con forza",
          "con dinamismo",
          "voglia animalesca",
          "ribolle nelle vene",
          "il verbo",
          "labbiale",
          "rabbia che esce",
          "qualcosa di dolce"
        )(
          gif"rphjb_ConForzaDinamismoRabbiaVitalijGif.mp4",
          mp3"rphjb_ConForzaDinamismoRabbiaVitalij.mp3",
          vid"rphjb_ConForzaDinamismoRabbiaVitalij.mp4"
        ),
      ReplyBundleMessage.textToMedia[F](
        "una sera"
      )(
        mp3"rphjb_Sera.mp3",
        mp3"rphjb_AdolfHitler.mp3",
        vid"rphjb_AdolfHitler.mp4"
      ),
      ReplyBundleMessage
        .textToMedia[F](
          "alla frutta"
        )(
          mp3"rphjb_GenteArrivataAllaFruttaColCervello.mp3",
          gif"rphjb_GenteArrivataAllaFruttaColCervelloGif.mp4",
          vid"rphjb_GenteArrivataAllaFruttaColCervello.mp4"
        ),
      ReplyBundleMessage
        .textToMedia[F]("yngwie", "malmsteen")(
          vid"rphjb_Ramarro.mp4",
          vid"rphjb_CanzoniNataleStavaMaleMalmsteen.mp4",
          vid"rphjb_BarzellettaGesuCristoParadisoPurgatorioMalmsteenDio.mp4",
          vid"rphjb_FotoMalmsteen.mp4",
          vid"rphjb_TommyAldridgeYngwieMalmsteenWhereAreYouGoing.mp4",
          mp3"rphjb_TracciaNuoveStradeKikoLoureiro.mp3",
          gif"rphjb_TracciaNuoveStradeKikoLoureiroGif.mp4",
          vid"rphjb_TracciaNuoveStradeKikoLoureiro.mp4"
        ),
      ReplyBundleMessage.textToMedia[F](
        "cervello pensante",
        "stupidità",
        "incresciosa",
        "sfogo di liberazione",
        "quinto pezzo"
      )(
        mp3"rphjb_CervelloPensante.mp3",
        vid"rphjb_CervelloPensante.mp4"
      ),
      ReplyBundleMessage.textToMedia[F](
        "far[e]? (ridere|gioire)".r.tr(10)
      )(
        mp3"rphjb_CervelloPensante.mp3",
        vid"rphjb_CervelloPensante.mp4",
        vid"rphjb_VogliaDiFarRidere.mp4",
        mp3"rphjb_VogliaDiFarRidere.mp3"
      ),
      ReplyBundleMessage.textToMedia[F](
        "chi risponde",
        "quesito",
        "in (regalo|premio)".r.tr(9)
      )(
        vid"rphjb_QuesitoRegaloOtelloProfazioMarioLanzaTullioPaneLucianoTaglioliGianniCeleste.mp4",
        gif"rphjb_SolitoPremioGianniCelesteGif.mp4",
        vid"rphjb_SolitoPremioGianniCeleste.mp4",
        mp3"rphjb_SolitoPremioGianniCeleste.mp3"
      ),
      ReplyBundleMessage
        .textToMedia[F](
          "grecia",
          "🇬🇷"
        )(
          gif"rphjb_VivaLaGreciaGif.mp4",
          vid"rphjb_VivaLaGrecia.mp4",
          mp3"rphjb_VivaLaGrecia.mp3"
        ),
      ReplyBundleMessage.textToMedia[F](
        "distrutto totalmente",
        "quello che ti meriti"
      )(
        vid"rphjb_QuelloCheTiMeritiFinoAllaFineDistruttoTotalmente.mp4",
        mp3"rphjb_QuelloCheTiMeritiFinoAllaFineDistruttoTotalmente.mp3",
        gif"rphjb_QuelloCheTiMeritiFinoAllaFineDistruttoTotalmenteGif.mp4"
      ),
      ReplyBundleMessage
        .textToMedia[F](
          "arriva(ndo)? a lui".r.tr(12)
        )(
          vid"rphjb_AngeloCarpenelliArrivaALui.mp4",
          mp3"rphjb_AngeloCarpenelliArrivaALui.mp3"
        ),
      ReplyBundleMessage
        .textToMedia[F](
          "è casa mia",
          "dove (io )?mi trovo bene".r.tr(18),
          "da qui parte tutto"
        )(
          gif"rphjb_CasaMiaGif.mp4",
          vid"rphjb_CasaMia.mp4",
          mp3"rphjb_CasaMia.mp3"
        ),
      ReplyBundleMessage
        .textToMedia[F](
          "voglia di divertire",
          "idee scarse",
          "maniera bieca"
        )(
          vid"rphjb_VogliaDiFarRidere.mp4",
          mp3"rphjb_VogliaDiFarRidere.mp3"
        ),
      ReplyBundleMessage.textToMedia[F](
        "orecchie sensibili",
        "rumore delle lacrime"
      )(
        vid"rphjb_OrecchieSensibiliRumoreLacrime.mp4",
        gif"rphjb_RumoreDelleLacrimeGif.mp4",
        vid"rphjb_RumoreDelleLacrime.mp4",
        mp3"rphjb_RumoreDelleLacrime.mp3",
        vid"rphjb_RumoreDelleLacrimeDegliAltri2.mp4",
        vid"rphjb_RumoreDelleLacrimeDegliAltri3.mp4"
      ),
      ReplyBundleMessage
        .textToMedia[F](
          "edizione limitata"
        )(
          gif"rphjb_EdizioneLimitataGif.mp4",
          vid"rphjb_EdizioneLimitata.mp4",
          mp3"rphjb_EdizioneLimitata.mp3"
        ),
      ReplyBundleMessage
        .textToMedia[F](
          "sessuofobico",
          "sessossesione",
          "culo (un po' )?chiacchierato".r.tr(18)
        )(
          vid"rphjb_CuloChiacchierato.mp4",
          mp3"rphjb_CuloChiacchierato.mp3"
        ),
      ReplyBundleMessage.textToMedia[F](
        "cazzi tuoi",
        "cazz[oi] piccol[io]".r.tr(13)
      )(
        vid"rphjb_CazziTuoiPiccoliEssereUmanoMinimo.mp4",
        mp3"rphjb_CazziTuoiPiccoliEssereUmanoMinimo.mp3"
      ),
      ReplyBundleMessage.textToMedia[F](
        "essere umano"
      )(
        vid"rphjb_CazziTuoiPiccoliEssereUmanoMinimo.mp4",
        mp3"rphjb_CazziTuoiPiccoliEssereUmanoMinimo.mp3",
        vid"rphjb_ScusateEssereUmanoTosse.mp4",
        mp3"rphjb_ScusateEssereUmanoTosse.mp3"
      ),
      ReplyBundleMessage
        .textToMedia[F](
          "mi accompagnavi"
        )(
          gif"rphjb_AccompagnaviRitardoFiguracciaGif.mp4",
          vid"rphjb_AccompagnaviRitardoFiguraccia.mp4",
          mp3"rphjb_AccompagnaviRitardoFiguraccia.mp3"
        ),
      ReplyBundleMessage.textToMedia[F](
        "in cantina"
      )(
        vid"rphjb_InCantina.mp4",
        gif"rphjb_IlPrimoInCantinaGif.mp4",
        vid"rphjb_IlPrimoInCantina.mp4",
        mp3"rphjb_IlPrimoInCantina.mp3"
      ),
      ReplyBundleMessage
        .textToMedia[F](
          "si tratta di suonare",
          "il primo ad andare",
          "sorci",
          "l'ultimo ad uscire"
        )(
          gif"rphjb_IlPrimoInCantinaGif.mp4",
          vid"rphjb_IlPrimoInCantina.mp4",
          mp3"rphjb_IlPrimoInCantina.mp3"
        ),
      ReplyBundleMessage
        .textToMedia[F](
          "l'amplificatore"
        )(
          gif"rphjb_IlPrimoInCantinaGif.mp4",
          vid"rphjb_IlPrimoInCantina.mp4",
          mp3"rphjb_IlPrimoInCantina.mp3",
          vid"rphjb_AncoraUnAltraCassa.mp4",
          mp3"rphjb_AncoraUnAltraCassa.mp3"
        ),
      ReplyBundleMessage
        .textToMedia[F](
          "tutte le scale",
          "(armonic|melodic)(he|a) minor[ie]".r.tr(15),
          "diminuit[ae]",
          "esatonal[ei]",
          "pentatonica",
          "scala blues",
          "cromatic(he|a)"
        )(
          vid"rphjb_PreparazioneRivoltiMordentiAlterazioniContrappunti.mp4",
          mp3"rphjb_PreparazioneRivoltiMordentiAlterazioniContrappunti.mp3"
        ),
      ReplyBundleMessage
        .textToMedia[F](
          "babele",
          "non ci capiamo pi[ùu]",
          "\\be allora!".r.tr(9)
        )(
          gif"rphjb_TorreDiBabeleGif.mp4",
          vid"rphjb_TorreDiBabele.mp4",
          mp3"rphjb_TorreDiBabele.mp3"
        ),
      ReplyBundleMessage
        .textToMedia[F](
          "manco un('| )euro".r.tr(13),
          "piccoletto",
          "timido",
          "non parla\\b".r.tr(9),
          "che parlano poco"
        )(
          gif"rphjb_QuelliCheParlanoPocoGif.mp4",
          vid"rphjb_QuelliCheParlanoPoco.mp4",
          mp3"rphjb_QuelliCheParlanoPoco.mp3"
        ),
      ReplyBundleMessage.textToMedia[F](
        "suonare libero"
      )(
        gif"rphjb_SognareSuonareLiberoGif.mp4",
        vid"rphjb_SognareSuonareLibero.mp4",
        mp3"rphjb_SognareSuonareLibero.mp3"
      ),
      ReplyBundleMessage
        .textToMedia[F](
          "contro (il|er) demonio".r.tr(17),
          "ritornato se stesso"
        )(
          gif"rphjb_LucaDiNoiaControDemonioGif.mp4",
          vid"rphjb_LucaDiNoiaControDemonio.mp4",
          mp3"rphjb_LucaDiNoiaControDemonio.mp3"
        ),
      ReplyBundleMessage.textToMedia[F](
        "dovete soffrire",
        "vi voglio far(e)? soffrire".r.tr(23),
        "soffrite",
        "sulla guancia"
      )(
        gif"rphjb_DoveteSoffrireGif.mp4",
        gif"rphjb_LacrimaSullaGuanciaGif.mp4",
        vid"rphjb_LacrimaSullaGuancia.mp4",
        mp3"rphjb_LacrimaSullaGuancia.mp3"
      ),
      ReplyBundleMessage.textToMedia[F](
        "televisione"
      )(
        mp3"rphjb_BaracconeFurgoneTelevisione.mp3",
        gif"rphjb_LacrimaSullaGuanciaGif.mp4",
        vid"rphjb_LacrimaSullaGuancia.mp4",
        mp3"rphjb_LacrimaSullaGuancia.mp3"
      ),
      ReplyBundleMessage
        .textToMedia[F](
          "vuoi sentire",
          "cosa so fare",
          "vuoi vedere",
          "francese",
          "tremendo"
        )(
          vid"rphjb_AssoliFrancese.mp4",
          mp3"rphjb_AssoliFrancese.mp3"
        ),
      ReplyBundleMessage.textToMedia[F](
        "van der graaf generator",
        "jethro tull"
      )(vid"rphjb_ListaGruppi.mp4", vid"rphjb_Regressive.mp4", mp3"rphjb_ListaGruppi.mp3"),
      ReplyBundleMessage
        .textToMedia[F](
          "mi da un consiglio",
          "figuriamoci",
          "se mi viene voglia",
          "seguire qualcuno",
          "quant'è dura",
          "è proprio dura",
          "vitella"
        )(
          gif"rphjb_QuanteDuraLaVitellaGif.mp4",
          vid"rphjb_QuanteDuraLaVitella.mp4",
          mp3"rphjb_QuanteDuraLaVitella.mp3"
        ),
      ReplyBundleMessage
        .textToMedia[F](
          "\\bconsigli(o|ato)?\\b".r.tr(8)
        )(
          gif"rphjb_QuanteDuraLaVitellaGif.mp4",
          vid"rphjb_QuanteDuraLaVitella.mp4",
          mp3"rphjb_QuanteDuraLaVitella.mp3",
          vid"rphjb_NoConsigli.mp4"
        ),
      ReplyBundleMessage
        .textToMedia[F](
          "quadr[oi]".r.tr(6),
          "i colori",
          "le tele",
          "le cornici",
          "fare un'altro lavoro",
          "fare l'artista"
        )(
          vid"rphjb_QuadriCambiare.mp4",
          mp3"rphjb_QuadriCambiare.mp3"
        ),
      ReplyBundleMessage
        .textToMedia[F](
          "pezzo unico",
          "diviso in .*\\bpezzi\\b".r.tr(16),
          "\\b(43|quarantatre)\\b".r.tr(2)
        )(
          vid"rphjb_PezzoUnicoDiviso43Pezzi.mp4",
          mp3"rphjb_PezzoUnicoDiviso43Pezzi.mp3"
        ),
      ReplyBundleMessage
        .textToMedia[F](
          "guai a dire",
          "sono degli dei",
          "fare una critica",
          "ai miei (fans|amici)".r.tr(12),
          "(parlare|suonare) di (pi[uù]|meno)".r.tr(14)
        )(
          gif"rphjb_NoCriticaComeGesuCristoGif.mp4",
          vid"rphjb_NoCriticaComeGesuCristo.mp4",
          mp3"rphjb_NoCriticaComeGesuCristo.mp3"
        ),
      ReplyBundleMessage
        .textToMedia[F](
          "ottava nota"
        )(
          vid"rphjb_OttavaNotaRobaVecchiaSchifosi.mp4",
          mp3"rphjb_OttavaNotaRobaVecchiaSchifosi.mp3"
        ),
      ReplyBundleMessage
        .textToMedia[F](
          "tutto un dire"
        )(
          gif"rphjb_SuccedonoLePeggioCoseGif.mp4",
          vid"rphjb_SuccedonoLePeggioCose.mp4",
          mp3"rphjb_SuccedonoLePeggioCose.mp3"
        ),
      ReplyBundleMessage
        .textToMedia[F](
          "vi saluto",
          "tant(o |')amore",
          "niente fiori",
          "opere di bene",
          "mi capiscono",
          "non sono più scemi"
        )(
          vid"rphjb_ViSalutoNienteFioriSoloOpereDiBene.mp4",
          mp3"rphjb_ViSalutoNienteFioriSoloOpereDiBene.mp3"
        ),
      ReplyBundleMessage
        .textToMedia[F](
          "ha vinto su tutti"
        )(
          gif"rphjb_AngeloHaVintoSuTuttiGif.mp4",
          vid"rphjb_AngeloHaVintoSuTutti.mp4",
          mp3"rphjb_AngeloHaVintoSuTutti.mp3"
        ),
      ReplyBundleMessage
        .textToMedia[F](
          "cesare",
          "i gusti sono gusti",
          "leccandosi il culo"
        )(
          gif"rphjb_CesareDisseIGustiSonoGustiGif.mp4",
          vid"rphjb_CesareDisseIGustiSonoGusti.mp4",
          mp3"rphjb_CesareDisseIGustiSonoGusti.mp3"
        ),
      ReplyBundleMessage
        .textToMedia[F](
          "in pi[uù]!",
          "scocciato"
        )(
          gif"rphjb_InPiuUnaCosaCheMiHaScocciatoGif.mp4",
          vid"rphjb_InPiuUnaCosaCheMiHaScocciato.mp4",
          mp3"rphjb_InPiuUnaCosaCheMiHaScocciato.mp3"
        ),
      ReplyBundleMessage
        .textToMedia[F](
          "viene naturale",
          "(sar[aà]|cos'[eè]) (sbagliato|giusto)".r.tr(11),
          "una parolaccia",
          "arriva uno nuovo",
          " e cambia tutto"
        )(
          vid"rphjb_GiustoSbagliato.mp4",
          mp3"rphjb_GiustoSbagliato.mp3"
        ),
      ReplyBundleMessage.textToMedia[F](
        "gerarca"
      )(
        gif"rphjb_GerarcaPianoSuperioreGif.mp4",
        vid"rphjb_GerarcaPianoSuperiore.mp4",
        mp3"rphjb_GerarcaPianoSuperiore.mp3",
        gif"rphjb_GerarcaSeLaPrendeGif.mp4",
        vid"rphjb_GerarcaSeLaPrende.mp4",
        mp3"rphjb_GerarcaSeLaPrende.mp3"
      ),
      ReplyBundleMessage
        .textToMedia[F](
          "se la (prende|dovesse prendere)".r.tr(12)
        )(
          gif"rphjb_GerarcaSeLaPrendeGif.mp4",
          vid"rphjb_GerarcaSeLaPrende.mp4",
          mp3"rphjb_GerarcaSeLaPrende.mp3"
        ),
      ReplyBundleMessage
        .textToMedia[F](
          "cavalcata",
          "siamo pronti",
          "(o[h]? a[h]?|ah) bella".r.tr(8)
        )(
          gif"rphjb_SiamoProntiCavalcataGif.mp4",
          vid"rphjb_SiamoProntiCavalcata.mp4",
          mp3"rphjb_SiamoProntiCavalcata.mp3"
        ),
      ReplyBundleMessage
        .textToMedia[F](
          "dire di tutto",
          "grande frocio",
          "s[ei] divertono".r.tr(12)
        )(
          gif"rphjb_IncazzatoFeliciGif.mp4",
          vid"rphjb_IncazzatoFelici.mp4",
          mp3"rphjb_IncazzatoFelici.mp3"
        ),
      ReplyBundleMessage
        .textToMedia[F](
          "pescatori",
          "tant(a |')acqua",
          "niente pesce"
        )(
          gif"rphjb_DuePescatoriGif.mp4",
          vid"rphjb_DuePescatori.mp4",
          mp3"rphjb_DuePescatori.mp3"
        ),
      ReplyBundleMessage
        .textToMedia[F](
          "il rumore dei capelli",
          "non c'era (niente|nessun rumore)",
          "desolazione"
        )(
          vid"rphjb_RumoreDeiCapelliCheCascavano.mp4",
          mp3"rphjb_RumoreDeiCapelliCheCascavano.mp3"
        ),
      ReplyBundleMessage
        .textToMedia[F](
          "(i locali|il locale) (vuoti|pieni)".r.tr(14),
          "siae"
        )(
          vid"rphjb_ILocaliNonPagano.mp4",
          mp3"rphjb_ILocaliNonPagano.mp3"
        ),
      ReplyBundleMessage
        .textToMedia[F](
          "parlo (tanto|troppo)".r.tr(12),
          "mente libera",
          "sentire la musica"
        )(
          vid"rphjb_ParloTantoNoCritiche.mp4",
          mp3"rphjb_ParloTantoNoCritiche.mp3"
        ),
      ReplyBundleMessage
        .textToMedia[F](
          "doppiaggio",
          "belgio",
          "brussels",
          "praga",
          "vienna"
        )(
          vid"rphjb_ImpegniListaCitta.mp4",
          mp3"rphjb_ImpegniListaCitta.mp3"
        ),
      ReplyBundleMessage.textToMedia[F](
        "londra"
      )(
        vid"rphjb_Londra.mp4",
        mp3"rphjb_Londra.mp3",
        vid"rphjb_ImpegniListaCitta.mp4",
        mp3"rphjb_ImpegniListaCitta.mp3"
      ),
      ReplyBundleMessage.textToMedia[F](
        "invivi[b]+ile".r.tr(10),
        "ci sono stato",
        "(uscir|girar)[e]? la sera".r.tr(13),
        "non t(i |')ammazzano",
        "ammazzano sul serio",
        "figuriamoci in america",
        "(uscir|girar)[e]? armato".r.tr(12)
      )(
        vid"rphjb_Londra.mp4",
        mp3"rphjb_Londra.mp3"
      ),
      ReplyBundleMessage.textToMedia[F](
        "zurigo"
      )(
        vid"rphjb_ImpegniListaCitta.mp4",
        mp3"rphjb_ImpegniListaCitta.mp3",
        vid"rphjb_InvestitoreGoverno.mp4"
      ),
      ReplyBundleMessage
        .textToMedia[F](
          "madrid",
          "interconnessione"
        )(
          vid"rphjb_MessaggioInternet.mp4",
          mp3"rphjb_MessaggioInternet.mp3"
        ),
      ReplyBundleMessage
        .textToMedia[F](
          "ho bisogno di",
          "femmina"
        )(
          gif"rphjb_BisognoDiSorcaGif.mp4",
          vid"rphjb_BisognoDiSorca.mp4",
          mp3"rphjb_BisognoDiSorca.mp3"
        ),
      ReplyBundleMessage
        .textToMedia[F](
          "armadi",
          "vestiti",
          "stivali",
          "d'argento",
          "di abiti"
        )(
          vid"rphjb_Vestiti.mp4",
          mp3"rphjb_Vestiti.mp3"
        ),
      ReplyBundleMessage
        .textToMedia[F](
          "stronzata",
          "orto botanico",
          "totti",
          "salice"
        )(
          vid"rphjb_BarzellettaTotti.mp4",
          mp3"rphjb_BarzellettaTotti.mp3"
        ),
      ReplyBundleMessage.textToMedia[F](
        "estremismo"
      )(
        vid"rphjb_VostraMenteAbbiettaCalpestataNoDirettiveEstremismoMafiaPoliticaPartitiStessaManfrina.mp4",
        gif"rphjb_ScherziAllucinantiZizzaniaConfusioneGif.mp4",
        vid"rphjb_ScherziAllucinantiZizzaniaConfusione.mp4",
        mp3"rphjb_ScherziAllucinantiZizzaniaConfusione.mp3"
      ),
      ReplyBundleMessage
        .textToMedia[F](
          "scherzi",
          "zizzania",
          "rivoluzione",
          "confusione",
          "mettere alla prova",
          "da[re]? fiducia".r.tr(10)
        )(
          gif"rphjb_ScherziAllucinantiZizzaniaConfusioneGif.mp4",
          vid"rphjb_ScherziAllucinantiZizzaniaConfusione.mp4",
          mp3"rphjb_ScherziAllucinantiZizzaniaConfusione.mp3"
        ),
      ReplyBundleMessage.textToMedia[F](
        "barzelletta"
      )(
        vid"rphjb_BarzellettaGesuCristoParadisoPurgatorioMalmsteenDio.mp4",
        vid"rphjb_BarzellettaPapaSonoGayPride.mp4",
        vid"rphjb_BarzellettaPoliticaGinecologo.mp4",
        vid"rphjb_BarzellettaTotti.mp4",
        mp3"rphjb_BarzellettaTotti.mp3"
      ),
      ReplyBundleMessage.textToMedia[F](
        "tacchi a spillo"
      )(
        vid"rphjb_SucchiarviCaramelleFumarviCalpestareTacchiASpilloDominatore.mp4",
        vid"rphjb_VoltoNuovoSempreUomoDonnaSeniCosceTacchiCalzeCameranWoman.mp4",
        vid"rphjb_Vestiti.mp4",
        mp3"rphjb_Vestiti.mp3"
      ),
      ReplyBundleMessage
        .textToMedia[F](
          "(una|un' altra) cassa".r.tr(6),
          "a me (mi )?piace".r.tr(10),
          "me la compro"
        )(
          vid"rphjb_AncoraUnAltraCassa.mp4",
          mp3"rphjb_AncoraUnAltraCassa.mp3"
        ),
      ReplyBundleMessage
        .textToMedia[F](
          "falco",
          "sei morto",
          "pane e vin[o]?".r.tr(10),
          "non ti mancava",
          "insalata",
          "nell'orto",
          "una casa c'avevi tu"
        )(
          vid"rphjb_FalcoMorto.mp4",
          mp3"rphjb_FalcoMorto.mp3"
        ),
      ReplyBundleMessage
        .textToMedia[F](
          "pippo baudo",
          "in anteprima",
          "infarto",
          "ricoverato",
          "possibilissimo"
        )(
          vid"rphjb_MortoPippoBaudo.mp4",
          mp3"rphjb_MortoPippoBaudo.mp3"
        ),
      ReplyBundleMessage.textToMedia[F](
        "piano superiore",
        "si compete"
      )(
        gif"rphjb_GerarcaPianoSuperioreGif.mp4",
        vid"rphjb_GerarcaPianoSuperiore.mp4",
        mp3"rphjb_GerarcaPianoSuperiore.mp3"
      ),
      ReplyBundleMessage
        .textToMedia[F](
          "stantio"
        )(
          gif"rphjb_StantioVecchioGif.mp4",
          vid"rphjb_StantioVecchio.mp4",
          mp3"rphjb_StantioVecchio.mp3"
        ),
      ReplyBundleMessage
        .textToMedia[F](
          "roba vecchia"
        )(
          gif"rphjb_StantioVecchioGif.mp4",
          vid"rphjb_StantioVecchio.mp4",
          mp3"rphjb_StantioVecchio.mp3",
          vid"rphjb_OttavaNotaRobaVecchiaSchifosi.mp4",
          mp3"rphjb_OttavaNotaRobaVecchiaSchifosi.mp3"
        ),
      ReplyBundleMessage
        .textToMedia[F](
          "al massimo"
        )(
          gif"rphjb_VolumeAlMassimoGif.mp4",
          vid"rphjb_VolumeAlMassimo.mp4",
          mp3"rphjb_VolumeAlMassimo.mp3"
        ),
      ReplyBundleMessage
        .textToMedia[F](
          "scoppia"
        )(
          vid"rphjb_EmuleReteInternettariaSitoScoppia.mp4",
          mp3"rphjb_EmuleReteInternettariaSitoScoppia.mp3",
          gif"rphjb_VolumeAlMassimoGif.mp4",
          vid"rphjb_VolumeAlMassimo.mp4",
          mp3"rphjb_VolumeAlMassimo.mp3"
        ),
      ReplyBundleMessage
        .textToMedia[F](
          "coscia",
          "\\banca\\b".r.tr(4),
          "ballerina",
          "sbilenca"
        )(
          gif"rphjb_CosciaAncaBallerinaRussaGif.mp4",
          vid"rphjb_CosciaAncaBallerinaRussa.mp4",
          mp3"rphjb_CosciaAncaBallerinaRussa.mp3"
        ),
      ReplyBundleMessage
        .textToMedia[F](
          "\\b(dieci|10)\\b".r.tr(2),
          "\\b(undici|11)\\b".r.tr(2),
          "\\b(nove|9)\\b".r.tr(2),
          "conto alla rovescia"
        )(
          gif"rphjb_ContoAllaRovesciaGif.mp4",
          vid"rphjb_ContoAllaRovescia.mp4",
          mp3"rphjb_ContoAllaRovescia.mp3"
        ),
      ReplyBundleMessage.textToMedia[F](
        "\\b(undici|11)\\b".r.tr(2)
      )(
        mp3"rphjb_UndiciMilioni.mp3",
        vid"rphjb_UndiciMilioni.mp4",
        gif"rphjb_UndiciMilioniGif.mp4",
        gif"rphjb_ContoAllaRovesciaGif.mp4",
        vid"rphjb_ContoAllaRovescia.mp4",
        mp3"rphjb_ContoAllaRovescia.mp3"
      ),
      ReplyBundleMessage
        .textToMedia[F](
          "modo di essere",
          "guardiamo negli occhi",
          "ci siamo capiti",
          "chiarire"
        )(
          vid"rphjb_ModoDiEssere.mp4",
          mp3"rphjb_ModoDiEssere.mp3"
        ),
      ReplyBundleMessage
        .textToMedia[F](
          "mio genio",
          "network",
          "mi vedono tutti"
        )(
          gif"rphjb_MioGenioGif.mp4",
          vid"rphjb_MioGenio.mp4",
          mp3"rphjb_MioGenio.mp3"
        ),
      ReplyBundleMessage.textToMedia[F](
        "batterista"
      )(
        vid"rphjb_CollaSerpeSigarettePercussionista.mp4",
        vid"rphjb_TommyAldridgeYngwieMalmsteenWhereAreYouGoing.mp4",
        vid"rphjb_GruppiNuoviSpaccanoCuloAbbellimentiRivoltiMordentiContrappunti.mp4",
        vid"rphjb_Batterista.mp4",
        mp3"rphjb_Batterista.mp3"
      ),
      ReplyBundleMessage
        .textToMedia[F](
          "doppio pedale",
          "indipendenza degli arti",
          "sette ottavi",
          "nove undicesimi",
          "poliritm",
          "tempi (composti|dispari)".r.tr(13)
        )(
          vid"rphjb_Batterista.mp4",
          mp3"rphjb_Batterista.mp3"
        ),
      ReplyBundleMessage
        .textToMedia[F](
          "pianista",
          "stesure armoniche",
          "popping",
          "tapping",
          "do maggiore",
          "la minore",
          "misolidio"
        )(
          vid"rphjb_Bassista.mp4",
          mp3"rphjb_Bassista.mp3"
        ),
      ReplyBundleMessage
        .textToMedia[F](
          "\\bstanza\\b".r.tr(6),
          "s'accorse\\b".r.tr(10)
        )(
          gif"rphjb_AuschwitzGif.mp4",
          vid"rphjb_Auschwitz.mp4",
          mp3"rphjb_Auschwitz.mp3"
        ),
      ReplyBundleMessage.textToMedia[F](
        "auschwitz"
      )(
        vid"rphjb_CanzonettePoesieAuschwitzCervello.mp4",
        gif"rphjb_AuschwitzGif.mp4",
        vid"rphjb_Auschwitz.mp4",
        mp3"rphjb_Auschwitz.mp3"
      ),
      ReplyBundleMessage
        .textToMedia[F](
          "incremento del sesso",
          "si insinua",
          "porta del (cesso|bagno)".r.tr(15),
          "catena",
          "palazzo",
          "camera da letto"
        )(
          gif"rphjb_IncrementoDelSessoGif.mp4",
          vid"rphjb_IncrementoDelSesso.mp4",
          mp3"rphjb_IncrementoDelSesso.mp3"
        ),
      ReplyBundleMessage
        .textToMedia[F](
          "protegge"
        )(
          gif"rphjb_MiProteggeGif.mp4",
          mp3"rphjb_MiProtegge.mp3",
          vid"rphjb_MiProtegge.mp4"
        ),
      ReplyBundleMessage
        .textToMedia[F](
          "rivalut",
          "mi fa incazzare",
          "musicisti veri"
        )(
          gif"rphjb_RivalutiamoLArteGif.mp4",
          mp3"rphjb_RivalutiamoLArte.mp3",
          vid"rphjb_RivalutiamoLArte.mp4"
        ),
      ReplyBundleMessage.textToMedia[F](
        "vestito (nuovo|vecchio)".r.tr(13),
        "rammenda",
        "rappezza",
        "rattoppa",
        "ricuci",
        "rinnova"
      )(
        vid"rphjb_CompriVestitoNuovoRammendaVecchio.mp4",
        gif"rphjb_CompriVestitoNuovoRinnovaVecchioGif.mp4",
        mp3"rphjb_CompriVestitoNuovoRinnovaVecchio.mp3",
        vid"rphjb_CompriVestitoNuovoRinnovaVecchio.mp4"
      ),
      ReplyBundleMessage
        .textToMedia[F](
          "cascando",
          "attaccat",
          "il fonico"
        )(
          mp3"rphjb_CascandoTuttoPalle.mp3",
          vid"rphjb_CascandoTuttoPalle.mp4",
          gif"rphjb_CascandoTuttoPalleGif.mp4"
        ),
      ReplyBundleMessage.textToMedia[F](
        "milioni di milioni",
        "(li|mi) co(j|gli)oni".r.tr(9)
      )(
        vid"rphjb_VieSonoTanteMilioniDiMilioniMiCoglioniViaDelleAlbizzie22.mp4",
        mp3"rphjb_DonneTanteMilioniDiMilioni.mp3",
        vid"rphjb_DonneTanteMilioniDiMilioni.mp4",
        gif"rphjb_DonneTanteMilioniDiMilioniGif.mp4",
        mp3"rphjb_StorieSonoTanteVecchiaccia.mp3",
        vid"rphjb_StorieSonoTanteVecchiaccia.mp4"
      ),
      ReplyBundleMessage
        .textToMedia[F](
          "leccare"
        )(
          vid"rphjb_LeccareLeTette.mp4",
          mp3"rphjb_LeccareLeTette.mp3",
          gif"rphjb_LeccareLeTetteGif.mp4"
        ),
      ReplyBundleMessage
        .textToMedia[F](
          "\\btette\\b".r.tr(6),
          "i seni\\b".r.tr(6)
        )(
          vid"rphjb_LeccareLeTette.mp4",
          mp3"rphjb_LeccareLeTette.mp3",
          gif"rphjb_LeccareLeTetteGif.mp4",
          vid"rphjb_DonneDiUnaVoltaSeniCuomoMadonna.mp4",
          mp3"rphjb_DonneDiUnaVoltaSeniCuomoMadonna.mp3",
          vid"rphjb_TiSeiFattaVedere.mp4",
          mp3"rphjb_TiSeiFattaVedere.mp3",
          gif"rphjb_TiSeiFattaVedereGif.mp4"
        ),
      ReplyBundleMessage.textToMedia[F](
        "\\bester( esposito)?\\b".r.tr(5)
      )(
        gif"rphjb_EsterGif.mp4",
        gif"rphjb_Ester2Gif.mp4",
        vid"rphjb_LeccareLeTette.mp4",
        mp3"rphjb_LeccareLeTette.mp3",
        gif"rphjb_LeccareLeTetteGif.mp4"
      ),
      ReplyBundleMessage.textToMedia[F](
        "ragazzetta"
      )(
        mp3"rphjb_RagazzettaCarne.mp3",
        vid"rphjb_RagazzettaDiProvincia.mp4",
        mp3"rphjb_RagazzettaDiProvincia.mp3",
        gif"rphjb_RagazzettaDiProvinciaGif.mp4"
      ),
      ReplyBundleMessage
        .textToMedia[F](
          "\\bprovincia\\b".r.tr(9),
          "non sarebbe meglio",
          "una delle (mie )?fans".r.tr(14),
          "non (mi )?chiede una lira".r.tr(19),
          "una (bella)?scopata".r.tr(11),
          "marinaio",
          "in ogni porto"
        )(
          vid"rphjb_RagazzettaDiProvincia.mp4",
          mp3"rphjb_RagazzettaDiProvincia.mp3",
          gif"rphjb_RagazzettaDiProvinciaGif.mp4"
        ),
      ReplyBundleMessage
        .textToMedia[F](
          "a modo loro",
          "i ragazzi"
        )(
          vid"rphjb_RagazziAModoLoro.mp4",
          mp3"rphjb_RagazziAModoLoro.mp3",
          gif"rphjb_RagazziAModoLoroGif.mp4"
        ),
      ReplyBundleMessage.textToMedia[F](
        "rispondere",
        "non si capisce"
      )(
        gif"rphjb_RispondereGif.mp4",
        mp3"rphjb_Rispondere.mp3",
        vid"rphjb_Rispondere.mp4"
      ),
      ReplyBundleMessage
        .textToMedia[F](
          "quanto m[ie] costi".r.tr(15)
        )(
          gif"rphjb_ChiCacciaISoldiGif.mp4",
          vid"rphjb_ChiCacciaISoldi.mp4",
          mp3"rphjb_ChiCacciaISoldi.mp3"
        ),
      ReplyBundleMessage.textToMedia[F](
        "dis[gc]apito".r.tr(9)
      )(
        gif"rphjb_DiscapitoGif.mp4",
        vid"rphjb_Discapito.mp4",
        mp3"rphjb_Discapito.mp3"
      ),
      ReplyBundleMessage.textToMedia[F](
        "[c']?ha [pure ]?ragione[ questo]?".r.tr(10),
        "o no?"
      )(
        gif"rphjb_RagioneGif.mp4",
        vid"rphjb_Ragione.mp4",
        mp3"rphjb_Ragione.mp3"
      ),
      ReplyBundleMessage
        .textToMedia[F](
          "sei fatta vedere"
        )(
          vid"rphjb_TiSeiFattaVedere.mp4",
          mp3"rphjb_TiSeiFattaVedere.mp3",
          gif"rphjb_TiSeiFattaVedereGif.mp4"
        ),
      ReplyBundleMessage
        .textToMedia[F](
          "l'accordo"
        )(
          vid"rphjb_InCercaDellAccordoPerduto.mp4",
          mp3"rphjb_InCercaDellAccordoPerduto.mp3",
          gif"rphjb_InCercaDellAccordoPerdutoGif.mp4"
        ),
      ReplyBundleMessage
        .textToMedia[F](
          "mio (fido )?amico".r.tr(9),
          "questa battaglia",
          "per il metallo"
        )(
          vid"rphjb_BraccioDestroEFidoAmicoBattagliaMetallo.mp4",
          mp3"rphjb_BraccioDestroEFidoAmicoBattagliaMetallo.mp3",
          gif"rphjb_BraccioDestroEFidoAmicoBattagliaMetalloGif.mp4"
        ),
      ReplyBundleMessage
        .textToMedia[F](
          "braccio destro"
        )(
          vid"rphjb_BraccioDestroAngelo.mp4",
          mp3"rphjb_BraccioDestroAngelo.mp3",
          gif"rphjb_BraccioDestroAngeloGif.mp4",
          vid"rphjb_BraccioDestroEFidoAmicoBattagliaMetallo.mp4",
          mp3"rphjb_BraccioDestroEFidoAmicoBattagliaMetallo.mp3",
          gif"rphjb_BraccioDestroEFidoAmicoBattagliaMetalloGif.mp4"
        ),
      ReplyBundleMessage
        .textToMedia[F](
          "due bestie",
          "più bravo di"
        )(
          vid"rphjb_DueBestie.mp4",
          mp3"rphjb_DueBestie.mp3",
          gif"rphjb_DueBestieGif.mp4"
        ),
      ReplyBundleMessage
        .textToMedia[F](
          "perfetto",
          "la perfezione",
          "guardo allo specchio"
        )(
          vid"rphjb_Perfetto.mp4",
          mp3"rphjb_Perfetto.mp3",
          gif"rphjb_PerfettoGif.mp4"
        ),
      ReplyBundleMessage.textToMedia[F](
        "\\battori\\b".r.tr(6),
        "\\barbeit\\b".r.tr(6),
        "\\bmacht\\b".r.tr(5),
        "\\bfrei\\b".r.tr(4),
        "sto cogline",
        "levat(i|evi|e|a|emelo|emela) d[ai] torn[o]?".r.tr(15),
        "fa[s]+[ ]?binder".r.tr(9)
      )(
        gif"rphjb_TantiAttoriArbeitMachtFreiGif.mp4",
        mp3"rphjb_TantiAttoriArbeitMachtFrei.mp3",
        vid"rphjb_TantiAttoriArbeitMachtFrei.mp4"
      ),
      ReplyBundleMessage.textToMedia[F](
        "pazienza",
        "marines"
      )(
        gif"rphjb_PazienzaDiFerroMarinesGif.mp4",
        mp3"rphjb_PazienzaDiFerroMarines.mp3",
        vid"rphjb_PazienzaDiFerroMarines.mp4",
        vid"rphjb_GaioInGiallo.mp4"
      ),
      ReplyBundleMessage.textToMedia[F](
        "mi sento un (pezzo di merda|l'ultimo)".r.tr(20)
      )(
        vid"rphjb_MiSentoPezzoDiMerdaUltimoGiuGiu.mp4",
        gif"rphjb_MiSentoPezzoDiMerdaUltimoGiuGiuGif.mp4",
        mp3"rphjb_MiSentoPezzoDiMerdaUltimoGiuGiu.mp3"
      ),
      ReplyBundleMessage.textToMedia[F](
        "in fila",
        "e lui..."
      )(
        gif"rphjb_MettitiInFilaGif.mp4",
        mp3"rphjb_MettitiInFila.mp3",
        vid"rphjb_MettitiInFila.mp4"
      ),
      ReplyBundleMessage.textToMedia[F](
        "non (e' |è )male".r.tr(6)
      )(
        gif"rphjb_NonEMaleGif.mp4",
        mp3"rphjb_NonEMale.mp3",
        vid"rphjb_NonEMale.mp4"
      ),
      ReplyBundleMessage.textToMedia[F](
        "loculi",
        "la pace",
        "tarpare le ali",
        "perchè da vivi"
      )(
        gif"rphjb_TrovatoPaceGenitoriGif.mp4",
        mp3"rphjb_TrovatoPaceGenitori.mp3",
        vid"rphjb_TrovatoPaceGenitori.mp4"
      ),
      ReplyBundleMessage.textToMedia[F](
        "genitori"
      )(
        gif"rphjb_TrovatoPaceGenitoriGif.mp4",
        mp3"rphjb_TrovatoPaceGenitori.mp3",
        vid"rphjb_TrovatoPaceGenitori.mp4",
        vid"rphjb_CristoPinocchio.mp4"
      ),
      ReplyBundleMessage
        .textToMedia[F](
          "ormai..."
        )(
          gif"rphjb_OrmaiRisataGif.mp4",
          mp3"rphjb_OrmaiRisata.mp3",
          vid"rphjb_OrmaiRisata.mp4"
        ),
      ReplyBundleMessage.textToMedia[F](
        "perch[eè][?]* si sente".r.tr(15),
        "si sente[?]+".r.tr(9)
      )(
        gif"rphjb_PercheSiSenteGif.mp4",
        mp3"rphjb_PercheSiSente.mp3",
        vid"rphjb_PercheSiSente.mp4"
      ),
      ReplyBundleMessage.textToMedia[F](
        "ballerino",
        "non posso salutare",
        "fornaio",
        "barman",
        "altro piano"
      )(
        vid"rphjb_DaHollywood.mp4",
        mp3"rphjb_DaHollywood.mp3"
      ),
      ReplyBundleMessage.textToMedia[F]("cantante")(
        vid"rphjb_CantantePreferitoNonSonoGaio.mp4",
        vid"rphjb_CantanteDonnaVoceDaUomoDueBocceCosiInnamoratoPerdutamente.mp4",
        vid"rphjb_DaHollywood.mp4",
        mp3"rphjb_DaHollywood.mp3",
        vid"rphjb_GruppiNuoviSpaccanoCuloAbbellimentiRivoltiMordentiContrappunti.mp4"
      ),
      ReplyBundleMessage.textToMedia[F](
        "co(gl|j)ion([e]{3,}|e!)".r.tr(9),
        "co(gl|j)ione(,|, | )co(gl|j)ione".r.tr(17)
      )(
        gif"rphjb_CoglioneGif.mp4",
        gif"rphjb_PiuCoglioneGif.mp4",
        gif"rphjb_CoglioneGif.mp4",
        vid"rphjb_FrocioCoglione.mp4",
        mp3"rphjb_FrocioCoglione.mp3"
      )
    )
}
