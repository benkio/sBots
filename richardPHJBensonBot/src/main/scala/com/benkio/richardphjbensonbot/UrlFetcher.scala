package com.benkio.richardphjbensonbot

import java.io.File
trait UrlFetcher[F[_]] {

  def fetch(url: String, filename: String): F[File]

}

object UrlFetcher {
  def apply[F[_]](): UrlFetcher[F] = new DropboxFetcherImpl[F]()

  private class DropboxFetcherImpl[F[_]]() extends UrlFetcher[F] {
    def fetch(url: String, filename: String): F[File] = ???
  }
}
