package com.benkio.replieseditor.server.store

import cats.effect.IO
import cats.effect.Resource
import cats.syntax.all.*
import com.benkio.replieseditor.server.module.ApiError
import com.benkio.replieseditor.server.module.RepliesChunk
import munit.CatsEffectSuite

import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.StandardCopyOption
import scala.concurrent.duration.*

/** Guards the cost of filtering a large, real replies collection. Richard is the largest bot in the repository and
  * exercises the same path used by the editor filter box: every keystroke asks the store for a filtered chunk.
  */
class BotStorePerformanceSpec extends CatsEffectSuite {

  private val botId                             = "rphjb"
  private val botName                           = "RichardPHJBensonBot"
  private val warmupRequests                    = 3
  private val measuredRequests                  = 20
  private val maxAverageRequest: FiniteDuration = 10.millis

  private def repoRoot: Path =
    sys.props
      .get("sbots.repoRoot")
      .map(Path.of(_))
      .filter(root => Files.isDirectory(root.resolve("modules").resolve("bots")))
      .getOrElse {
        var current = Path.of("").toAbsolutePath
        while current.getParent != null && !Files.isDirectory(current.resolve("modules").resolve("bots")) do current =
          current.getParent
        current
      }

  private def tempRepoWithRichard: Resource[IO, Path] =
    Resource
      .make(IO.blocking(Files.createTempDirectory("sbots-replies-editor-perf"))) { root =>
        IO.blocking {
          def deleteRec(path: Path): Unit = {
            if Files.isDirectory(path) then {
              val stream = Files.list(path)
              try stream.forEach(deleteRec)
              finally stream.close()
            }
            Files.deleteIfExists(path)
            ()
          }

          deleteRec(root)
        }
      }
      .flatMap { root =>
        val botsRoot = repoRoot.resolve("modules").resolve("bots")
        val botDir   = botsRoot.resolve(botName)
        val replies  = botDir.resolve("src").resolve("main").resolve("resources").resolve(s"${botId}_replies.json")
        val list     = botDir.resolve(s"${botId}_list.json")

        Resource.eval(IO.blocking {
          assert(Files.isRegularFile(replies), s"Missing Richard replies fixture: $replies")
          assert(Files.isRegularFile(list), s"Missing Richard list fixture: $list")
          val targetBotDir = root.resolve("modules").resolve("bots").resolve(botName)
          val targetDir    = targetBotDir.resolve("src").resolve("main").resolve("resources")
          Files.createDirectories(targetDir)
          Files.copy(replies, targetDir.resolve(s"${botId}_replies.json"), StandardCopyOption.REPLACE_EXISTING)
          Files.copy(list, targetBotDir.resolve(s"${botId}_list.json"), StandardCopyOption.REPLACE_EXISTING)
          root
        })
      }

  private def filterRequest(store: BotStore, message: String): IO[Either[ApiError, RepliesChunk]] =
    store.getFilteredRepliesChunk(botId, message = message, offset = 0, limit = 60)

  test("repeated filtered chunk reads over Richard replies do not re-decode the whole collection") {
    tempRepoWithRichard.use { root =>
      for {
        store <- BotStore.build(root)
        _     <- (1 to warmupRequests).toVector.traverse_(_ => filterRequest(store, "grazie"))
        start <- IO.monotonic
        _     <- (1 to measuredRequests).toVector.traverse_(_ => filterRequest(store, "grazie"))
        end   <- IO.monotonic
        elapsed = end - start
        average = elapsed / measuredRequests
      } yield assert(
        average <= maxAverageRequest,
        s"Average filtered request ${average.toMillis}ms exceeded ${maxAverageRequest.toMillis}ms"
      )
    }
  }
}
