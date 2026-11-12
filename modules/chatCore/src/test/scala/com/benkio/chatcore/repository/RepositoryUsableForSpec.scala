package com.benkio.chatcore.repository

import cats.data.NonEmptyList
import cats.effect.IO
import cats.effect.Resource
import com.benkio.chatcore.model.media.MediaResource
import com.benkio.chatcore.model.media.MediaResource.MediaResourceFile
import com.benkio.chatcore.model.media.MediaResource.MediaResourceIFile
import com.benkio.chatcore.model.reply.GifFile
import com.benkio.chatcore.model.reply.MediaFile
import com.benkio.chatcore.model.CommandSyntax
import com.benkio.chatcore.model.MarkdownSupport
import com.benkio.chatcore.model.MediaKind
import com.benkio.chatcore.model.Platform
import com.benkio.chatcore.model.PlatformCapabilities
import com.benkio.chatcore.model.SBotInfo.SBotId
import munit.CatsEffectSuite

import java.nio.file.Path

class RepositoryUsableForSpec extends CatsEffectSuite {

  private val file: MediaResource[IO]  = MediaResourceFile(Resource.pure(Path.of("a.mp4")))
  private val iFile: MediaResource[IO] = MediaResourceIFile("fileId")
  private val mediaFile: MediaFile     = GifFile("a.mp4")
  private val botId                    = SBotId("bot")

  private def repositoryOf(resources: NonEmptyList[MediaResource[IO]]): Repository[IO] = new Repository[IO] {
    override def getResourcesByKind(criteria: String, botId: SBotId) =
      Resource.pure(Right(NonEmptyList.one(resources)))
    override def getResourceFile(mediaFile: MediaFile) = Resource.pure(Right(resources))
  }

  private def names(r: NonEmptyList[MediaResource[IO]]): List[String] = r.toList.map {
    case MediaResourceIFile(_) => "ifile"
    case _                     => "file"
  }

  private val withIds: PlatformCapabilities = PlatformCapabilities(
    platform = Platform.Telegram,
    supportedMedia = MediaKind.values.toSet,
    maxTextLength = 4096,
    markdown = MarkdownSupport.Native,
    supportsDelete = true,
    supportsEdit = true,
    supportsButtons = true,
    acceptsFileIds = true,
    commandSyntax = CommandSyntax.Slash
  )
  private val withoutIds: PlatformCapabilities = withIds.copy(acceptsFileIds = false)

  test("a platform that accepts file ids gets every resource") {
    repositoryOf(NonEmptyList.of(iFile, file))
      .usableFor(withIds)
      .getResourceFile(mediaFile)
      .use(r => IO(assertEquals(r.map(names), Right(List("ifile", "file")))))
  }

  test("a platform that does not accept file ids gets only the others, in order") {
    repositoryOf(NonEmptyList.of(iFile, file))
      .usableFor(withoutIds)
      .getResourceFile(mediaFile)
      .use(r => IO(assertEquals(r.map(names), Right(List("file")))))
  }

  test("only file ids is a not found error") {
    repositoryOf(NonEmptyList.one(iFile))
      .usableFor(withoutIds)
      .getResourceFile(mediaFile)
      .use(r =>
        IO(assertEquals(r.left.map(_.getClass), Left(classOf[Repository.RepositoryError.NoResourcesFoundFile])))
      )
  }

  test("the resources by kind lose the file ids too, and a kind with none is a not found error") {
    val some = repositoryOf(NonEmptyList.of(iFile, file)).usableFor(withoutIds)
    val none = repositoryOf(NonEmptyList.one(iFile)).usableFor(withoutIds)
    some
      .getResourcesByKind("c", botId)
      .use(r => IO(assertEquals(r.map(_.toList.map(names)), Right(List(List("file")))))) >>
      none.getResourcesByKind("c", botId).use(r => IO(assert(r.isLeft)))
  }
}
