package com.benkio.richardphjbensonbot.data

import cats.Applicative
import com.benkio.telegrambotinfrastructure.messagefiltering.*
import com.benkio.telegrambotinfrastructure.model.*

object Audio {

  def messageRepliesAudioData[F[_]: Applicative]: List[ReplyBundleMessage[F]] = List(
    ReplyBundleMessage.textToMedia[F](
      stt"come state stasera"
    )(
      mf"rphjb_LetSGoodStateBene.mp3"
    ),
    ReplyBundleMessage.textToMedia[F](
      stt"tastierista"
    )(
      mf"rphjb_Tastierista.mp3"
    ),
    ReplyBundleMessage.textToMedia[F](
      stt"caprette",
      stt"acidi",
      stt"pomodori",
      stt"legumi",
      stt"ragni",
      stt"male il collo",
    )(
      mf"rphjb_ListaMaleCollo.mp3"
    ),
    ReplyBundleMessage.textToMedia[F](
      stt"lo sapevo"
    )(
      mf"rphjb_LoSapevoIo.mp3"
    ),
    ReplyBundleMessage.textToMedia[F](
      stt"mi auguro"
    )(
      mf"rphjb_IoMiAuguro.mp3"
    ),
    ReplyBundleMessage.textToMedia[F](
      stt"non leggo"
    )(
      mf"rphjb_NonLeggoQuelloCheScrivete.mp3"
    ),
    ReplyBundleMessage.textToMedia[F](
      stt"devo sopportare"
    )(
      mf"rphjb_DevoSopportare.mp3"
    ),
    ReplyBundleMessage.textToMedia[F](
      stt"non mi ricordo più"
    )(
      mf"rphjb_NonMiRicordoPiu.mp3"
    ),
    ReplyBundleMessage.textToMedia[F](
      stt"pensato a tutto",
      stt"accontentare tutti"
    )(
      mf"rphjb_PensatoATutto.mp3"
    ),
    ReplyBundleMessage.textToMedia[F](
      stt"se è un amico"
    )(
      mf"rphjb_VedereAmico.mp3"
    ),
    ReplyBundleMessage.textToMedia[F](
      stt"perchè l'ho fatto",
      stt"non do spiegazioni"
    )(
      mf"rphjb_PercheLHoFatto.mp3"
    ),
    ReplyBundleMessage.textToMedia[F](
      stt"non ho detto tutto",
      stt"ascoltami"
    )(
      mf"rphjb_NonHoDettoTutto.mp3"
    ),
    ReplyBundleMessage.textToMedia[F](
      stt"non mi calmo"
    )(
      mf"rphjb_NonMiCalmo.mp3"
    ),
    ReplyBundleMessage.textToMedia[F](
      stt"devo dire che"
    )(
      mf"rphjb_DevoDireChe.mp3"
    ),
    ReplyBundleMessage.textToMedia[F](
      stt"eccomi qua"
    )(
      mf"rphjb_EccomiQua.mp3"
    ),
    ReplyBundleMessage.textToMedia[F](
      stt"animali"
    )(
      mf"rphjb_Animali.mp3",
      mf"rphjb_Animali.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      "(mi sento|sto) meglio".r.tr(10)
    )(
      mf"rphjb_MiSentoMeglio.mp3"
    ),
    ReplyBundleMessage.textToMedia[F](
      stt"altari",
      stt"realtà",
    )(
      mf"rphjb_AltariFatiscentiRealta.mp3",
      mf"rphjb_AltariFatiscentiRealta.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      stt"non è vero"
    )(
      mf"rphjb_NonEVero.mp3"
    ),
    ReplyBundleMessage.textToMedia[F](
      stt"completamente nudo"
    )(
      mf"rphjb_CompletamenteNudo.mp3"
    ),
    ReplyBundleMessage.textToMedia[F](
      stt"dovrei lavarmelo di più",
      stt"il cazzo me lo pulisci un'altra volta",
    )(
      mf"rphjb_LavareCazzo.mp3"
    ),
    ReplyBundleMessage.textToMedia[F](
      "giù( giù)+".r.tr(7)
    )(
      mf"rphjb_GiuGiuGiu.mp3"
    ),
    ReplyBundleMessage.textToMedia[F](
      stt"viale zara",
      "cas(a|e) chius(a|e)".r.tr(11)
    )(
      mf"rphjb_VialeZara.mp3"
    ),
    ReplyBundleMessage.textToMedia[F](
      stt"tocca il culo"
    )(
      mf"rphjb_MiToccaIlCulo.mp3"
    ),
    ReplyBundleMessage.textToMedia[F](
      stt"negli occhiali",
      stt"sulla spalla",
      stt"gianguido",
    )(
      mf"rphjb_PannaOcchialiSpalla.mp3"
    ),
    ReplyBundleMessage.textToMedia[F](
      stt"state zitti"
    )(
      mf"rphjb_StateZittiZozziUltimi.mp3"
    ),
    ReplyBundleMessage.textToMedia[F](
      stt"solo io"
    )(
      mf"rphjb_SoloIo.mp3"
    ),
    ReplyBundleMessage.textToMedia[F](
      "(s(ono|o'|ò)?|saranno) cazzi vostri".r.tr(15)
    )(
      mf"rphjb_SarannoCazziVostri.mp3",
      mf"rphjb_SoCazziVostriStasera.mp4",
      mf"rphjb_SoCazziVostriGuaioPureCazziMia.mp4",
    ),
    ReplyBundleMessage.textToMedia[F](
      "poteri (ter[r]+ib[b]+ili|demoniaci)".r.tr(16)
    )(
      mf"rphjb_PoteriDemoniaci.mp3"
    ),
    ReplyBundleMessage.textToMedia[F](
      "sono( pure)? italiane".r.tr(13),
      "non so(no)? ungheresi".r.tr(16)
    )(
      mf"rphjb_ItalianeUngheresi.mp3"
    ),
    ReplyBundleMessage.textToMedia[F](
      "\\bcolpevole\\b".r.tr(9)
    )(
      mf"rphjb_IlColpevole.mp3"
    ),
    ReplyBundleMessage.textToMedia[F](
      stt"vi spacco il culo"
    )(
      mf"rphjb_ViSpaccoIlCulo.mp3",
      mf"rphjb_ViSpaccoIlCulo.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      stt"il sindaco"
    )(
      mf"rphjb_Sindaco.mp3"
    ),
    ReplyBundleMessage.textToMedia[F](
      "si leva (sta roba|sto schifo)".r.tr(16),
      stt"questo schifo"
    )(
      mf"rphjb_QuestoSchifo.mp3"
    ),
    ReplyBundleMessage.textToMedia[F](
      stt"preservativo"
    )(
      mf"rphjb_Preservativo.mp3"
    ),
    ReplyBundleMessage.textToMedia[F](
      stt"korn",
      stt"giovanni battista",
      stt"acque del giordano",
      stt"battezzato",
      stt"battesimo"
    )(
      mf"rphjb_Battesimo.mp3"
    ),
    ReplyBundleMessage.textToMedia[F](
      "a( )?s[s]+tronzo".r.tr(8),
      "stronz[o]{3,}".r.tr(9)
    )(
      mf"rphjb_AStronzo.mp3"
    ),
    ReplyBundleMessage.textToMedia[F](
      stt"ancora voi"
    )(
      mf"rphjb_AncoraVoi.mp3"
    ),
    ReplyBundleMessage.textToMedia[F](
      stt"sborrata",
      stt"scopare"
    )(
      mf"rphjb_Sborrata.mp3"
    ),
    ReplyBundleMessage.textToMedia[F](
      stt"finire male",
      stt"tocca benson"
    )(
      mf"rphjb_FinireMale.mp3"
    ),
    ReplyBundleMessage.textToMedia[F](
      stt"musica tecnica",
      stt"antonacci",
      stt"grignani",
      stt"jovanotti",
    )(
      mf"rphjb_Rock.mp3"
    ),
    ReplyBundleMessage.textToMedia[F](
      "conosce(nza|re)".r.tr(9),
      stt"il sapere",
      stt"veri valori",
    )(
      mf"rphjb_Conoscere.mp3"
    ),
    ReplyBundleMessage(
      trigger = TextTrigger(
        stt"sono",
        stt"ultimo"
      ),
      reply = MediaReply.fromList[F](
        List(
          mf"rphjb_SonoUltimo.mp3",
          mf"rphjb_SonoIoUltimo.mp3"
        )
      ),
      matcher = ContainsAll
    ),
    ReplyBundleMessage.textToMedia[F](
      stt"anguille",
      stt"polipi",
      stt"cetrioli",
      stt"il problema è uno solo",
      stt"non riesco a suonare"
    )(
      mf"rphjb_Problema.mp3"
    ),
    ReplyBundleMessage.textToMedia[F](
      stt"questo ragazzo",
      stt"eccitare",
      stt"lucio dalla"
    )(
      mf"rphjb_LucioDalla.mp3"
    ),
    ReplyBundleMessage.textToMedia[F](
      "preghier(a|ina)".r.tr(9),
      stt"io non credo",
      stt"la medicina",
      stt"andare dal dottore",
      "\\billusi\\b".r.tr(6),
      stt"manfrine",
    )(
      mf"rphjb_Chiesa.mp3"
    ),
    ReplyBundleMessage.textToMedia[F](
      stt"maledetto"
    )(
      mf"rphjb_Maledetto.mp3"
    ),
    ReplyBundleMessage.textToMedia[F](
      stt"..magari",
      stt"magari..",
    )(
      mf"rphjb_Magari.mp3"
    ),
    ReplyBundleMessage.textToMedia[F](
      stt"io ti aiuto"
    )(
      mf"rphjb_Aiuto.mp3"
    ),
    ReplyBundleMessage.textToMedia[F](
      stt"faccio schifo"
    )(
      mf"rphjb_FaccioSchifo.mp3"
    ),
    ReplyBundleMessage.textToMedia[F](
      "ci sei ritornat[ao]".r.tr(15)
    )(
      mf"rphjb_Ritornata.mp3"
    ),
    ReplyBundleMessage.textToMedia[F](
      stt"che succede"
    )(
      mf"rphjb_CheSuccede.mp3"
    ),
    ReplyBundleMessage.textToMedia[F](
      stt"due ossa"
    )(
      mf"rphjb_DueOssa.mp3"
    ),
    ReplyBundleMessage.textToMedia[F](
      "m[ie] fai( proprio)? schifo".r.tr(13)
    )(
      mf"rphjb_Schifo.mp3"
    ),
    ReplyBundleMessage.textToMedia[F](
      stt"una sera"
    )(
      mf"rphjb_Sera.mp3"
    ),
    ReplyBundleMessage.textToMedia[F](
      "oppur[ae]".r.tr(6)
    )(
      mf"rphjb_Oppura.mp3"
    ),
    ReplyBundleMessage.textToMedia[F](
      stt"cosa preferisci",
      stt"ragazzetta",
      stt"carne bianca",
      stt"carne saporita"
    )(
      mf"rphjb_RagazzettaCarne.mp3"
    ),
    ReplyBundleMessage.textToMedia[F](
      stt"fragolina",
      stt"fichina"
    )(
      mf"rphjb_FragolinaFichina.mp3"
    ),
    ReplyBundleMessage.textToMedia[F](
      stt"non vi deluderò"
    )(
      mf"rphjb_NonViDeludero.mp3"
    ),
    ReplyBundleMessage.textToMedia[F](
      stt"vi saluto",
      "(col|con il) (cuore|cervello|anima|pisello|martello)".r.tr(9),
    )(
      mf"rphjb_ViSaluto.mp3"
    ),
    ReplyBundleMessage.textToMedia[F](
      stt"gigi d'alessio",
      stt"anna tatangelo"
    )(
      mf"rphjb_GigiDAlessioAnnaTatangelo.mp3"
    ),
    ReplyBundleMessage.textToMedia[F](
      "funzioni gene[g]{2,}iative".r.tr(21),
      stt"non è un uomo",
      stt"voce da uomo",
      "è (veramente )?una donna".r.tr(11)
    )(
      mf"rphjb_VoceDaUomo.mp3"
    ),
    ReplyBundleMessage.textToMedia[F](
      stt"culo chiacchierato",
      stt"rob halford"
    )(
      mf"rphjb_CuloChiacchierato.mp3"
    ),
    ReplyBundleMessage.textToMedia[F](
      stt"negri",
      stt"sezione ritmica"
    )(
      mf"rphjb_NegriSezioneRitmica.mp3"
    ),
    ReplyBundleMessage.textToMedia[F](
      stt"incrinata la voce",
      "parlo come un(a specie di)? frocio".r.tr(20)
    )(
      mf"rphjb_IncrinataLaVoceFrocio.mp3"
    ),
    ReplyBundleMessage.textToMedia[F](
      stt"io mi ricordo tutto"
    )(
      mf"rphjb_IoMiRicordoTutto.mp3"
    ),
    ReplyBundleMessage.textToMedia[F](
      stt"via zara",
      stt"sei brava a truccare",
      stt"non vali niente",
      stt"sei l'ultima",
      "manco trucc[aà] sai".r.tr(16),
    )(
      mf"rphjb_TruccareViaZara.mp3",
    ),
    ReplyBundleMessage.textToMedia[F](
      stt"che schifo!",
      "che( )?(s)+chifo".r.tr(10),
    )(
      mf"rphjb_Schifosi4.mp3",
    ),
    ReplyBundleMessage.textToMedia[F](
      stt"non parlare",
      stt"non hai il diritto",
      stt"la trasmissione è la mia",
    )(
      mf"rphjb_NonParlareTeTrasmissioneMia.mp3",
    ),
    ReplyBundleMessage.textToMedia[F](
      stt"gelatina",
      stt"secchi d'acqua",
      stt"fazzoletti",
      stt"sapone"
    )(
      mf"rphjb_GelatinaFazzolettiSecchiAcquaSapone.mp3"
    ),
    ReplyBundleMessage.textToMedia[F](
      "suonato (abbastanza )?bene".r.tr(12),
      "e che cazz[o!]{2,}".r.tr(12),
    )(
      mf"rphjb_SuonatoAbbastanzaBeneEVero.mp3"
    ),
    ReplyBundleMessage.textToMedia[F](
      stt"romantic",
      "(ar|in) culo".r.tr(7)
    )(
      mf"rphjb_RomanticiDonneArCulo.mp3"
    ),
    ReplyBundleMessage.textToMedia[F](
      stt"bonolis",
      stt"vi lascio nelle mani"
    )(
      mf"rphjb_NelleManiDiBonolis.mp3"
    ),
    ReplyBundleMessage.textToMedia[F](
      "(9|nove) mesi".r.tr(6),
      "voler (uscire|rientrare)".r.tr(12),
      stt"una vita intera"
    )(
      mf"rphjb_9MesiUscireRientrare.mp3"
    ),
    ReplyBundleMessage.textToMedia[F](
      stt"suono che cosa",
      stt"alghe marine",
    )(
      mf"rphjb_ChitarraZuccheroAlgheVino.mp3"
    ),
    ReplyBundleMessage.textToMedia[F](
      stt"misero antro",
      stt"addibiti ad agnelli",
      stt"una pisciata",
    )(
      mf"rphjb_MiseroAntroGanciAgnelliPisciata.mp3"
    ),
    ReplyBundleMessage.textToMedia[F](
      "\\bsperma\\b".r.tr(5),
      "mio (fidanzato|partner|moroso)".r.tr(11),
    )(
      mf"rphjb_DonneSperma.mp3"
    ),
    ReplyBundleMessage.textToMedia[F](
      stt"incidente dal ponte",
      stt"ponte sisto",
      stt"manco il tevere",
      stt"cercando di farmi del male",
      stt"sono ancora vivo"
    )(
      mf"rphjb_IncidentePonte.mp3"
    ),
    ReplyBundleMessage.textToMedia[F](
      "\\btega\\b".r.tr(4),
      "peli (a[rl]|del) culo".r.tr(12),
      stt"il baffo",
    )(
      mf"rphjb_RaccondaStoriaTegaBaffoPeliCulo.mp3"
    ),
    ReplyBundleMessage.textToMedia[F](
      stt"solo l'inizio",
    )(
      mf"rphjb_TelevitaSonoInizioRisata.mp3"
    ),
    ReplyBundleMessage.textToMedia[F](
      stt"riccardo benzoni",
      stt"richard philip henry john benson",
    )(
      mf"rphjb_PassaportoRiccardoBenzoni.mp3"
    ),
    ReplyBundleMessage.textToMedia[F](
      stt"ah eccola",
      stt"l'ho trovata",
    )(
      mf"rphjb_LHoTrovata.mp3"
    ),
    ReplyBundleMessage.textToMedia[F](
      stt"inutili creature",
      stt"aglioso",
      stt"golgota",
      stt"avverto pericolo",
    )(
      mf"rphjb_PoesiaNatalizia.mp3"
    ),
    ReplyBundleMessage.textToMedia[F](
      stt"chiesa"
    )(
      mf"rphjb_PoesiaNatalizia.mp3",
      mf"rphjb_Chiesa.mp3"
    ),
  )
}
