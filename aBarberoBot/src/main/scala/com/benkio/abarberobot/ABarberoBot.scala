package com.benkio.abarberobot

import cats._
import cats.effect._
import cats.implicits._
import com.benkio.telegrambotinfrastructure.botCapabilities._
import com.benkio.telegrambotinfrastructure.model._
import com.benkio.telegrambotinfrastructure.Configurations
import com.benkio.telegrambotinfrastructure._
import org.http4s.blaze.client._
import telegramium.bots.high._

import scala.concurrent.ExecutionContext

class ABarberoBot[F[_]: Parallel: Async: Api] extends BotSkeleton[F] {

  override val resourceSource: ResourceSource = ABarberoBot.resourceSource

  override lazy val messageRepliesDataF: F[List[ReplyBundleMessage]] =
    ABarberoBot.messageRepliesData.pure[F]

  override lazy val commandRepliesDataF: F[List[ReplyBundleCommand]] = ABarberoBot.commandRepliesData.pure[F]
}

object ABarberoBot extends Configurations {

  val resourceSource: ResourceSource = FileSystem

  val messageRepliesAudioData: List[ReplyBundleMessage] = List(
    ReplyBundleMessage(
      TextTrigger(
        List(
          StringTextTriggerValue("ferro"),
          StringTextTriggerValue("fuoco"),
          StringTextTriggerValue("acqua bollente"),
          StringTextTriggerValue("aceto")
        )
      ),
      List(MediaFile("abar_FerroFuocoAcquaBollenteAceto.mp3"))
    ),
    ReplyBundleMessage(
      TextTrigger(
        List(
          StringTextTriggerValue("secolo")
        )
      ),
      List(MediaFile("abar_Secolo.mp3"))
    ),
    ReplyBundleMessage(
      TextTrigger(
        List(
          StringTextTriggerValue("streghe"),
          StringTextTriggerValue("maghi"),
          StringTextTriggerValue("draghi"),
          StringTextTriggerValue("roghi"),
        )
      ),
      List(MediaFile("abar_Draghi.mp3"))
    ),
    ReplyBundleMessage(
      TextTrigger(
        List(
          StringTextTriggerValue("crociate")
        )
      ),
      List(MediaFile("abar_Crociate.mp3"))
    ),
    ReplyBundleMessage(
      TextTrigger(List(StringTextTriggerValue("wikipedia"))),
      List(MediaFile("abar_Wikipedia.mp3"))
    ),
    ReplyBundleMessage(
      TextTrigger(List(RegexTextTriggerValue("\\beccoh\\b".r))),
      List(MediaFile("abar_Ecco.mp3"))
    ),
    ReplyBundleMessage(
      TextTrigger(
        List(StringTextTriggerValue("maglio"), StringTextTriggerValue("sbriciola"), StringTextTriggerValue("schiaccia"))
      ),
      List(MediaFile("abar_Maglio.mp3"))
    ),
    ReplyBundleMessage(
      TextTrigger(
        List(
          StringTextTriggerValue("chiese"),
          StringTextTriggerValue("castelli"),
          StringTextTriggerValue("villaggi"),
          StringTextTriggerValue("assedi")
        )
      ),
      List(MediaFile("abar_Assedi.mp3"))
    ),
    ReplyBundleMessage(
      TextTrigger(List(StringTextTriggerValue("furore"), StringTextTriggerValue("città"))),
      List(MediaFile("abar_Furore.mp3"))
    ),
    ReplyBundleMessage(
      TextTrigger(List(StringTextTriggerValue("demoni"), StringTextTriggerValue("scatenat"))),
      List(MediaFile("abar_Demoni.mp3"))
    ),
    ReplyBundleMessage(TextTrigger(List(StringTextTriggerValue("sensei"))), List(MediaFile("abar_Sensei.mp3"))),
    ReplyBundleMessage(TextTrigger(List(StringTextTriggerValue("miserabile"))), List(MediaFile("abar_Miserabile.mp3"))),
    ReplyBundleMessage(
      TextTrigger(List(StringTextTriggerValue("omicidio"), StringTextTriggerValue("cosa che capita"))),
      List(MediaFile("abar_CapitaOmicidio.mp3"))
    ),
    ReplyBundleMessage(
      TextTrigger(
        List(
          StringTextTriggerValue("cavallo"),
          RegexTextTriggerValue("tiriamo(lo)? giù".r),
          StringTextTriggerValue("ammazziamolo")
        )
      ),
      List(MediaFile("abar_Ammazziamolo.mp3"))
    ),
    ReplyBundleMessage(
      TextTrigger(
        List(
          StringTextTriggerValue("bruciare"),
          StringTextTriggerValue("saccheggiare"),
          StringTextTriggerValue("fuoco")
        )
      ),
      List(MediaFile("abar_Bbq.mp3"))
    ),
    ReplyBundleMessage(
      TextTrigger(
        List(
          StringTextTriggerValue("guerra"),
          StringTextTriggerValue("chi vuole"),
          StringTextTriggerValue("la vogliamo")
        )
      ),
      List(MediaFile("abar_Guerra.mp3"))
    ),
    ReplyBundleMessage(
      TextTrigger(
        List(StringTextTriggerValue("cagarelli"), StringTextTriggerValue("feci"), StringTextTriggerValue("cacca"))
      ),
      List(MediaFile("abar_Homines.mp3"))
    ),
    ReplyBundleMessage(
      TextTrigger(
        List(
          StringTextTriggerValue("monsignore"),
          StringTextTriggerValue("vescovo"),
          StringTextTriggerValue("in culo")
        )
      ),
      List(MediaFile("abar_Monsu.mp3"))
    ),
    ReplyBundleMessage(TextTrigger(List(StringTextTriggerValue("ottimismo"))), List(MediaFile("abar_Ottimismo.mp3"))),
    ReplyBundleMessage(
      TextTrigger(List(StringTextTriggerValue("latino"))),
      List(
        MediaFile("abar_Homines.mp3"),
        MediaFile("abar_Vagdavercustis.mp3"),
        MediaFile("abar_Yersinia.mp3"),
        MediaFile("abar_Culagium.mp3")
      ),
      replySelection = RandomSelection
    ),
    ReplyBundleMessage(TextTrigger(List(StringTextTriggerValue("radetzky"))), List(MediaFile("abar_Radetzky.mp3"))),
    ReplyBundleMessage(
      TextTrigger(List(StringTextTriggerValue("interrogateli"), StringTextTriggerValue("tortura"))),
      List(MediaFile("abar_Reinterrogateli.mp3"))
    ),
    ReplyBundleMessage(
      TextTrigger(List(StringTextTriggerValue(" re "), StringTextTriggerValue("decapita"))),
      List(MediaFile("abar_Re.mp3"))
    ),
    ReplyBundleMessage(
      TextTrigger(
        List(RegexTextTriggerValue("\\bascia\\b".r), StringTextTriggerValue("sangue"))
      ),
      List(MediaFile("abar_Sangue.mp3"))
    ),
    ReplyBundleMessage(TextTrigger(List(StringTextTriggerValue("spranga"))), List(MediaFile("abar_Spranga.mp3"))),
    ReplyBundleMessage(TextTrigger(List(StringTextTriggerValue("stupidi"))), List(MediaFile("abar_Stupidi.mp3"))),
    ReplyBundleMessage(TextTrigger(List(StringTextTriggerValue("subito"))), List(MediaFile("abar_Subito.mp3"))),
    ReplyBundleMessage(
      TextTrigger(List(StringTextTriggerValue("vagdavercustis"))),
      List(MediaFile("abar_Vagdavercustis.mp3"))
    ),
    ReplyBundleMessage(
      TextTrigger(List(StringTextTriggerValue("peste"), StringTextTriggerValue("yersinia"))),
      List(MediaFile("abar_Yersinia.mp3"))
    ),
    ReplyBundleMessage(TextTrigger(List(StringTextTriggerValue("zazzera"))), List(MediaFile("abar_Zazzera.mp3")))
  )

