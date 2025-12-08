package com.benkio.youtuboanchei0bot.data

import com.benkio.telegrambotinfrastructure.model.reply.sticker
import com.benkio.telegrambotinfrastructure.model.reply.ReplyBundleMessage

object Sticker {

  def messageRepliesStickerData: List[ReplyBundleMessage] = List(
    ReplyBundleMessage.textToSticker(
      "\\bpasta\\b".r,
      "🍝"
    )(
      sticker"ytai_PastaYtancheio.sticker",
      sticker"ytai_Pasta2Ytancheio.sticker"
    ),
    ReplyBundleMessage.textToSticker(
      "\\bduplo\\b".r,
      "🍫"
    )(
      sticker"ytai_DuploYtancheio.sticker"
    ),
    ReplyBundleMessage.textToSticker(
      "ka[p]?fen".r,
      "bombolone"
    )(
      sticker"ytai_KrapfenYtancheio.sticker"
    )
  )
}
