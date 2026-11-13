package com.benkio.chatcore.repository

import cats.data.NonEmptyList
import cats.effect.Async
import cats.syntax.all.*
import com.benkio.chatcore.model.media.MediaResource
import com.benkio.chatcore.model.reply.MarkdownFile
import log.effect.LogWriter

/** The text of the markdown files, for the platforms that send them as messages. */
object MarkdownFiles {

  final case class EmptyMarkdownFile(markdownFile: MarkdownFile)
      extends Throwable(s"[MarkdownFiles] The markdown file ${markdownFile.filepath} is empty")

  /** The content of the file, read from its resources in order, the next one if the previous fails: only the resources
    * that are a file can be read, a reference to an uploaded one fails. An empty file is an error.
    */
  def content[F[_]: Async: LogWriter](repository: Repository[F], markdownFile: MarkdownFile): F[String] =
    repository
      .getResourceFile(markdownFile)
      .evalMap(Async[F].fromEither)
      .use(resources => firstThatWorks(resources, markdownFile))
      .flatMap(text =>
        Async[F].raiseWhen(text.trim.isEmpty)(EmptyMarkdownFile(markdownFile)).as(text)
      )

  private def firstThatWorks[F[_]: Async: LogWriter](
      resources: NonEmptyList[MediaResource[F]],
      markdownFile: MarkdownFile
  ): F[String] = {
    def read(resource: MediaResource[F]): F[String] = resource match {
      case MediaResource.MediaResourceFile(file) => file.use(path => Repository.fileToString[F](path).use(Async[F].pure))
      case MediaResource.MediaResourceIFile(ref) =>
        Async[F].raiseError(new UnsupportedOperationException(s"A markdown file cannot be read from the reference $ref"))
    }
    resources.reduceLeftTo(read)((previous, next) =>
      previous.handleErrorWith(e =>
        LogWriter.error(s"[MarkdownFiles] Error reading a resource of $markdownFile: $e. Trying the next one") >>
          read(next)
      )
    )
  }
}
