package com.benkio.youtuboancheiobot

import cats._
import cats.effect._
import cats.implicits._
import com.benkio.telegrambotinfrastructure.model._
import com.benkio.telegrambotinfrastructure.patterns.CommandPatterns.InstructionsCommand
import com.benkio.telegrambotinfrastructure.patterns.CommandPatterns.RandomLinkCommand
import com.benkio.telegrambotinfrastructure.patterns.CommandPatterns.StatisticsCommands
import com.benkio.telegrambotinfrastructure.patterns.CommandPatterns.TriggerListCommand
import com.benkio.telegrambotinfrastructure.patterns.CommandPatterns.TriggerSearchCommand
import com.benkio.telegrambotinfrastructure.resources.ResourceAccess
import com.benkio.telegrambotinfrastructure.resources.db.DBLayer
import com.benkio.telegrambotinfrastructure.web.UrlFetcher
import com.benkio.telegrambotinfrastructure.BotOps
import com.benkio.telegrambotinfrastructure._
import com.lightbend.emoji.ShortCodes.Defaults._
import com.lightbend.emoji.ShortCodes.Implicits._
import com.lightbend.emoji._
import doobie.Transactor
import log.effect.LogWriter
import org.http4s.client.Client
import org.http4s.ember.client._
import org.http4s.implicits._
import org.http4s.Status
import org.http4s.Uri
import telegramium.bots.high._

class YoutuboAncheIoBotPolling[F[_]: Parallel: Async: Api: LogWriter](
    val dbLayer: DBLayer[F]
) extends BotSkeletonPolling[F]
    with YoutuboAncheIoBot[F] {
  override def resourceAccess(implicit syncF: Sync[F]): ResourceAccess[F] = dbLayer.resourceAccess
}

class YoutuboAncheIoBotWebhook[F[_]: Async: Api: LogWriter](uri: Uri, val dbLayer: DBLayer[F], path: Uri = uri"/")
    extends BotSkeletonWebhook[F](uri, path)
    with YoutuboAncheIoBot[F] {
  override def resourceAccess(implicit syncF: Sync[F]): ResourceAccess[F] = dbLayer.resourceAccess
}

trait YoutuboAncheIoBot[F[_]] extends BotSkeleton[F] {

  override val botName: String                     = YoutuboAncheIoBot.botName
  override val botPrefix: String                   = YoutuboAncheIoBot.botPrefix
  override val ignoreMessagePrefix: Option[String] = YoutuboAncheIoBot.ignoreMessagePrefix
  override val dbLayer: DBLayer[F]
  val linkSources = "ytai_LinkSources"

  override def messageRepliesDataF(implicit
      applicativeF: Applicative[F],
      log: LogWriter[F]
  ): F[List[ReplyBundleMessage[F]]] =
    YoutuboAncheIoBot.messageRepliesData[F].pure[F]

  override def commandRepliesDataF(implicit asyncF: Async[F], log: LogWriter[F]): F[List[ReplyBundleCommand[F]]] =
    YoutuboAncheIoBot.commandRepliesData[F](dbLayer, linkSources).pure[F]

}
object YoutuboAncheIoBot extends BotOps {

  val ignoreMessagePrefix: Option[String] = Some("!")
  val botName: String                     = "YoutuboAncheIoBot"
  val botPrefix: String                   = "ytai"
  val triggerListUri: Uri = uri"https://github.com/benkio/myTelegramBot/blob/master/youtuboAncheIoBot/ytai_triggers.txt"

  def messageRepliesAudioData[
      F[_]: Applicative
  ]: List[ReplyBundleMessage[F]] = List(
    ReplyBundleMessage(
      trigger = TextTrigger(
        StringTextTriggerValue("non vi costa nulla")
      ),
      mediafiles = List(
        MediaFile("ytai_Donazioni.mp3")
      ),
    )
  )

