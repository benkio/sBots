package com.benkio.telegramBotInfrastructure

import info.mukel.telegrambot4s._
import api._
import declarative._
import info.mukel.telegrambot4s.models.Message
import com.benkio.telegramBotInfrastructure.botCapabilities.ResourcesAccess
import com.benkio.telegramBotInfrastructure.default.DefaultActions
import com.benkio.telegramBotInfrastructure.model.{Reply, ReplyBundle, ReplyBundleMessage}

import scala.concurrent.Future

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

  onMessage((message : Message) => {
    message.text.foreach { m =>
      messageRepliesData
        .filter {
          case mrd: ReplyBundleMessage =>
            MessageMatches.doesMatch(
              mrd.triggers,
              m,
              mrd.matcher
            )
          case _ => false
        }
        .foreach(replyBundle => for {
          m1 <- Future.traverse(replyBundle.mediafiles)(Reply.toMessageReply(_)(sendAudio, sendGif, sendPhoto, sendReply, message))
          m2 <- Future.traverse(replyBundle.text)(Reply.toMessageReply(_)(sendAudio, sendGif, sendPhoto, sendReply, message))
        } yield m1 ++ m2)
    }
  })
}
