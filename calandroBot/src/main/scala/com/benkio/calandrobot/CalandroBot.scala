package com.benkio.calandrobot

import cats._
import cats.effect._
import cats.implicits._
import com.benkio.telegrambotinfrastructure.botcapabilities._
import com.benkio.telegrambotinfrastructure.messagefiltering._
import com.benkio.telegrambotinfrastructure.model._
import com.benkio.telegrambotinfrastructure.BotOps
import com.benkio.telegrambotinfrastructure._
import com.lightbend.emoji.ShortCodes.Defaults._
import com.lightbend.emoji.ShortCodes.Implicits._
import com.lightbend.emoji._
import log.effect.LogWriter
import org.http4s.Status
import org.http4s.blaze.client._
import org.http4s.client.Client
import telegramium.bots.Message
import telegramium.bots.high._

import scala.util.Random

class CalandroBotPolling[F[_]: Parallel: Async: Api: LogWriter] extends BotSkeletonPolling[F] with CalandroBot[F]

class CalandroBotWebhook[F[_]: Async: Api: LogWriter](url: String, path: String = "/")
    extends BotSkeletonWebhook[F](url, path)
    with CalandroBot[F]

trait CalandroBot[F[_]] extends BotSkeleton[F] {

  private def randomCardReplyBundleF(implicit asyncF: Async[F]): F[ReplyBundleCommand[F]] =
    resourceAccess
      .getResourcesByKind("cards")
      .use[ReplyBundleCommand[F]](files =>
        ReplyBundleCommand[F](
          CommandTrigger("randomcard"),
          files.map(f => MediaFile(f.getPath)),
          replySelection = RandomSelection
        ).pure[F]
      )

  override def messageRepliesDataF(implicit applicativeF: Applicative[F]): F[List[ReplyBundleMessage[F]]] =
    CalandroBot.messageRepliesData[F].pure[F]

  override def commandRepliesDataF(implicit asyncF: Async[F]): F[List[ReplyBundleCommand[F]]] =
    randomCardReplyBundleF.map(
      CalandroBot.commandRepliesData[F] :+ _
    )
}

object CalandroBot extends BotOps {

