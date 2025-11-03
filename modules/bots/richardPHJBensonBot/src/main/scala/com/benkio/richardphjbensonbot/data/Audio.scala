package com.benkio.richardphjbensonbot.data

import cats.Applicative
import com.benkio.telegrambotinfrastructure.model.reply.mp3
import com.benkio.telegrambotinfrastructure.model.reply.ReplyBundleMessage
import com.benkio.telegrambotinfrastructure.model.tr

object Audio {

  def messageRepliesAudioData[F[_]: Applicative]: List[ReplyBundleMessage[F]] = List(
    ReplyBundleMessage.textToMp3[F](
      "caprette",
      "acidi",
      "pomodori",
      "legumi",
      "ragni",
      "male il collo"
    )(
      mp3"rphjb_ListaMaleCollo.mp3"
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
      "(mi sento|sto) meglio".r
    )(
      mp3"rphjb_MiSentoMeglio.mp3"
    ),
    ReplyBundleMessage.textToMp3[F](
      "non è vero"
    )(
      mp3"rphjb_NonEVero.mp3"
    ),
    ReplyBundleMessage.textToMp3[F](
      "dovrei lavarmelo di più",
      "il cazzo me lo pulisci un'altra volta"
    )(
      mp3"rphjb_LavareCazzo.mp3"
    ),
    ReplyBundleMessage.textToMp3[F](
      "viale zara",
      "cas(a|e) chius(a|e)".r
    )(
      mp3"rphjb_VialeZara.mp3"
    ),
    ReplyBundleMessage.textToMp3[F](
      "tocca il culo"
    )(
      mp3"rphjb_MiToccaIlCulo.mp3"
    ),
    ReplyBundleMessage.textToMp3[F](
      "che (cazzo )?(m'|mi )ave[td]e mandato".r.tr(19),
      "negli occhiali",
      "sulla spalla",
      "gianguido"
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
      "poteri demoniaci"
    )(
      mp3"rphjb_PoteriDemoniaci.mp3"
    ),
    ReplyBundleMessage.textToMp3[F](
      "sono( pure)? italiane".r,
      "non so(no)? ungheresi".r
    )(
      mp3"rphjb_ItalianeUngheresi.mp3"
    ),
    ReplyBundleMessage.textToMp3[F](
      "\\bcolpevole\\b".r
    )(
      mp3"rphjb_IlColpevole.mp3"
    ),
    ReplyBundleMessage.textToMp3[F](
      "il sindaco"
    )(
      mp3"rphjb_Sindaco.mp3"
    ),
    ReplyBundleMessage.textToMp3[F](
      "si leva (sta roba|sto schifo)".r,
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
      "acque del giordano"
    )(
      mp3"rphjb_Battesimo.mp3"
    ),
    ReplyBundleMessage.textToMp3[F](
      "a( )?s[s]+tronzo".r,
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
      "qualche (storta|dispetto|torto) a me".r.tr(18)
    )(
      mp3"rphjb_FinireMale.mp3"
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
      "maledetto"
    )(
      mp3"rphjb_Maledetto.mp3"
    ),
    ReplyBundleMessage.textToMp3[F](
      "..magari",
      "magari.."
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
      "ci sei ritornat[ao]".r
    )(
      mp3"rphjb_Ritornata.mp3"
    ),
    ReplyBundleMessage.textToMp3[F](
      "che succede"
    )(
      mp3"rphjb_CheSuccede.mp3"
    ),
    ReplyBundleMessage.textToMp3[F](
      "m[ie] fai( proprio)? schifo".r
    )(
      mp3"rphjb_Schifo.mp3"
    ),
    ReplyBundleMessage.textToMp3[F](
      "oppur[ae]".r
    )(
      mp3"rphjb_Oppura.mp3"
    ),
    ReplyBundleMessage.textToMp3[F](
      "cosa preferisci",
      "carne bianca"
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
      "(col|con il) (cuore|anima|pisello)".r.tr(9),
      "con il martello",
      "con il cervello",
    )(
      mp3"rphjb_ViSaluto.mp3"
    ),
    ReplyBundleMessage.textToMp3[F](
      "funzioni geneg[g]+iative".r.tr(21),
      "non è un uomo",
      "(è|e') (veramente )?una donna".r
    )(
      mp3"rphjb_VoceDaUomo.mp3"
    ),
    ReplyBundleMessage.textToMp3[F](
      "rob halford"
    )(
      mp3"rphjb_CuloChiacchierato.mp3"
    ),
    ReplyBundleMessage.textToMp3[F](
      "io mi ricordo tutto"
    )(
      mp3"rphjb_IoMiRicordoTutto.mp3"
    ),
    ReplyBundleMessage.textToMp3[F](
      "gelatina",
      "secchi d'acqua",
      "sapone"
    )(
      mp3"rphjb_GelatinaFazzolettiSecchiAcquaSapone.mp3"
    ),
    ReplyBundleMessage.textToMp3[F](
      "fazzoletti"
    )(
      mp3"rphjb_Fazzoletti.mp3",
      mp3"rphjb_GelatinaFazzolettiSecchiAcquaSapone.mp3"
    ),
    ReplyBundleMessage.textToMp3[F](
      "e che cazzo[o]*[!]?".r
    )(
      mp3"rphjb_SuonatoAbbastanzaBeneEVero.mp3"
    ),
    ReplyBundleMessage.textToMp3[F](
      "romantic",
      "(ar|in) culo".r
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
      "(9|nove) mesi".r,
      "voler (uscire|rientrare)".r,
      "una vita intera",
      "ci avevo messo"
    )(
      mp3"rphjb_9MesiUscireRientrare.mp3"
    ),
    ReplyBundleMessage.textToMp3[F](
      "suono che cosa",
      "alghe marine"
    )(
      mp3"rphjb_ChitarraZuccheroAlgheVino.mp3"
    ),
    ReplyBundleMessage.textToMp3[F](
      "misero antro",
      "adibiti ad agnelli",
      "una pisciata",
      "appendila a",
      "\\bganci\\b".r
    )(
      mp3"rphjb_MiseroAntroGanciAgnelliPisciata.mp3"
    ),
    ReplyBundleMessage.textToMp3[F](
      "ponte sisto",
      "manco il tevere",
      "cercando di farmi del male",
      "sono ancora vivo"
    )(
      mp3"rphjb_IncidentePonte.mp3"
    ),
    ReplyBundleMessage.textToMp3[F](
      "peli (a[rl]|del) culo".r
    )(
      mp3"rphjb_RaccondaStoriaTegaBaffoPeliCulo.mp3"
    ),
    ReplyBundleMessage.textToMp3[F](
      "solo l'inizio"
    )(
      mp3"rphjb_TelevitaSonoInizioRisata.mp3"
    ),
    ReplyBundleMessage.textToMp3[F](
      "ah eccola",
      "l'ho trovata"
    )(
      mp3"rphjb_LHoTrovata.mp3"
    ),
    ReplyBundleMessage.textToMp3[F](
      "inutili creature",
      "aglioso",
      "golgota",
      "avverto pericolo"
    )(
      mp3"rphjb_PoesiaNatalizia.mp3"
    ),
    ReplyBundleMessage.textToMp3[F](
      "carezza",
      "opposto",
      "all'incontrario",
      "grace jones",
      "io godo",
      "sentire il male",
      "ti fa sentire bene",
      "e se fosse"
    )(
      mp3"rphjb_SentireMaleBeneCarezzaOppostoGraffiareGraceJonesMagari.mp3"
    ),
    ReplyBundleMessage.textToMp3[F](
      "scivola"
    )(
      mp3"rphjb_SiScivola.mp3"
    ),
    ReplyBundleMessage.textToMp3[F](
      "baraccone",
      "furgone"
    )(
      mp3"rphjb_BaracconeFurgoneTelevisione.mp3"
    ),
    ReplyBundleMessage.textToMp3[F](
      "mio (fidanzato|partner|ragazzo|moroso)".r,
      "capito che roba"
    )(
      mp3"rphjb_Fazzoletti.mp3"
    ),
    ReplyBundleMessage.textToMp3[F](
      "devi morire",
      "morirò",
      "sarai felice",
      "hanno dovuto spostare",
      "\\btomba\\b".r,
      "jim morrison"
    )(
      mp3"rphjb_JimMorrison.mp3"
    ),
    ReplyBundleMessage
      .textToMp3[F](
        "sei frocio"
      )(
        mp3"rphjb_SeiFrocio.mp3"
      ),
    ReplyBundleMessage
      .textToMp3[F](
        "rovinat[eoia]".r,
        "anche le sue",
        "poesie",
        "non se le ricorda pi[uù]",
        "pisci[aà](re)? in culo".r,
        "che cazzo stai a d[iì](re)?".r
      )(
        mp3"rphjb_PoesieRovinate.mp3"
      ),
    ReplyBundleMessage
      .textToMp3[F](
        "fregato",
        "mani tue"
      )(
        mp3"rphjb_FregatoManiTue.mp3"
      ),
    ReplyBundleMessage.textToMp3[F](
      "davanti a gente"
    )(
      mp3"rphjb_DavantiGenteNonHaCapisceUnCazzo.mp3"
    )
  )
}
