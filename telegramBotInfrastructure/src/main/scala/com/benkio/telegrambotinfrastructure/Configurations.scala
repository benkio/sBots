package com.benkio.telegrambotinfrastructure

import cats.effect.Effect
import cats.effect.Resource

trait Configurations {
  // Configuration Stuff //////////////////////////////////////////////////////
  def token[F[_]](implicit effectF: Effect[F]): Resource[F, String]
}
