package com.benkio.telegrambotinfrastructure.web

import cats.effect.IO
import com.benkio.telegrambotinfrastructure.resources.db.DBShowData
import io.circe.parser.*
import io.circe.Json
import log.effect.fs2.SyncLogWriter.consoleLogUpToLevel
import log.effect.LogLevels
import log.effect.LogWriter
import munit.*

class JsonParserSpec extends CatsEffectSuite {

  given log: LogWriter[IO] = consoleLogUpToLevel(LogLevels.Info)

  test("JsonParser.Ytdlp.parseYtdlp should parse correctly the input playlist") {
    val actual = JsonParser.Ytdlp.parseYtdlp[IO](JsonParserSpec.YtdlpData.youtubePlaylist, "testBot")
    val expected = List(
      DBShowData(
        show_url = "https://www.youtube.com/watch?v=2AOz2Zfr6LU",
        bot_name = "testBot",
        show_title = "Richard Benson | Quelli della Notte | Il metallaro di scuola americana (10 maggio 1985)",
        show_upload_date = "20200123",
        show_duration = 192,
        show_description = Some(
          value = """#RichardBenson #QuelliDellaNotte #RenzoArbore
                    |
                    |NUOVO GRUPPO E CANALE TELEGRAM:
                    |
                    |GRUPPO: https://t.me/bbensonreloaded
                    |
                    |CANALE:https://t.me/simposiodelmedallo""".stripMargin
        ),
        show_is_live = false,
        show_origin_automatic_caption = Some(
          value = "https://www.youtube.com/api/timedtext?ext=json3"
        )
      ),
      DBShowData(
        show_url = "https://www.youtube.com/watch?v=_YuVTacI2FA",
        bot_name = "testBot",
        show_title = "Richard Benson | Quelli della Notte | Conan Il Barbaro (17 maggio 1985)",
        show_upload_date = "20200123",
        show_duration = 114,
        show_description = Some(
          value = """Video integro: https://www.facebook.com/archiviobenson/videos/513055709331468/
                    |#RichardBenson #QuelliDellaNotte #RenzoArbore
                    |
                    |NUOVO GRUPPO E CANALE TELEGRAM:
                    |
                    |GRUPPO: https://t.me/bbensonreloaded
                    |
                    |CANALE:https://t.me/simposiodelmedallo""".stripMargin
        ),
        show_is_live = false,
        show_origin_automatic_caption = Some(
          value = "https://www.youtube.com/api/timedtext?ext=json3"
        )
      )
    )
    assertIO(actual, expected)
  }

  test("JsonParser.Ytdlp.parseYtdlp should parse correctly the input channel") {
    val actual = JsonParser.Ytdlp.parseYtdlp[IO](JsonParserSpec.YtdlpData.youtubeChannel, "testBot")
    val expected = List(
      DBShowData(
        show_url = "https://www.youtube.com/watch?v=CSxJQeWFDek",
        bot_name = "testBot",
        show_title = "Sandro Rojas - SOLA (feat Brown) Video Oficial 4K",
        show_upload_date = "20241212",
        show_duration = 166,
        show_description = Some(
          value = "DESCRIPTION SANDRO 2"
        ),
        show_is_live = false,
        show_origin_automatic_caption = Some(
          value = "https://www.youtube.com/api/timedtext?ext=json3"
        )
      ),
      DBShowData(
        show_url = "https://www.youtube.com/watch?v=ttpdmcwNsK8",
        bot_name = "testBot",
        show_title = "Sandro Rojas - TE COMPLICAS\u2764\ufe0f\u200d\ud83e\ude79 (Video Oficial)",
        show_upload_date = "20231005",
        show_duration = 190,
        show_description = Some(
          value = "DESCRIPTION SANDRO 3"
        ),
        show_is_live = false,
        show_origin_automatic_caption = Some(
          value = "https://www.youtube.com/api/timedtext?ext=json3"
        )
      )
    )
    assertIO(actual, expected)
  }

  test("JsonParser.Ytdlp.parseYtdlp should parse correctly the input channel streams") {
    val actual = JsonParser.Ytdlp.parseYtdlp[IO](JsonParserSpec.YtdlpData.youtubeStreams, "testBot")
    val expected = List(
      DBShowData(
        show_url = "https://www.youtube.com/watch?v=NvdYeg6lMcU",
        bot_name = "testBot",
        show_title = "40 Min HIIT Workout",
        show_upload_date = "20180228",
        show_duration = 2529,
        show_description = Some(
          value = ""
        ),
        show_is_live = false,
        show_origin_automatic_caption = Some(value = "https://youtube.com/api/timedtext?ext=json3")
      ),
      DBShowData(
        show_url = "https://www.youtube.com/watch?v=TvKNvWiIigw",
        bot_name = "testBot",
        show_title = "Full Body Workout with Dumbbells",
        show_upload_date = "20180224",
        show_duration = 1973,
        show_description = Some(
          value = ""
        ),
        show_is_live = false,
        show_origin_automatic_caption = Some(value = "https://www.youtube.com/api/timedtext?ext=json3")
      ),
      DBShowData(
        show_url = "https://www.youtube.com/watch?v=_iFc_5iUzi8",
        bot_name = "testBot",
        show_title = "HIIT Workout",
        show_upload_date = "20180221",
        show_duration = 236,
        show_description = Some(
          value = ""
        ),
        show_is_live = false,
        show_origin_automatic_caption = Some(value = "https://www.youtube.com/api/timedtext?ext=json3")
      ),
      DBShowData(
        show_url = "https://www.youtube.com/watch?v=PSF77PGC6Jg",
        bot_name = "testBot",
        show_title = "HIIT Workout",
        show_upload_date = "20180221",
        show_duration = 1787,
        show_description = Some(
          value = ""
        ),
        show_is_live = false,
        show_origin_automatic_caption = Some(value = "https://youtube.com/api/timedtext?ext=json3")
      )
    )
    assertIO(actual, expected)
  }

  test("JsonParser.Invidious.parseInvidiousVideoIds should return all the videoIds in a valid json response") {
    val expectedVideoId: List[String] = List(
      "abc123xyz",
      "live456abc",
      "ultra8k999"
    )
    val actual = JsonParser.Invidious.parseInvidiousVideoIds(JsonParserSpec.Invidious.videosWithContinuation)
    assertEquals(actual, (expectedVideoId, Some(JsonParserSpec.Invidious.continuationValue)))
  }
}

