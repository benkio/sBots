package com.benkio.chatcore

import cats.effect.syntax.all.*
import cats.effect.Clock
import cats.effect.Sync
import cats.effect.Temporal
import cats.implicits.*
import com.benkio.chatcore.config.StartupCheckConfig
import com.benkio.chatcore.repository.db.DBLayer
import org.http4s.client.Client
import org.http4s.Method
import org.http4s.Request
import org.http4s.Uri

import java.nio.file.Files
import java.nio.file.Path
import java.util.concurrent.TimeoutException
import scala.concurrent.duration.*

/** Something to check when a bot starts, e.g. that the DB or an external host answers.
  *
  * @param timeout
  *   a timeout of its own, instead of the one of the configuration
  */
final case class Probe[F[_]](name: String, run: F[Unit], timeout: Option[FiniteDuration] = None)

enum ProbeOutcome {
  case Ok(took: FiniteDuration)
  case Slow(took: FiniteDuration)
  case Failed(took: FiniteDuration, error: String)
  case TimedOut(after: FiniteDuration)
}

final case class ProbeResult(name: String, outcome: ProbeOutcome) {
  def isOk: Boolean = outcome match {
    case ProbeOutcome.Ok(_) => true
    case _                  => false
  }
}

/** Checks, at the start of a bot, that what it needs works and how fast: the first requests also warm up the JVM, the
  * TLS handshakes and the connection pool, so the first real reply of a user does not pay for them. A probe never fails
  * the start: its problem is a result.
  */
object StartupCheck {

  def runProbe[F[_]: Temporal](probe: Probe[F], config: StartupCheckConfig): F[ProbeResult] = {
    val timeout = probe.timeout.getOrElse(config.probeTimeout)
    for {
      start  <- Clock[F].monotonic
      result <- probe.run.timeout(timeout).attempt
      end    <- Clock[F].monotonic
      took    = end - start
      outcome = result match {
        case Right(_) if took >= config.slowThreshold => ProbeOutcome.Slow(took)
        case Right(_)                                 => ProbeOutcome.Ok(took)
        case Left(_: TimeoutException)                => ProbeOutcome.TimedOut(timeout)
        case Left(error)                              => ProbeOutcome.Failed(took, describe(error))
      }
    } yield ProbeResult(probe.name, outcome)
  }

  /** One after the other: the first ones warm up what the next ones use. */
  def run[F[_]: Temporal](probes: List[Probe[F]], config: StartupCheckConfig): F[List[ProbeResult]] =
    probes.traverse(runProbe(_, config))

  private def describe(error: Throwable): String =
    Option(error.getMessage).filter(_.nonEmpty).getOrElse(error.getClass.getSimpleName)

  def formatDuration(duration: FiniteDuration): String =
    if duration < 1.second then s"${duration.toMillis} ms" else f"${duration.toMillis / 1000.0}%.1f s"

  def formatResult(result: ProbeResult): String = result.outcome match {
    case ProbeOutcome.Ok(took)          => s"✅ ${result.name}: ${formatDuration(took)}"
    case ProbeOutcome.Slow(took)        => s"🐢 ${result.name}: ${formatDuration(took)} (slow)"
    case ProbeOutcome.Failed(took, err) => s"❌ ${result.name}: failed after ${formatDuration(took)}: $err"
    case ProbeOutcome.TimedOut(after)   => s"⏱️ ${result.name}: no answer after ${formatDuration(after)}"
  }

  /** The text for the ops chat: a line for each probe and what it says about the whole. */
  def report(results: List[ProbeResult]): String = {
    val attention = results.count(!_.isOk)
    val summary   =
      if results.isEmpty then "No startup check ran"
      else if attention == 0 then "Startup checks OK"
      else s"⚠️ Startup checks: $attention of ${results.size} need attention"
    (summary +: results.map(formatResult)).mkString("\n")
  }

  // Probes shared by the platforms /////////////////////////////////////////////////////////////////////////////////

  /** The DB answers a read. */
  def dbRead[F[_]: Sync](dbLayer: DBLayer[F]): Probe[F] =
    Probe("DB read", dbLayer.dbLog.getLastLog().void)

  /** The host answers over HTTPS: any HTTP status counts, what is checked is the DNS, the TLS and the connection. */
  def hostReachable[F[_]: Sync](name: String, client: Client[F], uri: Uri): Probe[F] =
    Probe(name, client.status(Request[F](Method.HEAD, uri)).void)
}

/** The deploy leaves a marker file to ask for the post deploy check: the first start that finds it removes it, so a bot
  * that restarts in a loop does not send the test file again and again.
  */
object PostDeployMarker {

  /** Whether the marker was there: it is removed in the same step, so only one caller gets `true`. */
  def consume[F[_]: Sync](markerFile: Path): F[Boolean] =
    Sync[F].blocking(Files.deleteIfExists(markerFile))
}
