package com.benkio.botDB.show

//import cats.effect.kernel.Async
import com.benkio.telegrambotinfrastructure.resources.db.DBShowData
//import log.effect.LogWriter

trait ShowFetcher[F[_]]:
  def generateShowJson(source: ShowSource): F[List[DBShowData]]

object ShowFetcher {
  def apply[
      F[_] // : Async: LogWriter
  ](youtubeApiKey: String): ShowFetcher[F] = ShowFetcherImpl[F](youtubeApiKey)

  private class ShowFetcherImpl[
      F[_] // : Async: LogWriter
  ](youtubeApiKey: String)
      extends ShowFetcher[F] {
    override def generateShowJson(source: ShowSource): F[List[DBShowData]] =
      val _ = youtubeApiKey
      ???

  }
}
