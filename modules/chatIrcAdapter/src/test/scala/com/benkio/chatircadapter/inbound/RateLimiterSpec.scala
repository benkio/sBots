package com.benkio.chatircadapter.inbound

import cats.effect.IO
import cats.syntax.all.*
import munit.CatsEffectSuite

import scala.concurrent.duration.*

class RateLimiterSpec extends CatsEffectSuite {

  test("a key gets its messages in the window, the next ones are refused") {
    for {
      limiter <- RateLimiter[IO](3, 10.seconds)
      results <- List.fill(5)("alice").traverse(limiter.allow)
    } yield assertEquals(results, List(true, true, true, false, false))
  }

  test("the keys are independent") {
    for {
      limiter <- RateLimiter[IO](1, 10.seconds)
      alice   <- List("alice", "alice").traverse(limiter.allow)
      bob     <- limiter.allow("bob")
    } yield {
      assertEquals(alice, List(true, false))
      assert(bob)
    }
  }

  test("a key is allowed again when its window is over") {
    for {
      limiter <- RateLimiter[IO](1, 200.millis)
      first   <- limiter.allow("alice")
      second  <- limiter.allow("alice")
      _       <- IO.sleep(300.millis)
      third   <- limiter.allow("alice")
    } yield assertEquals(List(first, second, third), List(true, false, true))
  }
}
