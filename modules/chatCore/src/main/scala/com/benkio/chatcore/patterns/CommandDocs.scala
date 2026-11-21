package com.benkio.chatcore.patterns

/** Shared links to the command documentation shipped in the repository. */
private[patterns] object CommandDocs {

  val commandsDocsBaseUrl: String = "https://github.com/benkio/sBots/blob/main/"

  def commandDocLink(path: String, lang: String): String =
    s"$commandsDocsBaseUrl$path#$lang"

  def chatCoreCommandDocLink(command: String, lang: String): String =
    commandDocLink(s"modules/chatCore/CommandsDocumentation/$command.md", lang)
}
