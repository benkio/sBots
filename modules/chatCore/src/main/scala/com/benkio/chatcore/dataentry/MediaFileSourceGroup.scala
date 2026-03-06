package com.benkio.chatcore.dataentry

import com.benkio.chatcore.model.media.MediaFileSource
import com.benkio.chatcore.model.reply.MediaFile
import com.benkio.chatcore.model.reply.ReplyBundleMessage

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
    ReplyBundleMessage.textToMedia("new data")(
      mediaFileSourceGroup.mediaFileSources.map(mediaFileSource => MediaFile.fromString(mediaFileSource.filename))*
    )
} // end MediaFileSourceGroup
