package com.benkio.integration.integrationmunit.chatcore.patterns

import cats.effect.IO
import cats.effect.Resource
import cats.syntax.all.*
import com.benkio.chatcore.patterns.CommandPatterns.MediaByKindCommand
import com.benkio.chatcore.repository.db.DBMediaData
import com.benkio.integration.DBFixture
import com.benkio.integrationtest.Logger.given
import com.benkio.XahLeeBot.XahLeeBot
import munit.CatsEffectSuite

class ITMediaCommandByKindSpec extends CatsEffectSuite with DBFixture {

  databaseFixture.test(
    "MediaCommandByKind should return a value within the ones with the same kind"
  ) { fixture =>
    val commandName    = "fak"
    val botId          = XahLeeBot.sBotInfo.botId
    val resourceAssert = for {
      dbLayer <- fixture.resourceDBLayer
      dbMedia = dbLayer.dbMedia
      dbDatas         <- Resource.eval(dbMedia.getMediaByKind(kind = commandName, botId = botId))
      resultMediaFile <- Resource.eval(
        MediaByKindCommand.mediaCommandByKindLogic(
          dbMedia = dbMedia,
          commandName = commandName,
          sBotInfo = XahLeeBot.sBotInfo
        )
      )
    } yield {
      assert(
        dbDatas.exists((dbMedia: DBMediaData) => dbMedia.media_name == resultMediaFile.show),
        s"[ITMediaCommandByKindSpec] The randomly selected value ${resultMediaFile.show} is not contained in the dbMedia with kind `fak`"
      )
    }

    resourceAssert.use_
  }

}
