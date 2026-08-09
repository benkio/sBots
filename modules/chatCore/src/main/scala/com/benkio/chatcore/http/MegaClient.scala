package com.benkio.chatcore.http

import cats.effect.Async
import cats.effect.Resource
import cats.implicits.*
import com.benkio.chatcore.repository.Repository
import io.chrisdavenport.mules.*
import io.chrisdavenport.mules.http4s.*
import io.circe.Json
import io.circe.parser.parse
import log.effect.LogWriter
import org.http4s.*
import org.http4s.client.middleware.FollowRedirect
import org.http4s.client.Client
import org.http4s.Method.GET
import org.http4s.Method.POST
import org.http4s.syntax.literals.*

import java.nio.file.Path
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

  final case class UnexpectedMegaResponse[F[_]](response: Response[F]) extends Throwable
  final case class InvalidMegaFileUrl(url: Uri)
      extends Throwable(s"[MegaClient] Invalid Mega file URL: $url")
  final case class InvalidMegaApiResponse(reason: String, body: String)
      extends Throwable(s"[MegaClient] Invalid Mega API response: $reason. body=$body")

  private class MegaClientImpl[F[_]: Async: LogWriter](httpClient: Client[F], megaApiUri: Uri) extends MegaClient[F] {

    private def extractFileId(url: Uri): F[String] =
      Async[F].fromOption({
        val segments = url.path.segments.map(_.decoded()).toList
        segments
          .sliding(2)
          .collectFirst {
            case first :: second :: Nil if first == "file" && second.nonEmpty => second
          }
      }, InvalidMegaFileUrl(url))

    private def extractDownloadUri(responseBody: String): F[Uri] =
      Async[F].fromEither(
        for {
          json <- parse(responseBody).leftMap(e => InvalidMegaApiResponse(s"cannot parse json: ${e.getMessage}", responseBody))
          arr <- json.asArray.toRight(InvalidMegaApiResponse("expected top-level JSON array", responseBody))
          first <- arr.headOption.toRight(InvalidMegaApiResponse("empty response array", responseBody))
          downloadUrl <- first.hcursor
            .get[String]("g")
            .leftMap(e => InvalidMegaApiResponse(s"missing download url 'g': ${e.getMessage}", responseBody))
          uri <- Uri.fromString(downloadUrl).leftMap(e =>
            InvalidMegaApiResponse(s"invalid download url uri: ${e.message}", responseBody)
          )
        } yield uri
      )

    private def resolveMegaDownloadUri(url: Uri): F[Uri] =
      for {
        fileId <- extractFileId(url)
        requestBody = Json.arr(
          Json.obj(
            "a" -> Json.fromString("g"),
            "g" -> Json.fromInt(1),
            "p" -> Json.fromString(fileId)
          )
        ).noSpaces
        request = Request[F](POST, megaApiUri)
          .withEntity(requestBody)
        responseBody <- httpClient.run(request).use { response =>
          response.as[String].flatMap(body =>
            Async[F].raiseWhen(response.status != Status.Ok)(UnexpectedMegaResponse[F](response)).as(body)
          )
        }
        downloadUri <- extractDownloadUri(responseBody)
      } yield downloadUri

    def fetchFile(filename: String, url: Uri): Resource[F, Path] = {
      for {
        downloadUri <- Resource.eval(resolveMegaDownloadUri(url))
        request = Request[F](GET, downloadUri)
        path <- httpClient.run(request).flatMap(response =>
          for {
            content <- Resource.eval(response.body.compile.toList)
            _       <- Resource
              .eval(LogWriter.info(s"[MegaClient] received ${content.length} bytes for $filename from $downloadUri"))
            _      <- Resource.eval(Async[F].raiseWhen(response.status != Status.Ok)(UnexpectedMegaResponse[F](response)))
            _      <- Resource.eval(Async[F].raiseWhen(content.isEmpty)(UnexpectedMegaResponse[F](response)))
            result <- Repository.toTempFile(filename, content.toArray)
          } yield result
        )
      } yield path
    }
  }
}
