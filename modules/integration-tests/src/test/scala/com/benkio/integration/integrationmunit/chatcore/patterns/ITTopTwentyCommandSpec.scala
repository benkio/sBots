package com.benkio.integration.integrationmunit.chatcore.patterns

import cats.effect.IO
import cats.effect.Resource
import com.benkio.chatcore.model.SBotInfo
import com.benkio.chatcore.model.SBotInfo.SBotId
import com.benkio.chatcore.model.SBotInfo.SBotName
import com.benkio.chatcore.patterns.CommandPatterns.RandomDataCommand
import com.benkio.integration.DBFixture
import com.benkio.integrationtest.Logger.given
import munit.CatsEffectSuite

class ITTopTwentyCommandSpec extends CatsEffectSuite with DBFixture {

  databaseFixture.test(
    "Top twenty command should return 20 results"
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
