package com.benkio.richardphjbensonbot

import cats._
import cats.effect._
import cats.implicits._
import com.benkio.telegrambotinfrastructure.botCapabilities.CommandPatterns._
import com.benkio.telegrambotinfrastructure.botCapabilities._
import com.benkio.telegrambotinfrastructure.model.{TextReply, _}
import com.benkio.telegrambotinfrastructure.{BotOps, _}
import com.lightbend.emoji.ShortCodes.Defaults._
import com.lightbend.emoji.ShortCodes.Implicits._
import log.effect.LogWriter
import org.http4s.Status
import org.http4s.blaze.client._
import org.http4s.client.Client
import telegramium.bots.high._

class RichardPHJBensonBotPolling[F[_]: Parallel: Async: Api: LogWriter]
    extends BotSkeletonPolling[F]
    with RichardPHJBensonBot

class RichardPHJBensonBotWebhook[F[_]: Async: Api: LogWriter](url: String, path: String = "/")
    extends BotSkeletonWebhook[F](url, path)
    with RichardPHJBensonBot

trait RichardPHJBensonBot extends BotSkeleton {

  override val resourceSource: ResourceSource = RichardPHJBensonBot.resourceSource

  override def messageRepliesDataF[F[_]: Applicative]: F[List[ReplyBundleMessage[F]]] =
    RichardPHJBensonBot.messageRepliesData[F].pure[F]

  override def commandRepliesDataF[F[_]: Async]: F[List[ReplyBundleCommand[F]]] =
    List(randomLinkByKeywordReplyBundleF, randomLinkReplyBundleF).sequence.map(cs =>
      cs ++ RichardPHJBensonBot.commandRepliesData[F]
    )

  private def randomLinkReplyBundleF[F[_]: Async]: F[ReplyBundleCommand[F]] =
    RandomLinkCommand
      .selectRandomLinkByKeyword[F](
        "",
        ResourceSource.selectResourceAccess(resourceSource),
        "rphjb_LinkSources"
      )
      .use[ReplyBundleCommand[F]](optMessage =>
        ReplyBundleCommand[F](
          trigger = CommandTrigger("randomshow"),
          text = Some(TextReply[F](_ => Applicative[F].pure(optMessage.toList), true)),
        ).pure[F]
      )

  private def randomLinkByKeywordReplyBundleF[F[_]: Async]: F[ReplyBundleCommand[F]] =
    ReplyBundleCommand[F](
      trigger = CommandTrigger("randomshowkeyword"),
      text = Some(
        TextReply[F](
          m =>
            handleCommandWithInput[F](
              m,
              "randomshowkeyword",
              "RichardPHJBensonBot",
              keywords =>
                RandomLinkCommand
                  .selectRandomLinkByKeyword[F](
                    keywords,
                    ResourceSource.selectResourceAccess(resourceSource),
                    "rphjb_LinkSources"
                  )
                  .use(_.foldl(List(s"Nessuna puntata/show contenente '$keywords' è stata trovata")) { case (_, v) =>
                    List(v)
                  }.pure[F]),
              s"Inserisci una keyword da cercare tra le puntate/shows"
            ),
          true
        )
      ),
    ).pure[F]
}

object RichardPHJBensonBot extends BotOps {

  val resourceSource: ResourceSource = All("rphjb.db")

  def messageRepliesAudioData[F[_]: Applicative]: List[ReplyBundleMessage[F]] = List(
    ReplyBundleMessage(
      TextTrigger(
        StringTextTriggerValue("come state stasera")
      ),
      List(
        MediaFile("rphjb_LetSGoodStateBene.mp3")
      )
    ),
    ReplyBundleMessage(
      TextTrigger(
        StringTextTriggerValue("tastierista")
      ),
      List(
        MediaFile("rphjb_Tastierista.mp3")
      )
    ),
    ReplyBundleMessage(
      TextTrigger(
        StringTextTriggerValue("caprette"),
        StringTextTriggerValue("acidi"),
        StringTextTriggerValue("pomodori"),
        StringTextTriggerValue("legumi"),
        StringTextTriggerValue("ratti"),
        StringTextTriggerValue("topi"),
        StringTextTriggerValue("ragni"),
        StringTextTriggerValue("male il collo"),
      ),
      List(
        MediaFile("rphjb_ListaMaleCollo.mp3")
      )
    ),
    ReplyBundleMessage(
      TextTrigger(
        RegexTextTriggerValue("scu[-]?sa[h]? scu[-]?sa[h]?".r)
      ),
      List(
        MediaFile("rphjb_Scusa.mp3")
      )
    ),
    ReplyBundleMessage(
      TextTrigger(
        StringTextTriggerValue("lo sapevo")
      ),
      List(
        MediaFile("rphjb_LoSapevoIo.mp3")
      )
    ),
    ReplyBundleMessage(
      TextTrigger(
        StringTextTriggerValue("mi auguro")
      ),
      List(
        MediaFile("rphjb_IoMiAuguro.mp3")
      )
    ),
    ReplyBundleMessage(
      TextTrigger(
        StringTextTriggerValue("non leggo")
      ),
      List(
        MediaFile("rphjb_NonLeggoQuelloCheScrivete.mp3")
      )
    ),
    ReplyBundleMessage(
      TextTrigger(
        RegexTextTriggerValue("perchè (cazzo)? mi hai fatto ven[i|ì](re)?".r)
      ),
      List(
        MediaFile("rphjb_PercheCazzoMiHaiFattoVeni.mp3")
      )
    ),
    ReplyBundleMessage(
      TextTrigger(
        StringTextTriggerValue("devo sopportare")
      ),
      List(
        MediaFile("rphjb_DevoSopportare.mp3")
      )
    ),
    ReplyBundleMessage(
      TextTrigger(
        StringTextTriggerValue("primo sbaglio")
      ),
      List(
        MediaFile("rphjb_PrimoSbaglio.mp3")
      )
    ),
    ReplyBundleMessage(
      TextTrigger(
        StringTextTriggerValue("non mi ricordo più")
      ),
      List(
        MediaFile("rphjb_NonMiRicordoPiu.mp3")
      )
    ),
    ReplyBundleMessage(
      TextTrigger(
        StringTextTriggerValue("pensato a tutto"),
        StringTextTriggerValue("accontentare tutti")
      ),
      List(
        MediaFile("rphjb_PensatoATutto.mp3")
      )
    ),
    ReplyBundleMessage(
      TextTrigger(
        StringTextTriggerValue("se è un amico")
      ),
      List(
        MediaFile("rphjb_VedereAmico.mp3")
      ),
      matcher = ContainsAll
    ),
    ReplyBundleMessage(
      TextTrigger(
        StringTextTriggerValue("appuntamento")
      ),
      List(
        MediaFile("rphjb_Appuntamento.mp3")
      )
    ),
    ReplyBundleMessage(
      TextTrigger(
        StringTextTriggerValue("perchè l'ho fatto"),
        StringTextTriggerValue("non do spiegazioni")
      ),
      List(
        MediaFile("rphjb_PercheLHoFatto.mp3")
      )
    ),
    ReplyBundleMessage(
      TextTrigger(
        StringTextTriggerValue("non ho detto tutto"),
        StringTextTriggerValue("ascoltami")
      ),
      List(
        MediaFile("rphjb_NonHoDettoTutto.mp3")
      )
    ),
    ReplyBundleMessage(
      TextTrigger(
        StringTextTriggerValue("ogni volta")
      ),
      List(
        MediaFile("rphjb_OgniVolta.mp3")
      )
    ),
    ReplyBundleMessage(
      TextTrigger(
        StringTextTriggerValue("non mi calmo")
      ),
      List(
        MediaFile("rphjb_NonMiCalmo.mp3")
      )
    ),
    ReplyBundleMessage(
      TextTrigger(
        StringTextTriggerValue("devo dire che")
      ),
      List(
        MediaFile("rphjb_DevoDireChe.mp3")
      )
    ),
    ReplyBundleMessage(
      TextTrigger(
        StringTextTriggerValue("eccomi qua")
      ),
      List(
        MediaFile("rphjb_EccomiQua.mp3")
      )
    ),
    ReplyBundleMessage(
      TextTrigger(
        StringTextTriggerValue("animali")
      ),
      List(
        MediaFile("rphjb_Animali.mp3")
      )
    ),
    ReplyBundleMessage(
      TextTrigger(
        RegexTextTriggerValue("(mi sento|sto) meglio".r)
      ),
      List(
        MediaFile("rphjb_MiSentoMeglio.mp3")
      )
    ),
    ReplyBundleMessage(
      TextTrigger(
        StringTextTriggerValue("altari"),
        StringTextTriggerValue("realtà"),
      ),
      List(
        MediaFile("rphjb_AltariFatiscentiRealta.mp3"),
        MediaFile("rphjb_AltariFatiscentiRealta.mp4")
      ),
      replySelection = RandomSelection
    ),
    ReplyBundleMessage(
      TextTrigger(
        StringTextTriggerValue("non è vero")
      ),
      List(
        MediaFile("rphjb_NonEVero.mp3")
      )
    ),
    ReplyBundleMessage(
      TextTrigger(
        StringTextTriggerValue("completamente nudo")
      ),
      List(
        MediaFile("rphjb_CompletamenteNudo.mp3")
      )
    ),
    ReplyBundleMessage(
      TextTrigger(
        StringTextTriggerValue("dovrei lavarmelo di più"),
        StringTextTriggerValue("il cazzo me lo pulisci un'altra volta"),
      ),
      List(
        MediaFile("rphjb_LavareCazzo.mp3")
      )
    ),
    ReplyBundleMessage(
      TextTrigger(
        RegexTextTriggerValue("giù( giù)+".r)
      ),
      List(
        MediaFile("rphjb_GiuGiuGiu.mp3")
      )
    ),
    ReplyBundleMessage(
      TextTrigger(
        StringTextTriggerValue("viale zara"),
        RegexTextTriggerValue("cas(a|e) chius(a|e)".r)
      ),
      List(
        MediaFile("rphjb_VialeZara.mp3")
      )
    ),
    ReplyBundleMessage(
      TextTrigger(
        StringTextTriggerValue("tocca il culo")
      ),
      List(
        MediaFile("rphjb_MiToccaIlCulo.mp3")
      )
    ),
    ReplyBundleMessage(
      TextTrigger(
        StringTextTriggerValue("negli occhiali"),
        StringTextTriggerValue("sulla spalla"),
      ),
      List(
        MediaFile("rphjb_PannaOcchialiSpalla.mp3")
      )
    ),
    ReplyBundleMessage(
      TextTrigger(
        StringTextTriggerValue("state zitti")
      ),
      List(
        MediaFile("rphjb_StateZittiZozziUltimi.mp3")
      )
    ),
    ReplyBundleMessage(
      TextTrigger(
        StringTextTriggerValue("solo io")
      ),
      List(
        MediaFile("rphjb_SoloIo.mp3")
      )
    ),
    ReplyBundleMessage(
      TextTrigger(
        StringTextTriggerValue("saranno cazzi vostri")
      ),
      List(
        MediaFile("rphjb_SarannoCazziVostri.mp3")
      )
    ),
    ReplyBundleMessage(
      TextTrigger(
        RegexTextTriggerValue("poteri (ter[r]+ib[b]+ili|demoniaci)".r)
      ),
      List(
        MediaFile("rphjb_PoteriDemoniaci.mp3")
      )
    ),
    ReplyBundleMessage(
      TextTrigger(
        RegexTextTriggerValue("sono( pure)? italiane".r),
        RegexTextTriggerValue("non so(no)? ungheresi".r)
      ),
      List(
        MediaFile("rphjb_ItalianeUngheresi.mp3")
      )
    ),
    ReplyBundleMessage(
      TextTrigger(
        RegexTextTriggerValue("\\bcolpevole\\b".r)
      ),
      List(
        MediaFile("rphjb_IlColpevole.mp3")
      )
    ),
    ReplyBundleMessage(
      TextTrigger(
        StringTextTriggerValue("vi spacco il culo")
      ),
      List(
        MediaFile("rphjb_ViSpaccoIlCulo.mp3")
      )
    ),
    ReplyBundleMessage(
      TextTrigger(
        StringTextTriggerValue("il sindaco")
      ),
      List(
        MediaFile("rphjb_Sindaco.mp3")
      )
    ),
    ReplyBundleMessage(
      TextTrigger(
        RegexTextTriggerValue("si leva (sta roba|sto schifo)".r),
        StringTextTriggerValue("questo schifo")
      ),
      List(
        MediaFile("rphjb_QuestoSchifo.mp3")
      )
    ),
    ReplyBundleMessage(
      TextTrigger(
        StringTextTriggerValue("preservativo")
      ),
      List(
        MediaFile("rphjb_Preservativo.mp3")
      )
    ),
    ReplyBundleMessage(
      TextTrigger(
        StringTextTriggerValue("giuda"),
        StringTextTriggerValue("chi è cristo"),
        StringTextTriggerValue("si è fatto fregare"),
        StringTextTriggerValue("bacio di un frocio"),
      ),
      List(
        MediaFile("rphjb_ChiECristo.mp3"),
        MediaFile("rphjb_GiudaFrocio.mp4")
      )
    ),
    ReplyBundleMessage(
      TextTrigger(
        StringTextTriggerValue("cameriera"),
        StringTextTriggerValue("moglie"),
        StringTextTriggerValue("si sposa"),
        StringTextTriggerValue("matrimonio")
      ),
      List(
        MediaFile("rphjb_Cameriera.mp3")
      )
    ),
    ReplyBundleMessage(
      TextTrigger(
        StringTextTriggerValue("korn"),
        StringTextTriggerValue("giovanni battista"),
        StringTextTriggerValue("acque del giordano"),
        StringTextTriggerValue("battezzato"),
        StringTextTriggerValue("battesimo")
      ),
      List(
        MediaFile("rphjb_Battesimo.mp3")
      )
    ),
    ReplyBundleMessage(
      TextTrigger(
        RegexTextTriggerValue("a( )?s[s]+tronzo".r),
        RegexTextTriggerValue("stronz[o]{3,}".r)
      ),
      List(
        MediaFile("rphjb_AStronzo.mp3")
      )
    ),
    ReplyBundleMessage(
      TextTrigger(
        StringTextTriggerValue("ancora voi")
      ),
      List(
        MediaFile("rphjb_AncoraVoi.mp3")
      )
    ),
    ReplyBundleMessage(
      TextTrigger(
        StringTextTriggerValue("sborrata"),
        StringTextTriggerValue("scopare")
      ),
      List(
        MediaFile("rphjb_Sborrata.mp3")
      )
    ),
    ReplyBundleMessage(
      TextTrigger(
        StringTextTriggerValue("finire male"),
        StringTextTriggerValue("tocca benson")
      ),
      List(
        MediaFile("rphjb_FinireMale.mp3")
      )
    ),
    ReplyBundleMessage(
      TextTrigger(
        StringTextTriggerValue("musica tecnica"),
        StringTextTriggerValue("antonacci"),
        StringTextTriggerValue("grignani"),
        StringTextTriggerValue("jovanotti"),
      ),
      List(
        MediaFile("rphjb_Rock.mp3")
      )
    ),
    ReplyBundleMessage(
      TextTrigger(
        RegexTextTriggerValue("conosce(nza|re)".r),
        StringTextTriggerValue("il sapere"),
        StringTextTriggerValue("veri valori"),
      ),
      List(
        MediaFile("rphjb_Conoscere.mp3")
      )
    ),
    ReplyBundleMessage(
      TextTrigger(
        RegexTextTriggerValue("vo[l]+[o]*[u]+[ou]*me".r)
      ),
      List(
        MediaFile("rphjb_MenoVolume.mp3")
      )
    ),
    ReplyBundleMessage(
      TextTrigger(
        StringTextTriggerValue("sono"),
        StringTextTriggerValue("ultimo")
      ),
      List(
        MediaFile("rphjb_SonoUltimo.mp3")
      ),
      matcher = ContainsAll
    ),
    ReplyBundleMessage(
      TextTrigger(
        StringTextTriggerValue("anguille"),
        StringTextTriggerValue("polipi"),
        StringTextTriggerValue("cetrioli"),
        RegexTextTriggerValue("il problema è uno solo".r),
        RegexTextTriggerValue("non riesco a suonare".r)
      ),
      List(
        MediaFile("rphjb_Problema.mp3")
      )
    ),
    ReplyBundleMessage(
      TextTrigger(
        StringTextTriggerValue("sono finito"),
        StringTextTriggerValue("ultimo stadio"),
        StringTextTriggerValue("stanco")
      ),
      List(
        MediaFile("rphjb_Stanco.mp3")
      )
    ),
    ReplyBundleMessage(
      TextTrigger(
        StringTextTriggerValue("negozio"),
        StringTextTriggerValue("pantaloni"),
        StringTextTriggerValue("shopping")
      ),
      List(
        MediaFile("rphjb_Pantaloni.mp3")
      )
    ),
    ReplyBundleMessage(
      TextTrigger(
        StringTextTriggerValue("brutto frocio")
      ),
      List(
        MediaFile("rphjb_BruttoFrocio.mp3")
      )
    ),
    ReplyBundleMessage(
      TextTrigger(
        StringTextTriggerValue("questo ragazzo"),
        StringTextTriggerValue("eccitare"),
        StringTextTriggerValue("lucio dalla")
      ),
      List(
        MediaFile("rphjb_LucioDalla.mp3")
      )
    ),
    ReplyBundleMessage(
      TextTrigger(
        StringTextTriggerValue("chiesa"),
        StringTextTriggerValue("preghiera"),
        StringTextTriggerValue("non credo")
      ),
      List(
        MediaFile("rphjb_Chiesa.mp3")
      )
    ),
    ReplyBundleMessage(
      TextTrigger(
        StringTextTriggerValue("maledetto")
      ),
      List(
        MediaFile("rphjb_Maledetto.mp3")
      )
    ),
    ReplyBundleMessage(
      TextTrigger(
        StringTextTriggerValue("..magari"),
        StringTextTriggerValue("magari.."),
      ),
      List(
        MediaFile("rphjb_Magari.mp3")
      )
    ),
    ReplyBundleMessage(
      TextTrigger(
        StringTextTriggerValue("io ti aiuto")
      ),
      List(
        MediaFile("rphjb_Aiuto.mp3")
      )
    ),
    ReplyBundleMessage(
      TextTrigger(
        StringTextTriggerValue("faccio schifo")
      ),
      List(
        MediaFile("rphjb_FaccioSchifo.mp3")
      )
    ),
    ReplyBundleMessage(
      TextTrigger(
        RegexTextTriggerValue("ci sei ritornat[ao]".r)
      ),
      List(
        MediaFile("rphjb_Ritornata.mp3")
      )
    ),
    ReplyBundleMessage(
      TextTrigger(
        StringTextTriggerValue("che succede")
      ),
      List(
        MediaFile("rphjb_CheSuccede.mp3")
      )
    ),
    ReplyBundleMessage(
      TextTrigger(
        StringTextTriggerValue("poveri cretini"),
        StringTextTriggerValue("poveri ignoranti")
      ),
      List(
        MediaFile("rphjb_PoveriCretini.mp3")
      )
    ),
    ReplyBundleMessage(
      TextTrigger(
        StringTextTriggerValue("due ossa")
      ),
      List(
        MediaFile("rphjb_DueOssa.mp3")
      )
    ),
    ReplyBundleMessage(
      TextTrigger(
        RegexTextTriggerValue("m[ie] fai( proprio)? schifo".r)
      ),
      List(
        MediaFile("rphjb_Schifo.mp3")
      )
    ),
    ReplyBundleMessage(
      TextTrigger(
        StringTextTriggerValue("una sera")
      ),
      List(
        MediaFile("rphjb_Sera.mp3")
      )
    ),
    ReplyBundleMessage(
      TextTrigger(
        StringTextTriggerValue("il venerdì")
      ),
      List(
        MediaFile("rphjb_Venerdi.mp3")
      )
    ),
    ReplyBundleMessage(
      TextTrigger(
        RegexTextTriggerValue("oppur[ae]".r)
      ),
      List(
        MediaFile("rphjb_Oppura.mp3")
      )
    ),
    ReplyBundleMessage(
      TextTrigger(
        StringTextTriggerValue("cosa preferisci"),
        StringTextTriggerValue("ragazzetta"),
        StringTextTriggerValue("carne bianca"),
        StringTextTriggerValue("carne saporita")
      ),
      List(
        MediaFile("rphjb_RagazzettaCarne.mp3")
      )
    ),
    ReplyBundleMessage(
      TextTrigger(
        StringTextTriggerValue("fragolina"),
        StringTextTriggerValue("fichina")
      ),
      List(
        MediaFile("rphjb_FragolinaFichina.mp3")
      )
    ),
    ReplyBundleMessage(
      TextTrigger(
        StringTextTriggerValue("non vi deluderò")
      ),
      List(
        MediaFile("rphjb_NonViDeludero.mp3")
      )
    ),
    ReplyBundleMessage(
      TextTrigger(
        StringTextTriggerValue("vi saluto")
      ),
      List(
        MediaFile("rphjb_ViSaluto.mp3")
      )
    ),
    ReplyBundleMessage(
      TextTrigger(
        StringTextTriggerValue("gigi d'alessio"),
        StringTextTriggerValue("anna tatangelo")
      ),
      List(
        MediaFile("rphjb_GigiDAlessioAnnaTatangelo.mp3")
      )
    ),
    ReplyBundleMessage(
      TextTrigger(
        RegexTextTriggerValue("funzioni gene[g]{2,}iative".r),
        StringTextTriggerValue("non è un uomo"),
        StringTextTriggerValue("voce da uomo"),
        RegexTextTriggerValue("è (veramente )?una donna".r)
      ),
      List(
        MediaFile("rphjb_VoceDaUomo.mp3")
      )
    ),
    ReplyBundleMessage(
      TextTrigger(
        StringTextTriggerValue("culo chiacchierato"),
        StringTextTriggerValue("rob halford")
      ),
      List(
        MediaFile("rphjb_CuloChiacchierato.mp3")
      )
    ),
    ReplyBundleMessage(
      TextTrigger(
        StringTextTriggerValue("negri"),
        StringTextTriggerValue("sezione ritmica")
      ),
      List(
        MediaFile("rphjb_NegriSezioneRitmica.mp3")
      )
    )
  )

