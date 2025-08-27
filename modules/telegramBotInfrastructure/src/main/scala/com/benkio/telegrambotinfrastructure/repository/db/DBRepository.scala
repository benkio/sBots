package com.benkio.telegrambotinfrastructure.repository.db

import cats.*
import cats.data.NonEmptyList
import cats.effect.*
import cats.implicits.*
import com.benkio.telegrambotinfrastructure.http.DropboxClient
import com.benkio.telegrambotinfrastructure.model.media.Media
import com.benkio.telegrambotinfrastructure.model.media.MediaResource
import com.benkio.telegrambotinfrastructure.model.media.MediaResource.MediaResourceFile
import com.benkio.telegrambotinfrastructure.model.media.MediaResource.MediaResourceIFile
import com.benkio.telegrambotinfrastructure.model.reply.MediaFile
import com.benkio.telegrambotinfrastructure.repository.db.DBMedia
import com.benkio.telegrambotinfrastructure.repository.db.DBMediaData
import com.benkio.telegrambotinfrastructure.repository.Repository
import log.effect.LogWriter
import org.http4s.Uri

object DBRepository:
  def dbResources[F[_]: Async: LogWriter](dbMedia: DBMedia[F], dropboxClient: DropboxClient[F]): Repository[F] =
    new Repository[F] {

      override def getResourcesByKind(criteria: String): Resource[F, NonEmptyList[NonEmptyList[MediaResource[F]]]] =
        for {
          _ <- Resource.eval(
            LogWriter.info(s"[dbRepository] getResourcesByKind fetching resources by $criteria")
          )
          medias <- Resource.eval(dbMedia.getMediaByKind(criteria))
          files  <-
            medias.traverse(
              dbMediaDataToMediaResource
            )
          result <- Resource.eval(
            Async[F].fromOption(NonEmptyList.fromList(files), Repository.NoResourcesFoundKind(criteria))
          )
        } yield result

      override def getResourceFile(
          mediaFile: MediaFile
      ): Resource[F, NonEmptyList[MediaResource[F]]] = {
        for {
          _             <- Resource.eval(LogWriter.info(s"[ResourcesAccess] getResourceFile of $mediaFile"))
          media         <- Resource.eval(dbMedia.getMedia(mediaFile.filepath))
          _             <- Resource.eval(dbMedia.incrementMediaCount(media.media_name))
          mediaResource <- dbMediaDataToMediaResource(media)
        } yield mediaResource
      }

      private[repository] def dbMediaDataToMediaResource(
          dbMediaData: DBMediaData
      ): Resource[F, NonEmptyList[MediaResource[F]]] = {
        def buildMediaResources(
            mediaName: String,
            sources: NonEmptyList[Either[String, Uri]]
        ): Resource[F, NonEmptyList[MediaResource[F]]] =
          Resource.pure(sources.map {
            case Left(iFile) => MediaResourceIFile(iFile)
            case Right(uri)  =>
              MediaResourceFile(
                dropboxClient
                  .fetchFile(mediaName, uri)
                  .onError(e =>
                    Resource.eval(
                      LogWriter
                        .error(s"[ResourcesAccess] Uri $uri for $dbMediaData failed to fetch the data with error: $e")
                    )
                  )
              )
          })

        for
          media <- Resource.eval(Async[F].fromEither(Media(dbMediaData)))
          _ <- Resource.eval(LogWriter.info(s"[ResourcesAccess] fetching data for $media from ${media.mediaSources}"))
          nonEmptyMediaSources <- Resource.eval(
            Async[F].fromOption(
              NonEmptyList.fromList(media.mediaSources),
              Repository.NoResourcesFoundDBMediaData(dbMediaData)
            )
          )
          mediaResource <- buildMediaResources(media.mediaName, nonEmptyMediaSources)
        yield mediaResource
      }
    }
end DBRepository
