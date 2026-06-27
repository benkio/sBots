package com.benkio.chatcore.model.show

import munit.*
import org.scalacheck.Prop.forAll

import java.time.LocalDate
import scala.concurrent.duration.*

class ShowQuerySpec extends FunSuite with ScalaCheckSuite {

  test("ShowQuery.apply should return RandomQuery if the input is empty") {
    assertEquals(ShowQuery(""), RandomQuery)
  }
  test(
    "ShowQuery.apply should return ShowQueryKeyword with one element as title keywords if the input is not a valid query string"
  ) {
    assertEquals(
      ShowQuery("testQuery"),
      SimpleShowQuery(titleKeyword = "testQuery", descriptionKeyword = "testQuery", captionKeyword = "testQuery")
    )
    assertEquals(
      ShowQuery("?key=value&minDate=abc"),
      SimpleShowQuery(
        titleKeyword = "?key=value&minDate=abc",
        descriptionKeyword = "?key=value&minDate=abc",
        captionKeyword = "?key=value&minDate=abc"
      )
    )
  }
  test("ShowQuery.apply should return expected ShowQueryKeyword if the input is a valid query string") {
    assertEquals(ShowQuery("title=richard"), ShowQueryKeyword(titleKeywords = Some(List("richard"))))
    assertEquals(
      ShowQuery("title=richard&title=benson"),
      ShowQueryKeyword(titleKeywords = Some(List("richard", "benson")))
    )
    assertEquals(
      ShowQuery("title=autostrada&title=pip+addosso"),
      ShowQueryKeyword(titleKeywords = Some(List("autostrada", "pip addosso")))
    )
    assertEquals(ShowQuery("title=richard&someotherKey=value"), ShowQueryKeyword(titleKeywords = Some(List("richard"))))
    assertEquals(
      ShowQuery("title=richard&description=carpenelli"),
      ShowQueryKeyword(titleKeywords = Some(List("richard")), descriptionKeywords = Some(List("carpenelli")))
    )
    assertEquals(
      ShowQuery("description=carpenelli"),
      ShowQueryKeyword(titleKeywords = None, descriptionKeywords = Some(List("carpenelli")))
    )
    assertEquals(
      ShowQuery("description=carpenelli&description=angelo"),
      ShowQueryKeyword(titleKeywords = None, descriptionKeywords = Some(List("carpenelli", "angelo")))
    )
    assertEquals(
      ShowQuery("description=luca+di+noia"),
      ShowQueryKeyword(titleKeywords = None, descriptionKeywords = Some(List("luca di noia")))
    )
    assertEquals(
      ShowQuery("caption=carpenelli"),
      ShowQueryKeyword(titleKeywords = None, captionKeywords = Some(List("carpenelli")))
    )
    assertEquals(
      ShowQuery("caption=carpenelli&caption=angelo"),
      ShowQueryKeyword(titleKeywords = None, captionKeywords = Some(List("carpenelli", "angelo")))
    )
    assertEquals(
      ShowQuery("caption=luca+di+noia"),
      ShowQueryKeyword(titleKeywords = None, captionKeywords = Some(List("luca di noia")))
    )
    assertEquals(
      ShowQuery("title=richard&description=carpenelli&caption=i+nodi&minduration=20"),
      ShowQueryKeyword(
        titleKeywords = Some(List("richard")),
        descriptionKeywords = Some(List("carpenelli")),
        captionKeywords = Some(List("i nodi")),
        minDuration = Some(20)
      )
    )
    assertEquals(
      ShowQuery("title=richard&description=carpenelli&minduration=invalidValue"),
      ShowQueryKeyword(titleKeywords = Some(List("richard")), descriptionKeywords = Some(List("carpenelli")))
    )
    assertEquals(
      ShowQuery("title=richard&description=carpenelli&maxduration=invalidValue"),
      ShowQueryKeyword(titleKeywords = Some(List("richard")), descriptionKeywords = Some(List("carpenelli")))
    )
    assertEquals(
      ShowQuery("title=richard&description=carpenelli&minduration=20&maxduration=40"),
      ShowQueryKeyword(
        titleKeywords = Some(List("richard")),
        descriptionKeywords = Some(List("carpenelli")),
        minDuration = Some(20),
        maxDuration = Some(40)
      )
    )
    assertEquals(
      ShowQuery("title=richard&description=carpenelli&minduration=20&maxduration=40&mindate=20220120"),
      ShowQueryKeyword(
        titleKeywords = Some(List("richard")),
        descriptionKeywords = Some(List("carpenelli")),
        minDuration = Some(20),
        maxDuration = Some(40),
        minDate = Some(LocalDate.of(2022, 1, 20))
      )
    )
    assertEquals(
      ShowQuery(
        "title=richard&description=carpenelli&caption=nodi&caption=sciolti&minduration=20&maxduration=40&mindate=20220120&maxdate=20220122"
      ),
      ShowQueryKeyword(
        titleKeywords = Some(List("richard")),
        descriptionKeywords = Some(List("carpenelli")),
        captionKeywords = Some(List("nodi", "sciolti")),
        minDuration = Some(20),
        maxDuration = Some(40),
        minDate = Some(LocalDate.of(2022, 1, 20)),
        maxDate = Some(LocalDate.of(2022, 1, 22))
      )
    )
    assertEquals(
      ShowQuery("title=richard&description=carpenelli&minduration=20&maxduration=40&mindate=invalid&maxdate=20220122"),
      ShowQueryKeyword(
        titleKeywords = Some(List("richard")),
        descriptionKeywords = Some(List("carpenelli")),
        minDuration = Some(20),
        maxDuration = Some(40),
        maxDate = Some(LocalDate.of(2022, 1, 22))
      )
    )
    assertEquals(
      ShowQuery("title=richard&description=carpenelli&minduration=20&maxduration=40&mindate=20220142&maxdate=20220122"),
      ShowQueryKeyword(
        titleKeywords = Some(List("richard")),
        descriptionKeywords = Some(List("carpenelli")),
        minDuration = Some(20),
        maxDuration = Some(40),
        maxDate = Some(LocalDate.of(2022, 1, 22))
      )
    )
    assertEquals(
      ShowQuery(
        "title=richard&description=carpenelli&minduration=20&maxduration=40&mindate=20220120&maxdate=invalidDate"
      ),
      ShowQueryKeyword(
        titleKeywords = Some(List("richard")),
        descriptionKeywords = Some(List("carpenelli")),
        minDuration = Some(20),
        maxDuration = Some(40),
        minDate = Some(LocalDate.of(2022, 1, 20))
      )
    )
    assertEquals(
      ShowQuery("title=richard&description=carpenelli&minduration=20&maxduration=40&mindate=20220120&maxdate=20220144"),
      ShowQueryKeyword(
        titleKeywords = Some(List("richard")),
        descriptionKeywords = Some(List("carpenelli")),
        minDuration = Some(20),
        maxDuration = Some(40),
        minDate = Some(LocalDate.of(2022, 1, 20)),
        maxDate = None
      )
    )
  }

