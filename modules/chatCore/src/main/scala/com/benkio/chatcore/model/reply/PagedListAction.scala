package com.benkio.chatcore.model.reply

import com.benkio.chatcore.model.CommandKey
import com.benkio.chatcore.model.CommandSyntax

/** What a user does on a [[PagedList]], whatever the platform: a button, a typed command. The id is the platform
  * neutral form of the action: it fits the button id of Discord, the smallest limit (see
  * `PagedListAction.maxIdLength`).
  */
enum PagedListAction {
  case PreviousPage(currentPage: Int, commandKey: CommandKey)
  case NextPage(currentPage: Int, commandKey: CommandKey)
  case Media(mediaFileName: String)
  case Show(showId: String, timestamp: Option[String])

  /** The id of the action, `None` when it is longer than [[PagedListAction.maxIdLength]]. */
  def toId: Option[String] = Some(rawId).filter(_.length <= PagedListAction.maxIdLength)

  private def rawId: String = this match {
    case PreviousPage(currentPage, commandKey) => s"previousPage-${commandKey.asString}-$currentPage"
    case NextPage(currentPage, commandKey)     => s"nextPage-${commandKey.asString}-$currentPage"
    case Media(value)                          => s"media-$value"
    case Show(value, timestamp)                => s"""show-$value|${timestamp.getOrElse("")}"""
  }
}

object PagedListAction {

  /** The limit of a Discord button id. */
  val maxIdLength: Int = 100

  /** The action of an id, `None` when the id is too long or is not one of an action. */
  def fromId(id: String, commandSyntax: CommandSyntax = CommandSyntax.Slash): Option[PagedListAction] =
    Option.when(id.length <= maxIdLength)(id).flatMap(parse(_, commandSyntax))

  private def parsePagination(
      split: Array[String],
      commandSyntax: CommandSyntax,
      build: (Int, CommandKey) => PagedListAction
  ): Option[PagedListAction] =
    for {
      commandKey  <- split.lift(1).flatMap(CommandKey.fromString(_, commandSyntax))
      currentPage <- split.lift(2).flatMap(_.toIntOption)
    } yield build(currentPage, commandKey)

  private def parseShow(raw: String): Show = {
    val payload = raw.stripPrefix("show-")
    val divider = payload.lastIndexOf('|')
    if divider < 0 then Show(payload, None)
    else {
      val timestamp = payload.drop(divider + 1)
      Show(payload.take(divider), Option.when(timestamp.nonEmpty)(timestamp))
    }
  }

  private def parse(id: String, commandSyntax: CommandSyntax): Option[PagedListAction] = {
    val split = id.split("-")
    split.lift(0).flatMap {
      case "previousPage" => parsePagination(split = split, commandSyntax = commandSyntax, build = PreviousPage.apply)
      case "nextPage"     => parsePagination(split = split, commandSyntax = commandSyntax, build = NextPage.apply)
      case "media"        => Some(Media(split.drop(1).mkString("-")))
      case "show"         => Some(parseShow(id))
      case _              => None
    }
  }
}
