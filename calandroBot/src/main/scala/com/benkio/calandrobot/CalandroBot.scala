package com.benkio.calandrobot

import cats._
import cats.effect._
import cats.implicits._
import com.benkio.telegrambotinfrastructure._
import com.benkio.telegrambotinfrastructure.default.Actions.Action
import com.benkio.telegrambotinfrastructure.initialization.BotSetup
import com.benkio.telegrambotinfrastructure.messagefiltering._
import com.benkio.telegrambotinfrastructure.model._
import com.benkio.telegrambotinfrastructure.resources.ResourceAccess
import com.benkio.telegrambotinfrastructure.resources.db.DBLayer
import com.lightbend.emoji.ShortCodes.Defaults._
import com.lightbend.emoji.ShortCodes.Implicits._
import com.lightbend.emoji._
import log.effect.LogWriter
import org.http4s.Uri
import org.http4s.client.Client
import org.http4s.ember.client._
import org.http4s.implicits._
import telegramium.bots.Message
import telegramium.bots.high._

import scala.util.Random

class CalandroBotPolling[F[_]: Parallel: Async: Api: Action: LogWriter](
    resAccess: ResourceAccess[F],
    val dbLayer: DBLayer[F]
) extends BotSkeletonPolling[F]
    with CalandroBot[F] {
  override def resourceAccess(implicit syncF: Sync[F]): ResourceAccess[F] = resAccess
}

class CalandroBotWebhook[F[_]: Async: Api: Action: LogWriter](
    uri: Uri,
    resAccess: ResourceAccess[F],
    val dbLayer: DBLayer[F],
    path: Uri = uri"/"
) extends BotSkeletonWebhook[F](uri, path)
    with CalandroBot[F] {
  override def resourceAccess(implicit syncF: Sync[F]): ResourceAccess[F] = resAccess
}

trait CalandroBot[F[_]] extends BotSkeleton[F] {

  override val botName: String   = CalandroBot.botName
  override val botPrefix: String = CalandroBot.botPrefix

  override def messageRepliesDataF(implicit
      applicativeF: Applicative[F],
      log: LogWriter[F]
  ): F[List[ReplyBundleMessage[F]]] =
    CalandroBot.messageRepliesData[F].pure[F]

  override def commandRepliesDataF(implicit asyncF: Async[F], log: LogWriter[F]): F[List[ReplyBundleCommand[F]]] =
    CalandroBot.commandRepliesData[F].pure[F]
}

object CalandroBot {

  val botName: String         = "CalandroBot"
  val botPrefix: String       = "cala"
  val tokenFilename: String   = "cala_CalandroBot.token"
  val configNamespace: String = "calaDB"

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
        RegexTextTriggerValue("\\bsalve\\b".r, 5)
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
        RegexTextTriggerValue("\\bhd\\b".r, 2),
        RegexTextTriggerValue("nitid(o|ezza)".r, 6),
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
        RegexTextTriggerValue("\\bfiga\\b".r, 4),
        RegexTextTriggerValue("\\bfregna\\b".r, 6),
        RegexTextTriggerValue("\\bgnocca\\b".r, 6),
        RegexTextTriggerValue("\\bpatacca\\b".r, 7)
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

  def commandRepliesData[F[_]]: List[ReplyBundleCommand[F]] = List(
    ReplyBundleCommand(
      trigger = CommandTrigger("randomcard"),
      mediafiles = List(
        MediaFile("cala_CalandraCamiciaGialla.jpg"),
        MediaFile("cala_CalandraMagliaRossa.jpg"),
        MediaFile("cala_CazzoDiBudda.jpg"),
        MediaFile("cala_EvilPencil.jpg"),
        MediaFile("cala_FattorinoGLS.jpg"),
        MediaFile("cala_GiamaRhythmLord.jpg"),
        MediaFile("cala_PorcoLadro.jpg")
      ),
      replySelection = RandomSelection
    ),
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
      List(MediaFile("cala_WwhaaawhaaaSingolo.mp3"))
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

  def buildPollingBot[F[_]: Parallel: Async, A](
      action: CalandroBotPolling[F] => F[A]
  )(implicit log: LogWriter[F]): F[A] = (for {
    httpClient <- EmberClientBuilder.default[F].build
    botSetup <- BotSetup(
      httpClient = httpClient,
      tokenFilename = tokenFilename,
      namespace = configNamespace,
      botName = botName
    )
  } yield botSetup).use { botSetup =>
    action(
      new CalandroBotPolling[F](
        resAccess = botSetup.resourceAccess,
        dbLayer = botSetup.dbLayer
      )(Parallel[F], Async[F], botSetup.api, botSetup.action, log)
    )
  }

  def buildWebhookBot[F[_]: Async](
      httpClient: Client[F],
      webhookBaseUrl: String = org.http4s.server.defaults.IPv4Host
  )(implicit log: LogWriter[F]): Resource[F, CalandroBotWebhook[F]] =
    BotSetup(
      httpClient = httpClient,
      tokenFilename = tokenFilename,
      namespace = configNamespace,
      botName = botName,
      webhookBaseUrl = webhookBaseUrl
    ).map { botSetup =>
      new CalandroBotWebhook[F](
        uri = botSetup.webhookUri,
        path = botSetup.webhookPath,
        resAccess = botSetup.resourceAccess,
        dbLayer = botSetup.dbLayer
      )(Async[F], botSetup.api, botSetup.action, log)
    }
}
