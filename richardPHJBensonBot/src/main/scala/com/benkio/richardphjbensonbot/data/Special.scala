package com.benkio.richardphjbensonbot.data

import cats._
import com.benkio.telegrambotinfrastructure.model._

object Special {

  def messageRepliesSpecialData[F[_]: Applicative]: List[ReplyBundleMessage[F]] = List(
    ReplyBundleMessage(
      NewMemberTrigger,
      List(
        MediaFile("rphjb_QuestaPersonaScusate.gif", true)
      )
    ),
    ReplyBundleMessage(
      LeftMemberTrigger,
      List(
        MediaFile("rphjb_LevatiDaiCoglioni.gif", true)
      )
    )
  )

}
