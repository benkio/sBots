package com.benkio.youtuboanchei0bot.data

import com.benkio.telegrambotinfrastructure.model.reply.vid
import com.benkio.telegrambotinfrastructure.model.reply.ReplyBundleMessage

object Video {

  def messageRepliesVideoData: List[ReplyBundleMessage] = List(
    ReplyBundleMessage.textToVideo(
      "in america",
      "dove ho sempre desiderato",
      "(posto|carico) i video".r,
      "(restiamo|teniamo) in contatto".r,
      "(attraverso i|nei) commenti".r,
      "sto risolvendo"
    )(
      vid"ytai_SognoAmericano.mp4"
    ),
    ReplyBundleMessage.textToVideo(
      "senape",
      "non è scaduta",
      "ha un gusto strano",
      "non ne mangio più"
    )(
      vid"ytai_Senape.mp4"
    ),
    ReplyBundleMessage.textToVideo(
      "gigantesco",
      "sushi",
      "che bellezza"
    )(
      vid"ytai_SushiQuestoEGigantescoBellezza.mp4"
    )
  )
} // end Video
