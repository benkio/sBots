package com.benkio.chatcore

import cats.effect.IO
import cats.effect.Ref
import cats.syntax.all.*
import com.benkio.chatcore.config.SBotConfig
import com.benkio.chatcore.mocks.BackgroundJobManagerMock
import com.benkio.chatcore.mocks.DBLayerMock
import com.benkio.chatcore.mocks.FakePlatformAdapter
import com.benkio.chatcore.model.reply.ReplyBundleCommand
import com.benkio.chatcore.model.reply.ReplyValue
import com.benkio.chatcore.model.reply.Text
import com.benkio.chatcore.model.reply.TextReply
import com.benkio.chatcore.model.CommandInstructionData
import com.benkio.chatcore.model.CommandSyntax
import com.benkio.chatcore.model.CommandTrigger
import com.benkio.chatcore.model.Message
import com.benkio.chatcore.model.MessageId
import com.benkio.chatcore.model.SBotInfo
import com.benkio.chatcore.model.SubscribeArguments
import com.benkio.chatcore.model.SubscriptionDraftStep
import com.benkio.chatcore.model.SubscriptionFlowAction
import com.benkio.chatcore.model.SubscriptionFlowKey
import com.benkio.chatcore.model.SubscriptionFlowPrompt
import com.benkio.chatcore.model.SubscriptionSchedulePreset
import com.benkio.chatcore.model.SubscriptionType
import com.benkio.chatcore.Logger.given
import com.benkio.chatcore.Notifier
import com.benkio.chatcore.TestIds
import munit.CatsEffectSuite
import org.http4s.syntax.literals.*
import org.http4s.Uri

import scala.concurrent.duration.*

/** The platform-neutral guided subscription flow: the engine owns the drafts, the adapters only render its prompts. */
class SubscriptionFlowSpec extends CatsEffectSuite {

  private val sBotInfo: SBotInfo = SBotInfo(SBotInfo.SBotId("eng"), SBotInfo.SBotName("EngineBot"))

  private val sBotConfig: SBotConfig = SBotConfig(
    sBotInfo = sBotInfo,
    triggerFilename = "eng_triggers.md",
    triggerListUri = uri"https://example.com/eng_triggers.md",
    instructionsFilename = "eng_instructions.md",
    instructionsUri = uri"https://example.com/eng_instructions.md",
    token = "eng.token",
    listJsonFilename = "eng_list.json",
    repliesJsonFilename = "eng_replies.json",
    commandsJsonFilename = "eng_commands.json",
    showFilename = "eng_shows.json"
  )

  private def message(text: String, authorId: String = "alice"): Message =
    Message(
      messageId = Some(MessageId("1")),
      date = 0L,
      chatId = TestIds.chatId(1L),
      chatType = "group",
      text = Some(text),
      authorId = Some(authorId)
    )

  private def prompt(outcome: SubscriptionFlowOutcome): SubscriptionFlowPrompt =
    outcome match {
      case SubscriptionFlowOutcome.Prompt(value) => value
      case other                                 => fail(s"expected a prompt, got $other")
    }

  private def text(outcome: SubscriptionFlowOutcome): String =
    outcome match {
      case SubscriptionFlowOutcome.Message(Text(value, _), _) => value
      case other                                              => fail(s"expected a text, got $other")
    }

  private def emptyCreated: Ref[IO, List[SubscribeArguments]] =
    Ref.unsafe[IO, List[SubscribeArguments]](List.empty)

  private def service(
      store: SubscriptionDraftStore[IO] = SubscriptionDraftStore.create[IO](10.minutes, 10),
      created: Ref[IO, List[SubscribeArguments]] = emptyCreated,
      failCreation: Boolean = false
  ): SubscriptionFlowService[IO] =
    new SubscriptionFlowService[IO](
      store = store,
      sBotInfo = sBotInfo,
      expiry = 10.minutes,
      ttl = None,
      createSubscription = { (_, _, arguments) =>
        for {
          _      <- created.update(_ :+ arguments)
          result <-
            (if failCreation then IO.raiseError[ReplyValue](new RuntimeException("bot has no replies"))
             else IO.pure[ReplyValue](Text("Subscription successfully scheduled."))).attempt
        } yield result
      }
    )

