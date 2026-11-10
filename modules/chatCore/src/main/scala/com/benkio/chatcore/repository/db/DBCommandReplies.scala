package com.benkio.chatcore.repository.db

import com.benkio.chatcore.model.reply.ReplyBundleCommand

/** The reply bundles of the commands of a bot. Loaded once at startup from its commands JSON and served from memory: it
  * sits next to the other repositories of the `DBLayer`, but there is no database behind it.
  */
trait DBCommandReplies {
  def commandReplies: List[ReplyBundleCommand]
}

object DBCommandReplies {

  val empty: DBCommandReplies = DBCommandReplies(List.empty)

  def apply(replies: List[ReplyBundleCommand]): DBCommandReplies =
    new DBCommandReplies {
      override val commandReplies: List[ReplyBundleCommand] = replies
    }
}
