package root.infrastructure.model

import info.mukel.telegrambot4s.models.Message
import root.infrastructure.default.Actions.Action
import root.infrastructure.{ContainsOnce, MessageMatches}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

case class ReplyBundleRefined(
  triggers: List[String],
  messageReply: Future[List[Message]],
  matcher: MessageMatches = ContainsOnce
)

object ReplyBundleRefined {

  def refineReplyBundle(replyBundle : ReplyBundle)(
    implicit audioAction : Action[Mp3File],
    gifAction : Action[GifFile],
    photoAction : Action[PhotoFile],
    message : Message
  ) : ReplyBundleRefined =
    ReplyBundleRefined(
      replyBundle.triggers,
      Future.traverse(
        replyBundle.mp3files ++ replyBundle.giffiles
      )(MediaFile.toMessageReply(_)),
      replyBundle.matcher
    )


}
