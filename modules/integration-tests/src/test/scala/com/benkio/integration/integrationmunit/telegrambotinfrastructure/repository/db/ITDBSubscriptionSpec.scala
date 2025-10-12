package com.benkio.integration.integrationmunit.telegrambotinfrastructure.repository.db

import cats.effect.IO
import cats.effect.Resource
import com.benkio.integration.DBFixture
import com.benkio.telegrambotinfrastructure.repository.db.DBSubscription
import com.benkio.telegrambotinfrastructure.repository.db.DBSubscriptionData
import doobie.munit.analysisspec.IOChecker
import doobie.Transactor
import munit.CatsEffectSuite

import java.sql.DriverManager
import java.util.UUID

class ITDBSubscriptionSpec extends CatsEffectSuite with DBFixture with IOChecker {

  val botId                      = "botname"
  val testSubscriptionId: String = "83beabe1-cad9-4845-a838-65bbed34bc46"

  val testSubscription: DBSubscriptionData = DBSubscriptionData(
    id = testSubscriptionId,
    chat_id = 2L,
    bot_id = botId,
    cron = "0 0 0 12 4 *",
    subscribed_at = "2022-11-06T19:54:46Z"
  )

  override def transactor: doobie.Transactor[cats.effect.IO] = {
    Class.forName("org.sqlite.JDBC")
    val conn = DriverManager.getConnection(DBFixture.dbUrl)
    DBFixture.runMigrations(DBFixture.dbUrl, DBFixture.migrationTable, DBFixture.migrationPath)
    val transactor = Transactor.fromConnection[IO](conn, None)
    transactor
  }

  test(
    "DBSubscription queries should check".ignore
  ) {
    check(DBSubscription.getSubscriptionsQuery(botId))
    check(DBSubscription.insertSubscriptionQuery(testSubscription))
    check(DBSubscription.deleteSubscriptionQuery(testSubscriptionId))
    check(DBSubscription.getSubscriptionQuery(testSubscriptionId))
    check(DBSubscription.getRandomSubscriptionQuery())
    check(DBSubscription.deleteSubscriptionsQuery(2L))
  }

  databaseFixture.test(
    "DBSubscription: given a subscription, insertSubscription should insert the subscription then getSubscriptions should return the subscription and deleteSubscription should delete it"
  ) { fixture =>
    val resourceAssert = for {
      dbSubscription <- fixture.resourceDBLayer.map(_.dbSubscription)
      _              <- Resource.eval(dbSubscription.insertSubscription(testSubscription))
      subscriptions1 <- Resource.eval(dbSubscription.getSubscriptions(botId))
      _              <- Resource.eval(dbSubscription.deleteSubscription(UUID.fromString(testSubscription.id)))
      subscriptions2 <- Resource.eval(dbSubscription.getSubscriptions(botId))
    } yield (subscriptions1, subscriptions2)

    resourceAssert.use { case (preSubscriptions, postSubscriptions) =>
      IO {
        assert(preSubscriptions.length == 1)
        assert(preSubscriptions.head == testSubscription)
        assert(postSubscriptions.isEmpty)
      }
    }
  }

  databaseFixture.test(
    "DBSubscription.getRandomSubscription should return a subscription when called"
  ) { fixture =>
    val resourceAssert = for {
      dbSubscription <- fixture.resourceDBLayer.map(_.dbSubscription)
      _              <- Resource.eval(dbSubscription.insertSubscription(testSubscription))
      result         <- Resource.eval(dbSubscription.getRandomSubscription())
    } yield assertEquals(result, Some(testSubscription))
    resourceAssert.use_
  }

}