  private def runFlow(
      responses: List[Either[String, SubscriptionFlowAction]],
      store: SubscriptionDraftStore[IO] = SubscriptionDraftStore.create[IO](10.minutes, 10),
      created: Ref[IO, List[SubscribeArguments]] = emptyCreated,
      failCreation: Boolean = false
  ): IO[(List[SubscriptionFlowOutcome], Ref[IO, List[SubscribeArguments]])] = {
    val flow = service(store = store, created = created, failCreation = failCreation)
    val msg  = message("/subscribe")
    flow.start(msg, "alice").flatMap { started =>
      responses
        .foldLeft(IO.pure(List(started))) { (previous, response) =>
          previous.flatMap { outcomes =>
            response match {
              case Left(_)       => flow.acceptPlainMessage(msg, "alice").map(_.get).map(outcomes :+ _)
              case Right(action) => flow.submit(msg, action, "alice").map(outcomes :+ _)
            }
          }
        }
        .map(_ -> created)
    }
  }

  private val oneShotSubscribeCommand = ReplyBundleCommand(
    trigger = CommandTrigger("subscribe"),
    reply = TextReply.fromList("Input Required: insert a valid 〈cron time〉. Check the instructions")(true),
    instruction = CommandInstructionData.NoInstructions
  )

  private def newEngine: BotEngine[IO] = new BotEngine[IO](
    sBotConfig = sBotConfig,
    dbLayer = DBLayerMock.mock(sBotInfo.botId, commandReplies = List(oneShotSubscribeCommand)),
    filteringMatchesMessages = (_, _) => IO.pure(true),
    postComputation = _ => IO.unit,
    notifier = Notifier.noop[IO],
    backgroundJobManager = BackgroundJobManagerMock.mock()
  )

  private val flowCapabilities = FakePlatformAdapter.textOnlyCapabilities.copy(
    platform = com.benkio.chatcore.model.Platform.Telegram,
    commandSyntax = CommandSyntax.Slash,
    supportsButtons = true,
    supportsEdit = true,
    suppliesSenderId = true
  )

  private val chooseShow: Right[String, SubscriptionFlowAction] =
    Right(SubscriptionFlowAction.chooseType(SubscriptionType.Show))
  private val firstPreset: Right[String, SubscriptionFlowAction] =
    Right(SubscriptionFlowAction.choosePreset(com.benkio.chatcore.model.SubscriptionSchedulePreset.EveryMinute))
  private val withTarget: Right[String, SubscriptionFlowAction] =
    Right(SubscriptionFlowAction.enterTarget("pino scotto"))
  private val confirm: Right[String, SubscriptionFlowAction] = Right(SubscriptionFlowAction.confirm)

  test("starting offers type and cancel, but no back") {
    service().start(message("/subscribe"), "alice").map { outcome =>
      val shown = prompt(outcome)
      assertEquals(shown.step, SubscriptionDraftStep.Type)
      assertEquals(shown.buttons.map(_.map(_.label)), List(List("show", "trigger"), List("cancel")))
      assert(shown.text.contains("10 minutes"))
    }
  }

  test("the flow walks type, schedule, target and confirm, then creates the subscription") {
    runFlow(responses = List(chooseShow, firstPreset, withTarget, confirm)).flatMap { (outcomes, created) =>
      created.get.map { args =>
        assertEquals(
          outcomes.map {
            case SubscriptionFlowOutcome.Prompt(value) => value.step
            case _                                     => "message"
          },
          List(
            SubscriptionDraftStep.Type,
            SubscriptionDraftStep.Schedule,
            SubscriptionDraftStep.Target,
            SubscriptionDraftStep.Confirm,
            "message"
          )
        )
        assertEquals(
          args,
          List(SubscribeArguments("0 * * ? * *", SubscriptionType.Show, Some("pino scotto")))
        )
        assert(text(outcomes.last).contains("Subscription successfully scheduled."))
      }
    }
  }

  test("a typed cron is a plain message at the schedule step, an invalid one drops the draft") {
    val flow = service()
    val msg  = message("/subscribe")
    for {
      _    <- flow.start(msg, "alice")
      _    <- flow.submit(msg, SubscriptionFlowAction.chooseType(SubscriptionType.Show), "alice")
      good <- flow.acceptPlainMessage(msg.copy(text = Some("0 */15 * ? * *")), "alice")
      bad  <- flow
        .start(msg, "alice") >> flow.submit(msg, SubscriptionFlowAction.chooseType(SubscriptionType.Show), "alice") >>
        flow.acceptPlainMessage(msg.copy(text = Some("not a cron")), "alice")
      after <- flow.submit(msg, SubscriptionFlowAction.cancel, "alice")
    } yield {
      assertEquals(prompt(good.get).step, SubscriptionDraftStep.Target)
      assert(text(bad.get).contains("invalid_cron"))
      assert(text(after).contains("no_active_flow"))
    }
  }

