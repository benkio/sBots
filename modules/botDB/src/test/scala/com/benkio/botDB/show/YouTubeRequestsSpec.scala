package com.benkio.botDB.show

import cats.effect.IO
import com.benkio.chatcore.Logger.given
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport
import com.google.api.client.http.HttpRequest
import com.google.api.client.http.HttpRequestInitializer
import com.google.api.client.json.gson.GsonFactory
import com.google.api.services.youtube.YouTube
import munit.CatsEffectSuite

class YouTubeRequestsSpec extends CatsEffectSuite {

  private def mkYouTube(applicationName: String): IO[YouTube] =
    IO.delay {
      val transport   = GoogleNetHttpTransport.newTrustedTransport()
      val jsonFactory = GsonFactory.getDefaultInstance()
      YouTube
        .Builder(
          transport,
          jsonFactory,
          new HttpRequestInitializer() {
            override def initialize(request: HttpRequest): Unit = ()
          }
        )
        .setApplicationName(applicationName)
        .build()
    }

  test("YouTubeRequests.createYouTubeVideoRequest should set key/fields/maxResults/ids") {
    val apiKey   = "test-api-key"
    val videoIds = List("v1", "v2")
    for {
      yt  <- mkYouTube("YouTubeRequestsSpec")
      req <- YouTubeRequests.createYouTubeVideoRequest[IO](yt, videoIds, apiKey)
    } yield {
      assertEquals(req.getKey, apiKey)
      assertEquals(req.getMaxResults, java.lang.Long.valueOf(YouTubeRequests.maxResults))
      assert(req.getFields.contains("items("))
      assert(req.getId != null)
    }
  }

  test("YouTubeRequests.createYouTubePlaylistRequest should set playlistId and optional pageToken") {
    val apiKey     = "test-api-key"
    val playlistId = "PL_test"
    val pageToken  = "PAGE_TOKEN"
    for {
      yt         <- mkYouTube("YouTubeRequestsSpec")
      reqNoToken <- YouTubeRequests.createYouTubePlaylistRequest[IO](yt, playlistId, None, apiKey)
      reqToken   <- YouTubeRequests.createYouTubePlaylistRequest[IO](yt, playlistId, Some(pageToken), apiKey)
    } yield {
      assertEquals(reqNoToken.getKey, apiKey)
      assertEquals(reqNoToken.getPlaylistId, playlistId)
      assertEquals(reqNoToken.getPageToken, null)

      assertEquals(reqToken.getKey, apiKey)
      assertEquals(reqToken.getPlaylistId, playlistId)
      assertEquals(reqToken.getPageToken, pageToken)
    }
  }

  test("YouTubeRequests.createYouTubeChannelUploadPlaylistRequest should set forHandle and key") {
    val apiKey        = "test-api-key"
    val channelHandle = "@someChannel"
    for {
      yt  <- mkYouTube("YouTubeRequestsSpec")
      req <- YouTubeRequests.createYouTubeChannelUploadPlaylistRequest[IO](yt, channelHandle, apiKey)
    } yield {
      assertEquals(req.getKey, apiKey)
      assertEquals(req.getForHandle, channelHandle)
      assert(req.getFields.contains("uploads"))
    }
  }
}
