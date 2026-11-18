package com.benkio.chatcore.model.media

import com.benkio.chatcore.model.media.MediaSource.FileRef
import com.benkio.chatcore.model.Platform

/** Keeps the sources of a media aligned with the file references returned by successful sends. See [[MediaSource]] for
  * how a reference is tagged with its platform.
  */
object MediaSources {

  /** How many references to keep per platform: the newest ones, followed by every URL. */
  val maxFileIds = 3

  /** Puts the new reference first for its platform, removes duplicates of that platform, keeps the other platforms'
    * references and every URL, and caps each platform's references at [[maxFileIds]].
    */
  def withFileId(sources: List[MediaSource], platform: Platform, id: String): List[MediaSource] = {
    val clean = id.trim
    if clean.isEmpty then sources
    else {
      val own    = sources.collect { case FileRef(`platform`, ref) => ref }
      val others = sources.collect { case ref @ FileRef(other, _) if other != platform => ref }
      val urls   = sources.filter(_.isUrl)
      val newest = (clean :: own.filterNot(_ == clean)).take(maxFileIds)
      newest.map(FileRef(platform, _)) ++ urls ++ others
    }
  }

  /** Drops the failed reference for `platform` and puts the fresh one in its place. Reuses [[withFileId]] so dedupe,
    * the per-platform cap and the ordering of URLs and other platforms' references stay identical.
    */
  def replaceFileId(
      sources: List[MediaSource],
      platform: Platform,
      failedId: String,
      newId: String
  ): List[MediaSource] =
    withFileId(sources = sources.filterNot(_ == FileRef(platform, failedId.trim)), platform = platform, id = newId)
}
