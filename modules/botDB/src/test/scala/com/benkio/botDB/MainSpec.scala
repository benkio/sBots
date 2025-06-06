package com.benkio.botDB

import cats.effect.IO
import munit.*

class MainSpec extends CatsEffectSuite {

  val ciEnvVar = sys.env.get("CI")

  test("Main.Initialization should not raise exception when inizialize the module") {
    // Disable for CI as there's no YoutubeApiKey file
    assume(ciEnvVar.contains("false") || ciEnvVar.isEmpty)

    assertIO_(Main.initialization(List.empty).use_)
  }
}
