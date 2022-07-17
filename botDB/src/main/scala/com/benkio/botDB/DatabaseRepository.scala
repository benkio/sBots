package com.benkio.botDB

import java.io.File

trait DatabaseRepository[F[_]] {
  def insertMedia(file: File): F[Unit]
}