  def messageRepliesGifData[F[_]: Applicative]: List[ReplyBundleMessage[F]] = List(
    ReplyBundleMessage(
      TextTrigger(
        RegexTextTriggerValue("\\bcontinua\\b".r)
      ),
      List(
        MediaFile("rphjb_Continua.mp3"),
        MediaFile("rphjb_Continua.mp4")
      ),
      replySelection = RandomSelection
    ),
    ReplyBundleMessage(
      TextTrigger(
        StringTextTriggerValue("schifose"),
        StringTextTriggerValue("ultime")
      ),
      List(
        MediaFile("rphjb_SchifoseUltime.gif")
      )
    ),
    ReplyBundleMessage(
      TextTrigger(
        RegexTextTriggerValue("(af+)?fanculo(,)? per contesia".r)
      ),
      List(
        MediaFile("rphjb_FanculoPerCortesia.gif")
      )
    ),
    ReplyBundleMessage(
      TextTrigger(
        StringTextTriggerValue("gli autori")
      ),
      List(
        MediaFile("rphjb_Autori.gif")
      )
    ),
    ReplyBundleMessage(
      TextTrigger(
        StringTextTriggerValue("questo è matto")
      ),
      List(
        MediaFile("rphjb_MattoRagazzi.gif")
      )
    ),
    ReplyBundleMessage(
      TextTrigger(
        StringTextTriggerValue("scivola")
      ),
      List(
        MediaFile("rphjb_SiScivola.mp3")
      )
    ),
    ReplyBundleMessage(
      TextTrigger(
        StringTextTriggerValue("graffi")
      ),
      List(
        MediaFile("rphjb_Graffi.gif")
      )
    ),
    ReplyBundleMessage(
      TextTrigger(
        StringTextTriggerValue("mi piaccio"),
        StringTextTriggerValue("impazzire"),
      ),
      List(
        MediaFile("rphjb_MiPiaccio.gif")
      )
    ),
    ReplyBundleMessage(
      TextTrigger(
        StringTextTriggerValue("pelle d'oca"),
        StringTextTriggerValue("sussult"),
        StringTextTriggerValue("brivid")
      ),
      List(
        MediaFile("rphjb_Brivido.gif")
      )
    ),
    ReplyBundleMessage(
      TextTrigger(
        StringTextTriggerValue("che siamo noi"),
        StringTextTriggerValue("pezzi di merda"),
      ),
      List(
        MediaFile("rphjb_PezziDiMerda.gif")
      )
    ),
    ReplyBundleMessage(
      TextTrigger(
        StringTextTriggerValue(" urlo"),
        StringTextTriggerValue("urlo "),
        StringTextTriggerValue("disper"),
      ),
      List(
        MediaFile("rphjb_Tuffo.gif"),
        MediaFile("rphjb_Urlo.gif"),
        MediaFile("rphjb_Urlo3.gif"),
        MediaFile("rphjb_Urlo2.gif"),
        MediaFile("rphjb_UrloCanaro.gif")
      ),
      replySelection = RandomSelection
    ),
    ReplyBundleMessage(
      TextTrigger(
        StringTextTriggerValue("rispondere")
      ),
      List(
        MediaFile("rphjb_Rispondere.gif")
      )
    ),
    ReplyBundleMessage(
      TextTrigger(
        StringTextTriggerValue("cuore in mano"),
        StringTextTriggerValue("mano nella mano"),
        StringTextTriggerValue("pelle contro la pelle")
      ),
      List(
        MediaFile("rphjb_CuoreInMano.gif")
      )
    ),
    ReplyBundleMessage(
      TextTrigger(
        StringTextTriggerValue("stato brado")
      ),
      List(
        MediaFile("rphjb_StatoBrado.gif")
      )
    ),
    ReplyBundleMessage(
      TextTrigger(
        StringTextTriggerValue("pronto dimmi")
      ),
      List(
        MediaFile("rphjb_ProntoDimmi.gif")
      )
    ),
    ReplyBundleMessage(
      TextTrigger(
        RegexTextTriggerValue(" port[a]+( |$)".r)
      ),
      List(
        MediaFile("rphjb_Porta.gif")
      )
    ),
    ReplyBundleMessage(
      TextTrigger(
        StringTextTriggerValue("prendo quello che cazzo c'è da prendere"),
        StringTextTriggerValue("prendo il motorino"),
        StringTextTriggerValue("prendo la macchina"),
        StringTextTriggerValue("prendo l'auto"),
      ),
      List(
        MediaFile("rphjb_PrendoIlNecessario.gif")
      )
    ),
    ReplyBundleMessage(
      TextTrigger(
        StringTextTriggerValue("nella gola")
      ),
      List(
        MediaFile("rphjb_NellaGola.gif")
      )
    ),
    ReplyBundleMessage(
      TextTrigger(
        StringTextTriggerValue("siamo qua")
      ),
      List(
        MediaFile("rphjb_SiamoQua.gif")
      )
    ),
    ReplyBundleMessage(
      TextTrigger(
        StringTextTriggerValue("cucciolo")
      ),
      List(
        MediaFile("rphjb_Cucciolo.gif"),
        MediaFile("rphjb_Cucciolo2.gif"),
      ),
      replySelection = RandomSelection
    ),
    ReplyBundleMessage(
      TextTrigger(
        StringTextTriggerValue("Che c'hai")
      ),
      List(
        MediaFile("rphjb_CheCHai.gif")
      )
    ),
    ReplyBundleMessage(
      TextTrigger(
        StringTextTriggerValue("06"),
        StringTextTriggerValue("prefisso")
      ),
      List(
        MediaFile("rphjb_06.gif")
      )
    ),
    ReplyBundleMessage(
      TextTrigger(
        StringTextTriggerValue("abbiamo vinto")
      ),
      List(
        MediaFile("rphjb_AbbiamoVinto.gif")
      )
    ),
    ReplyBundleMessage(
      TextTrigger(
        StringTextTriggerValue("telefonata pilotata"),
        StringTextTriggerValue("falsità")
      ),
      List(
        MediaFile("rphjb_TelefonataPilotata.gif")
      )
    ),
    ReplyBundleMessage(
      TextTrigger(
        StringTextTriggerValue("ed è vero")
      ),
      List(
        MediaFile("rphjb_Vero.gif")
      )
    ),
    ReplyBundleMessage(
      TextTrigger(
        StringTextTriggerValue("quante ore"),
        StringTextTriggerValue("quanto tempo")
      ),
      List(
        MediaFile("rphjb_QuanteOre.gif")
      )
    ),
    ReplyBundleMessage(
      TextTrigger(
        RegexTextTriggerValue("entrat[oa]".r)
      ),
      List(
        MediaFile("rphjb_ComeHaFattoAEntrare.gif")
      )
    ),
    ReplyBundleMessage(
      TextTrigger(
        StringTextTriggerValue("donna cane")
      ),
      List(
        MediaFile("rphjb_DonnaCane.gif")
      )
    ),
    ReplyBundleMessage(
      TextTrigger(
        StringTextTriggerValue("menzion")
      ),
      List(
        MediaFile("rphjb_NonMiMenzionareQuestaParola.gif")
      )
    ),
    ReplyBundleMessage(
      TextTrigger(
        StringTextTriggerValue("hollywood")
      ),
      List(
        MediaFile("rphjb_Hollywood.gif")
      )
    ),
    ReplyBundleMessage(
      TextTrigger(
        StringTextTriggerValue("piano superiore"),
        StringTextTriggerValue("compete"),
        StringTextTriggerValue("gerarca")
      ),
      List(
        MediaFile("rphjb_PianoSuperioreCompete.gif")
      )
    ),
    ReplyBundleMessage(
      TextTrigger(
        StringTextTriggerValue("chi è")
      ),
      List(
        MediaFile("rphjb_QuestaPersonaScusate.gif")
      )
    ),
    ReplyBundleMessage(
      TextTrigger(
        StringTextTriggerValue("calcolo"),
        StringTextTriggerValue("matematica")
      ),
      List(
        MediaFile("rphjb_MiPareLogico.gif")
      )
    ),
    ReplyBundleMessage(
      TextTrigger(
        StringTextTriggerValue("tucul")
      ),
      List(
        MediaFile("rphjb_TuCul.gif")
      )
    ),
    ReplyBundleMessage(
      TextTrigger(
        StringTextTriggerValue(";)"),
        StringTextTriggerValue("occhiolino"),
        StringTextTriggerValue("wink"),
        StringTextTriggerValue(e":wink:")
      ),
      List(
        MediaFile("rphjb_Occhiolino.gif")
      )
    ),
    ReplyBundleMessage(
      TextTrigger(
        StringTextTriggerValue("soffro")
      ),
      List(
        MediaFile("rphjb_Soffro.gif")
      )
    ),
    ReplyBundleMessage(
      TextTrigger(
        StringTextTriggerValue("indispettirmi"),
        StringTextTriggerValue("oltrepassare"),
        StringTextTriggerValue("divento cattivo")
      ),
      List(
        MediaFile("rphjb_Indispettirmi.gif")
      )
    ),
    ReplyBundleMessage(
      TextTrigger(
        StringTextTriggerValue("mannaggia"),
        StringTextTriggerValue("la salute")
      ),
      List(
        MediaFile("rphjb_MannaggiaLaSalute.gif")
      )
    ),
    ReplyBundleMessage(
      TextTrigger(
        StringTextTriggerValue("mi rompi il cazzo"),
        StringTextTriggerValue("mi dai fastidio")
      ),
      List(
        MediaFile("rphjb_MiRompiErCazzo.gif")
      )
    ),
    ReplyBundleMessage(
      TextTrigger(
        StringTextTriggerValue("dare fastidio")
      ),
      List(
        MediaFile("rphjb_DareFastidio.gif")
      )
    ),
    ReplyBundleMessage(
      TextTrigger(
        StringTextTriggerValue("succh"),
        StringTextTriggerValue("olio di croce")
      ),
      List(
        MediaFile("rphjb_OlioDiCroce.gif")
      )
    ),
    ReplyBundleMessage(
      TextTrigger(
        StringTextTriggerValue("c'ha [pure ]?ragione"),
        StringTextTriggerValue("o no?")
      ),
      List(
        MediaFile("rphjb_Ragione.gif")
      )
    ),
    ReplyBundleMessage(
      TextTrigger(
        StringTextTriggerValue("generi musicali"),
        RegexTextTriggerValue("solo il me(t|d)al".r)
      ),
      List(
        MediaFile("rphjb_GeneriMusicali.gif")
      )
    ),
    ReplyBundleMessage(
      TextTrigger(
        StringTextTriggerValue("bassista"),
        StringTextTriggerValue("slap")
      ),
      List(
        MediaFile("rphjb_Bassista.gif")
      )
    ),
    ReplyBundleMessage(
      TextTrigger(
        StringTextTriggerValue("discoteca laziale")
      ),
      List(
        MediaFile("rphjb_DiscotecaLaziale.gif")
      )
    ),
    ReplyBundleMessage(
      TextTrigger(
        StringTextTriggerValue("cosa è successo")
      ),
      List(
        MediaFile("rphjb_CosaSuccesso.gif")
      )
    ),
    ReplyBundleMessage(
      TextTrigger(
        StringTextTriggerValue("perchè mi guardi")
      ),
      List(
        MediaFile("rphjb_Guardi.gif")
      )
    ),
    ReplyBundleMessage(
      TextTrigger(
        RegexTextTriggerValue("[cg]hi[td]a[r]+is[td]a [bp]referi[dt]o".r)
      ),
      List(
        MediaFile("rphjb_Chitarrista.gif")
      )
    ),
    ReplyBundleMessage(
      TextTrigger(
        StringTextTriggerValue("divert")
      ),
      List(
        MediaFile("rphjb_Diverti.gif")
      )
    ),
    ReplyBundleMessage(
      TextTrigger(
        StringTextTriggerValue("e parla")
      ),
      List(
        MediaFile("rphjb_Parla.gif")
      )
    ),
    ReplyBundleMessage(
      TextTrigger(
        StringTextTriggerValue("uno scherzo")
      ),
      List(
        MediaFile("rphjb_Scherzo.gif")
      )
    ),
    ReplyBundleMessage(
      TextTrigger(
        StringTextTriggerValue("che si deve fà"),
        StringTextTriggerValue("campà"),
      ),
      List(
        MediaFile("rphjb_Campa.gif")
      )
    ),
    ReplyBundleMessage(
      TextTrigger(
        StringTextTriggerValue("pensa alla deficienza"),
        RegexTextTriggerValue("ma si può dire una cosa (del genere|così)".r),
      ),
      List(
        MediaFile("rphjb_Deficienza.gif")
      )
    ),
    ReplyBundleMessage(
      TextTrigger(
        RegexTextTriggerValue("lo[g]+i[cg]o".r)
      ),
      List(
        MediaFile("rphjb_MiPareLogico.gif"),
        MediaFile("rphjb_SembraLogico.gif")
      ),
      replySelection = RandomSelection
    ),
    ReplyBundleMessage(
      TextTrigger(
        RegexTextTriggerValue("e(s|c)certo".r),
        StringTextTriggerValue("accetto le critiche"),
        StringTextTriggerValue("non me ne frega un cazzo")
      ),
      List(
        MediaFile("rphjb_Escerto.gif")
      )
    ),
    ReplyBundleMessage(
      TextTrigger(
        RegexTextTriggerValue("levati[/. ]*dai coglioni".r),
        RegexTextTriggerValue("fuori[/. ]*dai coglioni".r)
      ),
      List(
        MediaFile("rphjb_LevatiDaiCoglioni.gif")
      )
    ),
    ReplyBundleMessage(
      TextTrigger(
        RegexTextTriggerValue("(non|mica) so(no)? (un |n )?co(gl|j)ione".r),
        RegexTextTriggerValue("sarete co(gl|j)ioni voi".r)
      ),
      List(
        MediaFile("rphjb_SareteCoglioniVoi.gif")
      )
    ),
    ReplyBundleMessage(
      TextTrigger(
        RegexTextTriggerValue("più co(gl|j)ione".r),
        RegexTextTriggerValue("dice co(gl|j)ione".r),
        RegexTextTriggerValue("co(gl|j)ion([e]{3,}|e[!]{3,})".r)
      ),
      List(
        MediaFile("rphjb_Coglione.gif"),
        MediaFile("rphjb_PiuCoglione.gif"),
        MediaFile("rphjb_Coglione.mp4")
      ),
      replySelection = RandomSelection
    ),
    ReplyBundleMessage(
      TextTrigger(
        StringTextTriggerValue("bravo!!!"),
        StringTextTriggerValue("bravooo")
      ),
      List(
        MediaFile("rphjb_Bravo.gif")
      )
    ),
    ReplyBundleMessage(
      TextTrigger(
        StringTextTriggerValue("capolavoro")
      ),
      List(
        MediaFile("rphjb_Capolavoro.gif")
      )
    ),
    ReplyBundleMessage(
      TextTrigger(
        StringTextTriggerValue(" metal")
      ),
      List(
        MediaFile("rphjb_Metal.gif")
      )
    ),
    ReplyBundleMessage(
      TextTrigger(
        StringTextTriggerValue("allucinante")
      ),
      List(
        MediaFile("rphjb_Allucinante.gif")
      )
    ),
    ReplyBundleMessage(
      TextTrigger(
        StringTextTriggerValue("mare di cazzate"),
        StringTextTriggerValue("non è possibile")
      ),
      List(
        MediaFile("rphjb_NonPossibile.gif")
      )
    ),
    ReplyBundleMessage(
      TextTrigger(
        StringTextTriggerValue("porca miseria"),
        StringTextTriggerValue("incazzare")
      ),
      List(
        MediaFile("rphjb_PorcaMiseria.gif")
      )
    ),
    ReplyBundleMessage(
      TextTrigger(
        RegexTextTriggerValue("schifos(o|!)+".r)
      ),
      List(
        MediaFile("rphjb_Schifoso.gif")
      )
    ),
    ReplyBundleMessage(
      TextTrigger(
        StringTextTriggerValue("facendo soffrire")
      ),
      List(
        MediaFile("rphjb_FacendoSoffrire.gif")
      )
    ),
    ReplyBundleMessage(
      TextTrigger(
        StringTextTriggerValue("dovete soffrire")
      ),
      List(
        MediaFile("rphjb_DoveteSoffrire.gif")
      )
    ),
    ReplyBundleMessage(
      TextTrigger(
        StringTextTriggerValue("sarete puniti")
      ),
      List(
        MediaFile("rphjb_SaretePuniti.gif")
      )
    ),
    ReplyBundleMessage(
      TextTrigger(
        StringTextTriggerValue("cantanti"),
        StringTextTriggerValue("serie z")
      ),
      List(
        MediaFile("rphjb_CantantiSerieZ.gif")
      )
    ),
    ReplyBundleMessage(
      TextTrigger(
        StringTextTriggerValue("sentendo male")
      ),
      List(
        MediaFile("rphjb_MiStoSentendoMale.gif")
      )
    ),
    ReplyBundleMessage(
      TextTrigger(
        StringTextTriggerValue("lunghezza d'onda"),
        StringTextTriggerValue("brave persone")
      ),
      List(
        MediaFile("rphjb_LunghezzaDOnda.gif")
      )
    ),
    ReplyBundleMessage(
      TextTrigger(
        StringTextTriggerValue("delirio")
      ),
      List(
        MediaFile("rphjb_Delirio.gif")
      )
    ),
    ReplyBundleMessage(
      TextTrigger(
        StringTextTriggerValue("querelare"),
        StringTextTriggerValue("guerelare")
      ),
      List(
        MediaFile("rphjb_Querelare.gif")
      )
    ),
    ReplyBundleMessage(
      TextTrigger(
        StringTextTriggerValue("cantate"),
        StringTextTriggerValue("arigliano")
      ),
      List(
        MediaFile("rphjb_Arigliano.gif")
      )
    ),
    ReplyBundleMessage(
      TextTrigger(
        StringTextTriggerValue("andati al cesso"),
        StringTextTriggerValue("diecimila volte")
      ),
      List(
        MediaFile("rphjb_Alcesso.gif")
      )
    ),
    ReplyBundleMessage(
      TextTrigger(
        StringTextTriggerValue("non manca niente"),
        StringTextTriggerValue("c'è tutto")
      ),
      List(
        MediaFile("rphjb_NonMancaNiente.gif")
      )
    ),
    ReplyBundleMessage(
      TextTrigger(
        StringTextTriggerValue("in fila")
      ),
      List(
        MediaFile("rphjb_MettitiInFila.gif")
      )
    ),
    ReplyBundleMessage(
      TextTrigger(
        StringTextTriggerValue("non male")
      ),
      List(
        MediaFile("rphjb_NonMale.gif")
      )
    ),
    ReplyBundleMessage(
      TextTrigger(
        StringTextTriggerValue("si sente")
      ),
      List(
        MediaFile("rphjb_SiSente.gif")
      )
    ),
    ReplyBundleMessage(
      TextTrigger(
        StringTextTriggerValue("colpa vostra"),
        StringTextTriggerValue("pazzo"),
        RegexTextTriggerValue("(divento|diventare|sono) matto".r)
      ),
      List(
        MediaFile("rphjb_StoDiventandoPazzo.gif")
      )
    ),
    ReplyBundleMessage(
      TextTrigger(
        StringTextTriggerValue("sorca"),
        StringTextTriggerValue("patonza"),
        StringTextTriggerValue("lecciso"),
        RegexTextTriggerValue("\\bfi[cg]a\\b".r)
      ),
      List(
        MediaFile("rphjb_SorcaLecciso.gif"),
        MediaFile("rphjb_FigaLarga.mp4"),
        MediaFile("rphjb_FragolinaFichina.mp3"),
        MediaFile("rphjb_Sorca.mp4")
      ),
      replySelection = RandomSelection
    ),
    ReplyBundleMessage(
      TextTrigger(
        StringTextTriggerValue("non li sopporto"),
        RegexTextTriggerValue("che si deve f(à|are)".r),
        StringTextTriggerValue("bisogna pure lavorà")
      ),
      List(
        MediaFile("rphjb_NonLiSopporto.gif")
      )
    ),
    ReplyBundleMessage(
      TextTrigger(
        StringTextTriggerValue("chi cazzo sei")
      ),
      List(
        MediaFile("rphjb_ChiCazzoSei.gif")
      )
    ),
    ReplyBundleMessage(
      TextTrigger(
        StringTextTriggerValue("feste")
      ),
      List(
        MediaFile("rphjb_Feste.gif")
      )
    ),
    ReplyBundleMessage(
      TextTrigger(
        StringTextTriggerValue("si ostina"),
        StringTextTriggerValue("foto vecchie")
      ),
      List(
        MediaFile("rphjb_Ostina.gif")
      )
    ),
    ReplyBundleMessage(
      TextTrigger(
        RegexTextTriggerValue("(è|diventa) vecchi[ao]".r),
      ),
      List(
        MediaFile("rphjb_Vecchio.gif")
      )
    ),
    ReplyBundleMessage(
      TextTrigger(
        StringTextTriggerValue("scatta qualcosa")
      ),
      List(
        MediaFile("rphjb_ScattaQualcosa.gif")
      )
    ),
    ReplyBundleMessage(
      TextTrigger(
        StringTextTriggerValue("pure bona")
      ),
      List(
        MediaFile("rphjb_Bona.gif")
      )
    ),
    ReplyBundleMessage(
      TextTrigger(
        StringTextTriggerValue("danza macabra"),
      ),
      List(
        MediaFile("rphjb_DanzaMacabra.gif"),
        MediaFile("rphjb_DanzaMacabra.mp4")
      ),
      replySelection = RandomSelection
    ),
    ReplyBundleMessage(
      TextTrigger(
        RegexTextTriggerValue("sei [gc]ambiat[oa]".r)
      ),
      List(
        MediaFile("rphjb_SeiCambiata.gif")
      )
    ),
    ReplyBundleMessage(
      TextTrigger(
        StringTextTriggerValue("mio discapito"),
        StringTextTriggerValue("disgabido")
      ),
      List(
        MediaFile("rphjb_Discapito.gif")
      )
    ),
    ReplyBundleMessage(
      TextTrigger(
        StringTextTriggerValue("squallida"),
        StringTextTriggerValue("abbia mai sentito")
      ),
      List(
        MediaFile("rphjb_Squallida.gif")
      )
    ),
    ReplyBundleMessage(
      TextTrigger(
        StringTextTriggerValue("la verità")
      ),
      List(
        MediaFile("rphjb_Verita.gif")
      )
    ),
    ReplyBundleMessage(
      TextTrigger(
        StringTextTriggerValue("ti dovresti vergognare")
      ),
      List(
        MediaFile("rphjb_TiDovrestiVergognare.gif")
      )
    ),
    ReplyBundleMessage(
      TextTrigger(
        RegexTextTriggerValue("nooo[o]*".r)
      ),
      List(
        MediaFile("rphjb_No.gif")
      )
    ),
    ReplyBundleMessage(
      TextTrigger(
        StringTextTriggerValue("destino"),
        StringTextTriggerValue("incontrare")
      ),
      List(
        MediaFile("rphjb_Destino.gif")
      )
    ),
    ReplyBundleMessage(
      TextTrigger(
        StringTextTriggerValue("meridionale"),
        StringTextTriggerValue("terron")
      ),
      List(
        MediaFile("rphjb_Meridionale.gif")
      )
    ),
    ReplyBundleMessage(
      TextTrigger(
        StringTextTriggerValue("baci"),
        StringTextTriggerValue("limonare"),
        StringTextTriggerValue("peggio cose")
      ),
      List(
        MediaFile("rphjb_Bacio.gif"),
        MediaFile("rphjb_DanzaMacabra.mp4")
      ),
      replySelection = RandomSelection
    ),
    ReplyBundleMessage(
      TextTrigger(
        StringTextTriggerValue("solo uno parló"),
        RegexTextTriggerValue("[cg]ri[dt]i[gc]a[dt]o".r)
      ),
      List(
        MediaFile("rphjb_FuCriticato.gif")
      )
    ),
    ReplyBundleMessage(
      TextTrigger(
        StringTextTriggerValue("giudica")
      ),
      List(
        MediaFile("rphjb_Giudicate.gif"),
        MediaFile("rphjb_ComeFaiAGiudicare.gif")
      ),
      replySelection = RandomSelection
    ),
    ReplyBundleMessage(
      TextTrigger(
        StringTextTriggerValue("drogati"),
        StringTextTriggerValue("sostanze improprie")
      ),
      List(
        MediaFile("rphjb_DrogatiRockettari1.gif"),
        MediaFile("rphjb_DrogatiRockettari.mp4"),
        MediaFile("rphjb_EricClaptonDrogaUominiAffari.mp4"),
        MediaFile("rphjb_DrogatiRockettari2.gif"),
        MediaFile("rphjb_DrogatiPiloti.gif"),
        MediaFile("rphjb_DrogatiPiloti.mp4"),
        MediaFile("rphjb_Rampolli.mp4")
      ),
      replySelection = RandomSelection
    ),
    ReplyBundleMessage(
      TextTrigger(
        RegexTextTriggerValue("ro[ckgh]+(ch|gh|k)e(d+|t+)ari".r),
        StringTextTriggerValue("stillati")
      ),
      List(
        MediaFile("rphjb_DrogatiRockettari1.gif"),
        MediaFile("rphjb_DrogatiRockettari.mp4"),
        MediaFile("rphjb_DrogatiRockettari2.gif"),
        MediaFile("rphjb_RocchettariCapelliCortiGiaccaCravattaPassaportoStronzi.mp4")
      ),
      replySelection = RandomSelection
    ),
    ReplyBundleMessage(
      TextTrigger(
        StringTextTriggerValue("sguardo")
      ),
      List(
        MediaFile("rphjb_Sguardo.gif"),
        MediaFile("rphjb_Sguardo2.gif"),
        MediaFile("rphjb_Sguardo3.gif"),
        MediaFile("rphjb_Sguardo4.gif"),
        MediaFile("rphjb_SguardoCanaro.gif")
      ),
      replySelection = RandomSelection
    ),
    ReplyBundleMessage(
      TextTrigger(
        StringTextTriggerValue("a quel punto")
      ),
      List(
        MediaFile("rphjb_QuelPunto.gif")
      )
    ),
    ReplyBundleMessage(
      TextTrigger(
        StringTextTriggerValue("quattro"),
        StringTextTriggerValue("faccio in tempo")
      ),
      List(
        MediaFile("rphjb_QuattroSolo.gif")
      )
    ),
    ReplyBundleMessage(
      TextTrigger(
        StringTextTriggerValue("faccio la parte"),
        StringTextTriggerValue(" recit"),
        StringTextTriggerValue(" fing"),
        RegexTextTriggerValue("a[t]{2,}[o]+re".r),
        StringTextTriggerValue("attrice")
      ),
      List(
        MediaFile("rphjb_FaccioLaParte.gif")
      )
    ),
    ReplyBundleMessage(
      TextTrigger(
        StringTextTriggerValue("assolutamente no"),
        StringTextTriggerValue("non mi lamento")
      ),
      List(
        MediaFile("rphjb_NonMiLamento.gif")
      )
    ),
    ReplyBundleMessage(
      TextTrigger(
        StringTextTriggerValue("inizio della fine")
      ),
      List(
        MediaFile("rphjb_InizioDellaFine.gif"),
        MediaFile("rphjb_InizioDellaFine.mp4")
      ),
      replySelection = RandomSelection
    ),
    ReplyBundleMessage(
      TextTrigger(
        StringTextTriggerValue("il senso")
      ),
      List(
        MediaFile("rphjb_IlSensoCapito.gif")
      )
    ),
    ReplyBundleMessage(
      TextTrigger(
        RegexTextTriggerValue("\\bester\\b".r)
      ),
      List(
        MediaFile("rphjb_Ester.gif"),
        MediaFile("rphjb_Ester2.gif"),
      ),
      replySelection = RandomSelection
    ),
    ReplyBundleMessage(
      TextTrigger(
        RegexTextTriggerValue("abi[td]ua[td]o".r),
        RegexTextTriggerValue("proprioll[aà]".r),
      ),
      List(
        MediaFile("rphjb_Propriolla.gif")
      )
    ),
        ReplyBundleMessage(
      TextTrigger(
        RegexTextTriggerValue("non vedo questo grande problema".r)
      ),
      List(
        MediaFile("rphjb_VabbeProblema.gif"),
      ),
    ),
  )

