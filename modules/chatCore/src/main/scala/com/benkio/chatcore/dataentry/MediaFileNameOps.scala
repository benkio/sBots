package com.benkio.chatcore.dataentry

import com.benkio.chatcore.model.reply.GifFile
import com.benkio.chatcore.model.reply.MarkdownFile
import com.benkio.chatcore.model.reply.MediaFile
import com.benkio.chatcore.model.reply.Sticker

object MediaFileNameOps {

  def getPrefix(mediaFileName: String): String =
    MediaFile.fromString(mediaFileName) match {
      case _: GifFile      => mediaFileName.dropRight(7)
      case _: MarkdownFile => mediaFileName.dropRight(3)
      case _: Sticker      => mediaFileName.dropRight(8)
      case _               =>
        mediaFileName.lastIndexOf('.') match {
          case -1 => mediaFileName
          case i  => mediaFileName.take(i)
        }
    }
}
