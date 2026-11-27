package com.benkio.main

import cats.effect.IO
import cats.effect.Resource
import com.benkio.chatcore.llm.LlmDependencies
import com.benkio.chatcore.Logger.given
import munit.CatsEffectSuite

class MainSetupSpec extends CatsEffectSuite {

  test("MainSetup.apply should successfully be loaded") {

    MainSetup[IO](Some(Resource.pure(LlmDependencies.noop[IO]))).attempt.use(
      _.fold(
        x => IO(fail(s"Failed to load the MainSetup. Got Left($x)")): IO[Unit],
        _ => IO.unit
      )
    )
  }
}
