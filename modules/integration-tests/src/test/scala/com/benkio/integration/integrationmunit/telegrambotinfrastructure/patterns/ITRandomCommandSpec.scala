package com.benkio.integration.integrationmunit.telegrambotinfrastructure.patterns

import cats.effect.IO
import cats.effect.Resource
import com.benkio.integration.DBFixture
import com.benkio.telegrambotinfrastructure.model.SBotInfo
import com.benkio.telegrambotinfrastructure.model.SBotInfo.SBotId
import com.benkio.telegrambotinfrastructure.model.SBotInfo.SBotName
import com.benkio.telegrambotinfrastructure.patterns.CommandPatterns.RandomDataCommand
import munit.CatsEffectSuite

class ITRandomCommandSpec extends CatsEffectSuite with DBFixture {

  databaseFixture.test(
    "Random Command should return a MediaFile of the given bot"
  ) { fixture =>
    val resourceAssert = for {
      dbLayer <- fixture.resourceDBLayer
      dbMedia = dbLayer.dbMedia
      botId   = SBotId("rphjb")
      resultMediaFile <- Resource.eval(
        RandomDataCommand.randomCommandLogic(dbMedia, SBotInfo(botId, SBotName("testBot")))
      )
    } yield assertEquals(
      resultMediaFile.filename.take(botId.value.length),
      botId.value
    )
    resourceAssert.use_
  }

}
