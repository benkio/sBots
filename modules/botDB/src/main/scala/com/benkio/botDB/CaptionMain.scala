package com.benkio.botDB

import cats.effect.*
import cats.effect.kernel.Async
import cats.implicits.*
import cats.syntax.all.*
import com.benkio.botDB.config.Config
import com.benkio.telegrambotinfrastructure.resources.db.DBLayer
import com.benkio.telegrambotinfrastructure.resources.db.DBShowData
import io.circe.*
import io.circe.parser.*
import log.effect.fs2.SyncLogWriter.consoleLogUpToLevel
import log.effect.LogLevels
import log.effect.LogWriter

import java.io.File
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import scala.io.Source
import scala.jdk.CollectionConverters.*
import scala.sys.process.*
import scala.util.Try

object CaptionMain extends IOApp {

  given log: LogWriter[IO] = consoleLogUpToLevel(LogLevels.Info)

  def run(args: List[String]): IO[ExitCode] = {
    val program = for {
      _      <- Resource.eval(checkDependencies)
      config <- Resource.eval(Config.loadConfig(args.headOption))
      transactor = Config.buildTransactor(config = config)
      dbLayer           <- Resource.eval(DBLayer[IO](transactor))
      storedDBShowDatas <- Resource.eval(getStoredDbShowDatas(config))
      target = storedDBShowDatas.head
      _      = println(s"[CaptionMain] target: $target")
      captionTempDir <- createTempDir
      _        = println(s"[CaptionMain] tempDir: ${captionTempDir}")
      language = "it"
      caption <- Resource.eval(downloadCaption(target.show_id, captionTempDir, language))
      _                    = println(s"$caption")
      newTarget            = target.copy(show_origin_automatic_caption = caption)
      newStoredDBShowDatas = storedDBShowDatas.filterNot(_.show_id == newTarget.show_id) :+ newTarget
      _ <- Resource.eval(dbLayer.dbShow.insertShow(newTarget))
    } yield ()

    program.use_.as(ExitCode.Success)
  }

  def downloadCaption(showId: String, outputPath: Path, language: String): IO[Option[String]] =
    val command =
      s"""yt-dlp --write-auto-subs --sub-lang $language --skip-download --sub-format json3 -o "%(id)s" -P $outputPath https://www.youtube.com/watch?v=${showId}"""
    val captionDownloadLogic = for {
      _                  <- IO(command.!)
      captionFileContent <- IO.fromTry(
        Try(Files.readAllLines(outputPath.resolve(s"${showId}.$language.json3")).asScala.mkString("\n"))
      )
      captionJson <- IO.fromEither(parse(captionFileContent))
      caption = captionJson.findAllByKey("utf8").map(_.as[String]).collect { case Right(value) => value }.mkString
    } yield caption
    captionDownloadLogic
      .map(Some(_))
      .handleError(_ =>
        println(s"[CaptionMain] ❌ $showId - $language Downloading Caption")
        None
      )

  // Copy pasted from the show updater
  def getStoredDbShowDatas(config: Config): IO[List[DBShowData]] = {
    val showFilesResource: Resource[IO, List[File]] =
      config.showConfig.showSources
        .traverse(showSource =>
          Resource.make(Async[IO].delay(File(showSource.outputFilePath)))(f =>
            LogWriter.info(s"[ShowUpdater] Closing file $f")
          )
        )
    val deleteFiles: IO[List[DBShowData]] =
      showFilesResource.use(showFiles =>
        for {
          _ <- LogWriter.info(s"[ShowUpdater] ✓ Dry Run. Delete show files: $showFiles")
          _ <- showFiles.traverse(showFile => Async[IO].delay(showFile.delete))
        } yield List.empty
      )
    val getStoredDbShowDatas: IO[List[DBShowData]] =
      showFilesResource.use(showFiles =>
        LogWriter.info("[ShowUpdater] ❌ Dry Run. read show files: $showFiles") >>
          showFiles.flatTraverse(showFile =>
            for {
              _                   <- LogWriter.info(s"[ShowUpdater] Parse show file content: $showFile")
              showFileContentJson <- Async[IO].fromEither(parse(Source.fromFile(showFile).mkString))
              dbShowDatas         <- Async[IO].fromEither(showFileContentJson.as[List[DBShowData]])
            } yield dbShowDatas
          )
      )
    if config.showConfig.dryRun
    then deleteFiles
    else getStoredDbShowDatas
  }

  val shellDependencies: List[String] = List("yt-dlp")
  def checkDependencies: IO[Unit]     = {
    shellDependencies
      .traverse_(program =>
        Async[IO]
          .delay(s"which $program".!)
          .flatMap(result =>
            Async[IO].raiseUnless(result == 0)(
              Throwable(s"[ShowFetcher] error checking dependencies: $program is missing")
            )
          )
      )
  }

  def createTempDir: Resource[IO, Path] =
    Resource.pure(Files.createTempDirectory(Paths.get("target"), "ytdlpCaptions").toAbsolutePath())
}
