package com.benkio.telegrambotinfrastructure.resources.db

import com.benkio.telegrambotinfrastructure.resources.db.DBMediaData
import cats.effect.Resource
import com.benkio.telegrambotinfrastructure.DBFixture
import munit.CatsEffectSuite

import cats.effect.IO

class ITDBMediaSpec extends CatsEffectSuite with DBFixture {

  val testMediaName = "rphjb_MaSgus.mp3"
  val testMedia: DBMediaData = DBMediaData(
    testMediaName,
    None,
    "https://www.dropbox.com/sh/xqaatugvq8zcoyu/AACBnRH33traQAKBGy9bidu0a/rphjb_MaSgus.mp3?dl=1",
    0,
    "1662126019680"
  )

  databaseFixture.test(
    "DBMedia.getMedia should return the expected media"
  ) { fixture =>
    val resourceAssert = for {
      dbMedia <- fixture.resourceDBLayer.map(_.dbMedia)
      media   <- Resource.eval(dbMedia.getMedia(filename = testMediaName, cache = false))
    } yield media == testMedia
    resourceAssert.use(IO.pure).assert
  }

  databaseFixture.test(
    "DBMedia.getMediaByKind should return the expected media list"
  ) { fixture =>
    val expected: List[DBMediaData] = List(
      DBMediaData(
        "ancheLaRabbiaHaUnCuore.txt",
        Some("rphjb_LinkSources"),
        "https://www.dropbox.com/sh/xqaatugvq8zcoyu/AABLDyXAOThfUrS3EoR3kL6ma/rphjb_LinkSources/ancheLaRabbiaHaUnCuore.txt?dl=1",
        0,
        "1662126021547"
      ),
      DBMediaData(
        "live.txt",
        Some("rphjb_LinkSources"),
        "https://www.dropbox.com/sh/xqaatugvq8zcoyu/AACKI915JzajxuCSLy4spvbYa/rphjb_LinkSources/live.txt?dl=1",
        0,
        "1662126021551"
      ),
      DBMediaData(
        "perCordeEGrida.txt",
        Some("rphjb_LinkSources"),
        "https://www.dropbox.com/sh/xqaatugvq8zcoyu/AAA6aMpu41wxHF3wFrYZTXGba/rphjb_LinkSources/perCordeEGrida.txt?dl=1",
        0,
        "1662126021555"
      ),
      DBMediaData(
        "puntateCocktailMicidiale.txt",
        Some("rphjb_LinkSources"),
        "https://www.dropbox.com/sh/xqaatugvq8zcoyu/AAAfPoTfoPzhKys-DPI0YV8aa/rphjb_LinkSources/puntateCocktailMicidiale.txt?dl=1",
        0,
        "1662126021560"
      ),
      DBMediaData(
        "puntateRockMachine.txt",
        Some("rphjb_LinkSources"),
        "https://www.dropbox.com/sh/xqaatugvq8zcoyu/AABSjYo7uJwDeQqKe3bA5cXea/rphjb_LinkSources/puntateRockMachine.txt?dl=1",
        0,
        "1662126021564"
      )
    )

    val resourceAssert = for {
      dbMedia <- fixture.resourceDBLayer.map(_.dbMedia)
      medias  <- Resource.eval(dbMedia.getMediaByKind(kind = "rphjb_LinkSources"))
    } yield medias == expected
    resourceAssert.use(IO.pure).assert
  }

  databaseFixture.test(
    "DBMedia.getMediaByMediaCount should return the expected list of media"
  ) { fixture =>
    val expected: List[DBMediaData] = List(
      DBMediaData(
        "xah_Asshole.mp3",
        Some("Ass"),
        "https://www.dropbox.com/sh/h9j3xata6j6d6h5/AAA75bTQjYFsnGLLhZsSbIBXa/Ass/xah_Asshole.mp3?dl=1",
        0,
        "1662126014510"
      ),
      DBMediaData(
        "xah_CCppSucksStonkyAss.mp3",
        Some("Sucks"),
        "https://www.dropbox.com/s/hth49nkewdrrrlw/xah_CCppSucksStonkyAss.mp3?dl=1",
        0,
        "1662126014670"
      ),
      DBMediaData(
        "xah_EmacsLispPainAss.mp3",
        Some("Emacs"),
        "https://www.dropbox.com/sh/h9j3xata6j6d6h5/AADMexmO4NL3-Ts6vWlamB9Xa/Emacs/xah_EmacsLispPainAss.mp3?dl=1",
        0,
        "1662126014677"
      )
    )

    val resourceAssert = for {
      dbMedia <- fixture.resourceDBLayer.map(_.dbMedia)
      medias  <- Resource.eval(dbMedia.getMediaByMediaCount(limit = 3))
    } yield medias == expected
    resourceAssert.use(IO.pure).assert
  }

}
