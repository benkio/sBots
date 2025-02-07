package com.benkio.botDB.mocks

import cats.effect.IO
import com.benkio.botDB.show.ShowFetcher
import com.benkio.botDB.show.ShowSource
import com.benkio.telegrambotinfrastructure.resources.db.DBShowData

class ShowFetcherMock() extends ShowFetcher[IO] {
  override def generateShowJson(showSource: ShowSource): IO[List[DBShowData]] =
    IO.pure(List.empty)
}
