package com.benkio.telegramBotInfrastructure

import com.benkio.telegramBotInfrastructure.botCapabilities.FileSystem
import com.benkio.telegramBotInfrastructure.botCapabilities.ResourceSource
import info.mukel.telegrambot4s._
import api._
import declarative._
import info.mukel.telegrambot4s.models.Message
import com.benkio.telegramBotInfrastructure.default.DefaultActions
import com.benkio.telegramBotInfrastructure.model.Reply
import com.benkio.telegramBotInfrastructure.model.ReplyBundle
import com.benkio.telegramBotInfrastructure.model.ReplyBundleMessage
import com.benkio.telegramBotInfrastructure.model.ReplyBundleCommand
import com.benkio.telegramBotInfrastructure.model.Timeout

import scala.concurrent.Future
import scala.concurrent.duration._

trait BotSkeleton extends TelegramBot with Polling with Commands with DefaultActions with Configurations {

  // Configuration values /////////////////////////////////////////////////////
  val resourceSource: ResourceSource      = FileSystem
  val ignoreMessagePrefix: Option[String] = Some("!")
  override val ignoreCommandReceiver      = true
  val inputTimeout: Option[Duration]      = Some(5.minute)

  // Reply to Messages ////////////////////////////////////////////////////////

  lazy val messageRepliesData: List[ReplyBundleMessage] = List.empty[ReplyBundleMessage]

  onMessage((message: Message) => {
    if (Timeout.isWithinTimeout(message.date, inputTimeout))
      message.text.foreach { m =>
        messageRepliesData
          .filter((mrd: ReplyBundleMessage) =>
            MessageMatches.doesMatch(
              mrd,
              m,
              ignoreMessagePrefix
            )
          )
          .foreach(ReplyBundle.computeReplyBundle(_, message))
      }
  })

  // Reply to Commands ////////////////////////////////////////////////////////

  lazy val commandRepliesData: List[ReplyBundleCommand] = List.empty[ReplyBundleCommand]

  commandRepliesData
    .foreach(rb =>
      onCommand(rb.trigger.command) { implicit msg =>
        ReplyBundle.computeReplyBundle(rb, msg)
      }
    )

}
