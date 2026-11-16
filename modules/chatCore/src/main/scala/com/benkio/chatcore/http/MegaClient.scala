package com.benkio.chatcore.http

import cats.effect.Async
import cats.effect.Resource
import cats.implicits.*
import cats.MonadThrow
import com.benkio.chatcore.http.MegaEncryptedFileRequest.given
import com.benkio.chatcore.http.MegaEncryptedFileResponse.given
import com.benkio.chatcore.repository.Repository
import fs2.Chunk
import fs2.Pipe
import fs2.Stream
import io.circe.syntax.*
import io.circe.Json
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec
import javax.crypto.Cipher
import log.effect.LogWriter
import org.http4s.*
import org.http4s.circe.*
import org.http4s.circe.CirceEntityDecoder.given
import org.http4s.client.middleware.FollowRedirect
import org.http4s.client.Client
import org.http4s.syntax.literals.*

import java.nio.file.Files
import java.nio.file.Path
import java.util.Base64
import scala.annotation.unused

trait MegaClient[F[_]] {
  def fetchFile(filename: String, url: Uri): Resource[F, Path]
}

object MegaClient {
  val megaApiUri: Uri = uri"https://g.api.mega.co.nz/cs"

  /** No HTTP cache here, unlike the Dropbox client: Mega answers its downloads with `Cache-Control: no-store`, so
    * nothing would be kept, and the caching middleware reads the whole body into memory to build its entry, whatever
    * the response says: the file would be held once more while it is downloaded.
    */
  def apply[F[_]: Async: LogWriter](httpClient: Client[F]): F[MegaClient[F]] =
    new MegaClientImpl[F](httpClient = FollowRedirect(3)(httpClient)).pure[F].widen

  final case class MegaUriComponents(fileId: String, decryptKey: String)

  final case class ErrorMegaUriFileIdtNotFound(url: Uri)
      extends Throwable(s"[MegaClient] 🚫 Error in extracting the first segment from the Url: $url")
  final case class ErrorMegaUriDecryptionKeyNotFound(url: Uri)
      extends Throwable(s"[MegaClient] 🚫 Expected '#' in the first segment from the Url: $url")
  final case class MegaEncryptedFileErrorEmptyResponse(filename: String, encryptedFileUris: List[Uri])
      extends Throwable(
        s"[MegaClient - $filename] 🚫 Expected non empty array response encripted uris: $encryptedFileUris"
      )

  final case class MegaDownloadFailed(filename: String, downloadUri: Uri, status: Status)
      extends Throwable(s"[MegaClient - $filename] 🚫 Download of $downloadUri answered $status")

  private class MegaClientImpl[F[_]: Async: LogWriter](@unused httpClient: Client[F]) extends MegaClient[F] {
    // https://gist.github.com/CypherpunkSamurai/7b476aef9d42fc29fe8a904a91039e85#file-basemax-megadownloader-md
    //
    // The file is decrypted while it is downloaded and written to the temporary file chunk by chunk: the memory does not
    // depend on the size of the file (the whole content, encrypted and decrypted, used to be held in two arrays).
    override def fetchFile(filename: String, url: Uri): Resource[F, Path] =
      for {
        _ <- Resource.eval(LogWriter.info(s"[MegaClient - $filename] Extract Mega Components from input Uri"))
        mediaUriComponents <- Resource.eval(extractMegaUrlComponents(url))
        _            <- Resource.eval(LogWriter.info(s"[MegaClient - $filename] Call Mega to get the Encrypted file"))
        downloadUris <- Resource.eval(
          getDownloadUris(
            filename = filename,
            encryptedFileUri = buildMegaEncryptedFileUrlRequest[F](mediaUriComponents),
            httpClient = httpClient
          )
        )
        path <- firstSuccessful(
          downloadUris.map(downloadDecrypted(filename, _, mediaUriComponents, httpClient)),
          MegaEncryptedFileErrorEmptyResponse(filename, downloadUris)
        )
        _ <- Resource.eval(LogWriter.info(s"[MegaClient - $filename] ✅ File successfully downloaded"))
      } yield path

  }