  def messageRepliesGifData[
      F[_]: Applicative
  ]: List[ReplyBundleMessage[F]] = List(
    ReplyBundleMessage(
      trigger = TextTrigger(
        StringTextTriggerValue("acqua calabria")
      ),
      mediafiles = List(
        MediaFile("ytai_AcquaSguardo.gif"),
        MediaFile("ytai_Sete.gif"),
        MediaFile("ytai_AcquaCalabria.gif")
      ),
      replySelection = RandomSelection
    ),
    ReplyBundleMessage(
      trigger = TextTrigger(
        StringTextTriggerValue("fatica"),
        StringTextTriggerValue("sudore"),
        StringTextTriggerValue("sudato")
      ),
      mediafiles = List(
        MediaFile("ytai_Affaticamento.gif")
      )
    ),
    ReplyBundleMessage(
      trigger = TextTrigger(
        RegexTextTriggerValue("ascolta (queste|le) mie parole".r),
        StringTextTriggerValue("amareggiati"),
        RegexTextTriggerValue("dedicaci (il tuo tempo|le tue notti)".r)
      ),
      mediafiles = List(
        MediaFile("ytai_Amareggiati.gif")
      )
    ),
    ReplyBundleMessage(
      trigger = TextTrigger(
        StringTextTriggerValue("architetta"),
        StringTextTriggerValue("notaia"),
        StringTextTriggerValue("ministra"),
        StringTextTriggerValue("avvocata")
      ),
      mediafiles = List(
        MediaFile("ytai_Architetta.gif")
      )
    ),
    ReplyBundleMessage(
      trigger = TextTrigger(
        StringTextTriggerValue("bel sogno")
      ),
      mediafiles = List(
        MediaFile("ytai_BelSogno.gif")
      )
    ),
    ReplyBundleMessage(
      trigger = TextTrigger(
        StringTextTriggerValue("brivido"),
        StringTextTriggerValue("fremito"),
        StringTextTriggerValue("tremito"),
        StringTextTriggerValue("tremore")
      ),
      mediafiles = List(
        MediaFile("ytai_Brivido.gif")
      )
    ),
    ReplyBundleMessage(
      trigger = TextTrigger(
        StringTextTriggerValue("buonanotte")
      ),
      mediafiles = List(
        MediaFile("ytai_Buonanotte.gif"),
        MediaFile("ytai_BuonanotteBrunchPlus.gif")
      ),
      replySelection = RandomSelection
    ),
    ReplyBundleMessage(
      trigger = TextTrigger(
        RegexTextTriggerValue("(buona )?pizza".r)
      ),
      mediafiles = List(
        MediaFile("ytai_BuonaPizza.gif"),
        MediaFile("ytai_PizzaAllegria.gif")
      )
    ),
    ReplyBundleMessage(
      trigger = TextTrigger(
        StringTextTriggerValue("buonasera")
      ),
      mediafiles = List(
        MediaFile("ytai_Buonasera.gif")
      )
    ),
    ReplyBundleMessage(
      trigger = TextTrigger(
        StringTextTriggerValue("che spettacolo")
      ),
      mediafiles = List(
        MediaFile("ytai_CheSpettacolo.gif")
      )
    ),
    ReplyBundleMessage(
      trigger = TextTrigger(
        StringTextTriggerValue("che vergogna"),
        StringTextTriggerValue("non ce l'ho"),
        StringTextTriggerValue("sopracciglia"),
        RegexTextTriggerValue("tutti (quanti )?mi criticheranno".r)
      ),
      mediafiles = List(
        MediaFile("ytai_CheVergogna.gif"),
        MediaFile("ytai_CheVergogna.mp3"),
      ),
      replySelection = RandomSelection
    ),
    ReplyBundleMessage(
      trigger = TextTrigger(
        StringTextTriggerValue("ciao!")
      ),
      mediafiles = List(
        MediaFile("ytai_Ciao.gif")
      )
    ),
    ReplyBundleMessage(
      trigger = TextTrigger(
        StringTextTriggerValue("ciao ragazzi"),
        StringTextTriggerValue("cari saluti")
      ),
      mediafiles = List(
        MediaFile("ytai_CiaoRagazzi.gif")
      )
    ),
    ReplyBundleMessage(
      trigger = TextTrigger(
        StringTextTriggerValue("ci divertiremo"),
        StringTextTriggerValue("bel percorso")
      ),
      mediafiles = List(
        MediaFile("ytai_CiDivertiremoPercorso.gif")
      )
    ),
    ReplyBundleMessage(
      trigger = TextTrigger(
        StringTextTriggerValue("colore meraviglioso")
      ),
      mediafiles = List(
        MediaFile("ytai_ColoreMeraviglioso.gif")
      )
    ),
    ReplyBundleMessage(
      trigger = TextTrigger(
        StringTextTriggerValue("corpulenti"),
        StringTextTriggerValue("ciccioni")
      ),
      mediafiles = List(
        MediaFile("ytai_CorpulentiCiccioni.gif")
      )
    ),
    ReplyBundleMessage(
      trigger = TextTrigger(
        StringTextTriggerValue("culetto")
      ),
      mediafiles = List(
        MediaFile("ytai_Culetto.gif")
      )
    ),
    ReplyBundleMessage(
      trigger = TextTrigger(
        StringTextTriggerValue("ed allora")
      ),
      mediafiles = List(
        MediaFile("ytai_EdAllora.gif")
      )
    ),
    ReplyBundleMessage(
      trigger = TextTrigger(
        StringTextTriggerValue("fai pure")
      ),
      mediafiles = List(
        MediaFile("ytai_FaiPure.gif")
      )
    ),
    ReplyBundleMessage(
      trigger = TextTrigger(
        RegexTextTriggerValue("fallo anche (tu|te)".r),
      ),
      mediafiles = List(
        MediaFile("ytai_FalloAncheTu.gif")
      )
    ),
    ReplyBundleMessage(
      trigger = TextTrigger(
        RegexTextTriggerValue("filet[ -]?o[ -]fish".r),
      ),
      mediafiles = List(
        MediaFile("ytai_FiletOFish.gif"),
        MediaFile("ytai_BagnarloAcqua.gif")
      ),
      replySelection = RandomSelection
    ),
    ReplyBundleMessage(
      trigger = TextTrigger(
        RegexTextTriggerValue(
          "(deluso|insoddisfatto|inappagato|abbattuto|scoraggiato|demoralizzato|depresso|demotivato|avvilito|scocciato)".r
        )
      ),
      mediafiles = List(
        MediaFile("ytai_Frustrazione.gif")
      )
    ),
    ReplyBundleMessage(
      trigger = TextTrigger(
        StringTextTriggerValue("ci sto già pensando")
      ),
      mediafiles = List(
        MediaFile("ytai_GiaPensando.gif")
      )
    ),
    ReplyBundleMessage(
      trigger = TextTrigger(
        StringTextTriggerValue("sono grande"),
        StringTextTriggerValue("sono corpulento")
      ),
      mediafiles = List(
        MediaFile("ytai_GrandeCorpulento.gif")
      )
    ),
    ReplyBundleMessage(
      trigger = TextTrigger(
        StringTextTriggerValue("grazie dottore"),
      ),
      mediafiles = List(
        MediaFile("ytai_GrazieDottore.gif")
      )
    ),
    ReplyBundleMessage(
      trigger = TextTrigger(
        StringTextTriggerValue("grazie tante"),
        StringTextTriggerValue("sconforto grave")
      ),
      mediafiles = List(
        MediaFile("ytai_GrazieTante.gif")
      )
    ),
    ReplyBundleMessage(
      trigger = TextTrigger(
        StringTextTriggerValue("incredibile profumo"),
        StringTextTriggerValue("incredibile aroma")
      ),
      mediafiles = List(
        MediaFile("ytai_IncredibileAromaProfumo.gif")
      )
    ),
    ReplyBundleMessage(
      trigger = TextTrigger(
        StringTextTriggerValue("incredibile"),
        StringTextTriggerValue("inimitabile"),
        RegexTextTriggerValue("the number (one|1)".r)
      ),
      mediafiles = List(
        MediaFile("ytai_IncredibileInimitabile.gif")
      )
    ),
    ReplyBundleMessage(
      trigger = TextTrigger(
        StringTextTriggerValue("io non lo so")
      ),
      mediafiles = List(
        MediaFile("ytai_IoNonLoSo.gif")
      )
    ),
    ReplyBundleMessage(
      trigger = TextTrigger(
        StringTextTriggerValue("loro dovere"),
        StringTextTriggerValue("vostro diritto")
      ),
      mediafiles = List(
        MediaFile("ytai_LoroDovereVostroDiritto.gif")
      )
    ),
    ReplyBundleMessage(
      trigger = TextTrigger(
        StringTextTriggerValue("lo so anche io")
      ),
      mediafiles = List(
        MediaFile("ytai_LoSoAncheIo.gif")
      )
    ),
    ReplyBundleMessage(
      trigger = TextTrigger(
        StringTextTriggerValue("mah")
      ),
      mediafiles = List(
        MediaFile("ytai_Mah.gif"),
        MediaFile("ytai_Mah2.gif"),
        MediaFile("ytai_ZoomMah.gif")
      ),
      replySelection = RandomSelection
    ),
    ReplyBundleMessage(
      trigger = TextTrigger(
        StringTextTriggerValue("meraviglioso"),
      ),
      mediafiles = List(
        MediaFile("ytai_Meraviglioso.gif")
      )
    ),
    ReplyBundleMessage(
      trigger = TextTrigger(
        StringTextTriggerValue("molla"),
        StringTextTriggerValue("ciccione"),
        StringTextTriggerValue("grassone"),
        StringTextTriggerValue("ancora non sei morto")
      ),
      mediafiles = List(
        MediaFile("ytai_Molla.gif")
      )
    ),
    ReplyBundleMessage(
      trigger = TextTrigger(
        StringTextTriggerValue("molto buona")
      ),
      mediafiles = List(
        MediaFile("ytai_MoltoBuona.gif"),
        MediaFile("ytai_Buona.gif")
      ),
      replySelection = RandomSelection
    ),
    ReplyBundleMessage(
      trigger = TextTrigger(
        StringTextTriggerValue("monoporzioni"),
        RegexTextTriggerValue("mezzo (chilo|kg)".r),
        StringTextTriggerValue("tiramisù"),
      ),
      mediafiles = List(
        MediaFile("ytai_MonoporzioniTiramisu.gif")
      )
    ),
    ReplyBundleMessage(
      trigger = TextTrigger(
        StringTextTriggerValue("non fa male")
      ),
      mediafiles = List(
        MediaFile("ytai_NonFaMale.gif")
      )
    ),
    ReplyBundleMessage(
      trigger = TextTrigger(
        StringTextTriggerValue("non la crede nessuno questa cosa"),
        StringTextTriggerValue("non ci crede nessuno")
      ),
      mediafiles = List(
        MediaFile("ytai_NonLaCredeNessuno.gif")
      )
    ),
    ReplyBundleMessage(
      trigger = TextTrigger(
        StringTextTriggerValue("non pensiamoci più")
      ),
      mediafiles = List(
        MediaFile("ytai_NonPensiamoci.gif")
      )
    ),
    ReplyBundleMessage(
      trigger = TextTrigger(
        StringTextTriggerValue("incomprensione"),
        StringTextTriggerValue("non vi capiscono")
      ),
      mediafiles = List(
        MediaFile("ytai_NonViCapiscono.gif")
      )
    ),
    ReplyBundleMessage(
      trigger = TextTrigger(
        StringTextTriggerValue("occhiolino"),
        StringTextTriggerValue(";)"),
        StringTextTriggerValue(e":wink:"),
      ),
      mediafiles = List(
        MediaFile("ytai_Occhiolino.gif"),
        MediaFile("ytai_Occhiolino2.gif"),
        MediaFile("ytai_Occhiolino3.gif")
      ),
      replySelection = RandomSelection
    ),
    ReplyBundleMessage(
      trigger = TextTrigger(
        StringTextTriggerValue("olè"),
      ),
      mediafiles = List(
        MediaFile("ytai_Ole.gif")
      )
    ),
    ReplyBundleMessage(
      trigger = TextTrigger(
        StringTextTriggerValue("olè anche io"),
      ),
      mediafiles = List(
        MediaFile("ytai_OleAncheIo.gif")
      )
    ),
    ReplyBundleMessage(
      trigger = TextTrigger(
        StringTextTriggerValue("la perfezione"),
        StringTextTriggerValue("la nostra tendenza")
      ),
      mediafiles = List(
        MediaFile("ytai_PerfezioneTendenza.gif")
      )
    ),
    ReplyBundleMessage(
      trigger = TextTrigger(
        StringTextTriggerValue("profumo meraviglioso"),
      ),
      mediafiles = List(
        MediaFile("ytai_ProfumoMeraviglioso.gif"),
        MediaFile("ytai_ProfumoGamberettiSalmone.gif")
      ),
      replySelection = RandomSelection
    ),
    ReplyBundleMessage(
      trigger = TextTrigger(
        StringTextTriggerValue("sentiamo il profumo")
      ),
      mediafiles = List(
        MediaFile("ytai_ProfumoMeraviglioso.gif")
      )
    ),
    ReplyBundleMessage(
      trigger = TextTrigger(
        StringTextTriggerValue("ricordami fino a domani")
      ),
      mediafiles = List(
        MediaFile("ytai_Ricordami.gif")
      )
    ),
    ReplyBundleMessage(
      trigger = TextTrigger(
        StringTextTriggerValue("ringraziamento"),
      ),
      mediafiles = List(
        MediaFile("ytai_Ringraziamento.gif")
      )
    ),
    ReplyBundleMessage(
      trigger = TextTrigger(
        StringTextTriggerValue("se non mi aiuta"),
        StringTextTriggerValue("cosa mi aiuta")
      ),
      mediafiles = List(
        MediaFile("ytai_SentiteCheRoba.gif")
      )
    ),
    ReplyBundleMessage(
      trigger = TextTrigger(
        StringTextTriggerValue("sete"),
        RegexTextTriggerValue("(sorso|bicchiere) d'acqua".r)
      ),
      mediafiles = List(
        MediaFile("ytai_Sete.gif")
      )
    ),
    ReplyBundleMessage(
      trigger = TextTrigger(
        StringTextTriggerValue(Emoji(0x1f937).toString)
      ),
      mediafiles = List(
        MediaFile("ytai_Shrug.gif")
      )
    ),
    ReplyBundleMessage(
      trigger = TextTrigger(
        StringTextTriggerValue("sta per dire qualcosa"),
      ),
      mediafiles = List(
        MediaFile("ytai_Silenzio.gif")
      )
    ),
    ReplyBundleMessage(
      trigger = TextTrigger(
        RegexTextTriggerValue("silenzio[,]? silenzio".r),
      ),
      mediafiles = List(
        MediaFile("ytai_Silenzio.gif"),
        MediaFile("ytai_SilenzioMomentoMagico.gif")
      ),
      replySelection = RandomSelection
    ),
    ReplyBundleMessage(
      trigger = TextTrigger(
        RegexTextTriggerValue("si v[àa] finch[eé] si v[aà]".r),
        StringTextTriggerValue("quando non si potrà andare più"),
        StringTextTriggerValue("è tanto facile")
      ),
      mediafiles = List(
        MediaFile("ytai_SiVaFincheSiVa.gif")
      )
    ),
    ReplyBundleMessage(
      trigger = TextTrigger(
        StringTextTriggerValue("sorprendente"),
      ),
      mediafiles = List(
        MediaFile("ytai_Sorprendente.gif")
      )
    ),
    ReplyBundleMessage(
      trigger = TextTrigger(
        StringTextTriggerValue(e":smile:"),
        StringTextTriggerValue(e":smiley:"),
        StringTextTriggerValue(":)"),
        StringTextTriggerValue("sorriso")
      ),
      mediafiles = List(
        MediaFile("ytai_Sorriso.gif")
      )
    ),
    ReplyBundleMessage(
      trigger = TextTrigger(
        StringTextTriggerValue("spuntino"),
      ),
      mediafiles = List(
        MediaFile("ytai_SpuntinoConMe.gif"),
        MediaFile("ytai_SpuntinoConMe2.gif"),
        MediaFile("ytai_BuonoSpuntino.gif")
      ),
      replySelection = RandomSelection
    ),
    ReplyBundleMessage(
      trigger = TextTrigger(
        RegexTextTriggerValue("ti voglio (tanto )?bene".r),
      ),
      mediafiles = List(
        MediaFile("ytai_TVTB.gif")
      )
    ),
    ReplyBundleMessage(
      trigger = TextTrigger(
        StringTextTriggerValue("l'unica cosa che sai fare"),
      ),
      mediafiles = List(
        MediaFile("ytai_UnicaCosaMangiare.gif")
      )
    ),
    ReplyBundleMessage(
      trigger = TextTrigger(
        StringTextTriggerValue("uno o due")
      ),
      mediafiles = List(
        MediaFile("ytai_UnoODue.gif")
      )
    ),
    ReplyBundleMessage(
      trigger = TextTrigger(
        StringTextTriggerValue("zenzero"),
        StringTextTriggerValue("mia risposta")
      ),
      mediafiles = List(
        MediaFile("ytai_Zenzero.gif")
      )
    ),
    ReplyBundleMessage(
      trigger = TextTrigger(
        StringTextTriggerValue("zoom"),
        StringTextTriggerValue("guardo da vicino")
      ),
      mediafiles = List(
        MediaFile("ytai_Zoom.gif"),
        MediaFile("ytai_ZoomMah.gif")
      ),
      replySelection = RandomSelection
    ),
    ReplyBundleMessage(
      trigger = TextTrigger(
        StringTextTriggerValue("auguri"),
      ),
      mediafiles = List(
        MediaFile("ytai_AuguriDiGusto.gif"),
        MediaFile("ytai_TantiAuguri.gif")
      ),
      replySelection = RandomSelection
    ),
    ReplyBundleMessage(
      trigger = TextTrigger(
        StringTextTriggerValue("bagnarlo")
      ),
      mediafiles = List(
        MediaFile("ytai_BagnarloAcqua.gif")
      )
    ),
    ReplyBundleMessage(
      trigger = TextTrigger(
        StringTextTriggerValue("benvenuti"),
        StringTextTriggerValue("ora e sempre")
      ),
      mediafiles = List(
        MediaFile("ytai_Benvenuti.gif")
      )
    ),
    ReplyBundleMessage(
      trigger = TextTrigger(
        StringTextTriggerValue("buon appetito")
      ),
      mediafiles = List(
        MediaFile("ytai_BuonAppetito.gif"),
      )
    ),
    ReplyBundleMessage(
      trigger = TextTrigger(
        StringTextTriggerValue("diploma"),
        StringTextTriggerValue("per pisciare"),
        RegexTextTriggerValue("ma (che )?stiamo scherzando".r)
      ),
      mediafiles = List(
        MediaFile("ytai_DiplomaPisciare.gif"),
      )
    ),
    ReplyBundleMessage(
      trigger = TextTrigger(
        RegexTextTriggerValue("non (mi sento|sto) bene".r)
      ),
      mediafiles = List(
        MediaFile("ytai_DiversiGioniNonStoBene.gif")
      )
    ),
    ReplyBundleMessage(
      trigger = TextTrigger(
        RegexTextTriggerValue("(grazie e )?arrivederci".r),
      ),
      mediafiles = List(
        MediaFile("ytai_GrazieArrivederci.gif")
      )
    ),
    ReplyBundleMessage(
      trigger = TextTrigger(
        StringTextTriggerValue("eccitato"),
      ),
      mediafiles = List(
        MediaFile("ytai_MoltoEccitato.gif")
      )
    ),
    ReplyBundleMessage(
      trigger = TextTrigger(
        StringTextTriggerValue("no pain"),
        StringTextTriggerValue("no gain")
      ),
      mediafiles = List(
        MediaFile("ytai_NoPainNoGain.gif")
      )
    ),
    ReplyBundleMessage(
      trigger = TextTrigger(
        StringTextTriggerValue("non hai più scuse"),
        StringTextTriggerValue("riprenditi"),
        StringTextTriggerValue("sei in gamba"),
      ),
      mediafiles = List(
        MediaFile("ytai_NoScuse.gif")
      )
    ),
    ReplyBundleMessage(
      trigger = TextTrigger(
        StringTextTriggerValue("caspita"),
        RegexTextTriggerValue("sono (grosso|sono (quasi )?enorme|una palla di lardo)".r),
      ),
      mediafiles = List(
        MediaFile("ytai_PallaDiLardo.gif")
      )
    ),
    ReplyBundleMessage(
      trigger = TextTrigger(
        StringTextTriggerValue("mette paura"),
      ),
      mediafiles = List(
        MediaFile("ytai_Paura.gif")
      )
    ),
    ReplyBundleMessage(
      trigger = TextTrigger(
        StringTextTriggerValue("per voi"),
      ),
      mediafiles = List(
        MediaFile("ytai_PerVoi.gif")
      )
    ),
    ReplyBundleMessage(
      trigger = TextTrigger(
        StringTextTriggerValue("alimenti"),
        StringTextTriggerValue("allegria"),
      ),
      mediafiles = List(
        MediaFile("ytai_PizzaAllegria.gif")
      )
    ),
    ReplyBundleMessage(
      trigger = TextTrigger(
        StringTextTriggerValue("gamberetti"),
        StringTextTriggerValue("salmone"),
      ),
      mediafiles = List(
        MediaFile("ytai_ProfumoGamberettiSalmone.gif"),
        MediaFile("ytai_Sorpresa.gif")
      ),
      replySelection = RandomSelection
    ),
    ReplyBundleMessage(
      trigger = TextTrigger(
        RegexTextTriggerValue("\\bproviamo\\b".r),
        StringTextTriggerValue("senza morire"),
      ),
      mediafiles = List(
        MediaFile("ytai_ProviamoSenzaMorire.gif")
      )
    ),
    ReplyBundleMessage(
      trigger = TextTrigger(
        StringTextTriggerValue("quello che riesco a fare"),
      ),
      mediafiles = List(
        MediaFile("ytai_RiescoAFare.gif")
      )
    ),
    ReplyBundleMessage(
      trigger = TextTrigger(
        RegexTextTriggerValue("sentendo (davvero )?male".r),
      ),
      mediafiles = List(
        MediaFile("ytai_SentendoDavveroMale.gif")
      )
    ),
    ReplyBundleMessage(
      trigger = TextTrigger(
        StringTextTriggerValue("momento magico"),
      ),
      mediafiles = List(
        MediaFile("ytai_SilenzioMomentoMagico.gif")
      )
    ),
    ReplyBundleMessage(
      trigger = TextTrigger(
        StringTextTriggerValue("sopraffino"),
      ),
      mediafiles = List(
        MediaFile("ytai_Sopraffino.gif")
      )
    ),
    ReplyBundleMessage(
      trigger = TextTrigger(
        StringTextTriggerValue("😮"),
        StringTextTriggerValue("😯"),
      ),
      mediafiles = List(
        MediaFile("ytai_Sorpresa.gif")
      )
    ),
    ReplyBundleMessage(
      trigger = TextTrigger(
        RegexTextTriggerValue("staccato (quasi )?il naso".r),
      ),
      mediafiles = List(
        MediaFile("ytai_StaccatoNaso.gif")
      )
    ),
    ReplyBundleMessage(
      trigger = TextTrigger(
        RegexTextTriggerValue("cuc[uù]+".r),
      ),
      mediafiles = List(
        MediaFile("ytai_Cucu.gif")
      )
    ),
    ReplyBundleMessage(
      trigger = TextTrigger(
        RegexTextTriggerValue("al[e]+ [o]{2,}".r),
      ),
      mediafiles = List(
        MediaFile("ytai_AleOoo.gif")
      )
    ),
    ReplyBundleMessage(
      trigger = TextTrigger(
        StringTextTriggerValue("fare senza"),
        StringTextTriggerValue("faenza"),
      ),
      mediafiles = List(
        MediaFile("ytai_SenzaFaenza.gif")
      )
    ),
    ReplyBundleMessage(
      trigger = TextTrigger(
        StringTextTriggerValue("caffè"),
      ),
      mediafiles = List(
        MediaFile("ytai_BuonCaffeATutti.gif")
      )
    ),
    ReplyBundleMessage(
      trigger = TextTrigger(
        StringTextTriggerValue("capolavoro"),
        StringTextTriggerValue("meravigliosa"),
      ),
      mediafiles = List(
        MediaFile("ytai_CapolavoroMeravigliosa.gif")
      )
    ),
    ReplyBundleMessage(
      trigger = TextTrigger(
        StringTextTriggerValue("ce la puoi fare"),
      ),
      mediafiles = List(
        MediaFile("ytai_CeLaPuoiFare.gif")
      )
    ),
    ReplyBundleMessage(
      trigger = TextTrigger(
        StringTextTriggerValue("che profumo")
      ),
      mediafiles = List(
        MediaFile("ytai_CheProfumo.gif")
      )
    ),
    ReplyBundleMessage(
      trigger = TextTrigger(
        StringTextTriggerValue("eccolo qua")
      ),
      mediafiles = List(
        MediaFile("ytai_EccoloQua.gif")
      )
    ),
    ReplyBundleMessage(
      trigger = TextTrigger(
        StringTextTriggerValue("non mi piacciono"),
      ),
      mediafiles = List(
        MediaFile("ytai_NonMiPiaccionoQuesteCose.gif")
      )
    ),
    ReplyBundleMessage(
      trigger = TextTrigger(
        StringTextTriggerValue("nonno")
      ),
      mediafiles = List(
        MediaFile("ytai_NonnoMito.gif")
      )
    ),
    ReplyBundleMessage(
      trigger = TextTrigger(
        StringTextTriggerValue("soltanto per questo"),
        StringTextTriggerValue("denaro"),
        StringTextTriggerValue("guadagno"),
      ),
      mediafiles = List(
        MediaFile("ytai_SoltantoPerQuesto.gif")
      )
    )
  )

