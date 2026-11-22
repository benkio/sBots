package com.benkio.chatcore.repository.db

import cats.effect.*
import cats.implicits.*
import com.benkio.chatcore.model.*
import com.benkio.chatcore.model.reply.*
import com.benkio.chatcore.model.show.Show
import com.benkio.chatcore.PagedListTextStore
import doobie.*
import doobie.implicits.*
import io.circe.*
import io.circe.parser.*
import io.circe.syntax.*
import log.effect.LogWriter

import java.time.LocalDate
import scala.collection.immutable.Vector
import scala.concurrent.duration.*

final case class DBPagedListTextData(
    platform: String,
    chat_id: String,
    bot_id: String,
    command_key: String,
    page: Int,
    page_items_json: String,
    request_json: String,
    saved_at_millis: Long
)

trait DBPagedListText[F[_]] {
  def save(
      chatId: ChatId,
      commandKey: CommandKey,
      page: Int,
      pageItems: List[PagedItem],
      request: Message,
      now: FiniteDuration
  ): F[Unit]
  def get(chatId: ChatId, now: FiniteDuration): F[Option[PagedListTextStore.Entry]]
  def isExpired(chatId: ChatId, now: FiniteDuration): F[Boolean]
  def size: F[Int]
}

object DBPagedListText {

  def apply[F[_]: Async](
      transactor: Transactor[F],
      botId: SBotInfo.SBotId,
      expiry: FiniteDuration,
      maxChats: Int
  )(using log: LogWriter[F]): DBPagedListText[F] =
    new DBPagedListTextImpl[F](transactor, botId, expiry, maxChats, log)

