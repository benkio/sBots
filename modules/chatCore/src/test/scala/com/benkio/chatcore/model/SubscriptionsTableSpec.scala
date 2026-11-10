package com.benkio.chatcore.model

import com.benkio.chatcore.model.SBotInfo.SBotId
import cron4s.Cron
import munit.FunSuite

import java.time.Instant
import java.time.LocalDateTime
import java.util.UUID

class SubscriptionsTableSpec extends FunSuite {

  private val now = LocalDateTime.of(2026, 11, 10, 10, 30)

  private def subscription(
      id: String,
      subscriptionType: SubscriptionType = SubscriptionType.Show,
      target: Option[String] = None,
      cron: String = "0 0 9 * * ?",
      subscribedAt: Long = 1L
  ): Subscription = Subscription(
    id = SubscriptionId(UUID.fromString(id)),
    chatId = ChatId(1L),
    botId = SBotId("sbot"),
    subscriptionType = subscriptionType,
    target = target,
    cron = Cron.unsafeParse(cron),
    subscribedAt = Instant.ofEpochSecond(subscribedAt)
  )

  private val idA = "11111111-1111-1111-1111-111111111111"
  private val idB = "22222222-2222-2222-2222-222222222222"
  private val idC = "33333333-3333-3333-3333-333333333333"

  private def id(value: String): SubscriptionId = SubscriptionId(UUID.fromString(value))

  private val tableHeader = "| Type | Query | Next trigger | Cron | ID |\n|---|---|---|---|---|"

  test("no subscriptions: just a sentence, no table") {
    assertEquals(SubscriptionsTable.render(List.empty, Set.empty, now), "No subscriptions for this chat.")
  }

  test("one row per subscription with type, query, next trigger, cron and the copyable id") {
    val shows = List(
      subscription(id = idA, subscriptionType = SubscriptionType.Show, target = Some("pino scotto")),
      subscription(
        id = idB,
        subscriptionType = SubscriptionType.Trigger,
        target = Some("buongiorno"),
        cron = "0 30 18 ? * MON"
      ),
      subscription(id = idC, subscriptionType = SubscriptionType.Calendar)
    )
    val expected = List(
      "Subscriptions of this chat (3), times are server time:",
      tableHeader,
      s"| show | pino scotto | 2026-11-11 09:00 | `0 0 9 * * ?` | `$idA` |\n" +
        s"| trigger | buongiorno | 2026-11-16 18:30 | `0 30 18 ? * mon` | `$idB` |\n" +
        s"| calendar | - | 2026-11-11 09:00 | `0 0 9 * * ?` | `$idC` |"
    ).mkString("\n\n").replace(s"$tableHeader\n\n", s"$tableHeader\n")
    assertEquals(SubscriptionsTable.render(shows, shows.map(_.id).toSet, now), expected)
  }

  test("an empty query means random, for shows and triggers") {
    val subscriptions = List(
      subscription(id = idA, subscriptionType = SubscriptionType.Show, target = None),
      subscription(id = idB, subscriptionType = SubscriptionType.Trigger, target = Some("   "))
    )
    val rendered = SubscriptionsTable.render(subscriptions, subscriptions.map(_.id).toSet, now)
    assert(rendered.contains(s"| show | _random_ | 2026-11-11 09:00 | `0 0 9 * * ?` | `$idA` |"), rendered)
    assert(rendered.contains(s"| trigger | _random_ | 2026-11-11 09:00 | `0 0 9 * * ?` | `$idB` |"), rendered)
  }

  test("the next trigger is today when the time has not passed yet") {
    val s        = subscription(id = idA)
    val rendered = SubscriptionsTable.render(List(s), Set(s.id), LocalDateTime.of(2026, 11, 10, 8, 0))
    assert(rendered.contains("| 2026-11-10 09:00 |"), rendered)
  }

  test("a stored subscription that is not scheduled is flagged instead of showing a next trigger") {
    val scheduled    = subscription(id = idA)
    val notScheduled = subscription(id = idB, subscribedAt = 2L)
    val rendered     = SubscriptionsTable.render(List(scheduled, notScheduled), Set(scheduled.id), now)
    assert(rendered.contains(s"| show | _random_ | 2026-11-11 09:00 | `0 0 9 * * ?` | `$idA` |"), rendered)
    assert(rendered.contains(s"| show | _random_ | ⚠️ not scheduled | `0 0 9 * * ?` | `$idB` |"), rendered)
  }

  test("the special characters of the query are escaped, a newline cannot break the row") {
    val s        = subscription(id = idA, subscriptionType = SubscriptionType.Show, target = Some("a|b *c*\n  d"))
    val rendered = SubscriptionsTable.render(List(s), Set(s.id), now)
    val row      = rendered.split("\n").last
    assert(row.startsWith("| show | a\\|b \\*c\\* d |"), row)
    assertEquals(row.count(_ == '\n'), 0)
  }

  test("the rows are ordered by type (show, trigger, calendar) and then by subscription time") {
    val calendar = subscription(id = idA, subscriptionType = SubscriptionType.Calendar, subscribedAt = 1L)
    val trigger  =
      subscription(id = idB, subscriptionType = SubscriptionType.Trigger, target = Some("x"), subscribedAt = 2L)
    val showLate =
      subscription(id = idC, subscriptionType = SubscriptionType.Show, target = Some("late"), subscribedAt = 9L)
    val showEarly =
      subscription(
        id = "44444444-4444-4444-4444-444444444444",
        subscriptionType = SubscriptionType.Show,
        target = Some("early"),
        subscribedAt = 3L
      )
    val all  = List(calendar, trigger, showLate, showEarly)
    val rows = SubscriptionsTable
      .render(all, all.map(_.id).toSet, now)
      .split("\n")
      .toList
      .filter(line => line.startsWith("| ") && !line.startsWith("| Type"))
    assertEquals(rows.map(_.split('|')(1).trim), List("show", "show", "trigger", "calendar"))
    assertEquals(rows.take(2).map(_.split('|')(2).trim), List("early", "late"))
  }

  test("the scheduled subscriptions that are not stored are reported below the table") {
    val s        = subscription(id = idA)
    val rendered = SubscriptionsTable.render(List(s), Set(s.id), now, scheduledOnly = List(id(idB), id(idC)))
    assert(rendered.endsWith(s"⚠️ Scheduled but not stored: `$idB`, `$idC`"), rendered)
    val onlyOrphans = SubscriptionsTable.render(List.empty, Set.empty, now, scheduledOnly = List(id(idB)))
    assertEquals(onlyOrphans, s"No subscriptions for this chat.\n\n⚠️ Scheduled but not stored: `$idB`")
  }
}
