package root.infrastructure

import info.mukel.telegrambot4s._
import api._
import declarative._

import info.mukel.telegrambot4s.models.Message
import root.infrastructure.botCapabilities.ResourcesAccess
import root.infrastructure.model.ReplyBundleRefined._
import root.infrastructure.default.DefaultActions
import root.infrastructure.model.ReplyBundleRefined
import root.infrastructure.model.ReplyBundle

trait BotSkeleton extends TelegramBot
    with Polling
    with Commands
    with DefaultActions
    with Messages
    with ResourcesAccess
    with Configurations {

  override val ignoreCommandReceiver = true

  val messageRepliesAudioData : List[ReplyBundle]
  val messageRepliesGifsData : List[ReplyBundle]
  val messageRepliesSpecialData : List[ReplyBundle]

  lazy val messageRepliesData : List[ReplyBundle] =
    messageRepliesAudioData ++ messageRepliesGifsData ++ messageRepliesSpecialData

  def messageRepliesDataRefined(implicit message : Message) : List[ReplyBundleRefined] =
    messageRepliesData.map(refineReplyBundle(_))

  onMessage((message : Message) =>
    message.text.foreach { m =>
      messageRepliesDataRefined(message)
        .flatMap(mrdr => MessageMatches.doesMatch(
          mrdr.triggers,
          m,
          mrdr.messageReply,
          mrdr.matcher).toList
        )
    }
  )
}
