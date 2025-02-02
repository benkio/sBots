package com.benkio.telegrambotinfrastructure.model.show

import com.benkio.telegrambotinfrastructure.resources.db.DBShowData
import org.http4s.QueryParamDecoder.*
import org.http4s.dsl.impl.QueryParamDecoderMatcher
import org.http4s.Query
import org.http4s.QueryParamDecoder

import java.time.LocalDate

sealed trait ShowQuery
final case class ShowQueryKeyword(
    titleKeywords: Option[List[String]],
    descriptionKeywords: Option[List[String]] = None,
    minDuration: Option[Int] = None,
    maxDuration: Option[Int] = None,
    minDate: Option[LocalDate] = None,
    maxDate: Option[LocalDate] = None
) extends ShowQuery
case object RandomQuery extends ShowQuery

object ShowQuery {

  object TitleKeywordsQueryParamMatcher       extends QueryParamDecoderMatcher[String]("title")
  object DescriptionKeywordsQueryParamMatcher extends QueryParamDecoderMatcher[String]("description")
  object MinDurationKeywordsQueryParamMatcher extends QueryParamDecoderMatcher[Int]("minduration")
  object MaxDurationKeywordsQueryParamMatcher extends QueryParamDecoderMatcher[Int]("maxduration")
  object MaxDateKeywordsQueryParamMatcher     extends QueryParamDecoderMatcher[LocalDate]("maxdate")
  object MinDateKeywordsQueryParamMatcher     extends QueryParamDecoderMatcher[LocalDate]("mindate")

  given deflocalDateQueryParamDecoder: QueryParamDecoder[LocalDate] = localDateQueryParamDecoder(
    DBShowData.dateTimeFormatter
  )

  def apply(queryString: String): ShowQuery = {
    def allParamsNone(query: Query) =
      List(
        TitleKeywordsQueryParamMatcher.unapplySeq(query.multiParams).map(_.toList),
        DescriptionKeywordsQueryParamMatcher.unapplySeq(query.multiParams).map(_.toList),
        MinDurationKeywordsQueryParamMatcher.unapply(query.multiParams),
        MaxDurationKeywordsQueryParamMatcher.unapply(query.multiParams),
        MinDateKeywordsQueryParamMatcher.unapply(query.multiParams),
        MaxDateKeywordsQueryParamMatcher.unapply(query.multiParams)
      ).forall(_.isEmpty)

    (queryString, Query.unsafeFromString(queryString)) match {
      case (x, _) if x.isEmpty                => RandomQuery
      case (x, query) if allParamsNone(query) => ShowQueryKeyword(titleKeywords = Some(List(x)))
      case (_, query) =>
        ShowQueryKeyword(
          titleKeywords = TitleKeywordsQueryParamMatcher.unapplySeq(query.multiParams).map(_.toList),
          descriptionKeywords = DescriptionKeywordsQueryParamMatcher.unapplySeq(query.multiParams).map(_.toList),
          minDuration = MinDurationKeywordsQueryParamMatcher.unapply(query.multiParams),
          maxDuration = MaxDurationKeywordsQueryParamMatcher.unapply(query.multiParams),
          minDate = MinDateKeywordsQueryParamMatcher.unapply(query.multiParams),
          maxDate = MaxDateKeywordsQueryParamMatcher.unapply(query.multiParams)
        )
    }
  }

}
