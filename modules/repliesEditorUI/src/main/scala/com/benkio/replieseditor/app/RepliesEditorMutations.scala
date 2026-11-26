package com.benkio.replieseditor.app

import com.benkio.replieseditor.load.ApiClient
import com.benkio.replieseditor.load.RepliesJsonMapping
import com.benkio.replieseditor.module.*
import io.circe.Json

import scala.concurrent.ExecutionContext.Implicits.global

final class RepliesEditorMutations(store: RepliesEditorStore, loader: RepliesEditorLoader) {

  def commit(): Unit =
    store.selectedBotVar.now().foreach { botId =>
      store.setStatus("Saving…")
      ApiClient
        .postJson(s"/api/bot/$botId/replies/commit", Json.obj())
        .onComplete(
          _.fold(
            ex => store.setStatus(s"Save failed: ${ex.getMessage}"),
            _.fold(
              err => store.setStatus(s"Save failed: $err"),
              _ => {
                store.dirtyVar.set(false)
                store.setStatus("Saved.")
              }
            )
          )
        )
    }

  def deleteEntry(entryIndex: Int): Unit =
    store.selectedBotVar.now().foreach { botId =>
      store.setStatus(s"Deleting reply #${entryIndex + 1}…")
      val payload = Json.obj("index" -> Json.fromInt(entryIndex))
      ApiClient
        .postJson(s"/api/bot/$botId/replies/delete", payload)
        .onComplete(
          _.fold(
            ex => store.setStatus(s"Delete failed: ${ex.getMessage}"),
            _.fold(
              err => store.setStatus(s"Delete failed: $err"),
              _ => {
                store.markDirty()
                loader.loadPage(botId, store.currentPageVar.now())
              }
            )
          )
        )
    }

  def addNewReplyAtCurrentPageTop(): Unit =
    store.selectedBotVar.now().foreach { botId =>
      val firstAllowed = store.allowedFilesVar.now().headOption.getOrElse("")
      val newEntry     =
        EditableEntry(
          replies =
            if firstAllowed.isEmpty then Vector(ReplyItem(ReplyItemKind.Text, ""))
            else Vector(ReplyItem(ReplyItemKind.File, firstAllowed)),
          triggers = Vector(TriggerEdit(TriggerKind.PlainString, "", None)),
          matcher = "ContainsOnce"
        )

      RepliesJsonMapping
        .buildJsonFromEditable(newEntry)
        .fold(
          err => store.setStatus(s"Cannot add: $err"),
          j => {
            store.setStatus("Adding reply…")
            val total   = store.totalVar.now().getOrElse(0)
            val ps      = store.pageSizeVar.now().max(1)
            val page    = store.currentPageVar.now().max(1)
            val offset  = ((page - 1) * ps).min(total).max(0)
            val payload = Json.obj("index" -> Json.fromInt(offset), "value" -> j)

            ApiClient
              .postJson(s"/api/bot/$botId/replies/insert", payload)
              .onComplete(
                _.fold(
                  ex => store.setStatus(s"Add failed: ${ex.getMessage}"),
                  _.fold(
                    err => store.setStatus(s"Add failed: $err"),
                    respJson => {
                      val newTotal =
                        respJson.hcursor
                          .downField("total")
                          .as[Int]
                          .toOption
                          .orElse(store.totalVar.now())
                          .getOrElse(total + 1)
                      store.markDirty()
                      store.totalVar.set(Some(newTotal))
                      loader.loadPage(botId, page)
                    }
                  )
                )
              )
          }
        )
    }

  // ---- reply items / triggers

  def setReplyItemValue(entryIndex: Int, replyIdx: Int, value: String): Unit =
    updateEditable(entryIndex) { e0 =>
      val kind = e0.replies.lift(replyIdx).map(_.kind).getOrElse(ReplyItemKind.Text)
      e0.copy(replies = e0.replies.updated(replyIdx, ReplyItem(kind, value)))
    }

