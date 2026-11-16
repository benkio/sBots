package com.benkio.chatcore

import cats.effect.IO
import cats.syntax.all.*
import com.benkio.chatcore.mocks.FakePlatformAdapter
import com.benkio.chatcore.model.reply.Document
import com.benkio.chatcore.model.reply.MarkdownFile
import com.benkio.chatcore.model.reply.MediaFile
import com.benkio.chatcore.model.reply.MediaReply
import com.benkio.chatcore.model.reply.Mp3File
import com.benkio.chatcore.model.reply.ReplyValue
import com.benkio.chatcore.model.MediaKind
import com.benkio.chatcore.model.PlatformCapabilities
import munit.Assertions.*
import munit.CatsEffectSuite

class BotEngineMediaSelectionSpec extends CatsEffectSuite {

  import BotEngineMediaSelectionSpec.pick

  // IRC like: sends the markdown files, as text, and nothing else
  private val markdownOnly: PlatformCapabilities =
    FakePlatformAdapter.textOnlyCapabilities.copy(supportedMedia = Set(MediaKind.MarkdownFile))

  // Telegram like: sends any media
  private val withMedia: PlatformCapabilities =
    FakePlatformAdapter.textOnlyCapabilities.copy(supportedMedia = MediaKind.values.toSet)

  test("on a text only platform the markdown file of the set is always chosen") {
    pick(Set(Mp3File("a.mp3"), MarkdownFile("a.md"), Document("a.pdf")), markdownOnly).map(chosen =>
      assertEquals(chosen.toSet, Set[ReplyValue](MarkdownFile("a.md")))
    )
  }

  test("on a text only platform the first markdown file by file name is always chosen") {
    pick(Set(MarkdownFile("c.md"), Mp3File("a.mp3"), MarkdownFile("a.md"), MarkdownFile("b.md")), markdownOnly).map(
      chosen => assertEquals(chosen.toSet, Set[ReplyValue](MarkdownFile("a.md")))
    )
  }

  test("on a text only platform a set without markdown files gets a random file of the set") {
    val files = Set[MediaFile](Mp3File("a.mp3"), Mp3File("b.mp3"), Mp3File("c.mp3"))
    pick(files, markdownOnly).map { chosen =>
      assert(chosen.toSet.subsetOf(files.toSet[ReplyValue]), chosen)
      assert(chosen.toSet.size > 1, s"expected a random choice, got always ${chosen.headOption}")
    }
  }

  test("on a platform that sends media the choice among the files stays random") {
    val files = Set[MediaFile](Mp3File("a.mp3"), MarkdownFile("a.md"))
    pick(files, withMedia).map { chosen =>
      assert(chosen.toSet.subsetOf(files.toSet[ReplyValue]), chosen)
      assertEquals(chosen.toSet.size, 2)
    }
  }

  test("a platform that sends nothing is not text only, so the choice stays random") {
    val files = Set[MediaFile](Mp3File("a.mp3"), MarkdownFile("a.md"))
    pick(files, FakePlatformAdapter.textOnlyCapabilities).map(chosen => assertEquals(chosen.toSet.size, 2))
  }
}

object BotEngineMediaSelectionSpec {

  private val repetitions = 50

  /** The files chosen by the engine for the same media reply, many times. */
  def pick(files: Set[MediaFile], capabilities: PlatformCapabilities): IO[List[ReplyValue]] =
    List.fill(repetitions)(BotEngine.runPlainReply[IO](MediaReply(files), capabilities)).sequence

  /** A set with a markdown file and another media: on a platform that sends media both are chosen, at random. */
  def assertRandomChoice(capabilities: PlatformCapabilities): IO[Unit] = {
    val files = Set[MediaFile](Mp3File("a.mp3"), MarkdownFile("a.md"))
    pick(files, capabilities).map { chosen =>
      assert(!capabilities.textOnly, s"${capabilities.platform} is not a text only platform")
      assertEquals(chosen.toSet, files.toSet[ReplyValue])
    }
  }
}
