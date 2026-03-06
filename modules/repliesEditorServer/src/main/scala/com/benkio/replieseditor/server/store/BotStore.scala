package com.benkio.replieseditor.server.store

import cats.effect.IO
import cats.effect.Ref
import cats.syntax.all.*
import com.benkio.chatcore.messagefiltering.MessageMatches
import com.benkio.chatcore.model.reply.ReplyBundleMessage
import com.benkio.chatcore.model.LeftMemberTrigger
import com.benkio.chatcore.model.MessageLengthTrigger
import com.benkio.chatcore.model.NewMemberTrigger
import com.benkio.chatcore.model.RegexTextTriggerValue
import com.benkio.chatcore.model.StringTextTriggerValue
import com.benkio.chatcore.model.TextTrigger
import com.benkio.chatcore.model.TextTriggerValue
import com.benkio.replieseditor.server.jsonio.ListJsonFile
import com.benkio.replieseditor.server.jsonio.RepliesJsonFile
import com.benkio.replieseditor.server.jsonio.TriggersTxtFile
import com.benkio.replieseditor.server.module.ApiBot
import com.benkio.replieseditor.server.module.ApiError
import com.benkio.replieseditor.server.module.BotFiles
import com.benkio.replieseditor.server.module.IndexedReply
import com.benkio.replieseditor.server.module.RepliesChunk
import com.benkio.replieseditor.server.module.SaveOk
import com.benkio.replieseditor.server.validation.MediaFilesAllowedValidation
import io.circe.syntax.*
import io.circe.Json

import java.nio.file.Files
import java.nio.file.Path
import scala.jdk.CollectionConverters.*

final class BotStore private (ref: Ref[IO, BotStore.State]) extends BotStoreApi {

  private def trimTextTriggerValue(tv: TextTriggerValue): TextTriggerValue =
    tv match {
      case StringTextTriggerValue(v)             => StringTextTriggerValue(v.trim)
      case RegexTextTriggerValue(r, regexLength) =>
        val original = r.toString
        val trimmed  = original.trim
        RegexTextTriggerValue(trimmed.r, regexLength)
    }

  private def trimReplyTriggers(rbm: ReplyBundleMessage): ReplyBundleMessage =
    rbm.trigger match {
      case TextTrigger(triggers*) =>
        val trimmed = triggers.toList.map(trimTextTriggerValue)
        rbm.copy(trigger = TextTrigger(trimmed*))
      case _ => rbm
    }

  def listBots: IO[Vector[ApiBot]] =
    ref.get.map(
      _.bots
        .filter(b => b.files.botName.endsWith("Bot"))
        .map(b => ApiBot(b.files.botId, b.files.botName))
    )

  def getReplies(botId: String): IO[Either[ApiError, Json]] =
    ref.get.map(_.byId.get(botId) match {
      case None    => Left(ApiError(s"Unknown botId: $botId"))
      case Some(b) =>
        b.repliesJson match {
          case Left(err)   => Left(ApiError(s"Failed to load replies for $botId: $err"))
          case Right(json) => Right(json)
        }
    })

  def getRepliesChunk(botId: String, offset: Int, limit: Int): IO[Either[ApiError, RepliesChunk]] =
    ref.get.map(_.byId.get(botId) match {
      case None    => Left(ApiError(s"Unknown botId: $botId"))
      case Some(b) =>
        b.repliesEntries match {
          case Left(err)      => Left(ApiError(s"Failed to load replies for $botId: $err"))
          case Right(entries) =>
            val safeOffset = offset.max(0).min(entries.length)
            val safeLimit  = limit.max(1).min(500)
            val items      =
              entries
                .slice(safeOffset, (safeOffset + safeLimit).min(entries.length))
                .zipWithIndex
                .map { case (j, i) => IndexedReply(index = safeOffset + i, value = j) }
            Right(RepliesChunk(total = entries.length, offset = safeOffset, items = items))
        }
    })

