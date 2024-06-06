package com.benkio.richardphjbensonbot.data

import cats.Applicative
import com.benkio.telegrambotinfrastructure.messagefiltering.*
import com.benkio.telegrambotinfrastructure.model.*

object Audio {

  def messageRepliesAudioData[F[_]: Applicative]: List[ReplyBundleMessage[F]] = List(
    ReplyBundleMessage.textToMp3[F](
      "tastierista"
    )(
      mp3"rphjb_Tastierista.mp3"
    ),
    ReplyBundleMessage.textToMp3[F](
      "caprette",
      "acidi",
      "pomodori",
      "legumi",
      "ragni",
      "male il collo",
    )(
      mp3"rphjb_ListaMaleCollo.mp3"
    ),
    ReplyBundleMessage.textToMp3[F](
      "lo sapevo"
    )(
      mp3"rphjb_LoSapevoIo.mp3"
    ),
    ReplyBundleMessage.textToMp3[F](
      "mi auguro"
    )(
      mp3"rphjb_IoMiAuguro.mp3"
    ),
    ReplyBundleMessage.textToMp3[F](
      "non leggo"
    )(
      mp3"rphjb_NonLeggoQuelloCheScrivete.mp3"
    ),
    ReplyBundleMessage.textToMp3[F](
      "devo sopportare"
    )(
      mp3"rphjb_DevoSopportare.mp3"
    ),
    ReplyBundleMessage.textToMp3[F](
      "non mi ricordo più"
    )(
      mp3"rphjb_NonMiRicordoPiu.mp3"
    ),
    ReplyBundleMessage.textToMp3[F](
      "pensato a tutto",
      "accontentare tutti"
    )(
      mp3"rphjb_PensatoATutto.mp3"
    ),
    ReplyBundleMessage.textToMp3[F](
      "se è un amico"
    )(
      mp3"rphjb_VedereAmico.mp3"
    ),
    ReplyBundleMessage.textToMp3[F](
      "perchè l'ho fatto",
      "non do spiegazioni"
    )(
      mp3"rphjb_PercheLHoFatto.mp3"
    ),
    ReplyBundleMessage.textToMp3[F](
      "non ho detto tutto",
      "ascoltami"
    )(
      mp3"rphjb_NonHoDettoTutto.mp3"
    ),
    ReplyBundleMessage.textToMp3[F](
      "non mi calmo"
    )(
      mp3"rphjb_NonMiCalmo.mp3"
    ),
    ReplyBundleMessage.textToMp3[F](
      "devo dire che"
    )(
      mp3"rphjb_DevoDireChe.mp3"
    ),
    ReplyBundleMessage.textToMp3[F](
      "eccomi qua"
    )(
      mp3"rphjb_EccomiQua.mp3"
    ),
    ReplyBundleMessage.textToMp3[F](
      "(mi sento|sto) meglio".r.tr(10)
    )(
      mp3"rphjb_MiSentoMeglio.mp3"
    ),
    ReplyBundleMessage.textToMp3[F](
      "non è vero"
    )(
      mp3"rphjb_NonEVero.mp3"
    ),
    ReplyBundleMessage.textToMp3[F](
      "completamente nudo"
    )(
      mp3"rphjb_CompletamenteNudo.mp3"
    ),
    ReplyBundleMessage.textToMp3[F](
      "dovrei lavarmelo di più",
      "il cazzo me lo pulisci un'altra volta",
    )(
      mp3"rphjb_LavareCazzo.mp3"
    ),
    ReplyBundleMessage.textToMp3[F](
      "giù( giù)+".r.tr(7)
    )(
      mp3"rphjb_GiuGiuGiu.mp3"
    ),
    ReplyBundleMessage.textToMp3[F](
      "viale zara",
      "cas(a|e) chius(a|e)".r.tr(11)
    )(
      mp3"rphjb_VialeZara.mp3"
    ),
    ReplyBundleMessage.textToMp3[F](
      "tocca il culo"
    )(
      mp3"rphjb_MiToccaIlCulo.mp3"
    ),
    ReplyBundleMessage.textToMp3[F](
      "negli occhiali",
      "sulla spalla",
      "gianguido",
    )(
      mp3"rphjb_PannaOcchialiSpalla.mp3"
    ),
    ReplyBundleMessage.textToMp3[F](
      "state zitti"
    )(
      mp3"rphjb_StateZittiZozziUltimi.mp3"
    ),
    ReplyBundleMessage.textToMp3[F](
      "solo io"
    )(
      mp3"rphjb_SoloIo.mp3"
    ),
    ReplyBundleMessage.textToMp3[F](
      "poteri (ter[r]+ib[b]+ili|demoniaci)".r.tr(16)
    )(
      mp3"rphjb_PoteriDemoniaci.mp3"
    ),
    ReplyBundleMessage.textToMp3[F](
      "sono( pure)? italiane".r.tr(13),
      "non so(no)? ungheresi".r.tr(16)
    )(
      mp3"rphjb_ItalianeUngheresi.mp3"
    ),
    ReplyBundleMessage.textToMp3[F](
      "\\bcolpevole\\b".r.tr(9)
    )(
      mp3"rphjb_IlColpevole.mp3"
    ),
    ReplyBundleMessage.textToMp3[F](
      "il sindaco"
    )(
      mp3"rphjb_Sindaco.mp3"
    ),
    ReplyBundleMessage.textToMp3[F](
      "si leva (sta roba|sto schifo)".r.tr(16),
      "questo schifo"
    )(
      mp3"rphjb_QuestoSchifo.mp3"
    ),
    ReplyBundleMessage.textToMp3[F](
      "preservativo"
    )(
      mp3"rphjb_Preservativo.mp3"
    ),
    ReplyBundleMessage.textToMp3[F](
      "giovanni battista",
      "acque del giordano",
      "battesimo"
    )(
      mp3"rphjb_Battesimo.mp3"
    ),
    ReplyBundleMessage.textToMp3[F](
      "a( )?s[s]+tronzo".r.tr(8),
      "stronz[o]{3,}".r.tr(9)
    )(
      mp3"rphjb_AStronzo.mp3"
    ),
    ReplyBundleMessage.textToMp3[F](
      "ancora voi"
    )(
      mp3"rphjb_AncoraVoi.mp3"
    ),
    ReplyBundleMessage.textToMp3[F](
      "sborrata",
      "scopare"
    )(
      mp3"rphjb_Sborrata.mp3"
    ),
    ReplyBundleMessage.textToMp3[F](
      "finire male",
      "tocca benson"
    )(
      mp3"rphjb_FinireMale.mp3"
    ),
    ReplyBundleMessage.textToMp3[F](
      "musica tecnica",
      "antonacci",
      "grignani",
      "jovanotti",
    )(
      mp3"rphjb_Rock.mp3"
    ),
    ReplyBundleMessage.textToMp3[F](
      "conosce(nza|re)".r.tr(9),
      "il sapere",
      "veri valori",
    )(
      mp3"rphjb_Conoscere.mp3"
    ),
    ReplyBundleMessage(
      trigger = TextTrigger(
        stt"sono",
        stt"ultimo"
      ),
      reply = MediaReply.fromList[F](
        List(
          mp3"rphjb_SonoUltimo.mp3",
          mp3"rphjb_SonoIoUltimo.mp3"
        )
      ),
      matcher = ContainsAll
    ),
    ReplyBundleMessage.textToMp3[F](
      "anguille",
      "polipi",
      "cetrioli",
      "il problema è uno solo",
      "non riesco a suonare"
    )(
      mp3"rphjb_Problema.mp3"
    ),
    ReplyBundleMessage.textToMp3[F](
      "eccitare",
      "lucio dalla"
    )(
      mp3"rphjb_LucioDalla.mp3"
    ),
    ReplyBundleMessage.textToMp3[F](
      "preghier(a|ina)".r.tr(9),
      "io non credo",
      "la medicina",
      "andare dal dottore",
      "\\billusi\\b".r.tr(6),
      "manfrine",
    )(
      mp3"rphjb_Chiesa.mp3"
    ),
    ReplyBundleMessage.textToMp3[F](
      "maledetto"
    )(
      mp3"rphjb_Maledetto.mp3"
    ),
    ReplyBundleMessage.textToMp3[F](
      "..magari",
      "magari..",
    )(
      mp3"rphjb_Magari.mp3",
      mp3"rphjb_SentireMaleBeneCarezzaOppostoGraffiareGraceJonesMagari.mp3"
    ),
    ReplyBundleMessage.textToMp3[F](
      "io ti aiuto"
    )(
      mp3"rphjb_Aiuto.mp3"
    ),
    ReplyBundleMessage.textToMp3[F](
      "faccio schifo"
    )(
      mp3"rphjb_FaccioSchifo.mp3"
    ),
    ReplyBundleMessage.textToMp3[F](
      "ci sei ritornat[ao]".r.tr(15)
    )(
      mp3"rphjb_Ritornata.mp3"
    ),
    ReplyBundleMessage.textToMp3[F](
      "che succede"
    )(
      mp3"rphjb_CheSuccede.mp3"
    ),
    ReplyBundleMessage.textToMp3[F](
      "m[ie] fai( proprio)? schifo".r.tr(13)
    )(
      mp3"rphjb_Schifo.mp3"
    ),
    ReplyBundleMessage.textToMp3[F](
      "una sera"
    )(
      mp3"rphjb_Sera.mp3"
    ),
    ReplyBundleMessage.textToMp3[F](
      "oppur[ae]".r.tr(6)
    )(
      mp3"rphjb_Oppura.mp3"
    ),
    ReplyBundleMessage.textToMp3[F](
      "cosa preferisci",
      "ragazzetta",
      "carne bianca",
      "carne saporita"
    )(
      mp3"rphjb_RagazzettaCarne.mp3"
    ),
    ReplyBundleMessage.textToMp3[F](
      "fragolina",
      "fichina"
    )(
      mp3"rphjb_FragolinaFichina.mp3"
    ),
    ReplyBundleMessage.textToMp3[F](
      "non vi deluderò"
    )(
      mp3"rphjb_NonViDeludero.mp3"
    ),
    ReplyBundleMessage.textToMp3[F](
      "vi saluto",
      "(col|con il) (cuore|cervello|anima|pisello|martello)".r.tr(9),
    )(
      mp3"rphjb_ViSaluto.mp3"
    ),
    ReplyBundleMessage.textToMp3[F](
      "anna tatangelo"
    )(
      mp3"rphjb_GigiDAlessioAnnaTatangelo.mp3"
    ),
    ReplyBundleMessage.textToMp3[F](
      "funzioni gene[g]{2,}iative".r.tr(21),
      "non è un uomo",
      "è (veramente )?una donna".r.tr(11)
    )(
      mp3"rphjb_VoceDaUomo.mp3"
    ),
    ReplyBundleMessage.textToMp3[F](
      "culo chiacchierato",
      "rob halford"
    )(
      mp3"rphjb_CuloChiacchierato.mp3"
    ),
    ReplyBundleMessage.textToMp3[F](
      "incrinata la voce",
      "parlo come un(a specie di)? frocio".r.tr(20)
    )(
      mp3"rphjb_IncrinataLaVoceFrocio.mp3"
    ),
    ReplyBundleMessage.textToMp3[F](
      "io mi ricordo tutto"
    )(
      mp3"rphjb_IoMiRicordoTutto.mp3"
    ),
    ReplyBundleMessage.textToMp3[F](
      "via zara",
      "sei brava a truccare",
      "non vali niente",
      "sei l'ultima",
      "manco trucc[aà] sai".r.tr(16),
    )(
      mp3"rphjb_TruccareViaZara.mp3",
    ),
    ReplyBundleMessage.textToMp3[F](
      "che schifo!",
      "che( )?(s)+chifo".r.tr(10),
    )(
      mp3"rphjb_Schifosi4.mp3",
    ),
    ReplyBundleMessage.textToMp3[F](
      "non parlare",
      "non hai il diritto",
      "la trasmissione è la mia",
    )(
      mp3"rphjb_NonParlareTeTrasmissioneMia.mp3",
    ),
    ReplyBundleMessage.textToMp3[F](
      "gelatina",
      "secchi d'acqua",
      "fazzoletti",
      "sapone"
    )(
      mp3"rphjb_GelatinaFazzolettiSecchiAcquaSapone.mp3"
    ),
    ReplyBundleMessage.textToMp3[F](
      "suonato (abbastanza )?bene".r.tr(12),
      "e che cazz[o!]{2,}".r.tr(12),
    )(
      mp3"rphjb_SuonatoAbbastanzaBeneEVero.mp3"
    ),
    ReplyBundleMessage.textToMp3[F](
      "romantic",
      "(ar|in) culo".r.tr(7)
    )(
      mp3"rphjb_RomanticiDonneArCulo.mp3"
    ),
    ReplyBundleMessage.textToMp3[F](
      "bonolis",
      "vi lascio nelle mani"
    )(
      mp3"rphjb_NelleManiDiBonolis.mp3"
    ),
    ReplyBundleMessage.textToMp3[F](
      "(9|nove) mesi".r.tr(6),
      "voler (uscire|rientrare)".r.tr(12),
      "una vita intera"
    )(
      mp3"rphjb_9MesiUscireRientrare.mp3"
    ),
    ReplyBundleMessage.textToMp3[F](
      "suono che cosa",
      "alghe marine",
    )(
      mp3"rphjb_ChitarraZuccheroAlgheVino.mp3"
    ),
    ReplyBundleMessage.textToMp3[F](
      "misero antro",
      "addibiti ad agnelli",
      "una pisciata",
    )(
      mp3"rphjb_MiseroAntroGanciAgnelliPisciata.mp3"
    ),
    ReplyBundleMessage.textToMp3[F](
      "\\bsperma\\b".r.tr(5),
      "mio (fidanzato|partner|moroso)".r.tr(11),
    )(
      mp3"rphjb_DonneSperma.mp3"
    ),
    ReplyBundleMessage.textToMp3[F](
      "incidente dal ponte",
      "ponte sisto",
      "manco il tevere",
      "cercando di farmi del male",
      "sono ancora vivo"
    )(
      mp3"rphjb_IncidentePonte.mp3"
    ),
    ReplyBundleMessage.textToMp3[F](
      "\\btega\\b".r.tr(4),
      "peli (a[rl]|del) culo".r.tr(12),
      "il baffo",
    )(
      mp3"rphjb_RaccondaStoriaTegaBaffoPeliCulo.mp3"
    ),
    ReplyBundleMessage.textToMp3[F](
      "solo l'inizio",
    )(
      mp3"rphjb_TelevitaSonoInizioRisata.mp3"
    ),
    ReplyBundleMessage.textToMp3[F](
      "riccardo benzoni",
      "richard philip henry john benson",
    )(
      mp3"rphjb_PassaportoRiccardoBenzoni.mp3"
    ),
    ReplyBundleMessage.textToMp3[F](
      "ah eccola",
      "l'ho trovata",
    )(
      mp3"rphjb_LHoTrovata.mp3"
    ),
    ReplyBundleMessage.textToMp3[F](
      "inutili creature",
      "aglioso",
      "golgota",
      "avverto pericolo",
    )(
      mp3"rphjb_PoesiaNatalizia.mp3"
    ),
    ReplyBundleMessage.textToMp3[F](
      "chiesa"
    )(
      mp3"rphjb_PoesiaNatalizia.mp3",
      mp3"rphjb_Chiesa.mp3"
    ),
    ReplyBundleMessage.textToMp3[F](
      "carezza",
      "opposto",
      "all'incontrario",
      "grace jones",
      "io godo",
      "sentire il male",
      "e se fosse"
    )(
      mp3"rphjb_SentireMaleBeneCarezzaOppostoGraffiareGraceJonesMagari.mp3"
    ),
    ReplyBundleMessage.textToMp3[F](
      "scivola"
    )(
      mp3"rphjb_SiScivola.mp3"
    ),
  )
}
