package com.benkio.chatcore.config

import pureconfig.*

import scala.concurrent.duration.*

/** The checks a bot runs when it starts, to know early if something is wrong (see
  * [[com.benkio.chatcore.StartupCheck]]).
  *
  * @param enabled
  *   whether the checks run at all
  * @param probeTimeout
  *   the time a probe can take before it counts as failed; the test file gets more time, it is a whole reply
  * @param slowThreshold
  *   a probe that takes more is reported as slow
  * @param testMedia
  *   the file sent to the ops chat once after a deploy, through the same path of a real reply: fetched by the
  *   repository and sent by the platform
  * @param markerFile
  *   the base name of the marker the deploy leaves for each bot (`<botId>_<markerFile>`): the check removes its own, so
  *   every bot sends its test file once and a restart does not send it again
  */
final case class StartupCheckConfig(
    enabled: Boolean = true,
    probeTimeout: FiniteDuration = 15.seconds,
    slowThreshold: FiniteDuration = 3.seconds,
    testMedia: Option[String] = None,
    markerFile: String = "post-deploy-test.marker"
) derives ConfigReader
