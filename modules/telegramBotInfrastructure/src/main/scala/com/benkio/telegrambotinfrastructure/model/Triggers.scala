package com.benkio.telegrambotinfrastructure.model

import wolfendale.scalacheck.regexp.RegexpGen
import org.scalacheck.Gen
import org.scalacheck.rng.Seed
import cats.effect.IO
import cats.implicits.*
import cats.Show
import io.circe.*
import io.circe.generic.semiauto.*

import scala.util.matching.Regex

///////////////////////////////////////////////////////////////////////////////
//                                   Errors                                  //
///////////////////////////////////////////////////////////////////////////////

final case class RegexTextTriggerValueLengthNotFound(regexTextTriggerValue: RegexTextTriggerValue) extends Throwable(s"Usable to calculate the length for RegexTextTriggerValue: $regexTextTriggerValue")

///////////////////////////////////////////////////////////////////////////////
//                   Extensionns and Basic Type Enrichment                   //
///////////////////////////////////////////////////////////////////////////////

extension (sc: StringContext) def stt(args: Any*): StringTextTriggerValue = StringTextTriggerValue(sc.s(args*))
extension (r: Regex)
  def tr(minimalLengthMatch: Int): RegexTextTriggerValue = RegexTextTriggerValue(r, minimalLengthMatch)
extension (textTriggerValue: TextTriggerValue)
  def isStringTriggerValue: Boolean = textTriggerValue match {
    case RegexTextTriggerValue(_, _) => false
    case StringTextTriggerValue(_)   => true
  }
extension (regexTextTriggerValue: RegexTextTriggerValue) {
  def length1: IO[Int] = {
    def canGenerateSize(
        targetSize: Int,
        trials: Int = 100
    ): Boolean = {
      val params = Gen.Parameters.default.withSize(targetSize)
      (1 to trials).exists { _ =>
        RegexpGen
          .from(regexTextTriggerValue.trigger.toString)
          .apply(params, Seed.random())
          .exists(v => v.length == targetSize)
      }
    }

    IO.fromOption(
      (0 to 100).find { size =>
        canGenerateSize(size)
      }
    )(RegexTextTriggerValueLengthNotFound(regexTextTriggerValue))
  }
}


given Decoder[Regex] = Decoder.decodeString.map(_.r)
given Encoder[Regex] = Encoder.encodeString.contramap[Regex](_.toString())

///////////////////////////////////////////////////////////////////////////////
//                              TextTriggerValue                             //
///////////////////////////////////////////////////////////////////////////////
sealed trait TextTriggerValue {
  def length: Int
}

object TextTriggerValue {

  def matchValue(trigger: TextTriggerValue, source: String): Boolean = trigger match {
    case RegexTextTriggerValue(v, _) => v.findFirstMatchIn(source).isDefined
    case StringTextTriggerValue(v)   => source `contains` v
  }

  given orderingInstance: Ordering[TextTriggerValue] = new Ordering[TextTriggerValue] {
    def compare(triggerValue1: TextTriggerValue, triggerValue2: TextTriggerValue) =
      triggerValue1.length.compare(triggerValue2.length)
  }
  given showInstance: Show[TextTriggerValue] = Show.show(ttv =>
    ttv match {
      case StringTextTriggerValue(t)   => t
      case RegexTextTriggerValue(t, _) => t.toString
    }
  )

  given Encoder[StringTextTriggerValue] = Encoder[StringTextTriggerValue](using sttv => Json.fromString(sttv.trigger))
  given Decoder[StringTextTriggerValue] = Decoder.decodeString.map(StringTextTriggerValue(_))
  given Decoder[TextTriggerValue]       = deriveDecoder[TextTriggerValue]
  given Encoder[TextTriggerValue]       = deriveEncoder[TextTriggerValue]

  def fromStringOrRegex(v: String | RegexTextTriggerValue): TextTriggerValue = v match {
    case s: String                => StringTextTriggerValue(s)
    case r: RegexTextTriggerValue => r
  }

  def isRegex(textTriggerValue: TextTriggerValue): Boolean = textTriggerValue match {
    case RegexTextTriggerValue(_, _) => true
    case _                           => false
  }
}

case class StringTextTriggerValue(trigger: String) extends TextTriggerValue {
  override def length: Int = trigger.length
}
case class RegexTextTriggerValue(trigger: Regex, minimalLengthMatch: Int) extends TextTriggerValue {
  override def length: Int = minimalLengthMatch
}

///////////////////////////////////////////////////////////////////////////////
//                                  Trigger                                   //
///////////////////////////////////////////////////////////////////////////////

sealed trait Trigger

sealed trait MessageTrigger extends Trigger

case class TextTrigger(triggers: TextTriggerValue*) extends MessageTrigger
case class MessageLengthTrigger(messageLength: Int) extends MessageTrigger
case object NewMemberTrigger                        extends MessageTrigger
case object LeftMemberTrigger                       extends MessageTrigger
case class CommandTrigger(command: String)          extends Trigger

object Trigger {

  given showInstance: Show[Trigger] = Show.show(t =>
    t match {
      case TextTrigger(tvs*)       => tvs.map(_.show).mkString("\n")
      case MessageLengthTrigger(l) => s"trigger when the length of message exceed $l"
      case NewMemberTrigger        => "trigger on new member joining a group"
      case LeftMemberTrigger       => "trigger when a member leaves a group"
      case CommandTrigger(c)       => c
    }
  )

  given orderingInstance: Ordering[Trigger] = new Ordering[Trigger] {
    def compare(trigger1: Trigger, trigger2: Trigger) =
      triggerLongestString(trigger1).compare(triggerLongestString(trigger2))
  }

  given Decoder[MessageTrigger] = deriveDecoder[MessageTrigger]
  given Encoder[MessageTrigger] = deriveEncoder[MessageTrigger]

  def triggerLongestString(trigger: Trigger): Int = trigger match {
    case TextTrigger(lt*)          => lt.max(using TextTriggerValue.orderingInstance).length
    case MessageLengthTrigger(_)   => 0
    case _: NewMemberTrigger.type  => 0
    case _: LeftMemberTrigger.type => 0
    case CommandTrigger(c)         => c.length
  }
}
