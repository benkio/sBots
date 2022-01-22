package com.benkio.richardphjbensonbot

import cats.effect.Async
import cats.effect.Resource
import com.benkio.telegrambotinfrastructure.botCapabilities.ResourceAccess

import scala.io.Source
import scala.util.Random

object RandomYoutubeLinkCommand {

  val youtubeLinkSources: String = "rphjb_YoutubeLinkSources"
  lazy val random                = new Random()

  def selectRandomYoutubeLink[F[_]: Async](resourceAccess: ResourceAccess): Resource[F, String] = for {
    sourceFiles            <- resourceAccess.getResourcesByKind[F](youtubeLinkSources)
    sourceSelectedIndex    <- Resource.eval(Async[F].delay(random.between(0, sourceFiles.length)))
    sourceSelectedRawBytes <- resourceAccess.getResourceByteArray(sourceFiles(sourceSelectedIndex).getPath)
    youtubeLinkReplies = Source.fromRawBytes(sourceSelectedRawBytes).getLines().toList
    lineSelectedIndex <- Resource.eval(Async[F].delay(random.between(0, youtubeLinkReplies.length)))
  } yield youtubeLinkReplies(lineSelectedIndex)
}
