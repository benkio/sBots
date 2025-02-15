package com.benkio.richardphjbensonbot.data

import cats.Applicative
import com.benkio.telegrambotinfrastructure.model.reply.GifFile
import com.benkio.telegrambotinfrastructure.model.reply.MediaReply
import com.benkio.telegrambotinfrastructure.model.reply.ReplyBundleMessage
import com.benkio.telegrambotinfrastructure.model.LeftMemberTrigger
import com.benkio.telegrambotinfrastructure.model.NewMemberTrigger

object Special {

  def messageRepliesSpecialData[F[_]: Applicative]: List[ReplyBundleMessage[F]] = List(
    ReplyBundleMessage(
      trigger = NewMemberTrigger,
      reply = MediaReply.fromList(
        List(
          GifFile("rphjb_QuestaPersonaScusateGif.mp4", true)
        )
      )
    ),
    ReplyBundleMessage(
      trigger = LeftMemberTrigger,
      reply = MediaReply.fromList(
        List(
          GifFile("rphjb_LevatiDaiCoglioniGif.mp4", true)
        )
      )
    )
  )

}
