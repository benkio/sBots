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
        mf"rphjb_ViviMorti.mp4"
      )
      .copy(matcher = ContainsAll),
    ReplyBundleMessage.textToMedia[F](
      "è un ordine"
    )(
      mf"rphjb_Ordine.mp3",
      mf"rphjb_Ordine.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      "piloti d'aereo",
      "disastri aerei"
    )(
      gif"rphjb_DrogatiPilotiGif.mp4",
      mf"rphjb_EricClaptonDrogaUominiAffari.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      "\\brock\\b".r.tr(4)
    )(
      mf"rphjb_PoesiaRock.mp4",
      mf"rphjb_Rock.mp3",
      mf"rphjb_EricClaptonDrogaUominiAffari.mp4",
      mf"rphjb_Rock.mp4",
      mf"rphjb_BluesEbbeBambinoRockNRollBeBopALulaStormyMondayButTuesdayIsJustAsBad.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      "ti distruggo"
    )(
      gif"rphjb_TiDistruggo.mp4",
      mf"rphjb_Ramarro.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      "cristo pinocchio",
      "lumicino",
      "(strade|vie) inferiori".r.tr(13)
    )(
      mf"rphjb_CristoPinocchio.mp3",
      mf"rphjb_CristoPinocchio.mp4",
      mf"rphjb_PoesiaMaria.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      "pappalardo"
    )(
      mf"rphjb_Pappalardo.mp3",
      mf"rphjb_Pappalardo.mp4",
    ),
    ReplyBundleMessage.textToMedia[F](
      "lasciami in pace",
      "\\bstronza\\b".r.tr(7)
    )(
      gif"rphjb_LasciamiInPace.mp4",
      mf"rphjb_LasciamiInPaceStronza.mp4"
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
      mf"rphjb_Pasqua.mp4",
    ),
    ReplyBundleMessage.textToMedia[F](
      "stare male",
      "melensa"
    )(
      gif"rphjb_MiFaStareMale.mp4",
      mf"rphjb_MelensaStareMale.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      "[a]+[t]{2,}[e]+[n]+[z]+[i]+[o]+[n]+[e]+[!]*[!e]$".r.tr(10)
    )(
      mf"rphjb_Attenzione.mp3",
      mf"rphjb_Attenzione.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      "paradosso"
    )(
      gif"rphjb_ParadossoGif.mp4",
      mf"rphjb_Paradosso.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      "\\bsput[ao]\\b".r.tr(5)
    )(
      gif"rphjb_SputoGif.mp4",
      mf"rphjb_Sputo.mp4",
      mf"rphjb_BicchiereSputoLimitazioniUomoDonna.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      "cobelini",
      "cobbolidi",
      "elfi",
      "\\bnani\\b".r.tr(4),
      "gobellini",
      "ossa dei morti"
    )(
      mf"rphjb_FigureMitologiche.mp3",
      mf"rphjb_FigureMitologiche.mp4",
      mf"rphjb_FigureMitologiche2.mp4",
      mf"rphjb_FigureMitologiche3.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      "anche la merda",
      "senza culo"
    )(
      mf"rphjb_Merda.mp3",
      mf"rphjb_AncheLaMerda.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      "chiama la polizia"
    )(
      gif"rphjb_ChiamaLaPoliziaGif.mp4",
      mf"rphjb_ChiamaLaPolizia.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      "stori(a|e)".r.tr(6)
    )(
      mf"rphjb_Storie.mp3",
      mf"rphjb_Storie2.mp3",
      mf"rphjb_StoriaNonDetta.mp4",
      mf"rphjb_StorieSonoTanteVecchiaccia.mp4",
      mf"rphjb_StoriaVeraPienaBugie.mp4",
      mf"rphjb_StoriaAmicoGrasso.mp4",
      mf"rphjb_StoriaSignorGionz.mp4",
      mf"rphjb_StoriaMula.mp3",
      mf"rphjb_CaniAlCimitero.mp4",
      mf"rphjb_IlCiano.mp4",
      mf"rphjb_CheStoDicendoDiVoltaIlCervelloPrincipiVeneziani.mp4",
      mf"rphjb_StoriaDellaMiaVita.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      "au[ ]?de".r.tr(4),
      "\\btime\\b".r.tr(4),
      "uir[ ]?bi[ ]?taim".r.tr(9)
    )(
      mf"rphjb_Audeuirbitaim.mp3",
      mf"rphjb_Audeuirbitaim2.mp3",
      mf"rphjb_Audeuirbitaim.mp4",
    ),
    ReplyBundleMessage.textToMedia[F](
      "micetta",
      "la morte"
    )(
      gif"rphjb_Micetta.mp4",
      mf"rphjb_LaMorteMicetta.mp4",
      mf"rphjb_LaMorte.mp4",
      mf"rphjb_LaMorte2.mp4",
      mf"rphjb_InnoAllaMorte.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      "\\bspalle\\b".r.tr(6),
      "\\bbraccia\\b".r.tr(7),
      "t(i|e) strozzo".r.tr(10)
    )(
      gif"rphjb_FaccioVedereSpalleBracciaGif.mp4",
      mf"rphjb_FaccioVedereSpalleBraccia.mp4",
      mf"rphjb_UccidereUnaPersona.mp4",
    ),
    ReplyBundleMessage.textToMedia[F](
      "non sapere",
      "aris(d|t)o(d|t)ele".r.tr(10),
      "socrate"
    )(
      gif"rphjb_SoDiNonSapereGif.mp4",
      mf"rphjb_SoDiNonSapere.mp4",
      mf"rphjb_SoDiNonSapere2.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      "non è roba per me"
    )(
      gif"rphjb_RobaPerMeGif.mp4",
      mf"rphjb_RobaPerMe.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      "io \\bn[o]{2,}\\b".r.tr(6)
    )(
      mf"rphjb_IoNo.mp3",
      mf"rphjb_GesuCoglione.mp4",
      mf"rphjb_GesuCoglione.mp3",
    ),
    ReplyBundleMessage.textToMedia[F](
      "bastone infernale",
      "un'arma"
    )(
      gif"rphjb_Bastone1.mp4",
      gif"rphjb_Bastone2.mp4",
      gif"rphjb_Bastone3.mp4",
      mf"rphjb_BastoneInfernale.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      "vi calpesto",
      "vermi",
      "strisciate per terra"
    )(
      gif"rphjb_ViCalpestoGif.mp4",
      mf"rphjb_ViCalpesto.mp3",
      mf"rphjb_ViCalpesto.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      "stringere i denti",
      "andare avanti",
    )(
      gif"rphjb_AndareAvanti.mp4",
      mf"rphjb_AndareAvanti.mp3",
      mf"rphjb_ComposizioneIdeaFrescaInnovazioneAndareAvantiStiamoTornandoIndetro.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      "non ci credete?",
      "grande s(d|t)ronza(d|t)(e|a)".r.tr(16)
    )(
      gif"rphjb_NonCiCredete.mp4",
      mf"rphjb_NonCiCredete.mp3",
    ),
    ReplyBundleMessage.textToMedia[F](
      "non me ne (frega|fotte)".r.tr(15),
      "chissenefrega",
      "non mi interessa"
    )(
      gif"rphjb_NonMeNeFotte.mp4",
      gif"rphjb_NonMeNeFrega.mp4",
      mf"rphjb_NonMiFregaParloIo.mp4",
      mf"rphjb_ENonMeNeFotteUnCazzo.mp3",
      mf"rphjb_NonLeggoQuelloCheScrivete.mp3",
      mf"rphjb_IncidentePonte.mp3",
      mf"rphjb_EscertoCritiche.mp4",
      mf"rphjb_Escerto.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      "ultimi"
    )(
      gif"rphjb_ViCalpestoGif.mp4",
      mf"rphjb_ViCalpesto.mp3",
      mf"rphjb_ViCalpesto.mp4",
      gif"rphjb_Ultimi.mp4",
      mf"rphjb_StateZittiZozziUltimi.mp3",
      mf"rphjb_RottoIlCazzoUltimi.mp4",
      mf"rphjb_BruttiSchifosiUltimiDegliUltimiNonSonoUltimo.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      "che (cazzo )?era quella roba".r.tr(19),
      "che (cazzo |cazzo di roba )?mi avete dato".r.tr(17),
      "lampi negli occhi",
      "gira(re|ra|rà|ndo)? la testa".r.tr(13),
      "insieme alla (c|g)o(c|g)a (c|g)ola".r.tr(22)
    )(
      mf"rphjb_CheCazzoEraQuellaRoba.mp3",
      mf"rphjb_CheCazzoEraQuellaRoba.mp4",
      mf"rphjb_CheCazzoEraQuellaRoba2.mp4",
      mf"rphjb_CheCazzoEraQuellaRoba3.mp4",
      mf"rphjb_RockMachineIntro.mp4",
      mf"rphjb_MaCheCazzoEraQuellaRobaDroghe.mp3"
    ),
    ReplyBundleMessage.textToMedia[F](
      "canzonette",
      "balera",
      "sagra dell'uva",
      "feste condominiali",
      "feste di piazza"
    )(
      mf"rphjb_Canzonette.mp3",
      mf"rphjb_Canzonette.mp4",
      mf"rphjb_Canzonette.gif"
    ),
    ReplyBundleMessage.textToMedia[F](
      "un pollo"
    )(
      mf"rphjb_Pollo.mp3",
      mf"rphjb_Pollo.mp4",
      mf"rphjb_Pollo2.mp4",
      gif"rphjb_PolloGif.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      "quello che dico io"
    )(
      gif"rphjb_QuelloCheDicoIo.mp4",
      mf"rphjb_FannoQuelloCheDicoIo.mp3",
      mf"rphjb_FannoQuelloCheDicoIo.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      "zucchero"
    )(
      mf"rphjb_ChitarraZuggherada.mp3",
      mf"rphjb_ChitarraZuccherada.mp4",
      mf"rphjb_ChitarraZuccheroAlgheVino.mp3",
      mf"rphjb_Zucchero.mp3",
      mf"rphjb_ZuccheroVanigliaCremaCioccolataPandoro.mp3",
      mf"rphjb_AuguriPerPasqua.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      "\\bgood\\b".r.tr(4),
      "\\bshow\\b".r.tr(4),
      "\\bfriends\\b".r.tr(7)
    )(
      gif"rphjb_OkGoodShowFriends.mp4",
      gif"rphjb_OkGoodShowFriends2.mp4",
      mf"rphjb_LetSGoodStateBene.mp3",
      mf"rphjb_WelaMyFriends.mp4",
      mf"rphjb_LetsGoodMyFriends.mp4",
      mf"rphjb_NonPoteteGiudicarUrloThatsGood.mp4",
      mf"rphjb_LetsGoodMyFriendsPassport.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      "vattene (a f|a[f]*)?fanculo".r.tr(16)
    )(
      gif"rphjb_MaVatteneAffanculo.mp4",
      mf"rphjb_MaVatteneAffanculo.mp3",
      mf"rphjb_PortlandVancuverFanculo.mp4",
      gif"rphjb_FanculoPerCortesia.mp4",
      mf"rphjb_DecidoIoMareCazzatePerCortesia.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      "feelings"
    )(
      mf"rphjb_Feelings.gif",
      gif"rphjb_Feelings2.mp4",
      mf"rphjb_Feelings.mp3",
      mf"rphjb_FeelingsIncazzarmiAndiamociSentireOriginale.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      "me ne vado"
    )(
      mf"rphjb_MeNeVado.mp3",
      mf"rphjb_EBastaETuHaiRottoIlCazzo.mp4",
      gif"rphjb_MiRompiErCazzo.mp4",
      gif"rphjb_MeNeVado.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      "mignotta",
      "puttana",
      "troia"
    )(
      mf"rphjb_Mignotta.mp3",
      gif"rphjb_Mignotta.mp4",
      mf"rphjb_VialeZara.mp3",
      mf"rphjb_StronzoFiglioMignotta.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      "ti devi spaventare"
    )(
      mf"rphjb_TiDeviSpaventare.mp3",
      gif"rphjb_TiDeviSpaventareGif.mp4",
      mf"rphjb_TiDeviSpaventare.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      "il martell"
    )(
      mf"rphjb_MaCheCazzoStoDicendo.mp3",
      mf"rphjb_MaCheCazzoStoDicendo.mp4",
      gif"rphjb_MaCheCazzoStoDicendoGif.mp4",
      gif"rphjb_IlMartel.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      "questa volta no"
    )(
      mf"rphjb_QuestaVoltaNo.mp3",
      gif"rphjb_QuestaVoltaNo.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      "una vergogna"
    )(
      mf"rphjb_Vergogna.mp3",
      mf"rphjb_Vergogna.mp4",
      gif"rphjb_VergognaGif.mp4",
      gif"rphjb_Vergogna2.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      "mi devo trasformare",
      "cristo canaro"
    )(
      mf"rphjb_Trasformista.mp3",
      gif"rphjb_Trasformista.mp4",
      mf"rphjb_CristoCanaro.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      "ma[ ]?s(c|g)us[a]?".r.tr(5)
    )(
      mf"rphjb_MaSgus.mp3",
      gif"rphjb_MaSgusGif.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      "grazie gianni",
    )(
      mf"rphjb_Grazie.mp3",
      gif"rphjb_GrazieGif.mp4",
      mf"rphjb_Grazie.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      "cia[o]{3,}".r.tr(6)
    )(
      mf"rphjb_Grazie.mp3",
      gif"rphjb_GrazieGif.mp4",
      mf"rphjb_Grazie.mp4",
      mf"rphjb_ViSaluto.mp3"
    ),
    ReplyBundleMessage.textToMedia[F](
      "stare attenti",
      "per strada"
    )(
      mf"rphjb_IncontratePerStrada.mp3",
      gif"rphjb_IncontratePerStrada.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      "lavora tu vecchiaccia",
      "hai la pelle dura",
      "io sono creatura"
    )(
      mf"rphjb_LavoraTu.mp3",
      mf"rphjb_LavoraTu.mp4",
      mf"rphjb_LavoraTu2.mp4",
      mf"rphjb_LavoraTu3.mp4",
      mf"rphjb_LavoraTu4.mp4",
      gif"rphjb_LavoraTuGif.mp4",
      mf"rphjb_StorieSonoTanteVecchiaccia.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      "infern[a]+l[ie]+[!]*".r.tr(9)
    )(
      mf"rphjb_Infernali.mp3",
      gif"rphjb_Infernali.mp4",
      mf"rphjb_Infernale.mp3",
      mf"rphjb_CarrellataInfernaleDelirioPureNellaGolaTrasmissioneDaUrloEchoDelayVitaNaturalDurante.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      "per il culo"
    )(
      mf"rphjb_PigliandoPerIlCulo.mp3",
      gif"rphjb_PigliandoPerIlCulo.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      "sorriso",
      "(😂|🤣){4,}".r.tr(4),
      "(😄|😀|😃){4,}".r.tr(4),
      "(ah|ha){7,}".r.tr(14)
    )(
      mf"rphjb_Risata.mp3",
      mf"rphjb_Risata.mp4",
      mf"rphjb_Risata2.mp4",
      mf"rphjb_Risata3.mp4",
      mf"rphjb_RisataInfernale.mp4",
      mf"rphjb_Risata.gif",
      gif"rphjb_RisataGif.mp4",
      mf"rphjb_OrmaiRisata.mp4",
      mf"rphjb_Sorriso2.gif",
      gif"rphjb_Sorriso.mp4",
      gif"rphjb_SorrisoSognante.mp4",
      mf"rphjb_Risata2.mp3",
      mf"rphjb_SepolturaRisata.mp4",
      gif"rphjb_RisataTrattenuta.mp4",
      mf"rphjb_CheGruppoMiRicordaRisata.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      "ammazza che sei",
      "quasi un frocio"
    )(
      mf"rphjb_Frocio.mp3",
      gif"rphjb_FrocioGif.mp4",
      mf"rphjb_EduFalasciQuasiFrocio.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      "(fammi|chiedere)? (una|questa)? cortesia".r.tr(18)
    )(
      mf"rphjb_FammiQuestaCortesia.mp3",
      gif"rphjb_FammiQuestaCortesia.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      "non mi sta bene"
    )(
      mf"rphjb_NonMiStaBene.mp3",
      mf"rphjb_NonMiStaBene2.mp3",
      gif"rphjb_NonMiStaBeneGif.mp4",
      gif"rphjb_NonMiStaBene2.mp4",
      mf"rphjb_NonMiStaBene.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      "le labbra",
    )(
      mf"rphjb_Labbra.mp3",
      gif"rphjb_Labbra.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      "la vita è il nemico"
    )(
      mf"rphjb_VitaNemico.mp3",
      gif"rphjb_VitaNemicoGif.mp4",
      mf"rphjb_VitaNemico.mp4",
      mf"rphjb_VitaNemico2.mp4",
      mf"rphjb_VitaNemico3.mp4",
      mf"rphjb_VitaNemicoCervello.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      "permettere"
    )(
      mf"rphjb_Permettere.mp3",
      gif"rphjb_Permettere.mp4",
      mf"rphjb_Labrie.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      "le note"
    )(
      mf"rphjb_Note.mp3",
      gif"rphjb_Note.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      "te[r]+[i]+[b]+[i]+l[e]+".r.tr(8)
    )(
      mf"rphjb_Terribile.mp3",
      mf"rphjb_Terribile.mp4",
      gif"rphjb_TerribileGif.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      "napoli"
    )(
      mf"rphjb_VivaNapoli.mp3",
      gif"rphjb_VivaNapoli.mp4",
      mf"rphjb_CuginiCampagnaGiardinoSempliciHomoSapiensLiscioRumbaChaChaChaCanzoneNapoletanaOsanna.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      "ciao a tutti",
      "belle gioie"
    )(
      gif"rphjb_CiaoComeStateGif.mp4",
      mf"rphjb_CiaoComeState.mp4",
      mf"rphjb_CiaoComeState.mp3"
    ),
    ReplyBundleMessage.textToMedia[F](
      "\\bbasta(a|!){2,}".r.tr(7)
    )(
      mf"rphjb_Basta.mp3",
      gif"rphjb_Basta.mp4",
      gif"rphjb_Basta2.mp4",
      mf"rphjb_Basta2.mp3",
      mf"rphjb_EBastaETuHaiRottoIlCazzo.mp4",
      gif"rphjb_BastaGif.mp4",
      gif"rphjb_Basta2Gif.mp4",
      gif"rphjb_Basta3.mp4",
      gif"rphjb_Basta4.mp4",
      mf"rphjb_BastaRottoIlCazzo.mp4",
      gif"rphjb_BastaSedia.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      "assolo",
      "(g|c)hi(t|d)arra".r.tr(8),
      "(as)?solo di basso".r.tr(13)
    )(
      mf"rphjb_Assolo.mp3",
      mf"rphjb_Assolo.mp4",
      mf"rphjb_Assolo2.mp4",
      mf"rphjb_AssoloBeat.mp4",
      mf"rphjb_AssoloSubdolo.mp4",
      mf"rphjb_Basso.mp4",
      gif"rphjb_Chitarra1.mp4",
      gif"rphjb_Chitarra2.mp4",
      gif"rphjb_Chitarra3.mp4",
      mf"rphjb_ChitarraPlettroVicoletto.mp4",
      mf"rphjb_ChitarraVicolettoPlettro2.mp4",
      mf"rphjb_ChitarraZuggherada.mp3",
      mf"rphjb_ChitarraZuccherada.mp4",
      mf"rphjb_ChitarraZuccheroAlgheVino.mp3",
      mf"rphjb_AssoloBasso.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      "\\b(g|c)a(b|p)i(d|t)o\\b".r.tr(6),
      "\\bcapissi\\b".r.tr(7),
    )(
      mf"rphjb_HoCapito.mp3",
      mf"rphjb_AveteCapito.mp3",
      mf"rphjb_AveteCapito.mp4",
      mf"rphjb_Capito.mp3",
      mf"rphjb_NonHannoCapitoUnCazzo.mp3",
      mf"rphjb_NonAveteCapitoUnCazzo.mp4",
      gif"rphjb_AveteCapitoComeSempre.mp4",
      gif"rphjb_NonAveteCapitoUnCazzoGif.mp4",
      gif"rphjb_VoiNonAveteCapitoUnCazzo.mp4",
      gif"rphjb_IlSensoCapito.mp4",
      gif"rphjb_CapitoDoveStiamo.mp4",
      gif"rphjb_NonHoCapito.mp4",
      gif"rphjb_AveteCapitoEh.mp4",
      gif"rphjb_ComeAlSolitoNonAveteCapito.mp4",
      mf"rphjb_CapitoDoveStiamo.mp3",
      mf"rphjb_CapisciRidotti.mp3",
      mf"rphjb_CapitoCheMagagna.mp3",
      mf"rphjb_DavantiGenteNonHaCapisceUnCazzo.mp3",
      mf"rphjb_AbbiamoCapito.mp4",
      mf"rphjb_AveteCapitoNo.mp4",
      mf"rphjb_StiamoNellaFollia.mp4",
      mf"rphjb_HaiCapitoAveteRapitoONonAveteCapitoUnCazzo.mp4",
      mf"rphjb_AveteCapito2.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      "esperiment",
      "1(,)? 2(,)? 3".r.tr(5),
      "uno(,)? due(,)? tre".r.tr(11)
    )(
      mf"rphjb_Esperimento.mp3",
      mf"rphjb_Esperimento.mp4",
      mf"rphjb_Esperimento2.mp4",
      gif"rphjb_EsperimentoGif.mp4",
      gif"rphjb_Esperimento2Gif.mp4",
      gif"rphjb_Esperimento3.mp4",
      mf"rphjb_DiciottoAnni.mp4",
      mf"rphjb_DiciottoAnni2.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      "schifosi"
    )(
      gif"rphjb_ViCalpestoGif.mp4",
      mf"rphjb_ViCalpesto.mp3",
      mf"rphjb_ViCalpesto.mp4",
      mf"rphjb_Schifosi.mp3",
      mf"rphjb_Schifosi.mp4",
      mf"rphjb_Schifosi2.mp3",
      mf"rphjb_Schifosi3.mp3",
      mf"rphjb_Schifosi4.mp3",
      mf"rphjb_Schifosi4.mp4",
      gif"rphjb_Schifosi3.mp4",
      mf"rphjb_SchifosoUltimi.mp4",
      mf"rphjb_StateZittiZozziUltimi.mp3",
      gif"rphjb_SchifosiGif.mp4",
      gif"rphjb_Schifosi2.mp4",
      mf"rphjb_Vigile.mp4",
      mf"rphjb_ConQuestaTecnica.mp4",
      mf"rphjb_ConQuestaTecnica.mp3",
      mf"rphjb_BruttiSchifosiUltimiDegliUltimiNonSonoUltimo.mp4",
      mf"rphjb_ChitarreVergognateviSchifosiGiornaliMerda.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      "schifos(o)+(!)*".r.tr(8)
    )(
      gif"rphjb_Schifoso.mp4",
      mf"rphjb_Vigile.mp4",
      mf"rphjb_AmoreSuonareFreddoBuddistiSchifoso.mp4",
      gif"rphjb_BruttoSquallidoSchifosoGif.mp4",
      mf"rphjb_BruttoSquallidoSchifosoUltimoEsseriUmani.mp4",
      mf"rphjb_DanzaMacabra.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      "mortacci vostri"
    )(
      gif"rphjb_MortacciVostriGif.mp4",
      mf"rphjb_StateZittiZozziUltimi.mp3",
      mf"rphjb_ConQuestaTecnica.mp4",
      mf"rphjb_ConQuestaTecnica.mp3",
      mf"rphjb_MortacciVostri.mp4",
      mf"rphjb_CheCazzoEraQuellaRoba2.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      "non vedo"
    )(
      mf"rphjb_Stanco.mp3",
      mf"rphjb_Stanco.mp4",
      mf"rphjb_PannaOcchialiSpalla.mp3",
    ),
    ReplyBundleMessage.textToMedia[F](
      "panna"
    )(
      mf"rphjb_Problema.mp3",
      mf"rphjb_PannaOcchialiSpalla.mp3"
    ),
    ReplyBundleMessage.textToMedia[F](
      "\\bapplauso\\b".r.tr(8)
    )(
      gif"rphjb_Applauso.mp4",
      mf"rphjb_Applauso.mp3",
      mf"rphjb_Applauso2.mp3",
      mf"rphjb_ApplausoPiuForte.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      "venite qua"
    )(
      gif"rphjb_VeniteQuaGif.mp4",
      mf"rphjb_VeniteQua.mp3",
      mf"rphjb_VeniteQua.mp4"
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
      mf"rphjb_ChiCacciaISoldi.mp3",
      mf"rphjb_SoldiButtatiDiscotecaLaziale.mp3",
      mf"rphjb_BigMoney.mp4",
      mf"rphjb_InvestitoreGoverno.mp4",
      mf"rphjb_ButtareSoldiFinestra.mp4",
      mf"rphjb_CoiSoldiMiei.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      "[od]?dio mio[,]? no".r.tr(10)
    )(
      gif"rphjb_OddioMioNoGif.mp4",
      mf"rphjb_OddioMioNo.mp3",
      mf"rphjb_OddioMioNo2.mp3",
      gif"rphjb_OddioMioNo.mp4",
      gif"rphjb_OddioMioNo2.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      "[sono ]?a[r]{1,2}iva(d|t)o".r.tr(12),
      "(eccomi|ciao).*\\bpiacere\\b".r.tr(13)
    )(
      gif"rphjb_ArivatoGif.mp4",
      mf"rphjb_Arivato.mp3",
      gif"rphjb_Arivato.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      "delu(s|d)".r.tr(5)
    )(
      gif"rphjb_Deluso.mp4",
      mf"rphjb_Deluso.mp3",
      mf"rphjb_DeludendoQuasiTutto.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      "fate come vi pare",
      "sti (g|c)azzi".r.tr(9)
    )(
      gif"rphjb_ComeViPare.mp4",
      mf"rphjb_ComeViPare.mp3"
    ),
    ReplyBundleMessage.textToMedia[F](
      "divento una bestia",
      "incazzo"
    )(
      mf"rphjb_DiventoBestia.mp3",
      mf"rphjb_Incazzo.mp3",
      mf"rphjb_Incazzo2.mp3",
      mf"rphjb_PrimoSbaglio.mp3",
      mf"rphjb_PrimoSbaglio.mp4",
    ),
    ReplyBundleMessage.textToMedia[F](
      "dove stiamo",
      "stiamo nella follia"
    )(
      mf"rphjb_CapitoDoveStiamo.mp3",
      mf"rphjb_StiamoNellaFollia.mp4",
      gif"rphjb_CapitoDoveStiamo.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      "non sai molto"
    )(
      gif"rphjb_NonSaiMolto.mp4",
      mf"rphjb_RadioRockErrori.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      "errori"
    )(
      gif"rphjb_MaiErroriGif.mp4",
      mf"rphjb_MaiErrori.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      "\\bpasqua\\b".r.tr(6)
    )(
      mf"rphjb_AuguriPasqua.mp3",
      mf"rphjb_AuguriPerPasqua.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      "vaniglia",
      "pandoro",
      "crema alla (g|c)io(g|c)+ola(d|t)a".r.tr(20),
    )(
      mf"rphjb_ZuccheroVanigliaCremaCioccolataPandoro.mp3",
      mf"rphjb_AuguriPerPasqua.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      "c'hai timore",
      "c'hai paura",
      "diri[g]+en(d|t)i".r.tr(9),
    )(
      gif"rphjb_Dirigenti.mp4",
      mf"rphjb_AncoraNoDirigenti.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      "guerra"
    )(
      gif"rphjb_GuerraTotaleGif.mp4",
      mf"rphjb_GuerraTotale.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      "non voglio nessuno",
      "mentre lavoro"
    )(
      gif"rphjb_NonVoglioNessunoGif.mp4",
      mf"rphjb_NonVoglioNessuno.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      "peggio del peggio"
    )(
      gif"rphjb_PeggioDelPeggioGif.mp4",
      mf"rphjb_PeggioDelPeggio.mp4"
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
      mf"rphjb_DiciottoAnni.mp4",
      mf"rphjb_DiciottoAnni2.mp4",
      gif"rphjb_DiciottoAnniGif.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      "(cinque|5) dita".r.tr(6),
      "pugno"
    )(
      mf"rphjb_CinqueDita.mp4",
      mf"rphjb_CinqueDita.mp3",
      mf"rphjb_CinqueDita2.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      "\\bratti\\b".r.tr(5),
      "topi"
    )(
      mf"rphjb_DubbioScantinatiGiocoRattoGatto.mp4",
      mf"rphjb_ListaMaleCollo.mp3"
    ),
    ReplyBundleMessage.textToMedia[F](
      "poveri cretini",
      "poveri ignoranti"
    )(
      mf"rphjb_PoveriCretini.mp3",
      mf"rphjb_PoveriIgnorantiLatteDrogaSoloGreggeSputo.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      "solo uno parló",
      "(c|g)ri(d|t)i(g|c)a(d|t)o".r.tr(9)
    )(
      gif"rphjb_FuCriticatoGif.mp4",
      mf"rphjb_FuCriticato.mp4",
    ),
    ReplyBundleMessage.textToMedia[F](
      "venerd[iì]".r.tr(7)
    )(
      mf"rphjb_Venerdi.mp3",
      mf"rphjb_Venerdi.mp4",
      mf"rphjb_IlVenerdi.mp4",
      mf"rphjb_TempoAlTempo.mp4",
      mf"rphjb_VenerdiAppuntamentoFissoFica.mp4",
    ),
    ReplyBundleMessage.textToMedia[F](
      "discoteca laziale"
    )(
      gif"rphjb_DiscotecaLazialeGif.mp4",
      mf"rphjb_DiscotecaLaziale.mp4",
      mf"rphjb_DiscotecaLaziale.mp3",
      mf"rphjb_SoldiButtatiDiscotecaLaziale.mp3",
    ),
    ReplyBundleMessage.textToMedia[F](
      "calcoli",
      "matematica",
      "geometrici",
      "matematici",
      "analitici",
    )(
      gif"rphjb_MiPareLogico.mp4",
      mf"rphjb_MiPareLogico.mp3",
      mf"rphjb_MatematiciAnaliticiDiNoia.mp3",
      mf"rphjb_MiPareLogicoMatematiciAnaliticiDiNoia.mp4",
      mf"rphjb_CoseCheNonSopportoCalcoliSbagliati.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      "\\blo[g]+i(c|g)o\\b".r.tr(6)
    )(
      gif"rphjb_MiPareLogico.mp4",
      mf"rphjb_MiPareLogico.mp3",
      gif"rphjb_SembraLogico.mp4",
      mf"rphjb_MiPareLogicoMatematiciAnaliticiDiNoia.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      "ti dovresti vergognare"
    )(
      gif"rphjb_TiDovrestiVergognare.mp4",
      mf"rphjb_TiDovrestiVergognare.mp3"
    ),
    ReplyBundleMessage.textToMedia[F](
      "(non|mica) so(no)? (un |n )?co(gl|j)ione".r.tr(13),
      "sarete co(gl|j)ioni voi".r.tr(17)
    )(
      gif"rphjb_SareteCoglioniVoiGif.mp4",
      mf"rphjb_SareteCoglioniVoi.mp3",
      mf"rphjb_SareteCoglioniVoi.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      "non li sopporto",
      "che si deve f(à|are)".r.tr(14),
      "bisogna pure lavorà"
    )(
      gif"rphjb_NonLiSopportoGif.mp4",
      mf"rphjb_NonLiSopporto.mp3",
      mf"rphjb_NonLiSopporto.mp4",
    ),
    ReplyBundleMessage.textToMedia[F](
      "simposio"
    )(
      mf"rphjb_PellegrinaggioSimposioMetallo.mp4",
      mf"rphjb_InnoSimposio.mp3",
    ),
    ReplyBundleMessage.textToMedia[F](
      "chi è cristo",
      "si è fatto fregare",
      "bacio di un frocio",
    )(
      mf"rphjb_ChiECristo.mp3",
      mf"rphjb_GiudaFrocio.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      "danza macabra",
    )(
      gif"rphjb_DanzaMacabraGif.mp4",
      mf"rphjb_DanzaMacabra.mp4",
      mf"rphjb_DanzaMacabra.mp3",
    ),
    ReplyBundleMessage.textToMedia[F](
      "steve vai",
    )(
      mf"rphjb_SteveVaiRiciclando.mp4",
      mf"rphjb_AmoreSuonareFreddoBuddistiSchifoso.mp4",
      mf"rphjb_DueOssa.mp3",
      gif"rphjb_Note.mp4",
      mf"rphjb_Paradosso.mp4",
      mf"rphjb_RelIllusions.mp4",
      gif"rphjb_TiDeviSpaventareGif.mp4",
      mf"rphjb_TiDeviSpaventare.mp3",
      mf"rphjb_FeelingsIncazzarmiAndiamociSentireOriginale.mp4",
      mf"rphjb_SteveVaiRamazzotti.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      "(divento|diventare|sono) (matto|pazzo)".r.tr(10)
    )(
      gif"rphjb_StoDiventandoPazzo.mp4",
      mf"rphjb_CompletamentePazzo.mp4",
      mf"rphjb_CompletamentePazzo2.mp4",
    ),
    ReplyBundleMessage.textToMedia[F](
      "vo[l]+[o]*[u]+[ou]*me".r.tr(6)
    )(
      mf"rphjb_MenoVolume.mp3",
      mf"rphjb_VolumeTelevisori.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      "generi musicali",
      "solo il me(t|d)al".r.tr(13)
    )(
      gif"rphjb_GeneriMusicali.mp4",
      mf"rphjb_GeneriMusicali2.mp4",
    ),
    ReplyBundleMessage.textToMedia[F](
      "sorca",
      "patonza",
      "lecciso",
      "\\bfi[cg]a\\b".r.tr(4)
    )(
      gif"rphjb_SorcaLecciso.mp4",
      mf"rphjb_SorcaLecciso2.mp4",
      mf"rphjb_FigaLarga.mp4",
      mf"rphjb_FragolinaFichina.mp3",
      mf"rphjb_Sorca.mp4",
      mf"rphjb_LeccisoOffrire.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      "schifose",
      "ultime"
    )(
      gif"rphjb_SchifoseUltime.mp4",
      mf"rphjb_SchifoseUltime.mp4",
      mf"rphjb_ImparaASputareMignottaSchifose.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      "e parl[a]+\\b".r.tr(7)
    )(
      gif"rphjb_Parla.mp4",
      mf"rphjb_Parla2.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      "cosa è successo",
      "\\bcosa[?]{1,}\\b".r.tr(5)
    )(
      gif"rphjb_CosaSuccesso.mp4",
      mf"rphjb_Cosa.mp4",
      mf"rphjb_CosaCosaSuccessoMeNeVadoFacendoSoffrire.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      "negozio",
      "pantaloni",
      "shopping"
    )(
      mf"rphjb_Pantaloni.mp3",
      mf"rphjb_Pantaloni.mp4",
    ),
    ReplyBundleMessage.textToMedia[F](
      "sono finito",
      "ultimo stadio",
      "stanco"
    )(
      mf"rphjb_Stanco.mp3",
      mf"rphjb_Stanco.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      "ratzinger",
      "(il|er) vaticano".r.tr(11)
    )(
      mf"rphjb_AndateDaRatzinger.mp4",
      gif"rphjb_AndateDaRatzinger2.mp4",
      mf"rphjb_AndateDaRatzinger.mp3",
    ),
    ReplyBundleMessage.textToMedia[F](
      "non è possibile"
    )(
      gif"rphjb_NonPossibile.mp4",
      mf"rphjb_NonPossibile2.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      "cameriera",
      "moglie",
      "si sposa",
      "matrimonio"
    )(
      mf"rphjb_Cameriera.mp3",
      mf"rphjb_Cameriera.mp4",
    ),
    ReplyBundleMessage.textToMedia[F](
      "cos(a)? hai trovato?".r.tr(16)
    )(
      gif"rphjb_CosHaiTrovato.mp4",
      mf"rphjb_NonPossibile2.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      "accetto (le|qualsiasi) critich[ea]".r.tr(17),
    )(
      gif"rphjb_Escerto.mp4",
      mf"rphjb_CriticaNoCazzate.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      "pronto dimmi"
    )(
      gif"rphjb_ProntoDimmi.mp4",
      gif"rphjb_ProntoDimmi2.mp4",
    ),
    ReplyBundleMessage.textToMedia[F](
      "bassista",
      "slap"
    )(
      gif"rphjb_Bassista.gif",
      gif"rphjb_Basso.mp4",
    ),
    ReplyBundleMessage.textToMedia[F](
      "è vero[!?]+".r.tr(6)
    )(
      gif"rphjb_Vero.mp4",
      mf"rphjb_EraVero.mp4",
      mf"rphjb_SuonatoAbbastanzaBeneEVero.mp3"
    ),
    ReplyBundleMessage.textToMedia[F](
      "perchè (cazzo)? mi hai fatto ven[i|ì](re)?".r.tr(24)
    )(
      mf"rphjb_PercheCazzoMiHaiFattoVeni.mp3",
      mf"rphjb_PercheCazzoMiHaiFattoVeni.mp4",
    ),
    ReplyBundleMessage.textToMedia[F](
      "e[sc]+erto".r.tr(6),
      "non me ne frega un cazzo"
    )(
      gif"rphjb_Escerto.mp4",
      mf"rphjb_EscertoCritiche.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      "decido io",
    )(
      gif"rphjb_DecidoIo.mp4",
      mf"rphjb_DecidoIoMareCazzatePerCortesia.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      "mi piaccio",
      "impazzire",
    )(
      gif"rphjb_MiPiaccio.mp4",
      mf"rphjb_MiPiaccio2.mp4",
    ),
    ReplyBundleMessage.textToMedia[F](
      "giudica"
    )(
      gif"rphjb_Giudicate.mp4",
      gif"rphjb_ComeFaiAGiudicareGif.mp4",
      gif"rphjb_ComeFaiAGiudicare.mp4",
      mf"rphjb_NonPoteteGiudicarUrloThatsGood.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      "fregare come un co(gl|j)ione".r.tr(22),
      "ges[uùù]".r.tr(4)
    )(
      mf"rphjb_GesuCoglione.mp4",
      mf"rphjb_GesuCoglione.mp3"
    ),
    ReplyBundleMessage.textToMedia[F](
      "non sono uno del branco",
      "agende",
      "figli dei figli",
      "quali fiori",
      "diluite le vostre droghe",
      "non sono uno da sangue",
      "aghi di culto",
      "bucati[,]? ma da quale chiodo".r.tr(25),
      "gerarchie infernali"
    )(
      mf"rphjb_GerarchieInfernali.mp4",
      mf"rphjb_GerarchieInfernali.mp3",
      mf"rphjb_GerarchieInfernali2.mp4",
      mf"rphjb_GerarchieInfernali3.mp4",
    ),
    ReplyBundleMessage.textToMedia[F](
      "con questa tecnica"
    )(
      mf"rphjb_ConQuestaTecnica.mp4",
      mf"rphjb_ConQuestaTecnica.mp3"
    ),
    ReplyBundleMessage.textToMedia[F](
      "platinette",
      "due persone in una",
      "quando scopo me la levo",
      "mi levo tutto",
    )(
      mf"rphjb_Platinette.mp4",
      mf"rphjb_Platinette.mp3"
    ),
    ReplyBundleMessage.textToMedia[F](
      "il mio sbadiglio",
      "donna solo per un taglio",
      "labbro superiore"
    )(
      mf"rphjb_DonnaTaglioSbadiglio.mp4",
      mf"rphjb_DonnaTaglioSbadiglio.mp3"
    ),
    ReplyBundleMessage.textToMedia[F](
      "\\bvino\\b".r.tr(4),
    )(
      mf"rphjb_ChitarraZuccheroAlgheVino.mp3",
      mf"rphjb_Rimpinzati.mp4",
      mf"rphjb_Pasqua.mp4",
      gif"rphjb_LimoncelliVino.gif"
    ),
    ReplyBundleMessage.textToMedia[F](
      "chi tocca (\\w)[,]? muore".r.tr(16),
      "ciao (2001|duemilauno)".r.tr(9)
    )(
      mf"rphjb_Ciao2001.mp4",
      mf"rphjb_Ciao2001_2.mp4",
      gif"rphjb_Ciao2001Gif.mp4",
    ),
    ReplyBundleMessage.textToMedia[F](
      "alle (22|ventidue)".r.tr(7)
    )(
      mf"rphjb_Alle22.mp3",
      mf"rphjb_VenerdiAppuntamentoFissoFica.mp4",
      mf"rphjb_Alle22MercolediTelevita.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      "appuntamento"
    )(
      mf"rphjb_Appuntamento.mp3",
      mf"rphjb_VenerdiAppuntamentoFissoFica.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      "succh",
      "olio di croce"
    )(
      gif"rphjb_OlioDiCroce.mp4",
      mf"rphjb_OlioDiCroce.mp3",
      mf"rphjb_PoesiaNatalizia.mp3"
    ),
    ReplyBundleMessage.textToMedia[F](
      "scu[-]?sa[h]? scu[-]?sa[h]?".r.tr(11)
    )(
      mf"rphjb_Scusa.mp3",
      mf"rphjb_ScusaScusa.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      "dare fastidio"
    )(
      gif"rphjb_DareFastidio.mp4",
      mf"rphjb_Regressive.mp4",
    ),
    ReplyBundleMessage.textToMedia[F](
      "regressive"
    )(
      gif"rphjb_RegressiveGif.mp4",
      mf"rphjb_Regressive.mp4",
    ),
    ReplyBundleMessage.textToMedia[F](
      "rotto il cazzo"
    )(
      mf"rphjb_EBastaETuHaiRottoIlCazzo.mp4",
      mf"rphjb_BastaRottoIlCazzo.mp4",
      mf"rphjb_RottoIlCazzoUltimi.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      "\\bmula\\b".r.tr(4),
      "storia della mula"
    )(
      mf"rphjb_Mula.mp4",
      mf"rphjb_StoriaMula.mp3"
    ),
    ReplyBundleMessage.textToMedia[F](
      "primo sbaglio"
    )(
      mf"rphjb_PrimoSbaglio.mp3",
      mf"rphjb_PrimoSbaglio.mp4",
    ),
    ReplyBundleMessage.textToMedia[F](
      "poesia"
    )(
      mf"rphjb_PoesiaMadre.mp4",
      mf"rphjb_PoesiaRock.mp4",
      mf"rphjb_Blues.mp4",
      mf"rphjb_PoesiaMaria.mp4",
      mf"rphjb_PoesiaArtistiImpiegati.mp4",
      mf"rphjb_CanzonettePoesieAuschwitzCervello.mp4",
      mf"rphjb_PoesiaDirittoPaura.mp4",
      mf"rphjb_PoesiaNatalizia.mp3"
    ),
    ReplyBundleMessage.textToMedia[F](
      "auguri di natale",
      "buon natale",
      "merry christmas",
    )(
      mf"rphjb_AuguriDiNatale.mp4",
      mf"rphjb_RockChristmasHappyNewYear.mp3",
      mf"rphjb_AuguriDiNataleCapodannoFeste.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      "giuda"
    )(
      mf"rphjb_ChiECristo.mp3",
      mf"rphjb_GiudaFrocio.mp4",
      mf"rphjb_PoesiaNatalizia.mp3"
    ),
    ReplyBundleMessage.textToMedia[F](
      "buon anno",
      "happy new year",
      "capodanno",
    )(
      mf"rphjb_RockChristmasHappyNewYear.mp3",
      mf"rphjb_AuguriDiNataleCapodannoFeste.mp4",
      mf"rphjb_PassatoAnnoVitaContinua.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      "i rage",
      "(sentiamo|ascoltiamo|senti|ascolta) la musica".r.tr(23)
    )(
      gif"rphjb_SentiamoMusicaRageGif.mp4",
      mf"rphjb_SentiamoMusicaRage.mp4",
      mf"rphjb_SentiamoMusicaRage.mp3"
    ),
    ReplyBundleMessage.textToMedia[F](
      "sei cambiata tutta",
      "piercing",
      "mi fai male",
      "in mezzo alle gambe",
      "proprio[ ]?[l]+a".r.tr(9)
    )(
      mf"rphjb_CambiataTuttaPiercingPropriolla.mp4",
      mf"rphjb_CambiataTuttaPiercingPropriolla.mp3",
      gif"rphjb_CambiataTuttaPiercingPropriollaGif.mp4",
    ),
    ReplyBundleMessage.textToMedia[F](
      "luca di noia",
      "alla regia",
      "regista",
    )(
      mf"rphjb_LucaDiNoia.mp3",
      mf"rphjb_LucaDiNoia2.mp3",
      mf"rphjb_LucaDiNoia3.mp4",
      mf"rphjb_LucaDiNoia4.mp4",
      mf"rphjb_MatematiciAnaliticiDiNoia.mp3",
      mf"rphjb_MiPareLogicoMatematiciAnaliticiDiNoia.mp4",
      mf"rphjb_GrandeRegistaLucaDiNoia.mp3",
      mf"rphjb_GrandeRegistaLucaDiNoia.mp4",
      mf"rphjb_LucaDiNoiaGrandeRegista.mp4",
      mf"rphjb_LucaDiNoiaRegia.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      "angelo",
      "carpenelli",
      "via delle albizzie",
      "istinti musicali",
    )(
      mf"rphjb_AngeloTrovamelo.mp4",
      mf"rphjb_AlbizziePerlaPioggia.mp4",
      mf"rphjb_IstintiMusicali.mp3",
      mf"rphjb_IstintiMusicali.mp4",
      mf"rphjb_GrandeAngelo.mp4",
      mf"rphjb_AngeloCarpenelliGrandeViaDelleAlbizzie22NumeroUnoImmensoInGinocchio.mp4",
      mf"rphjb_AngeloCarpenelliViaDelleAlbizzie22IstintiMusicali.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      "questa è una domanda",
      "non ti rispondo",
      "(qualche )?altra domanda".r.tr(14)
    )(
      gif"rphjb_QualcheAltraDomandaGif.mp4",
      mf"rphjb_QualcheAltraDomanda.mp3",
      mf"rphjb_QualcheAltraDomanda.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      "occhi (come le donne )?degli anni (settanta|70)".r.tr(18)
    )(
      mf"rphjb_OcchiDonneAnniSettanta.mp3",
      mf"rphjb_OcchiDonneAnniSettanta.mp4",
      gif"rphjb_OcchiDonneAnniSettantaGif.mp4",
      mf"rphjb_Ester2.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      "tu non mi conosci",
      "posso cambiare",
      "sono camaleontico",
      "sputarti in faccia",
    )(
      mf"rphjb_SputartiInFacciaCamaleontico.mp4",
      mf"rphjb_SputartiInFacciaCamaleontico.mp3",
      gif"rphjb_SputartiInFacciaCamaleonticoGif.mp4",
    ),
    ReplyBundleMessage.textToMedia[F](
      "madre tortura",
      "(madre )?parrucca".r.tr(8)
    )(
      mf"rphjb_MadreTorturaParrucca.mp4",
      mf"rphjb_MadreTorturaImprovvisata.mp3"
    ),
    ReplyBundleMessage.textToMedia[F](
      "cazzate"
    )(
      gif"rphjb_NonPossibile.mp4",
      mf"rphjb_NonPossibile2.mp4",
      mf"rphjb_DecidoIoMareCazzatePerCortesia.mp4",
      mf"rphjb_AltraCazzataVeritaSembranoCazzate.mp4",
      mf"rphjb_CriticaNoCazzate.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      "\\b06\\b".r.tr(2),
      "prefisso"
    )(
      gif"rphjb_06Gif.mp4",
      mf"rphjb_06.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      "vengognati",
    )(
      mf"rphjb_VergognatiMatosFalasci.mp3",
      mf"rphjb_VergognatiMatosFalasci.mp4",
    ),
    ReplyBundleMessage.textToMedia[F](
      "andre matos"
    )(
      mf"rphjb_MatosShaman.mp3",
      mf"rphjb_AndreMatosShaman.mp4",
      mf"rphjb_VergognatiMatosFalasci.mp3",
      mf"rphjb_VergognatiMatosFalasci.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      "edu falasci",
      "edoardo falaschi",
    )(
      mf"rphjb_EduFalasci.mp3",
      mf"rphjb_VergognatiMatosFalasci.mp3",
      mf"rphjb_VergognatiMatosFalasci.mp4",
      mf"rphjb_EduFalasciQuasiFrocio.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      "shaman",
    )(
      mf"rphjb_MatosShaman.mp3",
      mf"rphjb_AndreMatosShaman.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      "assolutamente no",
      "non mi lamento"
    )(
      gif"rphjb_NonMiLamentoGif.mp4",
      mf"rphjb_NonMiLamento.mp4",
    ),
    ReplyBundleMessage.textToMedia[F](
      "fa paura pure a",
      "al di meola"
    )(
      mf"rphjb_PauraAdAlDiMeola.mp3",
      mf"rphjb_PauraAdAlDiMeola.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      "mettermi in difficoltà",
      "amicizie (politiche| d[ie] polizia| d[ie] carabinieri| d[ei] tutt'altr[o]? genere)?".r.tr(9),
      "amici potenti"
    )(
      gif"rphjb_DifficoltaAmicizieTelefonataGif.mp4",
      mf"rphjb_DifficoltaAmicizieTelefonata.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      "in un attimo",
      "risolto tutto",
      "telefonata",
    )(
      gif"rphjb_Telefonata.mp4",
      mf"rphjb_DifficoltaAmicizieTelefonata.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      "nudo([ -]nudo)+".r.tr(4),
    )(
      mf"rphjb_NudoFrocio.mp3",
      mf"rphjb_NudoNudo.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      "ragazza indemoniata",
    )(
      gif"rphjb_LaRagazzaIndemoniataGif.mp4",
      mf"rphjb_LaRagazzaIndemoniata.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      "non manca niente",
      "c'è tutto"
    )(
      gif"rphjb_NonMancaNienteGif.mp4",
      mf"rphjb_NonMancaNiente.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      "un avvertimento",
      "bastoni tra le ruote"
    )(
      gif"rphjb_Ciao2001Gif.mp4",
      mf"rphjb_Ciao2001_2.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      "fro(ci|sh)o([ -]fro(ci|sh)o)+".r.tr(5)
    )(
      mf"rphjb_NudoFrocio.mp3",
      mf"rphjb_FrocioFrocio.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      "kiko loureiro",
      "che salva la situazione"
    )(
      mf"rphjb_KikoLoureiroSalvaSituazione.mp3",
      mf"rphjb_CapitoCheMagagnaKikoLoureiroSalvaSituazione.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      "che magagna",
      "che fregatura",
    )(
      mf"rphjb_CapitoCheMagagna.mp3",
      mf"rphjb_CapitoCheMagagnaKikoLoureiroSalvaSituazione.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      "passaporto"
    )(
      mf"rphjb_PassaportoRiccardoBenzoni.mp3",
      mf"rphjb_LetsGoodMyFriendsPassport.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      "quante ore",
      "quanti anni",
      "quanto tempo"
    )(
      gif"rphjb_QuanteOreGif.mp4",
      mf"rphjb_QuanteOreQuantiAnni.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      "squallido",
      "ultimo degli esseri umani"
    )(
      gif"rphjb_BruttoSquallidoSchifosoGif.mp4",
      mf"rphjb_BruttoSquallidoSchifosoUltimoEsseriUmani.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      "quattro solo",
      "faccio in tempo"
    )(
      gif"rphjb_QuattroSolo.mp4",
      mf"rphjb_4SoloTempiInTestaOrologiSfuggonoPolsi.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      "televita"
    )(
      mf"rphjb_TelevitaSonoInizioRisata.mp3",
      mf"rphjb_Alle22MercolediTelevita.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      "mercoled[iì]".r.tr(9)
    )(
      mf"rphjb_TempoAlTempo.mp4",
      mf"rphjb_Alle22MercolediTelevita.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      "brutto frocio"
    )(
      mf"rphjb_BruttoFrocio.mp3",
      mf"rphjb_CambiaCanaleBruttoFrocio.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      "ogni volta"
    )(
      mf"rphjb_OgniVolta.mp3",
      mf"rphjb_OgniVolta.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      "mandragola",
      "fico sacro",
      "betulla",
      "canfora"
    )(
      mf"rphjb_FigureMitologiche.mp3",
      mf"rphjb_FigureMitologiche.mp4",
      mf"rphjb_FigureMitologiche2.mp4",
      mf"rphjb_FigureMitologiche3.mp4",
      mf"rphjb_AssaporarePezzoMentaMandragolaFicoSacroIncensoBetullaCanforaSaporeStrano.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      "assaporare",
      "incenso",
      "\\bmenta\\b".r.tr(5),
      "sapore (strano|indefinito)".r.tr(13),
    )(
      mf"rphjb_AssaporarePezzoMentaMandragolaFicoSacroIncensoBetullaCanforaSaporeStrano.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      "fai schifo",
      "sei l'ultimo",
    )(
      gif"rphjb_FaiSchifoSeiUltimoGif.mp4",
      mf"rphjb_FaiSchifoSeiUltimo.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      "graffi"
    )(
      gif"rphjb_Graffi.mp4",
      mf"rphjb_SentireMaleBeneCarezzaOppostoGraffiareGraceJonesMagari.mp3"
    ),
    ReplyBundleMessage.textToMedia[F](
      "voce da uomo"
    )(
      mf"rphjb_VoceDaUomo.mp3",
      mf"rphjb_CantanteDonnaVoceDaUomoDueBocceCosiInnamoratoPerdutamente.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      "delirio"
    )(
      gif"rphjb_Delirio.mp4",
      mf"rphjb_DelirioDelSabatoSera.mp4",
      mf"rphjb_CarrellataInfernaleDelirioPureNellaGolaTrasmissioneDaUrloEchoDelayVitaNaturalDurante.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      "nella gola"
    )(
      gif"rphjb_NellaGola.mp4",
      mf"rphjb_CarrellataInfernaleDelirioPureNellaGolaTrasmissioneDaUrloEchoDelayVitaNaturalDurante.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      "ma che (cazzo )?sto dicendo".r.tr(18)
    )(
      mf"rphjb_MaCheCazzoStoDicendo.mp3",
      mf"rphjb_MaCheCazzoStoDicendo.mp4",
      gif"rphjb_MaCheCazzoStoDicendoGif.mp4",
      gif"rphjb_IlMartel.mp4",
      mf"rphjb_CheStoDicendoDiVoltaIlCervelloPrincipiVeneziani.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      "come state"
    )(
      gif"rphjb_CiaoComeStateGif.mp4",
      mf"rphjb_CiaoComeState.mp3",
      mf"rphjb_CiaoComeState.mp4",
      mf"rphjb_Arivato.mp4",
      mf"rphjb_LetSGoodStateBene.mp3"
    ),
    ReplyBundleMessage.textToMedia[F](
      "(af)?fanculo in una maniera pazzesca".r.tr(33),
      "altro che quel coglione",
    )(
      gif"rphjb_AffanculoManieraPazzescaGif.mp4",
      mf"rphjb_AffanculoManieraPazzesca.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      "facendo soffrire"
    )(
      gif"rphjb_FacendoSoffrire.mp4",
      mf"rphjb_CosaCosaSuccessoMeNeVadoFacendoSoffrire.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      "\\bn[o]{2,}!\\b".r.tr(3)
    )(
      gif"rphjb_No.mp4",
      mf"rphjb_FolliaQueenNo.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      "\\bport[a]+\\b".r.tr(5)
    )(
      gif"rphjb_Porta.mp4",
      mf"rphjb_NonApriteQuellaPorta.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      "a questo punto",
      "andiamo[ci]? a sentire".r.tr(18),
      "l'originale",
    )(
      gif"rphjb_SentireOriginale.mp4",
      mf"rphjb_FeelingsIncazzarmiAndiamociSentireOriginale.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      "gigi d'alessio"
    )(
      mf"rphjb_GigiDAlessioAnnaTatangelo.mp3",
      mf"rphjb_GianniCelesteMeglioGigiDAlessio.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      "gianni celeste"
    )(
      gif"rphjb_GianniCelesteMeglioGigiDAlessio.mp4",
      mf"rphjb_RapMusicaMelodicaListaCantanti.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      "negri",
      "sezione ritmica"
    )(
      mf"rphjb_NegriSezioneRitmica.mp3",
      mf"rphjb_DueNegriMostruosi.mp4"
    ),
  )

}
