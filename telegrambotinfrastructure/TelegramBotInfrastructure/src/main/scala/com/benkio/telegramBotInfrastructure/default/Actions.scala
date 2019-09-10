package com.benkio.telegramBotInfrastructure.default

import info.mukel.telegrambot4s._
import methods._
import models._
import java.nio.file.{Files, Path}

import info.mukel.telegrambot4s.api.declarative.Messages
import info.mukel.telegrambot4s.api.{ChatActions, RequestHandler}
import com.benkio.telegramBotInfrastructure.botCapabilities.ResourcesAccess
import com.benkio.telegramBotInfrastructure.default.Actions.Action
import com.benkio.telegramBotInfrastructure.model._

import scala.concurrent.Future

trait DefaultActions extends Messages with ChatActions with ResourcesAccess {

  implicit val sendPhoto: Action[PhotoFile] =
    Actions.sendPhoto(
      buildPath _,
      uploadingPhoto(_),
      request
    )

  implicit val sendAudio: Action[Mp3File] =
    Actions.sendAudio(
      buildPath _,
      uploadingAudio(_),
      request
    )

  implicit val sendGif: Action[GifFile] =
    Actions.sendGif(
      buildPath _,
      uploadingDocument(_),
      request
    )

  implicit val sendReply: Action[TextReply] =
    Actions.sendReply(
      typing(_),
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

  def sendReply(typing: Message => Future[Boolean],
              request: RequestHandler
             ): Action[TextReply] =
    (t: TextReply) => (msg: Message) => {
      typing(msg)
      val replyToMessageId : Option[Int] =
        if (t.replyToMessage) Some(msg.messageId) else None
      request(
        SendMessage(
          msg.source,
          t.text.fold("")(_ + "\n" + _),
          None,
          None,
          None,
          replyToMessageId,
          None
        )
      )
    }
}
