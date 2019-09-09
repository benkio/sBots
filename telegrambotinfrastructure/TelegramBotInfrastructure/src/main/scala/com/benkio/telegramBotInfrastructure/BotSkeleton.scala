package com.benkio.telegramBotInfrastructure

import info.mukel.telegrambot4s._
import api._
import declarative._
import info.mukel.telegrambot4s.models.Message
import com.benkio.telegramBotInfrastructure.botCapabilities.ResourcesAccess
import com.benkio.telegramBotInfrastructure.default.DefaultActions
import com.benkio.telegramBotInfrastructure.model.{Reply, ReplyBundle, ReplyBundleMessage, ReplyBundleCommand}

import scala.concurrent.Future

trait BotSkeleton extends TelegramBot
    with Polling
    with Commands
    with DefaultActions
    with Messages
    with ResourcesAccess
    with Configurations {

  override val ignoreCommandReceiver = true

  // Reply to Messages ////////////////////////////////////////////////////////

  lazy val messageRepliesData : List[ReplyBundleMessage] = List.empty[ReplyBundleMessage]

  onMessage((message : Message) => {
    message.text.foreach { m =>
      messageRepliesData
        .filter((mrd: ReplyBundleMessage) =>
          MessageMatches.doesMatch(
            mrd.triggers,
            m,
            mrd.matcher
          )
        )
        .foreach(ReplyBundle.computeReplyBundle(_, message))
    }
  })

  // Reply to Commands ////////////////////////////////////////////////////////

  val commandRepliesData : List[ReplyBundleCommand] = List.empty[ReplyBundleCommand]

  commandRepliesData
    .foreach(rb =>
      rb.triggers.foreach { c =>
        onCommand(c) { implicit msg =>
          ReplyBundle.computeReplyBundle(rb, msg)
        }
      })

}
