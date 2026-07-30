package com.benkio.chatcore.messagefiltering

import com.benkio.chatcore.Arbitraries.given
import com.benkio.chatcore.model.Message
import munit.ScalaCheckSuite
import org.scalacheck.Prop.*

class FilteringForwardSpec extends ScalaCheckSuite {

  property("filter is always true when disableForward is false") {
    forAll { (msg: Message) =>
      assert(FilteringForward.filter(msg, disableForward = false))
    }
  }

  property("filter is the negation of isForward when disableForward is true") {
    forAll { (msg: Message) =>
      assertEquals(FilteringForward.filter(msg, disableForward = true), !msg.isForward)
    }
  }
}
