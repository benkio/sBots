package com.benkio.chatcore

import cats.effect.IO
import cats.syntax.all.*
import com.benkio.chatcore.model.reply.Text
import munit.CatsEffectSuite

class BotEngineReactiveMessagesSpec extends CatsEffectSuite with BotEngineFixtures {

  test("a message that matches a trigger gets the reply of the bundle") {
    run(text = "ciao").map { case (sent, _) =>
      assertEquals(sent.map(s => (s.replyValue, s.replyToMessage)), List((Text("hello"), false)))
      assertEquals(sent.map(_.message.text), List(Some("ciao")))
    }
  }

  test("a message that matches no trigger gets no reply") {
    run(text = "something else").map { case (sent, _) => assertEquals(sent, List.empty) }
  }

  test("a reply bundle that is filtered out gets no reply") {
    run(text = "ciao", filter = false).map { case (sent, _) => assertEquals(sent, List.empty) }
  }

  test("the post computation runs once for every message that got a reply") {
    List("ciao", "/hi", "eng_file.mp3").traverse(run(_)).map { results =>
      assertEquals(results.map(_._2), List.fill(3)(1))
    }
  }

  test("the post computation does not run for a message that got no reply") {
    for {
      noMatch  <- List("something else", "/unknown").traverse(run(_))
      filtered <- run(text = "ciao", filter = false)
    } yield {
      assertEquals(noMatch.map(_._2), List.fill(2)(0))
      assertEquals(filtered._2, 0)
    }
  }
}
