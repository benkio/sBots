package com.benkio.chatcore.repository

import cats.data.NonEmptyList
import cats.effect.Resource
import com.benkio.chatcore.model.media.MediaResource
import com.benkio.chatcore.model.reply.MediaFile
import com.benkio.chatcore.model.SBotInfo.SBotId

/** Wraps a repository for a platform that cannot use the file references (`MediaResourceIFile`): it gives only the
  * other resources, in the same order. A media left without a usable resource is a not found error, the same as a
  * media that does not exist. See [[Repository.usableFor]].
  */
class UsableResourcesRepository[F[_]](underlying: Repository[F]) extends Repository[F] {

  override def getResourcesByKind(
      criteria: String,
      botId: SBotId
  ): Resource[F, Either[Repository.RepositoryError, NonEmptyList[NonEmptyList[MediaResource[F]]]]] =
    underlying
      .getResourcesByKind(criteria, botId)
      .map(_.flatMap(groups =>
        NonEmptyList
          .fromList(groups.toList.flatMap(usable(_).toList))
          .toRight(Repository.RepositoryError.NoResourcesFoundKind(criteria, botId))
      ))

  override def getResourceFile(
      mediaFile: MediaFile
  ): Resource[F, Either[Repository.RepositoryError, NonEmptyList[MediaResource[F]]]] =
    underlying
      .getResourceFile(mediaFile)
      .map(_.flatMap(usable(_).toRight(Repository.RepositoryError.NoResourcesFoundFile(mediaFile))))

  private def usable(resources: NonEmptyList[MediaResource[F]]): Option[NonEmptyList[MediaResource[F]]] =
    NonEmptyList.fromList(resources.filter {
      case MediaResource.MediaResourceIFile(_) => false
      case _                                   => true
    })
}