  def addFileReplyItem(entryIndex: Int): Unit =
    updateEditable(entryIndex) { e0 =>
      val first   = store.allowedFilesVar.now().headOption.getOrElse("")
      val hasText = e0.replies.exists(_.kind == ReplyItemKind.Text)
      val next    = ReplyItem(ReplyItemKind.File, first)
      if hasText then e0.copy(replies = Vector(next)) else e0.copy(replies = e0.replies :+ next)
    }

  def addTextReplyItem(entryIndex: Int): Unit =
    updateEditable(entryIndex) { e0 =>
      val hasFile = e0.replies.exists(_.kind == ReplyItemKind.File)
      val next    = ReplyItem(ReplyItemKind.Text, "")
      if hasFile then e0.copy(replies = Vector(next)) else e0.copy(replies = e0.replies :+ next)
    }

  def removeReplyItem(entryIndex: Int, replyIdx: Int): Unit =
    updateEditable(entryIndex)(e0 => e0.copy(replies = e0.replies.patch(from = replyIdx, other = Nil, replaced = 1)))

  def addTrigger(entryIndex: Int): Unit =
    updateEditable(entryIndex)(e0 => e0.copy(triggers = e0.triggers :+ TriggerEdit(TriggerKind.PlainString, "", None)))

  def removeTrigger(entryIndex: Int, triggerIdx: Int): Unit =
    updateEditable(entryIndex)(e0 =>
      e0.copy(triggers = e0.triggers.patch(from = triggerIdx, other = Nil, replaced = 1))
    )

  def setTriggerKind(entryIndex: Int, triggerIdx: Int, kind: TriggerKind): Unit =
    updateEditable(entryIndex) { e0 =>
      val old  = e0.triggers(triggerIdx)
      val next =
        kind match {
          case TriggerKind.PlainString => old.copy(kind = kind, regexLength = None)
          case TriggerKind.Regex       =>
            old.copy(kind = kind, regexLength = old.regexLength.orElse(Some(old.value.length)))
        }
      e0.copy(triggers = e0.triggers.updated(triggerIdx, next))
    }

  def setTriggerValue(entryIndex: Int, triggerIdx: Int, value: String): Unit =
    updateEditable(entryIndex)(e0 =>
      e0.copy(triggers = e0.triggers.updated(triggerIdx, e0.triggers(triggerIdx).copy(value = value)))
    )

  def setTriggerRegexLength(entryIndex: Int, triggerIdx: Int, len: Option[Int]): Unit =
    updateEditable(entryIndex)(e0 =>
      e0.copy(triggers = e0.triggers.updated(triggerIdx, e0.triggers(triggerIdx).copy(regexLength = len)))
    )

  // ---- internal: update local editable + persist to server

  private def updateEditable(entryIndex: Int)(f: EditableEntry => EditableEntry): Unit = {
    var updated: Option[EditableEntry] = None

    store.entriesVar.update { current =>
      current.map { st =>
        if st.index != entryIndex then st
        else
          st.editable.fold(st) { e0 =>
            val e1 = f(e0)
            updated = Some(e1)
            store.markDirty()
            st.copy(editable = Some(e1))
          }
      }
    }

    updated.foreach { e =>
      store.selectedBotVar.now().foreach { botId =>
        RepliesJsonMapping
          .buildJsonFromEditable(e)
          .fold(
            err => store.setStatus(s"Invalid entry #${entryIndex + 1}: $err"),
            j => {
              val payload = Json.obj("index" -> Json.fromInt(entryIndex), "value" -> j)
              ApiClient
                .postJson(s"/api/bot/$botId/replies/update", payload)
                .onComplete(
                  _.fold(
                    ex => store.setStatus(s"Update failed: ${ex.getMessage}"),
                    _.fold(
                      err => store.setStatus(s"Update failed: $err"),
                      _ =>
                        if store.filterTextVar.now().trim.nonEmpty then loader.loadPage(
                          botId,
                          store.currentPageVar.now()
                        )
                    )
                  )
                )
            }
          )
      }
    }
  }
}
