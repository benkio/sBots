package com.benkio.richardphjbensonbot.data

import com.benkio.telegrambotinfrastructure.messagefiltering._
import com.benkio.telegrambotinfrastructure.model._

object Mix {

  def messageRepliesMixData[F[_]]: List[ReplyBundleMessage[F]] = List(
    ReplyBundleMessage(
      TextTrigger(
        StringTextTriggerValue("vivi"),
        StringTextTriggerValue("morti")
      ),
      List(
        MediaFile("rphjb_ViviMorti.mp4")
      ),
      matcher = ContainsAll
    ),
    ReplyBundleMessage(
      TextTrigger(
        StringTextTriggerValue("è un ordine")
      ),
      List(
        MediaFile("rphjb_Ordine.mp3"),
        MediaFile("rphjb_Ordine.mp4")
      ),
      replySelection = RandomSelection
    ),
    ReplyBundleMessage(
      TextTrigger(
        StringTextTriggerValue("piloti d'aereo"),
        StringTextTriggerValue("disastri aerei")
      ),
      List(
        GifFile("rphjb_DrogatiPilotiGif.mp4"),
        MediaFile("rphjb_DrogatiPiloti.mp4")
      ),
      replySelection = RandomSelection
    ),
    ReplyBundleMessage(
      TextTrigger(
        RegexTextTriggerValue("\\brock\\b".r, 4)
      ),
      List(
        MediaFile("rphjb_PoesiaRock.mp4"),
        MediaFile("rphjb_Rock.mp3"),
        MediaFile("rphjb_EricClaptonDrogaUominiAffari.mp4"),
        MediaFile("rphjb_Rock.mp4")
      ),
      replySelection = RandomSelection
    ),
    ReplyBundleMessage(
      TextTrigger(
        StringTextTriggerValue("ti distruggo")
      ),
      List(
        GifFile("rphjb_TiDistruggo.mp4"),
        MediaFile("rphjb_Ramarro.mp4")
      ),
      replySelection = RandomSelection
    ),
    ReplyBundleMessage(
      TextTrigger(
        StringTextTriggerValue("cristo pinocchio"),
        StringTextTriggerValue("lumicino"),
        RegexTextTriggerValue("(strade|vie) inferiori".r, 13)
      ),
      List(
        MediaFile("rphjb_CristoPinocchio.mp3"),
        MediaFile("rphjb_CristoPinocchio.mp4"),
        MediaFile("rphjb_PoesiaMaria.mp4")
      ),
      replySelection = RandomSelection
    ),
    ReplyBundleMessage(
      TextTrigger(
        StringTextTriggerValue("pappalardo")
      ),
      List(
        MediaFile("rphjb_Pappalardo.mp3"),
        MediaFile("rphjb_Pappalardo.mp4"),
      ),
      replySelection = RandomSelection
    ),
    ReplyBundleMessage(
      TextTrigger(
        StringTextTriggerValue("lasciami in pace"),
        RegexTextTriggerValue("\\bstronza\\b".r, 7)
      ),
      List(
        GifFile("rphjb_LasciamiInPace.mp4"),
        MediaFile("rphjb_LasciamiInPaceStronza.mp4")
      ),
      replySelection = RandomSelection
    ),
    ReplyBundleMessage(
      TextTrigger(
        StringTextTriggerValue("rimpinzati"),
        RegexTextTriggerValue("(g|c)io(g|c)+ola(d|t)a".r, 9),
        StringTextTriggerValue("pandori"),
        StringTextTriggerValue("ciambelloni"),
        StringTextTriggerValue("gli amari"),
        RegexTextTriggerValue("limoncell(o|i)".r, 10),
        StringTextTriggerValue("ingrassati"),
        StringTextTriggerValue("andati al cesso"),
      ),
      List(
        GifFile("rphjb_Rimpinzati.mp4"),
        MediaFile("rphjb_Pasqua.mp4"),
      ),
      replySelection = RandomSelection
    ),
    ReplyBundleMessage(
      TextTrigger(
        StringTextTriggerValue("stare male"),
        StringTextTriggerValue("melensa")
      ),
      List(
        GifFile("rphjb_MiFaStareMale.mp4"),
        MediaFile("rphjb_MelensaStareMale.mp4")
      ),
      replySelection = RandomSelection
    ),
    ReplyBundleMessage(
      TextTrigger(
        RegexTextTriggerValue("[a]+[t]{2,}[e]+[n]+[z]+[i]+[o]+[n]+[e]+[!]*[!e]$".r, 10)
      ),
      List(
        MediaFile("rphjb_Attenzione.mp3"),
        MediaFile("rphjb_Attenzione.mp4")
      ),
      replySelection = RandomSelection
    ),
    ReplyBundleMessage(
      TextTrigger(
        StringTextTriggerValue("paradosso")
      ),
      List(
        GifFile("rphjb_ParadossoGif.mp4"),
        MediaFile("rphjb_Paradosso.mp4")
      ),
      replySelection = RandomSelection
    ),
    ReplyBundleMessage(
      TextTrigger(
        RegexTextTriggerValue("\\bsput[ao]\\b".r, 5)
      ),
      List(
        GifFile("rphjb_SputoGif.mp4"),
        MediaFile("rphjb_Sputo.mp4"),
        MediaFile("rphjb_BicchiereSputoLimitazioniUomoDonna.mp4")
      ),
      replySelection = RandomSelection
    ),
    ReplyBundleMessage(
      TextTrigger(
        StringTextTriggerValue("cobelini"),
        StringTextTriggerValue("cobbolidi"),
        StringTextTriggerValue("elfi"),
        RegexTextTriggerValue("\\bnani\\b".r, 4),
        StringTextTriggerValue("la mandragola"),
        StringTextTriggerValue("gobellini"),
        StringTextTriggerValue("fico sacro"),
        StringTextTriggerValue("la betulla"),
        StringTextTriggerValue("la canfora"),
        StringTextTriggerValue("le ossa dei morti")
      ),
      List(
        MediaFile("rphjb_FigureMitologiche.mp3"),
        MediaFile("rphjb_FigureMitologiche.mp4"),
        MediaFile("rphjb_FigureMitologiche2.mp4")
      ),
      replySelection = RandomSelection
    ),
    ReplyBundleMessage(
      TextTrigger(
        StringTextTriggerValue("anche la merda"),
        StringTextTriggerValue("senza culo")
      ),
      List(
        MediaFile("rphjb_Merda.mp3"),
        MediaFile("rphjb_AncheLaMerda.mp4")
      ),
      replySelection = RandomSelection
    ),
    ReplyBundleMessage(
      TextTrigger(
        StringTextTriggerValue("chiama la polizia")
      ),
      List(
        GifFile("rphjb_ChiamaLaPoliziaGif.mp4"),
        MediaFile("rphjb_ChiamaLaPolizia.mp4")
      ),
      replySelection = RandomSelection
    ),
    ReplyBundleMessage(
      TextTrigger(
        RegexTextTriggerValue("stori(a|e)".r, 6)
      ),
      List(
        MediaFile("rphjb_Storie.mp3"),
        MediaFile("rphjb_Storie2.mp3"),
        MediaFile("rphjb_StoriaNonDetta.mp4"),
        MediaFile("rphjb_StorieSonoTanteVecchiaccia.mp4"),
        MediaFile("rphjb_StoriaVeraPienaBugie.mp4")
      ),
      replySelection = RandomSelection
    ),
    ReplyBundleMessage(
      TextTrigger(
        RegexTextTriggerValue("au[ ]?de".r, 4),
        RegexTextTriggerValue("\\btime\\b".r, 4),
        RegexTextTriggerValue("uir[ ]?bi[ ]?taim".r, 9)
      ),
      List(
        MediaFile("rphjb_Audeuirbitaim.mp3"),
        MediaFile("rphjb_Audeuirbitaim2.mp3"),
        MediaFile("rphjb_Audeuirbitaim.mp4"),
      ),
      replySelection = RandomSelection
    ),
    ReplyBundleMessage(
      TextTrigger(
        StringTextTriggerValue("micetta"),
        StringTextTriggerValue("la morte")
      ),
      List(
        GifFile("rphjb_Micetta.mp4"),
        MediaFile("rphjb_LaMorteMicetta.mp4"),
        MediaFile("rphjb_LaMorte.mp4"),
        MediaFile("rphjb_LaMorte2.mp4"),
        MediaFile("rphjb_InnoAllaMorte.mp4")
      ),
      replySelection = RandomSelection
    ),
    ReplyBundleMessage(
      TextTrigger(
        RegexTextTriggerValue("\\bspalle\\b".r, 6),
        RegexTextTriggerValue("\\bbraccia\\b".r, 7),
        RegexTextTriggerValue("t(i|e) strozzo".r, 10)
      ),
      List(
        GifFile("rphjb_FaccioVedereSpalleBracciaGif.mp4"),
        MediaFile("rphjb_FaccioVedereSpalleBraccia.mp4"),
        MediaFile("rphjb_UccidereUnaPersona.mp4"),
      ),
      replySelection = RandomSelection
    ),
    ReplyBundleMessage(
      TextTrigger(
        StringTextTriggerValue("non sapere"),
        RegexTextTriggerValue("aris(d|t)o(d|t)ele".r, 10)
      ),
      List(
        GifFile("rphjb_SoDiNonSapereGif.mp4"),
        MediaFile("rphjb_SoDiNonSapere.mp4")
      ),
      replySelection = RandomSelection
    ),
    ReplyBundleMessage(
      TextTrigger(
        StringTextTriggerValue("non è roba per me")
      ),
      List(
        GifFile("rphjb_RobaPerMeGif.mp4"),
        MediaFile("rphjb_RobaPerMe.mp4")
      ),
      replySelection = RandomSelection
    ),
    ReplyBundleMessage(
      TextTrigger(
        RegexTextTriggerValue(" io \\bn[o]{2,}\\b".r, 6)
      ),
      List(
        MediaFile("rphjb_IoNo.mp3"),
        MediaFile("rphjb_GesuCoglione.mp4"),
        MediaFile("rphjb_GesuCoglione.mp3"),
      ),
      replySelection = RandomSelection
    ),
    ReplyBundleMessage(
      TextTrigger(
        StringTextTriggerValue("bastone infernale"),
        StringTextTriggerValue("un'arma")
      ),
      List(
        GifFile("rphjb_Bastone1.mp4"),
        GifFile("rphjb_Bastone2.mp4"),
        GifFile("rphjb_Bastone3.mp4"),
        MediaFile("rphjb_BastoneInfernale.mp4")
      ),
      replySelection = RandomSelection
    ),
    ReplyBundleMessage(
      TextTrigger(
        StringTextTriggerValue("vi calpesto"),
        StringTextTriggerValue("vermi"),
        StringTextTriggerValue("strisciate per terra")
      ),
      List(
        GifFile("rphjb_ViCalpestoGif.mp4"),
        MediaFile("rphjb_ViCalpesto.mp3"),
        MediaFile("rphjb_ViCalpesto.mp4")
      ),
      replySelection = RandomSelection
    ),
    ReplyBundleMessage(
      TextTrigger(
        StringTextTriggerValue("stringere i denti"),
        StringTextTriggerValue("andare avanti"),
      ),
      List(
        GifFile("rphjb_AndareAvanti.mp4"),
        MediaFile("rphjb_AndareAvanti.mp3"),
        MediaFile("rphjb_InnovazioneStiamoTornandoIndietro.mp4")
      ),
      replySelection = RandomSelection
    ),
    ReplyBundleMessage(
      TextTrigger(
        StringTextTriggerValue("non ci credete?"),
        RegexTextTriggerValue("grande s(d|t)ronza(d|t)(e|a)".r, 16)
      ),
      List(
        GifFile("rphjb_NonCiCredete.mp4"),
        MediaFile("rphjb_NonCiCredete.mp3"),
      ),
      replySelection = RandomSelection
    ),
    ReplyBundleMessage(
      TextTrigger(
        StringTextTriggerValue("non me ne fotte"),
        StringTextTriggerValue("chissenefrega"),
        StringTextTriggerValue("non mi interessa")
      ),
      List(
        GifFile("rphjb_NonMeNeFotte.mp4"),
        GifFile("rphjb_NonMeNeFrega.mp4"),
        MediaFile("rphjb_NonMiFregaParloIo.mp4"),
        MediaFile("rphjb_ENonMeNeFotteUnCazzo.mp3"),
        MediaFile("rphjb_NonLeggoQuelloCheScrivete.mp3")
      ),
      replySelection = RandomSelection
    ),
    ReplyBundleMessage(
      TextTrigger(
        StringTextTriggerValue("ultimi")
      ),
      List(
        GifFile("rphjb_ViCalpestoGif.mp4"),
        MediaFile("rphjb_ViCalpesto.mp3"),
        MediaFile("rphjb_ViCalpesto.mp4"),
        GifFile("rphjb_Ultimi.mp4"),
        MediaFile("rphjb_StateZittiZozziUltimi.mp3"),
      ),
      replySelection = RandomSelection
    ),
    ReplyBundleMessage(
      TextTrigger(
        RegexTextTriggerValue("che (cazzo )?era quella roba".r, 19),
        RegexTextTriggerValue("che (cazzo |cazzo di roba )?mi avete dato".r, 17),
        StringTextTriggerValue("lampi negli occhi"),
        RegexTextTriggerValue("gira(re|ra|rà|ndo)? la testa".r, 13),
        RegexTextTriggerValue("insieme alla (c|g)o(c|g)a (c|g)ola".r, 22)
      ),
      List(
        MediaFile("rphjb_CheCazzoEraQuellaRoba.mp3"),
        MediaFile("rphjb_CheCazzoEraQuellaRoba.mp4"),
        MediaFile("rphjb_CheCazzoEraQuellaRoba2.mp4"),
        MediaFile("rphjb_CheCazzoEraQuellaRoba3.mp4"),
        MediaFile("rphjb_RockMachineIntro.mp4"),
        MediaFile("rphjb_MaCheCazzoEraQuellaRobaDroghe.mp3")
      ),
      replySelection = RandomSelection
    ),
    ReplyBundleMessage(
      TextTrigger(
        StringTextTriggerValue("canzonette"),
        StringTextTriggerValue("balera"),
        StringTextTriggerValue("sagra dell'uva"),
        StringTextTriggerValue("feste condominiali"),
        StringTextTriggerValue("feste di piazza")
      ),
      List(
        MediaFile("rphjb_Canzonette.mp3"),
        MediaFile("rphjb_Canzonette.mp4"),
        MediaFile("rphjb_Canzonette.gif")
      ),
      replySelection = RandomSelection
    ),
    ReplyBundleMessage(
      TextTrigger(
        StringTextTriggerValue("un pollo")
      ),
      List(
        MediaFile("rphjb_Pollo.mp3"),
        MediaFile("rphjb_Pollo.mp4"),
        MediaFile("rphjb_Pollo2.mp4"),
        GifFile("rphjb_PolloGif.mp4")
      ),
      replySelection = RandomSelection
    ),
    ReplyBundleMessage(
      TextTrigger(
        StringTextTriggerValue("quello che dico io")
      ),
      List(
        GifFile("rphjb_QuelloCheDicoIo.mp4"),
        MediaFile("rphjb_FannoQuelloCheDicoIo.mp3"),
        MediaFile("rphjb_FannoQuelloCheDicoIo.mp4")
      ),
      replySelection = RandomSelection
    ),
    ReplyBundleMessage(
      TextTrigger(
        StringTextTriggerValue("zucchero")
      ),
      List(
        MediaFile("rphjb_ChitarraZuggherada.mp3"),
        MediaFile("rphjb_ChitarraZuccherada.mp4"),
        MediaFile("rphjb_ChitarraZuccheroAlgheVino.mp3"),
        MediaFile("rphjb_Zucchero.mp3"),
        MediaFile("rphjb_ZuccheroVanigliaCremaCioccolataPandoro.mp3"),
        MediaFile("rphjb_AuguriPerPasqua.mp4")
      ),
      replySelection = RandomSelection
    ),
    ReplyBundleMessage(
      TextTrigger(
        RegexTextTriggerValue("\\bgood\\b".r, 4),
        RegexTextTriggerValue("\\bshow\\b".r, 4),
        RegexTextTriggerValue("\\bfriends\\b".r, 7)
      ),
      List(
        GifFile("rphjb_OkGoodShowFriends.mp4"),
        GifFile("rphjb_OkGoodShowFriends2.mp4"),
        MediaFile("rphjb_LetSGoodStateBene.mp3"),
        MediaFile("rphjb_WelaMyFriends.mp4"),
        MediaFile("rphjb_LetsGoodMyFriends.mp4"),
        MediaFile("rphjb_NonPoteteGiudicarUrloThatsGood.mp4")
      ),
      replySelection = RandomSelection
    ),
    ReplyBundleMessage(
      TextTrigger(
        RegexTextTriggerValue("vattene (a f|a[f]*)?fanculo".r, 16)
      ),
      List(
        GifFile("rphjb_MaVatteneAffanculo.mp4"),
        MediaFile("rphjb_MaVatteneAffanculo.mp3"),
        MediaFile("rphjb_PortlandVancuverFanculo.mp4"),
        GifFile("rphjb_FanculoPerCortesia.mp4"),
        MediaFile("rphjb_DecidoIoMareCazzatePerCortesia.mp4")
      ),
      replySelection = RandomSelection
    ),
    ReplyBundleMessage(
      TextTrigger(
        StringTextTriggerValue("feelings")
      ),
      List(
        MediaFile("rphjb_Feelings.gif"),
        GifFile("rphjb_Feelings2.mp4"),
        MediaFile("rphjb_Feelings.mp3"),
        MediaFile("rphjb_Feelings.mp4")
      ),
      replySelection = RandomSelection
    ),
    ReplyBundleMessage(
      TextTrigger(
        StringTextTriggerValue("me ne vado")
      ),
      List(
        MediaFile("rphjb_MeNeVado.mp3"),
        GifFile("rphjb_MiRompiErCazzo.mp4"),
        GifFile("rphjb_MeNeVado.mp4")
      ),
      replySelection = RandomSelection
    ),
    ReplyBundleMessage(
      TextTrigger(
        StringTextTriggerValue("mignotta"),
        StringTextTriggerValue("puttana"),
        StringTextTriggerValue("troia")
      ),
      List(
        MediaFile("rphjb_Mignotta.mp3"),
        GifFile("rphjb_Mignotta.mp4"),
        MediaFile("rphjb_VialeZara.mp3"),
        MediaFile("rphjb_StronzoFiglioMignotta.mp4")
      ),
      replySelection = RandomSelection
    ),
    ReplyBundleMessage(
      TextTrigger(
        StringTextTriggerValue("ti devi spaventare")
      ),
      List(
        MediaFile("rphjb_TiDeviSpaventare.mp3"),
        GifFile("rphjb_TiDeviSpaventareGif.mp4"),
        MediaFile("rphjb_TiDeviSpaventare.mp4")
      ),
      replySelection = RandomSelection
    ),
    ReplyBundleMessage(
      TextTrigger(
        StringTextTriggerValue("ma che cazzo sto dicendo"),
        StringTextTriggerValue("il martell")
      ),
      List(
        MediaFile("rphjb_MaCheCazzoStoDicendo.mp3"),
        MediaFile("rphjb_MaCheCazzoStoDicendo.mp4"),
        GifFile("rphjb_MaCheCazzoStoDicendoGif.mp4"),
        GifFile("rphjb_IlMartel.mp4")
      ),
      replySelection = RandomSelection
    ),
    ReplyBundleMessage(
      TextTrigger(
        StringTextTriggerValue("questa volta no")
      ),
      List(
        MediaFile("rphjb_QuestaVoltaNo.mp3"),
        GifFile("rphjb_QuestaVoltaNo.mp4")
      ),
      replySelection = RandomSelection
    ),
    ReplyBundleMessage(
      TextTrigger(
        StringTextTriggerValue("una vergogna")
      ),
      List(
        MediaFile("rphjb_Vergogna.mp3"),
        MediaFile("rphjb_Vergogna.mp4"),
        GifFile("rphjb_VergognaGif.mp4"),
        GifFile("rphjb_Vergogna2.mp4")
      ),
      replySelection = RandomSelection
    ),
    ReplyBundleMessage(
      TextTrigger(
        StringTextTriggerValue("mi devo trasformare"),
        StringTextTriggerValue("cristo canaro")
      ),
      List(
        MediaFile("rphjb_Trasformista.mp3"),
        GifFile("rphjb_Trasformista.mp4"),
        MediaFile("rphjb_CristoCanaro.mp4")
      ),
      replySelection = RandomSelection
    ),
    ReplyBundleMessage(
      TextTrigger(
        RegexTextTriggerValue("ma[ ]?s(c|g)us[a]?".r, 5)
      ),
      List(
        MediaFile("rphjb_MaSgus.mp3"),
        GifFile("rphjb_MaSgus.mp4")
      ),
      replySelection = RandomSelection
    ),
    ReplyBundleMessage(
      TextTrigger(
        StringTextTriggerValue("grazie gianni"),
      ),
      List(
        MediaFile("rphjb_Grazie.mp3"),
        GifFile("rphjb_GrazieGif.mp4"),
        MediaFile("rphjb_Grazie.mp4")
      ),
      replySelection = RandomSelection
    ),
    ReplyBundleMessage(
      TextTrigger(
        RegexTextTriggerValue("cia[o]{3,}".r, 6)
      ),
      List(
        MediaFile("rphjb_Grazie.mp3"),
        GifFile("rphjb_GrazieGif.mp4"),
        MediaFile("rphjb_Grazie.mp4"),
        MediaFile("rphjb_ViSaluto.mp3")
      ),
      replySelection = RandomSelection
    ),
    ReplyBundleMessage(
      TextTrigger(
        StringTextTriggerValue("stare attenti"),
        StringTextTriggerValue("per strada")
      ),
      List(
        MediaFile("rphjb_IncontratePerStrada.mp3"),
        GifFile("rphjb_IncontratePerStrada.mp4")
      ),
      replySelection = RandomSelection
    ),
    ReplyBundleMessage(
      TextTrigger(
        StringTextTriggerValue("lavora tu vecchiaccia"),
        StringTextTriggerValue("hai la pelle dura"),
        StringTextTriggerValue("io sono creatura")
      ),
      List(
        MediaFile("rphjb_LavoraTu.mp3"),
        MediaFile("rphjb_LavoraTu.mp4"),
        MediaFile("rphjb_LavoraTu2.mp4"),
        GifFile("rphjb_LavoraTuGif.mp4"),
        MediaFile("rphjb_StorieSonoTanteVecchiaccia.mp4")
      ),
      replySelection = RandomSelection
    ),
    ReplyBundleMessage(
      TextTrigger(
        RegexTextTriggerValue("infern[a]+l[ie]+[!]*".r, 9)
      ),
      List(
        MediaFile("rphjb_Infernali.mp3"),
        GifFile("rphjb_Infernali.mp4"),
        MediaFile("rphjb_Infernale.mp3")
      ),
      replySelection = RandomSelection
    ),
    ReplyBundleMessage(
      TextTrigger(
        StringTextTriggerValue("per il culo")
      ),
      List(
        MediaFile("rphjb_PigliandoPerIlCulo.mp3"),
        GifFile("rphjb_PigliandoPerIlCulo.mp4")
      ),
      replySelection = RandomSelection
    ),
    ReplyBundleMessage(
      TextTrigger(
        StringTextTriggerValue("sorriso"),
        RegexTextTriggerValue("(😂|🤣){4,}".r, 4),
        RegexTextTriggerValue("(😄|😀|😃){4,}".r, 4),
        RegexTextTriggerValue("(ah|ha){7,}".r, 14)
      ),
      List(
        MediaFile("rphjb_Risata.mp3"),
        MediaFile("rphjb_Risata.mp4"),
        GifFile("rphjb_RisataGif.mp4"),
        MediaFile("rphjb_OrmaiRisata.mp4"),
        MediaFile("rphjb_Sorriso2.gif"),
        GifFile("rphjb_Sorriso.mp4"),
        GifFile("rphjb_SorrisoSognante.mp4"),
        MediaFile("rphjb_Risata2.mp3"),
        MediaFile("rphjb_SepolturaRisata.mp4"),
        GifFile("rphjb_RisataTrattenuta.mp4")
      ),
      replySelection = RandomSelection
    ),
    ReplyBundleMessage(
      TextTrigger(
        StringTextTriggerValue("ammazza che sei"),
        StringTextTriggerValue("quasi un frocio")
      ),
      List(
        MediaFile("rphjb_Frocio.mp3"),
        GifFile("rphjb_Frocio.mp4")
      ),
      replySelection = RandomSelection
    ),
    ReplyBundleMessage(
      TextTrigger(
        RegexTextTriggerValue("(fammi|chiedere)? (una|questa)? cortesia".r, 18)
      ),
      List(
        MediaFile("rphjb_FammiQuestaCortesia.mp3"),
        GifFile("rphjb_FammiQuestaCortesia.mp4")
      ),
      replySelection = RandomSelection
    ),
    ReplyBundleMessage(
      TextTrigger(
        StringTextTriggerValue("non mi sta bene")
      ),
      List(
        MediaFile("rphjb_NonMiStaBene.mp3"),
        MediaFile("rphjb_NonMiStaBene2.mp3"),
        GifFile("rphjb_NonMiStaBeneGif.mp4"),
        GifFile("rphjb_NonMiStaBene2.mp4"),
        MediaFile("rphjb_NonMiStaBene.mp4")
      ),
      replySelection = RandomSelection
    ),
    ReplyBundleMessage(
      TextTrigger(
        StringTextTriggerValue("le labbra"),
      ),
      List(
        MediaFile("rphjb_Labbra.mp3"),
        GifFile("rphjb_Labbra.mp4")
      ),
      replySelection = RandomSelection
    ),
    ReplyBundleMessage(
      TextTrigger(
        StringTextTriggerValue("la vita è il nemico")
      ),
      List(
        MediaFile("rphjb_VitaNemico.mp3"),
        GifFile("rphjb_VitaNemicoGif.mp4"),
        MediaFile("rphjb_VitaNemico.mp4"),
        MediaFile("rphjb_VitaNemico2.mp4"),
        MediaFile("rphjb_VitaNemicoCervello.mp4")
      ),
      replySelection = RandomSelection
    ),
    ReplyBundleMessage(
      TextTrigger(
        StringTextTriggerValue("permettere")
      ),
      List(
        MediaFile("rphjb_Permettere.mp3"),
        GifFile("rphjb_Permettere.mp4"),
        MediaFile("rphjb_Labrie.mp4")
      ),
      replySelection = RandomSelection
    ),
    ReplyBundleMessage(
      TextTrigger(
        StringTextTriggerValue("le note")
      ),
      List(
        MediaFile("rphjb_Note.mp3"),
        GifFile("rphjb_Note.mp4")
      ),
      replySelection = RandomSelection
    ),
    ReplyBundleMessage(
      TextTrigger(
        RegexTextTriggerValue("te[r]+[i]+[b]+[i]+l[e]+".r, 8)
      ),
      List(
        MediaFile("rphjb_Terribile.mp3"),
        MediaFile("rphjb_Terribile.mp4"),
        GifFile("rphjb_TerribileGif.mp4")
      ),
      replySelection = RandomSelection
    ),
    ReplyBundleMessage(
      TextTrigger(
        StringTextTriggerValue("viva napoli")
      ),
      List(
        MediaFile("rphjb_VivaNapoli.mp3"),
        GifFile("rphjb_VivaNapoli.mp4")
      ),
      replySelection = RandomSelection
    ),
    ReplyBundleMessage(
      TextTrigger(
        StringTextTriggerValue("ciao a tutti"),
        StringTextTriggerValue("come state"),
        StringTextTriggerValue("belle gioie")
      ),
      List(
        GifFile("rphjb_CiaoComeState.mp4"),
        MediaFile("rphjb_CiaoComeState.mp3")
      ),
      replySelection = RandomSelection
    ),
    ReplyBundleMessage(
      TextTrigger(
        RegexTextTriggerValue("\\bbasta(a|!){2,}".r, 7)
      ),
      List(
        MediaFile("rphjb_Basta.mp3"),
        GifFile("rphjb_Basta.mp4"),
        GifFile("rphjb_Basta2.mp4"),
        MediaFile("rphjb_Basta2.mp3"),
        GifFile("rphjb_BastaGif.mp4"),
        GifFile("rphjb_Basta2Gif.mp4"),
        GifFile("rphjb_Basta3.mp4"),
        GifFile("rphjb_Basta4.mp4"),
        GifFile("rphjb_BastaRottoIlCazzo.mp4"),
        GifFile("rphjb_BastaSedia.mp4")
      ),
      replySelection = RandomSelection
    ),
    ReplyBundleMessage(
      TextTrigger(
        StringTextTriggerValue("assolo"),
        RegexTextTriggerValue("(g|c)hi(t|d)arra".r, 8),
        RegexTextTriggerValue("(as)?solo di basso".r, 13)
      ),
      List(
        MediaFile("rphjb_Assolo.mp3"),
        MediaFile("rphjb_Assolo.mp4"),
        MediaFile("rphjb_Assolo2.mp4"),
        MediaFile("rphjb_AssoloBeat.mp4"),
        MediaFile("rphjb_AssoloSubdolo.mp4"),
        MediaFile("rphjb_Basso.mp4"),
        GifFile("rphjb_Chitarra1.mp4"),
        GifFile("rphjb_Chitarra2.mp4"),
        GifFile("rphjb_Chitarra3.mp4"),
        MediaFile("rphjb_ChitarraPlettroVicoletto.mp4"),
        MediaFile("rphjb_ChitarraPlettroVicoletto2.mp4"),
        MediaFile("rphjb_ChitarraZuggherada.mp3"),
        MediaFile("rphjb_ChitarraZuccherada.mp4"),
        MediaFile("rphjb_ChitarraZuccheroAlgheVino.mp3"),
        MediaFile("rphjb_AssoloBasso.mp4")
      ),
      replySelection = RandomSelection
    ),
    ReplyBundleMessage(
      TextTrigger(
        RegexTextTriggerValue("\\b(g|c)a(b|p)i(d|t)o\\b".r, 6),
        RegexTextTriggerValue("\\bcapissi\\b".r, 7),
      ),
      List(
        MediaFile("rphjb_HoCapito.mp3"),
        MediaFile("rphjb_AveteCapito.mp3"),
        MediaFile("rphjb_AveteCapito.mp4"),
        MediaFile("rphjb_Capito.mp3"),
        MediaFile("rphjb_NonHannoCapitoUnCazzo.mp3"),
        MediaFile("rphjb_NonAveteCapitoUnCazzo.mp4"),
        GifFile("rphjb_AveteCapitoComeSempre.mp4"),
        GifFile("rphjb_NonAveteCapitoUnCazzoGif.mp4"),
        GifFile("rphjb_VoiNonAveteCapitoUnCazzo.mp4"),
        GifFile("rphjb_IlSensoCapito.mp4"),
        GifFile("rphjb_CapitoDoveStiamo.mp4"),
        GifFile("rphjb_NonHoCapito.mp4"),
        GifFile("rphjb_AveteCapitoEh.mp4"),
        GifFile("rphjb_ComeAlSolitoNonAveteCapito.mp4"),
        MediaFile("rphjb_CapitoDoveStiamo.mp3"),
        MediaFile("rphjb_CapisciRidotti.mp3"),
        MediaFile("rphjb_CapitoCheMagagna.mp3"),
        MediaFile("rphjb_DavantiGenteNonHaCapisceUnCazzo.mp3")
      ),
      replySelection = RandomSelection
    ),
    ReplyBundleMessage(
      TextTrigger(
        StringTextTriggerValue("esperiment"),
        RegexTextTriggerValue("1(,)? 2(,)? 3".r, 5),
        RegexTextTriggerValue("uno(,)? due(,)? tre".r, 11)
      ),
      List(
        MediaFile("rphjb_Esperimento.mp3"),
        MediaFile("rphjb_Esperimento.mp4"),
        MediaFile("rphjb_Esperimento2.mp4"),
        GifFile("rphjb_EsperimentoGif.mp4"),
        GifFile("rphjb_Esperimento2Gif.mp4"),
        GifFile("rphjb_Esperimento3.mp4"),
        MediaFile("rphjb_DiciottoAnni.mp4"),
      ),
      replySelection = RandomSelection
    ),
    ReplyBundleMessage(
      TextTrigger(
        StringTextTriggerValue("schifosi")
      ),
      List(
        GifFile("rphjb_ViCalpestoGif.mp4"),
        MediaFile("rphjb_ViCalpesto.mp3"),
        MediaFile("rphjb_ViCalpesto.mp4"),
        MediaFile("rphjb_Schifosi.mp3"),
        MediaFile("rphjb_Schifosi.mp4"),
        MediaFile("rphjb_Schifosi2.mp3"),
        MediaFile("rphjb_Schifosi3.mp3"),
        MediaFile("rphjb_Schifosi4.mp3"),
        MediaFile("rphjb_Schifosi4.mp4"),
        GifFile("rphjb_Schifosi3.mp4"),
        MediaFile("rphjb_SchifosoUltimi.mp4"),
        MediaFile("rphjb_StateZittiZozziUltimi.mp3"),
        GifFile("rphjb_SchifosiGif.mp4"),
        GifFile("rphjb_Schifosi2.mp4"),
        MediaFile("rphjb_Vigile.mp4"),
        MediaFile("rphjb_ConQuestaTecnica.mp4"),
        MediaFile("rphjb_ConQuestaTecnica.mp3")
      ),
      replySelection = RandomSelection
    ),
    ReplyBundleMessage(
      TextTrigger(
        RegexTextTriggerValue("schifos(o)+(!)*".r, 8)
      ),
      List(
        GifFile("rphjb_Schifoso.mp4"),
        MediaFile("rphjb_Vigile.mp4"),
        MediaFile("rphjb_AmoreSuonareFreddoBuddistiSchifoso.mp4"),
        GifFile("rphjb_BruttoSquallidoSchifoso.mp4")
      ),
      replySelection = RandomSelection
    ),
    ReplyBundleMessage(
      TextTrigger(
        StringTextTriggerValue("mortacci vostri")
      ),
      List(
        GifFile("rphjb_MortacciVostriGif.mp4"),
        MediaFile("rphjb_StateZittiZozziUltimi.mp3"),
        MediaFile("rphjb_ConQuestaTecnica.mp4"),
        MediaFile("rphjb_ConQuestaTecnica.mp3"),
        MediaFile("rphjb_MortacciVostri.mp4"),
        MediaFile("rphjb_CheCazzoEraQuellaRoba2.mp4")
      ),
      replySelection = RandomSelection
    ),
    ReplyBundleMessage(
      TextTrigger(
        StringTextTriggerValue("non vedo")
      ),
      List(
        MediaFile("rphjb_Stanco.mp3"),
        MediaFile("rphjb_Stanco.mp4"),
        MediaFile("rphjb_PannaOcchialiSpalla.mp3"),
      ),
      replySelection = RandomSelection
    ),
    ReplyBundleMessage(
      TextTrigger(
        StringTextTriggerValue("panna")
      ),
      List(
        MediaFile("rphjb_Problema.mp3"),
        MediaFile("rphjb_PannaOcchialiSpalla.mp3")
      ),
      replySelection = RandomSelection
    ),
    ReplyBundleMessage(
      TextTrigger(
        RegexTextTriggerValue("\\bapplauso\\b".r, 8)
      ),
      List(
        GifFile("rphjb_Applauso.mp4"),
        MediaFile("rphjb_Applauso.mp3"),
        MediaFile("rphjb_Applauso2.mp3"),
        MediaFile("rphjb_ApplausoPiuForte.mp4")
      ),
      replySelection = RandomSelection
    ),
    ReplyBundleMessage(
      TextTrigger(
        StringTextTriggerValue("venite qua")
      ),
      List(
        GifFile("rphjb_VeniteQuaGif.mp4"),
        MediaFile("rphjb_VeniteQua.mp3"),
        MediaFile("rphjb_VeniteQua.mp4")
      ),
      replySelection = RandomSelection
    ),
    ReplyBundleMessage(
      TextTrigger(
        RegexTextTriggerValue("\\bpaga(re)?\\b".r, 4),
        StringTextTriggerValue("soldi"),
        StringTextTriggerValue("bollette"),
        StringTextTriggerValue("tasse"),
        StringTextTriggerValue("bolletta"),
        StringTextTriggerValue("tassa")
      ),
      List(
        GifFile("rphjb_ChiCacciaISoldi.mp4"),
        MediaFile("rphjb_ChiCacciaISoldi.mp3"),
        MediaFile("rphjb_SoldiButtatiDiscotecaLaziale.mp3"),
        MediaFile("rphjb_BigMoney.mp4"),
        MediaFile("rphjb_InvestitoreGoverno.mp4")
      ),
      replySelection = RandomSelection
    ),
    ReplyBundleMessage(
      TextTrigger(
        RegexTextTriggerValue("[od]?dio mio[,]? no".r, 10)
      ),
      List(
        GifFile("rphjb_OddioMioNoGif.mp4"),
        MediaFile("rphjb_OddioMioNo.mp3"),
        MediaFile("rphjb_OddioMioNo2.mp3"),
        GifFile("rphjb_OddioMioNo.mp4")
      ),
      replySelection = RandomSelection
    ),
    ReplyBundleMessage(
      TextTrigger(
        RegexTextTriggerValue("[sono ]?a[r]{1,2}iva(d|t)o".r, 12),
        RegexTextTriggerValue("(eccomi|ciao).*\\bpiacere\\b".r, 13)
      ),
      List(
        GifFile("rphjb_ArivatoGif.mp4"),
        MediaFile("rphjb_Arivato.mp3"),
        GifFile("rphjb_Arivato.mp4")
      ),
      replySelection = RandomSelection
    ),
    ReplyBundleMessage(
      TextTrigger(
        RegexTextTriggerValue("delu(s|d)".r, 5)
      ),
      List(
        GifFile("rphjb_Deluso.mp4"),
        MediaFile("rphjb_Deluso.mp3"),
        MediaFile("rphjb_DeludendoQuasiTutto.mp4")
      ),
      replySelection = RandomSelection
    ),
    ReplyBundleMessage(
      TextTrigger(
        StringTextTriggerValue("fate come vi pare"),
        RegexTextTriggerValue("sti (g|c)azzi".r, 9)
      ),
      List(
        GifFile("rphjb_ComeViPare.mp4"),
        MediaFile("rphjb_ComeViPare.mp3")
      ),
      replySelection = RandomSelection
    ),
    ReplyBundleMessage(
      TextTrigger(
        StringTextTriggerValue("divento una bestia"),
        StringTextTriggerValue("incazzo")
      ),
      List(
        MediaFile("rphjb_DiventoBestia.mp3"),
        MediaFile("rphjb_Incazzo.mp3"),
        MediaFile("rphjb_Incazzo2.mp3")
      ),
      replySelection = RandomSelection
    ),
    ReplyBundleMessage(
      TextTrigger(
        StringTextTriggerValue("dove stiamo"),
        StringTextTriggerValue("stiamo nella follia")
      ),
      List(
        MediaFile("rphjb_CapitoDoveStiamo.mp3"),
        MediaFile("rphjb_StiamoNellaFollia.mp4"),
        GifFile("rphjb_CapitoDoveStiamo.mp4")
      ),
      replySelection = RandomSelection
    ),
    ReplyBundleMessage(
      TextTrigger(
        StringTextTriggerValue("non sai molto")
      ),
      List(
        GifFile("rphjb_NonSaiMolto.mp4"),
        MediaFile("rphjb_RadioRockErrori.mp4")
      ),
      replySelection = RandomSelection
    ),
    ReplyBundleMessage(
      TextTrigger(
        StringTextTriggerValue("errori")
      ),
      List(
        GifFile("rphjb_MaiErroriGif.mp4"),
        MediaFile("rphjb_MaiErrori.mp4")
      ),
      replySelection = RandomSelection
    ),
    ReplyBundleMessage(
      TextTrigger(
        RegexTextTriggerValue("\\bpasqua\\b".r, 6)
      ),
      List(
        MediaFile("rphjb_AuguriPasqua.mp3"),
        MediaFile("rphjb_AuguriPerPasqua.mp4")
      ),
      replySelection = RandomSelection
    ),
    ReplyBundleMessage(
      TextTrigger(
        StringTextTriggerValue("vaniglia"),
        StringTextTriggerValue("pandoro"),
        RegexTextTriggerValue("crema alla (g|c)io(g|c)+ola(d|t)a".r, 20),
      ),
      List(
        MediaFile("rphjb_ZuccheroVanigliaCremaCioccolataPandoro.mp3"),
        MediaFile("rphjb_AuguriPerPasqua.mp4")
      ),
      replySelection = RandomSelection
    ),
    ReplyBundleMessage(
      TextTrigger(
        StringTextTriggerValue("c'hai timore"),
        StringTextTriggerValue("c'hai paura"),
        RegexTextTriggerValue("diri[g]+en(d|t)i".r, 9),
      ),
      List(
        GifFile("rphjb_Dirigenti.mp4"),
        MediaFile("rphjb_AncoraNoDirigenti.mp4")
      ),
      replySelection = RandomSelection
    ),
    ReplyBundleMessage(
      TextTrigger(
        StringTextTriggerValue("guerra")
      ),
      List(
        GifFile("rphjb_GuerraTotaleGif.mp4"),
        MediaFile("rphjb_GuerraTotale.mp4")
      ),
      replySelection = RandomSelection
    ),
    ReplyBundleMessage(
      TextTrigger(
        StringTextTriggerValue("non voglio nessuno"),
        StringTextTriggerValue("mentre lavoro")
      ),
      List(
        GifFile("rphjb_NonVoglioNessunoGif.mp4"),
        MediaFile("rphjb_NonVoglioNessuno.mp4")
      ),
      replySelection = RandomSelection
    ),
    ReplyBundleMessage(
      TextTrigger(
        StringTextTriggerValue("peggio del peggio")
      ),
      List(
        GifFile("rphjb_PeggioDelPeggioGif.mp4"),
        MediaFile("rphjb_PeggioDelPeggio.mp4")
      ),
      replySelection = RandomSelection
    ),
    ReplyBundleMessage(
      TextTrigger(
        StringTextTriggerValue("bebop"),
        StringTextTriggerValue("be bop"),
        StringTextTriggerValue("aluba"),
        StringTextTriggerValue("my baby")
      ),
      List(
        GifFile("rphjb_BebopGif.mp4"),
        GifFile("rphjb_Bebop.mp4")
      ),
      replySelection = RandomSelection
    ),
    ReplyBundleMessage(
      TextTrigger(
        RegexTextTriggerValue("(18|diciott['o]?) anni".r, 7)
      ),
      List(
        MediaFile("rphjb_DiciottoAnni.mp4"),
        GifFile("rphjb_DiciottoAnniGif.mp4")
      ),
      replySelection = RandomSelection
    ),
    ReplyBundleMessage(
      TextTrigger(
        RegexTextTriggerValue("(cinque|5) dita".r, 6),
        StringTextTriggerValue("pugno")
      ),
      List(
        MediaFile("rphjb_CinqueDita.mp4"),
        MediaFile("rphjb_CinqueDita.mp3")
      ),
      replySelection = RandomSelection
    ),
    ReplyBundleMessage(
      TextTrigger(
        RegexTextTriggerValue("\\bratti\\b".r, 5),
        StringTextTriggerValue("topi")
      ),
      List(
        MediaFile("rphjb_DubbioScantinatiGiocoRattoGatto.mp4"),
        MediaFile("rphjb_ListaMaleCollo.mp3")
      ),
      replySelection = RandomSelection
    ),
    ReplyBundleMessage(
      TextTrigger(
        StringTextTriggerValue("poveri cretini"),
        StringTextTriggerValue("poveri ignoranti")
      ),
      List(
        MediaFile("rphjb_PoveriCretini.mp3"),
        MediaFile("rphjb_PoveriIgnorantiLatteDrogaSoloGreggeSputo.mp4")
      ),
      replySelection = RandomSelection
    ),
    ReplyBundleMessage(
      TextTrigger(
        StringTextTriggerValue("solo uno parló"),
        RegexTextTriggerValue("(c|g)ri(d|t)i(g|c)a(d|t)o".r, 9)
      ),
      List(
        GifFile("rphjb_FuCriticatoGif.mp4"),
        MediaFile("rphjb_FuCriticato.mp4"),
      ),
      replySelection = RandomSelection
    ),
    ReplyBundleMessage(
      TextTrigger(
        StringTextTriggerValue("venerdì")
      ),
      List(
        MediaFile("rphjb_Venerdi.mp3"),
        MediaFile("rphjb_Venerdi.mp4"),
        MediaFile("rphjb_TempoAlTempo.mp4")
      ),
      replySelection = RandomSelection
    ),
    ReplyBundleMessage(
      TextTrigger(
        StringTextTriggerValue("discoteca laziale")
      ),
      List(
        GifFile("rphjb_DiscotecaLaziale.mp4"),
        MediaFile("rphjb_DiscotecaLaziale.mp3"),
        MediaFile("rphjb_SoldiButtatiDiscotecaLaziale.mp3"),
      ),
      replySelection = RandomSelection
    ),
    ReplyBundleMessage(
      TextTrigger(
        StringTextTriggerValue("calcolo"),
        StringTextTriggerValue("matematica")
      ),
      List(
        GifFile("rphjb_MiPareLogico.mp4"),
        MediaFile("rphjb_MiPareLogico.mp3"),
        MediaFile("rphjb_MatematiciAnaliticiDiNoia.mp3"),
      ),
      replySelection = RandomSelection
    ),
    ReplyBundleMessage(
      TextTrigger(
        RegexTextTriggerValue("\\blo[g]+i(c|g)o\\b".r, 6)
      ),
      List(
        GifFile("rphjb_MiPareLogico.mp4"),
        MediaFile("rphjb_MiPareLogico.mp3"),
        GifFile("rphjb_SembraLogico.mp4")
      ),
      replySelection = RandomSelection
    ),
    ReplyBundleMessage(
      TextTrigger(
        StringTextTriggerValue("ti dovresti vergognare")
      ),
      List(
        GifFile("rphjb_TiDovrestiVergognare.mp4"),
        MediaFile("rphjb_TiDovrestiVergognare.mp3")
      ),
      replySelection = RandomSelection
    ),
    ReplyBundleMessage(
      TextTrigger(
        RegexTextTriggerValue("(non|mica) so(no)? (un |n )?co(gl|j)ione".r, 13),
        RegexTextTriggerValue("sarete co(gl|j)ioni voi".r, 17)
      ),
      List(
        GifFile("rphjb_SareteCoglioniVoi.mp4"),
        MediaFile("rphjb_SareteCoglioniVoi.mp3")
      ),
      replySelection = RandomSelection
    ),
    ReplyBundleMessage(
      TextTrigger(
        StringTextTriggerValue("non li sopporto"),
        RegexTextTriggerValue("che si deve f(à|are)".r, 14),
        StringTextTriggerValue("bisogna pure lavorà")
      ),
      List(
        GifFile("rphjb_NonLiSopporto.mp4"),
        MediaFile("rphjb_NonLiSopporto.mp3")
      ),
      replySelection = RandomSelection
    ),
    ReplyBundleMessage(
      TextTrigger(
        StringTextTriggerValue("simposio")
      ),
      List(
        MediaFile("rphjb_PellegrinaggioSimposioMetallo.mp4"),
        MediaFile("rphjb_InnoSimposio.mp3"),
      ),
      replySelection = RandomSelection
    ),
    ReplyBundleMessage(
      TextTrigger(
        StringTextTriggerValue("giuda"),
        StringTextTriggerValue("chi è cristo"),
        StringTextTriggerValue("si è fatto fregare"),
        StringTextTriggerValue("bacio di un frocio"),
      ),
      List(
        MediaFile("rphjb_ChiECristo.mp3"),
        MediaFile("rphjb_GiudaFrocio.mp4")
      ),
      replySelection = RandomSelection
    ),
    ReplyBundleMessage(
      TextTrigger(
        StringTextTriggerValue("danza macabra"),
      ),
      List(
        GifFile("rphjb_DanzaMacabraGif.mp4"),
        MediaFile("rphjb_DanzaMacabra.mp4"),
        MediaFile("rphjb_DanzaMacabra.mp3"),
      ),
      replySelection = RandomSelection
    ),
    ReplyBundleMessage(
      TextTrigger(
        StringTextTriggerValue("steve vai"),
      ),
      List(
        MediaFile("rphjb_SteveVaiRiciclando.mp4"),
        MediaFile("rphjb_AmoreSuonareFreddoBuddistiSchifoso.mp4"),
        MediaFile("rphjb_DueOssa.mp3"),
        GifFile("rphjb_Note.mp4"),
        MediaFile("rphjb_Paradosso.mp4"),
        MediaFile("rphjb_RelIllusions.mp4"),
        GifFile("rphjb_TiDeviSpaventareGif.mp4"),
        MediaFile("rphjb_TiDeviSpaventare.mp3"),
        MediaFile("rphjb_Feelings.mp4")
      ),
      replySelection = RandomSelection
    ),
    ReplyBundleMessage(
      TextTrigger(
        RegexTextTriggerValue("(divento|diventare|sono) (matto|pazzo)".r, 10)
      ),
      List(
        GifFile("rphjb_StoDiventandoPazzo.mp4"),
        MediaFile("rphjb_CompletamentePazzo.mp4"),
        MediaFile("rphjb_CompletamentePazzo2.mp4"),
      ),
      replySelection = RandomSelection
    ),
    ReplyBundleMessage(
      TextTrigger(
        RegexTextTriggerValue("vo[l]+[o]*[u]+[ou]*me".r, 6)
      ),
      List(
        MediaFile("rphjb_MenoVolume.mp3"),
        MediaFile("rphjb_VolumeTelevisori.mp4")
      ),
      replySelection = RandomSelection
    ),
    ReplyBundleMessage(
      TextTrigger(
        StringTextTriggerValue("generi musicali"),
        RegexTextTriggerValue("solo il me(t|d)al".r, 13)
      ),
      List(
        GifFile("rphjb_GeneriMusicali.mp4"),
        MediaFile("rphjb_GeneriMusicali2.mp4"),
      ),
      replySelection = RandomSelection
    ),
    ReplyBundleMessage(
      TextTrigger(
        StringTextTriggerValue("sorca"),
        StringTextTriggerValue("patonza"),
        StringTextTriggerValue("lecciso"),
        RegexTextTriggerValue("\\bfi[cg]a\\b".r, 4)
      ),
      List(
        GifFile("rphjb_SorcaLecciso.mp4"),
        MediaFile("rphjb_SorcaLecciso2.mp4"),
        MediaFile("rphjb_FigaLarga.mp4"),
        MediaFile("rphjb_FragolinaFichina.mp3"),
        MediaFile("rphjb_Sorca.mp4")
      ),
      replySelection = RandomSelection
    ),
    ReplyBundleMessage(
      TextTrigger(
        StringTextTriggerValue("schifose"),
        StringTextTriggerValue("ultime")
      ),
      List(
        GifFile("rphjb_SchifoseUltime.mp4"),
        MediaFile("rphjb_SchifoseUltime.mp4"),
        MediaFile("rphjb_ImparaASputareMignottaSchifose.mp4")
      ),
      replySelection = RandomSelection
    ),
    ReplyBundleMessage(
      TextTrigger(
        RegexTextTriggerValue("e parl[a]+\\b".r, 7)
      ),
      List(
        GifFile("rphjb_Parla.mp4"),
        MediaFile("rphjb_Parla2.mp4")
      ),
      replySelection = RandomSelection
    ),
    ReplyBundleMessage(
      TextTrigger(
        StringTextTriggerValue("cosa è successo"),
        RegexTextTriggerValue("\\bcosa[?]{1,}\\b".r, 5)
      ),
      List(
        GifFile("rphjb_CosaSuccesso.mp4"),
        MediaFile("rphjb_Cosa.mp4")
      ),
      replySelection = RandomSelection
    ),
    ReplyBundleMessage(
      TextTrigger(
        StringTextTriggerValue("negozio"),
        StringTextTriggerValue("pantaloni"),
        StringTextTriggerValue("shopping")
      ),
      List(
        MediaFile("rphjb_Pantaloni.mp3"),
        MediaFile("rphjb_Pantaloni.mp4"),
      ),
      replySelection = RandomSelection
    ),
    ReplyBundleMessage(
      TextTrigger(
        StringTextTriggerValue("sono finito"),
        StringTextTriggerValue("ultimo stadio"),
        StringTextTriggerValue("stanco")
      ),
      List(
        MediaFile("rphjb_Stanco.mp3"),
        MediaFile("rphjb_Stanco.mp4")
      ),
      replySelection = RandomSelection
    ),
    ReplyBundleMessage(
      TextTrigger(
        StringTextTriggerValue("ratzinger"),
        RegexTextTriggerValue("(il|er) vaticano".r, 11)
      ),
      List(
        MediaFile("rphjb_AndateDaRatzinger.mp4"),
        GifFile("rphjb_AndateDaRatzinger2.mp4"),
        MediaFile("rphjb_AndateDaRatzinger.mp3"),
      ),
      replySelection = RandomSelection
    ),
    ReplyBundleMessage(
      TextTrigger(
        StringTextTriggerValue("mare di cazzate"),
        StringTextTriggerValue("non è possibile")
      ),
      List(
        GifFile("rphjb_NonPossibile.mp4"),
        MediaFile("rphjb_NonPossibile2.mp4")
      ),
      replySelection = RandomSelection
    ),
    ReplyBundleMessage(
      TextTrigger(
        StringTextTriggerValue("cameriera"),
        StringTextTriggerValue("moglie"),
        StringTextTriggerValue("si sposa"),
        StringTextTriggerValue("matrimonio")
      ),
      List(
        MediaFile("rphjb_Cameriera.mp3"),
        MediaFile("rphjb_Cameriera.mp4"),
      ),
      replySelection = RandomSelection
    ),
    ReplyBundleMessage(
      TextTrigger(
        RegexTextTriggerValue("cos(a)? hai trovato?".r, 16)
      ),
      List(
        GifFile("rphjb_CosHaiTrovato.mp4"),
        MediaFile("rphjb_NonPossibile2.mp4")
      ),
      replySelection = RandomSelection
    ),
    ReplyBundleMessage(
      TextTrigger(
        RegexTextTriggerValue("accetto (le|qualsiasi) critich[ea]".r, 17),
      ),
      List(
        GifFile("rphjb_Escerto.mp4"),
        MediaFile("rphjb_CriticaNoCazzate.mp4")
      ),
      replySelection = RandomSelection
    ),
    ReplyBundleMessage(
      TextTrigger(
        StringTextTriggerValue("pronto dimmi")
      ),
      List(
        GifFile("rphjb_ProntoDimmi.mp4"),
        GifFile("rphjb_ProntoDimmi2.mp4"),
      ),
      replySelection = RandomSelection
    ),
    ReplyBundleMessage(
      TextTrigger(
        StringTextTriggerValue("bassista"),
        StringTextTriggerValue("slap")
      ),
      List(
        GifFile("rphjb_Bassista.gif"),
        GifFile("rphjb_Basso.mp4"),
      ),
      replySelection = RandomSelection
    ),
    ReplyBundleMessage(
      TextTrigger(
        RegexTextTriggerValue("è vero[!?]+".r, 6)
      ),
      List(
        GifFile("rphjb_Vero.mp4"),
        MediaFile("rphjb_EraVero.mp4"),
        MediaFile("rphjb_SuonatoAbbastanzaBeneEVero.mp3")
      ),
      replySelection = RandomSelection
    ),
    ReplyBundleMessage(
      TextTrigger(
        RegexTextTriggerValue("perchè (cazzo)? mi hai fatto ven[i|ì](re)?".r, 24)
      ),
      List(
        MediaFile("rphjb_PercheCazzoMiHaiFattoVeni.mp3"),
        MediaFile("rphjb_PercheCazzoMiHaiFattoVeni.mp4"),
      ),
      replySelection = RandomSelection
    ),
    ReplyBundleMessage(
      TextTrigger(
        RegexTextTriggerValue("e(s|c)certo".r, 6),
        StringTextTriggerValue("non me ne frega un cazzo")
      ),
      List(
        GifFile("rphjb_Escerto.mp4"),
        MediaFile("rphjb_EscertoCritiche.mp4")
      ),
      replySelection = RandomSelection
    ),
    ReplyBundleMessage(
      TextTrigger(
        StringTextTriggerValue("decido io"),
      ),
      List(
        GifFile("rphjb_DecidoIo.mp4"),
        MediaFile("rphjb_DecidoIoMareCazzatePerCortesia.mp4")
      ),
      replySelection = RandomSelection
    ),
    ReplyBundleMessage(
      TextTrigger(
        StringTextTriggerValue("mi piaccio"),
        StringTextTriggerValue("impazzire"),
      ),
      List(
        GifFile("rphjb_MiPiaccio.mp4"),
        MediaFile("rphjb_MiPiaccio2.mp4"),
      ),
      replySelection = RandomSelection
    ),
    ReplyBundleMessage(
      TextTrigger(
        StringTextTriggerValue("giudica")
      ),
      List(
        GifFile("rphjb_Giudicate.mp4"),
        GifFile("rphjb_ComeFaiAGiudicare.mp4"),
        MediaFile("rphjb_NonPoteteGiudicarUrloThatsGood.mp4")
      ),
      replySelection = RandomSelection
    ),
    ReplyBundleMessage(
      TextTrigger(
        RegexTextTriggerValue("fregare come un co(gl|j)ione".r, 22),
        RegexTextTriggerValue("ges[uùù]".r, 4)
      ),
      List(
        MediaFile("rphjb_GesuCoglione.mp4"),
        MediaFile("rphjb_GesuCoglione.mp3")
      ),
      replySelection = RandomSelection
    ),
    ReplyBundleMessage(
      TextTrigger(
        StringTextTriggerValue("non sono uno del branco"),
        StringTextTriggerValue("agende"),
        StringTextTriggerValue("figli dei figli"),
        StringTextTriggerValue("quali fiori"),
        StringTextTriggerValue("diluite le vostre droghe"),
        StringTextTriggerValue("non sono uno da sangue"),
        StringTextTriggerValue("aghi di culto"),
        RegexTextTriggerValue("bucati[,]? ma da quale chiodo".r, 25),
      ),
      List(
        MediaFile("rphjb_GerarchieInfernali.mp4"),
        MediaFile("rphjb_GerarchieInfernali.mp3")
      ),
      replySelection = RandomSelection
    ),
    ReplyBundleMessage(
      TextTrigger(
        StringTextTriggerValue("con questa tecnica")
      ),
      List(
        MediaFile("rphjb_ConQuestaTecnica.mp4"),
        MediaFile("rphjb_ConQuestaTecnica.mp3")
      ),
      replySelection = RandomSelection
    ),
    ReplyBundleMessage(
      TextTrigger(
        StringTextTriggerValue("platinette"),
        StringTextTriggerValue("due persone in una"),
        StringTextTriggerValue("quando scopo me la levo"),
        StringTextTriggerValue("mi levo tutto"),
      ),
      List(
        MediaFile("rphjb_Platinette.mp4"),
        MediaFile("rphjb_Platinette.mp3")
      ),
      replySelection = RandomSelection
    ),
    ReplyBundleMessage(
      TextTrigger(
        StringTextTriggerValue("il mio sbadiglio"),
        StringTextTriggerValue("donna solo per un taglio"),
        StringTextTriggerValue("labbro superiore")
      ),
      List(
        MediaFile("rphjb_DonnaTaglioSbadiglio.mp4"),
        MediaFile("rphjb_DonnaTaglioSbadiglio.mp3")
      ),
      replySelection = RandomSelection
    ),
    ReplyBundleMessage(
      TextTrigger(
        RegexTextTriggerValue("\\bvino\\b".r, 4),
      ),
      List(
        MediaFile("rphjb_ChitarraZuccheroAlgheVino.mp3"),
        MediaFile("rphjb_Rimpinzati.mp4"),
        MediaFile("rphjb_Pasqua.mp4")
      ),
      replySelection = RandomSelection
    ),
    ReplyBundleMessage(
      TextTrigger(
        RegexTextTriggerValue("chi tocca (\\w)[,]? muore".r, 16),
        RegexTextTriggerValue("ciao (2001|duemilauno)".r, 9)
      ),
      List(
        MediaFile("rphjb_Ciao2001.mp4"),
        GifFile("rphjb_Ciao2001Gif.mp4"),
      ),
      replySelection = RandomSelection
    ),
  )

}