  def messageRepliesData[F[_]: Applicative]: List[ReplyBundleMessage[F]] = List(
    ReplyBundleMessage(
      TextTrigger(
        StringTextTriggerValue("sbrighi")
      ),
      text = Some(TextReply[F](_ => Applicative[F].pure(List("Passo")), false))
    ),
    ReplyBundleMessage(
      TextTrigger(
        StringTextTriggerValue("gay"),
        StringTextTriggerValue("frocio"),
        StringTextTriggerValue("culattone"),
        StringTextTriggerValue("ricchione")
      ),
      text = Some(TextReply[F](_ => Applicative[F].pure(List("CHE SCHIFO!!!")), false))
    ),
    ReplyBundleMessage(
      TextTrigger(
        StringTextTriggerValue("caldo"),
        StringTextTriggerValue("scotta")
      ),
      text = Some(TextReply[F](_ => Applicative[F].pure(List("Come i carbofreni della Brembo!!")), false))
    ),
    ReplyBundleMessage(
      TextTrigger(
        StringTextTriggerValue("ciao"),
        StringTextTriggerValue("buongiorno"),
        StringTextTriggerValue("salve")
      ),
      text = Some(TextReply[F](_ => Applicative[F].pure(List("Buongiorno Signori")), false))
    ),
    ReplyBundleMessage(
      TextTrigger(
        StringTextTriggerValue("film")
      ),
      text = Some(TextReply[F](_ => Applicative[F].pure(List("Lo riguardo volentieri")), false))
    ),
    ReplyBundleMessage(
      TextTrigger(
        StringTextTriggerValue("stasera"),
        StringTextTriggerValue("?")
      ),
      text = Some(TextReply[F](_ => Applicative[F].pure(List("Facciamo qualcosa tutti assieme?")), false)),
      matcher = ContainsAll
    ),
    ReplyBundleMessage(
      TextTrigger(
        StringTextTriggerValue(" hd"),
        StringTextTriggerValue("nitido"),
        StringTextTriggerValue("nitidezza"),
        StringTextTriggerValue("alta definizione")
      ),
      text =
        Some(TextReply[F](_ => Applicative[F].pure(List("Eh sì, vedi...si nota l'indecisione dell'immagine")), false))
    ),
    ReplyBundleMessage(
      TextTrigger(
        StringTextTriggerValue("qualità")
      ),
      text = Some(TextReply[F](_ => Applicative[F].pure(List("A 48x masterizza meglio")), false))
    ),
    ReplyBundleMessage(
      TextTrigger(
        StringTextTriggerValue("macchina"),
        StringTextTriggerValue("automobile")
      ),
      text = Some(TextReply[F](_ => Applicative[F].pure(List("Hai visto l'ultima puntata di \"Top Gear\"?")), false))
    ),
    ReplyBundleMessage(
      TextTrigger(
        RegexTextTriggerValue(" figa ".r),
        RegexTextTriggerValue("( )?fregna( )?".r),
        RegexTextTriggerValue("( )?gnocca( )?".r),
        RegexTextTriggerValue(" patacca ".r)
      ),
      text =
        Some(TextReply[F](_ => Applicative[F].pure(List("Io so come fare con le donne...ho letto tutto...")), false))
    ),
    ReplyBundleMessage(
      TextTrigger(
        StringTextTriggerValue("ambulanza"),
        StringTextTriggerValue(e":ambulance:")
      ),
      text = Some(
        TextReply[F](
          _ =>
            Applicative[F].pure(
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
      )
    ),
    ReplyBundleMessage(
      TextTrigger(
        StringTextTriggerValue("pc"),
        StringTextTriggerValue("computer")
      ),
      text =
        Some(TextReply[F](_ => Applicative[F].pure(List("Il fisso performa meglio rispetto al portatile!!!")), false))
    ),
    ReplyBundleMessage(
      TextTrigger(
        StringTextTriggerValue("videogioc"),
        StringTextTriggerValue(e":video_game:")
      ),
      text = Some(
        TextReply[F](
          _ =>
            Applicative[F].pure(
              List(s"GIOCHI PER IL MIO PC #${Random.nextInt(Int.MaxValue)}??No ma io non lo compro per i giochi!!!")
            ),
          false
        )
      )
    ),
    ReplyBundleMessage(
      TextTrigger(
        StringTextTriggerValue(" hs"),
        StringTextTriggerValue("hearthstone")
      ),
      text = Some(TextReply[F](_ => Applicative[F].pure(List("BASTA CON QUESTI TAUNT!!!")), false))
    ),
    ReplyBundleMessage(
      MessageLengthTrigger(280),
      text = Some(
        TextReply[F](
          (msg: Message) =>
            Applicative[F].pure(
              List(s"""wawaaa rischio calandrico in aumento(${msg.text.getOrElse("").length} / 280)""")
            ),
          true
        )
      )
    )
  )

  def commandRepliesData[F[_]: Applicative]: List[ReplyBundleCommand[F]] = List(
    ReplyBundleCommand(
      CommandTrigger("porcoladro"),
      List(MediaFile("cala_PorcoLadro.mp3"))
    ),
    ReplyBundleCommand(
      CommandTrigger("unoduetre"),
      List(MediaFile("cala_Unoduetre.mp3"))
    ),
    ReplyBundleCommand(
      CommandTrigger("ancorauna"),
      List(MediaFile("cala_AncoraUnaDoveLaMetto.mp3"))
    ),
    ReplyBundleCommand(
      CommandTrigger("lacipolla"),
      List(MediaFile("cala_CipollaCalandrica.mp3"))
    ),
    ReplyBundleCommand(
      CommandTrigger("lavorogiusto"),
      List(MediaFile("cala_IlLavoroVaPagato.mp3"))
    ),
    ReplyBundleCommand(
      CommandTrigger("motivazioniinternet"),
      List(MediaFile("cala_InternetMotivazioniCalandriche.mp3"))
    ),
    ReplyBundleCommand(
      CommandTrigger("cazzomene"),
      List(MediaFile("cala_IoSonVaccinato.mp3"))
    ),
    ReplyBundleCommand(
      CommandTrigger("arrivoarrivo"),
      List(MediaFile("cala_SubmissionCalandra.mp3"))
    ),
    ReplyBundleCommand(
      CommandTrigger("vaginadepilata"),
      List(MediaFile("cala_VaginaDepilataCalandra.mp3"))
    ),
    ReplyBundleCommand(
      CommandTrigger("whawha_fallout4"),
      List(MediaFile("cala_Waawahaawha.mp3"))
    ),
    ReplyBundleCommand(
      CommandTrigger("whawha_short"),
      List(MediaFile("cala_Wwhaaawhaaa Singolo.mp3"))
    ),
    ReplyBundleCommand(
      CommandTrigger("daccordissimo"),
      List(MediaFile("cala_D_accordissimo.mp3"))
    ),
    ReplyBundleCommand(
      CommandTrigger("stocazzo"),
      List(MediaFile("cala_Stocazzo.mp3"))
    ),
    ReplyBundleCommand(
      CommandTrigger("cazzodibudda"),
      List(MediaFile("cala_CazzoDiBudda.mp3"))
    ),
    ReplyBundleCommand(
      CommandTrigger("personapulita"),
      List(MediaFile("cala_PersonaPulita.mp3"))
    ),
    ReplyBundleCommand(
      CommandTrigger("losquirt"),
      List(MediaFile("cala_LoSquirt.mp3"))
    ),
    ReplyBundleCommand(
      CommandTrigger("fuoridalmondo"),
      List(MediaFile("cala_FuoriDalMondo.mp3"))
    ),
    ReplyBundleCommand(
      CommandTrigger("qualitaolive"),
      List(MediaFile("cala_QualitaOlive.mp3"))
    ),
    ReplyBundleCommand(
      CommandTrigger("gioielli"),
      List(MediaFile("cala_Gioielli.mp3"))
    ),
    ReplyBundleCommand(
      CommandTrigger("risata"),
      List(MediaFile("cala_RisataCalandrica.mp3"))
    ),
    ReplyBundleCommand(
      CommandTrigger("sonocosternato"),
      List(MediaFile("cala_SonoCosternato.mp3"))
    ),
    ReplyBundleCommand(
      CommandTrigger("demenza"),
      List(MediaFile("cala_LaDemenzaDiUnUomo.mp3"))
    ),
    ReplyBundleCommand(
      CommandTrigger("wha"),
      List(MediaFile("cala_WhaSecco.mp3"))
    ),
    ReplyBundleCommand(
      CommandTrigger("imparatounafava"),
      List(MediaFile("cala_ImparatoUnaFava.mp3"))
    ),
    ReplyBundleCommand(
      CommandTrigger("lesbiche"),
      List(MediaFile("cala_SieteLesbiche.mp3"))
    ),
    ReplyBundleCommand(
      CommandTrigger("firstlesson"),
      List(MediaFile("cala_FirstLessonPlease.mp3"))
    ),
    ReplyBundleCommand(
      CommandTrigger("noprogrammato"),
      List(MediaFile("cala_NoGrazieProgrammato.mp3"))
    ),
    ReplyBundleCommand(
      CommandTrigger("fiammeinferno"),
      List(MediaFile("cala_Fiamme.mp3"))
    )
  )

  def token[F[_]: Async]: Resource[F, String] =
    ResourceAccess.fromResources.getResourceByteArray("cala_CalandroBot.token").map(_.map(_.toChar).mkString)

  def buildCommonBot[F[_]: Async](httpClient: Client[F])(implicit log: LogWriter[F]): Resource[F, String] = for {
    tk                    <- token[F]
    _                     <- Resource.eval(log.info("[CalandroBot] Delete webook..."))
    deleteWebhookResponse <- deleteWebhooks[F](httpClient, tk)
    _ <- Resource.eval(
      Async[F].raiseWhen(deleteWebhookResponse.status != Status.Ok)(
        new RuntimeException("[CalandroBot] The delete webhook request failed: " + deleteWebhookResponse.as[String])
      )
    )
    _ <- Resource.eval(log.info("[CalandroBot] Webhook deleted"))

  } yield tk

  def buildPollingBot[F[_]: Parallel: Async, A](
      action: CalandroBotPolling[F] => F[A]
  )(implicit log: LogWriter[F]): F[A] = (for {
    httpClient <- BlazeClientBuilder[F].resource
    tk         <- buildCommonBot[F](httpClient)
  } yield (httpClient, tk))
    .use(httpClient_tk => {
      implicit val api: Api[F] = BotApi(httpClient_tk._1, baseUrl = s"https://api.telegram.org/bot${httpClient_tk._2}")
      action(new CalandroBotPolling[F])
    })

  def buildWebhookBot[F[_]: Async](
      httpClient: Client[F],
      webhookBaseUrl: String = org.http4s.server.defaults.IPv4Host
  )(implicit log: LogWriter[F]): Resource[F, CalandroBotWebhook[F]] = for {
    tk <- buildCommonBot[F](httpClient)
    baseUrl = s"https://api.telegram.org/bot$tk"
    path    = s"/$tk"
  } yield {
    implicit val api: Api[F] = BotApi(httpClient, baseUrl = baseUrl)
    new CalandroBotWebhook[F](
      url = webhookBaseUrl + path,
      path = s"/$tk"
    )
  }
}