  test("searchTimestamp should return none when caption keywords are not specified") {
    forAll { (caption: String, title: String, description: String) =>
      val captionMap = Map(1.seconds -> caption)
      val query      = ShowQueryKeyword(
        titleKeywords = Some(List(title)),
        descriptionKeywords = Some(List(description)),
        captionKeywords = None
      )
      assertEquals(ShowQuery.searchTimestamp(captionMap, query), None)
    }
  }

  test("searchTimestamp should return phrase timestamp for captionKeywords") {
    forAll { (keywordSeed: Long, timestampSeed: Long) =>
      val keyword    = s"needle${math.abs(keywordSeed % 100000L)}"
      val timestamp  = (math.abs(timestampSeed % 10000L) + 1L).seconds
      val captionMap = Map(
        1.seconds -> "some unrelated phrase",
        timestamp -> s"this contains $keyword in the middle"
      )
      val query = ShowQueryKeyword(
        titleKeywords = None,
        captionKeywords = Some(List(keyword))
      )

      assertEquals(ShowQuery.searchTimestamp(captionMap, query), Some(timestamp))
    }
  }

  test("searchTimestamp should remove last word recursively until matching") {
    forAll { (keywordSeed: Long, suffixSeed: Long, timestampSeed: Long) =>
      val baseWord1  = s"alpha${math.abs(keywordSeed % 100000L)}"
      val baseWord2  = s"beta${math.abs((keywordSeed + 1L) % 100000L)}"
      val suffix     = s"suffix${math.abs(suffixSeed % 100000L)}"
      val timestamp  = (math.abs(timestampSeed % 10000L) + 1L).seconds
      val baseTerm   = s"$baseWord1 $baseWord2"
      val captionMap = Map(
        timestamp -> s"caption text with $baseTerm included"
      )
      val query = ShowQueryKeyword(
        titleKeywords = None,
        captionKeywords = Some(List(s"$baseTerm $suffix"))
      )

      assertEquals(ShowQuery.searchTimestamp(captionMap, query), Some(timestamp))
    }
  }

  test("searchTimestamp should return none when fallback exhausts all terms") {
    forAll { (keywordSeed: Long) =>
      val missingKeyword = s"missing${math.abs(keywordSeed % 100000L)}"
      val captionMap     = Map(
        5.seconds  -> "foo bar baz",
        10.seconds -> "another caption row"
      )
      val query = ShowQueryKeyword(
        titleKeywords = None,
        captionKeywords = Some(List(s"$missingKeyword tail"))
      )

      assertEquals(ShowQuery.searchTimestamp(captionMap, query), None)
    }
  }
}
