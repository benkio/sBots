package com.benkio.chatcore.model

final case class Message(
    messageId: Option[MessageId],
    date: Long,
    chatId: ChatId,
    chatType: String,
    text: Option[String] = None,
    caption: Option[String] = None,
    newChatMembers: List[User] = List.empty, // the joined members other than the bot itself
    botAdded: Boolean = false,               // the bot itself is among the joined members
    leftChatMember: Option[User] = None,
    isForward: Boolean = false
)