  val messageRepliesGifData: List[ReplyBundleMessage] = List(
    ReplyBundleMessage(
      TextTrigger(
        List(
          StringTextTriggerValue("ha ragione")
        )
      ),
      List(MediaFile("abar_HaRagione.gif"))
    ),
    ReplyBundleMessage(
      TextTrigger(
        List(StringTextTriggerValue("seziona"), StringTextTriggerValue("cadaveri"), StringTextTriggerValue("morti"))
      ),
      List(MediaFile("abar_Cadaveri.gif"))
    ),
    ReplyBundleMessage(
      TextTrigger(
        List(
          StringTextTriggerValue("strappa"),
          StringTextTriggerValue("gli arti"),
          StringTextTriggerValue("le braccia")
        )
      ),
      List(MediaFile("abar_Strappare.gif"))
    ),
    ReplyBundleMessage(
      TextTrigger(List(StringTextTriggerValue("saltare la testa"), StringTextTriggerValue("questa macchina"))),
      List(MediaFile("abar_SaltareLaTesta.gif"))
    ),
    ReplyBundleMessage(TextTrigger(List(StringTextTriggerValue("paura"))), List(MediaFile("abar_Paura.gif"))),
    ReplyBundleMessage(
      TextTrigger(List(StringTextTriggerValue("sega"), StringTextTriggerValue("dov'è"))),
      List(MediaFile("abar_Sega.gif"))
    ),
    ReplyBundleMessage(
      TextTrigger(
        List(
          StringTextTriggerValue("potere"),
          StringTextTriggerValue("incarichi"),
          StringTextTriggerValue("poltrone"),
          StringTextTriggerValue("appalti"),
          StringTextTriggerValue("spartir")
        )
      ),
      List(MediaFile("abar_Potere.gif"))
    ),
    ReplyBundleMessage(
      TextTrigger(
        List(
          StringTextTriggerValue("grandioso"),
          StringTextTriggerValue("magnifico"),
          StringTextTriggerValue("capolavoro")
        )
      ),
      List(MediaFile("abar_Capolavoro.gif"))
    ),
    ReplyBundleMessage(
      TextTrigger(
        List(
          StringTextTriggerValue("troppo facile"),
          StringTextTriggerValue("easy")
        )
      ),
      List(MediaFile("abar_TroppoFacile.gif"))
    ),
    ReplyBundleMessage(
      TextTrigger(
        List(
          StringTextTriggerValue("trappola"),
          StringTextTriggerValue("tranello"),
          StringTextTriggerValue("inganno")
        )
      ),
      List(MediaFile("abar_Trappola.gif"))
    ),
    ReplyBundleMessage(
      TextTrigger(
        List(
          StringTextTriggerValue("faida"),
          StringTextTriggerValue("vendetta"),
          StringTextTriggerValue("rappresaglia"),
          StringTextTriggerValue("ritorsione")
        )
      ),
      List(MediaFile("abar_Faida.gif"))
    ),
    ReplyBundleMessage(
      TextTrigger(
        List(
          RegexTextTriggerValue("chi(s| )se( )?ne( )?frega".r)
        )
      ),
      List(MediaFile("abar_Chissenefrega.gif"))
    ),
    ReplyBundleMessage(
      TextTrigger(List(StringTextTriggerValue("buonasera"))),
      List(MediaFile("abar_Buonasera.gif"))
    ),
    ReplyBundleMessage(
      TextTrigger(
        List(
          StringTextTriggerValue("a morte"),
          RegexTextTriggerValue("(si| si|si ){2,}".r)
        )
      ),
      List(MediaFile("abar_SisiAMorte.gif"))
    ),
    ReplyBundleMessage(
      TextTrigger(List(StringTextTriggerValue("francesi"))),
      List(MediaFile("abar_Francesi.gif"))
    ),
    ReplyBundleMessage(
      TextTrigger(
        List(
          StringTextTriggerValue("viva il popolo"),
          StringTextTriggerValue("comunis")
        )
      ),
      List(MediaFile("abar_VivaIlPopolo.gif"))
    ),
    ReplyBundleMessage(
      TextTrigger(List(StringTextTriggerValue("fare qualcosa"))),
      List(MediaFile("abar_FareQualcosa.gif"))
    ),
    ReplyBundleMessage(
      TextTrigger(
        List(StringTextTriggerValue("(no|nessun|non c'è) problem(a)?"), StringTextTriggerValue("ammazziamo tutti"))
      ),
      List(MediaFile("abar_AmmazziamoTuttiNoProblem.gif"))
    ),
    ReplyBundleMessage(
      TextTrigger(List(RegexTextTriggerValue("\\bcerto\\b".r))),
      List(MediaFile("abar_Certo.gif"))
    ),
    ReplyBundleMessage(TextTrigger(List(StringTextTriggerValue("rogo"))), List(MediaFile("abar_Rogo.gif"))),
    ReplyBundleMessage(TextTrigger(List(StringTextTriggerValue("semplific"))), List(MediaFile("abar_Semplifico.gif"))),
    ReplyBundleMessage(
      TextTrigger(List(StringTextTriggerValue("bere"), RegexTextTriggerValue("taglia(re)? la gola".r))),
      List(MediaFile("abar_TaglioGolaBereSangue.gif"))
    ),
    ReplyBundleMessage(
      TextTrigger(List(RegexTextTriggerValue("spacco la (testa|faccia)".r))),
      List(MediaFile("abar_SpaccoLaTesta.gif"))
    ),
    ReplyBundleMessage(
      TextTrigger(List(RegexTextTriggerValue("dal (culo|sedere|fondo schiera|orifizio posteriore|dietro)".r))),
      List(MediaFile("abar_OrifizioPosteriore.gif"))
    ),
    ReplyBundleMessage(
      TextTrigger(List(StringTextTriggerValue("faccia tosta"), StringTextTriggerValue("furfante"))),
      List(MediaFile("abar_Furfante.gif"))
    ),
    ReplyBundleMessage(
      TextTrigger(List(StringTextTriggerValue("basta!"))),
      List(MediaFile("abar_Basta.gif"))
    ),
    ReplyBundleMessage(
      TextTrigger(List(StringTextTriggerValue("tutti insieme"), StringTextTriggerValue("ghigliottina"))),
      List(MediaFile("abar_GhigliottinaTuttiInsieme.gif"))
    ),
    ReplyBundleMessage(TextTrigger(List(StringTextTriggerValue("economisti"))), List(MediaFile("abar_Economisti.gif"))),
    ReplyBundleMessage(
      TextTrigger(List(RegexTextTriggerValue("vieni (un po' )?qui".r))),
      List(MediaFile("abar_VieniQui.gif"))
    ),
    ReplyBundleMessage(TextTrigger(List(StringTextTriggerValue("si fa così"))), List(MediaFile("abar_SiFaCosi.gif"))),
    ReplyBundleMessage(TextTrigger(List(StringTextTriggerValue("spranga"))), List(MediaFile("abar_Spranga.gif"))),
    ReplyBundleMessage(
      TextTrigger(List(StringTextTriggerValue("rapire"), StringTextTriggerValue("riscatto"))),
      List(MediaFile("abar_Riscatto.gif"))
    ),
    ReplyBundleMessage(
      TextTrigger(List(RegexTextTriggerValue(" n[o]+!".r), RegexTextTriggerValue("non (lo )?vogli(a|o)".r))),
      List(MediaFile("abar_No.gif"))
    ),
    ReplyBundleMessage(
      TextTrigger(List(StringTextTriggerValue("in un attimo"), StringTextTriggerValue("in piazza"))),
      List(MediaFile("abar_InPiazza.gif"))
    ),
    ReplyBundleMessage(
      TextTrigger(List(StringTextTriggerValue("in due pezzi"))),
      List(MediaFile("abar_InDuePezzi.gif"))
    ),
    ReplyBundleMessage(TextTrigger(List(StringTextTriggerValue("giusto"))), List(MediaFile("abar_Giusto.gif"))),
    ReplyBundleMessage(TextTrigger(List(StringTextTriggerValue("gli altri"))), List(MediaFile("abar_GliAltri.gif")))
  )

