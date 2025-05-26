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
