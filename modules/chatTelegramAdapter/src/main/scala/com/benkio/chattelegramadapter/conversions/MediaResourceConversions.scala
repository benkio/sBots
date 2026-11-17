package com.benkio.chattelegramadapter.conversions

import cats.effect.Resource
import com.benkio.chatcore.model.media.MediaResource
import com.benkio.chatcore.model.media.MediaSources
import telegramium.bots.IFile
import telegramium.bots.InputLinkFile
import telegramium.bots.InputPartFile

object MediaResourceConversions {
  extension [F[_]](mediaResource: MediaResource[F]) {
    def toTelegramApi: Resource[F, IFile] = mediaResource match {
      case MediaResource.MediaResourceFile(rFile)      => rFile.map(p => InputPartFile(p.toFile()))
      case MediaResource.MediaResourceIFile(reference) =>
        Resource.pure(InputLinkFile(MediaSources.rawId(reference)))
    }
  }
}