  def messageRepliesSpecialData[
      F[_]: Applicative
  ]: List[ReplyBundleMessage[F]] = List(
    ReplyBundleMessage(
      trigger = TextTrigger(
        RegexTextTriggerValue("ho perso (di nuovo )qualcosa".r)
      ),
      mediafiles = List(
        MediaFile("ytai_HoPersoQualcosa.gif"),
        MediaFile("ytai_HoPersoQualcosa.mp3")
      ),
      replySelection = RandomSelection
    ),
    ReplyBundleMessage(
      trigger = TextTrigger(
        StringTextTriggerValue("reputazione")
      ),
      mediafiles = List(
        MediaFile("ytai_LaReputazione.gif"),
        MediaFile("ytai_CheVergogna.gif")
      ),
      replySelection = RandomSelection
    ),
    ReplyBundleMessage(
      trigger = TextTrigger(
        StringTextTriggerValue("donazioni")
      ),
      mediafiles = List(
        MediaFile("ytai_Donazioni.gif"),
        MediaFile("ytai_Donazioni.mp3")
      ),
      replySelection = RandomSelection
    ),
    ReplyBundleMessage(
      trigger = TextTrigger(
        StringTextTriggerValue("a me niente va bene"),
        StringTextTriggerValue("non mi va bene niente")
      ),
      mediafiles = List(
        MediaFile("ytai_NienteVaBene.gif"),
        MediaFile("ytai_NienteVaBene.mp3"),
      ),
      replySelection = RandomSelection
    )
  )

