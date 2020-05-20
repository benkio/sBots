package com.benkio.telegramBotInfrastructure.model

import scala.util.Random

sealed trait ReplySelection {
  def logic: List[Reply] => List[Reply]
}

case object SelectAll extends ReplySelection {
  def logic = identity
}
case object RandomSelection extends ReplySelection {
  def logic = (replies: List[Reply]) => List(replies(Random.nextInt(replies.size)))
}