  test("a too long target keeps the draft and a valid target moves to confirm") {
    val flow = service()
    val msg  = message("/subscribe")
    for {
      _ <- flow.start(msg, "alice")
      _ <- flow.submit(msg, SubscriptionFlowAction.chooseType(SubscriptionType.Show), "alice")
      _ <- flow.submit(
        msg,
        SubscriptionFlowAction.choosePreset(com.benkio.chatcore.model.SubscriptionSchedulePreset.Hourly),
        "alice"
      )
      long     <- flow.submit(msg, SubscriptionFlowAction.enterTarget("x" * 201), "alice")
      accepted <- flow.submit(msg, SubscriptionFlowAction.enterTarget(""), "alice")
    } yield {
      assert(text(long).contains("target_too_long"))
      assertEquals(prompt(accepted).step, SubscriptionDraftStep.Confirm)
    }
  }

  test("no text at the target step is not a flow answer: media and empty text neither pick random nor advance") {
    val flow       = service()
    val randomFlow = service()
    val msg        = message("/subscribe")
    for {
      _     <- flow.start(msg, "alice")
      _     <- flow.submit(msg, SubscriptionFlowAction.chooseType(SubscriptionType.Show), "alice")
      _     <- flow.submit(msg, SubscriptionFlowAction.choosePreset(SubscriptionSchedulePreset.Hourly), "alice")
      media <- flow.acceptPlainMessage(msg.copy(text = None, caption = Some("a photo")), "alice")
      empty <- flow.acceptPlainMessage(msg.copy(text = Some("")), "alice")
      typed <- flow.acceptPlainMessage(msg.copy(text = Some("pino scotto")), "alice")

      _      <- randomFlow.start(msg, "alice")
      _      <- randomFlow.submit(msg, SubscriptionFlowAction.chooseType(SubscriptionType.Show), "alice")
      _      <- randomFlow.submit(msg, SubscriptionFlowAction.choosePreset(SubscriptionSchedulePreset.Hourly), "alice")
      random <- randomFlow.submit(msg, SubscriptionFlowAction.enterTarget(""), "alice")
    } yield {
      assertEquals(media, None)
      assertEquals(empty, None)
      // both drafts were still on target: real text and the random button advance as before
      assertEquals(prompt(typed.get).step, SubscriptionDraftStep.Confirm)
      assert(prompt(typed.get).text.contains("target pino scotto"))
      assertEquals(prompt(random).step, SubscriptionDraftStep.Confirm)
      assert(prompt(random).text.contains("target random"))
    }
  }

  test("back clears the value of the step being left, cancel and errors drop the draft") {
    val flow = service()
    val msg  = message("/subscribe")
    for {
      _ <- flow.start(msg, "alice")
      _ <- flow.submit(msg, SubscriptionFlowAction.chooseType(SubscriptionType.Show), "alice")
      _ <- flow.submit(
        msg,
        SubscriptionFlowAction.choosePreset(com.benkio.chatcore.model.SubscriptionSchedulePreset.Hourly),
        "alice"
      )
      back <- flow.submit(msg, SubscriptionFlowAction.back, "alice")
      _    <- flow.submit(
        msg,
        SubscriptionFlowAction.choosePreset(com.benkio.chatcore.model.SubscriptionSchedulePreset.Hourly),
        "alice"
      )
      cancelled <- flow.submit(msg, SubscriptionFlowAction.cancel, "alice")
      noFlow    <- flow.submit(msg, SubscriptionFlowAction.confirm, "alice")
    } yield {
      assertEquals(prompt(back).step, SubscriptionDraftStep.Schedule)
      assert(text(cancelled).contains("cancelled"))
      assert(text(noFlow).contains("no_active_flow"))
    }
  }

  test("a wrong action is rejected without moving the step") {
    val flow = service()
    val msg  = message("/subscribe")
    for {
      _     <- flow.start(msg, "alice")
      wrong <- flow.submit(msg, SubscriptionFlowAction.confirm, "alice")
      after <- flow.submit(msg, SubscriptionFlowAction.chooseType(SubscriptionType.Show), "alice")
    } yield {
      assert(text(wrong).contains("invalid_action"))
      assertEquals(prompt(after).step, SubscriptionDraftStep.Schedule)
    }
  }

