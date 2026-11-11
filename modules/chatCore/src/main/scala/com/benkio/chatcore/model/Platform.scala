package com.benkio.chatcore.model

import cats.Show

enum Platform(val id: String) {
  case Telegram extends Platform("telegram")
  case Discord  extends Platform("discord")
  case Irc      extends Platform("irc")
  case Slack    extends Platform("slack")
}

object Platform {
  given Show[Platform] = Show.show(_.id)

  def fromId(id: String): Either[String, Platform] =
    Platform.values.find(_.id == id.toLowerCase).toRight(s"Unknown platform: $id")
}
