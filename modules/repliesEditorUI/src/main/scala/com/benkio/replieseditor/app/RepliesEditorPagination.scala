package com.benkio.replieseditor.app

import com.raquo.airstream.core.Signal

final class RepliesEditorPagination(store: RepliesEditorStore, loader: RepliesEditorLoader) {

  val canPrev: Signal[Boolean] =
    store.currentPage.map(_ > 1)

  private val totalPages: Signal[Int] =
    store.totalOpt.combineWith(store.pageSize).map { case (tOpt, ps) =>
      RepliesEditorLogic.totalPages(tOpt, ps)
    }

  val canNext: Signal[Boolean] =
    store.currentPage.combineWith(totalPages).map { case (p, tp) => tp > 0 && p < tp }

  val pageLabel: Signal[String] =
    store.currentPage.combineWith(totalPages).map { case (p, tp) =>
      if tp == 0 then "" else s"Page ${p.max(1)} / $tp"
    }

  def requestPrev(): Unit = {
    val p = store.currentPageVar.now().max(1)
    if p > 1 then {
      val next = p - 1
      store.currentPageVar.set(next)
      store.selectedBotVar.now().foreach(loader.loadPage(_, next))
    }
  }

  def requestNext(): Unit = {
    val p  = store.currentPageVar.now().max(1)
    val tp =
      store.totalVar.now().fold(0) { total =>
        val ps = store.pageSizeVar.now().max(1)
        ((total + ps - 1) / ps).max(1)
      }
    if tp == 0 || p < tp then {
      val next = p + 1
      store.currentPageVar.set(next)
      store.selectedBotVar.now().foreach(loader.loadPage(_, next))
    }
  }

  def setPageSize(ps: Int): Unit = {
    store.pageSizeVar.set(ps.max(1))
    store.currentPageVar.set(1)
    store.selectedBotVar.now().foreach(loader.loadPage(_, 1))
  }
}