  def messageRepliesData[
      F[_]: Applicative
  ]: List[ReplyBundleMessage[F]] =
    (messageRepliesAudioData[F] ++ messageRepliesGifData[F] ++ messageRepliesSpecialData[F])
      .sorted(ReplyBundle.orderingInstance[F])
      .reverse

  def commandRepliesData[
      F[_]: Async
  ](dbLayer: DBLayer[F], linkSources: String)(implicit log: LogWriter[F]): List[ReplyBundleCommand[F]] = List(
    TriggerListCommand.triggerListReplyBundleCommand[F](triggerListUri),
    TriggerSearchCommand.triggerSearchReplyBundleCommand[F](
      botName = botName,
      ignoreMessagePrefix = ignoreMessagePrefix,
      mdr = messageRepliesData[F]
    ),
    RandomLinkCommand.selectRandomLinkReplyBundleCommand(
      resourceAccess = dbLayer.resourceAccess,
      youtubeLinkSources = linkSources
    ),
    RandomLinkCommand.selectRandomLinkByKeywordsReplyBundleCommand(
      resourceAccess = dbLayer.resourceAccess,
      botName = botName,
      youtubeLinkSources = linkSources
    ),
    StatisticsCommands.topTwentyReplyBundleCommand[F](
      botPrefix = botPrefix,
      dbMedia = dbLayer.dbMedia
    ),
    InstructionsCommand.instructionsReplyBundleCommand[F](
      botName = botName,
      ignoreMessagePrefix = ignoreMessagePrefix,
      commandDescriptionsIta = List(
        TriggerListCommand.triggerListCommandDescriptionIta,
        TriggerSearchCommand.triggerSearchCommandDescriptionIta,
        RandomLinkCommand.randomLinkCommandDescriptionIta,
        RandomLinkCommand.randomLinkKeywordCommandIta,
        StatisticsCommands.topTwentyTriggersCommandDescriptionIta
      ),
      commandDescriptionsEng = List(
        TriggerListCommand.triggerListCommandDescriptionEng,
        TriggerSearchCommand.triggerSearchCommandDescriptionEng,
        RandomLinkCommand.randomLinkCommandDescriptionEng,
        RandomLinkCommand.randomLinkKeywordCommandEng,
        StatisticsCommands.topTwentyTriggersCommandDescriptionEng
      )
    ),
  )

