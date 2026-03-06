package com.benkio.integration

import cats.effect.IO
import cats.effect.Resource
import com.benkio.chatcore.config.SBotConfig
import com.benkio.chatcore.http.DropboxClient
import com.benkio.chatcore.initialization.BotSetup
import com.benkio.chatcore.repository.db.DBRepository
import com.benkio.chatcore.repository.JsonDataRepository
import com.benkio.chatcore.TelegramBackgroundJobManager
import com.benkio.integrationtest.Logger.given
import munit.*
import org.http4s.ember.client.*
import org.http4s.implicits.*
import telegramium.bots.high.BotApi

import _root_.log.effect.LogWriter

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

  val log: LogWriter[IO] = summon[LogWriter[IO]]

  lazy val botSetupFixture: FunFixture[BotSetupFixtureResources] = FunFixture[BotSetupFixtureResources](
    setup = testOptions =>
      BotSetupFixture.fixtureSetup(sBotConfig = botSetupFixtureConfig, testOptions = testOptions)(using
        log
      ),
    teardown = BotSetupFixture.teardownFixture
  )
}

object BotSetupFixture {

  def fixtureSetup(
      sBotConfig: SBotConfig,
      testOptions: TestOptions
  )(using log: LogWriter[IO]): BotSetupFixtureResources = {
    val dbRes    = DBFixture.fixtureSetup(testOptions)
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
        TelegramBackgroundJobManager[IO](
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
      jsonDataRepository = JsonDataRepository[IO](),
      dbLayer = dbLayer,
      backgroundJobManager = backgroundJobManager,
      api = api,
      webhookUri = webhookUri,
      webhookPath = webhookPath,
      sBotConfig = sBotConfig
    )
}
