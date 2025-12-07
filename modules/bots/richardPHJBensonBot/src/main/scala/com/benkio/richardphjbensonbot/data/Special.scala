package com.benkio.richardphjbensonbot.data


import com.benkio.telegrambotinfrastructure.messagefiltering.MessageMatches
import com.benkio.telegrambotinfrastructure.model.reply.GifFile
import com.benkio.telegrambotinfrastructure.model.reply.MediaReply
import com.benkio.telegrambotinfrastructure.model.reply.ReplyBundleMessage
import com.benkio.telegrambotinfrastructure.model.LeftMemberTrigger
import com.benkio.telegrambotinfrastructure.model.NewMemberTrigger

object Special {

  def messageRepliesSpecialData: List[ReplyBundleMessage] = List(
    ReplyBundleMessage(
      trigger = NewMemberTrigger,
      reply = MediaReply.fromList(
        List(
          GifFile("rphjb_QuestaPersonaScusateGif.mp4", true)
        )
      ),
      matcher = MessageMatches.ContainsOnce
    ),
    ReplyBundleMessage(
      trigger = LeftMemberTrigger,
      reply = MediaReply.fromList(
        List(
          GifFile("rphjb_LevatiDaiCoglioniGif.mp4", true)
        )
      ),
      matcher = MessageMatches.ContainsOnce
    )
  )

}
