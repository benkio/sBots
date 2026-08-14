package com.benkio.chatcore.http

import cats.effect.Async
import cats.effect.Resource
import cats.implicits.*
import cats.Monad
import cats.MonadThrow
import com.benkio.chatcore.http.MegaEncryptedFileRequest.given
import com.benkio.chatcore.http.MegaEncryptedFileResponse.given
import com.benkio.chatcore.repository.Repository
import io.chrisdavenport.mules.*
import io.chrisdavenport.mules.http4s.*
import io.circe.syntax.*
import io.circe.Json
import log.effect.LogWriter
import org.http4s.*
import org.http4s.circe.*
import org.http4s.circe.CirceEntityDecoder.given
import org.http4s.client.middleware.FollowRedirect
import org.http4s.client.Client
import org.http4s.syntax.literals.*

import java.nio.file.Path
import scala.annotation.unused
import scala.concurrent.duration.*

trait MegaClient[F[_]] {
  def fetchFile(filename: String, url: Uri): Resource[F, Path]
}

object MegaClient {
  val megaApiUri: Uri = uri"https://g.api.mega.co.nz/cs"

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

  final case class MegaUriComponents(fileId: String, decryptKey: String)

  final case class ErrorMegaUriFileIdtNotFound(url: Uri)
      extends Throwable(s"[MegaClient] 🚫 Error in extracting the first segment from the Url: $url")
  final case class ErrorMegaUriDecryptionKeyNotFound(url: Uri)
      extends Throwable(s"[MegaClient] 🚫 Expected '#' in the first segment from the Url: $url")
  final case class MegaEncryptedFileErrorEmptyResponse(filename: String, encryptedFileUri: Uri)
      extends Throwable(
        s"[MegaClient - $filename] 🚫 Expected non empty array response encripted uri: $encryptedFileUri"
      )

  private class MegaClientImpl[F[_]: Async: LogWriter](@unused httpClient: Client[F]) extends MegaClient[F] {

    // https://gist.github.com/CypherpunkSamurai/7b476aef9d42fc29fe8a904a91039e85#file-basemax-megadownloader-md
    override def fetchFile(filename: String, url: Uri): Resource[F, Path] = {
      val fileContent: F[Array[Byte]] = for {
        _                  <- LogWriter.info(s"[MegaClient - $filename] Extract Mega Components from input Uri")
        mediaUriComponents <- extractMegaUrlComponents(url)
        _                  <- LogWriter.info(s"[MegaClient - $filename] Call Mega to get the Encrypted file")
        encryptedFileHttpRequest = buildMegaEncryptedFileUrlRequest[F](mediaUriComponents)
        encryptedFileContent <- getEncryptedFileContent(
          filename = filename,
          encryptedFileUri = encryptedFileHttpRequest,
          httpClient = httpClient
        )
        _                    <- LogWriter.info(s"[MegaClient - $filename] Decrypt the Encrypted file")
        decryptedFileContent <- decryptFileContent(encryptedFileContent, mediaUriComponents)
        _                    <- LogWriter.info(s"[MegaClient - $filename] ✅ File successfully downloaded")
      } yield decryptedFileContent

      Resource.eval(fileContent).flatMap(content => Repository.toTempFile(filename, content))
    }

  }

  private[http] def extractMegaUrlComponents[F[_]: MonadThrow](url: Uri): F[MegaUriComponents] = {
    for {
      fileId <- MonadThrow[F]
        .fromOption(url.path.segments.get(1), ErrorMegaUriFileIdtNotFound(url))
        .map(_.decoded())

      decryptionKey <- MonadThrow[F]
        .fromOption(url.fragment, ErrorMegaUriDecryptionKeyNotFound(url))
    } yield MegaUriComponents(fileId = fileId, decryptKey = decryptionKey)
  }

  private def buildMegaEncryptedFileUrlRequest[F[_]](megaUriComponents: MegaUriComponents): Request[F] = {
    val payload = Json.arr(MegaEncryptedFileRequest(megaUriComponents).asJson)

    Request[F](
      method = Method.POST,
      uri = megaApiUri
    ).withEntity(payload)
  }

  private def getEncryptedFileContent[F[_]: Async: LogWriter](
      filename: String,
      encryptedFileUri: Request[F],
      httpClient: Client[F]
  ): F[Array[Byte]] = {
    for {
      _            <- LogWriter.info(s"[MegaClient - $filename] 🚀 Executing the Encrypted File Request")
      response     <- httpClient.expect[Array[MegaEncryptedFileResponse]](encryptedFileUri)
      encryptedUri <- Async[F].fromOption(
        response.headOption,
        MegaEncryptedFileErrorEmptyResponse(filename, encryptedFileUri.uri)
      )
      encryptedFileContent <- httpClient.get(encryptedUri.g)(_.body.compile.to(Array))
      _                    <- LogWriter.info(s"[MegaClient - $filename] ✅ Encrypted File")
    } yield encryptedFileContent
  }
  private def decryptFileContent[F[_]: Monad](
      encryptedFileContent: Array[Byte],
      megaUriComponents: MegaUriComponents
  ): F[Array[Byte]] = { ??? }
}