  val messageRepliesSpecialData: List[ReplyBundleMessage] = List(
    ReplyBundleMessage(
      TextTrigger(List(StringTextTriggerValue("tedesco"))),
      List(
        MediaFile("abar_Kraft.mp3"),
        MediaFile("abar_Von_Hohenheim.mp3"),
        MediaFile("abar_Haushofer.mp3")
      )
    ),
    ReplyBundleMessage(
      TextTrigger(List(StringTextTriggerValue("genitali"), StringTextTriggerValue("cosi e coglioni"))),
      List(
        MediaFile("abar_Cosi.mp3"),
        MediaFile("abar_Sottaceto.mp3")
      ),
      replySelection = RandomSelection
    ),
    ReplyBundleMessage(
      TextTrigger(List(RegexTextTriggerValue("(figlio|fijo) (di|de) (mignotta|puttana|troia)".r))),
      List(
        MediaFile("abar_FiglioDi.gif"),
        MediaFile("abar_FiglioDi2.gif"),
        MediaFile("abar_FiglioDi3.mp3")
      ),
      replySelection = RandomSelection
    ),
    ReplyBundleMessage(
      TextTrigger(List(StringTextTriggerValue("sgozza"))),
      List(MediaFile("abar_Sgozzamento.mp3"), MediaFile("abar_Sgozzamento.gif"))
    ),
    ReplyBundleMessage(
      TextTrigger(List(StringTextTriggerValue("bruciargli"), StringTextTriggerValue("la casa"))),
      List(MediaFile("abar_Bruciare.mp3"), MediaFile("abar_Bruciare.gif"))
    ),
    ReplyBundleMessage(
      TextTrigger(List(StringTextTriggerValue("a pezzi"), StringTextTriggerValue("a pezzettini"))),
      List(MediaFile("abar_APezzettini.mp3"), MediaFile("abar_APezzettini.gif"))
    ),
    ReplyBundleMessage(
      TextTrigger(
        List(StringTextTriggerValue("allarme"), StringTextTriggerValue("priori"), StringTextTriggerValue("carne"))
      ),
      List(MediaFile("abar_Priori.mp3"), MediaFile("abar_Priori.gif"))
    ),
    ReplyBundleMessage(
      TextTrigger(List(StringTextTriggerValue("zagaglia"), StringTextTriggerValue("nemico"))),
      List(MediaFile("abar_Zagaglia.mp3"), MediaFile("abar_Zagaglia.gif"))
    )
  )

