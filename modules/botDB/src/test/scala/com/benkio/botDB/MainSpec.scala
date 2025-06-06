package com.benkio.botDB

import cats.effect.IO










import munit.*





class MainSpec extends CatsEffectSuite {

  test("Main.Initialization should not raise exception when inizialize the module") {
    assertIO_(Main.initialization(List.empty).use_)
  }
}
