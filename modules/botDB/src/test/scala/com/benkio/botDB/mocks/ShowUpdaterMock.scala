package com.benkio.botDB.mocks

import cats.effect.IO
import cats.effect.Resource
import com.benkio.botDB.show.ShowUpdater
//import com.benkio.telegrambotinfrastructure.repository.db.DBShowData

class ShowUpdaterMock() extends ShowUpdater[IO] {
  override def updateShow: Resource[IO, Unit] =
    Resource.pure(())
}
