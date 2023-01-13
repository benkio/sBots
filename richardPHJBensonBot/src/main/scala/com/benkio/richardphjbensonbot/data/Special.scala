package com.benkio.richardphjbensonbot.data

import com.benkio.telegrambotinfrastructure.model._

object Special {

  def messageRepliesSpecialData[F[_]]: List[ReplyBundleMessage[F]] = List(
    ReplyBundleMessage(
      NewMemberTrigger,
      List(
        MediaFile("rphjb_QuestaPersonaScusate.mp4", true)
      )
    ),
    ReplyBundleMessage(
      LeftMemberTrigger,
      List(
        MediaFile("rphjb_LevatiDaiCoglioni.mp4", true)
      )
    )
  )

}