  private def firstSuccessful[F[_]: Async, A](resources: List[Resource[F, A]], ifEmpty: Throwable): Resource[F, A] =
    resources match {
      case Nil                => Resource.raiseError[F, A, Throwable](ifEmpty)
      case resource :: others => resource.handleErrorWith(_ => firstSuccessful(others, ifEmpty))
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

  /** The addresses Mega gives to download the (encrypted) file. */
  private def getDownloadUris[F[_]: Async: LogWriter](
      filename: String,
      encryptedFileUri: Request[F],
      httpClient: Client[F]
  ): F[List[Uri]] =
    for {
      _                    <- LogWriter.info(s"[MegaClient - $filename] 🚀 Executing the Encrypted File Request")
      response             <- httpClient.expect[Array[MegaEncryptedFileResponse]](encryptedFileUri)
      encryptedUriResponse <- Async[F].fromOption(
        response.headOption,
        MegaEncryptedFileErrorEmptyResponse(filename, List(encryptedFileUri.uri))
      )
    } yield encryptedUriResponse.g.fold(List(_), _.toList)

  /** Downloads the file, decrypts it on the way and writes it into a temporary file: the file is deleted if the
    * download fails, so the next address starts from scratch.
    */
  private def downloadDecrypted[F[_]: Async: LogWriter](
      filename: String,
      downloadUri: Uri,
      megaUriComponents: MegaUriComponents,
      httpClient: Client[F]
  ): Resource[F, Path] =
    Repository
      .tempFile[F](filename)
      .evalTap(path =>
        httpClient.get(downloadUri)(response =>
          Async[F].raiseUnless(response.status.isSuccess)(MegaDownloadFailed(filename, downloadUri, response.status)) *>
            Resource
              .fromAutoCloseable(Async[F].blocking(Files.newOutputStream(path)))
              .use(out =>
                response.body
                  .through(decrypt[F](megaUriComponents))
                  .chunks
                  .evalMap(chunk => Async[F].blocking(out.write(chunk.toArray)))
                  .compile
                  .drain
              )
        ) <* LogWriter.info(s"[MegaClient - $filename] ✅ Encrypted File downloaded and decrypted")
      )

  /** AES-CTR is a stream cipher: the content is decrypted chunk by chunk, with the same result as in one piece. */
  private[http] def decrypt[F[_]: Async](megaUriComponents: MegaUriComponents): Pipe[F, Byte, Byte] = encrypted =>
    Stream.eval(Async[F].delay(decryptionCipher(megaUriComponents))).flatMap { cipher =>
      val decrypted = encrypted.chunks.flatMap(chunk =>
        Stream
          .evalSeq(
            Async[F].delay(Option(cipher.update(chunk.toArray)).filter(_.nonEmpty).map(Chunk.array(_)).toList)
          )
          .flatMap(Stream.chunk)
      )
      decrypted ++ Stream.evalSeq(Async[F].delay(List(Chunk.array(cipher.doFinal())))).flatMap(Stream.chunk)
    }

  private def decryptionCipher(megaUriComponents: MegaUriComponents): Cipher = {
    val rawKey = decodeMegaBase64Url(megaUriComponents.decryptKey)
    require(rawKey.length == 32, s"Invalid MEGA key length: ${rawKey.length}, expected 32")

    // MEGA file key layout: [k0 k1 k2 k3 n0 n1 m0 m1]
    // AES-CTR key is [k0^n0, k1^n1, k2^m0, k3^m1], IV starts with nonce [n0, n1].
    val aesKey = xorSlice(bytes = rawKey, leftStart = 0, rightStart = 16, length = 16)
    val iv     = new Array[Byte](16)
    System.arraycopy(rawKey, 16, iv, 0, 8)

    val cipher = Cipher.getInstance("AES/CTR/NoPadding")
    cipher.init(Cipher.DECRYPT_MODE, new SecretKeySpec(aesKey, "AES"), new IvParameterSpec(iv))
    cipher
  }

  private def xorSlice(bytes: Array[Byte], leftStart: Int, rightStart: Int, length: Int): Array[Byte] = {
    val out = new Array[Byte](length)
    var i   = 0
    while i < length do {
      out(i) = (bytes(leftStart + i) ^ bytes(rightStart + i)).toByte
      i += 1
    }
    out
  }

  private def decodeMegaBase64Url(input: String): Array[Byte] = {
    val padLen = (4 - (input.length % 4)) % 4
    Base64.getUrlDecoder.decode(input + ("=" * padLen))
  }
}
