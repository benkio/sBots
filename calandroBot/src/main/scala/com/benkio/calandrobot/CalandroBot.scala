package com.benkio.calandrobot

import cats._
import cats.effect._
import cats.implicits._
import com.benkio.telegrambotinfrastructure.botCapabilities._
import com.benkio.telegrambotinfrastructure.model._
import com.benkio.telegrambotinfrastructure.Configurations
import com.benkio.telegrambotinfrastructure._
import com.lightbend.emoji.ShortCodes.Defaults._
import com.lightbend.emoji.ShortCodes.Implicits._
import com.lightbend.emoji._
import org.http4s.blaze.client._
import telegramium.bots.Message
import telegramium.bots.high._

import scala.concurrent.ExecutionContext
import scala.util.Random

class CalandroBotPolling[F[_]: Parallel: Async: Api] extends BotSkeletonPolling[F] with CalandroBot

class CalandroBotWebhook[F[_]: Async](api: Api[F], url: String, path: String = "/")
    extends BotSkeletonWebhook[F](api, url, path)
    with CalandroBot

trait CalandroBot extends BotSkeleton {

  override val resourceSource: ResourceSource = CalandroBot.resourceSource

  private def randomCardReplyBundleF[F[_]: Async]: F[ReplyBundleCommand] =
    ResourceSource
      .selectResourceAccess(All("calandro.db"))
      .getResourcesByKind("cards")
      .use[ReplyBundleCommand](mediaFile =>
        ReplyBundleCommand(
          CommandTrigger("randomcard"),
          mediaFile,
          replySelection = RandomSelection
        ).pure[F]
      )

  override def messageRepliesDataF[F[_]: Applicative]: F[List[ReplyBundleMessage]] =
    CalandroBot.messageRepliesData.pure[F]

  override def commandRepliesDataF[F[_]: Async]: F[List[ReplyBundleCommand]] =
    randomCardReplyBundleF[F].map(
      CalandroBot.commandRepliesData :+ _
    )
}

object CalandroBot extends Configurations {

  val resourceSource: ResourceSource = All("calandro.db")

  val messageRepliesData: List[ReplyBundleMessage] = List(
    ReplyBundleMessage(
      TextTrigger(List(StringTextTriggerValue("sbrighi"))),
      text = TextReply(_ => List(List("Passo")), false)
    ),
    ReplyBundleMessage(
      TextTrigger(
        List(
          StringTextTriggerValue("gay"),
          StringTextTriggerValue("frocio"),
          StringTextTriggerValue("culattone"),
          StringTextTriggerValue("ricchione")
        )
      ),
      text = TextReply(_ => List(List("CHE SCHIFO!!!")), false)
    ),
    ReplyBundleMessage(
      TextTrigger(List(StringTextTriggerValue("caldo"), StringTextTriggerValue("scotta"))),
      text = TextReply(_ => List(List("Come i carbofreni della Brembo!!")), false)
    ),
    ReplyBundleMessage(
      TextTrigger(
        List(StringTextTriggerValue("ciao"), StringTextTriggerValue("buongiorno"), StringTextTriggerValue("salve"))
      ),
      text = TextReply(_ => List(List("Buongiorno Signori")), false)
    ),
    ReplyBundleMessage(
      TextTrigger(List(StringTextTriggerValue("film"))),
      text = TextReply(_ => List(List("Lo riguardo volentieri")), false)
    ),
    ReplyBundleMessage(
      TextTrigger(List(StringTextTriggerValue("stasera"), StringTextTriggerValue("?"))),
      text = TextReply(_ => List(List("Facciamo qualcosa tutti assieme?")), false),
      matcher = ContainsAll
    ),
    ReplyBundleMessage(
      TextTrigger(
        List(
          StringTextTriggerValue(" hd"),
          StringTextTriggerValue("nitido"),
          StringTextTriggerValue("nitidezza"),
          StringTextTriggerValue("alta definizione")
        )
      ),
      text = TextReply(_ => List(List("Eh sì, vedi...si nota l'indecisione dell'immagine")), false)
    ),
    ReplyBundleMessage(
      TextTrigger(List(StringTextTriggerValue("qualità"))),
      text = TextReply(_ => List(List("A 48x masterizza meglio")), false)
    ),
    ReplyBundleMessage(
      TextTrigger(List(StringTextTriggerValue("macchina"), StringTextTriggerValue("automobile"))),
      text = TextReply(_ => List(List("Hai visto l'ultima puntata di \"Top Gear\"?")), false)
    ),
    ReplyBundleMessage(
      TextTrigger(
        List(
          RegexTextTriggerValue(" figa ".r),
          RegexTextTriggerValue("( )?fregna( )?".r),
          RegexTextTriggerValue("( )?gnocca( )?".r),
          RegexTextTriggerValue(" patacca ".r)
        )
      ),
      text = TextReply(_ => List(List("Io so come fare con le donne...ho letto tutto...")), false)
    ),
    ReplyBundleMessage(
      TextTrigger(List(StringTextTriggerValue("ambulanza"), StringTextTriggerValue(e":ambulance:"))),
      text = TextReply(
        _ =>
          List(
            List(
              Emoji(0x1f624).toString      // 😤
                ++ Emoji(0x1f918).toString // 🤘
                ++ Emoji(0x1f91e).toString // 🤞
                ++ Emoji(0x1f91e).toString // 🤞
                ++ Emoji(0x1f918).toString // 🤘
                ++ Emoji(0x1f624).toString // 😤
            )
          ),
        false
      )
    ),
    ReplyBundleMessage(
      TextTrigger(List(StringTextTriggerValue("pc"), StringTextTriggerValue("computer"))),
      text = TextReply(_ => List(List("Il fisso performa meglio rispetto al portatile!!!")), false)
    ),
    ReplyBundleMessage(
      TextTrigger(List(StringTextTriggerValue("videogioc"), StringTextTriggerValue(e":video_game:"))),
      text = TextReply(
        _ =>
          List(List(s"GIOCHI PER IL MIO PC #${Random.nextInt(Int.MaxValue)}??No ma io non lo compro per i giochi!!!")),
        false
      )
    ),
    ReplyBundleMessage(
      TextTrigger(List(StringTextTriggerValue(" hs"), StringTextTriggerValue("hearthstone"))),
      text = TextReply(_ => List(List("BASTA CON QUESTI TAUNT!!!")), false)
    ),
    ReplyBundleMessage(
      MessageLengthTrigger(280),
      text = TextReply(
        (msg: Message) =>
          List(List(s"""wawaaa rischio calandrico in aumento(${msg.text.getOrElse("").length} / 280)""")),
        true
      )
    )
  )

