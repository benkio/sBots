package com.benkio.telegrambotinfrastructure.mocks

import cats.effect.IO
import cats.effect.Resource
import com.comcast.ip4s.*
import org.http4s.dsl.io.*
import org.http4s.ember.server.EmberServerBuilder
import org.http4s.headers.Location
import org.http4s.implicits.uri
import org.http4s.server.middleware.CORS
import org.http4s.server.middleware.Logger
import org.http4s.server.Server
import org.http4s.HttpRoutes
import org.http4s.Response
import org.http4s.Uri

object DropboxServerMock {
  private val serverUri: Uri = uri"http://0.0.0.0:8080"

  val servedFile = scala.io.Source.fromResource("test.txt")

  def build(response: String): Resource[IO, Server] = {

    val dropboxRoutes: HttpRoutes[IO] = HttpRoutes.of[IO] {
      case GET -> Root / "emptyResponseFilename" =>
        Ok()
      case GET -> Root / "302TestFile" =>
        println("[DropboxServerMock] 320")
        Found(Location(serverUri / "200TestFile"))
      case GET -> Root / "200TestFile" =>
        println("[DropboxServerMock] 200: " + response)
        Ok(response)
    }

    val httpApp = Logger.httpApp(true, true)(
      CORS.policy.withAllowOriginAll(dropboxRoutes.orNotFound)
    )

    EmberServerBuilder
      .default[IO]
      .withHost(host"0.0.0.0")
      .withPort(port"8080")
      .withHttpApp(httpApp)
      .build
  }
}