  private[db] object codecs {
    given Encoder[MessageId] = Encoder[String].contramap(_.value)
    given Decoder[MessageId] = Decoder[String].map(MessageId(_))

    given Encoder[ChatId] = Encoder.instance { cid =>
      Json.obj("platform" -> cid.platform.id.asJson, "id" -> cid.id.asJson)
    }
    given Decoder[ChatId] = Decoder.instance { c =>
      for {
        platformId <- c.downField("platform").as[String]
        id         <- c.downField("id").as[String]
        platform   <- Platform.fromId(platformId).leftMap(e => DecodingFailure(e, c.history))
      } yield ChatId(platform, id)
    }

    given Encoder[User] = Encoder.instance { u =>
      Json.obj("id" -> u.id.asJson, "isBot" -> u.isBot.asJson, "firstName" -> u.firstName.asJson)
    }
    given Decoder[User] = Decoder.instance { c =>
      for {
        id        <- c.downField("id").as[Long]
        isBot     <- c.downField("isBot").as[Boolean]
        firstName <- c.downField("firstName").as[String]
      } yield User(id, isBot, firstName)
    }

    given Encoder[Message] = Encoder.instance { m =>
      Json.obj(
        "messageId"      -> m.messageId.asJson,
        "date"           -> m.date.asJson,
        "chatId"         -> m.chatId.asJson,
        "chatType"       -> m.chatType.asJson,
        "text"           -> m.text.asJson,
        "caption"        -> m.caption.asJson,
        "newChatMembers" -> m.newChatMembers.asJson,
        "botAdded"       -> m.botAdded.asJson,
        "leftChatMember" -> m.leftChatMember.asJson,
        "isForward"      -> m.isForward.asJson,
        "authorId"       -> m.authorId.asJson
      )
    }
    given Decoder[Message] = Decoder.instance { c =>
      for {
        messageId      <- c.downField("messageId").as[Option[MessageId]]
        date           <- c.downField("date").as[Long]
        chatId         <- c.downField("chatId").as[ChatId]
        chatType       <- c.downField("chatType").as[String]
        text           <- c.downField("text").as[Option[String]]
        caption        <- c.downField("caption").as[Option[String]]
        newChatMembers <- c.downField("newChatMembers").as[List[User]]
        botAdded       <- c.downField("botAdded").as[Boolean]
        leftChatMember <- c.downField("leftChatMember").as[Option[User]]
        isForward      <- c.downField("isForward").as[Boolean]
        authorId       <- c.downField("authorId").as[Option[String]]
      } yield Message(
        messageId,
        date,
        chatId,
        chatType,
        text,
        caption,
        newChatMembers,
        botAdded,
        leftChatMember,
        isForward,
        authorId
      )
    }

    given Encoder[SBotInfo.SBotId] = Encoder[String].contramap(_.value)
    given Decoder[SBotInfo.SBotId] = Decoder[String].map(SBotInfo.SBotId(_))

    given Encoder[Show] = Encoder.instance { s =>
      Json.obj(
        "id"                     -> s.id.asJson,
        "botId"                  -> s.botId.asJson,
        "title"                  -> s.title.asJson,
        "uploadDate"             -> s.uploadDate.toString.asJson,
        "duration"               -> s.duration.asJson,
        "description"            -> s.description.asJson,
        "isLive"                 -> s.isLive.asJson,
        "originAutomaticCaption" -> s.originAutomaticCaption.asJson,
        "timestampMillis"        -> s.timestamp.map(_.toMillis).asJson
      )
    }
    given Decoder[Show] = Decoder.instance { c =>
      for {
        id         <- c.downField("id").as[String]
        botId      <- c.downField("botId").as[SBotInfo.SBotId]
        title      <- c.downField("title").as[String]
        uploadDate <- c
          .downField("uploadDate")
          .as[String]
          .flatMap(s => Either.catchNonFatal(LocalDate.parse(s)).leftMap(e => DecodingFailure(e.getMessage, c.history)))
        duration               <- c.downField("duration").as[Int]
        description            <- c.downField("description").as[Option[String]]
        isLive                 <- c.downField("isLive").as[Boolean]
        originAutomaticCaption <- c.downField("originAutomaticCaption").as[Option[String]]
        timestampMillis        <- c.downField("timestampMillis").as[Option[Long]]
      } yield Show(
        id = id,
        botId = botId,
        title = title,
        uploadDate = uploadDate,
        duration = duration,
        description = description,
        isLive = isLive,
        originAutomaticCaption = originAutomaticCaption,
        originAutomaticCaptionSrt = Vector.empty,
        timestamp = timestampMillis.map(_.millis)
      )
    }

    given Encoder[ReplyValue] = Encoder.instance { rv =>
      ReplyValue.encodeJson(rv) match {
        case Right(json)                                                 => json
        case Left(ReplyValue.EncodeError.UnsupportedAdapterValue(value)) =>
          throw new IllegalArgumentException(
            s"unsupported ReplyValue for paged list persistence: ${value.getClass.getName}"
          )
      }
    }

    given Encoder[PagedItem] = Encoder.instance {
      case PagedItem.ShowItem(show)     => Json.obj("type" -> "show".asJson, "show" -> show.asJson)
      case PagedItem.ValueItem(value)   => Json.obj("type" -> "value".asJson, "value" -> value.asJson)
      case PagedItem.TriggerItem(value) => Json.obj("type" -> "trigger".asJson, "value" -> value.asJson)
    }
    given Decoder[PagedItem] = Decoder.instance { c =>
      c.downField("type").as[String].flatMap {
        case "show"    => c.downField("show").as[Show].map(PagedItem.ShowItem(_))
        case "value"   => c.downField("value").as[ReplyValue].map(PagedItem.ValueItem(_))
        case "trigger" => c.downField("value").as[String].map(PagedItem.TriggerItem(_))
        case other     => Left(DecodingFailure(s"unsupported PagedItem type: $other", c.history))
      }
    }

    given Encoder[CommandKey] = Encoder[String].contramap(_.asString)
    given Decoder[CommandKey] =
      Decoder[String].emap(s => CommandKey.values.find(_.asString == s).toRight(s"unknown CommandKey: $s"))
  }

