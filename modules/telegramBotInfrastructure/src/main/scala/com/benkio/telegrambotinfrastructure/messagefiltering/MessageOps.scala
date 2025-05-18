package com.benkio.telegrambotinfrastructure.messagefiltering

import telegramium.bots.Message

extension (msg: Message)
  def isCommand: Boolean =
    msg.text.fold(false)(t => t.startsWith("/"))
  def getContent: Option[String] =
    msg.text.orElse(msg.caption)
