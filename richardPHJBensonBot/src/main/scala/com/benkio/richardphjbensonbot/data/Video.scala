package com.benkio.richardphjbensonbot.data

import cats.Applicative
import com.benkio.telegrambotinfrastructure.model.*

object Video {

  def messageRepliesVideoData[F[_]: Applicative]: List[ReplyBundleMessage[F]] = List(
    ReplyBundleMessage.textToMedia[F](
      "amici veri",
      "soldati"
    )(
      mf"rphjb_AmiciVeriVecchiSoldati.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      "gianni neri"
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
      "musica italiana",
      "tullio pane",
      "otello profazio",
      "mario lanza",
      "luciano tajoli"
    )(
      mf"rphjb_RapMusicaMelodicaListaCantanti.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      "eric clapton",
      "uo[m]+ini d'affari".r.tr(15),
      "andò in america"
    )(
      mf"rphjb_EricClaptonDrogaUominiAffari.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      "rampolli",
      "studi a boston",
      "borghesia alta",
      "idoli delle mamme",
      "figliole"
    )(
      mf"rphjb_Rampolli.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      "capelli corti",
      "giacca",
      "cravatta",
      "passaporto degli stronzi"
    )(
      mf"rphjb_RocchettariCapelliCortiGiaccaCravattaPassaportoStronzi.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      "fregat(a|ura)".r.tr(7)
    )(
      mf"rphjb_FregataFregatura.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      "si o no"
    )(
      mf"rphjb_SiONo.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      "streghe"
    )(
      mf"rphjb_Streghe.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      "(tornando|andando) (all')?indietro".r.tr(16),
      "innovazione",
      "composizione",
      "idea (nuova|fresca)".r.tr(10)
    )(
      mf"rphjb_ComposizioneIdeaFrescaInnovazioneAndareAvantiStiamoTornandoIndetro.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      "plettro",
      "vicoletto"
    )(
      mf"rphjb_ChitarraPlettroVicoletto.mp4",
      mf"rphjb_ChitarraVicolettoPlettro2.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      "diversi mondi",
      "letti sfatti"
    )(
      mf"rphjb_LettiSfattiDiversiMondi.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      "ramarro",
      "impellitteri"
    )(
      mf"rphjb_Ramarro.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      "vi dovete spaventare"
    )(
      mf"rphjb_ViDoveteSpaventare.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      "amore nello suonare",
      "uno freddo",
      "buddisti",
    )(
      mf"rphjb_AmoreSuonareFreddoBuddistiSchifoso.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      "riciclando il (suo )?peggio".r.tr(20)
    )(
      mf"rphjb_SteveVaiRiciclando.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      "già il titolo",
      "coi due punti",
      "re[a]?l illusions".r.tr(13)
    )(
      mf"rphjb_RelIllusions.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      "trattori",
      "palmizio",
      "meno c'è",
      "meno si rompe"
    )(
      mf"rphjb_Palmizio.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      "peso di un cervello"
    )(
      mf"rphjb_VitaNemicoCervello.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      "cervello pensante",
      "questa volta no",
      "stupidità incresciosa"
    )(
      mf"rphjb_CervelloPensante.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      "percussionista",
      "batterista"
    )(
      mf"rphjb_CollaSerpeSigarettePercussionista.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      "perla di pioggia",
      "dove non piove mai"
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
      "bruce kulick"
    )(
      mf"rphjb_CintaProblema.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      "sepoltura"
    )(
      mf"rphjb_SepolturaRisata.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      "colla tra serpe e serpe"
    )(
      mf"rphjb_CollaSerpe.mp4",
      mf"rphjb_CollaSerpe.mp3",
      mf"rphjb_CollaSerpeSigarettePercussionista.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      "big money"
    )(
      mf"rphjb_BigMoney.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      "in cantina"
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
      "diventare papa"
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
      "qualche volta vedo lei",
      "sfasciavamo tutti gli strumenti",
    )(
      mf"rphjb_AssoloBeat.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      "buon compleanno"
    )(
      mf"rphjb_Compleanno.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      "ringraziare",
      "traffico"
    )(
      mf"rphjb_RingraziareGianniTraffico.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      "(roba|droga) tagliata male".r.tr(18),
      "one television",
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
      "da piangere",
      "negro",
      "niente da perdere",
      "interferenze",
      "bestia offesa",
      "giudeo",
      "svastiche",
    )(
      mf"rphjb_Blues.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      "sabato sera",
      "lo sporco",
      "più pulito",
      "john travolta",
      "video didattico",
      "questo n[o]{2,}".r.tr(10),
      "fate venire le vostre (madri|mogli|fidanzate)".r.tr(27)
    )(
      mf"rphjb_DelirioDelSabatoSera.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      "marilyn manson"
    )(
      mf"rphjb_Ciao2001.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      "petrucci",
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
      "progressive",
      "i genesis",
      "tecno(-| )thrash".r.tr(12),
      "van der graaf generator",
      "emerson(,)? lake (e|&) palmer".r.tr(22),
      "gentle giant",
      "jetro tull",
      "Marillion",
    )(
      mf"rphjb_Regressive.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      "cresta dell'onda",
      "orlo del crollo",
    )(
      mf"rphjb_CrestaOnda.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      "stronzo"
    )(
      mf"rphjb_StronzoFiglioMignotta.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      "biscione",
    )(
      mf"rphjb_BiscionePiatti.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      "non aprite quella porta"
    )(
      mf"rphjb_NonApriteQuellaPorta.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      "paralitico"
    )(
      mf"rphjb_DanzaMacabra.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      "mettetevi in ginocchio",
      "nuovo messia"
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
      "orecchie sensibili",
      "rumore delle lacrime"
    )(
      mf"rphjb_OrecchieSensibiliRumoreLacrime.mp4",
      mf"rphjb_RumoreDelleLacrimeDegliAltri.mp4",
      mf"rphjb_RumoreDelleLacrimeDegliAltri2.mp4",
      mf"rphjb_RumoreDelleLacrimeDegliAltri3.mp4",
    ),
    ReplyBundleMessage.textToMedia[F](
      "sapere tutto",
      "se non le sai le cose",
      "jordan rudess",
      "radio rock",
      "informazioni sbagliate"
    )(
      mf"rphjb_RadioRockErrori.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      "uccidere"
    )(
      mf"rphjb_UccidereUnaPersona.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      "distruggere il proprio sesso",
      "ammaestrare il dolore"
    )(
      mf"rphjb_AmmaestrareIlDolore.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      "insegnante di [cg]hi[dt]arra".r.tr(22)
    )(
      mf"rphjb_InsegnanteDiChitarraModerna.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      "pellegrinaggio",
      "istinti musicali",
    )(
      mf"rphjb_PellegrinaggioSimposioMetallo.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      "ridicoli"
    )(
      mf"rphjb_Ridicoli.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      "l'unico bravo",
      "scarica d(i |')andrenalina".r.tr(20),
      "non valgono (un cazzo|niente)".r.tr(18),
    )(
      mf"rphjb_UnicoBravo.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      "come mi aiuta",
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
      "scantinati",
      "gioco (io )? del gatto e (voi )? del (ratto|topo)".r.tr(24)
    )(mf"rphjb_DubbioScantinatiGiocoRattoGatto.mp4"),
    ReplyBundleMessage.textToMedia[F](
      "londra"
    )(mf"rphjb_Londra.mp4"),
    ReplyBundleMessage.textToMedia[F](
      "latte droga",
      "solo gregge",
      "gregge da discoteca",
    )(mf"rphjb_PoveriIgnorantiLatteDrogaSoloGreggeSputo.mp4"),
    ReplyBundleMessage.textToMedia[F](
      "drogh[ae] (legger[ae]|pesant[ei])".r.tr(14),
      "ammoniaca",
      "(sprecano|allungano) le foglie".r.tr(18),
      "veleno per topi",
      "borotalco"
    )(mf"rphjb_DrogheLeggere.mp4"),
    ReplyBundleMessage.textToMedia[F](
      "peggio cose"
    )(
      mf"rphjb_Venerdi.mp4",
      mf"rphjb_DanzaMacabra.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      "albero grande",
      "anche un('| )amplificatore".r.tr(22),
    )(
      mf"rphjb_PoesiaRock.mp4",
    ),
    ReplyBundleMessage.textToMedia[F](
      "brutto vigile"
    )(
      mf"rphjb_Vigile.mp4",
    ),
    ReplyBundleMessage.textToMedia[F](
      "loculi",
      "la pace",
      "genitori",
      "tarpare le ali"
    )(
      mf"rphjb_TrovatoPaceGenitori.mp4",
    ),
    ReplyBundleMessage.textToMedia[F](
      "io non so mai",
      "buon compleanno",
      "più vicino alla fine",
      "hai un anno di più",
      "felicitazioni",
      "ma che siamo noi",
      "rumor[ie] di vetro e di metallo".r.tr(27)
    )(
      mf"rphjb_AuguriCompleanno.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      "3 minuti",
      "ti va bene cos[iì]".r.tr(15),
    )(
      mf"rphjb_3Minuti.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      "impara a sputare"
    )(
      mf"rphjb_ImparaASputareMignottaSchifose.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      "aiutatemi"
    )(
      mf"rphjb_Aiutatemi.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      "brescia"
    )(
      mf"rphjb_BresciaMiPiace.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      "\\bdvd\\b".r.tr(3),
      "non si trova online",
      "membrana speciale",
      "cellula fotoelettrica",
      "non si può inserire"
    )(
      mf"rphjb_CellulaFotoelettrica.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      "leon neon",
      "faccia d'angelo",
      "grande troia",
    )(
      mf"rphjb_LeonNeon.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      "james labrie",
      "james la lagna",
      "gallinaceo",
      "lisa dagli occhi blu",
      "vibrato melodico",
      "mario tessuto"
    )(
      mf"rphjb_Labrie.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      "non ho bisogno di consigli"
    )(
      mf"rphjb_NoConsigli.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      "lo devi spiegare"
    )(
      mf"rphjb_LoDeviSpiegare.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      "se non ci credi",
      "[pb]or[dt]land".r.tr(8),
      "vancuver",
      "vancuva",
    )(
      mf"rphjb_PortlandVancuverFanculo.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      "tempo al tempo",
      "non ne ho più"
    )(
      mf"rphjb_TempoAlTempo.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      "barzelletta",
      "ginecologo",
      "partiti politici",
    )(
      mf"rphjb_BarzellettaPoliticaGinecologo.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      "questa è una trasmissione",
      "caro avvocato",
      "punto di morte",
      "ti da (la carica|l'energia)".r.tr(15),
      "ritornare alla vita"
    )(
      mf"rphjb_InPuntoDiMorte.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      "le tecniche sono tante",
      "la tecnica che piace a me",
    )(
      mf"rphjb_LeTecnicheSonoTante.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      "mi controllate dappertutto",
      "perfidi lacci",
      "non posso più scappare",
    )(
      mf"rphjb_PerfidiLacci.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      "cortei femministi",
      "amplessi macisti",
    )(
      mf"rphjb_CorteiFemministiAmplessiMacisti.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      "facevo schifo",
      "(ora|adesso) spacco il culo",
    )(
      mf"rphjb_FacevoSchifoOraSpacco.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      "sono ingrassato",
      "esigenze cinematografiche",
    )(
      mf"rphjb_IngrassatoCinema.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      "parlo io"
    )(
      mf"rphjb_NonMiFregaParloIo.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      "sono il demonio"
    )(
      mf"rphjb_SonoDemonio.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      "jovanotti",
      "lorenzo cherubini",
    )(
      mf"rphjb_JovanottiUltimo.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      "colpa mia",
      "mi assumo (tutte )?le responsabilità".r.tr(27),
    )(
      mf"rphjb_TuttaColpaMia.mp4",
      mf"rphjb_MiaColpaColpaMia.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      "i colpevoli",
      "ho vinto io",
      "cercato di rovinarmi",
    )(
      mf"rphjb_RovinarmiVintoIo.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      "un casino(,)? come al solito".r.tr(24),
      "quando ci sono io",
      "l'acqua è (scivolata|scesa) (de|di) sotto".r.tr(28)
    )(
      mf"rphjb_UnCasinoComeAlSolito.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      "storia vera",
      "piena di bugie",
    )(
      mf"rphjb_StoriaVeraPienaBugie.mp4",
    ),
    ReplyBundleMessage.textToMedia[F](
      "tra i coglioni",
    )(
      mf"rphjb_TraICoglioni.mp4",
    ),
    ReplyBundleMessage.textToMedia[F](
      "investi(re|tore)".r.tr(9),
      "zurigo",
      "dubai",
      "governo svizzero",
      "affar(i|isti)".r.tr(6),
    )(
      mf"rphjb_InvestitoreGoverno.mp4",
    ),
    ReplyBundleMessage.textToMedia[F](
      "pesce avaria[dt]o".r.tr(14),
      "veramente di merda",
    )(
      mf"rphjb_PesceAvariato.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      "obama",
      "governo americano",
      "rock('n'| n |&)roll presidence band".r.tr(25),
    )(
      mf"rphjb_Obama.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      "piove in continuazione",
      "non ce la faccio più",
      "piove sempre",
      "a mio nonno",
      "nipote[!]+".r.tr(7),
    )(
      mf"rphjb_Nonno.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      "sangue (caldo )di un cavallo".r.tr(22),
      "il peso di un cervello",
      "diritto alla paura",
      "come una tigre",
      "migliaia di animali",
      "miliardi di uomini"
    )(
      mf"rphjb_PoesiaDirittoPaura.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      "er foco",
      "in mezzo alle cosce",
      "le donne mi fanno questo effetto",
      "frasi inconsulte",
      "ne capo ne coda",
    )(
      mf"rphjb_DonneErFoco.mp4",
    ),
    ReplyBundleMessage.textToMedia[F](
      "non sto mai male",
      "febbre",
      "influenza",
      "raffreddore",
      "straight edge",
    )(
      mf"rphjb_NonStoMaiMale.mp4",
    ),
    ReplyBundleMessage.textToMedia[F](
      "libro giallo",
      "pagine gialle",
      "troppi personaggi",
    )(
      mf"rphjb_LibroGiallo.mp4",
    ),
    ReplyBundleMessage.textToMedia[F](
      "sperma in un bicchiere",
      "in onore di satana",
      "mi tocca il pacco",
      "consigli sul pacco",
      "umori miscelati (allo |al tuo )?sperma".r.tr(27),
    )(
      mf"rphjb_ConsigliSulPacco.mp4",
    ),
    ReplyBundleMessage.textToMedia[F](
      "rebus",
      "tocco qua\\b".r.tr(9),
      "volt(o|are) pagina".r.tr(12),
    )(
      mf"rphjb_Rebus.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      "non c'è giudizio",
      "parola fine",
    )(
      mf"rphjb_GiudizioParolaFine.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      "spago",
    )(
      mf"rphjb_LegatiSpago.mp4",
      mf"rphjb_UnitiQualeSpago.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      "ci siamo sciolti",
      "non l'ha capita",
      "\\bnodi\\b".r.tr(4),
    )(
      mf"rphjb_Nodi.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      "una bella fi[cg]a".r.tr(14),
      "la fate aspettare",
      "in silenzio( dovuto)?".r.tr(11),
      "consumare dopo",
      "consumare durante",
    )(
      mf"rphjb_VenerdiAppuntamentoFissoFica.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      "slipknot",
      "type o negative",
      "morto pure",
      "morti tutti",
    )(
      mf"rphjb_MortiTutti.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      "amico grasso",
      "(fare la| mettersi a) dieta".r.tr(13),
      "circa (6|sei) mesi".r.tr(11),
      "peso ideale",
      "pioppo",
      "zinco",
      "una bara",
    )(
      mf"rphjb_StoriaAmicoGrasso.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      "dylan (dog|thomas)".r.tr(9),
      "poeti maledetti",
      "un po' gay",
      "la (collezione|colazione)".r.tr(12),
      "t[ei] sei sbagliato".r.tr(12),
    )(
      mf"rphjb_DylanDog.mp4",
    ),
    ReplyBundleMessage.textToMedia[F](
      "buckethead",
      "guns (n|n'|and) roses".r.tr(12),
    )(
      mf"rphjb_BucketheadGunsNRoses.mp4",
    ),
    ReplyBundleMessage.textToMedia[F](
      "signor (jones|jonz|jons|gionz)".r.tr(11),
      "janet",
      "coniglio"
    )(
      mf"rphjb_StoriaSignorGionz.mp4",
    ),
    ReplyBundleMessage.textToMedia[F](
      "brooklyn",
      "carne morta",
      "manhattan",
      "cane da guerra",
    )(
      mf"rphjb_PrimoSbaglio.mp4",
    ),
    ReplyBundleMessage.textToMedia[F](
      "sulla punta della lingua",
      "agisse da sola",
      "che me lo (in)?presti".r.tr(16),
      "cani al cimitero",
      "solo uomini",
      "nemmeno una donna"
    )(
      mf"rphjb_CaniAlCimitero.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      "anche la rabbia ha un cuore"
    )(
      mf"rphjb_AncheLaRabbiaHaUnCuore.mp4",
      mf"rphjb_AncheLaRabbiaHaUnCuore2.mp4",
      mf"rphjb_AncheLaRabbiaHaUnCuore3.mp4",
      mf"rphjb_AncheLaRabbiaHaUnCuore4.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      "trovamelo"
    )(
      mf"rphjb_AngeloTrovamelo.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      "(due|2) orecchie".r.tr(10),
      "(una|1) bocca".r.tr(6)
    )(
      mf"rphjb_2orecchie1Bocca.mp4",
      mf"rphjb_2orecchie1Bocca2.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      "ramazzotti"
    )(
      mf"rphjb_SteveVaiRamazzotti.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      "limitazioni dell'uomo",
      "limitazioni della donna"
    )(
      mf"rphjb_BicchiereSputoLimitazioniUomoDonna.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      "sputo in un bicchiere",
    )(
      mf"rphjb_PoveriIgnorantiLatteDrogaSoloGreggeSputo.mp4",
      mf"rphjb_BicchiereSputoLimitazioniUomoDonna.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      "sfuggono",
      "\\bpols[io]\\b".r.tr(5),
      "\\borolog[io]\\b".r.tr(7)
    )(
      gif"rphjb_QuattroSolo.mp4",
      mf"rphjb_4SoloTempiInTestaOrologiSfuggonoPolsi.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      "cambia canale"
    )(
      mf"rphjb_CambiaCanaleBruttoFrocio.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      "garage",
      "varazze",
      "\\banima\\b".r.tr(5),
    )(
      mf"rphjb_AnimaGarageVarazze.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      "arbitri (truccati|pagati)".r.tr(14),
      "giocatori dopati",
      "(gioco del|il) calcio".r.tr(9),
      "moggi",
      "direttore del coni",
      "(una|na) farsa".r.tr(8)
    )(
      mf"rphjb_ArbitriPagatiTruccatiGiocatoriDopatiMoggiCONITifosiUltrasTuttaFarsaGiocoCalcio.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      "incompetente"
    )(
      mf"rphjb_PerfettoIncompetente.mp4"
    ),
    ReplyBundleMessage.textToMedia[F]("sono (proprio )?un coglione".r.tr(16))(mf"rphjb_SonoCoglione.mp4"),
    ReplyBundleMessage.textToMedia[F]("sta male", "canzoni di natale")(
      mf"rphjb_CanzoniNataleStavaMaleMalmsteen.mp4"
    ),
    ReplyBundleMessage
      .textToMedia[F]("yngwie", "malmsteen")(
        mf"rphjb_Ramarro.mp4",
        mf"rphjb_CanzoniNataleStavaMaleMalmsteen.mp4",
        mf"rphjb_BarzellettaGesuCristoParadisoPurgatorioMalmsteenDio.mp4"
      ),
    ReplyBundleMessage.textToMedia[F]("invece no", "si ricomincia", "da capo")(
      mf"rphjb_InveceNoRicominciaDaCapo.mp4"
    ),
    ReplyBundleMessage.textToMedia[F]("allora parlo")(mf"rphjb_AlloraParlo.mp4"),
    ReplyBundleMessage.textToMedia[F]("da paura")(mf"rphjb_DaPaura.mp4"),
    ReplyBundleMessage.textToMedia[F](
      "cipresso",
      "guardando il padrone",
      "all'ombra",
      "aspett(a|ando|are)".r.tr(7)
    )(mf"rphjb_CaneOmbraCipressoPadroneMortoIcaniPiangono.mp4"),
    ReplyBundleMessage.textToMedia[F]("pride")(mf"rphjb_BarzellettaPapaSonoGayPride.mp4"),
    ReplyBundleMessage
      .textToMedia[F]("gay")(mf"rphjb_BarzellettaPapaSonoGayPride.mp4", mf"rphjb_CantantePreferitoNonSonoGaio.mp4"),
    ReplyBundleMessage.textToMedia[F]("barzelletta")(
      mf"rphjb_BarzellettaGesuCristoParadisoPurgatorioMalmsteenDio.mp4",
      mf"rphjb_BarzellettaPapaSonoGayPride.mp4"
    ),
    ReplyBundleMessage.textToMedia[F]("\\brbo\\b".r.tr(3), "cazzò", "(5|cinque) livelli".r.tr(9))(mf"rphjb_RBO.mp4"),
    ReplyBundleMessage.textToMedia[F]("effettivamente")(mf"rphjb_Effettivamente.mp4"),
    ReplyBundleMessage.textToMedia[F]("tigre")(mf"rphjb_LaTigre.mp4"),
    ReplyBundleMessage
      .textToMedia[F]("che gruppo", "m[ei] ricorda".r.tr(10))(mf"rphjb_CheGruppoMiRicordaRisata.mp4"),
    ReplyBundleMessage.textToMedia[F]("il ciano", "luciano")(mf"rphjb_IlCiano.mp4"),
    ReplyBundleMessage.textToMedia[F](
      "volta il cervello",
      "principi veneziani",
      "decaduti",
      "rimorti",
      "rinati"
    )(
      mf"rphjb_CheStoDicendoDiVoltaIlCervelloPrincipiVeneziani.mp4"
    ),
    ReplyBundleMessage.textToMedia[F]("gaio")(mf"rphjb_CantantePreferitoNonSonoGaio.mp4"),
    ReplyBundleMessage.textToMedia[F]("come si fa")(mf"rphjb_ComeSiFaItaliaIgnorante.mp4"),
    ReplyBundleMessage.textToMedia[F]("commissionato", "trovare")(mf"rphjb_CommissionatoMeLoDeviTrovare.mp4"),
    ReplyBundleMessage.textToMedia[F]("col sangue")(mf"rphjb_ColSangue.mp4"),
    ReplyBundleMessage.textToMedia[F]("vergognassero", "giornali")(
      mf"rphjb_ChitarreVergognateviSchifosiGiornaliMerda.mp4"
    ),
    ReplyBundleMessage.textToMedia[F]("echo")(
      mf"rphjb_CarrellataInfernaleDelirioPureNellaGolaTrasmissioneDaUrloEchoDelayVitaNaturalDurante.mp4",
      mf"rphjb_CattedraleCanterburyRavennaEcho.mp4"
    ),
    ReplyBundleMessage.textToMedia[F]("trasmissione da urlo", "delay", "vita natural durante")(
      mf"rphjb_CarrellataInfernaleDelirioPureNellaGolaTrasmissioneDaUrloEchoDelayVitaNaturalDurante.mp4"
    ),
    ReplyBundleMessage.textToMedia[F]("cantante")(
      mf"rphjb_CantantePreferitoNonSonoGaio.mp4",
      mf"rphjb_CantanteDonnaVoceDaUomoDueBocceCosiInnamoratoPerdutamente.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      "due bocce",
      "innamorato",
      "veronica frieman",
      "benedictum"
    )(mf"rphjb_CantanteDonnaVoceDaUomoDueBocceCosiInnamoratoPerdutamente.mp4"),
    ReplyBundleMessage
      .textToMedia[F]("verita", "verità")(mf"rphjb_AltraCazzataVeritaSembranoCazzate.mp4", mf"rphjb_Verita.mp4"),
    ReplyBundleMessage.textToMedia[F](
      "litfiba",
      "piero pelù",
      "ghigo renzulli",
      "\\bpuzz[oi]\\b".r.tr(5),
      "completamente fro(ci|sh)o".r.tr(20)
    )(mf"rphjb_PuzzoGhigoRenzulliPieroPeluFrocio.mp4"),
    ReplyBundleMessage.textToMedia[F]("fammelo avere", "al pi[ùu] presto".r.tr(13))(
      mf"rphjb_FammeloAvereAlPiuPresto.mp4"
    ),
    ReplyBundleMessage.textToMedia[F]("avvertire", "in guardia", "scelte giuste")(
      mf"rphjb_AvvertireMettereInGuardiaAiutareScelteGiuste.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      "abbellimenti",
      "mordenti",
      "rivolti",
      "rivoli",
      "impennate",
      "colori"
    )(
      mf"rphjb_AbbellimentiRivoltiRivoliMordentiImpennateColori.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      "cattegrale",
      "canterbury",
      "ravenna"
    )(
      mf"rphjb_CattedraleCanterburyRavennaEcho.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      "vestito (nuovo|vecchio)".r.tr(13),
      "(rammenda|rappezza|rattoppa|ricuci)".r.tr(6)
    )(
      mf"rphjb_CompriVestitoNuovoRammendaVecchio.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      "cugini di campagna",
      "giardino dei semplici",
      "homo sapiens",
      "gli osanna",
      "james senese",
      "elio d'anna",
      "(di|il) liscio".r.tr(9),
      "(di|il) rumba".r.tr(8),
      "cha[ ]?cha[ ]?cha".r.tr(9),
      "canzon[ei] napoletan[ae]".r.tr(18)
    )(
      mf"rphjb_CuginiCampagnaGiardinoSempliciHomoSapiensLiscioRumbaChaChaChaCanzoneNapoletanaOsanna.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      "cuore affogato",
      "affogato nel metallo",
      "alla ricerca",
      "in cerca",
      "ferisca nel cervello"
    )(
      mf"rphjb_CuoreAffogatoNelMetalloCercaCanzoneFeriscaCervello.mp4",
      mf"rphjb_CuoreAffogatoNelMetalloRicercaCanzoneFeriscaNelCervello.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      "stratovarius",
      "metallica",
      "ultimo (disco|album)".r.tr(13)
    )(
      mf"rphjb_DeludendoQuasiTutto.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      "donna (che merita|forte)".r.tr(11),
      "profilo (fisico|intellettuale)".r.tr(14),
      "simile a me",
      "versione donna"
    )(
      mf"rphjb_DonnaMerita.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      "alpheus",
      "whiskey",
      "mi sentivo (di fare)?così".r.tr(15),
      "ho fatto così"
    )(
      mf"rphjb_EtichetteSulleBottiglieDiWhiskeyAlpheus.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      "queen",
      "follia"
    )(
      mf"rphjb_FolliaQueenNo.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      "ghent",
      "albania"
    )(
      mf"rphjb_GhentPiattiAlbania.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      "piatti",
    )(
      mf"rphjb_BiscionePiatti.mp4",
      mf"rphjb_GhentPiattiAlbania.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      "amicizia",
      "amico del cuore",
      "migliore amico",
      "nella merda",
      "subliminali"
    )(
      mf"rphjb_AmicoDelCuoreLasciatoNellaMerdaParoleSubliminaliPoesiaAmiciziaVera.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      "battezzato"
    )(
      mf"rphjb_Blues.mp4",
      mf"rphjb_Battesimo.mp3"
    ),
    ReplyBundleMessage.textToMedia[F](
      "ebbe un bambino",
      "blues"
    )(
      mf"rphjb_Blues.mp4",
      mf"rphjb_BluesEbbeBambinoRockNRollBeBopALulaStormyMondayButTuesdayIsJustAsBad.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      "monday",
      "tuesday"
    )(
      mf"rphjb_BluesEbbeBambinoRockNRollBeBopALulaStormyMondayButTuesdayIsJustAsBad.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      "ultimo degli ultimi"
    )(
      mf"rphjb_DanzaMacabra.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      "di tutti i colori",
      "tramonto d'estate",
      "boschi in penombra",
      "per un ideale"
    )(
      mf"rphjb_PoesiaMadre.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      "difficile guardare",
      "tubo catodico",
      "(vedere|guardare) l[aà]".r.tr(9)
    )(
      mf"rphjb_DifficileGuardareTuboCatodicoNienteCiSepara.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      "negri"
    )(
      mf"rphjb_DueNegriMostruosi.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      "eccoci qu[aà]".r.tr(10),
      "io sto l[aà]\\b".r.tr(9),
      "dove (ca[z]+[o]?)?sto".r.tr(8),
      "sto d[ie] (qu[aà]|l[aà])".r.tr(9),
      "sto l[iì]\\b".r.tr(6),
      "luce[tta]? rossa".r.tr(10)
    )(
      mf"rphjb_EccociQuaStoLaDoCazzoStoDiQuaDiLaLiDavantiConTeLucettaRossa.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      "etichette"
    )(
      mf"rphjb_EtichetteSupermercatoSputatiMondo.mp4",
      mf"rphjb_EtichetteSulleBottiglieDiWhiskeyAlpheus.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      "supermercato",
      "sputati nel mondo",
      "gli altri siamo noi"
    )(
      mf"rphjb_EtichetteSupermercatoSputatiMondo.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      "tendetemi le vostre spire"
    )(
      mf"rphjb_GerarchieInfernali2.mp4",
      mf"rphjb_GerarchieInfernali3.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      "uno da bere",
      "sputatemi dalle vostre labbra",
      "figlie(,)? ma di quale madre".r.tr(24),
      "fetenti feti di fede",
      "che ti inganna di notte",
      "che muore di giorno",
      "rovistandoti nell'immondo"
    )(
      mf"rphjb_GerarchieInfernali2.mp4"
    )
  )
}
