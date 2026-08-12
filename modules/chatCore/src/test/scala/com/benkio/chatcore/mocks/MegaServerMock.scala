package com.benkio.chatcore.mocks

import cats.effect.IO
import cats.effect.Resource
import com.comcast.ip4s.*
import io.circe.Json
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec
import javax.crypto.Cipher
import org.http4s.dsl.io.*
import org.http4s.ember.server.EmberServerBuilder
import org.http4s.headers.Host
import org.http4s.server.middleware.CORS
import org.http4s.server.middleware.Logger
import org.http4s.server.Server
import org.http4s.HttpRoutes

import java.nio.ByteBuffer
import java.util.Base64

object MegaServerMock {
  val servedFile   = scala.io.Source.fromResource("test.txt")
  val testMegaLink = "https://mega.nz/file/LDxATYLQ#cAdB5jxfJov_BFr-bu548G7cXtLgEsdih4e69PmJ6OI"

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

  private def encryptWithMegaKey(plain: Array[Byte], keyFragment: String): Array[Byte] = {
    val keyBytes       = decodeBase64Url(keyFragment)
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
    val cipher    = Cipher.getInstance("AES/CTR/NoPadding")
    val secretKey = new SecretKeySpec(intsToBytes(secretKeyWords), "AES")
    val ivSpec    = new IvParameterSpec(intsToBytes(ivWords))
    cipher.init(Cipher.ENCRYPT_MODE, secretKey, ivSpec)
    cipher.doFinal(plain)
  }

  def build(response: String): Resource[IO, Server] = {
    val encryptedResponse =
      encryptWithMegaKey(
        plain = response.getBytes(),
        keyFragment = testMegaLink.split("#").last
      )

    val megaRoutes: HttpRoutes[IO] = HttpRoutes.of[IO] {
      case req @ POST -> Root / "cs" =>
        val hostHeader  = req.headers.get[Host]
        val host        = hostHeader.map(_.host).getOrElse("127.0.0.1")
        val portSuffix  = hostHeader.flatMap(_.port).map(p => s":$p").getOrElse("")
        val downloadUrl = s"http://$host$portSuffix/download/testFile"
        Ok(
          Json
            .arr(
              Json.obj(
                "g" -> Json.arr(Json.fromString(downloadUrl))
              )
            )
            .noSpaces
        )
      case GET -> Root / "download" / "testFile" =>
        Ok(encryptedResponse)
    }

    val httpApp = Logger.httpApp(true, true)(
      CORS.policy.withAllowOriginAll(megaRoutes.orNotFound)
    )

    EmberServerBuilder
      .default[IO]
      .withHost(host"127.0.0.1")
      .withPort(Port.fromInt(0).get)
      .withHttpApp(httpApp)
      .build
  }

  def buildInvalidApiResponse(responseBody: String): Resource[IO, Server] = {
    val megaRoutes: HttpRoutes[IO] = HttpRoutes.of[IO] { case POST -> Root / "cs" =>
      Ok(responseBody)
    }
    val httpApp = Logger.httpApp(true, true)(
      CORS.policy.withAllowOriginAll(megaRoutes.orNotFound)
    )

    EmberServerBuilder
      .default[IO]
      .withHost(host"127.0.0.1")
      .withPort(Port.fromInt(0).get)
      .withHttpApp(httpApp)
      .build
  }
}
