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
import com.benkio.telegrambotinfrastructure.repository.Repository.RepositoryError
import log.effect.LogWriter
import org.http4s.Uri

object DBRepository:
  def dbResources[F[_]: Async: LogWriter](dbMedia: DBMedia[F], dropboxClient: DropboxClient[F]): Repository[F] =
    new Repository[F] {

      override def getResourcesByKind(
          criteria: String
      ): Resource[F, Either[RepositoryError, NonEmptyList[NonEmptyList[MediaResource[F]]]]] =
        for {
          _ <- Resource.eval(
            LogWriter.info(s"[dbRepository] getResourcesByKind fetching resources by $criteria")
          )
          eitherMedias: Either[RepositoryError, NonEmptyList[DBMediaData]] <- Resource.eval(
            dbMedia
              .getMediaByKind(criteria)
              .map(medias =>
                NonEmptyList.fromList(medias).fold(Left(RepositoryError.NoResourcesFoundKind(criteria)))(Right(_))
              )
          )
          _ = println(s"[DBRepository] medias: ${eitherMedias}") // Remove
          eitherMediaResources <-
            eitherMedias.fold(
              err => Resource.pure[F, Either[RepositoryError, NonEmptyList[NonEmptyList[MediaResource[F]]]]](Left(err)),
              medias =>
                medias
                  .traverse(
                    dbMediaDataToMediaResource(_)
                  )
                  .map(_.sequence)
            )
        } yield eitherMediaResources

      override def getResourceFile(
          mediaFile: MediaFile
      ): Resource[F, Either[RepositoryError, NonEmptyList[MediaResource[F]]]] = {
        for {
          _        <- Resource.eval(LogWriter.info(s"[ResourcesAccess] getResourceFile of $mediaFile"))
          mediaOpt <- Resource.eval(dbMedia.getMedia(mediaFile.filepath))
          result   <- mediaOpt.fold(Resource.pure(Left(RepositoryError.NoResourcesFoundFile(mediaFile))))(media =>
            dbMediaDataToMediaResource(media).evalTap(_ => dbMedia.incrementMediaCount(media.media_name))
          )
        } yield result
      }

      private[repository] def dbMediaDataToMediaResource(
          dbMediaData: DBMediaData
      ): Resource[F, Either[RepositoryError, NonEmptyList[MediaResource[F]]]] = {
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

        val result = for
          media <- Resource.eval(Async[F].fromEither(Media(dbMediaData)))
          _ <- Resource.eval(LogWriter.info(s"[ResourcesAccess] fetching data for $media from ${media.mediaSources}"))
          nonEmptyMediaSources <- Resource.eval(
            Async[F].fromOption(
              NonEmptyList.fromList(media.mediaSources),
              RepositoryError.NoResourcesFoundDBMediaData(dbMediaData)
            )
          )
          mediaResource <- buildMediaResources(media.mediaName, nonEmptyMediaSources)
        yield mediaResource

        result
          .map(Right(_))
          .handleErrorWith(e => Resource.pure(Left(RepositoryError.DBMediaDataToMediaResourceError(dbMediaData, e))))
      }
    }
end DBRepository
