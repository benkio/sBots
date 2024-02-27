package com.benkio.richardphjbensonbot.data

import cats.Applicative
import com.benkio.telegrambotinfrastructure.model.*

object Video {

  def messageRepliesVideoData[F[_]: Applicative]: List[ReplyBundleMessage[F]] = List(
    ReplyBundleMessage.textToMedia[F](
      stt"amici veri",
      stt"soldati"
    )(
      mf"rphjb_AmiciVeriVecchiSoldati.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      stt"gianni neri"
    )(
      mf"rphjb_RingraziareGianniTraffico.mp4",
      mf"rphjb_GianniNeriCoppiaMiciciale.mp4",
      mf"rphjb_GianniNeriCheFineHaiFatto.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      "accor(data|dana)".r.tr(9)
    )(
      mf"rphjb_Accordana.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      "\\brap\\b".r.tr(3),
      stt"musica italiana",
      stt"tullio pane",
      stt"otello profazio",
      stt"mario lanza",
      stt"gianni celeste",
      stt"luciano tajoli"
    )(
      mf"rphjb_RapMusicaMelodicaListaCantanti.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      stt"eric clapton",
      "uo[m]+ini d'affari".r.tr(15),
    )(
      mf"rphjb_EricClaptonDrogaUominiAffari.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      stt"rampolli",
      stt"studi a boston",
      stt"borghesia alta",
      stt"idoli delle mamme",
      stt"figliole"
    )(
      mf"rphjb_Rampolli.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      stt"capelli corti",
      stt"giacca",
      stt"cravatta",
      stt"passaporto degli stronzi"
    )(
      mf"rphjb_RocchettariCapelliCortiGiaccaCravattaPassaportoStronzi.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      "fregat(a|ura)".r.tr(7)
    )(
      mf"rphjb_FregataFregatura.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      stt"si o no"
    )(
      mf"rphjb_SiONo.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      stt"streghe"
    )(
      mf"rphjb_Streghe.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      "(tornando|andando) (all')?indietro".r.tr(16),
      stt"innovazione"
    )(
      mf"rphjb_InnovazioneStiamoTornandoIndietro.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      stt"plettro",
      stt"vicoletto"
    )(
      mf"rphjb_ChitarraPlettroVicoletto.mp4",
      mf"rphjb_ChitarraPlettroVicoletto2.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      stt"diversi mondi",
      stt"letti sfatti"
    )(
      mf"rphjb_LettiSfattiDiversiMondi.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      stt"ramarro",
      stt"yngwie",
      stt"malmsteen",
      stt"impellitteri"
    )(
      mf"rphjb_Ramarro.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      stt"vi dovete spaventare"
    )(
      mf"rphjb_ViDoveteSpaventare.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      stt"amore nello suonare",
      stt"uno freddo",
      stt"buddisti",
    )(
      mf"rphjb_AmoreSuonareFreddoBuddistiSchifoso.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      "riciclando il (suo )?peggio".r.tr(20)
    )(
      mf"rphjb_SteveVaiRiciclando.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      stt"già il titolo",
      stt"coi due punti",
      "re[a]?l illusions".r.tr(13)
    )(
      mf"rphjb_RelIllusions.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      stt"trattori",
      stt"palmizio",
      stt"meno c'è",
      stt"meno si rompe"
    )(
      mf"rphjb_Palmizio.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      stt"peso di un cervello"
    )(
      mf"rphjb_VitaNemicoCervello.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      stt"cervello pensante",
      stt"questa volta no",
      stt"stupidità incresciosa"
    )(
      mf"rphjb_CervelloPensante.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      stt"percussionista",
      stt"batterista"
    )(
      mf"rphjb_CollaSerpeSigarettePercussionista.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      stt"perla di pioggia",
      stt"dove non piove mai"
    )(
      mf"rphjb_PerlaDiPioggia.mp4",
      mf"rphjb_AlbizziePerlaPioggia.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      "[l]+[i]+[b]+[e]+[r]+[i]+".r.tr(6)
    )(
      mf"rphjb_Liberi.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      "\\bcinta\\b".r.tr(5),
      stt"bruce kulick"
    )(
      mf"rphjb_CintaProblema.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      stt"sepoltura"
    )(
      mf"rphjb_SepolturaRisata.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      stt"colla tra serpe e serpe"
    )(
      mf"rphjb_CollaSerpe.mp4",
      mf"rphjb_CollaSerpe.mp3",
      mf"rphjb_CollaSerpeSigarettePercussionista.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      stt"big money"
    )(
      mf"rphjb_BigMoney.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      stt"in cantina"
    )(
      mf"rphjb_InCantina.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      "(mi|me) so(n|no)? rotto il ca\\b".r.tr(17),
      "impazzi(to|sce|ta) totalmente".r.tr(20),
      "a(gia[s]?){2,}".r.tr(7)
    )(
      mf"rphjb_RottoIlCa.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      stt"diventare papa"
    )(
      mf"rphjb_DiventarePapa.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      "(c|g)hi(t|d)a[r]+is(t|d)a pi(u|ù|ú) velo(c|g)e".r.tr(22)
    )(
      GifFile("rphjb_Arivato.mp4")
    ),
    ReplyBundleMessage.textToMedia[F](
      "\\bbeat\\b".r.tr(4),
      "(e poi[ ,]?[ ]?){2,}".r.tr(10),
      stt"qualche volta vedo lei",
      stt"sfasciavamo tutti gli strumenti",
    )(
      mf"rphjb_AssoloBeat.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      stt"buon compleanno"
    )(
      mf"rphjb_Compleanno.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      stt"ringraziare",
      stt"traffico"
    )(
      mf"rphjb_RingraziareGianniTraffico.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      "(roba|droga) tagliata male".r.tr(18),
      stt"one television",
      "(un po'|un attimo) (di|de) esercitazione".r.tr(23)
    )(
      mf"rphjb_RockMachineIntro.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      "w[e]+[l]+[a]+".r.tr(4)
    )(
      mf"rphjb_WelaMyFriends.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      stt"blues",
      stt"da piangere",
    )(
      mf"rphjb_Blues.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      stt"sabato sera",
      stt"suono sporco"
    )(
      mf"rphjb_DelirioDelSabatoSera.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      stt"marilyn manson"
    )(
      mf"rphjb_Ciao2001.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      stt"petrucci",
      "capelli (lunghi|corti)".r.tr(13),
      "(impiegato statale|impiegati statali)".r.tr(17),
    )(
      mf"rphjb_PetrucciCapelliCorti.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      "impiegat[oi]".r.tr(9),
    )(
      mf"rphjb_PetrucciCapelliCorti.mp4",
      mf"rphjb_PoesiaArtistiImpiegati.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      stt"progressive",
      stt"i genesis",
      "tecno(-| )thrash".r.tr(12),
      stt"van der graaf generator",
      "emerson, lake (e|&) palmer".r.tr(22),
      stt"gentle giant",
      stt"jetro tull",
      stt"Marillion",
    )(
      mf"rphjb_Regressive.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      stt"cresta dell'onda",
      stt"orlo del crollo",
    )(
      mf"rphjb_CrestaOnda.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      stt"stronzo"
    )(
      mf"rphjb_StronzoFiglioMignotta.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      stt"biscione",
      stt"i piatti",
    )(
      mf"rphjb_BiscionePiatti.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      stt"non aprite quella porta"
    )(
      mf"rphjb_NonApriteQuellaPorta.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      stt"paralitico"
    )(
      mf"rphjb_DanzaMacabra.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      stt"mettetevi in ginocchio",
      stt"nuovo messia"
    )(
      mf"rphjb_MetteteviInGinocchio.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      "sigarett[ea]".r.tr(9)
    )(
      mf"rphjb_Sigarette.mp4",
      mf"rphjb_CollaSerpeSigarettePercussionista.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      stt"orecchie sensibili",
      stt"rumore delle lacrime"
    )(
      mf"rphjb_OrecchieSensibiliRumoreLacrime.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      stt"sapere tutto",
      stt"se non le sai le cose",
      stt"jordan rudess",
      stt"radio rock",
      stt"informazioni sbagliate"
    )(
      mf"rphjb_RadioRockErrori.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      stt"uccidere"
    )(
      mf"rphjb_UccidereUnaPersona.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      stt"distruggere il proprio sesso",
      stt"ammaestrare il dolore"
    )(
      mf"rphjb_AmmaestrareIlDolore.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      "insegnante di [cg]hi[dt]arra".r.tr(22)
    )(
      mf"rphjb_InsegnanteDiChitarraModerna.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      stt"pellegrinaggio",
      stt"istinti musicali",
    )(
      mf"rphjb_PellegrinaggioSimposioMetallo.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      stt"ridicoli"
    )(
      mf"rphjb_Ridicoli.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      stt"l'unico bravo",
      "scarica d(i |')andrenalina".r.tr(20),
      "non valgono (un cazzo|niente)".r.tr(18),
    )(
      mf"rphjb_UnicoBravo.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      stt"come mi aiuta",
    )(
      mf"rphjb_DubbioComeMiAiuta.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      "\\bdubbio\\b".r.tr(6)
    )(
      mf"rphjb_DubbioComeMiAiuta.mp4",
      mf"rphjb_DubbioScantinatiGiocoRattoGatto.mp4",
    ),
    ReplyBundleMessage.textToMedia[F](
      stt"scantinati",
      "gioco (io )? del gatto e (voi )? del (ratto|topo)".r.tr(24)
    )(mf"rphjb_DubbioScantinatiGiocoRattoGatto.mp4"),
    ReplyBundleMessage.textToMedia[F](
      stt"londra"
    )(mf"rphjb_Londra.mp4"),
    ReplyBundleMessage.textToMedia[F](
      stt"latte droga",
      stt"solo gregge",
      stt"gregge da discoteca",
      stt"sputo in un bicchiere",
    )(mf"rphjb_PoveriIgnorantiLatteDrogaSoloGreggeSputo.mp4"),
    ReplyBundleMessage.textToMedia[F](
      "drogh[ae] (legger[ae]|pesant[ei])".r.tr(14),
      stt"ammoniaca",
      "(sprecano|allungano) le foglie".r.tr(18),
      stt"veleno per topi",
      stt"borotalco"
    )(mf"rphjb_DrogheLeggere.mp4"),
    ReplyBundleMessage.textToMedia[F](
      stt"peggio cose"
    )(
      mf"rphjb_Venerdi.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      stt"albero grande",
      "anche un('| )amplificatore".r.tr(22),
    )(
      mf"rphjb_PoesiaRock.mp4",
    ),
    ReplyBundleMessage.textToMedia[F](
      stt"brutto vigile"
    )(
      mf"rphjb_Vigile.mp4",
    ),
    ReplyBundleMessage.textToMedia[F](
      stt"loculi",
      stt"la pace",
      stt"genitori",
      stt"tarpare le ali"
    )(
      mf"rphjb_TrovatoPaceGenitori.mp4",
    ),
    ReplyBundleMessage.textToMedia[F](
      stt"io non so mai",
      stt"buon compleanno",
      stt"più vicino alla fine",
      stt"hai un anno di più",
      stt"felicitazioni",
      stt"ma che siamo noi",
      "rumor[ie] di vetro e di metallo".r.tr(27)
    )(
      mf"rphjb_AuguriCompleanno.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      stt"3 minuti",
      "ti va bene cos[iì]".r.tr(15),
    )(
      mf"rphjb_3Minuti.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      stt"impara a sputare"
    )(
      mf"rphjb_ImparaASputareMignottaSchifose.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      stt"aiutatemi"
    )(
      mf"rphjb_Aiutatemi.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      stt"brescia"
    )(
      mf"rphjb_BresciaMiPiace.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      "\\bdvd\\b".r.tr(3),
      stt"non si trova online",
      stt"membrana speciale",
      stt"cellula fotoelettrica",
      stt"non si può inserire"
    )(
      mf"rphjb_CellulaFotoelettrica.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      stt"leon neon",
      stt"faccia d'angelo",
      stt"grande troia",
    )(
      mf"rphjb_LeonNeon.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      stt"james labrie",
      stt"james la lagna",
      stt"gallinaceo",
      stt"lisa dagli occhi blu",
      stt"vibrato melodico",
      stt"mario tessuto"
    )(
      mf"rphjb_Labrie.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      stt"non ho bisogno di consigli"
    )(
      mf"rphjb_NoConsigli.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      stt"lo devi spiegare"
    )(
      mf"rphjb_LoDeviSpiegare.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      stt"se non ci credi",
      "[pb]or[dt]land".r.tr(8),
      stt"vancuver",
      stt"vancuva",
    )(
      mf"rphjb_PortlandVancuverFanculo.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      stt"tempo al tempo",
      stt"non ne ho più",
      stt"mercoledì",
    )(
      mf"rphjb_TempoAlTempo.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      stt"barzelletta",
      stt"ginecologo",
      stt"partiti politici",
    )(
      mf"rphjb_BarzellettaPoliticaGinecologo.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      stt"questa è una trasmissione",
      stt"caro avvocato",
      stt"punto di morte",
      "ti da (la carica|l'energia)".r.tr(15),
      stt"ritornare alla vita"
    )(
      mf"rphjb_InPuntoDiMorte.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      stt"le tecniche sono tante",
      stt"la tecnica che piace a me",
    )(
      mf"rphjb_LeTecnicheSonoTante.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      stt"mi controllate dappertutto",
      stt"perfidi lacci",
      stt"non posso più scappare",
    )(
      mf"rphjb_PerfidiLacci.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      stt"cortei femministi",
      stt"amplessi macisti",
    )(
      mf"rphjb_CorteiFemministiAmplessiMacisti.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      stt"facevo schifo",
      stt"(ora|adesso) spacco il culo",
    )(
      mf"rphjb_FacevoSchifoOraSpacco.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      stt"sono ingrassato",
      stt"esigenze cinematografiche",
    )(
      mf"rphjb_IngrassatoCinema.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      stt"parlo io"
    )(
      mf"rphjb_NonMiFregaParloIo.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      stt"sono il demonio"
    )(
      mf"rphjb_SonoDemonio.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      stt"jovanotti",
      stt"lorenzo cherubini",
    )(
      mf"rphjb_JovanottiUltimo.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      stt"colpa mia",
      "mi assumo (tutte )?le responsabilità".r.tr(27),
    )(
      mf"rphjb_TuttaColpaMia.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      stt"i colpevoli",
      stt"ho vinto io",
      stt"cercato di rovinarmi",
    )(
      mf"rphjb_RovinarmiVintoIo.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      "un casino(,)? come al solito".r.tr(24),
      stt"quando ci sono io",
      "l'acqua è (scivolata|scesa) (de|di) sotto".r.tr(28)
    )(
      mf"rphjb_UnCasinoComeAlSolito.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      stt"storia vera",
      stt"piena di bugie",
    )(
      mf"rphjb_StoriaVeraPienaBugie.mp4",
    ),
    ReplyBundleMessage.textToMedia[F](
      stt"tra i coglioni",
    )(
      mf"rphjb_TraICoglioni.mp4",
    ),
    ReplyBundleMessage.textToMedia[F](
      "investi(re|tore)".r.tr(9),
      stt"zurigo",
      stt"dubai",
      stt"governo svizzero",
      "affar(i|isti)".r.tr(6),
    )(
      mf"rphjb_InvestitoreGoverno.mp4",
    ),
    ReplyBundleMessage.textToMedia[F](
      "pesce avaria[dt]o".r.tr(14),
      stt"veramente di merda",
    )(
      mf"rphjb_PesceAvariato.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      stt"obama",
      stt"governo americano",
      "rock('n'| n |&)roll presidence band".r.tr(25),
    )(
      mf"rphjb_Obama.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      stt"piove in continuazione",
      stt"non ce la faccio più",
      stt"piove sempre",
      stt"a mio nonno",
      "nipote[!]+".r.tr(7),
    )(
      mf"rphjb_Nonno.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      "sangue (caldo )di un cavallo".r.tr(22),
      stt"il peso di un cervello",
      stt"diritto alla paura",
      stt"come una tigre",
      stt"migliaia di animali",
      stt"miliardi di uomini"
    )(
      mf"rphjb_PoesiaDirittoPaura.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      stt"er foco",
      stt"in mezzo alle cosce",
      stt"le donne mi fanno questo effetto",
      stt"frasi inconsulte",
      stt"ne capo ne coda",
    )(
      mf"rphjb_DonneErFoco.mp4",
    ),
    ReplyBundleMessage.textToMedia[F](
      stt"non sto mai male",
      stt"febbre",
      stt"influenza",
      stt"raffreddore",
      stt"straight edge",
    )(
      mf"rphjb_NonStoMaiMale.mp4",
    ),
    ReplyBundleMessage.textToMedia[F](
      stt"libro giallo",
      stt"pagine gialle",
      stt"troppi personaggi",
    )(
      mf"rphjb_LibroGiallo.mp4",
    ),
    ReplyBundleMessage.textToMedia[F](
      stt"sperma in un bicchiere",
      stt"in onore di satana",
      stt"mi tocca il pacco",
      stt"consigli sul pacco",
      "umori miscelati (allo |al tuo )?sperma".r.tr(27),
    )(
      mf"rphjb_ConsigliSulPacco.mp4",
    ),
    ReplyBundleMessage.textToMedia[F](
      stt"rebus",
      "tocco qua\\b".r.tr(9),
      "volt(o|are) pagina".r.tr(12),
    )(
      mf"rphjb_Rebus.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      stt"non c'è giudizio",
      stt"parola fine",
    )(
      mf"rphjb_GiudizioParolaFine.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      stt"legati",
      stt"spago",
    )(
      mf"rphjb_LegatiSpago.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      stt"ci siamo sciolti",
      stt"non l'ha capita",
      "\\bnodi\\b".r.tr(4),
    )(
      mf"rphjb_Nodi.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      "una bella fi[cg]a".r.tr(14),
      stt"la fate aspettare",
      "in silenzio( dovuto)?".r.tr(11),
      stt"consumare dopo",
      stt"consumare durante",
    )(
      mf"rphjb_VenerdiAppuntamentoFissoFica.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      stt"slipknot",
      stt"type o negative",
      stt"morto pure",
      stt"morti tutti",
    )(
      mf"rphjb_MortiTutti.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      stt"amico grasso",
      "(fare la| mettersi a) dieta".r.tr(13),
      "circa (6|sei) mesi".r.tr(11),
      stt"peso ideale",
      stt"pioppo",
      stt"zinco",
      stt"una bara",
    )(
      mf"rphjb_StoriaAmicoGrasso.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      "dylan (dog|thomas)".r.tr(9),
      stt"poeti maledetti",
      stt"un po' gay",
      "la (collezione|colazione)".r.tr(12),
      "t[ei] sei sbagliato".r.tr(12),
    )(
      mf"rphjb_DylanDog.mp4",
    ),
    ReplyBundleMessage.textToMedia[F](
      stt"buckethead",
      "guns (n|n'|and) roses".r.tr(12),
    )(
      mf"rphjb_BucketheadGunsNRoses.mp4",
    ),
    ReplyBundleMessage.textToMedia[F](
      "signor (jones|jonz|jons|gionz)".r.tr(11),
      stt"janet",
      stt"coniglio"
    )(
      mf"rphjb_StoriaSignorGionz.mp4",
    ),
    ReplyBundleMessage.textToMedia[F](
      stt"brooklyn",
      stt"carne morta",
      stt"manhattan",
      stt"cane da guerra",
    )(
      mf"rphjb_PrimoSbaglio.mp4",
    ),
    ReplyBundleMessage.textToMedia[F](
      stt"sulla punta della lingua",
      stt"agisse da sola",
      "che me lo (in)?presti".r.tr(16),
      stt"cani al cimitero",
      stt"solo uomini",
      stt"nemmeno una donna"
    )(
      mf"rphjb_CaniAlCimitero.mp4"
    )
  )

}
