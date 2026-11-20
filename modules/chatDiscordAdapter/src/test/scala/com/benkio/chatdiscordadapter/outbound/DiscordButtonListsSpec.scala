package com.benkio.chatdiscordadapter.outbound

import cats.effect.kernel.Ref
import cats.effect.IO
import com.benkio.chatcore.model.reply.PagedList
import com.benkio.chatcore.model.ChatId
import com.benkio.chatcore.model.CommandKey
import com.benkio.chatcore.model.Message
import com.benkio.chatcore.model.Platform
import cats.syntax.all.*
import munit.CatsEffectSuite

import scala.concurrent.duration.*

class DiscordButtonListsSpec extends CatsEffectSuite {

  private def entry(user: String): DiscordButtonList = DiscordButtonList(
    requestedBy = Some(user),
    request = Message(None, 0L, ChatId(Platform.Discord, "1"), "group"),
    pagedList = PagedList("t", List.empty, 0, CommandKey.TopTwenty)
  )

  private def lists(timeToLive: FiniteDuration, max: Int): IO[DiscordButtonLists[IO]] =
    Ref.of[IO, Map[String, (FiniteDuration, DiscordButtonList)]](Map.empty).map(new DiscordButtonLists[IO](_, timeToLive, max))

  test("a list is remembered by the id of its message, one per message") {
    for {
      l <- lists(1.hour, 10)
      _ <- l.put("m1", entry("a")) >> l.put("m2", entry("b"))
      a <- l.get("m1")
      b <- l.get("m2")
      c <- l.get("m3")
    } yield {
      assertEquals(a.flatMap(_.requestedBy), Some("a"))
      assertEquals(b.flatMap(_.requestedBy), Some("b"))
      assertEquals(c, None)
    }
  }

  test("a removed list is gone") {
    for {
      l <- lists(1.hour, 10)
      _ <- l.put("m1", entry("a")) >> l.remove("m1")
      a <- l.get("m1")
    } yield assertEquals(a, None)
  }

  test("a list expires") {
    for {
      l <- lists(1.millis, 10)
      _ <- l.put("m1", entry("a")) >> IO.sleep(20.millis)
      a <- l.get("m1")
    } yield assertEquals(a, None)
  }

  test("the oldest lists are dropped over the bound") {
    for {
      l <- lists(1.hour, 2)
      _ <- l.put("m1", entry("a")) >> IO.sleep(5.millis) >> l.put("m2", entry("b")) >> IO.sleep(5.millis) >> l.put("m3", entry("c"))
      r <- List("m1", "m2", "m3").traverse(l.get)
    } yield assertEquals(r.map(_.isDefined), List(false, true, true))
  }
}
