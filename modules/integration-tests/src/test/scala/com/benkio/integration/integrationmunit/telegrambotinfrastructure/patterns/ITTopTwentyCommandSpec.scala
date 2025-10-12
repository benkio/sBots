package com.benkio.integration.integrationmunit.telegrambotinfrastructure.patterns

import cats.effect.IO
import cats.effect.Resource
import com.benkio.integration.DBFixture
import com.benkio.telegrambotinfrastructure.patterns.CommandPatterns.RandomDataCommand
import munit.CatsEffectSuite

class ITTopTwentyCommandSpec extends CatsEffectSuite with DBFixture {

  databaseFixture.test(
    "Top twenty command should return 20 results"
  ) { fixture =>
    val resourceAssert = for {
      dbLayer <- fixture.resourceDBLayer
      dbMedia = dbLayer.dbMedia
      botId   = "rphjb_"
      resultMediaFile <- Resource.eval(
        RandomDataCommand.randomCommandLogic(dbMedia, botId)
      )
    } yield assertEquals(
      resultMediaFile.filename.take(botId.length),
      botId
    )
    resourceAssert.use_
  }

}
