package com.benkio.richardphjbensonbot.data

import com.benkio.telegrambotinfrastructure.model.NewMemberTrigger
import com.benkio.telegrambotinfrastructure.model.LeftMemberTrigger
import com.benkio.telegrambotinfrastructure.model.reply.GifFile
import com.benkio.telegrambotinfrastructure.model.reply.MediaReply
import com.benkio.telegrambotinfrastructure.model.reply.ReplyBundleMessage
import cats.Applicative

object Special {

  def messageRepliesSpecialData[F[_]: Applicative]: List[ReplyBundleMessage[F]] = List(
    ReplyBundleMessage(
      trigger = NewMemberTrigger,
      reply = MediaReply.fromList(
        List(
          GifFile("rphjb_QuestaPersonaScusate.mp4", true)
        )
      )
    ),
    ReplyBundleMessage(
      trigger = LeftMemberTrigger,
      reply = MediaReply.fromList(
        List(
          GifFile("rphjb_LevatiDaiCoglioni.mp4", true)
        )
      )
    )
  )

}