  val messageRepliesData: List[ReplyBundleMessage] =
    (messageRepliesAudioData ++ messageRepliesGifData ++ messageRepliesSpecialData).sorted(ReplyBundle.ordering).reverse

  val messageReplyDataStringChunks: List[List[String]] = {
    val (triggers, lastTriggers) = messageRepliesData
      .map(_.trigger match {
        case TextTrigger(lt) => lt.mkString("[", " - ", "]")
        case _               => ""
      })
      .foldLeft((List.empty[List[String]], List.empty[String])) { case ((acc, candidate), triggerString) =>
        if ((candidate :+ triggerString).mkString("\n").length > 4090) (acc :+ candidate, List(triggerString))
        else (acc, candidate :+ triggerString)
      }
    triggers :+ lastTriggers
  }

  val commandRepliesData: List[ReplyBundleCommand] = List(
    ReplyBundleCommand(
      trigger = CommandTrigger("triggerlist"),
      text = TextReply(
        _ => messageReplyDataStringChunks,
        false
      )
    )
  )

  def token[F[_]: Async]: Resource[F, String] =
    ResourceAccess.fileSystem.getResourceByteArray[F]("abar_ABarberoBot.token").map(_.map(_.toChar).mkString)

  def buildBot[F[_]: Parallel: Async, A](
      executorContext: ExecutionContext,
      action: ABarberoBot[F] => F[A]
  ): F[A] = (for {
    client <- BlazeClientBuilder[F](executorContext).resource
    tk     <- token[F]
  } yield (client, tk)).use(client_tk => {
    implicit val api: Api[F] = BotApi(client_tk._1, baseUrl = s"https://api.telegram.org/bot${client_tk._2}")
    action(new ABarberoBot[F])
  })
}