  def getFilteredRepliesChunk(
      botId: String,
      message: String,
      offset: Int,
      limit: Int
  ): IO[Either[ApiError, RepliesChunk]] =
    ref.get.map(_.byId.get(botId) match {
      case None    => Left(ApiError(s"Unknown botId: $botId"))
      case Some(b) =>
        b.repliesEntries match {
          case Left(err)      => Left(ApiError(s"Failed to load replies for $botId: $err"))
          case Right(entries) =>
            val msgLower                     = message.toLowerCase
            val matchingIndexes: Vector[Int] =
              entries.zipWithIndex.flatMap { case (j, idx) =>
                j.as[ReplyBundleMessage].toOption match {
                  case None      => Vector.empty
                  case Some(rbm) =>
                    if matchesMessage(rbm, msgLower) then Vector(idx) else Vector.empty
                }
              }

            val safeOffset = offset.max(0).min(matchingIndexes.length)
            val safeLimit  = limit.max(1).min(500)
            val pageIdxs   = matchingIndexes.slice(safeOffset, (safeOffset + safeLimit).min(matchingIndexes.length))
            val items      = pageIdxs.map(i => IndexedReply(index = i, value = entries(i)))
            Right(RepliesChunk(total = matchingIndexes.length, offset = safeOffset, items = items))
        }
    })

  private def matchesMessage(rbm: ReplyBundleMessage, messageLower: String): Boolean =
    (rbm.matcher, rbm.trigger) match {
      case (_, MessageLengthTrigger(messageLength)) =>
        messageLower.length >= messageLength
      case (_, _: NewMemberTrigger.type)                         => false
      case (_, _: LeftMemberTrigger.type)                        => false
      case (MessageMatches.ContainsOnce, TextTrigger(triggers*)) =>
        triggers
          .sorted(using TextTriggerValue.orderingInstance.reverse)
          .exists(TextTriggerValue.matchValue(_, messageLower))
      case (MessageMatches.ContainsAll, TextTrigger(triggers*)) =>
        triggers.forall(TextTriggerValue.matchValue(_, messageLower))
      case _ => false
    }

  def getAllowedFiles(botId: String): IO[Either[ApiError, Vector[String]]] =
    ref.get.map(_.byId.get(botId) match {
      case None    => Left(ApiError(s"Unknown botId: $botId"))
      case Some(b) =>
        b.allowedFiles match {
          case Left(err)    => Left(ApiError(s"Failed to load allowed files for $botId: $err"))
          case Right(files) => Right(files)
        }
    })

  def updateReplyAt(botId: String, index: Int, value: Json): IO[Either[ApiError, Unit]] =
    ref.modify { st =>
      st.byId.get(botId) match {
        case None    => (st, Left(ApiError(s"Unknown botId: $botId")))
        case Some(b) =>
          b.repliesEntries match {
            case Left(err) =>
              (st, Left(ApiError(s"Replies for $botId are not loaded: $err")))
            case Right(entries) =>
              if index < 0 || index >= entries.length then (
                st,
                Left(ApiError(s"Index out of bounds: $index (size=${entries.length})"))
              )
              else {
                val updatedEntries = entries.updated(index, value)
                val updatedBot     =
                  b.copy(
                    repliesEntries = Right(updatedEntries),
                    repliesJson = Right(Json.fromValues(updatedEntries))
                  )
                val newState =
                  st.copy(
                    bots = st.bots.map(x => if x.files.botId == botId then updatedBot else x),
                    byId = st.byId.updated(botId, updatedBot)
                  )
                (newState, Right(()))
              }
          }
      }
    }

  def insertAt(botId: String, index: Int, value: Json): IO[Either[ApiError, Int]] =
    ref.modify { st =>
      st.byId.get(botId) match {
        case None    => (st, Left(ApiError(s"Unknown botId: $botId")))
        case Some(b) =>
          b.repliesEntries match {
            case Left(err) =>
              (st, Left(ApiError(s"Replies for $botId are not loaded: $err")))
            case Right(entries) =>
              val safeIndex      = index.max(0).min(entries.length)
              val updatedEntries = entries.patch(from = safeIndex, other = Vector(value), replaced = 0)
              val updatedBot     =
                b.copy(
                  repliesEntries = Right(updatedEntries),
                  repliesJson = Right(Json.fromValues(updatedEntries))
                )
              val newState =
                st.copy(
                  bots = st.bots.map(x => if x.files.botId == botId then updatedBot else x),
                  byId = st.byId.updated(botId, updatedBot)
                )
              (newState, Right(updatedEntries.length))
          }
      }
    }

