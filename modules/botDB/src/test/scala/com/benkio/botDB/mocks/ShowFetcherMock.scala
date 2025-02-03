package com.benkio.botDB.mocks

import com.benkio.telegrambotinfrastructure.resources.db.DBShowData
import cats.effect.IO
import com.benkio.botDB.show.ShowSource
import com.benkio.botDB.show.ShowFetcher

class ShowFetcherMock() extends ShowFetcher[IO] {
  override def generateShowJson(showSource: ShowSource): IO[List[DBShowData]] =
    IO.pure(List.empty)
}
