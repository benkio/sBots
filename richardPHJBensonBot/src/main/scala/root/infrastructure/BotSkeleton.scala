package root.infrastructure

import info.mukel.telegrambot4s._, api._, methods._, models._, declarative._
import root.infrastructure._
import java.nio.file.Path
import java.nio.file.Paths
import java.nio.file.Files
import root.infrastructure.botCapabilities.ResourcesAccess
import root.infrastructure.MediaFile.MediaAction
import root.infrastructure.ReplyBundleRefined._
import scala.concurrent.Future


trait BotSkeleton extends TelegramBot
    with Polling
    with Commands
    with ChatActions
    with Messages
    with ResourcesAccess
    with Configurations {

  override val ignoreCommandReceiver = true

  implicit val sendAudioBenson: MediaAction[Mp3File] =
    (mediaFile: Mp3File) => (msg : Message) => {
    uploadingAudio(msg)
    val path = buildPath(mediaFile.filename)
    val mp3 = InputFile(path)
    request(SendAudio(msg.source, mp3))
  }

  implicit val sendGifBenson : MediaAction[GifFile] =
    (mediaFile : GifFile) => ( msg : Message) => {
    uploadingDocument(msg)
    val path = buildPath(mediaFile.filename)
    val byteArray : Array[Byte] = Files.readAllBytes(path)
    val gif = InputFile("benson.gif", byteArray)
    request(SendDocument(msg.source, gif))
  }

  val messageRepliesAudioData : List[ReplyBundle]
  val messageRepliesGifsData : List[ReplyBundle]
  val messageRepliesSpecialData : List[ReplyBundle]

  lazy val messageRepliesData : List[ReplyBundle] =
    messageRepliesAudioData ++ messageRepliesGifsData ++ messageRepliesSpecialData

   def messageRepliesDataRefined(implicit message : Message) : List[ReplyBundleRefined] =
    messageRepliesData.map(refineReplyBundle(_))

  onMessage((message : Message) =>
    message.text.map { m =>
      messageRepliesDataRefined(message)
        .flatMap(mrdr => MessageMatches.getHandler(
          mrdr.triggers,
          m,
          mrdr.messageReply,
          mrdr.matcher).toList
        )
    }
  )
}