  def messageRepliesVideoData[F[_]: Applicative]: List[ReplyBundleMessage[F]] = List(
    ReplyBundleMessage(
      TextTrigger(
        StringTextTriggerValue("amici veri"),
        StringTextTriggerValue("soldati")
      ),
      List(
        MediaFile("rphjb_AmiciVeriVecchiSoldati.mp4")
      )
    ),
    ReplyBundleMessage(
      TextTrigger(
        StringTextTriggerValue("gianni neri")
      ),
      List(
        MediaFile("rphjb_RingraziareGianniTraffico.mp4"),
        MediaFile("rphjb_GianniNeriCoppiaMiciciale.mp4"),
        MediaFile("rphjb_GianniNeriCheFineHaiFatto.mp4")
      ),
      replySelection = RandomSelection
    ),
    ReplyBundleMessage(
      TextTrigger(
        RegexTextTriggerValue("accor(data|dana)".r)
      ),
      List(
        MediaFile("rphjb_Accordana.mp4")
      )
    ),
    ReplyBundleMessage(
      TextTrigger(
        RegexTextTriggerValue("\\brap\\b".r),
        StringTextTriggerValue("musica italiana"),
        StringTextTriggerValue("tullio pane"),
        StringTextTriggerValue("otello profazio"),
        StringTextTriggerValue("mario lanza"),
        StringTextTriggerValue("gianni celeste"),
        StringTextTriggerValue("luciano tajoli")
      ),
      List(
        MediaFile("rphjb_RapMusicaMelodicaListaCantanti.mp4")
      )
    ),
    ReplyBundleMessage(
      TextTrigger(
        StringTextTriggerValue("eric clapton"),
        RegexTextTriggerValue("uo(m)+ini d'affari".r),
      ),
      List(
        MediaFile("rphjb_EricClaptonDrogaUominiAffari.mp4")
      )
    ),
    ReplyBundleMessage(
      TextTrigger(
        StringTextTriggerValue("rampolli"),
        StringTextTriggerValue("studi a boston"),
        StringTextTriggerValue("borghesia alta"),
        StringTextTriggerValue("idoli delle mamme"),
        StringTextTriggerValue("figliole")
      ),
      List(
        MediaFile("rphjb_Rampolli.mp4")
      )
    ),
    ReplyBundleMessage(
      TextTrigger(
        StringTextTriggerValue("capelli corti"),
        StringTextTriggerValue("giacca"),
        StringTextTriggerValue("cravatta"),
        StringTextTriggerValue("passaporto degli stronzi")
      ),
      List(
        MediaFile("rphjb_RocchettariCapelliCortiGiaccaCravattaPassaportoStronzi.mp4")
      )
    ),
    ReplyBundleMessage(
      TextTrigger(
        RegexTextTriggerValue("fregat(a|ura)".r)
      ),
      List(
        MediaFile("rphjb_FregataFregatura.mp4")
      )
    ),
    ReplyBundleMessage(
      TextTrigger(
        RegexTextTriggerValue("\\bmula\\b".r),
        StringTextTriggerValue("storia della mula")
      ),
      List(
        MediaFile("rphjb_Mula.mp4")
      )
    ),
    ReplyBundleMessage(
      TextTrigger(
        StringTextTriggerValue("si o no")
      ),
      List(
        MediaFile("rphjb_SiONo.mp4")
      )
    ),
    ReplyBundleMessage(
      TextTrigger(
        StringTextTriggerValue("streghe")
      ),
      List(
        MediaFile("rphjb_Streghe.mp4")
      )
    ),
    ReplyBundleMessage(
      TextTrigger(
        RegexTextTriggerValue("(tornando|andando) (all')?indietro".r),
        StringTextTriggerValue("innovazione")
      ),
      List(
        MediaFile("rphjb_InnovazioneStiamoTornandoIndietro.mp4")
      )
    ),
    ReplyBundleMessage(
      TextTrigger(
        StringTextTriggerValue("trovamelo")
      ),
      List(
        MediaFile("rphjb_AngeloTrovamelo.mp4")
      )
    ),
    ReplyBundleMessage(
      TextTrigger(
        StringTextTriggerValue("plettro"),
        StringTextTriggerValue("vicoletto"),
        StringTextTriggerValue("scopata")
      ),
      List(
        MediaFile("rphjb_ChitarraPlettroVicoletto.mp4")
      )
    ),
    ReplyBundleMessage(
      TextTrigger(
        StringTextTriggerValue("diversi mondi"),
        StringTextTriggerValue("letti sfatti")
      ),
      List(
        MediaFile("rphjb_LettiSfattiDiversiMondi.mp4")
      )
    ),
    ReplyBundleMessage(
      TextTrigger(
        StringTextTriggerValue("via delle albizzie"),
        StringTextTriggerValue("carpenelli")
      ),
      List(
        MediaFile("rphjb_AlbizziePerlaPioggia.mp4")
      )
    ),
    ReplyBundleMessage(
      TextTrigger(
        StringTextTriggerValue("ramarro"),
        StringTextTriggerValue("yngwie"),
        StringTextTriggerValue("malmsteen"),
        StringTextTriggerValue("impellitteri")
      ),
      List(
        MediaFile("rphjb_Ramarro.mp4")
      )
    ),
    ReplyBundleMessage(
      TextTrigger(
        RegexTextTriggerValue("(cinque|5) dita".r),
        StringTextTriggerValue("pugno")
      ),
      List(
        MediaFile("rphjb_CinqueDita.mp4")
      )
    ),
    ReplyBundleMessage(
      TextTrigger(
        StringTextTriggerValue("vi dovete spaventare")
      ),
      List(
        MediaFile("rphjb_ViDoveteSpaventare.mp4")
      )
    ),
    ReplyBundleMessage(
      TextTrigger(
        StringTextTriggerValue("amore nello suonare"),
        StringTextTriggerValue("uno freddo"),
        StringTextTriggerValue("buddisti"),
      ),
      List(
        MediaFile("rphjb_AmoreSuonareFreddoBuddistiSchifoso.mp4")
      )
    ),
    ReplyBundleMessage(
      TextTrigger(
        RegexTextTriggerValue("riciclando il (suo )?peggio".r)
      ),
      List(
        MediaFile("rphjb_SteveVaiRiciclando.mp4")
      )
    ),
    ReplyBundleMessage(
      TextTrigger(
        StringTextTriggerValue("già il titolo"),
        StringTextTriggerValue("coi due punti"),
        RegexTextTriggerValue("re[a]?l illusions".r)
      ),
      List(
        MediaFile("rphjb_RelIllusions.mp4")
      )
    ),
    ReplyBundleMessage(
      TextTrigger(
        StringTextTriggerValue("trattori"),
        StringTextTriggerValue("palmizio"),
        StringTextTriggerValue("meno c'è"),
        StringTextTriggerValue("meno si rompe")
      ),
      List(
        MediaFile("rphjb_Palmizio.mp4")
      )
    ),
    ReplyBundleMessage(
      TextTrigger(
        StringTextTriggerValue("peso di un cervello")
      ),
      List(
        MediaFile("rphjb_VitaNemicoCervello.mp4")
      )
    ),
    ReplyBundleMessage(
      TextTrigger(
        StringTextTriggerValue("cervello pensante"),
        StringTextTriggerValue("questa volta no"),
        StringTextTriggerValue("stupidità incresciosa")
      ),
      List(
        MediaFile("rphjb_CervelloPensante.mp4")
      )
    ),
    ReplyBundleMessage(
      TextTrigger(
        StringTextTriggerValue("percussionista"),
        StringTextTriggerValue("batterista")
      ),
      List(
        MediaFile("rphjb_CollaSerpeSigarettePercussionista.mp4")
      )
    ),
    ReplyBundleMessage(
      TextTrigger(
        StringTextTriggerValue("perla di pioggia"),
        StringTextTriggerValue("dove non piove mai")
      ),
      List(
        MediaFile("rphjb_PerlaDiPioggia.mp4"),
        MediaFile("rphjb_AlbizziePerlaPioggia.mp4")
      ),
      replySelection = RandomSelection
    ),
    ReplyBundleMessage(
      TextTrigger(
        StringTextTriggerValue("madre tortura"),
        RegexTextTriggerValue("(madre )?parrucca".r)
      ),
      List(
        MediaFile("rphjb_MadreTorturaParrucca.mp4")
      ),
    ),
    ReplyBundleMessage(
      TextTrigger(
        RegexTextTriggerValue("[l]+[i]+[b]+[e]+[r]+[i]+".r)
      ),
      List(
        MediaFile("rphjb_Liberi.mp4")
      )
    ),
    ReplyBundleMessage(
      TextTrigger(
        RegexTextTriggerValue("\\bcinta\\b".r),
        StringTextTriggerValue("bruce kulick")
      ),
      List(
        MediaFile("rphjb_CintaProblema.mp4")
      )
    ),
    ReplyBundleMessage(
      TextTrigger(
        StringTextTriggerValue("sepoltura")
      ),
      List(
        MediaFile("rphjb_SepolturaRisata.mp4")
      )
    ),
    ReplyBundleMessage(
      TextTrigger(
        RegexTextTriggerValue("\\bcolla\\b".r),
        StringTextTriggerValue("serpe e serpe")
      ),
      List(
        MediaFile("rphjb_CollaSerpe.mp4"),
        MediaFile("rphjb_CollaSerpeSigarettePercussionista.mp4")
      ),
      replySelection = RandomSelection
    ),
    ReplyBundleMessage(
      TextTrigger(
        StringTextTriggerValue("big money")
      ),
      List(
        MediaFile("rphjb_BigMoney.mp4")
      )
    ),
    ReplyBundleMessage(
      TextTrigger(
        StringTextTriggerValue("in cantina")
      ),
      List(
        MediaFile("rphjb_InCantina.mp4")
      )
    ),
    ReplyBundleMessage(
      TextTrigger(
        RegexTextTriggerValue("fregare come un co(gl|j)ione".r),
        RegexTextTriggerValue("Ges[uùù]".r)
      ),
      List(
        MediaFile("rphjb_GesuCoglione.mp4")
      )
    ),
    ReplyBundleMessage(
      TextTrigger(
        StringTextTriggerValue("con questa tecnica")
      ),
      List(
        MediaFile("rphjb_ConQuestaTecnica.mp4")
      )
    ),
    ReplyBundleMessage(
      TextTrigger(
        RegexTextTriggerValue("(mi|me) so(n|no)? rotto il ca\\b".r),
        StringTextTriggerValue("impazzisce totalmente")
      ),
      List(
        MediaFile("rphjb_RottoIlCa.mp4")
      )
    ),
    ReplyBundleMessage(
      TextTrigger(
        StringTextTriggerValue("diventare papa")
      ),
      List(
        MediaFile("rphjb_DiventarePapa.mp4")
      )
    ),
    ReplyBundleMessage(
      TextTrigger(
        RegexTextTriggerValue("[cg]hi[td]a[r]+is[td]a pi[uùú] velo[cg]e".r)
      ),
      List(
        MediaFile("rphjb_Arivato.mp4")
      )
    ),
    ReplyBundleMessage(
      TextTrigger(
        RegexTextTriggerValue("\\bbeat\\b".r),
        RegexTextTriggerValue("(e poi[ ,]?[ ]?){2,}".r),
        StringTextTriggerValue("qualche volta vedo lei"),
        StringTextTriggerValue("sfasciavamo tutti gli strumenti"),
      ),
      List(
        MediaFile("rphjb_AssoloBeat.mp4")
      )
    ),
    ReplyBundleMessage(
      TextTrigger(
        StringTextTriggerValue("buon compleanno")
      ),
      List(
        MediaFile("rphjb_Compleanno.mp4")
      )
    ),
    ReplyBundleMessage(
      TextTrigger(
        StringTextTriggerValue("ringraziare"),
        StringTextTriggerValue("traffico")
      ),
      List(
        MediaFile("rphjb_RingraziareGianniTraffico.mp4")
      )
    ),
    ReplyBundleMessage(
      TextTrigger(
        RegexTextTriggerValue("(roba|droga) tagliata male".r),
        StringTextTriggerValue("one television"),
        RegexTextTriggerValue("devo fare (un po'|un attimo) (di|de) esercitazione".r)
      ),
      List(
        MediaFile("rphjb_RockMachineIntro.mp4")
      )
    ),
    ReplyBundleMessage(
      TextTrigger(
        StringTextTriggerValue("poesia")
      ),
      List(
        MediaFile("rphjb_PoesiaMadre.mp4"),
        MediaFile("rphjb_PoesiaRock.mp4"),
        MediaFile("rphjb_Blues.mp4"),
        MediaFile("rphjb_PoesiaMaria.mp4"),
        MediaFile("rphjb_PoesiaArtistiImpiegati.mp4"),
        MediaFile("rphjb_CanzonettePoesieAuschwitzCervello.mp4")
      ),
      replySelection = RandomSelection
    ),
    ReplyBundleMessage(
      TextTrigger(
        RegexTextTriggerValue("w[e]+[l]+[a]+".r)
      ),
      List(
        MediaFile("rphjb_WelaMyFriends.mp4")
      )
    ),
    ReplyBundleMessage(
      TextTrigger(
        StringTextTriggerValue("blues"),
        StringTextTriggerValue("da piangere"),
      ),
      List(
        MediaFile("rphjb_Blues.mp4")
      )
    ),
    ReplyBundleMessage(
      TextTrigger(
        StringTextTriggerValue("sabato sera"),
        StringTextTriggerValue("suono sporco")
      ),
      List(
        MediaFile("rphjb_DelirioDelSabatoSera.mp4")
      )
    ),
    ReplyBundleMessage(
      TextTrigger(
        RegexTextTriggerValue("chi tocca (\\w)[,]? muore".r),
        RegexTextTriggerValue("ciao (2001|duemilauno)".r),
        StringTextTriggerValue("marilyn manson")
      ),
      List(
        MediaFile("rphjb_Ciao2001.mp4")
      )
    ),
    ReplyBundleMessage(
      TextTrigger(
        StringTextTriggerValue("petrucci"),
        RegexTextTriggerValue("capelli (lunghi|corti)".r),
        RegexTextTriggerValue("(impiegato statale|impiegati statali)".r),
      ),
      List(
        MediaFile("rphjb_PetrucciCapelliCorti.mp4")
      )
    ),
    ReplyBundleMessage(
      TextTrigger(
        RegexTextTriggerValue("impiegat[oi]".r),
      ),
      List(
        MediaFile("rphjb_PetrucciCapelliCorti.mp4"),
        MediaFile("rphjb_PoesiaArtistiImpiegati.mp4")
      ),
      replySelection = RandomSelection
    ),
    ReplyBundleMessage(
      TextTrigger(
        StringTextTriggerValue("progressive"),
        StringTextTriggerValue("regressive"),
        StringTextTriggerValue("i genesis")
      ),
      List(
        MediaFile("rphjb_Regressive.mp4")
      )
    ),
    ReplyBundleMessage(
      TextTrigger(
        StringTextTriggerValue("cresta dell'onda"),
        StringTextTriggerValue("orlo del crollo"),
      ),
      List(
        MediaFile("rphjb_CrestaOnda.mp4")
      )
    ),
    ReplyBundleMessage(
      TextTrigger(
        StringTextTriggerValue("stronzo")
      ),
      List(
        MediaFile("rphjb_StronzoFiglioMignotta.mp4")
      )
    ),
    ReplyBundleMessage(
      TextTrigger(
        StringTextTriggerValue("biscione"),
        StringTextTriggerValue("i piatti"),
      ),
      List(
        MediaFile("rphjb_BiscionePiatti.mp4")
      )
    ),
    ReplyBundleMessage(
      TextTrigger(
        StringTextTriggerValue("platinette"),
        StringTextTriggerValue("due persone in una"),
        StringTextTriggerValue("quando scopo me la levo"),
        StringTextTriggerValue("il mio sbadiglio"),
        StringTextTriggerValue("solo per un taglio"),
        StringTextTriggerValue("labbro superiore")
      ),
      List(
        MediaFile("rphjb_PlatinetteLabbroSuperiore.mp4")
      )
    ),
    ReplyBundleMessage(
      TextTrigger(
        StringTextTriggerValue("non aprite quella porta")
      ),
      List(
        MediaFile("rphjb_NonApriteQuellaPorta.mp4")
      )
    ),
    ReplyBundleMessage(
      TextTrigger(
        StringTextTriggerValue("paralitico")
      ),
      List(
        MediaFile("rphjb_DanzaMacabra.mp4")
      )
    ),
    ReplyBundleMessage(
      TextTrigger(
        StringTextTriggerValue("mettetevi in ginocchio"),
        StringTextTriggerValue("nuovo messia")
      ),
      List(
        MediaFile("rphjb_MetteteviInGinocchio.mp4")
      )
    ),
    ReplyBundleMessage(
      TextTrigger(
        RegexTextTriggerValue("sigarett[ea]".r)
      ),
      List(
        MediaFile("rphjb_Sigarette.mp4"),
        MediaFile("rphjb_CollaSerpeSigarettePercussionista.mp4")
      ),
      replySelection = RandomSelection
    ),
    ReplyBundleMessage(
      TextTrigger(
        StringTextTriggerValue("orecchie sensibili"),
        StringTextTriggerValue("rumore delle lacrime")
      ),
      List(
        MediaFile("rphjb_OrecchieSensibiliRumoreLacrime.mp4")
      )
    ),
    ReplyBundleMessage(
      TextTrigger(
        StringTextTriggerValue("sapere tutto"),
        StringTextTriggerValue("se non le sai le cose"),
        StringTextTriggerValue("jordan rudess"),
        StringTextTriggerValue("radio rock"),
        StringTextTriggerValue("informazioni sbagliate")
      ),
      List(
        MediaFile("rphjb_RadioRockErrori.mp4")
      )
    ),
    ReplyBundleMessage(
      TextTrigger(
        StringTextTriggerValue("uccidere")
      ),
      List(
        MediaFile("rphjb_UccidereUnaPersona.mp4")
      )
    ),
    ReplyBundleMessage(
      TextTrigger(
        StringTextTriggerValue("distruggere il proprio sesso"),
        StringTextTriggerValue("ammaestrare il dolore")
      ),
      List(
        MediaFile("rphjb_AmmaestrareIlDolore.mp4")
      )
    ),
    ReplyBundleMessage(
      TextTrigger(
        RegexTextTriggerValue("insegnante di [cg]hi[dt]arra".r)
      ),
      List(
        MediaFile("rphjb_InsegnanteDiChitarraModerna.mp4")
      )
    ),
    ReplyBundleMessage(
      TextTrigger(
        StringTextTriggerValue("pellegrinaggio"),
        StringTextTriggerValue("simposio del metallo"),
        StringTextTriggerValue("istinti musicali"),
      ),
      List(
        MediaFile("rphjb_PellegrinaggioSimposioMetallo.mp4")
      )
    ),
    ReplyBundleMessage(
      TextTrigger(
        StringTextTriggerValue("ridicoli")
      ),
      List(
        MediaFile("rphjb_Ridicoli.mp4")
      )
    ),
    ReplyBundleMessage(
      TextTrigger(
        RegexTextTriggerValue("(18|diciott['o]?) anni".r)
      ),
      List(
        MediaFile("rphjb_DiciottoAnni.mp4")
      )
    )
  )

