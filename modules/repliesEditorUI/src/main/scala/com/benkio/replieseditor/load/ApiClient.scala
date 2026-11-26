package com.benkio.replieseditor.load

import io.circe.parser.parse
import io.circe.Decoder
import io.circe.Json
import org.scalajs.dom
import org.scalajs.dom.Headers
import org.scalajs.dom.HttpMethod
import org.scalajs.dom.RequestInit
import org.scalajs.dom.Response

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

object ApiClient {

  private def decodeResponse(resp: Response): Future[Either[String, Json]] =
    resp.text().toFuture.map { body =>
      if !resp.ok then Left(s"HTTP ${resp.status}: $body")
      else
        parse(body).fold(e => Left(e.message), Right(_))
    }

  def fetchJson(url: String): Future[Either[String, Json]] =
    dom.fetch(url).toFuture.flatMap(decodeResponse)

  def fetchJson(url: String, init: RequestInit): Future[Either[String, Json]] =
    dom.fetch(url, init).toFuture.flatMap(decodeResponse)

  def postJson(url: String, json: Json): Future[Either[String, Json]] = {
    val hdrs = new Headers()
    hdrs.set("Content-Type", "application/json")
    val init = new RequestInit {
      method = HttpMethod.POST
      this.headers = hdrs
      body = json.noSpaces
    }
    fetchJson(url, init)
  }

  def decodeOrError[A: Decoder](json: Json): Either[String, A] =
    json.as[A].fold(e => Left(e.message), Right(_))
}
