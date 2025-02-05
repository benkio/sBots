package com.benkio.telegrambotinfrastructure.model.show

import munit.*
import java.time.LocalDate

class ShowQuerySpec extends FunSuite {

  test("ShowQuery.apply should return RandomQuery if the input is empty") {
    assertEquals(ShowQuery(""), RandomQuery)
  }
  test(
    "ShowQuery.apply should return ShowQueryKeyword with one element as title keywords if the input is not a valid query string"
  ) {
    assertEquals(ShowQuery("testQuery"), ShowQueryKeyword(titleKeywords = Some(List("testQuery"))))
    assertEquals(
      ShowQuery("?key=value&minDate=abc"),
      ShowQueryKeyword(titleKeywords = Some(List("?key=value&minDate=abc")))
    )
    assertEquals(
      ShowQuery("key=value&minDate=abc"),
      ShowQueryKeyword(titleKeywords = Some(List("key=value&minDate=abc")))
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
      ShowQuery("title=richard&description=carpenelli&minduration=20"),
      ShowQueryKeyword(
        titleKeywords = Some(List("richard")),
        descriptionKeywords = Some(List("carpenelli")),
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
      ShowQuery("title=richard&description=carpenelli&minduration=20&maxduration=40&mindate=20220120&maxdate=20220122"),
      ShowQueryKeyword(
        titleKeywords = Some(List("richard")),
        descriptionKeywords = Some(List("carpenelli")),
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
}
