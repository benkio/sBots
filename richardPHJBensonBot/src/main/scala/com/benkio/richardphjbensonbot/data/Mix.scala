package com.benkio.richardphjbensonbot.data

import cats._
import com.benkio.telegrambotinfrastructure.messagefiltering._
import com.benkio.telegrambotinfrastructure.model._
import com.lightbend.emoji.ShortCodes.Defaults._
import com.lightbend.emoji.ShortCodes.Implicits._

object Mix {

    def messageRepliesMixData[F[_]: Applicative]: List[ReplyBundleMessage[F]] = List(
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
        MediaFile("rphjb_DrogatiPiloti.gif"),
        MediaFile("rphjb_DrogatiPiloti.mp4")
      ),
      replySelection = RandomSelection
    ),
    ReplyBundleMessage(
      TextTrigger(
        RegexTextTriggerValue("\\brock\\b".r)
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
        StringTextTriggerValue("albero grande"),
        RegexTextTriggerValue("anche un('| )amplificatore".r),
      ),
      List(
        MediaFile("rphjb_PoesiaRock.mp4"),
      )
    ),
    ReplyBundleMessage(
      TextTrigger(
        StringTextTriggerValue("non sono uno del branco"),
        StringTextTriggerValue("agende"),
        StringTextTriggerValue("figli dei figli"),
        StringTextTriggerValue("quali fiori"),
        StringTextTriggerValue("diluite le vostre droghe")
      ),
      List(
        MediaFile("rphjb_GerarchieInfernali.mp4"),
      )
    ),
    ReplyBundleMessage(
      TextTrigger(
        StringTextTriggerValue("ti distruggo")
      ),
      List(
        MediaFile("rphjb_TiDistruggo.gif"),
        MediaFile("rphjb_Ramarro.mp4")
      ),
      replySelection = RandomSelection
    ),
    ReplyBundleMessage(
      TextTrigger(
        StringTextTriggerValue("cristo pinocchio"),
        StringTextTriggerValue("lumicino"),
        RegexTextTriggerValue("(strade|vie) inferiori".r)
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
        RegexTextTriggerValue("\\bstronza\\b".r)
      ),
      List(
        MediaFile("rphjb_LasciamiInPace.gif"),
        MediaFile("rphjb_LasciamiInPaceStronza.mp4")
      ),
      replySelection = RandomSelection
    ),
    ReplyBundleMessage(
      TextTrigger(
        StringTextTriggerValue("rimpinzati"),
        RegexTextTriggerValue("[gc]io[gc]+ola[dt]a".r),
        StringTextTriggerValue("pandori"),
        StringTextTriggerValue("ciambelloni"),
        StringTextTriggerValue("gli amari"),
        RegexTextTriggerValue("limoncell[oi]".r),
        StringTextTriggerValue("il vino"),
        StringTextTriggerValue("ingrassati"),
        StringTextTriggerValue("andati al cesso"),
      ),
      List(
        MediaFile("rphjb_Rimpinzati.gif"),
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
        MediaFile("rphjb_MiFaStareMale.gif"),
        MediaFile("rphjb_MelensaStareMale.mp4")
      ),
      replySelection = RandomSelection
    ),
    ReplyBundleMessage(
      TextTrigger(
        RegexTextTriggerValue("[a]+[t]{2,}[e]+[n]+[z]+[i]+[o]+[n]+[e]+[!]*[!e]$".r)
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
        MediaFile("rphjb_Paradosso.gif"),
        MediaFile("rphjb_Paradosso.mp4")
      ),
      replySelection = RandomSelection
    ),
    ReplyBundleMessage(
      TextTrigger(
        RegexTextTriggerValue("\\bsput[ao]\\b".r)
      ),
      List(
        MediaFile("rphjb_Sputo.gif"),
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
        StringTextTriggerValue(" nani"),
        StringTextTriggerValue("nani "),
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
        MediaFile("rphjb_ChiamaLaPolizia.gif"),
        MediaFile("rphjb_ChiamaLaPolizia.mp4")
      ),
      replySelection = RandomSelection
    ),
    ReplyBundleMessage(
      TextTrigger(
        RegexTextTriggerValue("stori(a|e)".r)
      ),
      List(
        MediaFile("rphjb_Storie.mp3"),
        MediaFile("rphjb_Storie2.mp3"),
        MediaFile("rphjb_StorieSonoTanteVecchiaccia.mp4")
      ),
      replySelection = RandomSelection
    ),
    ReplyBundleMessage(
      TextTrigger(
        RegexTextTriggerValue("au[ ]?de".r),
        RegexTextTriggerValue("\\btime\\b".r),
        RegexTextTriggerValue("uir[ ]?bi[ ]?taim".r)
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
        MediaFile("rphjb_Micetta.gif"),
        MediaFile("rphjb_LaMorteMicetta.mp4"),
        MediaFile("rphjb_LaMorte.mp4"),
        MediaFile("rphjb_InnoAllaMorte.mp4")
      ),
      replySelection = RandomSelection
    ),
    ReplyBundleMessage(
      TextTrigger(
        StringTextTriggerValue("spalle"),
        StringTextTriggerValue("braccia"),
        RegexTextTriggerValue("t[ie] strozzo".r)
      ),
      List(
        MediaFile("rphjb_FaccioVedereSpalleBraccia.gif"),
        MediaFile("rphjb_FaccioVedereSpalleBraccia.mp4"),
        MediaFile("rphjb_UccidereUnaPersona.mp4"),
      ),
      replySelection = RandomSelection
    ),
    ReplyBundleMessage(
      TextTrigger(
        StringTextTriggerValue("non sapere"),
        RegexTextTriggerValue("aris[dt]o[dt]ele".r)
      ),
      List(
        MediaFile("rphjb_SoDiNonSapere.gif"),
        MediaFile("rphjb_SoDiNonSapere.mp4")
      ),
      replySelection = RandomSelection
    ),
    ReplyBundleMessage(
      TextTrigger(
        StringTextTriggerValue("non è roba per me")
      ),
      List(
        MediaFile("rphjb_RobaPerMe.gif"),
        MediaFile("rphjb_RobaPerMe.mp4")
      ),
      replySelection = RandomSelection
    ),
    ReplyBundleMessage(
      TextTrigger(
        RegexTextTriggerValue("io nooo[o]*".r)
      ),
      List(
        MediaFile("rphjb_IoNo.mp3"),
        MediaFile("rphjb_GesuCoglione.mp4")
      ),
      replySelection = RandomSelection
    ),
    ReplyBundleMessage(
      TextTrigger(
        StringTextTriggerValue("bastone infernale"),
        StringTextTriggerValue("un'arma")
      ),
      List(
        MediaFile("rphjb_Bastone1.gif"),
        MediaFile("rphjb_Bastone2.gif"),
        MediaFile("rphjb_Bastone3.gif"),
        MediaFile("rphjb_BastoneInfernale.mp4")
      ),
      replySelection = RandomSelection
    ),
    ReplyBundleMessage(
      TextTrigger(
        StringTextTriggerValue("vi calpesto"),
        StringTextTriggerValue("vermi"),
        StringTextTriggerValue("strisciate")
      ),
      List(
        MediaFile("rphjb_ViCalpesto.gif"),
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
        MediaFile("rphjb_AndareAvanti.gif"),
        MediaFile("rphjb_AndareAvanti.mp3"),
        MediaFile("rphjb_InnovazioneStiamoTornandoIndietro.mp4")
      ),
      replySelection = RandomSelection
    ),
    ReplyBundleMessage(
      TextTrigger(
        StringTextTriggerValue("non ci credete?"),
        RegexTextTriggerValue("grande s[dt]ronza[dt][ea]".r)
      ),
      List(
        MediaFile("rphjb_NonCiCredete.gif"),
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
        MediaFile("rphjb_NonMeNeFotte.gif"),
        MediaFile("rphjb_NonMeNeFrega.gif"),
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
        MediaFile("rphjb_ViCalpesto.gif"),
        MediaFile("rphjb_ViCalpesto.mp4"),
        MediaFile("rphjb_Ultimi.gif"),
        MediaFile("rphjb_StateZittiZozziUltimi.mp3"),
      ),
      replySelection = RandomSelection
    ),
    ReplyBundleMessage(
      TextTrigger(
        RegexTextTriggerValue("che (cazzo )?era quella roba".r),
        RegexTextTriggerValue("che (cazzo |cazzo di roba )?mi avete dato".r),
        StringTextTriggerValue("lampi negli occhi"),
        RegexTextTriggerValue("gira(re|ra|rà|ndo)? la testa".r),
        RegexTextTriggerValue("insieme alla (c|g)o(c|g)a (c|g)ola".r)
      ),
      List(
        MediaFile("rphjb_CheCazzoEraQuellaRoba.mp3"),
        MediaFile("rphjb_CheCazzoEraQuellaRoba.mp4"),
        MediaFile("rphjb_CheCazzoEraQuellaRoba2.mp4"),
        MediaFile("rphjb_CheCazzoEraQuellaRoba3.mp4"),
        MediaFile("rphjb_RockMachineIntro.mp4"),
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
        MediaFile("rphjb_Pollo.gif")
      ),
      replySelection = RandomSelection
    ),
    ReplyBundleMessage(
      TextTrigger(
        StringTextTriggerValue("quello che dico io")
      ),
      List(
        MediaFile("rphjb_QuelloCheDicoIo.gif"),
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
        MediaFile("rphjb_Zucchero.mp3"),
        MediaFile("rphjb_ZuccheroVanigliaCremaCioccolataPandoro.mp3"),
        MediaFile("rphjb_AuguriPerPasqua.mp4")
      ),
      replySelection = RandomSelection
    ),
    ReplyBundleMessage(
      TextTrigger(
        RegexTextTriggerValue("\\bgood\\b".r),
        RegexTextTriggerValue("\\bshow\\b".r),
        RegexTextTriggerValue("\\bfriends\\b".r)
      ),
      List(
        MediaFile("rphjb_OkGoodShowFriends.gif"),
        MediaFile("rphjb_LetSGoodStateBene.mp3"),
        MediaFile("rphjb_WelaMyFriends.mp4"),
        MediaFile("rphjb_LetsGoodMyFriends.mp4")
      ),
      replySelection = RandomSelection
    ),
    ReplyBundleMessage(
      TextTrigger(
        RegexTextTriggerValue("vattene (af)?fanculo".r)
      ),
      List(
        MediaFile("rphjb_MaVatteneAffanculo.gif"),
        MediaFile("rphjb_MaVatteneAffanculo.mp3")
      ),
      replySelection = RandomSelection
    ),
    ReplyBundleMessage(
      TextTrigger(
        StringTextTriggerValue("feelings")
      ),
      List(
        MediaFile("rphjb_Feelings.gif"),
        MediaFile("rphjb_Feelings2.gif"),
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
        MediaFile("rphjb_MiRompiErCazzo.gif"),
        MediaFile("rphjb_MeNeVado.gif")
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
        MediaFile("rphjb_Mignotta.gif"),
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
        MediaFile("rphjb_TiDeviSpaventare.gif"),
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
        MediaFile("rphjb_MaCheCazzoStoDicendo.gif")
      ),
      replySelection = RandomSelection
    ),
    ReplyBundleMessage(
      TextTrigger(
        StringTextTriggerValue("questa volta no")
      ),
      List(
        MediaFile("rphjb_QuestaVoltaNo.mp3"),
        MediaFile("rphjb_QuestaVoltaNo.gif")
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
        MediaFile("rphjb_Vergogna.gif"),
        MediaFile("rphjb_Vergogna2.gif")
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
        MediaFile("rphjb_Trasformista.gif"),
        MediaFile("rphjb_CristoCanaro.mp4")
      ),
      replySelection = RandomSelection
    ),
    ReplyBundleMessage(
      TextTrigger(
        RegexTextTriggerValue("ma[ ]?s[cg]us[a]?".r)
      ),
      List(
        MediaFile("rphjb_MaSgus.mp3"),
        MediaFile("rphjb_MaSgus.gif")
      ),
      replySelection = RandomSelection
    ),
    ReplyBundleMessage(
      TextTrigger(
        StringTextTriggerValue("grazie gianni"),
      ),
      List(
        MediaFile("rphjb_Grazie.mp3"),
        MediaFile("rphjb_Grazie.gif"),
        MediaFile("rphjb_Grazie.mp4")
      ),
      replySelection = RandomSelection
    ),
    ReplyBundleMessage(
      TextTrigger(
        RegexTextTriggerValue("cia[o]{3,}".r)
      ),
      List(
        MediaFile("rphjb_Grazie.mp3"),
        MediaFile("rphjb_Grazie.gif"),
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
        MediaFile("rphjb_IncontratePerStrada.gif")
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
        MediaFile("rphjb_LavoraTu.gif"),
        MediaFile("rphjb_StorieSonoTanteVecchiaccia.mp4")
      ),
      replySelection = RandomSelection
    ),
    ReplyBundleMessage(
      TextTrigger(
        RegexTextTriggerValue("infern[a]+l[ie]+[!]*".r)
      ),
      List(
        MediaFile("rphjb_Infernali.mp3"),
        MediaFile("rphjb_Infernali.gif"),
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
        MediaFile("rphjb_PigliandoPerIlCulo.gif")
      ),
      replySelection = RandomSelection
    ),
    ReplyBundleMessage(
      TextTrigger(
        StringTextTriggerValue(e":lol:"),
        StringTextTriggerValue(e":rofl:"),
        StringTextTriggerValue("sorriso"),
        RegexTextTriggerValue("(ah|ha){3,}".r)
      ),
      List(
        MediaFile("rphjb_Risata.mp3"),
        MediaFile("rphjb_Risata.mp4"),
        MediaFile("rphjb_Risata.gif"),
        MediaFile("rphjb_OrmaiRisata.mp4"),
        MediaFile("rphjb_Sorriso2.gif"),
        MediaFile("rphjb_Sorriso.gif"),
        MediaFile("rphjb_SorrisoSognante.gif"),
        MediaFile("rphjb_Risata2.mp3"),
        MediaFile("rphjb_SepolturaRisata.mp4")
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
        MediaFile("rphjb_Frocio.gif")
      ),
      replySelection = RandomSelection
    ),
    ReplyBundleMessage(
      TextTrigger(
        RegexTextTriggerValue("(fammi|chiedere)? (una|questa)? cortesia".r)
      ),
      List(
        MediaFile("rphjb_FammiQuestaCortesia.mp3"),
        MediaFile("rphjb_FammiQuestaCortesia.gif")
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
        MediaFile("rphjb_NonMiStaBene.gif"),
        MediaFile("rphjb_NonMiStaBene2.gif"),
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
        MediaFile("rphjb_Labbra.gif")
      ),
      replySelection = RandomSelection
    ),
    ReplyBundleMessage(
      TextTrigger(
        StringTextTriggerValue("la vita è il nemico")
      ),
      List(
        MediaFile("rphjb_VitaNemico.mp3"),
        MediaFile("rphjb_VitaNemico.gif"),
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
        MediaFile("rphjb_Permettere.gif")
      ),
      replySelection = RandomSelection
    ),
    ReplyBundleMessage(
      TextTrigger(
        StringTextTriggerValue("le note")
      ),
      List(
        MediaFile("rphjb_Note.mp3"),
        MediaFile("rphjb_Note.gif")
      ),
      replySelection = RandomSelection
    ),
    ReplyBundleMessage(
      TextTrigger(
        RegexTextTriggerValue("te[r]+[i]+[b]+[i]+l[e]+".r)
      ),
      List(
        MediaFile("rphjb_Terribile.mp3"),
        MediaFile("rphjb_Terribile.mp4"),
        MediaFile("rphjb_Terribile.gif")
      ),
      replySelection = RandomSelection
    ),
    ReplyBundleMessage(
      TextTrigger(
        StringTextTriggerValue("viva napoli")
      ),
      List(
        MediaFile("rphjb_VivaNapoli.mp3"),
        MediaFile("rphjb_VivaNapoli.gif")
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
        MediaFile("rphjb_CiaoComeState.gif"),
        MediaFile("rphjb_CiaoComeState.mp3")
      ),
      replySelection = RandomSelection
    ),
    ReplyBundleMessage(
      TextTrigger(
        RegexTextTriggerValue("bast[a]{3,}[!]*".r)
      ),
      List(
        MediaFile("rphjb_Basta.mp3"),
        MediaFile("rphjb_Basta.mp4"),
        MediaFile("rphjb_Basta2.mp4"),
        MediaFile("rphjb_Basta2.mp3"),
        MediaFile("rphjb_Basta.gif"),
        MediaFile("rphjb_Basta2.gif"),
        MediaFile("rphjb_Basta3.gif"),
        MediaFile("rphjb_BastaSedia.gif")
      ),
      replySelection = RandomSelection
    ),
    ReplyBundleMessage(
      TextTrigger(
        StringTextTriggerValue("assolo"),
        RegexTextTriggerValue("[gc]hi[td]arra".r),
        RegexTextTriggerValue("(as)?solo di basso".r)
      ),
      List(
        MediaFile("rphjb_Assolo.mp3"),
        MediaFile("rphjb_Assolo.mp4"),
        MediaFile("rphjb_Assolo2.mp4"),
        MediaFile("rphjb_AssoloBeat.mp4"),
        MediaFile("rphjb_AssoloSubdolo.mp4"),
        MediaFile("rphjb_Chitarra1.gif"),
        MediaFile("rphjb_Chitarra2.gif"),
        MediaFile("rphjb_ChitarraPlettroVicoletto.mp4"),
        MediaFile("rphjb_ChitarraZuggherada.mp3"),
        MediaFile("rphjb_AssoloBasso.mp4")
      ),
      replySelection = RandomSelection
    ),
    ReplyBundleMessage(
      TextTrigger(
        RegexTextTriggerValue("[ ]?ca[bp]i[dt]o | ca[bp]i[dt]o[ ]?".r),
        RegexTextTriggerValue("[ ]?capissi | capissi[ ]?".r),
        StringTextTriggerValue("gabido"),
      ),
      List(
        MediaFile("rphjb_HoCapito.mp3"),
        MediaFile("rphjb_AveteCapito.mp3"),
        MediaFile("rphjb_AveteCapito.mp4"),
        MediaFile("rphjb_Capito.mp3"),
        MediaFile("rphjb_NonHannoCapitoUnCazzo.mp3"),
        MediaFile("rphjb_NonAveteCapitoUnCazzo.mp4"),
        MediaFile("rphjb_AveteCapitoComeSempre.gif"),
        MediaFile("rphjb_NonAveteCapitoUnCazzo.gif"),
        MediaFile("rphjb_VoiNonAveteCapitoUnCazzo.gif"),
        MediaFile("rphjb_IlSensoCapito.gif"),
        MediaFile("rphjb_CapitoDoveStiamo.gif"),
        MediaFile("rphjb_NonHoCapito.gif"),
        MediaFile("rphjb_AveteCapitoEh.gif"),
        MediaFile("rphjb_ComeAlSolitoNonAveteCapito.gif"),
        MediaFile("rphjb_CapitoDoveStiamo.mp3"),
        MediaFile("rphjb_CapisciRidotti.mp3")
      ),
      replySelection = RandomSelection
    ),
    ReplyBundleMessage(
      TextTrigger(
        StringTextTriggerValue("esperiment"),
        RegexTextTriggerValue("1(,)? 2(,)? 3".r),
        RegexTextTriggerValue("uno(,)? due(,)? tre".r)
      ),
      List(
        MediaFile("rphjb_Esperimento.mp3"),
        MediaFile("rphjb_Esperimento.mp4"),
        MediaFile("rphjb_Esperimento2.mp4"),
        MediaFile("rphjb_Esperimento.gif"),
        MediaFile("rphjb_Esperimento2.gif"),
        MediaFile("rphjb_Esperimento3.gif"),
        MediaFile("rphjb_DiciottoAnni.mp4"),
      ),
      replySelection = RandomSelection
    ),
    ReplyBundleMessage(
      TextTrigger(
        StringTextTriggerValue("schifosi")
      ),
      List(
        MediaFile("rphjb_ViCalpesto.gif"),
        MediaFile("rphjb_ViCalpesto.mp4"),
        MediaFile("rphjb_Schifosi.mp3"),
        MediaFile("rphjb_Schifosi.mp4"),
        MediaFile("rphjb_Schifosi2.mp3"),
        MediaFile("rphjb_Schifosi3.mp3"),
        MediaFile("rphjb_Schifosi3.gif"),
        MediaFile("rphjb_SchifosoUltimi.mp4"),
        MediaFile("rphjb_StateZittiZozziUltimi.mp3"),
        MediaFile("rphjb_Schifosi.gif"),
        MediaFile("rphjb_Schifosi2.gif"),
        MediaFile("rphjb_ConQuestaTecnica.mp4")
      ),
      replySelection = RandomSelection
    ),
    ReplyBundleMessage(
      TextTrigger(
        StringTextTriggerValue("mortacci vostri")
      ),
      List(
        MediaFile("rphjb_MortacciVostri.gif"),
        MediaFile("rphjb_StateZittiZozziUltimi.mp3"),
        MediaFile("rphjb_ConQuestaTecnica.mp4"),
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
        StringTextTriggerValue("applau")
      ),
      List(
        MediaFile("rphjb_Applauso.gif"),
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
        MediaFile("rphjb_VeniteQua.gif"),
        MediaFile("rphjb_VeniteQua.mp3"),
        MediaFile("rphjb_VeniteQua.mp4")
      ),
      replySelection = RandomSelection
    ),
    ReplyBundleMessage(
      TextTrigger(
        RegexTextTriggerValue("\\bpaga(re)?\\b".r),
        StringTextTriggerValue("soldi"),
        StringTextTriggerValue("bollette"),
        StringTextTriggerValue("tasse"),
        StringTextTriggerValue("bolletta"),
        StringTextTriggerValue("tassa")
      ),
      List(
        MediaFile("rphjb_ChiCacciaISoldi.gif"),
        MediaFile("rphjb_ChiCacciaISoldi.mp3"),
        MediaFile("rphjb_BigMoney.mp4")
      ),
      replySelection = RandomSelection
    ),
    ReplyBundleMessage(
      TextTrigger(
        RegexTextTriggerValue("[od]?dio mio[,]? no".r)
      ),
      List(
        MediaFile("rphjb_OddioMioNo.gif"),
        MediaFile("rphjb_OddioMioNo.mp3"),
        MediaFile("rphjb_OddioMioNo.mp4")
      ),
      replySelection = RandomSelection
    ),
    ReplyBundleMessage(
      TextTrigger(
        RegexTextTriggerValue("[sono ]?a[r]{1,2}iva[dt]o".r),
        StringTextTriggerValue("piacere")
      ),
      List(
        MediaFile("rphjb_Arivato.gif"),
        MediaFile("rphjb_Arivato.mp3"),
        MediaFile("rphjb_Arivato.mp4")
      ),
      replySelection = RandomSelection
    ),
    ReplyBundleMessage(
      TextTrigger(
        RegexTextTriggerValue("delu[sd]".r)
      ),
      List(
        MediaFile("rphjb_Deluso.gif"),
        MediaFile("rphjb_Deluso.mp3"),
        MediaFile("rphjb_DeludendoQuasiTutto.mp4")
      ),
      replySelection = RandomSelection
    ),
    ReplyBundleMessage(
      TextTrigger(
        StringTextTriggerValue("fate come vi pare"),
        RegexTextTriggerValue("sti [gc]azzi".r)
      ),
      List(
        MediaFile("rphjb_ComeViPare.gif"),
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
        MediaFile("rphjb_CapitoDoveStiamo.gif")
      ),
      replySelection = RandomSelection
    ),
    ReplyBundleMessage(
      TextTrigger(
        StringTextTriggerValue("non sai molto")
      ),
      List(
        MediaFile("rphjb_NonSaiMolto.gif"),
        MediaFile("rphjb_RadioRockErrori.mp4")
      ),
      replySelection = RandomSelection
    ),
    ReplyBundleMessage(
      TextTrigger(
        StringTextTriggerValue("errori")
      ),
      List(
        MediaFile("rphjb_MaiErrori.gif"),
        MediaFile("rphjb_MaiErrori.mp4")
      ),
      replySelection = RandomSelection
    ),
    ReplyBundleMessage(
      TextTrigger(
        RegexTextTriggerValue("\\bpasqua\\b".r)
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
        RegexTextTriggerValue("crema alla [gc]io[gc]+ola[dt]a".r),
      ),
      List(
        MediaFile("rphjb_ZuccheroVanigliaCremaCioccolataPandoro.mp3"),
        MediaFile("rphjb_AuguriPerPasqua.mp4")
      ),
      replySelection = RandomSelection
    ),
    ReplyBundleMessage(
      TextTrigger(
        StringTextTriggerValue("timore"),
        StringTextTriggerValue("paura"),
        RegexTextTriggerValue("diri[g]+en[dt]i".r),
      ),
      List(
        MediaFile("rphjb_Dirigenti.gif"),
        MediaFile("rphjb_AncoraNoDirigenti.mp4")
      ),
      replySelection = RandomSelection
    ),
    ReplyBundleMessage(
      TextTrigger(
        StringTextTriggerValue("guerra")
      ),
      List(
        MediaFile("rphjb_GuerraTotale.gif"),
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
        MediaFile("rphjb_NonVoglioNessuno.gif"),
        MediaFile("rphjb_NonVoglioNessuno.mp4")
      ),
      replySelection = RandomSelection
    ),
    ReplyBundleMessage(
      TextTrigger(
        StringTextTriggerValue("peggio del peggio")
      ),
      List(
        MediaFile("rphjb_PeggioDelPeggio.gif"),
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
        MediaFile("rphjb_Bebop.gif"),
        MediaFile("rphjb_Bebop.mp4")
      ),
      replySelection = RandomSelection
    ),
    ReplyBundleMessage(
      TextTrigger(
        RegexTextTriggerValue("(18|diciott['o]?) anni".r)
      ),
      List(
        MediaFile("rphjb_DiciottoAnni.mp4"),
        MediaFile("rphjb_AvremoDiciottanni.gif")
      ),
      replySelection = RandomSelection
    ),
    ReplyBundleMessage(
      TextTrigger(
        RegexTextTriggerValue("(cinque|5) dita".r),
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
        StringTextTriggerValue("ratti"),
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
        RegexTextTriggerValue("[cg]ri[dt]i[gc]a[dt]o".r)
      ),
      List(
        MediaFile("rphjb_FuCriticato.gif"),
        MediaFile("rphjb_FuCriticato.mp4"),
      ),
      replySelection = RandomSelection
    ),
    ReplyBundleMessage(
      TextTrigger(
        StringTextTriggerValue("il venerdì")
      ),
      List(
        MediaFile("rphjb_Venerdi.mp3"),
        MediaFile("rphjb_Venerdi.mp4")
      ),
      replySelection = RandomSelection
    ),
  )

}
