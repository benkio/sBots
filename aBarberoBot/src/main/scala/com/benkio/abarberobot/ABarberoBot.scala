package com.benkio.abarberobot

import org.http4s.client.blaze._
import scala.concurrent.ExecutionContext
import com.benkio.telegrambotinfrastructure.Configurations
import com.benkio.telegrambotinfrastructure.botCapabilities._
import com.benkio.telegrambotinfrastructure._
import cats.effect._
import com.benkio.telegrambotinfrastructure.model._
import telegramium.bots.high._
import cats._

class ABarberoBot[F[_]]()(implicit
    timerF: Timer[F],
    parallelF: Parallel[F],
    effectF: Effect[F],
    api: telegramium.bots.high.Api[F]
) extends BotSkeleton[F]()(timerF, parallelF, effectF, api) {

  override val resourceSource: ResourceSource = ABarberoBot.resourceSource

  override lazy val messageRepliesData: List[ReplyBundleMessage] =
    ABarberoBot.messageRepliesData

  override lazy val commandRepliesData: List[ReplyBundleCommand] = ABarberoBot.commandRepliesData
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
      List(MediaFile("ferroFuocoAcquaBollenteAceto.mp3"))
    ),
    ReplyBundleMessage(
      TextTrigger(
        List(
          StringTextTriggerValue("secolo")
        )
      ),
      List(MediaFile("secolo.mp3"))
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
      List(MediaFile("draghi.mp3"))
    ),
    ReplyBundleMessage(
      TextTrigger(
        List(
          StringTextTriggerValue("crociate")
        )
      ),
      List(MediaFile("crociate.mp3"))
    ),
    ReplyBundleMessage(
      TextTrigger(List(StringTextTriggerValue("wikipedia"))),
      List(MediaFile("wikipedia.mp3"))
    ),
    ReplyBundleMessage(
      TextTrigger(List(RegexTextTriggerValue("\\beccoh\\b".r))),
      List(MediaFile("ecco.mp3"))
    ),
    ReplyBundleMessage(
      TextTrigger(
        List(StringTextTriggerValue("maglio"), StringTextTriggerValue("sbriciola"), StringTextTriggerValue("schiaccia"))
      ),
      List(MediaFile("maglio.mp3"))
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
      List(MediaFile("assedi.mp3"))
    ),
    ReplyBundleMessage(
      TextTrigger(List(StringTextTriggerValue("furore"), StringTextTriggerValue("città"))),
      List(MediaFile("furore.mp3"))
    ),
    ReplyBundleMessage(
      TextTrigger(List(StringTextTriggerValue("demoni"), StringTextTriggerValue("scatenat"))),
      List(MediaFile("demoni.mp3"))
    ),
    ReplyBundleMessage(TextTrigger(List(StringTextTriggerValue("sensei"))), List(MediaFile("sensei.mp3"))),
    ReplyBundleMessage(TextTrigger(List(StringTextTriggerValue("miserabile"))), List(MediaFile("miserabile.mp3"))),
    ReplyBundleMessage(
      TextTrigger(List(StringTextTriggerValue("omicidio"), StringTextTriggerValue("cosa che capita"))),
      List(MediaFile("capitaOmicidio.mp3"))
    ),
    ReplyBundleMessage(
      TextTrigger(
        List(
          StringTextTriggerValue("cavallo"),
          RegexTextTriggerValue("tiriamo(lo)? giù".r),
          StringTextTriggerValue("ammazziamolo")
        )
      ),
      List(MediaFile("ammazziamolo.mp3"))
    ),
    ReplyBundleMessage(
      TextTrigger(
        List(
          StringTextTriggerValue("bruciare"),
          StringTextTriggerValue("saccheggiare"),
          StringTextTriggerValue("fuoco")
        )
      ),
      List(MediaFile("bbq.mp3"))
    ),
    ReplyBundleMessage(
      TextTrigger(List(StringTextTriggerValue("genitali"), StringTextTriggerValue("cosi e coglioni"))),
      List(MediaFile("cosi.mp3"))
    ),
    ReplyBundleMessage(
      TextTrigger(
        List(
          StringTextTriggerValue("guerra"),
          StringTextTriggerValue("chi vuole"),
          StringTextTriggerValue("la vogliamo")
        )
      ),
      List(MediaFile("guerra.mp3"))
    ),
    ReplyBundleMessage(
      TextTrigger(
        List(StringTextTriggerValue("cagarelli"), StringTextTriggerValue("feci"), StringTextTriggerValue("cacca"))
      ),
      List(MediaFile("homines.mp3"))
    ),
    ReplyBundleMessage(
      TextTrigger(List(StringTextTriggerValue("tedesco"), StringTextTriggerValue("kraft"))),
      List(MediaFile("kraft.mp3"))
    ),
    ReplyBundleMessage(
      TextTrigger(
        List(
          StringTextTriggerValue("monsignore"),
          StringTextTriggerValue("vescovo"),
          StringTextTriggerValue("in culo")
        )
      ),
      List(MediaFile("monsu.mp3"))
    ),
    ReplyBundleMessage(TextTrigger(List(StringTextTriggerValue("ottimismo"))), List(MediaFile("ottimismo.mp3"))),
    ReplyBundleMessage(
      TextTrigger(List(StringTextTriggerValue("latino"))),
      List(MediaFile("homines.mp3"), MediaFile("vagdavercustis.mp3"), MediaFile("yersinia.mp3")),
      replySelection = RandomSelection
    ),
    ReplyBundleMessage(TextTrigger(List(StringTextTriggerValue("radetzky"))), List(MediaFile("radetzky.mp3"))),
    ReplyBundleMessage(
      TextTrigger(List(StringTextTriggerValue("interrogateli"), StringTextTriggerValue("tortura"))),
      List(MediaFile("reinterrogateli.mp3"))
    ),
    ReplyBundleMessage(
      TextTrigger(List(StringTextTriggerValue(" re "), StringTextTriggerValue("decapita"))),
      List(MediaFile("re.mp3"))
    ),
    ReplyBundleMessage(
      TextTrigger(
        List(RegexTextTriggerValue("\\bascia\\b".r), StringTextTriggerValue("sangue"))
      ),
      List(MediaFile("sangue.mp3"))
    ),
    ReplyBundleMessage(TextTrigger(List(StringTextTriggerValue("spranga"))), List(MediaFile("spranga.mp3"))),
    ReplyBundleMessage(TextTrigger(List(StringTextTriggerValue("stupidi"))), List(MediaFile("stupidi.mp3"))),
    ReplyBundleMessage(TextTrigger(List(StringTextTriggerValue("subito"))), List(MediaFile("subito.mp3"))),
    ReplyBundleMessage(
      TextTrigger(List(StringTextTriggerValue("vagdavercustis"))),
      List(MediaFile("vagdavercustis.mp3"))
    ),
    ReplyBundleMessage(
      TextTrigger(List(StringTextTriggerValue("peste"), StringTextTriggerValue("yersinia"))),
      List(MediaFile("yersinia.mp3"))
    ),
    ReplyBundleMessage(TextTrigger(List(StringTextTriggerValue("zazzera"))), List(MediaFile("zazzera.mp3")))
  )

  val messageRepliesGifsData: List[ReplyBundleMessage] = List(
    ReplyBundleMessage(
      TextTrigger(
        List(StringTextTriggerValue("seziona"), StringTextTriggerValue("cadaveri"), StringTextTriggerValue("morti"))
      ),
      List(MediaFile("cadaveri.gif"))
    ),
    ReplyBundleMessage(
      TextTrigger(
        List(
          StringTextTriggerValue("strappa"),
          StringTextTriggerValue("gli arti"),
          StringTextTriggerValue("le braccia")
        )
      ),
      List(MediaFile("strappare.gif"))
    ),
    ReplyBundleMessage(
      TextTrigger(List(StringTextTriggerValue("saltare la testa"), StringTextTriggerValue("questa macchina"))),
      List(MediaFile("saltareLaTesta.gif"))
    ),
    ReplyBundleMessage(TextTrigger(List(StringTextTriggerValue("paura"))), List(MediaFile("paura.gif"))),
    ReplyBundleMessage(
      TextTrigger(List(StringTextTriggerValue("sega"), StringTextTriggerValue("dov'è"))),
      List(MediaFile("sega.gif"))
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
      List(MediaFile("potere.gif"))
    ),
    ReplyBundleMessage(
      TextTrigger(
        List(
          StringTextTriggerValue("grandioso"),
          StringTextTriggerValue("magnifico"),
          StringTextTriggerValue("capolavoro")
        )
      ),
      List(MediaFile("capolavoro.gif"))
    ),
    ReplyBundleMessage(
      TextTrigger(
        List(
          StringTextTriggerValue("troppo facile"),
          StringTextTriggerValue("easy")
        )
      ),
      List(MediaFile("troppoFacile.gif"))
    ),
    ReplyBundleMessage(
      TextTrigger(
        List(
          StringTextTriggerValue("trappola"),
          StringTextTriggerValue("tranello"),
          StringTextTriggerValue("inganno")
        )
      ),
      List(MediaFile("trappola.gif"))
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
      List(MediaFile("faida.gif"))
    ),
    ReplyBundleMessage(
      TextTrigger(
        List(
          RegexTextTriggerValue("chi(s| )se( )?ne( )?frega".r)
        )
      ),
      List(MediaFile("chissenefrega.gif"))
    ),
    ReplyBundleMessage(
      TextTrigger(List(StringTextTriggerValue("buonasera"))),
      List(MediaFile("buonasera.gif"))
    ),
    ReplyBundleMessage(
      TextTrigger(
        List(
          StringTextTriggerValue("a morte"),
          RegexTextTriggerValue("(si| si|si ){2,}".r)
        )
      ),
      List(MediaFile("sisiAMorte.gif"))
    ),
    ReplyBundleMessage(
      TextTrigger(List(StringTextTriggerValue("francesi"))),
      List(MediaFile("francesi.gif"))
    ),
    ReplyBundleMessage(
      TextTrigger(
        List(
          StringTextTriggerValue("viva il popolo"),
          StringTextTriggerValue("comunis")
        )
      ),
      List(MediaFile("vivaIlPopolo.gif"))
    ),
    ReplyBundleMessage(
      TextTrigger(List(StringTextTriggerValue("fare qualcosa"))),
      List(MediaFile("fareQualcosa.gif"))
    ),
    ReplyBundleMessage(
      TextTrigger(
        List(StringTextTriggerValue("(no|nessun|non c'è) problem(a)?"), StringTextTriggerValue("ammazziamo tutti"))
      ),
      List(MediaFile("ammazziamoTuttiNoProblem.gif"))
    ),
    ReplyBundleMessage(
      TextTrigger(List(RegexTextTriggerValue("\\bcerto\\b".r))),
      List(MediaFile("certo.gif"))
    ),
    ReplyBundleMessage(TextTrigger(List(StringTextTriggerValue("rogo"))), List(MediaFile("rogo.gif"))),
    ReplyBundleMessage(TextTrigger(List(StringTextTriggerValue("semplific"))), List(MediaFile("semplifico.gif"))),
    ReplyBundleMessage(
      TextTrigger(List(StringTextTriggerValue("bere"), RegexTextTriggerValue("taglia(re)? la gola".r))),
      List(MediaFile("taglioGolaBereSangue.gif"))
    ),
    ReplyBundleMessage(
      TextTrigger(List(RegexTextTriggerValue("spacco la (testa|faccia)".r))),
      List(MediaFile("spaccoLaTesta.gif"))
    ),
    ReplyBundleMessage(
      TextTrigger(List(RegexTextTriggerValue("dal (culo|sedere|fondo schiera|orifizio posteriore|dietro)".r))),
      List(MediaFile("orifizioPosteriore.gif"))
    ),
    ReplyBundleMessage(
      TextTrigger(List(StringTextTriggerValue("faccia tosta"), StringTextTriggerValue("furfante"))),
      List(MediaFile("furfante.gif"))
    ),
    ReplyBundleMessage(
      TextTrigger(List(StringTextTriggerValue("basta!"))),
      List(MediaFile("basta.gif"))
    ),
    ReplyBundleMessage(
      TextTrigger(List(StringTextTriggerValue("tutti insieme"), StringTextTriggerValue("ghigliottina"))),
      List(MediaFile("ghigliottinaTuttiInsieme.gif"))
    ),
    ReplyBundleMessage(TextTrigger(List(StringTextTriggerValue("economisti"))), List(MediaFile("economisti.gif"))),
    ReplyBundleMessage(
      TextTrigger(List(RegexTextTriggerValue("vieni (un po' )?qui".r))),
      List(MediaFile("vieniQui.gif"))
    ),
    ReplyBundleMessage(TextTrigger(List(StringTextTriggerValue("si fa così"))), List(MediaFile("siFaCosi.gif"))),
    ReplyBundleMessage(TextTrigger(List(StringTextTriggerValue("spranga"))), List(MediaFile("spranga.gif"))),
    ReplyBundleMessage(
      TextTrigger(List(StringTextTriggerValue("rapire"), StringTextTriggerValue("riscatto"))),
      List(MediaFile("riscatto.gif"))
    ),
    ReplyBundleMessage(
      TextTrigger(List(RegexTextTriggerValue(" n[o]+!".r), RegexTextTriggerValue("non (lo )?vogli(a|o)".r))),
      List(MediaFile("no.gif"))
    ),
    ReplyBundleMessage(
      TextTrigger(List(StringTextTriggerValue("in un attimo"), StringTextTriggerValue("in piazza"))),
      List(MediaFile("inPiazza.gif"))
    ),
    ReplyBundleMessage(TextTrigger(List(StringTextTriggerValue("in due pezzi"))), List(MediaFile("inDuePezzi.gif"))),
    ReplyBundleMessage(TextTrigger(List(StringTextTriggerValue("giusto"))), List(MediaFile("giusto.gif"))),
    ReplyBundleMessage(TextTrigger(List(StringTextTriggerValue("gli altri"))), List(MediaFile("gliAltri.gif")))
  )

  val messageRepliesSpecialData: List[ReplyBundleMessage] = List(
    ReplyBundleMessage(
      TextTrigger(List(RegexTextTriggerValue("(figlio|fijo) (di|de) (mignotta|puttana|troia)".r))),
      List(
        MediaFile("figlioDi.gif"),
        MediaFile("figlioDi2.gif"),
        MediaFile("figlioDi3.mp3")
      ),
      replySelection = RandomSelection
    ),
    ReplyBundleMessage(
      TextTrigger(List(StringTextTriggerValue("sgozza"))),
      List(MediaFile("sgozzamento.mp3"), MediaFile("sgozzamento.gif"))
    ),
    ReplyBundleMessage(
      TextTrigger(List(StringTextTriggerValue("bruciargli"), StringTextTriggerValue("la casa"))),
      List(MediaFile("bruciare.mp3"), MediaFile("bruciare.gif"))
    ),
    ReplyBundleMessage(
      TextTrigger(List(StringTextTriggerValue("a pezzi"), StringTextTriggerValue("a pezzettini"))),
      List(MediaFile("aPezzettini.mp3"), MediaFile("aPezzettini.gif"))
    ),
    ReplyBundleMessage(
      TextTrigger(
        List(StringTextTriggerValue("allarme"), StringTextTriggerValue("priori"), StringTextTriggerValue("carne"))
      ),
      List(MediaFile("priori.mp3"), MediaFile("priori.gif"))
    ),
    ReplyBundleMessage(
      TextTrigger(List(StringTextTriggerValue("zagaglia"), StringTextTriggerValue("nemico"))),
      List(MediaFile("zagaglia.mp3"), MediaFile("zagaglia.gif"))
    )
  )

  val messageRepliesData: List[ReplyBundleMessage] =
    messageRepliesAudioData ++ messageRepliesGifsData ++ messageRepliesSpecialData

  val messageReplyDataStringChunks = {
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

  def token[F[_]](implicit effectF: Effect[F]): Resource[F, String] =
    ResourceAccess.fileSystem.getResourceByteArray[F]("aBarberoBot.token").map(_.map(_.toChar).mkString)

  def buildBot[F[_], A](
      executorContext: ExecutionContext,
      action: ABarberoBot[F] => F[A]
  )(implicit
      timerF: Timer[F],
      parallelF: Parallel[F],
      contextShiftF: ContextShift[F],
      concurrentEffectF: ConcurrentEffect[F]
  ): F[A] = (for {
    client <- BlazeClientBuilder[F](executorContext).resource
    tk     <- token[F]
  } yield (client, tk)).use(client_tk => {
    implicit val api: Api[F] = BotApi(client_tk._1, baseUrl = s"https://api.telegram.org/bot${client_tk._2}")
    action(new ABarberoBot[F]())
  })
}
