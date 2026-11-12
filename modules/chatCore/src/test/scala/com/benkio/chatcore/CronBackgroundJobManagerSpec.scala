package com.benkio.chatcore

import cats.effect.kernel.Ref
import cats.effect.std.Random
import cats.effect.IO
import cats.syntax.all.*
import com.benkio.chatcore.mocks.DBLayerMock
import com.benkio.chatcore.mocks.FakePlatformAdapter
import com.benkio.chatcore.model.reply.GifFile
import com.benkio.chatcore.model.reply.Markdown
import com.benkio.chatcore.model.reply.Mp3File
import com.benkio.chatcore.model.reply.ReplyBundleMessage
import com.benkio.chatcore.model.reply.ReplyValue
import com.benkio.chatcore.model.reply.Text
import com.benkio.chatcore.model.ChatId
import com.benkio.chatcore.model.MediaKind
import com.benkio.chatcore.model.Platform
import com.benkio.chatcore.model.PlatformCapabilities
import com.benkio.chatcore.model.SBotInfo
import com.benkio.chatcore.model.Subscription
import com.benkio.chatcore.model.SubscriptionId
import com.benkio.chatcore.model.SubscriptionType
import com.benkio.chatcore.repository.db.DBMediaData
import com.benkio.chatcore.repository.db.DBSubscriptionData
import com.benkio.chatcore.Logger.given
import cron4s.*
import munit.CatsEffectSuite

import java.time.Instant
import java.util.UUID

class CronBackgroundJobManagerSpec extends CatsEffectSuite {

  private val sBotInfo: SBotInfo = SBotInfo(botId = SBotInfo.SBotId("eng"), botName = SBotInfo.SBotName("EngineBot"))
  private val ircChat: ChatId    = ChatId(Platform.Irc, "#sbots")

  private val textOnly: PlatformCapabilities   = FakePlatformAdapter.textOnlyCapabilities
  private val everything: PlatformCapabilities = textOnly.copy(
    platform = Platform.Telegram,
    supportedMedia = MediaKind.values.toSet,
    maxTextLength = 4096,
    markdown = com.benkio.chatcore.model.MarkdownSupport.Native,
    supportsDelete = true
  )

  private val mp3Data: DBMediaData = DBMediaData(
    media_name = "a.mp3",
    bot_id = "eng",
    kinds = "[]",
    mime_type = "audio/mpeg",
    media_sources = """["https://example.com/a.mp3"]""",
    media_count = 0,
    created_at = "1"
  )

  private def subscription(subscriptionType: SubscriptionType, target: Option[String]): Subscription = Subscription(
    id = SubscriptionId(UUID.randomUUID),
    chatId = ircChat,
    botId = sBotInfo.botId,
    subscriptionType = subscriptionType,
    target = target,
    cron = Cron.unsafeParse("0 0 12 * * ?"),
    subscribedAt = Instant.ofEpochSecond(1)
  )

  private final case class Sent(chatId: ChatId, value: ReplyValue, replyToMessage: Boolean)

  /** Fires the subscription once and gives back what is sent through the injected `send`. */
  private def fire(
      subscription: Subscription,
      capabilities: PlatformCapabilities,
      messageReplies: List[ReplyBundleMessage] = List.empty,
      calendar: Markdown = Markdown("**calendar**")
  ): IO[List[Sent]] = for {
    sent    <- Ref.of[IO, List[Sent]](List.empty)
    random  <- Random.scalaUtilRandom[IO]
    manager <- {
      given Random[IO] = random
      CronBackgroundJobManager[IO](
        dbLayer = DBLayerMock.mock(sBotInfo.botId, medias = List(mp3Data), messageReplies = messageReplies),
        sBotInfo = sBotInfo,
        ttl = None,
        capabilities = capabilities,
        send = (chatId, value, replyToMessage) => sent.update(_ :+ Sent(chatId, value, replyToMessage)),
        calendarMessage = (_, _) => Some(calendar)
      )
    }
    streamAndCancel <- manager.runSubscription(subscription)
    _               <- streamAndCancel._1.compile.drain
    result          <- sent.get
  } yield result

  test("a trigger subscription sends the reply of the target to the chat, whatever the platform of the chat") {
    fire(
      subscription = subscription(SubscriptionType.Trigger, Some("ciao")),
      capabilities = textOnly,
      messageReplies = List(ReplyBundleMessage.textToText("ciao")("hello"))
    ).map(sent => assertEquals(sent, List(Sent(ircChat, Text("hello"), replyToMessage = false))))
  }

