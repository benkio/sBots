/** Single source of truth for the list of bot projects. When adding a new bot:
  *   1. Add its name here
  *   2. In build.sbt: define the project and add it to the botProjects seq Then aggregate and main.dependsOn will
  *      include it automatically.
  */
object BotProjects {
  val botProjectNames: List[String] = List(
    "CalandroBot",
    "ABarberoBot",
    "RichardPHJBensonBot",
    "XahLeeBot",
    "YouTuboAncheI0Bot",
    "M0sconiBot"
  )
}
