package com.benkio.youtuboanchei0bot.data

import cats.Applicative
import com.benkio.telegrambotinfrastructure.model.reply.vid
import com.benkio.telegrambotinfrastructure.model.reply.ReplyBundleMessage
import com.benkio.telegrambotinfrastructure.model.tr
object Video:

  def messageRepliesVideoData[
      F[_]: Applicative
  ]: List[ReplyBundleMessage[F]] = List(
    ReplyBundleMessage.textToVideo[F](
      "in america",
      "dove ho sempre desiderato",
      "(posto|carico) i video".r.tr,
      "(restiamo|teniamo) in contatto".r.tr,
      "(attraverso i|nei) commenti".r.tr,
      "sto risolvendo"
    )(
      vid"ytai_SognoAmericano.mp4"
    ),
    ReplyBundleMessage.textToVideo[F](
      "senape",
      "non è scaduta",
      "ha un gusto strano",
      "non ne mangio più"
    )(
      vid"ytai_Senape.mp4"
    ),
    ReplyBundleMessage.textToVideo[F](
      "gigantesco",
      "sushi",
      "che bellezza"
    )(
      vid"ytai_SushiQuestoEGigantescoBellezza.mp4"
    )
  )
end Video
