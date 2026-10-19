package com.benkio.replieseditor.load

import com.benkio.replieseditor.module.*
import io.circe.Json

object RepliesJsonMapping {

  def extractEditableEntry(j: Json): Option[EditableEntry] = {
    val c       = j.hcursor
    val matcher = c.downField("matcher").as[String].getOrElse("ContainsOnce")

    val triggers: Option[Vector[TriggerEdit]] =
      for {
        triggerObj  <- c.downField("trigger").focus
        textTrigger <- triggerObj.hcursor.downField("TextTrigger").focus
        triggersArr <- textTrigger.hcursor.downField("triggers").as[Vector[Json]].toOption
      } yield triggersArr.flatMap { tj =>
        val tc = tj.hcursor
        tc.downField("StringTextTriggerValue")
          .as[String]
          .toOption
          .map(v => TriggerEdit(TriggerKind.PlainString, v, None))
          .orElse(
            tc.downField("RegexTextTriggerValue")
              .downField("trigger")
              .as[String]
              .toOption
              .map { v =>
                val len = tc.downField("RegexTextTriggerValue").downField("regexLength").as[Int].toOption
                TriggerEdit(TriggerKind.Regex, v, len)
              }
          )
      }

    val replyObjOpt = c.downField("reply").focus

    val replyItems: Option[Vector[ReplyItem]] =
      replyObjOpt.flatMap { replyObj =>
        val filesOpt: Option[Vector[ReplyItem]] =
          for {
            mediaReply    <- replyObj.hcursor.downField("MediaReply").focus
            mediaFilesArr <- mediaReply.hcursor.downField("mediaFiles").as[Vector[Json]].toOption
          } yield mediaFilesArr.flatMap { mf =>
            val mc        = mf.hcursor
            val fileKinds = List("Mp3File", "GifFile", "VideoFile", "PhotoFile", "MarkdownFile", "Document", "Sticker")
            fileKinds.view
              .flatMap(k => mc.downField(k).downField("filepath").as[String].toOption)
              .headOption
              .map(fp => ReplyItem(ReplyItemKind.File, fp))
          }

        val textsOpt: Option[Vector[ReplyItem]] =
          replyObj.hcursor
            .downField("TextReply")
            .downField("text")
            .as[Vector[String]]
            .toOption
            .orElse(
              replyObj.hcursor
                .downField("TextReply")
                .downField("text")
                .as[String]
                .toOption
                .map(s => Vector(s))
            )
            .map(_.map(t => ReplyItem(ReplyItemKind.Text, t)))

        filesOpt.orElse(textsOpt)
      }

    for {
      t <- triggers
      r <- replyItems
    } yield EditableEntry(replies = r, triggers = t, matcher = matcher)
  }

  private def mediaFileWrapper(filename: String): String =
    if filename.endsWith(".mp3") then "Mp3File"
    else if filename.endsWith(".jpg") || filename.endsWith(".png") then "PhotoFile"
    else if filename.endsWith(".mp4") && filename.endsWith("Gif.mp4") then "GifFile"
    else if filename.endsWith(".mp4") then "VideoFile"
    else if filename.endsWith(".md") then "MarkdownFile"
    else if filename.endsWith(".sticker") then "Sticker"
    else "Document"

  def buildJsonFromEditable(e: EditableEntry): Either[String, Json] = {
    val triggersJsonE: Either[String, Vector[Json]] = {
      val init: Either[String, Vector[Json]] = Right(Vector.empty)
      e.triggers.foldLeft(init) { (accE, t) =>
        accE.flatMap { acc =>
          t match {
            case TriggerEdit(TriggerKind.PlainString, v, _) =>
              Right(acc :+ Json.obj("StringTextTriggerValue" -> Json.fromString(v)))
            case TriggerEdit(TriggerKind.Regex, v, Some(len)) =>
              Right(
                acc :+ Json.obj(
                  "RegexTextTriggerValue" -> Json.obj(
                    "trigger"     -> Json.fromString(v),
                    "regexLength" -> Json.fromInt(len)
                  )
                )
              )
            case TriggerEdit(TriggerKind.Regex, v, None) =>
              Left(s"Regex trigger '$v' is missing regexLength")
          }
        }
      }
    }

    triggersJsonE.flatMap { triggersJson =>
      val kinds                            = e.replies.map(_.kind).distinct
      val replyJsonE: Either[String, Json] =
        kinds match {
          case Vector() =>
            Right(
              Json.obj(
                "TextReply" -> Json.obj(
                  "text"           -> Json.fromValues(Vector.empty),
                  "replyToMessage" -> Json.fromBoolean(false)
                )
              )
            )
          case Vector(ReplyItemKind.File) =>
            val mediaFilesJson: Vector[Json] =
              e.replies.map(_.value).map { f =>
                val wrapper = mediaFileWrapper(f)
                Json.obj(
                  wrapper -> Json.obj(
                    "filepath"       -> Json.fromString(f),
                    "replyToMessage" -> Json.fromBoolean(false)
                  )
                )
              }
            Right(
              Json.obj(
                "MediaReply" -> Json.obj(
                  "mediaFiles"     -> Json.fromValues(mediaFilesJson),
                  "replyToMessage" -> Json.fromBoolean(false)
                )
              )
            )
          case Vector(ReplyItemKind.Text) =>
            Right(
              Json.obj(
                "TextReply" -> Json.obj(
                  "text"           -> Json.fromValues(e.replies.map(_.value).map(Json.fromString)),
                  "replyToMessage" -> Json.fromBoolean(false)
                )
              )
            )
          case _ =>
            Left("Replies cannot mix file and text items in the same entry")
        }

      replyJsonE.map { replyJson =>
        Json.obj(
          "trigger" -> Json.obj(
            "TextTrigger" -> Json.obj(
              "triggers" -> Json.fromValues(triggersJson)
            )
          ),
          "reply"   -> replyJson,
          "matcher" -> Json.fromString(e.matcher)
        )
      }
    }
  }
}
