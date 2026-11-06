package com.benkio.chatcore.patterns

import cats.effect.IO
import cats.syntax.all.*
import com.benkio.chatcore.mocks.DBLayerMock
import com.benkio.chatcore.model.reply.ReplyValue
import com.benkio.chatcore.model.reply.Text
import com.benkio.chatcore.model.show.ShowQuery
import com.benkio.chatcore.model.show.SimpleShowQuery
import com.benkio.chatcore.model.SBotInfo
import com.benkio.chatcore.repository.db.DBShow
import com.benkio.chatcore.repository.db.DBShowData
import com.benkio.chatcore.Logger.given
import munit.CatsEffectSuite

class SelectShowByIdOrKeywordSpec extends CatsEffectSuite {

  private val sBotInfo = SBotInfo(
    botName = SBotInfo.SBotName("SampleBot"),
    botId = SBotInfo.SBotId("sbot")
  )

  private def dbShowData(id: String, botId: String, title: String): DBShowData = DBShowData(
    show_id = id,
    bot_id = botId,
    show_title = title,
    show_upload_date = "2020-05-17T11:46:11.000Z",
    show_duration = 3600,
    show_description = Some("a description"),
    show_is_live = false,
    show_origin_automatic_caption = None,
    show_origin_automatic_caption_srt = ""
  )

  private val ownShow   = dbShowData(id = "own1", botId = "sbot", title = "Own show")
  private val otherShow = dbShowData(id = "other1", botId = "anotherbot", title = "Another bot show")

  // the mock leaves the keyword searches unimplemented: they find nothing here
  private val dbShow: DBShow[IO] = new DBShow[IO] {
    private val underlying = DBLayerMock.mock(sBotInfo.botId, shows = List(ownShow, otherShow)).dbShow
    override def getShows(botId: SBotInfo.SBotId)                             = underlying.getShows(botId)
    override def getShowById(showId: String)                                  = underlying.getShowById(showId)
    override def getRandomShow(botId: SBotInfo.SBotId)                        = underlying.getRandomShow(botId)
    override def getShowByShowQuery(query: ShowQuery, botId: SBotInfo.SBotId) =
      List.empty[DBShowData].pure[IO]
    override def getShowBySimpleShowQuery(query: SimpleShowQuery, botId: SBotInfo.SBotId) =
      List.empty[DBShowData].pure[IO]
    override def insertShow(dbShowData: DBShowData) = underlying.insertShow(dbShowData)
    override def deleteShow(dbShowData: DBShowData) = underlying.deleteShow(dbShowData)
  }

  private def select(target: String): IO[ReplyValue] =
    CommandPatterns.SearchShowCommand.selectShowByIdOrKeyword[IO](target, dbShow, sBotInfo, None)

  private def textOf(reply: ReplyValue): String = reply match {
    case Text(value, _) => value
    case other          => fail(s"Expected a text, got $other")
  }

  test("the id of a show of the bot returns that show") {
    select("own1").map { reply =>
      val text = textOf(reply)
      assert(text.contains("https://www.youtube.com/watch?v=own1"), text)
      assert(text.contains("Own show"), text)
    }
  }

  test("the id is trimmed") {
    select("  own1 ").map(reply => assert(textOf(reply).contains("watch?v=own1")))
  }

  test("an empty target returns a random show of the bot") {
    select("").map(reply => assert(textOf(reply).contains("watch?v=own1")))
  }

  test("the id of a show of another bot is not a match: it falls back to the keyword search") {
    select("other1").map(reply =>
      assertEquals(textOf(reply), "Nessuna puntata/show contenente 'other1' è stata trovata")
    )
  }

  test("an unknown id falls back to the keyword search") {
    select("something").map(reply =>
      assertEquals(textOf(reply), "Nessuna puntata/show contenente 'something' è stata trovata")
    )
  }
}