  def messageRepliesMixData[F[_]: Applicative]: List[ReplyBundleMessage[F]] = List(
    ReplyBundleMessage(
      TextTrigger(
        StringTextTriggerValue("vivi"),
        StringTextTriggerValue("morti")
      ),
      List(
        MediaFile("rphjb_ViviMorti.mp4")
      ),
      matcher = ContainsAll
    ),
    ReplyBundleMessage(
      TextTrigger(
        StringTextTriggerValue("è un ordine")
      ),
      List(
        MediaFile("rphjb_Ordine.mp3"),
        MediaFile("rphjb_Ordine.mp4")
      ),
      replySelection = RandomSelection
    ),
    ReplyBundleMessage(
      TextTrigger(
        StringTextTriggerValue("piloti d'aereo"),
        StringTextTriggerValue("disastri aerei")
      ),
      List(
        MediaFile("rphjb_DrogatiPiloti.gif"),
        MediaFile("rphjb_DrogatiPiloti.mp4")
      ),
      replySelection = RandomSelection
    ),
    ReplyBundleMessage(
      TextTrigger(
        RegexTextTriggerValue("\\brock\\b".r)
      ),
      List(
        MediaFile("rphjb_PoesiaRock.mp4"),
        MediaFile("rphjb_Rock.mp3"),
        MediaFile("rphjb_EricClaptonDrogaUominiAffari.mp4"),
        MediaFile("rphjb_Rock.mp4")
      ),
      replySelection = RandomSelection
    ),
    ReplyBundleMessage(
      TextTrigger(
        StringTextTriggerValue("albero grande"),
        RegexTextTriggerValue("anche un('| )amplificatore".r),
      ),
      List(
        MediaFile("rphjb_PoesiaRock.mp4"),
      )
    ),
    ReplyBundleMessage(
      TextTrigger(
        StringTextTriggerValue("non sono uno del branco"),
        StringTextTriggerValue("agende"),
        StringTextTriggerValue("figli dei figli"),
        StringTextTriggerValue("quali fiori"),
        StringTextTriggerValue("diluite le vostre droghe")
      ),
      List(
        MediaFile("rphjb_GerarchieInfernali.mp4"),
      )
    ),
    ReplyBundleMessage(
      TextTrigger(
        StringTextTriggerValue("ti distruggo")
      ),
      List(
        MediaFile("rphjb_TiDistruggo.gif"),
        MediaFile("rphjb_Ramarro.mp4")
      ),
      replySelection = RandomSelection
    ),
    ReplyBundleMessage(
      TextTrigger(
        StringTextTriggerValue("cristo pinocchio"),
        StringTextTriggerValue("lumicino"),
        RegexTextTriggerValue("(strade|vie) inferiori".r)
      ),
      List(
        MediaFile("rphjb_CristoPinocchio.mp3"),
        MediaFile("rphjb_CristoPinocchio.mp4"),
        MediaFile("rphjb_PoesiaMaria.mp4")
      ),
      replySelection = RandomSelection
    ),
    ReplyBundleMessage(
      TextTrigger(
        StringTextTriggerValue("pappalardo")
      ),
      List(
        MediaFile("rphjb_Pappalardo.mp3"),
        MediaFile("rphjb_Pappalardo.mp4"),
      ),
      replySelection = RandomSelection
    ),
    ReplyBundleMessage(
      TextTrigger(
        StringTextTriggerValue("lasciami in pace"),
        RegexTextTriggerValue("\\bstronza\\b".r)
      ),
      List(
        MediaFile("rphjb_LasciamiInPace.gif"),
        MediaFile("rphjb_LasciamiInPaceStronza.mp4")
      ),
      replySelection = RandomSelection
    ),
    ReplyBundleMessage(
      TextTrigger(
        StringTextTriggerValue("rimpinzati"),
        RegexTextTriggerValue("[gc]io[gc]+ola[dt]a".r),
        StringTextTriggerValue("pandori"),
        StringTextTriggerValue("ciambelloni"),
        StringTextTriggerValue("gli amari"),
        RegexTextTriggerValue("limoncell[oi]".r),
        StringTextTriggerValue("il vino"),
        StringTextTriggerValue("ingrassati"),
        StringTextTriggerValue("andati al cesso"),
      ),
      List(
        MediaFile("rphjb_Rimpinzati.gif"),
        MediaFile("rphjb_Pasqua.mp4"),
      ),
      replySelection = RandomSelection
    ),
    ReplyBundleMessage(
      TextTrigger(
        StringTextTriggerValue("stare male"),
        StringTextTriggerValue("melensa")
      ),
      List(
        MediaFile("rphjb_MiFaStareMale.gif"),
        MediaFile("rphjb_MelensaStareMale.mp4")
      ),
      replySelection = RandomSelection
    ),
    ReplyBundleMessage(
      TextTrigger(
        RegexTextTriggerValue("[a]+[t]{2,}[e]+[n]+[z]+[i]+[o]+[n]+[e]+[!]*[!e]$".r)
      ),
      List(
        MediaFile("rphjb_Attenzione.mp3"),
        MediaFile("rphjb_Attenzione.mp4")
      ),
      replySelection = RandomSelection
    ),
    ReplyBundleMessage(
      TextTrigger(
        StringTextTriggerValue("paradosso")
      ),
      List(
        MediaFile("rphjb_Paradosso.gif"),
        MediaFile("rphjb_Paradosso.mp4")
      ),
      replySelection = RandomSelection
    ),
    ReplyBundleMessage(
      TextTrigger(
        RegexTextTriggerValue("sput[ao]".r)
      ),
      List(
        MediaFile("rphjb_Sputo.gif"),
        MediaFile("rphjb_Sputo.mp4"),
        MediaFile("rphjb_BicchiereSputoLimitazioniUomoDonna.mp4")
      ),
      replySelection = RandomSelection
    ),
    ReplyBundleMessage(
      TextTrigger(
        StringTextTriggerValue("cobelini"),
        StringTextTriggerValue("cobbolidi"),
        StringTextTriggerValue("elfi"),
        StringTextTriggerValue(" nani"),
        StringTextTriggerValue("nani "),
        StringTextTriggerValue("la mandragola"),
        StringTextTriggerValue("gobellini"),
        StringTextTriggerValue("fico sacro"),
        StringTextTriggerValue("la betulla"),
        StringTextTriggerValue("la canfora"),
        StringTextTriggerValue("le ossa dei morti")
      ),
      List(
        MediaFile("rphjb_FigureMitologiche.mp3"),
        MediaFile("rphjb_FigureMitologiche.mp4"),
        MediaFile("rphjb_FigureMitologiche2.mp4")
      ),
      replySelection = RandomSelection
    ),
    ReplyBundleMessage(
      TextTrigger(
        StringTextTriggerValue("anche la merda"),
        StringTextTriggerValue("senza culo")
      ),
      List(
        MediaFile("rphjb_Merda.mp3"),
        MediaFile("rphjb_AncheLaMerda.mp4")
      ),
      replySelection = RandomSelection
    ),
    ReplyBundleMessage(
      TextTrigger(
        StringTextTriggerValue("chiama la polizia")
      ),
      List(
        MediaFile("rphjb_ChiamaLaPolizia.gif"),
        MediaFile("rphjb_ChiamaLaPolizia.mp4")
      ),
      replySelection = RandomSelection
    ),
    ReplyBundleMessage(
      TextTrigger(
        RegexTextTriggerValue("stori(a|e)".r)
      ),
      List(
        MediaFile("rphjb_Storie.mp3"),
        MediaFile("rphjb_Storie2.mp3"),
        MediaFile("rphjb_StorieSonoTanteVecchiaccia.mp4")
      ),
      replySelection = RandomSelection
    ),
    ReplyBundleMessage(
      TextTrigger(
        RegexTextTriggerValue("au[ ]?de".r),
        RegexTextTriggerValue("\\btime\\b".r),
        RegexTextTriggerValue("uir[ ]?bi[ ]?taim".r)
      ),
      List(
        MediaFile("rphjb_Audeuirbitaim.mp3"),
        MediaFile("rphjb_Audeuirbitaim2.mp3"),
        MediaFile("rphjb_Audeuirbitaim.mp4"),
      ),
      replySelection = RandomSelection
    ),
    ReplyBundleMessage(
      TextTrigger(
        StringTextTriggerValue("micetta"),
        StringTextTriggerValue("la morte")
      ),
      List(
        MediaFile("rphjb_Micetta.gif"),
        MediaFile("rphjb_LaMorteMicetta.mp4"),
        MediaFile("rphjb_LaMorte.mp4"),
        MediaFile("rphjb_InnoAllaMorte.mp4")
      ),
      replySelection = RandomSelection
    ),
    ReplyBundleMessage(
      TextTrigger(
        StringTextTriggerValue("spalle"),
        StringTextTriggerValue("braccia"),
        RegexTextTriggerValue("t[ie] strozzo".r)
      ),
      List(
        MediaFile("rphjb_FaccioVedereSpalleBraccia.gif"),
        MediaFile("rphjb_FaccioVedereSpalleBraccia.mp4"),
        MediaFile("rphjb_UccidereUnaPersona.mp4"),
      ),
      replySelection = RandomSelection
    ),
    ReplyBundleMessage(
      TextTrigger(
        StringTextTriggerValue("non sapere"),
        RegexTextTriggerValue("aris[dt]o[dt]ele".r)
      ),
      List(
        MediaFile("rphjb_SoDiNonSapere.gif"),
        MediaFile("rphjb_SoDiNonSapere.mp4")
      ),
      replySelection = RandomSelection
    ),
    ReplyBundleMessage(
      TextTrigger(
        StringTextTriggerValue("non è roba per me")
      ),
      List(
        MediaFile("rphjb_RobaPerMe.gif"),
        MediaFile("rphjb_RobaPerMe.mp4")
      ),
      replySelection = RandomSelection
    ),
    ReplyBundleMessage(
      TextTrigger(
        RegexTextTriggerValue("io nooo[o]*".r)
      ),
      List(
        MediaFile("rphjb_IoNo.mp3"),
        MediaFile("rphjb_GesuCoglione.mp4")
      ),
      replySelection = RandomSelection
    ),
    ReplyBundleMessage(
      TextTrigger(
        StringTextTriggerValue("bastone infernale"),
        StringTextTriggerValue("un'arma")
      ),
      List(
        MediaFile("rphjb_Bastone1.gif"),
        MediaFile("rphjb_Bastone2.gif"),
        MediaFile("rphjb_Bastone3.gif"),
        MediaFile("rphjb_BastoneInfernale.mp4")
      ),
      replySelection = RandomSelection
    ),
    ReplyBundleMessage(
      TextTrigger(
        StringTextTriggerValue("vi calpesto"),
        StringTextTriggerValue("vermi"),
        StringTextTriggerValue("strisciate")
      ),
      List(
        MediaFile("rphjb_ViCalpesto.gif"),
        MediaFile("rphjb_ViCalpesto.mp4")
      ),
      replySelection = RandomSelection
    ),
    ReplyBundleMessage(
      TextTrigger(
        StringTextTriggerValue("stringere i denti"),
        StringTextTriggerValue("andare avanti"),
      ),
      List(
        MediaFile("rphjb_AndareAvanti.gif"),
        MediaFile("rphjb_AndareAvanti.mp3"),
        MediaFile("rphjb_InnovazioneStiamoTornandoIndietro.mp4")
      ),
      replySelection = RandomSelection
    ),
    ReplyBundleMessage(
      TextTrigger(
        StringTextTriggerValue("non ci credete?"),
        RegexTextTriggerValue("grande s[dt]ronza[dt][ea]".r)
      ),
      List(
        MediaFile("rphjb_NonCiCredete.gif"),
        MediaFile("rphjb_NonCiCredete.mp3"),
      ),
      replySelection = RandomSelection
    ),
    ReplyBundleMessage(
      TextTrigger(
        StringTextTriggerValue("non me ne fotte"),
        StringTextTriggerValue("chissenefrega"),
        StringTextTriggerValue("non mi interessa")
      ),
      List(
        MediaFile("rphjb_NonMeNeFotte.gif"),
        MediaFile("rphjb_NonMeNeFrega.gif"),
        MediaFile("rphjb_ENonMeNeFotteUnCazzo.mp3"),
        MediaFile("rphjb_NonLeggoQuelloCheScrivete.mp3")
      ),
      replySelection = RandomSelection
    ),
    ReplyBundleMessage(
      TextTrigger(
        StringTextTriggerValue("ultimi")
      ),
      List(
        MediaFile("rphjb_ViCalpesto.gif"),
        MediaFile("rphjb_ViCalpesto.mp4"),
        MediaFile("rphjb_Ultimi.gif"),
        MediaFile("rphjb_StateZittiZozziUltimi.mp3"),
      ),
      replySelection = RandomSelection
    ),
    ReplyBundleMessage(
      TextTrigger(
        RegexTextTriggerValue("che (cazzo )?era quella roba".r),
        RegexTextTriggerValue("che (cazzo |cazzo di roba )?mi avete dato".r),
        StringTextTriggerValue("lampi negli occhi"),
        RegexTextTriggerValue("gira(re|ra|rà|ndo)? la testa".r),
        RegexTextTriggerValue("insieme alla (c|g)o(c|g)a (c|g)ola".r)
      ),
      List(
        MediaFile("rphjb_CheCazzoEraQuellaRoba.mp3"),
        MediaFile("rphjb_CheCazzoEraQuellaRoba.mp4"),
        MediaFile("rphjb_CheCazzoEraQuellaRoba2.mp4"),
        MediaFile("rphjb_CheCazzoEraQuellaRoba3.mp4"),
        MediaFile("rphjb_RockMachineIntro.mp4"),
      ),
      replySelection = RandomSelection
    ),
    ReplyBundleMessage(
      TextTrigger(
        StringTextTriggerValue("canzonette"),
        StringTextTriggerValue("balera"),
        StringTextTriggerValue("sagra dell'uva"),
        StringTextTriggerValue("feste condominiali"),
        StringTextTriggerValue("feste di piazza")
      ),
      List(
        MediaFile("rphjb_Canzonette.mp3"),
        MediaFile("rphjb_Canzonette.mp4"),
        MediaFile("rphjb_Canzonette.gif")
      ),
      replySelection = RandomSelection
    ),
    ReplyBundleMessage(
      TextTrigger(
        StringTextTriggerValue("un pollo")
      ),
      List(
        MediaFile("rphjb_Pollo.mp3"),
        MediaFile("rphjb_Pollo.mp4"),
        MediaFile("rphjb_Pollo2.mp4"),
        MediaFile("rphjb_Pollo.gif")
      ),
      replySelection = RandomSelection
    ),
    ReplyBundleMessage(
      TextTrigger(
        StringTextTriggerValue("quello che dico io")
      ),
      List(
        MediaFile("rphjb_QuelloCheDicoIo.gif"),
        MediaFile("rphjb_FannoQuelloCheDicoIo.mp3"),
        MediaFile("rphjb_FannoQuelloCheDicoIo.mp4")
      ),
      replySelection = RandomSelection
    ),
    ReplyBundleMessage(
      TextTrigger(
        StringTextTriggerValue("zucchero")
      ),
      List(
        MediaFile("rphjb_ChitarraZuggherada.mp3"),
        MediaFile("rphjb_ChitarraZuccherada.mp4"),
        MediaFile("rphjb_Zucchero.mp3"),
        MediaFile("rphjb_ZuccheroVanigliaCremaCioccolataPandoro.mp3"),
        MediaFile("rphjb_AuguriPerPasqua.mp4")
      ),
      replySelection = RandomSelection
    ),
    ReplyBundleMessage(
      TextTrigger(
        RegexTextTriggerValue("\\bgood\\b".r),
        RegexTextTriggerValue("\\bshow\\b".r),
        RegexTextTriggerValue("\\bfriends\\b".r)
      ),
      List(
        MediaFile("rphjb_OkGoodShowFriends.gif"),
        MediaFile("rphjb_LetSGoodStateBene.mp3"),
        MediaFile("rphjb_WelaMyFriends.mp4"),
        MediaFile("rphjb_LetsGoodMyFriends.mp4")
      ),
      replySelection = RandomSelection
    ),
    ReplyBundleMessage(
      TextTrigger(
        RegexTextTriggerValue("vattene (af)?fanculo".r)
      ),
      List(
        MediaFile("rphjb_MaVatteneAffanculo.gif"),
        MediaFile("rphjb_MaVatteneAffanculo.mp3")
      ),
      replySelection = RandomSelection
    ),
    ReplyBundleMessage(
      TextTrigger(
        StringTextTriggerValue("feelings")
      ),
      List(
        MediaFile("rphjb_Feelings.gif"),
        MediaFile("rphjb_Feelings2.gif"),
        MediaFile("rphjb_Feelings.mp3"),
        MediaFile("rphjb_Feelings.mp4")
      ),
      replySelection = RandomSelection
    ),
    ReplyBundleMessage(
      TextTrigger(
        StringTextTriggerValue("me ne vado")
      ),
      List(
        MediaFile("rphjb_MeNeVado.mp3"),
        MediaFile("rphjb_MiRompiErCazzo.gif"),
        MediaFile("rphjb_MeNeVado.gif")
      ),
      replySelection = RandomSelection
    ),
    ReplyBundleMessage(
      TextTrigger(
        StringTextTriggerValue("mignotta"),
        StringTextTriggerValue("puttana"),
        StringTextTriggerValue("troia")
      ),
      List(
        MediaFile("rphjb_Mignotta.mp3"),
        MediaFile("rphjb_Mignotta.gif"),
        MediaFile("rphjb_VialeZara.mp3"),
        MediaFile("rphjb_StronzoFiglioMignotta.mp4")
      ),
      replySelection = RandomSelection
    ),
    ReplyBundleMessage(
      TextTrigger(
        StringTextTriggerValue("ti devi spaventare")
      ),
      List(
        MediaFile("rphjb_TiDeviSpaventare.mp3"),
        MediaFile("rphjb_TiDeviSpaventare.gif"),
        MediaFile("rphjb_TiDeviSpaventare.mp4")
      ),
      replySelection = RandomSelection
    ),
    ReplyBundleMessage(
      TextTrigger(
        StringTextTriggerValue("ma che cazzo sto dicendo"),
        StringTextTriggerValue("il martell")
      ),
      List(
        MediaFile("rphjb_MaCheCazzoStoDicendo.mp3"),
        MediaFile("rphjb_MaCheCazzoStoDicendo.mp4"),
        MediaFile("rphjb_MaCheCazzoStoDicendo.gif")
      ),
      replySelection = RandomSelection
    ),
    ReplyBundleMessage(
      TextTrigger(
        StringTextTriggerValue("questa volta no")
      ),
      List(
        MediaFile("rphjb_QuestaVoltaNo.mp3"),
        MediaFile("rphjb_QuestaVoltaNo.gif")
      ),
      replySelection = RandomSelection
    ),
    ReplyBundleMessage(
      TextTrigger(
        StringTextTriggerValue("una vergogna")
      ),
      List(
        MediaFile("rphjb_Vergogna.mp3"),
        MediaFile("rphjb_Vergogna.mp4"),
        MediaFile("rphjb_Vergogna.gif"),
        MediaFile("rphjb_Vergogna2.gif")
      ),
      replySelection = RandomSelection
    ),
    ReplyBundleMessage(
      TextTrigger(
        StringTextTriggerValue("mi devo trasformare"),
        StringTextTriggerValue("cristo canaro")
      ),
      List(
        MediaFile("rphjb_Trasformista.mp3"),
        MediaFile("rphjb_Trasformista.gif"),
        MediaFile("rphjb_CristoCanaro.mp4")
      ),
      replySelection = RandomSelection
    ),
    ReplyBundleMessage(
      TextTrigger(
        RegexTextTriggerValue("ma[ ]?s[cg]us[a]?".r)
      ),
      List(
        MediaFile("rphjb_MaSgus.mp3"),
        MediaFile("rphjb_MaSgus.gif")
      ),
      replySelection = RandomSelection
    ),
    ReplyBundleMessage(
      TextTrigger(
        StringTextTriggerValue("grazie gianni"),
      ),
      List(
        MediaFile("rphjb_Grazie.mp3"),
        MediaFile("rphjb_Grazie.gif"),
        MediaFile("rphjb_Grazie.mp4")
      ),
      replySelection = RandomSelection
    ),
    ReplyBundleMessage(
      TextTrigger(
        RegexTextTriggerValue("cia[o]{3,}".r)
      ),
      List(
        MediaFile("rphjb_Grazie.mp3"),
        MediaFile("rphjb_Grazie.gif"),
        MediaFile("rphjb_Grazie.mp4"),
        MediaFile("rphjb_ViSaluto.mp3")
      ),
      replySelection = RandomSelection
    ),
    ReplyBundleMessage(
      TextTrigger(
        StringTextTriggerValue("stare attenti"),
        StringTextTriggerValue("per strada")
      ),
      List(
        MediaFile("rphjb_IncontratePerStrada.mp3"),
        MediaFile("rphjb_IncontratePerStrada.gif")
      ),
      replySelection = RandomSelection
    ),
    ReplyBundleMessage(
      TextTrigger(
        StringTextTriggerValue("lavora tu vecchiaccia"),
        StringTextTriggerValue("hai la pelle dura"),
        StringTextTriggerValue("io sono creatura")
      ),
      List(
        MediaFile("rphjb_LavoraTu.mp3"),
        MediaFile("rphjb_LavoraTu.mp4"),
        MediaFile("rphjb_LavoraTu2.mp4"),
        MediaFile("rphjb_LavoraTu.gif"),
        MediaFile("rphjb_StorieSonoTanteVecchiaccia.mp4")
      ),
      replySelection = RandomSelection
    ),
    ReplyBundleMessage(
      TextTrigger(
        RegexTextTriggerValue("infern[a]+l[ie]+[!]*".r)
      ),
      List(
        MediaFile("rphjb_Infernali.mp3"),
        MediaFile("rphjb_Infernali.gif"),
        MediaFile("rphjb_Infernale.mp3")
      ),
      replySelection = RandomSelection
    ),
    ReplyBundleMessage(
      TextTrigger(
        StringTextTriggerValue("per il culo")
      ),
      List(
        MediaFile("rphjb_PigliandoPerIlCulo.mp3"),
        MediaFile("rphjb_PigliandoPerIlCulo.gif")
      ),
      replySelection = RandomSelection
    ),
    ReplyBundleMessage(
      TextTrigger(
        StringTextTriggerValue(e":lol:"),
        StringTextTriggerValue(e":rofl:"),
        StringTextTriggerValue("sorriso"),
        RegexTextTriggerValue("(ah|ha){3,}".r)
      ),
      List(
        MediaFile("rphjb_Risata.mp3"),
        MediaFile("rphjb_Risata.mp4"),
        MediaFile("rphjb_Risata.gif"),
        MediaFile("rphjb_OrmaiRisata.mp4"),
        MediaFile("rphjb_Sorriso2.gif"),
        MediaFile("rphjb_Sorriso.gif"),
        MediaFile("rphjb_SorrisoSognante.gif"),
        MediaFile("rphjb_Risata2.mp3"),
        MediaFile("rphjb_SepolturaRisata.mp4")
      ),
      replySelection = RandomSelection
    ),
    ReplyBundleMessage(
      TextTrigger(
        StringTextTriggerValue("ammazza che sei"),
        StringTextTriggerValue("quasi un frocio")
      ),
      List(
        MediaFile("rphjb_Frocio.mp3"),
        MediaFile("rphjb_Frocio.gif")
      ),
      replySelection = RandomSelection
    ),
    ReplyBundleMessage(
      TextTrigger(
        RegexTextTriggerValue("(fammi|chiedere)? (una|questa)? cortesia".r)
      ),
      List(
        MediaFile("rphjb_FammiQuestaCortesia.mp3"),
        MediaFile("rphjb_FammiQuestaCortesia.gif")
      ),
      replySelection = RandomSelection
    ),
    ReplyBundleMessage(
      TextTrigger(
        StringTextTriggerValue("non mi sta bene")
      ),
      List(
        MediaFile("rphjb_NonMiStaBene.mp3"),
        MediaFile("rphjb_NonMiStaBene2.mp3"),
        MediaFile("rphjb_NonMiStaBene.gif"),
        MediaFile("rphjb_NonMiStaBene2.gif"),
        MediaFile("rphjb_NonMiStaBene.mp4")
      ),
      replySelection = RandomSelection
    ),
    ReplyBundleMessage(
      TextTrigger(
        StringTextTriggerValue("le labbra"),
      ),
      List(
        MediaFile("rphjb_Labbra.mp3"),
        MediaFile("rphjb_Labbra.gif")
      ),
      replySelection = RandomSelection
    ),
    ReplyBundleMessage(
      TextTrigger(
        StringTextTriggerValue("la vita è il nemico")
      ),
      List(
        MediaFile("rphjb_VitaNemico.mp3"),
        MediaFile("rphjb_VitaNemico.gif"),
        MediaFile("rphjb_VitaNemico.mp4"),
        MediaFile("rphjb_VitaNemicoCervello.mp4")
      ),
      replySelection = RandomSelection
    ),
    ReplyBundleMessage(
      TextTrigger(
        StringTextTriggerValue("permettere")
      ),
      List(
        MediaFile("rphjb_Permettere.mp3"),
        MediaFile("rphjb_Permettere.gif")
      ),
      replySelection = RandomSelection
    ),
    ReplyBundleMessage(
      TextTrigger(
        StringTextTriggerValue("le note")
      ),
      List(
        MediaFile("rphjb_Note.mp3"),
        MediaFile("rphjb_Note.gif")
      ),
      replySelection = RandomSelection
    ),
    ReplyBundleMessage(
      TextTrigger(
        RegexTextTriggerValue("te[r]+[i]+[b]+[i]+l[e]+".r)
      ),
      List(
        MediaFile("rphjb_Terribile.mp3"),
        MediaFile("rphjb_Terribile.mp4"),
        MediaFile("rphjb_Terribile.gif")
      ),
      replySelection = RandomSelection
    ),
    ReplyBundleMessage(
      TextTrigger(
        StringTextTriggerValue("viva napoli")
      ),
      List(
        MediaFile("rphjb_VivaNapoli.mp3"),
        MediaFile("rphjb_VivaNapoli.gif")
      ),
      replySelection = RandomSelection
    ),
    ReplyBundleMessage(
      TextTrigger(
        StringTextTriggerValue("ciao a tutti"),
        StringTextTriggerValue("come state"),
        StringTextTriggerValue("belle gioie")
      ),
      List(
        MediaFile("rphjb_CiaoComeState.gif"),
        MediaFile("rphjb_CiaoComeState.mp3")
      ),
      replySelection = RandomSelection
    ),
    ReplyBundleMessage(
      TextTrigger(
        RegexTextTriggerValue("bast[a]{3,}[!]*".r)
      ),
      List(
        MediaFile("rphjb_Basta.mp3"),
        MediaFile("rphjb_Basta.mp4"),
        MediaFile("rphjb_Basta2.mp4"),
        MediaFile("rphjb_Basta2.mp3"),
        MediaFile("rphjb_Basta.gif"),
        MediaFile("rphjb_Basta2.gif"),
        MediaFile("rphjb_Basta3.gif"),
        MediaFile("rphjb_BastaSedia.gif")
      ),
      replySelection = RandomSelection
    ),
    ReplyBundleMessage(
      TextTrigger(
        StringTextTriggerValue("assolo"),
        RegexTextTriggerValue("[gc]hi[td]arra".r),
        RegexTextTriggerValue("(as)?solo di basso".r)
      ),
      List(
        MediaFile("rphjb_Assolo.mp3"),
        MediaFile("rphjb_Assolo.mp4"),
        MediaFile("rphjb_Assolo2.mp4"),
        MediaFile("rphjb_AssoloBeat.mp4"),
        MediaFile("rphjb_AssoloSubdolo.mp4"),
        MediaFile("rphjb_Chitarra1.gif"),
        MediaFile("rphjb_Chitarra2.gif"),
        MediaFile("rphjb_ChitarraPlettroVicoletto.mp4"),
        MediaFile("rphjb_ChitarraZuggherada.mp3"),
        MediaFile("rphjb_AssoloBasso.mp4")
      ),
      replySelection = RandomSelection
    ),
    ReplyBundleMessage(
      TextTrigger(
        RegexTextTriggerValue("[ ]?ca[bp]i[dt]o | ca[bp]i[dt]o[ ]?".r),
        RegexTextTriggerValue("[ ]?capissi | capissi[ ]?".r),
        StringTextTriggerValue("gabido"),
      ),
      List(
        MediaFile("rphjb_HoCapito.mp3"),
        MediaFile("rphjb_AveteCapito.mp3"),
        MediaFile("rphjb_AveteCapito.mp4"),
        MediaFile("rphjb_Capito.mp3"),
        MediaFile("rphjb_NonHannoCapitoUnCazzo.mp3"),
        MediaFile("rphjb_NonAveteCapitoUnCazzo.mp4"),
        MediaFile("rphjb_AveteCapitoComeSempre.gif"),
        MediaFile("rphjb_NonAveteCapitoUnCazzo.gif"),
        MediaFile("rphjb_VoiNonAveteCapitoUnCazzo.gif"),
        MediaFile("rphjb_IlSensoCapito.gif"),
        MediaFile("rphjb_CapitoDoveStiamo.gif"),
        MediaFile("rphjb_NonHoCapito.gif"),
        MediaFile("rphjb_AveteCapitoEh.gif"),
        MediaFile("rphjb_ComeAlSolitoNonAveteCapito.gif"),
        MediaFile("rphjb_CapitoDoveStiamo.mp3"),
        MediaFile("rphjb_CapisciRidotti.mp3")
      ),
      replySelection = RandomSelection
    ),
    ReplyBundleMessage(
      TextTrigger(
        StringTextTriggerValue("esperiment"),
        RegexTextTriggerValue("1(,)? 2(,)? 3".r),
        RegexTextTriggerValue("uno(,)? due(,)? tre".r)
      ),
      List(
        MediaFile("rphjb_Esperimento.mp3"),
        MediaFile("rphjb_Esperimento.mp4"),
        MediaFile("rphjb_Esperimento2.mp4"),
        MediaFile("rphjb_Esperimento.gif"),
        MediaFile("rphjb_Esperimento2.gif"),
        MediaFile("rphjb_Esperimento3.gif"),
        MediaFile("rphjb_DiciottoAnni.mp4"),
      ),
      replySelection = RandomSelection
    ),
    ReplyBundleMessage(
      TextTrigger(
        StringTextTriggerValue("schifosi")
      ),
      List(
        MediaFile("rphjb_ViCalpesto.gif"),
        MediaFile("rphjb_ViCalpesto.mp4"),
        MediaFile("rphjb_Schifosi.mp3"),
        MediaFile("rphjb_Schifosi.mp4"),
        MediaFile("rphjb_Schifosi2.mp3"),
        MediaFile("rphjb_Schifosi3.mp3"),
        MediaFile("rphjb_Schifosi3.gif"),
        MediaFile("rphjb_SchifosoUltimi.mp4"),
        MediaFile("rphjb_StateZittiZozziUltimi.mp3"),
        MediaFile("rphjb_Schifosi.gif"),
        MediaFile("rphjb_Schifosi2.gif"),
        MediaFile("rphjb_ConQuestaTecnica.mp4")
      ),
      replySelection = RandomSelection
    ),
    ReplyBundleMessage(
      TextTrigger(
        StringTextTriggerValue("mortacci vostri")
      ),
      List(
        MediaFile("rphjb_MortacciVostri.gif"),
        MediaFile("rphjb_StateZittiZozziUltimi.mp3"),
        MediaFile("rphjb_ConQuestaTecnica.mp4"),
        MediaFile("rphjb_MortacciVostri.mp4"),
        MediaFile("rphjb_CheCazzoEraQuellaRoba2.mp4")
      ),
      replySelection = RandomSelection
    ),
    ReplyBundleMessage(
      TextTrigger(
        StringTextTriggerValue("non vedo")
      ),
      List(
        MediaFile("rphjb_Stanco.mp3"),
        MediaFile("rphjb_PannaOcchialiSpalla.mp3"),
      ),
      replySelection = RandomSelection
    ),
    ReplyBundleMessage(
      TextTrigger(
        StringTextTriggerValue("panna")
      ),
      List(
        MediaFile("rphjb_Problema.mp3"),
        MediaFile("rphjb_PannaOcchialiSpalla.mp3")
      ),
      replySelection = RandomSelection
    ),
    ReplyBundleMessage(
      TextTrigger(
        StringTextTriggerValue("applau")
      ),
      List(
        MediaFile("rphjb_Applauso.gif"),
        MediaFile("rphjb_Applauso.mp3"),
        MediaFile("rphjb_Applauso2.mp3"),
        MediaFile("rphjb_ApplausoPiuForte.mp4")
      ),
      replySelection = RandomSelection
    ),
    ReplyBundleMessage(
      TextTrigger(
        StringTextTriggerValue("venite qua")
      ),
      List(
        MediaFile("rphjb_VeniteQua.gif"),
        MediaFile("rphjb_VeniteQua.mp3"),
        MediaFile("rphjb_VeniteQua.mp4")
      ),
      replySelection = RandomSelection
    ),
    ReplyBundleMessage(
      TextTrigger(
        RegexTextTriggerValue("\\bpaga(re)?\\b".r),
        StringTextTriggerValue("soldi"),
        StringTextTriggerValue("bollette"),
        StringTextTriggerValue("tasse"),
        StringTextTriggerValue("bolletta"),
        StringTextTriggerValue("tassa")
      ),
      List(
        MediaFile("rphjb_ChiCacciaISoldi.gif"),
        MediaFile("rphjb_ChiCacciaISoldi.mp3"),
        MediaFile("rphjb_BigMoney.mp4")
      ),
      replySelection = RandomSelection
    ),
    ReplyBundleMessage(
      TextTrigger(
        RegexTextTriggerValue("[od]?dio mio[,]? no".r)
      ),
      List(
        MediaFile("rphjb_OddioMioNo.gif"),
        MediaFile("rphjb_OddioMioNo.mp3"),
        MediaFile("rphjb_OddioMioNo.mp4")
      ),
      replySelection = RandomSelection
    ),
    ReplyBundleMessage(
      TextTrigger(
        RegexTextTriggerValue("[sono ]?a[r]{1,2}iva[dt]o".r),
        StringTextTriggerValue("piacere")
      ),
      List(
        MediaFile("rphjb_Arivato.gif"),
        MediaFile("rphjb_Arivato.mp3"),
        MediaFile("rphjb_Arivato.mp4")
      ),
      replySelection = RandomSelection
    ),
    ReplyBundleMessage(
      TextTrigger(
        RegexTextTriggerValue("delu[sd]".r)
      ),
      List(
        MediaFile("rphjb_Deluso.gif"),
        MediaFile("rphjb_Deluso.mp3"),
        MediaFile("rphjb_DeludendoQuasiTutto.mp4")
      ),
      replySelection = RandomSelection
    ),
    ReplyBundleMessage(
      TextTrigger(
        StringTextTriggerValue("fate come vi pare"),
        RegexTextTriggerValue("sti [gc]azzi".r)
      ),
      List(
        MediaFile("rphjb_ComeViPare.gif"),
        MediaFile("rphjb_ComeViPare.mp3")
      ),
      replySelection = RandomSelection
    ),
    ReplyBundleMessage(
      TextTrigger(
        StringTextTriggerValue("divento una bestia"),
        StringTextTriggerValue("incazzo")
      ),
      List(
        MediaFile("rphjb_DiventoBestia.mp3"),
        MediaFile("rphjb_Incazzo.mp3"),
        MediaFile("rphjb_Incazzo2.mp3")
      ),
      replySelection = RandomSelection
    ),
    ReplyBundleMessage(
      TextTrigger(
        StringTextTriggerValue("dove stiamo"),
        StringTextTriggerValue("stiamo nella follia")
      ),
      List(
        MediaFile("rphjb_CapitoDoveStiamo.mp3"),
        MediaFile("rphjb_StiamoNellaFollia.mp4"),
        MediaFile("rphjb_CapitoDoveStiamo.gif")
      ),
      replySelection = RandomSelection
    ),
    ReplyBundleMessage(
      TextTrigger(
        StringTextTriggerValue("non sai molto")
      ),
      List(
        MediaFile("rphjb_NonSaiMolto.gif"),
        MediaFile("rphjb_RadioRockErrori.mp4")
      ),
      replySelection = RandomSelection
    ),
    ReplyBundleMessage(
      TextTrigger(
        StringTextTriggerValue("errori")
      ),
      List(
        MediaFile("rphjb_MaiErrori.gif"),
        MediaFile("rphjb_MaiErrori.mp4")
      ),
      replySelection = RandomSelection
    ),
    ReplyBundleMessage(
      TextTrigger(
        RegexTextTriggerValue("\\bpasqua\\b".r)
      ),
      List(
        MediaFile("rphjb_AuguriPasqua.mp3"),
        MediaFile("rphjb_AuguriPerPasqua.mp4")
      ),
      replySelection = RandomSelection
    ),
    ReplyBundleMessage(
      TextTrigger(
        StringTextTriggerValue("vaniglia"),
        StringTextTriggerValue("pandoro"),
        RegexTextTriggerValue("crema alla [gc]io[gc]+ola[dt]a".r),
      ),
      List(
        MediaFile("rphjb_ZuccheroVanigliaCremaCioccolataPandoro.mp3"),
        MediaFile("rphjb_AuguriPerPasqua.mp4")
      ),
      replySelection = RandomSelection
    ),
    ReplyBundleMessage(
      TextTrigger(
        StringTextTriggerValue("timore"),
        StringTextTriggerValue("paura"),
        RegexTextTriggerValue("diri[g]+en[dt]i".r),
      ),
      List(
        MediaFile("rphjb_Dirigenti.gif"),
        MediaFile("rphjb_AncoraNoDirigenti.mp4")
      ),
      replySelection = RandomSelection
    ),
    ReplyBundleMessage(
      TextTrigger(
        StringTextTriggerValue("guerra")
      ),
      List(
        MediaFile("rphjb_GuerraTotale.gif"),
        MediaFile("rphjb_GuerraTotale.mp4")
      ),
      replySelection = RandomSelection
    ),
    ReplyBundleMessage(
      TextTrigger(
        StringTextTriggerValue("non voglio nessuno"),
        StringTextTriggerValue("mentre lavoro")
      ),
      List(
        MediaFile("rphjb_NonVoglioNessuno.gif"),
        MediaFile("rphjb_NonVoglioNessuno.mp4")
      ),
      replySelection = RandomSelection
    ),
    ReplyBundleMessage(
      TextTrigger(
        StringTextTriggerValue("peggio del peggio")
      ),
      List(
        MediaFile("rphjb_PeggioDelPeggio.gif"),
        MediaFile("rphjb_PeggioDelPeggio.mp4")
      ),
      replySelection = RandomSelection
    ),
    ReplyBundleMessage(
      TextTrigger(
        StringTextTriggerValue("bebop"),
        StringTextTriggerValue("be bop"),
        StringTextTriggerValue("aluba"),
        StringTextTriggerValue("my baby")
      ),
      List(
        MediaFile("rphjb_Bebop.gif"),
        MediaFile("rphjb_Bebop.mp4")
      ),
      replySelection = RandomSelection
    )
  )

