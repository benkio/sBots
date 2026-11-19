package com.benkio.chatcore

import com.benkio.chatcore.model.reply.ReplyValue
import com.benkio.chatcore.model.CommandSyntax
import com.benkio.chatcore.model.Message

/** The code of a bot behind an effectful reply of the `Callback` kind. It gets the message as the platform delivered
  * it, with the command syntax of the platform, to read the command as the user wrote it (`/command`, `!command`...).
  */
type EffectfulCallback[F[_]] = (Message, CommandSyntax) => F[ReplyValue]
