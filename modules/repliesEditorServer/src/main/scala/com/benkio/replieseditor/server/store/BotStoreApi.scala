package com.benkio.replieseditor.server.store

import cats.effect.IO
import com.benkio.chatcore.model.reply.ReplyBundleMessage
import com.benkio.replieseditor.server.module.ApiBot
import com.benkio.replieseditor.server.module.ApiError
import com.benkio.replieseditor.server.module.RepliesChunk
import com.benkio.replieseditor.server.module.SaveOk
import io.circe.Json

trait BotStoreApi {
  def listBots: IO[Vector[ApiBot]]

  def getReplies(botId: String): IO[Either[ApiError, Json]]
  def getRepliesChunk(botId: String, offset: Int, limit: Int): IO[Either[ApiError, RepliesChunk]]
  def getFilteredRepliesChunk(
      botId: String,
      message: String,
      offset: Int,
      limit: Int
  ): IO[Either[ApiError, RepliesChunk]]

  def getAllowedFiles(botId: String): IO[Either[ApiError, Vector[String]]]

  def updateReplyAt(botId: String, index: Int, value: Json): IO[Either[ApiError, Unit]]
  def insertAt(botId: String, index: Int, value: Json): IO[Either[ApiError, Int]]
  def deleteAt(botId: String, index: Int): IO[Either[ApiError, Int]]

  def reloadBotFromDisk(botId: String): IO[Either[ApiError, Unit]]

  def commit(botId: String): IO[Either[ApiError, SaveOk]]
  def saveReplies(botId: String, replies: List[ReplyBundleMessage]): IO[Either[ApiError, SaveOk]]
}
