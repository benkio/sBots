package com.benkio.chatcore.model

import com.benkio.chatcore.model.reply.Markdown
import cron4s.lib.javatime.*
import cron4s.toDateTimeCronOps

import java.time.format.DateTimeFormatter
import java.time.LocalDateTime

/** The `/subscriptions` reply: the subscriptions of a chat as a markdown table. */
object SubscriptionsTable {

  private val nextTriggerFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")

  private val header: String = "| Type | Query | Next trigger | Cron | ID |\n|---|---|---|---|---|"

  // A table row is a single line: whatever the user wrote, it is reduced to one
  private def oneLine(text: String): String = text.trim.replaceAll("\\s+", " ")

  // The query of the subscription: empty means random, a calendar has none
  private def query(subscription: Subscription): String =
    subscription.subscriptionType match {
      case SubscriptionType.Calendar                        => "-"
      case SubscriptionType.Show | SubscriptionType.Trigger =>
        subscription.target.map(oneLine).filter(_.nonEmpty).fold("_random_")(Markdown.escape)
    }

  private def nextTrigger(subscription: Subscription, isScheduled: Boolean, now: LocalDateTime): String =
    if !isScheduled then "⚠️ not scheduled"
    else subscription.cron.next(now).fold("-")(_.format(nextTriggerFormatter))

  private def row(subscription: Subscription, isScheduled: Boolean, now: LocalDateTime): String =
    List(
      subscription.subscriptionType.asString,
      query(subscription),
      nextTrigger(subscription = subscription, isScheduled = isScheduled, now = now),
      s"`${subscription.cron}`",
      s"`${subscription.id.value}`"
    ).mkString("| ", " | ", " |")

  /** @param subscriptions
    *   the stored subscriptions of the chat
    * @param scheduled
    *   the ones that are scheduled in memory: the others are flagged
    * @param now
    *   the instant the next trigger times are computed from (server time)
    * @param scheduledOnly
    *   the scheduled ones that are not stored anymore, reported below the table
    */
  def render(
      subscriptions: List[Subscription],
      scheduled: Set[SubscriptionId],
      now: LocalDateTime,
      scheduledOnly: List[SubscriptionId] = List.empty
  ): String = {
    val table =
      if subscriptions.isEmpty then "No subscriptions for this chat."
      else {
        val ordered = subscriptions.sortBy(s => (s.subscriptionType.ordinal, s.subscribedAt, s.id.value.toString))
        val rows    = ordered.map(s => row(subscription = s, isScheduled = scheduled.contains(s.id), now = now))
        (s"Subscriptions of this chat (${subscriptions.size}), times are server time:" :: (header :: rows).mkString(
          "\n"
        ) :: Nil)
          .mkString("\n\n")
      }
    val orphans =
      Option
        .when(scheduledOnly.nonEmpty)(
          s"⚠️ Scheduled but not stored: ${scheduledOnly.map(id => s"`${id.value}`").mkString(", ")}"
        )
        .toList
    (table :: orphans).mkString("\n\n")
  }
}
