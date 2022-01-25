package com.benkio.telegrambotinfrastructure.botCapabilities

import cats.effect.Async
import cats.effect.Resource
import cats.implicits._
import com.benkio.telegrambotinfrastructure.botCapabilities.ResourceAccess

import scala.io.Source
import scala.util.Random

object RandomLinkCommand {

  lazy val random = new Random()

  def selectRandomLinkByKeyword[F[_]: Async](
      keyword: String,
      resourceAccess: ResourceAccess,
      youtubeLinkSources: String
  ): Resource[F, String] = for {
    sourceFiles <- resourceAccess.getResourcesByKind[F](youtubeLinkSources)
    sourceRawBytesArray <- sourceFiles.traverse(f =>
      resourceAccess
        .getResourceByteArray(f.getPath)
    )
    sourceRawBytes = sourceRawBytesArray.foldLeft(Array.empty[Byte]) { case (acc, bs) =>
      acc ++ (('\n'.toByte) +: bs)
    }
    youtubeLinkReplies = Source.fromRawBytes(sourceRawBytes).getLines().toList.filter(_.contains(keyword))
    lineSelectedIndex <- Resource.eval(Async[F].delay(random.between(0, youtubeLinkReplies.length)))
  } yield youtubeLinkReplies(lineSelectedIndex)
}
