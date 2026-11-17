package com.benkio.chatcore.repository

import cats.data.NonEmptyList
import cats.effect.Resource
import com.benkio.chatcore.model.media.MediaResource
import com.benkio.chatcore.model.media.MediaSources
import com.benkio.chatcore.model.reply.MediaFile
import com.benkio.chatcore.model.PlatformCapabilities
import com.benkio.chatcore.model.SBotInfo.SBotId

/** Gives a platform only the sources it can use: URLs, plus the file references that belong to that platform and that
  * it accepts. A media left without a usable resource is a not found error, the same as a media that does not exist.
  * See [[Repository.usableFor]].
  */
class UsableResourcesRepository[F[_]](underlying: Repository[F], capabilities: PlatformCapabilities)
    extends Repository[F] {

  override def registerFileId(mediaFile: MediaFile, platform: com.benkio.chatcore.model.Platform, fileId: String)(using
      cats.Applicative[F]
  ): F[Unit] =
    underlying.registerFileId(mediaFile, platform, fileId)

  override def getResourcesByKind(
      criteria: String,
      botId: SBotId
  ): Resource[F, Either[Repository.RepositoryError, NonEmptyList[NonEmptyList[MediaResource[F]]]]] =
    underlying
      .getResourcesByKind(criteria, botId)
      .map(
        _.flatMap(groups =>
          NonEmptyList
            .fromList(groups.toList.flatMap(usable(_).toList))
            .toRight(Repository.RepositoryError.NoResourcesFoundKind(criteria, botId))
        )
      )

  override def getResourceFile(
      mediaFile: MediaFile
  ): Resource[F, Either[Repository.RepositoryError, NonEmptyList[MediaResource[F]]]] =
    underlying
      .getResourceFile(mediaFile)
      .map(_.flatMap(usable(_).toRight(Repository.RepositoryError.NoResourcesFoundFile(mediaFile))))

  private def usable(resources: NonEmptyList[MediaResource[F]]): Option[NonEmptyList[MediaResource[F]]] =
    NonEmptyList.fromList(resources.filter {
      case MediaResource.MediaResourceIFile(reference) =>
        capabilities.acceptsFileIds && MediaSources.belongsTo(reference, capabilities.platform)
      case _ => true
    })
}