  def deleteAt(botId: String, index: Int): IO[Either[ApiError, Int]] =
    ref.modify { st =>
      st.byId.get(botId) match {
        case None    => (st, Left(ApiError(s"Unknown botId: $botId")))
        case Some(b) =>
          b.repliesEntries match {
            case Left(err) =>
              (st, Left(ApiError(s"Replies for $botId are not loaded: $err")))
            case Right(entries) =>
              if index < 0 || index >= entries.length then (
                st,
                Left(ApiError(s"Index out of bounds: $index (size=${entries.length})"))
              )
              else {
                val updatedEntries = entries.patch(from = index, other = Nil, replaced = 1)
                val updatedBot     =
                  b.copy(
                    repliesEntries = Right(updatedEntries),
                    repliesJson = Right(Json.fromValues(updatedEntries))
                  )
                val newState =
                  st.copy(
                    bots = st.bots.map(x => if x.files.botId == botId then updatedBot else x),
                    byId = st.byId.updated(botId, updatedBot)
                  )
                (newState, Right(updatedEntries.length))
              }
          }
      }
    }

  def reloadBotFromDisk(botId: String): IO[Either[ApiError, Unit]] =
    ref.get.flatMap { st =>
      st.byId.get(botId) match {
        case None    => IO.pure(Left(ApiError(s"Unknown botId: $botId")))
        case Some(b) =>
          BotStore
            .loadCached(b.files)
            .attempt
            .flatMap {
              case Left(ex) =>
                IO.pure(Left(ApiError(s"Failed to reload bot $botId: ${ex.getMessage}")))
              case Right(reloaded) =>
                ref.update { st2 =>
                  if st2.byId.contains(botId) then st2.copy(
                    bots = st2.bots.map(x => if x.files.botId == botId then reloaded else x),
                    byId = st2.byId.updated(botId, reloaded)
                  )
                  else st2
                } *> IO.pure(Right(()))
            }
      }
    }

  def commit(botId: String): IO[Either[ApiError, SaveOk]] =
    ref.get.flatMap { st =>
      st.byId.get(botId) match {
        case None    => IO.pure(Left(ApiError(s"Unknown botId: $botId")))
        case Some(b) =>
          (b.repliesEntries, b.allowedFiles) match {
            case (Left(rErr), _) => IO.pure(Left(ApiError(s"Cannot commit, replies not loaded: $rErr")))
            case (_, Left(aErr)) => IO.pure(Left(ApiError(s"Cannot commit, allowed files not loaded: $aErr")))
            case (Right(entries), Right(allowedVec)) =>
              val json = Json.fromValues(entries)
              json.as[List[ReplyBundleMessage]] match {
                case Left(df)       => IO.pure(Left(ApiError(s"Cannot commit, decode failed: ${df.message}")))
                case Right(replies) =>
                  val trimmedReplies = replies.map(trimReplyTriggers)
                  MediaFilesAllowedValidation.validateAllFilesAreAllowed(trimmedReplies, allowedVec.toSet) match {
                    case Left(err) => IO.pure(Left(err))
                    case Right(_)  =>
                      val trimmedJson = trimmedReplies.asJson
                      for {
                        _ <- RepliesJsonFile.writePretty(b.files.repliesJson, trimmedJson.spaces2)
                        _ <- TriggersTxtFile.write(b.files.triggersTxt, trimmedReplies)
                        _ <- ref.update(_.updateReplies(botId, Right(trimmedJson)))
                      } yield Right(SaveOk(botId = botId, repliesCount = trimmedReplies.length))
                  }
              }
          }
      }
    }

