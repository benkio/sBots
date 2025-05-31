package com.benkio.botDB.show

import cats.syntax.all.*
import com.google.api.client.http.HttpRequest
import com.google.api.client.http.HttpRequestInitializer
import com.google.api.services.youtube.YouTube
import com.google.api.client.json.gson.GsonFactory
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport
import log.effect.LogWriter
import cats.effect.kernel.Async

object YoutubeService {
  def apply[F[_]: LogWriter: Async](applicationName: String) : F[YouTube] =
    for {
      _                      <- LogWriter.info("[YoutubeService] Create NetHttpTransport and jsonFactory for YouTube")
      googleNetHttpTransport <- Async[F].delay(GoogleNetHttpTransport.newTrustedTransport())
      jsonFactory = GsonFactory.getDefaultInstance()
      _ <- LogWriter.info("[YoutubeService] Create youtube")
      youtubeService <- Async[F].delay(
        YouTube
          .Builder(
            googleNetHttpTransport,
            jsonFactory,
            new HttpRequestInitializer() {
              override def initialize(request: HttpRequest): Unit = ()
            }
          )
          .setApplicationName(applicationName)
          .build()
      )
    } yield youtubeService
}
