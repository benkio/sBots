package com.benkio.chatcore.repository.db

import cats.effect.Async
import cats.implicits.*
import com.benkio.chatcore.config.SBotConfig
import com.benkio.chatcore.model.reply.ReplyBundleCommand
import com.benkio.chatcore.model.reply.ReplyBundleMessage
import com.benkio.chatcore.repository.JsonDataRepository
import doobie.Transactor
import log.effect.LogWriter

final case class DBLayer[F[_]](
    dbTimeout: DBTimeout[F],
    dbMedia: DBMedia[F],
    dbSubscription: DBSubscription[F],
    dbLog: DBLog[F],
    dbShow: DBShow[F],
    dbMessageReplies: DBMessageReplies = DBMessageReplies.empty,
    dbCommandReplies: DBCommandReplies = DBCommandReplies.empty
)

object DBLayer {

  /** The replies and the commands of the bot are loaded once, from its JSON files, and served from memory by the
    * `DBLayer`: to the background job manager and to the bot.
    */
  def loadReplies[F[_]: Async](
      dbLayer: DBLayer[F],
      jsonDataRepository: JsonDataRepository[F],
      sBotConfig: SBotConfig
  ): F[DBLayer[F]] = for {
    messageReplies <- jsonDataRepository.loadData[ReplyBundleMessage](sBotConfig.repliesJsonFilename)
    commandReplies <- jsonDataRepository.loadData[ReplyBundleCommand](sBotConfig.commandsJsonFilename)
  } yield dbLayer.copy(
    dbMessageReplies = DBMessageReplies(messageReplies),
    dbCommandReplies = DBCommandReplies(commandReplies)
  )

  def apply[F[_]: Async](
      transactor: Transactor[F]
  )(using log: LogWriter[F]): F[DBLayer[F]] = for {
    dbMedia <- DBMedia[F](transactor)
    dbSubscription = DBSubscription[F](transactor)
    dbTimeout      = new DBTimeout.DBTimeoutImpl[F](
      transactor,
      log
    )
    dbLog  = DBLog[F](transactor)
    dbShow = DBShow(transactor)
  } yield DBLayer[F](
    dbTimeout = dbTimeout,
    dbSubscription = dbSubscription,
    dbMedia = dbMedia,
    dbLog = dbLog,
    dbShow = dbShow
  )
}
