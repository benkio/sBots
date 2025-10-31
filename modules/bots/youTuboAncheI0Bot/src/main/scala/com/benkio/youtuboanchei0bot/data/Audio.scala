package com.benkio.youtuboanchei0bot.data

import cats.Applicative
import com.benkio.telegrambotinfrastructure.model.reply.mp3
import com.benkio.telegrambotinfrastructure.model.reply.ReplyBundleMessage

object Audio:

  def messageRepliesAudioData[
      F[_]: Applicative
  ]: List[ReplyBundleMessage[F]] = List(
    ReplyBundleMessage.textToMp3[F](
      "non vi costa nulla"
    )(
      mp3"ytai_Donazioni.mp3"
    ),
    ReplyBundleMessage.textToMp3[F](
      "bengalino",
      "pappagallo",
      "uccellino"
    )(
      mp3"ytai_BengalinoDiamantino.mp3"
    ),
    ReplyBundleMessage.textToMp3[F](
      "cocod[eè]".r,
      "gallina"
    )(
      mp3"ytai_Cocode.mp3"
    ),
    ReplyBundleMessage.textToMp3[F](
      "\\bmisc\\b".r,
      "\\bm[i]+[a]+[o]+\\b".r
    )(
      mp3"ytai_Misc.mp3"
    ),
    ReplyBundleMessage.textToMp3[F](
      "francesismo"
    )(
      mp3"ytai_Francesismo.mp3"
    ),
    ReplyBundleMessage.textToMp3[F](
      "grazie!"
    )(
      mp3"ytai_Grazie.mp3"
    ),
    ReplyBundleMessage.textToMp3[F](
      "3000",
      "tremila",
      "multa"
    )(
      mp3"ytai_Multa3000euro.mp3"
    )
  )
end Audio
