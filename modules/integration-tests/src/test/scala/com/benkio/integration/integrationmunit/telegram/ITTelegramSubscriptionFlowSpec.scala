package com.benkio.integration.integrationmunit.telegram

import cats.effect.IO
import cats.effect.Ref
import cats.effect.Resource
import cats.Applicative
import com.benkio.chatcore.mocks.NotifierMock.given
import com.benkio.chatcore.model.reply.EffectfulReply
import com.benkio.chatcore.model.reply.Markdown
import com.benkio.chatcore.model.reply.ReplyValue
import com.benkio.chatcore.model.reply.Text
import com.benkio.chatcore.model.CommandSyntax
import com.benkio.chatcore.model.MarkdownSupport
import com.benkio.chatcore.model.MediaKind
import com.benkio.chatcore.model.Message
import com.benkio.chatcore.model.MessageId
import com.benkio.chatcore.model.Platform
import com.benkio.chatcore.model.PlatformCapabilities
import com.benkio.chatcore.model.SubscriptionDraftStep
import com.benkio.chatcore.model.SubscriptionFlowAction
import com.benkio.chatcore.model.SubscriptionFlowKey
import com.benkio.chatcore.model.SubscriptionFlowPrompt
import com.benkio.chatcore.model.SubscriptionSchedulePreset
import com.benkio.chatcore.model.SubscriptionType
import com.benkio.chatcore.patterns.CommandPatternsGroup
import com.benkio.chatcore.BackgroundJobManager
import com.benkio.chatcore.BotEngine
import com.benkio.chatcore.ComputeReply
import com.benkio.chatcore.PlatformAdapter
import com.benkio.chatcore.TestIds
import com.benkio.chattelegramadapter.mocks.ApiMock.given
import com.benkio.chattelegramadapter.SBot
import com.benkio.chattelegramadapter.TelegramBackgroundJobManager
import com.benkio.integration.DBFixture
import com.benkio.integration.Logger.given
import com.benkio.RichardPHJBensonBot.RichardPHJBensonBot
import munit.CatsEffectSuite

import java.util.UUID

import _root_.log.effect.LogWriter

class ITTelegramSubscriptionFlowSpec extends CatsEffectSuite with DBFixture {

  private val sBotConfig = SBot.buildSBotConfig(RichardPHJBensonBot.sBotInfo)
  private val sBotInfo   = sBotConfig.sBotInfo
  private val chatId     = TestIds.chatId(1L)

  private val telegramCapabilities: PlatformCapabilities = PlatformCapabilities(
    platform = Platform.Telegram,
    supportedMedia = Set(MediaKind.Photo, MediaKind.Video, MediaKind.Audio, MediaKind.Gif, MediaKind.Document),
    maxTextLength = 4096,
    markdown = MarkdownSupport.Native,
    supportsDelete = true,
    supportsEdit = true,
    supportsButtons = true,
    acceptsFileIds = true,
    commandSyntax = CommandSyntax.Slash,
    suppliesSenderId = true
  )

  private final case class Sent(replyValue: ReplyValue, message: Message, replyToMessage: Boolean)

  private final class RecordingTelegramAdapter(
      val sent: Ref[IO, List[Sent]],
      val touched: Ref[IO, List[SubscriptionFlowKey]],
      backgroundJobManager: BackgroundJobManager[IO],
      dbLayer: com.benkio.chatcore.repository.db.DBLayer[IO],
      ttl: Option[scala.concurrent.duration.FiniteDuration]
  )(using cats.effect.Async[IO], LogWriter[IO])
      extends PlatformAdapter[IO] {
    override val capabilities: PlatformCapabilities                                         = telegramCapabilities
    override def run: IO[Unit]                                                              = IO.unit
    override def touchFlowPrompt(key: SubscriptionFlowKey)(using Applicative[IO]): IO[Unit] =
      touched.update(_ :+ key)
    override def send(replyValue: ReplyValue, message: Message, replyToMessage: Boolean): IO[Unit] =
      sent.update(_ :+ Sent(replyValue, message, replyToMessage))
    override def runEffectful(
        reply: EffectfulReply,
        message: Message,
        overridePage: Option[Int] = None
    ): IO[ReplyValue] =
      ComputeReply.runEffectfulReply[IO](
        reply = reply,
        msg = message,
        backgroundJobManager = backgroundJobManager,
        dbLayer = dbLayer,
        effectfulCallbacks = Map.empty,
        ttl = ttl,
        commandSyntax = capabilities.commandSyntax,
        overridePage = overridePage
      )
  }

  private def recordingAdapter(
      backgroundJobManager: BackgroundJobManager[IO],
      dbLayer: com.benkio.chatcore.repository.db.DBLayer[IO],
      ttl: Option[scala.concurrent.duration.FiniteDuration]
  ): IO[RecordingTelegramAdapter] =
    for {
      sent    <- Ref.of[IO, List[Sent]](List.empty)
      touched <- Ref.of[IO, List[SubscriptionFlowKey]](List.empty)
    } yield new RecordingTelegramAdapter(sent, touched, backgroundJobManager, dbLayer, ttl)

