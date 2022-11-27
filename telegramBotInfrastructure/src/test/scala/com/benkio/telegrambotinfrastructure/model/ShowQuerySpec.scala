package com.benkio.telegrambotinfrastructure.model

import munit._

import java.time.LocalDate

class ShowQuerySpec extends FunSuite {

  test("ShowQuery.apply should return RandomQuery if the input is empty") {
    assertEquals(ShowQuery(""), RandomQuery)
  }
  test("ShowQuery.apply should return ShowQueryKeyword with one element as title keywords if the input is not a valid query string") {
    assertEquals(ShowQuery("testQuery"), ShowQueryKeyword(titleKeywords = List("testQuery")))
    assertEquals(ShowQuery("?key=value&minDate=abc"), ShowQueryKeyword(titleKeywords = List("?key=value&minDate=abc")))
    assertEquals(ShowQuery("key=value&minDate=abc"), ShowQueryKeyword(titleKeywords = List("key=value&minDate=abc")))
  }
  test("ShowQuery.apply should return expected ShowQueryKeyword if the input is a valid query string") {
    assertEquals(ShowQuery("title=richard"), ShowQueryKeyword(titleKeywords = List("richard")))
    assertEquals(ShowQuery("title=richard&title=benson"), ShowQueryKeyword(titleKeywords = List("richard", "benson")))
    assertEquals(ShowQuery("title=richard&someotherKey=value"), ShowQueryKeyword(titleKeywords = List("richard")))
    assertEquals(ShowQuery("title=richard&description=carpenelli"), ShowQueryKeyword(titleKeywords = List("richard"), descriptionKeywords = Some(List("carpenelli"))))
    assertEquals(
      ShowQuery("title=richard&description=carpenelli&minduration=20"),
      ShowQueryKeyword(titleKeywords = List("richard"), descriptionKeywords = Some(List("carpenelli")), minDuration = Some(20)))
    assertEquals(
      ShowQuery("title=richard&description=carpenelli&minduration=invalidValue"),
      ShowQueryKeyword(titleKeywords = List("richard"), descriptionKeywords = Some(List("carpenelli"))))
    assertEquals(
      ShowQuery("title=richard&description=carpenelli&maxduration=invalidValue"),
      ShowQueryKeyword(titleKeywords = List("richard"), descriptionKeywords = Some(List("carpenelli"))))
    assertEquals(
      ShowQuery("title=richard&description=carpenelli&minduration=20&maxduration=40"),
      ShowQueryKeyword(titleKeywords = List("richard"), descriptionKeywords = Some(List("carpenelli")), minDuration = Some(20), maxDuration = Some(40)))
    assertEquals(
      ShowQuery("title=richard&description=carpenelli&minduration=20&maxduration=40&mindate=20220120"),
      ShowQueryKeyword(titleKeywords = List("richard"), descriptionKeywords = Some(List("carpenelli")), minDuration = Some(20), maxDuration = Some(40), minDate = Some(LocalDate.of(2022, 1, 20))))
    assertEquals(
      ShowQuery("title=richard&description=carpenelli&minduration=20&maxduration=40&mindate=20220120&maxdate=20220122"),
      ShowQueryKeyword(titleKeywords = List("richard"), descriptionKeywords = Some(List("carpenelli")), minDuration = Some(20), maxDuration = Some(40), minDate = Some(LocalDate.of(2022, 1, 20)), maxDate = Some(LocalDate.of(2022, 1, 22))))
    assertEquals(
      ShowQuery("title=richard&description=carpenelli&minduration=20&maxduration=40&mindate=invalid&maxdate=20220122"),
      ShowQueryKeyword(titleKeywords = List("richard"), descriptionKeywords = Some(List("carpenelli")), minDuration = Some(20), maxDuration = Some(40), maxDate = Some(LocalDate.of(2022, 1, 22))))
    assertEquals(
      ShowQuery("title=richard&description=carpenelli&minduration=20&maxduration=40&mindate=20220142&maxdate=20220122"),
      ShowQueryKeyword(titleKeywords = List("richard"), descriptionKeywords = Some(List("carpenelli")), minDuration = Some(20), maxDuration = Some(40), maxDate = Some(LocalDate.of(2022, 1, 22))))
        assertEquals(
      ShowQuery("title=richard&description=carpenelli&minduration=20&maxduration=40&mindate=20220120&maxdate=invalidDate"),
          ShowQueryKeyword(titleKeywords = List("richard"), descriptionKeywords = Some(List("carpenelli")), minDuration = Some(20), maxDuration = Some(40), minDate = Some(LocalDate.of(2022, 1, 20))))
        assertEquals(
      ShowQuery("title=richard&description=carpenelli&minduration=20&maxduration=40&mindate=20220120&maxdate=20220144"),
      ShowQueryKeyword(titleKeywords = List("richard"), descriptionKeywords = Some(List("carpenelli")), minDuration = Some(20), maxDuration = Some(40), minDate = Some(LocalDate.of(2022, 1, 20)), maxDate = None))
  }
}
