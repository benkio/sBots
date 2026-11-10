package com.benkio.chatcore.repository.db

import com.benkio.chatcore.model.reply.ReplyBundleMessage

/** The reply bundles of the messages of a bot. Loaded once at startup from its replies JSON and served from memory: it
  * sits next to the other repositories of the `DBLayer`, but there is no database behind it.
  */
trait DBMessageReplies {
  def messageReplies: List[ReplyBundleMessage]
}

object DBMessageReplies {

  val empty: DBMessageReplies = DBMessageReplies(List.empty)

  def apply(replies: List[ReplyBundleMessage]): DBMessageReplies =
    new DBMessageReplies {
      override val messageReplies: List[ReplyBundleMessage] = replies
    }
}
