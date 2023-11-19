package com.benkio.richardphjbensonbot.data

import cats.Applicative
import com.benkio.telegrambotinfrastructure.model.*

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
