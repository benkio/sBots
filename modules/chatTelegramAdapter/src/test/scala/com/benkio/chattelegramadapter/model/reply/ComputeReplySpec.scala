package com.benkio.chattelegramadapter.model.reply

import cats.effect.*
import cats.syntax.all.*
import com.benkio.chatcore.model.reply.TextReply
import com.benkio.chattelegramadapter.ComputeReply
import munit.CatsEffectSuite

class ComputeReplySpec extends CatsEffectSuite {

  test("ComputeReply.runPlainReply should pick one of the values of the reply") {
    ComputeReply
      .runPlainReply[IO](TextReply.fromList("a", "b")(false))
      .map(value => assert(Set("a", "b").contains(value.show.trim), value.show))
  }
}