object JsonParserSpec:

  object YtdlpData {
    val youtubeChannel: Json =
      parse(
        """{ "id": "UCbdVsLFphtk2UWuJC-gcXoQ", "channel": "Sandro Rojas", "channel_id": "UCbdVsLFphtk2UWuJC-gcXoQ", "title": "Sandro Rojas - Videos", "availability": null, "channel_follower_count": 81, "description": "DESCRIPTION SANDRO 1", "tags": [], "thumbnails": [], "uploader_id": "@sandrorojas3618", "uploader_url": "https://www.youtube.com/@sandrorojas3618", "modified_date": null, "view_count": null, "playlist_count": 2, "uploader": "Sandro Rojas", "channel_url": "https://www.youtube.com/channel/UCbdVsLFphtk2UWuJC-gcXoQ", "_type": "playlist", "entries": [ { "id": "CSxJQeWFDek", "title": "Sandro Rojas - SOLA (feat Brown) Video Oficial 4K", "formats": [], "thumbnails": [], "thumbnail": "https://i.ytimg.com/vi/CSxJQeWFDek/maxresdefault.jpg", "description": "DESCRIPTION SANDRO 2", "channel_id": "UCbdVsLFphtk2UWuJC-gcXoQ", "channel_url": "https://www.youtube.com/channel/UCbdVsLFphtk2UWuJC-gcXoQ", "duration": 166, "view_count": 617, "average_rating": null, "age_limit": 0, "webpage_url": "https://www.youtube.com/watch?v=CSxJQeWFDek", "categories": [], "tags": [], "playable_in_embed": true, "live_status": "not_live", "media_type": null, "release_timestamp": 1734030006, "_format_sort_fields": [], "automatic_captions": { "en": [ { "ext": "json3", "url": "https://www.youtube.com/api/timedtext?ext=json3", "name": "English" }, { "ext": "srv1", "url": "https://www.youtube.com/api/timedtext?fmt=srv1", "name": "English" }, { "ext": "srv2", "url": "https://www.youtube.com/api/timedtext?fmt=srv2", "name": "English" }, { "ext": "srv3", "url": "https://www.youtube.com/api/timedtext?fmt=srv3", "name": "English" }, { "ext": "ttml", "url": "https://www.youtube.com/api/timedtext?fmt=ttml", "name": "English" }, { "ext": "vtt", "url": "https://www.youtube.com/api/timedtext?fmt=vtt", "name": "English" } ], "it": [ { "ext": "json3", "url": "https://www.youtube.com/api/timedtext?ext=json3", "name": "Italian" }, { "ext": "srv1", "url": "https://www.youtube.com/api/timedtext?fmt=srv1", "name": "Italian" }, { "ext": "srv2", "url": "https://www.youtube.com/api/timedtext?fmt=srv2", "name": "Italian" }, { "ext": "srv3", "url": "https://www.youtube.com/api/timedtext?fmt=srv3", "name": "Italian" }, { "ext": "ttml", "url": "https://www.youtube.com/api/timedtext?fmt=ttml", "name": "Italian" }, { "ext": "vtt", "url": "https://www.youtube.com/api/timedtext?fmt=vtt", "name": "Italian" } ], "es-orig": [ { "ext": "json3", "url": "https://www.youtube.com/api/timedtext?ext=json3", "name": "Spanish (Original)" }, { "ext": "srv1", "url": "https://www.youtube.com/api/timedtext?fmt=srv1", "name": "Spanish (Original)" }, { "ext": "srv2", "url": "https://www.youtube.com/api/timedtext?fmt=srv2", "name": "Spanish (Original)" }, { "ext": "srv3", "url": "https://www.youtube.com/api/timedtext?fmt=srv3", "name": "Spanish (Original)" }, { "ext": "ttml", "url": "https://www.youtube.com/api/timedtext?fmt=ttml", "name": "Spanish (Original)" }, { "ext": "vtt", "url": "https://www.youtube.com/api/timedtext?fmt=vtt", "name": "Spanish (Original)" } ] }, "subtitles": {}, "comment_count": 11, "chapters": null, "heatmap": null, "like_count": 37, "channel": "Sandro Rojas", "channel_follower_count": 81, "uploader": "Sandro Rojas", "uploader_id": "@sandrorojas3618", "uploader_url": "https://www.youtube.com/@sandrorojas3618", "upload_date": "20241212", "timestamp": 1734030006, "availability": "public", "original_url": "https://www.youtube.com/watch?v=CSxJQeWFDek", "webpage_url_basename": "watch", "webpage_url_domain": "youtube.com", "extractor": "youtube", "extractor_key": "Youtube", "playlist_count": 2, "playlist": "Sandro Rojas - Videos", "playlist_id": "UCbdVsLFphtk2UWuJC-gcXoQ", "playlist_title": "Sandro Rojas - Videos", "playlist_uploader": "Sandro Rojas", "playlist_uploader_id": "@sandrorojas3618", "playlist_channel": "Sandro Rojas", "playlist_channel_id": "UCbdVsLFphtk2UWuJC-gcXoQ", "playlist_webpage_url": "https://www.youtube.com/@sandrorojas3618/videos", "n_entries": 2, "playlist_index": 1, "__last_playlist_index": 2, "playlist_autonumber": 1, "display_id": "CSxJQeWFDek", "fulltitle": "Sandro Rojas - SOLA (feat Brown) Video Oficial 4K", "duration_string": "2:46", "release_date": "20241212", "release_year": 2024, "is_live": false, "was_live": false, "requested_subtitles": null, "_has_drm": null, "epoch": 1738855795, "requested_downloads": [], "requested_formats": [], "format": "248 - 1920x1080 (1080p, TV)+251 - audio only (medium, TV)", "format_id": "248+251", "ext": "webm", "protocol": "https+https", "language": "es", "format_note": "1080p, TV+medium, TV", "filesize_approx": 43218394, "tbr": 2082.7799999999997, "width": 1920, "height": 1080, "resolution": "1920x1080", "fps": 25, "dynamic_range": "SDR", "vcodec": "vp9", "vbr": 1946.551, "stretched_ratio": null, "aspect_ratio": 1.78, "acodec": "opus", "abr": 136.229, "asr": 48000, "audio_channels": 2 }, { "id": "ttpdmcwNsK8", "title": "Sandro Rojas - TE COMPLICAS❤️‍🩹 (Video Oficial)", "formats": [], "thumbnails": [], "thumbnail": "https://i.ytimg.com/vi/ttpdmcwNsK8/sddefault.jpg", "description": "DESCRIPTION SANDRO 3", "channel_id": "UCbdVsLFphtk2UWuJC-gcXoQ", "channel_url": "https://www.youtube.com/channel/UCbdVsLFphtk2UWuJC-gcXoQ", "duration": 190, "view_count": 2355, "average_rating": null, "age_limit": 0, "webpage_url": "https://www.youtube.com/watch?v=ttpdmcwNsK8", "categories": [], "tags": [], "playable_in_embed": true, "live_status": "not_live", "media_type": null, "release_timestamp": null, "_format_sort_fields": [], "automatic_captions": { "it": [ { "ext": "json3", "url": "https://www.youtube.com/api/timedtext?ext=json3", "name": "Italian" }, { "ext": "srv1", "url": "https://www.youtube.com/api/timedtext?fmt=srv1", "name": "Italian" }, { "ext": "srv2", "url": "https://www.youtube.com/api/timedtext?fmt=srv2", "name": "Italian" }, { "ext": "srv3", "url": "https://www.youtube.com/api/timedtext?fmt=srv3", "name": "Italian" }, { "ext": "ttml", "url": "https://www.youtube.com/api/timedtext?fmt=ttml", "name": "Italian" }, { "ext": "vtt", "url": "https://www.youtube.com/api/timedtext?fmt=vtt", "name": "Italian" } ], "es-orig": [ { "ext": "json3", "url": "https://www.youtube.com/api/timedtext?ext=json3", "name": "Spanish (Original)" }, { "ext": "srv1", "url": "https://www.youtube.com/api/timedtext?fmt=srv1", "name": "Spanish (Original)" }, { "ext": "srv2", "url": "https://www.youtube.com/api/timedtext?fmt=srv2", "name": "Spanish (Original)" }, { "ext": "srv3", "url": "https://www.youtube.com/api/timedtext?fmt=srv3", "name": "Spanish (Original)" }, { "ext": "ttml", "url": "https://www.youtube.com/api/timedtext?fmt=ttml", "name": "Spanish (Original)" }, { "ext": "vtt", "url": "https://www.youtube.com/api/timedtext?fmt=vtt", "name": "Spanish (Original)" } ] }, "subtitles": {}, "comment_count": 6, "chapters": null, "heatmap": null, "location": "RIMINI", "like_count": 54, "channel": "Sandro Rojas", "channel_follower_count": 81, "uploader": "Sandro Rojas", "uploader_id": "@sandrorojas3618", "uploader_url": "https://www.youtube.com/@sandrorojas3618", "upload_date": "20231005", "timestamp": 1696514712, "availability": "public", "original_url": "https://www.youtube.com/watch?v=ttpdmcwNsK8", "webpage_url_basename": "watch", "webpage_url_domain": "youtube.com", "extractor": "youtube", "extractor_key": "Youtube", "playlist_count": 2, "playlist": "Sandro Rojas - Videos", "playlist_id": "UCbdVsLFphtk2UWuJC-gcXoQ", "playlist_title": "Sandro Rojas - Videos", "playlist_uploader": "Sandro Rojas", "playlist_uploader_id": "@sandrorojas3618", "playlist_channel": "Sandro Rojas", "playlist_channel_id": "UCbdVsLFphtk2UWuJC-gcXoQ", "playlist_webpage_url": "https://www.youtube.com/@sandrorojas3618/videos", "n_entries": 2, "playlist_index": 2, "__last_playlist_index": 2, "playlist_autonumber": 2, "display_id": "ttpdmcwNsK8", "fulltitle": "Sandro Rojas - TE COMPLICAS❤️‍🩹 (Video Oficial)", "duration_string": "3:10", "release_year": null, "is_live": false, "was_live": false, "requested_subtitles": null, "_has_drm": null, "epoch": 1738855798, "requested_downloads": [], "requested_formats": [], "format": "137 - 1920x1080 (1080p, TV)+251 - audio only (medium, TV)", "format_id": "137+251", "ext": "mkv", "protocol": "https+https", "language": "es", "format_note": "1080p, TV+medium, TV", "filesize_approx": 93881274, "tbr": 3952.9, "width": 1920, "height": 1080, "resolution": "1920x1080", "fps": 30, "dynamic_range": "SDR", "vcodec": "avc1.640028", "vbr": 3818.606, "stretched_ratio": null, "aspect_ratio": 1.78, "acodec": "opus", "abr": 134.294, "asr": 48000, "audio_channels": 2 } ], "extractor_key": "YoutubeTab", "extractor": "youtube:tab", "webpage_url": "https://www.youtube.com/@sandrorojas3618/videos", "original_url": "https://www.youtube.com/@sandrorojas3618", "webpage_url_basename": "videos", "webpage_url_domain": "youtube.com", "release_year": null, "epoch": 1738855798, "__files_to_move": {}, "_version": { "version": "2025.01.26", "current_git_head": null, "release_git_head": "3b4531934465580be22937fecbb6e1a3a9e2334f", "repository": "yt-dlp/yt-dlp" } }"""
      )
        .getOrElse(Json.Null)

    val youtubePlaylist: Json =
      parse(
        """{ "id": "PL1hlX04-g75DGniSXtYRSlMBaroamq96d", "title": "Richard Benson a Quelli della Notte (1985)", "availability": null, "channel_follower_count": null, "description": "", "tags": [], "thumbnails": [], "uploader_url": "https://www.youtube.com/@BrigateBenson", "_type": "playlist", "entries": [ { "id": "2AOz2Zfr6LU", "title": "Richard Benson | Quelli della Notte | Il metallaro di scuola americana (10 maggio 1985)", "formats": [], "thumbnails": [], "thumbnail": "https://i.ytimg.com/vi/2AOz2Zfr6LU/hqdefault.jpg", "description": "#RichardBenson #QuelliDellaNotte #RenzoArbore\n\nNUOVO GRUPPO E CANALE TELEGRAM:\n\nGRUPPO: https://t.me/bbensonreloaded\n\nCANALE:https://t.me/simposiodelmedallo", "channel_id": "UCyWnTfiZBuHXg_yIZl0u01g", "channel_url": "https://www.youtube.com/channel/UCyWnTfiZBuHXg_yIZl0u01g", "duration": 192, "view_count": 10451, "average_rating": null, "age_limit": 0, "webpage_url": "https://www.youtube.com/watch?v=2AOz2Zfr6LU", "categories": [], "tags": [], "playable_in_embed": false, "live_status": "not_live", "media_type": null, "release_timestamp": null, "_format_sort_fields": [], "automatic_captions": { "en": [ { "ext": "json3", "url": "https://www.youtube.com/api/timedtext?ext=json3", "name": "English" }, { "ext": "srv1", "url": "https://www.youtube.com/api/timedtext?fmt=srv1", "name": "English" }, { "ext": "srv2", "url": "https://www.youtube.com/api/timedtext?fmt=srv2", "name": "English" }, { "ext": "srv3", "url": "https://www.youtube.com/api/timedtext?fmt=srv3", "name": "English" }, { "ext": "ttml", "url": "https://www.youtube.com/api/timedtext?fmt=ttml", "name": "English" }, { "ext": "vtt", "url": "https://www.youtube.com/api/timedtext?fmt=vtt", "name": "English" } ], "it-orig": [ { "ext": "json3", "url": "https://www.youtube.com/api/timedtext?ext=json3", "name": "Italian (Original)" }, { "ext": "srv1", "url": "https://www.youtube.com/api/timedtext?fmt=srv1", "name": "Italian (Original)" }, { "ext": "srv2", "url": "https://www.youtube.com/api/timedtext?fmt=srv2", "name": "Italian (Original)" }, { "ext": "srv3", "url": "https://www.youtube.com/api/timedtext?fmt=srv3", "name": "Italian (Original)" }, { "ext": "ttml", "url": "https://www.youtube.com/api/timedtext?fmt=ttml", "name": "Italian (Original)" }, { "ext": "vtt", "url": "https://www.youtube.com/api/timedtext?fmt=vtt", "name": "Italian (Original)" } ], "it": [ { "ext": "json3", "url": "https://www.youtube.com/api/timedtext?ext=json3", "name": "Italian" }, { "ext": "srv1", "url": "https://www.youtube.com/api/timedtext?fmt=srv1", "name": "Italian" }, { "ext": "srv2", "url": "https://www.youtube.com/api/timedtext?fmt=srv2", "name": "Italian" }, { "ext": "srv3", "url": "https://www.youtube.com/api/timedtext?fmt=srv3", "name": "Italian" }, { "ext": "ttml", "url": "https://www.youtube.com/api/timedtext?fmt=ttml", "name": "Italian" }, { "ext": "vtt", "url": "https://www.youtube.com/api/timedtext?fmt=vtt", "name": "Italian" } ] }, "subtitles": {}, "comment_count": 44, "chapters": null, "heatmap": null, "like_count": 374, "channel": "Brigate Benson", "channel_follower_count": 8260, "uploader": "Brigate Benson", "uploader_id": "@BrigateBenson", "uploader_url": "https://www.youtube.com/@BrigateBenson", "upload_date": "20200123", "timestamp": 1579821496, "availability": "public", "original_url": "https://www.youtube.com/watch?v=2AOz2Zfr6LU", "webpage_url_basename": "watch", "webpage_url_domain": "youtube.com", "extractor": "youtube", "extractor_key": "Youtube", "playlist_count": 3, "playlist": "Richard Benson a Quelli della Notte (1985)", "playlist_id": "PL1hlX04-g75DGniSXtYRSlMBaroamq96d", "playlist_title": "Richard Benson a Quelli della Notte (1985)", "playlist_uploader": "Brigate Benson", "playlist_uploader_id": "@BrigateBenson", "playlist_channel": "Brigate Benson", "playlist_channel_id": "UCyWnTfiZBuHXg_yIZl0u01g", "playlist_webpage_url": "https://www.youtube.com/playlist?list=PL1hlX04-g75DGniSXtYRSlMBaroamq96d", "n_entries": 3, "playlist_index": 1, "__last_playlist_index": 3, "playlist_autonumber": 1, "display_id": "2AOz2Zfr6LU", "fulltitle": "Richard Benson | Quelli della Notte | Il metallaro di scuola americana (10 maggio 1985)", "duration_string": "3:12", "release_year": null, "is_live": false, "was_live": false, "requested_subtitles": null, "_has_drm": null, "epoch": 1738842921, "requested_downloads": [], "requested_formats": [], "format": "135 - 640x480 (480p, TV)+251 - audio only (medium, TV)", "format_id": "135+251", "ext": "mkv", "protocol": "https+https", "language": "it", "format_note": "480p, TV+medium, TV", "filesize_approx": 14861041, "tbr": 619.453, "width": 640, "height": 480, "resolution": "640x480", "fps": 25, "dynamic_range": "SDR", "vcodec": "avc1.4d401e", "vbr": 487.436, "stretched_ratio": null, "aspect_ratio": 1.33, "acodec": "opus", "abr": 132.017, "asr": 48000, "audio_channels": 2 }, { "id": "_YuVTacI2FA", "title": "Richard Benson | Quelli della Notte | Conan Il Barbaro (17 maggio 1985)", "formats": [], "thumbnails": [], "thumbnail": "https://i.ytimg.com/vi/_YuVTacI2FA/sddefault.jpg", "description": "Video integro: https://www.facebook.com/archiviobenson/videos/513055709331468/\n#RichardBenson #QuelliDellaNotte #RenzoArbore\n\nNUOVO GRUPPO E CANALE TELEGRAM:\n\nGRUPPO: https://t.me/bbensonreloaded\n\nCANALE:https://t.me/simposiodelmedallo", "channel_id": "UCyWnTfiZBuHXg_yIZl0u01g", "channel_url": "https://www.youtube.com/channel/UCyWnTfiZBuHXg_yIZl0u01g", "duration": 114, "view_count": 9116, "average_rating": null, "age_limit": 0, "webpage_url": "https://www.youtube.com/watch?v=_YuVTacI2FA", "categories": [], "tags": [], "playable_in_embed": false, "live_status": "not_live", "media_type": null, "release_timestamp": null, "_format_sort_fields": [], "automatic_captions": { "en": [ { "ext": "json3", "url": "https://www.youtube.com/api/timedtext?ext=json3", "name": "English" }, { "ext": "srv1", "url": "https://www.youtube.com/api/timedtext?fmt=srv1", "name": "English" }, { "ext": "srv2", "url": "https://www.youtube.com/api/timedtext?fmt=srv2", "name": "English" }, { "ext": "srv3", "url": "https://www.youtube.com/api/timedtext?fmt=srv3", "name": "English" }, { "ext": "ttml", "url": "https://www.youtube.com/api/timedtext?fmt=ttml", "name": "English" }, { "ext": "vtt", "url": "https://www.youtube.com/api/timedtext?fmt=vtt", "name": "English" } ], "it-orig": [ { "ext": "json3", "url": "https://www.youtube.com/api/timedtext?ext=json3", "name": "Italian (Original)" }, { "ext": "srv1", "url": "https://www.youtube.com/api/timedtext?fmt=srv1", "name": "Italian (Original)" }, { "ext": "srv2", "url": "https://www.youtube.com/api/timedtext?fmt=srv2", "name": "Italian (Original)" }, { "ext": "srv3", "url": "https://www.youtube.com/api/timedtext?fmt=srv3", "name": "Italian (Original)" }, { "ext": "ttml", "url": "https://www.youtube.com/api/timedtext?fmt=ttml", "name": "Italian (Original)" }, { "ext": "vtt", "url": "https://www.youtube.com/api/timedtext?fmt=vtt", "name": "Italian (Original)" } ], "it": [ { "ext": "json3", "url": "https://www.youtube.com/api/timedtext?ext=json3", "name": "Italian" }, { "ext": "srv1", "url": "https://www.youtube.com/api/timedtext?fmt=srv1", "name": "Italian" }, { "ext": "srv2", "url": "https://www.youtube.com/api/timedtext?fmt=srv2", "name": "Italian" }, { "ext": "srv3", "url": "https://www.youtube.com/api/timedtext?fmt=srv3", "name": "Italian" }, { "ext": "ttml", "url": "https://www.youtube.com/api/timedtext?fmt=ttml", "name": "Italian" }, { "ext": "vtt", "url": "https://www.youtube.com/api/timedtext?fmt=vtt", "name": "Italian" } ] }, "subtitles": {}, "comment_count": 32, "chapters": null, "heatmap": null, "like_count": 204, "channel": "Brigate Benson", "channel_follower_count": 8260, "uploader": "Brigate Benson", "uploader_id": "@BrigateBenson", "uploader_url": "https://www.youtube.com/@BrigateBenson", "upload_date": "20200123", "timestamp": 1579819580, "availability": "public", "original_url": "https://www.youtube.com/watch?v=_YuVTacI2FA", "n_entries": 3, "playlist_index": 2, "__last_playlist_index": 3, "playlist_autonumber": 2, "display_id": "_YuVTacI2FA", "fulltitle": "Richard Benson | Quelli della Notte | Conan Il Barbaro (17 maggio 1985)", "duration_string": "1:54", "release_year": null, "is_live": false, "was_live": false } ], "extractor_key": "YoutubeTab", "extractor": "youtube:tab", "webpage_url": "https://www.youtube.com/playlist?list=PL1hlX04-g75DGniSXtYRSlMBaroamq96d", "original_url": "https://www.youtube.com/playlist?list=PL1hlX04-g75DGniSXtYRSlMBaroamq96d", "webpage_url_basename": "playlist", "webpage_url_domain": "youtube.com", "release_year": null, "epoch": 1738842926, "__files_to_move": {}, "_version": { "version": "2025.01.26", "current_git_head": null, "release_git_head": "3b4531934465580be22937fecbb6e1a3a9e2334f", "repository": "yt-dlp/yt-dlp" } }"""
      )
        .getOrElse(Json.Null)

    val youtubeStreams =
      parse(
        """{ "id": "UCVQJZE...", "channel": "Sydney Cummings", "channel_id": "UCVQJZE...", "title": "Sydney Cummings - Live", "availability": null, "channel_follower_count": 1640000, "description": "", "tags": [], "thumbnails": [], "uploader_id": "@sydneycummings", "uploader_url": "https://youtube.com/@sydneycummings", "modified_date": null, "view_count": null, "playlist_count": 4, "uploader": "Sydney Cummings", "channel_url": "https://youtube.com/channel/UCVQJZE...", "_type": "playlist", "entries": [ { "id": "NvdYeg6lMcU", "title": "40 Min HIIT Workout", "description": "", "duration": 2529, "upload_date": "20180228", "is_live": false, "automatic_captions": { "es-orig": [ { "ext": "json3", "url": "https://youtube.com/api/timedtext?ext=json3", "name": "Spanish (Original)" } ] }, "channel_id": "UCVQJZE...", "channel_url": "https://youtube.com/channel/UCVQJZE...", "view_count": 11058, "webpage_url": "https://youtube.com/watch?v=NvdYeg6lMcU", "tags": [], "playable_in_embed": true, "live_status": "was_live", "media_type": "livestream", "release_timestamp": 1519779629, "comment_count": 27, "like_count": 202, "channel": "Sydney Cummings", "channel_follower_count": 1640000, "channel_is_verified": true, "uploader": "Sydney Cummings", "uploader_id": "@sydneycummings", "uploader_url": "https://youtube.com/@sydneycummings", "timestamp": 1519784321, "availability": "public", "extractor": "youtube", "playlist": "Sydney Cummings - Live", "playlist_id": "UCVQJZE...", "playlist_title": "Sydney Cummings - Live", "playlist_uploader": "Sydney Cummings", "playlist_uploader_id": "@sydneycummings", "playlist_channel": "Sydney Cummings", "playlist_channel_id": "UCVQJZE...", "playlist_webpage_url": "https://youtube.com/@sydneycummings/streams", "n_entries": 4, "playlist_index": 1, "format": "1080p+audio", "format_id": "137+140", "ext": "mp4", "protocol": "https", "language": "es", "format_note": "1080p+medium", "filesize_approx": 496669842, "tbr": 1571.24, "width": 1920, "height": 1080, "resolution": "1920x1080", "fps": 30, "dynamic_range": "SDR", "vcodec": "avc1", "vbr": 1444.179, "aspect_ratio": 1.78, "acodec": "mp4a", "abr": 127.061, "asr": 44100, "audio_channels": 2 }, { "id": "TvKNvWiIigw", "title": "Full Body Workout with Dumbbells", "description": "", "duration": 1973, "upload_date": "20180224", "is_live": false, "automatic_captions": { "en-orig": [ { "ext": "json3", "url": "https://www.youtube.com/api/timedtext?ext=json3", "name": "English (Original)" } ] }, "channel_id": "UCVQJZE...", "channel_url": "https://youtube.com/channel/UCVQJZE...", "view_count": 6264, "webpage_url": "https://youtube.com/watch?v=TvKNvWiIigw", "tags": [], "playable_in_embed": true, "live_status": "was_live", "media_type": "livestream", "release_timestamp": 1519480786, "comment_count": 33, "like_count": 206, "channel": "Sydney Cummings", "channel_follower_count": 1640000, "channel_is_verified": true, "uploader": "Sydney Cummings", "uploader_id": "@sydneycummings", "uploader_url": "https://youtube.com/@sydneycummings", "timestamp": 1519484830, "availability": "public", "extractor": "youtube", "playlist": "Sydney Cummings - Live", "playlist_id": "UCVQJZE...", "playlist_title": "Sydney Cummings - Live", "playlist_uploader": "Sydney Cummings", "playlist_uploader_id": "@sydneycummings", "playlist_channel": "Sydney Cummings", "playlist_channel_id": "UCVQJZE...", "playlist_webpage_url": "https://youtube.com/@sydneycummings/streams", "n_entries": 4, "playlist_index": 2, "format": "1080p+audio", "format_id": "137+140", "ext": "mp4", "protocol": "https", "language": "en", "format_note": "1080p+medium", "filesize_approx": 440289200, "tbr": 1785.674, "width": 1920, "height": 1080, "resolution": "1920x1080", "fps": 30, "dynamic_range": "SDR", "vcodec": "avc1", "vbr": 1658.611, "aspect_ratio": 1.78, "acodec": "mp4a", "abr": 127.063, "asr": 44100, "audio_channels": 2 }, { "id": "_iFc_5iUzi8", "title": "HIIT Workout", "description": "", "duration": 236, "upload_date": "20180221", "is_live": false, "automatic_captions": { "en-orig": [ { "ext": "json3", "url": "https://www.youtube.com/api/timedtext?ext=json3", "name": "English (Original)" } ] }, "channel_id": "UCVQJZE...", "channel_url": "https://youtube.com/channel/UCVQJZE...", "view_count": 2427, "webpage_url": "https://youtube.com/watch?v=_iFc_5iUzi8", "tags": [], "playable_in_embed": true, "live_status": "was_live", "media_type": "livestream", "release_timestamp": 1519176674, "comment_count": 1, "like_count": 37, "channel": "Sydney Cummings", "channel_follower_count": 1640000, "channel_is_verified": true, "uploader": "Sydney Cummings", "uploader_id": "@sydneycummings", "uploader_url": "https://youtube.com/@sydneycummings", "timestamp": 1519178943, "availability": "public", "extractor": "youtube", "playlist": "Sydney Cummings - Live", "playlist_id": "UCVQJZE...", "playlist_title": "Sydney Cummings - Live", "playlist_uploader": "Sydney Cummings", "playlist_uploader_id": "@sydneycummings", "playlist_channel": "Sydney Cummings", "playlist_channel_id": "UCVQJZE...", "playlist_webpage_url": "https://youtube.com/@sydneycummings/streams", "n_entries": 4, "playlist_index": 3, "format": "1080p+audio", "format_id": "137+140", "ext": "mp4", "protocol": "https", "language": "en", "format_note": "1080p+medium", "filesize_approx": 39205841, "tbr": 1331.422, "width": 1920, "height": 1080, "resolution": "1920x1080", "fps": 30, "dynamic_range": "SDR", "vcodec": "avc1", "vbr": 1204.34, "aspect_ratio": 1.78, "acodec": "mp4a", "abr": 127.082, "asr": 44100, "audio_channels": 2 }, { "id": "PSF77PGC6Jg", "title": "HIIT Workout", "description": "", "duration": 1787, "upload_date": "20180221", "is_live": false, "automatic_captions": { "en-orig": [ { "ext": "json3", "url": "https://youtube.com/api/timedtext?ext=json3", "name": "English (Original)" } ] }, "channel_id": "UCVQJZE...", "channel_url": "https://youtube.com/channel/UCVQJZE...", "view_count": 6691, "webpage_url": "https://youtube.com/watch?v=PSF77PGC6Jg", "tags": [], "playable_in_embed": true, "live_status": "was_live", "media_type": "livestream", "release_timestamp": 1519174589, "comment_count": 18, "like_count": 138, "channel": "Sydney Cummings", "channel_follower_count": 1640000, "channel_is_verified": true, "uploader": "Sydney Cummings", "uploader_id": "@sydneycummings", "uploader_url": "https://youtube.com/@sydneycummings", "timestamp": 1519177006, "availability": "public", "extractor": "youtube", "playlist": "Sydney Cummings - Live", "playlist_id": "UCVQJZE...", "playlist_title": "Sydney Cummings - Live", "playlist_uploader": "Sydney Cummings", "playlist_uploader_id": "@sydneycummings", "playlist_channel": "Sydney Cummings", "playlist_channel_id": "UCVQJZE...", "playlist_webpage_url": "https://youtube.com/@sydneycummings/streams", "n_entries": 4, "playlist_index": 4, "format": "1080p+audio", "format_id": "137+140", "ext": "mp4", "protocol": "https", "language": "en", "format_note": "1080p+medium", "filesize_approx": 331416574, "tbr": 1483.948, "width": 1920, "height": 1080, "resolution": "1920x1080", "fps": 30, "dynamic_range": "SDR", "vcodec": "avc1", "vbr": 1356.885, "aspect_ratio": 1.78, "acodec": "mp4a", "abr": 127.063, "asr": 44100, "audio_channels": 2 } ], "extractor_key": "YoutubeTab", "extractor": "youtube:tab", "webpage_url": "https://youtube.com/@sydneycummings/streams", "original_url": "https://youtube.com/@sydneycummings/streams", "webpage_url_basename": "streams", "webpage_url_domain": "youtube.com" }"""
      ).getOrElse(Json.Null)
  }

  object Invidious {
    val continuationValue = "testContinuation"
    lazy val videoData = List(
      videoId1 -> videoResponse1,
      videoId2 -> videoResponse2,
      videoId3 -> videoResponse3,
      videoId4 -> videoResponse4,
      videoId5 -> videoResponse5,
      videoId6 -> videoResponse6
    )
    val videoId1 = "abc123xyz"
    val videoId2 = "live456abc"
    val videoId3 = "ultra8k999"
    val videoId4 = "abc456xyz"
    val videoId5 = "livegame999"
    val videoId6 = "premiere8888"

    val videosWithContinuation: Json =
      parse(
        s"""{
           |  "videos": [
           |    {
           |      "type": "video",
           |      "title": "Exploring the Universe: A Journey Through Space",
           |      "videoId": "$videoId1",
           |      "author": "Space Discoveries",
           |      "authorId": "channel123",
           |      "authorUrl": "https://youtube.com/channel/channel123",
           |      "authorVerified": true,
           |      "videoThumbnails": [
           |        {
           |          "url": "https://i.ytimg.com/vi/abc123xyz/hqdefault.jpg",
           |          "width": 480,
           |          "height": 360
           |        }
           |      ],
           |      "description": "Join us as we travel through the cosmos in this documentary on the universe.",
           |      "descriptionHtml": "<p>Join us as we travel through the cosmos in this documentary on the universe.</p>",
           |      "viewCount": 1245678,
           |      "viewCountText": "1.2M views",
           |      "lengthSeconds": 3600,
           |      "published": 1656789000,
           |      "publishedText": "2 years ago",
           |      "premiereTimestamp": null,
           |      "liveNow": false,
           |      "premium": false,
           |      "isUpcoming": false,
           |      "isNew": false,
           |      "is4k": true,
           |      "is8k": false,
           |      "isVr180": false,
           |      "isVr360": false,
           |      "is3d": false,
           |      "hasCaptions": true
           |    },
           |    {
           |      "type": "video",
           |      "title": "Live Coding: Building a To-Do App with React",
           |      "videoId": "$videoId2",
           |      "author": "DevStream",
           |      "authorId": "channel456",
           |      "authorUrl": "https://youtube.com/channel/channel456",
           |      "authorVerified": false,
           |      "videoThumbnails": [
           |        {
           |          "url": "https://i.ytimg.com/vi/live456abc/hqdefault.jpg",
           |          "width": 480,
           |          "height": 360
           |        }
           |      ],
           |      "description": "Watch as we build a to-do app live using React and Tailwind CSS.",
           |      "descriptionHtml": "<p>Watch as we build a to-do app live using React and Tailwind CSS.</p>",
           |      "viewCount": 7890,
           |      "viewCountText": "7.8K views",
           |      "lengthSeconds": 5400,
           |      "published": 1689123456,
           |      "publishedText": "3 months ago",
           |      "premiereTimestamp": null,
           |      "liveNow": true,
           |      "premium": false,
           |      "isUpcoming": false,
           |      "isNew": true,
           |      "is4k": false,
           |      "is8k": false,
           |      "isVr180": false,
           |      "isVr360": false,
           |      "is3d": false,
           |      "hasCaptions": false
           |    },
           |    {
           |      "type": "video",
           |      "title": "Nature in 8K Ultra HD",
           |      "videoId": "$videoId3",
           |      "author": "EpicScapes",
           |      "authorId": "channel999",
           |      "authorUrl": "https://youtube.com/channel/channel999",
           |      "authorVerified": true,
           |      "videoThumbnails": [
           |        {
           |          "url": "https://i.ytimg.com/vi/ultra8k999/maxresdefault.jpg",
           |          "width": 1280,
           |          "height": 720
           |        }
           |      ],
           |      "description": "Relax with breathtaking 8K footage of nature and wildlife.",
           |      "descriptionHtml": "<p>Relax with breathtaking 8K footage of nature and wildlife.</p>",
           |      "viewCount": 987654,
           |      "viewCountText": "987K views",
           |      "lengthSeconds": 1800,
           |      "published": 1701010101,
           |      "publishedText": "5 weeks ago",
           |      "premiereTimestamp": 1701000000,
           |      "liveNow": false,
           |      "premium": true,
           |      "isUpcoming": false,
           |      "isNew": true,
           |      "is4k": true,
           |      "is8k": true,
           |      "isVr180": false,
           |      "isVr360": false,
           |      "is3d": false,
           |      "hasCaptions": true
           |    }
           |  ],
           |  "continuation": "$continuationValue"
           |}""".stripMargin
      ).getOrElse(Json.Null)

    val videosWithoutContinuation: Json =
      parse(
        s"""{
           |  "videos": [
           |    {
           |      "type": "video",
           |      "title": "Exploring the Future of AI in 2025",
           |      "videoId": "$videoId4",
           |      "author": "TechVision",
           |      "authorId": "UC1234567890abcdef",
           |      "authorUrl": "https://www.youtube.com/channel/UC1234567890abcdef",
           |      "authorVerified": true,
           |      "videoThumbnails": [
           |        {
           |          "url": "https://i.ytimg.com/vi/abc123xyz/hqdefault.jpg",
           |          "width": 480,
           |          "height": 360
           |        }
           |      ],
           |      "description": "A deep dive into AI trends shaping our future.",
           |      "descriptionHtml": "<p>A deep dive into <strong>AI trends</strong> shaping our future.</p>",
           |      "viewCount": 1245832,
           |      "viewCountText": "1.2M views",
           |      "lengthSeconds": 900,
           |      "published": 1716556800,
           |      "publishedText": "1 day ago",
           |      "liveNow": false,
           |      "premium": false,
           |      "isUpcoming": false,
           |      "isNew": true,
           |      "is4k": true,
           |      "is8k": false,
           |      "isVr180": false,
           |      "isVr360": false,
           |      "is3d": false,
           |      "hasCaptions": true
           |    },
           |    {
           |      "type": "video",
           |      "title": "Live Coding a Game Engine",
           |      "videoId": "$videoId5",
           |      "author": "CodeStream",
           |      "authorId": "UCcode9999999stream",
           |      "authorUrl": "https://www.youtube.com/channel/UCcode9999999stream",
           |      "authorVerified": false,
           |      "videoThumbnails": [
           |        {
           |          "url": "https://i.ytimg.com/vi/livegame999/hqdefault.jpg",
           |          "width": 480,
           |          "height": 360
           |        },
           |        {
           |          "url": "https://i.ytimg.com/vi/livegame999/mqdefault.jpg",
           |          "width": 320,
           |          "height": 180
           |        }
           |      ],
           |      "description": "Watch as we build a game engine from scratch—live!",
           |      "descriptionHtml": "<p>Watch as we build a <em>game engine</em> from scratch—live!</p>",
           |      "viewCount": 45219,
           |      "viewCountText": "45K views",
           |      "lengthSeconds": 10800,
           |      "published": 1716480000,
           |      "publishedText": "2 days ago",
           |      "liveNow": true,
           |      "premium": false,
           |      "isUpcoming": false,
           |      "isNew": false,
           |      "is4k": false,
           |      "is8k": false,
           |      "isVr180": false,
           |      "isVr360": false,
           |      "is3d": false,
           |      "hasCaptions": false
           |    },
           |    {
           |      "type": "video",
           |      "title": "Premiere: Nature Documentary 8K Ultra HD",
           |      "videoId": "$videoId6",
           |      "author": "NatureWorld",
           |      "authorId": "UCnature8888world",
           |      "authorUrl": "https://www.youtube.com/channel/UCnature8888world",
           |      "authorVerified": true,
           |      "videoThumbnails": [
           |        {
           |          "url": "https://i.ytimg.com/vi/premiere8888/maxresdefault.jpg",
           |          "width": 1280,
           |          "height": 720
           |        }
           |      ],
           |      "description": "Experience Earth in stunning 8K quality.",
           |      "descriptionHtml": "<p>Experience <strong>Earth</strong> in stunning 8K quality.</p>",
           |      "viewCount": 109284,
           |      "viewCountText": "109K views",
           |      "lengthSeconds": 3600,
           |      "published": 1716220800,
           |      "publishedText": "5 days ago",
           |      "premiereTimestamp": 1716210000,
           |      "liveNow": false,
           |      "premium": true,
           |      "isUpcoming": false,
           |      "isNew": false,
           |      "is4k": true,
           |      "is8k": true,
           |      "isVr180": false,
           |      "isVr360": false,
           |      "is3d": false,
           |      "hasCaptions": true
           |    }
           |  ]
           |}""".stripMargin
      ).getOrElse(Json.Null)

    val videoResponse1: Json =
      parse(s"""{
               |    "type": "video",
               |    "title": "Exploring the Ocean Depths",
               |    "videoId": "$videoId1",
               |    "videoThumbnails": [
               |      {
               |        "quality": "high",
               |        "url": "https://example.com/thumb1.jpg",
               |        "width": 1280,
               |        "height": 720
               |      }
               |    ],
               |    "storyboards": [
               |      {
               |        "url": "https://example.com/storyboard1.jpg",
               |        "templateUrl": "https://example.com/storyboard_{index}.jpg",
               |        "width": 160,
               |        "height": 90,
               |        "count": 100,
               |        "interval ": 5,
               |        "storyboardWidth": 1280,
               |        "storyboardHeight": 720,
               |        "storyboardCount": 10
               |      }
               |    ],
               |    "description": "A deep dive into the unknown parts of our oceans.",
               |    "descriptionHtml": "<p>A deep dive into the unknown parts of our oceans.</p>",
               |    "published": 1682995200000,
               |    "publishedText": "May 1, 2023",
               |    "keywords": ["ocean", "exploration", "marine life"],
               |    "viewCount": 1048576,
               |    "likeCount": 12000,
               |    "dislikeCount": 300,
               |    "paid": false,
               |    "premium": false,
               |    "isFamilyFriendly": true,
               |    "allowedRegions": ["US", "CA", "GB"],
               |    "genre": "Education",
               |    "genreUrl": "https://www.youtube.com/genre/education",
               |    "author": "DeepSeaDocs",
               |    "authorId": "deepsea123",
               |    "authorUrl": "https://youtube.com/channel/deepsea123",
               |    "authorThumbnails": [
               |      {
               |        "url": "https://example.com/author1.jpg",
               |        "width": 100,
               |        "height": 100
               |      }
               |    ],
               |    "subCountText": "1.2M subscribers",
               |    "lengthSeconds": 900,
               |    "allowRatings": true,
               |    "rating": 4.8,
               |    "isListed": true,
               |    "liveNow": false,
               |    "isPostLiveDvr": false,
               |    "isUpcoming": false,
               |    "dashUrl:": "https://example.com/dash/abc123xyz.mpd",
               |    "premiereTimestamp": null,
               |    "hlsUrl": "https://example.com/hls/abc123xyz.m3u8",
               |    "adaptiveFormats": [
               |      {
               |        "index": "0",
               |        "bitrate": "1500000",
               |        "init": "0-100",
               |        "url": "https://example.com/video1.mp4",
               |        "itag": "22",
               |        "type": "video/mp4",
               |        "clen": "10485760",
               |        "lmt": "1682995000",
               |        "projectionType": "rectangular",
               |        "container": "mp4",
               |        "encoding": "H.264",
               |        "qualityLabel": "720p",
               |        "resolution": "1280x720",
               |        "fps": 30,
               |        "size": "1280x720",
               |        "targetDurationsec": null,
               |        "maxDvrDurationSec": null,
               |        "audioQuality": "AUDIO_QUALITY_MEDIUM",
               |        "audioSampleRate": "44100",
               |        "audioChannels": "2",
               |        "colorInfo": null,
               |        "captionTrack": null
               |      }
               |    ],
               |    "formatStreams": [
               |      {
               |        "url": "https://example.com/stream1.mp4",
               |        "itag": "22",
               |        "type": "video/mp4",
               |        "quality": "hd720",
               |        "bitrate": "1500000",
               |        "container": "mp4",
               |        "encoding": "H.264",
               |        "qualityLabel": "720p",
               |        "resolution": "1280x720",
               |        "size": "1280x720"
               |      }
               |    ],
               |    "captions": [
               |      {
               |        "label": "English",
               |        "language_code": "en",
               |        "url": "https://example.com/captions/abc123xyz_en.vtt"
               |      }
               |    ],
               |    "musicTracks": [
               |      {
               |        "song": "Ocean Breeze",
               |        "artist": "Waveform",
               |        "album": "Deep Currents",
               |        "license": "YouTube Music License"
               |      }
               |    ],
               |    "recommendedVideos": [
               |      {
               |        "videoId": "xyz789abc",
               |        "title": "Coral Reefs: Underwater Cities",
               |        "videoThumbnails": [
               |          {
               |            "quality": "high",
               |            "url": "https://example.com/thumb2.jpg",
               |            "width": 1280,
               |            "height": 720
               |          }
               |        ],
               |        "author": "MarineWorld",
               |        "authorUrl": "https://youtube.com/channel/marineworld456",
               |        "authorId": "marineworld456",
               |        "authorVerified": true,
               |        "authorThumbnails": [
               |          {
               |            "url": "https://example.com/author2.jpg",
               |            "width": 100,
               |            "height": 100
               |          }
               |        ],
               |        "lengthSeconds": 720,
               |        "viewCount": 532000,
               |        "viewCountText": "532K views"
               |      }
               |    ]
               |  }""".stripMargin).getOrElse(Json.Null)

    val videoResponse2: Json =
      parse(s"""{
               |  "type": "video",
               |  "title": "Live Gaming Tournament Highlights",
               |  "videoId": "$videoId2",
               |  "videoThumbnails": [
               |    {
               |      "quality": "high",
               |      "url": "https://example.com/thumb_live456abc.jpg",
               |      "width": 1280,
               |      "height": 720
               |    }
               |  ],
               |  "storyboards": [
               |    {
               |      "url": "https://example.com/storyboard_live456abc.jpg",
               |      "templateUrl": "https://example.com/storyboard_live456abc_{index}.jpg",
               |      "width": 160,
               |      "height": 90,
               |      "count": 100,
               |      "interval": 5,
               |      "storyboardWidth": 1280,
               |      "storyboardHeight": 720,
               |      "storyboardCount": 10
               |    }
               |  ],
               |  "description": "Highlights from the most exciting moments of the live gaming tournament.",
               |  "descriptionHtml": "<p>Highlights from the most exciting moments of the live gaming tournament.</p>",
               |  "published": 1682995200000,
               |  "publishedText": "May 1, 2023",
               |  "keywords": ["gaming", "tournament", "highlights"],
               |  "viewCount": 2048576,
               |  "likeCount": 15000,
               |  "dislikeCount": 500,
               |  "paid": false,
               |  "premium": false,
               |  "isFamilyFriendly": true,
               |  "allowedRegions": ["US", "CA", "GB"],
               |  "genre": "Gaming",
               |  "genreUrl": "https://www.youtube.com/genre/gaming",
               |  "author": "GameMasters",
               |  "authorId": "gamemasters123",
               |  "authorUrl": "https://youtube.com/channel/gamemasters123",
               |  "authorThumbnails": [
               |    {
               |      "url": "https://example.com/author_gamemasters.jpg",
               |      "width": 100,
               |      "height": 100
               |    }
               |  ],
               |  "subCountText": "1.5M subscribers",
               |  "lengthSeconds": 1200,
               |  "allowRatings": true,
               |  "rating": 4.9,
               |  "isListed": true,
               |  "liveNow": true,
               |  "isPostLiveDvr": false,
               |  "isUpcoming": false,
               |  "dashUrl": "https://example.com/dash/live456abc.mpd",
               |  "premiereTimestamp": null,
               |  "hlsUrl": "https://example.com/hls/live456abc.m3u8",
               |  "adaptiveFormats": [
               |    {
               |      "index": "0",
               |      "bitrate": "2000000",
               |      "init": "0-100",
               |      "url": "https://example.com/video_live456abc.mp4",
               |      "itag": "22",
               |      "type": "video/mp4",
               |      "clen": "20485760",
               |      "lmt": "1682995000",
               |      "projectionType": "rectangular",
               |      "container": "mp4",
               |      "encoding": "H.264",
               |      "qualityLabel": "720p",
               |      "resolution": "1280x720",
               |      "fps": 30,
               |      "size": "1280x720",
               |      "targetDurationsec": null,
               |      "maxDvrDurationSec": null,
               |      "audioQuality": "AUDIO_QUALITY_HIGH",
               |      "audioSampleRate": "44100",
               |      "audioChannels": "2",
               |      "colorInfo": null,
               |      "captionTrack": null
               |    }
               |  ],
               |  "formatStreams": [
               |    {
               |      "url": "https://example.com/stream_live456abc.mp4",
               |      "itag": "22",
               |      "type": "video/mp4",
               |      "quality": "hd720",
               |      "bitrate": "2000000",
               |      "container": "mp4",
               |      "encoding": "H.264",
               |      "qualityLabel": "720p",
               |      "resolution": "1280x720",
               |      "size": "1280x720"
               |    }
               |  ],
               |  "captions": [
               |    {
               |      "label": "English",
               |      "language_code": "en",
               |      "url": "https://example.com/captions/live456abc_en.vtt"
               |    }
               |  ],
               |  "musicTracks": [
               |    {
               |      "song": "Epic Battle Theme",
               |      "artist": "GameSoundtrack",
               |      "album": "Epic Gaming",
               |      "license": "YouTube Music License"
               |    }
               |  ],
               |  "recommendedVideos": [
               |    {
               |      "videoId": "$videoId3",
               |      "title": "Ultra HD Gaming Experience",
               |      "videoThumbnails": [
               |        {
               |          "quality": "high",
               |          "url": "https://example.com/thumb_ultra8k999.jpg",
               |          "width": 1280,
               |          "height": 720
               |        }
               |      ],
               |      "author": "UltraGamers",
               |      "authorUrl": "https://youtube.com/channel/ultragamers456",
               |      "authorId": "ultragamers456",
               |      "authorVerified": true,
               |      "authorThumbnails": [
               |        {
               |          "url": "https://example.com/author_ultragamers.jpg",
               |          "width": 100,
               |          "height": 100
               |        }
               |      ],
               |      "lengthSeconds": 900,
               |      "viewCount": 732000,
               |      "viewCountText": "732K views"
               |    }
               |  ]
               |}""".stripMargin).getOrElse(Json.Null)
    val videoResponse3: Json =
      parse(s"""{
               |  "type": "video",
               |  "title": "Exploring the Future of Gaming",
               |  "videoId": "$videoId3",
               |  "videoThumbnails": [
               |    {
               |      "quality": "high",
               |      "url": "https://example.com/thumb_ultra8k999.jpg",
               |      "width": 1280,
               |      "height": 720
               |    }
               |  ],
               |  "storyboards": [
               |    {
               |      "url": "https://example.com/storyboard_ultra8k999.jpg",
               |      "templateUrl": "https://example.com/storyboard_ultra8k999_{index}.jpg",
               |      "width": 160,
               |      "height": 90,
               |      "count": 100,
               |      "interval": 5,
               |      "storyboardWidth": 1280,
               |      "storyboardHeight": 720,
               |      "storyboardCount": 10
               |    }
               |  ],
               |  "description": "A look into the advancements shaping the future of gaming.",
               |  "descriptionHtml": "<p>A look into the advancements shaping the future of gaming.</p>",
               |  "published": 1682995200000,
               |  "publishedText": "May 1, 2023",
               |  "keywords": ["gaming", "future", "technology"],
               |  "viewCount": 1548576,
               |  "likeCount": 18000,
               |  "dislikeCount": 200,
               |  "paid": false,
               |  "premium": false,
               |  "isFamilyFriendly": true,
               |  "allowedRegions": ["US", "CA", "GB"],
               |  "genre": "Technology",
               |  "genreUrl": "https://www.youtube.com/genre/technology",
               |  "author": "TechSavvy",
               |  "authorId": "techsavvy123",
               |  "authorUrl": "https://youtube.com/channel/techsavvy123",
               |  "authorThumbnails": [
               |    {
               |      "url": "https://example.com/author_techsavvy.jpg",
               |      "width": 100,
               |      "height": 100
               |    }
               |  ],
               |  "subCountText": "2.1M subscribers",
               |  "lengthSeconds": 1080,
               |  "allowRatings": true,
               |  "rating": 4.7,
               |  "isListed": true,
               |  "liveNow": false,
               |  "isPostLiveDvr": false,
               |  "isUpcoming": false,
               |  "dashUrl": "https://example.com/dash/ultra8k999.mpd",
               |  "premiereTimestamp": null,
               |  "hlsUrl": "https://example.com/hls/ultra8k999.m3u8",
               |  "adaptiveFormats": [
               |    {
               |      "index": "0",
               |      "bitrate": "3000000",
               |      "init": "0-100",
               |      "url": "https://example.com/video_ultra8k999.mp4",
               |      "itag": "22",
               |      "type": "video/mp4",
               |      "clen": "30485760",
               |      "lmt": "1682995000",
               |      "projectionType": "rectangular",
               |      "container": "mp4",
               |      "encoding": "H.264",
               |      "qualityLabel": "1080p",
               |      "resolution": "1920x1080",
               |      "fps": 60,
               |      "size": "1920x1080",
               |      "targetDurationsec": null,
               |      "maxDvrDurationSec": null,
               |      "audioQuality": "AUDIO_QUALITY_HIGH",
               |      "audioSampleRate": "44100",
               |      "audioChannels": "2",
               |      "colorInfo": null,
               |      "captionTrack": null
               |    }
               |  ],
               |  "formatStreams": [
               |    {
               |      "url": "https://example.com/stream_ultra8k999.mp4",
               |      "itag": "22",
               |      "type": "video/mp4",
               |      "quality": "hd1080",
               |      "bitrate": "3000000",
               |      "container": "mp4",
               |      "encoding": "H.264",
               |      "qualityLabel": "1080p",
               |      "resolution": "1920x1080",
               |      "size": "1920x1080"
               |    }
               |  ],
               |  "captions": [
               |    {
               |      "label": "English",
               |      "language_code": "en",
               |      "url": "https://example.com/captions/ultra8k999_en.vtt"
               |    }
               |  ],
               |  "musicTracks": [
               |    {
               |      "song": "Future Vision",
               |      "artist": "TechBeats",
               |      "album": "Innovative Sounds",
               |      "license": "YouTube Music License"
               |    }
               |  ],
               |  "recommendedVideos": [
               |    {
               |      "videoId": "$videoId4",
               |      "title": "The Evolution of Gaming Graphics",
               |      "videoThumbnails": [
               |        {
               |          "quality": "high",
               |          "url": "https://example.com/thumb_abc456xyz.jpg",
               |          "width": 1280,
               |          "height": 720
               |        }
               |      ],
               |      "author": "GraphicsGuru",
               |      "authorUrl": "https://youtube.com/channel/graphicsguru456",
               |      "authorId": "graphicsguru456",
               |      "authorVerified": true,
               |      "authorThumbnails": [
               |        {
               |          "url": "https://example.com/author_graphicsguru.jpg",
               |          "width": 100,
               |          "height": 100
               |        }
               |      ],
               |      "lengthSeconds": 840,
               |      "viewCount": 612000,
               |      "viewCountText": "612K views"
               |    }
               |  ]
               |}""".stripMargin).getOrElse(Json.Null)
    val videoResponse4: Json =
      parse(s"""{
               |  "type": "video",
               |  "title": "Understanding Game Mechanics",
               |  "videoId": "$videoId4",
               |  "videoThumbnails": [
               |    {
               |      "quality": "high",
               |      "url": "https://example.com/thumb_abc456xyz.jpg",
               |      "width": 1280,
               |      "height": 720
               |    }
               |  ],
               |  "storyboards": [
               |    {
               |      "url": "https://example.com/storyboard_abc456xyz.jpg",
               |      "templateUrl": "https://example.com/storyboard_abc456xyz_{index}.jpg",
               |      "width": 160,
               |      "height": 90,
               |      "count": 100,
               |      "interval": 5,
               |      "storyboardWidth": 1280,
               |      "storyboardHeight": 720,
               |      "storyboardCount": 10
               |    }
               |  ],
               |  "description": "A comprehensive guide to understanding game mechanics.",
               |  "descriptionHtml": "<p>A comprehensive guide to understanding game mechanics.</p>",
               |  "published": 1682995200000,
               |  "publishedText": "May 1, 2023",
               |  "keywords": ["gaming", "mechanics", "guide"],
               |  "viewCount": 1348576,
               |  "likeCount": 16000,
               |  "dislikeCount": 400,
               |  "paid": false,
               |  "premium": false,
               |  "isFamilyFriendly": true,
               |  "allowedRegions": ["US", "CA", "GB"],
               |  "genre": "Education",
               |  "genreUrl": "https://www.youtube.com/genre/education",
               |  "author": "GameAcademy",
               |  "authorId": "gameacademy123",
               |  "authorUrl": "https://youtube.com/channel/gameacademy123",
               |  "authorThumbnails": [
               |    {
               |      "url": "https://example.com/author_gameacademy.jpg",
               |      "width": 100,
               |      "height": 100
               |    }
               |  ],
               |  "subCountText": "900K subscribers",
               |  "lengthSeconds": 720,
               |  "allowRatings": true,
               |  "rating": 4.6,
               |  "isListed": true,
               |  "liveNow": false,
               |  "isPostLiveDvr": false,
               |  "isUpcoming": false,
               |  "dashUrl": "https://example.com/dash/abc456xyz.mpd",
               |  "premiereTimestamp": null,
               |  "hlsUrl": "https://example.com/hls/abc456xyz.m3u8",
               |  "adaptiveFormats": [
               |    {
               |      "index": "0",
               |      "bitrate": "2500000",
               |      "init": "0-100",
               |      "url": "https://example.com/video_abc456xyz.mp4",
               |      "itag": "22",
               |      "type": "video/mp4",
               |      "clen": "20485760",
               |      "lmt": "1682995000",
               |      "projectionType": "rectangular",
               |      "container": "mp4",
               |      "encoding": "H.264",
               |      "qualityLabel": "1080p",
               |      "resolution": "1920x1080",
               |      "fps": 60,
               |      "size": "1920x1080",
               |      "targetDurationsec": null,
               |      "maxDvrDurationSec": null,
               |      "audioQuality": "AUDIO_QUALITY_HIGH",
               |      "audioSampleRate": "44100",
               |      "audioChannels": "2",
               |      "colorInfo": null,
               |      "captionTrack": null
               |    }
               |  ],
               |  "formatStreams": [
               |    {
               |      "url": "https://example.com/stream_abc456xyz.mp4",
               |      "itag": "22",
               |      "type": "video/mp4",
               |      "quality": "hd1080",
               |      "bitrate": "2500000",
               |      "container": "mp4",
               |      "encoding": "H.264",
               |      "qualityLabel": "1080p",
               |      "resolution": "1920x1080",
               |      "size": "1920x1080"
               |    }
               |  ],
               |  "captions": [
               |    {
               |      "label": "English",
               |      "language_code": "en",
               |      "url": "https://example.com/captions/abc456xyz_en.vtt"
               |    }
               |  ],
               |  "musicTracks": [
               |    {
               |      "song": "Game On",
               |      "artist": "PlayMusic",
               |      "album": "Gaming Anthems",
               |      "license": "YouTube Music License"
               |    }
               |  ],
               |  "recommendedVideos": [
               |    {
               |      "videoId": "$videoId5",
               |      "title": "Live Game Streaming Tips",
               |      "videoThumbnails": [
               |        {
               |          "quality": "high",
               |          "url": "https://example.com/thumb_livegame999.jpg",
               |          "width": 1280,
               |          "height": 720
               |        }
               |      ],
               |      "author": "StreamerPro",
               |      "authorUrl": "https://youtube.com/channel/streamerpro456",
               |      "authorId": "streamerpro456",
               |      "authorVerified": true,
               |      "authorThumbnails": [
               |        {
               |          "url": "https://example.com/author_streamerpro.jpg",
               |          "width": 100,
               |          "height": 100
               |        }
               |      ],
               |      "lengthSeconds": 600,
               |      "viewCount": 450000,
               |      "viewCountText": "450K views"
               |    }
               |  ]
               |}""".stripMargin).getOrElse(Json.Null)
    val videoResponse5: Json =
      parse(s"""{
               |  "type": "video",
               |  "title": "Live Game Streaming Tips",
               |  "videoId": "$videoId5",
               |  "videoThumbnails": [
               |    {
               |      "quality": "high",
               |      "url": "https://example.com/thumb_livegame999.jpg",
               |      "width": 1280,
               |      "height": 720
               |    }
               |  ],
               |  "storyboards": [
               |    {
               |      "url": "https://example.com/storyboard_livegame999.jpg",
               |      "templateUrl": "https://example.com/storyboard_livegame999_{index}.jpg",
               |      "width": 160,
               |      "height": 90,
               |      "count": 100,
               |      "interval": 5,
               |      "storyboardWidth": 1280,
               |      "storyboardHeight": 720,
               |      "storyboardCount": 10
               |    }
               |  ],
               |  "description": "Essential tips for successful live game streaming.",
               |  "descriptionHtml": "<p>Essential tips for successful live game streaming.</p>",
               |  "published": 1682995200000,
               |  "publishedText": "May 1, 2023",
               |  "keywords": ["streaming", "gaming", "tips"],
               |  "viewCount": 1048576,
               |  "likeCount": 12000,
               |  "dislikeCount": 300,
               |  "paid": false,
               |  "premium": false,
               |  "isFamilyFriendly": true,
               |  "allowedRegions": ["US", "CA", "GB"],
               |  "genre": "Education",
               |  "genreUrl": "https://www.youtube.com/genre/education",
               |  "author": "StreamMasters",
               |  "authorId": "streammasters123",
               |  "authorUrl": "https://youtube.com/channel/streammasters123",
               |  "authorThumbnails": [
               |    {
               |      "url": "https://example.com/author_streammasters.jpg",
               |      "width": 100,
               |      "height": 100
               |    }
               |  ],
               |  "subCountText": "1.1M subscribers",
               |  "lengthSeconds": 720,
               |  "allowRatings": true,
               |  "rating": 4.8,
               |  "isListed": true,
               |  "liveNow": false,
               |  "isPostLiveDvr": false,
               |  "isUpcoming": false,
               |  "dashUrl": "https://example.com/dash/livegame999.mpd",
               |  "premiereTimestamp": null,
               |  "hlsUrl": "https://example.com/hls/livegame999.m3u8",
               |  "adaptiveFormats": [
               |    {
               |      "index": "0",
               |      "bitrate": "1500000",
               |      "init": "0-100",
               |      "url": "https://example.com/video_livegame999.mp4",
               |      "itag": "22",
               |      "type": "video/mp4",
               |      "clen": "10485760",
               |      "lmt": "1682995000",
               |      "projectionType": "rectangular",
               |      "container": "mp4",
               |      "encoding": "H.264",
               |      "qualityLabel": "720p",
               |      "resolution": "1280x720",
               |      "fps": 30,
               |      "size": "1280x720",
               |      "targetDurationsec": null,
               |      "maxDvrDurationSec": null,
               |      "audioQuality": "AUDIO_QUALITY_MEDIUM",
               |      "audioSampleRate": "44100",
               |      "audioChannels": "2",
               |      "colorInfo": null,
               |      "captionTrack": null
               |    }
               |  ],
               |  "formatStreams": [
               |    {
               |      "url": "https://example.com/stream_livegame999.mp4",
               |      "itag": "22",
               |      "type": "video/mp4",
               |      "quality": "hd720",
               |      "bitrate": "1500000",
               |      "container": "mp4",
               |      "encoding": "H.264",
               |      "qualityLabel": "720p",
               |      "resolution": "1280x720",
               |      "size": "1280x720"
               |    }
               |  ],
               |  "captions": [
               |    {
               |      "label": "English",
               |      "language_code": "en",
               |      "url": "https://example.com/captions/livegame999_en.vtt"
               |    }
               |  ],
               |  "musicTracks": [
               |    {
               |      "song": "Streaming Success",
               |      "artist": "LiveBeats",
               |      "album": "Streaming Essentials",
               |      "license": "YouTube Music License"
               |    }
               |  ],
               |  "recommendedVideos": [
               |    {
               |      "videoId": "$videoId6",
               |      "title": "Upcoming Game Releases",
               |      "videoThumbnails": [
               |        {
               |          "quality": "high",
               |          "url": "https://example.com/thumb_premiere8888.jpg",
               |          "width": 1280,
               |          "height": 720
               |        }
               |      ],
               |      "author": "GameReleases",
               |      "authorUrl": "https://youtube.com/channel/gamereleases456",
               |      "authorId": "gamereleases456",
               |      "authorVerified": true,
               |      "authorThumbnails": [
               |        {
               |          "url": "https://example.com/author_gamereleases.jpg",
               |          "width": 100,
               |          "height": 100
               |        }
               |      ],
               |      "lengthSeconds": 600,
               |      "viewCount": 300000,
               |      "viewCountText": "300K views"
               |    }
               |  ]
               |}""".stripMargin).getOrElse(Json.Null)
    val videoResponse6: Json =
      parse(s"""{
               |  "type": "video",
               |  "title": "Upcoming Game Releases",
               |  "videoId": "$videoId6",
               |  "videoThumbnails": [
               |    {
               |      "quality": "high",
               |      "url": "https://example.com/thumb_premiere8888.jpg",
               |      "width": 1280,
               |      "height": 720
               |    }
               |  ],
               |  "storyboards": [
               |    {
               |      "url": "https://example.com/storyboard_premiere8888.jpg",
               |      "templateUrl": "https://example.com/storyboard_premiere8888_{index}.jpg",
               |      "width": 160,
               |      "height": 90,
               |      "count": 100,
               |      "interval": 5,
               |      "storyboardWidth": 1280,
               |      "storyboardHeight": 720,
               |      "storyboardCount": 10
               |    }
               |  ],
               |  "description": "A preview of the most anticipated upcoming game releases.",
               |  "descriptionHtml": "<p>A preview of the most anticipated upcoming game releases.</p>",
               |  "published": 1682995200000,
               |  "publishedText": "May 1, 2023",
               |  "keywords": ["gaming", "upcoming", "releases"],
               |  "viewCount": 500000,
               |  "likeCount": 8000,
               |  "dislikeCount": 100,
               |  "paid": false,
               |  "premium": false,
               |  "isFamilyFriendly": true,
               |  "allowedRegions": ["US", "CA", "GB"],
               |  "genre": "News",
               |  "genreUrl": "https://www.youtube.com/genre/news",
               |  "author": "GameNews",
               |  "authorId": "gamenews123",
               |  "authorUrl": "https://youtube.com/channel/gamenews123",
               |  "authorThumbnails": [
               |    {
               |      "url": "https://example.com/author_gamenews.jpg",
               |      "width": 100,
               |      "height": 100
               |    }
               |  ],
               |  "subCountText": "600K subscribers",
               |  "lengthSeconds": 480,
               |  "allowRatings": true,
               |  "rating": 4.5,
               |  "isListed": true,
               |  "liveNow": false,
               |  "isPostLiveDvr": false,
               |  "isUpcoming": false,
               |  "dashUrl": "https://example.com/dash/premiere8888.mpd",
               |  "premiereTimestamp": null,
               |  "hlsUrl": "https://example.com/hls/premiere8888.m3u8",
               |  "adaptiveFormats": [
               |    {
               |      "index": "0",
               |      "bitrate": "1000000",
               |      "init": "0-100",
               |      "url": "https://example.com/video_premiere8888.mp4",
               |      "itag": "22",
               |      "type": "video/mp4",
               |      "clen": "10485760",
               |      "lmt": "1682995000",
               |      "projectionType": "rectangular",
               |      "container": "mp4",
               |      "encoding": "H.264",
               |      "qualityLabel": "720p",
               |      "resolution": "1280x720",
               |      "fps": 30,
               |      "size": "1280x720",
               |      "targetDurationsec": null,
               |      "maxDvrDurationSec": null,
               |      "audioQuality": "AUDIO_QUALITY_MEDIUM",
               |      "audioSampleRate": "44100",
               |      "audioChannels": "2",
               |      "colorInfo": null,
               |      "captionTrack": null
               |    }
               |  ],
               |  "formatStreams": [
               |    {
               |      "url": "https://example.com/stream_premiere8888.mp4",
               |      "itag": "22",
               |      "type": "video/mp4",
               |      "quality": "hd720",
               |      "bitrate": "1000000",
               |      "container": "mp4",
               |      "encoding": "H.264",
               |      "qualityLabel": "720p",
               |      "resolution": "1280x720",
               |      "size": "1280x720"
               |    }
               |  ],
               |  "captions": [
               |    {
               |      "label": "English",
               |      "language_code": "en",
               |      "url": "https://example.com/captions/premiere8888_en.vtt"
               |    }
               |  ],
               |  "musicTracks": [
               |    {
               |      "song": "Game Release Anthem",
               |      "artist": "ReleaseBeats",
               |      "album": "Anthems of Gaming",
               |      "license": "YouTube Music License"
               |    }
               |  ],
               |  "recommendedVideos": [
               |    {
               |      "videoId": "$videoId2",
               |      "title": "Live Gaming Tournament Highlights",
               |      "videoThumbnails": [
               |        {
               |          "quality": "high",
               |          "url": "https://example.com/thumb_live456abc.jpg",
               |          "width": 1280,
               |          "height": 720
               |        }
               |      ],
               |      "author": "GameMasters",
               |      "authorUrl": "https://youtube.com/channel/gamemasters123",
               |      "authorId": "gamemasters123",
               |      "authorVerified": true,
               |      "authorThumbnails": [
               |        {
               |          "url": "https://example.com/author_gamemasters.jpg",
               |          "width": 100,
               |          "height": 100
               |        }
               |      ],
               |      "lengthSeconds": 1200,
               |      "viewCount": 2048576,
               |      "viewCountText": "2.0M views"
               |    }
               |  ]
               |}""".stripMargin).getOrElse(Json.Null)
  }
end JsonParserSpec
