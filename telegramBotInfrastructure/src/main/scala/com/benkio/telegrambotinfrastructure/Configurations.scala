package com.benkio.telegrambotinfrastructure

import cats.effect.Async
import cats.effect.Resource

trait Configurations {
  // Configuration Stuff //////////////////////////////////////////////////////
  def token[F[_]: Async]: Resource[F, String]
}