  test("a failed confirmation drops the draft and shows the ready-to-run one-line command") {
    val flow = service(failCreation = true)
    val msg  = message("/subscribe")
    for {
      _ <- flow.start(msg, "alice")
      _ <- flow.submit(msg, SubscriptionFlowAction.chooseType(SubscriptionType.Trigger), "alice")
      _ <- flow.submit(
        msg,
        SubscriptionFlowAction.choosePreset(com.benkio.chatcore.model.SubscriptionSchedulePreset.Hourly),
        "alice"
      )
      _      <- flow.submit(msg, SubscriptionFlowAction.enterTarget("buongiorno"), "alice")
      failed <- flow.submit(msg, SubscriptionFlowAction.confirm, "alice")
      after  <- flow.submit(msg, SubscriptionFlowAction.confirm, "alice")
    } yield {
      assert(text(failed).contains("creation_rejected"))
      assert(text(failed).contains("/subscribe 0 0 * ? * * trigger buongiorno"))
      assert(text(after).contains("no_active_flow"))
    }
  }

  test("two concurrent confirms create exactly one subscription") {
    val created = emptyCreated
    val flow    = service(created = created)
    val msg     = message("/subscribe")
    for {
      _       <- flow.start(msg, "alice")
      _       <- flow.submit(msg, SubscriptionFlowAction.chooseType(SubscriptionType.Show), "alice")
      _       <- flow.submit(msg, SubscriptionFlowAction.choosePreset(SubscriptionSchedulePreset.EveryMinute), "alice")
      _       <- flow.submit(msg, SubscriptionFlowAction.enterTarget("pino scotto"), "alice")
      results <- List(
        flow.submit(msg, SubscriptionFlowAction.confirm, "alice"),
        flow.submit(msg, SubscriptionFlowAction.confirm, "alice")
      ).parSequence
      args <- created.get
    } yield {
      assertEquals(args.size, 1)
      assertEquals(
        results.count {
          case SubscriptionFlowOutcome.Message(Text(value, _), _) =>
            value.contains("Subscription successfully scheduled.")
          case _ => false
        },
        1
      )
      assertEquals(
        results.count {
          case SubscriptionFlowOutcome.Message(Text(value, _), _) => value.contains("no_active_flow")
          case _                                                  => false
        },
        1
      )
    }
  }

  test("two users in the same chat have separate drafts") {
    val flow  = service()
    val alice = message("/subscribe", "alice")
    val bob   = message("/subscribe", "bob")
    for {
      _      <- flow.start(alice, "alice")
      _      <- flow.submit(alice, SubscriptionFlowAction.chooseType(SubscriptionType.Show), "alice")
      bobs   <- flow.start(bob, "bob")
      alices <- flow.submit(
        alice,
        SubscriptionFlowAction.choosePreset(com.benkio.chatcore.model.SubscriptionSchedulePreset.Hourly),
        "alice"
      )
    } yield {
      assertEquals(prompt(bobs).step, SubscriptionDraftStep.Type)
      assertEquals(prompt(alices).step, SubscriptionDraftStep.Target)
    }
  }

  test("a bare /subscribe restarts a draft, a full store declines without evicting") {
    val full = SubscriptionDraftStore.create[IO](10.minutes, 1)
    val flow = service(store = full)
    val msg  = message("/subscribe")
    for {
      first   <- flow.start(msg, "alice")
      _       <- flow.submit(msg, SubscriptionFlowAction.chooseType(SubscriptionType.Show), "alice")
      restart <- flow.start(msg, "alice")
      bob     <- flow.start(message("/subscribe", "bob"), "bob")
    } yield {
      assertEquals(prompt(first).step, SubscriptionDraftStep.Type)
      assertEquals(prompt(restart).step, SubscriptionDraftStep.Type)
      assert(text(bob).contains("store_full"))
    }
  }

