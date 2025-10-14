package com.benkio.integration.integrationmunit.telegrambotinfrastructure.patterns

import cats.effect.IO
import cats.effect.Resource
import com.benkio.integration.DBFixture
import com.benkio.telegrambotinfrastructure.patterns.CommandPatterns.MediaByKindCommand
import com.benkio.xahleebot.XahLeeBot
import munit.CatsEffectSuite

class ITMediaCommandByKindSpec extends CatsEffectSuite with DBFixture {

  databaseFixture.test(
    "Top twenty command should return 20 results"
  ) { fixture =>
    val commandName    = "fak"
    val botId          = XahLeeBot.botId
    val resourceAssert = for {
      dbLayer <- fixture.resourceDBLayer
      dbMedia = dbLayer.dbMedia
      dbDatas          <- Resource.eval(dbMedia.getMediaByKind(kind = commandName, botId = botId))
      resultMediaFiles <- Resource.eval(
        MediaByKindCommand.mediaCommandByKindLogic(
          dbMedia = dbMedia,
          commandName = commandName,
          kind = None,
          botId = botId
        )
      )
    } yield
      assertEquals(
        dbDatas.length,
        resultMediaFiles.length,
        s"[ITMediaCommandByKindSpec] The data returned from the database and the command have different amount: db ${dbDatas.length} - command: ${resultMediaFiles.length}"
      )
      var mismatch = List.empty[String]
      assert(
        resultMediaFiles.forall(mediaFile =>
          val result = dbDatas.exists(dbData => dbData.media_name == mediaFile.filename)
          if !result then mismatch = mediaFile.filename :: mismatch
          result
        ),
        s"[ITMediaCommandByKindSpec] Unexpected files were returned from the DB: $mismatch"
      )

    resourceAssert.use_
  }

}
