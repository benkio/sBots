package com.benkio.chatcore

import cats.effect.Deferred
import cats.effect.IO
import cats.effect.Ref
import munit.CatsEffectSuite

import scala.concurrent.duration.*

// The end of the window is a Deferred the test completes: no timing involved.
class ThrottleSpec extends CatsEffectSuite {

  private final case class Fixture(
      throttle: Throttle[IO],
      window: Deferred[IO, Unit],
      sent: Ref[IO, List[String]],
      summaries: Ref[IO, List[Int]]
  ) {
    def run(key: String): IO[Unit] =
      throttle(key)(send = sent.update(_ :+ key), summary = count => summaries.update(_ :+ count))
    def awaitSent(count: Int): IO[List[String]] = sent.get.iterateUntil(_.size >= count).timeout(5.seconds)
    def awaitSummaries: IO[List[Int]]           = summaries.get.iterateUntil(_.nonEmpty).timeout(5.seconds)
  }

  private val fixture: IO[Fixture] = for {
    window    <- Deferred[IO, Unit]
    throttle  <- Throttle.withWindow[IO](window.get)
    sent      <- Ref.of[IO, List[String]](List.empty)
    summaries <- Ref.of[IO, List[Int]](List.empty)
  } yield Fixture(throttle, window, sent, summaries)

  test("the first occurrence goes through, the repeats in the window are counted and summarised at its end") {
    for {
      f         <- fixture
      _         <- f.run("a") >> f.run("a") >> f.run("a")
      beforeEnd <- f.sent.get
      _         <- f.window.complete(())
      summaries <- f.awaitSummaries
    } yield {
      assertEquals(beforeEnd, List("a"))
      assertEquals(summaries, List(2))
    }
  }

  test("different keys do not suppress each other") {
    for {
      f    <- fixture
      _    <- f.run("a") >> f.run("b") >> f.run("a")
      sent <- f.sent.get
    } yield assertEquals(sent, List("a", "b"))
  }

  test("after the window the key goes through again") {
    for {
      f    <- fixture
      _    <- f.run("a") >> f.run("a")
      _    <- f.window.complete(())
      _    <- f.awaitSummaries
      _    <- f.run("a")
      sent <- f.awaitSent(2)
    } yield assertEquals(sent, List("a", "a"))
  }

  test("no summary when nothing was suppressed") {
    for {
      f <- fixture
      _ <- f.run("a")
      _ <- f.window.complete(())
      // the key is free again once the window is closed: the summary step has run by then
      _         <- (f.run("a") >> f.sent.get).iterateUntil(_.size >= 2).timeout(5.seconds)
      summaries <- f.summaries.get
    } yield assertEquals(summaries, List.empty[Int])
  }

  test("a failing summary does not break the throttle") {
    for {
      window   <- Deferred[IO, Unit]
      throttle <- Throttle.withWindow[IO](window.get)
      sent     <- Ref.of[IO, Int](0)
      run = throttle("a")(send = sent.update(_ + 1), summary = _ => IO.raiseError(new RuntimeException("no chat")))
      _     <- run >> run
      _     <- window.complete(())
      _     <- (run >> sent.get).iterateUntil(_ >= 2).timeout(5.seconds)
      count <- sent.get
    } yield assertEquals(count, 2)
  }

  test("a failing send is the caller's error and the key is still throttled") {
    for {
      window   <- Deferred[IO, Unit]
      throttle <- Throttle.withWindow[IO](window.get)
      result   <- throttle("a")(send = IO.raiseError(new RuntimeException("down")), summary = _ => IO.unit).attempt
    } yield assert(result.isLeft)
  }
}
