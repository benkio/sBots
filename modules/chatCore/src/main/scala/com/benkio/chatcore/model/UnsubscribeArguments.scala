package com.benkio.chatcore.model

import java.util.UUID
import scala.util.Try

/** The parsed input of `/unsubscribe [uuid | type]`. */
enum UnsubscribeArguments {
  case All
  case ById(subscriptionId: SubscriptionId)
  case ByType(subscriptionType: SubscriptionType)
}

object UnsubscribeArguments {

  final case class InvalidUnsubscribeInput(input: String)
      extends Throwable(
        s"Invalid input '$input': expected a subscription UUID or a type (${SubscriptionType.values.map(_.asString).mkString(", ")})"
      )

  def parse(input: String): Either[Throwable, UnsubscribeArguments] = {
    val trimmed = input.trim
    if trimmed.isEmpty then Right(All)
    else
      Try(UUID.fromString(trimmed)).toOption
        .map(uuid => ById(SubscriptionId(uuid)))
        .orElse(SubscriptionType.fromString(trimmed).toOption.map(ByType(_)))
        .toRight(InvalidUnsubscribeInput(trimmed))
  }
}