  def token[F[_]: Async]: Resource[F, String] =
    ResourceAccess.fromResources.getResourceByteArray("ytai_YoutuboAncheIoBot.token").map(_.map(_.toChar).mkString)

  final case class BotSetup[F[_]](token: String, dbLayer: DBLayer[F])

  def buildCommonBot[F[_]: Async](
      httpClient: Client[F]
  )(implicit log: LogWriter[F]): Resource[F, BotSetup[F]] = for {
    tk     <- token[F]
    config <- Resource.eval(Config.loadConfig[F])
    _      <- Resource.eval(log.info(s"[$botName] Configuration: $config"))
    transactor = Transactor.fromDriverManager[F](
      config.driver,
      config.url,
      "",
      ""
    )
    urlFetcher            <- Resource.eval(UrlFetcher[F](httpClient))
    dbLayer               <- Resource.eval(DBLayer[F](transactor, urlFetcher))
    _                     <- Resource.eval(log.info(s"[$botName] Delete webook..."))
    deleteWebhookResponse <- deleteWebhooks[F](httpClient, tk)
    _ <- Resource.eval(
      Async[F].raiseWhen(deleteWebhookResponse.status != Status.Ok)(
        new RuntimeException(
          s"[$botName] The delete webhook request failed: " + deleteWebhookResponse.as[String]
        )
      )
    )
    _ <- Resource.eval(log.info(s"[$botName] Webhook deleted"))
  } yield BotSetup[F](tk, dbLayer)

