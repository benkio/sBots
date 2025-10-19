package com.benkio.youtuboanchei0bot.data

import cats.Applicative
import com.benkio.telegrambotinfrastructure.model.reply.pho
import com.benkio.telegrambotinfrastructure.model.reply.ReplyBundleMessage
import com.benkio.telegrambotinfrastructure.model.tr

object Photo:

  def messageRepliesPhotoData[
      F[_]: Applicative
  ]: List[ReplyBundleMessage[F]] = List(
    ReplyBundleMessage
      .textToMedia[F](
        "torta"
      )(
        pho"ytai_SorrisoTortaFelice.jpg"
      ),
    ReplyBundleMessage
      .textToMedia[F](
        "\\bsgrida[tr]".r.tr(7),
        "\\brimprover".r.tr(9)
      )(
        pho"ytai_Rimprovero.jpg"
      ),
    ReplyBundleMessage
      .textToMedia[F](
        "(baci )?perugina".r.tr(8)
      )(
        pho"ytai_BaciPerugina.jpg"
      )
  )
