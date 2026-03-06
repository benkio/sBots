package com.benkio.replieseditor.server.mocks

import cats.effect.IO
import com.benkio.chatcore.model.reply.ReplyBundleMessage
import com.benkio.replieseditor.server.module.ApiBot
import com.benkio.replieseditor.server.module.ApiError
import com.benkio.replieseditor.server.module.RepliesChunk
import com.benkio.replieseditor.server.module.SaveOk
import com.benkio.replieseditor.server.store.BotStoreApi
import io.circe.Json

final class MockBotStore(
    var listBotsF: () => IO[Vector[ApiBot]] = () => IO.pure(Vector.empty),
    var getRepliesF: String => IO[Either[ApiError, Json]] = _ => IO.pure(Left(ApiError("unconfigured getReplies"))),
    var getRepliesChunkF: (String, Int, Int) => IO[Either[ApiError, RepliesChunk]] = (_, _, _) =>
      IO.pure(Left(ApiError("unconfigured getRepliesChunk"))),
    var getFilteredRepliesChunkF: (String, String, Int, Int) => IO[Either[ApiError, RepliesChunk]] = (_, _, _, _) =>
      IO.pure(Left(ApiError("unconfigured getFilteredRepliesChunk"))),
    var getAllowedFilesF: String => IO[Either[ApiError, Vector[String]]] = _ =>
      IO.pure(Left(ApiError("unconfigured getAllowedFiles"))),
    var updateReplyAtF: (String, Int, Json) => IO[Either[ApiError, Unit]] = (_, _, _) =>
      IO.pure(Left(ApiError("unconfigured updateReplyAt"))),
    var insertAtF: (String, Int, Json) => IO[Either[ApiError, Int]] = (_, _, _) =>
      IO.pure(Left(ApiError("unconfigured insertAt"))),
    var deleteAtF: (String, Int) => IO[Either[ApiError, Int]] = (_, _) =>
      IO.pure(Left(ApiError("unconfigured deleteAt"))),
    var reloadBotFromDiskF: String => IO[Either[ApiError, Unit]] = _ =>
      IO.pure(Left(ApiError("unconfigured reloadBotFromDisk"))),
    var commitF: String => IO[Either[ApiError, SaveOk]] = _ => IO.pure(Left(ApiError("unconfigured commit"))),
    var saveRepliesF: (String, List[ReplyBundleMessage]) => IO[Either[ApiError, SaveOk]] = (_, _) =>
      IO.pure(Left(ApiError("unconfigured saveReplies")))
) extends BotStoreApi {

  override def listBots: IO[Vector[ApiBot]] = listBotsF()

  override def getReplies(botId: String): IO[Either[ApiError, Json]] =
    getRepliesF(botId)

  override def getRepliesChunk(botId: String, offset: Int, limit: Int): IO[Either[ApiError, RepliesChunk]] =
    getRepliesChunkF(botId, offset, limit)

  override def getFilteredRepliesChunk(
      botId: String,
      message: String,
      offset: Int,
      limit: Int
  ): IO[Either[ApiError, RepliesChunk]] =
    getFilteredRepliesChunkF(botId, message, offset, limit)

  override def getAllowedFiles(botId: String): IO[Either[ApiError, Vector[String]]] =
    getAllowedFilesF(botId)

  override def updateReplyAt(botId: String, index: Int, value: Json): IO[Either[ApiError, Unit]] =
    updateReplyAtF(botId, index, value)

  override def insertAt(botId: String, index: Int, value: Json): IO[Either[ApiError, Int]] =
    insertAtF(botId, index, value)

  override def deleteAt(botId: String, index: Int): IO[Either[ApiError, Int]] =
    deleteAtF(botId, index)

  override def reloadBotFromDisk(botId: String): IO[Either[ApiError, Unit]] =
    reloadBotFromDiskF(botId)

  override def commit(botId: String): IO[Either[ApiError, SaveOk]] =
    commitF(botId)

  override def saveReplies(botId: String, replies: List[ReplyBundleMessage]): IO[Either[ApiError, SaveOk]] =
    saveRepliesF(botId, replies)
}
