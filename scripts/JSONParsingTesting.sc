/*

Script to test the parsing of yt-dlp output json starting from a json file dump.

*/

// botDB/console
import com.benkio.botDB.show
import com.benkio.botDB.show.ShowFetcher

import cats.effect.implicits.*
import cats.effect.kernel.Async
import cats.effect.Resource
import cats.implicits.*
import com.benkio.telegrambotinfrastructure.resources.db.DBShowData
import com.benkio.telegrambotinfrastructure.resources.ResourceAccess
import io.circe.parser.*
import io.circe.syntax.*
import log.effect.LogWriter

import java.io.File
import java.nio.file.Files
import java.nio.file.Path
import java.util.UUID
import scala.sys.process.*
import scala.sys.process.Process
import cats.effect.IO
import log.effect.fs2.SyncLogWriter.consoleLogUpToLevel
import log.effect.LogLevels
import log.effect.LogWriter
import cats.effect.unsafe.implicits.global

val file = File("modules/botDB/ERR--1426863025-f2afb760-3d13-45d0-9e1c-ca52f186475110047902999000702394.json")

given log: LogWriter[IO] = consoleLogUpToLevel(LogLevels.Info)

ShowFetcher.filterAndParseJson[IO](file, "testBot").unsafeRunSync()
