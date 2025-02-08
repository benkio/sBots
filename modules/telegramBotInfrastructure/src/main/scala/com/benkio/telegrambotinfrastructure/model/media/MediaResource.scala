package com.benkio.telegrambotinfrastructure.model.media

import cats.effect.kernel.Async
import cats.effect.Resource
import cats.syntax.all.*
import telegramium.bots.IFile
import telegramium.bots.InputLinkFile
import telegramium.bots.InputPartFile

import java.io.File

enum MediaResource[F[_]]:
  case MediaResourceFile(file: Resource[F, File]) extends MediaResource[F]
  case MediaResourceIFile(iFile: String)          extends MediaResource[F]

extension [F[_]: Async](mediaResource: MediaResource[F])
  def toTelegramApi: Resource[F, IFile] = mediaResource match {
    case MediaResource.MediaResourceFile(rFile: Resource[F, File]) => rFile.map(InputPartFile(_))
    case MediaResource.MediaResourceIFile(iFile: String)           => Resource.pure(InputLinkFile(iFile))
  }
  def getMediaResourceFile: Option[Resource[F, File]] = mediaResource match {
    case MediaResource.MediaResourceFile(file: Resource[F, File]) => Some(file)
    case MediaResource.MediaResourceIFile(_)                      => None
  }
