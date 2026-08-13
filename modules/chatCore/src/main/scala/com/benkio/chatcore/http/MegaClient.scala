package com.benkio.chatcore.http

import cats.effect.Async
import cats.effect.Resource
import cats.implicits.*
import com.benkio.chatcore.repository.Repository
import io.chrisdavenport.mules.*
import io.chrisdavenport.mules.http4s.*
import io.circe.parser.parse
import io.circe.Json
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec
import javax.crypto.Cipher
import log.effect.LogWriter
import org.http4s.*
import org.http4s.client.middleware.FollowRedirect
import org.http4s.client.Client
import org.http4s.syntax.literals.*
import org.http4s.Method.GET
import org.http4s.Method.POST

import java.nio.file.Path
import java.nio.ByteBuffer
import java.util.Base64
import scala.concurrent.duration.*

trait MegaClient[F[_]] {
  def fetchFile(filename: String, url: Uri): Resource[F, Path]
}

object MegaClient {
  def apply[F[_]: Async: LogWriter](
      httpClient: Client[F],
      megaApiUri: Uri = uri"https://g.api.mega.co.nz/cs?id=1"
  ): F[MegaClient[F]] = for {
    httpCache <- MemoryCache.ofSingleImmutableMap[F, (Method, Uri), CacheItem](defaultExpiration =
      TimeSpec.fromDuration(6.hours)
    )
    cachedMiddleware = CacheMiddleware.client(httpCache, CacheType.Public)
  } yield new MegaClientImpl[F](
    httpClient = cachedMiddleware(FollowRedirect(3)(httpClient)),
    megaApiUri = megaApiUri
  )

  final case class UnexpectedMegaResponse[F[_]](message: String, response: Response[F]) extends Throwable(message)
  final case class InvalidMegaFileUrl(url: Uri) extends Throwable(s"[MegaClient] Invalid Mega file URL: $url")
  final case class InvalidMegaFileKey(url: Uri) extends Throwable(s"[MegaClient] Invalid Mega file key in URL: $url")
  final case class InvalidMegaApiResponse(reason: String, body: String)
      extends Throwable(s"[MegaClient] Invalid Mega API response: $reason. body=$body")
  final case class CloudraidUnsupported(urlCount: Int)
      extends Throwable(s"[MegaClient] Cloudraid response unsupported for direct download (urlCount=$urlCount)")

  private final case class MegaDecryptionConfig(
      secretKey: Array[Byte],
      iv: Array[Byte]
  )

  private class MegaClientImpl[F[_]: Async: LogWriter](httpClient: Client[F], megaApiUri: Uri) extends MegaClient[F] {

    private def extractFileId(url: Uri): F[String] =
      Async[F].fromOption(
        {
          val segments = url.path.segments.map(_.decoded()).toList
          segments
            .sliding(2)
            .collectFirst {
              case first :: second :: Nil if first == "file" && second.nonEmpty => second
            }
        },
        InvalidMegaFileUrl(url)
      )

    private def decodeBase64Url(input: String): Array[Byte] = {
      val normalized = input.replace('-', '+').replace('_', '/')
      val paddingLen = (4 - (normalized.length % 4)) % 4
      val padded     = normalized + ("=" * paddingLen)
      Base64.getDecoder.decode(padded)
    }

    private def toIntBigEndian(bytes: Array[Byte], offset: Int): Int =
      ByteBuffer.wrap(bytes, offset, 4).getInt

    private def intsToBytes(values: Array[Int]): Array[Byte] = {
      val buffer = ByteBuffer.allocate(values.length * 4)
      values.foreach(buffer.putInt)
      buffer.array()
    }

    private def extractMegaDecryptionConfig(url: Uri): F[MegaDecryptionConfig] =
      Async[F].fromOption(url.fragment, InvalidMegaFileKey(url)).flatMap { fragment =>
        val keyBytes = decodeBase64Url(fragment)
        if keyBytes.length != 32 then Async[F].raiseError(InvalidMegaFileKey(url))
        else {
          val keyWords       = Array.tabulate(8)(i => toIntBigEndian(keyBytes, i * 4))
          val secretKeyWords = Array(
            keyWords(0) ^ keyWords(4),
            keyWords(1) ^ keyWords(5),
            keyWords(2) ^ keyWords(6),
            keyWords(3) ^ keyWords(7)
          )
          val ivWords = Array(
            keyWords(4),
            keyWords(5),
            0,
            0
          )
          Async[F].pure(
            MegaDecryptionConfig(
              secretKey = intsToBytes(secretKeyWords),
              iv = intsToBytes(ivWords)
            )
          )
        }
      }