  private def message(text: String, id: String, authorId: String = "alice"): Message =
    Message(
      messageId = Some(MessageId(id)),
      date = 0L,
      chatId = chatId,
      chatType = "private",
      text = Some(text),
      authorId = Some(authorId)
    )

  private def promptStep(sent: List[Sent]): Option[SubscriptionDraftStep] =
    sent.collect { case Sent(p: SubscriptionFlowPrompt, _, _) => p.step }.lastOption

  private def lastReplyText(sent: List[Sent]): Option[String] =
    sent.collect {
      case Sent(Text(value, _), _, _)     => value
      case Sent(Markdown(value, _), _, _) => value
    }.lastOption

  private def subscriptionIdFromText(text: String): IO[UUID] = {
    val pattern = """`([0-9a-fA-F-]{36})`""".r
    IO.fromOption(pattern.findFirstMatchIn(text).map(m => UUID.fromString(m.group(1))))(
      new AssertionError(s"subscription id not found in: $text")
    )
  }

  databaseFixture.test(
    "Telegram guided /subscribe can be rerun after /unsubscribe and /subscriptions reports it deleted"
  ) { fixture =>
    val result = for {
      dbLayerBase <- fixture.resourceDBLayer
      dbLayer = dbLayerBase.copy(
        dbCommandReplies = com.benkio.chatcore.repository.db.DBCommandReplies(
          CommandPatternsGroup.ShowGroup.group(sBotInfo)
        )
      )
      repository           <- fixture.repositoryResource
      backgroundJobManager <- Resource.eval(
        TelegramBackgroundJobManager(
          repository = repository,
          dbLayer = dbLayer,
          sBotInfo = sBotInfo,
          ttl = sBotConfig.messageTimeToLive
        )
      )
      adapter <- Resource.eval(
        recordingAdapter(
          backgroundJobManager = backgroundJobManager,
          dbLayer = dbLayer,
          ttl = sBotConfig.messageTimeToLive
        )
      )
      engine = new BotEngine[IO](
        sBotConfig = sBotConfig,
        dbLayer = dbLayer,
        filteringMatchesMessages = (_, _) => IO.pure(true),
        postComputation = _ => IO.unit,
        notifier = summon[com.benkio.chatcore.Notifier[IO]],
        backgroundJobManager = backgroundJobManager
      )
      given PlatformAdapter[IO] = adapter

      subscribeMsg = message(text = "/subscribe", id = "1")
      _ <- Resource.eval(engine.onMessage(subscribeMsg))
      _ <- Resource.eval(
        engine.onSubscriptionFlowAction(subscribeMsg, SubscriptionFlowAction.chooseType(SubscriptionType.Show), "alice")
      )
      _ <- Resource.eval(
        engine.onSubscriptionFlowAction(
          subscribeMsg,
          SubscriptionFlowAction.choosePreset(SubscriptionSchedulePreset.Hourly),
          "alice"
        )
      )
      _ <- Resource.eval(
        engine.onSubscriptionFlowAction(subscribeMsg, SubscriptionFlowAction.enterTarget(""), "alice")
      )
      _ <- Resource.eval(
        engine.onSubscriptionFlowAction(subscribeMsg, SubscriptionFlowAction.confirm, "alice")
      )
      sentAfterSubscribe <- Resource.eval(adapter.sent.get)
      subscriptionId     <- Resource.eval(
        IO.fromOption(lastReplyText(sentAfterSubscribe))(
          new AssertionError(s"missing subscribe confirmation in $sentAfterSubscribe")
        ).flatMap(subscriptionIdFromText)
      )

      _ <- Resource.eval(engine.onMessage(message(text = s"/unsubscribe ${subscriptionId.toString}", id = "2")))
      sentAfterUnsubscribe <- Resource.eval(adapter.sent.get)

      _                      <- Resource.eval(engine.onMessage(message(text = "/subscriptions", id = "3")))
      sentAfterSubscriptions <- Resource.eval(adapter.sent.get)

      sentBeforeRerun <- Resource.eval(adapter.sent.get)
      rerunSubscribeMsg = message(text = "/subscribe", id = "4")
      _              <- Resource.eval(engine.onMessage(rerunSubscribeMsg))
      sentAfterRerun <- Resource.eval(adapter.sent.get)
    } yield {
      assertEquals(promptStep(sentAfterSubscribe), Some(SubscriptionDraftStep.Confirm))
      assert(
        lastReplyText(sentAfterSubscribe).exists(_.contains("Subscription successfully scheduled")),
        sentAfterSubscribe
      )

      assert(
        lastReplyText(sentAfterUnsubscribe).exists(_.contains("Subscription successfully cancelled")),
        sentAfterUnsubscribe
      )

      val subscriptionsReply = lastReplyText(sentAfterSubscriptions).getOrElse("")
      assert(subscriptionsReply.contains("No subscriptions for this chat."), subscriptionsReply)

      val rerunReplies = sentAfterRerun.drop(sentBeforeRerun.length)
      assertEquals(promptStep(rerunReplies), Some(SubscriptionDraftStep.Type))
    }

    result.use_
  }
}
