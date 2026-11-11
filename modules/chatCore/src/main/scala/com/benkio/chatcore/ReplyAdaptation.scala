package com.benkio.chatcore

import cats.syntax.all.*
import cats.Monad
import com.benkio.chatcore.model.media.Media
import com.benkio.chatcore.model.reply.MediaFile
import com.benkio.chatcore.model.reply.ReplyTransformer
import com.benkio.chatcore.model.reply.ReplyValue
import com.benkio.chatcore.model.PlatformCapabilities
import com.benkio.chatcore.repository.db.DBLayer

/** What a reply becomes before it is sent to a platform: the [[ReplyTransformer]] with the links of the media, which
  * are in the DB.
  */
object ReplyAdaptation {

  def adaptToPlatform[F[_]: Monad](dbLayer: DBLayer[F], capabilities: PlatformCapabilities)(
      value: ReplyValue
  ): F[List[ReplyValue]] = value match {
    // a media the platform cannot send becomes the link of the media, when it has one
    case mediaFile: MediaFile if !capabilities.supports(mediaFile) =>
      dbLayer.dbMedia
        .getMedia(mediaFile.filepath)
        .map(_.flatMap(data => Media(data).toOption).flatMap(_.getLink))
        .map(link => ReplyTransformer.transform(capabilities, _ => link)(mediaFile))
    case other => ReplyTransformer.transform(capabilities)(other).pure[F]
  }
}
