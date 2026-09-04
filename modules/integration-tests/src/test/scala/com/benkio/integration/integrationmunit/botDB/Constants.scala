package com.benkio.integration.integrationmunit.botDB

trait Constants {

  val dbName: String = "botDB.sqlite3"
  // `getClass.getResource("/")` used to resolve this project's own resources directory, but
  // sbt 2's forked test worker process resolves it to the worker's own launcher jar instead.
  // Read the build-injected paths (see IntegrationSettings) rather than guessing them.
  val repoRoot: String =
    sys.props.getOrElse("sbots.repoRoot", sys.error("System property sbots.repoRoot is not set"))
  val integrationResourcesPath: String = sys.props.getOrElse(
    "sbots.integrationTestResourcesDirectory",
    sys.error("System property sbots.integrationTestResourcesDirectory is not set")
  )
  val mainResourcesPath: String = s"$repoRoot/modules/botDB/src/main/resources/"
  val dbPath: String            = s"$repoRoot/modules/integration-tests/target/$dbName"
  val dbUrl: String             = s"jdbc:sqlite:$dbPath"

  val testApplicationConf: String      = "application.test.conf"
  val youTubeTokenFilename             = "youTubeApiKey.token"
  val youTubeTokenFilenamePath: String = s"$mainResourcesPath$youTubeTokenFilename"
}
