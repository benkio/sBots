package com.benkio.telegrambotinfrastructure.dataentry

object MediaFileNameOps {

  private def isGifMp4(mediaFileName: String): Boolean =
    mediaFileName.dropRight(4).takeRight(3).toLowerCase == "gif"
  def getPrefix(mediaFileName: String): String =
    if isGifMp4(mediaFileName)
    then mediaFileName.dropRight(7)
    else mediaFileName.dropRight(4)
}