  private[db] class DBPagedListTextImpl[F[_]: Async](
      transactor: Transactor[F],
      botId: SBotInfo.SBotId,
      expiry: FiniteDuration,
      maxChats: Int,
      log: LogWriter[F]
  ) extends DBPagedListText[F] {
    import codecs.given

    private def isExpiredSavedAt(savedAtMillis: Long, now: FiniteDuration): Boolean =
      now.toMillis - savedAtMillis > expiry.toMillis

    private def allCount: ConnectionIO[Int] =
      sql"SELECT COUNT(*) FROM paged_list_text WHERE bot_id = ${botId.value}".query[Int].unique

    private def evictOldest: ConnectionIO[Unit] =
      allCount.flatMap { count =>
        if count <= maxChats then ().pure[ConnectionIO]
        else
          sql"""DELETE FROM paged_list_text
                WHERE bot_id = ${botId.value} AND (platform, chat_id) IN (
                  SELECT platform, chat_id FROM paged_list_text
                  WHERE bot_id = ${botId.value}
                  ORDER BY saved_at_millis ASC
                  LIMIT ${count - maxChats}
                )""".update.run.void
      }

    override def save(
        chatId: ChatId,
        commandKey: CommandKey,
        page: Int,
        pageItems: List[PagedItem],
        request: Message,
        now: FiniteDuration
    ): F[Unit] =
      for {
        jsons <- Async[F].catchNonFatal((pageItems.asJson.noSpaces, request.asJson.noSpaces))
        (itemsJson, requestJson) = jsons
        nowMillis                = now.toMillis
        _ <- (for {
          _ <-
            sql"""INSERT INTO paged_list_text (platform, chat_id, bot_id, command_key, page, page_items_json, request_json, saved_at_millis)
                          VALUES (${chatId.platform.id}, ${chatId.id}, ${botId.value}, ${commandKey.asString}, $page, $itemsJson, $requestJson, $nowMillis)
                          ON CONFLICT(platform, chat_id, bot_id) DO UPDATE SET
                            command_key = EXCLUDED.command_key,
                            page = EXCLUDED.page,
                            page_items_json = EXCLUDED.page_items_json,
                            request_json = EXCLUDED.request_json,
                            saved_at_millis = EXCLUDED.saved_at_millis""".update.run
          _ <- evictOldest
        } yield ()).transact(transactor)
        _ <- log.debug(
          s"[DBPagedListText] save chatId=$chatId botId=${botId.value} commandKey=${commandKey.asString} page=$page"
        )
      } yield ()

    private def decodeEntry(data: DBPagedListTextData): Option[PagedListTextStore.Entry] =
      for {
        commandKey <- CommandKey.values.find(_.asString == data.command_key)
        items      <- decode[List[PagedItem]](data.page_items_json).toOption
        request    <- decode[Message](data.request_json).toOption
      } yield PagedListTextStore.Entry(
        commandKey = commandKey,
        page = data.page,
        pageItems = items,
        request = request,
        savedAt = data.saved_at_millis.millis
      )

    override def get(chatId: ChatId, now: FiniteDuration): F[Option[PagedListTextStore.Entry]] =
      sql"""SELECT platform, chat_id, bot_id, command_key, page, page_items_json, request_json, saved_at_millis
            FROM paged_list_text
            WHERE platform = ${chatId.platform.id}
              AND chat_id = ${chatId.id}
              AND bot_id = ${botId.value}"""
        .query[DBPagedListTextData]
        .option
        .transact(transactor)
        .flatMap {
          case Some(data) if !isExpiredSavedAt(data.saved_at_millis, now) =>
            decodeEntry(data) match {
              case some @ Some(_) => some.pure[F]
              case None           =>
                log
                  .warn(s"[DBPagedListText] failed to decode paged list entry for chatId=$chatId botId=${botId.value}")
                  .as(none[PagedListTextStore.Entry])
            }
          case _ => none.pure[F]
        }

    override def isExpired(chatId: ChatId, now: FiniteDuration): F[Boolean] =
      sql"""SELECT saved_at_millis FROM paged_list_text
            WHERE platform = ${chatId.platform.id} AND chat_id = ${chatId.id} AND bot_id = ${botId.value}"""
        .query[Long]
        .option
        .map(_.exists(savedAt => isExpiredSavedAt(savedAt, now)))
        .transact(transactor)

    override def size: F[Int] =
      allCount.transact(transactor)
  }
}
