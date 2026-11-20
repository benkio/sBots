package com.benkio.chatdiscordadapter.outbound

import cats.effect.Clock
import cats.effect.Ref
import cats.effect.Sync
import cats.syntax.all.*
import cats.Monad
import com.benkio.chatcore.model.reply.PagedList
import com.benkio.chatcore.model.Message

import scala.concurrent.duration.*

/** A list on Discord: who asked for it, the message that asked (the command is run again from it for another page) and
  * the page shown.
  */
final case class DiscordButtonList(requestedBy: Option[String], request: Message, pagedList: PagedList)

/** The lists with buttons that are on the channels, by the id of their message, in memory: a restart or the expiry
  * forget them and their buttons answer that the list is gone. Bounded: the oldest are dropped.
  */
class DiscordButtonLists[F[_]: Monad: Clock](
    lists: Ref[F, Map[String, (FiniteDuration, DiscordButtonList)]],
    timeToLive: FiniteDuration,
    maxLists: Int
) {

  def put(messageId: String, list: DiscordButtonList): F[Unit] =
    Clock[F].realTime.flatMap(now =>
      lists.update(current =>
        (current.filter { case (_, (at, _)) => now - at <= timeToLive } + (messageId -> (now, list))).toList
          .sortBy { case (_, (at, _)) => at }
          .takeRight(maxLists)
          .toMap
      )
    )

  def get(messageId: String): F[Option[DiscordButtonList]] =
    Clock[F].realTime.flatMap(now =>
      lists.get.map(_.get(messageId).collect { case (at, list) if now - at <= timeToLive => list })
    )

  def remove(messageId: String): F[Unit] = lists.update(_ - messageId)
}

object DiscordButtonLists {

  def create[F[_]: Sync: Clock](
      timeToLive: FiniteDuration = 30.minutes,
      maxLists: Int = 500
  ): F[DiscordButtonLists[F]] =
    Ref
      .of[F, Map[String, (FiniteDuration, DiscordButtonList)]](Map.empty)
      .map(new DiscordButtonLists[F](_, timeToLive, maxLists))
}