  def buildPollingBot[F[_]: Parallel: Async, A](
      action: YoutuboAncheIoBotPolling[F] => F[A]
  )(implicit log: LogWriter[F]): F[A] = (for {
    httpClient <- EmberClientBuilder.default[F].build
    botSetup   <- buildCommonBot[F](httpClient)
  } yield (httpClient, botSetup)).use(httpClient_botSetup => {
    implicit val api: Api[F] =
      BotApi(httpClient_botSetup._1, baseUrl = s"https://api.telegram.org/bot${httpClient_botSetup._2.token}")
    action(new YoutuboAncheIoBotPolling[F](httpClient_botSetup._2.dbLayer))
  })

  def buildWebhookBot[F[_]: Async](
      httpClient: Client[F],
      webhookBaseUrl: String = org.http4s.server.defaults.IPv4Host
  )(implicit log: LogWriter[F]): Resource[F, YoutuboAncheIoBotWebhook[F]] = for {
    botSetup <- buildCommonBot[F](httpClient)
    baseUrl  <- Resource.eval(Async[F].fromEither(Uri.fromString(s"https://api.telegram.org/bot${botSetup.token}")))
    path     <- Resource.eval(Async[F].fromEither(Uri.fromString(s"/${botSetup.token}")))
    webhookBaseUri <- Resource.eval(Async[F].fromEither(Uri.fromString(webhookBaseUrl + path)))
  } yield {
    implicit val api: Api[F] = BotApi(httpClient, baseUrl = baseUrl.renderString)
    new YoutuboAncheIoBotWebhook[F](
      uri = webhookBaseUri,
      dbLayer = botSetup.dbLayer,
      path = path
    )
  }
}
