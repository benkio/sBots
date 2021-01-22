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
          StringTextTriggerValue("bruciare"),
          StringTextTriggerValue("saccheggiare"),
          StringTextTriggerValue("fuoco")
        )
      ),
      List(MediaFile("bbq.mp3"))
    ),
    ReplyBundleMessage(
      TextTrigger(
        List(
          StringTextTriggerValue("coglioni"),
          StringTextTriggerValue("cazzo")
        )
      ),
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
        List(
          StringTextTriggerValue("merda"),
          StringTextTriggerValue("cacca")
        )
      ),
      List(MediaFile("homines.mp3"))
    ),
    ReplyBundleMessage(
      TextTrigger(
        List(
          StringTextTriggerValue("a pezzi"),
          StringTextTriggerValue("a pezzettini")
        )
      ),
      List(MediaFile("aPezzettini.mp3"))
    ),
    ReplyBundleMessage(
      TextTrigger(
        List(
          StringTextTriggerValue("tedesco"),
          StringTextTriggerValue("kraft")
        )
      ),
      List(MediaFile("kraft.mp3"))
    ),
    ReplyBundleMessage(
      TextTrigger(
        List(
          StringTextTriggerValue("monsignore"),
          StringTextTriggerValue("vescovo"),
          StringTextTriggerValue("in cul"),
          StringTextTriggerValue("a tutti")
        )
      ),
      List(MediaFile("monsu.mp3"))
    ),
    ReplyBundleMessage(
      TextTrigger(
        List(
          StringTextTriggerValue("ottimismo")
        )
      ),
      List(MediaFile("ottimismo.mp3"))
    ),
    ReplyBundleMessage(
      TextTrigger(
        List(
          StringTextTriggerValue("allarme"),
          StringTextTriggerValue("priori")
        )
      ),
      List(MediaFile("priori.mp3"))
    ),
    ReplyBundleMessage(
      TextTrigger(
        List(
          StringTextTriggerValue("carne")
        )
      ),
      List(
        MediaFile("priori.mp3"),
        MediaFile("bbq.mp3")
      ),
      replySelection = RandomSelection
    ),
    ReplyBundleMessage(
      TextTrigger(
        List(
          StringTextTriggerValue("latino")
        )
      ),
      List(
        MediaFile("homines.mp3"),
        MediaFile("vagdavercustis.mp3"),
        MediaFile("yersinia.mp3")
      ),
      replySelection = RandomSelection
    ),
    ReplyBundleMessage(
      TextTrigger(
        List(
          StringTextTriggerValue("radetzky")
        )
      ),
      List(MediaFile("radetzky.mp3"))
    ),
    ReplyBundleMessage(
      TextTrigger(
        List(
          StringTextTriggerValue("interrogateli"),
          StringTextTriggerValue("tortura")
        )
      ),
      List(MediaFile("reinterrogateli.mp3"))
    ),
    ReplyBundleMessage(
      TextTrigger(
        List(
          StringTextTriggerValue(" re "),
          StringTextTriggerValue("decapita")
        )
      ),
      List(MediaFile("re.mp3"))
    ),
    ReplyBundleMessage(
      TextTrigger(
        List(
          StringTextTriggerValue("ascia"),
          StringTextTriggerValue("sangue")
        )
      ),
      List(MediaFile("sangue.mp3"))
    ),
    ReplyBundleMessage(
      TextTrigger(
        List(
          StringTextTriggerValue("bruciargli"),
          StringTextTriggerValue("la casa")
        )
      ),
      List(MediaFile("bruciare.mp3"))
    ),
    ReplyBundleMessage(
      TextTrigger(
        List(
          StringTextTriggerValue("sgozza")
        )
      ),
      List(MediaFile("sgozzamento.mp3"))
    ),
    ReplyBundleMessage(
      TextTrigger(
        List(
          StringTextTriggerValue("spranga")
        )
      ),
      List(MediaFile("spranga.mp3"))
    ),
    ReplyBundleMessage(
      TextTrigger(
        List(
          StringTextTriggerValue("stupidi")
        )
      ),
      List(MediaFile("stupidi.mp3"))
    ),
    ReplyBundleMessage(
      TextTrigger(
        List(
          StringTextTriggerValue("subito")
        )
      ),
      List(MediaFile("subito.mp3"))
    ),
    ReplyBundleMessage(
      TextTrigger(
        List(
          StringTextTriggerValue("vagdavercustis")
        )
      ),
      List(MediaFile("vagdavercustis.mp3"))
    ),
    ReplyBundleMessage(
      TextTrigger(
        List(
          StringTextTriggerValue("peste"),
          StringTextTriggerValue("yersinia")
        )
      ),
      List(MediaFile("yersinia.mp3"))
    ),
    ReplyBundleMessage(
      TextTrigger(
        List(
          StringTextTriggerValue("zagaglia")
        )
      ),
      List(MediaFile("zagaglia.mp3"))
    ),
    ReplyBundleMessage(
      TextTrigger(
        List(
          StringTextTriggerValue("zazzera")
        )
      ),
      List(MediaFile("zazzera.mp3"))
    )
  )

  val messageRepliesGifsData: List[ReplyBundleMessage] = List()

  val messageRepliesSpecialData: List[ReplyBundleMessage] = List()

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
