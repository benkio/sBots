package com.benkio.youtuboanchei0bot.data

import com.benkio.telegrambotinfrastructure.model.reply.ReplyBundleMessage
import cats.Applicative
import com.benkio.telegrambotinfrastructure.model.reply.vid
import com.benkio.telegrambotinfrastructure.model.tr
object Video:

  def messageRepliesVideoData[
      F[_]: Applicative
  ]: List[ReplyBundleMessage[F]] = List(
    ReplyBundleMessage.textToVideo[F](
      "in america",
      "(posto|carico) i video".r.tr(13),
      "(restiamo|teniamo) in contatto".r.tr(19),
      "(attraverso i|nei) commenti".r.tr(12),
      "sto risolvendo"
    )(
      vid"ytai_SognoAmericano.mp4"
    ),
    ReplyBundleMessage.textToVideo[F](
      "senape",
      "non è scaduta",
      "ha un gusto strano",
      "non ne mangio più",
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
