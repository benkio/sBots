package com.benkio.telegrambotinfrastructure.dataentry

import com.benkio.telegrambotinfrastructure.model.media.MediaFileSource
import com.benkio.telegrambotinfrastructure.model.reply.ReplyBundleMessage
import com.benkio.telegrambotinfrastructure.model.reply.MediaFile

case class MediaFileSourceGroup(
    mediaFileSources: List[MediaFileSource]
)

object MediaFileSourceGroup {

  def fromMediaFileSourceList(mediaFileSources: List[MediaFileSource]): List[MediaFileSourceGroup] =
    mediaFileSources
      .groupBy(x => MediaFileNameOps.getPrefix(x.filename))
      .map { case (_, mediaFileSources: List[MediaFileSource]) =>
        MediaFileSourceGroup(mediaFileSources)
      }
      .toList

  def toReplyBundleMessage(mediaFileSourceGroup: MediaFileSourceGroup): ReplyBundleMessage =
    ReplyBundleMessage.textToMedia("")(
      mediaFileSourceGroup.mediaFileSources.map(mediaFileSource =>
        MediaFile.fromString(mediaFileSource.filename)
      )*
    )
} // end MediaFileSourceGroup
