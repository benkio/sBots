package com.benkio.chatcore.model.show

import org.http4s.dsl.impl.QueryParamDecoderMatcher
import org.http4s.Query
import org.http4s.QueryParamDecoder
import org.http4s.QueryParamDecoder.*

import java.time.format.DateTimeFormatter
import java.time.LocalDate
import scala.concurrent.duration.FiniteDuration

sealed trait ShowQuery
final case class ShowQueryKeyword(
    titleKeywords: Option[List[String]],
    descriptionKeywords: Option[List[String]] = None,
    captionKeywords: Option[List[String]] = None,
    minDuration: Option[Int] = None,
    maxDuration: Option[Int] = None,
    minDate: Option[LocalDate] = None,
    maxDate: Option[LocalDate] = None
) extends ShowQuery
final case class SimpleShowQuery(
    titleKeyword: String,
    descriptionKeyword: String,
    captionKeyword: String
) extends ShowQuery
case object RandomQuery extends ShowQuery

object ShowQuery {

  object CaptionKeywordsQueryParamMatcher     extends QueryParamDecoderMatcher[String]("caption")
  object DescriptionKeywordsQueryParamMatcher extends QueryParamDecoderMatcher[String]("description")
  object MaxDateKeywordsQueryParamMatcher     extends QueryParamDecoderMatcher[LocalDate]("maxdate")
  object MaxDurationKeywordsQueryParamMatcher extends QueryParamDecoderMatcher[Int]("maxduration")
  object MinDateKeywordsQueryParamMatcher     extends QueryParamDecoderMatcher[LocalDate]("mindate")
  object MinDurationKeywordsQueryParamMatcher extends QueryParamDecoderMatcher[Int]("minduration")
  object TitleKeywordsQueryParamMatcher       extends QueryParamDecoderMatcher[String]("title")

  given deflocalDateQueryParamDecoder: QueryParamDecoder[LocalDate] = localDateQueryParamDecoder(
    DateTimeFormatter.ofPattern("yyyyMMdd")
  )

  def apply(queryString: String): ShowQuery = {
    def allParamsNone(query: Query) =
      List(
        TitleKeywordsQueryParamMatcher.unapplySeq(query.multiParams).map(_.toList),
        DescriptionKeywordsQueryParamMatcher.unapplySeq(query.multiParams).map(_.toList),
        CaptionKeywordsQueryParamMatcher.unapplySeq(query.multiParams).map(_.toList),
        MinDurationKeywordsQueryParamMatcher.unapply(query.multiParams),
        MaxDurationKeywordsQueryParamMatcher.unapply(query.multiParams),
        MinDateKeywordsQueryParamMatcher.unapply(query.multiParams),
        MaxDateKeywordsQueryParamMatcher.unapply(query.multiParams)
      ).forall(_.isEmpty)

    (queryString, Query.unsafeFromString(queryString)) match {
      case (x, _) if x.isEmpty                => RandomQuery
      case (x, query) if allParamsNone(query) =>
        SimpleShowQuery(
          titleKeyword = x,
          descriptionKeyword = x,
          captionKeyword = x
        )
      case (_, query) =>
        ShowQueryKeyword(
          titleKeywords = TitleKeywordsQueryParamMatcher.unapplySeq(query.multiParams).map(_.toList),
          descriptionKeywords = DescriptionKeywordsQueryParamMatcher.unapplySeq(query.multiParams).map(_.toList),
          captionKeywords = CaptionKeywordsQueryParamMatcher.unapplySeq(query.multiParams).map(_.toList),
          minDuration = MinDurationKeywordsQueryParamMatcher.unapply(query.multiParams),
          maxDuration = MaxDurationKeywordsQueryParamMatcher.unapply(query.multiParams),
          minDate = MinDateKeywordsQueryParamMatcher.unapply(query.multiParams),
          maxDate = MaxDateKeywordsQueryParamMatcher.unapply(query.multiParams)
        )
    }
  }

  def searchTimestamp(captionMap: Map[FiniteDuration, String], query: ShowQuery): Option[FiniteDuration] = {
    def searchWithFallback(term: String): Option[FiniteDuration] = {
      val normalizedTerm = term.trim.toLowerCase
      if normalizedTerm.isEmpty then None
      else {
        val maybeTimestamp = captionMap.toList
          .sortBy(_._1)
          .collectFirst {
            case (timestamp, caption) if caption.toLowerCase.contains(normalizedTerm) =>
              timestamp
          }

        maybeTimestamp.orElse {
          val words = normalizedTerm.split("\\s+").toList
          if words.length <= 1 then None
          else searchWithFallback(words.dropRight(1).mkString(" "))
        }
      }
    }

    query match {
      case ShowQueryKeyword(_, _, Some(captionKeywords), _, _, _, _) =>
        captionKeywords.iterator
          .map(searchWithFallback)
          .collectFirst { case Some(timestamp) => timestamp }
      case SimpleShowQuery(_, _, captionKeyword) =>
        searchWithFallback(captionKeyword)
      case _ =>
        None
    }
  }
}
