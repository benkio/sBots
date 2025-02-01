package com.benkio.telegrambotinfrastructure.model.media

import java.io.File
import telegramium.bots.{InputLinkFile, InputPartFile, IFile}

enum MediaResource:
  case MediaResourceFile(file: File) extends MediaResource
  case MediaResourceIFile(iFile: String) extends MediaResource

extension (mediaResource: MediaResource)
  def toTelegramApi: IFile = mediaResource match {
    case MediaResource.MediaResourceFile(file: File)     => InputPartFile(file)
    case MediaResource.MediaResourceIFile(iFile: String) => InputLinkFile(iFile)
  }
  def getMediaResourceFile : Option[File] = mediaResource match {
    case MediaResource.MediaResourceFile(file: File)     => Some(file)
    case MediaResource.MediaResourceIFile(_) => None
  }
