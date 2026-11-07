package com.benkio.chatcore.model

/** The parsed input of `/subscribe <6-field cron> [type [target...]]`. */
final case class SubscribeArguments(
    cron: String,
    subscriptionType: SubscriptionType,
    target: Option[String]
)

object SubscribeArguments {

  final case class UnsupportedSubscriptionType(typeToken: String, supported: List[SubscriptionType])
      extends Throwable(
        s"Unsupported subscription type '$typeToken'. Supported types: ${supported.map(_.asString).mkString(", ")}"
      )

  /** The types a user can create with `/subscribe`. */
  val supportedTypes: List[SubscriptionType] = List(SubscriptionType.Show)

  private val cronFields = 6

  def parse(input: String): Either[Throwable, SubscribeArguments] = {
    // limit = cron fields + type + the whole target (which keeps its inner spaces)
    val (cronTokens, rest) = input.trim.split("\\s+", cronFields + 2).toList.splitAt(cronFields)
    val cron               = cronTokens.mkString(" ")
    rest match {
      case Nil                       => Right(SubscribeArguments(cron, SubscriptionType.Show, None))
      case typeToken :: targetTokens =>
        for {
          subscriptionType <- SubscriptionType
            .fromString(typeToken)
            .filterOrElse(supportedTypes.contains, ())
            .left
            .map(_ => UnsupportedSubscriptionType(typeToken, supportedTypes))
          target = targetTokens.headOption.map(_.trim).filter(_.nonEmpty)
          _ <- SubscriptionRules.validateTarget(target)
        } yield SubscribeArguments(cron, subscriptionType, target)
    }
  }
}
