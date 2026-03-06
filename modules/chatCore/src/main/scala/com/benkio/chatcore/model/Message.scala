package com.benkio.chatcore.model

final case class Message(
    messageId: Int,
    date: Long,
    chatId: ChatId,
    chatType: String,
    text: Option[String] = None,
    caption: Option[String] = None,
    newChatMembers: List[User] = List.empty,
    leftChatMember: Option[User] = None,
    isForward: Boolean = false
)
