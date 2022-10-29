package com.benkio.telegrambotinfrastructure.resources.db

import cats.effect._
import cats.implicits._
import com.benkio.telegrambotinfrastructure.resources.ResourceAccess
import com.benkio.telegrambotinfrastructure.web.UrlFetcher
import doobie._
import log.effect.LogWriter

import java.io.File
import java.nio.file.Files

object DBResourceAccess {

  // FIX: There should be no dependency to the UrlFetcher here!!
  def apply[F[_]: Async](
      transactor: Transactor[F],
      urlFetcher: UrlFetcher[F]
  )(implicit log: LogWriter[F]): F[ResourceAccess[F]] =
    DBMedia(transactor).map(dbMedia =>
      new DBResourceAccess[F](
        dbMedia = dbMedia,
        urlFetcher = urlFetcher
      )
    )

  private[telegrambotinfrastructure] class DBResourceAccess[F[_]: Async](
      dbMedia: DBMedia[F],
      urlFetcher: UrlFetcher[F]
  ) extends ResourceAccess[F] {

    def getResourceByteArray(resourceName: String): Resource[F, Array[Byte]] =
      for {
        media <- Resource.eval(dbMedia.getMedia(resourceName))
        file  <- urlFetcher.fetchFromDropbox(resourceName, media.media_url)
      } yield Files.readAllBytes(file.toPath)

    def getResourcesByKind(criteria: String): Resource[F, List[File]] =
      for {
        medias <- Resource.eval(dbMedia.getMediaByKind(criteria))
        files  <- medias.traverse(media => urlFetcher.fetchFromDropbox(media.media_name, media.media_url))
      } yield files
  }
}
