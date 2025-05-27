package com.benkio.botDB

import cats.effect.ExitCode
import cats.effect.IO
import cats.effect.IOApp
import cats.syntax.all.*
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport
import com.google.api.client.http.HttpRequest
import com.google.api.client.http.HttpRequestInitializer
import com.google.api.client.json.gson.GsonFactory
import com.google.api.services.youtube.model.PlaylistItemListResponse
import com.google.api.services.youtube.YouTube
import log.effect.fs2.SyncLogWriter.consoleLogUpToLevel
import log.effect.LogLevels
import log.effect.LogWriter

import scala.jdk.CollectionConverters.*

object PlaygroundMain extends IOApp {

  // Example Reference
  // https://github.com/youtube/api-samples/blob/master/java/src/main/java/com/google/api/services/samples/youtube/cmdline/data/Search.java

  // TODO: fill
  val apiKeys         = "IGIOT"
  val applicationName = "8CAHB"

  val playlistIds    = List("PL7lQFvEjqu8OBiulbaSNnlCtlfI8Zd7zS", "PL_ylk9jdHmDmXYfedifLgtea5J5Ru-N-m")
  val channelHandles = List("Xahlee", "BrigateBenson", "youtuboancheio1365")

  val log: LogWriter[IO] = consoleLogUpToLevel(LogLevels.Info)

