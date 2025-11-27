package com.benkio.youtuboanchei0bot.data

import cats.Applicative
import com.benkio.telegrambotinfrastructure.model.reply.sticker
import com.benkio.telegrambotinfrastructure.model.reply.ReplyBundleMessage

object Sticker {

  def messageRepliesStickerData[
      F[_]: Applicative
  ]: List[ReplyBundleMessage[F]] = List(
    ReplyBundleMessage.textToSticker[F](
      "\\bpasta\\b".r,
      "🍝"
    )(
      sticker"ytai_PastaYtancheio.sticker",
      sticker"ytai_Pasta2Ytancheio.sticker"
    ),
    ReplyBundleMessage.textToSticker[F](
      "\\bduplo\\b".r,
      "🍫"
    )(
      sticker"ytai_DuploYtancheio.sticker"
    ),
    ReplyBundleMessage.textToSticker[F](
      "ka[p]?fen".r,
      "bombolone"
    )(
      sticker"ytai_KrapfenYtancheio.sticker"
    )
  )
}
