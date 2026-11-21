package com.benkio.replieseditor.server.load

import com.benkio.chatcore.paths.SBotPaths

import java.nio.file.Path

object RepoRoot {

  /** Resolves the repository root through the shared explicit path configuration. */
  def detect(start: Path = SBotPaths.workingDirectory): Path =
    SBotPaths.repoRootFrom(start)
}
