package com.benkio.chatcore.mocks

import com.benkio.chatcore.model.show.RandomQuery
import com.benkio.chatcore.model.show.ShowQueryKeyword
import com.benkio.chatcore.model.show.SimpleShowQuery
import com.benkio.chatcore.model.SBotInfo.SBotId
import com.benkio.chatcore.repository.db.DBShowData
import munit.CatsEffectSuite

import java.time.LocalDate

class DBShowMockSpec extends CatsEffectSuite {

  private val botId = SBotId("sbot")

  private def show(id: String, title: String, duration: Int = 3600) = DBShowData(
    show_id = id,
    bot_id = botId.value,
    show_title = title,
    show_upload_date = "2020-05-17T11:46:11.000Z",
    show_duration = duration,
    show_description = Some("a description"),
    show_is_live = false,
    show_origin_automatic_caption = Some("a caption"),
    show_origin_automatic_caption_srt = ""
  )

  private val first  = show("1", "A title with words")
  private val second = show("2", "Another title")
  private val other  = show("3", "Another bot title").copy(bot_id = "otherbot")

  private def mock = DBLayerMock.mock(botId, shows = List(first, second, other)).dbShow

  test("getShowByShowQuery with RandomQuery returns every show of the bot") {
    mock.getShowByShowQuery(RandomQuery, botId).map(result => assertEquals(result, List(first, second)))
  }

  test("getShowByShowQuery ANDs all the keywords of the query") {
    val query = ShowQueryKeyword(titleKeywords = Some(List("title", "words")))
    for {
      found    <- mock.getShowByShowQuery(query, botId)
      notFound <- mock.getShowByShowQuery(ShowQueryKeyword(Some(List("title", "missing"))), botId)
    } yield {
      assertEquals(found, List(first))
      assertEquals(notFound, Nil)
    }
  }

  test("getShowByShowQuery applies duration and date bounds") {
    val query = ShowQueryKeyword(
      titleKeywords = None,
      minDuration = Some(1000),
      maxDuration = Some(4000),
      minDate = Some(LocalDate.of(2020, 5, 16)),
      maxDate = Some(LocalDate.of(2020, 5, 18))
    )
    val short = show("4", "Short show", duration = 100)
    DBLayerMock
      .mock(botId, shows = List(short, first))
      .dbShow
      .getShowByShowQuery(query, botId)
      .map(result => assertEquals(result, List(first)))
  }

  test("getShowBySimpleShowQuery ORs title, description and caption") {
    for {
      byTitle    <- mock.getShowBySimpleShowQuery(SimpleShowQuery("words", "none", "none"), botId)
      byCaption  <- mock.getShowBySimpleShowQuery(SimpleShowQuery("none", "none", "caption"), botId)
      byAnything <- mock.getShowBySimpleShowQuery(SimpleShowQuery("", "", ""), botId)
      nullField  <- DBLayerMock
        .mock(botId, shows = List(first.copy(show_origin_automatic_caption = None)))
        .dbShow
        .getShowBySimpleShowQuery(SimpleShowQuery("none", "none", ""), botId)
    } yield {
      assertEquals(byTitle, List(first))
      assertEquals(byCaption, List(first, second))
      assertEquals(byAnything, List(first, second))
      assertEquals(nullField, Nil)
    }
  }

  test("deleteShow removes only the show with the given id") {
    val dbShow = mock
    for {
      _     <- dbShow.deleteShow(first)
      shows <- dbShow.getShows(botId)
      byId  <- dbShow.getShowById(first.show_id)
      other <- dbShow.getShowById("3")
    } yield {
      assertEquals(shows, List(second))
      assertEquals(byId, None)
      assertEquals(other.map(_.show_id), Some("3"))
    }
  }
}
