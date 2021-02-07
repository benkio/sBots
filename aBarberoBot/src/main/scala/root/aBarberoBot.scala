///////////////////////////////////////////////////////////////////////////////
//             Bot of Alessandro Barbero, contains all the Barbero's frases  //
///////////////////////////////////////////////////////////////////////////////
package root

import com.benkio.telegramBotInfrastructure._
import com.benkio.telegramBotInfrastructure.botCapabilities._
import io.github.todokr.Emojipolation._
import com.benkio.telegramBotInfrastructure.model._

object ABarberoBot extends BotSkeleton {

  override val resourceSource: ResourceSource = FileSystem

  val messageRepliesAudioData: List[ReplyBundleMessage] = List(
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
      TextTrigger(List(StringTextTriggerValue("coglioni"), StringTextTriggerValue("cazzo"))),
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
      TextTrigger(List(StringTextTriggerValue("merda"), StringTextTriggerValue("cacca"))),
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
      TextTrigger(List(StringTextTriggerValue("ascia"), StringTextTriggerValue("sangue"))),
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
      TextTrigger(List(StringTextTriggerValue("certo "), StringTextTriggerValue(" certo"))),
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
      TextTrigger(List(StringTextTriggerValue("basta "), StringTextTriggerValue(" basta"))),
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
        MediaFile("figlioDi2.gif")
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

  override lazy val messageRepliesData: List[ReplyBundleMessage] =
    messageRepliesAudioData ++ messageRepliesGifsData ++ messageRepliesSpecialData

  override lazy val commandRepliesData: List[ReplyBundleCommand] = List(
    ReplyBundleCommand(
      trigger = CommandTrigger("triggerlist"),
      text = TextReply(
        _ =>
          messageRepliesData
            .take(messageRepliesData.length)
            .map(_.trigger match {
              case TextTrigger(lt) => lt.mkString("[", " - ", "]")
              case _               => ""
            }),
        false
      )
    )
  )
}
