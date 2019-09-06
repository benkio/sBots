package root.infrastructure.default

import info.mukel.telegrambot4s._
import methods._
import models._
import java.nio.file.{Files, Path}

import info.mukel.telegrambot4s.api.{BotBase, ChatActions, RequestHandler}
import root.infrastructure.botCapabilities.ResourcesAccess
import root.infrastructure.default.Actions.Action
import root.infrastructure.model.{GifFile, Mp3File, PhotoFile, Reply}

import scala.concurrent.Future

trait DefaultActions extends BotBase with ChatActions with ResourcesAccess {

  implicit val sendPhoto : Action[PhotoFile] =
    Actions.sendPhoto(
      buildPath _,
      uploadingPhoto(_),
      request
    )

  implicit val sendAudio : Action[Mp3File] =
    Actions.sendAudio(
      buildPath _,
      uploadingAudio(_),
      request
    )

  implicit val sendGif : Action[GifFile] =
    Actions.sendGif(
      buildPath _,
      uploadingDocument(_),
      request
    )
}

object Actions {

  type Action[T <: Reply] =
    T => Message => Future[Message]

  def sendPhoto(buildPath: String => Path,
                uploadingPhoto: Message => Future[Boolean],
                request: RequestHandler
               ): Action[PhotoFile] =
    (mediaFile: PhotoFile) => (msg: Message) => {
      uploadingPhoto(msg)
      val path = buildPath(mediaFile.filename)
      val photo = InputFile(path)
      request(SendPhoto(msg.source, photo))
    }

  def sendAudio(buildPath: String => Path,
                uploadingAudio: Message => Future[Boolean],
                request: RequestHandler
               ): Action[Mp3File] =
    (mediaFile: Mp3File) => (msg: Message) => {
      uploadingAudio(msg)
      val path = buildPath(mediaFile.filename)
      val mp3 = InputFile(path)
      request(SendAudio(msg.source, mp3))
    }

  def sendGif(buildPath: String => Path,
              uploadingDocument: Message => Future[Boolean],
              request: RequestHandler
             ): Action[GifFile] =
    (mediaFile: GifFile) => (msg: Message) => {
      uploadingDocument(msg)
      val path = buildPath(mediaFile.filename)
      val byteArray: Array[Byte] = Files.readAllBytes(path)
      val gif = InputFile("botGif.gif", byteArray)
      request(SendDocument(msg.source, gif))
    }

}
