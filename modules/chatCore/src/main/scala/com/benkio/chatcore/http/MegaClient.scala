package com.benkio.chatcore.http

import cats.effect.Async
import cats.effect.Resource
import cats.implicits.*
import com.benkio.chatcore.repository.Repository
import io.chrisdavenport.mules.*
import io.chrisdavenport.mules.http4s.*
import log.effect.LogWriter
import org.http4s.*
import org.http4s.client.middleware.FollowRedirect
import org.http4s.client.Client
import org.http4s.syntax.literals.*

import java.net.http.HttpRequest
import java.nio.file.Path
import scala.concurrent.duration.*

trait MegaClient[F[_]] {
  def fetchFile(filename: String, url: Uri): Resource[F, Path]
}

object MegaClient {
  def apply[F[_]: Async: LogWriter](
      httpClient: Client[F]
  ): F[MegaClient[F]] = for {
    httpCache <- MemoryCache.ofSingleImmutableMap[F, (Method, Uri), CacheItem](defaultExpiration =
      TimeSpec.fromDuration(6.hours)
    )
    cachedMiddleware = CacheMiddleware.client(httpCache, CacheType.Public)
  } yield new MegaClientImpl[F](
    httpClient = cachedMiddleware(FollowRedirect(3)(httpClient))
  )

  final case class MegaUriComponents(fileId: String, decriptKey: String)

  final case class ErrorExtractingMegaUriComponents(url: Uri)
      extends Throwable(s"[MegaClient] 🚫 Error in extracting the components from the Url: $url")

  private class MegaClientImpl[F[_]: Async: LogWriter](httpClient: Client[F]) extends MegaClient[F] {
    val megaApiUri: Uri = uri"https://g.api.mega.co.nz/cs"

    // https://gist.github.com/CypherpunkSamurai/7b476aef9d42fc29fe8a904a91039e85#file-basemax-megadownloader-md
    override def fetchFile(filename: String, url: Uri): Resource[F, Path] = {
      val fileContent: F[Array[Byte]] = for {
        mediaUriComponents       <- extractMegaUrlComponents(url)
        encryptedFileHttpRequest <- buildMegaEncryptedFileUrlRequest(mediaUriComponents)
        encryptedFileContent     <- getEncryptedFileContent(encryptedFileHttpRequest)
        decryptedFileContent     <- decryptFileContent(encryptedFileContent, mediaUriComponents)
      } yield decryptedFileContent

      Resource.eval(fileContent).flatMap(content => Repository.toTempFile(filename, content))
    }

    private def extractMegaUrlComponents(url: Uri): F[MegaUriComponents] = {
      ???
    }

    private def buildMegaEncryptedFileUrlRequest(megaUriComponents: MegaUriComponents): F[HttpRequest] = {
      ???
    }

    private def getMegaEncryptedFile(mediaUriComponents: MegaUriComponents): F[Uri] = {
      // buildMegaEncryptedFileUrlRequest(megaUriComponents)
      ???
    }

    private def getEncryptedFileContent(encriptedFileUri: HttpRequest): F[Array[Byte]] = { ??? }
    private def decryptFileContent(
        encryptedFileContent: Array[Byte],
        megaUriComponents: MegaUriComponents
    ): F[Array[Byte]] = { ??? }
  }
}