    private def decryptMegaContent(
        encrypted: Array[Byte],
        config: MegaDecryptionConfig
    ): F[Array[Byte]] = Async[F].delay {
      val cipher    = Cipher.getInstance("AES/CTR/NoPadding")
      val secretKey = new SecretKeySpec(config.secretKey, "AES")
      val ivSpec    = new IvParameterSpec(config.iv)
      cipher.init(Cipher.DECRYPT_MODE, secretKey, ivSpec)
      cipher.doFinal(encrypted)
    }

    private def extractDownloadUri(responseBody: String): F[Uri] =
      Async[F].fromEither(
        for {
          json <- parse(responseBody).leftMap(e =>
            InvalidMegaApiResponse(s"cannot parse json: ${e.getMessage}", responseBody)
          )
          arr    <- json.asArray.toRight(InvalidMegaApiResponse("expected top-level JSON array", responseBody))
          first  <- arr.headOption.toRight(InvalidMegaApiResponse("empty response array", responseBody))
          gField <- first.hcursor
            .get[Json]("g")
            .leftMap(e => InvalidMegaApiResponse(s"missing download url 'g': ${e.getMessage}", responseBody))
          downloadUrl <- gField.asString
            .orElse(
              gField.asArray
                .flatMap(urls =>
                  if urls.length == 1 then urls.headOption.flatMap(_.asString)
                  else None
                )
            )
            .toRight(
              gField.asArray
                .map(urls => CloudraidUnsupported(urls.length): Throwable)
                .getOrElse(
                  InvalidMegaApiResponse("download url 'g' is neither a string nor a 1-item array", responseBody)
                )
            )
          uri <- Uri
            .fromString(downloadUrl)
            .leftMap(e => InvalidMegaApiResponse(s"invalid download url uri: ${e.message}", responseBody))
        } yield uri
      )

    private def requestDownloadUri(fileId: String, includeV2: Boolean): F[Uri] = {
      val requestBody = Json
        .arr(
          Json
            .obj(
              "a" -> Json.fromString("g"),
              "g" -> Json.fromInt(1),
              "p" -> Json.fromString(fileId)
            )
            .deepMerge(
              if includeV2 then Json.obj("v" -> Json.fromInt(2))
              else Json.obj()
            )
        )
        .noSpaces
      val request = Request[F](POST, megaApiUri)
        .withEntity(requestBody)

      for {
        responseBody <- httpClient.run(request).use { response =>
          response
            .as[String]
            .flatMap(body =>
              Async[F]
                .raiseWhen(response.status != Status.Ok)(
                  UnexpectedMegaResponse[F](
                    message = s"[MegaClient] 🚫 Error, unexpected status: ${response.status}",
                    response = response
                  )
                )
                .as(body)
            )
        }
        downloadUri <- extractDownloadUri(responseBody)
      } yield downloadUri
    }

    private def resolveMegaDownloadUri(url: Uri): F[Uri] =
      for {
        fileId      <- extractFileId(url)
        downloadUri <- requestDownloadUri(fileId, includeV2 = true).handleErrorWith {
          case CloudraidUnsupported(urlCount) =>
            LogWriter.info(
              s"[MegaClient] detected cloudraid response with $urlCount urls for fileId=$fileId, falling back to non-v2 request"
            ) >> requestDownloadUri(fileId, includeV2 = false)
          case error => Async[F].raiseError(error)
        }
      } yield downloadUri

    def fetchFile(filename: String, url: Uri): Resource[F, Path] = {
      for {
        decryptConfig <- Resource.eval(extractMegaDecryptionConfig(url))
        downloadUri   <- Resource.eval(resolveMegaDownloadUri(url))
        request = Request[F](GET, downloadUri)
        path <- httpClient
          .run(request)
          .flatMap(response =>
            for {
              encryptedContent <- Resource.eval(response.body.compile.toList)
              _                <- Resource
                .eval(
                  LogWriter.info(
                    s"[MegaClient] received ${encryptedContent.length} encrypted bytes for $filename from $downloadUri"
                  )
                )
              _ <- Resource.eval(
                Async[F].raiseWhen(response.status != Status.Ok)(
                  UnexpectedMegaResponse[F](
                    message = s"[MegaClient] 🚫 Error, unexpected status: ${response.status}",
                    response = response
                  )
                )
              )
              _ <- Resource.eval(
                Async[F].raiseWhen(encryptedContent.isEmpty)(
                  UnexpectedMegaResponse[F](
                    message = "[MegaClient] 🚫 Error, encrypted content empty",
                    response = response
                  )
                )
              )
              decryptedContent <- Resource.eval(decryptMegaContent(encryptedContent.toArray, decryptConfig))
              result           <- Repository.toTempFile(filename, decryptedContent)
            } yield result
          )
      } yield path
    }
  }
}
