package root.infrastructure

import info.mukel.telegrambot4s._, api._, methods._, models._, declarative._
import root.infrastructure._
import java.nio.file.Path
import java.nio.file.Paths
import java.nio.file.Files
import root.infrastructure.botCapabilities.ResourcesAccess
import root.infrastructure.MediaFiles.MediaAction
import scala.concurrent.Future


trait BotSkeleton extends TelegramBot
    with Polling
    with Commands
    with ChatActions
    with Messages
    with ResourcesAccess
    with Configurations {

  override val ignoreCommandReceiver = true

  implicit val sendAudioBenson: MediaAction[Mp3Files] =
    (mediaFiles: Mp3Files) => (msg : Message) =>
  Future.traverse(mediaFiles.files)(filename => {
    uploadingAudio(msg)
    val path = buildPath(filename)
    val mp3 = InputFile(path)
    request(SendAudio(msg.source, mp3))
  })

  implicit val sendGifBenson : MediaAction[GifFiles] =
    (mediaFiles : GifFiles) => ( msg : Message) =>
  Future.traverse(mediaFiles.files)(filename => {
    uploadingDocument(msg)
    val path = buildPath(filename)
    val byteArray : Array[Byte] = Files.readAllBytes(path)
    val gif = InputFile("benson.gif", byteArray)
    request(SendDocument(msg.source, gif))
  })

  val messageRepliesAudioData : List[ReplyBundle]
  val messageRepliesGifsData : List[ReplyBundle]
  val messageRepliesSpecialData : List[ReplyBundle]

  lazy val messageRepliesData : List[ReplyBundle] =
    messageRepliesAudioData ++ messageRepliesGifsData ++ messageRepliesSpecialData

  lazy val messageRepliesDataRefined : List[ReplyBundleRefined] =
    refineReplyBundle(messageRepliesData)

  def refineReplyBundle(bundles : List[ReplyBundle]) : List[ReplyBundleRefined] =
    bundles.map((rb : ReplyBundle) =>
      ReplyBundleRefined(
        rb.triggers,
        MediaFiles.toMessageReply(rb.files),
        rb.matcher)
    )


  onMessage((message : Message) =>
    message.text.map { m =>
      messageRepliesDataRefined
        .flatMap(mrdr => MessageMatches.getHandler(
          mrdr.triggers,
          m,
          mrdr.messageReply,
          mrdr.matcher).toList
        )
        .foreach(_(message))
    }
  )
}
