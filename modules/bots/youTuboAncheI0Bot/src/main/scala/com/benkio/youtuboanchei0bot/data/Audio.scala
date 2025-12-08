package com.benkio.youtuboanchei0bot.data

import com.benkio.telegrambotinfrastructure.model.reply.mp3
import com.benkio.telegrambotinfrastructure.model.reply.ReplyBundleMessage

object Audio {

  def messageRepliesAudioData: List[ReplyBundleMessage] = List(
    ReplyBundleMessage.textToMp3(
      "non vi costa nulla"
    )(
      mp3"ytai_Donazioni.mp3"
    ),
    ReplyBundleMessage.textToMp3(
      "bengalino",
      "pappagallo",
      "uccellino"
    )(
      mp3"ytai_BengalinoDiamantino.mp3"
    ),
    ReplyBundleMessage.textToMp3(
      "cocod[eè]".r,
      "gallina"
    )(
      mp3"ytai_Cocode.mp3"
    ),
    ReplyBundleMessage.textToMp3(
      "\\bmisc\\b".r,
      "\\bm[i]+[a]+[o]+\\b".r
    )(
      mp3"ytai_Misc.mp3"
    ),
    ReplyBundleMessage.textToMp3(
      "francesismo"
    )(
      mp3"ytai_Francesismo.mp3"
    ),
    ReplyBundleMessage.textToMp3(
      "grazie!"
    )(
      mp3"ytai_Grazie.mp3"
    ),
    ReplyBundleMessage.textToMp3(
      "3000",
      "tremila",
      "multa"
    )(
      mp3"ytai_Multa3000euro.mp3"
    )
  )
} // end Audio
