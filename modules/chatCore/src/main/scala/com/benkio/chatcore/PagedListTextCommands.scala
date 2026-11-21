package com.benkio.chatcore

import cats.syntax.option.*
import com.benkio.chatcore.model.CommandSyntax
import com.benkio.chatcore.model.Message
import com.benkio.chatcore.model.SBotInfo.SBotName

import scala.concurrent.duration.FiniteDuration

/** The text commands of a platform without buttons: `!next` and `!prev` act on the paged list the engine remembers per
  * chat (see [[PagedListTextStore]]), `!pick n` is the pick of the item n of the page, through the navigation of the
  * platform-neutral [[PagedListNavigation]]: `!next`/`!prev` run the command of the list again at the next/previous
  * page (the navigation rule PagedListChangesPage), `!pick n` is the navigation rule PagedListPicksItem.
  *
  * The answers when the chat has no remembered list, the list expired, or the number of the pick is out of the page
  * or missing.
  */
object PagedListTextCommands {

  /** The answer of `!next`/`!prev` when the chat has no remembered list or it expired. */
  val noListAnswer: String = "Nessuna lista paginata in questa chat"

  /** The answer of `!next`/`!prev` when the remembered list expired. */
  val expiredAnswer: String = "La lista paginata precedente è scaduta"

  /** The answer of `!pick` when the number is out of the page of the remembered list. */
  def pickOutOfPageAnswer(number: Int): String = s"Nessun elemento con numero $number nella pagina corrente"

  /** The answer of `!pick` when no number is written. */
  val pickMissingNumberAnswer: String = "Scrivi il numero dell'elemento da prendere"

  /** What the text commands of the message ask the engine to take on the remembered list. */
  enum Action {
    /** `!next`/`!prev`: the command of the list run again at the page: the reply is the new page of the list. */
    case Rerun(entry: PagedListTextStore.Entry, page: Int)
    /** The clear answer: no list remembered or expired, or the number of `!pick` out of the page or missing. */
    case Answer(text: String)
    /** `!pick n`: the replies of the item n of the page of the list. */
    case Pick(entry: PagedListTextStore.Entry, number: Int)
  }

  private enum TextCommand {
    case Next, Prev
    case Pick(number: Option[Int])
  }

  /** What the message is: the text command of the platform (`!next`, `!prev`, `!pick n`), or `None` when the text is
    * not one of them.
    */
  private def parse(msg: Message, syntax: CommandSyntax, botName: SBotName): Option[TextCommand] =
    msg.text.flatMap { text =>
      if syntax.isInvocationOf(text, "next", botName) then Some(TextCommand.Next)
      else if syntax.isInvocationOf(text, "prev", botName) then Some(TextCommand.Prev)
      else
        Option.when(syntax.isInvocationOf(text, "pick", botName))(
          TextCommand.Pick(number = text.drop(syntax.invocation("pick").length).trim.toIntOption)
        )
    }

  /** The action the text commands of the message ask for on the remembered list of the chat: the list the commands
    * can act on, or the clear answer when there is none, it expired or the number of `!pick` is out of the page.
    * `None` when the message is not a text command.
    */
  def decide(
      msg: Message,
      syntax: CommandSyntax,
      botName: SBotName,
      store: PagedListTextStore.Store,
      now: FiniteDuration
  ): Option[Action] = parse(msg, syntax, botName).flatMap { command =>
    store.get(msg.chatId, now) match {
      case Some(entry) => decideOnEntry(command, entry)
      case None        =>
        Option
          .when(store.isExpired(msg.chatId, now))(Action.Answer(expiredAnswer))
          .orElse(Some(Action.Answer(noListAnswer)))
    }
  }

  private def decideOnEntry(command: TextCommand, entry: PagedListTextStore.Entry): Option[Action] = command match {
    case TextCommand.Next => Action.Rerun(entry, entry.page + 1).some
    case TextCommand.Prev => Action.Rerun(entry, PagedListNavigation.previousPage(entry.page)).some
    case TextCommand.Pick(number) =>
      number match {
        case Some(n) if n >= 1 && n <= entry.pageItems.size => Action.Pick(entry, n).some
        case Some(n)                                        => Action.Answer(pickOutOfPageAnswer(n)).some
        case None                                           => Action.Answer(pickMissingNumberAnswer).some
      }
  }
}
