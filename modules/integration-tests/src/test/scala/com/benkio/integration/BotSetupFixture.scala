package com.benkio.integration

import cats.effect.IO
import cats.effect.Resource
import com.benkio.telegrambotinfrastructure.config.SBotConfig
import com.benkio.telegrambotinfrastructure.http.DropboxClient
import com.benkio.telegrambotinfrastructure.initialization.BotSetup
import com.benkio.telegrambotinfrastructure.repository.db.DBRepository
import com.benkio.telegrambotinfrastructure.repository.JsonRepliesRepository
import com.benkio.telegrambotinfrastructure.BackgroundJobManager
import log.effect.LogWriter
import munit.*
import org.http4s.ember.client.*
import org.http4s.implicits.*
import telegramium.bots.high.BotApi

/** Test fixture that builds a real BotSetup as close as possible to main: real DBLayer (from DBFixture), DBRepository,
  * DropboxClient, Ember httpClient, BackgroundJobManager, JsonRepliesRepository. Uses a stub token and skips webhook
  * delete. Use `botSetupResource.use { botSetup => ... }` in tests.
  */
final case class BotSetupFixtureResources(
    dbResources: DBFixtureResources,
    botSetupResource: Resource[IO, BotSetup[IO]]
)

trait BotSetupFixture extends DBFixture { self: FunSuite =>

  /** Override to use a different SBotConfig for the fixture. */
  def botSetupFixtureConfig: SBotConfig

  lazy val botSetupFixture: FunFixture[BotSetupFixtureResources] = FunFixture[BotSetupFixtureResources](
    setup = testOptions =>
      BotSetupFixture.fixtureSetup(dbFixture = this, sBotConfig = botSetupFixtureConfig, testOptions = testOptions)(
        using log
      ),
    teardown = BotSetupFixture.teardownFixture
  )
}

object BotSetupFixture {

  def fixtureSetup(
      dbFixture: DBFixture,
      sBotConfig: SBotConfig,
      testOptions: TestOptions
  )(using log: LogWriter[IO]): BotSetupFixtureResources = {
    val dbRes    = DBFixture.fixtureSetup(testOptions)(using dbFixture.log)
    val resource = botSetupResource(dbRes, sBotConfig)
    BotSetupFixtureResources(
      dbResources = dbRes,
      botSetupResource = resource
    )
  }

  def teardownFixture(fixture: BotSetupFixtureResources): Unit =
    DBFixture.teardownFixture(fixture.dbResources)

  /** Builds a Resource[IO, BotSetup[IO]] using the same building blocks as main: real DBLayer, Ember httpClient,
    * DropboxClient, DBRepository, BackgroundJobManager, JsonRepliesRepository, BotApi.
    */
  def botSetupResource(
      dbRes: DBFixtureResources,
      sBotConfig: SBotConfig
  )(using log: LogWriter[IO]): Resource[IO, BotSetup[IO]] =
    for {
      dbLayer       <- dbRes.resourceDBLayer
      httpClient    <- EmberClientBuilder.default[IO].withMaxResponseHeaderSize(8192).build
      dropboxClient <- Resource.eval(DropboxClient[IO](httpClient))
      repository = DBRepository.dbResources[IO](
        dbMedia = dbLayer.dbMedia,
        dropboxClient = dropboxClient
      )
      token   = "test"
      baseUrl = s"https://api.telegram.org/bot$token"
      api     = BotApi(httpClient, baseUrl)
      backgroundJobManager <- Resource.eval(
        BackgroundJobManager[IO](
          dbLayer = dbLayer,
          sBotInfo = sBotConfig.sBotInfo,
          ttl = sBotConfig.messageTimeToLive
        )(using IO.asyncForIO, api, log)
      )
      webhookPath = uri"/test"
      webhookUri  = uri"https://localhost/test"
    } yield BotSetup(
      token = token,
      httpClient = httpClient,
      repository = repository,
      jsonRepliesRepository = JsonRepliesRepository[IO](),
      dbLayer = dbLayer,
      backgroundJobManager = backgroundJobManager,
      api = api,
      webhookUri = webhookUri,
      webhookPath = webhookPath,
      sBotConfig = sBotConfig
    )
}
