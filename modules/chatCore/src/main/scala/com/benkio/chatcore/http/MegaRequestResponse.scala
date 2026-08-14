package com.benkio.chatcore.http

import com.benkio.chatcore.http.MegaClient.MegaUriComponents
import io.circe.generic.semiauto.deriveDecoder
import io.circe.generic.semiauto.deriveEncoder
import io.circe.Decoder
import io.circe.Decoder.decodeArray
import io.circe.Decoder.given
import io.circe.Encoder
import org.http4s.Uri

final case class MegaEncryptedFileRequest(
    a: String,
    v: Int,
    p: String,
    ssl: Int,
    g: Int
)

object MegaEncryptedFileRequest {
  given Encoder[MegaEncryptedFileRequest] = deriveEncoder[MegaEncryptedFileRequest]

  def apply(megaUriComponents: MegaUriComponents): MegaEncryptedFileRequest =
    new MegaEncryptedFileRequest(
      a = "g",
      v = 2,
      p = megaUriComponents.fileId,
      ssl = 2,
      g = 1
    )
}

/*
{
    "s" : 735059,
    "at" : "MTJSxCxOyUGUhPCfpl_VW4m7Rdw-DK588eOAVKnPkqxr-bx5ILnhHeco1i2rRXZ_Llr4D25fErqKeyl8aaYaF5LNpfZyZruD8c7ZPt71N0U",
    "msd" : 1,
    "fa" : "616:8*BY0b0KrIYOE",
    "g" : "https://gfs270n150.userstorage.mega.co.nz/dl/AFgv0Si5mN0VvwIdHNMm601JzDyC1pmMrcszCdlPRy1Di0TA39Crd37shswG97fj6yvFeHisZmrLiemrnMxxpqxt9zcAHUUoPDodj0NdTYAVRQPPTcP_zgtGRDGdrg",
    "ip" : [
      "89.44.168.200",
      "2001:678:25c:2216::200"
    ],
    "fh" : "KhZLEObSbJk"
  }
 */
final case class MegaEncryptedFileResponse(
    s: Int,
    at: String,
    msd: Int,
    fa: String,
    g: Either[Uri, Array[Uri]],
    ip: Array[String],
    fh: String
)

object MegaEncryptedFileResponse {
  given uriDecoder: Decoder[Uri] = Decoder.decodeString.emap(value =>
    Uri
      .fromString(value)
      .fold(parseFailure => Left(parseFailure.message), uri => Right(uri))
  )
  given uriOrUriArrayDecoder: Decoder[Either[Uri, Array[Uri]]] =
    uriDecoder.either(decodeArray[Uri])
  given Decoder[MegaEncryptedFileResponse] = deriveDecoder[MegaEncryptedFileResponse]
}