  def messageRepliesData[F[_]: Applicative]: List[ReplyBundleMessage[F]] =
    (messageRepliesAudioData[F] ++ messageRepliesGifData[F] ++ messageRepliesVideoData[F] ++ messageRepliesMixData[F])
      .sorted(ReplyBundle.ordering[F])
      .reverse

  def messageReplyDataStringChunks[F[_]: Applicative]: List[String] = {
    val (triggers, lastTriggers) = messageRepliesData[F]
      .map(_.trigger match {
        case TextTrigger(lt @ _*) => lt.mkString("[", " - ", "]")
        case _                    => ""
      })
      .foldLeft((List.empty[String], "")) { case ((acc, candidate), triggerString) =>
        if ((candidate ++ triggerString).length > 4090)
          (acc :+ candidate, triggerString)
        else (acc, candidate ++ triggerString)
      }
    triggers :+ lastTriggers
  }

  def commandRepliesData[F[_]: Applicative]: List[ReplyBundleCommand[F]] = List(
    ReplyBundleCommand(
      trigger = CommandTrigger("triggerlist"),
      text = Some(
        TextReply[F](
          m => {
            if (m.chat.`type` == "private") Applicative[F].pure(messageReplyDataStringChunks[F])
            else
              Applicative[F].pure(List("NON TE LO PUOI PERMETTERE!!!(puoi usare questo comando sono in chat privata)"))
          },
          false
        )
      )
    ),
    ReplyBundleCommand(
      trigger = CommandTrigger("bensonify"),
      text = Some(
        TextReply[F](
          msg =>
            handleCommandWithInput[F](
              msg,
              "bensonify",
              "RichardPHJBensonBot",
              t => List(Bensonify.compute(t)).pure[F],
              "E PARLAAAAAAA!!!!"
            ),
          true
        )
      )
    ),
    ReplyBundleCommand(
      trigger = CommandTrigger("instructions"),
      text = Some(
        TextReply[F](
          _ => List(s"""
---- Instruzioni Per il Bot di Benson ----

Il bot reagisce automaticamente ai messaggi in base ai trigger che si
possono trovare dal comando:

/triggerlist

ATTENZIONE: tale comando invierà una lunga lista. Consultarlo
privatamente nella chat del bot.

Questo bot consente di convertire le frasi come le direbbe Richard
attraverso il comando:

/bensonify «Frase»

la frase è necessaria, altrimenti il bot vi risponderà in malomodo.

Infine, se si vuole disabilitare il bot per un particolare messaggio,
ad esempio per un messaggio lungo che potrebbe causare vari trigger
in una volta, è possibile farlo iniziando il messaggio con il
carattere '!':

! «Messaggio»
""").pure[F],
          false
        )
      )
    )
  )
  def token[F[_]: Async]: Resource[F, String] =
    ResourceAccess.fileSystem.getResourceByteArray[F]("rphjb_RichardPHJBensonBot.token").map(_.map(_.toChar).mkString)

