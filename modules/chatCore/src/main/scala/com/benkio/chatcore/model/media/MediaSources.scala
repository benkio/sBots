package com.benkio.chatcore.model.media

import com.benkio.chatcore.model.Platform
import org.http4s.Uri

/** Keeps the sources of a media aligned with the file references returned by successful sends.
  *
  * A reference is only useful to the platform that produced it and to the bot it was produced for, so it is stored as
  * `<platform.id>:<id>`. Untagged references are not accepted: the repository data is migrated to the tagged form (see
  * `scripts/MigrateFileIdSources.scala`), and a reference without a valid tag is simply unusable.
  */
object MediaSources {

  /** How many references to keep per platform: the newest ones, followed by every URL. */
  val maxFileIds = 3

  private val separator = ':'

  private def split(reference: String): (String, Option[String]) = {
    val index = reference.indexOf(separator)
    if index < 0 then (reference, None)
    else (reference.substring(0, index), Some(reference.substring(index + 1)))
  }

  def tagged(platform: Platform, id: String): String = s"${platform.id}$separator${id.trim}"

  /** Whether a stored reference is a valid tag for `platform`. Untagged or unknown tags match no platform. */
  def belongsTo(reference: String, platform: Platform): Boolean =
    split(reference) match {
      case (tag, Some(_)) => tag == platform.id
      case _              => false
    }

  /** The platform-specific part of a tagged reference. Callers only pass references that belong to their platform. */
  def rawId(reference: String): String =
    split(reference)._2.getOrElse(
      throw new IllegalArgumentException(s"File reference '$reference' has no platform tag")
    )

  /** Puts the new reference first for its platform, removes duplicates of that platform, keeps the other platforms'
    * tagged references and every URL, and caps each platform's references at [[maxFileIds]]. Malformed (untagged)
    * references are dropped.
    */
  def withFileId(
      sources: List[Either[String, Uri]],
      platform: Platform,
      id: String
  ): List[Either[String, Uri]] = {
    val clean = id.trim
    if clean.isEmpty then sources
    else {
      val own    = sources.collect { case Left(ref) if belongsTo(ref, platform) => rawId(ref) }
      val others = sources.collect {
        case Left(ref) if split(ref)._2.isDefined && !belongsTo(ref, platform) => Left(ref): Either[String, Uri]
      }
      val urls   = sources.collect { case Right(uri) => Right(uri): Either[String, Uri] }
      val newest = (clean :: own.filterNot(_ == clean)).take(maxFileIds)
      newest.map(newId => Left(tagged(platform, newId)): Either[String, Uri]) ++ urls ++ others
    }
  }
}
