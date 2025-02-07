package com.benkio.botDB.show

import cats.effect.Async
import cats.implicits.*
import com.benkio.telegrambotinfrastructure.resources.db.DBShowData
import io.circe.Json
import log.effect.LogWriter

object YoutubeJSONParser:

  def parseYoutube[F[_]: Async: LogWriter](json: Json, botName: String): F[List[DBShowData]] =
    for
      filteredJson <- filterYoutubePlaylistJson(json.deepDropNullValues, botName)
      dbMediaDatas <- Async[F].fromEither(filteredJson.as[List[DBShowData]])
    yield dbMediaDatas

  private def filterYoutubePlaylistJson[F[_]: Async: LogWriter](json: Json, botName: String): F[Json] =
    for
      _ <- LogWriter.info(s"[YoutubeJSONParser] parsing... ${json.noSpaces.take(100)}")
      entries <- Async[F].fromOption(
        json.hcursor.downField("entries").values.map(_.toList),
        Throwable(
          s"[YoutubeJSONParser] expected playlist entires, found None. jsonType: ${json.name} - Keys: ${json.hcursor.keys}"
        )
      )
      jsons <- entries.traverse(j => youtubeVideoToDBSHowData(j, botName))
    yield Json.fromValues(jsons)

  private def youtubeVideoToDBSHowData[F[_]: Async: LogWriter](json: Json, botName: String): F[Json] =
    LogWriter.info(s"[YoutubeJSONParser] parsing YT entry ${json.noSpaces.take(100)}") >>
      Async[F].fromEither(
        for
          id               <- json.hcursor.get[String]("id")
          show_title       <- json.hcursor.get[String]("title")
          show_upload_date <- json.hcursor.get[String]("upload_date")
          show_duration    <- json.hcursor.get[Int]("duration")
          show_description <- json.hcursor.get[String]("description")
          show_is_live     <- json.hcursor.get[Boolean]("is_live")
          show_origin_automatic_caption = extractOriginCaptions(json)
        yield Json.obj(
          "show_url"                      -> Json.fromString(s"https://www.youtube.com/watch?v=$id"),
          "bot_name"                      -> Json.fromString(botName),
          "show_title"                    -> Json.fromString(show_title),
          "show_upload_date"              -> Json.fromString(show_upload_date),
          "show_duration"                 -> Json.fromInt(show_duration),
          "show_description"              -> Json.fromString(show_description),
          "show_is_live"                  -> Json.fromBoolean(show_is_live),
          "show_origin_automatic_caption" -> Json.fromString(show_origin_automatic_caption)
        )
      )

  private def extractOriginCaptions(json: Json): String =
    val captions = json.hcursor.downField("automatic_captions")
    println(s"[YoutubeJSONParser] parsing Automatic Captions ${json.noSpaces.take(100)}")
    val optionOriginCaption =
      for
        originCaptionKeys <- captions.keys
        originCaptionKey  <- originCaptionKeys.find(k => k.contains("orig"))
        originCaption = captions.downField(originCaptionKey)
        originCaptionValues <- originCaption.values
        _ = println(s"[YoutubeJSONParser] parsing orig caption ${originCaptionValues.take(10)}")
        originCaptionJson3 <- originCaptionValues.find(j => j.hcursor.get[String]("ext") == Right("json3"))
      yield originCaptionJson3

    optionOriginCaption.fold("")(_.hcursor.get[String]("url").getOrElse(""))
end YoutubeJSONParser