  def buildPollingBot[F[_]: Parallel: Async, A](
      action: RichardPHJBensonBotPolling[F] => F[A]
  )(implicit log: LogWriter[F]): F[A] = (for {
    httpClient            <- BlazeClientBuilder[F].resource
    tk                    <- token[F]
    _                     <- Resource.eval(log.info("[RichardPHJBensonBot] Delete webook..."))
    deleteWebhookResponse <- deleteWebhooks[F](httpClient, tk)
    _ <- Resource.eval(
      Async[F].raiseWhen(deleteWebhookResponse.status != Status.Ok)(
        new RuntimeException(
          "[RichardPHJBensonBot] The delete webhook request failed: " + deleteWebhookResponse.as[String]
        )
      )
    )
    _ <- Resource.eval(log.info("[RichardPHJBensonBot] Webhook deleted"))
  } yield (httpClient, tk)).use(httpClient_tk => {
    implicit val api: Api[F] = BotApi(httpClient_tk._1, baseUrl = s"https://api.telegram.org/bot${httpClient_tk._2}")
    action(new RichardPHJBensonBotPolling[F])
  })

  def buildWebhookBot[F[_]: Async](
      httpClient: Client[F],
      webhookBaseUrl: String = org.http4s.server.defaults.IPv4Host,
  )(implicit log: LogWriter[F]): Resource[F, RichardPHJBensonBotWebhook[F]] = for {
    tk <- token[F]
    baseUrl = s"https://api.telegram.org/bot$tk"
    path    = s"/$tk"
    _                     <- Resource.eval(log.info("[RichardPHJBensonBot] Delete webook..."))
    deleteWebhookResponse <- deleteWebhooks[F](httpClient, tk)
    _ <- Resource.eval(
      Async[F].raiseWhen(deleteWebhookResponse.status != Status.Ok)(
        new RuntimeException(
          "[RichardPHJBensonBot] The delete webhook request failed: " + deleteWebhookResponse.as[String]
        )
      )
    )
    _ <- Resource.eval(log.info("[RichardPHJBensonBot] Webhook deleted"))
  } yield {
    implicit val api: Api[F] = BotApi(httpClient, baseUrl = baseUrl)
    new RichardPHJBensonBotWebhook[F](
      url = webhookBaseUrl + path,
      path = s"/$tk"
    )
  }
}
