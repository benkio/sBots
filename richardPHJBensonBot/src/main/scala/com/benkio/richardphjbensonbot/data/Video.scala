package com.benkio.richardphjbensonbot.data

import cats.Applicative
import com.benkio.telegrambotinfrastructure.model.*

object Video {

  def messageRepliesVideoData[F[_]: Applicative]: List[ReplyBundleMessage[F]] = List(
    ReplyBundleMessage.textToVideo[F](
      "soldati",
      "vorrei (tanto )?risentirlo".r.tr(17)
    )(
      vid"rphjb_AmiciVeriVecchiSoldati.mp4"
    ),
    ReplyBundleMessage.textToVideo[F](
      "amici veri",
      "veri amici"
    )(
      vid"rphjb_AmiciVeriVecchiSoldati.mp4",
      vid"rphjb_CuoreInMano.mp4"
    ),
    ReplyBundleMessage.textToVideo[F](
      "accor(data|dana)".r.tr(9)
    )(
      vid"rphjb_Accordana.mp4"
    ),
    ReplyBundleMessage.textToVideo[F](
      "\\brap\\b".r.tr(3),
      "musica italiana",
      "vi regalo"
    )(
      vid"rphjb_RapMusicaMelodicaListaCantanti.mp4"
    ),
    ReplyBundleMessage.textToVideo[F](
      "tullio pane",
      "otello profazio",
      "mario lanza",
      "luciano tajoli"
    )(
      vid"rphjb_RapMusicaMelodicaListaCantanti.mp4",
      vid"rphjb_QuesitoRegaloOtelloProfazioMarioLanzaTullioPaneLucianoTaglioliGianniCeleste.mp4"
    ),
    ReplyBundleMessage.textToVideo[F](
      "uo[m]+ini d'affari".r.tr(15),
      "andò in america",
      "non c'è la benzina",
      "a sbattere da tutte le parti",
      "mancano i motori"
    )(
      vid"rphjb_EricClaptonDrogaUominiAffari.mp4"
    ),
    ReplyBundleMessage.textToVideo[F](
      "rampolli",
      "studi a boston",
      "borghesia alta",
      "idoli delle mamme",
      "figliole",
      "con tanti soldi"
    )(
      vid"rphjb_Rampolli.mp4"
    ),
    ReplyBundleMessage.textToVideo[F](
      "giacca",
      "cravatta",
      "passaporto degli stronzi"
    )(
      vid"rphjb_RocchettariCapelliCortiGiaccaCravattaPassaportoStronzi.mp4",
      vid"rphjb_RockettariComeBestieCravattaPassaportoStronzi.mp4"
    ),
    ReplyBundleMessage.textToVideo[F](
      "capelli corti",
    )(
      vid"rphjb_RocchettariCapelliCortiGiaccaCravattaPassaportoStronzi.mp4",
      vid"rphjb_PetrucciCapelliCorti.mp4"
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
      "streghe",
      "inquisizione",
      "al rogo",
      "effetti (impropri|ipnotici)".r.tr(16)
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
      "(coi|quei) due punti".r.tr(13),
      "illusioni",
      "musica (non )?è grande".r.tr(15),
      "re[a]?l illusions".r.tr(13)
    )(
      vid"rphjb_RelIllusions.mp4"
    ),
    ReplyBundleMessage.textToVideo[F](
      "copertina"
    )(
      vid"rphjb_PrimoDiscoBeatlesRagioneVenutoMondoTroppoForte.mp4",
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
      vid"rphjb_VitaNemicoCervello.mp4",
      vid"rphjb_PoesiaDirittoPaura.mp4"
    ),
    ReplyBundleMessage.textToVideo[F](
      "cervello pensante",
      "stupidità incresciosa"
    )(
      vid"rphjb_CervelloPensante.mp4"
    ),
    ReplyBundleMessage.textToVideo[F](
      "percussionista",
    )(
      vid"rphjb_CollaSerpeSigarettePercussionista.mp4",
      vid"rphjb_TommyAldridgeYngwieMalmsteenWhereAreYouGoing.mp4"
    ),
    ReplyBundleMessage.textToVideo[F](
      "batterista"
    )(
      vid"rphjb_CollaSerpeSigarettePercussionista.mp4",
      vid"rphjb_TommyAldridgeYngwieMalmsteenWhereAreYouGoing.mp4",
      vid"rphjb_GruppiNuoviSpaccanoCuloAbbellimentiRivoltiMordentiContrappunti.mp4"
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
      "(c|g)hi(t|d)a[r]+is(t|d)a pi(u|ù|ú) velo(c|g)e".r.tr(22),
      "gli uomini(, |...)?mi fanno schifo",
      "le donne un po' meno"
    )(
      vid"rphjb_Arivato.mp4"
    ),
    ReplyBundleMessage.textToVideo[F](
      "il più veloce"
    )(
      vid"rphjb_Arivato.mp4",
      vid"rphjb_PistoleroVeloceAmmazzarePersoneServitorePubblico.mp4"
    ),
    ReplyBundleMessage.textToVideo[F](
      "\\bbeat\\b".r.tr(4),
      "(e poi[ ,]?[ ]?){2,}".r.tr(10),
      "qualche volta vedo lei",
      "non la vedo pi[uù]".r.tr(15),
      "mi (si )?piange il cuor[e]?".r.tr(20),
      "sfasciavamo tutti gli strumenti",
    )(
      vid"rphjb_AssoloBeat.mp4"
    ),
    ReplyBundleMessage.textToVideo[F](
      "buon compleanno"
    )(
      vid"rphjb_Compleanno.mp4",
      vid"rphjb_AuguriCompleanno.mp4"
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
      "rock machine",
      "(un po'|un attimo) (di|de) esercitazione".r.tr(23)
    )(
      vid"rphjb_RockMachineIntro.mp4"
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
      "distorsione",
      "più pulito",
      "john travolta",
      "video didattico",
      "fate venire le vostre (madri|mogli|fidanzate)".r.tr(27)
    )(
      vid"rphjb_DelirioDelSabatoSera.mp4"
    ),
    ReplyBundleMessage.textToVideo[F](
      "marilyn manson"
    )(
      vid"rphjb_Ciao2001.mp4",
      vid"rphjb_StoriaMarlinManson.mp4"
    ),
    ReplyBundleMessage.textToVideo[F](
      "capelli lunghi"
    )(
      vid"rphjb_PetrucciCapelliCorti.mp4",
      vid"rphjb_FotoLookDreamTheater.mp4"
    ),
    ReplyBundleMessage.textToVideo[F](
      "impiegat[oi]".r.tr(9),
    )(
      vid"rphjb_PetrucciCapelliCorti.mp4",
      vid"rphjb_PoesiaArtistiImpiegati.mp4"
    ),
    ReplyBundleMessage.textToVideo[F](
      "petrucci"
    )(
      vid"rphjb_PetrucciCapelliCorti.mp4",
      vid"rphjb_FotoLookDreamTheater.mp4"
    ),
    ReplyBundleMessage.textToVideo[F](
      "i genesis",
      "tecno(-| )thrash".r.tr(12),
      "van der graaf generator",
      "emerson(,)? lake (e|&) palmer".r.tr(22),
      "gentle giant",
      "jetro tull",
      "marillion",
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
      "in ginocchio",
      "inginocchiatevi",
      "nuovo messia"
    )(
      vid"rphjb_MetteteviInGinocchio.mp4"
    ),
    ReplyBundleMessage.textToVideo[F](
      "sigarett[ea]".r.tr(9)
    )(
      vid"rphjb_Sigarette.mp4",
      vid"rphjb_CollaSerpeSigarettePercussionista.mp4",
      vid"rphjb_SucchiarviCaramelleFumarviCalpestareTacchiASpilloDominatore.mp4"
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
      "radio rock",
      "informazioni sbagliate"
    )(
      vid"rphjb_RadioRockErrori.mp4"
    ),
    ReplyBundleMessage.textToVideo[F](
      "jordan rudess"
    )(
      vid"rphjb_RadioRockErrori.mp4",
      vid"rphjb_FotoLookDreamTheater.mp4"
    ),
    ReplyBundleMessage.textToVideo[F](
      "uccidere"
    )(
      vid"rphjb_UccidereUnaPersona.mp4"
    ),
    ReplyBundleMessage.textToVideo[F](
      "distruggere il proprio sesso",
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
      "gioco (io )?del gatto e (voi )?del (ratto|topo)".r.tr(24)
    )(vid"rphjb_DubbioScantinatiGiocoRattoGatto.mp4"),
    ReplyBundleMessage.textToVideo[F](
      "londra"
    )(vid"rphjb_Londra.mp4"),
    ReplyBundleMessage.textToVideo[F](
      "peggio cose"
    )(
      vid"rphjb_Venerdi.mp4",
      vid"rphjb_DanzaMacabra.mp4"
    ),
    ReplyBundleMessage.textToVideo[F](
      "albero grande",
      "anche un('| )amplificatore".r.tr(22),
      "tutti i (suoi )?frutti ti d[aà]".r.tr(20),
      "per quanti gliene domandi",
      "sempre uno ne troverà",
      "il (fiore|frutto)".r.tr(8),
      "la foglia",
      "di tutto s[ie] spoglia".r.tr(19),
    )(
      vid"rphjb_PoesiaRock.mp4",
      vid"rphjb_PoesiaRockAlberoGrande.mp4"
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
      "più vicino alla fine",
      "hai un anno di più",
      "felicitazioni",
      "ma che siamo noi",
      "rumor[ie] di vetro e di metallo".r.tr(27)
    )(
      vid"rphjb_AuguriCompleanno.mp4"
    ),
    ReplyBundleMessage.textToVideo[F](
      "(3|tre) minuti".r.tr(8),
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
      "james labrie"
    )(
      vid"rphjb_Labrie.mp4",
      vid"rphjb_FotoLookDreamTheater.mp4"
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
    )(
      vid"rphjb_PortlandVancuverFanculo.mp4"
    ),
    ReplyBundleMessage.textToVideo[F](
      "vancuv(er|a)".r.tr(7)
    )(
      vid"rphjb_PortlandVancuverFanculo.mp4",
      vid"rphjb_StoriaMarlinManson.mp4"
    ),
    ReplyBundleMessage.textToVideo[F](
      "tempo al tempo",
      "non ne ho più"
    )(
      vid"rphjb_TempoAlTempo.mp4"
    ),
    ReplyBundleMessage.textToVideo[F](
      "ginecologo",
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
      "parlo"
    )(
      vid"rphjb_NonMiFregaParloIo.mp4",
      vid"rphjb_IoParloDicoLaVeritaContrattiFantomaticiVieniQuiFaiVedereFacciaCovoDelMetalloSimposio.mp4"
    ),
    ReplyBundleMessage.textToVideo[F](
      "sono il demonio"
    )(
      vid"rphjb_SonoDemonio.mp4"
    ),
    ReplyBundleMessage.textToVideo[F](
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
      "bel[s]?ize".r.tr(6),
      "nei punti più rilevanti",
      "non figura il mio nome",
      "sotto codici",
      "non si possono risalire",
      "governo (svizzero|germanico|tedesco)".r.tr(15),
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
      vid"rphjb_UnitiQualeSpago.mp4",
      vid"rphjb_LacciMaleficiSpaghiCordeCateneRetoricheOstrogoteBislamicheCirconcisoPrigioneVita.mp4",
      vid"rphjb_LegatiMaDaQualeSpagoCateneInvisibiliFilamentiOroFinoAlleStelleForzaRitraeIndietro.mp4"
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
      "silenzio[,]?( dovuto)?".r.tr(11),
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
      "un po' gay",
      "la (collezione|colazione)".r.tr(12),
      "t[ei] sei sbagliato".r.tr(12),
    )(
      vid"rphjb_DylanDog.mp4",
    ),
    ReplyBundleMessage.textToVideo[F](
      "poeti maledetti"
    )(
      vid"rphjb_DylanDog.mp4",
      vid"rphjb_RollingStonesJimiHendrixPoetiMaledettiFemministeControculturaRivoluzioniRibelioni5DitaCazzottoInFacciaGiustiziere.mp4"
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
      "trovamelo",
      "me l'ha trovato"
    )(
      vid"rphjb_AngeloTrovamelo.mp4"
    ),
    ReplyBundleMessage.textToVideo[F](
      "(due|2) orecchie".r.tr(10),
      "(una|1) bocca".r.tr(6)
    )(
      vid"rphjb_2Orecchie1Bocca.mp4",
      vid"rphjb_2Orecchie1Bocca2.mp4"
    ),
    ReplyBundleMessage.textToVideo[F](
      "limitazioni",
      "quante(,|...)? troppe".r.tr(12)
    )(
      vid"rphjb_BicchiereSputoLimitazioniUomoDonna.mp4"
    ),
    ReplyBundleMessage.textToVideo[F](
      "l'uomo(,|...)? la donna".r.tr(15)
    )(
      vid"rphjb_BicchiereSputoLimitazioniUomoDonna.mp4",
      vid"rphjb_QuestaNoMisticaIbridaContortaDolceFunzioniUomoDonna.mp4"
    ),
    ReplyBundleMessage.textToVideo[F](
      "cambia canale"
    )(
      vid"rphjb_CambiaCanaleBruttoFrocio.mp4"
    ),
    ReplyBundleMessage.textToVideo[F](
      "garage",
      "varazze",
    )(
      vid"rphjb_AnimaGarageVarazze.mp4"
    ),
    ReplyBundleMessage.textToVideo[F](
      "arbitri (truccati|pagati)".r.tr(14),
      "giocatori dopati",
      "(gioco del|il) calcio".r.tr(9),
      "\\bmoggi\\b".r.tr(5),
      "il coni\\b".r.tr(7),
      "\\bultr(à|as)\\b".r.tr(5),
      "direttore del coni",
      "parole grosse",
      "fare del (male|bene)".r.tr(13),
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
      .textToVideo[F]("gay")(
        vid"rphjb_BarzellettaPapaSonoGayPride.mp4",
        vid"rphjb_CantantePreferitoNonSonoGaio.mp4",
        vid"rphjb_NonSonoGay.mp4",
        vid"rphjb_ParloDiDNonSonoGayCosiDifficileSemplice.mp4"
      ),
    ReplyBundleMessage.textToVideo[F]("barzelletta")(
      vid"rphjb_BarzellettaGesuCristoParadisoPurgatorioMalmsteenDio.mp4",
      vid"rphjb_BarzellettaPapaSonoGayPride.mp4",
      vid"rphjb_BarzellettaPoliticaGinecologo.mp4"
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
      vid"rphjb_DaHollywood.mp4",
      vid"rphjb_GruppiNuoviSpaccanoCuloAbbellimentiRivoltiMordentiContrappunti.mp4"
    ),
    ReplyBundleMessage.textToVideo[F](
      "due bocce",
      "innamorato",
      "veronica frieman",
      "benedictum"
    )(vid"rphjb_CantanteDonnaVoceDaUomoDueBocceCosiInnamoratoPerdutamente.mp4"),
    ReplyBundleMessage
      .textToVideo[F](
        "verita",
        "verità"
      )(
        vid"rphjb_AltraCazzataVeritaSembranoCazzate.mp4",
        vid"rphjb_Verita.mp4",
        vid"rphjb_IoParloDicoLaVeritaContrattiFantomaticiVieniQuiFaiVedereFacciaCovoDelMetalloSimposio.mp4"
      ),
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
      "rivoli",
      "impennate",
      "colori"
    )(
      vid"rphjb_AbbellimentiRivoltiRivoliMordentiImpennateColori.mp4"
    ),
    ReplyBundleMessage.textToVideo[F](
      "abbellimenti",
      "mordenti",
      "rivolti"
    )(
      vid"rphjb_AbbellimentiRivoltiRivoliMordentiImpennateColori.mp4",
      vid"rphjb_GruppiNuoviSpaccanoCuloAbbellimentiRivoltiMordentiContrappunti.mp4"
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
      "(di|il) liscio".r.tr(9),
      "(di|il) rumba".r.tr(8),
      "cha[ ]?cha[ ]?cha".r.tr(9),
      "canzon[ei] napoletan[ae]".r.tr(18)
    )(
      vid"rphjb_CuginiCampagnaGiardinoSempliciHomoSapiensLiscioRumbaChaChaChaCanzoneNapoletanaOsanna.mp4"
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
      "piatti",
    )(
      vid"rphjb_BiscionePiatti.mp4",
      vid"rphjb_GhentPiattiAlbania.mp4",
      vid"rphjb_PiattiGhentAlbaniaCiPensa.mp4",
      vid"rphjb_PiattiGhentDischiVolantiAlbaniaPortaCenere.mp4"
    ),
    ReplyBundleMessage.textToVideo[F](
      "amicizia",
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
      "tramonto d'estate",
      "boschi in penombra",
      "per un ideale"
    )(
      vid"rphjb_PoesiaMadre.mp4"
    ),
    ReplyBundleMessage.textToVideo[F](
      "di tutti i colori"
    )(
      vid"rphjb_PoesiaMadre.mp4",
      vid"rphjb_RingrazioPersoneAttenteDonneToccavanoSeniAnni70LettiPieniErbaCoca.mp4"
    ),
    ReplyBundleMessage.textToVideo[F](
      "difficile guardare",
      "(vedere|guardare) l[aà]".r.tr(9)
    )(
      vid"rphjb_DifficileGuardareTuboCatodicoNienteCiSepara.mp4",
      vid"rphjb_LaDoveDifficileGuardare.mp4",
      vid"rphjb_OcchiVistoLaDifficileGuardareTrasmissioneLetaleTiCambiaGerarchieInfernali.mp4"
    ),
    ReplyBundleMessage.textToVideo[F](
      "tubo catodico",
    )(
      vid"rphjb_DifficileGuardareTuboCatodicoNienteCiSepara.mp4"
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
      "supermercato",
      "gli altri siamo noi"
    )(
      vid"rphjb_EtichetteSupermercatoSputatiMondo.mp4"
    ),
    ReplyBundleMessage.textToVideo[F](
      "sputat[oi] nel mondo".r.tr(17),
    )(
      vid"rphjb_EtichetteSupermercatoSputatiMondo.mp4",
      vid"rphjb_SputatiNelMondoTrovareFelicita.mp4"
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
      "i('m|am) the leader",
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
      vid"rphjb_GenteSchifosa.mp4",
      vid"rphjb_QuestoNoETroppoIndicibileSchifosa.mp4"
    ),
    ReplyBundleMessage.textToVideo[F](
      "prendere in giro"
    )(
      vid"rphjb_GenteSchifosa.mp4"
    ),
    ReplyBundleMessage.textToVideo[F](
      "migliora(re)?".r.tr(8)
    )(
      vid"rphjb_SiPuoMigliorare.mp4",
      vid"rphjb_QuandoMiglioraStima.mp4"
    ),
    ReplyBundleMessage.textToVideo[F](
      "ballerino",
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
      "questa sera",
      "ancora di pi[uù]+".r.tr(13)
    )(
      vid"rphjb_QuestaSeraAncoraDiPiu.mp4"
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
    )(
      vid"rphjb_VicissitudiniPersoneInBestia.mp4"
    ),
    ReplyBundleMessage.textToVideo[F](
      "in bestia",
    )(
      vid"rphjb_VicissitudiniPersoneInBestia.mp4",
      vid"rphjb_SquallidaScorfanoRaganaCatafalcoAmbulante.mp4"
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
    )(
      vid"rphjb_FossaCollaSerpeSerpeFelicitaMusica.mp4"
    ),
    ReplyBundleMessage.textToVideo[F](
      "felicità"
    )(
      vid"rphjb_FossaCollaSerpeSerpeFelicitaMusica.mp4",
      vid"rphjb_SputatiNelMondoTrovareFelicita.mp4"
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
    )(
      vid"rphjb_LoSapevoIoMaleDiMenteTimoTolki.mp4"
    ),
    ReplyBundleMessage.textToVideo[F](
      "timo tolkki"
    )(
      vid"rphjb_LoSapevoIoMaleDiMenteTimoTolki.mp4",
      vid"rphjb_MiAuguroTimoTolkiTourneeMondiale.mp4"
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
      "mancano le parole",
      "le parole non esistono"
    )(
      vid"rphjb_GraziePerQuelloCheFaiPerMeMancanoLeParole.mp4"
    ),
    ReplyBundleMessage.textToVideo[F](
      "grazie"
    )(
      vid"rphjb_GraziePerQuelloCheFaiPerMeMancanoLeParole.mp4",
      vid"rphjb_CuoreInMano.mp4"
    ),
    ReplyBundleMessage.textToVideo[F](
      "non siamo niente",
      "siamo esseri umani",
      "sudore",
      "zozzeria",
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
    ),
    ReplyBundleMessage.textToVideo[F](
      "sono fuso",
      "si sta fondendo"
    )(
      vid"rphjb_IlCervelloStaFondendoNonCapiscoUnCazzo.mp4"
    ),
    ReplyBundleMessage.textToVideo[F](
      "dischi"
    )(
      vid"rphjb_PienoDiDischi.mp4"
    ),
    ReplyBundleMessage.textToVideo[F]("gaio")(
      vid"rphjb_CantantePreferitoNonSonoGaio.mp4",
      vid"rphjb_GaioInGiallo.mp4"
    ),
    ReplyBundleMessage.textToVideo[F](
      "in giallo",
      "capelli (imbiondati|gialli)".r.tr(14),
      "pazienza (d[ie] ferro| che hanno i marines)".r.tr(17),
      "non sentire più emozioni",
      "fa[s]+( )?[b]+inder".r.tr(9),
      "dizione",
      "indigeni",
      "selvaggina",
      "palme"
    )(
      vid"rphjb_GaioInGiallo.mp4"
    ),
    ReplyBundleMessage.textToVideo[F](
      "napoletani",
      "fatevi vivi",
      "(ndo|dove) stanno i".r.tr(12)
    )(
      vid"rphjb_NapoletaniDoveStannoFateviVivi.mp4"
    ),
    ReplyBundleMessage.textToVideo[F](
      "mi sento un (pezzo di merda|l'ultimo)".r.tr(20)
    )(
      vid"rphjb_MiSentoPezzoDiMerdaUltimoGiuGiu.mp4"
    ),
    ReplyBundleMessage.textToVideo[F](
      "è troppo",
      "indicibile",
      "non (ci sta|c'è) col cervello".r.tr(20)
    )(
      vid"rphjb_QuestoNoETroppoIndicibileSchifosa.mp4"
    ),
    ReplyBundleMessage.textToVideo[F](
      "cazzi tuoi",
      "cazz[oi] piccol[io]".r.tr(13),
    )(
      vid"rphjb_CazziTuoiPiccoliEssereUmanoMinimo.mp4"
    ),
    ReplyBundleMessage.textToVideo[F](
      "essere umano"
    )(
      vid"rphjb_CazziTuoiPiccoliEssereUmanoMinimo.mp4",
      vid"rphjb_ScusateEssereUmanoTosse.mp4"
    ),
    ReplyBundleMessage.textToVideo[F](
      "giocattol(o|ino)".r.tr(9)
    )(
      vid"rphjb_CristoPinocchio.mp4"
    ),
    ReplyBundleMessage.textToVideo[F](
      "terribile maria",
      "dragata",
      "a ruota d'incenso",
      "gravidanza scorretta",
      "falegname da croci"
    )(
      vid"rphjb_PoesiaMaria.mp4",
      vid"rphjb_MiDragaStradeInferioriCristoPinocchioGerarchieInfernali.mp4"
    ),
    ReplyBundleMessage.textToVideo[F](
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
      "ultimo dei camionisti",
    )(
      vid"rphjb_PoesiaMaria.mp4"
    ),
    ReplyBundleMessage.textToVideo[F](
      "italia di oggi"
    )(
      vid"rphjb_ItaliaOggiFaSchifo.mp4"
    ),
    ReplyBundleMessage.textToVideo[F](
      "nel nulla"
    )(
      vid"rphjb_SparisceNelNulla.mp4",
      vid"rphjb_TuttiFinitiNelNulla.mp4"
    ),
    ReplyBundleMessage.textToVideo[F](
      "sparisce nel",
      "pi[uù] nero".r.tr(8)
    )(
      vid"rphjb_SparisceNelNulla.mp4"
    ),
    ReplyBundleMessage.textToVideo[F](
      "finiti nel"
    )(
      vid"rphjb_TuttiFinitiNelNulla.mp4"
    ),
    ReplyBundleMessage.textToVideo[F](
      "sputare addosso",
      "mi faccio schifo"
    )(
      vid"rphjb_SputareAddossoQuantoFaccioSchifo.mp4"
    ),
    ReplyBundleMessage.textToVideo[F](
      "mia stima"
    )(
      vid"rphjb_QuandoMiglioraStima.mp4"
    ),
    ReplyBundleMessage.textToVideo[F](
      "suonare libero"
    )(
      vid"rphjb_SuonareLibero.mp4"
    ),
    ReplyBundleMessage.textToVideo[F](
      "sto male",
      "rif[aà]".r.tr(4),
      "(25|venticinque) anni".r.tr(7)
    )(
      vid"rphjb_RifaQuello25AnniStoMale.mp4"
    ),
    ReplyBundleMessage.textToVideo[F](
      "(1|un) minuto".r.tr(8),
      "(2|due) minuti".r.tr(8),
      "quanto c'ho?"
    )(
      vid"rphjb_UnMinutoDueMinuti.mp4"
    ),
    ReplyBundleMessage.textToVideo[F](
      "spinge in avanti",
      "una forza",
      "tiene per la coda"
    )(
      vid"rphjb_Attenzione.mp4",
      vid"rphjb_LegatiMaDaQualeSpagoCateneInvisibiliFilamentiOroFinoAlleStelleForzaRitraeIndietro.mp4"
    ),
    ReplyBundleMessage.textToVideo[F](
      "dream theater",
      "le ultime foto",
      "pelato",
      "mike portnoy",
      "capelli colorati",
      "john myung",
      "\\blook\\b".r.tr(4),
    )(
      vid"rphjb_FotoLookDreamTheater.mp4"
    ),
    ReplyBundleMessage.textToVideo[F](
      "malvagia",
      "la mia fine",
      "togliermi di mezzo",
      "[s]?parla alle spalle".r.tr(17),
      "giornalisti",
      "carpi",
      "mingoli",
      "antonella",
      "dario",
      "mi volete distruggere",
    )(
      vid"rphjb_GenteMalvagiaDistruggereSparlaGiornalistiSchifosiCarpiMingoliAntonellaDario.mp4"
    ),
    ReplyBundleMessage.textToVideo[F](
      "gruppi (nuovi|che spaccano il culo)".r.tr(13),
      "intramezzate",
      "ibride",
      "contrappunt[oi]".r.tr(12),
      "alterazioni",
      "stacchi all'unisono",
      "tempi (composti|dispari|pari)".r.tr(10),
      "armonie",
    )(
      vid"rphjb_GruppiNuoviSpaccanoCuloAbbellimentiRivoltiMordentiContrappunti.mp4"
    ),
    ReplyBundleMessage.textToVideo[F](
      "che h[ao] trovato".r.tr(14),
      "tutti coglioni",
      "dispendio",
      "intelletto",
      "per prova",
      "manici",
      "legni",
      "magneti",
      "pedali",
      "coni da (15|12|quindici|dodici)".r.tr(10),
      "vari intrugli",
      "casse",
      "jack",
      "basta che non sia rotto",
    )(
      vid"rphjb_HoTrovatoIoTuttiCoglioniFortunaDispendioIntellettoProvaCasseConiPedaliJack.mp4"
    ),
    ReplyBundleMessage.textToVideo[F](
      "(il grande |l')amore".r.tr(7),
      "dal (bene|male)".r.tr(8),
      "chi ti sta vicino",
    )(
      vid"rphjb_IlGrandeAmoreDalMaleAmoreVero.mp4"
    ),
    ReplyBundleMessage.textToVideo[F](
      "confraternita",
      "alla collina",
      "edicolante",
      "bibbie usate",
      "inchioda le mani",
      "sul suo portone",
      "ferire il tramonto",
      "tramonto di cristo",
    )(
      vid"rphjb_IlSimposioDelMetalloCristoEdicolantePerFerireTramontoCristo.mp4"
    ),
    ReplyBundleMessage.textToVideo[F](
      "in alto",
      "cieli dell'arte",
      "incontrare dio"
    )(
      vid"rphjb_InAltoCieliArteDioDonna.mp4"
    ),
    ReplyBundleMessage.textToVideo[F](
      "insegnanti",
      "dove stanno"
    )(
      vid"rphjb_InsegnantiImportantiInsegnantiColtiDoveStanno.mp4"
    ),
    ReplyBundleMessage.textToVideo[F](
      "(mica|non) sono come gli altri".r.tr(23),
      "fuori dal (coro|gregge)".r.tr(14)
    )(
      vid"rphjb_IoMicaSonoComeGliAltriBestiaFuoriDalGreggeVoceFuoriDalCoro.mp4"
    ),
    ReplyBundleMessage.textToVideo[F](
      "contratti",
      "vieni (qu[iì]|anche te|ad affrontare me)".r.tr(9),
      "la tua faccia",
      "se (c')?hai il coraggio".r.tr(18),
      "covo del metallo",
    )(
      vid"rphjb_IoParloDicoLaVeritaContrattiFantomaticiVieniQuiFaiVedereFacciaCovoDelMetalloSimposio.mp4"
    ),
    ReplyBundleMessage.textToVideo[F](
      "lacci malefici",
      "spaghi",
      "corde",
      "retoriche",
      "ostrogote",
      "bilsamiche",
      "non mi interessano",
      "circonciso",
      "che ci uniscono",
      "prigione",
    )(
      vid"rphjb_LacciMaleficiSpaghiCordeCateneRetoricheOstrogoteBislamicheCirconcisoPrigioneVita.mp4"
    ),
    ReplyBundleMessage.textToVideo[F](
      "catene"
    )(
      vid"rphjb_LacciMaleficiSpaghiCordeCateneRetoricheOstrogoteBislamicheCirconcisoPrigioneVita.mp4",
      vid"rphjb_LegatiMaDaQualeSpagoCateneInvisibiliFilamentiOroFinoAlleStelleForzaRitraeIndietro.mp4"
    ),
    ReplyBundleMessage.textToVideo[F](
      "filamenti d'oro",
      "fino alle stelle"
    )(
      vid"rphjb_LegatiMaDaQualeSpagoCateneInvisibiliFilamentiOroFinoAlleStelleForzaRitraeIndietro.mp4"
    ),
    ReplyBundleMessage.textToVideo[F](
      "malattia",
      "behringer",
      "ferma(rsi|ti)".r.tr(7)
    )(
      vid"rphjb_MalattiaBehringerNonRiesconoAFermarsi.mp4"
    ),
    ReplyBundleMessage.textToVideo[F](
      "tourn[eèé]{2}".r.tr(7),
      "(è|deve essere|dev'essere) ripreso".r.tr(9)
    )(
      vid"rphjb_MiAuguroTimoTolkiTourneeMondiale.mp4"
    ),
    ReplyBundleMessage.textToVideo[F](
      "non tutto il male viene per nuocere",
      "non sono (uno )?(scemo|stolto)",
      "so dove voglio",
      "so (quello che|cosa) sto facendo",
    )(
      vid"rphjb_NonTuttoIlMaleVieneNuocereSoQuelloCheStoFacendoNoScemoStolto.mp4"
    ),
    ReplyBundleMessage.textToVideo[F](
      "metallari",
      "incalliti",
      "costretto",
      "ingiurie",
      "ci credono",
      "fino in fondo",
      "mi (faccio|lavo) i denti".r.tr(15),
      "come dei monaci",
    )(
      vid"rphjb_MetallariPiuIncallitiFinoInFondoInContinuazioneMonaci.mp4"
    ),
    ReplyBundleMessage.textToVideo[F](
      "tagliare con un coltello",
      "sangue (vero )?che scende".r.tr(17),
      "amore (col|miscelato al) dolore".r.tr(16),
    )(
      vid"rphjb_MioPubblicoIoAmoServitoreTagliareConUnColtelloSangueVeroAmoreMiscelatoAlDolore.mp4"
    ),
    ReplyBundleMessage.textToVideo[F](
      "mio pubblico",
      "servitore"
    )(
      vid"rphjb_MioPubblicoIoAmoServitoreTagliareConUnColtelloSangueVeroAmoreMiscelatoAlDolore.mp4",
      vid"rphjb_PistoleroVeloceAmmazzarePersoneServitorePubblico.mp4"
    ),
    ReplyBundleMessage.textToVideo[F](
      "ripetitiva",
      "si ripetono",
      "storie d'amore",
      "sempre le stesse",
      "nei particolari",
      "(freg[aà]|fregare) d[ie] meno".r.tr(12),
    )(
      vid"rphjb_NonPiaceEssereRipetitivoVarzettaStorieAmoreMeNePuoFregaDeMeno.mp4"
    ),
    ReplyBundleMessage.textToVideo[F](
      "i tuoi occhi",
      "fari abbaglianti",
      "ci sono davanti",
      "ammaestrato",
      "non sono malato",
    )(
      vid"rphjb_OcchiFariAbbagliantiCiSonoDavantiAmmaestratoNonSonoMalato.mp4"
    ),
    ReplyBundleMessage.textToVideo[F](
      "sono (spariti|falliti)".r.tr(12),
      "non c'è più nessuno",
      "per colpa vostra",
      "negozi importanti",
    )(
      vid"rphjb_SonoSparitiNegoziSonoFallitiColpaVostra.mp4"
    ),
    ReplyBundleMessage.textToVideo[F](
      "in degli occhi",
      "hanno visto l[aà]",
      "trasmissione",
      "può essere (anche )?un (piacere|letale)".r.tr(20),
      "cambia la percezione",
      "auditiva",
      "discorsiva",
      "dire altre cose",
    )(
      vid"rphjb_OcchiVistoLaDifficileGuardareTrasmissioneLetaleTiCambiaGerarchieInfernali.mp4"
    ),
    ReplyBundleMessage.textToVideo[F](
      "è difficile",
      "potrebbe essere (così )?semplice".r.tr(24),
      "un'altra donna",
    )(
      vid"rphjb_ParloDiDNonSonoGayCosiDifficileSemplice.mp4"
    ),
    ReplyBundleMessage.textToVideo[F](
      "pistolero",
      "la pistola",
      "ammazzare",
      "lo farei ancora adesso",
      "amo la gente",
    )(
      vid"rphjb_PistoleroVeloceAmmazzarePersoneServitorePubblico.mp4"
    ),
    ReplyBundleMessage.textToVideo[F](
      "belle donne",
      "grande amico"
    )(
      vid"rphjb_PiaccionoBelleDonneVallettaGianniNeriGrandeAmico.mp4"
    ),
    ReplyBundleMessage.textToVideo[F](
      "donne (trasgressive|dominatrici)".r.tr(17),
      "soggiogare dal marito",
      "le nostre (madri|nonne|trisavole)".r.tr(15),
    )(
      vid"rphjb_PiaccionoDonneTrasgressiveDominatrici.mp4"
    ),
    ReplyBundleMessage.textToVideo[F](
      "sto frocio",
      "più italiano",
      "(non|nun) (te|ti) capiscono".r.tr(16),
      "italianizzare",
      "accento (all')?inglese".r.tr(16),
    )(
      vid"rphjb_PiuItalianoItalianizzareStoFrocio.mp4"
    ),
    ReplyBundleMessage.textToVideo[F](
      "il primo disco",
      "beatles",
      "ragione per cui io ero (venuto al mondo|nato)".r.tr(27),
      "troppo forte",
    )(
      vid"rphjb_PrimoDiscoBeatlesRagioneVenutoMondoTroppoForte.mp4"
    ),
    ReplyBundleMessage.textToVideo[F](
      "voi stessi",
      "non trovate nessun'altro",
      "siete solo voi"
    )(
      vid"rphjb_QuandoGuardateDentroVoiStessiNonTrovateNessunAltroSoloVoi.mp4"
    ),
    ReplyBundleMessage.textToVideo[F](
      "distrutto totalmente"
    )(
      vid"rphjb_QuelloCheTiMeritiFinoAllaFineDistruttoTotalmente.mp4"
    ),
    ReplyBundleMessage.textToVideo[F](
      "chi risponde",
      "quesito",
      "in regalo"
    )(
      vid"rphjb_QuesitoRegaloOtelloProfazioMarioLanzaTullioPaneLucianoTaglioliGianniCeleste.mp4"
    ),
    ReplyBundleMessage.textToVideo[F](
      "questa no",
      "questa è (mistica|ibrida|contorta)".r.tr(15),
      "può essere (anche )?dolce".r.tr(16)
    )(
      vid"rphjb_QuestaNoMisticaIbridaContortaDolceFunzioniUomoDonna.mp4"
    ),
    ReplyBundleMessage.textToVideo[F](
      "preoccupata"
    )(
      vid"rphjb_CuoreInMano.mp4"
    ),
    ReplyBundleMessage.textToVideo[F](
      "una bestia",
      "il bello d",
      "distinguiamo"
    )(
      vid"rphjb_RockettariComeBestieCravattaPassaportoStronzi.mp4"
    ),
    ReplyBundleMessage.textToVideo[F](
      "rolling stones",
      "ribellioni",
      "rivoluzioni",
      "il sangue",
      "giustiziere",
    )(
      vid"rphjb_RollingStonesJimiHendrixPoetiMaledettiFemministeControculturaRivoluzioniRibelioni5DitaCazzottoInFacciaGiustiziere.mp4"
    ),
    ReplyBundleMessage.textToVideo[F](
      "allegro",
      "s(i |')ammazza".r.tr(9)
    )(
      vid"rphjb_SembravaAllegroPoiSAmmazzaMioPensiero.mp4"
    ),
    ReplyBundleMessage.textToVideo[F](
      "sfumato",
      "grande richiesta"
    )(
      vid"rphjb_SfumatoGrandeRichiesta.mp4"
    ),
    ReplyBundleMessage.textToVideo[F](
      "ringrazio molto",
      "persone attente",
      "tipo cazzuto",
      "calmatev[ei]".r.tr(8),
      "\\bseni\\b".r.tr(4),
      "letti (circolari|pieni)".r.tr(11),
      "l'erba",
      "la coca",
    )(
      vid"rphjb_RingrazioPersoneAttenteDonneToccavanoSeniAnni70LettiPieniErbaCoca.mp4"
    ),
    ReplyBundleMessage.textToVideo[F](
      "anni (settanta|70)".r.tr(7),
    )(
      vid"rphjb_RingrazioPersoneAttenteDonneToccavanoSeniAnni70LettiPieniErbaCoca.mp4",
      vid"rphjb_VecchiAmiciAnni70VeranoSostanzeImproprieNonSonoMaiMorto.mp4"
    ),
    ReplyBundleMessage.textToVideo[F](
      "sarà lei",
      "a farmi male"
    )(
      vid"rphjb_SaraLeiAFarmiMale.mp4"
    ),
    ReplyBundleMessage.textToVideo[F](
      "verano",
      "si muore",
      "tanti muoiono",
      "non sono mai morto",
      "caus[ae] natural[ei]".r.tr(14)
    )(
      vid"rphjb_VecchiAmiciAnni70VeranoSostanzeImproprieNonSonoMaiMorto.mp4"
    ),
    ReplyBundleMessage.textToVideo[F](
      "può significare",
      "sono attento",
      "foglia che si muove",
      "attagliato dal tempo",
      "tutto ok",
    )(
      vid"rphjb_SonoAttentoVaTuttoBeneAttagliatoTempo5DitaPugno.mp4"
    ),
    ReplyBundleMessage.textToVideo[F](
      "nell'inferno",
      "guerre",
      "malattie",
      "malvagi",
      "nemici",
      "tocca a te",
      "momento (ibrido|contorto|instabile)".r.tr(15),
      "stabilità",
    )(
      vid"rphjb_SputatiNelMondoTrovareFelicita.mp4"
    ),
    ReplyBundleMessage.textToVideo[F](
      "intenditrice",
      "scorfano",
      "ragana",
      "catafalco ambulante",
      "pamela anderson",
      "luce più eterna",
      "spaccherei tutto"
    )(
      vid"rphjb_SquallidaScorfanoRaganaCatafalcoAmbulante.mp4"
    ),
    ReplyBundleMessage.textToVideo[F](
      "andrea carpi",
      "paolo bianco",
      "mancusi"
    )(
      vid"rphjb_VergognatiMancusiPaoloBiancoTastieristaAttentiPerStradaAndreaCarpi.mp4"
    ),
    ReplyBundleMessage.textToVideo[F](
      "sporco frocio",
      "sto su di (chi|te)".r.tr(12)
    )(
      vid"rphjb_StoSuDiChiTeSembrareSporcoFrocio.mp4"
    ),
    ReplyBundleMessage.textToVideo[F](
      "tic nervosi",
      "truccato",
      "fottuta in un cesso",
      "ai concerti col papà"
    )(
      vid"rphjb_StoriaMarlinManson.mp4"
    ),
    ReplyBundleMessage.textToVideo[F](
      "come l'olio",
      "sotto il tavolo",
      "leccare per terra",
      "più spazio",
      "25 minuti",
    )(
      vid"rphjb_StorieTanteTempoPassaOlioLeccarePiuSpazio.mp4"
    ),
    ReplyBundleMessage.textToVideo[F](
      "l'unico mezzo",
      "trasformare la materia",
      "in spirito",
    )(
      vid"rphjb_UnicoMezzoUccidereMorteMateriaSpirito.mp4"
    ),
    ReplyBundleMessage.textToVideo[F](
      "ti ho chiamato",
      "richiamarmi",
      "non c[ie] posso fa(re)? niente".r.tr(22),
      "faccio quello che posso",
      "arrivo fino a[d]? un certo punto".r.tr(28),
      "m[ei] butto d[ei] sotto".r.tr(17),
    )(
      vid"rphjb_TiHoChiamatoRichiamarmiFaccioQuelloChePosso.mp4"
    ),
    ReplyBundleMessage.textToVideo[F](
      "stringer(e|si) la mano".r.tr(17),
      "non ti conosco",
      "saluto metal",
      "🤘",
      "ci abbracceremo",
      "se te lo meriti",
    )(
      vid"rphjb_StringerciLaManoNonTiConoscoSalutoMetalAbbracceremoForseMeriti.mp4"
    ),
    ReplyBundleMessage.textToVideo[F](
      "succhiarvi",
      "fumarvi",
      "dominatore",
    )(
      vid"rphjb_SucchiarviCaramelleFumarviCalpestareTacchiASpilloDominatore.mp4"
    ),
    ReplyBundleMessage.textToVideo[F](
      "tacchi a spillo"
    )(
      vid"rphjb_SucchiarviCaramelleFumarviCalpestareTacchiASpilloDominatore.mp4",
      vid"rphjb_VoltoNuovoSempreUomoDonnaSeniCosceTacchiCalzeCameranWoman.mp4"
    ),
    ReplyBundleMessage.textToVideo[F](
      "lingua",
      "donna giusta",
      "donne sbagliate",
      "egregia",
    )(
      vid"rphjb_ViSalutaLinguaSuDonnaGiusta.mp4"
    ),
    ReplyBundleMessage.textToVideo[F](
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
    ReplyBundleMessage.textToVideo[F](
      "mente (abbietta|calpestata)".r.tr(14),
      "mafia",
      "punto di partenza",
      "estremismo",
    )(
      vid"rphjb_VostraMenteAbbiettaCalpestataNoDirettiveEstremismoMafiaPoliticaPartitiStessaManfrina.mp4"
    ),
    ReplyBundleMessage.textToVideo[F](
      "auschwitz",
      "melense"
    )(
      vid"rphjb_CanzonettePoesieAuschwitzCervello.mp4"
    ),
    ReplyBundleMessage.textToVideo[F](
      "gambale",
      "di una volta"
    )(
      vid"rphjb_GambaleCHaDeluso.mp4"
    ),
    ReplyBundleMessage.textToVideo[F](
      "molto colpito"
    )(
      vid"rphjb_MoltoColpito.mp4"
    ),
    ReplyBundleMessage.textToVideo[F](
      "grande!"
    )(
      vid"rphjb_Grande.mp4"
    ),
    ReplyBundleMessage.textToVideo[F](
      "figlio di puttana"
    )(
      vid"rphjb_FiglioDiPuttana.mp4"
    ),
    ReplyBundleMessage.textToVideo[F](
      "ciao bella",
      "\\bcome va\\b".r.tr(7),
      "bella gioia"
    )(
      vid"rphjb_CiaoBellaCameVaBellaGioia.mp4"
    ),
    ReplyBundleMessage.textToVideo[F](
      "che sicuramente"
    )(
      vid"rphjb_CheSicuramente.mp4"
    ),
    ReplyBundleMessage.textToVideo[F](
      "andate (direttamente )?a[ ]?[f]*fanculo".r.tr(16)
    )(
      vid"rphjb_AndateAFanculo.mp4"
    ),
    ReplyBundleMessage.textToVideo[F](
      "il pubblico sta cambiando",
      "stanno invecchiando",
      "non riescono a seguire",
      "il loro pubblico"
    )(
      vid"rphjb_IlPubblicoStaCambiandoLoroInvecchiando.mp4"
    ),
    ReplyBundleMessage.textToVideo[F](
      "questo è il bello",
      "riesco a seguire",
      "metallaro",
      "skinhead",
      "punkabbestia",
      "tossicomane",
      "case di cura",
      "in galera",
      "non (c'ha|c'è) più futuro".r.tr(18),
      "senza futuro",
      "litiga coi genitori",
      "che si ribella",
      "ribelle",
      "musicista",
      "incazzato per natura",
      "forse (un giorno)? lo troverai".r.tr(18),
      "attraverso noi",
      "armi da guerra"
    )(
      vid"rphjb_SeguireTuttiListaPersone.mp4"
    ),
    ReplyBundleMessage.textToVideo[F](
      "romeo",
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
    ReplyBundleMessage.textToVideo[F](
      "come mai"
    )(
      vid"rphjb_ComeMai.mp4"
    ),
    ReplyBundleMessage.textToVideo[F](
      "in linea"
    )(
      vid"rphjb_TelefonataInLinea.mp4"
    ),
    ReplyBundleMessage.textToVideo[F](
      "viva l'italia"
    )(
      vid"rphjb_VivaLItalia.mp4"
    ),
    ReplyBundleMessage.textToVideo[F](
      "abruzzo"
    )(
      vid"rphjb_VivaLAbruzzo.mp4"
    ),
    ReplyBundleMessage.textToVideo[F](
      "palermo",
      "balatonicus",
      "coglioni rotti"
    )(
      vid"rphjb_GeneriMusicali2.mp4"
    )
  )
}