  private def createYouTube: IO[YouTube] =
    for {
      _                      <- log.info("[PlaygroundMain] Create NetHttpTransport and jsonFactory for YouTube")
      googleNetHttpTransport <- IO(GoogleNetHttpTransport.newTrustedTransport())
      jsonFactory = GsonFactory.getDefaultInstance()
      _ <- log.info("[PlaygroundMain] Create youtube")
      youtubeService <- IO(
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

    // YouTube Requests ///////////////////////////////////////////////////////

  // private def createYouTubeVideoRequest(youtubeService: YouTube, videoIds: List[String]): IO[YouTube#Videos#List] =
  //   for {
  //     _ <- log.info(s"[PlaygroundMain] $videoIds Create a YouTube Video request")
  //     request <- IO(
  //       youtubeService
  //         .videos()
  //         .list(List("id", "snippet", "contentDetails", "liveStreamingDetails").asJava)
  //         .setKey(apiKeys)
  //         .setFields("items(id,snippet/title,snippet/publishedAt,snippet/description,contentDetails/duration,liveStreamingDetails)")
  //         .setId(videoIds.asJava)
  //     )
  //   } yield request

  // private def createYouTubeVideoCaptionRequest(youtubeService: YouTube, videoId: String): IO[YouTube#Captions#List] =
  //   for {
  //     _ <- log.info(s"[PlaygroundMain] $videoId Create a YouTube Video Caption request")
  //     request <- IO(
  //       youtubeService
  //         .captions()
  //         .list(List("id", "snippet").asJava, videoId)
  //         .setKey(apiKeys)
  //         .setFields("items(id,snippet/trackKind,snippet/language)")
  //     )
  //   } yield request

  private def createYouTubePlaylistRequest(
      youtubeService: YouTube,
      playlistId: String,
      pageToken: Option[String]
  ): IO[YouTube#PlaylistItems#List] =
    for {
      _ <- log.info(s"[PlaygroundMain] $playlistId Create a YouTube Video Playlist request")
      request <- IO(
        youtubeService
          .playlistItems()
          .list(List("contentDetails").asJava)
          .setKey(apiKeys)
          .setFields("items(contentDetails/videoId),nextPageToken,pageInfo")
          .setPlaylistId(playlistId)
          .setMaxResults(50)
      )
    } yield pageToken.fold(request)(pt => request.setPageToken(pt))

  private def createYouTubeChannelUploadPlaylistRequest(
      youtubeService: YouTube,
      channelHandle: String
  ): IO[YouTube#Channels#List] =
    for {
      _ <- log.info(s"[PlaygroundMain] $channelHandle Create a YouTube Channel request")
      request <- IO(
        youtubeService
          .channels()
          .list(List("contentDetails").asJava)
          .setForHandle(channelHandle)
          .setKey(apiKeys)
          .setFields("items(contentDetails/relatedPlaylists/uploads)")
      )
    } yield request

  // High Level Functions /////////////////////////////////////////////////////

  private def getYouTubePlaylistIds(
      youtubeService: YouTube,
      playlistId: String
  ): IO[List[String]] =
    def extractPlaylistIds(response: PlaylistItemListResponse): List[String] =
      response.getItems().asScala.toList.map(_.getContentDetails().getVideoId())
    def collectAllIds(response: PlaylistItemListResponse): IO[List[String]] =
      Option(response.getNextPageToken())
        .fold(List.empty.pure[IO])(nextPageToken =>
          for
            nextRequest  <- createYouTubePlaylistRequest(youtubeService, playlistId, nextPageToken.some)
            nextResponse <- IO(nextRequest.execute())
            nextIds      <- collectAllIds(nextResponse)
          yield nextIds
        )
        .map(nextIds => extractPlaylistIds(response) ++ nextIds)
    for {
      _               <- log.info(s"[PlaygroundMain] $playlistId Ids fetching")
      initialRequest  <- createYouTubePlaylistRequest(youtubeService, playlistId, None)
      _               <- log.info(s"[PlaygroundMain] $playlistId Initial Request execution")
      initialResponse <- IO(initialRequest.execute())
      _               <- log.info(s"[PlaygroundMain] $playlistId Computing video ids")
      ids             <- collectAllIds(initialResponse)
    } yield ids

  private def getYouTubePlaylistsIds(youtubeService: YouTube, playlistIds: List[String]): IO[List[String]] =
    playlistIds.foldMapM(pId => getYouTubePlaylistIds(youtubeService, pId))

  private def getYouTubeChannelUploadsPlaylistId(youtubeService: YouTube, channelHandle: String): IO[String] =
    for {
      request  <- createYouTubeChannelUploadPlaylistRequest(youtubeService, channelHandle)
      response <- IO(request.execute())
      firstItem <- IO.fromOption(response.getItems().asScala.toList.headOption)(
        Throwable(s"[PlaygroundMain] $channelHandle can't find the upload youtube playlist")
      )
      uploadPlaylistId = firstItem.getContentDetails().getRelatedPlaylists().getUploads()
    } yield uploadPlaylistId

  def run(args: List[String]): IO[ExitCode] =
    for
      _              <- log.info("[PlaygroundMain] Start the PlaygroundMain")
      youtubeService <- createYouTube

      // Multi Video Response
      // youtubeVideoRequest  <- createYouTubeVideoRequest(youtubeService, List("JInL-_qi8eA", "_N8OJARj2Xo"))
      // _                    <- log.info(s"[PlaygroundMain] Execute YouTube video request: $youtubeVideoRequest")
      // youtubeVideoResponse <- IO(youtubeVideoRequest.execute())

      // Single Video Caption Response
      // youtubeVideoCaptionRequest  <- createYouTubeVideoCaptionRequest(youtubeService, "r0bbq-soSfI")
      // _                    <- log.info(s"[PlaygroundMain] Execute YouTube video caption request: $youtubeVideoCaptionRequest")
      // youtubeVideoCaptionResponse <- IO(youtubeVideoCaptionRequest.execute())
      // _                    <- log.info(s"[PlaygroundMain] YouTube video caption response: $youtubeVideoCaptionResponse")

      // Single Video Playlist Response
      // youtubeVideoPlaylistRequest  <- createYouTubePlaylistRequest(youtubeService, "UUXEJNKH9I4xsoyUNN3IL96A", None)
      // _                    <- log.info(s"[PlaygroundMain] Execute YouTube video playlist request: $youtubeVideoPlaylistRequest")
      // youtubeVideoPlaylistResponse <- IO(youtubeVideoPlaylistRequest.execute())
      // _                    <- log.info(s"[PlaygroundMain] YouTube video playlist response: $youtubeVideoPlaylistResponse")

      // Multi Channel Response
      // youtubeChannelRequest  <- createYouTubeChannelUploadPlaylistRequest(youtubeService, "xahlee")
      // _                      <- log.info(s"[PlaygroundMain] Execute YouTube channel request: $youtubeChannelRequest")
      // youtubeChannelResponse <- IO(youtubeChannelRequest.execute())
      // _                      <- log.info(s"[PlaygroundMain] YouTube video channel response: $youtubeChannelResponse")

      // Get all ids from a playlistId
      allChannelsUploadPlaylistIds <- channelHandles.traverse(getYouTubeChannelUploadsPlaylistId(youtubeService, _))
      allPlaylistIds <- getYouTubePlaylistsIds(youtubeService, playlistIds ++ allChannelsUploadPlaylistIds)
      _              <- log.info(s"[PlaygroundMain] $playlistIds all video Ids length: ${allPlaylistIds.length}")
    yield ExitCode.Success
}

/*
Multi Video Response
{
  "items": [
    {
      "contentDetails": {
        "duration": "PT17M55S"
      },
      "id": "JInL-_qi8eA",
      "snippet": {
        "description": "*DIVENTA UN MEMBRO* : https://youtube.com/@asbestomolesto/join\n#vlogger #vlogs  #faidate #diy #diycrafts #laboratorio #repair #repairing #righttorepair #restoration #amazing #hacker #hack #laboratorio #lab #electronic #science #asmr\n#travel #viaggio #viaggiare #travelvlog #musif #miai \n#rain #thunderstorm #lightning #pioggia\n\nDECIDO FINALMENTE che l'UPS che tiene su il mio router wifi ed il ponte radio deve durare 3 GIORNI anziche' 20 minuti. \nERGO, mi adopero per modificarlo ed installare una batteria da 12V 100A/h.\n\nDoveva essere un lavoretto semplice ma non sono contento se non mi complico la vita inutilmente... \n\n*OCCHIO al mio AMAZON SHOP! https://www.amazon.it/shop/asbestomolesto*\n\n*Le mie attrezzature le trovate nel mio negozietto AMAZON:*\nQueste sono tutte cose che USO GIORNALMENTE e di cui sono molto soddisfatto :) \n\n*TRASMETTITORE RADIO AM* : https://it.aliexpress.com/item/1005005593010774.html\n\n*SALDATORE RAPIDO USB* : https://amzn.to/3H87Zt9\n*NASTRO AUTOAGGLOMERANTE* : https://amzn.to/3KKJ912\n*Scheda SD CARD Veloce* : https://amzn.to/3WNSKLI\n*Saldatore* economico ma funzionale: https://amzn.to/43MSuxZ\n*Stagno* 60/40 OTTIMO *NON-RoHS*: https://amzn.to/4cBf290\n*Trecciola dissaldante: https://amzn.to/3PNsgWj*\n*Flussante* Amtech NC-559-V2-TF: https://amzn.to/43DPAvo\n*Pasta per saldature* elettroniche: https://amzn.to/43GAN39\n*Disossidante* per saldature: https://amzn.to/4axQagu\n\n*Tester digitale* UNI-T UT139C:  https://amzn.to/43HeKZX\n*Oscilloscopio portatile* : https://amzn.to/4acVQwx\n*Cassettiera portacomponenti* : https://amzn.to/4cwPObX\n*Masterizzatore M-DISC Blueray* : https://amzn.to/3IZ3VJj\n\n*Antenna MINI WHIP* : https://amzn.to/4aDnnHx\n*ALTRA ANTENNA MINI WHIP*: https://amzn.to/43NHReh\n*Ricevitore SDR*: https://amzn.to/3TLirJR\n\n*Display Voltmetro 0-30Vdc* : https://amzn.to/3xepanL\n*XHDATA D-808* radio onde corte AM/FM/Airband/ETC: https://amzn.to/43EutZS\n*Evaporust* : https://amzn.to/3xaNRBy\n*Caricabatterie solare* USB etc: https://amzn.to/43Cn39D\n*Rotelle per sedia* : https://amzn.to/49kSrKZ\n\n*Le mie attrezzature VEVOR ed i codici per acquistarle con sconti:*\nCODICE DI *SCONTO 5%* su tutti i prodotti VEVOR: VVSALE5\n\n*Termocamera infrarossi* VEVOR SC240N: https://s.vevor.com/bfQsoD\n*Pulitrice ad ultrasuoni* VEVOR da 10 litri: https://s.vevor.com/bfQgEk\n*Telecamera endoscopica* VEVOR 5 metri 3 telecamere: https://s.vevor.com/bfQbze\n\n*BRAKLEEN pulitore freni* : https://amzn.to/3VGjgX0\n*Cavo adattatore da OBD2 a OBD 3 pin* : https://amzn.to/43I4zUY\n*Cavo diagnostico OBD per PANDA 141* ed altre auto storiche: https://amzn.to/3xiD9co\n*Adattatore Da Attacco H4 HS1 A H5 R2 G40* : https://amzn.to/4cAByPf\n*Lampade Philips RacingVision GT200 H4* per Panda 141: https://amzn.to/3xipx0O\n*Imbottitura sedile panda 141 seduta* : https://amzn.to/4cAMLPT\n\n*CINEBASTO Riscaldatore DIESEL* 8KW VEVOR lo trovate qui: \nDALL' ITALIA site https://s.vevor.com/bfQHz0\nEUROPA site https://s.vevor.com/bfQEk2\nGERMANY site https://s.vevor.com/bfQHz9\nCODICE DI SCONTO VVS10 RISPARMIA 10€ !!!\n\nSe vi interessa il VEVOR, , guardate la serie completa!\nPARTE 1: https://www.youtube.com/watch?v=A9xgrcAC4_8\nPARTE 2: https://www.youtube.com/watch?v=mwhiqrgUQck\nPARTE 3: https://www.youtube.com/watch?v=LVSfAheBhYk\nPARTE 4: https://www.youtube.com/watch?v=9DlC6uSOrno\nPARTE 5: https://www.youtube.com/watch?v=hdMPFVHCf7w\nPARTE 6: https://www.youtube.com/watch?v=yjk9LYeo7dI\n\n_In qualità di Affiliato Amazon io ricevo un guadagno dagli acquisti idonei: aiuterete il canale a crescere!_",
        "publishedAt": "2025-05-26T11:00:36.000Z",
        "title": "MONTARE UNA MEGA BATTERIA PER UPS: SCAVARSI LA FOSSA DA SOLI 1/3 #powersupply #battery #powerstation"
      }
    },
    {
      "contentDetails": {
        "duration": "PT50M36S"
      },
      "id": "_N8OJARj2Xo",
      "liveStreamingDetails": {
        "actualEndTime": "2025-05-23T20:04:28.000Z",
        "actualStartTime": "2025-05-23T19:13:47.000Z",
        "scheduledStartTime": "2025-05-23T19:00:00.000Z"
      },
      "snippet": {
        "description": "Notes at\nhttp ://xahlee .info/talk_show/xah_talk_show_2025-05-23.html\nCopy the link, remove spaces, paste the link in browser tab.\nGoogle Chrome and Firefox browsers censor websites by auto adding s in http and says the site Secure Connection Failed.\nUse Brave Browser or Safari.",
        "publishedAt": "2025-05-24T08:11:16.000Z",
        "title": "Ep660 Wolfram Language how to do Animation"
      }
    }
  ]
}

Single Video Caption Response
{
  "items": [
    {
      "id": "AUieDaY2e3fBvCcxzcQkNyeaed0NlbnCjhZ4rDLsXcrq8w5wqvE",
      "snippet": {
        "language": "en",
        "trackKind": "asr"
      }
    },
    {
      "id": "AUieDaZxUxVMVJod6duptflHB8VReXpcE3k2iaaEMhhN",
      "snippet": {
        "language": "en",
        "trackKind": "standard"
      }
    }
  ]
}

Single Playlist Video Ids
{
  "items": [
    {
      "contentDetails": {
        "videoId": "ADACFpS1qJo"
      }
    },
    {
      "contentDetails": {
        "videoId": "x0cvonF0GYU"
      }
    },
    {
      "contentDetails": {
        "videoId": "m3eF0adEZe0"
      }
    },
    {
      "contentDetails": {
        "videoId": "EHshUGJcBaM"
      }
    },
    ...
  ],
  "nextPageToken": "EAAaHlBUOkNESWlFRFl3UmtaRk1ETkJRa1l3UXpsR056VQ"
 }

 Channel Uploads Playlist
 {
  "items": [
    {
      "contentDetails": {
        "relatedPlaylists": {
          "uploads": "UUXEJNKH9I4xsoyUNN3IL96A"
        }
      }
    }
  ]
}
 */
