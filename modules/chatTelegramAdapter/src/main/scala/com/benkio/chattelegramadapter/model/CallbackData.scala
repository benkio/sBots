package com.benkio.chattelegramadapter.model

enum CallbackData {
  case PreviousPage(currentPage: Int) extends CallbackData
  case NextPage(currentPage: Int)     extends CallbackData
  case Media(mediaFileName: String)   extends CallbackData
}

extension (callbackData: CallbackData) {
  def toCallbackKey: String =
    callbackData match {
      case CallbackData.PreviousPage(currentPage) => s"previousPage-$currentPage"
      case CallbackData.NextPage(currentPage)     => s"nextPage-$currentPage"
      case CallbackData.Media(value)              => value
    }
}

object CallbackData {
  def apply(callbackData: String): CallbackData = callbackData match {
    case v if v.startsWith("previousPage-") => PreviousPage(v.stripPrefix("previousPage-").toInt)
    case v if v.startsWith("nextPage-")     => NextPage(v.stripPrefix("nextPage-").toInt)
    case v                                  => Media(v)
  }
}
