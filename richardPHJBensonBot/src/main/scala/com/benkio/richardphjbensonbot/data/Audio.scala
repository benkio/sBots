package com.benkio.richardphjbensonbot.data

import cats.Applicative
import com.benkio.telegrambotinfrastructure.messagefiltering.*
import com.benkio.telegrambotinfrastructure.model.*

object Audio {

  def messageRepliesAudioData[F[_]: Applicative]: List[ReplyBundleMessage[F]] = List(
    ReplyBundleMessage.textToMedia[F](
      "tastierista"
    )(
      mf"rphjb_Tastierista.mp3"
    ),
    ReplyBundleMessage.textToMedia[F](
      "caprette",
      "acidi",
      "pomodori",
      "legumi",
      "ragni",
      "male il collo",
    )(
      mf"rphjb_ListaMaleCollo.mp3"
    ),
    ReplyBundleMessage.textToMedia[F](
      "lo sapevo"
    )(
      mf"rphjb_LoSapevoIo.mp3"
    ),
    ReplyBundleMessage.textToMedia[F](
      "mi auguro"
    )(
      mf"rphjb_IoMiAuguro.mp3"
    ),
    ReplyBundleMessage.textToMedia[F](
      "non leggo"
    )(
      mf"rphjb_NonLeggoQuelloCheScrivete.mp3"
    ),
    ReplyBundleMessage.textToMedia[F](
      "devo sopportare"
    )(
      mf"rphjb_DevoSopportare.mp3"
    ),
    ReplyBundleMessage.textToMedia[F](
      "non mi ricordo più"
    )(
      mf"rphjb_NonMiRicordoPiu.mp3"
    ),
    ReplyBundleMessage.textToMedia[F](
      "pensato a tutto",
      "accontentare tutti"
    )(
      mf"rphjb_PensatoATutto.mp3"
    ),
    ReplyBundleMessage.textToMedia[F](
      "se è un amico"
    )(
      mf"rphjb_VedereAmico.mp3"
    ),
    ReplyBundleMessage.textToMedia[F](
      "perchè l'ho fatto",
      "non do spiegazioni"
    )(
      mf"rphjb_PercheLHoFatto.mp3"
    ),
    ReplyBundleMessage.textToMedia[F](
      "non ho detto tutto",
      "ascoltami"
    )(
      mf"rphjb_NonHoDettoTutto.mp3"
    ),
    ReplyBundleMessage.textToMedia[F](
      "non mi calmo"
    )(
      mf"rphjb_NonMiCalmo.mp3"
    ),
    ReplyBundleMessage.textToMedia[F](
      "devo dire che"
    )(
      mf"rphjb_DevoDireChe.mp3"
    ),
    ReplyBundleMessage.textToMedia[F](
      "eccomi qua"
    )(
      mf"rphjb_EccomiQua.mp3"
    ),
    ReplyBundleMessage.textToMedia[F](
      "animali"
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
      "altari",
      "realtà",
    )(
      mf"rphjb_AltariFatiscentiRealta.mp3",
      mf"rphjb_AltariFatiscentiRealta.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      "non è vero"
    )(
      mf"rphjb_NonEVero.mp3"
    ),
    ReplyBundleMessage.textToMedia[F](
      "completamente nudo"
    )(
      mf"rphjb_CompletamenteNudo.mp3"
    ),
    ReplyBundleMessage.textToMedia[F](
      "dovrei lavarmelo di più",
      "il cazzo me lo pulisci un'altra volta",
    )(
      mf"rphjb_LavareCazzo.mp3"
    ),
    ReplyBundleMessage.textToMedia[F](
      "giù( giù)+".r.tr(7)
    )(
      mf"rphjb_GiuGiuGiu.mp3"
    ),
    ReplyBundleMessage.textToMedia[F](
      "viale zara",
      "cas(a|e) chius(a|e)".r.tr(11)
    )(
      mf"rphjb_VialeZara.mp3"
    ),
    ReplyBundleMessage.textToMedia[F](
      "tocca il culo"
    )(
      mf"rphjb_MiToccaIlCulo.mp3"
    ),
    ReplyBundleMessage.textToMedia[F](
      "negli occhiali",
      "sulla spalla",
      "gianguido",
    )(
      mf"rphjb_PannaOcchialiSpalla.mp3"
    ),
    ReplyBundleMessage.textToMedia[F](
      "state zitti"
    )(
      mf"rphjb_StateZittiZozziUltimi.mp3"
    ),
    ReplyBundleMessage.textToMedia[F](
      "solo io"
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
      "vi spacco il culo"
    )(
      mf"rphjb_ViSpaccoIlCulo.mp3",
      mf"rphjb_ViSpaccoIlCulo.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      "il sindaco"
    )(
      mf"rphjb_Sindaco.mp3"
    ),
    ReplyBundleMessage.textToMedia[F](
      "si leva (sta roba|sto schifo)".r.tr(16),
      "questo schifo"
    )(
      mf"rphjb_QuestoSchifo.mp3"
    ),
    ReplyBundleMessage.textToMedia[F](
      "preservativo"
    )(
      mf"rphjb_Preservativo.mp3"
    ),
    ReplyBundleMessage.textToMedia[F](
      "korn",
      "giovanni battista",
      "acque del giordano",
      "battesimo"
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
      "ancora voi"
    )(
      mf"rphjb_AncoraVoi.mp3"
    ),
    ReplyBundleMessage.textToMedia[F](
      "sborrata",
      "scopare"
    )(
      mf"rphjb_Sborrata.mp3"
    ),
    ReplyBundleMessage.textToMedia[F](
      "finire male",
      "tocca benson"
    )(
      mf"rphjb_FinireMale.mp3"
    ),
    ReplyBundleMessage.textToMedia[F](
      "musica tecnica",
      "antonacci",
      "grignani",
      "jovanotti",
    )(
      mf"rphjb_Rock.mp3"
    ),
    ReplyBundleMessage.textToMedia[F](
      "conosce(nza|re)".r.tr(9),
      "il sapere",
      "veri valori",
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
      "anguille",
      "polipi",
      "cetrioli",
      "il problema è uno solo",
      "non riesco a suonare"
    )(
      mf"rphjb_Problema.mp3"
    ),
    ReplyBundleMessage.textToMedia[F](
      "questo ragazzo",
      "eccitare",
      "lucio dalla"
    )(
      mf"rphjb_LucioDalla.mp3"
    ),
    ReplyBundleMessage.textToMedia[F](
      "preghier(a|ina)".r.tr(9),
      "io non credo",
      "la medicina",
      "andare dal dottore",
      "\\billusi\\b".r.tr(6),
      "manfrine",
    )(
      mf"rphjb_Chiesa.mp3"
    ),
    ReplyBundleMessage.textToMedia[F](
      "maledetto"
    )(
      mf"rphjb_Maledetto.mp3"
    ),
    ReplyBundleMessage.textToMedia[F](
      "..magari",
      "magari..",
    )(
      mf"rphjb_Magari.mp3",
      mf"rphjb_SentireMaleBeneCarezzaOppostoGraffiareGraceJonesMagari.mp3"
    ),
    ReplyBundleMessage.textToMedia[F](
      "io ti aiuto"
    )(
      mf"rphjb_Aiuto.mp3"
    ),
    ReplyBundleMessage.textToMedia[F](
      "faccio schifo"
    )(
      mf"rphjb_FaccioSchifo.mp3"
    ),
    ReplyBundleMessage.textToMedia[F](
      "ci sei ritornat[ao]".r.tr(15)
    )(
      mf"rphjb_Ritornata.mp3"
    ),
    ReplyBundleMessage.textToMedia[F](
      "che succede"
    )(
      mf"rphjb_CheSuccede.mp3"
    ),
    ReplyBundleMessage.textToMedia[F](
      "due ossa"
    )(
      mf"rphjb_DueOssa.mp3"
    ),
    ReplyBundleMessage.textToMedia[F](
      "m[ie] fai( proprio)? schifo".r.tr(13)
    )(
      mf"rphjb_Schifo.mp3"
    ),
    ReplyBundleMessage.textToMedia[F](
      "una sera"
    )(
      mf"rphjb_Sera.mp3"
    ),
    ReplyBundleMessage.textToMedia[F](
      "oppur[ae]".r.tr(6)
    )(
      mf"rphjb_Oppura.mp3"
    ),
    ReplyBundleMessage.textToMedia[F](
      "cosa preferisci",
      "ragazzetta",
      "carne bianca",
      "carne saporita"
    )(
      mf"rphjb_RagazzettaCarne.mp3"
    ),
    ReplyBundleMessage.textToMedia[F](
      "fragolina",
      "fichina"
    )(
      mf"rphjb_FragolinaFichina.mp3"
    ),
    ReplyBundleMessage.textToMedia[F](
      "non vi deluderò"
    )(
      mf"rphjb_NonViDeludero.mp3"
    ),
    ReplyBundleMessage.textToMedia[F](
      "vi saluto",
      "(col|con il) (cuore|cervello|anima|pisello|martello)".r.tr(9),
    )(
      mf"rphjb_ViSaluto.mp3"
    ),
    ReplyBundleMessage.textToMedia[F](
      "anna tatangelo"
    )(
      mf"rphjb_GigiDAlessioAnnaTatangelo.mp3"
    ),
    ReplyBundleMessage.textToMedia[F](
      "funzioni gene[g]{2,}iative".r.tr(21),
      "non è un uomo",
      "è (veramente )?una donna".r.tr(11)
    )(
      mf"rphjb_VoceDaUomo.mp3"
    ),
    ReplyBundleMessage.textToMedia[F](
      "culo chiacchierato",
      "rob halford"
    )(
      mf"rphjb_CuloChiacchierato.mp3"
    ),
    ReplyBundleMessage.textToMedia[F](
      "incrinata la voce",
      "parlo come un(a specie di)? frocio".r.tr(20)
    )(
      mf"rphjb_IncrinataLaVoceFrocio.mp3"
    ),
    ReplyBundleMessage.textToMedia[F](
      "io mi ricordo tutto"
    )(
      mf"rphjb_IoMiRicordoTutto.mp3"
    ),
    ReplyBundleMessage.textToMedia[F](
      "via zara",
      "sei brava a truccare",
      "non vali niente",
      "sei l'ultima",
      "manco trucc[aà] sai".r.tr(16),
    )(
      mf"rphjb_TruccareViaZara.mp3",
    ),
    ReplyBundleMessage.textToMedia[F](
      "che schifo!",
      "che( )?(s)+chifo".r.tr(10),
    )(
      mf"rphjb_Schifosi4.mp3",
    ),
    ReplyBundleMessage.textToMedia[F](
      "non parlare",
      "non hai il diritto",
      "la trasmissione è la mia",
    )(
      mf"rphjb_NonParlareTeTrasmissioneMia.mp3",
    ),
    ReplyBundleMessage.textToMedia[F](
      "gelatina",
      "secchi d'acqua",
      "fazzoletti",
      "sapone"
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
      "romantic",
      "(ar|in) culo".r.tr(7)
    )(
      mf"rphjb_RomanticiDonneArCulo.mp3"
    ),
    ReplyBundleMessage.textToMedia[F](
      "bonolis",
      "vi lascio nelle mani"
    )(
      mf"rphjb_NelleManiDiBonolis.mp3"
    ),
    ReplyBundleMessage.textToMedia[F](
      "(9|nove) mesi".r.tr(6),
      "voler (uscire|rientrare)".r.tr(12),
      "una vita intera"
    )(
      mf"rphjb_9MesiUscireRientrare.mp3"
    ),
    ReplyBundleMessage.textToMedia[F](
      "suono che cosa",
      "alghe marine",
    )(
      mf"rphjb_ChitarraZuccheroAlgheVino.mp3"
    ),
    ReplyBundleMessage.textToMedia[F](
      "misero antro",
      "addibiti ad agnelli",
      "una pisciata",
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
      "incidente dal ponte",
      "ponte sisto",
      "manco il tevere",
      "cercando di farmi del male",
      "sono ancora vivo"
    )(
      mf"rphjb_IncidentePonte.mp3"
    ),
    ReplyBundleMessage.textToMedia[F](
      "\\btega\\b".r.tr(4),
      "peli (a[rl]|del) culo".r.tr(12),
      "il baffo",
    )(
      mf"rphjb_RaccondaStoriaTegaBaffoPeliCulo.mp3"
    ),
    ReplyBundleMessage.textToMedia[F](
      "solo l'inizio",
    )(
      mf"rphjb_TelevitaSonoInizioRisata.mp3"
    ),
    ReplyBundleMessage.textToMedia[F](
      "riccardo benzoni",
      "richard philip henry john benson",
    )(
      mf"rphjb_PassaportoRiccardoBenzoni.mp3"
    ),
    ReplyBundleMessage.textToMedia[F](
      "ah eccola",
      "l'ho trovata",
    )(
      mf"rphjb_LHoTrovata.mp3"
    ),
    ReplyBundleMessage.textToMedia[F](
      "inutili creature",
      "aglioso",
      "golgota",
      "avverto pericolo",
    )(
      mf"rphjb_PoesiaNatalizia.mp3"
    ),
    ReplyBundleMessage.textToMedia[F](
      "chiesa"
    )(
      mf"rphjb_PoesiaNatalizia.mp3",
      mf"rphjb_Chiesa.mp3"
    ),
    ReplyBundleMessage.textToMedia[F](
      "carezza",
      "opposto",
      "all'incontrario",
      "grace jones",
      "io godo",
      "sentire il male",
      "e se fosse"
    )(
      mf"rphjb_SentireMaleBeneCarezzaOppostoGraffiareGraceJonesMagari.mp3"
    )
  )
}
