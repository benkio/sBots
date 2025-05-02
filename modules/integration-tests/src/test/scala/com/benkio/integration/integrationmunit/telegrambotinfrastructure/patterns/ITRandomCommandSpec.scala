package com.benkio.integration.integrationmunit.telegrambotinfrastructure.patterns

import com.benkio.telegrambotinfrastructure.patterns.CommandPatterns.RandomDataCommand
import cats.effect.IO
import cats.effect.Resource

import com.benkio.integration.DBFixture





import munit.CatsEffectSuite





class ITRandomCommandSpec extends CatsEffectSuite with DBFixture {

  databaseFixture.test(
    "Random Command should return a MediaFile of the given bot"
  ) { fixture =>
    val resourceAssert = for {
      dbLayer        <- fixture.resourceDBLayer
      dbMedia   = dbLayer.dbMedia
      botPrefix = "rphjb_"
      resultMediaFile <- Resource.eval(
        RandomDataCommand.randomCommandLogic(dbMedia, botPrefix)
      )
    } yield assertEquals(
      resultMediaFile.filename.take(botPrefix.length),
      botPrefix
    )
    resourceAssert.use_
  }

}
