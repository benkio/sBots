package com.benkio.chatcore.model.media

import cats.effect.Resource

import java.nio.file.Path

enum MediaResource[F[_]] {
  case MediaResourceFile(file: Resource[F, Path]) extends MediaResource[F]
  case MediaResourceIFile(iFile: String)          extends MediaResource[F]
}

extension [F[_]](mediaResource: MediaResource[F]) {
  def getMediaResourceFile: Option[Resource[F, Path]] = mediaResource match {
    case MediaResource.MediaResourceFile(file: Resource[F, Path]) => Some(file)
    case MediaResource.MediaResourceIFile(_)                      => None
  }
}
