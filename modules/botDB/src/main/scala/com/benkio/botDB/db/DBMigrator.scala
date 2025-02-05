package com.benkio.botDB.db

import cats.effect.Sync
import com.benkio.botDB.config.Config
import org.flywaydb.core.Flyway
import org.flywaydb.core.api.Location
import org.flywaydb.core.api.configuration.FluentConfiguration

import scala.jdk.CollectionConverters.*

trait DBMigrator[F[_]] {
  def migrate(config: Config): F[Int]
}

object DBMigrator {

  def apply[F[_]: Sync]: DBMigrator[F] = new DBMigrator[F] {
    def migrate(config: Config): F[Int] =
      Sync[F].delay {
        println(
          "Running migrations from locations: " +
            config.migrationsLocations.mkString(", ")
        )
        val count = unsafeMigrate(config)
        println(s"Executed $count migrations")
        count
      }
  }

  def unsafeMigrate(config: Config): Int = {
    val m: FluentConfiguration = Flyway.configure
      .dataSource(
        config.url,
        "",
        ""
      )
      .group(true)
      .mixed(true)
      .outOfOrder(false)
      .table(config.migrationsTable)
      .validateOnMigrate(true)
      .failOnMissingLocations(true)
      .locations(
        config.migrationsLocations
          .map(new Location(_))
          .toList*
      )
      .baselineOnMigrate(true)

    logValidationErrorsIfAny(m)
    m.load().migrate().migrationsExecuted
  }

  private def logValidationErrorsIfAny(m: FluentConfiguration): Unit = {
    val validated = m
      .ignoreMigrationPatterns("*:pending")
      .load()
      .validateWithResult()

    if (!validated.validationSuccessful)
      for (error <- validated.invalidMigrations.asScala)
        println(s"""
                   |Failed validation:
                   |  - version: ${error.version}
                   |  - path: ${error.filepath}
                   |  - description: ${error.description}
                   |  - errorCode: ${error.errorDetails.errorCode}
                   |  - errorMessage: ${error.errorDetails.errorMessage}
        """.stripMargin.strip)
  }
}