  val commandRepliesData: List[ReplyBundleCommand] = List(
    ReplyBundleCommand(CommandTrigger("porcoladro"), List(MediaFile("cala_PorcoLadro.mp3"))),
    ReplyBundleCommand(CommandTrigger("unoduetre"), List(MediaFile("cala_Unoduetre.mp3"))),
    ReplyBundleCommand(CommandTrigger("ancorauna"), List(MediaFile("cala_AncoraUnaDoveLaMetto.mp3"))),
    ReplyBundleCommand(CommandTrigger("lacipolla"), List(MediaFile("cala_CipollaCalandrica.mp3"))),
    ReplyBundleCommand(CommandTrigger("lavorogiusto"), List(MediaFile("cala_IlLavoroVaPagato.mp3"))),
    ReplyBundleCommand(
      CommandTrigger("motivazioniinternet"),
      List(MediaFile("cala_InternetMotivazioniCalandriche.mp3"))
    ),
    ReplyBundleCommand(CommandTrigger("cazzomene"), List(MediaFile("cala_IoSonVaccinato.mp3"))),
    ReplyBundleCommand(CommandTrigger("arrivoarrivo"), List(MediaFile("cala_SubmissionCalandra.mp3"))),
    ReplyBundleCommand(CommandTrigger("vaginadepilata"), List(MediaFile("cala_VaginaDepilataCalandra.mp3"))),
    ReplyBundleCommand(CommandTrigger("whawha_fallout4"), List(MediaFile("cala_Waawahaawha.mp3"))),
    ReplyBundleCommand(CommandTrigger("whawha_short"), List(MediaFile("cala_Wwhaaawhaaa Singolo.mp3"))),
    ReplyBundleCommand(CommandTrigger("daccordissimo"), List(MediaFile("cala_D_accordissimo.mp3"))),
    ReplyBundleCommand(CommandTrigger("stocazzo"), List(MediaFile("cala_Stocazzo.mp3"))),
    ReplyBundleCommand(CommandTrigger("cazzodibudda"), List(MediaFile("cala_CazzoDiBudda.mp3"))),
    ReplyBundleCommand(CommandTrigger("personapulita"), List(MediaFile("cala_PersonaPulita.mp3"))),
    ReplyBundleCommand(CommandTrigger("losquirt"), List(MediaFile("cala_LoSquirt.mp3"))),
    ReplyBundleCommand(CommandTrigger("fuoridalmondo"), List(MediaFile("cala_FuoriDalMondo.mp3"))),
    ReplyBundleCommand(CommandTrigger("qualitaolive"), List(MediaFile("cala_QualitaOlive.mp3"))),
    ReplyBundleCommand(CommandTrigger("gioielli"), List(MediaFile("cala_Gioielli.mp3"))),
    ReplyBundleCommand(CommandTrigger("risata"), List(MediaFile("cala_RisataCalandrica.mp3"))),
    ReplyBundleCommand(CommandTrigger("sonocosternato"), List(MediaFile("cala_SonoCosternato.mp3"))),
    ReplyBundleCommand(CommandTrigger("demenza"), List(MediaFile("cala_LaDemenzaDiUnUomo.mp3"))),
    ReplyBundleCommand(CommandTrigger("wha"), List(MediaFile("cala_WhaSecco.mp3"))),
    ReplyBundleCommand(CommandTrigger("imparatounafava"), List(MediaFile("cala_ImparatoUnaFava.mp3"))),
    ReplyBundleCommand(CommandTrigger("lesbiche"), List(MediaFile("cala_SieteLesbiche.mp3"))),
    ReplyBundleCommand(CommandTrigger("firstlesson"), List(MediaFile("cala_FirstLessonPlease.mp3"))),
    ReplyBundleCommand(CommandTrigger("noprogrammato"), List(MediaFile("cala_NoGrazieProgrammato.mp3"))),
    ReplyBundleCommand(CommandTrigger("fiammeinferno"), List(MediaFile("cala_Fiamme.mp3")))
  )

  def token[F[_]: Async]: Resource[F, String] =
    ResourceAccess.fileSystem.getResourceByteArray[F]("cala_CalandroBot.token").map(_.map(_.toChar).mkString)

  def buildPollingBot[F[_]: Parallel: Async, A](
      executorContext: ExecutionContext,
      action: CalandroBotPolling[F] => F[A]
  ): F[A] = (for {
    client <- BlazeClientBuilder[F](executorContext).resource
    tk     <- token[F]
  } yield (client, tk)).use(client_tk => {
    implicit val api: Api[F] = BotApi(client_tk._1, baseUrl = s"https://api.telegram.org/bot${client_tk._2}")
    action(new CalandroBotPolling[F])
  })
}
