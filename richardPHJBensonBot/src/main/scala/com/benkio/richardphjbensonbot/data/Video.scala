package com.benkio.richardphjbensonbot.data

import cats.Applicative
import com.benkio.telegrambotinfrastructure.model.*

object Video {

  def messageRepliesVideoData[F[_]: Applicative]: List[ReplyBundleMessage[F]] = List(
    ReplyBundleMessage.textToVideo[F](
      "amici veri",
      "soldati"
    )(
      vid"rphjb_AmiciVeriVecchiSoldati.mp4"
    ),
    ReplyBundleMessage.textToVideo[F](
      "gianni neri"
    )(
      vid"rphjb_RingraziareGianniTraffico.mp4",
      vid"rphjb_GianniNeriCoppiaMiciciale.mp4",
      vid"rphjb_GianniNeriCheFineHaiFatto.mp4",
      vid"rphjb_MigliorAmicoCoppiaMicidialeGianniNeri.mp4"
    ),
    ReplyBundleMessage.textToVideo[F](
      "accor(data|dana)".r.tr(9)
    )(
      vid"rphjb_Accordana.mp4"
    ),
    ReplyBundleMessage.textToVideo[F](
      "\\brap\\b".r.tr(3),
      "musica italiana",
      "tullio pane",
      "otello profazio",
      "mario lanza",
      "luciano tajoli"
    )(
      vid"rphjb_RapMusicaMelodicaListaCantanti.mp4"
    ),
    ReplyBundleMessage.textToVideo[F](
      "eric clapton",
      "uo[m]+ini d'affari".r.tr(15),
      "andò in america"
    )(
      vid"rphjb_EricClaptonDrogaUominiAffari.mp4"
    ),
    ReplyBundleMessage.textToVideo[F](
      "rampolli",
      "studi a boston",
      "borghesia alta",
      "idoli delle mamme",
      "figliole"
    )(
      vid"rphjb_Rampolli.mp4"
    ),
    ReplyBundleMessage.textToVideo[F](
      "capelli corti",
      "giacca",
      "cravatta",
      "passaporto degli stronzi"
    )(
      vid"rphjb_RocchettariCapelliCortiGiaccaCravattaPassaportoStronzi.mp4"
    ),
    ReplyBundleMessage.textToVideo[F](
      "fregat(a|ura)".r.tr(7),
      "canaro",
      "magliana"
    )(
      vid"rphjb_FregataFregatura.mp4"
    ),
    ReplyBundleMessage.textToVideo[F](
      "si o no"
    )(
      vid"rphjb_SiONo.mp4"
    ),
    ReplyBundleMessage.textToVideo[F](
      "streghe"
    )(
      vid"rphjb_Streghe.mp4"
    ),
    ReplyBundleMessage.textToVideo[F](
      "(tornando|andando) (all')?indietro".r.tr(16),
      "innovazione",
      "composizione",
      "idea (nuova|fresca)".r.tr(10)
    )(
      vid"rphjb_ComposizioneIdeaFrescaInnovazioneAndareAvantiStiamoTornandoIndetro.mp4"
    ),
    ReplyBundleMessage.textToVideo[F](
      "plettro",
    )(
      vid"rphjb_ChitarraPlettroVicoletto.mp4",
      vid"rphjb_ChitarraVicolettoPlettro2.mp4",
      vid"rphjb_CollaSerpe.mp4"
    ),
    ReplyBundleMessage.textToVideo[F](
      "vicoletto",
    )(
      vid"rphjb_ChitarraPlettroVicoletto.mp4",
      vid"rphjb_ChitarraVicolettoPlettro2.mp4"
    ),
    ReplyBundleMessage.textToVideo[F](
      "diversi mondi",
      "letti sfatti"
    )(
      vid"rphjb_LettiSfattiDiversiMondi.mp4"
    ),
    ReplyBundleMessage.textToVideo[F](
      "ramarro",
      "impellitteri"
    )(
      vid"rphjb_Ramarro.mp4"
    ),
    ReplyBundleMessage.textToVideo[F](
      "vi dovete spaventare"
    )(
      vid"rphjb_ViDoveteSpaventare.mp4"
    ),
    ReplyBundleMessage.textToVideo[F](
      "amore nello suonare",
      "uno freddo",
      "buddisti",
    )(
      vid"rphjb_AmoreSuonareFreddoBuddistiSchifoso.mp4"
    ),
    ReplyBundleMessage.textToVideo[F](
      "riciclando"
    )(
      vid"rphjb_SteveVaiRiciclando.mp4"
    ),
    ReplyBundleMessage.textToVideo[F](
      "già il titolo",
      "coi due punti",
      "re[a]?l illusions".r.tr(13)
    )(
      vid"rphjb_RelIllusions.mp4"
    ),
    ReplyBundleMessage.textToVideo[F](
      "trattori",
      "palmizio",
      "meno c'è",
      "meno si rompe"
    )(
      vid"rphjb_Palmizio.mp4"
    ),
    ReplyBundleMessage.textToVideo[F](
      "peso di un cervello"
    )(
      vid"rphjb_VitaNemicoCervello.mp4"
    ),
    ReplyBundleMessage.textToVideo[F](
      "cervello pensante",
      "stupidità incresciosa"
    )(
      vid"rphjb_CervelloPensante.mp4"
    ),
    ReplyBundleMessage.textToVideo[F](
      "percussionista",
      "batterista"
    )(
      vid"rphjb_CollaSerpeSigarettePercussionista.mp4",
      vid"rphjb_TommyAldridgeYngwieMalmsteenWhereAreYouGoing.mp4"
    ),
    ReplyBundleMessage.textToVideo[F](
      "perla di pioggia",
      "dove non piove mai"
    )(
      vid"rphjb_PerlaDiPioggia.mp4",
      vid"rphjb_AlbizziePerlaPioggia.mp4"
    ),
    ReplyBundleMessage.textToVideo[F](
      "[l]+[i]+[b]+[e]+[r]+[i]+".r.tr(6)
    )(
      vid"rphjb_Liberi.mp4"
    ),
    ReplyBundleMessage.textToVideo[F](
      "\\bcinta\\b".r.tr(5),
      "bruce kulick"
    )(
      vid"rphjb_CintaProblema.mp4"
    ),
    ReplyBundleMessage.textToVideo[F](
      "sepoltura"
    )(
      vid"rphjb_SepolturaRisata.mp4"
    ),
    ReplyBundleMessage.textToVideo[F](
      "big money"
    )(
      vid"rphjb_BigMoney.mp4"
    ),
    ReplyBundleMessage.textToVideo[F](
      "in cantina"
    )(
      vid"rphjb_InCantina.mp4"
    ),
    ReplyBundleMessage.textToVideo[F](
      "(mi|me) so(n|no)? rotto il ca\\b".r.tr(17),
      "impazzi(to|sce|ta) totalmente".r.tr(20),
      "a(gia[s]?){2,}".r.tr(7)
    )(
      vid"rphjb_RottoIlCa.mp4"
    ),
    ReplyBundleMessage.textToVideo[F](
      "diventare papa"
    )(
      vid"rphjb_DiventarePapa.mp4"
    ),
    ReplyBundleMessage.textToVideo[F](
      "(c|g)hi(t|d)a[r]+is(t|d)a pi(u|ù|ú) velo(c|g)e".r.tr(22)
    )(
      vid"rphjb_Arivato.mp4"
    ),
    ReplyBundleMessage.textToVideo[F](
      "\\bbeat\\b".r.tr(4),
      "(e poi[ ,]?[ ]?){2,}".r.tr(10),
      "qualche volta vedo lei",
      "sfasciavamo tutti gli strumenti",
    )(
      vid"rphjb_AssoloBeat.mp4"
    ),
    ReplyBundleMessage.textToVideo[F](
      "buon compleanno"
    )(
      vid"rphjb_Compleanno.mp4"
    ),
    ReplyBundleMessage.textToVideo[F](
      "ringraziare",
      "traffico"
    )(
      vid"rphjb_RingraziareGianniTraffico.mp4"
    ),
    ReplyBundleMessage.textToVideo[F](
      "(roba|droga) tagliata male".r.tr(18),
      "one television",
      "(un po'|un attimo) (di|de) esercitazione".r.tr(23)
    )(
      vid"rphjb_RockMachineIntro.mp4"
    ),
    ReplyBundleMessage.textToVideo[F](
      "w[e]+[l]+[a]+".r.tr(4)
    )(
      vid"rphjb_WelaMyFriends.mp4",
      vid"rphjb_WelaHeyHeyHeyDiNuovoInsieme.mp4"
    ),
    ReplyBundleMessage.textToVideo[F](
      "da piangere",
      "negro",
      "niente da perdere",
      "interferenze",
      "bestia offesa",
      "giudeo",
      "svastiche",
    )(
      vid"rphjb_Blues.mp4"
    ),
    ReplyBundleMessage.textToVideo[F](
      "sabato sera",
      "lo sporco",
      "più pulito",
      "john travolta",
      "video didattico",
      "questo n[o]{2,}".r.tr(10),
      "fate venire le vostre (madri|mogli|fidanzate)".r.tr(27)
    )(
      vid"rphjb_DelirioDelSabatoSera.mp4"
    ),
    ReplyBundleMessage.textToVideo[F](
      "marilyn manson"
    )(
      vid"rphjb_Ciao2001.mp4"
    ),
    ReplyBundleMessage.textToVideo[F](
      "petrucci",
      "capelli (lunghi|corti)".r.tr(13),
      "(impiegato statale|impiegati statali)".r.tr(17),
    )(
      vid"rphjb_PetrucciCapelliCorti.mp4"
    ),
    ReplyBundleMessage.textToVideo[F](
      "impiegat[oi]".r.tr(9),
    )(
      vid"rphjb_PetrucciCapelliCorti.mp4",
      vid"rphjb_PoesiaArtistiImpiegati.mp4"
    ),
    ReplyBundleMessage.textToVideo[F](
      "progressive",
      "i genesis",
      "tecno(-| )thrash".r.tr(12),
      "van der graaf generator",
      "emerson(,)? lake (e|&) palmer".r.tr(22),
      "gentle giant",
      "jetro tull",
      "Marillion",
    )(
      vid"rphjb_Regressive.mp4"
    ),
    ReplyBundleMessage.textToVideo[F](
      "cresta dell'onda",
      "orlo del crollo",
    )(
      vid"rphjb_CrestaOnda.mp4"
    ),
    ReplyBundleMessage.textToVideo[F](
      "stronzo"
    )(
      vid"rphjb_StronzoFiglioMignotta.mp4"
    ),
    ReplyBundleMessage.textToVideo[F](
      "biscione",
    )(
      vid"rphjb_BiscionePiatti.mp4"
    ),
    ReplyBundleMessage.textToVideo[F](
      "non aprite quella porta"
    )(
      vid"rphjb_NonApriteQuellaPorta.mp4"
    ),
    ReplyBundleMessage.textToVideo[F](
      "paralitico"
    )(
      vid"rphjb_DanzaMacabra.mp4"
    ),
    ReplyBundleMessage.textToVideo[F](
      "mettetevi in ginocchio",
      "nuovo messia"
    )(
      vid"rphjb_MetteteviInGinocchio.mp4"
    ),
    ReplyBundleMessage.textToVideo[F](
      "sigarett[ea]".r.tr(9)
    )(
      vid"rphjb_Sigarette.mp4",
      vid"rphjb_CollaSerpeSigarettePercussionista.mp4"
    ),
    ReplyBundleMessage.textToVideo[F](
      "orecchie sensibili",
      "rumore delle lacrime"
    )(
      vid"rphjb_OrecchieSensibiliRumoreLacrime.mp4",
      vid"rphjb_RumoreDelleLacrimeDegliAltri.mp4",
      vid"rphjb_RumoreDelleLacrimeDegliAltri2.mp4",
      vid"rphjb_RumoreDelleLacrimeDegliAltri3.mp4",
    ),
    ReplyBundleMessage.textToVideo[F](
      "sapere tutto",
      "se non le sai le cose",
      "jordan rudess",
      "radio rock",
      "informazioni sbagliate"
    )(
      vid"rphjb_RadioRockErrori.mp4"
    ),
    ReplyBundleMessage.textToVideo[F](
      "uccidere"
    )(
      vid"rphjb_UccidereUnaPersona.mp4"
    ),
    ReplyBundleMessage.textToVideo[F](
      "distruggere il proprio sesso",
      "ammaestrare il dolore"
    )(
      vid"rphjb_AmmaestrareIlDolore.mp4"
    ),
    ReplyBundleMessage.textToVideo[F](
      "insegnante di [cg]hi[dt]arra".r.tr(22)
    )(
      vid"rphjb_InsegnanteDiChitarraModerna.mp4"
    ),
    ReplyBundleMessage.textToVideo[F](
      "pellegrinaggio",
      "istinti musicali",
    )(
      vid"rphjb_PellegrinaggioSimposioMetallo.mp4"
    ),
    ReplyBundleMessage.textToVideo[F](
      "ridicoli"
    )(
      vid"rphjb_Ridicoli.mp4"
    ),
    ReplyBundleMessage.textToVideo[F](
      "l'unico bravo",
      "scarica d(i |')andrenalina".r.tr(20),
      "non valgono (un cazzo|niente)".r.tr(18),
    )(
      vid"rphjb_UnicoBravo.mp4"
    ),
    ReplyBundleMessage.textToVideo[F](
      "come mi aiuta",
    )(
      vid"rphjb_DubbioComeMiAiuta.mp4"
    ),
    ReplyBundleMessage.textToVideo[F](
      "\\bdubbio\\b".r.tr(6)
    )(
      vid"rphjb_DubbioComeMiAiuta.mp4",
      vid"rphjb_DubbioScantinatiGiocoRattoGatto.mp4",
    ),
    ReplyBundleMessage.textToVideo[F](
      "scantinati",
      "gioco (io )? del gatto e (voi )? del (ratto|topo)".r.tr(24)
    )(vid"rphjb_DubbioScantinatiGiocoRattoGatto.mp4"),
    ReplyBundleMessage.textToVideo[F](
      "londra"
    )(vid"rphjb_Londra.mp4"),
    ReplyBundleMessage.textToVideo[F](
      "latte droga",
      "solo gregge",
      "gregge da discoteca",
    )(vid"rphjb_PoveriIgnorantiLatteDrogaSoloGreggeSputo.mp4"),
    ReplyBundleMessage.textToVideo[F](
      "drogh[ae] (legger[ae]|pesant[ei])".r.tr(14),
      "ammoniaca",
      "(sprecano|allungano) le foglie".r.tr(18),
      "veleno per topi",
      "borotalco"
    )(vid"rphjb_DrogheLeggere.mp4"),
    ReplyBundleMessage.textToVideo[F](
      "peggio cose"
    )(
      vid"rphjb_Venerdi.mp4",
      vid"rphjb_DanzaMacabra.mp4"
    ),
    ReplyBundleMessage.textToVideo[F](
      "albero grande",
      "anche un('| )amplificatore".r.tr(22),
    )(
      vid"rphjb_PoesiaRock.mp4",
    ),
    ReplyBundleMessage.textToVideo[F](
      "brutto vigile"
    )(
      vid"rphjb_Vigile.mp4",
    ),
    ReplyBundleMessage.textToVideo[F](
      "loculi",
      "la pace",
      "tarpare le ali"
    )(
      vid"rphjb_TrovatoPaceGenitori.mp4",
    ),
    ReplyBundleMessage.textToVideo[F](
      "io non so mai",
      "buon compleanno",
      "più vicino alla fine",
      "hai un anno di più",
      "felicitazioni",
      "ma che siamo noi",
      "rumor[ie] di vetro e di metallo".r.tr(27)
    )(
      vid"rphjb_AuguriCompleanno.mp4"
    ),
    ReplyBundleMessage.textToVideo[F](
      "3 minuti",
      "ti va bene cos[iì]".r.tr(15),
    )(
      vid"rphjb_3Minuti.mp4"
    ),
    ReplyBundleMessage.textToVideo[F](
      "impara a sputare"
    )(
      vid"rphjb_ImparaASputareMignottaSchifose.mp4"
    ),
    ReplyBundleMessage.textToVideo[F](
      "aiutatemi"
    )(
      vid"rphjb_Aiutatemi.mp4"
    ),
    ReplyBundleMessage.textToVideo[F](
      "brescia"
    )(
      vid"rphjb_BresciaMiPiace.mp4"
    ),
    ReplyBundleMessage.textToVideo[F](
      "\\bdvd\\b".r.tr(3),
      "non si trova online",
      "membrana speciale",
      "cellula fotoelettrica",
      "non si può inserire"
    )(
      vid"rphjb_CellulaFotoelettrica.mp4"
    ),
    ReplyBundleMessage.textToVideo[F](
      "leon neon",
      "faccia d'angelo",
      "grande troia",
    )(
      vid"rphjb_LeonNeon.mp4"
    ),
    ReplyBundleMessage.textToVideo[F](
      "james labrie",
      "james la lagna",
      "gallinaceo",
      "lisa dagli occhi blu",
      "vibrato melodico",
      "mario tessuto",
      "disco solistico"
    )(
      vid"rphjb_Labrie.mp4"
    ),
    ReplyBundleMessage.textToVideo[F](
      "non ho bisogno di consigli"
    )(
      vid"rphjb_NoConsigli.mp4"
    ),
    ReplyBundleMessage.textToVideo[F](
      "lo devi spiegare"
    )(
      vid"rphjb_LoDeviSpiegare.mp4"
    ),
    ReplyBundleMessage.textToVideo[F](
      "se non ci credi",
      "[pb]or[dt]land".r.tr(8),
      "vancuver",
      "vancuva",
    )(
      vid"rphjb_PortlandVancuverFanculo.mp4"
    ),
    ReplyBundleMessage.textToVideo[F](
      "tempo al tempo",
      "non ne ho più"
    )(
      vid"rphjb_TempoAlTempo.mp4"
    ),
    ReplyBundleMessage.textToVideo[F](
      "barzelletta",
      "ginecologo",
      "partiti politici",
    )(
      vid"rphjb_BarzellettaPoliticaGinecologo.mp4"
    ),
    ReplyBundleMessage.textToVideo[F](
      "questa è una trasmissione",
      "caro avvocato",
      "punto di morte",
      "ti da (la carica|l'energia)".r.tr(15),
      "ritornare alla vita"
    )(
      vid"rphjb_InPuntoDiMorte.mp4"
    ),
    ReplyBundleMessage.textToVideo[F](
      "le tecniche sono tante",
      "la tecnica che piace a me",
    )(
      vid"rphjb_LeTecnicheSonoTante.mp4"
    ),
    ReplyBundleMessage.textToVideo[F](
      "mi controllate dappertutto",
      "perfidi lacci",
      "non posso più scappare",
    )(
      vid"rphjb_PerfidiLacci.mp4"
    ),
    ReplyBundleMessage.textToVideo[F](
      "cortei femministi",
      "amplessi macisti",
    )(
      vid"rphjb_CorteiFemministiAmplessiMacisti.mp4"
    ),
    ReplyBundleMessage.textToVideo[F](
      "facevo schifo",
      "(ora|adesso) spacco il culo",
    )(
      vid"rphjb_FacevoSchifoOraSpacco.mp4"
    ),
    ReplyBundleMessage.textToVideo[F](
      "sono ingrassato",
      "esigenze cinematografiche",
    )(
      vid"rphjb_IngrassatoCinema.mp4"
    ),
    ReplyBundleMessage.textToVideo[F](
      "parlo io"
    )(
      vid"rphjb_NonMiFregaParloIo.mp4"
    ),
    ReplyBundleMessage.textToVideo[F](
      "sono il demonio"
    )(
      vid"rphjb_SonoDemonio.mp4"
    ),
    ReplyBundleMessage.textToVideo[F](
      "jovanotti",
      "lorenzo cherubini",
    )(
      vid"rphjb_JovanottiUltimo.mp4"
    ),
    ReplyBundleMessage.textToVideo[F](
      "colpa mia",
      "mi assumo (tutte )?le responsabilità".r.tr(27),
    )(
      vid"rphjb_TuttaColpaMia.mp4",
      vid"rphjb_MiaColpaColpaMia.mp4"
    ),
    ReplyBundleMessage.textToVideo[F](
      "i colpevoli",
      "ho vinto io",
      "cercato di rovinarmi",
    )(
      vid"rphjb_RovinarmiVintoIo.mp4"
    ),
    ReplyBundleMessage.textToVideo[F](
      "un casino(,)? come al solito".r.tr(24),
      "quando ci sono io",
      "l'acqua è (scivolata|scesa) (de|di) sotto".r.tr(28)
    )(
      vid"rphjb_UnCasinoComeAlSolito.mp4"
    ),
    ReplyBundleMessage.textToVideo[F](
      "storia vera",
      "piena di bugie",
    )(
      vid"rphjb_StoriaVeraPienaBugie.mp4",
    ),
    ReplyBundleMessage.textToVideo[F](
      "tra i coglioni",
    )(
      vid"rphjb_TraICoglioni.mp4",
    ),
    ReplyBundleMessage.textToVideo[F](
      "investi(re|tore)".r.tr(9),
      "zurigo",
      "dubai",
      "governo svizzero",
      "affar(i|isti)".r.tr(6),
    )(
      vid"rphjb_InvestitoreGoverno.mp4",
    ),
    ReplyBundleMessage.textToVideo[F](
      "pesce avaria[dt]o".r.tr(14),
      "veramente di merda",
    )(
      vid"rphjb_PesceAvariato.mp4"
    ),
    ReplyBundleMessage.textToVideo[F](
      "governo americano",
      "rock('n'| n |&)roll presidence band".r.tr(25),
      "sax"
    )(
      vid"rphjb_Obama.mp4"
    ),
    ReplyBundleMessage.textToVideo[F](
      "piove in continuazione",
      "non ce la faccio più",
      "piove sempre",
      "a mio nonno",
      "nipote[!]+".r.tr(7),
    )(
      vid"rphjb_Nonno.mp4"
    ),
    ReplyBundleMessage.textToVideo[F](
      "sangue (caldo )di un cavallo".r.tr(22),
      "il peso di un cervello",
      "diritto alla paura",
      "come una tigre",
      "migliaia di animali",
      "miliardi di uomini"
    )(
      vid"rphjb_PoesiaDirittoPaura.mp4"
    ),
    ReplyBundleMessage.textToVideo[F](
      "er foco",
      "in mezzo alle cosce",
      "le donne mi fanno questo effetto",
      "frasi inconsulte",
      "ne capo ne coda",
    )(
      vid"rphjb_DonneErFoco.mp4",
    ),
    ReplyBundleMessage.textToVideo[F](
      "non sto mai male",
      "febbre",
      "influenza",
      "raffreddore",
      "straight edge",
    )(
      vid"rphjb_NonStoMaiMale.mp4",
    ),
    ReplyBundleMessage.textToVideo[F](
      "libro giallo",
      "pagine gialle",
      "troppi personaggi",
    )(
      vid"rphjb_LibroGiallo.mp4",
    ),
    ReplyBundleMessage.textToVideo[F](
      "sperma in un bicchiere",
      "in onore di satana",
      "mi tocca il pacco",
      "consigli sul pacco",
      "umori miscelati (allo |al tuo )?sperma".r.tr(27),
    )(
      vid"rphjb_ConsigliSulPacco.mp4",
    ),
    ReplyBundleMessage.textToVideo[F](
      "rebus",
      "tocco qua\\b".r.tr(9),
      "volt(o|are) pagina".r.tr(12),
    )(
      vid"rphjb_Rebus.mp4"
    ),
    ReplyBundleMessage.textToVideo[F](
      "non c'è giudizio",
      "parola fine",
    )(
      vid"rphjb_GiudizioParolaFine.mp4"
    ),
    ReplyBundleMessage.textToVideo[F](
      "spago",
    )(
      vid"rphjb_LegatiSpago.mp4",
      vid"rphjb_UnitiQualeSpago.mp4"
    ),
    ReplyBundleMessage.textToVideo[F](
      "ci siamo sciolti",
      "non l'ha capita",
      "\\bnodi\\b".r.tr(4),
    )(
      vid"rphjb_Nodi.mp4"
    ),
    ReplyBundleMessage.textToVideo[F](
      "una bella fi[cg]a".r.tr(14),
      "la fate aspettare",
      "in silenzio( dovuto)?".r.tr(11),
      "consumare dopo",
      "consumare durante",
    )(
      vid"rphjb_VenerdiAppuntamentoFissoFica.mp4"
    ),
    ReplyBundleMessage.textToVideo[F](
      "slipknot",
      "type o negative",
      "morto pure",
      "morti tutti",
    )(
      vid"rphjb_MortiTutti.mp4"
    ),
    ReplyBundleMessage.textToVideo[F](
      "amico grasso",
      "(fare la| mettersi a) dieta".r.tr(13),
      "circa (6|sei) mesi".r.tr(11),
      "peso ideale",
      "pioppo",
      "zinco",
      "una bara",
    )(
      vid"rphjb_StoriaAmicoGrasso.mp4"
    ),
    ReplyBundleMessage.textToVideo[F](
      "dylan (dog|thomas)".r.tr(9),
      "poeti maledetti",
      "un po' gay",
      "la (collezione|colazione)".r.tr(12),
      "t[ei] sei sbagliato".r.tr(12),
    )(
      vid"rphjb_DylanDog.mp4",
    ),
    ReplyBundleMessage.textToVideo[F](
      "buckethead",
      "guns (n|n'|and) roses".r.tr(12),
    )(
      vid"rphjb_BucketheadGunsNRoses.mp4",
    ),
    ReplyBundleMessage.textToVideo[F](
      "signor (jones|jonz|jons|gionz)".r.tr(11),
      "janet",
      "coniglio"
    )(
      vid"rphjb_StoriaSignorGionz.mp4",
    ),
    ReplyBundleMessage.textToVideo[F](
      "brooklyn",
      "carne morta",
      "manhattan",
      "cane da guerra",
    )(
      vid"rphjb_PrimoSbaglio.mp4",
    ),
    ReplyBundleMessage.textToVideo[F](
      "sulla punta della lingua",
      "agisse da sola",
      "che me lo (in)?presti".r.tr(16),
      "cani al cimitero",
      "solo uomini",
      "nemmeno una donna"
    )(
      vid"rphjb_CaniAlCimitero.mp4"
    ),
    ReplyBundleMessage.textToVideo[F](
      "anche la rabbia ha un cuore"
    )(
      vid"rphjb_AncheLaRabbiaHaUnCuore.mp4",
      vid"rphjb_AncheLaRabbiaHaUnCuore2.mp4",
      vid"rphjb_AncheLaRabbiaHaUnCuore3.mp4",
      vid"rphjb_AncheLaRabbiaHaUnCuore4.mp4"
    ),
    ReplyBundleMessage.textToVideo[F](
      "trovamelo"
    )(
      vid"rphjb_AngeloTrovamelo.mp4"
    ),
    ReplyBundleMessage.textToVideo[F](
      "(due|2) orecchie".r.tr(10),
      "(una|1) bocca".r.tr(6)
    )(
      vid"rphjb_2orecchie1Bocca.mp4",
      vid"rphjb_2orecchie1Bocca2.mp4"
    ),
    ReplyBundleMessage.textToVideo[F](
      "ramazzotti"
    )(
      vid"rphjb_SteveVaiRamazzotti.mp4"
    ),
    ReplyBundleMessage.textToVideo[F](
      "limitazioni dell'uomo",
      "limitazioni della donna"
    )(
      vid"rphjb_BicchiereSputoLimitazioniUomoDonna.mp4"
    ),
    ReplyBundleMessage.textToVideo[F](
      "sputo in un bicchiere",
    )(
      vid"rphjb_PoveriIgnorantiLatteDrogaSoloGreggeSputo.mp4",
      vid"rphjb_BicchiereSputoLimitazioniUomoDonna.mp4"
    ),
    ReplyBundleMessage.textToVideo[F](
      "sfuggono",
      "\\bpols[io]\\b".r.tr(5),
      "\\borolog[io]\\b".r.tr(7)
    )(
      vid"rphjb_4SoloTempiInTestaOrologiSfuggonoPolsi.mp4"
    ),
    ReplyBundleMessage.textToVideo[F](
      "cambia canale"
    )(
      vid"rphjb_CambiaCanaleBruttoFrocio.mp4"
    ),
    ReplyBundleMessage.textToVideo[F](
      "garage",
      "varazze",
      "\\banima\\b".r.tr(5),
    )(
      vid"rphjb_AnimaGarageVarazze.mp4"
    ),
    ReplyBundleMessage.textToVideo[F](
      "arbitri (truccati|pagati)".r.tr(14),
      "giocatori dopati",
      "(gioco del|il) calcio".r.tr(9),
      "\\bmoggi\\b".r.tr(5),
      "direttore del coni",
      "(una|na) farsa".r.tr(8)
    )(
      vid"rphjb_ArbitriPagatiTruccatiGiocatoriDopatiMoggiCONITifosiUltrasTuttaFarsaGiocoCalcio.mp4"
    ),
    ReplyBundleMessage.textToVideo[F](
      "incompetente"
    )(
      vid"rphjb_PerfettoIncompetente.mp4"
    ),
    ReplyBundleMessage.textToVideo[F]("sono (proprio )?un coglione".r.tr(16))(vid"rphjb_SonoCoglione.mp4"),
    ReplyBundleMessage.textToVideo[F]("sta male", "canzoni di natale")(
      vid"rphjb_CanzoniNataleStavaMaleMalmsteen.mp4"
    ),
    ReplyBundleMessage
      .textToVideo[F]("yngwie", "malmsteen")(
        vid"rphjb_Ramarro.mp4",
        vid"rphjb_CanzoniNataleStavaMaleMalmsteen.mp4",
        vid"rphjb_BarzellettaGesuCristoParadisoPurgatorioMalmsteenDio.mp4",
        vid"rphjb_FotoMalmsteen.mp4",
        vid"rphjb_TommyAldridgeYngwieMalmsteenWhereAreYouGoing.mp4"
      ),
    ReplyBundleMessage.textToVideo[F]("invece no", "si ricomincia", "da capo")(
      vid"rphjb_InveceNoRicominciaDaCapo.mp4"
    ),
    ReplyBundleMessage.textToVideo[F]("allora parlo")(vid"rphjb_AlloraParlo.mp4"),
    ReplyBundleMessage.textToVideo[F]("da paura")(vid"rphjb_DaPaura.mp4"),
    ReplyBundleMessage.textToVideo[F](
      "cipresso",
      "guardando il padrone",
      "all'ombra",
      "aspett(a|ando|are)".r.tr(7)
    )(vid"rphjb_CaneOmbraCipressoPadroneMortoIcaniPiangono.mp4"),
    ReplyBundleMessage.textToVideo[F]("pride")(vid"rphjb_BarzellettaPapaSonoGayPride.mp4"),
    ReplyBundleMessage
      .textToVideo[F]("gay")(vid"rphjb_BarzellettaPapaSonoGayPride.mp4", vid"rphjb_CantantePreferitoNonSonoGaio.mp4"),
    ReplyBundleMessage.textToVideo[F]("barzelletta")(
      vid"rphjb_BarzellettaGesuCristoParadisoPurgatorioMalmsteenDio.mp4",
      vid"rphjb_BarzellettaPapaSonoGayPride.mp4"
    ),
    ReplyBundleMessage.textToVideo[F]("\\brbo\\b".r.tr(3), "cazzò", "(5|cinque) livelli".r.tr(9))(vid"rphjb_RBO.mp4"),
    ReplyBundleMessage.textToVideo[F]("effettivamente")(vid"rphjb_Effettivamente.mp4"),
    ReplyBundleMessage.textToVideo[F]("tigre")(vid"rphjb_LaTigre.mp4"),
    ReplyBundleMessage
      .textToVideo[F]("che gruppo", "m[ei] ricorda".r.tr(10))(vid"rphjb_CheGruppoMiRicordaRisata.mp4"),
    ReplyBundleMessage.textToVideo[F]("il ciano", "luciano")(vid"rphjb_IlCiano.mp4"),
    ReplyBundleMessage.textToVideo[F](
      "volta il cervello",
      "principi veneziani",
      "decaduti",
      "rimorti",
      "rinati"
    )(
      vid"rphjb_CheStoDicendoDiVoltaIlCervelloPrincipiVeneziani.mp4"
    ),
    ReplyBundleMessage.textToVideo[F]("gaio")(vid"rphjb_CantantePreferitoNonSonoGaio.mp4"),
    ReplyBundleMessage.textToVideo[F]("come si fa")(vid"rphjb_ComeSiFaItaliaIgnorante.mp4"),
    ReplyBundleMessage.textToVideo[F]("commissionato")(vid"rphjb_CommissionatoMeLoDeviTrovare.mp4"),
    ReplyBundleMessage.textToVideo[F]("col sangue")(vid"rphjb_ColSangue.mp4"),
    ReplyBundleMessage.textToVideo[F]("vergognassero", "giornali")(
      vid"rphjb_ChitarreVergognateviSchifosiGiornaliMerda.mp4"
    ),
    ReplyBundleMessage.textToVideo[F]("echo")(
      vid"rphjb_CarrellataInfernaleDelirioPureNellaGolaTrasmissioneDaUrloEchoDelayVitaNaturalDurante.mp4",
      vid"rphjb_CattedraleCanterburyRavennaEcho.mp4"
    ),
    ReplyBundleMessage.textToVideo[F]("trasmissione da urlo", "delay", "vita natural durante")(
      vid"rphjb_CarrellataInfernaleDelirioPureNellaGolaTrasmissioneDaUrloEchoDelayVitaNaturalDurante.mp4"
    ),
    ReplyBundleMessage.textToVideo[F]("cantante")(
      vid"rphjb_CantantePreferitoNonSonoGaio.mp4",
      vid"rphjb_CantanteDonnaVoceDaUomoDueBocceCosiInnamoratoPerdutamente.mp4",
      vid"rphjb_DaHollywood.mp4"
    ),
    ReplyBundleMessage.textToVideo[F](
      "due bocce",
      "innamorato",
      "veronica frieman",
      "benedictum"
    )(vid"rphjb_CantanteDonnaVoceDaUomoDueBocceCosiInnamoratoPerdutamente.mp4"),
    ReplyBundleMessage
      .textToVideo[F]("verita", "verità")(vid"rphjb_AltraCazzataVeritaSembranoCazzate.mp4", vid"rphjb_Verita.mp4"),
    ReplyBundleMessage.textToVideo[F](
      "litfiba",
      "piero pelù",
      "ghigo renzulli",
      "\\bpuzz[oi]\\b".r.tr(5),
      "completamente fro(ci|sh)o".r.tr(20)
    )(vid"rphjb_PuzzoGhigoRenzulliPieroPeluFrocio.mp4"),
    ReplyBundleMessage.textToVideo[F]("fammelo avere", "al pi[ùu] presto".r.tr(13))(
      vid"rphjb_FammeloAvereAlPiuPresto.mp4"
    ),
    ReplyBundleMessage.textToVideo[F]("avvertire", "in guardia", "scelte giuste")(
      vid"rphjb_AvvertireMettereInGuardiaAiutareScelteGiuste.mp4"
    ),
    ReplyBundleMessage.textToVideo[F](
      "abbellimenti",
      "mordenti",
      "rivolti",
      "rivoli",
      "impennate",
      "colori"
    )(
      vid"rphjb_AbbellimentiRivoltiRivoliMordentiImpennateColori.mp4"
    ),
    ReplyBundleMessage.textToVideo[F](
      "cattegrale",
      "canterbury",
      "ravenna"
    )(
      vid"rphjb_CattedraleCanterburyRavennaEcho.mp4"
    ),
    ReplyBundleMessage.textToVideo[F](
      "vestito (nuovo|vecchio)".r.tr(13),
      "(rammenda|rappezza|rattoppa|ricuci)".r.tr(6)
    )(
      vid"rphjb_CompriVestitoNuovoRammendaVecchio.mp4"
    ),
    ReplyBundleMessage.textToVideo[F](
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
      vid"rphjb_CuginiCampagnaGiardinoSempliciHomoSapiensLiscioRumbaChaChaChaCanzoneNapoletanaOsanna.mp4"
    ),
    ReplyBundleMessage.textToVideo[F](
      "cuore affogato",
      "affogato nel metallo",
      "alla ricerca",
      "in cerca",
      "ferisca nel cervello"
    )(
      vid"rphjb_CuoreAffogatoNelMetalloCercaCanzoneFeriscaCervello.mp4",
      vid"rphjb_CuoreAffogatoNelMetalloRicercaCanzoneFeriscaNelCervello.mp4"
    ),
    ReplyBundleMessage.textToVideo[F](
      "stratovarius",
      "metallica",
      "ultimo (disco|album)".r.tr(13)
    )(
      vid"rphjb_DeludendoQuasiTutto.mp4"
    ),
    ReplyBundleMessage.textToVideo[F](
      "donna (che merita|forte)".r.tr(11),
      "profilo (fisico|intellettuale)".r.tr(14),
      "simile a me",
      "versione donna"
    )(
      vid"rphjb_DonnaMerita.mp4"
    ),
    ReplyBundleMessage.textToVideo[F](
      "alpheus",
      "whiskey",
      "mi sentivo (di fare)?così".r.tr(15),
      "ho fatto così"
    )(
      vid"rphjb_EtichetteSulleBottiglieDiWhiskeyAlpheus.mp4"
    ),
    ReplyBundleMessage.textToVideo[F](
      "queen",
      "follia"
    )(
      vid"rphjb_FolliaQueenNo.mp4"
    ),
    ReplyBundleMessage.textToVideo[F](
      "ghent",
      "albania"
    )(
      vid"rphjb_GhentPiattiAlbania.mp4",
      vid"rphjb_PiattiGhentAlbaniaCiPensa.mp4",
      vid"rphjb_PiattiGhentDischiVolantiAlbaniaPortaCenere.mp4"
    ),
    ReplyBundleMessage.textToVideo[F](
      "piatti",
    )(
      vid"rphjb_BiscionePiatti.mp4",
      vid"rphjb_GhentPiattiAlbania.mp4",
      vid"rphjb_PiattiGhentAlbaniaCiPensa.mp4",
      vid"rphjb_PiattiGhentDischiVolantiAlbaniaPortaCenere.mp4"
    ),
    ReplyBundleMessage.textToVideo[F](
      "amicizia",
      "amico del cuore",
      "nella merda",
      "subliminali"
    )(
      vid"rphjb_AmicoDelCuoreLasciatoNellaMerdaParoleSubliminaliPoesiaAmiciziaVera.mp4"
    ),
    ReplyBundleMessage.textToVideo[F](
      "migliore amico"
    )(
      vid"rphjb_AmicoDelCuoreLasciatoNellaMerdaParoleSubliminaliPoesiaAmiciziaVera.mp4",
      vid"rphjb_MigliorAmicoCoppiaMicidialeGianniNeri.mp4"
    ),
    ReplyBundleMessage.textToVideo[F](
      "ebbe un bambino",
      "blues"
    )(
      vid"rphjb_Blues.mp4",
      vid"rphjb_BluesEbbeBambinoRockNRollBeBopALulaStormyMondayButTuesdayIsJustAsBad.mp4"
    ),
    ReplyBundleMessage.textToVideo[F](
      "monday",
      "tuesday"
    )(
      vid"rphjb_BluesEbbeBambinoRockNRollBeBopALulaStormyMondayButTuesdayIsJustAsBad.mp4"
    ),
    ReplyBundleMessage.textToVideo[F](
      "ultimo degli ultimi"
    )(
      vid"rphjb_DanzaMacabra.mp4"
    ),
    ReplyBundleMessage.textToVideo[F](
      "di tutti i colori",
      "tramonto d'estate",
      "boschi in penombra",
      "per un ideale"
    )(
      vid"rphjb_PoesiaMadre.mp4"
    ),
    ReplyBundleMessage.textToVideo[F](
      "difficile guardare",
      "(vedere|guardare) l[aà]".r.tr(9)
    )(
      vid"rphjb_DifficileGuardareTuboCatodicoNienteCiSepara.mp4",
      vid"rphjb_LaDoveDifficileGuardare.mp4"
    ),
    ReplyBundleMessage.textToVideo[F](
      "tubo catodico",
    )(
      vid"rphjb_DifficileGuardareTuboCatodicoNienteCiSepara.mp4"
    ),
    ReplyBundleMessage.textToVideo[F](
      "negri"
    )(
      vid"rphjb_DueNegriMostruosi.mp4"
    ),
    ReplyBundleMessage.textToVideo[F](
      "eccoci qu[aà]".r.tr(10),
      "io sto l[aà]\\b".r.tr(9),
      "dove (ca[z]+[o]?)?sto".r.tr(8),
      "sto d[ie] (qu[aà]|l[aà])".r.tr(9),
      "sto l[iì]\\b".r.tr(6),
      "luce[tta]? rossa".r.tr(10)
    )(
      vid"rphjb_EccociQuaStoLaDoCazzoStoDiQuaDiLaLiDavantiConTeLucettaRossa.mp4"
    ),
    ReplyBundleMessage.textToVideo[F](
      "etichette"
    )(
      vid"rphjb_EtichetteSupermercatoSputatiMondo.mp4",
      vid"rphjb_EtichetteSulleBottiglieDiWhiskeyAlpheus.mp4"
    ),
    ReplyBundleMessage.textToVideo[F](
      "supermercato",
      "sputati nel mondo",
      "gli altri siamo noi"
    )(
      vid"rphjb_EtichetteSupermercatoSputatiMondo.mp4"
    ),
    ReplyBundleMessage.textToVideo[F](
      "tendetemi le vostre spire"
    )(
      vid"rphjb_GerarchieInfernali2.mp4",
      vid"rphjb_GerarchieInfernali3.mp4"
    ),
    ReplyBundleMessage.textToVideo[F](
      "uno da bere",
      "sputatemi dalle vostre labbra",
      "figlie(,)? ma di quale madre".r.tr(24),
      "fetenti feti di fede",
      "che ti inganna di notte",
      "che muore di giorno",
      "rovistandoti nell'immondo"
    )(
      vid"rphjb_GerarchieInfernali2.mp4"
    ),
    ReplyBundleMessage.textToVideo[F](
      "cambiato totalmente",
      "un maiale",
      "diventato grasso",
      "simpatico"
    )(
      vid"rphjb_FotoMalmsteen.mp4"
    ),
    ReplyBundleMessage.textToVideo[F](
      "le gambe"
    )(
      vid"rphjb_GambeInesistentiDueOssa.mp4",
      vid"rphjb_DanzaMacabra.mp4"
    ),
    ReplyBundleMessage.textToVideo[F](
      "tommy aldridge",
      "white[ ]?snake".r.tr(10),
      "pat travers",
      "manona",
      "I('m|am) the leader",
      "quello stronzo",
      "io sono dio",
      "is god"
    )(
      vid"rphjb_TommyAldridgeYngwieMalmsteenWhereAreYouGoing.mp4"
    ),
    ReplyBundleMessage.textToVideo[F](
      "dire e il fare",
      "di mezzo il mare"
    )(
      vid"rphjb_TraDireFareMezzoMare.mp4"
    ),
    ReplyBundleMessage.textToVideo[F](
      "figura di merda"
    )(
      vid"rphjb_FiguraDiMerdaQuestaVoltaNo.mp4"
    ),
    ReplyBundleMessage.textToVideo[F](
      "la mia faccia",
      "figuracc(e|ia)".r.tr(9)
    )(
      vid"rphjb_FiguracceDiscoSteveVai.mp4"
    ),
    ReplyBundleMessage.textToVideo[F](
      "il demonio",
      "venuto a trovarmi"
    )(
      vid"rphjb_IlDemonioVenutoATrovarmi.mp4"
    ),
    ReplyBundleMessage.textToVideo[F](
      "sta sul cazzo"
    )(
      vid"rphjb_MiStaSulCazzo.mp4"
    ),
    ReplyBundleMessage.textToVideo[F](
      "momenti di gloria"
    )(
      vid"rphjb_MomentiGloria.mp4"
    ),
    ReplyBundleMessage.textToVideo[F](
      "sepultura"
    )(
      vid"rphjb_SepolturaRisata.mp4"
    ),
    ReplyBundleMessage.textToVideo[F](
      "obama"
    )(
      vid"rphjb_Obama.mp4",
      vid"rphjb_ObamaRichardBensonInsieme.mp4"
    ),
    ReplyBundleMessage.textToVideo[F](
      "dischi volanti",
      "posa cenere"
    )(
      vid"rphjb_PiattiGhentDischiVolantiAlbaniaPortaCenere.mp4"
    ),
    ReplyBundleMessage.textToVideo[F](
      "è troppo tempo",
      "non ti vedo"
    )(
      vid"rphjb_TroppoTempoNonTiVedo.mp4"
    ),
    ReplyBundleMessage.textToVideo[F](
      "tagliano le gambe"
    )(
      vid"rphjb_QuaTaglianoGambe.mp4"
    ),
    ReplyBundleMessage.textToVideo[F](
      "proselito"
    )(
      vid"rphjb_NuovoProselito.mp4"
    ),
    ReplyBundleMessage.textToVideo[F](
      "colpa di uno"
    )(
      vid"rphjb_PerColpaUno.mp4"
    ),
    ReplyBundleMessage.textToVideo[F](
      "fassino",
      "ritratto della salute",
      "sembra un cadavere"
    )(
      vid"rphjb_SembraCadavereFassinoRitrattoSalute.mp4"
    ),
    ReplyBundleMessage.textToVideo[F](
      "vuoi (questo|la merda)".r.tr(11),
      "io te (lo d[òo]|la suono)".r.tr(11)
    )(
      vid"rphjb_VuoiMerdaIoSuono.mp4"
    ),
    ReplyBundleMessage.textToVideo[F](
      "schifosa"
    )(
      vid"rphjb_GenteSchifosa.mp4"
    ),
    ReplyBundleMessage.textToVideo[F](
      "prendere in giro"
    )(
      vid"rphjb_GenteSchifosa.mp4"
    ),
    ReplyBundleMessage.textToVideo[F](
      "migliorare"
    )(
      vid"rphjb_SiPuoMigliorare.mp4"
    ),
    ReplyBundleMessage.textToVideo[F](
      "ballerino",
      "attore",
      "non posso salutare",
      "fornaio",
      "barman",
      "altro piano"
    )(
      vid"rphjb_DaHollywood.mp4"
    ),
    ReplyBundleMessage.textToVideo[F](
      "non mi fa rabbia",
      "fa ridere"
    )(
      vid"rphjb_NoRabbiaRidereMeNeFrego.mp4"
    ),
    ReplyBundleMessage.textToVideo[F](
      "melensa"
    )(
      vid"rphjb_MelensaStareMale.mp4"
    ),
    ReplyBundleMessage.textToVideo[F](
      "questa sera",
      "ancora di pi[uù]".r.tr(13)
    )(
      vid"rphjb_QuestaSeraAncoraDiPiù.mp4"
    ),
    ReplyBundleMessage.textToVideo[F](
      "paul gilbert"
    )(
      vid"rphjb_FesteACasaNicolaArigliano.mp4"
    ),
    ReplyBundleMessage.textToVideo[F](
      "genitori"
    )(
      vid"rphjb_TrovatoPaceGenitori.mp4",
      vid"rphjb_CristoPinocchio.mp4"
    ),
    ReplyBundleMessage.textToVideo[F](
      "trovare"
    )(
      vid"rphjb_CommissionatoMeLoDeviTrovare.mp4",
      vid"rphjb_MoltoDifficileDaTrovare.mp4"
    ),
    ReplyBundleMessage.textToVideo[F](
      "le vie sono tante"
    )(
      vid"rphjb_VieSonoTanteMilioniDiMilioniMiCoglioniViaDelleAlbizzie22.mp4"
    ),
    ReplyBundleMessage.textToVideo[F](
      "essere libero"
    )(
      vid"rphjb_VoglioEssereLibero.mp4"
    ),
    ReplyBundleMessage.textToVideo[F](
      "vicissitudini",
      "in bestia"
    )(
      vid"rphjb_VicissitudiniPersoneInBestia.mp4"
    ),
    ReplyBundleMessage.textToVideo[F](
      "un viaggio",
      "nella mente"
    )(
      vid"rphjb_ViaggioMenteUmana.mp4"
    ),
    ReplyBundleMessage.textToVideo[F](
      "eccoci qua",
      "di nuovo insieme",
      "([ ]?hey[ ]?){2,}".r.tr(8)
    )(
      vid"rphjb_WelaHeyHeyHeyDiNuovoInsieme.mp4"
    ),
    ReplyBundleMessage.textToVideo[F](
      "sono simpatico"
    )(
      vid"rphjb_TantoSimpatico.mp4"
    ),
    ReplyBundleMessage.textToVideo[F](
      "micetto"
    )(
      vid"rphjb_SembriMicetto.mp4"
    ),
    ReplyBundleMessage.textToVideo[F](
      "contro il pisello"
    )(
      vid"rphjb_SbatteControPiselloSonoAbituatoEssereSbattutoLa.mp4"
    ),
    ReplyBundleMessage.textToVideo[F](
      "vivete",
      "sognate",
      "vivere per sempre",
      "morire oggi"
    )(
      vid"rphjb_SognateViverePerSempreViveteMorireOggi.mp4"
    ),
    ReplyBundleMessage.textToVideo[F](
      "(quanta|troppa) gente".r.tr(12)
    )(
      vid"rphjb_QuantaGenteTroppa.mp4"
    ),
    ReplyBundleMessage.textToVideo[F](
      "una fossa",
      "felicità"
    )(
      vid"rphjb_FossaCollaSerpeSerpeFelicitaMusica.mp4"
    ),
    ReplyBundleMessage.textToVideo[F](
      "i (dis)?gusti".r.tr(7)
    )(
      vid"rphjb_GustiPubblicoRappresentanoMieiDisgusti.mp4"
    ),
    ReplyBundleMessage.textToVideo[F](
      "che stronzo",
      "male di mente",
      "interviste",
      "non (ci|cene) siamo (mai )?accorti".r.tr(20),
      "fuori di testa",
      "timo tolkki",
    )(
      vid"rphjb_LoSapevoIoMaleDiMenteTimoTolki.mp4"
    ),
    ReplyBundleMessage.textToVideo[F](
      "coppia micidiale",
      "si è riformata"
    )(
      vid"rphjb_MigliorAmicoCoppiaMicidialeGianniNeri.mp4"
    ),
    ReplyBundleMessage.textToVideo[F](
      "non (ne )?posso (più|continuare|più continuare)".r.tr(13)
    )(
      vid"rphjb_NonPossoContinuareCosiGianni.mp4"
    ),
    ReplyBundleMessage.textToVideo[F](
      "grazie",
      "mancano le parole",
      "le parole non esistono"
    )(vid"rphjb_GraziePerQuelloCheFaiPerMeMancanoLeParole.mp4"),
    ReplyBundleMessage.textToVideo[F](
     "non siamo niente",
     "siamo esseri umani",
     "sudore",
     "pelle",
     "zozzeria",
     "carne",
     "sperma",
     "da togliere",
     "levare d[ia] dosso".r.tr(15),
     "non contiamo niente",
    )(
      vid"rphjb_EsseriUmaniZozzeriaCarnePelleSputoSudoreSpermaNonContiamoNiente.mp4"
    ),
    ReplyBundleMessage.textToVideo[F](
      "giovenca",
      "varzetta",
      "me la sposo"
    )(
      vid"rphjb_GiovencaVarzettaSposoChissenefrega.mp4"
    )
  )
}
