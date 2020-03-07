package com.benkio.telegramBotInfrastructure.model

import java.time.Instant
import scala.concurrent.duration._

object Timeout {

  def isWithinTimeout(inputDate : Int, timeout : Option[Duration]) : Boolean = {
    val nowUnixSeconds : Long = Instant.now().getEpochSecond()
    timeout.map(t =>
      (nowUnixSeconds - inputDate.toLong) < t.toSeconds
    ).getOrElse(true)

  }
}