  def saveReplies(
      botId: String,
      replies: List[ReplyBundleMessage]
  ): IO[Either[ApiError, SaveOk]] =
    ref.get.flatMap { st =>
      st.byId.get(botId) match {
        case None      => IO.pure(Left(ApiError(s"Unknown botId: $botId")))
        case Some(bot) =>
          bot.allowedFiles match {
            case Left(err) =>
              IO.pure(Left(ApiError(s"Cannot validate files, allowed files not loaded: $err")))
            case Right(allowedVec) =>
              val trimmedReplies = replies.map(trimReplyTriggers)
              val allowed        = allowedVec.toSet
              MediaFilesAllowedValidation.validateAllFilesAreAllowed(trimmedReplies, allowed) match {
                case Left(err) => IO.pure(Left(err))
                case Right(_)  =>
                  val json = trimmedReplies.asJson
                  for {
                    _ <- RepliesJsonFile.writePretty(bot.files.repliesJson, json.spaces2)
                    _ <- TriggersTxtFile.write(bot.files.triggersTxt, trimmedReplies)
                    _ <- ref.update(_.updateReplies(botId, Right(json)))
                  } yield Right(SaveOk(botId = botId, repliesCount = trimmedReplies.length))
              }
          }
      }
    }
}

object BotStore {

  final case class CachedBot(
      files: BotFiles,
      repliesEntries: Either[String, Vector[Json]],
      repliesJson: Either[String, Json],
      allowedFiles: Either[String, Vector[String]]
  )

  final case class State(bots: Vector[CachedBot], byId: Map[String, CachedBot]) {
    def updateReplies(botId: String, replies: Either[String, Json]): State =
      byId.get(botId) match {
        case None    => this
        case Some(b) =>
          val entriesE: Either[String, Vector[Json]] =
            replies.flatMap { j =>
              j.asArray match {
                case None      => Left("Replies JSON is not an array")
                case Some(arr) => Right(arr.toVector)
              }
            }
          val updated =
            b.copy(
              repliesEntries = entriesE,
              repliesJson = replies
            )
          copy(
            bots = bots.map(x => if x.files.botId == botId then updated else x),
            byId = byId.updated(botId, updated)
          )
      }
  }

  object State {
    val empty: State = State(Vector.empty, Map.empty)
  }

  def build(repoRoot: Path): IO[BotStore] =
    for {
      bots   <- scanBots(repoRoot)
      cached <- bots.traverse(loadCached)
      st = State(cached.toVector, cached.map(b => b.files.botId -> b).toMap)
      ref <- Ref.of[IO, State](st)
    } yield new BotStore(ref)

  private def scanBots(repoRoot: Path): IO[List[BotFiles]] =
    IO.blocking {
      val botsRoot = repoRoot.resolve("modules").resolve("bots")
      if !Files.isDirectory(botsRoot) then List.empty
      else {
        Files
          .list(botsRoot)
          .iterator()
          .asScala
          .toList
          .filter(Files.isDirectory(_))
          .flatMap { botDir =>
            val botName     = botDir.getFileName.toString
            val listJsonOpt =
              Files
                .newDirectoryStream(botDir, "*_list.json")
                .iterator()
                .asScala
                .toList
                .sortBy(_.getFileName.toString)
                .headOption

            listJsonOpt.flatMap { listJson =>
              val botId       = listJson.getFileName.toString.stripSuffix("_list.json")
              val repliesJson =
                botDir.resolve("src").resolve("main").resolve("resources").resolve(s"${botId}_replies.json")
              val triggersTxt = botDir.resolve(s"${botId}_triggers.txt")

              if Files.isRegularFile(repliesJson) && Files.isRegularFile(listJson)
              then Some(BotFiles(botId, botName, repliesJson, listJson, triggersTxt))
              else None
            }
          }
          .sortBy(_.botId)
      }
    }

  private def loadCached(files: BotFiles): IO[CachedBot] = {
    val repliesIO =
      RepliesJsonFile.read(files.repliesJson).attempt.map {
        case Left(ex)    => Left(ex.getMessage)
        case Right(json) => Right(json)
      }

    val allowedIO =
      ListJsonFile.readFilenamesSorted(files.listJson).attempt.map {
        case Left(ex)    => Left(ex.getMessage)
        case Right(list) => Right(list.toVector)
      }

    (repliesIO, allowedIO).mapN { (repliesJsonE, allowed) =>
      val repliesEntriesE: Either[String, Vector[Json]] =
        repliesJsonE.flatMap { json =>
          json.asArray match {
            case None      => Left("Replies JSON is not an array")
            case Some(arr) => Right(arr.toVector)
          }
        }
      CachedBot(files = files, repliesEntries = repliesEntriesE, repliesJson = repliesJsonE, allowedFiles = allowed)
    }
  }
}
