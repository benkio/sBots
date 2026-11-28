package com.benkio.replieseditor.ui.components.replies.replies

import com.benkio.replieseditor.module.*
import com.raquo.airstream.state.Var
import com.raquo.laminar.api.L.*
import com.raquo.laminar.codecs.StringAsIsCodec

object ReplyItemRow {

  def render(
      entryIdx: Int,
      replyIdx: Int,
      itemSignal: Signal[ReplyItem],
      allowedFiles: Signal[Vector[String]],
      onValueChange: String => Unit,
      onRemove: () => Unit
  ): Div = {
    val kindSignal  = itemSignal.map(_.kind).distinct
    val valueSignal = itemSignal.map(_.value)
    val datalistId  = s"replies-$entryIdx-$replyIdx"

    // A large bot has thousands of allowed filenames: building one option node per file for every row on page load
    // dominates rendering time. Delay the list until the field is focused, then key the options so editing a value
    // only adds/removes the single option that differs instead of rebuilding the whole list.
    val optionsVisibleVar                    = Var(false)
    val optionValues: Signal[Vector[String]] =
      optionsVisibleVar.signal.combineWith(allowedFiles).combineWith(valueSignal).map {
        case (false, _, _)             => Vector.empty[String]
        case (true, allowed, selected) =>
          if allowed.contains(selected) then allowed else selected +: allowed
      }

    div(
      cls := "input-group input-group-sm mb-1",
      child <-- kindSignal.map {
        case ReplyItemKind.File =>
          input(
            cls                               := "form-control",
            typ                               := "text",
            htmlAttr("list", StringAsIsCodec) := datalistId,
            controlled(
              value <-- valueSignal,
              onInput.mapToValue --> onValueChange
            ),
            onFocus --> { _ => optionsVisibleVar.set(true) }
          )
        case ReplyItemKind.Text =>
          input(
            cls         := "form-control",
            typ         := "text",
            placeholder := "text reply",
            controlled(
              value <-- valueSignal,
              onInput.mapToValue --> onValueChange
            )
          )
      },
      child <-- kindSignal.map {
        case ReplyItemKind.File =>
          dataList(
            idAttr := datalistId,
            children <-- optionValues.split(identity) { (_, _, optionValueSignal) =>
              option(value <-- optionValueSignal)
            }
          )
        case ReplyItemKind.Text =>
          emptyNode
      },
      button(
        cls := "btn btn-outline-danger",
        "−",
        onClick --> { _ => onRemove() }
      )
    )
  }
}
