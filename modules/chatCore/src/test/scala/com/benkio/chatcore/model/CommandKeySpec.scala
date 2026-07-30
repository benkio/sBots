package com.benkio.chatcore.model

import com.benkio.chatcore.Arbitraries.given
import com.benkio.chatcore.Generators.commandInputGen
import munit.ScalaCheckSuite
import org.scalacheck.Prop.*

class CommandKeySpec extends ScalaCheckSuite {

  property("asString values are unique") {
    val values = CommandKey.values.toList.map(_.asString)
    assertEquals(values.distinct.size, values.size)
  }

  property("fromString round-trips canonical asString") {
    forAll { (key: CommandKey) =>
      assertEquals(CommandKey.fromString(key.asString), Some(key))
      assertEquals(CommandKey.toStringValue(key), key.asString)
    }
  }

  property("fromString accepts slash, @bot, casing and whitespace wrappers") {
    forAll(commandInputGen) { case (key, input) =>
      assertEquals(CommandKey.fromString(input), Some(key))
    }
  }

  property("commandTriggers lists a trigger for every command key") {
    assertEquals(CommandKey.commandTriggers, CommandKey.values.toList.map(_.trigger))
  }
}
