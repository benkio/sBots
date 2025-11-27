package com.benkio.telegrambotinfrastructure.dataentry

import com.benkio.telegrambotinfrastructure.model.media.MediaFileSource
import com.benkio.telegrambotinfrastructure.model.MimeType

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

  def toReplyBundleMessageCode(mediaFileSourceGroup: MediaFileSourceGroup): String = {
    def toMediaFiles(mfs: List[MediaFileSource]): String = mfs
      .map(mfs => {
        val stringContext = mfs.mime match {
          case MimeType.GIF                 => "gif"
          case MimeType.JPEG | MimeType.PNG => "pho"
          case MimeType.STICKER             => "sticker"
          case MimeType.MPEG                => "mp3"
          case MimeType.MP4                 => "vid"
          case MimeType.DOC                 => "doc"
        }
        s"""$stringContext"${mfs.filename}""""
      })
      .mkString(",\n    ")
    val replyBundleMessageMethod: String = mediaFileSourceGroup match {
      case MediaFileSourceGroup(mediaFileSources) if mediaFileSources.forall(_.mime == MimeType.GIF) =>
        "textToGif"
      case MediaFileSourceGroup(mediaFileSources) if mediaFileSources.forall(_.mime == MimeType.MPEG) =>
        "textToMp3"
      case MediaFileSourceGroup(mediaFileSources) if mediaFileSources.forall(_.mime == MimeType.MP4) =>
        "textToVideo"
      case MediaFileSourceGroup(mediaFileSources) => "textToMedia"
    }
    s"""ReplyBundleMessage
       |  .$replyBundleMessageMethod[F](
       |    ""
       |  )(
       |    ${toMediaFiles(mediaFileSourceGroup.mediaFileSources)}
       |  )""".stripMargin
  }
} // end MediaFileSourceGroup
