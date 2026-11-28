package com.benkio.replieseditor.app

object RepliesEditorLogic {

  def totalPages(totalOpt: Option[Int], pageSize: Int): Int =
    totalOpt.fold(0) { total =>
      val ps = pageSize.max(1)
      ((total + ps - 1) / ps).max(1)
    }

  def pageLabel(currentPage: Int, totalOpt: Option[Int], pageSize: Int): String = {
    val tp = totalPages(totalOpt, pageSize)
    if tp == 0 then "" else s"Page ${currentPage.max(1)} / $tp"
  }

}
