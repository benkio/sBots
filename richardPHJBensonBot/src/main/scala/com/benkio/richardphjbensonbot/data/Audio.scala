package com.benkio.richardphjbensonbot.data

import com.benkio.telegrambotinfrastructure.messagefiltering._
import com.benkio.telegrambotinfrastructure.model._

object Audio {

  def messageRepliesAudioData[F[_]]: List[ReplyBundleMessage[F]] = List(
    ReplyBundleMessage(
      TextTrigger(
        StringTextTriggerValue("come state stasera")
      ),
      List(
        MediaFile("rphjb_LetSGoodStateBene.mp3")
      )
    ),
    ReplyBundleMessage(
      TextTrigger(
        StringTextTriggerValue("tastierista")
      ),
      List(
        MediaFile("rphjb_Tastierista.mp3")
      )
    ),
    ReplyBundleMessage(
      TextTrigger(
        StringTextTriggerValue("caprette"),
        StringTextTriggerValue("acidi"),
        StringTextTriggerValue("pomodori"),
        StringTextTriggerValue("legumi"),
        StringTextTriggerValue("ragni"),
        StringTextTriggerValue("male il collo"),
      ),
      List(
        MediaFile("rphjb_ListaMaleCollo.mp3")
      )
    ),
    ReplyBundleMessage(
      TextTrigger(
        RegexTextTriggerValue("scu[-]?sa[h]? scu[-]?sa[h]?".r, 11)
      ),
      List(
        MediaFile("rphjb_Scusa.mp3")
      )
    ),
    ReplyBundleMessage(
      TextTrigger(
        StringTextTriggerValue("lo sapevo")
      ),
      List(
        MediaFile("rphjb_LoSapevoIo.mp3")
      )
    ),
    ReplyBundleMessage(
      TextTrigger(
        StringTextTriggerValue("mi auguro")
      ),
      List(
        MediaFile("rphjb_IoMiAuguro.mp3")
      )
    ),
    ReplyBundleMessage(
      TextTrigger(
        StringTextTriggerValue("non leggo")
      ),
      List(
        MediaFile("rphjb_NonLeggoQuelloCheScrivete.mp3")
      )
    ),
    ReplyBundleMessage(
      TextTrigger(
        StringTextTriggerValue("devo sopportare")
      ),
      List(
        MediaFile("rphjb_DevoSopportare.mp3")
      )
    ),
    ReplyBundleMessage(
      TextTrigger(
        StringTextTriggerValue("primo sbaglio")
      ),
      List(
        MediaFile("rphjb_PrimoSbaglio.mp3")
      )
    ),
    ReplyBundleMessage(
      TextTrigger(
        StringTextTriggerValue("non mi ricordo più")
      ),
      List(
        MediaFile("rphjb_NonMiRicordoPiu.mp3")
      )
    ),
    ReplyBundleMessage(
      TextTrigger(
        StringTextTriggerValue("pensato a tutto"),
        StringTextTriggerValue("accontentare tutti")
      ),
      List(
        MediaFile("rphjb_PensatoATutto.mp3")
      )
    ),
    ReplyBundleMessage(
      TextTrigger(
        StringTextTriggerValue("se è un amico")
      ),
      List(
        MediaFile("rphjb_VedereAmico.mp3")
      ),
      matcher = ContainsAll
    ),
    ReplyBundleMessage(
      TextTrigger(
        StringTextTriggerValue("appuntamento")
      ),
      List(
        MediaFile("rphjb_Appuntamento.mp3")
      )
    ),
    ReplyBundleMessage(
      TextTrigger(
        StringTextTriggerValue("perchè l'ho fatto"),
        StringTextTriggerValue("non do spiegazioni")
      ),
      List(
        MediaFile("rphjb_PercheLHoFatto.mp3")
      )
    ),
    ReplyBundleMessage(
      TextTrigger(
        StringTextTriggerValue("non ho detto tutto"),
        StringTextTriggerValue("ascoltami")
      ),
      List(
        MediaFile("rphjb_NonHoDettoTutto.mp3")
      )
    ),
    ReplyBundleMessage(
      TextTrigger(
        StringTextTriggerValue("ogni volta")
      ),
      List(
        MediaFile("rphjb_OgniVolta.mp3")
      )
    ),
    ReplyBundleMessage(
      TextTrigger(
        StringTextTriggerValue("non mi calmo")
      ),
      List(
        MediaFile("rphjb_NonMiCalmo.mp3")
      )
    ),
    ReplyBundleMessage(
      TextTrigger(
        StringTextTriggerValue("devo dire che")
      ),
      List(
        MediaFile("rphjb_DevoDireChe.mp3")
      )
    ),
    ReplyBundleMessage(
      TextTrigger(
        StringTextTriggerValue("eccomi qua")
      ),
      List(
        MediaFile("rphjb_EccomiQua.mp3")
      )
    ),
    ReplyBundleMessage(
      TextTrigger(
        StringTextTriggerValue("animali")
      ),
      List(
        MediaFile("rphjb_Animali.mp3")
      )
    ),
    ReplyBundleMessage(
      TextTrigger(
        RegexTextTriggerValue("(mi sento|sto) meglio".r, 10)
      ),
      List(
        MediaFile("rphjb_MiSentoMeglio.mp3")
      )
    ),
    ReplyBundleMessage(
      TextTrigger(
        StringTextTriggerValue("altari"),
        StringTextTriggerValue("realtà"),
      ),
      List(
        MediaFile("rphjb_AltariFatiscentiRealta.mp3"),
        MediaFile("rphjb_AltariFatiscentiRealta.mp4")
      ),
      replySelection = RandomSelection
    ),
    ReplyBundleMessage(
      TextTrigger(
        StringTextTriggerValue("non è vero")
      ),
      List(
        MediaFile("rphjb_NonEVero.mp3")
      )
    ),
    ReplyBundleMessage(
      TextTrigger(
        StringTextTriggerValue("completamente nudo")
      ),
      List(
        MediaFile("rphjb_CompletamenteNudo.mp3")
      )
    ),
    ReplyBundleMessage(
      TextTrigger(
        StringTextTriggerValue("dovrei lavarmelo di più"),
        StringTextTriggerValue("il cazzo me lo pulisci un'altra volta"),
      ),
      List(
        MediaFile("rphjb_LavareCazzo.mp3")
      )
    ),
    ReplyBundleMessage(
      TextTrigger(
        RegexTextTriggerValue("giù( giù)+".r, 7)
      ),
      List(
        MediaFile("rphjb_GiuGiuGiu.mp3")
      )
    ),
    ReplyBundleMessage(
      TextTrigger(
        StringTextTriggerValue("viale zara"),
        RegexTextTriggerValue("cas(a|e) chius(a|e)".r, 11)
      ),
      List(
        MediaFile("rphjb_VialeZara.mp3")
      )
    ),
    ReplyBundleMessage(
      TextTrigger(
        StringTextTriggerValue("tocca il culo")
      ),
      List(
        MediaFile("rphjb_MiToccaIlCulo.mp3")
      )
    ),
    ReplyBundleMessage(
      TextTrigger(
        StringTextTriggerValue("negli occhiali"),
        StringTextTriggerValue("sulla spalla"),
        StringTextTriggerValue("gianguido"),
      ),
      List(
        MediaFile("rphjb_PannaOcchialiSpalla.mp3")
      )
    ),
    ReplyBundleMessage(
      TextTrigger(
        StringTextTriggerValue("state zitti")
      ),
      List(
        MediaFile("rphjb_StateZittiZozziUltimi.mp3")
      )
    ),
    ReplyBundleMessage(
      TextTrigger(
        StringTextTriggerValue("solo io")
      ),
      List(
        MediaFile("rphjb_SoloIo.mp3")
      )
    ),
    ReplyBundleMessage(
      TextTrigger(
        StringTextTriggerValue("saranno cazzi vostri")
      ),
      List(
        MediaFile("rphjb_SarannoCazziVostri.mp3")
      )
    ),
    ReplyBundleMessage(
      TextTrigger(
        RegexTextTriggerValue("poteri (ter[r]+ib[b]+ili|demoniaci)".r, 16)
      ),
      List(
        MediaFile("rphjb_PoteriDemoniaci.mp3")
      )
    ),
    ReplyBundleMessage(
      TextTrigger(
        RegexTextTriggerValue("sono( pure)? italiane".r, 13),
        RegexTextTriggerValue("non so(no)? ungheresi".r, 16)
      ),
      List(
        MediaFile("rphjb_ItalianeUngheresi.mp3")
      )
    ),
    ReplyBundleMessage(
      TextTrigger(
        RegexTextTriggerValue("\\bcolpevole\\b".r, 9)
      ),
      List(
        MediaFile("rphjb_IlColpevole.mp3")
      )
    ),
    ReplyBundleMessage(
      TextTrigger(
        StringTextTriggerValue("vi spacco il culo")
      ),
      List(
        MediaFile("rphjb_ViSpaccoIlCulo.mp3")
      )
    ),
    ReplyBundleMessage(
      TextTrigger(
        StringTextTriggerValue("il sindaco")
      ),
      List(
        MediaFile("rphjb_Sindaco.mp3")
      )
    ),
    ReplyBundleMessage(
      TextTrigger(
        RegexTextTriggerValue("si leva (sta roba|sto schifo)".r, 16),
        StringTextTriggerValue("questo schifo")
      ),
      List(
        MediaFile("rphjb_QuestoSchifo.mp3")
      )
    ),
    ReplyBundleMessage(
      TextTrigger(
        StringTextTriggerValue("preservativo")
      ),
      List(
        MediaFile("rphjb_Preservativo.mp3")
      )
    ),
    ReplyBundleMessage(
      TextTrigger(
        StringTextTriggerValue("korn"),
        StringTextTriggerValue("giovanni battista"),
        StringTextTriggerValue("acque del giordano"),
        StringTextTriggerValue("battezzato"),
        StringTextTriggerValue("battesimo")
      ),
      List(
        MediaFile("rphjb_Battesimo.mp3")
      )
    ),
    ReplyBundleMessage(
      TextTrigger(
        RegexTextTriggerValue("a( )?s[s]+tronzo".r, 8),
        RegexTextTriggerValue("stronz[o]{3,}".r, 9)
      ),
      List(
        MediaFile("rphjb_AStronzo.mp3")
      )
    ),
    ReplyBundleMessage(
      TextTrigger(
        StringTextTriggerValue("ancora voi")
      ),
      List(
        MediaFile("rphjb_AncoraVoi.mp3")
      )
    ),
    ReplyBundleMessage(
      TextTrigger(
        StringTextTriggerValue("sborrata"),
        StringTextTriggerValue("scopare")
      ),
      List(
        MediaFile("rphjb_Sborrata.mp3")
      )
    ),
    ReplyBundleMessage(
      TextTrigger(
        StringTextTriggerValue("finire male"),
        StringTextTriggerValue("tocca benson")
      ),
      List(
        MediaFile("rphjb_FinireMale.mp3")
      )
    ),
    ReplyBundleMessage(
      TextTrigger(
        StringTextTriggerValue("musica tecnica"),
        StringTextTriggerValue("antonacci"),
        StringTextTriggerValue("grignani"),
        StringTextTriggerValue("jovanotti"),
      ),
      List(
        MediaFile("rphjb_Rock.mp3")
      )
    ),
    ReplyBundleMessage(
      TextTrigger(
        RegexTextTriggerValue("conosce(nza|re)".r, 9),
        StringTextTriggerValue("il sapere"),
        StringTextTriggerValue("veri valori"),
      ),
      List(
        MediaFile("rphjb_Conoscere.mp3")
      )
    ),
    ReplyBundleMessage(
      TextTrigger(
        StringTextTriggerValue("sono"),
        StringTextTriggerValue("ultimo")
      ),
      List(
        MediaFile("rphjb_SonoUltimo.mp3"),
        MediaFile("rphjb_SonoIoUltimo.mp3")
      ),
      matcher = ContainsAll,
      replySelection = RandomSelection
    ),
    ReplyBundleMessage(
      TextTrigger(
        StringTextTriggerValue("anguille"),
        StringTextTriggerValue("polipi"),
        StringTextTriggerValue("cetrioli"),
        StringTextTriggerValue("il problema è uno solo"),
        StringTextTriggerValue("non riesco a suonare")
      ),
      List(
        MediaFile("rphjb_Problema.mp3")
      )
    ),
    ReplyBundleMessage(
      TextTrigger(
        StringTextTriggerValue("brutto frocio")
      ),
      List(
        MediaFile("rphjb_BruttoFrocio.mp3")
      )
    ),
    ReplyBundleMessage(
      TextTrigger(
        StringTextTriggerValue("questo ragazzo"),
        StringTextTriggerValue("eccitare"),
        StringTextTriggerValue("lucio dalla")
      ),
      List(
        MediaFile("rphjb_LucioDalla.mp3")
      )
    ),
    ReplyBundleMessage(
      TextTrigger(
        StringTextTriggerValue("chiesa"),
        RegexTextTriggerValue("preghier(a|ina)".r, 9),
        StringTextTriggerValue("io non credo"),
        StringTextTriggerValue("la medicina"),
        StringTextTriggerValue("andare dal dottore"),
        RegexTextTriggerValue("\\billusi\\b".r, 6),
        StringTextTriggerValue("manfrine"),
      ),
      List(
        MediaFile("rphjb_Chiesa.mp3")
      )
    ),
    ReplyBundleMessage(
      TextTrigger(
        StringTextTriggerValue("maledetto")
      ),
      List(
        MediaFile("rphjb_Maledetto.mp3")
      )
    ),
    ReplyBundleMessage(
      TextTrigger(
        StringTextTriggerValue("..magari"),
        StringTextTriggerValue("magari.."),
      ),
      List(
        MediaFile("rphjb_Magari.mp3")
      )
    ),
    ReplyBundleMessage(
      TextTrigger(
        StringTextTriggerValue("io ti aiuto")
      ),
      List(
        MediaFile("rphjb_Aiuto.mp3")
      )
    ),
    ReplyBundleMessage(
      TextTrigger(
        StringTextTriggerValue("faccio schifo")
      ),
      List(
        MediaFile("rphjb_FaccioSchifo.mp3")
      )
    ),
    ReplyBundleMessage(
      TextTrigger(
        RegexTextTriggerValue("ci sei ritornat[ao]".r, 15)
      ),
      List(
        MediaFile("rphjb_Ritornata.mp3")
      )
    ),
    ReplyBundleMessage(
      TextTrigger(
        StringTextTriggerValue("che succede")
      ),
      List(
        MediaFile("rphjb_CheSuccede.mp3")
      )
    ),
    ReplyBundleMessage(
      TextTrigger(
        StringTextTriggerValue("due ossa")
      ),
      List(
        MediaFile("rphjb_DueOssa.mp3")
      )
    ),
    ReplyBundleMessage(
      TextTrigger(
        RegexTextTriggerValue("m[ie] fai( proprio)? schifo".r, 13)
      ),
      List(
        MediaFile("rphjb_Schifo.mp3")
      )
    ),
    ReplyBundleMessage(
      TextTrigger(
        StringTextTriggerValue("una sera")
      ),
      List(
        MediaFile("rphjb_Sera.mp3")
      )
    ),
    ReplyBundleMessage(
      TextTrigger(
        RegexTextTriggerValue("oppur[ae]".r, 6)
      ),
      List(
        MediaFile("rphjb_Oppura.mp3")
      )
    ),
    ReplyBundleMessage(
      TextTrigger(
        StringTextTriggerValue("cosa preferisci"),
        StringTextTriggerValue("ragazzetta"),
        StringTextTriggerValue("carne bianca"),
        StringTextTriggerValue("carne saporita")
      ),
      List(
        MediaFile("rphjb_RagazzettaCarne.mp3")
      )
    ),
    ReplyBundleMessage(
      TextTrigger(
        StringTextTriggerValue("fragolina"),
        StringTextTriggerValue("fichina")
      ),
      List(
        MediaFile("rphjb_FragolinaFichina.mp3")
      )
    ),
    ReplyBundleMessage(
      TextTrigger(
        StringTextTriggerValue("non vi deluderò")
      ),
      List(
        MediaFile("rphjb_NonViDeludero.mp3")
      )
    ),
    ReplyBundleMessage(
      TextTrigger(
        StringTextTriggerValue("vi saluto"),
        RegexTextTriggerValue("(col|con il) (cuore|cervello|anima|pisello|martello)".r, 9),
      ),
      List(
        MediaFile("rphjb_ViSaluto.mp3")
      )
    ),
    ReplyBundleMessage(
      TextTrigger(
        StringTextTriggerValue("gigi d'alessio"),
        StringTextTriggerValue("anna tatangelo")
      ),
      List(
        MediaFile("rphjb_GigiDAlessioAnnaTatangelo.mp3")
      )
    ),
    ReplyBundleMessage(
      TextTrigger(
        RegexTextTriggerValue("funzioni gene[g]{2,}iative".r, 21),
        StringTextTriggerValue("non è un uomo"),
        StringTextTriggerValue("voce da uomo"),
        RegexTextTriggerValue("è (veramente )?una donna".r, 11)
      ),
      List(
        MediaFile("rphjb_VoceDaUomo.mp3")
      )
    ),
    ReplyBundleMessage(
      TextTrigger(
        StringTextTriggerValue("culo chiacchierato"),
        StringTextTriggerValue("rob halford")
      ),
      List(
        MediaFile("rphjb_CuloChiacchierato.mp3")
      )
    ),
    ReplyBundleMessage(
      TextTrigger(
        StringTextTriggerValue("negri"),
        StringTextTriggerValue("sezione ritmica")
      ),
      List(
        MediaFile("rphjb_NegriSezioneRitmica.mp3")
      )
    ),
    ReplyBundleMessage(
      TextTrigger(
        StringTextTriggerValue("incrinata la voce"),
        RegexTextTriggerValue("parlo come un(a specie di)? frocio".r, 20)
      ),
      List(
        MediaFile("rphjb_IncrinataLaVoceFrocio.mp3")
      )
    ),
    ReplyBundleMessage(
      TextTrigger(
        StringTextTriggerValue("io mi ricordo tutto")
      ),
      List(
        MediaFile("rphjb_IoMiRicordoTutto.mp3")
      )
    ),
    ReplyBundleMessage(
      TextTrigger(
        StringTextTriggerValue("che magagna"),
        StringTextTriggerValue("che fregatura"),
      ),
      List(
        MediaFile("rphjb_CapitoCheMagagna.mp3")
      )
    ),
    ReplyBundleMessage(
      TextTrigger(
        StringTextTriggerValue("edu falasci"),
        StringTextTriggerValue("edoardo falaschi"),
      ),
      List(
        MediaFile("rphjb_EduFalasci.mp3"),
        MediaFile("rphjb_VergognatiMatosFalasci.mp3")
      ),
      replySelection = RandomSelection
    ),
    ReplyBundleMessage(
      TextTrigger(
        StringTextTriggerValue("kiko loureiro"),
        StringTextTriggerValue("che salva la situazione")
      ),
      List(
        MediaFile("rphjb_KikoLoureiroSalvaSituazione.mp3")
      )
    ),
    ReplyBundleMessage(
      TextTrigger(
        StringTextTriggerValue("luca di noia"),
        StringTextTriggerValue("geometrici"),
        StringTextTriggerValue("matematici"),
        StringTextTriggerValue("analitici"),
      ),
      List(
        MediaFile("rphjb_MatematiciAnaliticiDiNoia.mp3")
      )
    ),
    ReplyBundleMessage(
      TextTrigger(
        StringTextTriggerValue("andre matos")
      ),
      List(
        MediaFile("rphjb_MatosShaman.mp3"),
        MediaFile("rphjb_VergognatiMatosFalasci.mp3")
      ),
      replySelection = RandomSelection
    ),
    ReplyBundleMessage(
      TextTrigger(
        StringTextTriggerValue("shaman"),
      ),
      List(
        MediaFile("rphjb_MatosShaman.mp3"),
      ),
    ),
    ReplyBundleMessage(
      TextTrigger(
        StringTextTriggerValue("vengognati"),
      ),
      List(
        MediaFile("rphjb_VergognatiMatosFalasci.mp3"),
      ),
    ),
    ReplyBundleMessage(
      TextTrigger(
        StringTextTriggerValue("via zara"),
        StringTextTriggerValue("sei brava a truccare"),
        StringTextTriggerValue("non vali niente"),
        StringTextTriggerValue("sei l'ultima"),
        RegexTextTriggerValue("manco trucc[aà] sai".r, 16),
      ),
      List(
        MediaFile("rphjb_TruccareViaZara.mp3"),
      ),
    ),
    ReplyBundleMessage(
      TextTrigger(
        StringTextTriggerValue("luca di noia")
      ),
      List(
        MediaFile("rphjb_LucaDiNoia.mp3"),
        MediaFile("rphjb_LucaDiNoia2.mp3"),
      ),
      replySelection = RandomSelection
    ),
    ReplyBundleMessage(
      TextTrigger(
        StringTextTriggerValue("che schifo!"),
        RegexTextTriggerValue("che( )?(s)+chifo".r, 10),
      ),
      List(
        MediaFile("rphjb_Schifosi4.mp3"),
      )
    ),
    ReplyBundleMessage(
      TextTrigger(
        StringTextTriggerValue("non parlare"),
        StringTextTriggerValue("non hai il diritto"),
        StringTextTriggerValue("la trasmissione è la mia"),
      ),
      List(
        MediaFile("rphjb_NonParlareTeTrasmissioneMia.mp3"),
      )
    ),
    ReplyBundleMessage(
      TextTrigger(
        StringTextTriggerValue("fa paura pure a"),
        StringTextTriggerValue("al di meola")
      ),
      List(
        MediaFile("rphjb_PauraAdAlDiMeola.mp3"),
      )
    ),
    ReplyBundleMessage(
      TextTrigger(
        RegexTextTriggerValue("alle (22|ventidue)".r, 7)
      ),
      List(
        MediaFile("rphjb_Alle22.mp3")
      )
    ),
    ReplyBundleMessage(
      TextTrigger(
        StringTextTriggerValue("gelatina"),
        StringTextTriggerValue("secchi d'acqua"),
        StringTextTriggerValue("fazzoletti"),
        StringTextTriggerValue("sapone")
      ),
      List(
        MediaFile("rphjb_GelatinaFazzolettiSecchiAcquaSapone.mp3")
      )
    ),
    ReplyBundleMessage(
      TextTrigger(
        RegexTextTriggerValue("suonato (abbastanza )?bene".r, 12),
        RegexTextTriggerValue("e che cazz[o!]{2,}".r, 12),
      ),
      List(
        MediaFile("rphjb_SuonatoAbbastanzaBeneEVero.mp3")
      )
    ),
    ReplyBundleMessage(
      TextTrigger(
        StringTextTriggerValue("romantic"),
        RegexTextTriggerValue("(ar|in) culo".r, 7)
      ),
      List(
        MediaFile("rphjb_RomanticiDonneArCulo.mp3")
      )
    ),
    ReplyBundleMessage(
      TextTrigger(
        StringTextTriggerValue("bonolis"),
        StringTextTriggerValue("vi lascio nelle mani")
      ),
      List(
        MediaFile("rphjb_NelleManiDiBonolis.mp3")
      )
    ),
    ReplyBundleMessage(
      TextTrigger(
        RegexTextTriggerValue("(9|nove) mesi".r, 6),
        RegexTextTriggerValue("voler (uscire|rientrare)".r, 12),
        StringTextTriggerValue("una vita intera")
      ),
      List(
        MediaFile("rphjb_9MesiUscireRientrare.mp3")
      )
    ),
    ReplyBundleMessage(
      TextTrigger(
        StringTextTriggerValue("suono che cosa"),
        StringTextTriggerValue("alghe marine"),
      ),
      List(
        MediaFile("rphjb_ChitarraZuccheroAlgheVino.mp3")
      ),
    ),
    ReplyBundleMessage(
      TextTrigger(
        StringTextTriggerValue("misero antro"),
        StringTextTriggerValue("addibiti ad agnelli"),
        StringTextTriggerValue("una pisciata"),
      ),
      List(
        MediaFile("rphjb_MiseroAntroGanciAgnelliPisciata.mp3")
      ),
    )
  )
}
