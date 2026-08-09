package com.benkio.chatcore.mocks

import cats.effect.IO
import cats.effect.Resource
import com.comcast.ip4s.*
import io.circe.Json
import org.http4s.dsl.io.*
import org.http4s.ember.server.EmberServerBuilder
import org.http4s.headers.Host
import org.http4s.server.middleware.CORS
import org.http4s.server.middleware.Logger
import org.http4s.server.Server
import org.http4s.HttpRoutes

object MegaServerMock {
  val servedFile = scala.io.Source.fromResource("test.txt")

  def build(response: String): Resource[IO, Server] = {

    val megaRoutes: HttpRoutes[IO] = HttpRoutes.of[IO] {
      case req @ POST -> Root / "cs" =>
        val hostHeader = req.headers.get[Host]
        val host       = hostHeader.map(_.host).getOrElse("127.0.0.1")
        val portSuffix = hostHeader.flatMap(_.port).map(p => s":$p").getOrElse("")
        val downloadUrl = s"http://$host$portSuffix/download/testFile"
        Ok(
          Json.arr(
            Json.obj(
              "g" -> Json.fromString(downloadUrl)
            )
          ).noSpaces
        )
      case GET -> Root / "download" / "testFile" =>
        Ok(response)
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
    val megaRoutes: HttpRoutes[IO] = HttpRoutes.of[IO] {
      case POST -> Root / "cs" => Ok(responseBody)
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
