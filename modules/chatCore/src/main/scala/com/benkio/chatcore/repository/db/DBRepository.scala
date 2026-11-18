package com.benkio.chatcore.repository.db

import cats.*
import cats.data.NonEmptyList
import cats.effect.*
import cats.implicits.*
import com.benkio.chatcore.http.HttpClients
import com.benkio.chatcore.model.media.Media
import com.benkio.chatcore.model.media.MediaResource
import com.benkio.chatcore.model.media.MediaResource.MediaResourceFile
import com.benkio.chatcore.model.media.MediaResource.MediaResourceIFile
import com.benkio.chatcore.model.media.MediaSource
import com.benkio.chatcore.model.reply.MediaFile
import com.benkio.chatcore.model.Platform
import com.benkio.chatcore.model.SBotInfo.SBotId
import com.benkio.chatcore.repository.db.DBMedia
import com.benkio.chatcore.repository.db.DBMediaData
import com.benkio.chatcore.repository.Repository
import com.benkio.chatcore.repository.Repository.RepositoryError
import log.effect.LogWriter

object DBRepository {
  def dbResources[F[_]: Async: LogWriter](
      dbMedia: DBMedia[F],
      clients: HttpClients[F]
  ): Repository[F] =
    new Repository[F] {

      override def getResourcesByKind(
          criteria: String,
          botId: SBotId
      ): Resource[F, Either[RepositoryError, NonEmptyList[NonEmptyList[MediaResource[F]]]]] =
        for {
          _ <- Resource.eval(
            LogWriter.info(s"[dbRepository] getResourcesByKind fetching resources by $criteria")
          )
          eitherMedias: Either[RepositoryError, NonEmptyList[DBMediaData]] <- Resource.eval(
            dbMedia
              .getMediaByKind(kind = criteria, botId = botId)
              .map(medias =>
                NonEmptyList
                  .fromList(medias)
                  .fold(Left(RepositoryError.NoResourcesFoundKind(criteria, botId)))(Right(_))
              )
          )
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

      override def registerFileId(mediaFile: MediaFile, platform: Platform, fileId: String)(using
          Applicative[F]
      ): F[Unit] =
        dbMedia.registerFileId(mediaFile.filepath, platform, fileId)

      override def replaceFileId(mediaFile: MediaFile, platform: Platform, failedId: String, newId: String)(using
          Applicative[F]
      ): F[Unit] =
        dbMedia.replaceFileId(mediaFile.filepath, platform, failedId, newId)

      private[repository] def dbMediaDataToMediaResource(
          dbMediaData: DBMediaData
      ): Resource[F, Either[RepositoryError, NonEmptyList[MediaResource[F]]]] = {
        def buildMediaResources(
            mediaName: String,
            sources: NonEmptyList[MediaSource]
        ): Resource[F, NonEmptyList[MediaResource[F]]] =
          Resource.pure(sources.map {
            case ref: MediaSource.FileRef => MediaResourceIFile(ref)
            case MediaSource.Url(uri)     =>
              val fetchFileResource = HttpClients.fetchFile(clients, mediaName, uri)
              MediaResourceFile(
                fetchFileResource
                  .onError(e =>
                    Resource.eval(
                      LogWriter
                        .error(s"[ResourcesAccess] Uri $uri for $dbMediaData failed to fetch the data with error: $e")
                    )
                  )
              )
          })

        val result = for {
          media <- Resource.eval(Async[F].fromEither(Media(dbMediaData)))
          dropped = Media.droppedSources(dbMediaData)
          _ <- Resource.eval(
            Async[F].whenA(dropped.nonEmpty)(
              LogWriter.warn(
                s"[ResourcesAccess] ${dbMediaData.media_name} has stored entries that are not a source, left out: $dropped"
              )
            )
          )
          _ <- Resource.eval(LogWriter.info(s"[ResourcesAccess] fetching data for $media from ${media.mediaSources}"))
          nonEmptyMediaSources <- Resource.eval(
            Async[F].fromOption(
              NonEmptyList.fromList(media.mediaSources),
              RepositoryError.NoResourcesFoundDBMediaData(dbMediaData)
            )
          )
          mediaResource <- buildMediaResources(media.mediaName, nonEmptyMediaSources)
        } yield mediaResource

        result
          .map(Right(_))
          .handleErrorWith(e => Resource.pure(Left(RepositoryError.DBMediaDataToMediaResourceError(dbMediaData, e))))
      }
    }
} // end DBRepository
