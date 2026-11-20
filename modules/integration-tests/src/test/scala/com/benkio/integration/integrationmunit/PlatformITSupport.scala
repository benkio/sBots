package com.benkio.integration.integrationmunit

import cats.effect.IO
import cats.effect.Resource
import com.benkio.chatcore.config.SBotConfig
import com.benkio.chatcore.http.HttpClients
import com.benkio.chatcore.initialization.BotCoreSetup
import com.benkio.chatcore.model.reply.MarkdownFile
import com.benkio.chatcore.model.reply.MediaReply
import com.benkio.chatcore.model.reply.ReplyBundleMessage
import com.benkio.chatcore.model.StringTextTriggerValue
import com.benkio.chatcore.model.TextTrigger
import com.benkio.chatcore.repository.db.DBLayer
import com.benkio.chatcore.repository.db.DBRepository
import com.benkio.chatcore.repository.JsonDataRepository
import com.benkio.integration.DBFixtureResources
import com.benkio.integration.Logger.given
import org.testcontainers.containers.GenericContainer
import org.testcontainers.containers.wait.strategy.WaitStrategy
import org.testcontainers.utility.DockerImageName

import scala.concurrent.duration.*

/** What the integration tests of a chat platform on a local server (a container started with testcontainers) share: the
  * container, the bot core on the real DB of the integration tests, and the texts the tests send.
  */
object PlatformITSupport {

  val answerTimeout: FiniteDuration = 30.seconds
  val quietPeriod: FiniteDuration   = 2.seconds

  /** What the answer is when the media is sent as its link. */
  val bareLink = "https?://\\S+".r

  /** Starts the container of the image exposing the ports, stops it on release. */
  def container(
      image: String,
      ports: Int*
  )(
      waitStrategy: WaitStrategy,
      configure: GenericContainer[?] => Unit = _ => ()
  ): Resource[IO, GenericContainer[?]] =
    Resource
      .make(IO.blocking {
        val container = new GenericContainer(DockerImageName.parse(image))
        container.withExposedPorts(ports.map(Int.box)*)
        container.waitingFor(waitStrategy)
        configure(container)
        container.start()
        container: GenericContainer[?]
      })(container => IO.blocking(container.stop()))

  /** Retries the check until it is true, or fails after the timeout. */
  def until(what: String, timeout: FiniteDuration)(check: IO[Boolean]): IO[Unit] = {
    def go: IO[Unit] = check.flatMap(ok => if ok then IO.unit else IO.sleep(500.millis) >> go)
    go.timeoutTo(timeout, IO.raiseError(new AssertionError(s"Timeout after $timeout: $what")))
  }

  /** Waits until the number of the answers is stable for the quiet period. */
  def waitQuiet[A](answers: IO[List[A]], previousCount: Int = 0, stableFor: FiniteDuration = 0.seconds): IO[Unit] =
    IO.sleep(500.millis) >> answers.flatMap { current =>
      val count = current.length
      if count == previousCount then {
        val nextStable = stableFor + 500.millis
        if nextStable >= quietPeriod then IO.unit else waitQuiet(answers, previousCount, nextStable)
      } else waitQuiet(answers, count, 0.seconds)
    }

  /** Sends, waits that the bot answers and then goes quiet, gives what the bot said. */
  def answer[A](nick: String, answers: IO[List[A]], send: IO[Unit]): IO[List[A]] =
    for {
      _ <- send
      _ <- until(s"$nick answers", answerTimeout)(answers.map(_.nonEmpty))
      _ <- waitQuiet(answers)
        .timeoutTo(answerTimeout, IO.raiseError(new AssertionError(s"Timeout after $answerTimeout: $nick goes quiet")))
      result <- answers
    } yield result

  def core(fixture: DBFixtureResources, sBotConfig: SBotConfig): Resource[IO, BotCoreSetup[IO]] =
    for {
      base    <- fixture.resourceDBLayer
      dbLayer <- Resource.eval(DBLayer.loadReplies[IO](base, JsonDataRepository[IO](), sBotConfig))
      dropbox <- fixture.dropboxClientResource
      mega    <- fixture.megaClientResource
    } yield BotCoreSetup(
      jsonDataRepository = JsonDataRepository[IO](),
      dbLayer = dbLayer,
      repository =
        DBRepository.dbResources[IO](dbLayer.dbMedia, HttpClients(dropboxClient = dropbox, megaClient = mega)),
      sBotConfig = sBotConfig
    )

  /** The text that triggers a reply made only of markdown files (or of some, if no reply is made only of them), when
    * the bot has one that a text can trigger.
    */
  def markdownTrigger(replies: List[ReplyBundleMessage]): Option[String] = {
    val withMarkdown = for {
      bundle <- replies
      files  <- bundle.reply match {
        case media: MediaReply => Some(media.mediaFiles)
        case _                 => None
      }
      if files.exists(_.isInstanceOf[MarkdownFile])
      strings <- bundle.trigger match {
        case TextTrigger(values*) => Some(values.collect { case StringTextTriggerValue(s) => s }.toList)
        case _                    => None
      }
      if strings.nonEmpty
    } yield (files.forall(_.isInstanceOf[MarkdownFile]), strings.mkString(" "))
    withMarkdown.sortBy(!_._1).headOption.map(_._2)
  }
}