  test("a trigger subscription whose target matches nothing sends nothing") {
    fire(
      subscription = subscription(SubscriptionType.Trigger, Some("something else")),
      capabilities = textOnly,
      messageReplies = List(ReplyBundleMessage.textToText("ciao")("hello"))
    ).map(sent => assertEquals(sent, List.empty))
  }

  test("a media the platform cannot send is sent as its link") {
    fire(
      subscription = subscription(SubscriptionType.Trigger, Some("song")),
      capabilities = textOnly,
      messageReplies = List(ReplyBundleMessage.textToMp3("song")(Mp3File("a.mp3")))
    ).map(sent => assertEquals(sent.map(_.value), List(Text("https://example.com/a.mp3"))))
  }

  test("a media the platform cannot send and the DB does not know is dropped") {
    fire(
      subscription = subscription(SubscriptionType.Trigger, Some("clip")),
      capabilities = textOnly,
      messageReplies = List(ReplyBundleMessage.textToGif("clip")(GifFile("unknownGif.mp4")))
    ).map(sent => assertEquals(sent, List.empty))
  }

  test("a media the platform can send is sent as it is") {
    fire(
      subscription = subscription(SubscriptionType.Trigger, Some("song")),
      capabilities = everything,
      messageReplies = List(ReplyBundleMessage.textToMp3("song")(Mp3File("a.mp3")))
    ).map(sent => assertEquals(sent.map(_.value), List(Mp3File("a.mp3"))))
  }

  test("the calendar goes through the transformer: plain text on a platform without markdown") {
    fire(subscription = subscription(SubscriptionType.Calendar, None), capabilities = textOnly).map(sent =>
      assertEquals(sent, List(Sent(ircChat, Text("calendar"), replyToMessage = true)))
    )
  }

  test("the calendar is left as it is on a platform with markdown") {
    fire(subscription = subscription(SubscriptionType.Calendar, None), capabilities = everything).map(sent =>
      assertEquals(sent.map(_.value), List(Markdown("**calendar**")))
    )
  }

  test("a calendar over the text limit of the platform is sent in more messages") {
    val longCalendar = Markdown(List.fill(10)("a" * 90).mkString("\n"))
    fire(subscription = subscription(SubscriptionType.Calendar, None), capabilities = textOnly, calendar = longCalendar)
      .map { sent =>
        assert(sent.size > 1, sent.toString)
        assert(sent.forall(_.value.show.length <= textOnly.maxTextLength), sent.toString)
      }
  }

  private def storedSubscription(platform: Platform, chatId: String): DBSubscriptionData = DBSubscriptionData(
    id = UUID.randomUUID().toString,
    platform = platform.id,
    chat_id = chatId,
    bot_id = sBotInfo.botId.value,
    cron = "0 0 12 * * ?",
    subscribed_at = "1"
  )

  /** The chats of the subscriptions a manager of the platform loads from the DB, where every platform has some. */
  private def loadedChats(capabilities: PlatformCapabilities): IO[Set[ChatId]] =
    for {
      random  <- Random.scalaUtilRandom[IO]
      manager <- {
        given Random[IO] = random
        CronBackgroundJobManager[IO](
          dbLayer = DBLayerMock.mock(
            sBotInfo.botId,
            subscriptions = List(
              storedSubscription(Platform.Telegram, "1"),
              storedSubscription(Platform.Irc, "#sbots"),
              storedSubscription(Platform.Irc, "#other"),
              storedSubscription(Platform.Discord, "1")
            )
          ),
          sBotInfo = sBotInfo,
          ttl = None,
          capabilities = capabilities,
          send = (_, _, _) => IO.unit
        )
      }
    } yield manager.getScheduledSubscriptions().map(_.chatId).toSet

  test("a manager loads the subscriptions of its platform only, the bot can run on others with a manager each") {
    for {
      irc      <- loadedChats(textOnly)
      telegram <- loadedChats(everything)
      slack    <- loadedChats(textOnly.copy(platform = Platform.Slack))
    } yield {
      assertEquals(irc, Set(ChatId(Platform.Irc, "#sbots"), ChatId(Platform.Irc, "#other")))
      assertEquals(telegram, Set(ChatId(Platform.Telegram, "1")))
      assertEquals(slack, Set.empty[ChatId])
    }
  }
}