  test("outcomes are delivered under the draft lock, so two presses cannot deliver out of order") {
    val flow                                             = service()
    val msg                                              = message("/subscribe")
    val emitted                                          = Ref.unsafe[IO, List[String]](List.empty)
    val active                                           = Ref.unsafe[IO, Int](0)
    val peak                                             = Ref.unsafe[IO, Int](0)
    def emit(outcome: SubscriptionFlowOutcome): IO[Unit] =
      for {
        n <- active.updateAndGet(_ + 1)
        _ <- peak.updateAndGet(current => math.max(current, n))
        _ <- emitted.update(_ :+ text(outcome))
        _ <- IO.sleep(5.millis)
        _ <- active.update(_ - 1)
      } yield ()
    for {
      _ <- flow.start(msg, "alice")
      _ <- List(
        flow.submit(msg, SubscriptionFlowAction.cancel, "alice", emit),
        flow.submit(msg, SubscriptionFlowAction.cancel, "alice", emit)
      ).parSequence
      peakSeen <- peak.get
      order    <- emitted.get
    } yield {
      assertEquals(peakSeen, 1)
      assertEquals(
        order,
        List(
          "Subscription setup failed (cancelled): Subscription setup cancelled.",
          "Subscription setup failed (no_active_flow): No active subscription setup. Start again with /subscribe."
        )
      )
    }
  }

  test("every flow answer, including a rejected plain one, refreshes the adapter's prompt") {
    val engine  = newEngine
    val msg     = message("/subscribe")
    val key     = SubscriptionFlowKey(msg.chatId, sBotInfo.botId, "alice")
    val tooLong = msg.copy(messageId = Some(MessageId("2")), text = Some("x" * 201))
    for {
      telegram <- FakePlatformAdapter(capabilities = flowCapabilities)
      _        <- engine.onMessage(msg)(using telegram)
      _ <- engine.onSubscriptionFlowAction(msg, SubscriptionFlowAction.chooseType(SubscriptionType.Show), "alice")(using
        telegram
      )
      _ <- engine.onSubscriptionFlowAction(
        msg,
        SubscriptionFlowAction.choosePreset(SubscriptionSchedulePreset.EveryMinute),
        "alice"
      )(using telegram)
      _       <- engine.onMessage(tooLong)(using telegram)
      touches <- telegram.touchedPrompts.get
      sent    <- telegram.sent.get
    } yield {
      assertEquals(touches, List.fill(4)(key))
      assert(sent.last.replyValue match {
        case Text(value, _) => value.contains("target_too_long")
        case _              => false
      })
    }
  }

  test("terminal flow outcomes clear the draft from the in-memory store") {
    val store   = SubscriptionDraftStore.create[IO](10.minutes, 10)
    val created = emptyCreated
    val flow    = service(store = store, created = created)
    val msg     = message("/subscribe")
    val key     = SubscriptionFlowKey(msg.chatId, sBotInfo.botId, "alice")

    for {
      _                <- flow.start(msg, "alice")
      draftAfterStart  <- store.get(key)
      _                <- flow.submit(msg, SubscriptionFlowAction.cancel, "alice")
      draftAfterCancel <- store.get(key)
      _                <- flow.start(msg.copy(messageId = Some(MessageId("2"))), "alice")
      _                <- flow.submit(msg, SubscriptionFlowAction.chooseType(SubscriptionType.Show), "alice")
      _                <- flow.submit(
        msg,
        SubscriptionFlowAction.choosePreset(SubscriptionSchedulePreset.EveryMinute),
        "alice"
      )
      _                 <- flow.submit(msg, SubscriptionFlowAction.enterTarget("pino scotto"), "alice")
      _                 <- flow.submit(msg, SubscriptionFlowAction.confirm, "alice")
      draftAfterSuccess <- store.get(key)
    } yield {
      assert(draftAfterStart.isDefined, "draft should exist after start")
      assertEquals(draftAfterCancel, None)
      assertEquals(draftAfterSuccess, None)
    }
  }

  test("the engine starts the flow only where the platform supplies a requester and edits") {
    val engine = newEngine
    for {
      telegram <- FakePlatformAdapter(capabilities = flowCapabilities)
      irc      <- FakePlatformAdapter()
      _        <- engine.onMessage(message("/subscribe"))(using telegram)
      _        <- engine.onMessage(message("!subscribe").copy(messageId = Some(MessageId("3"))))(using irc)
      sentT    <- telegram.sent.get
      sentI    <- irc.sent.get
    } yield {
      assertEquals(
        sentT.map(_.replyValue).collect { case p: SubscriptionFlowPrompt => p.step },
        List(SubscriptionDraftStep.Type)
      )
      assertEquals(
        sentI.map(_.replyValue).collect { case Text(value, _) => value },
        List("Input Required: insert a valid 〈cron time〉. Check the instructions")
      )
    }
  }
}
