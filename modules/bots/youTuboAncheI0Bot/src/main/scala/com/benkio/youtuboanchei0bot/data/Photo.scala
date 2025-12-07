package com.benkio.youtuboanchei0bot.data


import com.benkio.telegrambotinfrastructure.model.reply.pho
import com.benkio.telegrambotinfrastructure.model.reply.ReplyBundleMessage

object Photo {

  def messageRepliesPhotoData: List[ReplyBundleMessage] = List(
    ReplyBundleMessage
      .textToMedia(
        "torta"
      )(
        pho"ytai_SorrisoTortaFelice.jpg"
      ),
    ReplyBundleMessage
      .textToMedia(
        "\\bsgrida[tr]".r,
        "\\brimprover".r
      )(
        pho"ytai_Rimprovero.jpg"
      ),
    ReplyBundleMessage
      .textToMedia(
        "(baci )?perugina".r
      )(
        pho"ytai_BaciPerugina.jpg"
      )
  )
}
