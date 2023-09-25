package com.benkio.richardphjbensonbot.data

import com.benkio.telegrambotinfrastructure.model._

object Video {

  def messageRepliesVideoData[F[_]]: List[ReplyBundleMessage[F]] = List(
    ReplyBundleMessage(
      TextTrigger(
        StringTextTriggerValue("amici veri"),
        StringTextTriggerValue("soldati")
      ),
      List(
        MediaFile("rphjb_AmiciVeriVecchiSoldati.mp4")
      )
    ),
    ReplyBundleMessage(
      TextTrigger(
        StringTextTriggerValue("gianni neri")
      ),
      List(
        MediaFile("rphjb_RingraziareGianniTraffico.mp4"),
        MediaFile("rphjb_GianniNeriCoppiaMiciciale.mp4"),
        MediaFile("rphjb_GianniNeriCheFineHaiFatto.mp4")
      ),
      replySelection = RandomSelection
    ),
    ReplyBundleMessage(
      TextTrigger(
        RegexTextTriggerValue("accor(data|dana)".r, 9)
      ),
      List(
        MediaFile("rphjb_Accordana.mp4")
      )
    ),
    ReplyBundleMessage(
      TextTrigger(
        RegexTextTriggerValue("\\brap\\b".r, 3),
        StringTextTriggerValue("musica italiana"),
        StringTextTriggerValue("tullio pane"),
        StringTextTriggerValue("otello profazio"),
        StringTextTriggerValue("mario lanza"),
        StringTextTriggerValue("gianni celeste"),
        StringTextTriggerValue("luciano tajoli")
      ),
      List(
        MediaFile("rphjb_RapMusicaMelodicaListaCantanti.mp4")
      )
    ),
    ReplyBundleMessage(
      TextTrigger(
        StringTextTriggerValue("eric clapton"),
        RegexTextTriggerValue("uo[m]+ini d'affari".r, 15),
      ),
      List(
        MediaFile("rphjb_EricClaptonDrogaUominiAffari.mp4")
      )
    ),
    ReplyBundleMessage(
      TextTrigger(
        StringTextTriggerValue("rampolli"),
        StringTextTriggerValue("studi a boston"),
        StringTextTriggerValue("borghesia alta"),
        StringTextTriggerValue("idoli delle mamme"),
        StringTextTriggerValue("figliole")
      ),
      List(
        MediaFile("rphjb_Rampolli.mp4")
      )
    ),
    ReplyBundleMessage(
      TextTrigger(
        StringTextTriggerValue("capelli corti"),
        StringTextTriggerValue("giacca"),
        StringTextTriggerValue("cravatta"),
        StringTextTriggerValue("passaporto degli stronzi")
      ),
      List(
        MediaFile("rphjb_RocchettariCapelliCortiGiaccaCravattaPassaportoStronzi.mp4")
      )
    ),
    ReplyBundleMessage(
      TextTrigger(
        RegexTextTriggerValue("fregat(a|ura)".r, 7)
      ),
      List(
        MediaFile("rphjb_FregataFregatura.mp4")
      )
    ),
    ReplyBundleMessage(
      TextTrigger(
        RegexTextTriggerValue("\\bmula\\b".r, 4),
        StringTextTriggerValue("storia della mula")
      ),
      List(
        MediaFile("rphjb_Mula.mp4")
      )
    ),
    ReplyBundleMessage(
      TextTrigger(
        StringTextTriggerValue("si o no")
      ),
      List(
        MediaFile("rphjb_SiONo.mp4")
      )
    ),
    ReplyBundleMessage(
      TextTrigger(
        StringTextTriggerValue("streghe")
      ),
      List(
        MediaFile("rphjb_Streghe.mp4")
      )
    ),
    ReplyBundleMessage(
      TextTrigger(
        RegexTextTriggerValue("(tornando|andando) (all')?indietro".r, 16),
        StringTextTriggerValue("innovazione")
      ),
      List(
        MediaFile("rphjb_InnovazioneStiamoTornandoIndietro.mp4")
      )
    ),
    ReplyBundleMessage(
      TextTrigger(
        StringTextTriggerValue("trovamelo")
      ),
      List(
        MediaFile("rphjb_AngeloTrovamelo.mp4")
      )
    ),
    ReplyBundleMessage(
      TextTrigger(
        StringTextTriggerValue("plettro"),
        StringTextTriggerValue("vicoletto")
      ),
      List(
        MediaFile("rphjb_ChitarraPlettroVicoletto.mp4"),
        MediaFile("rphjb_ChitarraPlettroVicoletto2.mp4")
      ),
      replySelection = RandomSelection
    ),
    ReplyBundleMessage(
      TextTrigger(
        StringTextTriggerValue("diversi mondi"),
        StringTextTriggerValue("letti sfatti")
      ),
      List(
        MediaFile("rphjb_LettiSfattiDiversiMondi.mp4")
      )
    ),
    ReplyBundleMessage(
      TextTrigger(
        StringTextTriggerValue("via delle albizzie"),
        StringTextTriggerValue("carpenelli")
      ),
      List(
        MediaFile("rphjb_AlbizziePerlaPioggia.mp4")
      )
    ),
    ReplyBundleMessage(
      TextTrigger(
        StringTextTriggerValue("ramarro"),
        StringTextTriggerValue("yngwie"),
        StringTextTriggerValue("malmsteen"),
        StringTextTriggerValue("impellitteri")
      ),
      List(
        MediaFile("rphjb_Ramarro.mp4")
      )
    ),
    ReplyBundleMessage(
      TextTrigger(
        StringTextTriggerValue("vi dovete spaventare")
      ),
      List(
        MediaFile("rphjb_ViDoveteSpaventare.mp4")
      )
    ),
    ReplyBundleMessage(
      TextTrigger(
        StringTextTriggerValue("amore nello suonare"),
        StringTextTriggerValue("uno freddo"),
        StringTextTriggerValue("buddisti"),
      ),
      List(
        MediaFile("rphjb_AmoreSuonareFreddoBuddistiSchifoso.mp4")
      )
    ),
    ReplyBundleMessage(
      TextTrigger(
        RegexTextTriggerValue("riciclando il (suo )?peggio".r, 20)
      ),
      List(
        MediaFile("rphjb_SteveVaiRiciclando.mp4")
      )
    ),
    ReplyBundleMessage(
      TextTrigger(
        StringTextTriggerValue("già il titolo"),
        StringTextTriggerValue("coi due punti"),
        RegexTextTriggerValue("re[a]?l illusions".r, 13)
      ),
      List(
        MediaFile("rphjb_RelIllusions.mp4")
      )
    ),
    ReplyBundleMessage(
      TextTrigger(
        StringTextTriggerValue("trattori"),
        StringTextTriggerValue("palmizio"),
        StringTextTriggerValue("meno c'è"),
        StringTextTriggerValue("meno si rompe")
      ),
      List(
        MediaFile("rphjb_Palmizio.mp4")
      )
    ),
    ReplyBundleMessage(
      TextTrigger(
        StringTextTriggerValue("peso di un cervello")
      ),
      List(
        MediaFile("rphjb_VitaNemicoCervello.mp4")
      )
    ),
    ReplyBundleMessage(
      TextTrigger(
        StringTextTriggerValue("cervello pensante"),
        StringTextTriggerValue("questa volta no"),
        StringTextTriggerValue("stupidità incresciosa")
      ),
      List(
        MediaFile("rphjb_CervelloPensante.mp4")
      )
    ),
    ReplyBundleMessage(
      TextTrigger(
        StringTextTriggerValue("percussionista"),
        StringTextTriggerValue("batterista")
      ),
      List(
        MediaFile("rphjb_CollaSerpeSigarettePercussionista.mp4")
      )
    ),
    ReplyBundleMessage(
      TextTrigger(
        StringTextTriggerValue("perla di pioggia"),
        StringTextTriggerValue("dove non piove mai")
      ),
      List(
        MediaFile("rphjb_PerlaDiPioggia.mp4"),
        MediaFile("rphjb_AlbizziePerlaPioggia.mp4")
      ),
      replySelection = RandomSelection
    ),
    ReplyBundleMessage(
      TextTrigger(
        StringTextTriggerValue("madre tortura"),
        RegexTextTriggerValue("(madre )?parrucca".r, 8)
      ),
      List(
        MediaFile("rphjb_MadreTorturaParrucca.mp4")
      ),
    ),
    ReplyBundleMessage(
      TextTrigger(
        RegexTextTriggerValue("[l]+[i]+[b]+[e]+[r]+[i]+".r, 6)
      ),
      List(
        MediaFile("rphjb_Liberi.mp4")
      )
    ),
    ReplyBundleMessage(
      TextTrigger(
        RegexTextTriggerValue("\\bcinta\\b".r, 5),
        StringTextTriggerValue("bruce kulick")
      ),
      List(
        MediaFile("rphjb_CintaProblema.mp4")
      )
    ),
    ReplyBundleMessage(
      TextTrigger(
        StringTextTriggerValue("sepoltura")
      ),
      List(
        MediaFile("rphjb_SepolturaRisata.mp4")
      )
    ),
    ReplyBundleMessage(
      TextTrigger(
        StringTextTriggerValue("colla tra serpe e serpe")
      ),
      List(
        MediaFile("rphjb_CollaSerpe.mp4"),
        MediaFile("rphjb_CollaSerpe.mp3"),
        MediaFile("rphjb_CollaSerpeSigarettePercussionista.mp4")
      ),
      replySelection = RandomSelection
    ),
    ReplyBundleMessage(
      TextTrigger(
        StringTextTriggerValue("big money")
      ),
      List(
        MediaFile("rphjb_BigMoney.mp4")
      )
    ),
    ReplyBundleMessage(
      TextTrigger(
        StringTextTriggerValue("in cantina")
      ),
      List(
        MediaFile("rphjb_InCantina.mp4")
      )
    ),
    ReplyBundleMessage(
      TextTrigger(
        RegexTextTriggerValue("(mi|me) so(n|no)? rotto il ca\\b".r, 17),
        RegexTextTriggerValue("impazzi(to|sce|ta) totalmente".r, 20),
        RegexTextTriggerValue("a(gia[s]?){2,}".r, 7)
      ),
      List(
        MediaFile("rphjb_RottoIlCa.mp4")
      )
    ),
    ReplyBundleMessage(
      TextTrigger(
        StringTextTriggerValue("diventare papa")
      ),
      List(
        MediaFile("rphjb_DiventarePapa.mp4")
      )
    ),
    ReplyBundleMessage(
      TextTrigger(
        RegexTextTriggerValue("(c|g)hi(t|d)a[r]+is(t|d)a pi(u|ù|ú) velo(c|g)e".r, 22)
      ),
      List(
        GifFile("rphjb_Arivato.mp4")
      )
    ),
    ReplyBundleMessage(
      TextTrigger(
        RegexTextTriggerValue("\\bbeat\\b".r, 4),
        RegexTextTriggerValue("(e poi[ ,]?[ ]?){2,}".r, 10),
        StringTextTriggerValue("qualche volta vedo lei"),
        StringTextTriggerValue("sfasciavamo tutti gli strumenti"),
      ),
      List(
        MediaFile("rphjb_AssoloBeat.mp4")
      )
    ),
    ReplyBundleMessage(
      TextTrigger(
        StringTextTriggerValue("buon compleanno")
      ),
      List(
        MediaFile("rphjb_Compleanno.mp4")
      )
    ),
    ReplyBundleMessage(
      TextTrigger(
        StringTextTriggerValue("ringraziare"),
        StringTextTriggerValue("traffico")
      ),
      List(
        MediaFile("rphjb_RingraziareGianniTraffico.mp4")
      )
    ),
    ReplyBundleMessage(
      TextTrigger(
        RegexTextTriggerValue("(roba|droga) tagliata male".r, 18),
        StringTextTriggerValue("one television"),
        RegexTextTriggerValue("(un po'|un attimo) (di|de) esercitazione".r, 23)
      ),
      List(
        MediaFile("rphjb_RockMachineIntro.mp4")
      )
    ),
    ReplyBundleMessage(
      TextTrigger(
        StringTextTriggerValue("poesia")
      ),
      List(
        MediaFile("rphjb_PoesiaMadre.mp4"),
        MediaFile("rphjb_PoesiaRock.mp4"),
        MediaFile("rphjb_Blues.mp4"),
        MediaFile("rphjb_PoesiaMaria.mp4"),
        MediaFile("rphjb_PoesiaArtistiImpiegati.mp4"),
        MediaFile("rphjb_CanzonettePoesieAuschwitzCervello.mp4"),
        MediaFile("rphjb_PoesiaDirittoPaura.mp4")
      ),
      replySelection = RandomSelection
    ),
    ReplyBundleMessage(
      TextTrigger(
        RegexTextTriggerValue("w[e]+[l]+[a]+".r, 4)
      ),
      List(
        MediaFile("rphjb_WelaMyFriends.mp4")
      )
    ),
    ReplyBundleMessage(
      TextTrigger(
        StringTextTriggerValue("blues"),
        StringTextTriggerValue("da piangere"),
      ),
      List(
        MediaFile("rphjb_Blues.mp4")
      )
    ),
    ReplyBundleMessage(
      TextTrigger(
        StringTextTriggerValue("sabato sera"),
        StringTextTriggerValue("suono sporco")
      ),
      List(
        MediaFile("rphjb_DelirioDelSabatoSera.mp4")
      )
    ),
    ReplyBundleMessage(
      TextTrigger(
        StringTextTriggerValue("marilyn manson")
      ),
      List(
        MediaFile("rphjb_Ciao2001.mp4")
      )
    ),
    ReplyBundleMessage(
      TextTrigger(
        StringTextTriggerValue("petrucci"),
        RegexTextTriggerValue("capelli (lunghi|corti)".r, 13),
        RegexTextTriggerValue("(impiegato statale|impiegati statali)".r, 17),
      ),
      List(
        MediaFile("rphjb_PetrucciCapelliCorti.mp4")
      )
    ),
    ReplyBundleMessage(
      TextTrigger(
        RegexTextTriggerValue("impiegat[oi]".r, 9),
      ),
      List(
        MediaFile("rphjb_PetrucciCapelliCorti.mp4"),
        MediaFile("rphjb_PoesiaArtistiImpiegati.mp4")
      ),
      replySelection = RandomSelection
    ),
    ReplyBundleMessage(
      TextTrigger(
        StringTextTriggerValue("progressive"),
        StringTextTriggerValue("i genesis"),
        RegexTextTriggerValue("tecno(-| )thrash".r, 12),
        StringTextTriggerValue("van der graaf generator"),
        RegexTextTriggerValue("emerson, lake (e|&) palmer".r, 22),
        StringTextTriggerValue("gentle giant"),
        StringTextTriggerValue("jetro tull"),
        StringTextTriggerValue("Marillion"),
      ),
      List(
        MediaFile("rphjb_Regressive.mp4")
      )
    ),
    ReplyBundleMessage(
      TextTrigger(
        StringTextTriggerValue("cresta dell'onda"),
        StringTextTriggerValue("orlo del crollo"),
      ),
      List(
        MediaFile("rphjb_CrestaOnda.mp4")
      )
    ),
    ReplyBundleMessage(
      TextTrigger(
        StringTextTriggerValue("stronzo")
      ),
      List(
        MediaFile("rphjb_StronzoFiglioMignotta.mp4")
      )
    ),
    ReplyBundleMessage(
      TextTrigger(
        StringTextTriggerValue("biscione"),
        StringTextTriggerValue("i piatti"),
      ),
      List(
        MediaFile("rphjb_BiscionePiatti.mp4")
      )
    ),
    ReplyBundleMessage(
      TextTrigger(
        StringTextTriggerValue("non aprite quella porta")
      ),
      List(
        MediaFile("rphjb_NonApriteQuellaPorta.mp4")
      )
    ),
    ReplyBundleMessage(
      TextTrigger(
        StringTextTriggerValue("paralitico")
      ),
      List(
        MediaFile("rphjb_DanzaMacabra.mp4")
      )
    ),
    ReplyBundleMessage(
      TextTrigger(
        StringTextTriggerValue("mettetevi in ginocchio"),
        StringTextTriggerValue("nuovo messia")
      ),
      List(
        MediaFile("rphjb_MetteteviInGinocchio.mp4")
      )
    ),
    ReplyBundleMessage(
      TextTrigger(
        RegexTextTriggerValue("sigarett[ea]".r, 9)
      ),
      List(
        MediaFile("rphjb_Sigarette.mp4"),
        MediaFile("rphjb_CollaSerpeSigarettePercussionista.mp4")
      ),
      replySelection = RandomSelection
    ),
    ReplyBundleMessage(
      TextTrigger(
        StringTextTriggerValue("orecchie sensibili"),
        StringTextTriggerValue("rumore delle lacrime")
      ),
      List(
        MediaFile("rphjb_OrecchieSensibiliRumoreLacrime.mp4")
      )
    ),
    ReplyBundleMessage(
      TextTrigger(
        StringTextTriggerValue("sapere tutto"),
        StringTextTriggerValue("se non le sai le cose"),
        StringTextTriggerValue("jordan rudess"),
        StringTextTriggerValue("radio rock"),
        StringTextTriggerValue("informazioni sbagliate")
      ),
      List(
        MediaFile("rphjb_RadioRockErrori.mp4")
      )
    ),
    ReplyBundleMessage(
      TextTrigger(
        StringTextTriggerValue("uccidere")
      ),
      List(
        MediaFile("rphjb_UccidereUnaPersona.mp4")
      )
    ),
    ReplyBundleMessage(
      TextTrigger(
        StringTextTriggerValue("distruggere il proprio sesso"),
        StringTextTriggerValue("ammaestrare il dolore")
      ),
      List(
        MediaFile("rphjb_AmmaestrareIlDolore.mp4")
      )
    ),
    ReplyBundleMessage(
      TextTrigger(
        RegexTextTriggerValue("insegnante di [cg]hi[dt]arra".r, 22)
      ),
      List(
        MediaFile("rphjb_InsegnanteDiChitarraModerna.mp4")
      )
    ),
    ReplyBundleMessage(
      TextTrigger(
        StringTextTriggerValue("pellegrinaggio"),
        StringTextTriggerValue("istinti musicali"),
      ),
      List(
        MediaFile("rphjb_PellegrinaggioSimposioMetallo.mp4")
      )
    ),
    ReplyBundleMessage(
      TextTrigger(
        StringTextTriggerValue("ridicoli")
      ),
      List(
        MediaFile("rphjb_Ridicoli.mp4")
      )
    ),
    ReplyBundleMessage(
      TextTrigger(
        StringTextTriggerValue("l'unico bravo"),
        RegexTextTriggerValue("scarica d(i |')andrenalina".r, 20),
        RegexTextTriggerValue("non valgono (un cazzo|niente)".r, 18),
      ),
      List(
        MediaFile("rphjb_UnicoBravo.mp4")
      )
    ),
    ReplyBundleMessage(
      TextTrigger(
        StringTextTriggerValue("come mi aiuta"),
      ),
      List(
        MediaFile("rphjb_DubbioComeMiAiuta.mp4")
      )
    ),
    ReplyBundleMessage(
      TextTrigger(
        RegexTextTriggerValue("\\bdubbio\\b".r, 6)
      ),
      List(
        MediaFile("rphjb_DubbioComeMiAiuta.mp4"),
        MediaFile("rphjb_DubbioScantinatiGiocoRattoGatto.mp4"),
      ),
      replySelection = RandomSelection
    ),
    ReplyBundleMessage(
      TextTrigger(
        StringTextTriggerValue("scantinati"),
        RegexTextTriggerValue("gioco (io )? del gatto e (voi )? del (ratto|topo)".r, 24)
      ),
      List(MediaFile("rphjb_DubbioScantinatiGiocoRattoGatto.mp4"))
    ),
    ReplyBundleMessage(
      TextTrigger(
        StringTextTriggerValue("londra")
      ),
      List(MediaFile("rphjb_Londra.mp4"))
    ),
    ReplyBundleMessage(
      TextTrigger(
        StringTextTriggerValue("latte droga"),
        StringTextTriggerValue("solo gregge"),
        StringTextTriggerValue("gregge da discoteca"),
        StringTextTriggerValue("sputo in un bicchiere"),
      ),
      List(MediaFile("rphjb_PoveriIgnorantiLatteDrogaSoloGreggeSputo.mp4"))
    ),
    ReplyBundleMessage(
      TextTrigger(
        RegexTextTriggerValue("drogh[ae] (legger[ae]|pesant[ei])".r, 14),
        StringTextTriggerValue("ammoniaca"),
        RegexTextTriggerValue("(sprecano|allungano) le foglie".r, 18),
        StringTextTriggerValue("veleno per topi"),
        StringTextTriggerValue("borotalco")
      ),
      List(MediaFile("rphjb_DrogheLeggere.mp4"))
    ),
    ReplyBundleMessage(
      TextTrigger(
        StringTextTriggerValue("peggio cose")
      ),
      List(
        MediaFile("rphjb_Venerdi.mp4")
      )
    ),
    ReplyBundleMessage(
      TextTrigger(
        StringTextTriggerValue("albero grande"),
        RegexTextTriggerValue("anche un('| )amplificatore".r, 22),
      ),
      List(
        MediaFile("rphjb_PoesiaRock.mp4"),
      )
    ),
    ReplyBundleMessage(
      TextTrigger(
        StringTextTriggerValue("brutto vigile")
      ),
      List(
        MediaFile("rphjb_Vigile.mp4"),
      )
    ),
    ReplyBundleMessage(
      TextTrigger(
        StringTextTriggerValue("loculi"),
        StringTextTriggerValue("la pace"),
        StringTextTriggerValue("genitori"),
        StringTextTriggerValue("tarpare le ali")
      ),
      List(
        MediaFile("rphjb_TrovatoPaceGenitori.mp4"),
      )
    ),
    ReplyBundleMessage(
      TextTrigger(
        StringTextTriggerValue("io non so mai"),
        StringTextTriggerValue("buon compleanno"),
        StringTextTriggerValue("più vicino alla fine"),
        StringTextTriggerValue("hai un anno di più"),
        StringTextTriggerValue("felicitazioni"),
        StringTextTriggerValue("ma che siamo noi"),
        RegexTextTriggerValue("rumor[ie] di vetro e di metallo".r, 27)
      ),
      List(
        MediaFile("rphjb_AuguriCompleanno.mp4")
      )
    ),
    ReplyBundleMessage(
      TextTrigger(
        StringTextTriggerValue("3 minuti"),
        RegexTextTriggerValue("ti va bene cos[iì]".r, 15),
      ),
      List(
        MediaFile("rphjb_3Minuti.mp4")
      )
    ),
    ReplyBundleMessage(
      TextTrigger(
        StringTextTriggerValue("impara a sputare")
      ),
      List(
        MediaFile("rphjb_ImparaASputareMignottaSchifose.mp4")
      )
    ),
    ReplyBundleMessage(
      TextTrigger(
        StringTextTriggerValue("aiutatemi")
      ),
      List(
        MediaFile("rphjb_Aiutatemi.mp4")
      )
    ),
    ReplyBundleMessage(
      TextTrigger(
        StringTextTriggerValue("brescia")
      ),
      List(
        MediaFile("rphjb_BresciaMiPiace.mp4")
      )
    ),
    ReplyBundleMessage(
      TextTrigger(
        StringTextTriggerValue("auguri di natale"),
        StringTextTriggerValue("buon natale"),
      ),
      List(
        MediaFile("rphjb_AuguriDiNatale.mp4")
      )
    ),
    ReplyBundleMessage(
      TextTrigger(
        RegexTextTriggerValue("\\bdvd\\b".r, 3),
        StringTextTriggerValue("non si trova online"),
        StringTextTriggerValue("membrana speciale"),
        StringTextTriggerValue("cellula fotoelettrica"),
        StringTextTriggerValue("non si può inserire")
      ),
      List(
        MediaFile("rphjb_CellulaFotoelettrica.mp4")
      )
    ),
    ReplyBundleMessage(
      TextTrigger(
        StringTextTriggerValue("leon neon"),
        StringTextTriggerValue("faccia d'angelo"),
        StringTextTriggerValue("grande troia"),
      ),
      List(
        MediaFile("rphjb_LeonNeon.mp4")
      )
    ),
    ReplyBundleMessage(
      TextTrigger(
        StringTextTriggerValue("james labrie"),
        StringTextTriggerValue("james la lagna"),
        StringTextTriggerValue("gallinaceo"),
        StringTextTriggerValue("lisa dagli occhi blu"),
        StringTextTriggerValue("vibrato melodico"),
        StringTextTriggerValue("mario tessuto")
      ),
      List(
        MediaFile("rphjb_Labrie.mp4")
      )
    ),
    ReplyBundleMessage(
      TextTrigger(
        StringTextTriggerValue("non ho bisogno di consigli")
      ),
      List(
        MediaFile("rphjb_NoConsigli.mp4")
      ),
    ),
    ReplyBundleMessage(
      TextTrigger(
        StringTextTriggerValue("lo devi spiegare")
      ),
      List(
        MediaFile("rphjb_LoDeviSpiegare.mp4")
      ),
    ),
    ReplyBundleMessage(
      TextTrigger(
        StringTextTriggerValue("se non ci credi"),
        RegexTextTriggerValue("[pb]or[dt]land".r, 8),
        StringTextTriggerValue("vancuver"),
        StringTextTriggerValue("vancuva"),
      ),
      List(
        MediaFile("rphjb_PortlandVancuverFanculo.mp4")
      ),
    ),
    ReplyBundleMessage(
      TextTrigger(
        StringTextTriggerValue("tempo al tempo"),
        StringTextTriggerValue("non ne ho più"),
        StringTextTriggerValue("mercoledì"),
      ),
      List(
        MediaFile("rphjb_TempoAlTempo.mp4")
      )
    ),
    ReplyBundleMessage(
      TextTrigger(
        StringTextTriggerValue("barzelletta"),
        StringTextTriggerValue("ginecologo"),
        StringTextTriggerValue("partiti politici"),
      ),
      List(
        MediaFile("rphjb_BarzellettaPoliticaGinecologo.mp4")
      )
    ),
    ReplyBundleMessage(
      TextTrigger(
        StringTextTriggerValue("questa è una trasmissione"),
        StringTextTriggerValue("caro avvocato"),
        StringTextTriggerValue("punto di morte"),
        RegexTextTriggerValue("ti da (la carica|l'energia)".r, 15),
        StringTextTriggerValue("ritornare alla vita")
      ),
      List(
        MediaFile("rphjb_InPuntoDiMorte.mp4")
      )
    ),
    ReplyBundleMessage(
      TextTrigger(
        StringTextTriggerValue("le tecniche sono tante"),
        StringTextTriggerValue("la tecnica che piace a me"),
      ),
      List(
        MediaFile("rphjb_LeTecnicheSonoTante.mp4")
      ),
    ),
    ReplyBundleMessage(
      TextTrigger(
        StringTextTriggerValue("mi controllate dappertutto"),
        StringTextTriggerValue("perfidi lacci"),
        StringTextTriggerValue("non posso più scappare"),
      ),
      List(
        MediaFile("rphjb_PerfidiLacci.mp4")
      )
    ),
    ReplyBundleMessage(
      TextTrigger(
        StringTextTriggerValue("cortei femministi"),
        StringTextTriggerValue("amplessi macisti"),
      ),
      List(
        MediaFile("rphjb_CorteiFemministiAmplessiMacisti.mp4")
      )
    ),
    ReplyBundleMessage(
      TextTrigger(
        StringTextTriggerValue("facevo schifo"),
        StringTextTriggerValue("(ora|adesso) spacco il culo"),
      ),
      List(
        MediaFile("rphjb_FacevoSchifoOraSpacco.mp4")
      )
    ),
    ReplyBundleMessage(
      TextTrigger(
        StringTextTriggerValue("sono ingrassato"),
        StringTextTriggerValue("esigenze cinematografiche"),
      ),
      List(
        MediaFile("rphjb_IngrassatoCinema.mp4")
      )
    ),
    ReplyBundleMessage(
      TextTrigger(
        StringTextTriggerValue("parlo io")
      ),
      List(
        MediaFile("rphjb_NonMiFregaParloIo.mp4")
      )
    ),
    ReplyBundleMessage(
      TextTrigger(
        StringTextTriggerValue("sono il demonio")
      ),
      List(
        MediaFile("rphjb_SonoDemonio.mp4")
      )
    ),
    ReplyBundleMessage(
      TextTrigger(
        StringTextTriggerValue("jovanotti"),
        StringTextTriggerValue("lorenzo cherubini"),
      ),
      List(
        MediaFile("rphjb_JovanottiUltimo.mp4")
      ),
    ),
    ReplyBundleMessage(
      TextTrigger(
        StringTextTriggerValue("colpa mia"),
        RegexTextTriggerValue("mi assumo (tutte )?le responsabilità".r, 27),
      ),
      List(
        MediaFile("rphjb_TuttaColpaMia.mp4")
      )
    ),
    ReplyBundleMessage(
      TextTrigger(
        StringTextTriggerValue("i colpevoli"),
        StringTextTriggerValue("ho vinto io"),
        StringTextTriggerValue("cercato di rovinarmi"),
      ),
      List(
        MediaFile("rphjb_RovinarmiVintoIo.mp4")
      )
    ),
    ReplyBundleMessage(
      TextTrigger(
        RegexTextTriggerValue("un casino(,)? come al solito".r, 24),
        StringTextTriggerValue("quando ci sono io"),
        RegexTextTriggerValue("l'acqua è (scivolata|scesa) (de|di) sotto".r, 28)
      ),
      List(
        MediaFile("rphjb_UnCasinoComeAlSolito.mp4")
      )
    ),
    ReplyBundleMessage(
      TextTrigger(
        StringTextTriggerValue("storia vera"),
        StringTextTriggerValue("piena di bugie"),
      ),
      List(
        MediaFile("rphjb_StoriaVeraPienaBugie.mp4"),
      )
    ),
    ReplyBundleMessage(
      TextTrigger(
        StringTextTriggerValue("tra i coglioni"),
      ),
      List(
        MediaFile("rphjb_TraICoglioni.mp4"),
      )
    ),
    ReplyBundleMessage(
      TextTrigger(
        RegexTextTriggerValue("investi(re|tore)".r, 9),
        StringTextTriggerValue("zurigo"),
        StringTextTriggerValue("dubai"),
        StringTextTriggerValue("governo svizzero"),
        RegexTextTriggerValue("affar(i|isti)".r, 6),
      ),
      List(
        MediaFile("rphjb_InvestitoreGoverno.mp4"),
      )
    ),
    ReplyBundleMessage(
      TextTrigger(
        RegexTextTriggerValue("pesce avaria[dt]o".r, 14),
        StringTextTriggerValue("veramente di merda"),
      ),
      List(
        MediaFile("rphjb_PesceAvariato.mp4")
      )
    ),
    ReplyBundleMessage(
      TextTrigger(
        StringTextTriggerValue("obama"),
        StringTextTriggerValue("governo americano"),
        RegexTextTriggerValue("rock('n'| n |&)roll presidence band".r, 25),
      ),
      List(
        MediaFile("rphjb_Obama.mp4")
      )
    ),
    ReplyBundleMessage(
      TextTrigger(
        StringTextTriggerValue("piove in continuazione"),
        StringTextTriggerValue("non ce la faccio più"),
        StringTextTriggerValue("piove sempre"),
        StringTextTriggerValue("a mio nonno"),
        RegexTextTriggerValue("nipote[!]+".r, 7),
      ),
      List(
        MediaFile("rphjb_Nonno.mp4")
      )
    ),
    ReplyBundleMessage(
      TextTrigger(
        RegexTextTriggerValue("sangue (caldo )di un cavallo".r, 22),
        StringTextTriggerValue("il peso di un cervello"),
        StringTextTriggerValue("diritto alla paura"),
        StringTextTriggerValue("come una tigre"),
        StringTextTriggerValue("migliaia di animali"),
        StringTextTriggerValue("miliardi di uomini")
      ),
      List(
        MediaFile("rphjb_PoesiaDirittoPaura.mp4")
      )
    ),
    ReplyBundleMessage(
      TextTrigger(
        StringTextTriggerValue("er foco"),
        StringTextTriggerValue("in mezzo alle cosce"),
        StringTextTriggerValue("le donne mi fanno questo effetto"),
        StringTextTriggerValue("frasi inconsulte"),
        StringTextTriggerValue("ne capo ne coda"),
      ),
      List(
        MediaFile("rphjb_DonneErFoco.mp4"),
      ),
    ),
    ReplyBundleMessage(
      TextTrigger(
        StringTextTriggerValue("non sto mai male"),
        StringTextTriggerValue("febbre"),
        StringTextTriggerValue("influenza"),
        StringTextTriggerValue("raffreddore"),
        StringTextTriggerValue("straight edge"),
      ),
      List(
        MediaFile("rphjb_NonStoMaiMale.mp4"),
      )
    ),
    ReplyBundleMessage(
      TextTrigger(
        StringTextTriggerValue("libro giallo"),
        StringTextTriggerValue("pagine gialle"),
        StringTextTriggerValue("troppi personaggi"),
      ),
      List(
        MediaFile("rphjb_LibroGiallo.mp4"),
      )
    ),
    ReplyBundleMessage(
      TextTrigger(
        StringTextTriggerValue("sperma in un bicchiere"),
        StringTextTriggerValue("in onore di satana"),
        StringTextTriggerValue("mi tocca il pacco"),
        StringTextTriggerValue("consigli sul pacco"),
        RegexTextTriggerValue("umori miscelati (allo |al tuo )?sperma".r, 27),
      ),
      List(
        MediaFile("rphjb_ConsigliSulPacco.mp4"),
      )
    ),
    ReplyBundleMessage(
      TextTrigger(
        StringTextTriggerValue("rebus"),
        RegexTextTriggerValue("tocco qua\\b".r, 9),
        RegexTextTriggerValue("volt(o|are) pagina".r, 12),
      ),
      List(
        MediaFile("rphjb_Rebus.mp4")
      )
    ),
    ReplyBundleMessage(
      TextTrigger(
        StringTextTriggerValue("non c'è giudizio"),
        StringTextTriggerValue("parola fine"),
      ),
      List(
        MediaFile("rphjb_SentireOriginale.mp4")
      )
    ),
    ReplyBundleMessage(
      TextTrigger(
        StringTextTriggerValue("legati"),
        StringTextTriggerValue("spago"),
      ),
      List(
        MediaFile("rphjb_LegatiSpago.mp4")
      )
    ),
    ReplyBundleMessage(
      TextTrigger(
        StringTextTriggerValue("ci siamo sciolti"),
        StringTextTriggerValue("non l'ha capita"),
        RegexTextTriggerValue("\\bnodi\\b".r, 4),
      ),
      List(
        MediaFile("rphjb_Nodi.mp4")
      )
    ),
    ReplyBundleMessage(
      TextTrigger(
        RegexTextTriggerValue("una bella fi[cg]a".r, 14),
        StringTextTriggerValue("la fate aspettare"),
        RegexTextTriggerValue("in silenzio( dovuto)?".r, 11),
        StringTextTriggerValue("consumare dopo"),
        StringTextTriggerValue("consumare durante"),
      ),
      List(
        MediaFile("rphjb_VenerdiAppuntamentoFissoFica.mp4")
      )
    ),
    ReplyBundleMessage(
      TextTrigger(
        StringTextTriggerValue("slipknot"),
        StringTextTriggerValue("type o negative"),
        StringTextTriggerValue("morto pure"),
        StringTextTriggerValue("morti tutti"),
      ),
      List(
        MediaFile("rphjb_MortiTutti.mp4")
      )
    ),
    ReplyBundleMessage(
      TextTrigger(
        StringTextTriggerValue("amico grasso"),
        RegexTextTriggerValue("(fare la| mettersi a) dieta".r, 13),
        RegexTextTriggerValue("circa (6|sei) mesi".r, 11),
        StringTextTriggerValue("peso ideale"),
        StringTextTriggerValue("pioppo"),
        StringTextTriggerValue("zinco"),
        StringTextTriggerValue("una bara"),
      ),
      List(
        MediaFile("rphjb_StoriaAmicoGrasso.mp4")
      )
    ),
  )

}
