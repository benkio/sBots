package com.benkio.richardphjbensonbot

import cats.effect.{Async, Resource}
import com.benkio.telegrambotinfrastructure.botCapabilities.ResourceAccess

import scala.io.Source
import scala.util.Random

object RandomYoutubeLinkCommand {

  val youtubeLinkSources: String = "rphjb_YoutubeLinkSources"
  lazy val random = new Random()

  def selectRandomYoutubeLinks[F[_]: Async](resourceAccess: ResourceAccess) : Resource[F, List[String]] = for {
    sourceFiles <- resourceAccess.getResourcesByKind[F](youtubeLinkSources)
    sourceSelectedIndex <- Resource.eval(Async[F].delay(random.between(0, sourceFiles.length)))
    youtubeLinkReplies = Source.fromFile(sourceFiles(sourceSelectedIndex)).getLines().toList
  } yield youtubeLinkReplies
}
