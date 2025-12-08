package com.benkio.richardphjbensonbot.data

import com.benkio.telegrambotinfrastructure.model.reply.vid
import com.benkio.telegrambotinfrastructure.model.reply.ReplyBundleMessage
import com.benkio.telegrambotinfrastructure.model.tr

object Video {

  def messageRepliesVideoData: List[ReplyBundleMessage] = List(
    ReplyBundleMessage.textToVideo(
      "soldati",
      "vorrei (tanto )?risentirlo".r
    )(
      vid"rphjb_AmiciVeriVecchiSoldati.mp4"
    ),
    ReplyBundleMessage.textToVideo(
      "amici veri",
      "veri amici"
    )(
      vid"rphjb_AmiciVeriVecchiSoldati.mp4",
      vid"rphjb_CuoreInMano.mp4"
    ),
    ReplyBundleMessage.textToVideo(
      "accor(data|dana)".r
    )(
      vid"rphjb_Accordana.mp4"
    ),
    ReplyBundleMessage.textToVideo(
      "\\brap\\b".r,
      "musica italiana",
      "vi regalo",
      "quella merda"
    )(
      vid"rphjb_RapMusicaMelodicaListaCantanti.mp4"
    ),
    ReplyBundleMessage.textToVideo(
      "luciano tajoli"
    )(
      vid"rphjb_RapMusicaMelodicaListaCantanti.mp4",
      vid"rphjb_QuesitoRegaloOtelloProfazioMarioLanzaTullioPaneLucianoTaglioliGianniCeleste.mp4"
    ),
    ReplyBundleMessage.textToVideo(
      "uo[m]+ini d'affari".r,
      "andò in america",
      "non c'è la benzina",
      "a sbattere da tutte le parti",
      "mancano i motori"
    )(
      vid"rphjb_EricClaptonDrogaUominiAffari.mp4"
    ),
    ReplyBundleMessage.textToVideo(
      "rampolli",
      "studi a boston",
      "borghesia alta",
      "idoli delle mamme",
      "figliole",
      "con tanti soldi"
    )(
      vid"rphjb_Rampolli.mp4"
    ),
    ReplyBundleMessage.textToVideo(
      "giacca",
      "cravatta",
      "passaporto degli stronzi"
    )(
      vid"rphjb_RocchettariCapelliCortiGiaccaCravattaPassaportoStronzi.mp4",
      vid"rphjb_RockettariComeBestieCravattaPassaportoStronzi.mp4"
    ),
    ReplyBundleMessage.textToVideo(
      "capelli corti"
    )(
      vid"rphjb_RocchettariCapelliCortiGiaccaCravattaPassaportoStronzi.mp4",
      vid"rphjb_PetrucciCapelliCorti.mp4"
    ),
    ReplyBundleMessage.textToVideo(
      "canaro",
      "magliana"
    )(
      vid"rphjb_FregataFregatura.mp4"
    ),
    ReplyBundleMessage.textToVideo(
      "si o no"
    )(
      vid"rphjb_SiONo.mp4"
    ),
    ReplyBundleMessage.textToVideo(
      "streghe",
      "inquisizione",
      "al rogo",
      "effetti (impropri|ipnotici)".r
    )(
      vid"rphjb_Streghe.mp4"
    ),
    ReplyBundleMessage.textToVideo(
      "(tornando|andando) (all')?indietro".r.tr(16),
      "innovazione",
      "composizione",
      "idea (nuova|fresca)".r
    )(
      vid"rphjb_ComposizioneIdeaFrescaInnovazioneAndareAvantiStiamoTornandoIndetro.mp4"
    ),
    ReplyBundleMessage.textToVideo(
      "vicoletto"
    )(
      vid"rphjb_ChitarraPlettroVicoletto.mp4",
      vid"rphjb_ChitarraVicolettoPlettro2.mp4"
    ),
    ReplyBundleMessage.textToVideo(
      "diversi mondi",
      "letti sfatti"
    )(
      vid"rphjb_LettiSfattiDiversiMondi.mp4",
      vid"rphjb_AmmaestrareIlDolore.mp4"
    ),
    ReplyBundleMessage.textToVideo(
      "ramarro",
      "impellitteri"
    )(
      vid"rphjb_Ramarro.mp4"
    ),
    ReplyBundleMessage.textToVideo(
      "vi dovete spaventare"
    )(
      vid"rphjb_ViDoveteSpaventare.mp4"
    ),
    ReplyBundleMessage.textToVideo(
      "amore nello suonare",
      "uno freddo",
      "buddisti"
    )(
      vid"rphjb_AmoreSuonareFreddoBuddistiSchifoso.mp4"
    ),
    ReplyBundleMessage.textToVideo(
      "riciclando"
    )(
      vid"rphjb_SteveVaiRiciclando.mp4"
    ),
    ReplyBundleMessage.textToVideo(
      "già il titolo",
      "(coi|quei) due punti".r,
      "illusioni",
      "m[ei] danno fastidio",
      "musica (non )?è grande".r,
      "re[a]?l i[l]+lusion[s]?".r
    )(
      vid"rphjb_RelIllusions.mp4"
    ),
    ReplyBundleMessage.textToVideo(
      "copertina"
    )(
      vid"rphjb_PrimoDiscoBeatlesRagioneVenutoMondoTroppoForte.mp4",
      vid"rphjb_RelIllusions.mp4"
    ),
    ReplyBundleMessage.textToVideo(
      "trattori",
      "palmizio",
      "meno c'è",
      "meno si rompe"
    )(
      vid"rphjb_Palmizio.mp4"
    ),
    ReplyBundleMessage.textToVideo(
      "peso di un cervello"
    )(
      vid"rphjb_VitaNemicoCervello.mp4",
      vid"rphjb_PoesiaDirittoPaura.mp4"
    ),
    ReplyBundleMessage.textToVideo(
      "percussionista"
    )(
      vid"rphjb_CollaSerpeSigarettePercussionista.mp4",
      vid"rphjb_TommyAldridgeYngwieMalmsteenWhereAreYouGoing.mp4"
    ),
    ReplyBundleMessage.textToVideo(
      "perla di pioggia",
      "dove non piove mai"
    )(
      vid"rphjb_PerlaDiPioggia.mp4",
      vid"rphjb_AlbizziePerlaPioggia.mp4"
    ),
    ReplyBundleMessage.textToVideo(
      "l[i]+[b]+[e]+r[i]+".r.tr(6)
    )(
      vid"rphjb_Liberi.mp4"
    ),
    ReplyBundleMessage.textToVideo(
      "\\bcinta\\b".r,
      "bruce kulick"
    )(
      vid"rphjb_CintaProblema.mp4"
    ),
    ReplyBundleMessage.textToVideo(
      "sepoltura"
    )(
      vid"rphjb_SepolturaRisata.mp4"
    ),
    ReplyBundleMessage.textToVideo(
      "big money"
    )(
      vid"rphjb_BigMoney.mp4"
    ),
    ReplyBundleMessage.textToVideo(
      "(mi|me) so(n|no)? rotto il ca\\b".r.tr(17),
      "impazzi(to|sce|ta) totalmente".r.tr(20),
      "a[cg]ia[s]?[cg]ia[s]?".r
    )(
      vid"rphjb_RottoIlCa.mp4"
    ),
    ReplyBundleMessage.textToVideo(
      "diventare papa"
    )(
      vid"rphjb_DiventarePapa.mp4"
    ),
    ReplyBundleMessage.textToVideo(
      "[cg]hi[td]a[r]+is[td]a pi[uùú] velo[cg]e".r.tr(21),
      "gli uomini(, |...)?mi fanno schifo",
      "le donne un po' meno"
    )(
      vid"rphjb_Arivato.mp4"
    ),
    ReplyBundleMessage.textToVideo(
      "il più veloce"
    )(
      vid"rphjb_Arivato.mp4",
      vid"rphjb_PistoleroVeloceAmmazzarePersoneServitorePubblico.mp4"
    ),
    ReplyBundleMessage.textToVideo(
      "\\bbeat\\b".r,
      "e poi(, | )e poi".r,
      "qualche volta vedo lei",
      "non la vedo pi[uù]".r,
      "mi (si )?piange il cuor[e]?".r,
      "sfasciavamo tutti gli strumenti"
    )(
      vid"rphjb_AssoloBeat.mp4"
    ),
    ReplyBundleMessage.textToVideo(
      "buon compleanno"
    )(
      vid"rphjb_Compleanno.mp4",
      vid"rphjb_AuguriCompleanno.mp4"
    ),
    ReplyBundleMessage.textToVideo(
      "ringraziare",
      "traffico"
    )(
      vid"rphjb_RingraziareGianniTraffico.mp4"
    ),
    ReplyBundleMessage.textToVideo(
      "(roba|droga) tagliata male".r,
      "one television",
      "rock machine",
      "(un po'|un attimo) (di|de) esercitazione".r.tr(23)
    )(
      vid"rphjb_RockMachineIntro.mp4"
    ),
    ReplyBundleMessage.textToVideo(
      "c'è un testo",
      "da piangere",
      "negro",
      "niente da perdere",
      "interferenze",
      "bestia offesa",
      "giudeo",
      "svastiche"
    )(
      vid"rphjb_Blues.mp4"
    ),
    ReplyBundleMessage.textToVideo(
      "sabato sera",
      "lo sporco",
      "distorsione",
      "più pulito",
      "john travolta",
      "video didattico",
      "fate venire le vostre (madri|mogli|fidanzate)".r.tr(27)
    )(
      vid"rphjb_DelirioDelSabatoSera.mp4"
    ),
    ReplyBundleMessage.textToVideo(
      "marilyn manson"
    )(
      vid"rphjb_Ciao2001FarsaManson.mp4",
      vid"rphjb_StoriaMarlinManson.mp4"
    ),
    ReplyBundleMessage.textToVideo(
      "capelli lunghi"
    )(
      vid"rphjb_PetrucciCapelliCorti.mp4",
      vid"rphjb_FotoLookDreamTheater.mp4"
    ),
    ReplyBundleMessage.textToVideo(
      "impiegat[oi]".r
    )(
      vid"rphjb_PetrucciCapelliCorti.mp4",
      vid"rphjb_PoesiaArtistiImpiegati.mp4"
    ),
    ReplyBundleMessage.textToVideo(
      "petrucci"
    )(
      vid"rphjb_PetrucciCapelliCorti.mp4",
      vid"rphjb_FotoLookDreamTheater.mp4"
    ),
    ReplyBundleMessage.textToVideo(
      "i genesis",
      "tecno(-| )thrash".r,
      "emerson(,)? lake (e|&) palmer".r.tr(21),
      "gentle giant",
      "marillion"
    )(
      vid"rphjb_Regressive.mp4"
    ),
    ReplyBundleMessage.textToVideo(
      "cresta dell'onda",
      "orlo del crollo"
    )(
      vid"rphjb_CrestaOnda.mp4"
    ),
    ReplyBundleMessage.textToVideo(
      "biscione"
    )(
      vid"rphjb_BiscionePiatti.mp4"
    ),
    ReplyBundleMessage.textToVideo(
      "non aprite quella porta"
    )(
      vid"rphjb_NonApriteQuellaPorta.mp4"
    ),
    ReplyBundleMessage.textToVideo(
      "paralitico"
    )(
      vid"rphjb_DanzaMacabra.mp4"
    ),
    ReplyBundleMessage.textToVideo(
      "in ginocchio",
      "inginocchiatevi",
      "nuovo messia"
    )(
      vid"rphjb_MetteteviInGinocchio.mp4"
    ),
    ReplyBundleMessage.textToVideo(
      "sigarett[ea]".r
    )(
      vid"rphjb_Sigarette.mp4",
      vid"rphjb_CollaSerpeSigarettePercussionista.mp4",
      vid"rphjb_SucchiarviCaramelleFumarviCalpestareTacchiASpilloDominatore.mp4"
    ),
    ReplyBundleMessage.textToVideo(
      "sapere tutto",
      "se non le sai le cose",
      "radio rock",
      "informazioni sbagliate",
      "errore tragico",
      "(22|ventidue) maggio".r
    )(
      vid"rphjb_RadioRockErrori.mp4"
    ),
    ReplyBundleMessage.textToVideo(
      "uccidere",
      "con due (dita|mani)".r,
      "lo soffoco"
    )(
      vid"rphjb_UccidereUnaPersona.mp4"
    ),
    ReplyBundleMessage.textToVideo(
      "insegnante di [cg]hi[dt]arra".r.tr(22)
    )(
      vid"rphjb_InsegnanteDiChitarraModerna.mp4"
    ),
    ReplyBundleMessage.textToVideo(
      "pellegrinaggio"
    )(
      vid"rphjb_PellegrinaggioSimposioMetallo.mp4"
    ),
    ReplyBundleMessage.textToVideo(
      "ridicoli"
    )(
      vid"rphjb_Ridicoli.mp4"
    ),
    ReplyBundleMessage.textToVideo(
      "l'unico bravo",
      "scarica d(i |')andrenalina".r.tr(21),
      "non valgono (un cazzo|niente)".r
    )(
      vid"rphjb_UnicoBravo.mp4"
    ),
    ReplyBundleMessage.textToVideo(
      "come mi aiuta"
    )(
      vid"rphjb_DubbioComeMiAiuta.mp4"
    ),
    ReplyBundleMessage.textToVideo(
      "\\bdubbio\\b".r
    )(
      vid"rphjb_DubbioComeMiAiuta.mp4",
      vid"rphjb_DubbioScantinatiGiocoRattoGatto.mp4"
    ),
    ReplyBundleMessage.textToVideo(
      "scantinati",
      "fare il gioco",
      "gioco (io )?del gatto".r,
      "(voi )?del (ratto|topo)".r
    )(vid"rphjb_DubbioScantinatiGiocoRattoGatto.mp4"),
    ReplyBundleMessage.textToVideo(
      "albero grande",
      "anche un('| )amplificatore".r.tr(22),
      "tutti i (suoi )?frutti ti d[aà]".r.tr(20),
      "per quanti gliene domandi",
      "sempre uno ne troverà",
      "il (fiore|frutto)".r,
      "la foglia",
      "di tutto s[ie] spoglia".r,
      "cos'(è|e'|e) il rock".r
    )(
      vid"rphjb_PoesiaRock.mp4",
      vid"rphjb_PoesiaRockAlberoGrande.mp4"
    ),
    ReplyBundleMessage.textToVideo(
      "brutto vigile"
    )(
      vid"rphjb_Vigile.mp4"
    ),
    ReplyBundleMessage.textToVideo(
      "io non so mai",
      "più vicino alla fine",
      "hai un anno di più",
      "felicitazioni",
      "ma che siamo noi",
      "rumor[ie] di vetro e di metallo".r.tr(28)
    )(
      vid"rphjb_AuguriCompleanno.mp4"
    ),
    ReplyBundleMessage.textToVideo(
      "(3|tre) minuti".r,
      "ti va bene cos[iì]".r
    )(
      vid"rphjb_3Minuti.mp4"
    ),
    ReplyBundleMessage.textToVideo(
      "impara a sputare"
    )(
      vid"rphjb_ImparaASputareMignottaSchifose.mp4"
    ),
    ReplyBundleMessage.textToVideo(
      "brescia"
    )(
      vid"rphjb_BresciaMiPiace.mp4"
    ),
    ReplyBundleMessage.textToVideo(
      "\\bdvd\\b".r,
      "non si trova online",
      "membrana speciale",
      "cellula fotoelettrica",
      "non si può inserire"
    )(
      vid"rphjb_CellulaFotoelettrica.mp4"
    ),
    ReplyBundleMessage.textToVideo(
      "leon neon",
      "faccia d'angelo",
      "grande troia"
    )(
      vid"rphjb_LeonNeon.mp4"
    ),
    ReplyBundleMessage.textToVideo(
      "james la lagna",
      "gallinaceo",
      "lisa dagli occhi blu",
      "vibrato melodico",
      "mario tessuto",
      "disco solistico"
    )(
      vid"rphjb_Labrie.mp4"
    ),
    ReplyBundleMessage.textToVideo(
      "james labrie"
    )(
      vid"rphjb_Labrie.mp4",
      vid"rphjb_FotoLookDreamTheater.mp4"
    ),
    ReplyBundleMessage.textToVideo(
      "lo devi spiegare"
    )(
      vid"rphjb_LoDeviSpiegare.mp4"
    ),
    ReplyBundleMessage.textToVideo(
      "tempo al tempo",
      "non ne ho più"
    )(
      vid"rphjb_TempoAlTempo.mp4"
    ),
    ReplyBundleMessage.textToVideo(
      "ginecologo"
    )(
      vid"rphjb_BarzellettaPoliticaGinecologo.mp4"
    ),
    ReplyBundleMessage.textToVideo(
      "questa è una trasmissione",
      "caro avvocato",
      "punto di morte",
      "ti da (la carica|l'energia)".r,
      "ritornare alla vita"
    )(
      vid"rphjb_InPuntoDiMorte.mp4"
    ),
    ReplyBundleMessage.textToVideo(
      "le tecniche sono tante",
      "la tecnica che piace a me"
    )(
      vid"rphjb_LeTecnicheSonoTante.mp4"
    ),
    ReplyBundleMessage.textToVideo(
      "mi controllate dappertutto",
      "perfidi lacci",
      "non posso più scappare"
    )(
      vid"rphjb_PerfidiLacci.mp4"
    ),
    ReplyBundleMessage.textToVideo(
      "amplessi macisti"
    )(
      vid"rphjb_CorteiFemministiAmplessiMacisti.mp4"
    ),
    ReplyBundleMessage.textToVideo(
      "facevo schifo"
    )(
      vid"rphjb_FacevoSchifoOraSpacco.mp4"
    ),
    ReplyBundleMessage.textToVideo(
      "sono ingrassato",
      "esigenze cinematografiche"
    )(
      vid"rphjb_IngrassatoCinema.mp4"
    ),
    ReplyBundleMessage.textToVideo(
      "parlo io",
      "io parlo"
    )(
      vid"rphjb_NonMiFregaParloIo.mp4",
      vid"rphjb_IoParloDicoLaVeritaContrattiFantomaticiVieniQuiFaiVedereFacciaCovoDelMetalloSimposio.mp4"
    ),
    ReplyBundleMessage.textToVideo(
      "sono il demonio"
    )(
      vid"rphjb_SonoDemonio.mp4"
    ),
    ReplyBundleMessage.textToVideo(
      "mi assumo (tutte )?le responsabilità".r.tr(27)
    )(
      vid"rphjb_TuttaColpaMia.mp4",
      vid"rphjb_MiaColpaColpaMia.mp4"
    ),
    ReplyBundleMessage.textToVideo(
      "i colpevoli",
      "ho vinto io",
      "cercato di rovinarmi"
    )(
      vid"rphjb_RovinarmiVintoIo.mp4"
    ),
    ReplyBundleMessage.textToVideo(
      "un casino(,)? come al solito".r,
      "quando ci sono io",
      "l'acqua è (scivolata|scesa) (de|di) sotto".r.tr(24)
    )(
      vid"rphjb_UnCasinoComeAlSolito.mp4"
    ),
    ReplyBundleMessage.textToVideo(
      "storia vera",
      "piena di bugie"
    )(
      vid"rphjb_StoriaVeraPienaBugie.mp4"
    ),
    ReplyBundleMessage.textToVideo(
      "tra i coglioni"
    )(
      vid"rphjb_TraICoglioni.mp4"
    ),
    ReplyBundleMessage.textToVideo(
      "investi(re|tore)".r,
      "dubai",
      "bel[s]?ize".r,
      "nei punti più rilevanti",
      "non figura il mio nome",
      "sotto codici",
      "non si possono risalire",
      "governo (svizzero|germanico|tedesco)".r.tr(15),
      "affaristi"
    )(
      vid"rphjb_InvestitoreGoverno.mp4"
    ),
    ReplyBundleMessage.textToVideo(
      "pesce avaria[dt]o".r,
      "veramente di merda"
    )(
      vid"rphjb_PesceAvariato.mp4"
    ),
    ReplyBundleMessage.textToVideo(
      "governo americano",
      "rock('n'| n |&)roll presidence band".r.tr(25),
      "sax"
    )(
      vid"rphjb_Obama.mp4"
    ),
    ReplyBundleMessage.textToVideo(
      "piove in continuazione",
      "non ce la faccio più",
      "piove sempre",
      "a mio nonno",
      "nipote[!]+".r
    )(
      vid"rphjb_Nonno.mp4"
    ),
    ReplyBundleMessage.textToVideo(
      "sangue (caldo )di un cavallo".r,
      "diritto alla paura",
      "come una tigre",
      "migliaia di animali",
      "miliardi di uomini"
    )(
      vid"rphjb_PoesiaDirittoPaura.mp4"
    ),
    ReplyBundleMessage.textToVideo(
      "er foco",
      "in mezzo alle cosce",
      "le donne mi fanno questo effetto",
      "frasi inconsulte",
      "ne capo ne coda"
    )(
      vid"rphjb_DonneErFoco.mp4"
    ),
    ReplyBundleMessage.textToVideo(
      "non sto mai male",
      "febbre",
      "influenza",
      "raffreddore",
      "straight edge"
    )(
      vid"rphjb_NonStoMaiMale.mp4"
    ),
    ReplyBundleMessage.textToVideo(
      "libro giallo",
      "pagine gialle",
      "troppi personaggi"
    )(
      vid"rphjb_LibroGiallo.mp4"
    ),
    ReplyBundleMessage.textToVideo(
      "sperma in un bicchiere",
      "in onore di satana",
      "mi tocca il pacco",
      "lo bevo",
      "(il suo|i suoi) umor[ei]".r
    )(
      vid"rphjb_ConsigliSulPacco.mp4"
    ),
    ReplyBundleMessage.textToVideo(
      "rebus",
      "tocco qua\\b".r,
      "volt(o|are) pagina".r
    )(
      vid"rphjb_Rebus.mp4"
    ),
    ReplyBundleMessage.textToVideo(
      "non c'è giudizio",
      "parola fine"
    )(
      vid"rphjb_GiudizioParolaFine.mp4"
    ),
    ReplyBundleMessage.textToVideo(
      "spago"
    )(
      vid"rphjb_LegatiSpago.mp4",
      vid"rphjb_UnitiQualeSpago.mp4",
      vid"rphjb_LacciMaleficiSpaghiCordeCateneRetoricheOstrogoteBislamicheCirconcisoPrigioneVita.mp4",
      vid"rphjb_LegatiMaDaQualeSpagoCateneInvisibiliFilamentiOroFinoAlleStelleForzaRitraeIndietro.mp4"
    ),
    ReplyBundleMessage.textToVideo(
      "ci siamo sciolti",
      "non l'ha capita",
      "\\bnodi\\b".r
    )(
      vid"rphjb_Nodi.mp4"
    ),
    ReplyBundleMessage.textToVideo(
      "una bella fi[cg]a".r,
      "la fate aspettare",
      "in silenzio[,]?( dovuto)?".r,
      "consumare dopo",
      "consumare durante"
    )(
      vid"rphjb_VenerdiAppuntamentoFissoFica.mp4"
    ),
    ReplyBundleMessage.textToVideo(
      "slipknot",
      "type o negative",
      "morto pure",
      "morti tutti"
    )(
      vid"rphjb_MortiTutti.mp4"
    ),
    ReplyBundleMessage.textToVideo(
      "amico grasso",
      "(fare la| mettersi a) dieta".r,
      "circa (6|sei) mesi".r,
      "peso ideale",
      "pioppo",
      "zinco",
      "una bara"
    )(
      vid"rphjb_StoriaAmicoGrasso.mp4"
    ),
    ReplyBundleMessage.textToVideo(
      "dylan (dog|thomas)".r,
      "un po' gay",
      "la (collezione|colazione)".r,
      "t[ei] sei sbagliato".r
    )(
      vid"rphjb_DylanDog.mp4"
    ),
    ReplyBundleMessage.textToVideo(
      "poeti maledetti"
    )(
      vid"rphjb_DylanDog.mp4",
      vid"rphjb_RollingStonesJimiHendrixPoetiMaledettiFemministeControculturaRivoluzioniRibelioni5DitaCazzottoInFacciaGiustiziere.mp4"
    ),
    ReplyBundleMessage.textToVideo(
      "buckethead",
      "guns (n|n'|and) roses".r
    )(
      vid"rphjb_BucketheadGunsNRoses.mp4"
    ),
    ReplyBundleMessage.textToVideo(
      "signor (jones|jonz|jons|gionz)".r,
      "janet",
      "coniglio"
    )(
      vid"rphjb_StoriaSignorGionz.mp4"
    ),
    ReplyBundleMessage.textToVideo(
      "brooklyn",
      "carne morta",
      "manhattan",
      "cane da guerra"
    )(
      vid"rphjb_PrimoSbaglio.mp4"
    ),
    ReplyBundleMessage.textToVideo(
      "agisse da sola",
      "che me lo (in)?presti".r,
      "cani al cimitero",
      "solo uomini",
      "nemmeno una donna"
    )(
      vid"rphjb_CaniAlCimitero.mp4"
    ),
    ReplyBundleMessage.textToVideo(
      "anche la rabbia ha un cuore"
    )(
      vid"rphjb_AncheLaRabbiaHaUnCuore.mp4",
      vid"rphjb_AncheLaRabbiaHaUnCuore2.mp4",
      vid"rphjb_AncheLaRabbiaHaUnCuore3.mp4",
      vid"rphjb_AncheLaRabbiaHaUnCuore4.mp4"
    ),
    ReplyBundleMessage.textToVideo(
      "trovamelo",
      "me l'ha trovato"
    )(
      vid"rphjb_AngeloTrovamelo.mp4"
    ),
    ReplyBundleMessage.textToVideo(
      "(due|2) orecchie".r,
      "(una|1) bocca".r
    )(
      vid"rphjb_2Orecchie1Bocca.mp4",
      vid"rphjb_2Orecchie1Bocca2.mp4"
    ),
    ReplyBundleMessage.textToVideo(
      "limitazioni",
      "quante(,|...)? troppe".r
    )(
      vid"rphjb_BicchiereSputoLimitazioniUomoDonna.mp4"
    ),
    ReplyBundleMessage.textToVideo(
      "l'uomo(,|\\.\\.\\.)? la donna".r
    )(
      vid"rphjb_BicchiereSputoLimitazioniUomoDonna.mp4",
      vid"rphjb_QuestaNoMisticaIbridaContortaDolceFunzioniUomoDonna.mp4"
    ),
    ReplyBundleMessage.textToVideo(
      "cambia canale",
      "\\bcerchion[ie]\\b".r
    )(
      vid"rphjb_CambiaCanaleBruttoFrocio.mp4"
    ),
    ReplyBundleMessage.textToVideo(
      "garage",
      "varazze"
    )(
      vid"rphjb_AnimaGarageVarazze.mp4"
    ),
    ReplyBundleMessage.textToVideo(
      "arbitri (truccati|pagati)".r,
      "giocatori dopati",
      "(gioco del|il) calcio".r,
      "\\bmoggi\\b".r,
      "il coni\\b".r,
      "(\\b|^)ultr(à|as)(\\b|$)".r,
      "direttore del coni",
      "parole grosse",
      "fare del (male|bene)".r
    )(
      vid"rphjb_ArbitriPagatiTruccatiGiocatoriDopatiMoggiCONITifosiUltrasTuttaFarsaGiocoCalcio.mp4"
    ),
    ReplyBundleMessage.textToVideo(
      "incompetente"
    )(
      vid"rphjb_PerfettoIncompetente.mp4"
    ),
    ReplyBundleMessage.textToVideo(
      "sono (proprio )?un co(gl|j)ione".r.tr(15)
    )(vid"rphjb_SonoCoglione.mp4"),
    ReplyBundleMessage.textToVideo(
      "sta[va]? male".r,
      "canzoni di natale",
      "lo so (da anni|solo io)".r
    )(
      vid"rphjb_CanzoniNataleStavaMaleMalmsteen.mp4"
    ),
    ReplyBundleMessage.textToVideo("invece no", "si ricomincia", "da capo")(
      vid"rphjb_InveceNoRicominciaDaCapo.mp4"
    ),
    ReplyBundleMessage.textToVideo("allora parlo")(vid"rphjb_AlloraParlo.mp4"),
    ReplyBundleMessage.textToVideo("da paura")(vid"rphjb_DaPaura.mp4"),
    ReplyBundleMessage.textToVideo(
      "cipresso",
      "guardando il padrone",
      "all'ombra",
      "cani piangono",
      "aspett(a|ando|are)".r
    )(vid"rphjb_CaneOmbraCipressoPadroneMortoIcaniPiangono.mp4"),
    ReplyBundleMessage.textToVideo("pride")(vid"rphjb_BarzellettaPapaSonoGayPride.mp4"),
    ReplyBundleMessage
      .textToVideo("gay")(
        vid"rphjb_BarzellettaPapaSonoGayPride.mp4",
        vid"rphjb_CantantePreferitoNonSonoGaio.mp4",
        vid"rphjb_NonSonoGay.mp4",
        vid"rphjb_ParloDiDNonSonoGayCosiDifficileSemplice.mp4"
      ),
    ReplyBundleMessage.textToVideo(
      "\\brbo\\b".r,
      "cazzò",
      "(5|cinque) livelli".r
    )(vid"rphjb_RBO.mp4"),
    ReplyBundleMessage.textToVideo("effettivamente")(vid"rphjb_Effettivamente.mp4"),
    ReplyBundleMessage.textToVideo("tigre")(vid"rphjb_LaTigre.mp4"),
    ReplyBundleMessage
      .textToVideo(
        "che gruppo",
        "m[ei] ricorda".r
      )(vid"rphjb_CheGruppoMiRicordaRisata.mp4"),
    ReplyBundleMessage.textToVideo("il ciano", "luciano")(vid"rphjb_IlCiano.mp4"),
    ReplyBundleMessage.textToVideo(
      "volta il cervello",
      "principi veneziani",
      "decaduti",
      "rimorti",
      "rinati"
    )(
      vid"rphjb_CheStoDicendoDiVoltaIlCervelloPrincipiVeneziani.mp4"
    ),
    ReplyBundleMessage.textToVideo("come si fa")(vid"rphjb_ComeSiFaItaliaIgnorante.mp4"),
    ReplyBundleMessage.textToVideo("commissionato")(vid"rphjb_CommissionatoMeLoDeviTrovare.mp4"),
    ReplyBundleMessage.textToVideo("vergognassero", "giornali")(
      vid"rphjb_ChitarreVergognateviSchifosiGiornaliMerda.mp4"
    ),
    ReplyBundleMessage.textToVideo("echo")(
      vid"rphjb_CarrellataInfernaleDelirioPureNellaGolaTrasmissioneDaUrloEchoDelayVitaNaturalDurante.mp4",
      vid"rphjb_CattedraleCanterburyRavennaEcho.mp4"
    ),
    ReplyBundleMessage.textToVideo("trasmissione da urlo", "delay", "vita natural durante")(
      vid"rphjb_CarrellataInfernaleDelirioPureNellaGolaTrasmissioneDaUrloEchoDelayVitaNaturalDurante.mp4"
    ),
    ReplyBundleMessage.textToVideo(
      "due bocce",
      "innamorato",
      "veronica frieman",
      "benedictum"
    )(vid"rphjb_CantanteDonnaVoceDaUomoDueBocceCosiInnamoratoPerdutamente.mp4"),
    ReplyBundleMessage.textToVideo(
      "litfiba",
      "piero pelù",
      "ghigo renzulli",
      "\\bpuzz[oi]\\b".r,
      "completamente fro(ci|sh)o".r
    )(vid"rphjb_PuzzoGhigoRenzulliPieroPeluFrocio.mp4"),
    ReplyBundleMessage.textToVideo(
      "fammelo avere",
      "al pi[ùu] presto".r
    )(
      vid"rphjb_FammeloAvereAlPiuPresto.mp4"
    ),
    ReplyBundleMessage.textToVideo("avvertire", "in guardia", "scelte giuste")(
      vid"rphjb_AvvertireMettereInGuardiaAiutareScelteGiuste.mp4"
    ),
    ReplyBundleMessage.textToVideo(
      "rivoli",
      "impennate",
      "colori"
    )(
      vid"rphjb_AbbellimentiRivoltiRivoliMordentiImpennateColori.mp4"
    ),
    ReplyBundleMessage.textToVideo(
      "cattegrale",
      "canterbury",
      "ravenna"
    )(
      vid"rphjb_CattedraleCanterburyRavennaEcho.mp4"
    ),
    ReplyBundleMessage.textToVideo(
      "cugini di campagna",
      "giardino dei semplici",
      "homo sapiens",
      "(di|il) liscio".r,
      "(di|il) rumba".r,
      "cha[ ]?cha[ ]?cha".r,
      "canzon[ei] napoletan[ae]".r
    )(
      vid"rphjb_CuginiCampagnaGiardinoSempliciHomoSapiensLiscioRumbaChaChaChaCanzoneNapoletanaOsanna.mp4"
    ),
    ReplyBundleMessage.textToVideo(
      "stratovarius",
      "metallica",
      "ultimo (disco|album)".r
    )(
      vid"rphjb_DeludendoQuasiTutto.mp4"
    ),
    ReplyBundleMessage.textToVideo(
      "donna (che merita|forte)".r,
      "profilo (fisico|intellettuale)".r,
      "simile a me",
      "versione donna"
    )(
      vid"rphjb_DonnaMerita.mp4"
    ),
    ReplyBundleMessage.textToVideo(
      "mi sentivo (di fare)?così".r,
      "ho fatto così"
    )(
      vid"rphjb_EtichetteSulleBottiglieDiWhiskeyAlpheus.mp4"
    ),
    ReplyBundleMessage.textToVideo(
      "queen",
      "follia"
    )(
      vid"rphjb_FolliaQueenNo.mp4"
    ),
    ReplyBundleMessage.textToVideo(
      "amicizia",
      "nella merda",
      "subliminali"
    )(
      vid"rphjb_AmicoDelCuoreLasciatoNellaMerdaParoleSubliminaliPoesiaAmiciziaVera.mp4"
    ),
    ReplyBundleMessage.textToVideo(
      "migliore amico"
    )(
      vid"rphjb_AmicoDelCuoreLasciatoNellaMerdaParoleSubliminaliPoesiaAmiciziaVera.mp4",
      vid"rphjb_MigliorAmicoCoppiaMicidialeGianniNeri.mp4"
    ),
    ReplyBundleMessage.textToVideo(
      "ebbe un bambino"
    )(
      vid"rphjb_Blues.mp4",
      vid"rphjb_BluesEbbeBambinoRockNRollBeBopALulaStormyMondayButTuesdayIsJustAsBad.mp4"
    ),
    ReplyBundleMessage.textToVideo(
      "monday",
      "tuesday"
    )(
      vid"rphjb_BluesEbbeBambinoRockNRollBeBopALulaStormyMondayButTuesdayIsJustAsBad.mp4"
    ),
    ReplyBundleMessage.textToVideo(
      "ultimo degli ultimi"
    )(
      vid"rphjb_DanzaMacabra.mp4"
    ),
    ReplyBundleMessage.textToVideo(
      "tramonto d'estate",
      "boschi in penombra",
      "per un ideale"
    )(
      vid"rphjb_PoesiaMadre.mp4"
    ),
    ReplyBundleMessage.textToVideo(
      "di tutti i colori"
    )(
      vid"rphjb_PoesiaMadre.mp4",
      vid"rphjb_RingrazioPersoneAttenteDonneToccavanoSeniAnni70LettiPieniErbaCoca.mp4"
    ),
    ReplyBundleMessage.textToVideo(
      "difficile guardare",
      "(vedere|guardare) l[aà]".r
    )(
      vid"rphjb_DifficileGuardareTuboCatodicoNienteCiSepara.mp4",
      vid"rphjb_LaDoveDifficileGuardare.mp4",
      vid"rphjb_OcchiVistoLaDifficileGuardareTrasmissioneLetaleTiCambiaGerarchieInfernali.mp4"
    ),
    ReplyBundleMessage.textToVideo(
      "tubo catodico"
    )(
      vid"rphjb_DifficileGuardareTuboCatodicoNienteCiSepara.mp4"
    ),
    ReplyBundleMessage.textToVideo(
      "io sto l[aà](\\b|$)".r,
      "dove (ca[z]+[o]?)?sto".r,
      "sto d[ie] (qu[aà]|l[aà])".r,
      "sto l[iì](\\b|$)".r,
      "luce[tta]? rossa".r
    )(
      vid"rphjb_EccociQuaStoLaDoCazzoStoDiQuaDiLaLiDavantiConTeLucettaRossa.mp4"
    ),
    ReplyBundleMessage.textToVideo(
      "eccoci qu[aà]".r
    )(
      vid"rphjb_EccociQuaStoLaDoCazzoStoDiQuaDiLaLiDavantiConTeLucettaRossa.mp4",
      vid"rphjb_WelaHeyHeyHeyDiNuovoInsieme.mp4"
    ),
    ReplyBundleMessage.textToVideo(
      "supermercato",
      "gli altri siamo noi"
    )(
      vid"rphjb_EtichetteSupermercatoSputatiMondo.mp4"
    ),
    ReplyBundleMessage.textToVideo(
      "sputat[oi] nel mondo".r
    )(
      vid"rphjb_EtichetteSupermercatoSputatiMondo.mp4",
      vid"rphjb_SputatiNelMondoTrovareFelicita.mp4"
    ),
    ReplyBundleMessage.textToVideo(
      "tendetemi le vostre spire"
    )(
      vid"rphjb_GerarchieInfernali2.mp4",
      vid"rphjb_GerarchieInfernali3.mp4"
    ),
    ReplyBundleMessage.textToVideo(
      "uno da bere",
      "sputatemi dalle vostre labbra",
      "figlie(,)? ma di quale madre".r,
      "fetenti feti di fede",
      "che ti inganna di notte",
      "che muore di giorno",
      "rovistandoti nell'immondo"
    )(
      vid"rphjb_GerarchieInfernali2.mp4"
    ),
    ReplyBundleMessage.textToVideo(
      "cambiato totalmente",
      "un maiale",
      "diventato grasso",
      "simpatico"
    )(
      vid"rphjb_FotoMalmsteen.mp4"
    ),
    ReplyBundleMessage.textToVideo(
      "le gambe"
    )(
      vid"rphjb_GambeInesistentiDueOssa.mp4",
      vid"rphjb_DanzaMacabra.mp4"
    ),
    ReplyBundleMessage.textToVideo(
      "tommy aldridge",
      "white[ ]?snake".r,
      "pat travers",
      "manona",
      "i('m|am) the leader",
      "quello stronzo",
      "io sono dio",
      "is god"
    )(
      vid"rphjb_TommyAldridgeYngwieMalmsteenWhereAreYouGoing.mp4"
    ),
    ReplyBundleMessage.textToVideo(
      "dire e il fare",
      "di mezzo il mare"
    )(
      vid"rphjb_TraDireFareMezzoMare.mp4"
    ),
    ReplyBundleMessage.textToVideo(
      "figura di merda"
    )(
      vid"rphjb_FiguraDiMerdaQuestaVoltaNo.mp4"
    ),
    ReplyBundleMessage.textToVideo(
      "la mia faccia"
    )(
      vid"rphjb_FiguracceDiscoSteveVai.mp4"
    ),
    ReplyBundleMessage.textToVideo(
      "il demonio",
      "venuto a trovarmi"
    )(
      vid"rphjb_IlDemonioVenutoATrovarmi.mp4"
    ),
    ReplyBundleMessage.textToVideo(
      "sta sul cazzo"
    )(
      vid"rphjb_MiStaSulCazzo.mp4"
    ),
    ReplyBundleMessage.textToVideo(
      "momenti di gloria"
    )(
      vid"rphjb_MomentiGloria.mp4"
    ),
    ReplyBundleMessage.textToVideo(
      "sepultura"
    )(
      vid"rphjb_SepolturaRisata.mp4"
    ),
    ReplyBundleMessage.textToVideo(
      "dischi volanti",
      "posa cenere"
    )(
      vid"rphjb_PiattiGhentDischiVolantiAlbaniaPortaCenere.mp4"
    ),
    ReplyBundleMessage.textToVideo(
      "è troppo tempo",
      "non ti vedo"
    )(
      vid"rphjb_TroppoTempoNonTiVedo.mp4"
    ),
    ReplyBundleMessage.textToVideo(
      "tagliano le gambe"
    )(
      vid"rphjb_QuaTaglianoGambe.mp4"
    ),
    ReplyBundleMessage.textToVideo(
      "proselito"
    )(
      vid"rphjb_NuovoProselito.mp4"
    ),
    ReplyBundleMessage.textToVideo(
      "colpa di uno"
    )(
      vid"rphjb_PerColpaUno.mp4"
    ),
    ReplyBundleMessage.textToVideo(
      "fassino",
      "ritratto della salute",
      "cadavere"
    )(
      vid"rphjb_SembraCadavereFassinoRitrattoSalute.mp4"
    ),
    ReplyBundleMessage.textToVideo(
      "vuoi (questo|la merda)".r,
      "io te (lo d[òo]|la suono)".r
    )(
      vid"rphjb_VuoiMerdaIoSuono.mp4"
    ),
    ReplyBundleMessage.textToVideo(
      "prendere in giro"
    )(
      vid"rphjb_GenteSchifosa.mp4"
    ),
    ReplyBundleMessage.textToVideo(
      "migliora(re)?".r
    )(
      vid"rphjb_SiPuoMigliorare.mp4",
      vid"rphjb_QuandoMiglioraStima.mp4"
    ),
    ReplyBundleMessage.textToVideo(
      "non mi fa rabbia",
      "fa ridere"
    )(
      vid"rphjb_NoRabbiaRidereMeNeFrego.mp4"
    ),
    ReplyBundleMessage.textToVideo(
      "questa sera",
      "ancora di pi[uù]+".r
    )(
      vid"rphjb_QuestaSeraAncoraDiPiu.mp4"
    ),
    ReplyBundleMessage.textToVideo(
      "paul gilbert"
    )(
      vid"rphjb_FesteACasaNicolaArigliano.mp4"
    ),
    ReplyBundleMessage.textToVideo(
      "le vie sono tante"
    )(
      vid"rphjb_VieSonoTanteMilioniDiMilioniMiCoglioniViaDelleAlbizzie22.mp4"
    ),
    ReplyBundleMessage.textToVideo(
      "essere libero"
    )(
      vid"rphjb_VoglioEssereLibero.mp4"
    ),
    ReplyBundleMessage.textToVideo(
      "vicissitudini"
    )(
      vid"rphjb_VicissitudiniPersoneInBestia.mp4"
    ),
    ReplyBundleMessage.textToVideo(
      "in bestia"
    )(
      vid"rphjb_VicissitudiniPersoneInBestia.mp4",
      vid"rphjb_SquallidaScorfanoRaganaCatafalcoAmbulante.mp4"
    ),
    ReplyBundleMessage.textToVideo(
      "un viaggio",
      "nella mente"
    )(
      vid"rphjb_ViaggioMenteUmana.mp4"
    ),
    ReplyBundleMessage.textToVideo(
      "di nuovo insieme",
      "hey hey"
    )(
      vid"rphjb_WelaHeyHeyHeyDiNuovoInsieme.mp4"
    ),
    ReplyBundleMessage.textToVideo(
      "sono simpatico"
    )(
      vid"rphjb_TantoSimpatico.mp4"
    ),
    ReplyBundleMessage.textToVideo(
      "micetto"
    )(
      vid"rphjb_SembriMicetto.mp4"
    ),
    ReplyBundleMessage.textToVideo(
      "contro il pisello"
    )(
      vid"rphjb_SbatteControPiselloSonoAbituatoEssereSbattutoLa.mp4"
    ),
    ReplyBundleMessage.textToVideo(
      "vivete",
      "sognate",
      "vivere per sempre",
      "morire oggi"
    )(
      vid"rphjb_SognateViverePerSempreViveteMorireOggi.mp4"
    ),
    ReplyBundleMessage.textToVideo(
      "(quanta|troppa) gente".r
    )(
      vid"rphjb_QuantaGenteTroppa.mp4"
    ),
    ReplyBundleMessage.textToVideo(
      "una fossa"
    )(
      vid"rphjb_FossaCollaSerpeSerpeFelicitaMusica.mp4"
    ),
    ReplyBundleMessage.textToVideo(
      "felicità"
    )(
      vid"rphjb_FossaCollaSerpeSerpeFelicitaMusica.mp4",
      vid"rphjb_SputatiNelMondoTrovareFelicita.mp4"
    ),
    ReplyBundleMessage.textToVideo(
      "i (dis)?gusti".r
    )(
      vid"rphjb_GustiPubblicoRappresentanoMieiDisgusti.mp4"
    ),
    ReplyBundleMessage.textToVideo(
      "che stronzo",
      "male di mente",
      "interviste",
      "non (ci|cene) siamo (mai )?accorti".r.tr(20)
    )(
      vid"rphjb_LoSapevoIoMaleDiMenteTimoTolki.mp4"
    ),
    ReplyBundleMessage.textToVideo(
      "timo tolkki"
    )(
      vid"rphjb_LoSapevoIoMaleDiMenteTimoTolki.mp4",
      vid"rphjb_MiAuguroTimoTolkiTourneeMondiale.mp4"
    ),
    ReplyBundleMessage.textToVideo(
      "coppia micidiale",
      "si è riformata"
    )(
      vid"rphjb_MigliorAmicoCoppiaMicidialeGianniNeri.mp4"
    ),
    ReplyBundleMessage.textToVideo(
      "non (ne )?posso (più|continuare|più continuare)".r
    )(
      vid"rphjb_NonPossoContinuareCosiGianni.mp4"
    ),
    ReplyBundleMessage.textToVideo(
      "mancano le parole",
      "le parole non esistono"
    )(
      vid"rphjb_GraziePerQuelloCheFaiPerMeMancanoLeParole.mp4"
    ),
    ReplyBundleMessage.textToVideo(
      "grazie!"
    )(
      vid"rphjb_GraziePerQuelloCheFaiPerMeMancanoLeParole.mp4",
      vid"rphjb_CuoreInMano.mp4"
    ),
    ReplyBundleMessage.textToVideo(
      "non siamo niente",
      "siamo esseri umani",
      "sudore",
      "zozzeria",
      "da togliere",
      "levare d[ia] dosso".r,
      "non contiamo niente"
    )(
      vid"rphjb_EsseriUmaniZozzeriaCarnePelleSputoSudoreSpermaNonContiamoNiente.mp4"
    ),
    ReplyBundleMessage.textToVideo(
      "giovenca",
      "varzetta",
      "me la sposo"
    )(
      vid"rphjb_GiovencaVarzettaSposoChissenefrega.mp4"
    ),
    ReplyBundleMessage.textToVideo(
      "sono fuso",
      "si sta fondendo"
    )(
      vid"rphjb_IlCervelloStaFondendoNonCapiscoUnCazzo.mp4"
    ),
    ReplyBundleMessage.textToVideo("gaio")(
      vid"rphjb_CantantePreferitoNonSonoGaio.mp4",
      vid"rphjb_GaioInGiallo.mp4"
    ),
    ReplyBundleMessage.textToVideo(
      "in giallo",
      "capelli (imbiondati|gialli)".r,
      "non sentire più emozioni",
      "dizione",
      "indigeni",
      "selvaggina",
      "palme"
    )(
      vid"rphjb_GaioInGiallo.mp4"
    ),
    ReplyBundleMessage.textToVideo(
      "napoletani",
      "fatevi vivi",
      "(ndo|dove) stanno i".r
    )(
      vid"rphjb_NapoletaniDoveStannoFateviVivi.mp4"
    ),
    ReplyBundleMessage.textToVideo(
      "è troppo",
      "indicibile",
      "non (ci sta|c'è) col cervello".r
    )(
      vid"rphjb_QuestoNoETroppoIndicibileSchifosa.mp4"
    ),
    ReplyBundleMessage.textToVideo(
      "giocattol(o|ino)".r
    )(
      vid"rphjb_CristoPinocchio.mp4"
    ),
    ReplyBundleMessage.textToVideo(
      "terribile maria",
      "dragata",
      "a ruota d'incenso",
      "gravidanza scorretta",
      "falegname da croci"
    )(
      vid"rphjb_PoesiaMaria.mp4",
      vid"rphjb_MiDragaStradeInferioriCristoPinocchioGerarchieInfernali.mp4"
    ),
    ReplyBundleMessage.textToVideo(
      "chiesa inferiore",
      "dio minore",
      "rabbia di un giudeo",
      "svastiche appese",
      "satanista acceso",
      "sahara di neve",
      "il mio fallo",
      "legata ai fili",
      "cesso d'amplificatore",
      "esibirne i fasti",
      "ultimo dei camionisti"
    )(
      vid"rphjb_PoesiaMaria.mp4"
    ),
    ReplyBundleMessage.textToVideo(
      "italia di oggi"
    )(
      vid"rphjb_ItaliaOggiFaSchifo.mp4"
    ),
    ReplyBundleMessage.textToVideo(
      "nel nulla"
    )(
      vid"rphjb_SparisceNelNulla.mp4",
      vid"rphjb_TuttiFinitiNelNulla.mp4"
    ),
    ReplyBundleMessage.textToVideo(
      "sparisce nel",
      "pi[uù] nero".r
    )(
      vid"rphjb_SparisceNelNulla.mp4"
    ),
    ReplyBundleMessage.textToVideo(
      "finiti nel"
    )(
      vid"rphjb_TuttiFinitiNelNulla.mp4"
    ),
    ReplyBundleMessage.textToVideo(
      "sputare addosso",
      "mi faccio schifo"
    )(
      vid"rphjb_SputareAddossoQuantoFaccioSchifo.mp4"
    ),
    ReplyBundleMessage.textToVideo(
      "mia stima"
    )(
      vid"rphjb_QuandoMiglioraStima.mp4"
    ),
    ReplyBundleMessage.textToVideo(
      "sto male",
      "rif[aà]".r,
      "(25|venticinque) anni".r
    )(
      vid"rphjb_RifaQuello25AnniStoMale.mp4"
    ),
    ReplyBundleMessage.textToVideo(
      "(1|un) minuto".r,
      "(2|due) minuti".r,
      "quanto c'ho?"
    )(
      vid"rphjb_UnMinutoDueMinuti.mp4"
    ),
    ReplyBundleMessage.textToVideo(
      "spinge in avanti",
      "una forza",
      "tiene per la coda"
    )(
      vid"rphjb_Attenzione.mp4",
      vid"rphjb_LegatiMaDaQualeSpagoCateneInvisibiliFilamentiOroFinoAlleStelleForzaRitraeIndietro.mp4"
    ),
    ReplyBundleMessage.textToVideo(
      "dream theater",
      "le ultime foto",
      "pelato",
      "mike portnoy",
      "capelli colorati",
      "john myung"
    )(
      vid"rphjb_FotoLookDreamTheater.mp4"
    ),
    ReplyBundleMessage.textToVideo(
      "malvagia",
      "la mia fine",
      "togliermi di mezzo",
      "[s]?parla alle spalle".r,
      "giornalisti",
      "carpi",
      "mingoli",
      "antonella",
      "dario",
      "mi volete distruggere"
    )(
      vid"rphjb_GenteMalvagiaDistruggereSparlaGiornalistiSchifosiCarpiMingoliAntonellaDario.mp4"
    ),
    ReplyBundleMessage.textToVideo(
      "gruppi (nuovi|che spaccano il culo)".r,
      "intramezzate",
      "ibride",
      "stacchi all'unisono"
    )(
      vid"rphjb_GruppiNuoviSpaccanoCuloAbbellimentiRivoltiMordentiContrappunti.mp4"
    ),
    ReplyBundleMessage.textToVideo(
      "che h[ao] trovato".r,
      "tutti coglioni",
      "dispendio",
      "intelletto",
      "per prova",
      "manici",
      "legni",
      "magneti",
      "pedali",
      "coni da (15|12|quindici|dodici)".r,
      "vari intrugli",
      "casse",
      "jack",
      "basta che non sia rotto"
    )(
      vid"rphjb_HoTrovatoIoTuttiCoglioniFortunaDispendioIntellettoProvaCasseConiPedaliJack.mp4"
    ),
    ReplyBundleMessage.textToVideo(
      "(il grande |l')amore".r,
      "dal (bene|male)".r,
      "chi ti sta vicino"
    )(
      vid"rphjb_IlGrandeAmoreDalMaleAmoreVero.mp4"
    ),
    ReplyBundleMessage.textToVideo(
      "confraternita",
      "alla collina",
      "edicolante",
      "bibbie usate",
      "inchioda le mani",
      "sul suo portone",
      "ferire il tramonto",
      "tramonto di cristo"
    )(
      vid"rphjb_IlSimposioDelMetalloCristoEdicolantePerFerireTramontoCristo.mp4"
    ),
    ReplyBundleMessage.textToVideo(
      "in alto",
      "cieli dell'arte",
      "incontrare dio"
    )(
      vid"rphjb_InAltoCieliArteDioDonna.mp4"
    ),
    ReplyBundleMessage.textToVideo(
      "insegnanti",
      "dove stanno"
    )(
      vid"rphjb_InsegnantiImportantiInsegnantiColtiDoveStanno.mp4"
    ),
    ReplyBundleMessage.textToVideo(
      "(mica|non) sono come gli altri".r,
      "fuori dal (coro|gregge)".r
    )(
      vid"rphjb_IoMicaSonoComeGliAltriBestiaFuoriDalGreggeVoceFuoriDalCoro.mp4"
    ),
    ReplyBundleMessage.textToVideo(
      "contratti",
      "vieni (qu[iì]|anche te|ad affrontare me)".r,
      "la tua faccia",
      "se (c')?hai il coraggio".r,
      "covo del metallo",
      "che non funzionano"
    )(
      vid"rphjb_IoParloDicoLaVeritaContrattiFantomaticiVieniQuiFaiVedereFacciaCovoDelMetalloSimposio.mp4"
    ),
    ReplyBundleMessage.textToVideo(
      "affidarmi",
      "lacci malefici",
      "spaghi",
      "corde",
      "retoriche",
      "ostrogote",
      "bilsamiche",
      "non mi interessano",
      "circonciso",
      "che ci uniscono",
      "prigione"
    )(
      vid"rphjb_LacciMaleficiSpaghiCordeCateneRetoricheOstrogoteBislamicheCirconcisoPrigioneVita.mp4"
    ),
    ReplyBundleMessage.textToVideo(
      "catene"
    )(
      vid"rphjb_LacciMaleficiSpaghiCordeCateneRetoricheOstrogoteBislamicheCirconcisoPrigioneVita.mp4",
      vid"rphjb_LegatiMaDaQualeSpagoCateneInvisibiliFilamentiOroFinoAlleStelleForzaRitraeIndietro.mp4"
    ),
    ReplyBundleMessage.textToVideo(
      "filamenti d'oro",
      "fino alle stelle"
    )(
      vid"rphjb_LegatiMaDaQualeSpagoCateneInvisibiliFilamentiOroFinoAlleStelleForzaRitraeIndietro.mp4"
    ),
    ReplyBundleMessage.textToVideo(
      "malattia",
      "behringer",
      "ferma(rsi|ti)".r
    )(
      vid"rphjb_MalattiaBehringerNonRiesconoAFermarsi.mp4"
    ),
    ReplyBundleMessage.textToVideo(
      "tourn[eèé]{2}".r,
      "(è|deve essere|dev'essere) ripreso".r
    )(
      vid"rphjb_MiAuguroTimoTolkiTourneeMondiale.mp4"
    ),
    ReplyBundleMessage.textToVideo(
      "non tutto il male viene (per|a) nuocere".r,
      "non sono (uno |mica )?(scemo|stolto)".r.tr(14),
      "so dove (io )?voglio".r,
      "so (quello che|cosa) (sto facendo|faccio)".r.tr(14)
    )(
      vid"rphjb_NonTuttoIlMaleVieneNuocereSoQuelloCheStoFacendoNoScemoStolto.mp4"
    ),
    ReplyBundleMessage.textToVideo(
      "metallari",
      "incalliti",
      "costretto",
      "ingiurie",
      "ci credono",
      "fino in fondo",
      "mi (faccio|lavo) i denti".r,
      "come dei monaci"
    )(
      vid"rphjb_MetallariPiuIncallitiFinoInFondoInContinuazioneMonaci.mp4"
    ),
    ReplyBundleMessage.textToVideo(
      "tagliare con un coltello",
      "sangue (vero )?che scende".r,
      "amore (col|miscelato al) dolore".r
    )(
      vid"rphjb_MioPubblicoIoAmoServitoreTagliareConUnColtelloSangueVeroAmoreMiscelatoAlDolore.mp4"
    ),
    ReplyBundleMessage.textToVideo(
      "mio pubblico",
      "servitore"
    )(
      vid"rphjb_MioPubblicoIoAmoServitoreTagliareConUnColtelloSangueVeroAmoreMiscelatoAlDolore.mp4",
      vid"rphjb_PistoleroVeloceAmmazzarePersoneServitorePubblico.mp4"
    ),
    ReplyBundleMessage.textToVideo(
      "storie d'amore",
      "nei particolari",
      "(freg[aà]|fregare) d[ie] meno".r
    )(
      vid"rphjb_NonPiaceEssereRipetitivoVarzettaStorieAmoreMeNePuoFregaDeMeno.mp4"
    ),
    ReplyBundleMessage.textToVideo(
      "i tuoi occhi",
      "fari abbaglianti",
      "ci sono davanti",
      "ammaestrato",
      "non sono malato"
    )(
      vid"rphjb_OcchiFariAbbagliantiCiSonoDavantiAmmaestratoNonSonoMalato.mp4"
    ),
    ReplyBundleMessage.textToVideo(
      "sono (spariti|falliti)".r,
      "non c'è più nessuno",
      "per colpa vostra",
      "negozi importanti"
    )(
      vid"rphjb_SonoSparitiNegoziSonoFallitiColpaVostra.mp4"
    ),
    ReplyBundleMessage.textToVideo(
      "in degli occhi",
      "hanno visto l[aà]",
      "trasmissione",
      "può essere (anche )?un (piacere|letale)".r.tr(20),
      "cambia la percezione",
      "auditiva",
      "discorsiva",
      "dire altre cose"
    )(
      vid"rphjb_OcchiVistoLaDifficileGuardareTrasmissioneLetaleTiCambiaGerarchieInfernali.mp4"
    ),
    ReplyBundleMessage.textToVideo(
      "è difficile",
      "potrebbe essere (così )?semplice".r,
      "un'altra donna"
    )(
      vid"rphjb_ParloDiDNonSonoGayCosiDifficileSemplice.mp4"
    ),
    ReplyBundleMessage.textToVideo(
      "pistolero",
      "la pistola",
      "ammazzare",
      "lo farei ancora adesso",
      "amo la gente"
    )(
      vid"rphjb_PistoleroVeloceAmmazzarePersoneServitorePubblico.mp4"
    ),
    ReplyBundleMessage.textToVideo(
      "belle donne",
      "grande amico"
    )(
      vid"rphjb_PiaccionoBelleDonneVallettaGianniNeriGrandeAmico.mp4"
    ),
    ReplyBundleMessage.textToVideo(
      "donne (trasgressive|dominatrici)".r,
      "soggiogare dal marito",
      "le nostre (madri|nonne|trisavole)".r
    )(
      vid"rphjb_PiaccionoDonneTrasgressiveDominatrici.mp4"
    ),
    ReplyBundleMessage.textToVideo(
      "sto frocio",
      "più italiano",
      "(non|nun) (te|ti) capiscono".r,
      "italianizzare",
      "accento (all')?inglese".r
    )(
      vid"rphjb_PiuItalianoItalianizzareStoFrocio.mp4"
    ),
    ReplyBundleMessage.textToVideo(
      "il primo disco",
      "beatles",
      "ragione per cui io ero (venuto al mondo|nato)".r.tr(27),
      "troppo forte"
    )(
      vid"rphjb_PrimoDiscoBeatlesRagioneVenutoMondoTroppoForte.mp4"
    ),
    ReplyBundleMessage.textToVideo(
      "voi stessi",
      "non trovate nessun'altro",
      "siete solo voi"
    )(
      vid"rphjb_QuandoGuardateDentroVoiStessiNonTrovateNessunAltroSoloVoi.mp4"
    ),
    ReplyBundleMessage.textToVideo(
      "questa no",
      "questa è (mistica|ibrida|contorta)".r,
      "può essere (anche )?dolce".r
    )(
      vid"rphjb_QuestaNoMisticaIbridaContortaDolceFunzioniUomoDonna.mp4"
    ),
    ReplyBundleMessage.textToVideo(
      "preoccupata"
    )(
      vid"rphjb_CuoreInMano.mp4"
    ),
    ReplyBundleMessage.textToVideo(
      "una bestia",
      "il bello d",
      "distinguiamo"
    )(
      vid"rphjb_RockettariComeBestieCravattaPassaportoStronzi.mp4"
    ),
    ReplyBundleMessage.textToVideo(
      "rolling stones",
      "ribellioni",
      "rivoluzioni",
      "giustiziere"
    )(
      vid"rphjb_RollingStonesJimiHendrixPoetiMaledettiFemministeControculturaRivoluzioniRibelioni5DitaCazzottoInFacciaGiustiziere.mp4"
    ),
    ReplyBundleMessage.textToVideo(
      "allegro",
      "s(i |')ammazza".r
    )(
      vid"rphjb_SembravaAllegroPoiSAmmazzaMioPensiero.mp4"
    ),
    ReplyBundleMessage.textToVideo(
      "sfumato",
      "grande richiesta"
    )(
      vid"rphjb_SfumatoGrandeRichiesta.mp4"
    ),
    ReplyBundleMessage.textToVideo(
      "persone attente",
      "tipo cazzuto",
      "calmatev[ei]".r,
      "\\bseni\\b".r,
      "letti (circolari|pieni)".r,
      "l'erba",
      "la coca"
    )(
      vid"rphjb_RingrazioPersoneAttenteDonneToccavanoSeniAnni70LettiPieniErbaCoca.mp4"
    ),
    ReplyBundleMessage.textToVideo(
      "sarà lei",
      "a farmi male"
    )(
      vid"rphjb_SaraLeiAFarmiMale.mp4"
    ),
    ReplyBundleMessage.textToVideo(
      "verano",
      "si muore",
      "tanti muoiono",
      "non sono mai morto",
      "caus[ae] natural[ei]".r
    )(
      vid"rphjb_VecchiAmiciAnni70VeranoSostanzeImproprieNonSonoMaiMorto.mp4"
    ),
    ReplyBundleMessage.textToVideo(
      "può significare",
      "sono attento",
      "foglia che si muove",
      "attagliato dal tempo",
      "tutto ok"
    )(
      vid"rphjb_SonoAttentoVaTuttoBeneAttagliatoTempo5DitaPugno.mp4"
    ),
    ReplyBundleMessage.textToVideo(
      "nell'inferno",
      "guerre",
      "malattie",
      "malvagi",
      "nemici",
      "tocca a te",
      "momento (ibrido|contorto|instabile)".r,
      "stabilità"
    )(
      vid"rphjb_SputatiNelMondoTrovareFelicita.mp4"
    ),
    ReplyBundleMessage.textToVideo(
      "intenditrice",
      "scorfano",
      "ragana",
      "catafalco ambulante",
      "luce più eterna",
      "spaccherei tutto"
    )(
      vid"rphjb_SquallidaScorfanoRaganaCatafalcoAmbulante.mp4"
    ),
    ReplyBundleMessage.textToVideo(
      "andrea carpi",
      "paolo bianco",
      "mancusi"
    )(
      vid"rphjb_VergognatiMancusiPaoloBiancoTastieristaAttentiPerStradaAndreaCarpi.mp4"
    ),
    ReplyBundleMessage.textToVideo(
      "sporco frocio",
      "sto su di (chi|te)".r
    )(
      vid"rphjb_StoSuDiChiTeSembrareSporcoFrocio.mp4"
    ),
    ReplyBundleMessage.textToVideo(
      "tic nervosi",
      "fottuta in un cesso",
      "ai concerti col papà"
    )(
      vid"rphjb_StoriaMarlinManson.mp4"
    ),
    ReplyBundleMessage.textToVideo(
      "come l'olio",
      "sotto il tavolo",
      "leccare per terra",
      "più spazio",
      "25 minuti"
    )(
      vid"rphjb_StorieTanteTempoPassaOlioLeccarePiuSpazio.mp4"
    ),
    ReplyBundleMessage.textToVideo(
      "l'unico mezzo",
      "trasformare la materia",
      "in spirito"
    )(
      vid"rphjb_UnicoMezzoUccidereMorteMateriaSpirito.mp4"
    ),
    ReplyBundleMessage.textToVideo(
      "ti ho chiamato",
      "richiamarmi",
      "non c[ie] posso fa(re)? niente".r.tr(22),
      "faccio quello che posso",
      "arrivo fino a[d]? un certo punto".r,
      "m[ei] butto d[ei] sotto".r
    )(
      vid"rphjb_TiHoChiamatoRichiamarmiFaccioQuelloChePosso.mp4"
    ),
    ReplyBundleMessage.textToVideo(
      "stringer(e|si) la mano".r,
      "non ti conosco",
      "saluto metal",
      "🤘",
      "ci abbracceremo",
      "se te lo meriti"
    )(
      vid"rphjb_StringerciLaManoNonTiConoscoSalutoMetalAbbracceremoForseMeriti.mp4"
    ),
    ReplyBundleMessage.textToVideo(
      "succhiarvi",
      "fumarvi",
      "dominatore"
    )(
      vid"rphjb_SucchiarviCaramelleFumarviCalpestareTacchiASpilloDominatore.mp4"
    ),
    ReplyBundleMessage.textToVideo(
      "donna giusta",
      "donne sbagliate",
      "egregia"
    )(
      vid"rphjb_ViSalutaLinguaSuDonnaGiusta.mp4"
    ),
    ReplyBundleMessage.textToVideo(
      "volto nuovo",
      "ogni settimana",
      "sempre un'uomo",
      "un po' i seni",
      "un po' le cosce",
      "calze a rete",
      "camerawoman"
    )(
      vid"rphjb_VoltoNuovoSempreUomoDonnaSeniCosceTacchiCalzeCameranWoman.mp4"
    ),
    ReplyBundleMessage.textToVideo(
      "la vostra mente (abbietta|calpestata)?".r,
      "mafia",
      "punto di partenza"
    )(
      vid"rphjb_VostraMenteAbbiettaCalpestataNoDirettiveEstremismoMafiaPoliticaPartitiStessaManfrina.mp4"
    ),
    ReplyBundleMessage.textToVideo(
      "melense"
    )(
      vid"rphjb_CanzonettePoesieAuschwitzCervello.mp4"
    ),
    ReplyBundleMessage.textToVideo(
      "gambale"
    )(
      vid"rphjb_GambaleCHaDeluso.mp4"
    ),
    ReplyBundleMessage.textToVideo(
      "molto colpito"
    )(
      vid"rphjb_MoltoColpito.mp4"
    ),
    ReplyBundleMessage.textToVideo(
      "grande!"
    )(
      vid"rphjb_Grande.mp4"
    ),
    ReplyBundleMessage.textToVideo(
      "figlio di puttana"
    )(
      vid"rphjb_FiglioDiPuttana.mp4"
    ),
    ReplyBundleMessage.textToVideo(
      "che sicuramente"
    )(
      vid"rphjb_CheSicuramente.mp4"
    ),
    ReplyBundleMessage.textToVideo(
      "il pubblico sta cambiando",
      "stanno invecchiando",
      "non riescono a seguire",
      "il loro pubblico"
    )(
      vid"rphjb_IlPubblicoStaCambiandoLoroInvecchiando.mp4"
    ),
    ReplyBundleMessage.textToVideo(
      "questo è il bello",
      "riesco a seguire",
      "metallaro",
      "skinhead",
      "punkabbestia",
      "tossicomane",
      "case di cura",
      "in galera",
      "non (c'ha|c'è) più futuro".r,
      "senza futuro",
      "litiga coi genitori",
      "che si ribella",
      "ribelle",
      "musicista",
      "incazzato per natura",
      "forse (un giorno)? lo troverai".r,
      "attraverso noi",
      "armi da guerra"
    )(
      vid"rphjb_SeguireTuttiListaPersone.mp4"
    ),
    ReplyBundleMessage.textToVideo(
      "\\bromeo\\b".r,
      "(settanta |settant'|70 )anni",
      "actor studio",
      "new york",
      "piccolo pubblico",
      "gli alievi",
      "entra in scena",
      "settant'enne",
      "alla prima battuta",
      "insistere"
    )(
      vid"rphjb_ParteDiRomeo.mp4"
    ),
    ReplyBundleMessage.textToVideo(
      "in linea"
    )(
      vid"rphjb_TelefonataInLinea.mp4"
    ),
    ReplyBundleMessage.textToVideo(
      "viva l'italia"
    )(
      vid"rphjb_VivaLItalia.mp4"
    ),
    ReplyBundleMessage.textToVideo(
      "abruzzo"
    )(
      vid"rphjb_VivaLAbruzzo.mp4"
    ),
    ReplyBundleMessage.textToVideo(
      "palermo",
      "balatonicus",
      "coglioni rotti"
    )(
      vid"rphjb_GeneriMusicali2.mp4"
    ),
    ReplyBundleMessage.textToVideo(
      "alpheus"
    )(
      vid"rphjb_EtichetteSulleBottiglieDiWhiskeyAlpheus.mp4",
      vid"rphjb_BiscionePiatti.mp4"
    ),
    ReplyBundleMessage.textToVideo(
      "latranti",
      "carne incancrenata",
      "avvoltoi",
      "voluttà"
    )(
      vid"rphjb_Schifosi4.mp4"
    ),
    ReplyBundleMessage.textToVideo(
      "era vero"
    )(vid"rphjb_EraVero.mp4"),
    ReplyBundleMessage.textToVideo(
      "(una|na) farsa".r
    )(
      vid"rphjb_ArbitriPagatiTruccatiGiocatoriDopatiMoggiCONITifosiUltrasTuttaFarsaGiocoCalcio.mp4",
      vid"rphjb_Ciao2001FarsaManson.mp4"
    ),
    ReplyBundleMessage.textToVideo(
      "in mille trappole",
      "l'unica salvezza"
    )(
      vid"rphjb_AmmaestrareIlDolore.mp4"
    )
  )
}
